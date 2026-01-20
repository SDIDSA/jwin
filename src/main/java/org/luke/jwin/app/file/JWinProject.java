package org.luke.jwin.app.file;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.luke.gui.exception.ErrorHandler;
import org.luke.jwin.app.layout.JwinUi;
import org.w3c.dom.*;
import javax.xml.parsers.*;

public class JWinProject {
	private static final String PROJECT_ROOT = "root";
	private static final String CLASSPATH = "classpath";
	private static final String ROOT_FILES_INCLUDE = "rootFilesInclude";
	private static final String ROOT_FILES_EXCLUDE = "rootFilesExclude";
	private static final String ROOT_FILES_RUN = "rootFileRun";
	private static final String MAIN_CLASS = "mainClass";
	private static final String JDK = "jdk";
	private static final String JRE = "jre";
	private static final String ICON = "icon";
	private static final String MANUAL_JARS = "manualJars";
	private static final String APP_NAME = "appName";
	private static final String APP_VERSION = "appVersion";
	private static final String APP_PUBLISHER = "appPublisher";
	private static final String CONSOLE = "console";
	private static final String ADMIN = "admin";
	private static final String GUID = "guid";
	private static final String FILE_TYPE_ASSO = "fileTypeAsso";
	private static final String URL_PROTOCOL_ASSO = "urlProtocolAsso";

	private static final String CLASS_NAME = "className";
	private static final String FILE_PATH = "filePath";

	private static final String JVM_ARGS = "jvmArgs";

	private File root;

	private final ArrayList<File> classpath;

	private final ArrayList<File> rootFilesInclude;
	private final ArrayList<File> rootFilesExclude;
	private final ArrayList<File> rootFilesRun;

	private final Entry<String, File> mainClass;

	private final File jdk;
	private final File jre;

	private final File icon;

	private final ArrayList<File> manualJars;

	private final String appName;
	private final String appVersion;
	private final String appPublisher;

	private final Boolean console;
	private final Boolean admin;

	private final String guid;

	private final FileTypeAssociation fileTypeAsso;
	private final UrlProtocolAssociation urlProtocolAsso;
	private final String jvmArgs;

	private JWinProject(File root, List<File> classpath, List<File> rootFilesInclude, List<File> rootFilesExclude, List<File> rootFilesRun, Entry<String, File> mainClass, File jdk, File jre, File icon,
						List<File> manualJars, String appName, String appVersion, String appPublisher, Boolean console,
						Boolean admin, String guid, FileTypeAssociation fileTypeAsso, UrlProtocolAssociation urlProtocolAsso, String jvmArgs) {
		this.root = root;
		this.classpath = new ArrayList<>(classpath);
		this.rootFilesInclude = new ArrayList<>(rootFilesInclude);
		this.rootFilesExclude = new ArrayList<>(rootFilesExclude);
		this.rootFilesRun = new ArrayList<>(rootFilesRun);
		this.mainClass = mainClass;
		this.jdk = jdk;
		this.jre = jre;
		this.icon = icon;
		this.manualJars = new ArrayList<>(manualJars);
		this.appName = appName;
		this.appVersion = appVersion;
		this.appPublisher = appPublisher;
		this.console = console;
		this.admin = admin;
		this.guid = guid;
		this.fileTypeAsso = fileTypeAsso;
		this.urlProtocolAsso = urlProtocolAsso;
		this.jvmArgs = jvmArgs;
	}

