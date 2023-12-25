package org.luke.jwin.app.layout.ui2;

import java.io.File;

import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.MenuItem;
import org.luke.gui.controls.popup.context.items.MenuMenuItem;
import org.luke.jwin.app.layout.JwinUi;

public class JreMenu extends MenuMenuItem {

	public JreMenu(ContextMenu menu, JwinUi config) {
		super(menu, "jre_pack");
		
		MenuItem valItem = new MenuItem(getSubMenu(), "");
		valItem.setDisable(true);
		getSubMenu().addOnShowing(() -> {
			File val = config.getJre().getValue();
			String valDisp = "(not selected)";
			if (val != null) {
				valDisp = "jre " + config.getJre().getVersion();
			}
			valItem.setText(valDisp);
		});
		
		addMenuItem(valItem);

		getSubMenu().separate();
		
		addMenuItem("generate_with_jlink", () -> {
			menu.hide();
			config.getJre().generateFromJdk(menu.getOwner(), config);
		});
		
		addMenuItem("browse_archive", () -> {
			menu.hide();
			config.getJre().browseArchive();
		});
		
		addMenuItem("browse_folder", () -> {
			menu.hide();
			config.getJre().browseDir();
		});
	}

}
