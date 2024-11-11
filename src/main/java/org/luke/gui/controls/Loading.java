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

/**
 * custom loading indicator with animated circles.
 * @author SDIDSA
 */
public class Loading extends Pane {
    private final Circle[] circles = new Circle[4];
    private final Runnable init;
    private final Timeline animation;
    private final ObjectProperty<Paint> fill = new SimpleObjectProperty<>();

    /**
     * Constructs a Loading indicator with the specified size.
     *
     * @param size The size of the loading indicator.
     */
    public Loading(double size) {
        double width = size * 5;
        setMinSize(width, size);
        setMaxSize(width, size);

        for (int i = 0; i < 4; i++) {
            circles[i] = new Circle(size / 2);
            circles[i].fillProperty().bind(fill);
            getChildren().add(circles[i]);
        }

        setTranslateY(size / 2);
        setTranslateX(size / 2);

        animation = new Timeline(new KeyFrame(Duration.seconds(.4),
                new KeyValue(circles[0].translateXProperty(), 0, Interpolator.EASE_BOTH),
                new KeyValue(circles[1].translateXProperty(), size * 2, Interpolator.EASE_BOTH),
                new KeyValue(circles[2].translateXProperty(), size * 4, Interpolator.EASE_BOTH),
                new KeyValue(circles[3].translateXProperty(), size * 6, Interpolator.EASE_BOTH),
                new KeyValue(circles[0].opacityProperty(), 1, Interpolator.EASE_BOTH),
                new KeyValue(circles[3].opacityProperty(), 0, Interpolator.EASE_BOTH),
                new KeyValue(circles[0].scaleXProperty(), 1, Interpolator.EASE_BOTH),
                new KeyValue(circles[0].scaleYProperty(), 1, Interpolator.EASE_BOTH),
                new KeyValue(circles[3].scaleXProperty(), .5, Interpolator.EASE_BOTH),
                new KeyValue(circles[3].scaleYProperty(), .5, Interpolator.EASE_BOTH)));

        init = () -> {
            circles[0].setOpacity(0);
            circles[0].setScaleX(.5);
            circles[0].setScaleY(.5);
            circles[0].setTranslateX(-2 * size);
            circles[1].setTranslateX(0);
            circles[2].setTranslateX(2 * size);
            circles[3].setTranslateX(4 * size);
            circles[3].setOpacity(1);
            circles[3].setScaleX(1);
            circles[3].setScaleY(1);
        };

        animation.setOnFinished(e -> {
            init.run();
            animation.playFromStart();
        });
    }

    /**
     * Starts the loading animation.
     */
    public void play() {
        init.run();
        animation.playFromStart();
    }

    /**
     * Stops the loading animation.
     */
    public void stop() {
        animation.stop();
    }

    /**
     * Sets the fill color of the loading circles.
     *
     * @param fill The fill color to set.
     */
    public void setFill(Paint fill) {
        this.fill.set(fill);
    }

    /**
     * Returns the fill property of the loading circles.
     *
     * @return The fill property.
     */
    public ObjectProperty<Paint> fillProperty() {
        return fill;
    }
}
