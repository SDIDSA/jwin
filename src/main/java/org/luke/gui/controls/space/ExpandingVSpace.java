package org.luke.gui.controls.space;

import javafx.geometry.Orientation;

/**
 * extends the ExpandingSpace class and represents an expanding vertical space.
 *
 * @author SDIDSA
 */
public class ExpandingVSpace extends ExpandingSpace {

	/**
	 * Constructs an ExpandingVSpace with the default priority for vertical growth
	 * (ALWAYS).
	 */
	public ExpandingVSpace() {
		super(Orientation.VERTICAL);
	}
}
