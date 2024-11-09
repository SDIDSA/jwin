package org.luke.jwin.app;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.lang.module.ModuleDescriptor.Version;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.luke.gui.controls.alert.Alert;
import org.luke.gui.controls.alert.AlertType;
import org.luke.gui.controls.alert.ButtonType;
import org.luke.gui.exception.ErrorHandler;
import org.luke.gui.file.FileUtils;
import org.luke.gui.locale.Locale;
import org.luke.gui.window.Window;
import org.luke.jwin.app.console.Console;
import org.luke.jwin.app.file.*;
import org.luke.jwin.app.layout.JwinUi;
import org.luke.jwin.app.param.JdkParam;
import org.luke.jwin.local.LocalStore;

import javafx.application.Platform;
import javafx.stage.DirectoryChooser;

public class JwinActions {
	private static Window window;

	private final Random random = new Random();

	private File preBuild;

	private final Window ps;
	static private JwinUi config;

	private final DirectoryChooser fc;

	public JwinActions(Window ps, JwinUi config) {
		this.ps = ps;
		window = ps;
		JwinActions.config = config;

		fc = new DirectoryChooser();
	}

	private boolean preRun() {
		List<File> cp = config.getClasspath().getFiles();

		if (cp.isEmpty()) {
			error("missing_cp_head", "missing_cp_body");
			return false;
		}

		if (config.getMainClass().getValue() == null) {
			config.separate();
			config.logStd("mc_auto_attempt");
			Map<String, File> mcs = config.getMainClass().listMainClasses();

			if (mcs.size() == 1) {
				Map.Entry<String, File> mc = mcs.entrySet().iterator().next();
				config.logStd(Locale.key("mc_set", "class", mc.getKey()));
				config.getMainClass().set(mc);
			} else {
				if(mcs.size() > 1) {
					config.logStd("mutltipe_mains");
				}
				Semaphore wait = new Semaphore(0);
				AtomicBoolean selected = new AtomicBoolean(false);
				error("mc_required_head", "mc_required_body", (r) -> {
					if(r == ButtonType.SELECT_NOW) {
						String n = config.getMainClass().showChooser();
						if (n != null) {
							config.logStd(Locale.key("mc_set", "class", n));
							selected.set(true);
						}
					}
					wait.release();
				}, ButtonType.SELECT_NOW, ButtonType.CLOSE);
				wait.acquireUninterruptibly();
				if(!selected.get())
					return false;
			}
		}

		if (!config.getClasspath().isValidMainClass(config.getMainClass().getValue().getValue())) {
			error("mc_invalid_head", "mc_invalid_body");
			return false;
		}

		File dk = config.getJdk().getValue();

		if (dk == null) {
			config.separate();
			config.logStd("missing_jdk_head");

			File defJdk = new File(LocalStore.getDefaultJdk());
			if (defJdk.exists()) {
				Semaphore s = new Semaphore(0);
				config.getJdk().set(defJdk, "", () -> {
					config.logStd(Locale.key("using_default_jdk", "version", config.getJdk().getVersion()));
					s.release();
				});
				s.acquireUninterruptibly();
			} else {
				List<File> fs = JdkParam.detectJdkCache();
				if (!fs.isEmpty()) {
					File f = fs.getFirst();
					Semaphore s = new Semaphore(0);
					config.getJdk().set(f, " (found in your system)", () -> {
						config.logStd("jdk " + config.getJdk().getVersion() + " was detected, using that...");
						s.release();
					});
					s.acquireUninterruptibly();
				} else {
					error("Missing Jdk", "You didn't select the jdk to compile your application");
					return false;
				}
			}
			config.separate();
		}

		if (!config.getJdk().isJdk()) {
			error("Invalid Jdk",
					"The jdk directory you selected is unsupported and can not be used to compile your app");
			return false;
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
				config.getDependencies().resolve(config.getClasspath().getRoot(), (_) -> s.release());
				s.acquireUninterruptibly();
			}

			Semaphore s = new Semaphore(0);
			config.getJre().set(config.getJdk().getValue(), s::release);
			s.acquireUninterruptibly();

			rt = config.getJre().getValue();
			if (rt == null) {
				error("missing_jre_head", "missing_jre_body");
				return false;
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
			return false;
		}

		if (config.getDependencies().isResolving()) {
			warn("resolving_dependencies", "");
			return false;
		}

		if (config.getProjectInUse().isConsole() == null && config.getConsole().isUnset() && config.getClasspath().isConsoleApp()) {
			config.logStd("console_estimated");
			config.getConsole().checkedProperty().set(true);
		}

		config.disable(true, false);

		preBuild = new File(System.getProperty("java.io.tmpdir") + "/jwin_pre_build_" + random.nextInt(999999));
		preBuild.mkdir();

		config.setState("copying_dependencies");
		File preBuildLibs;
		try {
			preBuildLibs = config.getDependencies().copy(preBuild, config::setProgress);
		} catch (IOException e2) {
			copyDependenciesFailure();
			config.onErr();
			return false;
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
			return false;
		}
		List<RootFileScanner.DetectedFile> detectedFiles = null;
        try {
            detectedFiles = RootFileScanner.scanRoot(config.getClasspath().getRoot());
        } catch (IOException e) {
            ErrorHandler.handle(e, "detect important files from root folder");
        }

		if(detectedFiles != null && !detectedFiles.isEmpty()) {
			List<File> files = config.getRootFiles().getFiles();
			List<File> exclude = config.getRootFiles().getExclude();
			boolean found = false;
			for(RootFileScanner.DetectedFile df: detectedFiles) {
				if(!exclude.contains(df.file()) && !files.contains(df.file())) {
					found = true;
					break;
				}
			}

			if(found) {
				Semaphore wait = new Semaphore(0);
				List<RootFileScanner.DetectedFile> fdfs = detectedFiles;
				Platform.runLater(() -> config.getRootFiles().showOverlay(fdfs, wait::release));
				wait.acquireUninterruptibly();
			}

			for(File toInclude : config.getRootFiles().getFiles()) {
                try {
                    Files.copy(toInclude.toPath(), new File(preBuild, toInclude.getName()).toPath());
                } catch (IOException e) {
                    ErrorHandler.handle(e, "copy file " + toInclude.getName());
                }
            }
		}

		if(detectedFiles != null && !detectedFiles.isEmpty()) {
			List<File> files = config.getRootFiles().getFiles();
			List<File> exclude = config.getRootFiles().getExclude();
			for (RootFileScanner.DetectedFile df : detectedFiles) {
				if (!exclude.contains(df.file()) && !files.contains(df.file())) {
					exclude.add(df.file());
					config.logStd(Locale.key("default_excluded", "file", df.file().getName()));
				}
			}
		}

        config.setState("copying_resources");
		config.getClasspath().copyRes(preBuild, config::setProgress);
		return true;
	}

