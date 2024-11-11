package org.luke.jwin.app.layout.ui2;

import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.CheckMenuItem;
import javafx.beans.property.BooleanProperty;

public class BooleanSetting extends CheckMenuItem {

	public BooleanSetting(ContextMenu menu, String name, BooleanProperty property) {
		super(menu, name, null);

		property.addListener((_, _, nv) -> checkedProperty().set(nv));

		checkedProperty().addListener((_, ov, nv) -> {
			if (ov != nv)
				property.set(nv);
		});
	}

}
