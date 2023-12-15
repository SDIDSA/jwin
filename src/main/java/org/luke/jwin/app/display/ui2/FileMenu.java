package org.luke.jwin.app.display.ui2;

import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.MenuItem;
import org.luke.gui.controls.popup.context.items.MenuMenuItem;
import org.luke.jwin.app.display.JwinUi;

public class FileMenu extends MenuMenuItem {

	public FileMenu(ContextMenu menu, JwinUi config) {
		super(menu, "File");

		MenuItem save = new MenuItem(getSubMenu(), "Save");

		getSubMenu().addOnShowing(() -> {
			save.setDisable(
					config.getFileInUse() == null || config.export().compare(config.getProjectInUse()).isEmpty());
		});

		save.setAction(() -> {
			menu.hide();
			config.save();
		});

		addMenuItem(save);

		addMenuItem("Save as", () -> {
			menu.hide();
			config.saveAs();
		});

		addMenuItem("Import Jwin Project", () -> {
			menu.hide();
			config.importProject(menu.getOwner());
		});

		addMenuItem("Import Java Project", () -> {
			menu.hide();
			config.importJavaProject(menu.getOwner());
		});
	}

}
