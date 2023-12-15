package org.luke.jwin.app.display.ui2;

import org.luke.gui.controls.check.RadioGroup;
import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.MenuMenuItem;
import org.luke.gui.controls.popup.context.items.RadioMenuItem;

import javafx.beans.property.BooleanProperty;

public class BooleanMenu extends MenuMenuItem {

	public BooleanMenu(ContextMenu menu, String name, BooleanProperty property) {
		super(menu, name);

		RadioMenuItem enabled = new RadioMenuItem(getSubMenu(), "enable", null);
		RadioMenuItem disabled = new RadioMenuItem(getSubMenu(), "disable", null);

		RadioGroup ce = new RadioGroup(enabled.getCheck(), disabled.getCheck());

		property.addListener((obs, ov, nv) -> {
			if(nv.booleanValue()) enabled.setChecked(true);
			else disabled.setChecked(true);
		});

		ce.valueProperty().addListener((obs, ov, nv) -> {
			if (ov != nv)
				property.set(nv == enabled.getCheck());
		});

		addMenuItem(enabled);
		addMenuItem(disabled);
	}

}
