package org.luke.gui.controls.input;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.input.styles.ModernInputStyle;
import org.luke.gui.window.Window;

public class ModernTextInput extends TextInput {

	public ModernTextInput(Window window, Font font, String key, boolean hidden) {
		super(window, font, key, hidden);

		inputStyle = new ModernInputStyle(this);

		applyStyle(window.getStyl());
	}
}
