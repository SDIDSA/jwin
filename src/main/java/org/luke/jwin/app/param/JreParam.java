package org.luke.jwin.app.param;

import java.io.File;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class JreParam extends JavaParam {

	public JreParam(Stage ps) {
		super("Jre (will be packed with your app)");

		DirectoryChooser dc = new DirectoryChooser();
		addButton("directory", e -> {
			File dir = dc.showDialog(ps);
			if (dir != null) {
				set(dir);
			}
		});

		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("archive", "*.zip"));
		addButton("archive", e -> {
			File file = fc.showOpenDialog(ps);
			if (file != null) {
				set(file);
			}
		});
	}

}
