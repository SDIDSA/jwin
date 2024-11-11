package org.luke.gui.controls.scroll;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * A {@code HorizontalScrollable} is a container that allows its content to be scrolled horizontally.
 * It includes a horizontal scrollbar at the bottom.
 * <p>
 * Author: SDIDSA
 */
public class HorizontalScrollable extends StackPane {
    private final StackPane contentCont;
    private final HorizontalScrollBar sb;

    /**
     * Constructs a new {@code HorizontalScrollable} with default settings.
     */
    public HorizontalScrollable() {
        setAlignment(Pos.TOP_LEFT);
        setMinWidth(0);

        contentCont = new StackPane();
        StackPane scrollBarCont = new StackPane();
        scrollBarCont.setAlignment(Pos.BOTTOM_CENTER);

        sb = new HorizontalScrollBar(15, 5);
        scrollBarCont.setPickOnBounds(false);
        scrollBarCont.getChildren().add(sb);
        sb.install(this, contentCont);

        sb.opacityProperty().bind(Bindings.when(hoverProperty().or(sb.pressedProperty())).then(1).otherwise(.4));

        contentCont.translateXProperty().bind(
                sb.positionProperty().multiply(contentCont.widthProperty().subtract(widthProperty())).multiply(-1));

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        clip.xProperty().bind(contentCont.translateXProperty().negate());

        contentCont.setClip(clip);
        getChildren().addAll(contentCont, scrollBarCont);
    }

    /**
     * Gets the scrollbar associated with this {@code HorizontalScrollable}.
     *
     * @return the scrollbar
     */
    public HorizontalScrollBar getScrollBar() {
        return sb;
    }

    /**
     * Sets the content of this {@code HorizontalScrollable}.
     *
     * @param content the content to be displayed
     */
    public void setContent(Parent content) {
        contentCont.getChildren().setAll(content);
    }

    /**
     * Gets the read-only property representing the width of the content in this {@code HorizontalScrollable}.
     *
     * @return the content width property
     */
    public ReadOnlyDoubleProperty contentWidthProperty() {
        return contentCont.widthProperty();
    }
}