package org.luke.jwin.app.layout.ui2;

import java.io.File;

import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.MenuItem;
import org.luke.gui.controls.popup.context.items.MenuMenuItem;
import org.luke.jwin.app.layout.JwinUi;

public class ClassPathMenu extends MenuMenuItem {

	public ClassPathMenu(ContextMenu menu, JwinUi config) {
		super(menu, "classpath");
		
		getSubMenu().addOnShowing(() -> {
			getSubMenu().clear();
			
			if(config.getClasspath().getFiles().isEmpty()) {
				MenuItem entDisp = new MenuItem(getSubMenu(), "empty_classpath", true);
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
			addMenuItem("clear", () -> {
				menu.hide();
				config.getClasspath().clear();
				config.logStd("the classpath was cleared");
			});
			
			addMenuItem("add", () -> {
				menu.hide();
				File added = config.getClasspath().add();
				if(added != null) {
					config.logStd(config.getClasspath().generateDisplay(added) + " was added to the classpath");
				}
			});
		});
	}

}
