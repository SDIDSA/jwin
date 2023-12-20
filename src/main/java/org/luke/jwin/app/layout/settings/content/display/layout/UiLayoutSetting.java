package org.luke.jwin.app.layout.settings.content.display.layout;

import org.luke.gui.controls.check.RadioGroup;
import org.luke.gui.window.Page;
import org.luke.jwin.app.JwinHome;
import org.luke.jwin.app.layout.JwinUi;
import org.luke.jwin.app.layout.JwinUi1;
import org.luke.jwin.app.layout.settings.abs.Settings;
import org.luke.jwin.app.layout.ui2.JwinUi2;

import javafx.scene.layout.HBox;

public class UiLayoutSetting extends HBox {
	public UiLayoutSetting(Settings settings) {
		super(15);

		UiLayoutOption sim = new UiLayoutOption(settings.getWindow(), "Simplified", "simplified");

		UiLayoutOption man = new UiLayoutOption(settings.getWindow(), "Advanced", "manual");
		
		sim.getCheck().flip();

		RadioGroup group = new RadioGroup(man.getCheck(), sim.getCheck());

		group.valueProperty().addListener((obs, ov, nv) -> {
			Page p = settings.getWindow().getLoadedPage();
			if (p instanceof JwinHome home) {
				if (nv == man.getCheck()) {
					setConfig(settings, home, JwinUi1.class);
				} else {
					setConfig(settings, home, JwinUi2.class);
				}
			}
		});

		getChildren().addAll(man, sim);
	}

	private void setConfig(Settings setts, JwinHome home, Class<? extends JwinUi> type) {
		home.setConfig(type);
	}
}
