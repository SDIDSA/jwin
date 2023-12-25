package org.luke.jwin.app.layout.settings.content.display;

import org.luke.jwin.app.layout.settings.abs.Settings;
import org.luke.jwin.app.layout.settings.abs.SettingsContent;
import org.luke.jwin.app.layout.settings.content.display.layout.UiLayoutSetting;
import org.luke.jwin.app.layout.settings.content.display.theme.ThemeSetting;

public class DisplaySettings extends SettingsContent {

	public DisplaySettings(Settings settings) {
		super(settings);

		addHeader(settings.getWindow(), "color_theme");

		getChildren().add(new ThemeSetting(settings));

		separate(settings.getWindow(), 20);

		addHeader(settings.getWindow(), "ui_layout");

		getChildren().add(new UiLayoutSetting(settings));

		applyStyle(settings.getWindow().getStyl());
	}
}
