package cafe.adriel.androidaudiorecorder.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.Color;

import cafe.adriel.androidaudiorecorder.BR;
import cafe.adriel.androidaudiorecorder.R;

public class AudioRecorderModel extends BaseObservable {
	private AudioSource source = AudioSource.MIC;
	private AudioChannel channel = AudioChannel.STEREO;
	private AudioSampleRate sampleRate = AudioSampleRate.HZ_32000;
	private int color;
	private boolean autoStart;
	private boolean keepDisplayOn;
	private String filePath;
	//
	private String time = "00:00:00";
	private String nls = "";
	private int textColor = Color.WHITE;
	// false stop true playing
	// 0 wait 1 recording 2 pause 3 playing 4 finish
	private int status = 0;
	private boolean recording = false;
	private boolean playing = false;
	private boolean restart = false;
	private final static String[] statusArray = {
			"Waiting",
			"Recording",
			"Playing",
			"Paused",
			"Finish"
	};
	private final static int[] iconArray = {
			R.drawable.aar_ic_rec,
			R.drawable.aar_ic_pause,
			R.drawable.aar_ic_play,
			R.drawable.aar_ic_pause,
			R.drawable.aar_ic_restart,
	};
	private String statusText = statusArray[0];
	private int iconRecord = iconArray[0];

	private int iconPlay = iconArray[2];

	@Bindable
	public String getStatusText() {
		return statusText;
	}

	public void setStatusText(String statusText) {
		this.statusText = statusText;
		notifyPropertyChanged(BR.statusText);
	}

	@Bindable
	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	@Bindable
	public String getFilePath() {
		return filePath;
	}

	@Bindable
	public AudioSource getSource() {
		return source;
	}

	@Bindable
	public AudioChannel getChannel() {
		return channel;
	}

	@Bindable
	public AudioSampleRate getSampleRate() {
		return sampleRate;
	}

	@Bindable
	public int getColor() {
		return color;
	}

	@Bindable
	public boolean isAutoStart() {
		return autoStart;
	}

	@Bindable
	public boolean isKeepDisplayOn() {
		return keepDisplayOn;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public void setSource(AudioSource source) {
		this.source = source;
	}

	public void setChannel(AudioChannel channel) {
		this.channel = channel;
	}

	public void setSampleRate(AudioSampleRate sampleRate) {
		this.sampleRate = sampleRate;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public void setAutoStart(boolean autoStart) {
		this.autoStart = autoStart;
	}

	public void setKeepDisplayOn(boolean keepDisplayOn) {
		this.keepDisplayOn = keepDisplayOn;
	}

	@Bindable
	public int getIconRecord() {
		return iconRecord;
	}

	public void setIconRecord(int iconRecord) {
		this.iconRecord = iconRecord;
		notifyPropertyChanged(BR.iconRecord);
	}

	@Bindable
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
		notifyPropertyChanged(BR.time);
	}

	@Bindable
	public String getNls() {
		return nls;
	}

	public void setNls(String nls) {
		this.nls = nls;
		notifyPropertyChanged(BR.nls);
	}

	@Bindable
	public boolean isRestart() {
		return restart;
	}

	public void setRestart(boolean restart) {
		this.restart = restart;
		notifyPropertyChanged(BR.restart);
	}

	@Bindable
	public boolean isRecording() {
		return recording;
	}

	public void setRecording(boolean recording) {
		this.recording = recording;
		if (recording) {
			setStatus(1);
			setIconRecord(iconArray[status]);
		} else {
			setStatus(4);
			setIconRecord(iconArray[status]);
		}
		notifyPropertyChanged(BR.recording);
	}

	@Bindable
	public boolean isPlaying() {
		return playing;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
		if (playing) {
			setStatus(2);
			setIconPlay(iconArray[status]);
		} else {
			setStatus(4);
			setIconPlay(iconArray[status]);
		}
		notifyPropertyChanged(BR.playing);
	}

	public void setStatus(int status) {
		this.status = status;
		setStatusText(statusArray[status]);
	}

	@Bindable
	public int getIconPlay() {
		return iconPlay;
	}

	public void setIconPlay(int iconPlay) {
		this.iconPlay = iconPlay;
		notifyPropertyChanged(BR.iconPlay);
	}
}
