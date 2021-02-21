# Handler 机制

## ThreadLocal

ThreadLocal 是 Thread对象中一个 map 对象中保存的一个key，有与之对应的 Value 即我们保存的值。

对于同一个 Thread，可以有 new多个 ThreadLocal 对象，来存储多个值

对于多个 Thread，同一个 threadLocal，get 出来的值是不同的，是因为要从 Thread 对象中的一个 map，以 threadLocal 为 key，来获取的 value。



## 初始化

1. Loop.prepare() 在ThreadLocal中存下当前Thread对应的looper对象。
2. Looper的构造方法中会触发MessageQueue的初始化，包括native的初始化

## 发送消息

1. Handler初始化，获取当前线程中保存的Looper对象，以及MessageQueue
2. 通过handler发送消息最后都会进入，sendMessageAtTime()， 即会调用MessageQueue.enqueue()
3. 入队列的时候，会根据msg.when进行排队。进入队列后，会判断是否唤醒looper.next()的阻塞

## 处理消息

1. 从Looper.loop中开始启动事件循环，可能阻塞在queue.next()（此方法返回一个message）中
2. queue.next() 会根据当前的message的when来判断是否需要阻塞，还是返回当前消息
3. Loop从MessageQueue中取到消息之后，dispatchMessage来分发消息

## 同步栅栏

1. 调用MessageQueue的postSyncBarrier() 会根据when向当前MessageQueue添加一个没有target的Message，即为栅栏
2. 在MessageQueue.next()中，发现头部是一个栅栏，则开始寻找队列中的异步消息，优先进行处理。