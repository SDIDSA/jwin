package org.luke.gui.factory;

import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Paint;

public class Borders {
	private Borders() {
		
	}
	
	public static Border make(Paint fill, BorderStrokeStyle style, CornerRadii radius, BorderWidths width) {
		return new Border(new BorderStroke(fill, style, radius, width));
	}

	public static Border make(Paint fill, BorderStrokeStyle style, CornerRadii radius) {
		return make(fill, style, radius, null);
	}

	public static Border make(Paint fill, BorderStrokeStyle style, BorderWidths width) {
		return make(fill, style, null, width);
	}

	public static Border make(Paint fill, CornerRadii radius, BorderWidths width) {
		return make(fill, BorderStrokeStyle.SOLID, radius, width);
	}

	public static Border make(Paint fill, double radius, double width) {
		return make(fill, BorderStrokeStyle.SOLID, new CornerRadii(radius), new BorderWidths(width));
	}

	public static Border make(Paint fill, CornerRadii radius) {
		return make(fill, BorderStrokeStyle.SOLID, radius, null);
	}

	public static Border make(Paint fill, double radius) {
		return make(fill, BorderStrokeStyle.SOLID, new CornerRadii(radius), null);
	}

	public static Border make(Paint fill, BorderStrokeStyle style) {
		return make(fill, style, null, null);
	}

	public static Border make(Paint fill, BorderWidths width) {
		return make(fill, BorderStrokeStyle.SOLID, null, width);
	}

	public static Border make(Paint fill) {
		return make(fill, BorderStrokeStyle.SOLID, null, null);
	}
}
