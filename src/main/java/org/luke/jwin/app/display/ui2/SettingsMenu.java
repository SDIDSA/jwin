package org.luke.jwin.app.display.ui2;

import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.window.Window;
import org.luke.jwin.app.display.JwinUi;

public class SettingsMenu extends ContextMenu {

	public SettingsMenu(Window window, JwinUi config) {
		super(window);

		FileMenu fileMen = new FileMenu(this, config);

		ClassPathMenu cpMen = new ClassPathMenu(this, config);

		MainClassMenu mcMen = new MainClassMenu(this, config);

		JdkMenu jdkMen = new JdkMenu(this, config);

		JreMenu jreMen = new JreMenu(this, config);

		DependencyMenu deps = new DependencyMenu(this, config);

		ConsoleMenu conMen = new ConsoleMenu(this, config);

		RunAsAdmin adMen = new RunAsAdmin(this, config);

		addMenuItem(fileMen);
		addMenuItem(cpMen);
		addMenuItem(mcMen);
		addMenuItem(jdkMen);
		addMenuItem(deps);
		addMenuItem(jreMen);
		addMenuItem(conMen);
		addMenuItem(adMen);

		addMenuItem("More settings", config.getMoreSettings()::show);
	}

}
