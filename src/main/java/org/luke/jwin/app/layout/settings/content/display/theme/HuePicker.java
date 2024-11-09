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
import javafx.scene.shape.Rectangle;

public class HuePicker extends StackPane implements Styleable {
	private Canvas hueCan;

	private DoubleProperty hue;

	private Rectangle selector;

	public HuePicker(Window window, int size) {
		setAlignment(Pos.TOP_LEFT);

		hue = new SimpleDoubleProperty(100);

		double width = size / 6d;
		
		hueCan = new Canvas();
		hueCan.setWidth(width);
		hueCan.setHeight(size);
		
		setMaxSize(width + 2, size + 2);
		setMinSize(width + 2, size + 2);

		selector = new Rectangle(width + 6, size / 15d);
		selector.setFill(Color.TRANSPARENT);
		selector.setStroke(Color.WHITE);
		selector.setStrokeWidth(2);
		selector.setEffect(new DropShadow(4, Color.gray(0, 1)));
		selector.setTranslateY(-5);
		selector.setTranslateX(-4);

		Runnable updateSelectorPos = () -> {
			selector.setTranslateY(getHeight() * hue.get() / 360 - selector.getHeight() / 2 - 2);
		};

		hue.addListener((obs, ov, nv) -> {
			updateSelectorPos.run();
		});

		EventHandler<MouseEvent> mouseHandler = (ev) -> {
			double perY = ev.getY() / getHeight();

			double h = perY * 360;

			h = Math.max(h, 0);
			h = Math.min(h, 360);

			hue.set(h);
		};

		setOnMouseDragged(mouseHandler);
		setOnMousePressed(mouseHandler);

		hue.set(180);

		widthProperty().addListener((obs, ov, nv) -> {
			update();
			updateSelectorPos.run();
		});

		heightProperty().addListener((obs, ov, nv) -> {
			update();
			updateSelectorPos.run();
		});

		getChildren().addAll(hueCan, selector);

		applyStyle(window.getStyl());
	}
	
	public DoubleProperty hueProperty() {
		return hue;
	}
	
	public double getHue() {
		return hue.get();
	}
	
	public void onChange(Runnable exec) {
		hue.addListener((obs, ov, nv) -> exec.run());
	}

	public void update() {
		GraphicsContext gc = hueCan.getGraphicsContext2D();
        gc.clearRect(0, 0, hueCan.getWidth(), hueCan.getHeight());

        for (int i = 0; i < hueCan.getHeight(); i++) {
            double hue = (i / hueCan.getHeight()) * 360;
            Color color = Color.hsb(hue, 1, 1);
            gc.setFill(color);
            gc.fillRect(0, i, hueCan.getWidth(), 1);
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
