
package org.luke.gui.locale;

import java.lang.ref.WeakReference;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * interface that represents a Localized Node and offers methods for
 * weak-binding a localized node to a Locale Observable.<br>
 * <br>
 *
 * Classes implementing this interface have to define the
 * {@code applyLocale(Locale)} method and will be affected by
 * {@link org.luke.gui.window.Window#setLocale(Locale) Window.setLocale(Locale)}
 * calls in the Window class.
 *
 * @author SDIDSA
 */
public interface Localized {

	/**
	 * Applies the passed Locale on this Node. The behavior of this method is
	 * defined by subclasses.
	 * <p>
	 * Note: Do not manually call this method; use
	 * {@link org.luke.gui.window.Window#setLocale(Locale) Window.setLocale(Locale)}
	 * to apply a Locale on the whole scene graph.
	 *
	 * @param locale - the Locale to be applied on this Node
	 */
	void applyLocale(Locale locale);

	/**
	 * Applies the Locale from the provided ObjectProperty on this Node.
	 *
	 * @param locale - the ObjectProperty holding the Locale to be applied on this
	 *               Node
	 */
	void applyLocale(ObjectProperty<Locale> locale);

	/**
	 * Binds the Locale of the provided Localized node to the given ObjectProperty.
	 *
	 * @param node   - the Localized node to bind the Locale
	 * 
	 * @param locale - the ObjectProperty holding the Locale
	 */
	static void bindLocale(Localized node, ObjectProperty<Locale> locale) {
		bindLocaleWeak(node, locale);
	}

	/**
	 * Binds the Locale weakly to the provided Localized node and ObjectProperty.
	 *
	 * @param node   - the Localized node to bind the Locale
	 * 
	 * @param locale - the ObjectProperty holding the Locale
	 */
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
