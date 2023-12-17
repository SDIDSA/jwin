package org.luke.gui.controls.popup.tooltip;

import java.util.HashMap;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.controls.shape.Triangle;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.PopupControl;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class Tooltip extends PopupControl implements Styleable {
	public static final Direction UP = Direction.UP;
	public static final Direction RIGHT = Direction.RIGHT;
	public static final Direction DOWN = Direction.DOWN;
	public static final Direction LEFT = Direction.LEFT;

	private static DropShadow ds = new DropShadow(15, Color.gray(0, .25));

	protected Window owner;
	private Pane root;
	private Text text;

	private Direction direction;

	private StackPane content;
	private Triangle arr;

	private Timeline fadeIn;
	private Timeline fadeOut;

	private double offsetX;
	private double offsetY;

	public Tooltip(Window window, String val, Direction direction, double offsetX, double offsetY) {
		this.owner = window;
		this.direction = direction;
		this.offsetX = offsetX;
		this.offsetY = offsetY;

		StackPane preroot = new StackPane();
		preroot.setPadding(new Insets(15));

		root = direction.toPane();
		root.setEffect(ds);
		root.setOpacity(0);
		root.setScaleX(.7);
		root.setScaleY(.7);

		content = new StackPane();
		content.setPadding(new Insets(8, 12, 8, 12));

		text = new Text(val);
		text.setFont(new Font(Font.DEFAULT_FAMILY_MEDIUM, 16).getFont());

		arr = new Triangle(10);
		switch (direction) {
		case LEFT:
			arr.setRotate(180);
			break;
		case UP:
			arr.setRotate(-90);
			arr.setTranslateY(-2.5);
			break;
		case DOWN:
			arr.setRotate(90);
			arr.setTranslateY(2.5);
			break;
		default:
			break;
		}

		content.getChildren().add(text);

		if (direction.isArrowFirst()) {
			root.getChildren().addAll(arr, content);
		} else {
			root.getChildren().addAll(content, arr);
		}

		fadeIn = new Timeline(
				new KeyFrame(Duration.seconds(.05), new KeyValue(root.opacityProperty(), 1.0, Interpolator.EASE_BOTH),
						new KeyValue(root.scaleXProperty(), 1.0, Interpolator.EASE_BOTH),
						new KeyValue(root.scaleYProperty(), 1.0, Interpolator.EASE_BOTH)));

		fadeOut = new Timeline(
				new KeyFrame(Duration.seconds(.05), new KeyValue(root.opacityProperty(), 0, Interpolator.EASE_BOTH),
						new KeyValue(root.scaleXProperty(), .7, Interpolator.EASE_BOTH),
						new KeyValue(root.scaleYProperty(), .7, Interpolator.EASE_BOTH)));

		fadeOut.setOnFinished(e -> hide());

		preroot.getChildren().add(root);
		
		getScene().setRoot(preroot);

		applyStyle(window.getStyl());
	}

	public void setFont(Font font) {
		text.setFont(font.getFont());
	}
	
	public void setOffsetX(double offsetX) {
		this.offsetX = offsetX;
	}

	public void setOffsetY(double offsetY) {
		this.offsetY = offsetY;
	}
	
	public Tooltip(Window window, String val, Direction direction) {
		this(window, val, direction, 0, 0);
	}

	public void setText(String txt) {
		text.setText(txt);
	}

	private void position(Node node) {
		double[] pos = direction.calcPos(this, node, offsetX, offsetY);

		double px = pos[0];
		double py = pos[1];

		setX(px);
		setY(py);
	}
	
	public void showPop(Node node) {
		fadeOut.stop();
		Runnable adjust = () -> {			
			position(node);
			fadeIn.playFromStart();
		};
		if (isShowing()) {
			adjust.run();
		} else {
			setOnShown(e -> adjust.run());
			this.show(owner);
		}
	}

	public void fadeOut() {
		fadeIn.stop();
		fadeOut.playFromStart();
	}

	private static HashMap<Node, Installation> registered = new HashMap<>();

	public static void install(Node node, Tooltip tooltip) {
		Installation evs = registered.get(node);
		if (evs != null) {
			evs.uninstall();
		}
		evs = new Installation(node, tooltip);
		evs.install();
		registered.put(node, evs);
	}

	public static void uninstall(Node node, Tooltip tooltip) {
		Installation evs = registered.get(node);
		if (evs != null) {
			evs.uninstall();
			registered.remove(node);
		}
	}
	
	public static void clear() {
		registered.clear();
	}

	public static void install(Node node, Direction dir, String value, double offsetX, double offsetY, boolean key) {
		if (node.getScene() == null) {
			node.sceneProperty().addListener(new ChangeListener<Scene>() {
				@Override
				public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
					if (newValue != null) {
						Window w = (Window) newValue.getWindow();
						Tooltip tip = key ? new KeyedTooltip(w, value, dir, offsetX, offsetY)
								: new Tooltip(w, value, dir, offsetX, offsetY);
						install(node, tip);
						node.sceneProperty().removeListener(this);
					}
				}
			});
		} else {
			Window w = (Window) node.getScene().getWindow();
			Tooltip tip = key ? new KeyedTooltip(w, value, dir, offsetX, offsetY) : new Tooltip(w, value, dir, offsetX, offsetY);
			install(node, tip);
		}
	}

	public static void install(Node node, Direction dir, String value, boolean key) {
		install(node, dir, value, 0, 0, key);
	}

	public static void install(Node node, Direction dir, String value, double offset) {
		install(node, dir, value, offset, offset, false);
	}

	public static void install(Node node, Direction dir, String value) {
		install(node, dir, value, 0, 0, false);
	}

	static class Installation {
		EventHandler<MouseEvent> onEnter;
		EventHandler<MouseEvent> onExit;
		Node node;

		public Installation(Node node, Tooltip tip) {
			this.node = node;
			this.onEnter = e -> tip.showPop(node);
			this.onExit = e -> tip.fadeOut();
		}

		public void install() {
			node.addEventFilter(MouseEvent.MOUSE_ENTERED, onEnter);
			node.addEventFilter(MouseEvent.MOUSE_EXITED, onExit);
			node.addEventFilter(MouseEvent.MOUSE_CLICKED, onExit);
		}

		public void uninstall() {
			node.removeEventFilter(MouseEvent.MOUSE_ENTERED, onEnter);
			node.removeEventFilter(MouseEvent.MOUSE_EXITED, onExit);
			node.removeEventFilter(MouseEvent.MOUSE_CLICKED, onExit);
		}
	}

	@Override
	public void applyStyle(Style style) {
		content.setBackground(Backgrounds.make(style.getBackgroundFloating(), 5.0));
		text.setFill(style.getTextNormal());
		arr.setFill(style.getBackgroundFloating());
	}
	
	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

}
