package org.luke.gui.style;

import java.lang.ref.WeakReference;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;

/**
 * interface that represents a Styleable Node and offers methods for
 * weak-binding a Styleable node to a Style Observable.<br>
 * <br>
 * 
 * Classes implementing this interface have to define the
 * {@code applyStyle(Style)} method and will be affected by
 * {@link org.luke.gui.window.Window#setStyle(Style) Window.setStyle(Style)}
 * calls in the Window class.
 * 
 * @author SDIDSA
 */
public interface Styleable {

	/**
	 * Applies the passed Style on this Node. The behavior of this method is defined
	 * by subclasses.
	 * <p>
	 * Note: Do not manually call this method; use
	 * {@link org.luke.gui.window.Window#setStyle(Style) Window.setStyle(Style)} to
	 * apply a Style on the whole scene graph.
	 * 
	 * @param style - the Style to be applied on this Node
	 */
	void applyStyle(Style style);

	/**
	 * Applies the Style from the provided ObjectProperty on this Node.
	 * 
	 * @param style - the ObjectProperty holding the Style to be applied on this
	 *              Node
	 */
	void applyStyle(ObjectProperty<Style> style);

	/**
	 * Converts a Color to a CSS representation.
	 * 
	 * @param color - the Color to be converted
	 * @return a CSS representation of the Color
	 */
	static String colorToCss(Color color) {
		return "rgb(" + (int) (color.getRed() * 255) + "," + (int) (color.getGreen() * 255) + ","
				+ (int) (color.getBlue() * 255) + ", " + color.getOpacity() + ")";
	}

	/**
	 * Binds the Style of the provided Styleable node to the given ObjectProperty.
	 * 
	 * @param node  - the Styleable node to bind the Style
	 * @param style - the ObjectProperty holding the Style
	 */
	static void bindStyle(Styleable node, ObjectProperty<Style> style) {
		bindStyleWeak(node, style);
	}

	/**
	 * Binds the Style weakly to the provided Styleable node and ObjectProperty.
	 * 
	 * @param node  - the Styleable node to bind the Style
	 * @param style - the ObjectProperty holding the Style
	 */
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
