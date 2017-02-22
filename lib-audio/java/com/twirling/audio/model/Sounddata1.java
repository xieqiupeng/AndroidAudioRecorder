package com.twirling.audio.model;

/**
 * Created by xieqi on 2017/2/15.
 */

public class Sounddata1 {
	private static Sounddata1 instance = null;

	private Sounddata1() {
		wpt = 0;
		rpt = 0;
		spkCircleBuf = new short[FRAMESIZE * MAXFRAMES];
	}

	public synchronized static Sounddata1 getInstance() {
		if (instance == null) {
			instance = new Sounddata1();
		}
		return instance;
	}

	private static final int FRAMESIZE = 256;
	private static final int MAXFRAMES = 10;
	private int wpt = 0;
	private int rpt = 0;
	private short[] spkCircleBuf = null;
	private int numberOfShortsRead = 0;
	private boolean firstFlag = true;

	public void release() {
		wpt = 0;
		rpt = 0;
		spkCircleBuf = null;
		instance = null;
	}

	public void setSpkCircleBuf(short[] src) {
		try {
			System.arraycopy(src, 0, spkCircleBuf, wpt, src.length);
			wpt += FRAMESIZE;
			if (wpt >= FRAMESIZE * MAXFRAMES) {
				wpt = 0;
			}
		} catch (Exception e) {
		}
	}

	public void getSpkCircleBuf(short[] src) {
		try {
			if (firstFlag == true) {
				if (wpt <= 0) {
					return;
				}
				firstFlag = false;
				rpt = wpt - FRAMESIZE;
				if (rpt < 0) rpt += FRAMESIZE * MAXFRAMES;
			}
			System.arraycopy(spkCircleBuf, rpt, src, 0, src.length);
			rpt += FRAMESIZE;
			if (rpt >= FRAMESIZE * MAXFRAMES) {
				rpt = 0;
			}
		} catch (Exception e) {
		}
//		Log.w("wpt_rpt", wpt + ", " + rpt);
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

	public int getWpt() {
		return wpt;
	}

	public int getRpt() {
		return rpt;
	}
}
