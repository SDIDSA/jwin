package org.luke.jwin.app.layout.settings;

import org.luke.gui.window.Window;
import org.luke.jwin.app.layout.settings.abs.Settings;
import org.luke.jwin.app.layout.settings.abs.left.Section;
import org.luke.jwin.app.layout.settings.abs.left.SectionItem;
import org.luke.jwin.app.layout.settings.content.display.DisplaySettings;
import org.luke.jwin.app.layout.settings.content.gradle.GradleSettings;
import org.luke.jwin.app.layout.settings.content.jdk.JDKSettings;


public class AppSettings extends Settings {

	public AppSettings(Window win) {
		super(win);
		
		Section appSettings = new Section(this, "JWIN Settings", true);
		appSettings.addItem(new SectionItem(this, "Display", DisplaySettings.class));
		appSettings.addItem(new SectionItem(this, "JDK versions", JDKSettings.class));
		appSettings.addItem(new SectionItem(this, "Gradle versions", GradleSettings.class));

		sideBar.addSection(appSettings);
		sideBar.separate(win);
	}

}
