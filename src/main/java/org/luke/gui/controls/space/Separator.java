package org.luke.gui.controls.space;

import org.luke.gui.factory.Backgrounds;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * extends the Region class and represents a separator that can be used
 * in JavaFX layouts either horizontally or vertically. It provides customization options
 * for the separator's appearance and style.
 * <p>
 * Implements the Styleable interface to enable styling based on the current style settings.
 * </p>
 *
 * @author SDIDSA
 */
public class Separator extends Region implements Styleable {
    private final DropShadow ds;

    /**
     * Constructs a Separator with the specified window and orientation.
     *
     * @param window The window associated with the separator.
     * @param or     The orientation of the separator (HORIZONTAL or VERTICAL).
     */
    public Separator(Window window, Orientation or) {
        switch (or) {
            case HORIZONTAL:
                setMaxHeight(1);
                setMinHeight(1);
                HBox.setHgrow(this, Priority.ALWAYS);
                break;
            case VERTICAL:
                setMinWidth(1);
                setMaxWidth(1);
                VBox.setVgrow(this, Priority.ALWAYS);
                break;
            default:
                break;
        }

        setFill(Color.GRAY);

        ds = new DropShadow();
        ds.setRadius(4);
        setEffect(ds);

        setViewOrder(-1);

        applyStyle(window.getStyl());
    }

    /**
     * Sets the fill color of the separator.
     *
     * @param fill The fill color to set.
     */
    public void setFill(Paint fill) {
        setBackground(Backgrounds.make(fill));
    }

    @Override
    public void applyStyle(Style style) {
        setFill(style.getBackgroundModifierAccent());
        ds.setColor(style.getBackgroundTertiaryOr());
    }

    @Override
    public void applyStyle(ObjectProperty<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
