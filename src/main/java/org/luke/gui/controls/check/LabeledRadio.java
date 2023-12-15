package org.luke.gui.controls.check;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.unkeyed.Text;
import org.luke.gui.window.Window;

import javafx.beans.property.BooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;

public class LabeledRadio extends HBox {
	private Radio radio;
	private Text label;
	
	public LabeledRadio(Window window, String text, double size) {
		super(8);
		setAlignment(Pos.CENTER_LEFT);
		
		radio = new Radio(window, size);
		label = new Text(text);
		
		radio.setMouseTransparent(true);
		label.setMouseTransparent(true);
		
		setOnMouseClicked(e-> radio.flip());
		
		setCursor(Cursor.HAND);
		
		getChildren().addAll(radio, label);
	}
	
	public Radio getRadio() {
		return radio;
	}
	
	public BooleanProperty checkedProperty() {
		return radio.checkedProperty();
	}
	
	public void setTextFill(Paint fill) {
		label.setFill(fill);
	}
	
	public void setFont(Font font) {
		label.setFont(font);
	}
	
	public void setText(String key) {
		label.setText(key);
	}
}
