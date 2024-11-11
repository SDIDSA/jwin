package org.luke.gui.controls.popup.tooltip;

import java.util.HashMap;

import org.luke.gui.controls.popup.Direction;
import org.luke.gui.controls.shape.Triangle;
import org.luke.gui.exception.ErrorHandler;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.PopupControl;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * A custom tooltip implementation that extends PopupControl and implements
 * Styleable. It provides features such as specifying the direction, offset, and
 * radius of the tooltip, as well as methods for adding content and
 * showing/hiding the tooltip based on mouse events.
 * 
 * @author SDIDSA
 */
public class Tooltip extends PopupControl implements Styleable {
	public static final Direction UP = Direction.UP;
	public static final Direction RIGHT = Direction.RIGHT;
	public static final Direction DOWN = Direction.DOWN;
	public static final Direction LEFT = Direction.LEFT;

	protected static DropShadow ds = new DropShadow(10, Color.gray(0, .25));
	protected static DropShadow outline = new DropShadow(BlurType.GAUSSIAN, Color.gray(.5, .3), 1, 1, 0, 0);
	static {
		ds.setInput(outline);
	}

	protected Window owner;
	protected Pane root;

	protected Direction direction;

	protected StackPane content;
	protected Triangle arr;

	protected Timeline fadeIn;
	protected Timeline fadeOut;

	protected double offsetX;
	protected double offsetY;

	protected double radius;

    /**
	 * Constructs a tooltip with the specified window, direction, offset, and
	 * radius.
	 * 
	 * @param window    The window associated with the tooltip.
	 * @param direction The direction of the tooltip.
	 * @param offsetX   The horizontal offset of the tooltip.
	 * @param offsetY   The vertical offset of the tooltip.
	 * @param radius    The radius of the tooltip corners.
	 */
	public Tooltip(Window window, Direction direction, double offsetX, double offsetY, double radius) {
		this.owner = window;
		this.direction = direction;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.radius = radius;

        StackPane preroot = new StackPane();
		preroot.setPadding(new Insets(10));

		preroot.getChildren().clear();
		root = direction.toPane();
		root.setEffect(ds);
		root.setOpacity(0);
		root.setScaleX(.7);
		root.setScaleY(.7);

		content = new StackPane();
		content.setPadding(new Insets(15));

		arr = new Triangle(10);
		switch (direction) {
			case LEFT:
				arr.setRotate(180);
				break;
			case UP:
				arr.setRotate(-90);
				break;
			case DOWN:
				arr.setRotate(90);
				break;
			default:
				break;
		}

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

		fadeOut.setOnFinished(_ -> hide());

		preroot.getChildren().add(root);

		getScene().setRoot(preroot);

		applyStyle(window.getStyl());
	}

	public Tooltip(Window window, Direction direction, double offsetX, double offsetY) {
		this(window, direction, offsetX, offsetY, 5.0);
	}

	public Tooltip(Window window, Direction direction, double offset) {
		this(window, direction, offset, offset, 5.0);
	}

	public void add(Node... nodes) {
		content.getChildren().addAll(nodes);
	}

	public void setPadding(int pad) {
		content.setPadding(new Insets(8 + pad, 12 + pad, 8 + pad, 12 + pad));
	}

	public void setOffsetX(double offsetX) {
		this.offsetX = offsetX;
	}

	public void setOffsetY(double offsetY) {
		this.offsetY = offsetY;
	}

	public Tooltip(Window window, Direction direction) {
		this(window, direction, 0, 0);
	}

	private void position(Node node, Direction finalToUse) {

		double[] pos = finalToUse.calcPos(this, node, offsetX, offsetY);

		double px = pos[0];
		double py = pos[1];

		setX(px);
		setY(py);
	}

	/**
	 * Shows the tooltip relative to the provided node. If the tooltip is already
	 * showing, adjusts its position.
	 * 
	 * @param node The node relative to which the tooltip is shown.
	 */
	public void showPop(Node node) {
		Direction toUse = direction;
		if(direction.isHorizontal() && owner.getLocale().get().isRtl()) {
			toUse = direction.flipHorizontal();
		}

		switch (toUse) {
			case LEFT:
				arr.setRotate(180);
				break;
			case RIGHT:
				arr.setRotate(0);
				break;
			case UP:
				arr.setRotate(-90);
				break;
			case DOWN:
				arr.setRotate(90);
				break;
			default:
				break;
		}
		if (toUse.isArrowFirst()) {
			root.getChildren().setAll(arr, content);
		} else {
			root.getChildren().setAll(content, arr);
		}
		fadeOut.stop();
		Direction finalToUse = toUse;
		Runnable adjust = () -> {
			int size = 6 + (int) (content.getHeight() / 30);
			size /= 2;
			size *= 2;
			arr.setSize(size);

			new Thread(() -> {
				try {
					Thread.sleep(10);
				} catch (InterruptedException x) {
					ErrorHandler.handle(x, "show tooltip");
					Thread.currentThread().interrupt();
				}

				Platform.runLater(() -> {
					position(node, finalToUse);
					fadeIn.playFromStart();
				});
			}, "tooltip thread").start();
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

	private static final HashMap<Node, Installation> registered = new HashMap<>();

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

	static class Installation {
		EventHandler<MouseEvent> onEnter;
		EventHandler<MouseEvent> onExit;
		Node node;

		public Installation(Node node, Tooltip tip) {
			this.node = node;
			this.onEnter = _ -> tip.showPop(node);
			this.onExit = _ -> tip.fadeOut();
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
		content.setBackground(Backgrounds.make(style.getBackgroundFloatingOr(), radius));
		arr.setFill(style.getBackgroundFloatingOr());
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

}
