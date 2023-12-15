package org.luke.gui.controls.space;

import javafx.geometry.Orientation;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Pane;

public class ExpandingSpace extends Pane {
	public ExpandingSpace(Orientation or, Priority priority) {
		switch (or) {
		case HORIZONTAL:
			HBox.setHgrow(this, priority);
			GridPane.setHgrow(this, priority);
			break;
		case VERTICAL:
			VBox.setVgrow(this, priority);
			GridPane.setVgrow(this, priority);
			break;
		default:
			break;
		}
		
		setMouseTransparent(true);
	}
	
	public ExpandingSpace(Orientation or) {
		this(or, Priority.ALWAYS);
	}
}
