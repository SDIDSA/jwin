package org.luke.jwin.app;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.luke.gui.locale.Locale;
import org.luke.gui.window.Window;
import org.luke.gui.window.content.app_bar.AppBarButton;
import org.luke.jwin.app.about.Credits;
import org.luke.jwin.app.layout.JwinUi;
import org.luke.jwin.app.layout.settings.SettingsPan;
import org.luke.jwin.local.LocalStore;

import javafx.application.Application;
import javafx.stage.Stage;

public class Jwin extends Application {
	public static Jwin instance;
	public static Window winstance;

	public Jwin() {
		instance = this;
	}

	private SettingsPan sets;
	private JwinHome home;

	@Override
	public void start(Stage ps) throws Exception {

		System.setProperty("prism.lcdtext", "false");

		Window window = new Window(this, LocalStore.getStyle(), Locale.FR_FR);
		winstance = window;
		
		window.getStyl().addListener((obs, ov, nv) -> {
			LocalStore.setStyle(nv);
		});

		home = new JwinHome(window);

		Credits about = new Credits(home);

		sets = new SettingsPan(home);

		AppBarButton openSettings = new AppBarButton(window, "settings");

		window.addBarButton(1, openSettings);
		window.setOnInfo(() -> about.showAndWait());

		window.setWindowIcon("jwin-icon");
		window.setTaskIcon("jwin-task-icon");

		window.setTitle("jWin");

		openSettings.setAction(sets::show);

		window.setOnShown(e -> {
			window.loadPage(home);
		});
		window.show();
	}

	public void openSettings(String match) {
		if (sets.fire(match)) {
			sets.showAndWait();
		}
	}
	
	public JwinHome getHome() {
		return home;
	}
	
	public JwinUi getConfig() {
		return home.getConfig();
	}

	public static File get7z() {
		try {
			return new File(URLDecoder.decode(Jwin.class.getResource("/7z.exe").getFile(), "utf-8")).getParentFile();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
}
