package org.luke.gui.controls.space;

import javafx.geometry.Orientation;
import javafx.scene.layout.Priority;

/**
 * extends the ExpandingSpace class and represents an expanding horizontal
 * space.
 *
 * @author SDIDSA
 */
public class ExpandingHSpace extends ExpandingSpace {

	/**
	 * Constructs an ExpandingHSpace with the specified priority for horizontal
	 * growth.
	 *
	 * @param priority The priority for growing horizontally within a layout.
	 */
	public ExpandingHSpace(Priority priority) {
		super(Orientation.HORIZONTAL, priority);
	}

	/**
	 * Constructs an ExpandingHSpace with the default priority for horizontal growth
	 * (ALWAYS).
	 */
	public ExpandingHSpace() {
		this(Priority.ALWAYS);
	}
}
