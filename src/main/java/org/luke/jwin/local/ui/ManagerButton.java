package org.luke.jwin.local.ui;

import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.controls.popup.tooltip.KeyedTooltip;
import org.luke.gui.controls.popup.tooltip.TextTooltip;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.style.Style;
import org.luke.gui.window.Window;

import javafx.beans.binding.Bindings;
import javafx.scene.Cursor;
import javafx.scene.layout.Background;

public class ManagerButton extends ColorIcon {
	
	private final KeyedTooltip tt;

	public ManagerButton(Window win, String name, String tooltip) {
		super(name, 12);
		
		setPadding(8);
		
		setFocusTraversable(true);

		tt = new KeyedTooltip(win, tooltip, Direction.UP, 10, 10);
		TextTooltip.install(this, tt);
		
		setCursor(Cursor.HAND);
		
		applyStyle(win.getStyl());
	}
	
	public void setIcon(String icon) {
		setImage(icon);
	}
	
	public void setTooltip(String tooltip) {
		tt.setKey(tooltip);
	}

	@Override
	public void applyStyle(Style style) {
		setFill(style.getTextNormal());
		backgroundProperty()
				.bind(Bindings.when(pressedProperty()).then(Backgrounds.make(style.getBackgroundModifierActive(), 5))
						.otherwise(Bindings.when(hoverProperty())
								.then(Backgrounds.make(style.getBackgroundModifierHover(), 5))
								.otherwise(Background.EMPTY)));

		super.applyStyle(style);
	}

}
