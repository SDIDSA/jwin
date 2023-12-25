package org.luke.jwin.app.layout.settings.abs.left;

import java.util.ArrayList;

import org.luke.gui.controls.space.FixedVSpace;
import org.luke.gui.controls.space.Separator;
import org.luke.gui.window.Window;
import org.luke.jwin.app.layout.settings.abs.Settings;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

public class SettingsMenu extends VBox {

	private ArrayList<Section> sections;

	public SettingsMenu(Settings settings) {
		setPadding(new Insets(30, 6, 30, 6));
		setMinWidth(218);
		setAlignment(Pos.TOP_CENTER);

		sections = new ArrayList<>();
	}

	public void addSection(Section section) {
		getChildren().add(section);
		sections.add(section);
	}

	public boolean fire(String match) {
		for (Section section : sections) {
			if(section.fire(match)) {
				return true;
			}
		}

		return false;
	}

	public void separate(Window win) {
		Separator sep = new Separator(win, Orientation.HORIZONTAL);
		getChildren().addAll(new FixedVSpace(10), sep, new FixedVSpace(8));
	}
}
