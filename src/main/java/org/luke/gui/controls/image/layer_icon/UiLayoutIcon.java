package org.luke.gui.controls.image.layer_icon;

import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

public class UiLayoutIcon extends LayerIcon implements Styleable {

	private final DropShadow ds;

	public UiLayoutIcon(Window win, String type, double size, Style style) {
		super(size);

		addLayer("lay-bac-" + type, 512, size);
		addLayer("lay-par-" + type, 512, size);
		addLayer("lay-tex-" + type, 512, size);
		addLayer("lay-acc-" + type, 512, size);

		ds = new DropShadow(BlurType.GAUSSIAN, Color.gray(.5, .3), 1, 1, 0, 0);

		setEffect(1, ds);
		setEffect(new DropShadow(7, Color.gray(0, .5)));

		if (style == null)
			applyStyle(win.getStyl());
		else
			applyStyle(style);
	}

	public UiLayoutIcon(Window win, String type, double size) {
		this(win, type, size, null);
	}

	@Override
	public void applyStyle(Style style) {
		setFill(0, style.getBackgroundSecondary());
		setFill(1, style.getBackgroundTertiaryOr());
		setFill(2, style.getTextMuted());
		setFill(3, style.getAccent());

		ds.setColor(style.getDeprecatedTextInputBorder());
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

}
