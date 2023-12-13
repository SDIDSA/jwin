package org.luke.jwin.app.display;

import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.MenuItem;
import org.luke.gui.controls.popup.context.items.MenuMenuItem;

public class FileMenu extends MenuMenuItem {

	public FileMenu(ContextMenu menu, JwinUi config) {
		super(menu, "File");

		MenuItem save = new MenuItem(getSubMenu(), "Save");

		getSubMenu().addOnShowing(() -> {
			save.setDisable(config.getFileInUse() == null);
		});

		save.setAction(() -> {
			menu.hide();
			config.save();
		});

		addMenuItem(save);

		addMenuItem("Save as",() -> {
			menu.hide();
			config.saveAs();
		});

		addMenuItem("Import", () -> {
			menu.hide();
			config.importProject(menu.getOwner());
		});
	}

}
