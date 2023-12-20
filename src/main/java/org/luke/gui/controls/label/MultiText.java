package org.luke.gui.controls.label;

import java.util.ArrayList;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.keyed.KeyedLink;
import org.luke.gui.controls.label.keyed.KeyedTextNode;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.controls.label.unkeyed.Link;
import org.luke.gui.controls.label.unkeyed.Text;
import org.luke.gui.controls.label.unkeyed.TextNode;
import org.luke.gui.style.ColorItem;
import org.luke.gui.window.Window;

import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

public class MultiText extends TextFlow {
	private Window window;

	private ArrayList<TextNode> textNodes;
	private ArrayList<ColorItem> nodes;

	private Color fill;

	public MultiText(Window window) {
		this.window = window;
		textNodes = new ArrayList<>();
		nodes = new ArrayList<>();
	}

	public MultiText(Window window, String key, Font font) {
		this(window);

		addLabel(key, font);
	}

	public void setFill(Color fill) {
		this.fill = fill;
		getChildren().forEach((e) -> {
			if(e instanceof javafx.scene.text.Text t) {
				t.setFill(fill);
			}
		});
		nodes.forEach(node -> node.setFill(fill));
	}

	public void setKey(int index, String key) {
		if (textNodes.get(index)instanceof KeyedTextNode node) {
			node.setKey(key);
		} else {
			throw new IllegalArgumentException("the TextNode at " + index + " is not a KeyedTextNode");
		}
	}

	public void setTransform(int index, TextTransform transform) {
		textNodes.get(index).setTransform(transform);
	}

	public void setTransform(TextTransform transform) {
		setTransform(0, transform);
	}

	public void setAction(int index, Runnable action) {
		if (textNodes.get(index)instanceof Link link) {
			link.setAction(action);
		} else {
			throw new IllegalArgumentException("the TextNode at " + index + " is not a Link");
		}
	}

	public void setKey(String key) {
		setKey(0, key);
	}

	public void addLabel(String key, Font font) {
		addTextNode(new Label(window, key, font));
	}

	public void addLabel(String key) {
		addLabel(key, Font.DEFAULT);
	}

	public void addText(String value, Font font) {
		addTextNode(new Text(value, font));
	}

	public void addText(String key) {
		addText(key, Font.DEFAULT);
	}

	public void addKeyedLink(String key, Font font) {
		addTextNode(new KeyedLink(window, key, font));
	}

	public void addKeyedLink(String key) {
		addKeyedLink(key, Font.DEFAULT);
	}

	public void addLink(String key, Font font) {
		addTextNode(new Link(window, key, font));
	}

	public void addLink(String key) {
		addLink(key, Font.DEFAULT);
	}

	private void addTextNode(TextNode node) {
		textNodes.add(node);
		addNode(node);
	}

	private void addNode(ColorItem node) {
		nodes.add(node);
		if (fill != null) {
			node.setFill(fill);
		}
		getChildren().add(node.getNode());
	}

	public void addNode(int index, ColorItem node) {
		nodes.add(node);
		if (fill != null) {
			node.setFill(fill);
		}
		getChildren().add(index, node.getNode());
	}

	public void center() {
		setTextAlignment(TextAlignment.CENTER);
	}

	public void clear() {
		textNodes.clear();
		nodes.clear();
		getChildren().clear();
	}
}