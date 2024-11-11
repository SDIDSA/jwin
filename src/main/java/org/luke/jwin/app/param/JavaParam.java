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
import java.util.Objects;
import java.util.zip.ZipFile;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.unkeyed.Text;
import org.luke.gui.exception.ErrorHandler;
import org.luke.gui.window.Window;

import javafx.application.Platform;

public class JavaParam extends Param {

	protected File value;
	protected String version;

	public JavaParam(Window window, String name) {
		super(window, name);
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
		for (File sf : Objects.requireNonNull(file.listFiles())) {
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
					ErrorHandler.handle(e, "get java version");
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
						ErrorHandler.handle(e1, "parse jre version from archive");
					}
				}
			});
			arch.close();
			return meta.get("JAVA_VERSION");
		} catch (IOException e1) {
			ErrorHandler.handle(e1, "parse jre version from archive");
		}
		return null;
	}

	public boolean isJdk() {
		return isJdk(value);
	}

	public static boolean isJdk(File root) {
		if (root == null || !root.exists())
			return false;
		File javac = new File(root.getAbsolutePath().concat("/bin/javac.exe"));
		return javac.exists();
	}

	protected void set(File file) {
		set(file, "", null);
	}

	protected void set(File file, Runnable onFinish) {
		set(file, "", onFinish);
	}

	protected void set(File file, String additional) {
		set(file, additional, null);
	}

	protected void set(File file, String additional, Runnable onFinish) {
		if (file == null || !file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			setDir(file, additional, onFinish);
		} else {
			setZip(file, additional, onFinish);
		}
	}

	protected void setDir(File dir, String additional, Runnable onFinish) {
		if (!dir.exists()) {
			return;
		}
		startLoading();
		new Thread(() -> {
			Entry<String, File> version = getVersionFromDir(dir);
			if (version != null && version.getKey() != null) {
				this.version = version.getKey().replace("\"", "");
				this.value = version.getValue();
				Platform.runLater(() -> {
					list.getChildren().clear();
					addFile(getWindow(), value, value.getName() + additional, new Text(this.version));
				});
			}
			Platform.runLater(this::stopLoading);

			if (onFinish != null) {
				Platform.runLater(onFinish);
			}
		}, "java directory parser").start();
	}

	protected void setZip(File file, String additional, Runnable onFinish) {
		startLoading();
		new Thread(() -> {
			String version = getVersionFromZip(file);
			if (version != null) {
				this.version = version.replace("\"", "");
				this.value = file;
				Platform.runLater(() -> {
					list.getChildren().clear();
					addFile(getWindow(), file, file.getName() + additional,
							new Text(this.version, new Font(12)));
				});
			}
			Platform.runLater(this::stopLoading);

			if (onFinish != null) {
				Platform.runLater(onFinish);
			}
		}, "java zip parser").start();
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
