package org.luke.jwin.app.param.deps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.luke.gui.controls.alert.AlertType;
import org.luke.gui.controls.alert.ButtonType;
import org.luke.gui.exception.ErrorHandler;
import org.luke.gui.locale.Locale;
import org.luke.jwin.app.Command;
import org.luke.jwin.app.Jwin;
import org.luke.jwin.app.JwinActions;
import org.luke.jwin.app.file.FileDealer;
import org.luke.jwin.app.param.JdkParam;
import org.luke.jwin.local.Downloader;
import org.luke.jwin.local.LocalStore;
import org.luke.jwin.local.managers.GradleManager;
import org.luke.jwin.local.managers.JdkManager;
import org.luke.jwin.local.managers.LocalInstall;

public class GradleResolver {
	public static List<File> resolve(File grad) {
		try {
			ArrayList<File> jars = new ArrayList<File>();

			// prepare gradle
			File defaultGradle = new File(LocalStore.getDefaultGradle());
			File projectGradle = null;

			File gwp = new File(
					grad.getParentFile().getAbsolutePath() + "\\gradle\\wrapper\\gradle-wrapper.properties");
			if (gwp.exists()) {
				String url = parseInputStream(new FileInputStream(gwp)).get("distributionUrl").replace("https\\://",
						"https://");

				String version = "gradle_" + url.split("-")[1];

				File groot = GradleManager.dirForVer(version);

				if (groot.exists()) {
					projectGradle = groot;
				} else {
					// check the gradle wrapper dist folder
					String userHome = System.getProperty("user.home");

					File gradleDists = new File(userHome + "\\.gradle\\wrapper\\dists");
					if (gradleDists.exists()) {
						for (File sf : gradleDists.listFiles()) {
							LocalInstall inst = GradleManager.versionFromDir(sf);
							if (inst != null && inst.getVersion().equals(version)) {
								Jwin.instance.getConfig().logStd(Locale.key("ver_found_in", "version", version,
										"path", sf.getAbsolutePath()));
								JwinActions.copyDirCont(inst.getRoot(), groot, null);
								projectGradle = groot;
								break;
							}
						}
					}

					if (projectGradle == null) {
						// download if not found
						Jwin.instance.getConfig().logStd(Locale.key("down_vers", "version", version));
						Downloader.downloadZipInto(url, (dp) -> {
							Jwin.instance.getConfig().logStdOver(Locale.key("down_prog", "version", version,
									"progress", displayProgressBar(((int) (dp * 100)))));
						}, (cp) -> {
							Jwin.instance.getConfig().logStdOver(Locale.key("ext_prog", "version", version,
									"progress", displayProgressBar(((int) (cp * 100)))));
						}, groot, f -> GradleManager.versionFromDir(f).getRoot());

						Jwin.instance.getConfig().logStdOver(Locale.key("vers_ready", "version", version));
						projectGradle = groot;
					}

				}
			}

			if (!defaultGradle.exists() && projectGradle == null) {
				Semaphore s = new Semaphore(0);

				JwinActions.alert("gradle_unset_head", "gradle_unset_body", AlertType.ERROR, res -> {
					if (res == ButtonType.YES) {
						Jwin.instance.openSettings("gradle_versions");
					}
					s.release();
				}, ButtonType.CANCEL, ButtonType.YES);

				s.acquireUninterruptibly();

				defaultGradle = new File(LocalStore.getDefaultGradle());
				if (!defaultGradle.exists()) {
					return null;
				}
			}

			// gradle is ready
			File gradle = projectGradle != null ? projectGradle : defaultGradle;

			String gradleVersion = GradleManager.versionFromDir(gradle).getVersion();

			int maxJavaVer = Downloader.maxGradleVer(gradleVersion);

			File[] jdkToUse = new File[] { null };

			JdkParam jdkParam = Jwin.instance.getConfig().getJdk();
			File defaultJdk = new File(LocalStore.getDefaultJdk());

			if (defaultJdk.exists()) {
				String ver = JdkManager.versionFromDir(defaultJdk).getVersion();
				int majVer = JdkManager.majorVer(ver);

				if (majVer <= maxJavaVer) {
					jdkToUse[0] = defaultJdk;
				}
			}

			if (jdkParam.isJdk() && jdkToUse[0] == null) {
				File projectJdk = jdkParam.getValue();
				String ver = JdkManager.versionFromDir(projectJdk).getVersion();
				int majVer = JdkManager.majorVer(ver);

				if (majVer <= maxJavaVer) {
					jdkToUse[0] = projectJdk;
				}
			}

			if (jdkToUse[0] == null) {
				for (LocalInstall inst : JdkManager.allInstalls()) {
					if (JdkManager.majorVer(inst.getVersion()) <= maxJavaVer) {
						jdkToUse[0] = inst.getRoot();
						break;
					}
				}
			}

			if (jdkToUse[0] == null) {
				List<File> detected = JdkParam.detectJdkCache();
				if (detected != null) {
					for (File jdk : detected) {
						LocalInstall inst = JdkManager.versionFromDir(jdk);
						if (JdkManager.majorVer(inst.getVersion()) <= maxJavaVer) {
							jdkToUse[0] = inst.getRoot();
							break;
						}
					}
				}
			}

			if (jdkToUse[0] == null) {
				Jwin.instance.getConfig().separate();
				Jwin.instance.getConfig().logStd(Locale.key(

						"gradle_jdk_ver", "gradleVersion", gradleVersion, "maxJavaVersion", maxJavaVer));
				Jwin.instance.getConfig().logStd("jdk_unfound");

				File downloadInto = JdkManager.dirForVer("jdk_" + maxJavaVer);
				Jwin.instance.getConfig().logStd(Locale.key("down_vers", "version", "jdk " + maxJavaVer));
				Downloader.downloadJava(maxJavaVer, (dp) -> {
					Jwin.instance.getConfig().logStdOver(Locale.key("down_prog", "version", "jdk " + maxJavaVer,
							"progress", displayProgressBar(((int) (dp * 100)))));
				}, (cp) -> {
					Jwin.instance.getConfig().logStdOver(Locale.key("ext_prog", "version", "jdk " + maxJavaVer,
							"progress", displayProgressBar(((int) (cp * 100)))));
				}, downloadInto);
				Jwin.instance.getConfig().logStdOver(Locale.key("vers_ready", "version", "jdk " + maxJavaVer));
				Jwin.instance.getConfig().separate();
				jdkToUse[0] = downloadInto;
			}

			if (jdkToUse[0] == null) {
				return null;
			}

			if (!jdkParam.isJdk()) {
				jdkParam.set(jdkToUse[0]);
			}

			// jdk is ready
			File jdk = jdkToUse[0];

			File temp = new File(
					grad.getParentFile().getAbsolutePath() + "\\" + grad.getName().replace("build", "build_1"));
			grad.renameTo(temp);

			String kotlinTask = "task(\"printDepsForJwin\") {\r\n" + "    doLast {\r\n"
					+ "        configurations.compileClasspath.get().forEach(::println)\r\n" + "    }\r\n" + "}";

			String groovyTask = "task printDepsForJwin {\r\n" + "    doLast {\r\n"
					+ "        println \"Dependencies:\"\r\n"
					+ "        configurations.runtimeClasspath.each { println it }\r\n" + "    }\r\n" + "}";

			FileDealer.write(FileDealer.read(temp) + (grad.getName().endsWith(".kts") ? kotlinTask : groovyTask), grad);

			File bin = new File(gradle.getAbsolutePath() + "\\bin");
			while (!bin.exists()) {
				gradle = gradle.listFiles()[0];
				bin = new File(gradle.getAbsolutePath() + "\\bin");
			}

			String com = "gradle printDepsForJwin -Dorg.gradle.java.home=\"" + jdk.getAbsolutePath() + "\" -p=\""
					+ grad.getParentFile().getAbsolutePath() + "\" --no-daemon --quiet";

			Command c = new Command("cmd.exe", "/C", com);

			c.addInputHandler(line -> {
				File f = new File(line);
				if (f.exists())
					jars.add(f);
			});

			c.execute(bin).waitFor();

			grad.delete();
			temp.renameTo(grad);

			return jars;
		} catch (

		Exception x) {
			ErrorHandler.handle(x, "resolve dependencies");
			JwinActions.error("resolve_fail_head",
					"resolve_fail_body");
			return null;
		}
	}

	private static HashMap<String, String> parseInputStream(InputStream is) throws IOException {
		HashMap<String, String> data = new HashMap<>();

		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;

		while ((line = br.readLine()) != null) {
			if (line.contains("=")) {
				String[] parts = line.split("=");
				data.put(parts[0], parts[1]);
			}
		}

		is.close();
		br.close();

		return data;
	}

	private static String displayProgressBar(int percentage) {
		int width = 20;
		int progress = (int) (width * percentage / 100.0);

		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < width; i++) {
			if (i < progress) {
				sb.append("=");
			} else {
				sb.append(" ");
			}
		}
		sb.append("] %" + percentage);

		return sb.toString();
	}

}
