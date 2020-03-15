## 是什么

* SpringBoot是新一代Spring框架

* SpringBoot可以快速构建Spring程序，简化Spring开发
* SpringBoot不是应用服务器
* SpringBoot不是SpringMVC的替代
* SpringBoot没有代码生成(自动配置autoconfig)

## 为什么要用

* 配置和依赖简化。简单易上手
* 不用关心配置，所以可以直接开始编写业务。通过main方法进行运行调试。不需要额外环境

## 创建SpringBoot程序

* 通过start.spring.io创建
* 使用IDEA创建

## SpringBoot的核心

* 自动配置，利用条件化注解，即conditional相关注解，来加载写好的自动配置类
  * @ConditionalonBean：当前配置了这个bean，就会激活对应的代码
  * @ConditionalOnMissBean：没有配置这个bean，就会激活对应的代码
  * @ConditionalOnClass: classPath中有指定类，激活对应代码(classpath即指当前运行的环境中没有这个类，简单的说依赖中没有这个class)
  * @ConditionalOnMissingClass: classpath中没有指定类，激活对应代码
  * @ConditionalOnProperty: 配置(application.yml)属性中包含某个值(prefix,name,havingValue)匹配上了就会激活对应代码块
  * @ConditionalOnResource: classpath中有指定资源
  * @ConditionalOnWebApplication: 是一个web程序
  * @ConditionalOnNotWebApplication: 不是一个web程序
* 起步依赖
  * 即配置好相关pom依赖
* 命令行界面
* Actuator监控