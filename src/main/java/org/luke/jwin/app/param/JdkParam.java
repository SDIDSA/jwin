package org.luke.jwin.app.param;

import java.io.File;

import org.luke.gui.window.Window;
import org.luke.jwin.app.Command;

import javafx.application.Platform;
import javafx.stage.DirectoryChooser;

public class JdkParam extends JavaParam {
	private DirectoryChooser dc;
	public JdkParam(Window ps) {
		super(ps, "Jdk (will be used to compile your app)");

		dc = new DirectoryChooser();
		addButton(ps, "detect", () -> detect());
		addButton(ps, "select", () -> browse());
	}
	
	public File browse() {
		File dir = dc.showDialog(getWindow());
		if (dir != null) {
			set(dir);
			return dir;
		}
		
		return null;
	}

	public void detect() {
		startLoading();
		new Thread(() -> {
			File jdk = detectJdk();

			Platform.runLater(() -> {
				if (jdk != null && jdk.exists()) {
					set(jdk, " (found in your system)");
				}
				stopLoading();
			});
		}).start();
	}
	
	public File detectJdk() {
		String[] source = new String[1];
		Command find = new Command(line -> source[0] = line, "cmd.exe", "/C", "where javac");

		try {
			find.execute(new File("/")).waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
		
		if (source[0] != null) {
			File javac = new File(source[0]);
			if (javac.exists()) {
				return javac.getParentFile().getParentFile();
			}
		}
		
		return null;
	}

}
