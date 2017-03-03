/*
 * Copyright (C) 2016 Kailash Dabhi (Kingbull Technology)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package omrecorder;

import android.media.AudioRecord;
import android.util.Log;

import com.twirling.audio.api.AudioProcessApi;
import com.twirling.audio.model.Sounddata1;
import com.twirling.libaec.api.AudioAecApi;

import java.io.IOException;
import java.io.OutputStream;

import omrecorder.widget.FileUtil;
import omrecorder.widget.RealtimeDemo;

/**
 * A PullTransport is a object who pulls the data from {@code AudioSource} and transport it to
 * OutputStream
 *
 * @author Kailash Dabhi (kailash09dabhi@gmail.com)
 * @date 06-07-2016
 * @skype kailash.09
 */
public interface PullTransport {

	/**
	 * It starts to pull the {@code  AudioSource}  and transport it to
	 * OutputStream
	 *
	 * @param outputStream the OutputStream where we want to transport the pulled audio data.
	 * @throws IOException if there is any problem arise in pulling and transporting
	 */
	void start(OutputStream outputStream) throws IOException;

	//It immediately stop pulling {@code  AudioSource}
	void stop();

	//Returns the source which is used for pulling
	AudioSource source();

	/**
	 * Interface definition for a callback to be invoked when a chunk of audio is pulled from {@code
	 * AudioSource}.
	 */
	interface OnAudioChunkPulledListener {
		/**
		 * Called when {@code AudioSource} is pulled and returned{@code AudioChunk}.
		 */
		void onAudioChunkPulled(AudioChunk audioChunk);
	}

	abstract class AbstractPullTransport implements PullTransport {
		final AudioSource audioRecordSource;
		final OnAudioChunkPulledListener onAudioChunkPulledListener;
		private final UiThread uiThread = new UiThread();

		AbstractPullTransport(AudioSource audioRecordSource,
		                      OnAudioChunkPulledListener onAudioChunkPulledListener) {
			this.audioRecordSource = audioRecordSource;
			this.onAudioChunkPulledListener = onAudioChunkPulledListener;
		}

		@Override
		public void start(OutputStream outputStream) throws IOException {
			startPoolingAndWriting(preparedSourceToBePulled(), audioRecordSource.minimumBufferSize(),
					outputStream);
		}

		void startPoolingAndWriting(AudioRecord audioRecord, int minimumBufferSize,
		                            OutputStream outputStream) throws IOException {
		}

		@Override
		public void stop() {
			audioRecordSource.isEnableToBePulled(false);
			audioRecordSource.audioRecorder().stop();
		}

		public AudioSource source() {
			return audioRecordSource;
		}

		AudioRecord preparedSourceToBePulled() {
			final AudioRecord audioRecord = audioRecordSource.audioRecorder();
			audioRecord.startRecording();
			audioRecordSource.isEnableToBePulled(true);
			return audioRecord;
		}

		void postSilenceEvent(final Recorder.OnSilenceListener onSilenceListener,
		                      final long silenceTime) {
			uiThread.execute(new Runnable() {
				@Override
				public void run() {
					onSilenceListener.onSilence(silenceTime);
				}
			});
		}

		void postPullEvent(final AudioChunk audioChunk) {
			uiThread.execute(new Runnable() {
				@Override
				public void run() {
					onAudioChunkPulledListener.onAudioChunkPulled(audioChunk);
				}
			});
		}
	}

	final class Default extends AbstractPullTransport {
		private final WriteAction writeAction;
		private final AudioChunk.Shorts audioChunk;
		private final int FRAMESIZE = 512;
		private short[] aecInputMic = new short[FRAMESIZE / 2];
		private short[] aecInputSpk = new short[FRAMESIZE / 2];
		private int recordFrameSize = 0;
		private int recordFrameNum = 0;
		private AudioAecApi audioAecApi;
		private AudioProcessApi audioProcessApi;
		String fileName = "sdcard/music/mono.wav";

		public void stopProcess() {
			if (audioProcessApi != null) {
				audioProcessApi.stopPlay();
			}
			if (audioAecApi != null) {
//				audioAecApi.stopProcess();
			}
			Sounddata1.getInstance().release();
		}

		public Default(AudioSource audioRecordSource,
		               OnAudioChunkPulledListener onAudioChunkPulledListener,
		               WriteAction writeAction) {
			super(audioRecordSource, onAudioChunkPulledListener);
			this.writeAction = writeAction;
			recordFrameSize = audioRecordSource.minimumBufferSize();
			recordFrameNum = (int) (Math.ceil((float) recordFrameSize / FRAMESIZE)); //check
			recordFrameSize = recordFrameNum * FRAMESIZE;
			audioChunk = new AudioChunk.Shorts(new short[recordFrameSize / 2]);
			Log.w("xqp", audioChunk.shorts.length + "");

			audioProcessApi = new AudioProcessApi();
			audioProcessApi.init();
			audioProcessApi.LoadWavFile(fileName);
			//int sysdelayEst = (recordFrameSize/2) + (audioProcessApi.getPlayBufferSize()*2);
			int sysdelayEst = 11000;

			// 处理aec
			audioAecApi = new AudioAecApi();
			audioAecApi.init(sysdelayEst);
		}

