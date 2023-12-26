package org.luke.gui.controls.popup.context.items;

import org.luke.gui.controls.check.Check;
import org.luke.gui.controls.popup.context.ContextMenu;

import javafx.beans.property.BooleanProperty;
import javafx.scene.paint.Color;

/**
 * Custom implementation of a check menu item, extending {@link KeyedMenuItem}.
 * Represents a menu item with an associated check mark that can be toggled on
 * or off. Manages the appearance, behavior, and interaction of the check menu
 * item.
 *
 * @author SDIDSA
 */
public class CheckMenuItem extends KeyedMenuItem {

	private Check check;

	/**
	 * Constructs a check menu item with the specified parent context menu, key, and
	 * fill color.
	 *
	 * @param menu The parent {@link ContextMenu} to which this menu item belongs.
	 * @param key  The key or text associated with the menu item.
	 * @param fill The fill color of the menu item.
	 */
	public CheckMenuItem(ContextMenu menu, String key, Color fill) {
		super(menu, key, fill);

		check = new Check(menu.getOwner(), 14);

		check.invertedProperty().bind(active);

		check.setMouseTransparent(true);

		getChildren().add(check);

		setAction(() -> check.flip());

		setHideOnAction(false);
	}

	/**
	 * Sets whether the check mark of the menu item is checked.
	 *
	 * @param checked {@code true} to set the check mark as checked, {@code false}
	 *                otherwise.
	 */
	public void setChecked(boolean checked) {
		check.setChecked(checked);
	}

	/**
	 * Gets the boolean property representing the checked state of the check mark.
	 *
	 * @return The boolean property representing the checked state.
	 */
	public BooleanProperty checkedProperty() {
		return check.checkedProperty();
	}
}
