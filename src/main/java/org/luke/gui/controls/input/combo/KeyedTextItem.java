package org.luke.gui.controls.input.combo;

import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.KeyedMenuItem;
import org.luke.gui.controls.popup.context.items.MenuItem;

public class KeyedTextItem extends KeyedMenuItem implements ComboItem {

	protected KeyedTextItem(ContextMenu men, String key) {
		super(men, key, null);
	}

	public String getDisplay() {
		return lab.getText();
	}

	public String getValue() {
		return getKey();
	}

	@Override
	public MenuItem menuItem() {
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof KeyedTextItem kti)
			return getValue().equals(kti.getValue());

		return false;
	}

	public boolean match(String toMatch) {
		return getValue().toLowerCase().contains(toMatch.toLowerCase())
				|| getDisplay().toLowerCase().contains(toMatch.toLowerCase());
	}

}
