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
import org.luke.jwin.app.Jwin;

/**
 * Custom implementation of a horizontal scrollbar for scrolling content within a
 * specified region. The scrollbar consists of a track and a draggable thumb for
 * adjusting the scroll position. Supports mouse interaction to drag the thumb
 * and scroll the content.
 *
 * @author SDIDSA
 */
public class HorizontalScrollBar extends StackPane {
    private final Rectangle track;
    private final Rectangle thumb;

    private final DoubleProperty position;

    private double initPos;
    private double initX;

    /**
     * Constructs a horizontal scrollbar with the specified height and padding.
     *
     * @param height  The height of the scrollbar.
     * @param padding The padding applied to the scrollbar.
     */
    public HorizontalScrollBar(double height, double padding) {
        setAlignment(Pos.CENTER_LEFT);
        double effectiveHeight = height - padding * 2;

        StackPane.setAlignment(this, Pos.BOTTOM_CENTER);

        setPadding(new Insets(padding));
        position = new SimpleDoubleProperty(0);

        track = new Rectangle();
        track.widthProperty().bind(widthProperty().subtract(padding * 2));
        track.setFill(Color.TRANSPARENT);
        thumb = new Rectangle();

        thumb.setArcHeight(effectiveHeight + 2);
        thumb.setArcWidth(effectiveHeight + 2);
        track.setArcHeight(effectiveHeight + 2);
        track.setArcWidth(effectiveHeight + 2);

        thumb.translateXProperty().bind(position.multiply(track.widthProperty().subtract(thumb.widthProperty())));

        setOnMousePressed(e -> {
            initPos = position.get();
            initX = e.getScreenX();
        });

        setOnMouseDragged(e -> {
            double dx = (e.getScreenX() - initX) / (track.getWidth() - thumb.getWidth());
            int direction = Jwin.winstance.getLocale().get().isRtl() ? -1 : 1;
            setPos(initPos + dx * direction);
        });

        getChildren().addAll(track, thumb);

        thumb.setHeight(effectiveHeight);
        track.setHeight(effectiveHeight);
        setMinHeight(height);
        setMaxHeight(height);

        setMaxWidth(USE_PREF_SIZE);
        setMinWidth(USE_PREF_SIZE);
        setCursor(Cursor.DEFAULT);
    }

    /**
     * Installs the scrollbar within a parent region and binds it to a child region.
     *
     * @param parent The parent region.
     * @param child  The child region to bind the scrollbar to.
     * @param sb     The scrollbar to install.
     */
    private static void install(Region parent, Region child, HorizontalScrollBar sb) {
        sb.thumb.widthProperty().bind(Bindings.max(40,
                parent.widthProperty().divide(child.widthProperty()).multiply(sb.track.widthProperty())));

        child.translateXProperty().bind(
                sb.positionProperty().multiply(child.widthProperty().subtract(parent.widthProperty())).multiply(-1));

        child.addEventFilter(ScrollEvent.ANY, e -> sb.scrollByPixels(e.getDeltaX(), child.getWidth()));

        sb.prefWidthProperty().bind(parent.widthProperty());

        sb.visibleProperty().bind(sb.thumb.widthProperty().lessThan(sb.track.widthProperty()));

        WeakReference<Region> weakChild = new WeakReference<>(child);
        WeakReference<Region> weakParent = new WeakReference<>(parent);
        WeakReference<HorizontalScrollBar> weakThis = new WeakReference<>(sb);

        Consumer<Node> onFocus = node -> {
            Bounds b = weakChild.get().sceneToLocal(node.localToScene(node.getBoundsInLocal()));
            double minX = b.getMinX();
            double maxX = b.getMaxX();

            double minDX = -weakChild.get().getTranslateX();
            double maxDX = minDX + weakParent.get().getWidth();
            if (minDX > minX) {
                weakThis.get().setPos(minX / (weakChild.get().getWidth() - weakParent.get().getWidth()));
            } else if (maxDX < maxX) {
                weakThis.get().setPos((maxX - weakParent.get().getWidth())
                        / (weakChild.get().getWidth() - weakParent.get().getWidth()));
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
     * Sets the scrollbar position to the left.
     */
    public void left() {
        setPos(0);
    }
}