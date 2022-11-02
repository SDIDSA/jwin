package org.luke.gui.window.content;

import javafx.scene.Parent;
import javafx.scene.paint.Color;

public class TransparentScene extends javafx.scene.Scene {
	public TransparentScene(Parent root, double width, double height) {
		super(root, width, height);

		setFill(Color.TRANSPARENT);
	}
}
