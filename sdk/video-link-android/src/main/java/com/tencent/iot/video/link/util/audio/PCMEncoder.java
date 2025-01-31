package com.tencent.iot.video.link.util.audio;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;


public class PCMEncoder {
    //比特率
    private static final int KEY_BIT_RATE = 96000;
    //读取数据的最大字节数
    private static final int KEY_MAX_INPUT_SIZE = 1024 * 1024;
    public static final int AAC_FORMAT = 0;
    public static final int G711_FORMAT = 1;
    private MediaCodec mediaCodec;
    private ByteBuffer[] encodeInputBuffers;
    private ByteBuffer[] encodeOutputBuffers;
    private MediaCodec.BufferInfo encodeBufferInfo;
    private EncoderListener encoderListener;
    private int encodeType = 0;
    private int sampleRate = 0;
    //声道数
    private int channelCount = 0;

    // 采样频率对照表
    private static Map<Integer, Integer> samplingFrequencyIndexMap = new HashMap<>();

    static {
        samplingFrequencyIndexMap.put(96000, 0);
        samplingFrequencyIndexMap.put(88200, 1);
        samplingFrequencyIndexMap.put(64000, 2);
        samplingFrequencyIndexMap.put(48000, 3);
        samplingFrequencyIndexMap.put(44100, 4);
        samplingFrequencyIndexMap.put(32000, 5);
        samplingFrequencyIndexMap.put(24000, 6);
        samplingFrequencyIndexMap.put(22050, 7);
        samplingFrequencyIndexMap.put(16000, 8);
        samplingFrequencyIndexMap.put(12000, 9);
        samplingFrequencyIndexMap.put(11025, 10);
        samplingFrequencyIndexMap.put(8000, 11);
    }

    public PCMEncoder(int sampleRate, int channelCount, EncoderListener encoderListener, int encodeFormat) {
        this.encoderListener = encoderListener;
        this.encodeType = encodeFormat;
        this.sampleRate = sampleRate;
        this.channelCount = channelCount;
        init();
    }

    /**
     * 初始化AAC编码器
     */
    private void init() {
        try {
            //参数对应-> mime type、采样率、声道数
            MediaFormat encodeFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
                    sampleRate, channelCount);
            //比特率
            encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, KEY_BIT_RATE);
            encodeFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            encodeFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, KEY_MAX_INPUT_SIZE);
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            mediaCodec.configure(encodeFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaCodec.start();
        encodeInputBuffers = mediaCodec.getInputBuffers();
        encodeOutputBuffers = mediaCodec.getOutputBuffers();
        encodeBufferInfo = new MediaCodec.BufferInfo();
    }

    /**
     * PCM 转 AAC
     *
     * @param data PCM数据
     */
    public void encodeData(byte[] data) {
        //dequeueInputBuffer（time）需要传入一个时间值，-1表示一直等待，0表示不等待有可能会丢帧，其他表示等待多少毫秒
        //获取输入缓存的index
        int inputIndex = mediaCodec.dequeueInputBuffer(-1);
        if (inputIndex >= 0) {
            ByteBuffer inputByteBuf = encodeInputBuffers[inputIndex];
            inputByteBuf.clear();
            //添加数据
            inputByteBuf.put(data);
            //限制ByteBuffer的访问长度
            inputByteBuf.limit(data.length);
            //把输入缓存塞回去给MediaCodec
            mediaCodec.queueInputBuffer(inputIndex, 0, data.length, 0, 0);
        }
        //获取输出缓存的index
        int outputIndex = mediaCodec.dequeueOutputBuffer(encodeBufferInfo, 0);
        while (outputIndex >= 0) {
            //获取缓存信息的长度
            int byteBufSize = encodeBufferInfo.size;
            //添加ADTS头部后的长度
            int bytePacketSize = byteBufSize + 7;
            //拿到输出Buffer
            ByteBuffer outPutBuf = encodeOutputBuffers[outputIndex];
            if (encodeBufferInfo.size > 2) {
                outPutBuf.position(encodeBufferInfo.offset);
                outPutBuf.limit(encodeBufferInfo.offset + encodeBufferInfo.size);

                byte[] aacData = new byte[bytePacketSize];
                //添加ADTS头部
                addADTStoPacket(aacData, bytePacketSize);

                outPutBuf.get(aacData, 7, byteBufSize);
                outPutBuf.position(encodeBufferInfo.offset);

                //编码成功
                if (encoderListener != null) {
                    encoderListener.encodeAAC(aacData, encodeBufferInfo.presentationTimeUs);
                }
            }

            //释放
            mediaCodec.releaseOutputBuffer(outputIndex, false);
            outputIndex = mediaCodec.dequeueOutputBuffer(encodeBufferInfo, 0);
        }
    }

    /**
     * 添加ADTS头
     */
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2;  // AAC LC
        int chanCfg = channelCount;
        int freqIdx = samplingFrequencyIndexMap.get(sampleRate);
        // filled in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }
}
