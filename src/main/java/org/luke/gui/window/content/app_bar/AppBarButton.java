package org.luke.gui.window.content.app_bar;

import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Cursor;

public class AppBarButton extends ColorIcon implements Styleable {
	public AppBarButton(Window window, String name) {
		super("window_" + name, 14);

		setPickOnBounds(true);
		setCursor(Cursor.HAND);

		applyStyle(window.getStyl());
	}

	public void setIcon(String name) {
		super.setName("window_" + name);
	}

	@Override
	public void applyStyle(Style style) {
		fillProperty()
				.bind(Bindings.when(hoverProperty()).then(style.getAccent().brighter()).otherwise(style.getAccent()));
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
