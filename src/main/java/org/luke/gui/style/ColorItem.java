package org.luke.gui.style;

import javafx.scene.Node;
import javafx.scene.paint.Paint;

/**
 * The {@code ColorItem} interface represents an item that can be colored.
 * Implementing classes should provide methods to set the fill color and
 * retrieve the associated JavaFX node.
 *
 * @author SDIDSA
 */
public interface ColorItem {
	
	/**
	 * Sets the fill color of the item.
	 *
	 * @param fill The color to set for the item.
	 */
	void setFill(Paint fill);

	/**
     * Retrieves the JavaFX node associated with this color item.
     *
     * @return The JavaFX node representing the color item.
     */
	Node getNode();
}
