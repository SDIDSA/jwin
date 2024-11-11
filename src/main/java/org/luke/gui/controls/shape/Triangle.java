package org.luke.gui.controls.shape;

import org.luke.gui.style.ColorItem;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;

/**
 * The {@code Triangle} class represents a triangle shape with customizable size
 * and fill color. It implements the {@code ColorItem} interface for setting the
 * fill color.
 * <p>
 * Author: SDIDSA
 */
public class Triangle extends StackPane implements ColorItem {

	private final Polygon poly;

	public Triangle(double size) {
		poly = new Polygon();

		setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
		setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);

		getChildren().add(poly);
		setSize(size);
	}

	public void setSize(double size) {
		double offset = (size * 0.4);
		poly.getPoints().setAll(0.0, size / 2, size, -offset, size, size + offset);

		setPrefSize(size, size);
	}

	@Override
	public void setFill(Paint fill) {
		poly.setFill(fill);
	}

	@Override
	public Node getNode() {
		return this;
	}

}
