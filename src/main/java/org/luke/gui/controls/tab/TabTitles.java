package org.luke.gui.controls.tab;

import org.luke.gui.factory.Backgrounds;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;

/**
 * TabTitles Class.
 *
 * <p>
 * This class extends the HBox class and represents the container for tab titles in a TabPane.
 * It provides methods for applying styles to the container.
 * </p>
 * <p>
 * Implements the Styleable interface to enable styling based on the current style settings.
 * </p>
 *
 * @author SDIDSA
 */
public class TabTitles extends HBox implements Styleable {

    /**
     * Constructs a TabTitles with the specified window.
     *
     * @param window The window associated with the TabTitles.
     */
    public TabTitles(Window window) {
        setPadding(new Insets(5));
        applyStyle(window.getStyl());
    }

    @Override
    public void applyStyle(Style style) {
        setBackground(Backgrounds.make(style.getBackgroundTertiaryOr(), new CornerRadii(5, 5, 0, 0, false)));
    }

    @Override
    public void applyStyle(ObjectProperty<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
