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
import com.twirling.libaec.model.SurfaceModel;

/**
 * Contains vertex, normal and color data.
 */
public class AudioAecApi {
	//
	private VoiceProcessing aecInst;
	private static final int FRAME_SIZE = 256;
	private static final int SAMPLE_RATE = 44100;
	private static final int iInChan = 1;
	private static final int iOutChan = 1;
	private long instance = 0;
	//
	boolean enableRes = true;
	float resLevel = 0.5f;
	boolean enableNS = true;
	float nsDB = -10.0f;
	boolean enableSpkClip = false;
	float spkClipThd = 1.0f;
	float maxCoupling = 10.0f;

	public void init() {
		aecInst = new VoiceProcessing();
		instance = aecInst.aecInit(FRAME_SIZE, iInChan, SAMPLE_RATE, 7500, false);
		aecInst.aecSet(instance, enableRes, resLevel, enableNS, nsDB, enableSpkClip, spkClipThd, maxCoupling);
	}

	public void doProcess(short[] mic, short[] spk) {
		int i;
		int tmp32;
		float[] audioOutput = new float[FRAME_SIZE * iOutChan];
		float[] audioInputMic = new float[FRAME_SIZE * iInChan];
		float[] audioInputSpk = new float[FRAME_SIZE * iInChan];
		//
		for (i = 0; i < FRAME_SIZE * iInChan; i++) {
			audioInputMic[i] = (float) mic[i] / 32768.0f;
			audioInputSpk[i] = (float) spk[i] / 32768.0f;
		}
		for (i = 0; i < FRAME_SIZE * iOutChan; i++) {
			audioOutput[i] = audioInputMic[i];
		}
		if (SurfaceModel.getInstance().isAnsTurnOn()) {
			enableNS = true;
		} else {
			enableNS = false;
		}
		aecInst.aecSet(instance, enableRes, resLevel, enableNS, nsDB, enableSpkClip, spkClipThd, maxCoupling);
		aecInst.aecProcess(instance, audioInputSpk, audioInputMic);
		if (SurfaceModel.getInstance().isAecTurnOn()) {
			for (i = 0; i < FRAME_SIZE * iOutChan; i++) {
				audioOutput[i] = audioInputMic[i];
			}
		}
		for (i = 0; i < FRAME_SIZE * iOutChan; i++) {
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