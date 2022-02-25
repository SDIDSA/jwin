package org.luke.jwin.ui;

import javafx.collections.ListChangeListener.Change;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import jna.ITaskbarList3;
import jna.TaskbarPeer;

public class ProgressBar extends javafx.scene.control.ProgressBar {

	public ProgressBar(Stage stage) {

		getChildren().addListener((Change<? extends Node> c) -> {
			c.next();
			StackPane stack1 = (StackPane) c.getAddedSubList().get(0);
			Style.styleRegion(stack1);

			progressProperty().addListener((obs, ov, nv) -> {
				if(isIndeterminate()) {
					TaskbarPeer.setProgressState(stage, ITaskbarList3.TBPF_NOPROGRESS);
				}else {
					TaskbarPeer.setProgressState(stage, ITaskbarList3.TBPF_NORMAL);
					TaskbarPeer.setProgress(stage, nv.doubleValue());
				}
			});
		});
	}
}
