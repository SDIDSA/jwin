package org.luke.gui.controls.label.unkeyed;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.TextTransform;
import org.luke.gui.style.ColorItem;

import javafx.scene.Node;

/**
 * The {@code TextNode} interface represents a node that displays text and
 * supports color, font, and text transformation. Implementing classes should
 * provide methods to set the font, text transformation, and retrieve the
 * associated JavaFX node.
 *
 * @author SDIDSA
 */
public interface TextNode extends ColorItem {
	/**
	 * Sets the font for displaying text.
	 *
	 * @param font The font to set for the text.
	 */
	void setFont(Font font);

	/**
	 * Sets the text transformation for displaying text.
	 *
	 * @param textTransform The text transformation to apply.
	 */
	void setTransform(TextTransform textTransform);

	/**
	 * Retrieves the JavaFX node associated with this text node.
	 *
	 * @return The JavaFX node representing the text node.
	 */
	Node getNode();
}
