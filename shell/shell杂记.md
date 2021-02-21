## brew相关

* brew cask doctor可以检查cask安装的状态



## jenv

管理java的版本共存的问题



### sz rz

命令行设置sz (上传文件)和rz (下载文件)

设置mac的iterm2支持这两个命令<https://github.com/mmastrac/iterm2-zmodem>



###  设置mac的finder是否显示隐藏文件

`defaults write com.apple.finder AppleShowAllFiles -bool true ` 显示

`defaults write com.apple.finder AppleShowAllFiles -bool false` 隐藏



### Vim编辑器相关

1. /<关键字> 查找关键  

   n 下一个， shift+n 上一个

2. %s/<关键字><替换字>/gc 

   s: 查找, g: 继续查找下一个， c：与用户交互

3. set number 显示行号

4. 设置行号替换

   <其实行号>,<结束行号>s/<关键字>/<替换字>/gc

5. 
