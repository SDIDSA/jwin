package org.luke.gui.controls.input.styles;

import org.luke.gui.controls.input.Input;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.factory.Borders;
import org.luke.gui.style.Style;

import javafx.scene.paint.Color;

/**
 * The abstract base class for defining the visual style of an input control.
 * This class is meant to be extended by specific input style implementations.
 *
 * @author SDIDSA
 */
public abstract class InputStyle {
	protected Input input;

	private double radius = 5;

	protected boolean ignoreHover = false;
	protected boolean ignoreFocus = false;

	/**
	 * Constructs an input style associated with the specified input control.
	 *
	 * @param input The input control to which this style is applied.
	 */
	protected InputStyle(Input input) {
		this.input = input;
	}

	/**
	 * Sets whether to ignore focus events for the associated input control.
	 *
	 * @param ignoreFocus If true, focus events are ignored; otherwise, they are not
	 *                    ignored.
	 */
	public void setIgnoreFocus(boolean ignoreFocus) {
		this.ignoreFocus = ignoreFocus;
	}

	/**
	 * Sets whether to ignore hover events for the associated input control.
	 *
	 * @param ignoreHover If true, hover events are ignored; otherwise, they are not
	 *                    ignored.
	 */
	public void setIgnoreHover(boolean ignoreHover) {
		this.ignoreHover = ignoreHover;
	}

	/**
	 * Applies the background with the specified fill color to the associated input
	 * control.
	 *
	 * @param fill The fill color for the background.
	 */
	protected void applyBack(Color fill) {
		input.setBackground(Backgrounds.make(fill, radius));
	}

	/**
	 * Applies the border with the specified color to the associated input control.
	 *
	 * @param border The color for the border.
	 */
	protected void applyBorder(Color border) {
		input.setBorder(Borders.make(border, radius));
	}

	/**
	 * Sets the radius for rounded corners of the associated input control.
	 *
	 * @param radius The radius for rounded corners.
	 */
	public void setRadius(double radius) {
		this.radius = radius;
	}

	/**
	 * Applies the style for focus to the associated input control.
	 *
	 * @param focus If true, applies the focus style; otherwise, removes the focus
	 *              style.
	 */
	public abstract void focus(boolean focus);

	/**
	 * Applies the style for hover to the associated input control.
	 */
	public abstract void hover();

	/**
	 * Removes the style for hover from the associated input control.
	 */
	public abstract void unhover();

	/**
	 * Applies the style for focus to the associated input control.
	 */
	public abstract void focus();

	/**
	 * Removes the style for focus from the associated input control.
	 */
	public abstract void unfocus();

	/**
	 * Sets the border colors for the associated input control based on the
	 * specified colors.
	 *
	 * @param border The color for the default border.
	 * @param hover  The color for the hover border.
	 * @param focus  The color for the focus border.
	 */
	public abstract void setBorder(Color border, Color hover, Color focus);

	/**
	 * Applies the overall visual style based on the provided style information.
	 *
	 * @param style The style information to be applied.
	 */
	public abstract void applyStyle(Style style);
}
