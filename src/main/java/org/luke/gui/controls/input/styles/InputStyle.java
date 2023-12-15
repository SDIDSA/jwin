package org.luke.gui.controls.input.styles;

import org.luke.gui.controls.input.Input;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.factory.Borders;
import org.luke.gui.style.Style;

import javafx.scene.paint.Color;

public abstract class InputStyle {
	protected Input input;

	private double radius = 5;
	
	protected boolean ignoreHover = false;
	protected boolean ignoreFocus = false;
	
	protected InputStyle(Input input) {
		this.input = input;
	}
	
	public void setIgnoreFocus(boolean ignoreFocus) {
		this.ignoreFocus = ignoreFocus;
	}
	
	public void setIgnoreHover(boolean ignoreHover) {
		this.ignoreHover = ignoreHover;
	}

	protected void applyBack(Color fill) {
		input.setBackground(Backgrounds.make(fill, radius));
	}

	protected void applyBorder(Color border) {
		input.setBorder(Borders.make(border, radius));
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	public abstract void focus(boolean focus);

	public abstract void hover();

	public abstract void unhover();

	public abstract void focus();

	public abstract void unfocus();

	public abstract void setBorder(Color border, Color hover, Color foc);
	
	public abstract void applyStyle(Style style);
}
