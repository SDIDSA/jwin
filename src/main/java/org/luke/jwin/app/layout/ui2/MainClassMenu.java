package org.luke.jwin.app.layout.ui2;

import java.io.File;
import java.util.Map.Entry;

import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.KeyedMenuItem;
import org.luke.gui.controls.popup.context.items.MenuItem;
import org.luke.gui.controls.popup.context.items.MenuMenuItem;
import org.luke.gui.locale.Locale;
import org.luke.jwin.app.layout.JwinUi;

public class MainClassMenu extends MenuMenuItem {

	public MainClassMenu(ContextMenu menu, JwinUi config) {
		super(menu, "main_class");

		KeyedMenuItem valItem = new KeyedMenuItem(getSubMenu(), "");
		valItem.setDisable(true);
		getSubMenu().addOnShowing(() -> {
			Entry<String, File> main = config.getMainClass().getValue();
			if (main != null) {
				StringBuilder mc = new StringBuilder();
				String[] parts = main.getKey().split("\\.");
				for(int i = parts.length - 1; i >= 0; i--) {

					if(mc.length() + parts[i].length() > 20 && !mc.isEmpty()) {
						break;
					}
					mc.insert(0, parts[i] + (mc.toString().isBlank() ? "" : "."));
				}
				valItem.setText(mc.toString());
			} else {
				valItem.setKey("not_selected");
			}
		});

		addMenuItem(valItem);

		getSubMenu().separate();

		addMenuItem("select_main_class", () -> {
			menu.hide();
			String n = config.getMainClass().showChooser();
			if (n != null)
				config.logStd(Locale.key("mc_set", "class", n));
		});
	}

}
