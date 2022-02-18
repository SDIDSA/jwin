package org.luke.jwin.app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.module.ModuleDescriptor.Version;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.Random;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.luke.jwin.app.file.FileDealer;
import org.luke.jwin.app.file.JWinProject;
import org.luke.jwin.app.param.ClasspathParam;
import org.luke.jwin.app.param.DependenciesParam;
import org.luke.jwin.app.param.IconParam;
import org.luke.jwin.app.param.JdkParam;
import org.luke.jwin.app.param.JreParam;
import org.luke.jwin.app.param.MainClassParam;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class Jwin extends Application {

	Random random = new Random();

	private JWinProject projectFile;
	
	@Override
	public void start(Stage ps) throws Exception {
		HBox preRoot = new HBox(15);
		preRoot.setPadding(new Insets(15));

		VBox root1 = new VBox(15);

		VBox root2 = new VBox(15);

		ClasspathParam classpath = new ClasspathParam(ps);

		MainClassParam mainClass = new MainClassParam(ps, classpath::listClasses);

		DependenciesParam dependencies = new DependenciesParam(ps, classpath::getPom);

		JdkParam jdk = new JdkParam(ps);
		JreParam jre = new JreParam(ps);

		IconParam icon = new IconParam(ps);

		Insets pad = new Insets(8, 10, 8, 10);

		Button compile = new Button("Build");
		compile.setPadding(pad);
		compile.setMinWidth(60);

		Button save = new Button("Save");
		save.setPadding(pad);
		save.setMinWidth(60);

		Button load = new Button("Load");
		load.setPadding(pad);
		load.setMinWidth(60);

		DirectoryChooser fc = new DirectoryChooser();

		HBox bottom = new HBox(10);
		bottom.setAlignment(Pos.BOTTOM_CENTER);

		ProgressBar progress = new ProgressBar();
		Label state = new Label("doing nothing");
		state.setPadding(pad);

		StackPane progressCont = new StackPane(progress, state);
		progressCont.setAlignment(Pos.BOTTOM_LEFT);
		progress.prefWidthProperty().bind(progressCont.widthProperty());

		HBox.setHgrow(progressCont, Priority.ALWAYS);

		progress.minHeightProperty().bind(compile.heightProperty());
		progress.setStyle("-fx-accent: lightblue");

		TextVal appName = new TextVal("App name");
		TextVal version = new TextVal("Version");
		TextVal publisher = new TextVal("Publisher");

		CheckBox console = new CheckBox("Console");

		TextField guid = new TextField();
		guid.setPromptText("GUID");
		guid.setEditable(false);
		HBox.setHgrow(guid, Priority.ALWAYS);
		
		Button generate = new Button("Generate");
		generate.setOnAction(e->guid.setText(UUID.randomUUID().toString()));
		
		Supplier<JWinProject> export = () -> new JWinProject(
				classpath.getFiles(), 
				mainClass.getValue(), 
				jdk.getValue(), 
				jre.getValue(), 
				icon.getValue(), 
				dependencies.getResolvedJars(), 
				dependencies.getManualJars(), 
				appName.getValue(), 
				version.getValue(), 
				publisher.getValue(), 
				console.isSelected(),
				guid.getText()
		);
		
		HBox preConsole = new HBox(10);
		preConsole.setAlignment(Pos.CENTER);
		
		preConsole.getChildren().addAll(console, new Separator(Orientation.VERTICAL), new Label("GUID"),guid, generate);
		
		HBox preBottom = new HBox(15, appName, version, publisher);

		bottom.getChildren().addAll(progressCont, compile, save, load);

		root1.getChildren().addAll(classpath, new Separator(), mainClass, vSpace(), jdk, jre);

		root2.getChildren().addAll(icon, new Separator(), dependencies, preBottom, new Separator(), preConsole,
				bottom);

		preRoot.getChildren().addAll(root1, root2);

		Scene scene = new Scene(preRoot);

		ps.setScene(scene);
		ps.setHeight(516);
		ps.setTitle("jWin");
		ps.setOnShown(e -> ps.centerOnScreen());

		for (int i = 16; i <= 128; i *= 2) {
			ps.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png"), i, i, true, true));
		}

		ps.show();

		compile.setOnAction(e -> {
			// Check for errors
			List<File> cp = classpath.getFiles();

			if (cp.isEmpty()) {
				error("Missing classpath", "You didn't add any classpath folders");
				return;
			}

			Entry<String, File> launcher = mainClass.getValue();

			if (launcher == null) {
				error("Main class required", "You didn't specify the main class for your application");
				return;
			}

			List<File> deps = dependencies.getJars();

			File dk = jdk.getValue();

			if (dk == null) {
				error("Missing Jdk", "You didn't select the jdk to compile your application");
				return;
			}

			if (!jdk.isJdk()) {
				error("Invalid Jdk",
						"The jdk directory you selected is unsupported and can not be used to compile your app");
				return;
			}

			File rt = jre.getValue();

			if (rt == null) {
				error("Missing Jre", "You didn't select the jre to run your application");
				return;
			}

			String jreVersionString = jre.getVersion().split("_")[0];
			String jdkVersionString = jdk.getVersion().split("_")[0];

			Version jreVersion = Version.parse(jreVersionString);
			Version jdkVersion = Version.parse(jdkVersionString);

			int versionCompare = jreVersion.compareTo(jdkVersion);
			if (versionCompare < 0) {
				error("Uncompatible Java versions",
						"Your JRE " + jreVersion + " will not be able to run code compiled by Your JDK " + jdkVersion);
				return;
			}

			// Check for warnings
			if (jre.isJdk()) {
				warn("Using JDK as a runtime", "not recommended unless required by your app (increases package size)");
			}

			if(projectFile == null) {
				alert("Do you want to save the project before building ?", "It is recommended to save this project so you can use the same GUID when you build it again, do not use the same GUID with different projects", AlertType.CONFIRMATION, res -> {
					if(res.equals(ButtonType.OK)) {
						save.fire();
						return;
					}
				});
			}else {
				List<String> diffs = export.get().compare(projectFile);
				
				if(!diffs.isEmpty()) {
					StringBuilder diffStr = new StringBuilder();
					diffs.forEach(diff -> diffStr.append("\t").append(diff).append("\n"));
					
					alert("Do you want to save the changes you made ?", "You made changes to the following properties : \n\t" + diffStr.toString().trim(), AlertType.CONFIRMATION, res -> {
						if(res.equals(ButtonType.OK)) {
							save.fire();
							return;
						}
					});
				}
			}
			
			alert("Select output directory", "select the directory where you want to save the generated installer", AlertType.INFORMATION);
			File saveTo = fc.showDialog(ps);
			if (saveTo != null) {
				compile.setDisable(true);
				new Thread(() -> {
					File preBuild = new File(
							System.getProperty("java.io.tmpdir") + "/jwin_pre_build_" + random.nextInt(999999));
					preBuild.mkdir();

					Platform.runLater(() -> state.setText("Copying dependencies"));
					File preBuildLibs = new File(preBuild.getAbsolutePath().concat("/lib"));
					preBuildLibs.mkdir();
					for (int i = 0; i < deps.size(); i++) {
						File dep = deps.get(i);
						try {
							Files.copy(dep.toPath(),
									Path.of(preBuildLibs.getAbsolutePath().concat("/").concat(dep.getName())));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						final int fi = i;
						Platform.runLater(() -> progress.setProgress((fi / (double) deps.size()) * .2));
					}

					Platform.runLater(() -> state.setText("Copying runtime"));
					File preBuildRt = new File(preBuild.getAbsolutePath().concat("/rt"));
					preBuildRt.mkdir();
					if (rt.isFile()) {
						try {
							ZipFile zip = new ZipFile(rt);

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
											File entryDir = new File(
													preBuildRt.getAbsolutePath().concat("/").concat(newName));
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

									Platform.runLater(
											() -> progress.setProgress(.2 + (copyCount[0] / (double) ec) * .2));
								});
							}

							zip.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					} else {
						int count = countDir(rt);
						int[] copyCount = new int[] { 0 };
						copyDirCont(rt, preBuildRt, () -> {
							copyCount[0]++;
							Platform.runLater(() -> progress.setProgress(.2 + (copyCount[0] / (double) count) * .2));
						});
					}

					Platform.runLater(() -> state.setText("Compiling source code"));
					File preBuildBin = new File(preBuild.getAbsolutePath().concat("/bin"));
					preBuildBin.mkdir();
					File binDir = new File(dk.getAbsolutePath().concat("/bin"));
					StringBuilder cpc = new StringBuilder();
					Consumer<String> append = path -> cpc.append(cpc.isEmpty() ? "" : ";").append(path);
					append.accept(preBuildLibs.getAbsolutePath().concat("/*"));
					cp.forEach(file -> append.accept(file.getAbsolutePath()));
					Command compileCommand = new Command("cmd.exe", "/C", "javac -cp \"" + cpc + "\" -d \""
							+ preBuildBin.getAbsolutePath() + "\" \"" + launcher.getValue().getAbsolutePath() + "\"");
					try {
						compileCommand
								.execute(binDir,
										() -> progress.setProgress(Math.min(.6, progress.getProgress() + .005)))
								.waitFor();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
						Thread.currentThread().interrupt();
					}

					Platform.runLater(() -> state.setText("Copying resources"));
					File preBuildRes = new File(preBuild.getAbsolutePath().concat("/res"));
					preBuildRes.mkdir();

					int[] resCount = new int[] { 0 };
					cp.forEach(file -> {
						if (ClasspathParam.listClasses(file, file).isEmpty()) {
							resCount[0] += countDir(file);
						}
					});
					int[] resCopyCount = new int[] { 0 };
					cp.forEach(file -> {
						if (ClasspathParam.listClasses(file, file).isEmpty()) {
							copyDirCont(file, preBuildRes, () -> {
								resCopyCount[0]++;
								Platform.runLater(
										() -> progress.setProgress(.6 + (resCopyCount[0] / (double) resCount[0]) * .2));
							});
						}
					});

					Platform.runLater(() -> state.setText("Generating launcher"));

					File preBuildBat = new File(
							preBuild.getAbsolutePath().concat("/").concat(appName.getValue()).concat(".bat"));
					try {
						BufferedWriter bw = new BufferedWriter(
								new OutputStreamWriter(new FileOutputStream(preBuildBat)));
						bw.append("\"rt/bin/java\" -cp \"res;bin;lib/*\" " + launcher.getKey());
						bw.flush();
						bw.close();
					} catch (IOException x) {
						x.printStackTrace();
					}

					File b2e = new File(
							URLDecoder.decode(getClass().getResource("/b2e.exe").getFile(), Charset.defaultCharset()));

					String convertCommand = "b2e /bat \"" + preBuildBat.getAbsolutePath() + "\" /exe \""
							+ preBuildBat.getAbsolutePath().replace(".bat", ".exe") + "\"";

					if (!console.isSelected()) {
						convertCommand += " /invisible";
					}
					if (icon.getValue() != null) {
						convertCommand += " /icon \"" + icon.getValue().getAbsolutePath() + "\"";
					}

					Command convert = new Command("cmd.exe", "/C", convertCommand);

					try {
						convert.execute(b2e.getParentFile()).waitFor();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
						Thread.currentThread().interrupt();
					}
					System.out.println(preBuildBat.delete());

					Platform.runLater(() -> state.setText("Generating installer"));
					String template = FileDealer.read("/ist.txt").replace(key("app_name"), appName.getValue())
							.replace(key("app_version"), version.getValue())
							.replace(key("app_publisher"), publisher.getValue())
							.replace(key("output_folder"), saveTo.getAbsolutePath())
							.replace(key("installer_name"),
									appName.getValue().concat("_").concat(version.getValue().replace(".", "-"))
											.concat("_installer"))
							.replace(key("app_icon"), icon.getValue().getAbsolutePath())
							.replace(key("prebuild_path"), preBuild.getAbsolutePath())
							.replace(key("GUID"), guid.getText());
					File buildScript = new File(
							System.getProperty("java.io.tmpdir") + "/jwin_iss_" + random.nextInt(999999) + ".iss");
					FileDealer.write(template, buildScript);

					File ise = new File(URLDecoder.decode(getClass().getResource("/is/ISCC.exe").getFile(),
							Charset.defaultCharset()));

					int fileCount = countDir(preBuild);
					int[] compressCount = new int[] { 0 };
					Consumer<String> buildLine = line -> {
						if (line.trim().indexOf("Compressing") == 0) {
							compressCount[0]++;
							Platform.runLater(
									() -> progress.setProgress(.8 + (compressCount[0] / (double) fileCount) * .2));
						}
					};
					Command build = new Command(buildLine, buildLine, "cmd.exe", "/C",
							"ISCC \"" + buildScript.getAbsolutePath() + "\"");

					try {
						build.execute(ise.getParentFile()).waitFor();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
						Thread.currentThread().interrupt();
					}

					try {
						Files.walk(preBuild.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile)
								.forEach(File::delete);
					} catch (IOException e1) {
						e1.printStackTrace();
					}

					Platform.runLater(() -> {
						state.setText("done");
						progress.setProgress(-1);
						compile.setDisable(false);
					});
				}).start();

			}
		});
		
		FileChooser saver = new FileChooser();
		saver.getExtensionFilters().add(new ExtensionFilter("jWin Project", "*.jwp"));
		save.setOnAction(e-> {
			JWinProject project = export.get();
			File saveTo = saver.showSaveDialog(ps);
			if(saveTo != null) {
				FileDealer.write(project.serialize(), saveTo);
				projectFile = project;
			}
			
		});
		
		load.setOnAction(e-> {
			File loadFrom = saver.showOpenDialog(ps);
			if(loadFrom != null) {
				JWinProject project = JWinProject.deserialize(FileDealer.read(loadFrom));
				
				project.getClasspath().forEach(classpath::add);
				mainClass.set(project.getMainClass());
				jdk.set(project.getJdk());
				jre.set(project.getJre());
				icon.set(project.getIcon());
				project.getResolvedJars().forEach(dependencies::addResolvedJar);
				project.getManualJars().forEach(dependencies::addManualJar);
				appName.setValue(project.getAppName());
				version.setValue(project.getAppVersion());
				publisher.setValue(project.getAppPublisher());
				console.setSelected(project.isConsole());
				guid.setText(project.getGuid());
				
				projectFile = project;
			}
		});
	}

	private String key(String val) {
		return "$".concat(val).concat("$");
	}

	private void copyDirCont(File src, File dest, Runnable onItemCopied) {
		for (File file : src.listFiles()) {
			Path relative = src.toPath().relativize(file.toPath());
			File target = Paths.get(dest.getAbsolutePath(), relative.toString()).toFile();

			if (file.isDirectory()) {
				target.mkdir();
				copyDirCont(file, target, onItemCopied);
			} else {
				try {
					Files.copy(file.toPath(), target.toPath());
					onItemCopied.run();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private int countDir(File dir) {
		int count = 0;

		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				count += countDir(f);
			} else {
				count += 1;
			}
		}

		return count;
	}

	private void warn(String head, String content) {
		alert(head, content, AlertType.WARNING);
	}

	private void error(String head, String content) {
		alert(head, content, AlertType.ERROR);
	}

	private void alert(String head, String content, AlertType type) {
		alert(head, content, type, null);
	}
	
	private void alert(String head, String content, AlertType type, Consumer<ButtonType> onRes) {
		Alert al = new Alert(type);
		al.setHeaderText(head);
		al.setContentText(content);
		Optional<ButtonType> res = al.showAndWait();
		if(onRes != null && res.isPresent()) {
			onRes.accept(res.get());
		}
	}

	private Pane vSpace() {
		Pane space = new Pane();
		VBox.setVgrow(space, Priority.ALWAYS);
		return space;
	}

	static class TextVal extends VBox {
		private TextField field;

		public TextVal(String name) {
			super(5);
			field = new TextField();

			getChildren().addAll(new Label(name), field);
		}

		public String getValue() {
			return field.getText();
		}

		public void setValue(String value) {
			field.setText(value);
		}

	}

}
