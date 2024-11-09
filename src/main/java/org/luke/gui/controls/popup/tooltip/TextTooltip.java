package org.luke.gui.controls.popup.tooltip;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.text.Text;

/**
 * A specialized tooltip implementation that includes a Text node for displaying
 * text content. This tooltip provides a convenient way to display textual
 * information in a small Popup.
 *
 * @author SDIDSA
 */
public class TextTooltip extends Tooltip {
	private Text text;

	/**
	 * Constructs a new TextTooltip with the specified parameters.
	 *
	 * @param window    The window associated with the tooltip.
	 * @param textValue The text content to be displayed in the tooltip.
	 * @param direction The direction in which the tooltip is oriented.
	 * @param offsetX   The horizontal offset from the target node.
	 * @param offsetY   The vertical offset from the target node.
	 */
	TextTooltip(Window window, String textValue, Direction direction, double offsetX, double offsetY) {
		super(window, direction, offsetX, offsetY);
		content.setPadding(new Insets(8, 12, 8, 12));

		text = new Text(textValue);
		text.setFont(new Font(Font.DEFAULT_FAMILY, 12).getFont());

		content.getChildren().add(text);

		applyStyle(window.getStyl().get());
	}

	/**
	 * Constructs a new TextTooltip with the specified parameters and default offset
	 * values.
	 *
	 * @param window    The window associated with the tooltip.
	 * @param textValue The text content to be displayed in the tooltip.
	 * @param direction The direction in which the tooltip is oriented.
	 */
	TextTooltip(Window window, String textValue, Direction direction) {
		this(window, textValue, direction, 0, 0);
	}

	/**
	 * Sets the text content to be displayed in the tooltip.
	 *
	 * @param txt The new text content.
	 */
	public void setText(String txt) {
		text.setText(txt);
	}

	/**
	 * Installs the TextTooltip on the specified node with the given parameters.
	 *
	 * @param node    The node to attach the tooltip to.
	 * @param dir     The direction in which the tooltip is oriented.
	 * @param value   The text content to be displayed in the tooltip.
	 * @param offsetX The horizontal offset from the target node.
	 * @param offsetY The vertical offset from the target node.
	 * @param key     Indicates whether the tooltip is a KeyedTooltip.
	 */
	public static void install(Node node, Direction dir, String value, double offsetX, double offsetY, boolean key) {
		if (node.getScene() == null) {
			node.sceneProperty().addListener(new ChangeListener<Scene>() {
				@Override
				public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
					if (newValue != null) {
						Window w = (Window) newValue.getWindow();
						TextTooltip tip = key ? new KeyedTooltip(w, value, dir, offsetX, offsetY)
								: new TextTooltip(w, value, dir, offsetX, offsetY);
						install(node, tip);
						node.sceneProperty().removeListener(this);
					}
				}
			});
		} else {
			Window w = (Window) node.getScene().getWindow();
			TextTooltip tip = key ? new KeyedTooltip(w, value, dir, offsetX, offsetY)
					: new TextTooltip(w, value, dir, offsetX, offsetY);
			install(node, tip);
		}
	}

	public static void install(Node node, Direction dir, String value, boolean key) {
		install(node, dir, value, 0, 0, key);
	}

	public static void install(Node node, Direction dir, String value, double offsetX, double offsetY) {
		install(node, dir, value, offsetX, offsetY, false);
	}

	public static void install(Node node, Direction dir, String value, double offset) {
		install(node, dir, value, offset, offset);
	}

	public static void install(Node node, Direction dir, String value) {
		install(node, dir, value, 0, 0, false);
	}

	@Override
	public void applyStyle(Style style) {
		if (text == null)
			return;

		text.setFill(style.getTextNormal());
		super.applyStyle(style);
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

}
