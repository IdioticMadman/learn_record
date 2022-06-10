Kotlin协程语言级别支持

* Kotlin标准库
* 协程上下文
* 拦截器
* 挂在函数

Kotlin协程框架级别支持

* Job
* 调度器
* 作用域
* Channel
* Flow
* Select



### 简介

Kotlinx.coroutines

* 官方协程框架，基于标准库实现的特性封装
* https://github.com/kotlin/kotlinx.coroutines

### 引入

```groovy
//标准库
implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
//协程库基础库
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutine_version"
//协程A ndroid库，提供Android UI调度器
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutine_version"
//协程Swing库，提供Swing UI调度器
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutine_version"
```

### 组件

* 协程Builder， launch、async、produce创建协程，以及对应启动模式
* 调度器，CPU密集型(Default)，IO密集型(IO)，UI等
* 异常处理，异常处理器，异常传递，全局异常处理器(通过ServiceLoader进行加载)
* 作用域，顶级，主从，协同
* Channel，Flow，Select

### 启动模式

```kotlin
GlobalScope.launch(start = CoroutineStart.DEFAULT){
  log(1)
  delay(1000L)
  log(2)
}
```

| 启动模式     | 功能特性                                                     |
| ------------ | ------------------------------------------------------------ |
| DEFAULT      | 立即开始调度协程体，调度前若取消则直接取消                   |
| ATOMIC       | 立即开始调度协程体，且第一个挂起点前不能取消                 |
| LAZY         | 只有在需要的 (start/join/await)时开始调度                    |
| UNDISPATCHED | 立即在当前线程执行协程体，直到遇到第一个挂起点(后面取决于调度器) |

### 调度器

| 调度器     | Java VM                      |
| ---------- | ---------------------------- |
| Default    | 线程池（CPU密集型）          |
| Main       | UI线程                       |
| Unconfined | 直接执行                     |
| IO         | 线程池（IO密集型，无限队列） |

### 特性

* Channel: "热"数据流，并发安全的通信机制。

  非阻塞的通信基础设施，类似于BlockingQueue+挂起函数

  两个挂起函数：Send()  Receive()

  | 分类       | 描述                                           |
  | ---------- | ---------------------------------------------- |
  | RENDEZVOUS | 不见不散，send调用后挂起直到receive到达        |
  | UNLIMITED  | 无限容量，send调用直接返回                     |
  | CONFLATED  | 保留最新，receive只能获取最近一次send的值      |
  | BUFFERED   | 默认容量，可通过程序参数设置默认大小，默认为64 |
  | FIXED      | 固定容量，通过参数执行缓存大小                 |

* Flow: "冷"数据流，协程的响应式API。

* Select: 可以多个挂起事件进行等待

