package org.luke.gui.factory;

import java.util.HashMap;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Paint;

public class Backgrounds {
	private Backgrounds() {

	}

	private static HashMap<BackSet, Background> cache = new HashMap<>();

	public static Background make(Paint fill, CornerRadii radius, Insets insets) {
		BackSet bs = new BackSet(fill, radius, insets);
		
		Background res = cache.get(bs);
		if(res == null) {
			res = bs.getBackground();
			cache.put(bs, res);
		}
		
		return res;
	}

	public static Background make(Paint fill, CornerRadii radius) {
		return make(fill, radius, null);
	}

	public static Background make(Paint fill, Insets insets) {
		return make(fill, null, insets);
	}

	public static Background make(Paint fill, double radius) {
		return make(fill, new CornerRadii(radius), null);
	}

	public static Background make(Paint fill, double radius, double insets) {
		return make(fill, new CornerRadii(radius), new Insets(insets));
	}

	public static Background make(Paint fill) {
		return make(fill, null, null);
	}

	private static class BackSet {
		private Paint fill;
		private CornerRadii radius;
		private Insets insets;

		public BackSet(Paint fill, CornerRadii radius, Insets insets) {
			this.fill = fill;
			this.radius = radius;
			this.insets = insets;
		}

		public Background getBackground() {
			return new Background(new BackgroundFill(fill, radius, insets));
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof BackSet backSet) {
				return equals(fill, backSet.fill) && equals(radius, backSet.radius) && equals(insets, backSet.insets);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return hash(fill) + hash(radius) + hash(insets);
		}

		private boolean equals(Object o1, Object o2) {
			return ((o1 != null && o1.equals(o2)) || (o1 == null && o2 == null));
		}

		private static int hash(Object obj) {
			return (obj == null ? 0 : obj.hashCode());
		}
	}
}
