package org.luke.gui.locale;

import java.lang.ref.WeakReference;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Represents a Localized Node. <br>
 * <br>
 * Classes implementing this interface have to define the
 * {@link Localized#applyLocale(Locale) applyLocale} method and will be affected
 * by {@link mesa.gui.window.Window#setLocale(Locale) Window.setLocale(Locale)}
 * calls.
 * 
 * @author Lukas Owen
 * 
 */
public interface Localized {

	/**
	 * Applies the passed {@link Locale} on this Node, The behavior of this method
	 * is defined by subclasses. <br>
	 * <br>
	 * <b>Note :</b> do not manually call this, use {@link mesa.gui.window.Window
	 * Window}<span style= "color:
	 * #0066cc;">.</span>{@link mesa.gui.window.Window#setLocale(Locale)
	 * setLocale(Locale)} in to apply a {@link Locale} on the whole scene graph.
	 *
	 * @param locale - the {@link Locale} to be applied on this Node
	 */
	public void applyLocale(Locale locale);

	public void applyLocale(ObjectProperty<Locale> locale);

	public static void bindLocale(Localized node, ObjectProperty<Locale> locale) {
		bindLocaleWeak(node, locale);
	}

	private static void bindLocaleWeak(Localized node, ObjectProperty<Locale> locale) {
		node.applyLocale(locale.get());

		WeakReference<Localized> weakNode = new WeakReference<>(node);

		ChangeListener<Locale> listener = new ChangeListener<Locale>() {
			@Override
			public void changed(ObservableValue<? extends Locale> obs, Locale ov, Locale nv) {
				if (weakNode.get() != null) {
					if (nv != ov) {
						weakNode.get().applyLocale(nv);
					}
				} else {
					locale.removeListener(this);
				}
			}
		};

		locale.addListener(listener);
	}
}
