package org.luke.gui.factory;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Paint;

import java.util.HashMap;

/**
 * Utility class for creating and caching JavaFX Background objects with various
 * configurations. This class allows the creation of Background objects with
 * different fill colors, corner radii, and insets. It also caches the
 * Background objects to improve performance by avoiding redundant object
 * creation.
 */
public class Backgrounds {

	private Backgrounds() {
		// Private constructor to prevent instantiation; this class is intended for
		// static use only.
	}

	private static final HashMap<BackSet, Background> cache = new HashMap<>();

	/**
	 * Creates a Background with the specified fill color, corner radii, and insets.
	 * 
	 * @param fill   The fill color of the background.
	 * @param radius The corner radii of the background.
	 * @param insets The insets of the background.
	 * @return The created Background object.
	 */
	public static Background make(Paint fill, CornerRadii radius, Insets insets) {
		BackSet bs = new BackSet(fill, radius, insets);

		Background res = cache.get(bs);
		if (res == null) {
			res = bs.getBackground();
			cache.put(bs, res);
		}

		return res;
	}

	/**
	 * Creates a Background with the specified fill color and corner radii.
	 * 
	 * @param fill   The fill color of the background.
	 * @param radius The corner radii of the background.
	 * @return The created Background object.
	 */
	public static Background make(Paint fill, CornerRadii radius) {
		return make(fill, radius, null);
	}

	/**
	 * Creates a Background with the specified fill color and insets.
	 * 
	 * @param fill   The fill color of the background.
	 * @param insets The insets of the background.
	 * @return The created Background object.
	 */
	public static Background make(Paint fill, Insets insets) {
		return make(fill, null, insets);
	}

	/**
	 * Creates a Background with the specified fill color and a single corner radius
	 * for all corners.
	 * 
	 * @param fill   The fill color of the background.
	 * @param radius The corner radius for all corners of the background.
	 * @return The created Background object.
	 */
	public static Background make(Paint fill, double radius) {
		return make(fill, new CornerRadii(radius), null);
	}

	/**
	 * Creates a Background with the specified fill color, corner radius, and
	 * insets.
	 * 
	 * @param fill   The fill color of the background.
	 * @param radius The corner radius for all corners of the background.
	 * @param insets The insets of the background.
	 * @return The created Background object.
	 */
	public static Background make(Paint fill, double radius, double insets) {
		return make(fill, new CornerRadii(radius), new Insets(insets));
	}

	/**
	 * Creates a Background with the specified fill color.
	 * 
	 * @param fill The fill color of the background.
	 * @return The created Background object.
	 */
	public static Background make(Paint fill) {
		return make(fill, null, null);
	}

	private static class BackSet {
		private final Paint fill;
		private final CornerRadii radius;
		private final Insets insets;

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
