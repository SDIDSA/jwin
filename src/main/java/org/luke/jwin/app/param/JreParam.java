package org.luke.jwin.app.param;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.function.DoubleConsumer;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.alert.AlertType;
import org.luke.gui.controls.alert.ButtonType;
import org.luke.gui.controls.label.unkeyed.Link;
import org.luke.gui.window.Window;
import org.luke.jwin.app.Command;
import org.luke.jwin.app.JwinActions;
import org.luke.jwin.app.param.deps.DependenciesParam;
import org.luke.jwin.app.param.main.MainClassParam;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class JreParam extends JavaParam {

	public JreParam(Window ps, ClasspathParam cp, JdkParam jdk, DependenciesParam dependencies, MainClassParam mc) {
		super(ps, "Jre (will be packed with your app)");

		DirectoryChooser dc = new DirectoryChooser();
		addButton(ps, "directory", () -> {
			File dir = dc.showDialog(ps);
			if (dir != null) {
				set(dir);
			}
		});

		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("archive", "*.zip"));
		addButton(ps, "archive", () -> {
			File file = fc.showOpenDialog(ps);
			if (file != null) {
				set(file);
			}
		});

		Link generateFromJdk = new Link(ps, "Generate JRE using jlink", new Font(12));
		root.setAlignment(Pos.CENTER_RIGHT);
		root.getChildren().add(0, generateFromJdk);

		generateFromJdk.setAction(() -> generateFromJdk(ps, cp, jdk, dependencies, mc));
	}

	public void generateFromJdk(Window ps, ClasspathParam cp, JdkParam jdk, DependenciesParam dependencies,
			MainClassParam mc) {
		startLoading();

		new Thread(() -> {
			Runnable cancel = this::stopLoading;
			Runnable invalidJdk = () -> {
				cancel.run();
				JwinActions.error("Invalid jdk", "The jdk you have specified can not be used to generate a jre");
			};

			if (dependencies.isResolving()) {
				cancel.run();
				JwinActions.warn("Resolving dependencies", "try again after dependencies are successfully resolved");
				return;
			}

			if (jdk.getValue() == null) {
				cancel.run();
				JwinActions.error("Missing Jdk", "You didn't specify a jdk");
				return;
			}

			if (!jdk.isJdk()) {
				invalidJdk.run();
				return;
			}

			File jlink = new File(jdk.getValue().getAbsolutePath().concat("/bin/jlink.exe"));
			File jdeps = new File(jdk.getValue().getAbsolutePath().concat("/bin/jdeps.exe"));
			if (!jlink.exists() || !jdeps.exists()) {
				invalidJdk.run();
				return;
			}

			ArrayList<String> deps = new ArrayList<>();

			File preGen = new File(System.getProperty("java.io.tmpdir") + "/jwin_preGen_" + new Random().nextInt(9999));
			preGen.mkdir();

			JwinActions.deleteDirOnShutdown(preGen);

			File preGenLibs = null;
			try {
				preGenLibs = dependencies.copy(preGen, null);
			} catch (IOException e1) {
				cancel.run();
				JwinActions.copyDependenciesFailure();
				return;
				
			}

			if(mc.getValue() == null) {
				cancel.run();
				JwinActions.error("Main class required", "You didn't specify the main class for your application");
				return;
			}
			
			if(!cp.isValidMainClass(mc.getValue().getValue())) {
				cancel.run();
				JwinActions.error("invalid mainClass", "The main class you selected doesn't belong in any of your classpath entries, are you sure you didn't remove it?");
				return;
			}
			
			File preGenBin = null;
			try {
				preGenBin = cp.compile(preGen, preGenLibs, jdk.getValue(), mc.getValue(), null, null);
			}catch(Exception x) {
				cancel.run();
				JwinActions.compileFailure();
				return;
			}
			
			if(preGenBin == null) {
				cancel.run();
				JwinActions.compileFailure();
				return;
			}
			
			File jdkBin = new File(jdk.getValue().getAbsolutePath().concat("/bin"));
			try {
				Platform.runLater(() -> startLoading("Analyzing dependencies ..."));
				Predicate<String> isValid = dep -> dep.indexOf("java.") == 0 || dep.indexOf("jdk.") == 0;
				// analyze code for module dependencies
				Command analCode = new Command(line -> {
					String[] parts = line.split("\\s+");

					if (parts.length == 3) {
						String dep = parts[parts.length - 1];

						if (!deps.contains(dep) && isValid.test(dep)) {
							deps.add(dep);
						}
					}
				}, "cmd", "/C", "jdeps -cp \"" + preGenBin.getAbsolutePath() + "\" --module-path \""
						+ preGenLibs.getAbsolutePath() + "\" \"" + preGenBin.getAbsolutePath() + "\"");

				analCode.execute(jdkBin).waitFor();
				deps.remove("bin");

				// analyze libs for moduleDependencies
				if (preGenLibs.listFiles().length != 0) {
					StringBuilder sb = new StringBuilder();
					for (File lib : preGenLibs.listFiles()) {
						sb.append(" \"").append(URLEncoder.encode(lib.getAbsolutePath(), StandardCharsets.UTF_8)).append("\"");
					}

					Command analLibs = new Command(line -> {
						String[] parts = line.split("\\s+");
						if (parts.length == 3) {
							String dep = parts[parts.length - 1];
							if (!deps.contains(dep) && isValid.test(dep)) {
								deps.add(dep);
							}
						}
					}, "cmd", "/c", "jdeps" + sb);
					analLibs.execute(jdkBin).waitFor();
				}

				System.out.println(deps);
				Platform.runLater(() -> startLoading("Generating JRE ..."));
				Command gen = new Command("cmd", "/c",
						"jlink --no-header-files --no-man-pages --strip-debug --module-path \""
								+ preGenLibs.getAbsolutePath() + "\" --add-modules " + String.join(",", deps)
								+ " --output \"" + preGen.getAbsolutePath().concat("/rt") + "\"");

				gen.execute(jdkBin).waitFor();

				File preGenRt = new File(preGen.getAbsolutePath().concat("/rt"));

				JwinActions.alert("Save this runtime",
						"Your custom runtime was generated successfully, do you want to save it for future use ?",
						AlertType.CONFIRM, result -> {
							if (result.equals(ButtonType.YES)) {
								DirectoryChooser dc = new DirectoryChooser();
								File saveTo = dc.showDialog(ps);
								if (saveTo != null) {
									startLoading();
									new Thread(() -> {
										File saveToRt = new File(saveTo.getAbsolutePath()
												.concat("/custom_jre_" + new Random().nextInt(999)));
										saveToRt.mkdir();
										JwinActions.copyDirCont(preGenRt, saveToRt, null);
										Platform.runLater(() -> {
											stopLoading();
											set(saveToRt, " (generated with jlink)");
										});
									}).start();
								}
							}
						}, ButtonType.YES, ButtonType.NO);

				Platform.runLater(() -> set(preGenRt, " (generated with jlink)"));

			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}

			Platform.runLater(this::stopLoading);

		}).start();
	}

	public void copy(File preBuild, DoubleConsumer onProgress) {
		File preBuildRt = new File(preBuild.getAbsolutePath().concat("/rt"));
		preBuildRt.mkdir();
		if (value.isFile()) {
			try {
				ZipFile zip = new ZipFile(value);

				String[] rootEntryPath = new String[1];

				Iterator<? extends ZipEntry> it = zip.entries().asIterator();
				int entryCount = 0;
				while (it.hasNext()) {
					entryCount++;
					Path path = Path.of(it.next().getName());
					if (path.endsWith("java.exe")) {
						String rootPath = "";
						for (int i = 0; i < path.getNameCount() - 2; i++) {
							rootPath = rootPath.concat(path.getName(i).toString()).concat("/");
						}
						rootEntryPath[0] = rootPath;
					}
				}

				String rootPath = rootEntryPath[0];
				if (rootPath != null) {
					final int ec = entryCount;
					int[] copyCount = new int[] { 0 };
					zip.entries().asIterator().forEachRemaining(entry -> {
						copyCount[0]++;
						String newName = entry.getName().replace(rootPath, "");

						if (!newName.isBlank()) {
							if (entry.isDirectory()) {
								File entryDir = new File(preBuildRt.getAbsolutePath().concat("/").concat(newName));
								entryDir.mkdir();
							} else {
								try {
									InputStream src = zip.getInputStream(entry);
									FileOutputStream dest = new FileOutputStream(
											preBuildRt.getAbsolutePath().concat("/").concat(newName));
									dest.write(src.readAllBytes());
									dest.close();
								} catch (IOException x) {
									x.printStackTrace();
								}
							}
						}
						
						if(onProgress != null) {
							onProgress.accept(.2 + (copyCount[0] / (double) ec) * .2);
						}
					});
				}

				zip.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else {
			int count = JwinActions.countDir(value);
			int[] copyCount = new int[] { 0 };
			JwinActions.copyDirCont(value, preBuildRt, () -> {
				copyCount[0]++;
				if(onProgress != null) {
					onProgress.accept(.2 + (copyCount[0] / (double) count) * .2);
				}
			});
		}
	}

}
