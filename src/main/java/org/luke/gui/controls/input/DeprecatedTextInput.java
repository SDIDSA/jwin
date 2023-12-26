package org.luke.gui.controls.input;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.input.styles.DeprecatedInputStyle;
import org.luke.gui.window.Window;

/**
 * An implementation of {@link TextInput} with a {@link DeprecatedInputStyle}.
 *
 * @author SDIDSA
 */
public class DeprecatedTextInput extends TextInput {
	/**
     * Creates a new instance of {@code DeprecatedTextInput}.
     *
     * @param window The parent {@link Window}.
     * @param font   The {@link Font} to be applied to the text input.
     * @param key    The key associated with the text input.
     * @param hidden Whether the input should be initially hidden (e.g., for password fields).
     */
	public DeprecatedTextInput(Window window, Font font, String key, boolean hidden) {
		super(window, font, key, hidden);

		inputStyle = new DeprecatedInputStyle(this);

		applyStyle(window.getStyl());
	}

	/**
	 * Creates a new instance of {@code DeprecatedTextInput}.
	 *
	 * @param window The parent {@link Window}.
	 * @param f      The {@link Font} to be applied to the text input.
	 * @param key    The key associated with the text input.
	 */
	public DeprecatedTextInput(Window window, Font f, String key) {
		this(window, f, key, false);
	}
}
