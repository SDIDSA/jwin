package org.luke.gui.controls.input;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.input.styles.InputStyle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.Styleable;
import javafx.scene.layout.StackPane;

/**
 * The abstract base class for input controls, providing common functionality
 * and properties for input components.
 *
 * @author SDIDSA
 */
public abstract class Input extends StackPane implements Styleable {
	protected StringProperty value;

	protected InputStyle inputStyle;

	/**
	 * Constructs an input control with the specified key.
	 *
	 * @param key The key associated with the input control.
	 */
	protected Input(String key) {
		getStyleClass().addAll("input", key);

		value = new SimpleStringProperty("");

		opacityProperty().bind(Bindings.when(disabledProperty()).then(0.5).otherwise(1));

		setMinHeight(40);
	}

	/**
	 * Gets the input style associated with this input control.
	 *
	 * @return The input style.
	 */
	public InputStyle getInputStyle() {
		return inputStyle;
	}

	/**
	 * Sets the input style for this input control.
	 *
	 * @param inputStyle The input style to be set.
	 */
	public void setInputStyle(InputStyle inputStyle) {
		this.inputStyle = inputStyle;
	}

	/**
	 * Gets the current value of the input control.
	 *
	 * @return The current value.
	 */
	public String getValue() {
		return value.get();
	}

	/**
	 * Gets the property representing the value of the input control.
	 *
	 * @return The value property.
	 */
	public StringProperty valueProperty() {
		return value;
	}

	/**
	 * Sets whether to ignore hover events for the input control.
	 *
	 * @param val If true, hover events are ignored; otherwise, they are not
	 *            ignored.
	 */
	public void ignoreHover(boolean val) {
		inputStyle.setIgnoreHover(val);
	}

	/**
	 * Sets whether to ignore focus events for the input control.
	 *
	 * @param val If true, focus events are ignored; otherwise, they are not
	 *            ignored.
	 */
	public void ignoreFocus(boolean val) {
		inputStyle.setIgnoreFocus(val);
	}

	/**
	 * Checks whether the input control currently has focus.
	 *
	 * @return True if the input control has focus, false otherwise.
	 */
	public abstract boolean isFocus();

	/**
	 * Sets the font for the input control.
	 *
	 * @param font The font to be set.
	 */
	public abstract void setFont(Font font);

	/**
	 * Sets the value of the input control.
	 *
	 * @param value The value to be set.
	 */
	public abstract void setValue(String value);

	/**
	 * Clears the value of this input.
	 */
	public abstract void clear();
}
