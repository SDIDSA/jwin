package org.luke.jwin.app.layout.settings.content.jdk;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import org.luke.gui.controls.input.combo.ComboItem;
import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.window.Window;
import org.luke.jwin.app.layout.settings.abs.JdkVersionItem;
import org.luke.jwin.app.layout.settings.abs.LocalManagerSettings;
import org.luke.jwin.app.layout.settings.abs.Settings;
import org.luke.jwin.local.LocalStore;
import org.luke.jwin.local.managers.JdkManager;
import org.luke.jwin.local.managers.LocalInstall;
import org.luke.jwin.local.ui.DownloadJob;

import javafx.scene.Node;

public class JdkSettings extends LocalManagerSettings {

	public JdkSettings(Settings settings) {
		super(settings, "JDK");
	}

	@Override
	public Comparator<String> comparator() {
		return JdkManager.COMPARATOR;
	}

	@Override
	public List<DownloadJob> downloadJobs() {
		return JdkManager.downloadJobs();
	}

	@Override
	public Node managedUi(Window win, String version, Runnable refresh) {
		return JdkManager.managedUi(win, version, refresh);
	}

	@Override
	public Node localUi(Window win, LocalInstall version, Runnable refresh) {
		return JdkManager.localUi(win, version, refresh);
	}

	@Override
	public List<File> managedInstalls() {
		return JdkManager.managedInstalls();
	}

	@Override
	public List<LocalInstall> localInstalls() {
		return JdkManager.localInstalls();
	}

	@Override
	public List<String> installableVersions() {
		return JdkManager.installableVersions();
	}

	@Override
	public DownloadJob install(Window win, String version) {
		return JdkManager.install(win, version);
	}

	@Override
	public void setDefaultVersion(String version) {
		LocalStore.setDefaultJdk(version);
	}

	@Override
	public String getDefaultVersion() {
		return LocalStore.getDefaultJdk();
	}

	@Override
	public boolean isValid(String version) {
		return JdkManager.isValid(version);
	}

	@Override
	public void addInst(File dir) {
		JdkManager.addLocal(dir.getAbsolutePath());
	}

	@Override
	public void clearDefault() {
		LocalStore.setDefaultJdk(null);
		defCombo.setValue("");
	}

	@Override
	public ComboItem createComboItem(ContextMenu men, String key) {
		return new JdkVersionItem(men, key);
	}
}
