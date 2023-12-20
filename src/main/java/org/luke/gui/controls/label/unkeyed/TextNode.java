package org.luke.gui.controls.label.unkeyed;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.TextTransform;
import org.luke.gui.style.ColorItem;

import javafx.scene.Node;

public interface TextNode extends ColorItem {
	void setFont(Font font);
	void setTransform(TextTransform tt);
	Node getNode();
}
