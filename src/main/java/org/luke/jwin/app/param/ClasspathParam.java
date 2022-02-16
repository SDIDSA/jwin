package org.luke.jwin.app.param;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.control.Hyperlink;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class ClasspathParam extends Param {

	private ArrayList<File> files;

	public ClasspathParam(Stage ps) {
		super("Classpath");
		files = new ArrayList<>();

		DirectoryChooser dc = new DirectoryChooser();
		addButton("add", e -> {
			File dir = dc.showDialog(ps);
			if (dir != null) {
				File projectRoot = findProjectRoot(dir);
				dc.setInitialDirectory(projectRoot);

				Hyperlink remove = new Hyperlink("remove");
				HBox line = addFile(dir, projectRoot.toURI().relativize(dir.toURI()).toString(), remove);
				files.add(dir);

				remove.setOnAction(ev -> {
					list.getChildren().remove(line);
					files.remove(dir);
				});
			}
		});
	}

	public Map<String, File> listClasses() {
		HashMap<String, File> res = new HashMap<>();

		files.forEach(file -> {
			List<String> classes = listClasses(file);
			classes.forEach(className -> res.put(className, file));
		});

		return res;
	}

	private static List<String> listClasses(File root) {
		return listClasses(root, root);
	}

	public static List<String> listClasses(File dir, File root) {
		ArrayList<String> res = new ArrayList<>();

		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				res.addAll(listClasses(file, root));
			} else if (file.getName().toLowerCase().contains(".java")) {
				res.add(root.toPath().relativize(file.toPath()).toString());
			}
		}

		return res;
	}

	private File findProjectRoot(File file) {
		File parent = file.getParentFile();
		File pom = null;

		for (File sf : parent.listFiles()) {
			if (sf.getName().equals("pom.xml")) {
				pom = sf;
				break;
			}
		}

		if (pom != null) {
			return parent;
		} else {
			return findProjectRoot(parent);
		}
	}

	public List<File> getPom() {
		ArrayList<File> res = new ArrayList<>();

		for (File file : files) {
			for (File sf : findProjectRoot(file).listFiles()) {
				if (sf.getName().equals("pom.xml") && !res.contains(sf)) {
					res.add(sf);
					break;
				}
			}
		}

		return res;
	}

	public List<File> getFiles() {
		return files;
	}

}
