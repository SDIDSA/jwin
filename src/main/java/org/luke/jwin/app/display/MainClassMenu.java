package org.luke.jwin.app.display;

import java.io.File;
import java.util.Map.Entry;

import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.MenuItem;
import org.luke.gui.controls.popup.context.items.MenuMenuItem;

public class MainClassMenu extends MenuMenuItem {

	public MainClassMenu(ContextMenu menu, JwinUi config) {
		super(menu, "Main Class");

		MenuItem valItem = new MenuItem(getSubMenu(), "");
		valItem.setDisable(true);
		getSubMenu().addOnShowing(() -> {
			Entry<String, File> main = config.getMainClass().getValue();
			String disp = "(not selected)";
			if (main != null) {
				disp = main.getKey();
			}
			valItem.setText(disp);
		});

		addMenuItem(valItem);

		getSubMenu().separate();

		addMenuItem("Select Main Class", () -> {
			menu.hide();
			String n = config.mainClass.showChooser();
			if (n != null)
				config.logStd("Main Class param was set to " + n);
		});
	}

}
