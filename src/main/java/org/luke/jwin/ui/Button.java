package org.luke.jwin.ui;

import org.luke.gui.controls.Font;
import org.luke.gui.style.Style;
import org.luke.gui.window.Window;

import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;

public class Button extends org.luke.gui.controls.button.Button {

	public Button(Window window, String key, int width) {
		super(window, key, width);
		
		setFont(new Font(14, FontWeight.NORMAL));
		
		applyStyle(window.getStyl());
	}

	public Button(Window window, String text, int width, int height) {
		super(window, text, 5, width, height);
		
		setFont(new Font(14, FontWeight.NORMAL));
	}
	
	@Override
	public void applyStyle(Style style) {
		if(label == null) {
			return;
		}
		
		setTextFill(Color.WHITE);
		setFill(style.getAccent());
		
		super.applyStyle(style);
	}

}