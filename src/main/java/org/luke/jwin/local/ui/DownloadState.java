package org.luke.jwin.local.ui;

public enum DownloadState {
	IDLE("Starting download"), RUNNING("Downloading"), PAUSED("Download Paused"), CANCELED("Canceled"), 
	EXTRACTING("Processing files"), DONE("Installed"), FAILED("Failed");

	private String text;

	private DownloadState(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
}
