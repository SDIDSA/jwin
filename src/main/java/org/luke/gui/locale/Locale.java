package org.luke.gui.locale;

import java.util.HashMap;
import org.json.JSONObject;
import org.luke.gui.exception.EntryNotFountException;
import org.luke.gui.exception.ErrorHandler;
import org.luke.gui.file.FileUtils;

public class Locale {
	public static final Locale EN_US = new Locale("en_US");
	public static final Locale FR_FR = new Locale("fr_FR");

	private String name;
	private HashMap<String, String> values;

	private Locale(String name) {
		this.name = name;
		values = new HashMap<>();
		String file = FileUtils.readFile("/locales/" + name + ".json");
		JSONObject obj = new JSONObject(file);

		for (String key : obj.keySet()) {
			values.put(key, obj.getString(key));
		}
	}

	public String get(String key) {
		String found = values.get(key);

		if (found == null) {
			ErrorHandler.handle(new EntryNotFountException(), "get value of [" + key + "] for locale [" + name + "]");
			found = key;
		}

		return found;
	}
}