	public void run() {
		new Thread(() -> {
			if(!preRun()) {
				config.setProgress(-1);
				config.setState("idle");
				return;
			}

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
					ErrorHandler.handle(e, "open logs file");
				}
			};

			String c = "\"rt/bin/java\" -cp \"bin;res;lib/*\" "
					+ (config.getMainClass().getAltMain() == null ? config.getMainClass().getValue().getKey()
							: config.getMainClass().getAltMain());
			Command command = new Command(_ -> {
			}, err -> {
				if(err.endsWith("com.sun.javafx.application.PlatformImpl startup")) return;
				if(err.startsWith("WARNING: Unsupported JavaFX configuration: classes were loaded from 'unnamed module")) return;
				errBuilder.append(err).append('\n');
			}, "cmd", "/c", c);

			Process p = command.execute(preBuild);

			if (config.getConsole().checkedProperty().get()) {
				Platform.runLater(() -> {
					Console con = new Console(window.getLoadedPage(), command);
					con.show();

					Runnable end = () -> config.stop(p);

					command.addOnExit(ec ->
							Platform.runLater(() -> {
                        		con.exited(ec);
                        		con.setOnStop(null);
							}));

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
			error("temp_unfound_head", "temp_unfound_body");
			return;
		}

		if (config.getIcon().getValue() == null || !config.getIcon().getValue().exists()) {
			config.getIcon().set(new File(
					URLDecoder.decode(Objects.requireNonNull(getClass().getResource("/def.ico")).getFile(),
							Charset.defaultCharset())));
		}

		if (config.getAppName().getValue().isBlank()) {
			error("app_name_head", "app_name_body");
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
								config.logStd("changes_saved");
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

				config.setState("generating_launcher");

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
						URLDecoder.decode(Objects.requireNonNull(getClass().getResource("/b2e.exe")).getFile(), Charset.defaultCharset()));

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
					ErrorHandler.handle(e1, "convert bat to exe");
					Thread.currentThread().interrupt();
				}

				try {
					Files.delete(preBuildBat.toPath());
				} catch (IOException e2) {
					ErrorHandler.handle(e2, "delete launcher file");
				}

				config.setState("generating_installer");
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
								ErrorHandler.handle(e1, "copy icon");
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
						URLDecoder.decode(Objects.requireNonNull(getClass().getResource("/is/ISCC.exe")).getFile(),
								Charset.defaultCharset()));

				//set process icon and description
				try {
					File javaBin = new File(preBuild, "\\rt\\bin");
					File java = new File(javaBin, "\\java.exe");
					File javaw = new File(javaBin, "\\javaw.exe");
					File javadll = new File(javaBin, "\\java.dll");

					setFileDescription(java);
					setFileDescription(javaw);
					setFileDescription(javadll);

					setFileIcon(java, config.getIcon().getValue());
					setFileIcon(javaw, config.getIcon().getValue());

					new Command("cmd.exe", "/C","ie4uinit.exe -ClearIconCache").execute(preBuild).waitFor();
					new Command("cmd.exe", "/C","ie4uinit.exe -show").execute(preBuild).waitFor();
				}catch(Exception e) {
					ErrorHandler.handle(e, "set process description & icon");
				}

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
					ErrorHandler.handle(e1, "generate installer");
					Thread.currentThread().interrupt();
				}

