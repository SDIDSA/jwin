package org.luke.gui.controls.check;

import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Cursor;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;

/**
 * Radio is a JavaFX control that represents a stylized radio button. It
 * consists of two concentric circles, an outer circle representing the radio
 * button's border and an inner circle indicating the selected state.
 *
 * @author SDIDSA
 */
public class Radio extends StackPane implements Styleable {
	private BooleanProperty checked;
	private BooleanProperty inverted;

	private Circle outer;
	private Circle inner;

	/**
	 * Constructs a Radio instance with the specified window and size.
	 *
	 * @param window The associated Window for styling.
	 * @param size   The size of the radio button.
	 */
	public Radio(Window window, double size) {
		setMinSize(size, size);
		setMaxSize(size, size);

		inverted = new SimpleBooleanProperty(false);
		checked = new SimpleBooleanProperty(false);

		outer = new Circle(size / 2);
		outer.setFill(Color.TRANSPARENT);
		outer.setStrokeWidth(Math.max((int) (size / 10), 2));
		outer.setStrokeType(StrokeType.INSIDE);

		inner = new Circle((int) ((size / 2) - (size / 4.5)));

		getChildren().addAll(outer, inner);

		inner.visibleProperty().bind(checked);

		setCursor(Cursor.HAND);
		setOnMouseClicked(e -> flip());

		applyStyle(window.getStyl());
	}

	/**
	 * Flips the check status to true.
	 */
	public void flip() {
		setChecked(true);
	}

	/**
	 * Gets the BooleanProperty for the checked property.
	 *
	 * @return The BooleanProperty for the checked property.
	 */
	public BooleanProperty checkedProperty() {
		return checked;
	}

	/**
	 * Gets the BooleanProperty for the inverted property.
	 *
	 * @return The BooleanProperty for the inverted property.
	 */
	public BooleanProperty invertedProperty() {
		return inverted;
	}

	/**
	 * Sets the checked property to the specified value.
	 *
	 * @param val The value to set for the checked property.
	 */
	public void setChecked(boolean val) {
		checked.set(val);
	}

	@Override
	public void applyStyle(Style style) {
		inner.fillProperty()
				.bind(Bindings.when(inverted).then(style.getInteractiveActive()).otherwise(style.getTextNormal()));
		outer.strokeProperty().bind(Bindings.when(checked.or(inverted)).then(style.getInteractiveActive())
				.otherwise(style.getInteractiveNormal()));
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
