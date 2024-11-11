package org.luke.jwin.app.console;

import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.controls.popup.tooltip.KeyedTooltip;
import org.luke.gui.controls.popup.tooltip.TextTooltip;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.style.Style;
import org.luke.gui.window.Window;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Cursor;
import javafx.scene.layout.Background;

public class ConsoleToggleAction extends ColorIcon {

	private final BooleanProperty enabled;
	public ConsoleToggleAction(Window win, String name, String tooltip) {
		super(name, 20);
		
		setPadding(3);
		
		enabled = new SimpleBooleanProperty(false);
		
		TextTooltip tt = new KeyedTooltip(win, tooltip, Direction.UP, 15, 15);
		
		setCursor(Cursor.HAND);
		
		setAction(() -> enabled.set(!enabled.get()));
		
		TextTooltip.install(this, tt);
		
		applyStyle(win.getStyl());
	}
	
	public boolean isEnabled() {
		return enabled.get();
	}
	
	public BooleanProperty enabledProperty() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled.set(enabled);
	}
	
	@Override
	public void applyStyle(Style style) {		
		fillProperty().unbind();
		fillProperty().bind(Bindings.when(enabled).then(style.getBackgroundTertiary()).otherwise(style.getHeaderSecondary()));

		backgroundProperty().bind(Bindings.when(enabled).then(Backgrounds.make(style.getHeaderSecondary(), 5)).otherwise(Background.EMPTY));
		
		super.applyStyle(style);
	}

}
