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

public class ScrollBar extends StackPane {
	private Rectangle track;
	private Rectangle thumb;

	private DoubleProperty position;

	private double initPos;
	private double initY;

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
								if (weakThis.get().getScene() != null && NodeUtils.isChildOf(focused, weakChild.get())) {
									onFocus.accept(focused);
								}
							}
						}
					});
				}
			}
		});
	}
	
	public void install(Region parent, Region child) {
		install(parent, child, this);
	}

	public void scrollByPixels(double pixels, double relativeTo) {
		double newPos = position.get() - (pixels / relativeTo);
		setPos(newPos);
	}

	public void bindOpacityToHover(Node node) {
		opacityProperty().unbind();
		opacityProperty().bind(Bindings.when(node.hoverProperty().or(this.hoverProperty()).or(this.pressedProperty()))
				.then(1).otherwise(0));
	}

	private void setPos(double val) {
		position.set(Math.max(0, Math.min(1, val)));
	}

	public DoubleProperty positionProperty() {
		return position;
	}

	public void setThumbFill(Paint fill) {
		thumb.setFill(fill);
	}

	public void setTrackFill(Paint fill) {
		track.setFill(fill);
	}

	public void top() {
		setPos(0);
	}
}
