package org.luke.gui.controls.input.styles;

import org.luke.gui.controls.input.Input;
import org.luke.gui.style.Style;

import javafx.scene.paint.Color;

public class ModernInputStyle extends InputStyle {

	public ModernInputStyle(Input input) {
		super(input);
	}

	@Override
	public void focus(boolean focus) {
		//DO NOTHING
	}

	@Override
	public void hover() {
		//DO NOTHING
	}

	@Override
	public void unhover() {
		//DO NOTHING		
	}

	@Override
	public void focus() {
		//DO NOTHING
	}

	@Override
	public void unfocus() {
		//DO NOTHING
	}

	@Override
	public void setBorder(Color border, Color hover, Color foc) {
		//DO NOTHING
	}

	@Override
	public void applyStyle(Style style) {
		applyBack(style.getBackgroundTertiary());
	}

}
