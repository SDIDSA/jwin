package org.luke.jwin.app.layout.settings.content.display.theme;

import java.util.function.Consumer;

import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.factory.Borders;
import org.luke.gui.style.Style;
import org.luke.gui.window.Window;

import javafx.scene.Cursor;

public class ThemeOption extends ColorIcon {

	public ThemeOption(Window win, Style style) {
		super("app-theme", 48, 32);

		setBackground(Backgrounds.make(style.getBackgroundSecondary(), 48));
		setFill(style.getAccent());
		
		setCursor(Cursor.HAND);

		Consumer<Style> onChange = (nv) -> {
			setBorder(nv == style ? Borders.make(nv.getTextLink(), 48, 1) : Borders.make(nv.getBackgroundModifierActive(), 48));
		};

		onChange.accept(win.getStyl().get());

		win.getStyl().addListener((obs, ov, nv) -> onChange.accept(nv));

		setPadding(10);

		setAction(() -> win.getStyl().set(style));
	}

}
