package org.luke.gui.controls.scroll;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * A {@code Scrollable} is a container that allows its content to be scrolled vertically.
 * It includes a vertical scrollbar on the right side.
 * <p>
 * Author: SDIDSA
 */
public class Scrollable extends StackPane {
    private StackPane contentCont;

    private ScrollBar sb;

    /**
     * Constructs a new {@code Scrollable} with default settings.
     */
    public Scrollable() {
        setAlignment(Pos.TOP_LEFT);
        setMinHeight(0);

        contentCont = new StackPane();
        StackPane scrollBarCont = new StackPane();
        scrollBarCont.setAlignment(Pos.CENTER_RIGHT);

        sb = new ScrollBar(15, 5);
        scrollBarCont.setPickOnBounds(false);
        scrollBarCont.getChildren().add(sb);
        sb.install(this, contentCont);

        sb.opacityProperty().bind(Bindings.when(hoverProperty().or(sb.pressedProperty())).then(1).otherwise(.4));

        contentCont.translateYProperty().bind(
                sb.positionProperty().multiply(contentCont.heightProperty().subtract(heightProperty())).multiply(-1));

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        clip.yProperty().bind(contentCont.translateYProperty().negate());

        contentCont.setClip(clip);
        getChildren().addAll(contentCont, scrollBarCont);
    }

    /**
     * Gets the scrollbar associated with this {@code Scrollable}.
     *
     * @return the scrollbar
     */
    public ScrollBar getScrollBar() {
        return sb;
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
     * Gets the read-only property representing the height of the content in this {@code Scrollable}.
     *
     * @return the content height property
     */
    public ReadOnlyDoubleProperty contentHeightProperty() {
        return contentCont.heightProperty();
    }
}
