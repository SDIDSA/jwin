package org.luke.jwin.app.layout.settings;

import org.luke.gui.controls.alert.FullOverlay;
import org.luke.gui.window.Page;

public class SettingsPan extends FullOverlay {
	public SettingsPan(Page page) {
		super(page);
		
		AppSettings appSetts = new AppSettings(page.getWindow());
		
		appSetts.setMinWidth(0);
		appSetts.maxWidthProperty().bind(preRoot.widthProperty());
		
		preRoot.getChildren().add(appSetts);
	}
}
