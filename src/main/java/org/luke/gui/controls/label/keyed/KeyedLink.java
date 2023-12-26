package org.luke.gui.controls.label.keyed;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.unkeyed.Link;
import org.luke.gui.window.Window;

/**
 * The {@code KeyedLink} class represents a clickable link with an associated
 * key for localization. It extends {@link Link} and implements the
 * {@link KeyedTextNode} interface.
 * 
 * @author SDIDSA
 */
public class KeyedLink extends Link implements KeyedTextNode {

	/**
	 * Constructs a keyed link with the specified window, localization key, and
	 * font.
	 * 
	 * @param window The window associated with the link.
	 * @param key    The localization key for the link text.
	 * @param font   The font to use for the link.
	 */
	public KeyedLink(Window window, String key, Font font) {
		super(window, key, font, true);
	}

	/**
	 * Constructs a keyed link with the specified window and localization key, using
	 * the default font.
	 * 
	 * @param window The window associated with the link.
	 * @param key    The localization key for the link text.
	 */
	public KeyedLink(Window window, String key) {
		this(window, key, Font.DEFAULT);
	}

	@Override
	public void setKey(String key) {
		if (label instanceof KeyedTextNode keyed)
			keyed.setKey(key);
	}
}
