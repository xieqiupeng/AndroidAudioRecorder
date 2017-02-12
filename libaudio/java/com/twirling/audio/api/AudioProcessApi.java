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

import com.twirling.audio.AudioEngine;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Contains vertex, normal and color data.
 */
public class AudioProcessApi {

    private AudioEngine audioEngine;
    private AudioTrack audioplayer;
    //
    private int frameSize = Constants.FRAME_SIZE;
    private int sampleRate = Constants.SAMPLE_RATE[Constants.SAMPLE_INDEX];
    private int profileID = Constants.PROFILE_ID[Constants.SAMPLE_INDEX];
    private int iInChan = Constants.CHANNEL_NUM[Constants.SAMPLE_INDEX];
    //
    private short[] sounddata;
    private float[] metadata;
    private int datasize;
    //
    private long instance = 0;
    private int iOutChan = 2;
    private float azi;
    private float elv;
    private float raw;
    private int stopFlag = 0;

    public void init() {
        if (profileID != 1 && profileID != 5 && profileID != 11 && profileID != 13) {
            Log.i("Audio", "profileID: " + profileID + " is not supported");
            return;
        }
        audioEngine = new AudioEngine();
        /* -----------------------------------------
             audioInit:  Init function.
             profileID:
             1:  B format (wyzx sn3d) to binaural
             5:  5.1 to binaural
             11: objects to binaural
             13: stereo to binaural
             FrameLength:
             Equal or less than 512.
             Nchannels:
             Input audio channels. 1 -- 128
             SAMPLE_RATE:
             Must be 44100 now.
         ------------------------------------------*/
        instance = audioEngine.audioInit(profileID, frameSize, iInChan, sampleRate, false);

       /* -----------------------------------------
             audioEngineSet: Config parameter setting function.
             reverbOn:
             Flag of turning on reverb, default false.
             reverbLevel:
             Level of reverb, 0.0 -- 1.0.
             virtualSpk:
             Flag of turning on virtual speaker mode, default false.
             dycOn:
             Flag of turning on dynamic range control, default false.
             dycMaxValue:
             Max input data value of dynamic range control.
         ------------------------------------------*/
//        audioEngine.audioSet(instance, false, 0, false, false, 1.0f);
        audioEngine.audioSet(instance, false, 0, false, true, 1.0f);
//        audioEngine.audioSet(instance, true, 0.1f, false, true, 1.0f);
//
        float angleFactor = (float) (Math.PI / 180);
        metadata = new float[3 * iInChan];
        for (int i = 0; i < iInChan; i += 3) {
            metadata[i] = 2;
            metadata[i + 1] = 0;
            metadata[i + 2] = 0;
        }
    }

    public void setMetadata(float[] metadataP) {
        elv = metadataP[0];
        azi = metadataP[1];
        raw = metadataP[2];
//        Log.w("set", azi + "  " + elv + "  " + raw);
    }

    public void soundPlay() {
        audioplayer.play();
        int pos = 0;
        int i;
        int n;
        short[] sounddataFrame = new short[frameSize * iOutChan];
        float[] binauralOutput = new float[frameSize * iOutChan];
        float[] audioInput = new float[frameSize * iInChan];
        //
        while (stopFlag != 1) {
            if (audioplayer != null) {
                for (i = 0; i < frameSize * iInChan; i++) {
                    audioInput[i] = (float) sounddata[pos + i] / 32768.0f;
                }
                /* -----------------------------------------
                   audioEngineProcess: Process function.
                   heading:
                     Heading angle of head in RADIAN measure, -PI -- PI, positive when head to left.
                   pitch:
                     Pitch angle of head in RADIAN measure, -PI -- PI, positive when head down.
                   bank:
                     Bank angle of head in RADIAN measure, -PI -- PI, positive when head to right.
                   objectInput:
                     interlaced multichannel input audio data buffer.Data in this buffer will not be modified during processing.
                   binauralOutput:
                     interlaced binaural output audio data buffer.
                   metaDataFrame: (only necessary for profile 11)
                     object audio position metadata buffer. The buffer length is 3*objectNumber.
                   Format:
                     r1, azi1, elv1, r2, azi2, elv2, ...
                   where:
                     ri is distance of object i;
                     azii is azimuth angle of object i;
                     elvi is elevation angle of object i.
                 ------------------------------------------*/
                audioEngine.audioProcess(instance, azi, elv, 0, audioInput, binauralOutput, metadata);
                //
                for (i = 0; i < frameSize * iOutChan; i++) {
                    sounddataFrame[i] = (short) (binauralOutput[i] * 32768.0f);
                }
                //  FileUtil.writeFileFromShort(sounddataFrame);
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
            if (audioplayer.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                stopFlag = 1;
                audioplayer.stop();
                audioplayer.release();
                audioplayer = null;
                /* -----------------------------------------
                *  audioEngine.Release(): Release function.
                *  -----------------------------------------
                * */
                audioEngine.audioRelease(instance);
            }
        }
    }

    public boolean LoadWavFile(String filenamein) {
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
//            Log.i("LoadWavFile ", "channel: " + stereoormono);

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
                datasize = sizebuff.getInt(0);

                // read the data...
                //selectedfilenametext.setText(selectedfilenametext.getText() + " : size " + new Integer(datasize).toString() + " bytes");

                // now read in the data
                // we have to go through some pain because in Java data is little endian, we need big endian
                ByteBuffer fulldata = ByteBuffer.allocate(datasize);
                byte[] data = new byte[datasize];
                tempfs.read(data);
                fulldata.put(data);
                fulldata.order(ByteOrder.LITTLE_ENDIAN);

                // ok now we can create an array of shorts (16 bit data) and load it
                // we are stereo 16 bit, so each sample is 2 bytes
                sounddata = new short[datasize / 2];

                // copy data from ByteBuffer into our short buffer. Short buffer is used
                // to load the AudioTrack object
                int totalsamples = datasize / 2;
                for (int counter1 = 0; counter1 < totalsamples; counter1++) {
                    sounddata[counter1] = fulldata.getShort(counter1 * 2);
                }

                // "frames" are two full samples (Because of stereo), so datasize/4
                int totalnumberframes = datasize / 4;

                // create the audio track, load it, play it
                //audioplayer = new AudioTrack(AudioManager.STREAM_MUSIC,44100,AudioFormat.CHANNEL_OUT_STEREO,AudioFormat.ENCODING_PCM_16BIT,datasize,AudioTrack.MODE_STATIC);
                int minBufSize = AudioTrack.getMinBufferSize(sampleRate,
                        AudioFormat.CHANNEL_OUT_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT);
                Log.i("buffersize ", "buffersize = " + minBufSize);
                audioplayer = new AudioTrack(AudioManager.STREAM_MUSIC,
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        minBufSize,
                        AudioTrack.MODE_STREAM);
                audioplayer.setLoopPoints(0, totalnumberframes, 100);

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