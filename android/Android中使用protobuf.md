# Android中使用protobuf

protobuf序列化框架的一中。不多说，直接撸

### 配置

*  根目录的build.gradle中添加

  ```
   dependencies {
       ...
       classpath 'com.google.protobuf:protobuf-gradle-plugin:0.8.7'
       ...
   }
  ```

* module中的build.gradle添加插件

  ```
  ...
  apply plugin: 'com.google.protobuf'
  ...
  ```

* module中的build.gradle添加依赖

  ```
  ...
  implementation "com.google.protobuf:protobuf-lite:3.0.0"
  ...
  ```

* module中的build.gradle配置protobuf节点

  ```
  //最外层
  protobuf {
      //配置protoc编译器
      protoc {
          artifact = 'com.google.protobuf:protoc:3.0.0'
      }
      plugins {
          javalite {
              artifact = 'com.google.protobuf:protoc-gen-javalite:3.0.0'
          }
      }
      generateProtoTasks {
          all().each { task ->
              task.plugins {
                  javalite {}
              }
          }
      }
  }
  ```

* module中的build.gradle的android节点下配置sourceSets

  ```
  sourceSets {
      main {
           proto {
               srcDir 'src/main/proto'
           }
      }
  }
  ```

* 在src/mian/目录下，新建proto文件夹，放入对应的proto文件即可

### 使用

* 序列化对象

  ```
  public class MessageFactory {
  
      public static Vpn.LoginRequest buildLogin(String userName, String password) {
          return Vpn.LoginRequest.newBuilder()
                  .setUsername(userName)
                  .setPassword(password)
                  .setRemember(true)
                  .build();
      }
  
      public static Vpn.TokenLoginRequest buildTokenLogin(String token) {
          return Vpn.TokenLoginRequest.newBuilder()
                  .setToken(token)
                  .build();
      }
  
      public static Vpn.GetVpnEntryRequest buildGetVpnEntry() {
          return Vpn.GetVpnEntryRequest.newBuilder()
                  .build();
      }
  
  }
  ```

  拿到GeneratedMessageLite对象以后，就可以toByteArray转换成字节数组了

* 反序列化对象

  ```
  GeneratedMessageLite body = null;
  try {
      switch (respCmd) {
          case CmdFactory.RESP_CMD.LOGIN:
              body = Vpn.LoginResponse.parseFrom(bytes);
              break;
          case CmdFactory.RESP_CMD.TOKEN_LOGIN:
              body = Vpn.TokenLoginResponse.parseFrom(bytes);
              break;
          case CmdFactory.RESP_CMD.GET_VPN_ENTRY:
              body = Vpn.GetVpnEntryResponse.parseFrom(bytes);
              break;
          default:
              Log.e(TAG, "channelRead: 读取到未知cmd");
      }
  ```

  可以直接从拿到字节数组进行反序列化