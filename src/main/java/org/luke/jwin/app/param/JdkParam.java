package org.luke.jwin.app.param;

import java.io.File;
import org.luke.jwin.app.Command;

import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class JdkParam extends JavaParam {
	public JdkParam(Stage ps) {
		super("Jdk (will be use to compile your app)");

		DirectoryChooser dc = new DirectoryChooser();
		addButton("detect", e -> detect());
		addButton("select", e -> {
			File dir = dc.showDialog(ps);
			if (dir != null) {
				set(dir);
			}
		});

	}

	private void detect() {
		startLoading();
		new Thread(() -> {
			String[] source = new String[1];
			Command find = new Command(line -> source[0] = line, "cmd.exe", "/C", "where javac");

			try {
				find.execute(new File("/")).waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}

			Platform.runLater(() -> {
				if (source[0] != null) {
					File javac = new File(source[0]);
					if (javac.exists()) {
						set(javac.getParentFile().getParentFile());
					}
				}

				stopLoading();
			});
		}).start();
	}

}
