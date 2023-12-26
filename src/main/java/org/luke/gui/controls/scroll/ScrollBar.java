package org.luke.gui.controls.scroll;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

import org.luke.gui.NodeUtils;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

/**
 * Custom implementation of a vertical scrollbar for scrolling content within a
 * specified region. The scrollbar consists of a track and a draggable thumb for
 * adjusting the scroll position. Supports mouse interaction to drag the thumb
 * and scroll the content.
 *
 * @author SDIDSA
 */
public class ScrollBar extends StackPane {
	private Rectangle track;
	private Rectangle thumb;

	private DoubleProperty position;

	private double initPos;
	private double initY;

	/**
	 * Constructs a vertical scrollbar with the specified width and padding.
	 *
	 * @param width   The width of the scrollbar.
	 * @param padding The padding applied to the scrollbar.
	 */
	public ScrollBar(double width, double padding) {
		setAlignment(Pos.TOP_CENTER);
		double effectiveWidth = width - padding * 2;

		StackPane.setAlignment(this, Pos.CENTER_RIGHT);

		setPadding(new Insets(padding));
		position = new SimpleDoubleProperty(0);

		track = new Rectangle();
		track.heightProperty().bind(heightProperty().subtract(padding * 2));
		track.setFill(Color.TRANSPARENT);
		thumb = new Rectangle();

		thumb.setArcHeight(effectiveWidth + 2);
		thumb.setArcWidth(effectiveWidth + 2);
		track.setArcHeight(effectiveWidth + 2);
		track.setArcWidth(effectiveWidth + 2);

		thumb.translateYProperty().bind(position.multiply(track.heightProperty().subtract(thumb.heightProperty())));

		setOnMousePressed(e -> {
			initPos = position.get();
			initY = e.getScreenY();
		});

		setOnMouseDragged(e -> {
			double dy = (e.getScreenY() - initY) / (track.getHeight() - thumb.getHeight());
			setPos(initPos + dy);
		});

		getChildren().addAll(track, thumb);

		thumb.setWidth(effectiveWidth);
		track.setWidth(effectiveWidth);
		setMinWidth(width);
		setMaxWidth(width);

		setMaxHeight(USE_PREF_SIZE);
		setMinHeight(USE_PREF_SIZE);
		setCursor(Cursor.DEFAULT);
	}

	/**
	 * Installs the scrollbar within a parent region and binds it to a child region.
	 *
	 * @param parent The parent region.
	 * @param child  The child region to bind the scrollbar to.
	 * @param sb     The scrollbar to install.
	 */
	private static void install(Region parent, Region child, ScrollBar sb) {
		sb.thumb.heightProperty().bind(Bindings.max(40,
				parent.heightProperty().divide(child.heightProperty()).multiply(sb.track.heightProperty())));

		child.translateYProperty().bind(
				sb.positionProperty().multiply(child.heightProperty().subtract(parent.heightProperty())).multiply(-1));

		child.addEventFilter(ScrollEvent.ANY, e -> sb.scrollByPixels(e.getDeltaY(), child.getHeight()));

		sb.prefHeightProperty().bind(parent.heightProperty());

		sb.visibleProperty().bind(sb.thumb.heightProperty().lessThan(sb.track.heightProperty()));

		WeakReference<Region> weakChild = new WeakReference<>(child);
		WeakReference<Region> weakParent = new WeakReference<>(parent);
		WeakReference<ScrollBar> weakThis = new WeakReference<>(sb);

		Consumer<Node> onFocus = node -> {
			Bounds b = weakChild.get().sceneToLocal(node.localToScene(node.getBoundsInLocal()));
			double minY = b.getMinY();
			double maxY = b.getMaxY();

			double minDY = -weakChild.get().getTranslateY();
			double maxDY = minDY + weakParent.get().getHeight();
			if (minDY > minY) {
				weakThis.get().setPos(minY / (weakChild.get().getHeight() - weakParent.get().getHeight()));
			} else if (maxDY < maxY) {
				weakThis.get().setPos((maxY - weakParent.get().getHeight())
						/ (weakChild.get().getHeight() - weakParent.get().getHeight()));
			}
		};

		child.sceneProperty().addListener(new ChangeListener<Scene>() {
			public void changed(ObservableValue<? extends Scene> obs, Scene oldScene, Scene scene) {
				if (scene != null) {
					weakChild.get().sceneProperty().removeListener(this);
					scene.focusOwnerProperty().addListener(new ChangeListener<Node>() {
						public void changed(ObservableValue<? extends Node> obn, Node oldFocused, Node focused) {
							if (weakChild.get() == null || weakParent.get() == null || weakThis.get() == null) {
								scene.focusOwnerProperty().removeListener(this);
							} else {
								if (weakThis.get().getScene() != null
										&& NodeUtils.isChildOf(focused, weakChild.get())) {
									onFocus.accept(focused);
								}
							}
						}
					});
				}
			}
		});
	}

	/**
	 * Installs the scrollbar within a parent region and binds it to a child region.
	 *
	 * @param parent The parent region.
	 * @param child  The child region to bind the scrollbar to.
	 */
	public void install(Region parent, Region child) {
		install(parent, child, this);
	}

	/**
	 * Scrolls the content by a specified number of pixels.
	 *
	 * @param pixels     The number of pixels to scroll.
	 * @param relativeTo The relative dimension for calculating the scroll.
	 */
	public void scrollByPixels(double pixels, double relativeTo) {
		double newPos = position.get() - (pixels / relativeTo);
		setPos(newPos);
	}

	/**
	 * Binds the scrollbar's opacity to the hover state of a specified node.
	 *
	 * @param node The node whose hover state determines the scrollbar's opacity.
	 */
	public void bindOpacityToHover(Node node) {
		opacityProperty().unbind();
		opacityProperty().bind(Bindings.when(node.hoverProperty().or(this.hoverProperty()).or(this.pressedProperty()))
				.then(1).otherwise(0));
	}

	/**
	 * Sets the position of the scrollbar.
	 *
	 * @param val The new position value (between 0 and 1).
	 */
	private void setPos(double val) {
		position.set(Math.max(0, Math.min(1, val)));
	}

	/**
	 * Gets the position property of the scrollbar.
	 *
	 * @return The position property of the scrollbar.
	 */
	public DoubleProperty positionProperty() {
		return position;
	}

	/**
	 * Sets the fill color of the thumb.
	 *
	 * @param fill The fill color to set.
	 */
	public void setThumbFill(Paint fill) {
		thumb.setFill(fill);
	}

	/**
	 * Sets the fill color of the track.
	 *
	 * @param fill The fill color to set.
	 */
	public void setTrackFill(Paint fill) {
		track.setFill(fill);
	}

	/**
	 * Sets the scrollbar position to the top.
	 */
	public void top() {
		setPos(0);
	}
}
