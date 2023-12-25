package org.luke.jwin.local.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
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

public class JdkManager {

	private static HashMap<String, String> versionCache = new HashMap<>();

	private static HashMap<String, String> installable;

	private static HashMap<String, Installed> managedCache = new HashMap<>();

	private static HashMap<File, Installed> localCache = new HashMap<>();

	private static final File root = new File(System.getenv("appData") + "\\jwin\\jdk");

	private static HashMap<String, DownloadJob> downloadJobs = new HashMap<>();

	static {
		installable = new HashMap<>();

		JSONObject obj = new JSONObject(FileDealer.read("/versions.json")).getJSONObject("jdk");

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
				LocalStore.removeJdkInst(ver.getRoot().getAbsolutePath());
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
		DownloadJob job = new DownloadJob(win, version, downloadUrlForVer(version), dirForVer(version),
				f -> versionFromDir(f).getRoot());
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
		return (LocalStore.jdkAdded().contains(version) || managedPaths.contains(version)) && versionOf(version) != null
				&& new File(version).exists();
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

			String vers = versionOf(sub.getAbsolutePath());
			if (vers == null) {
				versionCache.put(sub.getAbsolutePath(), versionFromDir(sub).getVersion());
			}
		}

		res.sort(FCOMPARATOR);
		return res;
	}

	public static List<LocalInstall> localInstalls() {
		List<String> paths = LocalStore.jdkAdded();

		ArrayList<LocalInstall> res = new ArrayList<>();

		paths.forEach(p -> {
			File r = new File(p);
			if (r.exists() && r.isDirectory()) {
				LocalInstall inst = null;
				String vers = versionOf(r.getAbsolutePath());
				if (vers == null) {
					inst = versionFromDir(r);
					vers = inst.getVersion();
					versionCache.put(inst.getRoot().getAbsolutePath(), inst.getVersion());
				} else {
					inst = new LocalInstall(r, vers);
				}

				if (inst != null) {
					res.add(inst);
				}
			}
		});

		return res;
	}

	public static void addLocal(String absolutePath) {
		LocalInstall inst = versionFromDir(new File(absolutePath));
		if (inst != null) {
			LocalStore.addJdkInst(inst.getRoot().getAbsolutePath());
		}
	}

	public static String versionOf(String path) {
		return versionCache.get(path);
	}

	public static List<LocalInstall> allInstalls() {
		ArrayList<LocalInstall> inst = new ArrayList<LocalInstall>();

		managedInstalls().forEach(f -> inst.add(versionFromDir(f)));
		localInstalls().forEach(inst::add);

		inst.sort((i1, i2) -> {
			return compareVersions(i1.getVersion(), i2.getVersion());
		});

		return inst;
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
			} else if (sf.getName().equals("release")) {
				try {
					HashMap<String, String> data = parseInputStream(new FileInputStream(sf));

					File root = sf.getParentFile();

					String jver = data.get("JAVA_VERSION").replace("\"", "").split("_")[0];
					String arch = data.get("OS_ARCH").replace("\"", "");
					String version = "jdk_" + jver + " " + arch;

					return new LocalInstall(root, version);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	private static HashMap<String, String> parseInputStream(InputStream is) throws IOException {
		HashMap<String, String> data = new HashMap<>();

		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;

		while ((line = br.readLine()) != null) {
			String[] parts = line.split("=");
			data.put(parts[0], parts[1]);
		}

		is.close();
		br.close();

		return data;
	}

	public static final int majorVer(String ver) {
		return tokenize(ver)[0];
	}

	public static final Comparator<String> COMPARATOR = (v1, v2) -> {
		return compareVersions(v1, v2);
	};

	public static int compareVersions(String v1, String v2) {
		return -compare(tokenize(v1), tokenize(v2));
	}

	private static int[] tokenize(String ver) {
		int[] tokens = Arrays.stream(ver.split("_")[1].split(" ")[0].split("\\.")).mapToInt(Integer::parseInt)
				.toArray();
		return tokens;
	}

	private static int compare(int[] v1, int[] v2) {
		for (int p = 0; p < v1.length; p++) {
			int t1 = v1[p];
			int t2 = v2[p];

			int comp = Integer.compare(t1, t2);
			if (comp != 0)
				return comp;
		}

		return v1.length > v2.length ? 1 : v2.length > v1.length ? -1 : 0;
	}

	public static final Comparator<File> FCOMPARATOR = (v1, v2) -> {
		return compareVersions(versionOf(v1.getAbsolutePath()), versionOf(v2.getAbsolutePath()));
	};
}
