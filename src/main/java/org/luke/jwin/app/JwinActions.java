package org.luke.jwin.app;

import java.awt.Desktop;
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
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.luke.gui.controls.alert.Alert;
import org.luke.gui.controls.alert.AlertType;
import org.luke.gui.controls.alert.ButtonType;
import org.luke.gui.file.FileUtils;
import org.luke.gui.window.Window;
import org.luke.jwin.app.console.Console;
import org.luke.jwin.app.display.JwinUi;
import org.luke.jwin.app.file.FileDealer;
import org.luke.jwin.app.file.FileTypeAssociation;
import org.luke.jwin.app.file.JWinProject;
import org.luke.jwin.app.file.UrlProtocolAssociation;

import javafx.application.Platform;
import javafx.stage.DirectoryChooser;

public class JwinActions {
	private static Window window;

	private Random random = new Random();

	private File preBuild;

	private Window ps;
	static private JwinUi config;

	private DirectoryChooser fc;

	public JwinActions(Window ps, JwinUi config) {
		this.ps = ps;
		window = ps;
		JwinActions.config = config;

		fc = new DirectoryChooser();
	}

	public void run() {
		List<File> cp = config.getClasspath().getFiles();

		if (cp.isEmpty()) {
			error("Missing classpath", "You didn't add any classpath folders");
			return;
		}

		if (config.getMainClass().getValue() == null) {
			config.logStd("main class was not set, attempting to set automatically...");
			Map<String, File> mcs = config.getMainClass().listMainClasses();

			if (mcs.size() == 1) {
				Map.Entry<String, File> mc = mcs.entrySet().iterator().next();
				config.logStd("setting the main class to " + mc.getValue());
				config.getMainClass().set(mc);
			} else {
				error("Main class required", "You didn't specify the main class for your application");
				return;
			}
		}

		if (!config.getClasspath().isValidMainClass(config.getMainClass().getValue().getValue())) {
			error("invalid mainClass",
					"The main class you selected doesn't belong in any of your classpath entries, are you sure you didn't remove it?");
			return;
		}

		File dk = config.getJdk().getValue();

		if (dk == null) {
			config.logStd("jdk was not set, attempting to detect from system...");
			File f = config.getJdk().detectJdk();
			if (f != null) {
				final Long key = new Random().nextLong();
				config.getJdk().set(f, " (found in your system)", () -> {
					config.logStd("jdk " + config.getJdk().getVersion() + " was detected, using that...");
					Platform.exitNestedEventLoop(key, null);
				});
				Platform.enterNestedEventLoop(key);
			} else {
				error("Missing Jdk", "You didn't select the jdk to compile your application");
				return;
			}
		}

		if (!config.getJdk().isJdk()) {
			error("Invalid Jdk",
					"The jdk directory you selected is unsupported and can not be used to compile your app");
			return;
		}

		File rt = config.getJre().getValue();

		if (rt == null) {
			config.logStd("jre was not set, attempting to generate from jdk using jlink...");
			final Long key = new Random().nextLong();
			config.getDependencies().resolve(config.getClasspath()::getPom, config, false, () -> {
				Platform.exitNestedEventLoop(key, null);
			});
			Platform.enterNestedEventLoop(key);

			final Long key2 = new Random().nextLong();
			config.getJre().generateFromJdk(ps, config, false, () -> {
				Platform.exitNestedEventLoop(key2, null);
			});
			Platform.enterNestedEventLoop(key2);

			rt = config.getJre().getValue();
			if (rt == null) {
				error("Missing Jre", "You didn't select the jre to run your application");
				return;
			}
		}

		String jreVersionString = config.getJre().getVersion().split("_")[0];
		String jdkVersionString = config.getJdk().getVersion().split("_")[0];

		Version jreVersion = Version.parse(jreVersionString);
		Version jdkVersion = Version.parse(jdkVersionString);

		int versionCompare = jreVersion.compareTo(jdkVersion);
		if (versionCompare < 0) {
			error("Uncompatible Java versions",
					"Your JRE " + jreVersion + " will not be able to run code compiled by Your JDK " + jdkVersion);
			return;
		}

		if (config.getDependencies().isResolving()) {
			warn("Resolving dependencies", "try again after dependencies are successfully resolved");
			return;
		}

		if (config.getProjectInUse().isConsole() == null && config.getClasspath().isConsoleApp()) {
			config.logStd("it was estimated that your project is a console app, setting that...");
			config.getConsole().checkedProperty().set(true);
		}

		config.disable(true, false);
		new Thread(() -> {
			config.logStd("creating temp folder");
			preBuild = new File(System.getProperty("java.io.tmpdir") + "/jwin_pre_build_" + random.nextInt(999999));
			preBuild.mkdir();

			deleteDirOnShutdown(preBuild);

			config.logStd("Copying dependencies to the temp folder");
			config.setState("Copying dependencies");
			File preBuildLibs = null;
			try {
				preBuildLibs = config.getDependencies().copy(preBuild, config::setProgress);
			} catch (IOException e2) {
				copyDependenciesFailure();
				config.onErr();
				return;
			}

			config.logStd("Copying java runtime...");
			config.setState("Copying runtime");
			config.getJre().copy(preBuild, config::setProgress);

			config.logStd("Compiling source code...");
			config.setState("Compiling source code");
			try {
				config.getClasspath().compile(preBuild, preBuildLibs, config.getJdk().getValue(),
						config.getMainClass().getValue(), config::incrementProgress, config.getMainClass()::setAltMain);
			} catch (IllegalStateException x) {
				compileFailure();
				config.onErr();
				return;
			}

			config.logStd("Copying resources...");
			config.setState("Copying resources");
			config.getClasspath().copyRes(preBuild, config::setProgress);

			config.setProgress(-1);
			config.setState("Running...");
			config.logStd("Running your project");

			StringBuilder errBuilder = new StringBuilder();

			Runnable showLog = () -> {
				try {
					File f = File.createTempFile("jwin_log_", ".txt");
					FileUtils.write(f, errBuilder.toString());
					Desktop.getDesktop().open(f);
				} catch (IOException e) {
					e.printStackTrace();
				}
			};

			String c = "\"rt/bin/java\" -cp \"bin;res;lib/*\" "
					+ (config.getMainClass().getAltMain() == null ? config.getMainClass().getValue().getKey()
							: config.getMainClass().getAltMain());
			Command command = new Command(std -> {
			}, err -> errBuilder.append(err).append('\n'), "cmd", "/c", c);

			Process p = command.execute(preBuild);

			if (config.getConsole().checkedProperty().get()) {
				Platform.runLater(() -> {
					Console con = new Console(window.getLoadedPage(), command);
					con.show();

					command.addOnExit(() -> Platform.runLater(con::hide));
					con.addOnHidden(() -> {
						config.setStopped(true);
						if (p.isAlive()) {
							p.descendants().forEach(ProcessHandle::destroy);
						}
					});
				});
			}

			config.logStd("your project is running");

			config.run(p, showLog, errBuilder);
		}).start();
	}

