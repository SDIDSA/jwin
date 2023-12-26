package org.luke.jwin.app.param;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.unkeyed.Link;
import org.luke.gui.file.FileUtils;
import org.luke.gui.window.Window;
import org.luke.jwin.app.Command;
import org.luke.jwin.app.JwinActions;
import org.luke.jwin.app.file.FileDealer;

import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;

public class ClasspathParam extends Param {

	private File root;

	private ArrayList<File> files;

	private DirectoryChooser dc;

	public ClasspathParam(Window ps, File root) {
		super(ps, "classpath");

		this.root = root;

		files = new ArrayList<>();

		dc = new DirectoryChooser();
		addButton(ps, "add", () -> add());
	}

	public void setRoot(File root) {
		this.root = root;
	}

	public File getRoot() {
		if (root != null && root.exists()) {
			return root;
		} else {
			return findRoot();
		}
	}

	public File findRoot() {
		if (files.isEmpty()) {
			return null;
		} else {
			for (File f : files) {
				File root = findRoot(f, 0);
				if (root != null)
					return root;
			}
		}

		return null;
	}

	private File findRoot(File source, int step) {
		if (source.isDirectory() && (isMaven(source) || isGradle(source))) {
			return source;
		} else {
			File parent = source.getParentFile();
			if (parent.exists() && step < 6) {
				return findRoot(parent, step + 1);
			} else {
				return null;
			}
		}

	}

	private boolean isMaven(File root) {
		return new File(root.getAbsolutePath() + "\\pom.xml").exists();
	}

	private boolean isGradle(File root) {
		return new File(root.getAbsolutePath() + "\\build.gradle").exists()
				|| new File(root.getAbsolutePath() + "\\build.gradle.kts").exists();
	}

