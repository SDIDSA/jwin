package org.luke.gui.controls.input;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.input.styles.InputStyle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.Styleable;
import javafx.scene.layout.StackPane;

public abstract class Input extends StackPane implements Styleable {
	protected StringProperty value;
	
	protected InputStyle inputStyle;
	
	protected Input(String key) {
		getStyleClass().addAll("input", key);
		
		value = new SimpleStringProperty("");
		
		opacityProperty().bind(Bindings.when(disabledProperty()).then(0.5).otherwise(1));
		
		setMinHeight(40);
	}
	
	public InputStyle getInputStyle() {
		return inputStyle;
	}
	
	public void setInputStyle(InputStyle inputStyle) {
		this.inputStyle = inputStyle;
	}
	
	public String getValue() {
		return value.get();
	}

	public StringProperty valueProperty() {
		return value;
	}
	
	public void ignoreHover(boolean val) {
		inputStyle.setIgnoreHover(val);
	}
	
	public void ignoreFocus(boolean val) {
		inputStyle.setIgnoreFocus(val);
	}
	
	public abstract boolean isFocus();
	
	public abstract void setFont(Font font);

	public abstract void setValue(String value);

	/**
	 * Clear the value of this input, note that this method is abstract and the
	 * implementation of this method depends on the input type
	 */
	public abstract void clear();
}
