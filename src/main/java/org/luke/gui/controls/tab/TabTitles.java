package org.luke.gui.controls.tab;

import org.luke.gui.factory.Backgrounds;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;

public class TabTitles extends HBox implements Styleable {
	public TabTitles(Window window) {
		setPadding(new Insets(5));
		applyStyle(window.getStyl());
	}

	@Override
	public void applyStyle(Style style) {
		setBackground(Backgrounds.make(style.getBackgroundFloating(), new CornerRadii(5, 5, 0, 0, false)));
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
