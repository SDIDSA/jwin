package org.luke.gui.controls.alert;

import org.luke.gui.factory.Backgrounds;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Page;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * specialized overlay that covers the entire area of its
 * owner, providing a full-screen overlay effect. It inherits from the Overlay
 * class and implements the Styleable interface for consistent styling.
 *
 * @author SDIDSA
 */
public class FullOverlay extends Overlay implements Styleable {
	protected StackPane preRoot;

	/**
	 * Constructs a FullOverlay instance with the specified owner Page.
	 *
	 * @param session The Page instance representing the owner of the overlay.
	 */
	public FullOverlay(Page session) {
		super(session);

		preRoot = new StackPane();
		preRoot.setAlignment(Pos.TOP_RIGHT);
		preRoot.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
		preRoot.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);

		preRoot.prefWidthProperty().bind(session.widthProperty().subtract(15));
		preRoot.prefHeightProperty().bind(session.heightProperty().subtract(15));

		setContent(preRoot);

		applyStyle(session.getWindow().getStyl());
	}

	/**
	 * Sets the owner of the FullOverlay, updating its size to cover the entire area
	 * of the new owner.
	 *
	 * @param owner The Pane representing the new owner of the FullOverlay.
	 */
	@Override
	public void setOwner(Pane owner) {
		preRoot.prefWidthProperty().unbind();
		preRoot.prefHeightProperty().unbind();
		preRoot.prefWidthProperty().bind(owner.widthProperty().subtract(15));
		preRoot.prefHeightProperty().bind(owner.heightProperty().subtract(15));
		super.setOwner(owner);
	}

	@Override
	public void applyStyle(Style style) {
		preRoot.setBackground(Backgrounds.make(style.getBackgroundPrimaryOr(), 5));
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
