package org.luke.jwin.app.layout.ui2;

import org.luke.gui.controls.check.RadioGroup;
import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.MenuMenuItem;
import org.luke.gui.controls.popup.context.items.RadioMenuItem;
import org.luke.jwin.app.layout.JwinUi;

public class RunAsAdmin extends MenuMenuItem {

	public RunAsAdmin(ContextMenu menu, JwinUi config) {
		super(menu, "Run As Admin");

		RadioMenuItem enabled = new RadioMenuItem(getSubMenu(), "enable", null);
		RadioMenuItem disabled = new RadioMenuItem(getSubMenu(), "disable", null);

		RadioGroup ce = new RadioGroup(enabled.getCheck(), disabled.getCheck());

		config.getAdmin().checkedProperty().addListener((obs, ov, nv) -> {
			if (ov.booleanValue() != nv.booleanValue()) {
				if(nv.booleanValue()) enabled.setChecked(true);
				else disabled.setChecked(true);
			}
		});

		ce.valueProperty().addListener((obs, ov, nv) -> {
			if (ov != nv)
				config.getAdmin().checkedProperty().set(nv == enabled.getCheck());
		});

		addMenuItem(enabled);
		addMenuItem(disabled);
	}

}
