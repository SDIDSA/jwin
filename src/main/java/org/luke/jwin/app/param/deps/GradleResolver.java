package org.luke.jwin.app.param.deps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
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
	/**
	 * Resolves project dependencies by simulating the execution of a Gradle task to print dependencies.
	 * Utilizes a specified or downloaded Gradle version and JDK based on project requirements.
	 *
	 * @param grad The build.gradle or build.gradle.kts file.
	 * @return A List of resolved dependency JAR files, or null if resolution fails.
	 */
	public static List<File> resolve(File grad) {
		try {
			ArrayList<File> jars = new ArrayList<File>();

			// prepare gradle
			File defaultGradle = new File(LocalStore.getDefaultGradle());
			File projectGradle = null;

			String dist_url = getDistributionUrl(grad.getParentFile());
			if(dist_url == null) return null;
			String version = getGradleVersion(grad.getParentFile());
			if(version == null) return null;

			File groot = GradleManager.dirForVer(version);

			if (groot.exists()) {
				projectGradle = groot;
			} else {
				// check the gradle wrapper dist folder
				String userHome = System.getProperty("user.home");

				File gradleDists = new File(userHome + "\\.gradle\\wrapper\\dists");
				if (gradleDists.exists()) {
					for (File sf : Objects.requireNonNull(gradleDists.listFiles())) {
						LocalInstall inst = GradleManager.versionFromDir(sf);
						if (inst != null && inst.getVersion().equals(version)) {
							Jwin.instance.getConfig().logStd(
									Locale.key("ver_found_in", "version", version, "path", sf.getAbsolutePath()));
							JwinActions.copyDirCont(inst.getRoot(), groot, null);
							projectGradle = groot;
							break;
						}
					}
				}

				if (projectGradle == null) {
					Jwin.instance.getConfig().logStd(Locale.key("down_vers", "version", version));
					Downloader.downloadZipInto(dist_url, (dp) -> {
						Jwin.instance.getConfig().logStdOver(Locale.key("down_prog", "version", version, "progress",
								displayProgressBar(((int) (dp * 100)))));
					}, (cp) -> {
						Jwin.instance.getConfig().logStdOver(Locale.key("ext_prog", "version", version, "progress",
								displayProgressBar(((int) (cp * 100)))));
					}, groot, f -> Objects.requireNonNull(GradleManager.versionFromDir(f)).getRoot());

					Jwin.instance.getConfig().logStdOver(Locale.key("vers_ready", "version", version));
					projectGradle = groot;
				}

			}


            defaultGradle.exists();

            // gradle is ready
			File gradle = projectGradle;

			String gradleVersion = Objects.requireNonNull(GradleManager.versionFromDir(gradle)).getVersion();

			int maxJavaVer = Downloader.maxGradleVer(gradleVersion);

			File[] jdkToUse = new File[] { null };

			JdkParam jdkParam = Jwin.instance.getConfig().getJdk();
			File defaultJdk = new File(LocalStore.getDefaultJdk());

			if (defaultJdk.exists()) {
				String ver = Objects.requireNonNull(JdkManager.versionFromDir(defaultJdk)).getVersion();
				int majVer = JdkManager.majorVer(ver);

				if (majVer <= maxJavaVer) {
					jdkToUse[0] = defaultJdk;
				}
			}

			if (jdkParam.isJdk() && jdkToUse[0] == null) {
				File projectJdk = jdkParam.getValue();
				String ver = Objects.requireNonNull(JdkManager.versionFromDir(projectJdk)).getVersion();
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
                        assert inst != null;
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

			// jdk is ready
			File jdk = jdkToUse[0];

			File temp = new File(
					grad.getParentFile().getAbsolutePath() + "\\" + grad.getName().replace("build.", "build_1."));
			grad.renameTo(temp);

			String kotlinTask = "task(\"printDepsForJwin\") {\r\n" + "    doLast {\r\n"
					+ "        configurations.compileClasspath.get().forEach(::println)\r\n" + "    }\r\n" + "}\n\n" +
					"tasks.register(\"getGroupForJwin\") {\n" +
					"    doFirst {\n" +
					"        println(project.group)\n" +
					"    }\n" +
					"}\n" +
					"\n" +
					"tasks.register(\"getVersionForJwin\") {\n" +
					"    doFirst {\n" +
					"        println(project.version)\n" +
					"    }\n" +
					"}";

			String groovyTask = "task printDepsForJwin {\r\n" + "    doLast {\r\n"
					+ "        println \"Dependencies:\"\r\n"
					+ "        configurations.runtimeClasspath.each { println it }\r\n" + "    }\r\n" + "}\r\n"
					+ "\n" +
					"task getGroupForJwin {\n" +
					"    doFirst {\n" +
					"        println project.group\n" +
					"    }\n" +
					"}\n" +
					"\n" +
					"task getVersionForJwin() {\n" +
					"    doFirst {\n" +
					"        println project.version\n" +
					"    }\n" +
					"}";

			FileDealer.write(FileDealer.read(temp) + (grad.getName().endsWith(".kts") ? kotlinTask : groovyTask), grad);

			File bin = new File(gradle.getAbsolutePath() + "\\bin");
			while (!bin.exists()) {
				gradle = Objects.requireNonNull(gradle.listFiles())[0];
				bin = new File(gradle.getAbsolutePath() + "\\bin");
			}

			String com = "gradle printDepsForJwin -Dorg.gradle.java.home=\"" + jdk.getAbsolutePath() + "\" -p=\""
					+ grad.getParentFile().getAbsolutePath() + "\" --no-daemon --quiet";
			String gcom = "gradle getGroupForJwin -Dorg.gradle.java.home=\"" + jdk.getAbsolutePath() + "\" -p=\""
					+ grad.getParentFile().getAbsolutePath() + "\" --no-daemon --quiet";
			String vcom = "gradle getVersionForJwin -Dorg.gradle.java.home=\"" + jdk.getAbsolutePath() + "\" -p=\""
					+ grad.getParentFile().getAbsolutePath() + "\" --no-daemon --quiet";

			Command c = new Command("cmd.exe", "/C", com);
			Command gc = new Command("cmd.exe", "/C", gcom);
			Command vc = new Command("cmd.exe", "/C", vcom);

			c.addInputHandler(line -> {
				File f = new File(line);
				if (f.exists())
					jars.add(f);
			});

			gc.addInputHandler(line -> {
				Jwin.instance.getConfig().getPublisher().setValue(line.trim());
			});

			vc.addInputHandler(line -> {
				Jwin.instance.getConfig().getVersion().setValue(line.trim());
			});

			ArrayList<Process> processes = new ArrayList<>();
			processes.add(c.execute(bin));
			processes.add(gc.execute(bin));
			processes.add(vc.execute(bin));
			for(Process p : processes) {
				p.waitFor();
			}

			grad.delete();
			temp.renameTo(grad);

			return jars;
		} catch (

		Exception x) {
			ErrorHandler.handle(x, "resolve dependencies");
			JwinActions.error("resolve_fail_head", "resolve_fail_body");
			return null;
		}
	}
	
	/**
	 * Retrieves the Gradle distribution URL from a Gradle project directory.
	 *
	 * @param root The root directory of the Gradle project.
	 * @return The Gradle distribution URL, or null if the gradle-wrapper.properties file is not found or cannot be read.
	 */
	public static String getDistributionUrl(File root) {
		try {
			File gwp = new File(root.getAbsolutePath(), "\\gradle\\wrapper\\gradle-wrapper.properties");
			if (gwp.exists()) {
                return parseInputStream(new FileInputStream(gwp)).get("distributionUrl").replace("https\\://",
                        "https://");
			}
		} catch (Exception x) {
			ErrorHandler.handle(x, "getting gradle version");
		}
		return null;
	}

	/**
	 * Retrieves the Gradle version from a Gradle project directory.
	 * Parses the distribution URL to extract the version information.
	 *
	 * @param root The root directory of the Gradle project.
	 * @return The Gradle version, or null if the distribution URL is not available.
	 */
	public static String getGradleVersion(File root) {
		String url = getDistributionUrl(root);
		if(url != null) {
            return "gradle_" + url.split("-")[1];
		}
		return null;
	}

	/**
	 * Parses key-value pairs from a properties file.
	 *
	 * @param is The InputStream to parse.
	 * @return A Map containing key-value pairs parsed from the InputStream.
	 * @throws IOException If an I/O error occurs while reading the InputStream.
	 */
	public static Map<String, String> parseInputStream(InputStream is) throws IOException {
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

	/**
	 * Generates a textual progress bar representation based on a given percentage.
	 *
	 * @param percentage The completion percentage for the progress bar.
	 * @return A string representing a progress bar with visual indicators and
	 *         percentage information.
	 */
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
