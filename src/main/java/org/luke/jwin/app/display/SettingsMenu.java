package org.luke.jwin.app.display;

import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.window.Window;

public class SettingsMenu extends ContextMenu {

	public SettingsMenu(Window window, JwinUi config) {
		super(window);
		
		FileMenu fileMen = new FileMenu(this, config);

		ClassPathMenu cpMen = new ClassPathMenu(this, config);
		
		MainClassMenu mcMen = new MainClassMenu(this, config);
		
		JdkMenu jdkMen = new JdkMenu(this, config);
		
		JreMenu jreMen = new JreMenu(this, config);
		
		DependencyMenu deps = new DependencyMenu(this, config);

		
		addMenuItem(fileMen);
		addMenuItem(cpMen);
		addMenuItem(mcMen);
		addMenuItem(jdkMen);
		addMenuItem(deps);
		addMenuItem(jreMen);
	}

}
