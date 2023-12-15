package org.luke.gui.controls.check;

import org.luke.gui.factory.Backgrounds;
import org.luke.gui.factory.Borders;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Cursor;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class Check extends StackPane implements Styleable {
	private BooleanProperty checked;
	private BooleanProperty inverted;
	
	private Pane tick;

	public Check(Window window, double size) {
		setMinSize(size, size);
		setMaxSize(size, size);

		inverted = new SimpleBooleanProperty(false);
		checked = new SimpleBooleanProperty(false);

		tick = new Pane();
		tick.setMinSize(size / 3.5, size / 1.6);
		tick.setMaxSize(size / 3.5, size / 1.6);
		tick.setTranslateY(-size / 15);
		tick.setTranslateX(-size / 40);
		tick.setRotate(45);
		tick.setMouseTransparent(true);

		getChildren().add(tick);

		tick.visibleProperty().bind(checked);

		setCursor(Cursor.HAND);
		setOnMouseClicked(e -> flip());

		applyStyle(window.getStyl());
	}
	
	public void flip() {
		checked.set(!checked.get());
	}

	private ChangeListener<Boolean> listener;
	@Override
	public void applyStyle(Style style) {
		Runnable restyle = () -> {
			boolean checkedVal = this.checked.get();
			boolean invertedVal = this.inverted.get();

			if (checkedVal) {
				if (invertedVal) {
					setBorder(Borders.make(Color.WHITE, 2.0, 2.0));
					setBackground(Backgrounds.make(Color.WHITE, 2.0));
					tick.setBorder(Borders.make(style.getAccent(), new BorderWidths(0, 2, 2, 0)));
				} else {
					setBorder(Borders.make(style.getAccent(), 2.0, 2.0));
					setBackground(Backgrounds.make(style.getAccent(), 2.0));
					tick.setBorder(Borders.make(Color.WHITE, new BorderWidths(0, 2, 2, 0)));
				}
			} else {
				setBackground(Background.EMPTY);
				if (invertedVal) {
					setBorder(Borders.make(Color.WHITE, 2.0, 2.0));
				} else {
					setBorder(Borders.make(style.getChannelsDefault(), 2.0, 2.0));
				}
			}
		};

		restyle.run();
		
		if(listener != null) {
			checked.removeListener(listener);
			inverted.removeListener(listener);
		}
		
		listener = (obs, ov, nv)-> restyle.run();
		
		checked.addListener(listener);
		inverted.addListener(listener);
	}
	
	public BooleanProperty invertedProperty() {
		return inverted;
	}
	
	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

	public BooleanProperty checkedProperty() {
		return checked;
	}
	
	public void setChecked(boolean checked) {
		this.checked.set(checked);
	}
}
