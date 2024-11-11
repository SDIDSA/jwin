package org.luke.jwin.app.param.main;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.controls.input.ModernTextInput;
import org.luke.gui.controls.space.FixedHSpace;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

public class MainClassSearch extends StackPane implements Styleable {

	private final ModernTextInput field;
	private final ColorIcon icon;

	public MainClassSearch(Window window) {
		setPadding(new Insets(1));
		setAlignment(Pos.CENTER_LEFT);

		field = new ModernTextInput(window, new Font(14), "classSearch", false);

		field.setKeyedPrompt("search_for_class");
		icon = new ColorIcon("search", 24, 20);
		field.addPostField(icon, new FixedHSpace(10));

		getChildren().add(field);

		applyStyle(window.getStyl());
	}

	public StringProperty valueProperty() {
		return field.valueProperty();
	}

	public String getValue() {
		return field.getValue();
	}

	public void clear() {
		field.clear();
	}

	@Override
	public void requestFocus() {
		field.requestFocus();
	}

	@Override
	public void applyStyle(Style style) {
		icon.setFill(style.getAccent());
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
