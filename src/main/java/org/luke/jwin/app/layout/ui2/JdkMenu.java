package org.luke.jwin.app.layout.ui2;

import java.io.File;
import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.KeyedMenuItem;
import org.luke.gui.controls.popup.context.items.MenuItem;
import org.luke.gui.controls.popup.context.items.MenuMenuItem;
import org.luke.gui.locale.Locale;
import org.luke.jwin.app.Jwin;
import org.luke.jwin.app.layout.JwinUi;
import org.luke.jwin.local.managers.JdkManager;

import javafx.application.Platform;

public class JdkMenu extends MenuMenuItem {

	public JdkMenu(ContextMenu menu, JwinUi config) {
		super(menu, "jdk_compile");

		KeyedMenuItem valItem = new KeyedMenuItem(getSubMenu(), "");
		valItem.setDisable(true);

		MenuItem configureJdks = new MenuItem(getSubMenu(), "configure_jdk_versions", true);

		configureJdks.setAction(() -> {
			menu.hide();
			Jwin.instance.openSettings("jdk_versions");
		});

		getSubMenu().addOnShowing(() -> {
			File val = config.getJdk().getValue();
			if (val != null) {
				valItem.setText("jdk " + config.getJdk().getVersion());
			}else {
				valItem.setKey("not_selected");
			}

			getSubMenu().clear();

			addMenuItem(valItem);

			getSubMenu().separate();

			addMenuItem(configureJdks);

			getSubMenu().separate();

			new Thread(() -> {
				JdkManager.allInstalls().forEach(jdk -> {
					String disp = jdk.getVersion();
					MenuItem detIt = new MenuItem(getSubMenu(), disp);
					detIt.setAction(() -> {
						menu.hide();
						config.getJdk().setFile(jdk.getRoot());
					});
					Platform.runLater(() -> addMenuItem(detIt));
				});
			}).start();
		});
	}

}
