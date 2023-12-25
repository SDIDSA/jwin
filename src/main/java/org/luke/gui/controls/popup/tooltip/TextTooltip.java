package org.luke.gui.controls.popup.tooltip;

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

public class TextTooltip extends Tooltip {
	private Text text;

	TextTooltip(Window window, String val, Direction direction, double offsetX, double offsetY) {
		super(window, direction, offsetX, offsetY);
		content.setPadding(new Insets(8, 12, 8, 12));
		
		text = new Text(val);
		
		content.getChildren().add(text);

		applyStyle(window.getStyl().get());
	}

	TextTooltip(Window window, String val, Direction direction) {
		this(window, val, direction, 0, 0);
	}

	public void setText(String txt) {
		text.setText(txt);
	}

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
		if(text == null) return;
		
		text.setFill(style.getTextNormal());
		super.applyStyle(style);
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

}
