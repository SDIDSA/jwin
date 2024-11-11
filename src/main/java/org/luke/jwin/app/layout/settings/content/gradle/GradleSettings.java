package org.luke.jwin.app.layout.settings.content.gradle;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import org.luke.gui.controls.input.combo.ComboItem;
import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.window.Window;
import org.luke.jwin.app.layout.settings.abs.GradleVersionItem;
import org.luke.jwin.app.layout.settings.abs.LocalManagerSettings;
import org.luke.jwin.app.layout.settings.abs.Settings;
import org.luke.jwin.local.LocalStore;
import org.luke.jwin.local.managers.GradleManager;
import org.luke.jwin.local.managers.LocalInstall;
import org.luke.jwin.local.ui.DownloadJob;

import javafx.scene.Node;

public class GradleSettings extends LocalManagerSettings {

	public GradleSettings(Settings settings) {
		super(settings, "Gradle");
	}

	@Override
	public Comparator<String> comparator() {
		return GradleManager.COMPARATOR;
	}

	@Override
	public List<DownloadJob> downloadJobs() {
		return GradleManager.downloadJobs();
	}

	@Override
	public Node managedUi(Window win, String version, Runnable refresh) {
		return GradleManager.managedUi(win, version, refresh);
	}

	@Override
	public Node localUi(Window win, LocalInstall version, Runnable refresh) {
		return GradleManager.localUi(win, version, refresh);
	}

	@Override
	public List<File> managedInstalls() {
		return GradleManager.managedInstalls();
	}

	@Override
	public List<LocalInstall> localInstalls() {
		return GradleManager.localInstalls();
	}

	@Override
	public List<String> installableVersions() {
		return GradleManager.installableVersions();
	}

	@Override
	public DownloadJob install(Window win, String version) {
		return GradleManager.install(win, version);
	}

	@Override
	public void setDefaultVersion(String version) {
		LocalStore.setDefaultGradle(version);
	}

	@Override
	public String getDefaultVersion() {
		return LocalStore.getDefaultGradle();
	}

	@Override
	public boolean isValid(String version) {
		return GradleManager.isValid(version);
	}

	@Override
	public void addInst(File dir) {
		GradleManager.addLocal(dir.getAbsolutePath());
	}

	@Override
	public void clearDefault() {
		LocalStore.setDefaultGradle(null);
		defCombo.setValue("");
	}

	@Override
	public ComboItem createComboItem(ContextMenu men, String key) {
		return new GradleVersionItem(men, key);
	}
}
