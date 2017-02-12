package com.twirling.audio.player;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 解决问题：立体声变为全景声，大帧转为小帧处理
 * Created by 谢秋鹏 on 2016/6/6.
 */
public class TwirlingAudioProcess {
    private static TwirlingAudioProcess ins = null;
    private static final int FRAME_LENGTH = 512;
    private int chunkSize = 0;
    private int loopNum = 0;
    private AudioEngine audioEngine = null;
    private float pitch = 0;
    private float yaw = 0;
    private int channels = 0;
    private float[] metadata = null;
    private float[][] metadataFromJson = null;
    private float postgain = 0.9f;
    private long instance = 0;

    private TwirlingAudioProcess() {
    }

    public static TwirlingAudioProcess getIns(AudioEngine audioEngine) {
        if (ins == null) {
            ins = new TwirlingAudioProcess(audioEngine, 0);
        }
        return ins;
    }

    public void setChannels(int channels) {
        this.channels = channels;
        metadata = new float[channels * 3];
        for (int num = 0; num < channels; num++) {
            metadata[num * 3] = 2;
            metadata[num * 3 + 1] = 45 * num * (3.1415926f / 180);
            metadata[num * 3 + 2] = 0;
        }
    }

    public TwirlingAudioProcess(AudioEngine audioEngine, long instance) {
        this.audioEngine = audioEngine;
        this.instance = instance;
    }

    // TODO
    public void audioOutputProcess(short[] audioFlat, short[] audioFlatOutput) {
        int n_acc = 0;
        int n_acc_out = 0;
        int ii = 0;
        int normfactor = 32768;
        int tmp32;
        if (metadata == null) {
            return;
        }
        // 0度方向
        float[] audioInput = new float[FRAME_LENGTH * channels];
        float[] audioOutput = new float[FRAME_LENGTH * 2];
        for (int loopi = 0; loopi < loopNum; loopi++) {
            for (ii = 0; ii < FRAME_LENGTH * channels; ii++) {
                audioInput[ii] = (float) audioFlat[n_acc++] / normfactor;
            }
//            for (int i = 0; i < metadata.length; i++) {
//                Log.w("angle", metadata[i] + "");
//            }
//            Log.w("angle", yaw + " " + pitch);
            audioEngine.audioProcess(instance, yaw, pitch, 0, audioInput, audioOutput, metadata);
            // 转成双声道
//            for (ii = 0; ii < FRAME_LENGTH; ii++) {
//                audioOutput[ii * 2] = audioInput[ii * channels + 0];
//                audioOutput[ii * 2 + 1] = audioInput[ii * channels + 1];
//            }
            for (ii = 0; ii < FRAME_LENGTH * 2; ii++) {
                tmp32 = (int) (audioOutput[ii] * normfactor * postgain);
                if (tmp32 > 32767) {
                    tmp32 = 32767;
                } else if (tmp32 < -32768) {
                    tmp32 = -32768;
                }
                audioFlatOutput[n_acc_out++] = (short) tmp32;
            }
        }
        return;
    }

    // byte2short
    public short[] byte2Short(byte[] chunk, int loopNum) {
        //
        chunkSize = chunk.length;
        this.loopNum = loopNum;
        ByteBuffer fulldata = ByteBuffer.allocate(chunkSize);
        fulldata.put(chunk);
        fulldata.order(ByteOrder.LITTLE_ENDIAN);

        // ok now we can create an array of shorts (16 bit data) and load it
        // we are stereo 16 bit, so each sample is 2 bytes
        short[] sounddata = new short[chunkSize / 2];

        // copy data from ByteBuffer into our short buffer. Short buffer is used
        // to load the AudioTrack object
        int totalsamples = chunkSize / 2;
        for (int counter1 = 0; counter1 < totalsamples; counter1++) {
            sounddata[counter1] = fulldata.getShort(counter1 * 2);
        }
        return sounddata;
    }

    public byte[] shortToByte(short[] shorts) {
        int datasize = shorts.length;
        ByteBuffer fulldata = ByteBuffer.allocate(datasize * 2);
        byte[] bytes = new byte[datasize * 2];
        fulldata.put(bytes);
        fulldata.order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < datasize * 2; i++) {
            bytes[i] = fulldata.get(i);
        }
        return bytes;
    }

    public void setMetadataFromJson(float[][] metadata) {
        if (metadataFromJson == null) {
            int row = metadata.length;
            int colume = metadata[0].length;
            metadataFromJson = new float[row][colume];
            metadataFromJson = metadata;
        }
    }

    public void setAudioPlayTime(float playtime) {
        int i, c, n;
        float azi, elv, r;
        // output
        if (metadataFromJson == null || channels == 8) {
            return;
        }
        int metadataLen = metadataFromJson.length;
//        Log.d("angle", "playtime: " + playtime + " metadataLength:" + metadataLen);
        int MetadataIndex = metadataLen - 1;
        float interplateFactor = 1.0f;
        for (i = 0; i < metadataLen; i++) {
            if (playtime <= metadataFromJson[i][0]) {
                MetadataIndex = i;
                if (i == 0)
                    interplateFactor = 1.0f;
                else {
                    interplateFactor = (playtime - metadataFromJson[i - 1][0])
                            / (metadataFromJson[i][0] - metadataFromJson[i - 1][0]);
                }
                break;
            }
        }
        n = 0;
        //channel objectNum
        for (c = 0; c < channels; c++) {
            //metadata二维数组
            if (MetadataIndex == 0) {
                r = metadataFromJson[MetadataIndex][1 + c * 3];
                //r = 1; //for test.
                azi = (float) (metadataFromJson[MetadataIndex][2 + c * 3] * Math.PI / 180.0f);
                elv = (float) (metadataFromJson[MetadataIndex][3 + c * 3] * Math.PI / 180.0f);
            } else {
                r = (metadataFromJson[MetadataIndex][1 + c * 3] *
                        interplateFactor + metadataFromJson[MetadataIndex - 1][1 + c * 3] *
                        (1 - interplateFactor));
                //r = 1; //for test.
                azi = (float) ((metadataFromJson[MetadataIndex][2 + c * 3] * interplateFactor +
                        metadataFromJson[MetadataIndex - 1][2 + c * 3] * (1 - interplateFactor)) * Math.PI / 180.0f);
                elv = (float) ((metadataFromJson[MetadataIndex][3 + c * 3] * interplateFactor +
                        metadataFromJson[MetadataIndex - 1][3 + c * 3] * (1 - interplateFactor)) * Math.PI / 180.0f);
            }
            //metadata output
            metadata[n++] = r;
            metadata[n++] = -azi; //positive direction of y is to left.
            metadata[n++] = elv;
        }
    }

    public void setGyroscope(float[] gyroscope) {
        yaw = gyroscope[1];
        pitch = -gyroscope[0];
    }

}
