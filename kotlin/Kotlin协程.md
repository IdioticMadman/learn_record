### 协程要素

介绍协程相关的概念点。并梳理一下这些元素，在执行的过程中是怎么结合的。

* Continuation

* 挂起函数：函数类型、回调改写

* 协程的创建

* 协程上下文

*翻译：Continuation: 延续  coroutine: 协程  coroutineContext: 协程上下文*

#### Continuation

类似callback，也就是你在调用resume的时候，其实就是调用了resumeWith()进行callback

```kotlin
interface Continuation<in T>{
  val context: CoroutineContext
  fun resumeWith(result:Result<T>)
}

fun <T> Continuation<T>.resume(value: T) = resumeWith(Result.success(valule))
fun <T> Continuation<T>.resumeWithException(t: Throwable) = resumeWith(Result.failure(t))
```

关键点：Continuation是贯穿整个协程的载体（怎么理解？）

#### 挂起函数

* 以suspend修饰的函数

* 挂起函数只能在其他挂起函数或者协程中执行（为什么？什么导致的）

* 挂起函数调用时包含了协程“挂起”的语义

* 挂起函数返回时包含了协程“恢复”的语义

* 挂起函数类型

  ```kotlin
  suspend fun hello() // suspend ()->Unit
  suspend fun bar(a:Int):String // suspend (Int)->String
  ```

  编译后

  ```kotlin
  fun hello(continuation: Continuation<Any>):Any
  fun bar(a:Int, continuation Continuation<String>):Any
  ```

  参数的Continuation的泛型，即挂起函数的返回值。

  挂起函数的返回值类型是Any。如果没有真正挂起，即直接return或没有切换线程resume。 如果挂起的话，会返回一个标志。真正挂起一定是切换了线程，或者单线程的事件循环异步执行。

* 回调函数转成挂起函数

  * 使用suspendCoroutine获取挂起函数的Continuation
  * 回调成功调用Continuation的resume
  * 回调失败调用Continuation的resumeWithExcetption()
  
  ```kotlin
  suspend fun getUserSuspend(name: String) = suspendCoroutine<User> { continuation ->
      githubApi.getUserCallback(name).enqueue(object: Callback<User>{
          override fun onFailure(call: Call<User>, t: Throwable) =
              continuation.resumeWithException(t)
          override fun onResponse(call: Call<User>, response: Response<User>) =
              response.takeIf { it.isSuccessful }?.body()?.let(continuation::resume)
                  ?: continuation.resumeWithException(HttpException(response))
      })
  }
  ```

#### 协程创建

* 协程是一段可执行程序，即一段代码，指令集合

* 协程的创建通常需要一个函数即 suspend function

* 协程的创建的api `createCoroutine` 和`startCoroutine`

  ```kotlin
  fun <T> (suspend ()->T).createCoroutine(completion: Continuation<T>): Continuation<Unit>
  fun <R, T> (suspend R.()->T).createCoroutine(receiver:R, completion: Continuation<T>): Continuation<Unit>
  ```

  * suspend函数本身执行需要一个Continuation实例在**恢复时**进行调用，即此处参数的completion（怎么理解这个恢复时？completion的作用）
  * 返回值Continuation<Unit>则是创建出来的协程载体，receiver ，suspend函数会被传给该实例作为协程的实际执行体（协程载体？执行体？）

  ```kotlin
  suspend{
    ...
  }.createCoroutine(object: Contination<Unit> {
    override val context = EmptyCoroutineContext
    override fun resumeWith(result: Result<Unit>){
      log("Coroutine End with $result") //协程执行完后调用
    }
  }).resume(Unit) //调用resume(Unit)用于启动协程
  ```

  一般是使用`startCoroutine`在创建之后立马启动

  ```kotlin
  fun <T> (suspend ()->T).startCoroutine(completion: Continuation<T>): Continuation<Unit>
  fun <R, T> (suspend R.()->T).startCoroutine(receiver:R, completion: Continuation<T>): Continuation<Unit>
  ```

  ```kotlin
  suspend{
    ...
  }.startCoroutine(object: Contination<Unit> {
    override val context = EmptyCoroutineContext
    override fun resumeWith(result: Result<Unit>){
      log("Coroutine End with $result") //协程执行完后调用
    }
  })
  ```

  就相当于`createCoroutine`调用resume(Unit)。

  *suspend中如果有n个挂起点，总共调用多少次resume?*  (n+2 个，因为启动的时候会调用一次，结束的时候也需要调用一次)

