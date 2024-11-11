package org.luke.jwin.local.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

import org.luke.gui.controls.space.ExpandingHSpace;
import org.luke.gui.exception.ErrorHandler;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.style.Style;
import org.luke.gui.window.Window;
import org.luke.jwin.app.Command;
import org.luke.jwin.app.Jwin;
import org.luke.jwin.app.JwinActions;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class DownloadJob extends LocalInstallUi {
	private final String urlString;

	private DownloadState state;

	private final ArrayList<Consumer<Double>> onProgress;
	private final ArrayList<Consumer<DownloadState>> onStateChanged;

	private final StackPane pTrack;
	private final Rectangle pThumb;
	
	private final Function<File, File> rootSupplier;

	public DownloadJob(Window win, String version, String urlString, File targetDir, Function<File, File> rootSupplier) {
		super(win, version, targetDir);
		
		this.rootSupplier = rootSupplier;

		this.urlString = urlString;
		state = DownloadState.IDLE;

		onProgress = new ArrayList<>();
		onStateChanged = new ArrayList<>();

		stateLabl.setKey("...");

		pThumb = new Rectangle();
		pTrack = new StackPane(pThumb);
		pTrack.setAlignment(Pos.CENTER_LEFT);

		pTrack.setMinHeight(USE_PREF_SIZE);
		pTrack.setMaxHeight(USE_PREF_SIZE);

		pTrack.setPrefHeight(8);
		pThumb.setHeight(8);

		ManagerButton pause = new ManagerButton(win, "wait", "download_starting");
		ManagerButton cancel = new ManagerButton(win, "close", "cancel_download");

		cancel.setAction(() -> setState(DownloadState.CANCELED));

		addOnProgress(p -> Platform.runLater(() -> pThumb.setWidth(p * pTrack.getWidth())));

		root.getChildren().addAll(new ExpandingHSpace(), new HBox(0, pause, cancel));
		getChildren().add(pTrack);

		addOnStateChanged(s -> {
			Platform.runLater(() -> stateLabl.setKey(s.getText()));

			if (s == DownloadState.PAUSED) {
				pause.setIcon("play");
				pause.setAction(() -> setState(DownloadState.RUNNING));
				pause.setTooltip("resume_download");
			}

			if (s == DownloadState.RUNNING) {
				pause.setIcon("pause");
				pause.setAction(() -> setState(DownloadState.PAUSED));
				pause.setTooltip("pause_download");
			}
		});

		applyStyle(win.getStyl().get());
	}

	@Override
	public void applyStyle(Style style) {
		if (pTrack == null)
			return;

		pTrack.setBackground(Backgrounds.make(style.getBackgroundModifierSelected()));
		pThumb.setFill(style.getAccent());

		super.applyStyle(style);
	}

    public DownloadState getState() {
		return state;
	}

	public void addOnProgress(Consumer<Double> onProgress) {
		this.onProgress.add(onProgress);
	}

	public void addOnStateChanged(Consumer<DownloadState> onStateChanged) {
		this.onStateChanged.add(onStateChanged);
	}

	public void setState(DownloadState state) {
		this.state = state;
		onStateChanged.forEach(e -> e.accept(state));
	}

	public void setProgress(double progress) {
		onProgress.forEach(e -> e.accept(progress));
	}

	public void cancel() {
		setState(DownloadState.CANCELED);
	}

	public void start() {
		new Thread(() -> {

			try {
				URL url = URI.create(urlString).toURL();
				URLConnection con = url.openConnection();
				InputStream is = con.getInputStream();

				int ind = urlString.indexOf("?");
				String name = urlString.substring(urlString.lastIndexOf("/") + 1, ind == -1 ? urlString.length() : ind);
				name = name.substring(0, name.lastIndexOf("."));

				File output = File.createTempFile(name + "_", ".zip");
				OutputStream os = new FileOutputStream(output);

				int fileLength = con.getContentLength();
				int count;
				byte[] buffer = new byte[2048];
				int totalRead = 0;
				if (state == DownloadState.CANCELED) {
					os.close();
					is.close();
					return;
				}
				setState(DownloadState.RUNNING);
				while ((count = is.read(buffer)) != -1 && state != DownloadState.CANCELED) {
					while (state == DownloadState.PAUSED) {
						Thread.sleep(100);
					}

					os.write(buffer, 0, count);
					totalRead += count;

					setProgress((double) totalRead / fileLength);
				}
				if (state == DownloadState.CANCELED) {
					os.close();
					is.close();
					return;
				}
				os.flush();
				os.close();

				// extract
				setState(DownloadState.EXTRACTING);

				File temp = File.createTempFile(name, "");
				temp.delete();
				new Command("cmd", "/c",
						"7z x \"" + output.getAbsolutePath() + "\" -aou -o\"" + temp.getAbsolutePath() + "\"")
						.execute(Jwin.get7z()).waitFor();

				if (!targetDir.exists() || !targetDir.isDirectory()) {
					targetDir.mkdir();
				}

				int fileCount = JwinActions.countDir(temp);
				int[] copyCount = new int[] { 0 };
				JwinActions.copyDirCont(rootSupplier.apply(temp), targetDir, () -> {
					copyCount[0]++;
					setProgress((copyCount[0] / (double) fileCount));
				});

				setState(DownloadState.DONE);
			} catch (IOException | InterruptedException e) {
				ErrorHandler.handle(e, "download file " + urlString);
			}
		}).start();
	}

}
