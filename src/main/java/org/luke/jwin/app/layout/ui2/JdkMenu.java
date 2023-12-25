package org.luke.jwin.app.layout.ui2;

import java.io.File;
import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.MenuItem;
import org.luke.gui.controls.popup.context.items.MenuMenuItem;
import org.luke.jwin.app.Jwin;
import org.luke.jwin.app.layout.JwinUi;
import org.luke.jwin.local.managers.JdkManager;

import javafx.application.Platform;

public class JdkMenu extends MenuMenuItem {

	public JdkMenu(ContextMenu menu, JwinUi config) {
		super(menu, "jdk_compile");

		MenuItem valItem = new MenuItem(getSubMenu(), "", true);
		valItem.setDisable(true);

		MenuItem configureJdks = new MenuItem(getSubMenu(), "configure_jdk_versions", true);

		configureJdks.setAction(() -> {
			menu.hide();
			Jwin.instance.openSettings("jdk versions");
		});

		getSubMenu().addOnShowing(() -> {
			File val = config.getJdk().getValue();
			String valDisp = "(not selected)";
			if (val != null) {
				valDisp = "jdk " + config.getJdk().getVersion();
			}
			valItem.setText(valDisp);

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
						config.getJdk().set(jdk.getRoot());
						config.logStd("The project JDK was set to " + disp);
					});
					Platform.runLater(() -> addMenuItem(detIt));
				});
			}).start();
		});
	}

}
