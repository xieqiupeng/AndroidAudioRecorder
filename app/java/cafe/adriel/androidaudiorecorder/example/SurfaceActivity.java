package cafe.adriel.androidaudiorecorder.example;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.tencent.bugly.crashreport.CrashReport;
import com.twirling.audio.api.AudioProcessApi;
import com.twirling.audio.api.Constants;
import com.twirling.audio.utils.FileUtil;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;

public class SurfaceActivity extends AppCompatActivity {
	private static final int REQUEST_RECORD_AUDIO = 0;
	private static final String AUDIO_FILE_PATH =
			Environment.getExternalStorageDirectory().getPath()
					+ "/"
					+ Environment.DIRECTORY_MUSIC + "/audio_processed.wav";
	private AudioProcessApi audioProcessApi;
	private Thread audioThread;
	private String wavFilePath;

	private ExitAppReceiver exitReceiver = new ExitAppReceiver();
	private static final String EXIT_APP_ACTION = "com.exit";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_surface);
		//
		CrashReport.initCrashReport(getApplicationContext(), "8a09d6d42a", false);
		//
		if (getSupportActionBar() != null) {
			getSupportActionBar().setBackgroundDrawable(
					new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryDark)));
		}
		registerExitReceiver();
		//
		Constants.SAMPLE_INDEX = 2;
		wavFilePath = FileUtil.copyAssetFileToFiles(SurfaceActivity.this, Constants.FILE_NAME[Constants.SAMPLE_INDEX]);
		//
		recordAudio(null);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_RECORD_AUDIO) {
			if (resultCode == RESULT_OK) {
//				Toast.makeText(this, "Audio recorded successfully!", Toast.LENGTH_SHORT).show();
				finish();
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Audio was not recorded", Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void recordAudio(View v) {
		AndroidAudioRecorder.with(this)
				// Required
				.setFilePath(AUDIO_FILE_PATH)
				.setColor(ContextCompat.getColor(this, R.color.recorder_bg))
				.setRequestCode(REQUEST_RECORD_AUDIO)
				// Optional
				.setSource(AudioSource.MIC)
				.setChannel(AudioChannel.MONO)
				.setSampleRate(AudioSampleRate.HZ_32000)
				.setAutoStart(false)
				.setKeepDisplayOn(true)
				// Start recording
				.record();
	}

	private void registerExitReceiver() {
		IntentFilter exitFilter = new IntentFilter();
		exitFilter.addAction(EXIT_APP_ACTION);
		registerReceiver(exitReceiver, exitFilter);
	}

	private void unRegisterExitReceiver() {
		unregisterReceiver(exitReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unRegisterExitReceiver();
	}

	public class ExitAppReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			SurfaceActivity.this.finish();
		}
	}
}