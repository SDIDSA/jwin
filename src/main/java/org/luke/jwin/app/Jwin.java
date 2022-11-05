package org.luke.jwin.app;

import org.luke.gui.controls.check.RadioGroup;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.RadioMenuItem;
import org.luke.gui.locale.Locale;
import org.luke.gui.style.Style;
import org.luke.gui.window.Window;
import org.luke.gui.window.content.app_bar.AppBarButton;
import org.luke.jwin.app.about.Credits;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Jwin extends Application {

	@Override
	public void start(Stage ps) throws Exception {
		System.setProperty("prism.lcdtext", "false");

		Window window = new Window(this, Style.DARK, Locale.EN_US);

		JwinHome home = new JwinHome(window);
		
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
		settings.addMenuItem("Exit", Platform::exit);
		
		AppBarButton openSettings = new AppBarButton(window, "settings");
		openSettings.setAction(() -> settings.showPop(openSettings, Direction.DOWN_LEFT, 15));
		
		window.addBarButton(1, openSettings);
		
		Credits about = new Credits(home);
		window.setOnInfo(about::show);
		
		window.setWindowIcon("jwin-icon");
		window.setTaskIcon("jwin-task-icon");
		
		window.setTitle("jWin");
		window.setOnShown(e -> window.loadPage(home));
		window.show();
	}
}
