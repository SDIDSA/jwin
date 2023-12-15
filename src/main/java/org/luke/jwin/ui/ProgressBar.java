package org.luke.jwin.ui;

import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import jna.ITaskbarList3;
import jna.TaskbarPeer;

public class ProgressBar extends javafx.scene.control.ProgressBar implements Styleable {
	private Window stage;
	
	public ProgressBar(Window stage) {
		this.stage = stage;
		
		applyStyle(stage.getStyl());
	}

	@Override
	public void applyStyle(Style style) {
		getChildren().addListener((Change<? extends Node> c) -> {
			c.next();
			StackPane stack1 = (StackPane) c.getAddedSubList().get(0);
			Styler.styleRegion(style, stack1);

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

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
