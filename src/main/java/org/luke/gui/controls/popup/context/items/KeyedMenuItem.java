package org.luke.gui.controls.popup.context.items;

import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.controls.popup.context.ContextMenu;

import javafx.scene.paint.Color;

public class KeyedMenuItem extends MenuItem {

	public KeyedMenuItem(ContextMenu menu, String key, Color fill) {
		super(menu, key, fill, true);
	}
	
	public KeyedMenuItem(ContextMenu menu, String key) {
		this(menu, key, null);
	}

	public String getKey() {
		return ((Label) lab).getKey();
	}

}
