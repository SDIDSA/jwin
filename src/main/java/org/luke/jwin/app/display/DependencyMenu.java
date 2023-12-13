package org.luke.jwin.app.display;

import java.io.File;

import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.MenuItem;
import org.luke.gui.controls.popup.context.items.MenuMenuItem;

public class DependencyMenu extends MenuMenuItem {

	public DependencyMenu(ContextMenu menu, JwinUi config) {
		super(menu, "Dependencies");

		MenuMenuItem manual = new MenuMenuItem(menu, "manual dependecies (jar)");
		MenuMenuItem maven = new MenuMenuItem(menu, "maven dependencies");

		MenuItem addManual = new MenuItem(manual.getSubMenu(), "Add");
		addManual.setAction(() -> {
			manual.getSubMenu().hide();
			menu.hide();
			config.getDependencies().addJars(config);
		});

		MenuItem clearManual = new MenuItem(manual.getSubMenu(), "Clear");
		clearManual.setAction(() -> {
			manual.getSubMenu().hide();
			menu.hide();
			config.getDependencies().clearManuals();
			config.logStd("Manual dependencies were cleared");
		});

		MenuItem resolveMaven = new MenuItem(maven.getSubMenu(), "Resolve");
		resolveMaven.setAction(() -> {
			manual.getSubMenu().hide();
			menu.hide();
			config.logStd("Resolving maven dependencies...");
			config.getDependencies().resolve(config.getClasspath()::getPom, config, true,
					() -> config.logStd("Finished resolving maven dependencies ("
							+ config.getDependencies().getResolvedJars().size() + " jars added)"));
		});
		addMenuItem(manual);
		addMenuItem(maven);

		getSubMenu().addOnShowing(() -> {
			manual.getSubMenu().clear();
			maven.getSubMenu().clear();

			manual.addMenuItem(addManual);
			manual.addMenuItem(clearManual);
			manual.getSubMenu().separate();

			if (config.getDependencies().getManualJars().isEmpty()) {
				MenuItem entDisp = new MenuItem(getSubMenu(), "(no manual dependencies)");
				entDisp.setDisable(true);

				manual.addMenuItem(entDisp);
			}

			config.getDependencies().getManualJars().forEach(entry -> {
				String disp = entry.getName();

				MenuItem entDisp = new MenuItem(getSubMenu(), disp);
				entDisp.setDisable(true);

				manual.addMenuItem(entDisp);
			});

			maven.addMenuItem(resolveMaven);
			maven.getSubMenu().separate();

			if (config.getDependencies().getResolvedJars().isEmpty()) {
				MenuItem entDisp = new MenuItem(getSubMenu(), "(no maven dependencies)");
				entDisp.setDisable(true);

				maven.addMenuItem(entDisp);
			}

			for (int i = 0; i < config.getDependencies().getResolvedJars().size() && i < 5; i++) {
				File jar = config.getDependencies().getResolvedJars().get(i);
				String disp = jar.getName();

				MenuItem entDisp = new MenuItem(getSubMenu(), disp);
				entDisp.setDisable(true);

				maven.addMenuItem(entDisp);
			}

			int remaining = config.getDependencies().getResolvedJars().size() - 5;
			if (remaining > 0) {
				MenuItem entDisp = new MenuItem(getSubMenu(), "and " + remaining + " others...");
				entDisp.setDisable(true);

				maven.addMenuItem(entDisp);
			}
		});
	}

}
