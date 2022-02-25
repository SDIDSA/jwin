package org.luke.jwin.app;

import java.io.File;
import java.io.IOException;
import java.lang.module.ModuleDescriptor.Version;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.Random;
import java.util.UUID;
import org.luke.jwin.app.file.FileDealer;
import org.luke.jwin.app.file.FileTypeAssociation;
import org.luke.jwin.app.file.JWinProject;
import org.luke.jwin.app.file.UrlProtocolAssociation;
import org.luke.jwin.app.more.MoreSettings;
import org.luke.jwin.app.param.ClasspathParam;
import org.luke.jwin.app.param.DependenciesParam;
import org.luke.jwin.app.param.IconParam;
import org.luke.jwin.app.param.JdkParam;
import org.luke.jwin.app.param.JreParam;
import org.luke.jwin.app.param.MainClassParam;
import org.luke.jwin.app.param.Param;
import org.luke.jwin.ui.Button;
import org.luke.jwin.ui.CheckBox;
import org.luke.jwin.ui.ProgressBar;
import org.luke.jwin.ui.TextField;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class Jwin extends Application {

	Random random = new Random();

	private JWinProject projectFile;
	private File fileInUse;

	private File preBuild;

	private Runnable onRun;

	@Override
	public void start(Stage ps) throws Exception {
		StackPane loader = new StackPane();
		ProgressIndicator loading = new ProgressIndicator();
		loading.setMouseTransparent(true);
		loading.setVisible(false);

		HBox preRoot = new HBox(15);
		preRoot.setPadding(new Insets(15));

		VBox root1 = new VBox(15);

		VBox root2 = new VBox(15);

		ClasspathParam classpath = new ClasspathParam(ps);

		MainClassParam mainClass = new MainClassParam(ps, classpath::listClasses);

		JdkParam jdk = new JdkParam(ps);

		DependenciesParam dependencies = new DependenciesParam(ps, classpath::getPom, jdk);

		JreParam jre = new JreParam(ps, classpath, jdk, dependencies, mainClass);

		IconParam icon = new IconParam(ps);

		Button run = new Button("Run");
		run.setMinWidth(60);

		Button compile = new Button("Build");
		compile.setMinWidth(60);
		compile.setDisable(true);

		Button advanced = new Button("more\nsettings");
		advanced.setMinWidth(80);
		advanced.setTextAlignment(TextAlignment.CENTER);

		Button save = new Button("Save");
		save.setMinWidth(60);

		Button load = new Button("Load");
		load.setMinWidth(60);

		VBox saveLoad = new VBox(5, save, load);
		VBox buildRun = new VBox(5, run, compile);

		advanced.minHeightProperty().bind(saveLoad.heightProperty());

		DirectoryChooser fc = new DirectoryChooser();

		HBox bottom = new HBox(10);
		bottom.setAlignment(Pos.BOTTOM_CENTER);

		ProgressBar progress = new ProgressBar(ps);
		Label state = new Label("doing nothing");

		StackPane progressCont = new StackPane(progress, state);
		progressCont.setAlignment(Pos.CENTER);
		progress.prefWidthProperty().bind(progressCont.widthProperty());

		HBox.setHgrow(progressCont, Priority.ALWAYS);

		progress.minHeightProperty().bind(saveLoad.heightProperty());
		progress.setStyle("-fx-accent: lightblue");

		TextVal appName = new TextVal("App name");
		TextVal version = new TextVal("Version");
		TextVal publisher = new TextVal("Publisher");

		CheckBox console = new CheckBox("Console");

		TextField guid = new TextField();
		guid.setEditable(false);
		HBox.setHgrow(guid, Priority.ALWAYS);

		Button generate = new Button("Generate");
		generate.setOnAction(e -> guid.setText(UUID.randomUUID().toString()));

		MoreSettings moreSettings = new MoreSettings(ps);

		advanced.setOnAction(e -> moreSettings.show());

		Supplier<JWinProject> export = () -> new JWinProject(classpath.getFiles(), mainClass.getValue(), jdk.getValue(),
				jre.getValue(), icon.getValue(), dependencies.getManualJars(), appName.getValue(), version.getValue(),
				publisher.getValue(), console.isSelected(), guid.getText(), moreSettings.getFileTypeAssociation(),
				moreSettings.getUrlProtocolAssociation());

		HBox preConsole = new HBox(10);
		preConsole.setAlignment(Pos.CENTER);

		preConsole.getChildren().addAll(console, new Separator(Orientation.VERTICAL), new Label("GUID"), guid,
				generate);

		HBox preBottom = new HBox(15, appName, version, publisher);

		bottom.getChildren().addAll(progressCont, buildRun, saveLoad, advanced);

		root1.getChildren().addAll(classpath, new Separator(), mainClass, vSpace(), jdk, jre);

		root2.getChildren().addAll(icon, new Separator(), dependencies, preBottom, preConsole, bottom);

		preRoot.getChildren().addAll(root1, root2);

		HBox.setHgrow(root2, Priority.ALWAYS);
		HBox.setHgrow(root1, Priority.ALWAYS);

		loader.getChildren().addAll(preRoot, loading);
		Scene scene = new Scene(loader);

		ps.setScene(scene);
		ps.setHeight(550);
		ps.setMinHeight(550);
		ps.setWidth((double) 424 * 2 + 15 * 4);
		ps.setMinWidth((double) 424 * 2 + 15 * 4);
		ps.setTitle("jWin");
		ps.setOnShown(e -> ps.centerOnScreen());

		for (int i = 16; i <= 128; i *= 2) {
			ps.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png"), i, i, true, true));
		}

		ps.show();

		Consumer<Boolean> setDisable = (b) -> {
			Param.disable(b);
			preBottom.setDisable(b);
			preConsole.setDisable(b);
			saveLoad.setDisable(b);
			buildRun.setDisable(b);
			advanced.setDisable(b);
		};

		onRun = () -> {
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

			if (dependencies.isResolving()) {
				Jwin.warn("Resolving dependencies", "try again after dependencies are successfully resolved");
				return;
			}

			setDisable.accept(true);
			compile.setDisable(true);
			new Thread(() -> {
				preBuild = new File(System.getProperty("java.io.tmpdir") + "/jwin_pre_build_" + random.nextInt(999999));
				preBuild.mkdir();

				deleteDirOnShutdown(preBuild);

				Platform.runLater(() -> state.setText("Copying dependencies"));
				File preBuildLibs = dependencies.copy(preBuild, progress);

				Platform.runLater(() -> state.setText("Copying runtime"));
				jre.copy(preBuild, progress);

				Platform.runLater(() -> state.setText("Compiling source code"));
				classpath.compile(preBuild, preBuildLibs, jdk.getValue(), mainClass.getValue().getValue(), progress);

				Platform.runLater(() -> state.setText("Copying resources"));
				classpath.copyRes(preBuild, progress);

				Platform.runLater(() -> {
					progress.setProgress(-1);
					state.setText("Running...");
				});

				Command command = new Command("cmd", "/c",
						"\"rt/bin/java\" -cp \"bin;res;lib/*\" " + mainClass.getValue().getKey());
				try {
					Process p = command.execute(preBuild);
					Platform.runLater(() -> {
						buildRun.setDisable(false);
						run.setText("Stop");
						run.setDisable(false);
						run.setOnAction(e -> {
							p.descendants().forEach(ProcessHandle::destroyForcibly);
							run.setText("Run");
							run.setOnAction(ev -> onRun.run());
						});
					});
					p.waitFor();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					Thread.currentThread().interrupt();
				}

				Platform.runLater(() -> {
					run.setText("Run");
					progress.setProgress(-1);
					state.setText("doing nothing");
					setDisable.accept(false);
					compile.setDisable(false);
				});
			}).start();
		};

		run.setOnAction(e -> onRun.run());

		compile.setOnAction(e -> {
			boolean[] contin = new boolean[] { true };

			ButtonType useDefault = new ButtonType("use default");
			ButtonType select = new ButtonType("select now");
			if (icon.getValue() == null || !icon.getValue().exists()) {
				contin[0] = false;
				alert("Missing icon", "you didn't select an icon for your app", AlertType.WARNING, res -> {
					if (res.equals(select)) {
						icon.select(ps);
						if (icon.getValue() != null) {
							contin[0] = true;
						}
					} else if (res.equals(useDefault)) {
						icon.set(new File(URLDecoder.decode(getClass().getResource("/def.ico").getFile(),
								Charset.defaultCharset())));
						if (icon.getValue() != null) {
							contin[0] = true;
						}
					}

				}, useDefault, select, ButtonType.CANCEL);
			}

			if (!contin[0]) {
				return;
			}

			if (appName.getValue().isBlank()) {
				error("Missing app name", "The application name field is required");
				return;
			}

			if (version.getValue().isBlank()) {
				error("Missing app version", "The application version field is required");
				return;
			}

			if (guid.getText().isBlank()) {
				error("Missing app GUID",
						"click generate to generate a new GUID, it is recommended to save the project for future builds so you can use the same GUID");
				return;
			}

			// Check for warnings
			if (jre.isJdk()) {
				warn("Using JDK as a runtime", "not recommended unless required by your app (increases package size)");
			}

			if (projectFile == null) {
				alert("Do you want to save the project before building ?",
						"It is recommended to save this project so you can use the same GUID when you build it again, do not use the same GUID with different projects",
						AlertType.CONFIRMATION, res -> {
							if (res.equals(ButtonType.OK)) {
								save.fire();
							}
						});
			} else {
				JWinProject exported = export.get();
				List<String> diffs = exported.compare(projectFile);

				if (!diffs.isEmpty()) {
					StringBuilder diffStr = new StringBuilder();
					diffs.forEach(diff -> diffStr.append("\t").append(diff).append("\n"));

					alert("Do you want to save the changes you made ?",
							"You made changes to the following properties : \n\t" + diffStr.toString().trim(),
							AlertType.CONFIRMATION, res -> {
								if (res.equals(ButtonType.OK)) {
									FileDealer.write(exported.serialize(), fileInUse);
									projectFile = exported;
								}
							});
				}
			}

			File preSaveTo = fileInUse == null ? null : fileInUse.getParentFile();
			if (preSaveTo == null) {
				contin[0] = false;
				alert("Select output directory", "select the directory where you want to save the generated installer",
						AlertType.CONFIRMATION, res -> {
							if (res.equals(ButtonType.OK)) {
								contin[0] = true;
							}
						});
				if (!contin[0]) {
					return;
				}
				preSaveTo = fc.showDialog(ps);
			}

			final File saveTo = preSaveTo;

			if (saveTo != null) {
				setDisable.accept(true);
				new Thread(() -> {

					Platform.runLater(() -> state.setText("Generating launcher"));

					File preBuildBat = new File(
							preBuild.getAbsolutePath().concat("/").concat(appName.getValue()).concat(".bat"));

					FileDealer.write("set batdir=%~dp0 \n" + "pushd \"%batdir%\" \n"
							+ "\"rt/bin/java\" -cp \"res;bin;lib/*\" " + mainClass.getValue().getKey() + " %*",
							preBuildBat);

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

					try {
						Files.delete(preBuildBat.toPath());
					} catch (IOException e2) {
						e2.printStackTrace();
					}

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

					FileTypeAssociation fta = moreSettings.getFileTypeAssociation();
					UrlProtocolAssociation upa = moreSettings.getUrlProtocolAssociation();

					if (fta == null && upa == null) {
						template = template.replace(key("add_to_file"), "");
					} else {
						StringBuilder reg = new StringBuilder("[Registry]\n");
						if (fta != null) {
							if (fta.getIcon() != null) {
								try {
									Files.copy(fta.getIcon().toPath(), new File(
											preBuild.getAbsolutePath().concat("/").concat(fta.getIcon().getName()))
													.toPath());
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}

							String typeDef = FileDealer.read("/type_def.txt")
									.replace(key("type_name"), fta.getTypeName())
									.replace(key("type_extension"), fta.getTypeExtension());

							template = template.replace(key("add_define"), typeDef).replace(key("add_to_setup"),
									"ChangesAssociations=yes");

							String typeReg = FileDealer.read("/type_reg.txt")
									.replace(key("type_extension"), fta.getTypeExtension())
									.replace(key("type_icon"), fta.getIcon() == null ? appName.getValue().concat(".exe")
											: fta.getIcon().getName());

							reg.append(typeReg).append("\n");
						}

						if (upa != null) {
							String urlReg = FileDealer.read("/url_reg.txt").replace(key("protocol"), upa.getProtocol());

							reg.append("\n").append(urlReg);
						}

						template = template.replace(key("add_to_file"), reg);
					}

					if (fta == null) {
						template = template.replace(key("add_define"), "").replace(key("add_to_setup"), "");
					}

					System.out.println(template);

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
							Platform.runLater(() -> progress.setProgress((compressCount[0] / (double) fileCount) * .9));
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

					Platform.runLater(() -> {
						state.setText("done");
						progress.setProgress(-1);
						setDisable.accept(false);
					});
				}).start();
			}
		});

		FileChooser saver = new FileChooser();
		saver.getExtensionFilters().add(new ExtensionFilter("jWin Project", "*.jwp"));
		save.setOnAction(e -> {
			JWinProject project = export.get();
			File saveTo = saver.showSaveDialog(ps);
			if (saveTo != null) {
				FileDealer.write(project.serialize(), saveTo);
				projectFile = project;
				fileInUse = saveTo;
			}

		});

		Consumer<File> importer = loadFrom -> {
			preRoot.setDisable(true);
			loading.setVisible(true);
			new Thread(() -> {
				JWinProject project = JWinProject.deserialize(FileDealer.read(loadFrom));

				runOnUiThread(Param::clearAll);
				project.getClasspath().forEach(f -> runOnUiThread(() -> classpath.add(f)));
				runOnUiThread(() -> mainClass.set(project.getMainClass()));
				runOnUiThread(() -> jdk.set(project.getJdk()));
				runOnUiThread(() -> jre.set(project.getJre()));
				runOnUiThread(() -> icon.set(project.getIcon()));
				runOnUiThread(() -> appName.setValue(project.getAppName()));
				runOnUiThread(() -> version.setValue(project.getAppVersion()));
				runOnUiThread(() -> publisher.setValue(project.getAppPublisher()));
				runOnUiThread(() -> console.setSelected(project.isConsole()));
				runOnUiThread(() -> guid.setText(project.getGuid()));
				project.getManualJars().forEach(f -> runOnUiThread(() -> dependencies.addManualJar(f)));

				runOnUiThread(() -> moreSettings.setFileTypeAssociation(project.getFileTypeAsso()));
				runOnUiThread(() -> moreSettings.setUrlProtocolAssociation(project.getUrlProtocolAsso()));

				projectFile = project;
				fileInUse = loadFrom;

				runOnUiThread(() -> dependencies.resolve(classpath::getPom, jdk, false));

				Platform.runLater(() -> {
					preRoot.setDisable(false);
					loading.setVisible(false);
					compile.setDisable(true);
				});
			}).start();
		};

		load.setOnAction(e -> {
			File loadFrom = saver.showOpenDialog(ps);
			if (loadFrom != null) {
				importer.accept(loadFrom);
			}
		});

		for (String param : getParameters().getRaw()) {
			String ext = param.substring(param.lastIndexOf(".") + 1);
			if (ext.equalsIgnoreCase("jwp")) {
				importer.accept(new File(param));
				return;
			}
		}
	}

	private static void runOnUiThread(Runnable r) {
		Platform.runLater(r);
		sleep(50);
	}

	private static void sleep(long dur) {
		try {
			Thread.sleep(dur);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	private String key(String val) {
		return "$".concat(val).concat("$");
	}

	public static void copyDirCont(File src, File dest, Runnable onItemCopied) {
		for (File file : src.listFiles()) {
			Path relative = src.toPath().relativize(file.toPath());
			File target = Paths.get(dest.getAbsolutePath(), relative.toString()).toFile();

			if (file.isDirectory()) {
				target.mkdir();
				copyDirCont(file, target, onItemCopied);
			} else {
				try {
					Files.copy(file.toPath(), target.toPath());
					if (onItemCopied != null) {
						onItemCopied.run();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static int countDir(File dir) {
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

	public static void deleteDirOnShutdown(File dir) {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> deleteDir(dir)));
	}

	public static void deleteDir(File dir) {
		try (Stream<Path> stream = Files.walk(dir.toPath())) {
			stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static void warn(String head, String content) {
		alert(head, content, AlertType.WARNING);
	}

	public static void error(String head, String content) {
		alert(head, content, AlertType.ERROR);
	}

	public static void alert(String head, String content, AlertType type) {
		alert(head, content, type, null);
	}

	public static void alert(String head, String content, AlertType type, Consumer<ButtonType> onRes,
			ButtonType... types) {
		Runnable exe = () -> {
			Alert al = new Alert(type);
			al.setHeaderText(head);
			al.setContentText(content);

			if (types.length != 0) {
				al.getButtonTypes().setAll(types);
			}

			Optional<ButtonType> res = al.showAndWait();
			if (onRes != null && res.isPresent()) {
				onRes.accept(res.get());
			}
		};

		if (Platform.isFxApplicationThread()) {
			exe.run();
		} else {
			Platform.runLater(exe);
		}
	}

	public static Pane vSpace() {
		Pane space = new Pane();
		VBox.setVgrow(space, Priority.ALWAYS);
		return space;
	}

	public static class TextVal extends VBox {
		private TextField field;

		private HBox bottom;

		public TextVal(String name) {
			super(5);
			field = new TextField();

			field.setMinWidth(0);

			bottom = new HBox(10, field);

			HBox.setHgrow(this, Priority.ALWAYS);
			HBox.setHgrow(field, Priority.ALWAYS);
			getChildren().addAll(new Label(name), bottom);
		}

		public void addToBottom(Node node) {
			bottom.getChildren().add(node);
		}

		public String getValue() {
			return field.getText();
		}

		public void setValue(String value) {
			field.setText(value);
		}
	}
}
