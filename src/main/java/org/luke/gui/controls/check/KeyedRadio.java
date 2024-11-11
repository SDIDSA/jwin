package org.luke.gui.controls.check;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.HBox;

/**
 * a control that combines a stylized radio button (Radio) with a label (Label)
 * in an HBox.
 *
 * @author SDIDSA
 */
public class KeyedRadio extends HBox implements Styleable {
	private final Radio check;
	private final Label label;

	/**
	 * Constructs a KeyedRadio instance with the specified window, key, and size.
	 *
	 * @param window The associated Window for styling.
	 * @param key    The key to display.
	 * @param size   The size of the radio button.
	 */
	public KeyedRadio(Window window, String key, double size) {
		super(8);
		setAlignment(Pos.CENTER_LEFT);

		check = new Radio(window, size);
		label = new Label(window, key);

		check.setMouseTransparent(true);
		label.setMouseTransparent(true);

		setOnMouseClicked(e -> check.flip());

		setCursor(Cursor.HAND);

		getChildren().addAll(check, label);

		applyStyle(window.getStyl());
	}

	/**
	 * Gets the Radio control associated with the KeyedRadio.
	 *
	 * @return The Radio control.
	 */
	public Radio getCheck() {
		return check;
	}

	/**
	 * Gets the BooleanProperty for the checked property of the associated Radio.
	 *
	 * @return The BooleanProperty for the checked property.
	 */
	public BooleanProperty checkedProperty() {
		return check.checkedProperty();
	}

	/**
	 * Sets the font for the displayed label.
	 *
	 * @param font The Font to set.
	 */
	public void setFont(Font font) {
		label.setFont(font);
	}
	
	/**
	 * Sets the key to display.
	 *
	 * @param key The key to display.
	 */
	public void setKey(String key) {
		label.setKey(key);
	}

	@Override
	public void applyStyle(Style style) {
		label.setFill(style.getTextNormal());
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
