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

package com.twirling.audioRun.api;

import com.twirling.audio.VoiceProcessing;

/**
 * Contains vertex, normal and color data.
 */
public class AudioAecApi {
	//
	private VoiceProcessing aecInst;
	int frameSize = Constants.FRAME_SIZE;
	int sampleRate = Constants.SAMPLE_RATE[Constants.SAMPLE_INDEX];
	int iInChan = 1;
	int iOutChan = 1;
	long instance = 0;
	boolean enableAec = false;

	public void init() {
		aecInst = new VoiceProcessing();
		instance = aecInst.aecInit(frameSize, iInChan, sampleRate, 7500, false);
		//
		boolean enableRes = true;
		float resLevel = 0.5f;
		boolean enableNS = true;
		float nsDB = -10.0f;
		boolean enableSpkClip = false;
		float spkClipThd = 1.0f;
		float maxCoupling = 10.0f;
		aecInst.aecSet(instance, enableRes, resLevel, enableNS, nsDB, enableSpkClip, spkClipThd, maxCoupling);
	}

	public void doProcess(short[] mic, short[] spk) {
		int i;
		int tmp32;
		float[] audioOutput = new float[frameSize * iOutChan];
		float[] audioInputMic = new float[frameSize * iInChan];
		float[] audioInputSpk = new float[frameSize * iInChan];
		//
		for (i = 0; i < frameSize * iInChan; i++) {
			audioInputMic[i] = (float) mic[i] / 32768.0f;
			audioInputSpk[i] = (float) spk[i] / 32768.0f;
		}
		for (i = 0; i < frameSize * iOutChan; i++) {
			audioOutput[i] = audioInputMic[i];
		}
		if (enableAec == true) {
			aecInst.aecProcess(instance, audioInputSpk, audioInputMic);
			for (i = 0; i < frameSize * iOutChan; i++) {
				audioOutput[i] = audioInputMic[i];
			}
		}
		//
		for (i = 0; i < frameSize * iOutChan; i++) {
			tmp32 = (int) (audioOutput[i] * 32768.0f);
			if (tmp32 > 32767)
				tmp32 = 32767;
			else if (tmp32 < -32768)
				tmp32 = -32768;
			mic[i] = (short) tmp32;
		}
	}

	public void stopProcess() {
		aecInst.aecRelease(instance);
	}
}