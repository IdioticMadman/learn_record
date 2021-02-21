### android系统的启动

* android有哪些主要的系统进程
* 这些系统是怎么启动的
* 进程启动之后主要做了哪些

### 系统进程

可以查看init.rc看到

* zygote -> SystemServer

  init进程fork出zygote进程，启动虚拟机，注册jni函数，预加载系统资源，启动System Server，启动Launcher，进入Socket Loop  -> runOnce

  ```java
  startSystemServer()-> handleSystemServerProcess(args)
  	RuntimeInit.zygoteinit -> zygoteInit
  		-> commonInit()//一些常规init
  		-> nativeInit() //初始化binder
  		-> applicationInit() // SystemServer main函数
  			->run()
        	-> Looper.prepareMainLooper()
        	   System.loadLibrary("android_servers")
        	   createSystemContext()
        	   startBootstrapServices()
        	   startCoreServices()
        	   startOtherServices()
        	   Looper.loop()
  ```

  * 系统服务是怎么启动的？

    * 系统服务怎么发布的

      通过publishBinderService() -> ServiceManager.add()， 注册到ServiceManager上面

    * 系统服务跑在什么线程

      1. 主线程

      2. 工作线程（DisplayThread FgThread IoThread UiThread）像AMS PMS会有自己单独的thread
      3. binder线程，binder回来的肯定是回调binder线程

    * 系统服务跑在binder线程和跑在工作线程，如何取舍

      binder线程是大家一起共享的，系统负载很重，binder线程池忙碌，会影响系统的服务的实时性，长时间占用binder线程也不妥。也不能每个服务都启动一个工作线程，一共上百个系统服务，线程开太多会内存溢出，而且太多线程之间切换对线程不利。总的来说，对于实时性要求不那么高，并且处理起来不太耗时的任务就可以放到binder线程里。另外启动工作线程也可以避免同步的问题，因为应用跨进程调用过来是在binder线程池，通过切换到工作线程可以让binder调用序列化，不用到处上锁。

  * 系统服务启动的依赖相互关系？

    就是上面的SystemServer.run 方法里面启动的那些个流程

    * 分批启动
    * 分阶段启动

  * 桌面启动

    systemReady() -> startHomeActivityLocked() 启动Launcher

    -> LoadTask() -> queryIntentActivitiesAsUser 问pms要已安装的app

* serviceManager

* surfaceflinger

* media

* ...



