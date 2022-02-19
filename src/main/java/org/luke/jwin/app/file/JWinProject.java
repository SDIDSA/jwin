package org.luke.jwin.app.file;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

public class JWinProject {
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
	private static final String GUID = "guid";
	private static final String FILE_TYPE_ASSO = "fileTypeAsso";

	private static final String CLASS_NAME = "className";
	private static final String FILE_PATH = "filePath";

	private ArrayList<File> classpath;
	private Entry<String, File> mainClass;

	private File jdk;
	private File jre;

	private File icon;

	private ArrayList<File> manualJars;

	private String appName;
	private String appVersion;
	private String appPublisher;

	private boolean console;

	private String guid;
	
	private FileTypeAssociation fileTypeAsso;

	public JWinProject(List<File> classpath, Entry<String, File> mainClass, File jdk, File jre, File icon,
			List<File> manualJars, String appName, String appVersion, String appPublisher, boolean console,
			String guid, FileTypeAssociation fileTypeAsso) {
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
		this.guid = guid;
		this.fileTypeAsso = fileTypeAsso;
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

	public boolean isConsole() {
		return console;
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

	private boolean compare(Object o1, Object o2) {
		if (o1 instanceof List<?> list1 && o2 instanceof List<?> list2) {
			return list1.containsAll(list2) && list2.containsAll(list1);
		}
		return (o1 == null && o2 == null) || (o1 != null && o1.equals(o2));
	}

	public String serialize() {
		JSONObject data = new JSONObject();
		JSONObject mc = new JSONObject();
		mc.put(CLASS_NAME, mainClass.getKey());
		mc.put(FILE_PATH, mainClass.getValue());
		data.put(CLASSPATH, serializeFileList(classpath));
		data.put(MAIN_CLASS, mc);
		data.put(JDK, jdk.getAbsolutePath());
		data.put(JRE, jre.getAbsolutePath());
		data.put(ICON, icon.getAbsolutePath());
		data.put(MANUAL_JARS, serializeFileList(manualJars));
		data.put(APP_NAME, appName);
		data.put(APP_VERSION, appVersion);
		data.put(APP_PUBLISHER, appPublisher);
		data.put(CONSOLE, console);
		data.put(GUID, guid);

		if(fileTypeAsso != null) {
			data.put(FILE_TYPE_ASSO, fileTypeAsso.serialize());
		}
		
		return data.toString(4);
	}

	public static JWinProject deserialize(String data) {
		JSONObject obj = new JSONObject(data);
		JSONObject mc = obj.getJSONObject(MAIN_CLASS);
		return new JWinProject(deserializeFileList(obj.getJSONArray(CLASSPATH)),
				Map.entry(mc.getString(CLASS_NAME), new File(mc.getString(FILE_PATH))), new File(obj.getString(JDK)),
				new File(obj.getString(JRE)), new File(obj.getString(ICON)),
				deserializeFileList(obj.getJSONArray(MANUAL_JARS)), obj.getString(APP_NAME), obj.getString(APP_VERSION),
				obj.getString(APP_PUBLISHER), obj.getBoolean(CONSOLE), obj.getString(GUID),
				obj.has(FILE_TYPE_ASSO) ? FileTypeAssociation.deserialize(obj.getJSONObject(FILE_TYPE_ASSO)) : null);
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
