文件操作

* 文件删除与重命名

  ```c
  avpriv_io_delete();
  avpriv_io_move();
  ```

* 操作文件目录

  ```c
  avio_open_dir();
  avio_read_dir();
  avio_close_dir();
  ```

* 读取当前目录下面的文件

  ```c
  #include <libavformat/avformat.h>
  #include <libavutil/log.h>
  int main(int argc, char const *argv[]) {
      int ret;
      //设置log级别
      av_log_set_level(AV_LOG_DEBUG);
      AVIODirContext *dirContext;
      AVIODirEntry *dirEntry;
      //获取文件夹上下文
      ret = avio_open_dir(&dirContext, "./", NULL);
      if (ret < 0) {
          av_log(NULL, AV_LOG_ERROR, "打开目录失败：%s", av_err2str(ret));
          return -1;
      }
      while (1) {
          //开始读取文件列表
          ret = avio_read_dir(dirContext, &dirEntry);
          if (ret < 0) {
              av_log(NULL, AV_LOG_ERROR, "读取目录失败: %s", av_err2str(ret));
              goto __failed;
          }
          if (!dirEntry) {
              break;
          }
          //打印文件信息
          av_log(NULL, AV_LOG_INFO, "%lld, %s\n", dirEntry->size, dirEntry->name);
          //释放Entry
          avio_free_directory_entry(&dirEntry);
      }
  
  __failed:
      av_log(NULL, AV_LOG_INFO, "------- 读取完毕！-------\n");
      //关闭上下文
      avio_close_dir(&dirContext);
      return 0;
  }
  ```

  