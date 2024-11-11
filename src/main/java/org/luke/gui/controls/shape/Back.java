package org.luke.gui.controls.shape;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;

/**
 * The {@code Back} class represents a customizable background pane with
 * optional border. It provides properties for setting the width, height, fill
 * color, stroke color, corner radius, stroke width, and stroke type.
 * <p>
 * Author: SDIDSA
 */
public class Back extends Pane {

	private final DoubleProperty width, height;
	private final ObjectProperty<Paint> fill;
	private final ObjectProperty<Paint> stroke;
	private final ObjectProperty<CornerRadii> radius;
	private final ObjectProperty<BorderWidths> strokeWidth;
	private final ObjectProperty<StrokeType> strokeType;

	/**
	 * Constructs a new {@code Back} with default settings.
	 */
	public Back() {
		width = new SimpleDoubleProperty(0, "width");
		height = new SimpleDoubleProperty(0, "height");
		fill = new SimpleObjectProperty<Paint>(Color.TRANSPARENT, "fill");
		fill.set(Color.TRANSPARENT);
		stroke = new SimpleObjectProperty<Paint>(Color.TRANSPARENT, "stroke");
		stroke.set(Color.TRANSPARENT);
		radius = new SimpleObjectProperty<>(new CornerRadii(0), "corner radius");
		radius.set(new CornerRadii(0));
		strokeWidth = new SimpleObjectProperty<>(new BorderWidths(0), "stroke width");
		strokeWidth.set(new BorderWidths(0));
		strokeType = new SimpleObjectProperty<>(StrokeType.CENTERED, "stroke type");
		strokeType.set(StrokeType.CENTERED);

		setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
		setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
		prefWidthProperty().bind(width);
		prefHeightProperty().bind(height);

		backgroundProperty().bind(Bindings.createObjectBinding(() ->
				new Background(new BackgroundFill(fill.get(), radius.get(), null)), fill, radius));

		borderProperty().bind(Bindings.createObjectBinding(() ->
				new Border(new BorderStroke(stroke.get(),
                	new BorderStrokeStyle(strokeType.get(), StrokeLineJoin.ROUND,
							StrokeLineCap.ROUND, 0, 0, null),
               			radius.get(), strokeWidth.get())), stroke, radius, strokeWidth, strokeType));
	}

	public DoubleProperty wProp() {
		return width;
	}

	public DoubleProperty hProp() {
		return height;
	}

	public ObjectProperty<CornerRadii> radiusProperty() {
		return radius;
	}

	public ObjectProperty<Paint> fillProperty() {
		return fill;
	}

	public double width() {
		return width.get();
	}

	public double height() {
		return height.get();
	}

	public Paint getFill() {
		return fill.get();
	}

	public Paint getStroke() {
		return stroke.get();
	}

	public CornerRadii getRadius() {
		return radius.get();
	}

	public BorderWidths getStrokeWidth() {
		return strokeWidth.get();
	}

	public void setFill(Paint fill) {
		this.fill.set(fill);
	}

	public void setStroke(Paint stroke) {
		this.stroke.set(stroke);
	}

	public void setStrokeType(StrokeType type) {
		strokeType.set(type);
	}

}
