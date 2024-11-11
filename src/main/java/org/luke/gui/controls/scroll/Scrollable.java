package org.luke.gui.controls.scroll;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * A {@code Scrollable} is a container that allows its content to be scrolled both horizontally and vertically.
 * It includes a horizontal scrollbar at the bottom and a vertical scrollbar on the right side.
 * <p>
 * Author: SDIDSA
 */
public class Scrollable extends StackPane {
    private final StackPane contentCont;
    private final HorizontalScrollBar hScrollBar;
    private final VerticalScrollBar vScrollBar;

    /**
     * Constructs a new {@code Scrollable} with default settings.
     */
    public Scrollable() {
        setAlignment(Pos.TOP_LEFT);
        setMinWidth(0);
        setMinHeight(0);

        contentCont = new StackPane();
        StackPane hScrollBarCont = new StackPane();
        StackPane vScrollBarCont = new StackPane();

        hScrollBarCont.setAlignment(Pos.BOTTOM_CENTER);
        vScrollBarCont.setAlignment(Pos.CENTER_RIGHT);

        hScrollBar = new HorizontalScrollBar(10, 2);
        vScrollBar = new VerticalScrollBar(10, 2);

        hScrollBarCont.setPickOnBounds(false);
        vScrollBarCont.setPickOnBounds(false);

        hScrollBarCont.getChildren().add(hScrollBar);
        vScrollBarCont.getChildren().add(vScrollBar);

        hScrollBar.install(this, contentCont);
        vScrollBar.install(this, contentCont);

        hScrollBar.opacityProperty().bind(Bindings.when(hoverProperty().or(hScrollBar.pressedProperty())).then(.6).otherwise(.3));
        vScrollBar.opacityProperty().bind(Bindings.when(hoverProperty().or(vScrollBar.pressedProperty())).then(.6).otherwise(.3));

        contentCont.translateXProperty().bind(
                hScrollBar.positionProperty().multiply(contentCont.widthProperty().subtract(widthProperty())).multiply(-1));
        contentCont.translateYProperty().bind(
                vScrollBar.positionProperty().multiply(contentCont.heightProperty().subtract(heightProperty())).multiply(-1));

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        clip.xProperty().bind(contentCont.translateXProperty().negate());
        clip.yProperty().bind(contentCont.translateYProperty().negate());

        contentCont.setClip(clip);
        getChildren().addAll(contentCont, hScrollBarCont, vScrollBarCont);
        hScrollBarCont.prefHeightProperty().bind(heightProperty());
    }

    /**
     * Gets the horizontal scrollbar associated with this {@code Scrollable}.
     *
     * @return the horizontal scrollbar
     */
    public HorizontalScrollBar getHorizontalScrollBar() {
        return hScrollBar;
    }

    /**
     * Gets the vertical scrollbar associated with this {@code Scrollable}.
     *
     * @return the vertical scrollbar
     */
    public VerticalScrollBar getVerticalScrollBar() {
        return vScrollBar;
    }

    /**
     * Sets the content of this {@code Scrollable}.
     *
     * @param content the content to be displayed
     */
    public void setContent(Parent content) {
        contentCont.getChildren().setAll(content);
    }

    /**
     * Gets the read-only property representing the width of the content in this {@code Scrollable}.
     *
     * @return the content width property
     */
    public ReadOnlyDoubleProperty contentWidthProperty() {
        return contentCont.widthProperty();
    }

    /**
     * Gets the read-only property representing the height of the content in this {@code Scrollable}.
     *
     * @return the content height property
     */
    public ReadOnlyDoubleProperty contentHeightProperty() {
        return contentCont.heightProperty();
    }
}