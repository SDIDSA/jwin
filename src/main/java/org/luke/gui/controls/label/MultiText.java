package org.luke.gui.controls.label;

import java.util.ArrayList;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.keyed.KeyedLink;
import org.luke.gui.controls.label.keyed.KeyedTextNode;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.controls.label.unkeyed.Link;
import org.luke.gui.controls.label.unkeyed.Text;
import org.luke.gui.controls.label.unkeyed.TextNode;
import org.luke.gui.window.Window;

import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

public class MultiText extends TextFlow {
	private Window window;

	private ArrayList<TextNode> nodes;

	private Color fill;
	
	public MultiText(Window window) {
		this.window = window;
		nodes = new ArrayList<>();
	}

	public MultiText(Window window, String key, Font font) {
		this(window);
		
		addKeyedLabel(key, font);
	}
	
	public void setFill(Color fill) {
		this.fill = fill;
		nodes.forEach(node -> {
			if(node instanceof Text label) {
				label.setFill(fill);
			}
		});
	}

	public void setKey(int index, String key) {
		if(nodes.get(index) instanceof KeyedTextNode node) {
			node.setKey(key);
		}else {
			throw new IllegalArgumentException("the TextNode at " + index + " is not a KeyedTextNode");
		}
	}

	public void setAction(int index, Runnable action) {
		if(nodes.get(index) instanceof Link link) {
			link.setAction(action);
		}else {
			throw new IllegalArgumentException("the TextNode at " + index + " is not a Link");
		}
	}

	public void setKey(String key) {
		setKey(0, key);
	}

	public void addKeyedLabel(String key, Font font) {
		Label lab = new Label(window, key, font);
		if(fill != null) {
			lab.setFill(fill);
		}
		addNode(lab);
	}

	public void addKeyedLabel(String key) {
		addKeyedLabel(key, Font.DEFAULT);
	}

	public void addLabel(String txt, Font font) {
		Text lab = new Text(txt, font);
		if(fill != null) {
			lab.setFill(fill);
		}
		addNode(lab);
	}

	public void addLabel(String txt) {
		addLabel(txt, Font.DEFAULT);
	}

	public void addLink(String key, Font font) {
		addNode(new Link(window, key, font));
	}

	public void addKeyedLink(String key, Font font) {
		addNode(new KeyedLink(window, key, font));
	}

	public void addLink(String key) {
		addLink(key, Font.DEFAULT);
	}

	private void addNode(TextNode node) {
		nodes.add(node);
		getChildren().add(node.getNode());
	}
	
	public void clear() {
		nodes.clear();
		getChildren().clear();
	}
	
	public void center() {
		setTextAlignment(TextAlignment.CENTER);
	}
}
