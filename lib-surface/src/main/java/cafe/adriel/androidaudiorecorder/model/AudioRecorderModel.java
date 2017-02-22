package cafe.adriel.androidaudiorecorder.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.Color;

import cafe.adriel.androidaudiorecorder.BR;
import cafe.adriel.androidaudiorecorder.R;

public class AudioRecorderModel extends BaseObservable {
	private String filePath;
	private AudioSource source = AudioSource.MIC;
	private AudioChannel channel = AudioChannel.STEREO;
	private AudioSampleRate sampleRate = AudioSampleRate.HZ_32000;
	private int color;
	private boolean autoStart;
	private boolean keepDisplayOn;
	//
	private String time = "00:00:00";
	private int textColor = Color.WHITE;
	private boolean status = false;
	private String statusText = "Playing";
	private boolean restart = false;
	private boolean recording = false;
	private int icon = R.drawable.aar_ic_rec;

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
	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
		notifyPropertyChanged(BR.icon);
	}

	@Bindable
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	@Bindable
	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
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
			setStatusText("Recording");
			setIcon(R.drawable.aar_ic_pause);
		} else {
			setStatusText("Finish");
			setIcon(R.drawable.aar_ic_restart);
		}
		notifyPropertyChanged(BR.recording);
	}
}
