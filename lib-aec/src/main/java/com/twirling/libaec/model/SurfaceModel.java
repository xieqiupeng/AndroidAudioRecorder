package com.twirling.libaec.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

public class SurfaceModel extends BaseObservable {
	boolean play = false;
	boolean aecTurnOn = false;
	boolean ansTurnOn = false;
	private static SurfaceModel instance = null;

	private SurfaceModel() {
	}

	public static SurfaceModel getInstance() {
		if (instance == null) {
			instance = new SurfaceModel();
		}
		return instance;
	}

	@Bindable
	public boolean isPlay() {
		return play;
	}

	public void setPlay(boolean play) {
		this.play = play;
		notifyPropertyChanged(com.twirling.libaec.BR.play);
	}

	@Bindable
	public boolean isAecTurnOn() {
		return aecTurnOn;
	}

	public void setAecTurnOn(boolean aecTurnOn) {
		this.aecTurnOn = aecTurnOn;
		if (!aecTurnOn) {
			setAnsTurnOn(false);
		}
		notifyPropertyChanged(com.twirling.libaec.BR.aecTurnOn);
	}

	@Bindable
	public boolean isAnsTurnOn() {
		return ansTurnOn;
	}

	public void setAnsTurnOn(boolean ansTurnOn) {
		this.ansTurnOn = ansTurnOn;
		notifyPropertyChanged(com.twirling.libaec.BR.ansTurnOn);
	}
}
