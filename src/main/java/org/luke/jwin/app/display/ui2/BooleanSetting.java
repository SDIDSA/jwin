package org.luke.jwin.app.display.ui2;

import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.CheckMenuItem;
import javafx.beans.property.BooleanProperty;

public class BooleanSetting extends CheckMenuItem {

	public BooleanSetting(ContextMenu menu, String name, BooleanProperty property) {
		super(menu, name, null);

		property.addListener((obs, ov, nv) -> {
			checkedProperty().set(nv);
		});

		checkedProperty().addListener((obs, ov, nv) -> {
			if (ov != nv)
				property.set(nv);
		});
	}

}
