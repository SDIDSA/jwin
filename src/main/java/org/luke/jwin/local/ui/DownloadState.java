package org.luke.jwin.local.ui;

public enum DownloadState {
	IDLE("download_starting"), RUNNING("download_running"), PAUSED("download_paused"),
	CANCELED("download_canceled"),
	EXTRACTING("download_extraction"), DONE("download_installed"), FAILED("download_failed");

	private final String text;

	DownloadState(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
}
