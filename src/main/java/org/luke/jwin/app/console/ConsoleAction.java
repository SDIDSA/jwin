package org.luke.jwin.app.console;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.image.ColoredIcon;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.controls.popup.tooltip.Tooltip;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.style.Style;
import org.luke.gui.window.Window;

import javafx.beans.binding.Bindings;
import javafx.scene.Cursor;
import javafx.scene.layout.Background;

public class ConsoleAction extends ColoredIcon {

	public ConsoleAction(Window win, String name, String tooltip) {
		super(win, name, 20, Style::getHeaderSecondary);
		
		setPadding(3);
		
		Tooltip tt = new Tooltip(win, tooltip, Direction.UP, 0, 10);
		tt.setFont(new Font(14));
		Tooltip.install(this, tt);
		
		
		setCursor(Cursor.HAND);
		
		opacityProperty().bind(Bindings.when(disabledProperty()).then(.5).otherwise(1));
		
	}
	
	@Override
	public void applyStyle(Style style) {
		backgroundProperty().bind(Bindings.when(hoverProperty()).then(Backgrounds.make(style.getBackgroundPrimary(), 5)).otherwise(Background.EMPTY));
		
		super.applyStyle(style);
	}

}
