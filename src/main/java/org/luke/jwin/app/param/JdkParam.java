package org.luke.jwin.app.param;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.luke.gui.exception.ErrorHandler;
import org.luke.gui.locale.Locale;
import org.luke.gui.window.Window;
import org.luke.jwin.app.Command;

import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import org.luke.jwin.app.Jwin;

public class JdkParam extends JavaParam {
	private final DirectoryChooser dc;

	public JdkParam(Window ps) {
		super(ps, "jdk_compile");

		dc = new DirectoryChooser();
		addButton(ps, "detect", this::detect);
		addButton(ps, "select", this::browse);
	}

	public void browse() {
		File dir = dc.showDialog(getWindow());
		if (dir != null) {
			if (isJdk(dir))
				setFile(dir);
		}
	}

	public void setFile(File file) {
		super.set(file, log());
	}

	public void setFile(File file, Runnable onFinish) {
		super.set(file, log(onFinish));
	}

	public void setFile(File file, String additional) {
		super.set(file, additional, log());
	}

	public void setFile(File file, String additional, Runnable onFinish) {
		super.set(file, additional, log(onFinish));
	}

	@Override
	public void set(File file, String additional, Runnable onFinish) {
		super.set(file, additional, log(onFinish));
	}

	private Runnable log;
	private synchronized Runnable log() {
		if(log == null) {
			log = () -> Jwin.instance.getConfig()
					.logStd(Locale.key("jdk_set", "version", version));
		}
		return log;
	}

	private Runnable log(Runnable or) {
		return () -> {
			if(or != null) or.run();
			if(or != log) log.run();
		};
	}

	public void detect() {
		startLoading();
		new Thread(() -> {
			List<File> detected = detectJdk();
			if (detected.isEmpty()) {
				return;
			}
			File jdk = detected.getFirst();

			Platform.runLater(() -> {
				if (jdk != null && jdk.exists()) {
					set(jdk, " (found in your system)");
				}
				stopLoading();
			});
		}, "jdk detector").start();
	}

	private static List<File> detected;

	public synchronized static List<File> detectJdkCache() {
		if (detected == null) {
			detected = detectJdk();
		}

		return detected;
	}

	private static List<File> detectJdk() {
		ArrayList<File> res = new ArrayList<>();

		ArrayList<String> sources = new ArrayList<>();
		Command find = new Command(sources::add, "cmd.exe", "/C", "dir /b /s javac.exe");

		try {
			find.execute(new File("C:\\Program Files")).waitFor();
		} catch (InterruptedException e) {
			ErrorHandler.handle(e, "detect jdk installations");
			Thread.currentThread().interrupt();
		}

		for (String source : sources) {
			File javac = new File(source);
			if (javac.exists() && getVersionFromDir(javac.getParentFile().getParentFile()) != null) {
				res.add(javac.getParentFile().getParentFile());
			}
		}

		return res;
	}

}