	public JWinProject(JwinUi config) {
		this.root = config.getClasspath().getRoot();
		this.classpath = new ArrayList<>(config.getClasspath().getFiles());
		this.rootFilesInclude = new ArrayList<>(config.getRootFiles().getInclude());
		this.rootFilesExclude = new ArrayList<>(config.getRootFiles().getExclude());
		this.rootFilesRun = new ArrayList<>(config.getRootFiles().getRun());
		this.mainClass = config.getMainClass().getValue();
		this.jdk = config.getJdk().getValue();
		this.jre = config.getJre().getValue();
		this.icon = config.getIcon().getValue();
		this.manualJars = new ArrayList<>(config.getDependencies().getManualJars());
		this.appName = config.getAppName().getValue();
		this.appVersion = config.getVersion().getValue();
		this.appPublisher = config.getPublisher().getValue();
		this.console = config.getConsole().isUnset() ? null : config.getConsole().get();
		this.admin = config.getAdmin().get();
		this.guid = config.getGuid().getValue();
		this.fileTypeAsso = config.getMoreSettings().getFileTypeAssociation();
		this.urlProtocolAsso = config.getMoreSettings().getUrlProtocolAssociation();
		this.jvmArgs = config.getJre().getJvmArgs();
    }

	public File getRoot() {
		return root;
	}

	public void setRoot(File root) {
		this.root = root;
	}

	public UrlProtocolAssociation getUrlProtocolAsso() {
		return urlProtocolAsso;
	}

	public FileTypeAssociation getFileTypeAsso() {
		return fileTypeAsso;
	}

	public List<File> getClasspath() {
		return classpath;
	}

	public List<File> getRootFilesInclude() {
		return rootFilesInclude;
	}

	public List<File> getRootFilesExclude() {
		return rootFilesExclude;
	}

	public List<File> getRootFilesRun() {
		return rootFilesRun;
	}

	public Entry<String, File> getMainClass() {
		return mainClass;
	}

	public File getJdk() {
		return jdk;
	}

	public File getJre() {
		return jre;
	}

	public File getIcon() {
		return icon;
	}

	public List<File> getManualJars() {
		return manualJars;
	}