	public void compile() {
		boolean[] contin = new boolean[] { true };

		if (preBuild == null) {
			error("Temp files can't be found",
					"Pre Build files were deleted or not correctly generated, try running again");
			return;
		}

		ButtonType useDefault = ButtonType.USE_DEFAULT;
		ButtonType select = ButtonType.SELECT_NOW;
		if (config.getIcon().getValue() == null || !config.getIcon().getValue().exists()) {
			contin[0] = false;
			alert("Missing icon", "you didn't select an icon for your app", AlertType.INFO, res -> {
				if (res.equals(select)) {
					config.getIcon().select(ps);
					if (config.getIcon().getValue() != null) {
						contin[0] = true;
					}
				} else if (res.equals(useDefault)) {
					config.getIcon().set(new File(
							URLDecoder.decode(getClass().getResource("/def.ico").getFile(), Charset.defaultCharset())));
					if (config.getIcon().getValue() != null) {
						contin[0] = true;
					}
				}

			}, useDefault, select, ButtonType.CANCEL);
		}

		if (!contin[0]) {
			return;
		}

		if (config.getAppName().getValue().isBlank()) {
			error("Missing app name", "The application name field is required");
			return;
		}

		if (config.getVersion().getValue().isBlank()) {
			error("Missing app version", "The application version field is required");
			return;
		}

		if (config.getGuid().getValue() == null || config.getGuid().getValue().isBlank()) {
			String guid = UUID.randomUUID().toString();
			config.logStd("a guid was generated for your project : " + guid);
			config.getGuid().setValue(guid);
//			error("Missing app GUID",
//					"click generate to generate a new GUID, it is recommended to save the project for future builds so you can use the same GUID");
//			return;
		}

		if (!preBuild.exists()) {
			error("Temp files can't be found",
					"Pre Build files were deleted or not correctly generated, try running again");
			return;
		}

		// Check for warnings
		if (config.getJre().isJdk()) {
			warn("Using JDK as a runtime", "not recommended unless required by your app (increases package size)");
		}

		if (config.getFileInUse() == null) {
			alert("Do you want to save the project before building ?",
					"It is recommended to save this project so you can use the same GUID when you build it again, do not use the same GUID with different projects",
					AlertType.CONFIRM, res -> {
						if (res.equals(ButtonType.YES)) {
							if (config.saveAs())
								config.logStd("project saved.");
						} else if (res.equals(ButtonType.CANCEL)) {
							contin[0] = false;
						}
					});
		} else {
			JWinProject exported = config.export();
			List<String> diffs = exported.compare(config.getProjectInUse());

			if (!diffs.isEmpty()) {
				StringBuilder diffStr = new StringBuilder();
				diffs.forEach(diff -> diffStr.append("\t").append(diff).append("\n"));

				alert("Do you want to save the changes you made ?",
						"You made changes to the following properties : \n\t" + diffStr.toString().trim(),
						AlertType.CONFIRM, res -> {
							if (res.equals(ButtonType.YES)) {
								FileDealer.write(exported.serialize(), config.getFileInUse());
								config.setProjectInUse(exported);
								config.logStd("changes saved.");
							} else if (res.equals(ButtonType.CANCEL)) {
								contin[0] = false;
							}
						});
			}
		}

		if (!contin[0]) {
			return;
		}

		File preSaveTo = config.getFileInUse() == null ? null : config.getFileInUse().getParentFile();
		if (preSaveTo == null) {
			contin[0] = false;
			alert("Select output directory", "select the directory where you want to save the generated installer",
					AlertType.INFO, res -> {
						if (res.equals(ButtonType.OK)) {
							contin[0] = true;
						}
					}, ButtonType.OK);
			if (!contin[0]) {
				return;
			}
			preSaveTo = fc.showDialog(ps);
		}

		final File saveTo = preSaveTo;

		if (saveTo != null) {
			config.disable(true, true);
			new Thread(() -> {
				config.setState("Generating launcher");

				File preBuildBat = new File(
						preBuild.getAbsolutePath().concat("/").concat(config.getAppName().getValue()).concat(".bat"));

				FileDealer.write(
						"set batdir=%~dp0 \n" + "pushd \"%batdir%\" \ncls\n" + "\"rt/bin/java\" -cp \"res;bin;lib/*\" "
								+ (config.getMainClass().getAltMain() == null
										? config.getMainClass().getValue().getKey()
										: config.getMainClass().getAltMain())
								+ " %*",
						preBuildBat);

				File b2e = new File(
						URLDecoder.decode(getClass().getResource("/b2e.exe").getFile(), Charset.defaultCharset()));

				String convertCommand = "b2e /bat \"" + preBuildBat.getAbsolutePath() + "\" /exe \""
						+ preBuildBat.getAbsolutePath().replace(".bat", ".exe") + "\"";

				if (!config.getConsole().checkedProperty().get()) {
					convertCommand += " /invisible";
				}
				if (config.getAdmin().checkedProperty().get()) {
					convertCommand += " /uac-admin";
				}
				if (config.getIcon().getValue() != null) {
					convertCommand += " /icon \"" + config.getIcon().getValue().getAbsolutePath() + "\"";
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

				config.setState("Generating installer");
				String template = FileDealer.read("/ist.txt").replace(key("app_name"), config.getAppName().getValue())
						.replace(key("app_version"), config.getVersion().getValue())
						.replace(key("app_publisher"), config.getPublisher().getValue())
						.replace(key("output_folder"), saveTo.getAbsolutePath())
						.replace(key("installer_name"),
								config.getAppName().getValue().concat("_")
										.concat(config.getVersion().getValue().replace(".", "-")).concat("_installer"))
						.replace(key("app_icon"), config.getIcon().getValue().getAbsolutePath())
						.replace(key("prebuild_path"), preBuild.getAbsolutePath())
						.replace(key("GUID"), config.getGuid().getValue());

				FileTypeAssociation fta = config.getMoreSettings().getFileTypeAssociation();
				UrlProtocolAssociation upa = config.getMoreSettings().getUrlProtocolAssociation();

				if (fta == null && upa == null) {
					template = template.replace(key("add_to_file"), "");
				} else {
					StringBuilder reg = new StringBuilder("[Registry]\n");
					if (fta != null) {
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

						template = template.replace(key("add_define"), typeDef).replace(key("add_to_setup"),
								"ChangesAssociations=yes");

						String typeReg = FileDealer.read("/type_reg.txt")
								.replace(key("type_extension"), fta.getTypeExtension()).replace(key("type_icon"),
										fta.getIcon() == null ? config.getAppName().getValue().concat(".exe")
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

				File buildScript = new File(
						System.getProperty("java.io.tmpdir") + "/jwin_iss_" + random.nextInt(999999) + ".iss");
				FileDealer.write(template, buildScript);

				File ise = new File(
						URLDecoder.decode(getClass().getResource("/is/ISCC.exe").getFile(), Charset.defaultCharset()));

				int fileCount = countDir(preBuild);
				int[] compressCount = new int[] { 0 };
				Consumer<String> buildLine = line -> {
					if (line.trim().indexOf("Compressing") == 0) {
						compressCount[0]++;
						config.setProgress((compressCount[0] / (double) fileCount) * .9);
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
					Desktop.getDesktop().open(saveTo);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				Platform.runLater(() -> {
					config.setState("done");
					config.setProgress(-1);
					config.disable(false, true);
				});
			}).start();
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

	private String key(String val) {
		return "$".concat(val).concat("$");
	}

	public static void compileFailure() {
		error("Failed to compile your code",
				"please check your code and classpath settings for potential errors, also don't forget to resolve dependencies");
	}

	public static void copyDependenciesFailure() {
		error("Failed to copy dependencies", "the referenced jar files might have been deleted, try resolving again");
	}

	public static void warn(String head, String content) {
		alert(head, content, AlertType.INFO);
		config.logErr(head);
		config.logErr("\t" + content);
	}

	public static void error(String head, String content) {
		alert(head, content, AlertType.ERROR);
		config.logErr(head);
		config.logErr("\t" + content);
	}

	public static void alert(String head, String content, AlertType type) {
		alert(head, content, type, null);
	}

	public static void alert(String head, String content, AlertType type, Consumer<ButtonType> onRes, Runnable onHide,
			ButtonType... types) {
		Runnable exe = () -> {
			Alert al = new Alert(window.getLoadedPage(), type);
			al.setHead(head);
			al.addLabel(content);

			if (types.length != 0) {
				al.setButtonTypes(types);
			}

			if (onRes != null) {
				for (ButtonType t : (types.length == 0 ? type.getButtons().toArray(types) : types)) {
					al.addAction(t, () -> {
						al.hide();
						onRes.accept(t);
					});
				}
			}

			if (onHide != null) {
				al.addOnHidden(onHide);
			}

			al.showAndWait();
		};

		if (Platform.isFxApplicationThread()) {
			exe.run();
		} else {
			Platform.runLater(exe);
		}
	}

	public static void alert(String head, String content, AlertType type, Consumer<ButtonType> onRes,
			ButtonType... types) {
		alert(head, content, type, onRes, null, types);
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
}
