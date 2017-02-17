package cafe.adriel.androidaudiorecorder.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.Color;

import cafe.adriel.androidaudiorecorder.R;

public class AudioRecorderModel extends BaseObservable {
	private String filePath;
	private AudioSource source;
	private AudioChannel channel;
	private AudioSampleRate sampleRate;
	private int color;
	private boolean autoStart;
	private boolean keepDisplayOn;
	//
	private String time = "00:00:00";
	private int textColor = Color.WHITE;
	private boolean status = false;
	private String statusText = "Pause";
	private boolean restart = false;
	private boolean recording = false;
	private int recordIcon = R.drawable.aar_ic_rec;
	private int stopIcon = R.drawable.aar_ic_stop;

	@Bindable
	public String getStatusText() {
		return statusText;
	}

	public void setStatusText(String statusText) {
		this.statusText = statusText;
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
	}

	@Bindable
	public boolean isRecording() {
		return recording;
	}

	public void setRecording(boolean recording) {
		this.recording = recording;
	}

	@Bindable
	public int getRecordIcon() {
		return recordIcon;
	}

	public void setRecordIcon(int recordIcon) {
		this.recordIcon = recordIcon;
	}

	@Bindable
	public int getStopIcon() {
		return stopIcon;
	}

	public void setStopIcon(int stopIcon) {
		this.stopIcon = stopIcon;
	}
}
