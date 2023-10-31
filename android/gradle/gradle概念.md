# 和android相关Gradle的基础知识

### gradle执行周期

* Task Graphs

  每个project都是由task组成的一个有向无环图。而且这个图是在每次构建的时候生成，会避免掉一些当前构建无关的task

* 构建阶段

  * 初始化
    1. 检查setting.gradle文件
    2. 解析setting.gradle文件，确定需要被构建的project
    3. 给每个project创建project对象
  * 配置
    1. 解析被include进来的所有的project (project.gradle)
    2. 根据脚本创建所有project的task graph
  * 执行
    1. 根据project 的task graph进行执行task



参考：https://docs.gradle.org/current/userguide/build_lifecycle.html



### 关于开发gradle plugin时一些概念

project apply plugin的时候，其实就是把当前project对象给了plugin。

在开发插件的时候，主要是 定义一系列task，挂在到当前project上面，设定好是独立task，还是依赖一些别的task的执行。
其次，task的执行一般都会需要一些配置定义在build.gradle，可以在插件中根据 project 获取到对应的extension来解决 

