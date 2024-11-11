package org.luke.gui.controls.label;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

/**
 * The {@code MultiText} class extends {@link TextFlow} and provides a flexible
 * container for managing multiple text nodes, links, and labels in a flow
 * layout. It supports adding, styling, and modifying various types of text
 * nodes dynamically.
 * <p>
 * The class includes methods for adding labels, text, and links, setting their
 * properties, and performing actions on them.
 * </p>
 * 
 * @author SDIDSA
 */
public class MultiText extends TextFlow {
	private final Window window;

	private final ArrayList<TextNode> textNodes;
	private final ArrayList<ColorItem> nodes;

	private Color fill;

	/**
	 * Constructs an empty {@code MultiText} with the specified window.
	 * 
	 * @param window The window associated with the multi-text.
	 */
	public MultiText(Window window) {
		this.window = window;
		textNodes = new ArrayList<>();
		nodes = new ArrayList<>();
	}

	/**
	 * Constructs a {@code MultiText} with a single label using the specified
	 * window, localization key, and font.
	 * 
	 * @param window The window associated with the multi-text.
	 * @param key    The localization key for the label.
	 * @param font   The font to use for the label.
	 */
	public MultiText(Window window, String key, Font font) {
		this(window);

		addLabel(key, font);
	}

	/**
	 * Sets the fill color for all text nodes in the multi-text.
	 * 
	 * @param fill The fill color to set.
	 */
	public void setFill(Color fill) {
		this.fill = fill;
		getChildren().forEach((e) -> {
			if (e instanceof javafx.scene.text.Text t) {
				t.setFill(fill);
			}
		});
		nodes.forEach(node -> node.setFill(fill));
	}

	/**
	 * Sets the localization key for the text node at the specified index.
	 * 
	 * @param index The index of the text node.
	 * @param key   The localization key to set.
	 * @throws IllegalArgumentException If the text node at the specified index is
	 *                                  not a {@link KeyedTextNode}.
	 */
	public void setKey(int index, String key) {
		if (textNodes.get(index) instanceof KeyedTextNode node) {
			node.setKey(key);
		} else {
			throw new IllegalArgumentException("the TextNode at " + index + " is not a KeyedTextNode");
		}
	}

	/**
	 * Sets the text transformation for the text node at the specified index.
	 * 
	 * @param index     The index of the text node.
	 * @param transform The text transformation to apply.
	 */
	public void setTransform(int index, TextTransform transform) {
		textNodes.get(index).setTransform(transform);
	}

	/**
	 * Sets the text transformation for the first text node in the multi-text.
	 * 
	 * @param transform The text transformation to apply.
	 */
	public void setTransform(TextTransform transform) {
		setTransform(0, transform);
	}

	/**
	 * Sets the action to be performed when the text node at the specified index is
	 * clicked.
	 * 
	 * @param index  The index of the text node.
	 * @param action The action to set.
	 * @throws IllegalArgumentException If the text node at the specified index is
	 *                                  not a {@link Link}.
	 */
	public void setAction(int index, Runnable action) {
		List<Link> links = textNodes.stream().filter(n -> n instanceof Link).map(n -> (Link) n)
				.collect(Collectors.toList());
		if (links.get(index) instanceof Link link) {
			link.setAction(action);
		} else {
			throw new IllegalArgumentException("the TextNode at " + index + " is not a Link");
		}
	}

	/**
	 * Sets the action to be performed when the first text node is clicked.
	 * 
	 * @param action The action to set.
	 */
	public void setAction(Runnable action) {
		setAction(0, action);
	}

	/**
	 * Sets the localization key for the first text node.
	 * 
	 * @param key The localization key to set.
	 */
	public void setKey(String key) {
		setKey(0, key);
	}

	/**
	 * Adds a labeled text node to the multi-text with the specified localization
	 * key and font.
	 * 
	 * @param key  The localization key for the label.
	 * @param font The font to use for the label.
	 */
	public void addLabel(String key, Font font) {
		addTextNode(new Label(window, key, font));
	}

	/**
	 * Adds a labeled text node to the multi-text with the specified localization
	 * key, using the default font.
	 * 
	 * @param key The localization key for the label.
	 */
	public void addLabel(String key) {
		addLabel(key, Font.DEFAULT);
	}

	/**
	 * Adds a plain text node to the multi-text with the specified value and font.
	 * 
	 * @param value The text value for the node.
	 * @param font  The font to use for the node.
	 */
	public void addText(String value, Font font) {
		addTextNode(new Text(value, font));
	}

	/**
	 * Adds a plain text node to the multi-text with the specified value, using the
	 * default font.
	 * 
	 * @param key The localization key for the label.
	 */
	public void addText(String key) {
		addText(key, Font.DEFAULT);
	}

	/**
	 * Adds a keyed link to the multi-text with the specified localization key and
	 * font.
	 * 
	 * @param key  The localization key for the link.
	 * @param font The font to use for the link.
	 */
	public void addKeyedLink(String key, Font font) {
		addTextNode(new KeyedLink(window, key, font));
	}

	/**
	 * Adds a keyed link to the multi-text with the specified localization key,
	 * using the default font.
	 * 
	 * @param key The localization key for the link.
	 */
	public void addKeyedLink(String key) {
		addKeyedLink(key, Font.DEFAULT);
	}

	/**
	 * Adds a link to the multi-text with the specified localization key and font.
	 * 
	 * @param key  The localization key for the link.
	 * @param font The font to use for the link.
	 */
	public void addLink(String key, Font font) {
		addTextNode(new Link(window, key, font));
	}

	/**
	 * Adds a space to the multi-text.
	 */
	public void addSpace() {
		addText(" ");
	}

	/**
	 * Adds a link to the multi-text with the specified localization key, using the
	 * default font.
	 * 
	 * @param key The localization key for the link.
	 */
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

	/**
	 * Adds a colored node to the multi-text at the specified index.
	 * 
	 * @param index The index at which to add the node.
	 * @param node  The colored node to add.
	 */
	public void addNode(int index, ColorItem node) {
		nodes.add(node);
		if (fill != null) {
			node.setFill(fill);
		}
		getChildren().add(index, node.getNode());
	}

	/**
	 * Centers the text in the multi-text.
	 */
	public void center() {
		setTextAlignment(TextAlignment.CENTER);
	}

	/**
	 * Clears all text nodes and colored nodes from the multi-text.
	 */
	public void clear() {
		textNodes.clear();
		nodes.clear();
		getChildren().clear();
	}
}