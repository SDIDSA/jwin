package org.luke.gui.controls.input.styles;

import org.luke.gui.controls.input.Input;
import org.luke.gui.style.Style;

import javafx.scene.paint.Color;

public class ChannelInputStyle extends InputStyle {

	public ChannelInputStyle(Input input) {
		super(input);
	}

	@Override
	public void focus(boolean focus) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hover() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unhover() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focus() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unfocus() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBorder(Color border, Color hover, Color foc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void applyStyle(Style style) {
		applyBack(style.getChanneltextareaBackground());
	}

}
