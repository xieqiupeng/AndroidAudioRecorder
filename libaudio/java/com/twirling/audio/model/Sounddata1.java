package com.twirling.audio.model;

import android.util.Log;

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

	private static final int FRAMESIZE = 256;
	private static final int MAXFRAMES = 100;
	public short[] spkCircleBuf = new short[FRAMESIZE * MAXFRAMES];
	public int numberOfShortsRead = 0;
	public int wpt = 0;
	public int rpt = 0;

	public void setSpkCircleBuf(short[] src) {
		System.arraycopy(src, 0, spkCircleBuf, wpt, src.length);
		wpt += FRAMESIZE;
		if (wpt >= FRAMESIZE * MAXFRAMES) {
			wpt = 0;
		}
	}

	public void getSpkCircleBuf(short[] src) {
		System.arraycopy(spkCircleBuf, rpt, src, 0, src.length);
		rpt += FRAMESIZE;
		if (rpt >= FRAMESIZE * MAXFRAMES) {
			rpt = 0;
		}
		Log.w("wpt&rpt", wpt + ", " + rpt);
	}

	public boolean isEmpty() {
		if (spkCircleBuf == null) {
			return true;
		}
		return false;
	}

	public byte[] toBytes() {
		int shortIndex, byteIndex;
		byte[] buffer = new byte[numberOfShortsRead * 2];
		shortIndex = byteIndex = 0;
		for (; shortIndex != numberOfShortsRead; ) {
			buffer[byteIndex] = (byte) (spkCircleBuf[shortIndex] & 0x00FF);
			buffer[byteIndex + 1] = (byte) ((spkCircleBuf[shortIndex] & 0xFF00) >> 8);
			++shortIndex;
			byteIndex += 2;
		}
		return buffer;
	}
}
