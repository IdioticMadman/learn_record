### virtualenv工具使用

#### 通过pip安装

```shell
pip3 install virtualenv //安装virtualenv
pip3 install virtualenvwrapper //安装virtualenvwrapper
```

### 配置virtualenvwrappter

```shell
echo "export WORKON_HOME=~/Envs\nexport PROJECT_HOME=~/workspace/pythonProject\nsource /usr/local/bin/virtualenvwrapper.sh" >> ~/.bash_profile  //写入WORKON_HOME、PROJECT_HOME、以及相关设置 到环境变量中
source ~/.bash_profile
mkdir -p $WORKON_HOME
mkdir -p $PROJECT_HOME

```

virtualenvwrapper官网地址：[https://virtualenvwrapper.readthedocs.io](https://virtualenvwrapper.readthedocs.io/)

### 常用命令使用

* mkvirtualenv 创建虚拟环境
* lsvirtualenv 列举虚拟环境
* rmvirtualenv 删除虚拟环境
* workon 使用虚拟机环境
* deactivate 退出虚拟环境

其他命令，请详细查看官方的[Command Reference](https://virtualenvwrapper.readthedocs.io/en/latest/command_ref.html#)

