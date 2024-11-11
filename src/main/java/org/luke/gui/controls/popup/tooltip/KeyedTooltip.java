package org.luke.gui.controls.popup.tooltip;

import org.luke.gui.controls.popup.Direction;
import org.luke.gui.locale.Locale;
import org.luke.gui.locale.Localized;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;

/**
 * Represents a tooltip with localized content using a key. Extends
 * {@link TextTooltip}.
 *
 * @author SDIDSA
 */
public class KeyedTooltip extends TextTooltip implements Localized {
	private String key;

	/**
	 * Constructs a {@code KeyedTooltip} with the specified window, key, direction,
	 * offsetX, and offsetY.
	 *
	 * @param window    The window associated with the tooltip.
	 * @param key       The key for localization.
	 * @param direction The direction in which the tooltip points.
	 * @param offsetX   The horizontal offset from the reference node.
	 * @param offsetY   The vertical offset from the reference node.
	 */
	public KeyedTooltip(Window window, String key, Direction direction, double offsetX, double offsetY) {
		super(window, "", direction, offsetX, offsetY);
		this.key = key;
		applyLocale(window.getLocale());
	}

	/**
	 * Constructs a {@code KeyedTooltip} with the specified window, key, and
	 * direction. Uses default values for offsetX and offsetY.
	 *
	 * @param window    The window associated with the tooltip.
	 * @param key       The key for localization.
	 * @param direction The direction in which the tooltip points.
	 */
	public KeyedTooltip(Window window, String key, Direction direction) {
		this(window, key, direction, 0, 0);
	}

	/**
	 * Sets the key for localization.
	 *
	 * @param key The key for localization.
	 */
	public void setKey(String key) {
		this.key = key;
		applyLocale(owner.getLocale().get());
	}

	/**
	 * Applies the specified {@link Locale} to update the tooltip content based on
	 * the key.
	 *
	 * @param locale The locale to apply.
	 */
	@Override
	public void applyLocale(Locale locale) {
		if (key != null && !key.isEmpty()) {
			setText(locale.get(key));
		} else {
			setText("");
		}
	}

	/**
	 * Applies the specified {@link ObjectProperty<Locale>} to bind the tooltip's
	 * locale to the property.
	 *
	 * @param locale The locale property to bind.
	 */
	@Override
	public void applyLocale(ObjectProperty<Locale> locale) {
		Localized.bindLocale(this, locale);
	}
}
