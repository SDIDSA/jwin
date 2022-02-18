package org.luke.jwin.app.param;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
	
	public boolean isJdk() {
		File javac = new File(value.getAbsolutePath().concat("/bin/javac.exe"));
		return javac.exists();
	}

	public void set(File dir) {
		Entry<String, File> version = getVersionFromDir(dir);
		if (version != null && version.getKey() != null) {
			this.version = version.getKey().replace("\"", "");
			list.getChildren().clear();
			this.value = version.getValue();
			addFile(value, value.getName(), new Label(this.version));
		}
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
}
