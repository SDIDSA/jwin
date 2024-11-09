package org.luke.gui.style;

import java.util.function.Function;

import javafx.scene.paint.Color;

/**
 * 
 * functional interface that maps a Style to a Color.
 * 
 * @author zinou SDIDSA
 */
public interface StyledColor extends Function<Style, Color> {

}
