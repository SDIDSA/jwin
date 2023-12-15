package org.luke.gui.controls.space;

import javafx.geometry.Orientation;
import javafx.scene.shape.Rectangle;

public class FixedSpace extends Rectangle {
	public FixedSpace(double width, double height)  {
		setWidth(width);
		setHeight(height);
		setVisible(false);
	}
	
	public FixedSpace(double size, Orientation or) {
		this(or == Orientation.HORIZONTAL ? size : 1, or == Orientation.VERTICAL ? size : 1);
	}
}
