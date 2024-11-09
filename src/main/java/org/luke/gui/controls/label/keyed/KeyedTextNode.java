package org.luke.gui.controls.label.keyed;

import org.luke.gui.controls.label.unkeyed.TextNode;

/**
 * The {@code KeyedTextNode} interface represents a text node with an associated key,
 * commonly used for localization purposes. It extends the {@link TextNode} interface
 * and provides a method to set the key.
 * 
 * @author SDIDSA
 */
public interface KeyedTextNode extends TextNode {

    /**
     * Sets the key associated with this text node for localization.
     * 
     * @param key The localization key to set.
     */
    void setKey(String key);
}
