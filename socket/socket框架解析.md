## 转自imooc，邱老师的socket视频<https://coding.imooc.com/class/286.html>

### 框架支持的功能

1. 客户端通过发送udp广播，服务端接收到广播，回送tcp服务端的ip和端口，建立socket连接
2. 客户端可以单点发送文本，群发文本
3. 支持客户端和服务端互传文件，且可以响应中断传输，但是不中断链接
4. 支持客户端与客户端之间传输语音数据

### 框架主体介绍

* IoContext：全局上下文，含有一个Scheduler，IoProvder的实现。
* IoProvider：socketChannel注册到这个provider，当channel就绪（当前可读或可写）时，提供回调，进行读写。全局一份，存放再IoContext中。
* IoArgs：封装ByteBuffer，并提供对channel的读写。
* Frame：从channel中每次接收或发送的数据帧。一个packet可能会被拆成多个Frame进行发送。
* Packet：一份Packet，表示业务层，想（发送|接收）的一个包，可以是一段文字，一个文件，一个语音连接。
* Connector：表示一个链接，即服务端，和客户端。提供发送，以及接收到数据时给出回调。当有语音传输时，负责绑定语音传输的channel。
* SocketChannelAdatper：实现了sender和receiver，将当前channel注册到IoProvider中，当channel就绪时调用callback，回调到dispatcher中进行发送与接收。
* SenderDispatcher/ReceiveDispatcher：对packet进行分发，内部调用PacketReader/PacketWriter进行packet的消费，并从其中提供IoArgs给注册在IoProvider的回调中进行消费
* PackerReader/PackerWriter：从Dispatcher中获取到Packet，拆分成Frame，回送给Dispatcher IoArgs
* Scheduler：执行或者延时执行任务。
* ConnectorHandlerChain：消费消息的链式结构。对到达的消息，进行链式分发进行消费。
* ConnectorHandler：继承Connector，提供字符chain头和close的头，并提供当文件，或者直播流到达时的outputStream去接收对应的数据。

### 流程分析

#### 服务端启动流程

