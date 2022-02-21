package org.luke.jwin.app.param;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import org.luke.jwin.app.Command;

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

	private Semaphore mutex = new Semaphore(1);

	public void add(File dir) {
		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}
		startLoading();
		new Thread(() -> {
			File projectRoot = findProjectRoot(dir);
			dc.setInitialDirectory(projectRoot);

			Hyperlink remove = new Hyperlink("remove");
			HBox line = generateLine(dir,
					projectRoot == null ? dir.getParentFile().getParentFile().toURI().relativize(dir.toURI()).toString()
							: projectRoot.toURI().relativize(dir.toURI()).toString(),
					remove);
			mutex.acquireUninterruptibly();
			files.add(dir);
			mutex.release();

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

	public static List<File> listResources(File dir) {
		ArrayList<File> res = new ArrayList<>();

		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				res.addAll(listResources(file));
			} else if (!file.getName().toLowerCase().contains(".java")) {
				res.add(file);
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

	public File compile(File preBuild, File preBuildLibs, File jdk, File launcher, ProgressBar progress)
			throws IllegalStateException {
		File preBuildBin = new File(preBuild.getAbsolutePath().concat("/bin"));
		preBuildBin.mkdir();
		File binDir = new File(jdk.getAbsolutePath().concat("/bin"));
		StringBuilder cpc = new StringBuilder();
		Consumer<String> append = path -> cpc.append(cpc.isEmpty() ? "" : ";").append(path);
		append.accept(preBuildLibs.getAbsolutePath().concat("/*"));
		files.forEach(file -> append.accept(file.getAbsolutePath()));
		ArrayList<String> x = new ArrayList<>();
		Command compileCommand = new Command(line -> {
			if (!line.isBlank()) {
				x.add(line);
			}
		}, "cmd.exe", "/C", "javac -cp \"" + cpc + "\" -d \"" + preBuildBin.getAbsolutePath() + "\" \""
				+ launcher.getAbsolutePath() + "\"");
		try {
			compileCommand.execute(binDir, () -> {
				if (progress != null)
					progress.setProgress(Math.min(.6, progress.getProgress() + .005));
			}).waitFor();

			if (!x.isEmpty()) {
				IllegalStateException ex = new IllegalStateException("Failed to Compile");
				ex.initCause(new IllegalStateException(String.join("\n", x)));

				throw ex;
			}
			return preBuildBin;
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			Thread.currentThread().interrupt();
		}
		return null;
	}

	public boolean isValidMainClass(File mainClass) {
		for (File root : files) {
			if (mainClass.getAbsolutePath().contains(root.getAbsolutePath())) {
				return true;
			}
		}

		return false;
	}

	public void copyRes(File preBuild, ProgressBar progress) {
		File preBuildRes = new File(preBuild.getAbsolutePath().concat("/res"));
		preBuildRes.mkdir();

		int[] resCount = new int[] { 0 };

		HashMap<File, List<File>> resourcesToCopy = new HashMap<>();

		files.forEach(file -> {
			List<File> resources = listResources(file);
			if (!resources.isEmpty()) {
				resCount[0] += resources.size();
				resourcesToCopy.put(file, resources);
			}
		});
		int[] resCopyCount = new int[] { 0 };

		resourcesToCopy.keySet().forEach(key -> {
			List<File> resources = resourcesToCopy.get(key);
			resources.forEach(resource -> {
				resCopyCount[0]++;
				copyResource(resource, key, preBuildRes);
				Platform.runLater(() -> progress.setProgress(.6 + (resCopyCount[0] / (double) resCount[0]) * .2));
			});
		});
	}

	private void copyResource(File src, File srcRoot, File destRoot) {
		File dest = new File(src.getAbsolutePath().replace(srcRoot.getAbsolutePath(), destRoot.getAbsolutePath()));

		System.out.println(src.getAbsolutePath() + " => " + dest.getAbsolutePath());

		List<File> parents = getParentsUntil(dest, destRoot);
		Collections.sort(parents);
		parents.forEach(e -> {
			if (!e.exists()) {
				e.mkdir();
			}
		});

		try {
			Files.copy(src.toPath(), dest.toPath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private List<File> getParentsUntil(File src, File root) {
		ArrayList<File> res = new ArrayList<>();

		File parent = src.getParentFile();
		if (parent.equals(root)) {
			return res;
		} else {
			res.add(parent);
			res.addAll(getParentsUntil(parent, root));
		}

		return res;
	}

}
