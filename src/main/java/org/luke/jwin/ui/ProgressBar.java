package org.luke.jwin.ui;

import javafx.collections.ListChangeListener.Change;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class ProgressBar extends javafx.scene.control.ProgressBar {
	public ProgressBar() {
		getChildren().addListener((Change<? extends Node> c) -> {
			c.next();
			StackPane stack1 = (StackPane) c.getAddedSubList().get(0);
			Style.styleRegion(stack1);
		});
	}
}
