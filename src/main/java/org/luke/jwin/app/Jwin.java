package org.luke.jwin.app;

import org.luke.gui.controls.check.RadioGroup;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.MenuMenuItem;
import org.luke.gui.controls.popup.context.items.RadioMenuItem;
import org.luke.gui.locale.Locale;
import org.luke.gui.style.Style;
import org.luke.gui.window.Window;
import org.luke.gui.window.content.app_bar.AppBarButton;
import org.luke.jwin.app.about.Credits;
import org.luke.jwin.app.layout.JwinUi1;
import org.luke.jwin.app.layout.settings.SettingsPan;
import org.luke.jwin.app.layout.ui2.JwinUi2;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Jwin extends Application {

	@Override
	public void start(Stage ps) throws Exception {
		System.setProperty("prism.lcdtext", "false");

		Window window = new Window(this, Style.GRAY_1, Locale.EN_US);

		ContextMenu settings = new ContextMenu(window);

		MenuMenuItem color = new MenuMenuItem(settings, "Color Theme");
		RadioMenuItem light = new RadioMenuItem(color.getSubMenu(), "Light Theme", null);
		RadioMenuItem dark = new RadioMenuItem(color.getSubMenu(), "Dark Theme", null);

		color.addMenuItem(light);
		color.addMenuItem(dark);

		dark.getCheck().flip();
		RadioGroup appTheme = new RadioGroup(light.getCheck(), dark.getCheck());

		MenuMenuItem ui = new MenuMenuItem(settings, "Display Style");
		RadioMenuItem classic = new RadioMenuItem(color.getSubMenu(), "Classic UI", null);
		RadioMenuItem simplified = new RadioMenuItem(color.getSubMenu(), "Simplified UI", null);
		RadioGroup appUi = new RadioGroup(classic.getCheck(), simplified.getCheck());

		ui.addMenuItem(classic);
		ui.addMenuItem(simplified);

		appTheme.valueProperty().addListener((obs, ov, nv) -> {
			if (nv == light.getCheck()) {
				window.setStyle(Style.DARK_1);
			} else {
				window.setStyle(Style.LIGHT_1);
			}
		});

		JwinHome home = new JwinHome(window);

		Credits about = new Credits(home);

		appUi.valueProperty().addListener((obs, ov, nv) -> {
			home.setConfig(nv == simplified.getCheck() ? JwinUi2.class : JwinUi1.class);
		});

		SettingsPan sets = new SettingsPan(home);

		settings.addMenuItem(color);
		settings.addMenuItem(ui);
		settings.separate();
		settings.addMenuItem("Exit", Platform::exit);

		AppBarButton openSettings = new AppBarButton(window, "settings");
		openSettings.setAction(() -> settings.showPop(openSettings, Direction.DOWN_LEFT, 15));

		window.addBarButton(1, openSettings);
		window.setOnInfo(() -> about.showAndWait());

		window.setWindowIcon("jwin-icon");
		window.setTaskIcon("jwin-task-icon");

		window.setTitle("jWin");

		openSettings.setAction(sets::show);

		window.setOnShown(e -> {
			window.loadPage(home);
			simplified.getCheck().setChecked(true);
		});
		window.show();
	}
}
