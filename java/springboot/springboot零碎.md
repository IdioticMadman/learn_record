* 配置devtools，在开发期间热重启

  * 加依赖

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <!--optional 别人不一定要引入-->
        <optional>true</optional>
    </dependency>
    ```

  * application.yaml中配置

    ```yaml
    spring:
    	devtools:
        	restart:
          		enabled: true
    ```



* maven打包

  ```
  mvn clean 清理上次生成的
  mvn package -Dmaven.test.skip=true
  ```


* springboot默认异常处理类

* ```
  ResponseEntityExceptionHandler
  
  ```
