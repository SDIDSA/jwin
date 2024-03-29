package org.luke.jwin.app.file;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.luke.jwin.app.layout.JwinUi;
import org.w3c.dom.*;
import javax.xml.parsers.*;

public class JWinProject {
	private static final String PROJECT_ROOT = "root";
	private static final String CLASSPATH = "classpath";
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

	private File root;

	private ArrayList<File> classpath;
	private Entry<String, File> mainClass;

	private File jdk;
	private File jre;

	private File icon;

	private ArrayList<File> manualJars;

	private String appName;
	private String appVersion;
	private String appPublisher;

	private Boolean console;
	private Boolean admin;

	private String guid;

	private FileTypeAssociation fileTypeAsso;
	private UrlProtocolAssociation urlProtocolAsso;

	private JWinProject(File root, List<File> classpath, Entry<String, File> mainClass, File jdk, File jre, File icon,
			List<File> manualJars, String appName, String appVersion, String appPublisher, Boolean console,
			Boolean admin, String guid, FileTypeAssociation fileTypeAsso, UrlProtocolAssociation urlProtocolAsso) {
		this.root = root;
		this.classpath = new ArrayList<>(classpath);
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
	}

	public JWinProject(JwinUi config) {
		this.root = config.getClasspath().getRoot();
		this.classpath = new ArrayList<>(config.getClasspath().getFiles());
		this.mainClass = config.getMainClass().getValue();
		this.jdk = config.getJdk().getValue();
		this.jre = config.getJre().getValue();
		this.icon = config.getIcon().getValue();
		this.manualJars = new ArrayList<>(config.getDependencies().getManualJars());
		this.appName = config.getAppName().getValue();
		this.appVersion = config.getVersion().getValue();
		this.appPublisher = config.getPublisher().getValue();
		this.console = config.getConsole().checkedProperty().get();
		this.admin = config.getAdmin().checkedProperty().get();
		this.guid = config.getGuid().getValue();
		this.fileTypeAsso = config.getMoreSettings().getFileTypeAssociation();
		this.urlProtocolAsso = config.getMoreSettings().getUrlProtocolAssociation();
	}

	public File getRoot() {
		return root;
	}

	public void setRoot(File root) {
		this.root = root;
	}

	public void setUrlProtocolAsso(UrlProtocolAssociation urlProtocolAsso) {
		this.urlProtocolAsso = urlProtocolAsso;
	}

	public UrlProtocolAssociation getUrlProtocolAsso() {
		return urlProtocolAsso;
	}

	public void setFileTypeAsso(FileTypeAssociation fileTypeAsso) {
		this.fileTypeAsso = fileTypeAsso;
	}

	public FileTypeAssociation getFileTypeAsso() {
		return fileTypeAsso;
	}

	public List<File> getClasspath() {
		return classpath;
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
				e.printStackTrace();
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
		data.put(MAIN_CLASS, mc);
		data.put(JDK, jdk == null ? "" : jdk.getAbsolutePath());
		data.put(JRE, jre == null ? "" : jre.getAbsolutePath());
		data.put(ICON, icon == null ? "" : icon.getAbsolutePath());
		data.put(MANUAL_JARS, serializeFileList(manualJars));
		data.put(APP_NAME, appName);
		data.put(APP_VERSION, appVersion);
		data.put(APP_PUBLISHER, appPublisher);
		data.put(CONSOLE, console);
		data.put(ADMIN, admin);
		data.put(GUID, guid);

		if (fileTypeAsso != null) {
			data.put(FILE_TYPE_ASSO, fileTypeAsso.serialize());
		}

		if (urlProtocolAsso != null) {
			data.put(URL_PROTOCOL_ASSO, urlProtocolAsso.serialize());
		}

		return data.toString(4);
	}

	public static JWinProject deserialize(String data) {
		JSONObject obj = new JSONObject(data);
		JSONObject mc = obj.getJSONObject(MAIN_CLASS);

		String root = obj.isNull(PROJECT_ROOT) ? null : obj.getString(PROJECT_ROOT);

		return new JWinProject(root == null ? null : new File(root), deserializeFileList(obj.getJSONArray(CLASSPATH)),
				Map.entry(mc.getString(CLASS_NAME), new File(mc.getString(FILE_PATH))), new File(obj.getString(JDK)),
				new File(obj.getString(JRE)), new File(obj.getString(ICON)),
				deserializeFileList(obj.getJSONArray(MANUAL_JARS)), obj.getString(APP_NAME), obj.getString(APP_VERSION),
				obj.getString(APP_PUBLISHER), obj.getBoolean(CONSOLE), obj.has(ADMIN) ? obj.getBoolean(ADMIN) : false,
				obj.getString(GUID),
				obj.has(FILE_TYPE_ASSO) ? FileTypeAssociation.deserialize(obj.getJSONObject(FILE_TYPE_ASSO)) : null,
				obj.has(URL_PROTOCOL_ASSO) ? UrlProtocolAssociation.deserialize(obj.getJSONObject(URL_PROTOCOL_ASSO))
						: null);
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
							classpath.add(pathFile);
						}
					}
				}
			} catch (Exception x) {
				x.printStackTrace();
			}
		}

		File dProject = new File(root + "\\.project");
		if (dProject.exists()) {
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();

				Document doc = builder.parse(dProject);

				Element cpDoc = doc.getDocumentElement();

				Element nameEl = (Element) cpDoc.getElementsByTagName("name").item(0);

				appName = nameEl.getTextContent();
			} catch (Exception x) {
				x.printStackTrace();
			}
		}

		File pom = new File(root + "\\pom.xml");
		if (pom.exists()) {

			File jav = new File(root + "\\src\\main\\java");
			File res = new File(root + "\\src\\main\\resources");

			if (jav.exists() && jav.isDirectory() && !classpath.contains(jav)) {
				classpath.add(jav);
			}
			if (res.exists() && res.isDirectory() && !classpath.contains(res)) {
				classpath.add(res);
			}

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
						appName = n.getTextContent();
					}
				}

				NodeList versions = proj.getElementsByTagName("version");
				for(int i = 0; i < versions.getLength(); i++) {
					Node n = versions.item(i);
					Node p = n.getParentNode();
					if(p instanceof Element e && e.getTagName().equals("project")) {
						appVersion = n.getTextContent();
					}
				}

				NodeList groupIds = proj.getElementsByTagName("groupId");
				for(int i = 0; i < groupIds.getLength(); i++) {
					Node n = groupIds.item(i);
					Node p = n.getParentNode();
					if(p instanceof Element e && e.getTagName().equals("project")) {
						appPublisher = n.getTextContent();
					}
				}

			} catch (Exception x) {
				x.printStackTrace();
			}
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

		return new JWinProject(root, classpath, mainClass, jdk, jre, icon, manualJars, appName, appVersion,
				appPublisher, console, admin, guid, fileTypeAsso, urlProtocolAsso);
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
}
