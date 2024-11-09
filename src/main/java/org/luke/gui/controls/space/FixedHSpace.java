package org.luke.gui.controls.space;

import javafx.geometry.Orientation;

/**
 * extends the FixedSpace class and represents a fixed horizontal space in a
 * layout.
 *
 * @author SDIDSA
 */
public class FixedHSpace extends FixedSpace {

	/**
	 * Constructs a FixedHSpace with the specified width.
	 *
	 * @param width The width of the fixed horizontal space.
	 */
	public FixedHSpace(double width) {
		super(width, Orientation.HORIZONTAL);
	}
}
