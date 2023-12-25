package org.luke.gui.controls.input.combo;

import org.luke.gui.controls.popup.context.items.MenuItem;

public interface ComboItem {
	String getDisplay();
	String getValue();
	MenuItem menuItem();
	boolean match(String other);
}
