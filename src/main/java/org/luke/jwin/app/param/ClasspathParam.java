package org.luke.jwin.app.param;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class ClasspathParam extends Param {

	private ArrayList<File> files;

	private DirectoryChooser dc;
	
	public ClasspathParam(Stage ps) {
		super("Classpath");
		files = new ArrayList<>();

		dc = new DirectoryChooser();
		addButton("add", e -> {
			File dir = dc.showDialog(ps);
			if (dir != null) {
				add(dir);
			}
		});
	}
	
	public void add(File dir) {
		startLoading();
		new Thread(()-> {
			File projectRoot = findProjectRoot(dir);
			dc.setInitialDirectory(projectRoot);

			Hyperlink remove = new Hyperlink("remove");
			HBox line = generateLine(dir,
					projectRoot == null
							? dir.getParentFile().getParentFile().toURI().relativize(dir.toURI()).toString()
							: projectRoot.toURI().relativize(dir.toURI()).toString(),
					remove);
			files.add(dir);

			remove.setOnAction(ev -> {
				list.getChildren().remove(line);
				files.remove(dir);
			});
			
			Platform.runLater(()-> {
				list.getChildren().add(line);
				stopLoading();
			});
		}).start();	
	}
	
	@Override
	public void clear() {
		files.clear();
		list.getChildren().clear();
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

		if (parent == null) {
			return parent;
		}

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
			File root = findProjectRoot(file);
			if(root != null) {
				for (File sf : root.listFiles()) {
					if (sf.getName().equals("pom.xml") && !res.contains(sf)) {
						res.add(sf);
						break;
					}
				}
			}
		}

		return res;
	}

	public List<File> getFiles() {
		return files;
	}

}
