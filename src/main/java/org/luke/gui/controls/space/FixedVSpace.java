package org.luke.gui.controls.space;

import javafx.geometry.Orientation;

/**
 * extends the FixedSpace class and represents a fixed
 * vertical space in a layout.
 *
 * @author SDIDSA
 */
public class FixedVSpace extends FixedSpace {

	/**
	 * Constructs a FixedVSpace with the specified height.
	 *
	 * @param height The height of the fixed vertical space.
	 */
	public FixedVSpace(double height) {
		super(height, Orientation.VERTICAL);
	}
}
