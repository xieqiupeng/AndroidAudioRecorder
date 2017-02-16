package com.twirling.audio.model;

/**
 * Created by xieqi on 2017/2/15.
 */

public class Sounddata1 {
	private static Sounddata1 instance = null;

	private Sounddata1() {
	}

	public static Sounddata1 getInstance() {
		if (instance == null) {
			instance = new Sounddata1();
		}
		return instance;
	}

	public void setShorts(short[] shorts) {
		this.shorts = shorts;
	}

	private static final int FRAMESIZE = 512;
	private static final int CHANNEL = 1;
	public short[] shorts;
	public int numberOfShortsRead = 0;
	float[] audioInputMic = new float[FRAMESIZE * CHANNEL];
	float[] audioInputSpk = new float[FRAMESIZE * CHANNEL];

	public boolean isEmpty() {
		if (shorts == null) {
			return true;
		}
		return false;
	}

	public byte[] toBytes() {
		int shortIndex, byteIndex;
		byte[] buffer = new byte[numberOfShortsRead * 2];
		shortIndex = byteIndex = 0;
		for (; shortIndex != numberOfShortsRead; ) {
			buffer[byteIndex] = (byte) (shorts[shortIndex] & 0x00FF);
			buffer[byteIndex + 1] = (byte) ((shorts[shortIndex] & 0xFF00) >> 8);
			++shortIndex;
			byteIndex += 2;
		}
		return buffer;
	}
}
