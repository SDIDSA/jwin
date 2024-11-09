package org.luke.jwin.app.layout.ui2;

import java.io.File;

import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.KeyedMenuItem;
import org.luke.gui.controls.popup.context.items.MenuItem;
import org.luke.gui.controls.popup.context.items.MenuMenuItem;
import org.luke.gui.locale.Locale;
import org.luke.jwin.app.Jwin;
import org.luke.jwin.app.layout.JwinUi;

public class DependencyMenu extends MenuMenuItem {

	public DependencyMenu(ContextMenu menu, JwinUi config) {
		super(menu, "dependencies");

		MenuMenuItem manual = new MenuMenuItem(menu, "manual_dependencies");
		MenuMenuItem auto = new MenuMenuItem(menu, "resolved_dependencies");

		MenuItem addManual = new MenuItem(manual.getSubMenu(), "add", true);
		addManual.setAction(() -> {
			manual.getSubMenu().hide();
			menu.hide();
			config.getDependencies().addJars(config);
		});

		MenuItem clearManual = new MenuItem(manual.getSubMenu(), "clear", true);
		clearManual.setAction(() -> {
			manual.getSubMenu().hide();
			menu.hide();
			config.getDependencies().clearManuals();
			config.logStd("Manual dependencies were cleared");
		});

		MenuItem resolve = new MenuItem(auto.getSubMenu(), "resolve", true);
		resolve.setAction(() -> {
			manual.getSubMenu().hide();
			menu.hide();
			config.setState("resolving_dependencies");
			config.getDependencies().resolve(config.getClasspath().getRoot(), (_) -> config.setState("idle"));
		});
		addMenuItem(manual);
		addMenuItem(auto);

		getSubMenu().addOnShowing(() -> {
			manual.getSubMenu().clear();
			auto.getSubMenu().clear();

			manual.addMenuItem(addManual);
			manual.addMenuItem(clearManual);
			manual.getSubMenu().separate();

			if (config.getDependencies().getManualJars().isEmpty()) {
				MenuItem entDisp = new MenuItem(getSubMenu(), "0_man_deps", true);
				entDisp.setDisable(true);

				manual.addMenuItem(entDisp);
			}

			config.getDependencies().getManualJars().forEach(entry -> {
				String disp = entry.getName();

				MenuItem entDisp = new MenuItem(getSubMenu(), disp);
				entDisp.setDisable(true);

				manual.addMenuItem(entDisp);
			});

			auto.addMenuItem(resolve);
			auto.getSubMenu().separate();

			if (config.getDependencies().getResolvedJars().isEmpty()) {
				MenuItem entDisp = new MenuItem(getSubMenu(), "0_auto_deps", true);
				entDisp.setDisable(true);

				auto.addMenuItem(entDisp);
			}

			for (int i = 0; i < config.getDependencies().getResolvedJars().size() && i < 5; i++) {
				File jar = config.getDependencies().getResolvedJars().get(i);
				String disp = jar.getName();

				MenuItem entDisp = new MenuItem(getSubMenu(), disp);
				entDisp.setDisable(true);

				auto.addMenuItem(entDisp);
			}

			int remaining = config.getDependencies().getResolvedJars().size() - 5;
			if (remaining > 0) {
				MenuItem entDisp = new KeyedMenuItem(getSubMenu(), Locale.key( "and_other_deps", "count", remaining));
				entDisp.setDisable(true);

				auto.addMenuItem(entDisp);
			}
		});
	}

}
