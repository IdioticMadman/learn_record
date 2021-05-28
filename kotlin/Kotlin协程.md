### 协程要素

1. Continuation。类似callback

   ```kotlin
   interface Continuation<in T>{
     fun resumeWith(result:Result<T>)
   }
   
   fun <T> Continuation<T>.resume(value: T) = resumeWith(Result.success(valule))
   fun <T> Continuation<T>.resumeWithException(t: Throwable) = resumeWith(Result.failure(t))
   ```

2. 挂起函数

   ```kotlin
   suspend fun hello() // suspend ()->Unit
   ```

   编译后：

   ```java
   public Object hello(Continuation<Object> continuation)
   ```

   ```kotlin
   suspend fun bar(a:Int):String // suspend (Int)->String
   ```

   编译后：

   ```java
   public Object bar(int a, Continuation<String> continuation)
   ```

   

