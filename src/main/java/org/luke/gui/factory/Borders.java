package org.luke.gui.factory;

import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;

/**
 * Utility class for creating JavaFX Border objects with various configurations.
 * This class allows the creation of Border objects with different stroke
 * styles, corner radii, widths, and colors.
 */
public class Borders {

	private Borders() {
		// Private constructor to prevent instantiation; this class is intended for
		// static use only.
	}

	private static final BorderStrokeStyle SOLID = BorderStrokeStyle.SOLID;

	/**
	 * Represents a solid BorderStrokeStyle with round line joins and caps, suitable
	 * for outside stroke.
	 */
	public static final BorderStrokeStyle OUTSIDE = new BorderStrokeStyle(StrokeType.OUTSIDE, StrokeLineJoin.ROUND,
			StrokeLineCap.ROUND, 0, 0, null);

	/**
	 * Creates a Border with the specified fill color, stroke style, corner radii,
	 * and border widths.
	 * 
	 * @param fill   The fill color of the border.
	 * @param style  The stroke style of the border.
	 * @param radius The corner radii of the border.
	 * @param width  The widths of the border.
	 * @return The created Border object.
	 */
	public static Border make(Paint fill, BorderStrokeStyle style, CornerRadii radius, BorderWidths width) {
		return new Border(new BorderStroke(fill, style, radius, width));
	}

	/**
	 * Creates a Border with the specified fill color, stroke style, and corner
	 * radii.
	 * 
	 * @param fill   The fill color of the border.
	 * @param style  The stroke style of the border.
	 * @param radius The corner radii of the border.
	 * @return The created Border object.
	 */
	public static Border make(Paint fill, BorderStrokeStyle style, CornerRadii radius) {
		return make(fill, style, radius, null);
	}

	/**
	 * Creates a Border with the specified fill color, stroke style, and border
	 * widths.
	 * 
	 * @param fill  The fill color of the border.
	 * @param style The stroke style of the border.
	 * @param width The widths of the border.
	 * @return The created Border object.
	 */
	public static Border make(Paint fill, BorderStrokeStyle style, BorderWidths width) {
		return make(fill, style, null, width);
	}

	/**
	 * Creates a Border with the specified fill color, corner radii, and border
	 * widths.
	 * 
	 * @param fill   The fill color of the border.
	 * @param radius The corner radii of the border.
	 * @param width  The widths of the border.
	 * @return The created Border object.
	 */
	public static Border make(Paint fill, CornerRadii radius, BorderWidths width) {
		return make(fill, SOLID, radius, width);
	}

	/**
	 * Creates a Border with the specified fill color and corner radii.
	 * 
	 * @param fill   The fill color of the border.
	 * @param radius The corner radii of the border.
	 * @return The created Border object.
	 */
	public static Border make(Paint fill, CornerRadii radius) {
		return make(fill, SOLID, radius, null);
	}

	/**
	 * Creates a Border with the specified fill color and corner radius.
	 * 
	 * @param fill   The fill color of the border.
	 * @param radius The corner radius of the border.
	 * @return The created Border object.
	 */
	public static Border make(Paint fill, double radius) {
		return make(fill, SOLID, new CornerRadii(radius), null);
	}

	/**
	 * Creates a Border with the specified fill color, stroke style, and border
	 * widths.
	 * 
	 * @param fill  The fill color of the border.
	 * @param style The stroke style of the border.
	 * @return The created Border object.
	 */
	public static Border make(Paint fill, BorderStrokeStyle style) {
		return make(fill, style, null, null);
	}

	/**
	 * Creates a Border with the specified fill color, stroke style, and corner
	 * radius.
	 * 
	 * @param fill   The fill color of the border.
	 * @param style  The stroke style of the border.
	 * @param radius The corner radius of the border.
	 * @return The created Border object.
	 */
	public static Border make(Color fill, BorderStrokeStyle style, double radius) {
		return make(fill, style, new CornerRadii(radius), null);
	}

	/**
	 * Creates a Border with the specified fill color and border widths.
	 * 
	 * @param fill  The fill color of the border.
	 * @param width The widths of the border.
	 * @return The created Border object.
	 */
	public static Border make(Paint fill, BorderWidths width) {
		return make(fill, SOLID, null, width);
	}

	/**
	 * Creates a Border with the specified fill color, corner radius, and border
	 * width.
	 * 
	 * @param fill   The fill color of the border.
	 * @param radius The corner radius of the border.
	 * @param width  The width of the border.
	 * @return The created Border object.
	 */
	public static Border make(Paint fill, double radius, double width) {
		return make(fill, SOLID, new CornerRadii(radius), new BorderWidths(width));
	}

	/**
	 * Creates a Border with the specified fill color.
	 * 
	 * @param fill The fill color of the border.
	 * @return The created Border object.
	 */
	public static Border make(Paint fill) {
		return make(fill, SOLID, null, null);
	}
}
