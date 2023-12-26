package org.luke.gui.style;

import java.lang.ref.WeakReference;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;

/**
 * Represents a Styleable Node. <br>
 * <br>
 * Classes implementing this interface have to define the
 * {@link Styleable#applyStyle(Style) applyStyle} method and will be affected by
 * {@link mesa.gui.window.Window#setStyle(Style) Window.setStyle(Style)} calls.
 * 
 * @author SDIDSA
 * 
 */
public interface Styleable {

	/**
	 * Applies the passed {@link Style} on this Node, The behavior of this method is
	 * defined by subclasses. <br>
	 * <br>
	 * <b>Note :</b> do not manually call this, use {@link mesa.gui.window.Window
	 * Window}<span style= "color:
	 * #0066cc;">.</span>{@link mesa.gui.window.Window#setStyle(Style)
	 * setStyle(Style)} in to apply a {@link Style} on the whole scene graph.
	 * 
	 * @param style - the {@link Style} to be applied on this Node
	 */

	void applyStyle(Style style);

	void applyStyle(ObjectProperty<Style> style);

	public static String colorToCss(Color color) {
		return "rgb(" + (int) (color.getRed() * 255) + "," + (int) (color.getGreen() * 255) + ","
				+ (int) (color.getBlue() * 255) + ", " + color.getOpacity() + ")";
	}

	public static void bindStyle(Styleable node, ObjectProperty<Style> style) {
		bindStyleWeak(node, style);
	}
	
	private static void bindStyleWeak(Styleable node, ObjectProperty<Style> style) {
		node.applyStyle(style.get());

		WeakReference<Styleable> weakNode = new WeakReference<>(node);
		
		ChangeListener<Style> listener = new ChangeListener<Style>() {
			@Override
			public void changed(ObservableValue<? extends Style> obs, Style ov, Style nv) {
				if (weakNode.get() != null) {
					if (nv != ov) {
						weakNode.get().applyStyle(nv);
					}
				} else {
					style.removeListener(this);
				}
			}
		};
		
		style.addListener(listener);
	}
}
