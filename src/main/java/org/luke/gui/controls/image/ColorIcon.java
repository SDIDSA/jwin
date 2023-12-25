package org.luke.gui.controls.image;

import org.luke.gui.NodeUtils;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;

import javafx.beans.property.ObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class ColorIcon extends StackPane implements Styleable {
	private ImageView view;
	private Rectangle overlay;

	private String name;
	private double loadSize;
	private double displaySize;

	private Runnable action;

	public ColorIcon(String name, double loadSize, double displaySize, boolean focusable) {
		view = new ImageView();
		overlay = new Rectangle();
		overlay.setClip(view);

		if(name != null)
			setImage(name, loadSize, displaySize);

		if (focusable) {
			setFocusTraversable(true);
		}

		setOnMouseClicked(this::fire);
		setOnKeyPressed(this::fire);

		getChildren().addAll(overlay);
	}

	public ColorIcon(String name, double loadSize, double displaySize) {
		this(name, loadSize, displaySize, false);
	}

	public ColorIcon(String name, double size, boolean focusable) {
		this(name, size, size, focusable);
	}

	private void fire(MouseEvent dismiss) {
		fire();
	}

	private void fire(KeyEvent e) {
		if (e.getCode().equals(KeyCode.SPACE)) {
			fire();
			e.consume();
		}
	}

	public void fire() {
		if (action != null) {
			action.run();
		}
	}

	public void setAction(Runnable action) {
		this.action = action;
	}

	public ColorIcon(String name, double size) {
		this(name, size, size, false);
	}

	public void setName(String name) {
		setImage(name, loadSize, displaySize);
	}

	public void setSize(int size) {
		setImage(name, size);
	}

	public void setSize(int loadSize, int displaySize) {
		setImage(name, loadSize, displaySize);
	}

	public void setImage(String name, double size) {
		setImage(name, size, size);
	}
	
	public void setImage(String name) {
		view.setImage(ImageProxy.loadResize(name, loadSize, displaySize));
	}

	public void setImage(String name, double loadSize, double displaySize) {
		this.name = name;
		this.loadSize = loadSize;
		this.displaySize = displaySize;
		Image img = loadSize == displaySize ? ImageProxy.load(name, loadSize)
				: ImageProxy.loadResize(name, loadSize, displaySize);
		double w = img.getWidth();
		double h = img.getHeight();

		view.setImage(img);

		setMinSize(w, h);
		setMaxSize(w, h);

		overlay.setWidth(w);
		overlay.setHeight(h);
	}

	public void setPadding(double val) {
		setMinSize(displaySize + val * 2, displaySize + val * 2);
		setMaxSize(displaySize + val * 2, displaySize + val * 2);
	}

	public ObjectProperty<Paint> fillProperty() {
		return overlay.fillProperty();
	}

	public void setFill(Paint fill) {
		overlay.setFill(fill);
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

	@Override
	public void applyStyle(Style style) {
		NodeUtils.focusBorder(this, style.getTextLink());
	}
}
