package org.luke.gui.controls.label.unkeyed;

import org.luke.gui.NodeUtils;
import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.TextTransform;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;

/**
 * The {@code Link} class represents a clickable link within a GUI. It extends
 * {@link StackPane} and implements the {@link Styleable} and {@link TextNode}
 * interfaces. The link can be created with either a simple text label or a
 * localized, keyed label.
 * 
 * @author SDIDSA
 */
public class Link extends StackPane implements Styleable, TextNode {
	protected Text label;

	private Runnable action;

	/**
	 * Constructs a link with the specified window, text value, font, and keyed
	 * status.
	 * 
	 * @param window The window associated with the link.
	 * @param val    The text value of the link.
	 * @param font   The font to use for the link.
	 * @param keyed  Specifies whether the link is localized with a key.
	 */
	public Link(Window window, String val, Font font, boolean keyed) {
		getStyleClass().addAll("link", val);

		label = keyed ? new Label(window, val, font) : new Text(val, font);

		label.underlineProperty().bind(hoverProperty());
		getChildren().add(label);

		prefWidthProperty()
				.bind(Bindings.createDoubleBinding(() -> label.getBoundsInLocal().getWidth() + (isFocused() ? 8 : 0),
						label.boundsInLocalProperty(), focusedProperty()));

		prefHeightProperty().bind(Bindings.createDoubleBinding(() -> label.getBoundsInLocal().getHeight() + 4,
				label.boundsInLocalProperty(), focusedProperty()));

		setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
		setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);

		setFocusTraversable(true);

		setOnMouseClicked(this::fire);
		setOnKeyPressed(this::fire);

		setCursor(Cursor.HAND);

		applyStyle(window.getStyl());
	}

	/**
	 * Constructs a link with the specified window, key, and keyed status, using the
	 * default font.
	 * 
	 * @param window The window associated with the link.
	 * @param key    The localization key or text value of the link.
	 * @param keyed  Specifies whether the link is localized with a key.
	 */
	public Link(Window window, String key, boolean keyed) {
		this(window, key, Font.DEFAULT, keyed);
	}

	/**
	 * Constructs a link with the specified window, key, and font.
	 * 
	 * @param window The window associated with the link.
	 * @param key    The localization key or text value of the link.
	 * @param font   The font to use for the link.
	 */
	public Link(Window window, String key, Font font) {
		this(window, key, font, false);
	}

	/**
	 * Constructs a link with the specified window and key, using the default font.
	 * 
	 * @param window The window associated with the link.
	 * @param key    The localization key or text value of the link.
	 */
	public Link(Window window, String key) {
		this(window, key, Font.DEFAULT);
	}

	/**
	 * Fires the action associated with this link.
	 */
	protected void fire(MouseEvent dismiss) {
		fire();
	}

	protected void fire(KeyEvent e) {
		if (e.getCode().equals(KeyCode.SPACE)) {
			fire();
			e.consume();
		}
	}

	public void fire() {
		if (action != null) {
			action.run();
		}
	}

	/**
	 * Sets the action to be performed when the link is clicked.
	 * 
	 * @param action The action to set.
	 */
	public void setAction(Runnable action) {
		this.action = action;
	}

	public void setFont(Font font) {
		label.setFont(font);
	}

	/**
	 * Gets the text value of the link.
	 * 
	 * @return The text value of the link.
	 */
	public String getText() {
		return label.getText();
	}

	@Override
	public Node getNode() {
		return this;
	}

	@Override
	public void applyStyle(Style style) {
		label.setFill(style.getTextLink());
		NodeUtils.focusBorder(this, style.getTextLink());
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

	/**
	 * Sets the fill color for the link. Links must use a unified color, so this
	 * method does nothing.
	 * 
	 * @param fill The fill color to set (ignored).
	 */
	@Override
	public void setFill(Paint fill) {
		// links shouldn't be recolored ?
	}

	@Override
	public void setTransform(TextTransform tt) {
		label.setTransform(tt);
	}
}
