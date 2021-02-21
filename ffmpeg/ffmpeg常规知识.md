note: mian函数的第一个参数是，当前程序的名字。所以判断需要输入两个参数的时候，实际上argc要有3个以上

### 重要的结构体

* AVFormatContext：封装格式的上下文
* AVStream：媒体文件的轨道，音频轨道，视频轨道，字幕轨道
* AVPacket：数据包

### 操作数据流的基本步骤:

* 解复用：封装格式解封
* 获取流： 视频轨道或音频轨道
* 读取数据包：读取视频包或者音频包
* 释放资源

### 打印音视频信息

* av_register_all() : 注册所有组件
* avformat_open_input()/avformat_close_input()：打开或关闭AVFormatContext
* av_dump_format()：打印音频文件信息

```c
#include <libavformat/avformat.h>
#include <libavutil/log.h>
int main(int args, char **argv) {
    int ret = 0;
    AVFormatContext *fmt_ctx = NULL;
    // av_register_all();
    //上下文，文件路径，文件格式(AVInputFormat)，设置参数(AVDictionary)
    ret = avformat_open_input(&fmt_ctx, "./test.mp4", NULL, NULL);
    if (ret < 0) {
        av_log(NULL, AV_LOG_ERROR, "打开文件失败！");
        return -1;
    }
    /*
     * @param ic        the context to analyze
     * @param index     index of the stream to dump information about
     * @param url       the URL to print, such as source or destination file
     * @param is_output Select whether the specified context is an input(0) or output(1)
     * */
    av_dump_format(fmt_ctx, 0, "test.mp4", 0);
    //关闭上下文
    avformat_close_input(&fmt_ctx);
    return -1;
}
```

### 抽取音频文件

* av_init_packet()
* av_find_best_stream()
* av_read_frame() / av_packet_unref()

步骤：

1. 读取输入文件和输出文件位置
2. 获取音频流
3. 写出音频到aac文件(要能播放的话，要加入adts头)

