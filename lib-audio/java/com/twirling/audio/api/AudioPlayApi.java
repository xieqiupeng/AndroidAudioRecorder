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

import android.media.AudioTrack;
import android.util.Log;

import com.twirling.audio.model.Sounddata1;

/**
 * Contains vertex, normal and color data.
 */
public class AudioPlayApi {

	private AudioTrack audioplayer;
	//
	private int frameSize = Constants.FRAME_SIZE;
	private int sampleRate = Constants.SAMPLE_RATE[Constants.SAMPLE_INDEX];
	private int profileID = Constants.PROFILE_ID[Constants.SAMPLE_INDEX];
	private int iInChan = Constants.CHANNEL_NUM[Constants.SAMPLE_INDEX];
	private int datasize;
	private long instance = 0;
	private int iOutChan = iInChan;

	private int stopFlag = 0;
	private int pos = 0;
	private int minbufsize = 0;

	public void init() {

	}

	public void soundPlay(short[] sounddata) {
		int i;
		int n;
		short[] sounddataFrame = new short[frameSize * iOutChan];
		//
		if (stopFlag != 1) {
			if (audioplayer != null) {
				for (i = 0; i < frameSize * iInChan; i++) {
					sounddataFrame[i] = sounddata[pos + i];
				}
				Sounddata1.getInstance().setSpkCircleBuf(sounddataFrame);
				for (i = 0; i < frameSize * iInChan; i++) {
					sounddataFrame[i] = (short)(sounddataFrame[i]/4);
				}
				//  FileUtil.writeFileFromShort(sounddataFrame);
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
				//Sounddata1.getInstance().release();
				audioplayer.stop();
				audioplayer.release();
				audioplayer = null;

			}
		}
	}
}