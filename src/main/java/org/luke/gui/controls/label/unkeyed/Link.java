package org.luke.gui.controls.label.unkeyed;

import org.luke.gui.NodeUtils;
import org.luke.gui.controls.Font;
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

public class Link extends StackPane implements Styleable, TextNode {
	protected Text label;

	private Runnable action;

	public Link(Window window, String val, Font font, boolean keyed) {
		getStyleClass().addAll("link", val);

		label = keyed ? new Label(window, val, font):new Text(val, font);

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

	public Link(Window window, String key, boolean keyed) {
		this(window, key, Font.DEFAULT, keyed);
	}

	public Link(Window window, String key, Font font) {
		this(window, key, font, false);
	}

	public Link(Window window, String key) {
		this(window, key, Font.DEFAULT);
	}

	protected void fire(MouseEvent dismiss) {
		fire();
	}

	protected void fire(KeyEvent e) {
		if (e.getCode().equals(KeyCode.SPACE)) {
			fire();
		}
	}

	public void fire() {
		if (action != null) {
			action.run();
		}
	}

	public void setAction(Runnable action) {
		this.action = action;
	}

	public void setFont(Font font) {
		label.setFont(font);
	}

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
}
