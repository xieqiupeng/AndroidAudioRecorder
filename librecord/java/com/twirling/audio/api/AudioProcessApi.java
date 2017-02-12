/*
 * Copyright 2014 Google Inc. All Rights Reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twirling.audio.api;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.twirling.audio.VoiceProcessing;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Contains vertex, normal and color data.
 */
public class AudioProcessApi {
    AudioTrack audioplayer;
    private VoiceProcessing aecInst;
    //
    int frameSize = Constants.FRAME_SIZE;
    int sampleRate = Constants.SAMPLE_RATE[Constants.SAMPLE_INDEX];
    int iInChan = Constants.CHANNEL_NUM[Constants.SAMPLE_INDEX];
    //
    short[] sounddata0; //mic

    short[] sounddata1; //spk
    int []datasizeList;  //mic
    int datasize;
    //
    long instance = 0;
    int iOutChan = iInChan;
    int stopFlag = 0;

    public void init() {
        datasizeList = new int[2];
        aecInst = new VoiceProcessing();
        /* -----------------------------------------
             aecInst:  Init function.
         ------------------------------------------*/
        instance = aecInst.aecInit(frameSize, iInChan,sampleRate, 0, false);

       /* -----------------------------------------
             audioEngineSet: Config parameter setting function.

         ------------------------------------------*/
        boolean enableRes = true;
        float resLevel = 1.0f;
        boolean enableNS = false;
        float nsDB = -15.0f;
        boolean enableSpkClip = false;
        float spkClipThd = 1.0f;
        float maxCoupling = 4.0f;
        aecInst.aecSet(instance, enableRes, resLevel, enableNS, nsDB, enableSpkClip, spkClipThd, maxCoupling);
    }

    public void setMetadata(float[] metadataP) {

    }

    public void soundPlay() {
        audioplayer.play();
        int pos = 0;
        int i;
        int n;
        int tmp32;
        short[] sounddataFrame = new short[frameSize * iOutChan];
        float[] audioOutput = new float[frameSize * iOutChan];
        float[] audioInputMic = new float[frameSize * iInChan];
        float[] audioInputSpk = new float[frameSize * iInChan];
        //
        datasize = Math.min(datasizeList[0], datasizeList[1]);
        while (stopFlag != 1) {
            if (audioplayer != null) {
                for (i = 0; i < frameSize * iInChan; i++) {
                    audioInputMic[i] = (float) sounddata0[pos + i] / 32768.0f;
                    audioInputSpk[i] = (float) sounddata1[pos + i] / 32768.0f;
                }
                /* -----------------------------------------
                   audioEngineProcess: Process function.
                 ------------------------------------------*/
                aecInst.aecProcess(instance, audioInputSpk, audioInputMic);
                for (i = 0; i < frameSize * iOutChan; i++) {
                    audioOutput[i] = audioInputMic[i];
                }
                //
                for (i = 0; i < frameSize * iOutChan; i++) {
                    tmp32 = (int) (audioOutput[i] * 32768.0f);
                    if (tmp32 > 32767)
                        tmp32 = 32767;
                    else if (tmp32 < -32768)
                        tmp32 = -32768;
                    sounddataFrame[i] = (short) tmp32;
                }
                //
                audioplayer.write(sounddataFrame, 0, frameSize * iOutChan);
                pos += frameSize * iInChan;
                if (pos >= datasize / 2 - frameSize * iInChan) {
                    pos = 0;
                }
            }
        }
    }

    public void stopPlay() {
        Log.i("stopPlay!", "stopPlay");
        if (audioplayer != null) {
            if (audioplayer.getPlayState() == audioplayer.PLAYSTATE_PLAYING) {
                stopFlag = 1;
                audioplayer.stop();
                audioplayer.release();
                audioplayer = null;
                /* -----------------------------------------
                *  audioEngine.Release(): Release function.
                *  -----------------------------------------
                * */
                aecInst.aecRelease(instance);
            }
        }
    }

