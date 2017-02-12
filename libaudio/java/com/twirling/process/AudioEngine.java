package com.twirling.process;

/**
 * Created by xieqi on 2016/11/17.
 */
public class AudioEngine {
    public AudioEngine() {
    }

    public native long audioInit(int var1, int var2, int var3, int var4, boolean var5);

    public native void audioSet(long var1, boolean var3, float var4, boolean var5, boolean var6, float var7);

    public native void audioProcess(long var1, float var3, float var4, float var5, float[] var6, float[] var7, float[] var8);

    public native void audioRelease(long var1);

    static {
        System.loadLibrary("TwirlingAudioEngine");
    }
}