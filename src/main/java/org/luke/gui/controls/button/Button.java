package org.luke.gui.controls.button;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.window.Window;

import javafx.beans.binding.Bindings;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * customizable button with label and styling support. It provides features such
 * as configurable appearance, label text, font, and hover effects.
 *
 * @author SDIDSA
 */
public class Button extends AbstractButton {
	protected Label label;

	private boolean ulOnHover = false;

	public Button(Window window, String key, double radius, double width, double height) {
		this(window, key, new CornerRadii(radius), width, height);
	}

	/**
	 * Constructs a Button instance with the specified window, label key, radius,
	 * width, and height.
	 *
	 * @param window The associated Window for styling.
	 * @param key    The key for the label text.
	 * @param radius The corner radius of the button.
	 * @param width  The preferred width of the button.
	 * @param height The preferred height of the button.
	 */
	public Button(Window window, String key, CornerRadii radius, double width, double height) {
		super(window, radius, height);

		label = new Label(window, key);

		setFont(new Font(16, FontWeight.BOLD));

		if (width < 50) {
			prefWidthProperty()
					.bind(Bindings.createDoubleBinding(() -> label.getBoundsInLocal().getWidth() + (width * 2),
							label.textProperty(), label.fontProperty()));
		} else {
			setPrefWidth(width);
		}

		label.opacityProperty().bind(back.opacityProperty());

		StackPane preLabel = new StackPane(label);

		HBox.setHgrow(preLabel, Priority.ALWAYS);

		add(preLabel);

		applyStyle(window.getStyl());
	}

	public Button(Window window, String key) {
		this(window, key, DEFAULT_RADIUS, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	public Button(Window window, String key, double width) {
		this(window, key, DEFAULT_RADIUS, width, DEFAULT_HEIGHT);
	}

	public Button(Window window, String string, double radius, double width) {
		this(window, string, radius, width, DEFAULT_HEIGHT);
	}

	@Override
	protected void onEnter(MouseEvent event) {
		if (ulOnHover) {
			label.setUnderline(true);
		}
		super.onEnter(event);
	}

	@Override
	protected void onExit(MouseEvent event) {
		if (ulOnHover) {
			label.setUnderline(false);
		}
		super.onExit(event);
	}

	/**
	 * Sets whether to underline the label on hover.
	 *
	 * @param ulOnHover true to underline label on hover, false otherwise.
	 */
	public void setUlOnHover(boolean ulOnHover) {
		this.ulOnHover = ulOnHover;
	}

	/**
	 * Sets the font for the label.
	 *
	 * @param font The font for the label.
	 */
	public void setFont(Font font) {
		label.setFont(font);
	}

	/**
	 * Sets the key for the label text.
	 *
	 * @param key The key for the label text.
	 */
	public void setKey(String key) {
		label.setKey(key);
	}

	/**
	 * Sets the text alignment for the label.
	 *
	 * @param pos The text alignment for the label.
	 */
	public void setTextAlignment(TextAlignment pos) {
		label.setTextAlignment(pos);
	}

	@Override
	public void setTextFill(Paint fill) {
		label.setFill(fill);
		super.setTextFill(fill);
	}
}
