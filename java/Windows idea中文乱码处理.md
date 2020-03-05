# Windows idea中文乱码处理

1. 顶部导航  Help-> Edit Custom VM Options

   添加一句 -Dfile.encoding=UTF-8

2. ctrl+alt+s 打开设置页面，输入 encoding 查找到 File Encodings 选型卡，把所有的选项都设置成 UTF-8

3. 此时还有异常的话，把.idea、.gradle 以及 build 文件夹全删，重新导入一下项目