	public String getAppName() {
		return appName;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public String getAppPublisher() {
		return appPublisher;
	}

	public Boolean isConsole() {
		return console;
	}

	public Boolean isAdmin() {
		return admin;
	}

	public String getGuid() {
		return guid;
	}

	public String getJvmArgs() {
		return jvmArgs;
	}

	public List<String> compare(JWinProject other) {
		ArrayList<String> res = new ArrayList<>();

		for (Field field : other.getClass().getDeclaredFields()) {
			try {
				Object o1 = getClass().getDeclaredField(field.getName()).get(this);
				Object o2 = field.get(other);

				if (!compare(o1, o2)) {
					res.add(field.getName());
				}
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				ErrorHandler.handle(e, "compare projects for diffs");
			}
		}

		return res;
	}

	private Boolean compare(Object o1, Object o2) {
		if (o1 instanceof List<?> list1 && o2 instanceof List<?> list2) {
			return list1.containsAll(list2) && list2.containsAll(list1);
		}
		return (o1 == null && o2 == null) || (o1 != null && o1.equals(o2));
	}

	public String serialize() {
		JSONObject data = new JSONObject();
		JSONObject mc = new JSONObject();
		mc.put(CLASS_NAME, mainClass == null ? "" : mainClass.getKey());
		mc.put(FILE_PATH, mainClass == null ? "" : mainClass.getValue());
		data.put(PROJECT_ROOT, root);
		data.put(CLASSPATH, serializeFileList(classpath));
		data.put(ROOT_FILES_INCLUDE, serializeFileList(rootFilesInclude));
		data.put(ROOT_FILES_EXCLUDE, serializeFileList(rootFilesExclude));
		data.put(ROOT_FILES_RUN, serializeFileList(rootFilesRun));
		data.put(MAIN_CLASS, mc);
		data.put(JDK, jdk == null ? "" : jdk.getAbsolutePath());
		data.put(JRE, jre == null ? "" : jre.getAbsolutePath());
		data.put(ICON, icon == null ? "" : icon.getAbsolutePath());
		data.put(MANUAL_JARS, serializeFileList(manualJars));
		data.put(APP_NAME, appName);
		data.put(APP_VERSION, appVersion);
		data.put(APP_PUBLISHER, appPublisher);
		if(console != null) {
			data.put(CONSOLE, console);
		}
		data.put(ADMIN, admin);
		data.put(GUID, guid);

		if (fileTypeAsso != null) {
			data.put(FILE_TYPE_ASSO, fileTypeAsso.serialize());
		}

		if (urlProtocolAsso != null) {
			data.put(URL_PROTOCOL_ASSO, urlProtocolAsso.serialize());
		}

		data.put(JVM_ARGS, jvmArgs);

		return data.toString(4);
	}

	public static JWinProject deserialize(String data) {
		JSONObject obj = new JSONObject(data);
		JSONObject mc = obj.getJSONObject(MAIN_CLASS);

		String root = obj.isNull(PROJECT_ROOT) ? null : obj.getString(PROJECT_ROOT);

		return new JWinProject(root == null ? null : new File(root),
				deserializeFileList(obj.getJSONArray(CLASSPATH)),
				deserializeFileList(obj, ROOT_FILES_INCLUDE),
				deserializeFileList(obj, ROOT_FILES_EXCLUDE),
				deserializeFileList(obj, ROOT_FILES_RUN),
				Map.entry(mc.getString(CLASS_NAME), new File(mc.getString(FILE_PATH))),
				new File(obj.getString(JDK)),
				new File(obj.getString(JRE)),
				new File(obj.getString(ICON)),
				deserializeFileList(obj.getJSONArray(MANUAL_JARS)),
				obj.getString(APP_NAME),
				obj.getString(APP_VERSION),
				obj.getString(APP_PUBLISHER),
				obj.isNull(CONSOLE) ? null : obj.getBoolean(CONSOLE),
                obj.has(ADMIN) && obj.getBoolean(ADMIN),
				obj.optString(GUID, ""),
				obj.has(FILE_TYPE_ASSO) ? FileTypeAssociation.deserialize(obj.getJSONObject(FILE_TYPE_ASSO)) : null,
				obj.has(URL_PROTOCOL_ASSO) ? UrlProtocolAssociation.deserialize(obj.getJSONObject(URL_PROTOCOL_ASSO))
						: null,
				obj.has(JVM_ARGS) ? obj.getString(JVM_ARGS) : "");
	}

	public static JWinProject fromJavaProject(File root) {
		ArrayList<File> classpath = new ArrayList<>();
		Entry<String, File> mainClass = null;

		File jdk = null;
		File jre = null;

		File icon = null;

		ArrayList<File> manualJars = new ArrayList<>();

		String appName = "";
		String appVersion = "";
		String appPublisher = "";

		Boolean console = null;
		Boolean admin = null;

		String guid = null;

		FileTypeAssociation fileTypeAsso = null;
		UrlProtocolAssociation urlProtocolAsso = null;

		File cpFile = new File(root + "\\.classpath");
		if (cpFile.exists()) {
			classpath.addAll(loadEclipseClasspath(root, cpFile));
		}

		File dProject = new File(root + "\\.project");
		if (dProject.exists()) {
			String ln = loadEclipseProjectName(dProject);
			if(ln != null) {
				appName = ln;
			}
		}

		File pom = new File(root + "\\pom.xml");
		if (pom.exists()) {
			loadMavenClasspath(root, classpath);

			String[] mavenProps = loadMavenProps(pom);
			appName = mavenProps[0] != null ? mavenProps[0] : appName;
			appVersion = mavenProps[1] != null ? mavenProps[1] : appVersion;
			appPublisher = mavenProps[2] != null ? mavenProps[2] : appPublisher;
		}

		File grad1 = new File(root + "\\build.gradle");
		File grad2 = new File(root + "\\build.gradle.kts");
		
		if(grad1.exists() || grad2.exists()) {
			File jav = new File(root + "\\src\\main\\java");
			File res = new File(root + "\\src\\main\\resources");

			if (jav.exists() && jav.isDirectory() && !classpath.contains(jav)) {
				classpath.add(jav);
			}
			if (res.exists() && res.isDirectory() && !classpath.contains(res)) {
				classpath.add(res);
			}
			
			appName = root.getName();
		}

		//TODO maybe load jvmArgs
		String jvmArgs = "";

		return new JWinProject(root, classpath, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), mainClass, jdk, jre, icon, manualJars, appName, appVersion,
				appPublisher, console, admin, guid, fileTypeAsso, urlProtocolAsso, "");
	}

