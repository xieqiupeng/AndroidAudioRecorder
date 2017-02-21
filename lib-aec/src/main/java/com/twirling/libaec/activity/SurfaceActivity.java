package com.twirling.libaec.activity;

import android.Manifest;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.twirling.libaec.R;
import com.twirling.libaec.api.AudioProcessApi;
import com.twirling.libaec.util.PermissionUtil;
import com.twirling.libaec.databinding.ActivitySurfaceBinding;
import com.twirling.libaec.model.SurfaceModel;

public class SurfaceActivity extends AppCompatActivity {
	private AudioProcessApi audioACEApi;
	private Thread audioThread;
	private SurfaceModel surfaceModel;
	private ActivitySurfaceBinding binding;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = DataBindingUtil.setContentView(this, R.layout.activity_surface);
		binding.setPresenter(new Presenter());
		surfaceModel = SurfaceModel.getInstance();
		binding.setItem(surfaceModel);
		if (savedInstanceState != null) {
			surfaceModel.setPlay(savedInstanceState.getBoolean("isPlay", false));
			surfaceModel.setAecTurnOn(savedInstanceState.getBoolean("turnOn", false));
		}
		PermissionUtil.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("turnOn", surfaceModel.isAecTurnOn());
		outState.putBoolean("isPlay", surfaceModel.isPlay());
		super.onSaveInstanceState(outState);
	}

	public class Presenter {
		public void toggleAECTurnOn(View view) {
			SurfaceModel.getInstance().setAecTurnOn(!SurfaceModel.getInstance().isAecTurnOn());
		}

		public void toggleANSTurnOn(View view) {
			SurfaceModel.getInstance().setAnsTurnOn(!SurfaceModel.getInstance().isAnsTurnOn());
		}

		public void togglePlay(View view) {
			surfaceModel.setPlay(!surfaceModel.isPlay());
			if (!surfaceModel.isPlay()) {
				try {
					audioACEApi.stopPlay();
				} catch (Exception e) {
					return;
				}
				try {
					audioThread.interrupt();
				} catch (Exception e) {
					return;
				}
				return;
			}
			final String wavFilePathMic = "sdcard/Music/mic.wav";
			final String wavFilePathSpk = "sdcard/Music/spk.wav";
			audioThread = new Thread(
					new Runnable() {
						public void run() {
							// Start spatial audio playback of SOUND_FILE at the model postion. The returned
							//soundId handle is stored and allows for repositioning the sound object whenever
							// the cube position changes.
							audioACEApi = new AudioProcessApi();
							audioACEApi.init();
							try {
								audioACEApi.LoadWavFile(wavFilePathMic, 0);
								audioACEApi.LoadWavFile(wavFilePathSpk, 1);
								audioACEApi.soundPlay();
							} catch (Exception e) {
								Log.w("", e.toString());
							}
						}
					});
			audioThread.start();
		}

		public void saveFile(View view) {
			final String wavFilePathMic = "sdcard/Music/mic.wav";
			final String wavFilePathSpk = "sdcard/Music/spk.wav";
			audioThread = new Thread(
					new Runnable() {
						public void run() {
							audioACEApi = new AudioProcessApi();
							audioACEApi.init();
							try {
								audioACEApi.LoadWavFile(wavFilePathMic, 0);
								audioACEApi.LoadWavFile(wavFilePathSpk, 1);
								audioACEApi.saveFile(audioACEApi.getSounddata0(), audioACEApi.getSounddata1());
							} catch (Exception e) {
								Log.w("", e.toString());
							}
						}
					});
			audioThread.start();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (audioACEApi != null)
			audioACEApi.stopPlay();
		if (audioThread != null)
			audioThread.interrupt();
		finish();
	}
}