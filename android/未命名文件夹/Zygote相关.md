### Zygote相关

### Zygote的作用

* 启动SystemServer

* 孵化应用进程

  常用类，JNI函数，主题资源，共享库

### 启动三段式

进程启动流程

1. 进程启动
2. 准备工作
3. Loop  处理消息

### Zygote的启动流程

* Zygote进程是怎么启动的？

  是由Init进程加载init.rc，Zygote是其中之一，还有ServiceManager等。

  fork函数会回调两次，返回如果pid是0，则表示子进程，不为零则为父进程，fork之后，子进程继承父进程所有资源

  ![image-20200331192824807](https://tva1.sinaimg.cn/large/00831rSTgy1gddcn8xhn7j325w0u07jt.jpg)

  execve， 加载新的程序，path：可执行文件的路径，argv：参数，env环境变量

  信号处理：SIGCHILD  ，fork出来的子进程挂掉的时候，父进程会收到SIGCHILD信号，可以重新拉起子进程

* Zygote进程启动之后做了啥

  * Zygote 的native

    1. 启动Android虚拟机
    2. 注册JNI函数
    3. 进入Java流程

    ![image-20200331194301160](https://tva1.sinaimg.cn/large/00831rSTgy1gddcmbb3fvj31h80u07qq.jpg)

  * Zygote的Java

    loop循环处理请求

    

    

