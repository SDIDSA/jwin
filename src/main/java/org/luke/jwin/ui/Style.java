package org.luke.jwin.ui;

import org.luke.jwin.app.utils.Backgrounds;
import org.luke.jwin.app.utils.Borders;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class Style {
	public static final double RADIUS = 0;
	private Style() {}
	
	public static void styleRegion(Region region) {
		styleBackground(region);
		styleBorder(region);
	}
	
	public static void styleBackground(Region region) {
		region.setBackground(Backgrounds.make(Color.WHITE, RADIUS));
	}
	
	public static void styleBorder(Region region) {
		region.setBorder(Borders.make(Color.LIGHTGRAY, RADIUS));
	}
}
