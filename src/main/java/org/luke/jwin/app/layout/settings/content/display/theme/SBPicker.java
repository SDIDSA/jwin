package org.luke.jwin.app.layout.settings.content.display.theme;

import org.luke.gui.factory.Borders;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class SBPicker extends StackPane implements Styleable {
	private Canvas sbCan;

	private DoubleProperty saturation;
	private DoubleProperty brightness;

	private Circle selector;

	public SBPicker(Window window, int size) {
		setAlignment(Pos.TOP_LEFT);

		saturation = new SimpleDoubleProperty(1);
		brightness = new SimpleDoubleProperty(0.5);
		
		setMaxSize(size + 2, size + 2);
		setMinSize(size + 2, size + 2);
		
		sbCan = new Canvas();
		sbCan.setWidth(size);
		sbCan.setHeight(size);

		selector = new Circle(4);
		selector.setFill(Color.TRANSPARENT);
		selector.setStroke(Color.WHITE);
		selector.setStrokeWidth(2);
		selector.setEffect(new DropShadow(4, Color.gray(0, 1)));

		Rectangle clip = new Rectangle();
		clip.widthProperty().bind(widthProperty());
		clip.heightProperty().bind(heightProperty());
		setClip(clip);

		Runnable updateSelectorPos = () -> {
			selector.setTranslateX(getWidth() * saturation.get() - 6);
			selector.setTranslateY(getHeight() - (getHeight() * brightness.get()) - 6);
		};
		
		saturation.addListener((obs, ov, nv) -> {
			updateSelectorPos.run();
		});

		brightness.addListener((obs, ov, nv) -> {
			updateSelectorPos.run();
		});

		EventHandler<MouseEvent> mouseHandler = (ev) -> {
			double perX = ev.getX() / getWidth();
			double perY = ev.getY() / getHeight();

			double sat = perX;
			double br = 1 - perY;

			sat = Math.max(sat, 0);
			sat = Math.min(sat, 1);

			br = Math.max(br, 0);
			br = Math.min(br, 1);

			saturation.set(sat);
			brightness.set(br);
		};

		setOnMouseDragged(mouseHandler);
		setOnMousePressed(mouseHandler);

		saturation.set(1);
		brightness.set(1);
		
		widthProperty().addListener((obs, ov, nv) -> {
			update(hue);
			updateSelectorPos.run();
		});
		
		heightProperty().addListener((obs, ov, nv) -> {
			update(hue);
			updateSelectorPos.run();
		});

		getChildren().addAll(sbCan, selector);

		applyStyle(window.getStyl());
	}
	
	public double getSaturation() {
		return saturation.get();
	}
	
	public double getBrightness() {
		return brightness.get();
	}
	
	public DoubleProperty saturationProperty() {
		return saturation;
	}
	
	public DoubleProperty brightnessProperty() {
		return brightness;
	}
	
	public void onChange(Runnable exec) {
		saturation.addListener((obs, ov, nv) -> exec.run());
		brightness.addListener((obs, ov, nv) -> exec.run());
	}

	private double hue;
	public void update(double hue) {
		this.hue = hue;
		GraphicsContext gc = sbCan.getGraphicsContext2D();
		gc.clearRect(0, 0, sbCan.getWidth(), sbCan.getHeight());
		for (int i = 0; i < sbCan.getWidth(); i++) {
			double saturation = i / sbCan.getWidth();
			for (int j = 0; j < sbCan.getHeight(); j++) {
				double brightness = 1 - (j / sbCan.getHeight());
				Color color = Color.hsb(hue, saturation, brightness);
				gc.setFill(color);
				gc.fillRect(i, j, 1, 1);
			}
		}
	}

	@Override
	public void applyStyle(Style style) {
		setBorder(Borders.make(style.getBackgroundModifierActive()));
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
