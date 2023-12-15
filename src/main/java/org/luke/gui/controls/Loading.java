package org.luke.gui.controls;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import javafx.scene.shape.Circle;

public class Loading extends Pane {
	private Circle c0;
	private Circle c1;
	private Circle c2;
	private Circle c3;
	private Runnable init;
	private Timeline animation;
	
	private ObjectProperty<Paint> fill;
	
	public Loading(double size) {
		double width = size * 5;
		setMinSize(width, size);
		setMaxSize(width, size);
		
		fill = new SimpleObjectProperty<>();

		c0 = new Circle(size / 2);
		c1 = new Circle(size / 2);
		c2 = new Circle(size / 2);
		c3 = new Circle(size / 2);
		
		c0.fillProperty().bind(fill);
		c1.fillProperty().bind(fill);
		c2.fillProperty().bind(fill);
		c3.fillProperty().bind(fill);

		setTranslateY(size / 2);
		setTranslateX(size / 2);
		
		animation = new Timeline(new KeyFrame(Duration.seconds(.4),
				new KeyValue(c0.translateXProperty(), 0, Interpolator.EASE_BOTH),
				new KeyValue(c1.translateXProperty(), size * 2, Interpolator.EASE_BOTH),
				new KeyValue(c2.translateXProperty(), size * 4, Interpolator.EASE_BOTH),
				new KeyValue(c3.translateXProperty(), size * 6, Interpolator.EASE_BOTH),
				
				new KeyValue(c0.opacityProperty(), 1, Interpolator.EASE_BOTH),
				new KeyValue(c3.opacityProperty(), 0, Interpolator.EASE_BOTH),
				
				new KeyValue(c0.scaleXProperty(), 1, Interpolator.EASE_BOTH),
				new KeyValue(c0.scaleYProperty(), 1, Interpolator.EASE_BOTH),
				
				new KeyValue(c3.scaleXProperty(), .5, Interpolator.EASE_BOTH),
				new KeyValue(c3.scaleYProperty(), .5, Interpolator.EASE_BOTH)));
		
		init = () -> {
			c0.setOpacity(0);
			c0.setScaleX(.5);
			c0.setScaleY(.5);

			c0.setTranslateX(-2 * size);
			c1.setTranslateX(0);
			c2.setTranslateX(2 * size);
			c3.setTranslateX(4 * size);
			
			c3.setOpacity(1);
			c3.setScaleX(1);
			c3.setScaleY(1);
		};
		
		animation.setOnFinished(e-> {
			init.run();
			animation.playFromStart();
		});
		
		getChildren().addAll(c0, c1, c2, c3);
	}
	
	public void play() {
		init.run();
		animation.playFromStart();
	}
	
	
	public void stop() {
		animation.stop();
	}

	public void setFill(Paint fill) {
		this.fill.set(fill);
	}
	
	public ObjectProperty<Paint> fillProperty() {
		return fill;
	}
}
