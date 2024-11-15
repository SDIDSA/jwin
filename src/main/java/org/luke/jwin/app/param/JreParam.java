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
import java.util.Objects;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.DoubleConsumer;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.alert.AlertType;
import org.luke.gui.controls.alert.ButtonType;
import org.luke.gui.controls.label.unkeyed.Link;
import org.luke.gui.exception.ErrorHandler;
import org.luke.gui.locale.Locale;
import org.luke.gui.window.Window;
import org.luke.jwin.app.Command;
import org.luke.jwin.app.Jwin;
import org.luke.jwin.app.JwinActions;
import org.luke.jwin.app.layout.JwinUi;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class JreParam extends JavaParam {
	private final DirectoryChooser dc;
	private final FileChooser fc;

	public JreParam(Window ps, JwinUi config) {
		super(ps, "jre_pack");

		dc = new DirectoryChooser();
		addButton(ps, "directory", this::browseDir);

		fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("archive", "*.zip"));
		addButton(ps, "archive", this::browseArchive);

		Link generateFromJdk = new Link(ps, "Generate JRE using jlink", new Font(12));
		root.setAlignment(Pos.CENTER_RIGHT);
		root.getChildren().addFirst(generateFromJdk);

		generateFromJdk.setAction(() -> generateFromJdk(ps, config));
	}

	public void browseDir() {
		File dir = dc.showDialog(getWindow());
		if (dir != null) {
			setFile(dir);
		}
	}

	public void browseArchive() {
		File file = fc.showOpenDialog(getWindow());
		if (file != null) {
			setFile(file);
		}
	}

	public void setFile(File file) {
		super.set(file, log());
	}

	public void setFile(File file, Runnable onFinish) {
		super.set(file, log(onFinish));
	}

	public void setFile(File file, String additional) {
		super.set(file, additional, log());
	}

	public void setFile(File file, String additional, Runnable onFinish) {
		super.set(file, additional, log(onFinish));
	}

	private Runnable log;
	private synchronized Runnable log() {
		if(log == null) {
			log = () -> Jwin.instance.getConfig()
					.logStd(Locale.key("jre_set", "version", version));
		}
		return log;
	}

	private Runnable log(Runnable or) {
		if(or == log()) return or;
		return () -> {
			if(or != null) or.run();
			log.run();
		};
	}

	public void generateFromJdk(Window ps, JwinUi config) {
		generateFromJdk(ps, config, true, null);
	}

	public void generateFromJdk(Window ps, JwinUi config, boolean alert, Runnable onFinish) {
		startLoading();

		config.logStd("generating_jre");
		new Thread(() -> {
			Runnable cancel = () -> {
				stopLoading();
				config.logStd("jre_gen_failed");
			};
			Runnable invalidJdk = () -> {
				cancel.run();
				JwinActions.error("invalid_jdk_head", "invalid_jdk_body");
			};

			if (config.getDependencies().isResolving()) {
				cancel.run();
				JwinActions.warn("resolving_dependencies", "still_resolving");
				return;
			}

			if (config.getJdk().getValue() == null) {
				cancel.run();
				JwinActions.error("missing_jdk_head", "missing_jdk_body");
				return;
			}

			if (!config.getJdk().isJdk()) {
				invalidJdk.run();
				return;
			}

			File jlink = new File(config.getJdk().getValue().getAbsolutePath().concat("/bin/jlink.exe"));
			File jdeps = new File(config.getJdk().getValue().getAbsolutePath().concat("/bin/jdeps.exe"));
			if (!jlink.exists() || !jdeps.exists()) {
				invalidJdk.run();
				return;
			}

			ArrayList<String> deps = new ArrayList<>();
			deps.add("jdk.crypto.ec");
			deps.add("jdk.unsupported");

			File preGen = new File(System.getProperty("java.io.tmpdir") + "/jwin_preGen_" + new Random().nextInt(9999));
			preGen.mkdir();

			File preGenLibs;
			try {
				preGenLibs = config.getDependencies().copy(preGen, null);
			} catch (IOException e1) {
				cancel.run();
				JwinActions.copyDependenciesFailure();
				return;

			}

			if (config.getMainClass().getValue() == null) {
				cancel.run();
				JwinActions.error("mc_required_head", "mc_required_body");
				return;
			}

			if (config.getClasspath().isInvalidMainClass(config.getMainClass().getValue().getValue())) {
				cancel.run();
				JwinActions.error("mc_invalid_head", "mc_invalid_body");
				return;
			}

			File preGenBin;
			try {
				preGenBin = config.getClasspath().compile(preGen, preGenLibs, config.getJdk().getValue(),
						config.getMainClass().getValue(), null, null);
			} catch (Exception x) {
				cancel.run();
				JwinActions.compileFailure();
				return;
			}

			if (preGenBin == null) {
				cancel.run();
				JwinActions.compileFailure();
				return;
			}

			File jdkBin = new File(config.getJdk().getValue().getAbsolutePath().concat("/bin"));
			try {
				Platform.runLater(() -> startLoading("Analyzing dependencies ..."));
				Predicate<String> isValid = dep -> dep.indexOf("java.") == 0 || dep.indexOf("jdk.") == 0;

				String mr = "base";

				String com = "jdeps --ignore-missing-deps -q --recursive  --multi-release " + mr
						+ " --print-module-deps --class-path \"" + preGenLibs.getAbsolutePath() + "\\*\" \""
						+ preGenBin.getAbsolutePath() + "\"";

				// analyze code for module dependencies
				Command analCode = new Command(line -> {
					if (!line.isBlank()) {
						for (String dep : line.split(",")) {
							if (!deps.contains(dep) && isValid.test(dep)) {
								deps.add(dep);
							}
						}
					}
				}, "cmd", "/C", com);

				analCode.execute(jdkBin).waitFor();
				deps.remove("bin");

				// analyze libs for moduleDependencies
				if (Objects.requireNonNull(preGenLibs.listFiles()).length != 0) {
					StringBuilder sb = new StringBuilder();
					for (File lib : Objects.requireNonNull(preGenLibs.listFiles())) {
						sb.append(" \"").append(URLEncoder.encode(lib.getAbsolutePath(), StandardCharsets.UTF_8))
								.append("\"");
					}

					Command analLibs = new Command(line -> {
						String[] parts = line.split("\\s+");
						if (parts.length == 3) {
							String dep = parts[parts.length - 1];
							if (!deps.contains(dep) && isValid.test(dep)) {
								deps.add(dep);
							}
						}
					}, "cmd", "/c", "jdeps --multi-release " + mr + sb);
					analLibs.execute(jdkBin).waitFor();
				}

				Platform.runLater(() -> startLoading("Generating JRE ..."));
				Command gen = new Command("cmd", "/c",
						"jlink --no-header-files --no-man-pages --strip-debug --module-path \""
								+ preGenLibs.getAbsolutePath() + "\" --add-modules " + String.join(",", deps)
								+ " --output \"" + preGen.getAbsolutePath().concat("/rt") + "\"");

				gen.execute(jdkBin).waitFor();

				File preGenRt = new File(preGen.getAbsolutePath().concat("/rt"));

				if (alert) {
					JwinActions.alert("save_runtime_head", "save_runtime_body",
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
												config.logStd(Locale.key("runtime_saved", "path",
														saveTo.getAbsolutePath()));
											});
										}).start();
									}
								}
							}, ButtonType.YES, ButtonType.NO);
				}

				Platform.runLater(() -> {
					set(preGenRt, " (generated with jlink)", onFinish);
					Entry<String, File> version = JavaParam.getVersionFromDir(preGenRt);
					if (version != null && version.getKey() != null) {
						String disp = "jre " + version.getKey().replace("\"", "");
						config.logStd(Locale.key("jre_jlink_set", "version", disp));
					}
				});

			} catch (InterruptedException e) {
				ErrorHandler.handle(e, "generate jre with jlink");
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
									ErrorHandler.handle(x, "extract jre zip");
								}
							}
						}

						if (onProgress != null) {
							onProgress.accept(.2 + (copyCount[0] / (double) ec) * .2);
						}
					});
				}

				zip.close();
			} catch (IOException e1) {
				ErrorHandler.handle(e1, "extract jre zip");
			}
		} else {
			int count = JwinActions.countDir(value);
			int[] copyCount = new int[] { 0 };
			JwinActions.copyDirCont(value, preBuildRt, () -> {
				copyCount[0]++;
				if (onProgress != null) {
					onProgress.accept(.2 + (copyCount[0] / (double) count) * .2);
				}
			});
		}
	}

}
