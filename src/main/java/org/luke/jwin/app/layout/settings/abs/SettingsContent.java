package org.luke.jwin.app.layout.settings.abs;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.space.Separator;
import org.luke.gui.window.Window;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.layout.VBox;

public class SettingsContent extends VBox {
	protected Font header = new Font(Font.DEFAULT_FAMILY_MEDIUM, 20);

	public SettingsContent(Settings settings) {
		setPadding(new Insets(30, 40, 80, 40));
		
		setMinWidth(0);
		maxWidthProperty().bind(settings.contentWidth());
	}

	public void separate(Window win, double margin) {
		Separator sep = new Separator(win, Orientation.HORIZONTAL);
		setMargin(sep, new Insets(margin, 0, margin, 0));
		getChildren().add(sep);
	}
}
