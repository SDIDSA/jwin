package org.luke.jwin.app.display;

import java.io.File;
import java.util.Map.Entry;

import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.MenuItem;
import org.luke.gui.controls.popup.context.items.MenuMenuItem;
import org.luke.jwin.app.param.JavaParam;

public class JdkMenu extends MenuMenuItem {

	public JdkMenu(ContextMenu menu, JwinUi config) {
		super(menu, "JDK (compiles your code)");
		
		MenuItem valItem = new MenuItem(getSubMenu(), "");
		valItem.setDisable(true);
		getSubMenu().addOnShowing(() -> {
			File val = config.getJdk().getValue();
			String valDisp = "(not selected)";
			if (val != null) {
				valDisp = "jdk " + config.getJdk().getVersion();
			}
			valItem.setText(valDisp);
		});
		
		addMenuItem(valItem);

		getSubMenu().separate();
		
		File detected = config.getJdk().detectJdk();
		if (detected != null && detected.exists()) {
			Entry<String, File> version = JavaParam.getVersionFromDir(detected);
			if (version != null && version.getKey() != null) {
				String disp = "system [ jdk " + version.getKey().replace("\"", "") + " ]";
				MenuItem detIt = new MenuItem(getSubMenu(), disp);
				detIt.setAction(() -> {
					menu.hide();
					config.getJdk().set(detected);
					config.logStd("The project JDK was set to " + disp + " (found on your system)");
				});
				addMenuItem(detIt);
			}
		}

		addMenuItem("browse", () -> {
			menu.hide();
			File selected = config.getJdk().browse();
			if (selected != null) {
				Entry<String, File> version = JavaParam.getVersionFromDir(selected);
				if (version != null && version.getKey() != null) {
					String disp = "jdk " + version.getKey().replace("\"", "");
					config.logStd("The project JDK was set to " + disp + " (Selected by you)");
				}
			}
		});
	}

}
