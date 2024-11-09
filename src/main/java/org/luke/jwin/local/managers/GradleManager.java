package org.luke.jwin.local.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.luke.gui.window.Window;
import org.luke.jwin.app.JwinActions;
import org.luke.jwin.app.file.FileDealer;
import org.luke.jwin.local.LocalStore;
import org.luke.jwin.local.ui.DownloadJob;
import org.luke.jwin.local.ui.DownloadState;
import org.luke.jwin.local.ui.Installed;

public class GradleManager {

	private static final HashMap<String, String> versionCache = new HashMap<>();

	private static final HashMap<String, String> installable;

	private static final HashMap<String, Installed> managedCache = new HashMap<>();

	private static final HashMap<File, Installed> localCache = new HashMap<>();

	private static final File root = new File(System.getenv("appData") + "\\jwin\\gradle");

	private static final HashMap<String, DownloadJob> downloadJobs = new HashMap<>();

	static {
		installable = new HashMap<>();
		JSONObject obj = new JSONObject(FileDealer.read("/versions.json")).getJSONObject("gradle");
		obj.keySet().forEach(key -> {
			installable.put(key, obj.getString(key));
		});
		if (!root.exists() || !root.isDirectory())
			root.mkdir();
	}

	public static Installed managedUi(Window win, String version, Runnable onChange) {
		Installed found = managedCache.get(version);

		if (found == null) {
			found = new Installed(win, version, dirForVer(version), onChange);
			final Installed ffound = found;
			found.setOnRemove(() -> {
				JwinActions.deleteDir(ffound.getTargetDir());
			});
			managedCache.put(version, found);
		}

		return found;
	}

	public static Installed localUi(Window win, LocalInstall ver, Runnable onChange) {
		Installed found = localCache.get(ver.getRoot());

		if (found == null) {
			found = new Installed(win, ver.getVersion(), ver.getRoot(), onChange);
			found.setOnRemove(() -> {
				LocalStore.removeGradleInst(ver.getRoot().getAbsolutePath());
			});
			localCache.put(ver.getRoot(), found);
		}

		return found;
	}

	public static List<String> installableVersions() {
		ArrayList<String> res = new ArrayList<>();
		installable.keySet().forEach(version -> {
			if (!dirForVer(version).exists()) {
				res.add(version);
			}
		});
		res.sort(COMPARATOR);
		return res;
	}

	public static DownloadJob install(Window win, String version) {
		if (downloadJobs.containsKey(version))
			return null;

		DownloadJob job = new DownloadJob(win, version, downloadUrlForVer(version), dirForVer(version), f -> versionFromDir(f).getRoot());

		job.addOnStateChanged(s -> {
			if (s == DownloadState.DONE || s == DownloadState.CANCELED || s == DownloadState.FAILED) {
				downloadJobs.remove(version);
			}
		});

		job.start();

		downloadJobs.put(version, job);

		return job;
	}

	public static boolean isValid(String version) {
		List<String> managedPaths = managedInstalls().stream().map(File::getAbsolutePath).collect(Collectors.toList());
		return (LocalStore.gradleAdded().contains(version) || managedPaths.contains(version)) && versionOf(version) != null && new File(version).exists();
	}

	public static List<DownloadJob> downloadJobs() {
		return new ArrayList<>(downloadJobs.values());
	}

	public static String downloadUrlForVer(String version) {
		return installable.get(version);
	}

	public static File dirForVer(String version) {
		return new File(root.getAbsolutePath() + "\\" + version);
	}

	public static List<File> managedInstalls() {
		ArrayList<File> res = new ArrayList<File>();

		for (File sub : root.listFiles()) {
			res.add(sub);

			if (!versionCache.containsKey(sub.getAbsolutePath()))
				versionCache.put(sub.getAbsolutePath(), versionFromDir(sub).getVersion());
		}

		res.sort(FCOMPARATOR);

		return res;
	}

	public static List<LocalInstall> localInstalls() {
		List<String> paths = LocalStore.gradleAdded();

		ArrayList<LocalInstall> res = new ArrayList<>();

		paths.forEach(p -> {
			File r = new File(p);
			if (r.exists() && r.isDirectory()) {
				LocalInstall version = versionFromDir(r);

				if (!versionCache.containsKey(version.getRoot().getAbsolutePath()))
					versionCache.put(version.getRoot().getAbsolutePath(), version.getVersion());

				if (version != null) {
					res.add(version);
				}
			}
		});

		return res;
	}

	public static void addLocal(String absolutePath) {
		LocalInstall inst = versionFromDir(new File(absolutePath));
		if (inst != null) {
			LocalStore.addGradleInst(inst.getRoot().getAbsolutePath());
		}
	}

	public static String versionOf(String path) {
		return versionCache.get(path);
	}

	public static LocalInstall versionFromDir(File file) {
		if (file.listFiles() == null) {
			return null;
		}
		for (File sf : file.listFiles()) {
			if (sf.isDirectory()) {
				LocalInstall version = versionFromDir(sf);
				if (version != null) {
					return version;
				}
			} else if (sf.getName().startsWith("gradle-launcher-") && sf.getName().endsWith(".jar")) {
				File lib = sf.getParentFile();
				File root = lib.getParentFile();

				String version = "gradle_"
						+ sf.getName().replace("gradle-launcher-", "").replace(".jar", "").toLowerCase();

				return new LocalInstall(root, version);
			}
		}

		return null;
	}

	public static final Comparator<String> COMPARATOR = (v1, v2) -> {
		String[] iv1 = v1.split("_")[1].split(" ")[0].split("\\.");
		String[] iv2 = v2.split("_")[1].split(" ")[0].split("\\.");
		int it1 = Integer.parseInt(iv1[0]);
		int it2 = Integer.parseInt(iv2[0]);
		int dc1 = Integer.parseInt(iv1[1]);
		int dc2 = Integer.parseInt(iv2[1]);
		int itc = -Integer.compare(it1, it2);
		int dcc = -Integer.compare(dc1, dc2);
		return itc == 0 ? dcc : itc;
	};

	public static final Comparator<File> FCOMPARATOR = (v1, v2) -> COMPARATOR.compare(v1.getName(), v2.getName());
}
