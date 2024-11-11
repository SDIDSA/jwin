
package org.luke.gui.controls.space;

import javafx.geometry.Orientation;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Pane;

/**
 * extends Pane and is designed to be used in JavaFX layouts to create expanding
 * spaces.
 *
 * @author SDIDSA
 */
public class ExpandingSpace extends Pane {

	/**
	 * Constructs an ExpandingSpace with the specified orientation and priority.
	 *
	 * @param or       The orientation of the expanding space (HORIZONTAL or
	 *                 VERTICAL).
	 * @param priority The priority for growing within a layout.
	 */
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

	/**
	 * Constructs an ExpandingSpace with the specified orientation and default
	 * priority (ALWAYS).
	 *
	 * @param or The orientation of the expanding space (HORIZONTAL or VERTICAL).
	 */
	public ExpandingSpace(Orientation or) {
		this(or, Priority.ALWAYS);
	}
}
