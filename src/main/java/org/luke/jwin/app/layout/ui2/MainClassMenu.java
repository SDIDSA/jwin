package org.luke.jwin.app.layout.ui2;

import java.io.File;
import java.util.Map.Entry;

import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.MenuItem;
import org.luke.gui.controls.popup.context.items.MenuMenuItem;
import org.luke.jwin.app.layout.JwinUi;

public class MainClassMenu extends MenuMenuItem {

	public MainClassMenu(ContextMenu menu, JwinUi config) {
		super(menu, "main_class");

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

		addMenuItem("select_main_class", () -> {
			menu.hide();
			String n = config.getMainClass().showChooser();
			if (n != null)
				config.logStd("Main Class param was set to " + n);
		});
	}

}
