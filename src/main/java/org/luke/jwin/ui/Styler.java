package org.luke.jwin.ui;

import org.luke.gui.factory.Backgrounds;
import org.luke.gui.factory.Borders;
import org.luke.gui.style.Style;

import javafx.scene.layout.Region;

public class Styler {
	public static final double RADIUS = 5;
	private Styler() {}
	
	public static void styleRegion(Style style, Region region) {
		styleBackground(style, region);
		styleBorder(style, region);
	}
	
	public static void styleBackground(Style style, Region region) {
		region.setBackground(Backgrounds.make(style.getDeprecatedTextInputBg(), RADIUS));
	}
	
	public static void styleBorder(Style style, Region region) {
		region.setBorder(Borders.make(style.getDeprecatedTextInputBorder(), RADIUS));
	}
}
