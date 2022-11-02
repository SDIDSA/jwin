package org.luke.gui.controls.button;

import org.luke.gui.NodeUtils;
import org.luke.gui.controls.Loading;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.util.Duration;

public class AbstractButton extends StackPane implements Styleable {
	protected static final double DEFAULT_HEIGHT = 32;
	protected static final double DEFAULT_RADIUS = 5;
	
	private double radius;
	private DoubleProperty radiusProperty;
	private Timeline enter;
	private Timeline exit;
	
	private Runnable mouseAction;
	private Runnable keyAction;

	private Loading load;

	private HBox content;

	protected Rectangle back;

	private BooleanProperty loading;

	public AbstractButton(Window window, double radius, double height) {
		this.radius = radius;
		getStyleClass().addAll("butt");

		getStylesheets().clear();

		loading = new SimpleBooleanProperty(false);

		setFocusTraversable(true);
		setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
		setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
		setPrefHeight(height);

		radiusProperty = new SimpleDoubleProperty(radius);

		back = new Rectangle();
		back.setFill(Color.TRANSPARENT);
		back.setStrokeType(StrokeType.INSIDE);

		back.arcWidthProperty().bind(radiusProperty.multiply(2));
		back.arcHeightProperty().bind(radiusProperty.multiply(2));

		back.widthProperty().bind(widthProperty().subtract(Bindings.when(focusedProperty()).then(6).otherwise(0)));
		back.heightProperty().bind(heightProperty().subtract(Bindings.when(focusedProperty()).then(6).otherwise(0)));

		setCursor(Cursor.HAND);

		load = new Loading(height / 5);
		load.setOpacity(.7);

		setOnMouseEntered(this::onEnter);

		setOnMouseExited(this::onExit);

		setOnMouseClicked(this::fire);
		setOnKeyReleased(this::fire);

		ColorAdjust bw = new ColorAdjust();
		bw.setSaturation(-.5);
		ColorAdjust col = new ColorAdjust();

		effectProperty().bind(Bindings.when(disabledProperty()).then(bw).otherwise(col));
		back.opacityProperty().bind(Bindings.when(disabledProperty()).then(.7).otherwise(1));

		content = new HBox();
		content.setAlignment(Pos.CENTER);

		getChildren().addAll(back, content);
		
		applyStyle(window.getStyl());
	}

	public void setContentPadding(Insets insets) {
		content.setPadding(insets);
	}

	public void add(Node... nodes) {
		content.getChildren().addAll(nodes);
	}

	protected void onEnter(MouseEvent event) {
		exit.stop();
		enter.playFromStart();
	}

	protected void onExit(MouseEvent event) {
		enter.stop();
		exit.playFromStart();
	}

	public void startLoading() {
		loading.set(true);
		getParent().requestFocus();
		setMouseTransparent(true);
		setFocusTraversable(false);
		getChildren().setAll(back, load);
		load.play();
	}

	public void stopLoading() {
		setMouseTransparent(false);
		setFocusTraversable(true);
		getChildren().setAll(back, content);
		load.stop();
		loading.set(false);
	}

	public BooleanProperty loadingProperty() {
		return loading;
	}

	public AbstractButton(Window window) {
		this(window, DEFAULT_RADIUS, DEFAULT_HEIGHT);
	}

	public AbstractButton(Window window, double radius) {
		this(window, radius, DEFAULT_HEIGHT);
	}

	private void fire(MouseEvent dismiss) {
		fire(mouseAction);
	}

	private void fire(KeyEvent e) {
		if (e.getCode().equals(KeyCode.SPACE)) {
			fire(keyAction);
		}
	}

	private void fire(Runnable action) {
		if (isDisabled() || loading.get()) {
			return;
		}
		if (action != null) {
			action.run();
		}
	}
	
	public void fire() {
		fire(keyAction);
	}

	public void setAction(Runnable action) {
		this.mouseAction = action;
		this.keyAction = action;
	}
	
	public void setMouseAction(Runnable mouseAction) {
		this.mouseAction = mouseAction;
	}
	
	public void setKeyAction(Runnable keyAction) {
		this.keyAction = keyAction;
	}

	public void setTextFill(Paint fill) {
		load.setFill(fill);
	}

	public double getRadius() {
		return radius;
	}

	public void setFill(Color fill) {
		back.setFill(fill);
		enter = new Timeline(new KeyFrame(Duration.seconds(.15),
				new KeyValue(back.fillProperty(), fill.darker(), Interpolator.EASE_BOTH)));

		exit = new Timeline(
				new KeyFrame(Duration.seconds(.15), new KeyValue(back.fillProperty(), fill, Interpolator.EASE_BOTH)));
	}

	public void setStroke(Color fill) {
		back.setStroke(fill);
	}

	public void setRadius(double radius) {
		this.radius = radius;
		radiusProperty.set(radius);
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

	@Override
	public void applyStyle(Style style) {		
		NodeUtils.focusBorder(this, style.getTextLink(), radius + 2);
	}
}
