package org.luke.jwin.app;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javafx.scene.input.TransferMode;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.window.Window;
import org.luke.gui.window.content.app_bar.AppBarButton;
import org.luke.jwin.app.file.DragDropOverlay;
import org.luke.jwin.app.about.Credits;
import org.luke.jwin.app.file.ImportMode;
import org.luke.jwin.app.layout.JwinUi;
import org.luke.jwin.app.layout.settings.SettingsPan;
import org.luke.jwin.lang.LanguageMenu;
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

	private DragDropOverlay dragDropOverlay;

	@Override
	public void start(Stage ps) throws Exception {

		System.setProperty("prism.lcdtext", "false");

		Window window = new Window(this, LocalStore.getStyle(), LocalStore.getLanguage());
		winstance = window;
		
		window.getStyl().addListener((_, _, nv) -> {
			LocalStore.setStyle(nv);
		});

		home = new JwinHome(window);

		Credits about = new Credits(home);

		sets = new SettingsPan(home);

		AppBarButton openSettings = new AppBarButton(window, "settings");
		AppBarButton lang = new AppBarButton(window, "language");

		LanguageMenu langMenu = new LanguageMenu(window);

		window.addBarButton(1, openSettings);
		window.addBarButton(1, lang);
		window.setOnInfo(about::showAndWait);

		window.setWindowIcon("jwin-icon");
		window.setTaskIcon("jwin-task-icon");

		window.setTitle("jWin");

		window.getRoot().setOnDragOver(ev -> {
			if(ev.getDragboard().getFiles().size() == 1) {
				File file = ev.getDragboard().getFiles().getFirst();
				if(file.isDirectory() || file.getName().toLowerCase().endsWith(".jwp")) {
					ev.acceptTransferModes(TransferMode.LINK);
					ev.acceptTransferModes(TransferMode.LINK);
					if(dragDropOverlay == null) {
						dragDropOverlay = new DragDropOverlay(window.getLoadedPage(), window);
					}
					if(!dragDropOverlay.isShowing()) {
						dragDropOverlay.setMode(file.isDirectory() ? ImportMode.JAVA : ImportMode.JWIN);
						dragDropOverlay.show();
					}
				}
			}
			ev.consume();
		});

		window.getRoot().setOnDragExited(_ -> {
			if(dragDropOverlay != null && dragDropOverlay.isShowing()) {
				dragDropOverlay.hide();
			}
		});

		window.getRoot().setOnDragDropped(ev -> {
			if(ev.getDragboard().getFiles().size() == 1) {
				File file = ev.getDragboard().getFiles().getFirst();
				if(file.isDirectory()) {
					getConfig().importJavaProject(file);
				}else if(file.getName().toLowerCase().endsWith(".jwp")) {
					getConfig().importProject(file);
				}
			}
		});

		openSettings.setAction(sets::show);

		lang.setAction(() -> {
			langMenu.showPop(lang, Direction.DOWN, 0, 15);
		});

		window.setOnShown(_ -> window.loadPage(home));
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
        return new File(URLDecoder.decode(Objects.requireNonNull(Jwin.class.getResource("/7z.exe")).getFile(),
				StandardCharsets.UTF_8)).getParentFile();
    }

	public static File getResourceHacker() {
		return new File(URLDecoder.decode(Objects.requireNonNull(Jwin.class.getResource("/ResourceHacker.exe")).getFile(),
				StandardCharsets.UTF_8)).getParentFile();
	}
}
