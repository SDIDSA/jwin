package org.luke.jwin.app.param;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.luke.jwin.app.Command;
import org.luke.jwin.app.Jwin;

import javafx.application.Platform;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ProgressBar;
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
		new Thread(() -> {
			File projectRoot = findProjectRoot(dir);
			dc.setInitialDirectory(projectRoot);

			Hyperlink remove = new Hyperlink("remove");
			HBox line = generateLine(dir,
					projectRoot == null ? dir.getParentFile().getParentFile().toURI().relativize(dir.toURI()).toString()
							: projectRoot.toURI().relativize(dir.toURI()).toString(),
					remove);
			files.add(dir);

			remove.setOnAction(ev -> {
				list.getChildren().remove(line);
				files.remove(dir);
			});

			Platform.runLater(() -> {
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
			if (root != null) {
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

	public void compile(File preBuild, File preBuildLibs, File jdk, File launcher, ProgressBar progress) {
		File preBuildBin = new File(preBuild.getAbsolutePath().concat("/bin"));
		preBuildBin.mkdir();
		File binDir = new File(jdk.getAbsolutePath().concat("/bin"));
		StringBuilder cpc = new StringBuilder();
		Consumer<String> append = path -> cpc.append(cpc.isEmpty() ? "" : ";").append(path);
		append.accept(preBuildLibs.getAbsolutePath().concat("/*"));
		files.forEach(file -> append.accept(file.getAbsolutePath()));
		Command compileCommand = new Command("cmd.exe", "/C", "javac -cp \"" + cpc + "\" -d \""
				+ preBuildBin.getAbsolutePath() + "\" \"" + launcher.getAbsolutePath() + "\"");
		try {
			compileCommand.execute(binDir, () -> progress.setProgress(Math.min(.6, progress.getProgress() + .005)))
					.waitFor();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	public void copyRes(File preBuild, ProgressBar progress) {
		File preBuildRes = new File(preBuild.getAbsolutePath().concat("/res"));
		preBuildRes.mkdir();

		int[] resCount = new int[] { 0 };

		ArrayList<File> valid = new ArrayList<>();
		files.forEach(file -> {
			if (ClasspathParam.listClasses(file, file).isEmpty()) {
				resCount[0] += Jwin.countDir(file);
				valid.add(file);
			}
		});
		int[] resCopyCount = new int[] { 0 };
		valid.forEach(file -> Jwin.copyDirCont(file, preBuildRes, () -> {
			resCopyCount[0]++;
			Platform.runLater(() -> progress.setProgress(.6 + (resCopyCount[0] / (double) resCount[0]) * .2));
		}));
	}

}
