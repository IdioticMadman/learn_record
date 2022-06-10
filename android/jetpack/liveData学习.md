### 关键类说明

1. LifeCycleOwner：宿主，提供lifeCycle，默认实现 `LifecycleRegistry`

   ```java
   public interface LifecycleOwner {
       /**
        * Returns the Lifecycle of the provider.
        *
        * @return The lifecycle of the provider.
        */
       @NonNull
       Lifecycle getLifecycle();
   }
   ```

2. LifecycleObserver: 声明周期观察者，LifeCycleOwner作为生命周期的被观察者，持有多个LifeCycleObserver。默认实现是`LifecycleEventObserver` 或者实现LifecycleObserver，使用注解`@OnLifecycleEvent`接收对应的声明周期事件。

3. 生命周期事件是通过LifecycleRegistry在对应的生命周期发出事件给LifecycleObserver。默认是实现是通过注入一个fragment，即`ReportFragment.injectIfNeededIn(this)`

### 关键过程分析

1. 添加观察者`addObserver()` 
2. 生命周期事件分发`handleLifecycleEvent()`



