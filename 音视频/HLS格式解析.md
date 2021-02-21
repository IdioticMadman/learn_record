### HLS格式解析

1. HLS类型

   1. 媒体播放列表

      ```java
      #EXTM3U
      #EXT-X-TARGETDURATION:10
      
      #EXTINF:9.009,
      http://media.example.com/first.ts
      #EXTINF:9.009,
      http://media.example.com/second.ts
      #EXTINF:3.003,
      http://media.example.com/third.ts
      #EXT-X-ENDLIST
      ```

      内部记录的事一系列媒体片段资源，顺序播放该片段资源，即可完整展示多媒体资源

   2. 主播放列表

      ```java
      #EXTM3U
      #EXT-X-STREAM-INF:BANDWIDTH=150000,RESOLUTION=416x234,CODECS="avc1.42e00a,mp4a.40.2"
      http://example.com/low/index.m3u8
      #EXT-X-STREAM-INF:BANDWIDTH=240000,RESOLUTION=416x234,CODECS="avc1.42e00a,mp4a.40.2"
      http://example.com/lo_mid/index.m3u8
      #EXT-X-STREAM-INF:BANDWIDTH=440000,RESOLUTION=416x234,CODECS="avc1.42e00a,mp4a.40.2"
      http://example.com/hi_mid/index.m3u8
      #EXT-X-STREAM-INF:BANDWIDTH=640000,RESOLUTION=640x360,CODECS="avc1.42e00a,mp4a.40.2"
      http://example.com/high/index.m3u8
      #EXT-X-STREAM-INF:BANDWIDTH=64000,CODECS="mp4a.40.5"
      http://example.com/audio/index.m3u8
      #EXT-X-ENDLIST
      ```

      作为主播放列表是，内部提供的是同一份媒体资源的多份流列表资源

2. HLS基本字段

   ```
   #EXTM3U                    M3U8文件头，必须放在第一行;
   #EXT-X-MEDIA-SEQUENCE      第一个TS分片的序列号，一般情况下是0，但是在直播场景下，这个序列号标识直播段的起始位置; #EXT-X-MEDIA-SEQUENCE:0
   #EXT-X-TARGETDURATION      每个分片TS的最大的时长;   #EXT-X-TARGETDURATION:10     每个分片的最大时长是 10s
   #EXT-X-ALLOW-CACHE         是否允许cache;          #EXT-X-ALLOW-CACHE:YES      #EXT-X-ALLOW-CACHE:NO    默认情况下是YES
   #EXT-X-ENDLIST             M3U8文件结束符；
   #EXTINF                    extra info，分片TS的信息，如时长，带宽等；一般情况下是    #EXTINF:<duration>,[<title>] 后面可以跟着其他的信息，逗号之前是当前分片的ts时长，分片时长 移动要小于 #EXT-X-TARGETDURATION 定义的值；
   #EXT-X-VERSION             M3U8版本号
   #EXT-X-DISCONTINUITY       该标签表明其前一个切片与下一个切片之间存在中断。下面会详解
   #EXT-X-PLAYLIST-TYPE       表明流媒体类型；
   #EXT-X-KEY                 是否加密解析，    #EXT-X-KEY:METHOD=AES-128,URI="https://priv.example.com/key.php?r=52"    加密方式是AES-128,秘钥需要请求   https://priv.example.com/key.php?r=52  ，请求回来存储在本地；
   ```

3. 判断HLS是否是直播

   1. 判断是否存在`#EXT-X-ENDLIST`

      对于一个M3U8文件，如果结尾不存在`#EXT-X-ENDLIST`，那么一定是直播

   2. 判断`#EXT-X-PLAYLIST-TYPE`

      * VOD 即Video on Demand表示该视频流为点播源
      * EVENT 表示该视频流为直播源，因此服务器不能更改或删除文件任务部分内容

      注：VOD文件通常带有EXT-X-ENDLIST标签，因为其是点播源，不会改变。而EVENT文件初始化时一般不会EXT-X-ENDLIST标签，暗示有新的文件会添加到播放列表末尾。因此也需要客户端定时获取M3U8文件，以获取新的媒体片段资源，直到访问到EXT-X-ENDLIST标签才停止

4. HLS提供多码率

   ```java
   #EXTM3U
   #EXT-X-STREAM-INF:BANDWIDTH=150000,RESOLUTION=416x234,CODECS="avc1.42e00a,mp4a.40.2"
   http://example.com/low/index.m3u8
   #EXT-X-STREAM-INF:BANDWIDTH=240000,RESOLUTION=416x234,CODECS="avc1.42e00a,mp4a.40.2"
   http://example.com/lo_mid/index.m3u8
   #EXT-X-STREAM-INF:BANDWIDTH=440000,RESOLUTION=416x234,CODECS="avc1.42e00a,mp4a.40.2"
   http://example.com/hi_mid/index.m3u8
   #EXT-X-STREAM-INF:BANDWIDTH=640000,RESOLUTION=640x360,CODECS="avc1.42e00a,mp4a.40.2"
   http://example.com/high/index.m3u8
   #EXT-X-STREAM-INF:BANDWIDTH=64000,CODECS="mp4a.40.5"
   http://example.com/audio/index.m3u8
   #EXT-X-ENDLIST
   ```

   '#EXT-X-STREAM-INF' 字段后面有：
   BANDWIDTH 指定码率
   RESOLUTION 分辨率
   PROGRAM-ID 唯一ID
   CODECS 指定流的编码类型

5. HLS中插入广告

   M3U8文件中想插入广告，要使用到`#EXT-X-DISCOUNTINUITY`。该标签表明前一个切片与下一个切片之间存在中断，说明有不连续的视频出现，这个视频绝大多数情况下就是广告

   出现以下情况时，必须使用该标签

   * File format
   * Encoding parameters

### HLS协议草案

可以参考看 https://tools.ietf.org/html/rfc8216

