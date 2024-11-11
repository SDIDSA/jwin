package org.luke.gui.controls.space;

import javafx.geometry.Orientation;
import javafx.scene.shape.Rectangle;

/**
 * extends the Rectangle class and represents a fixed-size space in a layout.
 *
 * @author SDIDSA
 */
public class FixedSpace extends Rectangle {

	/**
	 * Constructs a FixedSpace with the specified width and height.
	 *
	 * @param width  The width of the fixed space.
	 * @param height The height of the fixed space.
	 */
	public FixedSpace(double width, double height) {
		setWidth(width);
		setHeight(height);
		setVisible(false);
	}

	/**
	 * Constructs a FixedSpace with a single size and orientation. The size is
	 * applied to either the width or height based on the specified orientation.
	 *
	 * @param size The size of the fixed space.
	 * @param or   The orientation of the fixed space (HORIZONTAL or VERTICAL).
	 */
	public FixedSpace(double size, Orientation or) {
		this(or == Orientation.HORIZONTAL ? size : 1, or == Orientation.VERTICAL ? size : 1);
	}
}
