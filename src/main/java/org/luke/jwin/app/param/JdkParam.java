package org.luke.jwin.app.param;

import java.io.File;
import java.util.Map.Entry;

import org.luke.jwin.app.Command;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class JdkParam extends JavaParam {
	public JdkParam(Stage ps) {
		super("Jdk (will be use to compile your app)");

		DirectoryChooser dc = new DirectoryChooser();
		addButton("detect", e-> detect());
		addButton("select", e -> {
			File dir = dc.showDialog(ps);
			if (dir != null) {
				Entry<String, File> version = getVersionFromDir(dir);
				if (version != null && version.getKey() != null) {
					this.version = version.getKey().replace("\"", "");
					list.getChildren().clear();
					this.value = version.getValue();
					addFile(value, value.getName(), new Label(this.version));
				}
			}
		});
		
	}

	private void detect() {
		startLoading();
		new Thread(()-> {
			String[] source = new String[1];
			Command find = new Command(line -> {
				source[0] = line;
			}, "cmd.exe", "/C", "where javac");

			try {
				find.execute(new File("/")).waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}

			
			Platform.runLater(()-> {
				if(source[0] != null) {
					File javac = new File(source[0]);
					if(javac.exists()) {
						Entry<String, File> version = getVersionFromDir(javac.getParentFile().getParentFile());
						if (version != null && version.getKey() != null) {
							this.version = version.getKey().replace("\"", "");
							list.getChildren().clear();
							this.value = version.getValue();
							addFile(value, value.getName().concat(" (found in the system)"), new Label(this.version));
						}
					}
				}
				
				stopLoading();
			});
		}).start();

	}

}
