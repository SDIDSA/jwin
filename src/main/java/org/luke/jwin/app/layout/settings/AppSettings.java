package org.luke.jwin.app.layout.settings;

import org.luke.gui.window.Window;
import org.luke.jwin.app.layout.settings.abs.Settings;
import org.luke.jwin.app.layout.settings.abs.left.Section;
import org.luke.jwin.app.layout.settings.abs.left.SectionItem;
import org.luke.jwin.app.layout.settings.content.display.DisplaySettings;
import org.luke.jwin.app.layout.settings.content.gradle.GradleSettings;
import org.luke.jwin.app.layout.settings.content.jdk.JdkSettings;


public class AppSettings extends Settings {

	public AppSettings(Window win) {
		super(win);
		
		Section appSettings = new Section(this, "jwin_settings", true);
		appSettings.addItem(new SectionItem(this, "display", DisplaySettings.class));
		appSettings.addItem(new SectionItem(this, "jdk_versions", JdkSettings.class));
		appSettings.addItem(new SectionItem(this, "gradle_versions", GradleSettings.class));

		sideBar.addSection(appSettings);
		sideBar.separate(win);

		fire("display");
	}
	
	public boolean fire(String match) {
		return sideBar.fire(match);
	}

}
