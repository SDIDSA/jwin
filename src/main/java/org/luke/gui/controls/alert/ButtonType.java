package org.luke.gui.controls.alert;

import javafx.scene.paint.Color;

/**
 * different types of buttons commonly
 * used in alert overlays. Each enum constant defines a key, filled status, and
 * optional fill color for the corresponding button type.<br><br>
 *
 * Enum Constants:<br><br> - CLOSE: Close button with default styling.<br>
 * - DONE: Done button with default styling.<br>
 * - CANCEL: Cancel button with default styling.<br>
 * - DELETE: Delete button with default styling.<br>
 * - USE_DEFAULT: Use default button with default styling.<br>
 * - SELECT_NOW: Select now button with default styling.<br>
 * - YES: Yes button with default styling.<br>
 * - NO: No button with default styling.<br>
 * - OK: OK button with default styling.<br>
 * - VIEW_LOG: View log button with default styling.<br>
 * - IGNORE: Ignore button with default styling.<br>
 *
 * @author SDIDSA
 */
public enum ButtonType {
	CLOSE("close"), DONE("done"), CANCEL("cancel", false), DELETE("delete"), USE_DEFAULT("use_default", true),
	SELECT_NOW("select_now", true), YES("yes"), NO("no"), OK("ok", true), VIEW_LOG("view_full_error_log"),
	IGNORE("ignore", true),
	SKIP("skip", true);

	private final String key;
	private final boolean filled;
	private final Color fill;

	/**
	 * Constructs a ButtonType with the specified key, fill color, and filled
	 * status.
	 *
	 * @param key    The key (text) associated with the button type.
	 * @param fill   The optional fill color of the button.
	 * @param filled The filled status of the button.
	 */
    ButtonType(String key, Color fill, boolean filled) {
		this.key = key;
		this.fill = fill;
		this.filled = filled;
	}

	/**
	 * Constructs a filled ButtonType with the specified key.
	 *
	 * @param key The key (text) associated with the button type.
	 */
    ButtonType(String key) {
		this(key, null, true);
	}

	/**
	 * Constructs a ButtonType with the specified key and filled status.
	 *
	 * @param key    The key (text) associated with the button type.
	 * @param filled The filled status of the button.
	 */
    ButtonType(String key, boolean filled) {
		this(key, null, filled);
	}

	/**
	 * Constructs a ButtonType with the specified key and fill color.
	 *
	 * @param key  The key (text) associated with the button type.
	 * @param fill The optional fill color of the button.
	 */
    ButtonType(String key, Color fill) {
		this(key, fill, true);
	}

	/**
	 * Returns the key (text) associated with the button type.
	 *
	 * @return The key (text) associated with the button type.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Returns the filled status of the button.
	 *
	 * @return True if the button is filled, false otherwise.
	 */
	public boolean isFilled() {
		return filled;
	}

	/**
	 * Returns the optional fill color of the button.
	 *
	 * @return The optional fill color of the button.
	 */
	public Color getFill() {
		return fill;
	}
}
