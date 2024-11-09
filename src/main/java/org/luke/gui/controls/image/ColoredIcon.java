package org.luke.gui.controls.image;

import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.style.StyledColor;
import org.luke.gui.window.Window;

/**
 * a specialized ColorIcon that allows setting a {@link StyledColor} fill color.
 *
 * @author SDIDSA
 */
public class ColoredIcon extends ColorIcon implements Styleable {
    private final StyledColor fill;

    /**
     * Constructs a ColoredIcon with the specified window, image name, size, and styled fill color.
     *
     * @param window The window associated with the ColoredIcon.
     * @param name   The name of the image file.
     * @param size   The size used for loading and displaying the image.
     * @param fill   The styled fill color applied to the icon.
     */
    public ColoredIcon(Window window, String name, double size, StyledColor fill) {
        super(name, size);
        this.fill = fill;
        applyStyle(window.getStyl());
    }

    public ColoredIcon(Window window, String name, double readSize, double displaySize, StyledColor fill) {
        super(name, readSize, displaySize);
        this.fill = fill;
        applyStyle(window.getStyl());
    }
    
    @Override
    public void applyStyle(Style style) {
        setFill(fill.apply(style));
        super.applyStyle(style);
    }
}
