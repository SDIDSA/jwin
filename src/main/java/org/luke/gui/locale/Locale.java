package org.luke.gui.locale;

import java.util.HashMap;
import java.util.Map;

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
	
	private static final String BIG_SEPARATOR = "-key-";
	private static final String SMALL_SEPARATOR = "-val-";

	public synchronized String get(String key) {
		String[] parts = key.split(BIG_SEPARATOR);

		HashMap<String, String> params = new HashMap<>();
		for (int i = 1; i < parts.length; i++) {
			String[] pars = parts[i].split(SMALL_SEPARATOR);

			params.put(pars[0], pars[1]);
		}

		if (!params.isEmpty()) {
			key = parts[0];
		}

		String found = values.get(key);

		if (found == null) {
			ErrorHandler.handle(new EntryNotFountException(), "get value of [" + key + "] for locale [" + name + "]");
			found = key;
		}

		for (Map.Entry<String, String> param : params.entrySet()) {
			found = found.replace("<" + param.getKey() + ">", param.getValue());
		}
		return found;
	}

	public static String key(String base, String... params) {
		if (params.length % 2 != 0) {
			throw new IllegalArgumentException(
					"length of params must be even, so it can be used as pairs of key-value");
		}

		StringBuilder res = new StringBuilder(base);

		for (int i = 0; i < params.length; i += 2) {
			res.append(BIG_SEPARATOR).append(params[i]).append(SMALL_SEPARATOR).append(params[i + 1]);
		}

		return res.toString();
	}
	
	public static String key(String base, String k1, int v1) {
		return key(base, k1, String.valueOf(v1));
	}

	public static String key(String base, String k1, String v1, String k2,
			int v2) {
		return key(base, k1, v1, k2, String.valueOf(v2));
	}
}