```c
#include <libavformat/avformat.h>
#include <libavutil/log.h>
#include <stdio.h>

void adts_header(char *szAdtsHeader, int dataLen) {

    int audio_object_type = 2;
    int sampling_frequency_index = 4; //采样率
    int channel_config = 2;//通道数

    /*int avpriv_mpeg4audio_sample_rates[] = {
            96000, 88200, 64000, 48000, 44100, 32000,
                    24000, 22050, 16000, 12000, 11025, 8000, 7350
        };
        channel_configuration: 表示声道数chanCfg
        0: Defined in AOT Specifc Config
        1: 1 channel: front-center
        2: 2 channels: front-left, front-right
        3: 3 channels: front-center, front-left, front-right
        4: 4 channels: front-center, front-left, front-right, back-center
        5: 5 channels: front-center, front-left, front-right, back-left, back-right
        6: 6 channels: front-center, front-left, front-right, back-left, back-right, LFE-channel
        7: 8 channels: front-center, front-left, front-right, side-left, side-right, back-left,
         back-right, LFE-channel 8-15: Reserved
    */

    int adtsLen = dataLen + 7;

    szAdtsHeader[ 0 ] = 0xff;      // syncword:0xfff                          高8bits
    szAdtsHeader[ 1 ] = 0xf0;      // syncword:0xfff                          低4bits
    szAdtsHeader[ 1 ] |= (0 << 3); // MPEG Version:0 for MPEG-4,1 for MPEG-2  1bit
    szAdtsHeader[ 1 ] |= (0 << 1); // Layer:0                                 2bits
    szAdtsHeader[ 1 ] |= 1;        // protection absent:1                     1bit

    szAdtsHeader[ 2 ] = (audio_object_type - 1) << 6; // profile:audio_object_type - 1 2bits
    szAdtsHeader[ 2 ] |= (sampling_frequency_index & 0x0f)
                         << 2;     // sampling frequency index:sampling_frequency_index  4bits
    szAdtsHeader[ 2 ] |= (0 << 1); // private bit:0                                      1bit
    szAdtsHeader[ 2 ] |=
        (channel_config & 0x04) >> 2; // channel configuration:channel_config               高1bit

    szAdtsHeader[ 3 ] = (channel_config & 0x03)
                        << 6;      // channel configuration:channel_config      低2bits
    szAdtsHeader[ 3 ] |= (0 << 5); // original：0                               1bit
    szAdtsHeader[ 3 ] |= (0 << 4); // home：0                                   1bit
    szAdtsHeader[ 3 ] |= (0 << 3); // copyright id bit：0                       1bit
    szAdtsHeader[ 3 ] |= (0 << 2); // copyright id start：0                     1bit
    szAdtsHeader[ 3 ] |= ((adtsLen & 0x1800) >> 11); // frame length：value   高2bits

    szAdtsHeader[ 4 ] = (uint8_t)((adtsLen & 0x7f8) >> 3); // frame length:value    中间8bits
    szAdtsHeader[ 5 ] = (uint8_t)((adtsLen & 0x7) << 5);   // frame length:value    低3bits
    szAdtsHeader[ 5 ] |= 0x1f;                             // buffer fullness:0x7ff 高5bits
    szAdtsHeader[ 6 ] = 0xfc;
}

int main(int args, char **argv) {
    av_log_set_level(AV_LOG_INFO);
    if (args < 3) {
        av_log(NULL, AV_LOG_ERROR, "输入参数错误，请输入输入文件以及输出文件");
        return -1;
    }
  	//输入文件
    char *inputFilePath = argv[ 1 ];
  	//输出文件
    char *outputFilePath = argv[ 2 ];
    FILE *outputFile = fopen(outputFilePath, "wb");
    if (!outputFile) {
        av_log(NULL, AV_LOG_ERROR, "输出文件打开失败!");
        return -1;
    }

    AVFormatContext *fmt_ctx = NULL;
    AVPacket pkt;
    int ret = 0;
    int audio_index = -1;
    // 1. open file 
    ret = avformat_open_input(&fmt_ctx, inputFilePath, NULL, NULL);
    if (ret < 0) {
        av_log(NULL, AV_LOG_ERROR, "avformat open input error: %s \n", av_err2str(ret));
        fclose(outputFile);
        return -1;
    }
  	// 打印视频信息
    av_dump_format(fmt_ctx, 0, inputFilePath, 0);
    // 2. find beat stream 
    ret = av_find_best_stream(fmt_ctx, AVMEDIA_TYPE_AUDIO, -1, -1, NULL, 0);
    if (ret < 0) {
        av_log(NULL, AV_LOG_ERROR, "cannot find best stream: %s \n", av_err2str(ret));
        avformat_close_input(&fmt_ctx);
        fclose(outputFile);
        return -1;
    }
    audio_index = ret;
    int len = 0;
    av_init_packet(&pkt);
    // 3. read audio data
    while (av_read_frame(fmt_ctx, &pkt) >= 0) {
        if (pkt.stream_index == audio_index) {
            char adts_header_buf[ 7 ];
            adts_header(adts_header_buf, pkt.size);
          	// add adts header
            fwrite(adts_header_buf, 1, 7, outputFile);
            len = fwrite(pkt.data, 1, pkt.size, outputFile);
            if (len != pkt.size) {
                av_log(NULL, AV_LOG_ERROR, "warning, write file size not equals pkt size\n");
            }
        }
        av_packet_unref(&pkt);
    }
		//4. release 
    avformat_close_input(&fmt_ctx);
    fclose(outputFile);
}
```

### 抽取视频数据

获取是视频流和和获取音频流类似。只是我们要取出数据编译成h264

从帧的特征码开始

* start code: 帧的开头，特殊编码，用来标识帧
* SPS/PPS: 解码的视频参数，分辨率，码流，帧率。
  * 本地文件，理论上只有一个SPS、PPS就可以
  * 视频文件视频流参数发生变化的时候，需要重新设置sps pps
  * 直播的情况下，应对复杂的网络情况，可以在每个关键帧都加上sps pps，防止切换分辨率的时候，没有拿到对应的信息
* codec->extradata 中获取sps pps文件信息

AVPacket里面可能会有多个H264帧

H264结构：

0-3：nal_size ：长度为4个字节，表示当前帧的长度

4: nal_unit : 当前帧的类型，类型为5的时候是关键帧

抽取h264步骤：

1. av_read_frame()获取packet
2. 从packet里读取nal_size，循环获取h264帧
3. 写入特征码（00 00 00 01）和 sps pps(如果是关键帧的话)
4. 写入帧体数据，完成一帧写入
5. 