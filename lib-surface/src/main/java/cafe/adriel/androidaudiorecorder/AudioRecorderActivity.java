package cafe.adriel.androidaudiorecorder;

import android.Manifest;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.twirling.libaec.model.SurfaceModel;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import cafe.adriel.androidaudiorecorder.databinding.AarActivityAudioRecorderBinding;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioRecorderModel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import omrecorder.AudioChunk;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.Recorder;

public class AudioRecorderActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {
	private AudioRecorderModel arModel = null;

	private MediaPlayer player;
	private Recorder recorder;
	private VisualizerHandler visualizerHandler;

	private Timer timer;
	private MenuItem saveMenuItem;
	private int recorderSecondsElapsed;
	private int playerSecondsElapsed;
//	private boolean isRecording;

	private GLAudioVisualizationView visualizerView;
	private TextView statusView;
	private TextView timerView;
	private ImageButton restartView;
	private ImageButton recordView;
	private ImageButton playView;
	private PullTransport.Default pullTransport;
	private Presenter presenter;
	private AarActivityAudioRecorderBinding binding;
	private boolean backpress = false;
	//
	String fileName = Environment.getExternalStorageDirectory().getPath()
			+ "/"
			+ Environment.DIRECTORY_DOWNLOADS + "/mono.wav";

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		binding = DataBindingUtil.setContentView(this, R.layout.aar_activity_audio_recorder);
		presenter = new Presenter();
		arModel = new AudioRecorderModel();
		binding.setPresenter(presenter);
		binding.setItem(arModel);
		binding.setItem2(SurfaceModel.getInstance());
		//
		initdata(savedInstanceState);
		//
		initView();
	}

	private void initView() {
		if (arModel.isKeepDisplayOn()) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		//
		if (getSupportActionBar() != null) {
			getSupportActionBar().hide();
		}
//		if (getSupportActionBar() != null) {
//			getSupportActionBar().setHomeButtonEnabled(true);
//			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//			getSupportActionBar().setDisplayShowTitleEnabled(false);
//			getSupportActionBar().setElevation(0);
//			getSupportActionBar().setBackgroundDrawable(
//					new ColorDrawable(PermissionUtil.getDarkerColor(arModel.getColor())));
//			getSupportActionBar().setHomeAsUpIndicator(
//					ContextCompat.getDrawable(this, R.drawable.aar_ic_clear));
//		}
		//
		visualizerView = new GLAudioVisualizationView.Builder(this)
				.setLayersCount(1)
				.setWavesCount(6)
				.setWavesHeight(R.dimen.aar_wave_height)
				.setWavesFooterHeight(R.dimen.aar_footer_height)
				.setBubblesPerLayer(20)
				.setBubblesSize(R.dimen.aar_bubble_size)
				.setBubblesRandomizeSize(true)
				.setBackgroundColor(Util.getDarkerColor(arModel.getColor()))
				.setLayerColors(new int[]{arModel.getColor()})
				.build();
		//
		FrameLayout contentLayout = (FrameLayout) findViewById(R.id.content);
		contentLayout.setBackgroundColor(Util.getDarkerColor(arModel.getColor()));

		statusView = (TextView) findViewById(R.id.status);
		timerView = (TextView) findViewById(R.id.timer);
		restartView = (ImageButton) findViewById(R.id.restart);
		recordView = (ImageButton) findViewById(R.id.record);
		playView = (ImageButton) findViewById(R.id.play);

		contentLayout.addView(visualizerView, 0);
		restartView.setVisibility(View.INVISIBLE);
		playView.setVisibility(View.INVISIBLE);

		if (Util.isBrightColor(arModel.getColor())) {
			ContextCompat.getDrawable(this, R.drawable.aar_ic_clear)
					.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
			ContextCompat.getDrawable(this, R.drawable.aar_ic_check)
					.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
			statusView.setTextColor(Color.BLACK);
			timerView.setTextColor(Color.BLACK);
			restartView.setColorFilter(Color.BLACK);
			recordView.setColorFilter(Color.BLACK);
			playView.setColorFilter(Color.BLACK);
		}
	}

	private void initdata(Bundle savedInstanceState) {
		String filePath;
		AudioSource source;
		AudioChannel channel;
		AudioSampleRate sampleRate;
		int color;
		boolean autoStart;
		boolean keepDisplayOn;
		if (savedInstanceState != null) {
			filePath = savedInstanceState.getString(AndroidAudioRecorder.EXTRA_FILE_PATH);
			source = (AudioSource) savedInstanceState.getSerializable(AndroidAudioRecorder.EXTRA_SOURCE);
			channel = (AudioChannel) savedInstanceState.getSerializable(AndroidAudioRecorder.EXTRA_CHANNEL);
			sampleRate = (AudioSampleRate) savedInstanceState.getSerializable(AndroidAudioRecorder.EXTRA_SAMPLE_RATE);
			color = savedInstanceState.getInt(AndroidAudioRecorder.EXTRA_COLOR);
			autoStart = savedInstanceState.getBoolean(AndroidAudioRecorder.EXTRA_AUTO_START);
			keepDisplayOn = savedInstanceState.getBoolean(AndroidAudioRecorder.EXTRA_KEEP_DISPLAY_ON);
		} else {
			filePath = getIntent().getStringExtra(AndroidAudioRecorder.EXTRA_FILE_PATH);
			source = (AudioSource) getIntent().getSerializableExtra(AndroidAudioRecorder.EXTRA_SOURCE);
			channel = (AudioChannel) getIntent().getSerializableExtra(AndroidAudioRecorder.EXTRA_CHANNEL);
			sampleRate = (AudioSampleRate) getIntent().getSerializableExtra(AndroidAudioRecorder.EXTRA_SAMPLE_RATE);
			color = getIntent().getIntExtra(AndroidAudioRecorder.EXTRA_COLOR, Color.BLACK);
			autoStart = getIntent().getBooleanExtra(AndroidAudioRecorder.EXTRA_AUTO_START, false);
			keepDisplayOn = getIntent().getBooleanExtra(AndroidAudioRecorder.EXTRA_KEEP_DISPLAY_ON, false);
		}
		arModel.setFilePath(filePath);
		arModel.setSource(source);
		arModel.setChannel(channel);
		arModel.setSampleRate(sampleRate);
		arModel.setColor(color);
		arModel.setAutoStart(autoStart);
		arModel.setKeepDisplayOn(keepDisplayOn);
	}


	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (arModel.isAutoStart() && !arModel.isRecording()) {
//			presenter.toggleRecording(null);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			visualizerView.onResume();
		} catch (Exception e) {
		}
	}

	@Override
	protected void onPause() {
//		presenter.restartRecording(null);
		try {
			visualizerView.onPause();
		} catch (Exception e) {
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		presenter.restartRecording(null);
		setResult(RESULT_CANCELED);
		try {
			visualizerView.release();
		} catch (Exception e) {
		}
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(AndroidAudioRecorder.EXTRA_FILE_PATH, arModel.getFilePath());
		outState.putInt(AndroidAudioRecorder.EXTRA_COLOR, arModel.getColor());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {
		presenter.stopPlaying();
	}

	public class Presenter {

		public void toggleAECTurnOn(View view) {
			SurfaceModel.getInstance().setAecTurnOn(!SurfaceModel.getInstance().isAecTurnOn());
		}

		public void toggleANSTurnOn(View view) {
			SurfaceModel.getInstance().setAnsTurnOn(!SurfaceModel.getInstance().isAnsTurnOn());
		}

//		private void selectAudio() {
//			stopRecording();
//			setResult(RESULT_OK);
//			finish();
//		}

		public void toggleRecording(View v) {
			if (arModel.isRecording()) {
				stopRecording();
			} else {
				new RxPermissions(AudioRecorderActivity.this)
						.request(Manifest.permission.RECORD_AUDIO,
								Manifest.permission.WRITE_EXTERNAL_STORAGE)
						.observeOn(AndroidSchedulers.mainThread())
						.subscribeOn(AndroidSchedulers.mainThread())
						.subscribe(new Consumer<Boolean>() {
							@Override
							public void accept(Boolean grant) throws Exception {
								if (grant) {
									resumeRecording();
								} else {
									Toast.makeText(AudioRecorderActivity.this, "请打开权限", Toast.LENGTH_LONG).show();
								}
							}
						});
			}
			arModel.setRecording(!arModel.isRecording());
		}

		public void togglePlaying(View v) {
			pauseRecording();
			Util.wait(100, new Runnable() {
				@Override
				public void run() {
					if (isPlaying()) {
						stopPlaying();
					} else {
						startPlaying();
					}
				}
			});
		}

		public void restartRecording(View v) {
			if (isPlaying()) {
				stopPlaying();
			} else {
				visualizerHandler = new VisualizerHandler();
				visualizerView.linkTo(visualizerHandler);
				visualizerView.release();
				if (visualizerHandler != null) {
					visualizerHandler.stop();
				}
			}
			if (saveMenuItem != null)
				saveMenuItem.setVisible(false);
			arModel.setRestart(false);
			arModel.setTime("00:00:00");
			recorderSecondsElapsed = 0;
			playerSecondsElapsed = 0;
		}

		private void resumeRecording() {
			if (saveMenuItem != null)
				saveMenuItem.setVisible(false);
			statusView.setVisibility(View.VISIBLE);
			restartView.setVisibility(View.INVISIBLE);
			playView.setVisibility(View.INVISIBLE);
			playView.setImageResource(R.drawable.aar_ic_play);

			visualizerHandler = new VisualizerHandler();

			visualizerView.onResume();
			visualizerView.linkTo(visualizerHandler);
			if (recorder == null) {
				arModel.setTime("00:00:00");
				arModel.setSource(AudioSource.MIC);
				arModel.setChannel(AudioChannel.MONO);
				arModel.setSampleRate(AudioSampleRate.HZ_32000);
				//
				pullTransport = new PullTransport.Default(
						Util.getMic(arModel.getSource(), arModel.getChannel(), arModel.getSampleRate()),
						new PullTransport.OnAudioChunkPulledListener() {
							@Override
							public void onAudioChunkPulled(AudioChunk audioChunk) {
								float amplitude = arModel.isRecording() ? (float) audioChunk.maxAmplitude() : 0f;
								visualizerHandler.onDataReceived(amplitude);
							}
						});
				recorder = OmRecorder.wav(pullTransport, new File(arModel.getFilePath()));
			}
			//
			recorder.resumeRecording();
			startTimer();
		}

		private void pauseRecording() {
			arModel.setRecording(false);
			if (!isFinishing()) {
				if (saveMenuItem != null)
					saveMenuItem.setVisible(true);
			}
			statusView.setText(R.string.aar_paused);
			statusView.setVisibility(View.VISIBLE);
			restartView.setVisibility(View.VISIBLE);
			playView.setVisibility(View.VISIBLE);
			arModel.setRecording(false);
			playView.setImageResource(R.drawable.aar_ic_play);

			visualizerView.release();
			if (visualizerHandler != null) {
				visualizerHandler.stop();
			}

			if (recorder != null) {
				recorder.pauseRecording();
			}

			stopTimer();
		}

		private void stopRecording() {
			Log.i("stopRecording!", "stopRecording");
			arModel.setRestart(true);
			arModel.setRecording(true);
			visualizerView.release();
			if (visualizerHandler != null) {
				visualizerHandler.stop();
			}
			recorderSecondsElapsed = 0;
			if (recorder != null) {
				recorder.stopRecording();
				recorder = null;
				Toast.makeText(AudioRecorderActivity.this, "Audio recorded successfully!", Toast.LENGTH_SHORT).show();
			}
			stopTimer();
			if (pullTransport != null) {
				pullTransport.stopProcess();
				pullTransport = null;
			}
		}

		private void startPlaying() {
			try {
				player = new MediaPlayer();
				player.setDataSource(arModel.getFilePath());
				player.prepare();
				player.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				visualizerView.linkTo(DbmHandler.Factory.newVisualizerHandler(AudioRecorderActivity.this, player));
				visualizerView.post(new Runnable() {
					@Override
					public void run() {
						player.setOnCompletionListener(AudioRecorderActivity.this);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				timerView.setText("00:00:00");
				statusView.setText(R.string.aar_playing);
				statusView.setVisibility(View.VISIBLE);
				playView.setImageResource(R.drawable.aar_ic_stop);
				playerSecondsElapsed = 0;
				startTimer();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void stopPlaying() {
			statusView.setText("");
			statusView.setVisibility(View.INVISIBLE);
			playView.setImageResource(R.drawable.aar_ic_play);

			visualizerView.release();
			if (visualizerHandler != null) {
				visualizerHandler.stop();
			}

			if (player != null) {
				try {
					player.stop();
					player.reset();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			stopTimer();
		}

		private boolean isPlaying() {
			try {
				return player != null && player.isPlaying() && !arModel.isRecording();
			} catch (Exception e) {
				return false;
			}
		}

		private void startTimer() {
			stopTimer();
			timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					updateTimer();
				}
			}, 0, 1000);
		}

		private void stopTimer() {
			if (timer != null) {
				timer.cancel();
				timer.purge();
				timer = null;
			}
		}

		private void updateTimer() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (arModel.isRecording()) {
						recorderSecondsElapsed++;
						timerView.setText(Util.formatSeconds(recorderSecondsElapsed));
					} else if (isPlaying()) {
						playerSecondsElapsed++;
						timerView.setText(Util.formatSeconds(playerSecondsElapsed));
					}
				}
			});
		}
	}

	@Override
	public void onBackPressed() {
		if (!backpress) {
			presenter.stopRecording();
		}
		backpress = true;
		setResult(RESULT_OK);
		finish();
	}
}