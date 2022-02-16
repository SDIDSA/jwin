package org.luke.jwin.app.param;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.ZipFile;

import javafx.scene.control.Label;
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
				Entry<String, File> version = getVersionFromDir(dir);
				if (version != null && version.getKey() != null) {
					this.version = version.getKey().replace("\"", "");
					list.getChildren().clear();
					this.value = version.getValue();
					addFile(value, value.getName(), new Label(this.version));
				}
			}
		});

		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("archive", "*.zip"));
		addButton("archive", e -> {
			File file = fc.showOpenDialog(ps);
			if (file != null) {
				String version = getVersionFromZip(file);
				if (version != null) {
					this.version = version.replace("\"", "");
					list.getChildren().clear();
					this.value = file;
					addFile(file, file.getName(), new Label(this.version));
				}
			}
		});
	}

	public static String getVersionFromZip(File file) {
		try {
			ZipFile arch = new ZipFile(file);
			HashMap<String, String> meta = new HashMap<>();
			arch.entries().asIterator().forEachRemaining(entry -> {
				Path path = Paths.get(entry.getName());
				if (path.endsWith("release")) {
					try {
						InputStream is = arch.getInputStream(entry);

						meta.putAll(parseInputStream(is));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
			arch.close();
			return meta.get("JAVA_VERSION");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}

}
