## java相关

使用homebrew 安装openJDk

brew install openjdk@8  // openjdk@11 openjdk@17 

在mac上 java安装目录可以有两个（其他应用回去这里面找，而不是/usr/local）：
系统Library目录：/Library/Java/JavaVirtualMachines

用户Library ： ~/Library/Java/JavaVirtualMachines

所以在brew安装完需要链接到这两个其中一个目录

```shell
sudo ln -sfn /usr/local/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

可以使用/usr/libexec/java_home 找到当前用户的系统中安装的java列表

```shell
➜  ~ /usr/libexec/java_home -V
Matching Java Virtual Machines (4):
    18.0.1.1 (x86_64) "Oracle Corporation" - "OpenJDK 18.0.1.1" /Users/rober/Library/Java/JavaVirtualMachines/openjdk-18.0.1.1/Contents/Home
    17.0.7 (x86_64) "Homebrew" - "OpenJDK 17.0.7" /usr/local/Cellar/openjdk@17/17.0.7/libexec/openjdk.jdk/Contents/Home
    11.0.19 (x86_64) "Homebrew" - "OpenJDK 11.0.19" /usr/local/Cellar/openjdk@11/11.0.19/libexec/openjdk.jdk/Contents/Home
    1.8.0_372 (x86_64) "Homebrew" - "OpenJDK 8" /usr/local/Cellar/openjdk@8/1.8.0+372/libexec/openjdk.jdk/Contents/Home
/Users/rober/Library/Java/JavaVirtualMachines/openjdk-18.0.1.1/Contents/Home
```

所以可以设置alias快捷切换java版本

```shell
export JAVA_HOME=`/usr/libexec/java_home -v 11` // 默认开terminal 使用的java版本
alias java-17="export JAVA_HOME=`/usr/libexec/java_home -v 17`; java -version" // 切换成17
alias java-11="export JAVA_HOME=`/usr/libexec/java_home -v 11`; java -version"// 切换成11
alias java-18="export JAVA_HOME=`/usr/libexec/java_home -v 18`; java -version"// 切换成18
```

