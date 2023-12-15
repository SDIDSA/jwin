package org.luke.gui.controls.popup.context.items;

import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.controls.popup.context.ContextMenu;

public class MenuMenuItem extends MenuItem {
	private ColorIcon arrow;

	private ContextMenu subMenu;

	public MenuMenuItem(ContextMenu menu, String key) {
		super(menu, key);

		arrow = new ColorIcon("menu-right", 12, 8);
		arrow.fillProperty().bind(lab.fillProperty());

		getChildren().add(arrow);

		subMenu = new ContextMenu(menu.getOwner());
		active.addListener((obs, ov, nv) -> {
			if (nv.booleanValue()) {
				subMenu.showPop(this, Direction.RIGHT_DOWN, 15);
			} else {
				subMenu.hide();
			}
		});

		applyStyle(menu.getOwner().getStyl());
	}
	
	public void clear() {
		subMenu.clear();
	}
	
	public void addMenuItem(MenuItem i) {
		subMenu.addMenuItem(i);
	}
	
	public void addMenuItem(String key) {
		subMenu.addMenuItem(key);
	}
	
	public void addMenuItem(String key, Runnable action) {
		subMenu.addMenuItem(key, action);
	}
	
	public ContextMenu getSubMenu() {
		return subMenu;
	}

}
