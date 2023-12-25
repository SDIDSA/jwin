package org.luke.jwin.local.managers;

import java.io.File;

public class LocalInstall {
	private File root;
	private String version;
	public LocalInstall(File root, String version) {
		this.root = root;
		this.version = version;
	}
	public File getRoot() {
		return root;
	}
	public void setRoot(File root) {
		this.root = root;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

}