				try {
					Desktop.getDesktop().open(saveTo);
				} catch (IOException e1) {
					ErrorHandler.handle(e1, "open installer folder");
				}

				Platform.runLater(() -> {
					config.setState("done");
					config.setProgress(-1);
					config.disable(false, true);
				});
			}).start();
		}
	}

	private static void setFileIcon(File file, File icon) throws InterruptedException {
		Command extractVersionInfo = new Command("cmd.exe", "/C","ResourceHacker.exe " +
				"-open \"" + file.getAbsolutePath() + "\" " +
				"-save \"" + file.getAbsolutePath() + "\" " +
				"-action addoverwrite " +
				"-res \"" + icon.getAbsolutePath() + "\" " +
				"-mask ICONGROUP,MAINICON,\n");
		extractVersionInfo.execute(Jwin.getResourceHacker()).waitFor();
	}

	private static void setFileDescription(File file) throws IOException, InterruptedException {
		File tempVersionInfo = File.createTempFile("jwin_version_info_", ".rc");
		Command extractVersionInfo = new Command("cmd.exe", "/C",
				"ResourceHacker.exe " +
						"-open \"" + file.getAbsolutePath() + "\" " +
						"-save \"" + tempVersionInfo.getAbsolutePath() + "\" " +
						"-action extract " +
						"-mask VERSIONINFO,1,");

		extractVersionInfo.execute(Jwin.getResourceHacker()).waitFor();

		String content = FileDealer.read(tempVersionInfo, StandardCharsets.UTF_16);
        assert content != null;
        String modifiedContent = content.replace("VALUE \"FileDescription\", \"Java(TM) Platform SE binary\"",
						"VALUE \"FileDescription\", \"" + config.getAppName().getValue() + "\"")
				.replace("VALUE \"ProductName\", \"Java(TM) Platform SE 23.0.1\"",
						"VALUE \"ProductName\", \"" + config.getAppName().getValue() + "\"");

		FileDealer.write(modifiedContent, tempVersionInfo, StandardCharsets.UTF_16);

		File compiledVersionInfo = File.createTempFile("jwin_version_info_", ".res");
		Command compileVersionInfo = new Command("cmd.exe", "/C", "ResourceHacker.exe " +
				"-open \"" + tempVersionInfo.getAbsolutePath() + "\" " +
				"-save \"" + compiledVersionInfo.getAbsolutePath() + "\" " +
				"-action compile");

		compileVersionInfo.execute(Jwin.getResourceHacker()).waitFor();

		Command setVersionInfo = new Command("cmd.exe", "/C", "ResourceHacker.exe " +
				"-open \"" + file.getAbsolutePath() + "\" " +
				"-save \"" + file.getAbsolutePath() + "\" " +
				"-action addoverwrite -res \"" + compiledVersionInfo.getAbsolutePath() + "\" " +
				"-mask VERSIONINFO,1,");

		setVersionInfo.execute(Jwin.getResourceHacker()).waitFor();
	}

	public static int countDir(File dir) {
		int count = 0;

		for (File f : Objects.requireNonNull(dir.listFiles())) {
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
		config.logErr(content);
	}

	public static void error(String head, String content) {
		error(head, content, null, (Runnable) null);
	}

	public static void error(String head, String content, Runnable onHide, ButtonType... types) {
		error(head, content, null, onHide, types);
	}

	public static void error(String head, String content, Consumer<ButtonType> onRes, ButtonType... types) {
		error(head, content, onRes, null, types);
	}

	public static void error(String head, String content, Consumer<ButtonType> onRes, Runnable onHide, ButtonType... types) {
		alert(head, content, AlertType.ERROR, onRes, onHide, types);
		config.logErr(head);
		config.logErr(content);
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

	public static long deleteDir(File dir) {
		AtomicLong size = new AtomicLong(0);
		try (Stream<Path> stream = Files.walk(dir.toPath())) {
			stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(file -> {
				long l = file.length();
				if(file.delete()) {
					size.addAndGet(l);
				}
			});
		} catch (IOException e1) {
			ErrorHandler.handle(e1, "delete dir " + dir.getName());
		}
		return size.get();
	}

	public static void copyDirCont(File src, File dest, Runnable onItemCopied) {
		if (!dest.exists()) {
			dest.mkdir();
		}
		for (File file : Objects.requireNonNull(src.listFiles())) {
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
					ErrorHandler.handle(e, "copyDirCont from " + src.getAbsolutePath() +
							" to " + dest.getAbsolutePath());
				}
			}
		}
	}
}