1. 初始化IoContext

   * 初始化IoProvider

     提供读写Selector，启动两个最高级权限的线程，对读写selector进行调度。

     ![image.png](https://i.loli.net/2019/11/21/h27FQjlgDuNLwGC.png)

     这里有一个操作，我们拿到就绪的channel后，handleSelection时，需要将SelectionKey的interestOps对应的操作位取反。表示不再关注此类型事件。当消费完成后，按需再次修改SelectionKey的interestOps

   * 初始化Scheduler

     提供两个线程池执行任务

     ```java
     public ScheduleImpl(int poolSize) {
         //可执行计划任务的线程池
         this.scheduledService = Executors.newScheduledThreadPool(poolSize,
                 new NameableThreadFactory("scheduled-thread-pool"));
         //固定大小的线程池，常规任务
         this.deliveryService = Executors.newFixedThreadPool(4,
                 new NameableThreadFactory("delivery-thread-pool"));
     }
     ```

2. 启动TCPServer

   bind对应端口。启动ServerAccptor线程监听接受新的客户端到达。

   当新的客户端连接到达后，添加StringPacketChain和ConnectorCloseChain，以及启动心跳。

3. 启动UDPProvider

   监听UDP端口，如果接受到客户的广播，判断合法性，并回送TCPServer的port。

4. 监听控制台输入

   监听控制台的输入，通过tcpServer进行发送消息

#### 客户端启动流程

1. 发送广播，询问到TCPServer的port

   发送特定广播信息，等待UDPProvider回应服务端的address和port

2. 启动IoContext

   如同服务端

3. 启动TcpClient

   拿到服务器address和port进行连接，连接成功后，添加StringPacketChain和ConnectorCloseChain，以及启动心跳。

#### 发送流程解析



#### 接收流程解析



## 关键类介绍

### IoArgs，Frame和Packet

```java
/**
 * 对ByteBuffer的包装类
 */
public class IoArgs {
    //是否需要消费所有空间
    private final boolean isNeedConsumeReaming;
    //单次操作的最大空间
    private int limit;
    private final ByteBuffer byteBuffer;
    public IoArgs(int size, boolean isNeedConsumeReaming) {
        this.limit = size;
        this.isNeedConsumeReaming = isNeedConsumeReaming;
        this.byteBuffer = ByteBuffer.allocate(size);
    }
    /**
     * 读取数据到当前byteBuffer中
     */
    public int readFrom();
    /**
     * 从当前byteBuffer中写出数据
     */
    public int writeTo();
    /**
     * IoArgs 提供者、处理者
     */
    public interface IoArgsEventProcessor {
        /**
         * 提供一份可供消费的IoArgs
         * @return IoArgs
         */
        IoArgs provideIoArgs();

        /**
         * 消费失败时回调
         * @param throwable 异常
         * @return 是否关闭链接，True为关闭链接
         */
        boolean onConsumeFailed(Throwable throwable);

        /**
         * 消费完成回调
         * @param ioArgs 数据
         * @return 是否继续注册下一次消费。true为注册调度下一次消费
         */
        boolean onConsumeComplete(IoArgs ioArgs);
    }
}
```

```java
/**
 * 帧-分片使用
 */
public abstract class Frame {
    //单帧最大容量 64k
    public static final int MAX_CAPACITY = 64 * 1024 - 1;
    //帧头的长度
    public static final int FRAME_HEADER_LENGTH = 6;
    //头部信息
    protected final byte[] header = new byte[FRAME_HEADER_LENGTH];
    //PACKET的头帧
    public static final byte TYPE_PACKET_HEADER = 11;
    //PACKET的body帧
    public static final byte TYPE_PACKET_ENTITY = 12;
    //指令，发送方取消
    public static final byte TYPE_COMMAND_SEND_CANCEL = 41;
    //指令，接收方拒绝
    public static final byte TYPE_COMMAND_RECEIVE_REJECT = 42;
    //指令，心跳包
    public static final byte TYPE_COMMAND_HEARTBEAT = 81;
    //无任何标记
    public static final byte FLAG_NONE = 0;

    /**
     * @param length     Frame的长度
     * @param type       frame的类型
     * @param flag
     * @param identifier frame的标识
     */
    public Frame(int length, byte type, byte flag, short identifier) {
        if (length < 0 || length > MAX_CAPACITY) {
            throw new RuntimeException("Frame的长度不合法！");
        }
        if (identifier < 1 || identifier > 255) {
            throw new RuntimeException("！");
        }
        //设置frame的长度
        header[0] = (byte) (length >> 8);
        header[1] = (byte) length;

        header[2] = type;
        header[3] = flag;

        header[4] = (byte) identifier;
        //预留位
        header[5] = 0;
    }
    /*
     * 获取frame长度
     */
    public int getBodyLength();
    /*
     * 获取frame的类型
     */
    public byte getBodyType();
	/*
     * 获取frame的flag
     */
    public byte getBodyFlag();
	/*
     * 获取frame的标识
     */
    public short getBodyIdentifier();
    /**
     * 进行数据读或写操作
     *
     * @param args 数据
     * @return 是否已消费完全， True：则无需再传递数据到Frame或从当前Frame读取数据
     */
    public abstract boolean handle(IoArgs args) throws IOException;
    /**
     * 基于当前帧尝试构建下一份待消费的帧
     *
     * @return NULL：没有待消费的帧
     */
    public abstract Frame nextFrame();
    /**
     * 获取可消费的长度
     */
    public abstract int getConsumableLength();
}
```

```java
/**
 * 公共的数据封装
 * 提供了类型以及基本的长度的定义
 */
public abstract class Packet<Stream extends Closeable> implements Closeable {
    /**
     * 最大包大小，5个字节满载组成的Long类型
     */
    public static final long MAX_PACKET_SIZE = (((0xFFL) << 32) | ((0xFFL) << 24) | ((0xFFL) << 16) | ((0xFFL) << 8) | ((0xFFL)));

    // BYTES 类型
    public static final byte TYPE_MEMORY_BYTES = 1;
    // String 类型
    public static final byte TYPE_MEMORY_STRING = 2;
    // 文件 类型
    public static final byte TYPE_STREAM_FILE = 3;
    // 长链接流 类型
    public static final byte TYPE_STREAM_DIRECT = 4;

    protected long length;
    private Stream stream;

    public long length() {
        return length;
    }

    /**
     * 对外的获取当前实例的流操作
     *
     * @return {@link java.io.InputStream} or {@link java.io.OutputStream}
     */
    public final Stream open() {
        if (stream == null) {
            stream = createStream();
        }
        return stream;
    }
    /**
     * 对外的关闭资源操作，如果流处于打开状态应当进行关闭
     *
     * @throws IOException IO异常
     */
    @Override
    public final void close() throws IOException {
        if (stream != null) {
            closeStream(stream);
            stream = null;
        }
    }
    /**
     * 类型，直接通过方法得到:
     * <p>
     * {@link #TYPE_MEMORY_BYTES}
     * {@link #TYPE_MEMORY_STRING}
     * {@link #TYPE_STREAM_FILE}
     * {@link #TYPE_STREAM_DIRECT}
     *
     * @return 类型
     */
    public abstract byte type();
    /**
     * 创建流操作，应当将当前需要传输的数据转化为流
     *
     * @return {@link java.io.InputStream} or {@link java.io.OutputStream}
     */
    protected abstract Stream createStream();
    /**
     * 关闭流，当前方法会调用流的关闭操作
     *
     * @param stream 待关闭的流
     * @throws IOException IO异常
     */
    protected void closeStream(Stream stream) throws IOException {
        stream.close();
    }
    /**
     * 头部额外信息，用于携带额外的校验信息等
     *
     * @return byte 数组，最大255长度
     */
    public byte[] headerInfo() {
        return null;
    }
}
```

发送(SenderDispatcher|PacketReader)：SendPacket->SendFrame->IoArg->Channel

接收：channel->IoArgs->ReceiveFrame->ReceivePacket

### IoSelectorProvider

