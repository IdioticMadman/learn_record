打印日志：

* 引入头文件

  ```c
  include <libavutil/log.h>
  ```

* 设置日志打印级别

  ```c
  av_log_set_level(AV_LOG_DEBUG)
  ```

* 打印日志（设置输出级别）

  ```c
  av_log(NULL, AV_LOG_INFO,"...%s\n",op)
  ```

* 日志级别

  * AV_LOG_ERROR
  * AV_LOG_WARNING
  * AV_LOG_INFO
  * AV_LOG_DEBUG