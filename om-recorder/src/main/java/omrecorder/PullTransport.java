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
import android.media.audiofx.AcousticEchoCanceler;
import android.util.Log;

import com.twirling.audio.model.Sounddata1;
import com.twirling.audioRun.api.AudioAecApi;

import java.io.IOException;
import java.io.OutputStream;

import omrecorder.widget.FileUtil;

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
		private final WriteAction writeAction2;
		private final AudioChunk.Shorts audioChunk;
		private final int FRAMESIZE = 512;
		private short[] aecInputMic = new short[FRAMESIZE / 2];
		private short[] aecInputSpk = new short[FRAMESIZE / 2];
		private int recordFrameSize = 0;
		private int recordFrameNum = 0;
		private AudioAecApi audioAecApi;

		public void stopProcess() {
			audioAecApi.stopProcess();
		}

		public Default(AudioSource audioRecordSource,
		               OnAudioChunkPulledListener onAudioChunkPulledListener,
		               WriteAction writeAction) {
			super(audioRecordSource, onAudioChunkPulledListener);
			this.writeAction = writeAction;
			writeAction2 = new WriteAction.Default();
			recordFrameSize = audioRecordSource.minimumBufferSize();
			recordFrameNum = (int) (Math.ceil((float) recordFrameSize / FRAMESIZE)); //check
			recordFrameSize = recordFrameNum * FRAMESIZE;
			audioChunk = new AudioChunk.Shorts(new short[recordFrameSize / 2]);
//			Log.w("xqp", audioChunk.shorts.length + "");
			// 处理aec
			audioAecApi = new AudioAecApi();
			audioAecApi.init();
		}

		public Default(AudioSource audioRecordSource, WriteAction writeAction) {
			this(audioRecordSource, null, writeAction);
		}

		public Default(AudioSource audioRecordSource,
		               OnAudioChunkPulledListener onAudioChunkPulledListener) {
			this(audioRecordSource,
					onAudioChunkPulledListener,
					new WriteAction.Default());
		}

		public Default(AudioSource audioRecordSource) {
			this(audioRecordSource,
					null,
					new WriteAction.Default());
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

		@Override
		void startPoolingAndWriting(AudioRecord audioRecord, int minimumBufferSize,
		                            OutputStream outputStream) throws IOException {
			AcousticEchoCanceler aec = AcousticEchoCanceler.create(audioRecord.getAudioSessionId());
			aec.setEnabled(true);
			OutputStream outputStream1 = FileUtil.getOutputStream();
			while (audioRecordSource.isEnableToBePulled()) {
				Log.w(PullTransport.class.getSimpleName(), audioRecord.getAudioSessionId() + " "
						+ aec.getEnabled() + " "
						+ aec.getId() + " "
						+ aec.getDescriptor() + " "
						+ AcousticEchoCanceler.isAvailable());
				audioChunk.numberOfShortsRead = audioRecord.read(audioChunk.shorts, 0, audioChunk.shorts.length);
//				Log.w("num", audioChunk.numberOfShortsRead + ",  " + audioChunk.shorts.length);
				//
				if (AudioRecord.ERROR_INVALID_OPERATION != audioChunk.numberOfShortsRead) {
					if (onAudioChunkPulledListener != null) {
						postPullEvent(audioChunk);
					}
				}
				Log.w("123", Sounddata1.getInstance().spkCircleBuf.length + "");
				try {
					int n = 0;
					int n2 = 0;
					for (int i = 0; i < recordFrameNum; i++) {
						Sounddata1.getInstance().getSpkCircleBuf(aecInputSpk);
						for (int j = 0; j < FRAMESIZE / 2; j++) {
							aecInputMic[j] = audioChunk.shorts[n++];
						}
//						audioAecApi.doProcess(aecInputMic, aecInputSpk);
						for (int j = 0; j < FRAMESIZE / 2; j++) {
							audioChunk.shorts[n2++] = aecInputMic[j];
						}
					}
//					writeAction.execute(toBytes(audioChunk.shorts), outputStream);
//					toBytes(aecInputMic)
//					audioChunk.toBytes()
					writeAction.execute(toBytes(audioChunk.shorts), outputStream1);
				} catch (Exception e) {
					Log.w("", e.toString());
				}
			}
		}
	}

	final class Noise extends AbstractPullTransport {

		private final AudioChunk.Shorts audioChunk;
		private final long silenceTimeThreshold;
		private final Recorder.OnSilenceListener silenceListener;
		private final WriteAction writeAction;
		private long firstSilenceMoment = 0;
		private int noiseRecordedAfterFirstSilenceThreshold = 0;

		public Noise(AudioSource audioRecordSource,
		             OnAudioChunkPulledListener onAudioChunkPulledListener, WriteAction writeAction,
		             Recorder.OnSilenceListener silenceListener, long silenceTimeThreshold) {
			super(audioRecordSource, onAudioChunkPulledListener);
			this.writeAction = writeAction;
			this.silenceListener = silenceListener;
			this.silenceTimeThreshold = silenceTimeThreshold;
			audioChunk = new AudioChunk.Shorts(new short[audioRecordSource.minimumBufferSize()]);
		}

		public Noise(AudioSource audioRecordSource,
		             OnAudioChunkPulledListener onAudioChunkPulledListener,
		             Recorder.OnSilenceListener silenceListener, long silenceTimeThreshold) {
			this(audioRecordSource, onAudioChunkPulledListener, new WriteAction.Default(),
					silenceListener, silenceTimeThreshold);
		}

		public Noise(AudioSource audioRecordSource, WriteAction writeAction,
		             Recorder.OnSilenceListener silenceListener, long silenceTimeThreshold) {
			this(audioRecordSource, null, writeAction, silenceListener, silenceTimeThreshold);
		}

		public Noise(AudioSource audioRecordSource, Recorder.OnSilenceListener silenceListener,
		             long silenceTimeThreshold) {
			this(audioRecordSource, null, new WriteAction.Default(), silenceListener,
					silenceTimeThreshold);
		}

		public Noise(AudioSource audioRecordSource, Recorder.OnSilenceListener silenceListener) {
			this(audioRecordSource, null, new WriteAction.Default(), silenceListener, 200);
		}

		public Noise(AudioSource audioRecordSource) {
			this(audioRecordSource, null, new WriteAction.Default(), null, 200);
		}

		@Override
		public void start(OutputStream outputStream) throws IOException {
			final AudioRecord audioRecord = audioRecordSource.audioRecorder();
			audioRecord.startRecording();
			audioRecordSource.isEnableToBePulled(true);
			while (audioRecordSource.isEnableToBePulled()) {
				audioChunk.numberOfShortsRead =
						audioRecord.read(audioChunk.shorts, 0, audioChunk.shorts.length);
				if (AudioRecord.ERROR_INVALID_OPERATION != audioChunk.numberOfShortsRead) {
					if (onAudioChunkPulledListener != null) {
						postPullEvent(audioChunk);
					}
					if (audioChunk.peakIndex() > -1) {
						writeAction.execute(audioChunk.toBytes(), outputStream);
						firstSilenceMoment = 0;
						noiseRecordedAfterFirstSilenceThreshold++;
					} else {
						if (firstSilenceMoment == 0) {
							firstSilenceMoment = System.currentTimeMillis();
						}
						final long silenceTime = System.currentTimeMillis() - firstSilenceMoment;
						if (firstSilenceMoment != 0 && silenceTime > this.silenceTimeThreshold) {
							if (silenceTime > 1000) {
								if (noiseRecordedAfterFirstSilenceThreshold >= 3) {
									noiseRecordedAfterFirstSilenceThreshold = 0;
									if (silenceListener != null) {
										postSilenceEvent(silenceListener, silenceTime);
									}
								}
							}
						} else {
							writeAction.execute(audioChunk.toBytes(), outputStream);
						}
					}
				}
			}
		}
	}
}
