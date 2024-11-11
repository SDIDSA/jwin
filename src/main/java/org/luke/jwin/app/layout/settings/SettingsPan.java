package org.luke.jwin.app.layout.settings;

import org.luke.gui.controls.alert.FullOverlay;
import org.luke.gui.window.Page;

public class SettingsPan extends FullOverlay {
	
	private final AppSettings appSetts;
	public SettingsPan(Page page) {
		super(page);
		
		appSetts = new AppSettings(page.getWindow());
		
		appSetts.setMinWidth(0);
		appSetts.maxWidthProperty().bind(preRoot.widthProperty());
		
		preRoot.getChildren().add(appSetts);
	}
	
	public boolean fire(String match) {
		return appSetts.fire(match);
	}
}
