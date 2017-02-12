package com.twirling.audio.api;

/**
 * Created by xieqiupeng on 2016/11/9.
 */
public class Constants {
    // Sample demo Index ，from 0  3
    public static int SAMPLE_INDEX = 2;

    /**
     * Sample FILE_NAME
     * Four music
     */
    public static final String[] FILE_NAME = {
            "left2right_sn3d.wav",
            "BestPulse5.1.wav",
            "mono.wav",
            "stereo.wav"};

    /**
     * Sample PROFILE_ID
     * 1:  B format (wxyz) to binaural
     * 5:  5.1 to binaural
     * 11: Objects to binaural
     * 13: Stereo to binaural
     */
    public static final int[] PROFILE_ID = {
            1,
            5,
            11,
            13
    };

    /**
     * Sample channel num
     * Input audio channels. 1 -- 128
     */
    public static final int[] CHANNEL_NUM = {
            4,
            6,
            1,
            2
    };

    /**
     * Sample rate
     * Could be 44100 or 48000 now.
     */
    public static final int[] SAMPLE_RATE = {
            44100,
            44100,
            44100,
            44100
    };

    /**
     * Equal or less than 512 now.
     */
    public static final int FRAME_SIZE = 512;
}
