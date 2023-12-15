package org.luke.gui.controls.label.keyed;

import java.util.ArrayList;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.unkeyed.Text;
import org.luke.gui.locale.Locale;
import org.luke.gui.locale.Localized;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;

public class Label extends Text implements Localized, KeyedTextNode {
	private Window window;
	private String key;
	private ArrayList<String> params = new ArrayList<>();

	public Label(Window window, String key, Font font) {
		super(null, font);
		this.window = window;
		this.key = key;
		applyLocale(window.getLocale());
	}

	public Label(Window window, String key) {
		this(window, key, Font.DEFAULT);
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
		applyLocale(window.getLocale());
	}

	public void addParam(int i, String param) {
		if (i >= params.size()) {
			params.add(param);
		} else {
			params.set(i, param);
		}
		applyLocale(window.getLocale());
	}

	@Override
	public void applyLocale(Locale locale) {
		if (key != null && !key.isEmpty()) {
			String val = locale.get(key);
			for (int i = 0; i < params.size(); i++) {
				String param = params.get(i);
				param = (param.charAt(0) == '&' && param.length() > 1) ? locale.get(param.substring(1)) : param;
				val = val.replace("{$" + i + "}", param);
			}
			set(val);
		} else {
			setText("");
		}
	}
	
	@Override
	public void applyLocale(ObjectProperty<Locale> locale) {
		Localized.bindLocale(this, locale);
	}
}
