package org.luke.jwin.app.layout.ui2;

import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.MenuItem;
import org.luke.gui.controls.popup.context.items.MenuMenuItem;
import org.luke.jwin.app.layout.JwinUi;

public class FileMenu extends MenuMenuItem {

	public FileMenu(ContextMenu menu, JwinUi config) {
		super(menu, "file");

		MenuItem save = new MenuItem(getSubMenu(), "save", true);

		getSubMenu().addOnShowing(() -> {
			save.setDisable(
					config.getFileInUse() == null || config.export().compare(config.getProjectInUse()).isEmpty());
		});

		save.setAction(() -> {
			menu.hide();
			config.save();
		});

		addMenuItem(save);

		addMenuItem("save_as", () -> {
			menu.hide();
			config.saveAs();
		});

		addMenuItem("import_jwin_project", () -> {
			menu.hide();
			config.importProject(menu.getOwner());
		});

		addMenuItem("import_java_project", () -> {
			menu.hide();
			config.importJavaProject(menu.getOwner());
		});
	}

}
