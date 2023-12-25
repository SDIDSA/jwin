package org.luke.gui.controls.popup.tooltip;

import org.luke.gui.controls.popup.Direction;
import org.luke.gui.locale.Locale;
import org.luke.gui.locale.Localized;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;

public class KeyedTooltip extends TextTooltip implements Localized {
	private String key;

	public KeyedTooltip(Window window, String key, Direction direction, double offsetX, double offsetY) {
		super(window, "", direction, offsetX, offsetY);
		this.key = key;		
		applyLocale(window.getLocale());
	}

	public KeyedTooltip(Window window, String key, Direction direction) {
		this(window, key, direction, 0, 0);
	}

	public void setKey(String key) {
		this.key = key;
		
		applyLocale(owner.getLocale().get());
	}

	@Override
	public void applyLocale(Locale locale) {
		if (key != null && !key.isEmpty()) {
			setText(locale.get(key));
		} else {
			setText("");
		}
	}

	@Override
	public void applyLocale(ObjectProperty<Locale> locale) {
		Localized.bindLocale(this, locale);
	}

}
