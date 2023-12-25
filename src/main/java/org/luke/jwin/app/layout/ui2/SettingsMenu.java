package org.luke.jwin.app.layout.ui2;

import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.MenuItem;
import org.luke.gui.window.Window;
import org.luke.jwin.app.layout.JwinUi;

public class SettingsMenu extends ContextMenu {

	public SettingsMenu(Window window, JwinUi config) {
		super(window);

		MenuItem fileMen = new FileMenu(this, config);

		MenuItem cpMen = new ClassPathMenu(this, config);

		MenuItem mcMen = new MainClassMenu(this, config);

		MenuItem jdkMen = new JdkMenu(this, config);

		MenuItem jreMen = new JreMenu(this, config);

		MenuItem deps = new DependencyMenu(this, config);

		MenuItem conMen = new BooleanSetting(this, "console", config.getConsole().checkedProperty());

		MenuItem adMen = new BooleanSetting(this, "run_as_admin", config.getAdmin().checkedProperty());

		addMenuItem(fileMen);
		addMenuItem(cpMen);
		addMenuItem(mcMen);
		addMenuItem(jdkMen);
		addMenuItem(deps);
		addMenuItem(jreMen);
		addMenuItem(conMen);
		addMenuItem(adMen);

		addMenuItem("more_settings", config.getMoreSettings()::show);
	}

}