		public Default(AudioSource audioRecordSource,
		               OnAudioChunkPulledListener onAudioChunkPulledListener) {
			this(audioRecordSource,
					onAudioChunkPulledListener,
					new WriteAction.Default());
		}

		@Override
		void startPoolingAndWriting(AudioRecord audioRecord, int minimumBufferSize,
		                            OutputStream outputStream) throws IOException {
			OutputStream outputStream1 = FileUtil.getOutputStream();
			while (audioRecordSource.isEnableToBePulled()) {
				audioChunk.numberOfShortsRead = audioRecord.read(audioChunk.shorts, 0, audioChunk.shorts.length);
//				Log.w("num", audioChunk.numberOfShortsRead + ",  " + audioChunk.shorts.length);
				//
				if (AudioRecord.ERROR_INVALID_OPERATION != audioChunk.numberOfShortsRead) {
					if (onAudioChunkPulledListener != null) {
						postPullEvent(audioChunk);
					}
				}
				try {
					int n = 0;
					int n2 = 0;
					for (int i = 0; i < recordFrameNum; i++) {
						audioProcessApi.soundPlay();
						Sounddata1.getInstance().getSpkCircleBuf(aecInputSpk);
						for (int j = 0; j < FRAMESIZE / 2; j++) {
							aecInputMic[j] = audioChunk.shorts[n++];
						}
						writeAction.execute(toBytes(aecInputMic), outputStream1);
						if (audioAecApi != null) {
							audioAecApi.doProcess(aecInputMic, aecInputSpk);
						}
						for (int j = 0; j < FRAMESIZE / 2; j++) {
							audioChunk.shorts[n2++] = aecInputMic[j];
						}
						writeAction.execute(toBytes(aecInputMic), outputStream);
					}
				} catch (Exception e) {
					Log.w("", e.toString());
				}
			}
		}

		public byte[] toBytes(short[] shorts) {
			int shortIndex, byteIndex;
			byte[] buffer = new byte[shorts.length * 2];
			shortIndex = byteIndex = 0;
			for (; shortIndex != shorts.length; ) {
				buffer[byteIndex] = (byte) (shorts[shortIndex] & 0x00FF);
				buffer[byteIndex + 1] = (byte) ((shorts[shortIndex] & 0xFF00) >> 8);
				++shortIndex;
				byteIndex += 2;
			}
			return buffer;
		}
	}

	final class Realtime extends PullTransport.AbstractPullTransport {
		private final WriteAction writeAction;
		private final AudioChunk.Shorts audioChunk;
		private int recordFrameSize = 0;
		private AudioProcessApi audioProcessApi;
		private RealtimeDemo realtimeDemo;

		public Realtime(AudioSource audioRecordSource,
		                OnAudioChunkPulledListener onAudioChunkPulledListener) {
			super(audioRecordSource, onAudioChunkPulledListener);
			this.writeAction = new WriteAction.Default();
			recordFrameSize = audioRecordSource.minimumBufferSize();
			audioChunk = new AudioChunk.Shorts(new short[recordFrameSize]);
			audioProcessApi = new AudioProcessApi();
			audioProcessApi.initAudioTrack();
			//
			realtimeDemo = new RealtimeDemo();
			realtimeDemo.start();
		}

		public void stopProcess() {
			if (audioProcessApi != null) {
				audioProcessApi.stopPlay();
			}
			realtimeDemo.shutDown();
			Sounddata1.getInstance().release();
		}

		@Override
		void startPoolingAndWriting(AudioRecord audioRecord, int minimumBufferSize,
		                            OutputStream outputStream) throws IOException {
			while (audioRecordSource.isEnableToBePulled()) {
				audioChunk.numberOfShortsRead = audioRecord.read(audioChunk.shorts, 0, audioChunk.shorts.length);
				//
				if (AudioRecord.ERROR_INVALID_OPERATION != audioChunk.numberOfShortsRead) {
					if (onAudioChunkPulledListener != null) {
						postPullEvent(audioChunk);
					}
				}
//				if (audioChunk.numberOfShortsRead <= 0) {
//					return;
//				}
//				realtimeDemo.hearIt();
//				realtimeDemo.hearByte(audioChunk.toBytes());
				writeAction.execute(audioChunk.toBytes(), outputStream);
//				audioProcessApi.soundPlay(audioChunk.shorts);
			}
		}
	}
}
