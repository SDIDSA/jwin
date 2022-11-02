package org.luke.gui.controls.image;

import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;

public class Icon extends ImageView implements Styleable {
	private Window window;

	private String name;
	private double size;

	private ColorAdjust ca;

	public Icon(Window window, String name, double size) {
		setPreserveRatio(true);
		this.window = window;
		this.name = name;
		this.size = size;

		ca = new ColorAdjust();

		setEffect(ca);

		applyStyle(window.getStyl());
	}

	public void setIcon(String name) {
		this.name = name;
		applyStyle(window.getStyl());
	}

	public void setBrightness(double brightness) {
		ca.setBrightness(brightness);
	}

	public DoubleProperty brightnessProperty() {
		return ca.brightnessProperty();
	}

	@Override
	public void applyStyle(Style style) {
		StringBuilder path = new StringBuilder();
		path.append("/images/icons/").append(name).append('_').append(size).append(".png");
		ImageProxy.asyncLoad(name, size, this::setImage);
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

}
