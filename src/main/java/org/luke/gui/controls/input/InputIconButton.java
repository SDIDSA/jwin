package org.luke.gui.controls.input;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.controls.input.styles.DeprecatedInputStyle;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.controls.popup.tooltip.TextTooltip;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.input.KeyCode;

/**
 * A specialized input control with an associated icon button. This class
 * extends the {@link Input} class and includes an icon button with additional
 * functionality.
 *
 * @author SDIDSA
 */
public class InputIconButton extends Input implements Styleable {
	private ColorIcon icon;

	/**
	 * Creates a new instance of {@code InputIconButton}.
	 *
	 * @param win     The parent {@link Window}.
	 * @param name    The name of the icon.
	 * @param size    The size of the icon.
	 * @param tooltip The tooltip text for the icon button.
	 * @param fire    The {@link Runnable} action to be performed when the icon
	 *                button is clicked.
	 */
	public InputIconButton(Window win, String name, double size, String tooltip, Runnable fire) {
		super("app_icon");

		setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
		setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);

		setPrefSize(40, 40);

		inputStyle = new DeprecatedInputStyle(this);

		setFocusTraversable(true);

		setOnMouseClicked(e -> {
			requestFocus();
			fire.run();
		});

		focusedProperty().addListener((obs, ov, nv) -> {
			inputStyle.focus(nv);
		});

		setOnKeyPressed(ke -> {
			if (ke.getCode().equals(KeyCode.SPACE)) {
				fire.run();
			}
		});

		icon = new ColorIcon(name, size);

		TextTooltip.install(this, Direction.UP, tooltip, 15, 15, true);

		getChildren().setAll(icon);

		setCursor(Cursor.HAND);

		applyStyle(win.getStyl());
	}

	@Override
	public boolean isFocus() {
		return isFocused();
	}

	@Override
	public void setFont(Font font) {
		// doesn't have font
	}

	@Override
	public void setValue(String value) {
		// can't set value
	}

	@Override
	public void clear() {
		// can't be cleared
	}

	@Override
	public void applyStyle(Style style) {
		inputStyle.applyStyle(style);
		icon.setFill(style.getHeaderSecondary());
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
