package org.luke.jwin.ui;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class TextVal extends VBox implements Styleable {
	private TextField field;

	private Label lab;
	private HBox bottom;

	public TextVal(Window window, String name) {
		super(7);
		field = new TextField(window);

		field.setMinWidth(0);

		bottom = new HBox(10, field);

		HBox.setHgrow(this, Priority.ALWAYS);
		HBox.setHgrow(field, Priority.ALWAYS);

		lab = new Label(window, name, new Font(14));
		getChildren().addAll(lab, bottom);
		
		applyStyle(window.getStyl());
	}

	public void setAction(Runnable r) {
		field.setAction(r);
	}

	public void setInputFont(Font font) {
		field.setFont(font);
	}
	
	public void setEditable(boolean editable) {
		field.setEditable(editable);
	}

	public void setPrompt(String prompt) {
		field.setPrompt(prompt);
	}

	public void addToBottom(Node node) {
		bottom.getChildren().add(node);
	}

	public String getValue() {
		return field.getValue();
	}

	public void setValue(String value) {
		field.setValue(value);
	}

	@Override
	public void applyStyle(Style style) {
		lab.setFill(style.getTextNormal());
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}