#### 协程上下文（CoroutineContext）

* 协程执行过程中需要携带数据
* 索引是CoroutineContext.Key
* 元素是CoroutineContext.Element

#### 拦截器

* 拦截器ContinuationInterceptor是一类协程上下文元素(CoroutineContext.Element)

* 可以对协程上下文所在的协程的Continuation进行拦截，进行变换

  ```kotlin
  interface ContinuationInterceptor: CoroutineContext.Element{
    fun <T> interceptionContinuation(continuation: Continuation<T>): Continuation<T>
  }
  ```

### 协程执行简单示意

```kotlin
suspend {
	a() //函数执行体
}.startCoroutine(...)

suspend fun a() = suspendCoroutine<Unit> {
  thread{
    it.resume(Unit) //回调恢复
  }
}
```

![image-20210604094208919](https://tva1.sinaimg.cn/large/008i3skNgy1gr5zmo1677j30xi0bgq5y.jpg)

* SuspendLambda继承于Continuation，即上面 调用startCoroutine的语句块，也被叫做协程函数执行体

![image-20210604094304461](https://tva1.sinaimg.cn/large/008i3skNgy1gr5znl0tb1j314e0ja7dm.jpg)

* 如果suspendLambda中有挂起，则会用SafeContinuation包裹
  * 确保resume只被调用一次
  * 如果在当前线程调用栈上直接调用则不会挂起

![image-20210604094625555](https://tva1.sinaimg.cn/large/008i3skNgy1gr5zr2j3fxj31480jegts.jpg)

* 拦截器会被加入在SafeContinuation和SuspendLambda之间。也就是说，如果在协程函数体中，一般切换线程的话，就会加拦截器在其中。

看一个示例

```kotlin
//是在协程的scope中
Logger.debug(1)
Logger.debug(returnSuspend()) //有挂起 
Logger.debug(2)
delay(1000) //有挂起
Logger.debug(3)
Logger.debug(returnImmediately()) //直接return
Logger.debug(4)
```

转成java代码

```java
public class ContinuationImpl implements Continuation<Object>{
  private int label = 0;
  private final Continuation<Unit> completion;
  public ContinuationImpl(Continuation<Unit> completion){
    this.completion = completion;
  }
  @NotNull
  @Override
  public CoroutineContext getContext(){
    return EmptyCoroutineContext.INSTANCE;
  }
  @Override
  public void resumeWith(@NotNull Object obj){
    try{
      Object result = obj;
      switch(label){
        case 0:{
          Logger.debug(1);
          result = ConsoleMainKt.returnSuspend(this); //挂起
          label++;
          if(isSuspended(result)) return;
        }
        case 1:{
          Logger.debug(result);
          Logger.debug(2);
          result = DelayKt.delay(1000, this); //挂起
          label++;
          if(isSuspended(result)) return;
        }
        case 2:{
          Logger.debug(3);
          result = ConsoleMainKt.returnImmediately(this); //未挂起
          label++;
          if(isSuspended(result)) return;
        }
        case 3:{
          Logger.debug(result);
          Logger.debug(4);
        }
      }
      completion.resume(Unit.INSTANCE);
    }catch(Exception e){
      completion.resumeWithException(e);
    }
  }
  private boolean isSuspended(Object obj){
    return o == IntrinsicsKt.getCOROUTINE_SUSPENDED();
  }
}
```

* 协程体内的代码都是通过Continuation.resumeWith调用
* 每次调用一次label加1，每一个挂起点对应于一个case分支
* 挂起函数在返回COUROUTINE_SUSPEND时才会挂起

这也就是为啥suspend函数需要在***其他挂起函数或者协程中执行***（需要Continuation进行resumeWith）。挂起和恢复的实现，其实就可以转化为Continuation的resumeWith调度。如果说是需要切换线程的话，就是在调用执行体的resumeWith之前，先resume，Continuation的context（DispatcherContext）进行拦截后Continuation。

