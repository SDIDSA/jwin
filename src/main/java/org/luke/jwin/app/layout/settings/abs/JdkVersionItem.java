package org.luke.jwin.app.layout.settings.abs;

import org.luke.gui.controls.input.combo.ComboItem;
import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.MenuItem;
import org.luke.jwin.local.managers.JdkManager;

public class JdkVersionItem extends MenuItem implements ComboItem {

	private String path;
	private String version;

	public JdkVersionItem(ContextMenu menu, String text) {
		super(menu, JdkManager.versionOf(text), false);

		path = text;
		version = lab.getText();
	}

	@Override
	public String getDisplay() {
		return version;
	}

	@Override
	public String getValue() {
		return path;
	}

	@Override
	public MenuItem menuItem() {
		return this;
	}

	public boolean match(String toMatch) {
		return getValue().toLowerCase().contains(toMatch.toLowerCase())
				|| getDisplay().toLowerCase().contains(toMatch.toLowerCase());
	}

}
