package org.luke.gui.controls.popup.context.items;

import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.controls.popup.context.ContextMenu;

import javafx.scene.paint.Color;

/**
 * Custom implementation of a keyed menu item, extending {@link MenuItem}.
 * Represents a menu item associated with a key, typically used for localized
 * text. Manages the appearance, behavior, and interaction of the keyed menu
 * item.
 *
 * @author SDIDSA
 */
public class KeyedMenuItem extends MenuItem {

	/**
	 * Constructs a keyed menu item with the specified parent context menu, key, and
	 * fill color.
	 *
	 * @param menu The parent {@link ContextMenu} to which this menu item belongs.
	 * @param key  The key or text associated with the menu item.
	 * @param fill The fill color of the menu item.
	 */
	public KeyedMenuItem(ContextMenu menu, String key, Color fill) {
		super(menu, key, fill, true);
	}

	/**
	 * Constructs a keyed menu item with the specified parent context menu and key.
	 * Uses a default fill color for the menu item.
	 *
	 * @param menu The parent {@link ContextMenu} to which this menu item belongs.
	 * @param key  The key or text associated with the menu item.
	 */
	public KeyedMenuItem(ContextMenu menu, String key) {
		this(menu, key, null);
	}

	/**
	 * Gets the key associated with the menu item.
	 *
	 * @return The key associated with the menu item.
	 */
	public String getKey() {
		return ((Label) lab).getKey();
	}
}
