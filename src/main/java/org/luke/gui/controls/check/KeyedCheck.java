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

public class KeyedCheck extends HBox implements Styleable {
	private Check check;
	private Label label;
	
	public KeyedCheck(Window window, String key, double size) {
		super(8);
		setAlignment(Pos.CENTER_LEFT);
		
		check = new Check(window, size);
		label = new Label(window, key);
		
		check.setMouseTransparent(true);
		label.setMouseTransparent(true);
		
		setOnMouseClicked(e-> check.flip());
		
		setCursor(Cursor.HAND);
		
		getChildren().addAll(check, label);
		
		applyStyle(window.getStyl());
	}
	
	public BooleanProperty checkedProperty() {
		return check.checkedProperty();
	}
	
	public void setFont(Font font) {
		label.setFont(font);
	}
	
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
