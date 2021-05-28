## ServiceManager

### 怎么添加一个系统服务

* 怎么使用系统服务

  context.getSystemSerivce(name)

* 系统服务调用的基本原理

  serviceFetcher.getService() -> serivceFetcher.createService()->ServiceManager.getService();

  ![image-20200401161853007](https://tva1.sinaimg.cn/large/00831rSTgy1gdec983kluj312u0keqdw.jpg)

* 系统服务注册的原理

  就是调用的ServiceManager.addService()  什么时候启动的系统服务呢？ 再启动SystemServer的时候，有三个步骤![image-20200401173956925](https://tva1.sinaimg.cn/large/00831rSTgy1gdeelk3yn4j31n70u0wtv.jpg)

  就是在这个时候注册的系统服务

  有一小部分的服务，是单独进程的。列如surfaceFlinger

* 独立进程的系统服务

  看一下SurfaceFlinger启动流程

  ![image-20200401174534510](https://tva1.sinaimg.cn/large/00831rSTgy1gdeerf2mqgj31pk0u0nn9.jpg)



### 系统服务和bind的应用服务有什么区别？

* 启动方式

  * 系统服务，大多是在SystemServer启动

  * 应用服务，是通过与AMS交互，createService

* 注册方式

  * 系统服务会通过ServiceManager.add 注册serviceManager上面
  * 应用服务，是通过AMS的bindService获取到binder的Token，service被动注册![image-20200401153955261](https://tva1.sinaimg.cn/large/00831rSTgy1gdeb4q7fdhj31sy0u0wma.jpg)

* 使用方式

  * 系统服务通过ServiceFetcher，实际还是通过ServiceManager.getService获取IBinder，然后拿到代理对象![image-20200401154329520](https://tva1.sinaimg.cn/large/00831rSTgy1gdeb8epctcj31z90u0b29.jpg)
  * 应用服务是通过bindService，拿到serviceConnnection，连接成功的时候的IBinder对象![image-20200401154447104](https://tva1.sinaimg.cn/large/00831rSTgy1gdeb9qvi06j329f0u0nlo.jpg)

