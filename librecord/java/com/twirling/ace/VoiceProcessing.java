package com.twirling.ace;

public class VoiceProcessing {
	public VoiceProcessing() {
	}

	public native long aecInit(int var1, int var2, int var3, int var4, boolean var5);

	public native void aecSet(long var1, boolean var3, float var4, boolean var5, float var6, boolean var7, float var8, float var9);

	public native void aecProcess(long var1, float[] var3, float[] var4);

	public native void aecRelease(long var1);

	static {
		System.loadLibrary("VoiceProcessing");
	}
}
