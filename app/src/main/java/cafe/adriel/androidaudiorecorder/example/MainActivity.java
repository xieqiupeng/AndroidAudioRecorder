package cafe.adriel.androidaudiorecorder.example;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.twirling.audio.api.AudioProcessApi;
import com.twirling.audio.api.Constants;
import com.twirling.audio.utils.FileUtil;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;

public class MainActivity extends AppCompatActivity {
	private static final int REQUEST_RECORD_AUDIO = 0;
	private static final String AUDIO_FILE_PATH =
			Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_MUSIC + "/recorded_audio.wav";
	private AudioProcessApi audioProcessApi;
	private Thread audioThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (getSupportActionBar() != null) {
			getSupportActionBar().setBackgroundDrawable(
					new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryDark)));
		}

		Util.requestPermission(this, Manifest.permission.RECORD_AUDIO);
		Util.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_RECORD_AUDIO) {
			if (resultCode == RESULT_OK) {
				Toast.makeText(this, "Audio recorded successfully!", Toast.LENGTH_SHORT).show();
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Audio was not recorded", Toast.LENGTH_SHORT).show();
			}
		}
		if (audioProcessApi != null) {
			audioProcessApi.stopPlay();
			audioThread.interrupt();
		}
	}

	public void recordAudio(View v) {
		Constants.SAMPLE_INDEX = 2;
		final String wavFilePath = FileUtil.copyAssetFileToFiles(MainActivity.this, Constants.FILE_NAME[Constants.SAMPLE_INDEX]);
		audioThread = new Thread(
				new Runnable() {
					public void run() {
						// Start spatial audio playback of SOUND_FILE at the model postion. The returned
						//soundId handle is stored and allows for repositioning the sound object whenever
						// the cube position changes.
						Thread.currentThread().getName();
						audioProcessApi = new AudioProcessApi();
						audioProcessApi.init();
						try {
							audioProcessApi.LoadWavFile(wavFilePath);
							audioProcessApi.soundPlay();
						} catch (Exception e) {
							Log.w("", e.toString());
						}
					}
				});
		audioThread.start();
		//
		AndroidAudioRecorder.with(this)
				// Required
				.setFilePath(AUDIO_FILE_PATH)
				.setColor(ContextCompat.getColor(this, R.color.recorder_bg))
				.setRequestCode(REQUEST_RECORD_AUDIO)

				// Optional
				.setSource(AudioSource.MIC)
				.setChannel(AudioChannel.STEREO)
				.setSampleRate(AudioSampleRate.HZ_48000)
				.setAutoStart(true)
				.setKeepDisplayOn(true)

				// Start recording
				.record();
	}

}