package org.luke.gui.controls.label.unkeyed;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.TextTransform;

import javafx.beans.binding.Bindings;
import javafx.scene.CacheHint;
import javafx.scene.Node;

public class Text extends javafx.scene.text.Text implements TextNode {

	private TextTransform transform = TextTransform.NONE;
	
	public Text(String val, Font font) {
		if (val != null) {
			setText(val);
		}
		setFont(font);
		
		setCache(true);
		setCacheHint(CacheHint.SPEED);
		
		opacityProperty().bind(Bindings.when(disabledProperty()).then(0.5).otherwise(1));
	}

	public Text(String val) {
		this(val, Font.DEFAULT);
	}

	public void set(String text) {
		setText(transform.apply(text));
	}

	public void setTransform(TextTransform transform) {
		this.transform = transform;
		setText(transform.apply(getText()));
	}
	
	@Override
	public void setFont(Font font) {
		setFont(font.getFont());
	}

	@Override
	public Node getNode() {
		return this;
	}

}
