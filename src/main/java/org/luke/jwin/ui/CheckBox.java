package org.luke.jwin.ui;

import javafx.collections.ListChangeListener.Change;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class CheckBox extends javafx.scene.control.CheckBox {
	public CheckBox(String text) {
		super(Character.toUpperCase(text.charAt(0)) + text.substring(1));
		getChildren().addListener((Change<? extends Node> c) -> {
			c.next();
			Node node = c.getAddedSubList().get(0);
			if (node instanceof StackPane stack) {
				Style.styleRegion(stack);
			}
		});
		setCursor(Cursor.HAND);
	}
}
