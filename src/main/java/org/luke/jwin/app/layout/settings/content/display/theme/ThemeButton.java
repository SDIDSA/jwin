package org.luke.jwin.app.layout.settings.content.display.theme;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.controls.popup.tooltip.Tooltip;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.style.Style;
import org.luke.gui.window.Window;

import javafx.beans.binding.Bindings;
import javafx.scene.Cursor;
import javafx.scene.layout.Background;

public class ThemeButton extends ColorIcon {

	public ThemeButton(Window win, String name, String tooltip, Direction dir, int offX, int offY) {
		super(name, 24);
		
		setPadding(14);

		Tooltip tt = new Tooltip(win, tooltip, dir, offX, offY);
		tt.setPadding(4);
		tt.setFont(new Font(14));
		Tooltip.install(this, tt);
		
		setCursor(Cursor.HAND);
		
		applyStyle(win.getStyl());
	}

	public ThemeButton(Window win, String name, String tooltip) {
		this(win, name, tooltip, Direction.RIGHT, 5, 0);
	}

	@Override
	public void applyStyle(Style style) {
		setFill(style.getTextNormal());
		backgroundProperty()
				.bind(Bindings.when(pressedProperty()).then(Backgrounds.make(style.getBackgroundModifierActive(), 52))
						.otherwise(Bindings.when(hoverProperty())
								.then(Backgrounds.make(style.getBackgroundModifierHover(), 52))
								.otherwise(Background.EMPTY)));

		super.applyStyle(style);
	}

}
