# Spring Boot学习笔记

### mvn打包的时候除了一个jar文件，还有一个jar.original文件，为啥

.jar.original, 就是当前项目的Java文件打包出来的class文件，不包含所有依赖的jar包。

.jar包含所有的dependency，包含web容器。

为什么会有两个，是因为`spring-boot-maven-plugin` 里面有一个repackage 重新把dependency打进去了，把之前的jar包，改成了.jar.original

### 如何替换Spring默认的版本号

在当前工程中的pom文件中，修改<properties> 节点中的对应的属性

### 如何打war包

创建工程的时候，选择war包，进行创建。

1. 实现一个WebApplicationInitializer接口

2. pom的packing节点为war

3. 需要添加一个编译时的tomcat

   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-tomcat</artifactId>
       <scope>provided</scope>
   </dependency>
   ```
