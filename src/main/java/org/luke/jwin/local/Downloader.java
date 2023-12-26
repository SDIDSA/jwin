package org.luke.jwin.local;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Consumer;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONObject;
import org.luke.gui.exception.ErrorHandler;
import org.luke.jwin.app.Command;
import org.luke.jwin.app.Jwin;
import org.luke.jwin.app.JwinActions;
import org.luke.jwin.app.file.FileDealer;
import org.luke.jwin.local.managers.JdkManager;

public class Downloader {
	/**
	 * Downloads a ZIP file from the specified URL, extracts its contents, and copies them to a target directory.
	 * Provides progress updates during download and copy operations.
	 *
	 * @param urlString   The URL of the ZIP file to download.
	 * @param onDownload  Consumer to receive download progress updates.
	 * @param onCopy      Consumer to receive copy progress updates.
	 * @param targetDir   The target directory where the extracted contents will be copied.
	 * @param rootSupplier Function to determine the root directory of the extracted contents.
	 */
	public static void downloadZipInto(String urlString, Consumer<Double> onDownload, Consumer<Double> onCopy,
			File targetDir, Function<File, File> rootSupplier) {
		try {
			URL url = URI.create(urlString).toURL();
			URLConnection con = url.openConnection();
			InputStream is = con.getInputStream();

			int ind = urlString.indexOf("?");
			String name = urlString.substring(urlString.lastIndexOf("/") + 1, ind == -1 ? urlString.length() : ind);
			name = name.substring(0, name.lastIndexOf("."));

			File output = File.createTempFile(name + "_", ".zip");
			OutputStream os = new FileOutputStream(output);

			int fileLength = con.getContentLength();
			int count;
			byte[] buffer = new byte[2048];
			int totalRead = 0;

			long lastUpdate = System.currentTimeMillis();

			while ((count = is.read(buffer)) != -1) {
				os.write(buffer, 0, count);
				totalRead += count;

				long now = System.currentTimeMillis();
				if (now - lastUpdate > 200) {
					onDownload.accept((double) totalRead / fileLength);
					lastUpdate = now;
				}
			}
			os.flush();
			os.close();
			
			onDownload.accept(1.0);

			File temp = File.createTempFile(name, "");
			temp.delete();
			new Command(System.out::println, System.err::println, "cmd", "/c",
					"7z x \"" + output.getAbsolutePath() + "\" -aou -o\"" + temp.getAbsolutePath() + "\"")
					.execute(Jwin.get7z()).waitFor();

			if (!targetDir.exists() || !targetDir.isDirectory()) {
				targetDir.mkdir();
			}

			int fileCount = JwinActions.countDir(temp);
			int[] copyCount = new int[] { 0 };
			long[] la = new long[] { 0 };
			JwinActions.copyDirCont(rootSupplier.apply(temp), targetDir, () -> {
				copyCount[0]++;

				long now = System.currentTimeMillis();
				if (now - la[0] > 200) {
					onCopy.accept((copyCount[0] / (double) fileCount));
					la[0] = now;
				}
			});
		} catch (IOException | InterruptedException e) {
			ErrorHandler.handle(e, "download file " + urlString);
		}
	}

	private static JSONObject compMat = null;
	
	/**
	 * Retrieves the maximum Java version supported by a given Gradle version or lower.
	 * Uses a compatibility mapping stored in "/versions.json".
	 *
	 * @param ver The Gradle version to find compatibility for.
	 * @return The maximum compatible Java version, or -1 if no compatible version is found.
	 */
	public synchronized static int maxGradleVer(String ver) {
		if (compMat == null) {
			String versions = FileDealer.read("/versions.json");
			JSONObject all = new JSONObject(versions);
			compMat = all.getJSONObject("gradle_jdk_map");
		}
		int max = -1;
		for (String over : compMat.keySet()) {
			if (JdkManager.compareVersions(ver, over) <= 0) {
				int jver = Integer.parseInt(compMat.getString(over));
				if (jver > max || max == -1) {
					max = jver;
				}
			}
		}
		return max;
	}

	/**
	 * Downloads the latest version of Java with the specified major version from Azul Zulu,
	 * and extracts the contents to a target directory. Provides progress updates during download and copy operations.
	 *
	 * @param major       The major version of Java to download.
	 * @param onDownload  Consumer to receive download progress updates.
	 * @param onCopy      Consumer to receive copy progress updates.
	 * @param targetDir   The target directory where the Java contents will be extracted.
	 */
	public static void downloadJava(int major, Consumer<Double> onDownload, Consumer<Double> onCopy, File targetDir) {
		String urlString = "https://api.azul.com/metadata/v1/zulu/packages/?java_version=" + major
				+ "&os=windows&arch=x64&archive_type=zip&java_package_type=jdk&javafx_bundled=false&latest=true&release_status=ga&certifications=tck&page=1&page_size=100";

		try {
			URL url = new URI(urlString).toURL();
			InputStream is = url.openStream();
			String cont = FileDealer.read(is);
			JSONArray arr = new JSONArray(cont);
			JSONObject obj = arr.getJSONObject(0);
			String downloadUrl = obj.getString("download_url");
			downloadZipInto(downloadUrl, onDownload, onCopy, targetDir,
					f -> JdkManager.versionFromDir(f).getRoot());
		} catch (Exception x) {
			ErrorHandler.handle(x, "download file " + urlString);
		}
	}
}
