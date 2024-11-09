package org.luke.gui.controls.check;

import javafx.geometry.NodeOrientation;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.factory.Borders;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Cursor;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * custom checkbox with a stylized check mark.
 *
 * @author SDIDSA
 */
public class Check extends StackPane implements Styleable {
	private final BooleanProperty checked;
	private final BooleanProperty inverted;

	private final Pane tick;

	/**
	 * Constructs a Check instance with the specified window and size.
	 *
	 * @param window The associated Window for styling.
	 * @param size   The size of the check mark.
	 */
	public Check(Window window, double size) {
		setMinSize(size, size);
		setMaxSize(size, size);

		setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);

		inverted = new SimpleBooleanProperty(false);
		checked = new SimpleBooleanProperty(false);

		tick = new Pane();
		tick.setMinSize(size / 3.5, size / 1.6);
		tick.setMaxSize(size / 3.5, size / 1.6);
		tick.setTranslateY(-size / 15);
		tick.setTranslateX(-size / 40);
		tick.setRotate(45);
		tick.setMouseTransparent(true);

		getChildren().add(tick);

		tick.visibleProperty().bind(checked);

		setCursor(Cursor.HAND);
		setOnMouseClicked(e -> flip());

		applyStyle(window.getStyl());
	}

	/**
	 * Flips the check status, toggling between checked and unchecked.
	 */
	public void flip() {
		checked.set(!checked.get());
	}

	private ChangeListener<Boolean> listener;

	@Override
	public void applyStyle(Style style) {
		Runnable restyle = () -> {
			boolean checkedVal = this.checked.get();
			boolean invertedVal = this.inverted.get();

			if (checkedVal) {
				if (invertedVal) {
					setBorder(Borders.make(style.getTextOnAccent(), 2.0, 2.0));
					setBackground(Backgrounds.make(style.getTextOnAccent(), 2.0));
					tick.setBorder(Borders.make(style.getAccent(), new BorderWidths(0, 2, 2, 0)));
				} else {
					setBorder(Borders.make(style.getAccent(), 2.0, 2.0));
					setBackground(Backgrounds.make(style.getAccent(), 2.0));
					tick.setBorder(Borders.make(style.getTextOnAccent(), new BorderWidths(0, 2, 2, 0)));
				}
			} else {
				setBackground(Background.EMPTY);
				if (invertedVal) {
					setBorder(Borders.make(style.getTextOnAccent(), 2.0, 2.0));
				} else {
					setBorder(Borders.make(style.getChannelsDefault(), 2.0, 2.0));
				}
			}
		};

		restyle.run();

		if (listener != null) {
			checked.removeListener(listener);
			inverted.removeListener(listener);
		}

		listener = (obs, ov, nv) -> restyle.run();

		checked.addListener(listener);
		inverted.addListener(listener);
	}

	/**
	 * Gets the BooleanProperty for the inverted property.
	 *
	 * @return The BooleanProperty for the inverted property.
	 */
	public BooleanProperty invertedProperty() {
		return inverted;
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
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
	 * Sets the checked status of the check mark.
	 *
	 * @param checked true for checked, false for unchecked.
	 */
	public void setChecked(boolean checked) {
		this.checked.set(checked);
	}
}
