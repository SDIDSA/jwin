package org.luke.jwin.app.param;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipFile;

import javafx.application.Platform;
import javafx.scene.control.Label;

public class JavaParam extends Param {

	protected File value;
	protected String version;

	public JavaParam(String name) {
		super(name);
	}

	public File getValue() {
		return value;
	}

	public String getVersion() {
		return version;
	}

	public static Entry<String, File> getVersionFromDir(File file) {
		if (file.listFiles() == null) {
			return null;
		}
		for (File sf : file.listFiles()) {
			if (sf.isDirectory()) {
				Entry<String, File> version = getVersionFromDir(sf);
				if (version != null) {
					return version;
				}
			} else if (sf.getName().equals("release")) {
				try {
					HashMap<String, String> data = parseInputStream(new FileInputStream(sf));
					return Map.entry(data.get("JAVA_VERSION"), sf.getParentFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
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

	public boolean isJdk() {
		File javac = new File(value.getAbsolutePath().concat("/bin/javac.exe"));
		return javac.exists();
	}

	public void set(File file) {
		if (file.isDirectory()) {
			setDir(file);
		} else {
			setZip(file);
		}
	}

	public void setDir(File dir) {
		startLoading();
		new Thread(() -> {
			Entry<String, File> version = getVersionFromDir(dir);
			if (version != null && version.getKey() != null) {
				this.version = version.getKey().replace("\"", "");
				this.value = version.getValue();
				Platform.runLater(() -> {
					list.getChildren().clear();
					addFile(value, value.getName(), new Label(this.version));
				});
			}
			Platform.runLater(this::stopLoading);
		}).start();
	}

	public void setZip(File file) {
		startLoading();
		new Thread(() -> {
			String version = getVersionFromZip(file);
			if (version != null) {
				this.version = version.replace("\"", "");
				this.value = file;
				Platform.runLater(() -> {
					list.getChildren().clear();
					addFile(file, file.getName(), new Label(this.version));
				});
			}
			Platform.runLater(this::stopLoading);
		}).start();
	}

	protected static HashMap<String, String> parseInputStream(InputStream is) throws IOException {
		HashMap<String, String> data = new HashMap<>();

		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;

		while ((line = br.readLine()) != null) {
			String[] parts = line.split("=");
			data.put(parts[0], parts[1]);
		}

		is.close();
		br.close();

		return data;
	}

	@Override
	public void clear() {
		value = null;
		version = null;
		list.getChildren().clear();
	}
}
