package org.luke.jwin.app.display;

import java.io.File;

import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.MenuItem;
import org.luke.gui.controls.popup.context.items.MenuMenuItem;

public class ClassPathMenu extends MenuMenuItem {

	public ClassPathMenu(ContextMenu menu, JwinUi config) {
		super(menu, "Class path");
		
		getSubMenu().addOnShowing(() -> {
			getSubMenu().clear();
			
			if(config.getClasspath().getFiles().isEmpty()) {
				MenuItem entDisp = new MenuItem(getSubMenu(), "(empty classpath)");
				entDisp.setDisable(true);
				
				addMenuItem(entDisp);
			}
			
			config.getClasspath().getFiles().forEach(entry -> {
				String disp = config.getClasspath().generateDisplay(entry);
				
				MenuItem entDisp = new MenuItem(getSubMenu(), disp);
				entDisp.setDisable(true);
				
				addMenuItem(entDisp);
			});
			
			getSubMenu().separate();
			addMenuItem("Clear", () -> {
				menu.hide();
				config.classpath.clear();
				config.logStd("the classpath was cleared");
			});
			
			addMenuItem("Add", () -> {
				menu.hide();
				File added = config.classpath.add();
				if(added != null) {
					config.logStd(config.classpath.generateDisplay(added) + " was added to the classpath");
				}
			});
		});
	}

}
