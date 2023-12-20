package org.luke.jwin.app.layout.settings.content.display;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.controls.space.FixedVSpace;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.jwin.app.layout.settings.abs.Settings;
import org.luke.jwin.app.layout.settings.abs.SettingsContent;
import org.luke.jwin.app.layout.settings.content.display.layout.UiLayoutSetting;
import org.luke.jwin.app.layout.settings.content.display.theme.ThemeSetting;

import javafx.beans.property.ObjectProperty;

public class DisplaySettings extends SettingsContent implements Styleable {

	private Label themeHead;
	private Label uiLayoutHead;

	public DisplaySettings(Settings settings) {
		super(settings);

		themeHead = new Label(settings.getWindow(), "Color Theme", new Font(Font.DEFAULT_FAMILY_MEDIUM, 16));

		uiLayoutHead = new Label(settings.getWindow(), "Ui Layout", new Font(Font.DEFAULT_FAMILY_MEDIUM, 16));

		getChildren().addAll(themeHead, new FixedVSpace(20), new ThemeSetting(settings));

		separate(settings.getWindow(), 20);

		getChildren().addAll(uiLayoutHead, new FixedVSpace(20), new UiLayoutSetting(settings));

		applyStyle(settings.getWindow().getStyl());
	}

	@Override
	public void applyStyle(Style style) {
		themeHead.setFill(style.getHeaderPrimary());
		uiLayoutHead.setFill(style.getHeaderPrimary());
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
