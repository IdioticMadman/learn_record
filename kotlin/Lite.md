## Coroutine Lite

* launch 实现

* dispatcher线程分发，实现线程切换

* async await 实现

* cancellableContinuation 支持可取消协程

* 协程异常处理

* CoroutineScope 实现 

  * 协同式 异常时，父协程会被取消，异常会被抛至顶级协程
  * 主从式 异常时，只会发生在当前子协程，不进行上抛

  子协程都会响应父协程的取消。父协程取消，即子协程也取消。

