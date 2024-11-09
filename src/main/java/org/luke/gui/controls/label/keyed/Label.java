package org.luke.gui.controls.label.keyed;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.unkeyed.Text;
import org.luke.gui.locale.Locale;
import org.luke.gui.locale.Localized;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;

/**
 * The {@code Label} class extends the {@link Text} class and implements the
 * {@link Localized} and {@link KeyedTextNode} interfaces. It is designed for
 * displaying localized text with support for text keys associated with a
 * {@link Locale}.
 * 
 * @author SDIDSA
 */
public class Label extends Text implements Localized, KeyedTextNode {
	private Window window;
	private String key;

	/**
	 * Constructs a labeled text node with the specified window, localization key,
	 * and font.
	 * 
	 * @param window The window associated with the label.
	 * @param key    The localization key for the label text.
	 * @param font   The font to use for the label text.
	 */
	public Label(Window window, String key, Font font) {
		super(null, font);
		this.window = window;
		this.key = key;
		applyLocale(window.getLocale());
	}

	/**
	 * Constructs a labeled text node with the specified window and localization
	 * key, using the default font.
	 * 
	 * @param window The window associated with the label.
	 * @param key    The localization key for the label text.
	 */
	public Label(Window window, String key) {
		this(window, key, Font.DEFAULT);
	}

	/**
	 * Returns the localization key associated with this label.
	 * 
	 * @return The localization key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Sets the localization key for this label and updates the displayed text based
	 * on the current locale.
	 * 
	 * @param key The new localization key.
	 */
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
