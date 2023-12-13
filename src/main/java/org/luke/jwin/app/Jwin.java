package org.luke.jwin.app;

import org.luke.gui.app.pages.Page;
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
import org.luke.jwin.app.display.JwinUi1;
import org.luke.jwin.app.display.JwinUi2;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Jwin extends Application {

	Credits about;

	@Override
	public void start(Stage ps) throws Exception {
		System.setProperty("prism.lcdtext", "false");

		Window window = new Window(this, Style.DARK, Locale.EN_US);

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
				window.setStyle(Style.LIGHT);
			} else {
				window.setStyle(Style.DARK);
			}
		});

		appUi.valueProperty().addListener((obs, ov, nv) -> {
			JwinHome h = new JwinHome(window, nv == classic.getCheck() ? JwinUi1.class : JwinUi2.class);
			if (about == null)
				about = new Credits(h);
			about.setOwner(h);

			Page old = window.getLoadedPage();
			if (old != null && old instanceof JwinHome home) {
				if (home.getConfig().getFileInUse() != null)
					h.getConfig().importProject(home.getConfig().getFileInUse());
				else
					h.getConfig().loadProject(home.getConfig().export());
			}

			window.loadPage(h);
			settings.hide();
		});

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
		window.setOnShown(e -> simplified.getCheck().setChecked(true));
		window.show();
	}
}
