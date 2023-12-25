package org.luke.jwin.app.layout.settings.content.display.layout;

import org.luke.gui.controls.check.RadioGroup;
import org.luke.jwin.app.Jwin;
import org.luke.jwin.app.layout.settings.abs.Settings;
import org.luke.jwin.local.LocalStore;

import javafx.scene.layout.HBox;

public class UiLayoutSetting extends HBox {
	public UiLayoutSetting(Settings settings) {
		super(15);

		UiLayoutOption sim = new UiLayoutOption(settings.getWindow(), "simplified", "sim");

		UiLayoutOption man = new UiLayoutOption(settings.getWindow(), "advanced", "adv");
		
		if(LocalStore.getUiLayout().equals("sim")) {
			sim.getCheck().flip();
		}else {
			man.getCheck().flip();
		}

		RadioGroup group = new RadioGroup(man.getCheck(), sim.getCheck());

		group.valueProperty().addListener((obs, ov, nv) -> {
			if (nv == man.getCheck()) {
				Jwin.instance.getHome().setUiLayout("adv");
			} else {
				Jwin.instance.getHome().setUiLayout("sim");
			}
		});

		getChildren().addAll(man, sim);
	}
}
