package org.luke.gui.controls.label.keyed;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.unkeyed.Text;
import org.luke.gui.locale.Locale;
import org.luke.gui.locale.Localized;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;

public class Label extends Text implements Localized, KeyedTextNode {
	private Window window;
	private String key;

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
		applyLocale(window.getLocale().get());
	}

	@Override
	public void applyLocale(Locale locale) {
		if (key != null && !key.isEmpty()) {
			set(locale.get(key));
		} else {
			setText("");
		}
	}

	@Override
	public void applyLocale(ObjectProperty<Locale> locale) {
		Localized.bindLocale(this, locale);
	}
}