    public boolean LoadWavFile(String filenamein, int channelIndex) {
        Log.w("LoadWavFile", filenamein);
        try {
            // test here if the sample is a valid sample or not
            File tempfile = new File(filenamein);
            long tempfilelen = tempfile.length();
            RandomAccessFile tempfs = new RandomAccessFile(tempfile, "r");
            int TestRiff = tempfs.readByte();
            tempfs.skipBytes(7);
            int TestWav = tempfs.readByte();
            //
            // selectedfilenametext.setText("Invalid file: Not a WAV file");
            if ((TestRiff != 'R') || (TestWav != 'W')) {
                tempfs.close();
                Log.w("LoadWavFile", "Invalid file: Not a WAV file");
                return false;
            }
            // scan start position
            int scanpos = 12;
            // scan for "fmt " section to get the sample rate data
            while (true) {
                tempfs.seek(scanpos);
                int testval = tempfs.readInt();
                if (testval == 0x666d7420) {
                    break;
                }
                scanpos++;
            }
            tempfs.skipBytes(6);
            byte[] buffer = new byte[2];
            buffer[0] = tempfs.readByte();
            buffer[1] = tempfs.readByte();
            short stereoormono = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getShort();
            // Log.i("LoadWavFile ", "channel: " + stereoormono);
            //
            if ((stereoormono != iInChan)) {
                Log.i("LoadWavFile ", "iInChan not match audio wav channels!" + stereoormono + "," + iInChan);
                tempfs.close();
                return false;
            }

            // Invalid file:sample rate support 44100 or 48000
            int TestRate = tempfs.readByte();

            tempfs.skipBytes(9);
            int TestSample = tempfs.readByte();
            //
            if (TestSample != 16) {
                Log.w("LoadWavFile", "Invalid file: not 16 bit");
                tempfs.close();
                return false;
            }
            if (TestSample == 16) {
                // scan for data start position
                while (true) {
                    tempfs.seek(scanpos);
                    int datatest = tempfs.readInt();
                    // look for "data" string
                    if (datatest == 0x64617461) break;
                    scanpos++;
                }

                // get the size of WAV data we need to read
                ByteBuffer sizebuff = ByteBuffer.allocate(8);
                byte[] sizeval = new byte[4];

                tempfs.read(sizeval);
                sizebuff.put(sizeval);
                sizebuff.order(ByteOrder.LITTLE_ENDIAN);
                datasizeList[channelIndex] = sizebuff.getInt(0);

                // read the data...
                //selectedfilenametext.setText(selectedfilenametext.getText() + " : size " + new Integer(datasize).toString() + " bytes");

                // now read in the data
                // we have to go through some pain because in Java data is little endian, we need big endian
                ByteBuffer fulldata = ByteBuffer.allocate(datasizeList[channelIndex]);
                byte[] data = new byte[datasizeList[channelIndex]];
                tempfs.read(data);
                fulldata.put(data);
                fulldata.order(ByteOrder.LITTLE_ENDIAN);

                // ok now we can create an array of shorts (16 bit data) and load it
                // we are stereo 16 bit, so each sample is 2 bytes
                if(channelIndex == 0)
                    sounddata0 = new short[datasizeList[channelIndex] / 2];
                else
                    sounddata1 = new short[datasizeList[channelIndex] / 2];

                // copy data from ByteBuffer into our short buffer. Short buffer is used
                // to load the AudioTrack object
                int totalsamples = datasizeList[channelIndex] / 2;
                if(channelIndex == 0)
                    for (int counter1 = 0; counter1 < totalsamples; counter1++) {
                        sounddata0[counter1] = fulldata.getShort(counter1 * 2);
                    }
                else
                    for (int counter1 = 0; counter1 < totalsamples; counter1++) {
                        sounddata1[counter1] = fulldata.getShort(counter1 * 2);
                    }

                if(channelIndex == 0) {
                    // "frames" are two full samples (Because of stereo), so datasize/4
                    int totalnumberframes = datasizeList[channelIndex] / 4;

                    // create the audio track, load it, play it
                    //audioplayer = new AudioTrack(AudioManager.STREAM_MUSIC,44100,AudioFormat.CHANNEL_OUT_STEREO,AudioFormat.ENCODING_PCM_16BIT,datasize,AudioTrack.MODE_STATIC);
                    int minBufSize = AudioTrack.getMinBufferSize(sampleRate,
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);
                    Log.i("buffersize ", "buffersize = " + minBufSize);
                    audioplayer = new AudioTrack(AudioManager.STREAM_MUSIC,
                            sampleRate,
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            minBufSize,
                            AudioTrack.MODE_STREAM);
                    audioplayer.setLoopPoints(0, totalnumberframes, 1);
                }
                // this will cause a seamless loop
                tempfs.close();
                return true;
            }
        } catch (Exception i) {
            Log.w("LoadWavFile", i.toString());
            return false;
        }
        return false;
    }
}