package org.luke.jwin.ui;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.button.AbstractButton;
import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;

public class MultiButton extends HBox implements Styleable {

	private org.luke.gui.controls.button.Button main;
	private AbstractButton preIc;
	
	private ContextMenu options;
	
	public MultiButton(Window window, String key) {
		super(4);
		
		main = new org.luke.gui.controls.button.Button(window, key, new CornerRadii(5,2,2,5, false), 100, 40);
		main.setFill(Color.TRANSPARENT);
		main.setTextFill(Color.WHITE);
		main.setFont(new Font(14, FontWeight.NORMAL));
		
		setAlignment(Pos.CENTER);
		setMaxHeight(40);
		
		preIc = new AbstractButton(window, new CornerRadii(2,5,5,2, false), 40, 24);
		ColorIcon ic = new ColorIcon("menu-right", 12, 10);
		ic.setFill(Color.WHITE);
		ic.setRotate(90);
		preIc.add(ic);
		
		preIc.setAction(() -> {
			options.showPop(preIc, Direction.DOWN_LEFT, 0, 10);
		});
		
		options = new ContextMenu(window);
		
		getChildren().addAll(main, preIc);
		
		applyStyle(window.getStyl());
	}
	
	public void setAction(Runnable r) {
		main.setAction(r);
	}
	
	public void addAction(String key, Runnable action) {
		options.addMenuItem(key, () -> {
			options.hide();
			action.run();
		});
	}
	
	@Override
	public void applyStyle(Style style) {
		main.setFill(style.getAccent());
		preIc.setFill(style.getAccent());
	}
	
	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

}
