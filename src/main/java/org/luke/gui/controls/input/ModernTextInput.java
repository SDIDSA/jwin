package org.luke.gui.controls.input;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.input.styles.ModernInputStyle;
import org.luke.gui.window.Window;

/**
 * A modern style text input control.
 * This class extends the {@link TextInput} class and uses the {@link ModernInputStyle}.
 *
 * @author SDIDSA
 */
public class ModernTextInput extends TextInput {

    /**
     * Creates a new instance of {@code ModernTextInput}.
     *
     * @param window The parent {@link Window}.
     * @param font   The {@link Font} for the text input.
     * @param key    The key associated with the text input.
     * @param hidden Determines if the text input is hidden (e.g., for password fields).
     */
    public ModernTextInput(Window window, Font font, String key, boolean hidden) {
        super(window, font, key, hidden);

        inputStyle = new ModernInputStyle(this);

        applyStyle(window.getStyl());
    }
}
