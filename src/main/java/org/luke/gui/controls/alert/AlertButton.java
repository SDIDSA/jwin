package org.luke.gui.controls.alert;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.button.Button;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;

import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;

public class AlertButton extends Button implements Styleable {
	private ButtonType type;
	
	public AlertButton(Alert alert, ButtonType type) {
		super(alert.getWindow(), type.getKey(), 5.0, 16, 38);
		this.type = type;
		
		setFont(type.isFilled() ? new Font(14, FontWeight.BOLD) : new Font(Font.DEFAULT_FAMILY_MEDIUM, 14));
		
		if(!type.isFilled()) {
			setFill(Color.TRANSPARENT);
			setUlOnHover(true);
		}
		
		applyStyle(alert.getWindow().getStyl());
	}

	@Override
	public void applyStyle(Style style) {
		super.applyStyle(style);
		if(type.isFilled()) {
			setTextFill(Color.WHITE);
			if(type.getFill() == null) {
				setFill(style.getAccent());
			}else {
				setFill(type.getFill());
			}
		}else {
			setTextFill(style.getLinkButtonText());
		}
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		if(type == null) {
			return;
		}
		Styleable.bindStyle(this, style);
	}

}
