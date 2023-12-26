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
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.luke.gui.controls.alert.Alert;
import org.luke.gui.controls.alert.AlertType;
import org.luke.gui.controls.alert.ButtonType;
import org.luke.gui.file.FileUtils;
import org.luke.gui.locale.Locale;
import org.luke.gui.window.Window;
import org.luke.jwin.app.console.Console;
import org.luke.jwin.app.file.FileDealer;
import org.luke.jwin.app.file.FileTypeAssociation;
import org.luke.jwin.app.file.JWinProject;
import org.luke.jwin.app.file.UrlProtocolAssociation;
import org.luke.jwin.app.layout.JwinUi;
import org.luke.jwin.app.param.JdkParam;
import org.luke.jwin.local.LocalStore;

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

	private void preRun() {
		List<File> cp = config.getClasspath().getFiles();

		if (cp.isEmpty()) {
			error("missing_cp_head", "missing_cp_body");
			return;
		}

		if (config.getMainClass().getValue() == null) {
			config.separate();
			config.logStd("mc_auto_attempt");
			Map<String, File> mcs = config.getMainClass().listMainClasses();

			if (mcs.size() == 1) {
				Map.Entry<String, File> mc = mcs.entrySet().iterator().next();
				config.logStd(Locale.key("mc_auto_set", "class", mc.getKey()));
				config.getMainClass().set(mc);
			} else {
				error("mc_required_head", "mc_required_body");
				return;
			}
		}

		if (!config.getClasspath().isValidMainClass(config.getMainClass().getValue().getValue())) {
			error("mc_invalid_head", "mc_invalid_body");
			return;
		}

		File dk = config.getJdk().getValue();

		if (dk == null) {
			config.separate();
			config.logStd("jdk was not set");

			File defJdk = new File(LocalStore.getDefaultJdk());
			if (defJdk.exists()) {
				Semaphore s = new Semaphore(0);
				config.getJdk().set(defJdk, "", () -> {
					config.logStd("using default jdk : " + config.getJdk().getVersion());
					s.release();
				});
				s.acquireUninterruptibly();
			} else {
				List<File> fs = JdkParam.detectJdkCache();
				if (!fs.isEmpty()) {
					File f = fs.get(0);
					Semaphore s = new Semaphore(0);
					config.getJdk().set(f, " (found in your system)", () -> {
						config.logStd("jdk " + config.getJdk().getVersion() + " was detected, using that...");
						s.release();
					});
					s.acquireUninterruptibly();
				} else {
					error("Missing Jdk", "You didn't select the jdk to compile your application");
					return;
				}
			}
			config.separate();
		}

		if (!config.getJdk().isJdk()) {
			error("Invalid Jdk",
					"The jdk directory you selected is unsupported and can not be used to compile your app");
			return;
		}

		File rt = config.getJre().getValue();

		if (rt == null) {
			config.separate();
			config.logStd("no_jre_using_jdk");
			config.logStd("jdk_consider_1");
			config.logStd("jdk_consider_2");
			config.separate();

			List<File> jars = config.getDependencies().getJars();
			boolean rereso = jars.isEmpty();
			for (File jar : jars) {
				if (!jar.exists()) {
					rereso = true;
					break;
				}
			}

			if (rereso) {
				Semaphore s = new Semaphore(0);
				config.getDependencies().resolve(config.getClasspath().getRoot(), (r) -> {
					s.release();
				});
				s.acquireUninterruptibly();
			}

			Semaphore s = new Semaphore(0);
			config.getJre().set(config.getJdk().getValue(), () -> {
				s.release();
			});
			s.acquireUninterruptibly();

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

		preBuild = new File(System.getProperty("java.io.tmpdir") + "/jwin_pre_build_" + random.nextInt(999999));
		preBuild.mkdir();

		config.setState("copying_dependencies");
		File preBuildLibs = null;
		try {
			preBuildLibs = config.getDependencies().copy(preBuild, config::setProgress);
		} catch (IOException e2) {
			copyDependenciesFailure();
			config.onErr();
			return;
		}

		config.setState("copying_runtime");
		config.getJre().copy(preBuild, config::setProgress);

		config.setState("compiling_source_code");
		try {
			config.getClasspath().compile(preBuild, preBuildLibs, config.getJdk().getValue(),
					config.getMainClass().getValue(), config::incrementProgress, config.getMainClass()::setAltMain);
		} catch (IllegalStateException x) {
			compileFailure();
			config.onErr();
			return;
		}

		config.setState("copying_resources");
		config.getClasspath().copyRes(preBuild, config::setProgress);
	}

	public void run() {
		new Thread(() -> {
			preRun();

			config.setProgress(-1);
			config.logStd("running_proj");
			config.setState("running");

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

					Runnable end = () -> config.stop(p);

					command.addOnExit(ec -> {
						Platform.runLater(() -> {
							con.exited(ec);
							con.setOnStop(null);
						});
					});

					con.setOnStop(end);

					con.addOnHidden(end);
					con.setAction(end);
				});
			}

			config.logStd("proj_running");

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

		if (config.getIcon().getValue() == null || !config.getIcon().getValue().exists()) {
			config.getIcon().set(new File(
					URLDecoder.decode(getClass().getResource("/def.ico").getFile(), Charset.defaultCharset())));
		}

		if (config.getAppName().getValue().isBlank()) {
			error("Missing app name", "The application name field is required");
			return;
		}

		if (config.getVersion().getValue().isBlank()) {
			config.getVersion().setValue("0.0.1");
		}

		if (config.getGuid().getValue() == null || config.getGuid().getValue().isBlank()) {
			String guid = UUID.randomUUID().toString();
			config.logStd(Locale.key("guid_generated", "guid", guid));
			config.getGuid().setValue(guid);
		}

		// Check for warnings
		if (config.getJre().isJdk()) {
			boolean[] cont = new boolean[] { true };
			alert("using_jdk_as_runtime", "not_recommended_warning", AlertType.ERROR, res -> {
				if (res != ButtonType.YES) {
					cont[0] = false;
				}
			}, ButtonType.CANCEL, ButtonType.YES);
			if (!cont[0])
				return;
		}

		if (config.getFileInUse() == null) {
			alert("save_before_building", "save_project_recommendation", AlertType.CONFIRM, res -> {
				if (res.equals(ButtonType.YES)) {
					if (config.saveAs())
						config.logStd("project_saved");
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

				alert("save_changes_head", Locale.key("save_changes_body", "diffStr", diffStr.toString().trim()),
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
			alert("select_output_directory", "select_directory_description", AlertType.INFO, res -> {
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
				preRun();

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
		error("failed_to_compile_head", "failed_to_compile_body");
	}

	public static void copyDependenciesFailure() {
		error("failed_copy_deps_head", "failed_copy_deps_body");
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

	public static void deleteDir(File dir) {
		try (Stream<Path> stream = Files.walk(dir.toPath())) {
			stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static void copyDirCont(File src, File dest, Runnable onItemCopied) {
		if (!dest.exists()) {
			dest.mkdir();
		}
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
