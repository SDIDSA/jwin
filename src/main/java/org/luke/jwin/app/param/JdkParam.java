package org.luke.jwin.app.param;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
			List<File> detected = detectJdk();
			if(detected.isEmpty()) {
				return;
			}
			File jdk = detected.get(0);

			Platform.runLater(() -> {
				if (jdk != null && jdk.exists()) {
					set(jdk, " (found in your system)");
				}
				stopLoading();
			});
		}).start();
	}
	
	private static List<File> detected;
	
	public synchronized static List<File> detectJdkCache() {
		if(detected == null) {
			detected = detectJdk();
		}
		
		return detected;
	}
	 
	private static List<File> detectJdk() {
		ArrayList<File> res = new ArrayList<>();
		
		ArrayList<String> sources = new ArrayList<>();
		Command find = new Command(line -> sources.add(line), "cmd.exe", "/C", "dir /b /s javac.exe");

		try {
			find.execute(new File("C:\\Program Files")).waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
		
		for(String source : sources) {
			File javac = new File(source);
			if (javac.exists() && getVersionFromDir(javac.getParentFile().getParentFile()) != null) {
				res.add(javac.getParentFile().getParentFile());
			}
		}
		
		return res;
	}

}
