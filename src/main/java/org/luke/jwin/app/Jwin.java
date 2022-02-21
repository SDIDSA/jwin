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
import java.util.Random;
import java.util.UUID;
import org.luke.jwin.app.file.FileDealer;
import org.luke.jwin.app.file.FileTypeAssociation;
import org.luke.jwin.app.file.JWinProject;
import org.luke.jwin.app.more.MoreSettings;
import org.luke.jwin.app.param.ClasspathParam;
import org.luke.jwin.app.param.DependenciesParam;
import org.luke.jwin.app.param.IconParam;
import org.luke.jwin.app.param.JdkParam;
import org.luke.jwin.app.param.JreParam;
import org.luke.jwin.app.param.MainClassParam;
import org.luke.jwin.app.param.Param;

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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
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

		DependenciesParam dependencies = new DependenciesParam(ps, classpath::getPom);

		JdkParam jdk = new JdkParam(ps);
		JreParam jre = new JreParam(ps, classpath, jdk, dependencies, mainClass);

		IconParam icon = new IconParam(ps);

		Button compile = new Button("Build");
		compile.setMinWidth(60);

		Button advanced = new Button("more\nsettings");
		advanced.setMinWidth(60);
		advanced.setTextAlignment(TextAlignment.CENTER);

		Button save = new Button("Save");
		save.setMinWidth(60);

		Button load = new Button("Load");
		load.setMinWidth(60);

		VBox saveLoad = new VBox(5, save, load);

		compile.minHeightProperty().bind(saveLoad.heightProperty());
		advanced.minHeightProperty().bind(saveLoad.heightProperty());

		DirectoryChooser fc = new DirectoryChooser();

		HBox bottom = new HBox(10);
		bottom.setAlignment(Pos.BOTTOM_CENTER);

		ProgressBar progress = new ProgressBar();
		Label state = new Label("doing nothing");

		StackPane progressCont = new StackPane(progress, state);
		progressCont.setAlignment(Pos.CENTER);
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
		generate.setOnAction(e -> guid.setText(UUID.randomUUID().toString()));

		MoreSettings moreSettings = new MoreSettings(ps);

		advanced.setOnAction(e -> moreSettings.show());

		Supplier<JWinProject> export = () -> new JWinProject(classpath.getFiles(), mainClass.getValue(), jdk.getValue(),
				jre.getValue(), icon.getValue(), dependencies.getManualJars(), appName.getValue(), version.getValue(),
				publisher.getValue(), console.isSelected(), guid.getText(), moreSettings.getFileTypeAssociation());

		HBox preConsole = new HBox(10);
		preConsole.setAlignment(Pos.CENTER);

		preConsole.getChildren().addAll(console, new Separator(Orientation.VERTICAL), new Label("GUID"), guid,
				generate);

		HBox preBottom = new HBox(15, appName, version, publisher);

		bottom.getChildren().addAll(progressCont, compile, advanced, saveLoad);

		root1.getChildren().addAll(classpath, new Separator(), mainClass, vSpace(), jdk, jre);

		root2.getChildren().addAll(icon, new Separator(), dependencies, preBottom, preConsole, bottom);

		preRoot.getChildren().addAll(root1, root2);

		HBox.setHgrow(root2, Priority.ALWAYS);
		HBox.setHgrow(root1, Priority.ALWAYS);

		loader.getChildren().addAll(preRoot, loading);
		Scene scene = new Scene(loader);

		ps.setScene(scene);
		ps.setHeight(540);
		ps.setMinHeight(540);
		ps.setWidth(424 * 2 + 15 * 4);
		ps.setMinWidth(424 * 2 + 15 * 4);
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

			if (appName.getValue().isBlank()) {
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
								return;
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
									save.fire();
									return;
								}
							});
				}
			}

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
			File saveTo = fc.showDialog(ps);
			if (saveTo != null) {
				compile.setDisable(true);
				new Thread(() -> {
					File preBuild = new File(
							System.getProperty("java.io.tmpdir") + "/jwin_pre_build_" + random.nextInt(999999));
					preBuild.mkdir();

					Runtime.getRuntime().addShutdownHook(new Thread(() -> {
						try {
							Files.walk(preBuild.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile)
									.forEach(File::delete);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}));

					Platform.runLater(() -> state.setText("Copying dependencies"));
					File preBuildLibs = dependencies.copy(preBuild, progress);

					Platform.runLater(() -> state.setText("Copying runtime"));
					jre.copy(preBuild, progress);

					Platform.runLater(() -> state.setText("Compiling source code"));
					classpath.compile(preBuild, preBuildLibs, dk, launcher.getValue(), progress);

					Platform.runLater(() -> state.setText("Copying resources"));
					classpath.copyRes(preBuild, progress);

					Platform.runLater(() -> state.setText("Generating launcher"));

					File preBuildBat = new File(
							preBuild.getAbsolutePath().concat("/").concat(appName.getValue()).concat(".bat"));

					FileDealer.write("set batdir=%~dp0 \n" + "pushd \"%batdir%\" \n"
							+ "\"rt/bin/java\" -cp \"res;bin;lib/*\" " + launcher.getKey() + " %*", preBuildBat);

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

					FileTypeAssociation fta = moreSettings.getFileTypeAssociation();

					if (fta == null) {
						template = template.replace(key("add_define"), "").replace(key("add_to_setup"), "")
								.replace(key("add_to_file"), "");
					} else {
						if (fta.getIcon() != null) {
							try {
								Files.copy(fta.getIcon().toPath(),
										new File(preBuild.getAbsolutePath().concat("/").concat(fta.getIcon().getName()))
												.toPath());
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}

						String typeDef = FileDealer.read("/type_def.txt").replace(key("type_name"), fta.getTypeName())
								.replace(key("type_extension"), fta.getTypeExtension());

						String typeReg = FileDealer.read("/type_reg.txt")
								.replace(key("type_extension"), fta.getTypeExtension())
								.replace(key("type_icon"), fta.getIcon() == null ? appName.getValue().concat(".exe")
										: fta.getIcon().getName());

						template = template.replace(key("add_define"), typeDef)
								.replace(key("add_to_setup"), "ChangesAssociations=yes")
								.replace(key("add_to_file"), typeReg);
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
		save.setOnAction(e -> {
			JWinProject project = export.get();
			File saveTo = saver.showSaveDialog(ps);
			if (saveTo != null) {
				FileDealer.write(project.serialize(), saveTo);
				projectFile = project;
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

				runOnUiThread(() -> dependencies.resolve(classpath::getPom));
				project.getManualJars().forEach(f -> runOnUiThread(() -> dependencies.addManualJar(f)));

				runOnUiThread(() -> moreSettings.setFileTypeAssociation(project.getFileTypeAsso()));

				projectFile = project;

				Platform.runLater(() -> {
					preRoot.setDisable(false);
					loading.setVisible(false);
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
					onItemCopied.run();
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

	public static void warn(String head, String content) {
		alert(head, content, AlertType.WARNING);
	}

	public static void error(String head, String content) {
		alert(head, content, AlertType.ERROR);
	}

	public static void alert(String head, String content, AlertType type) {
		alert(head, content, type, null);
	}

	public static void alert(String head, String content, AlertType type, Consumer<ButtonType> onRes, ButtonType... types) {
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
	}

	private Pane vSpace() {
		Pane space = new Pane();
		VBox.setVgrow(space, Priority.ALWAYS);
		return space;
	}

	public static class TextVal extends VBox {
		private TextField field;

		public TextVal(String name) {
			super(5);
			field = new TextField();

			field.setMinWidth(0);

			HBox.setHgrow(this, Priority.ALWAYS);
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