	private static List<File> loadEclipseClasspath(File root, File cpFile) {
		ArrayList<File> res = new ArrayList<>();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document doc = builder.parse(cpFile);

			Element cpDoc = doc.getDocumentElement();

			NodeList entries = cpDoc.getElementsByTagName("classpathentry");

			for (int i = 0; i < entries.getLength(); i++) {
				Element entry = (Element) entries.item(i);

				if (entry.getAttribute("kind").equals("src") && !entry.getAttribute("output").contains("test")) {
					String path = entry.getAttribute("path");
					File pathFile = new File(root + "\\" + path);
					if (pathFile.exists()) {
						res.add(pathFile);
					}
				}
			}
		} catch (Exception x) {
			ErrorHandler.handle(x, "parse project for class path");
		}
		return res;
	}

	private static String loadEclipseProjectName(File dProject) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document doc = builder.parse(dProject);

			Element cpDoc = doc.getDocumentElement();

			Element nameEl = (Element) cpDoc.getElementsByTagName("name").item(0);

			return nameEl.getTextContent();
		} catch (Exception x) {
			ErrorHandler.handle(x, "parse project for app name");
		}
		return null;
	}

	private static void loadMavenClasspath(File root, List<File> classpath) {
		File jav = new File(root + "\\src\\main\\java");
		File res = new File(root + "\\src\\main\\resources");
		if (jav.exists() && jav.isDirectory() && !classpath.contains(jav)) {
			classpath.add(jav);
		}
		if (res.exists() && res.isDirectory() && !classpath.contains(res)) {
			classpath.add(res);
		}
	}

	private static String[] loadMavenProps(File pom) {
		String[] res = new String[] {null, null, null};
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document doc = builder.parse(pom);

			Element proj = ((Element) doc.getElementsByTagName("project").item(0));

			NodeList artifactIds = proj.getElementsByTagName("artifactId");
			for(int i = 0; i < artifactIds.getLength(); i++) {
				Node n = artifactIds.item(i);
				Node p = n.getParentNode();
				if(p instanceof Element e && e.getTagName().equals("project")) {
					res[0] = n.getTextContent();
				}
			}

			NodeList versions = proj.getElementsByTagName("version");
			for(int i = 0; i < versions.getLength(); i++) {
				Node n = versions.item(i);
				Node p = n.getParentNode();
				if(p instanceof Element e && e.getTagName().equals("project")) {
					res[1] = n.getTextContent();
				}
			}

			NodeList groupIds = proj.getElementsByTagName("groupId");
			for(int i = 0; i < groupIds.getLength(); i++) {
				Node n = groupIds.item(i);
				Node p = n.getParentNode();
				if(p instanceof Element e && e.getTagName().equals("project")) {
					res[2] = n.getTextContent();
				}
			}

		} catch (Exception x) {
			ErrorHandler.handle(x, "parsing project for classpath, version and group id");
		}
		return res;
	}

	private static JSONArray serializeFileList(List<File> list) {
		JSONArray res = new JSONArray();
		list.forEach(file -> res.put(file.getAbsolutePath()));
		return res;
	}

	private static List<File> deserializeFileList(JSONArray arr) {
		ArrayList<File> res = new ArrayList<>();
		arr.forEach(obj -> res.add(new File((String) obj)));
		return res;
	}

	private static List<File> deserializeFileList(JSONObject obj, String name) {
		if(!obj.has(name)) {
			return new ArrayList<>();
		}
		ArrayList<File> res = new ArrayList<>();
		obj.getJSONArray(name).forEach(element -> res.add(new File((String) element)));
		return res;
	}
}
