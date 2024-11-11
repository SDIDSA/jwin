package org.luke.jwin.app.param.rootFiles;

import org.luke.gui.style.Style;
import org.luke.gui.style.StyledColor;

public enum RootFileType {
    EXCLUDE(Style::getTextDanger, .7f),
    SENSITIVE(Style::getTextDanger),
    INCLUDE(Style::getTextLink),
    UNKNOWN(Style::getTextMuted, .7f);

    private final StyledColor color;
    private final double opacity;

    RootFileType(StyledColor color) {
        this(color, 1.0);
    }

    RootFileType(StyledColor color, double opacity) {
        this.color = color;
        this.opacity = opacity;
    }

    public StyledColor getColor() {
        return color;
    }

    public double getOpacity() {
        return opacity;
    }
}