	public File add() {
		File dir = dc.showDialog(getWindow());
		if (dir != null) {
			add(dir);
		}

		return dir;
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

			Link remove = new Link(getWindow(), "remove");
			remove.setFont(new Font(12));
			HBox line = generateLine(getWindow(), dir, generateDisplay(dir), remove);
			mutex.acquireUninterruptibly();
			files.add(dir);
			mutex.release();

			remove.setAction(() -> {
				list.getChildren().remove(line);
				files.remove(dir);
			});

			Platform.runLater(() -> {
				list.getChildren().add(line);
				stopLoading();
			});
		}).start();
	}

	public boolean isConsoleApp() {
		Map<String, File> fs = listClasses();

		for (Map.Entry<String, File> ent : fs.entrySet()) {
			File f = new File(ent.getValue().getAbsolutePath().concat("/").concat(ent.getKey()));
			String content = FileUtils.readFile(f);
			String formattedSource = content.replace(" ", "").replace("\t", "").replace("\n", "");
			if (formattedSource.contains("(System.in)") || formattedSource.contains("System.out")
					|| formattedSource.contains("System.err")) {
				return true;
			}
		}

		return false;
	}

	public String generateDisplay(File dir) {
		File projectRoot = findProjectRoot(dir);
		return projectRoot == null ? dir.getParentFile().getParentFile().toURI().relativize(dir.toURI()).toString()
				: projectRoot.toURI().relativize(dir.toURI()).toString();
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

	public File compile(File preBuild, File preBuildLibs, File jdk, Entry<String, File> launcher,
			DoubleConsumer increment, Consumer<String> setAltMain) throws IllegalStateException {

		File preBuildBin = new File(preBuild.getAbsolutePath().concat("/bin"));
		preBuildBin.mkdir();
		File binDir = new File(jdk.getAbsolutePath().concat("/bin"));
		StringBuilder cpc = new StringBuilder();
		Consumer<String> append = path -> cpc.append(cpc.isEmpty() ? "" : ";").append(path);
		append.accept(preBuildLibs.getAbsolutePath().concat("/*"));
		files.forEach(file -> append.accept(file.getAbsolutePath()));
		ArrayList<String> x = new ArrayList<>();

		// manually include all classes for the javac command
		Map<String, File> classes = listClasses();
		List<Map.Entry<String, File>> classPaths = new ArrayList<Map.Entry<String, File>>(classes.entrySet());
		Function<Map.Entry<String, File>, String> parametrize = ent -> {
			return " \"" + ent.getValue().getAbsolutePath() + "\\" + ent.getKey() + "\"";
		};

		try {
			String mainClassCommand = "javac -cp \"" + cpc + "\" -d \"" + preBuildBin.getAbsolutePath() + "\" \""
					+ launcher.getValue().getAbsolutePath() + "\"";

			Command mainCompileCommand = new Command(line -> {
				if (!line.isBlank()) {
					x.add(line);
				}
			}, "cmd.exe", "/C", mainClassCommand.toString());

			mainCompileCommand.execute(binDir, () -> {
				if (increment != null)
					increment.accept(.6);
			}).waitFor();

			while (!classPaths.isEmpty()) {
				StringBuilder com = new StringBuilder(
						"javac -cp \"" + cpc + "\" -d \"" + preBuildBin.getAbsolutePath() + "\"");

				ArrayList<Map.Entry<String, File>> added = new ArrayList<Map.Entry<String, File>>();
				ArrayList<Map.Entry<String, File>> removed = new ArrayList<Map.Entry<String, File>>();

				for (Map.Entry<String, File> source : classPaths) {
					String path = parametrize.apply(source);
					if (com.length() + path.length() < 4000) {
						File fileToCheck = new File(
								preBuildBin.getAbsolutePath() + "\\" + source.getKey().replace(".java", ".class"));
						if (!fileToCheck.exists()) {
							com.append(path);
							added.add(source);
						}

						removed.add(source);
					}
				}

				classPaths.removeAll(removed);
				
				if(!added.isEmpty()) {
					Command compileCommand = new Command(line -> {
						if (!line.isBlank()) {
							x.add(line);
						}
					}, "cmd.exe", "/C", com.toString());

					compileCommand.execute(binDir, () -> {
						if (increment != null)
							increment.accept(.6);
					}).waitFor();
				}
			}

			if (!x.isEmpty()) {
				IllegalStateException ex = new IllegalStateException("Failed to Compile");
				ex.initCause(new IllegalStateException(String.join("\n", x)));
				throw ex;
			}

			String content = FileUtils.readFile(launcher.getValue());

			String formattedSource = content.replace(" ", "").replace("\t", "").replace("\n", "");

			if (formattedSource.contains("extendsApplication{")) {
				JwinActions.deleteDir(preBuildBin);
				preBuildBin.mkdir();

				String packageName = content.trim().split(";")[0].trim().toLowerCase();
				packageName = packageName.startsWith("") ? packageName.replace("package", "").trim() : "";

				File newLauncherFile = new File(preBuildBin.getAbsolutePath().concat("/").concat("Launcher.java"));

				String newLauncherContent = generateLauncher(packageName, launcher);

				FileDealer.write(newLauncherContent, newLauncherFile);

				Command compileNewLauncher = new Command("cmd.exe", "/C", "javac -cp \"" + cpc + "\" -d \""
						+ preBuildBin.getAbsolutePath() + "\" \"" + newLauncherFile.getAbsolutePath() + "\"");
				compileNewLauncher.execute(binDir).waitFor();

				if (setAltMain != null) {
					setAltMain.accept(packageName.concat((packageName.isBlank() ? "" : ".") + "Launcher"));
				}
			}

			return preBuildBin;
		} catch (InterruptedException | SecurityException | UnsupportedClassVersionError e1) {
			e1.printStackTrace();
			JwinActions.error("Failed to compile/load your code", e1.getMessage());
			Thread.currentThread().interrupt();
		}
		return null;
	}

	private String generateLauncher(String packageName, Entry<String, File> launcher) {
		StringBuilder launcherContent = new StringBuilder();
		boolean blankPackage = packageName.isBlank();
		if (!blankPackage) {
			launcherContent.append("package ").append(packageName).append(";\n\n");
		}
		launcherContent.append(
				"import javafx.application.Application;\n\npublic class Launcher {\n\tpublic static void main(String[] args) {\n\t\tApplication.launch(")
				.append(launcher.getKey()).append(".class, args);\n\t}\n}").toString();
		return launcherContent.toString();
	}

	public boolean isValidMainClass(File mainClass) {
		for (File root : files) {
			if (mainClass.getAbsolutePath().contains(root.getAbsolutePath())) {
				return true;
			}
		}

		return false;
	}

	public void copyRes(File preBuild, DoubleConsumer onProgress) {
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
				onProgress.accept(.6 + (resCopyCount[0] / (double) resCount[0]) * .4);
			});
		});
	}

	private void copyResource(File src, File srcRoot, File destRoot) {
		File dest = new File(src.getAbsolutePath().replace(srcRoot.getAbsolutePath(), destRoot.getAbsolutePath()));

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
