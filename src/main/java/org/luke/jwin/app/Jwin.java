package org.luke.jwin.app;

import org.luke.gui.controls.check.RadioGroup;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.RadioMenuItem;
import org.luke.gui.locale.Locale;
import org.luke.gui.style.Style;
import org.luke.gui.window.Window;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Jwin extends Application {

	@Override
	public void start(Stage ps) throws Exception {
		System.setProperty("prism.lcdtext", "false");

		Window window = new Window(this, Style.DARK, Locale.EN_US);

		ContextMenu settings = new ContextMenu(window);
		
		RadioMenuItem light = new RadioMenuItem(settings, "Light Theme", null);
		RadioMenuItem dark = new RadioMenuItem(settings, "Dark Theme", null);
		dark.getCheck().flip();
		RadioGroup appTheme = new RadioGroup(light.getCheck(), dark.getCheck());
		
		appTheme.valueProperty().addListener((obs, ov, nv) -> {
			if(nv == light.getCheck()) {
				window.setStyle(Style.LIGHT);
			}else {
				window.setStyle(Style.DARK);
			}
		});
		
		settings.addMenuItem(light);
		settings.addMenuItem(dark);
		settings.separate();
		settings.addMenuItem("Exit", () -> {
			Platform.exit();
		});
		
		window.setOnInfo(() -> {
			settings.showPop(window.getInfo(), Direction.LEFT_DOWN);
		});
		
		window.setWindowIcon("jwin-icon");
		window.setTaskIcon("jwin-task-icon");
		
		window.setTitle("jWin");
		window.setOnShown(e -> window.loadPage(new JwinHome(window)));
		window.show();
	}
}
