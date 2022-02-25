package org.luke.jwin.ui;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class Button extends StackPane {
	private Rectangle darkBack;
	private Label label;
	private Label darkLabel;

	private ObjectProperty<EventHandler<ActionEvent>> onAction;

	private Timeline entered;
	private Timeline exited;

	public Button(String text) {
		super();
		onAction = new SimpleObjectProperty<>(null);

		darkBack = new Rectangle();
		darkBack.setFill(Color.gray(.3));
		darkBack.setMouseTransparent(true);

		text = Character.toUpperCase(text.charAt(0)) + text.substring(1);
		
		label = new Label(text);
		label.setPadding(new Insets(4, 8, 4, 8));

		darkLabel = new Label(text);
		darkLabel.setPadding(new Insets(4, 8, 4, 8));
		darkLabel.setTextFill(Color.WHITE);

		setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
		
		setCursor(Cursor.HAND);

		getChildren().addAll(darkBack, label, darkLabel);

		Style.styleRegion(this);

		Rectangle clip = new Rectangle();
		double radi = getBackground().getFills().get(0).getRadii().getTopLeftVerticalRadius() * 2;
		darkBack.setArcHeight(radi);
		darkBack.setArcWidth(radi);
		
		setClip(clip);

		heightProperty().addListener((obs, ov, nv) -> {
			if (isHover()) {
				darkBack.setTranslateY(0);
				label.setTranslateY(nv.doubleValue());
				darkLabel.setTranslateY(0);
			} else {
				darkBack.setTranslateY(nv.doubleValue());
				label.setTranslateY(0);
				darkLabel.setTranslateY(-nv.doubleValue());
			}

			darkBack.setHeight(nv.doubleValue() - 2);

			entered = new Timeline(new KeyFrame(Duration.seconds(Animate.Speed.FAST),
					new KeyValue(darkBack.translateYProperty(), 0, Interpolator.EASE_BOTH),
					new KeyValue(darkLabel.translateYProperty(), 0, Interpolator.EASE_BOTH),
					new KeyValue(label.translateYProperty(), nv.doubleValue(), Interpolator.EASE_BOTH)));
			exited = new Timeline(new KeyFrame(Duration.seconds(Animate.Speed.FAST),
					new KeyValue(darkBack.translateYProperty(), nv.doubleValue(), Interpolator.EASE_BOTH),
					new KeyValue(darkLabel.translateYProperty(), -nv.doubleValue(), Interpolator.EASE_BOTH),
					new KeyValue(label.translateYProperty(), 0, Interpolator.EASE_BOTH)));

			clip.setHeight(nv.doubleValue());
		});

		widthProperty().addListener((obs, ov, nv) -> {
			darkBack.setWidth(nv.doubleValue() - 2);

			clip.setWidth(nv.doubleValue());
		});

		addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
			exited.stop();
			entered.playFromStart();
		});

		addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
			entered.stop();
			exited.playFromStart();
		});
		
		addEventHandler(MouseEvent.MOUSE_CLICKED, e-> fire());
	}
	
	public void setFont(Font font) {
		label.setFont(font);
		darkLabel.setFont(font);
	}

	public void setOnAction(EventHandler<ActionEvent> onAction) {
		this.onAction.set(onAction);
	}

	public void fire() {
		if (onAction.get() != null) {
			onAction.get().handle(new ActionEvent());
		}
	}

	public void setTextAlignment(TextAlignment ta) {
		label.setTextAlignment(ta);
		darkLabel.setTextAlignment(ta);
	}

	public void setText(String string) {
		label.setText(string);
		darkLabel.setText(string);
	}
}