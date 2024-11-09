package org.luke.jwin.ui;

import org.luke.gui.controls.Font;
import org.luke.gui.style.Style;
import org.luke.gui.window.Window;

import javafx.scene.text.FontWeight;

public class Button extends org.luke.gui.controls.button.Button {

	public Button(Window window, String key, double width) {
		super(window, key, width);
		
		setFont(new Font(14, FontWeight.NORMAL));
		
		applyStyle(window.getStyl());
	}

	public Button(Window window, String text, double width, double height) {
		super(window, text, 5, width, height);
		
		setFont(new Font(14, FontWeight.NORMAL));
	}
	
	public Button(Window window, String key) {
		this(window, key, DEFAULT_WIDTH);
	}

	@Override
	public void applyStyle(Style style) {
		if(label == null) {
			return;
		}
		
		setTextFill(style.getTextOnAccent());
		setFill(style.getAccent());
		
		super.applyStyle(style);
	}

}