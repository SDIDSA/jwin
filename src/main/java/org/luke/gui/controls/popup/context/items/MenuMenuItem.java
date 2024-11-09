package org.luke.gui.controls.popup.context.items;

import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.controls.popup.context.ContextMenu;

/**
 * Custom implementation of a menu item with a submenu, extending {@link KeyedMenuItem}.
 * Represents a menu item associated with a key and contains a submenu.
 * Manages the appearance, behavior, and interaction of the menu item with a submenu.
 *
 * @author SDIDSA
 */
public class MenuMenuItem extends KeyedMenuItem {

    /**
     * The submenu associated with this menu item.
     */
    private final ContextMenu subMenu;

    /**
     * Constructs a menu item with a submenu using the specified parent context menu and key.
     *
     * @param menu The parent {@link ContextMenu} to which this menu item belongs.
     * @param key  The key or text associated with the menu item.
     */
    public MenuMenuItem(ContextMenu menu, String key) {
        super(menu, key);

        ColorIcon arrow = new ColorIcon("menu-right", 12, 8);
        arrow.fillProperty().bind(lab.fillProperty());

        setSpacing(5);
        getChildren().add(arrow);

        subMenu = new ContextMenu(menu.getOwner());
        active.addListener((_, _, nv) -> {
            if (nv) {
                subMenu.showPop(this, Direction.RIGHT_DOWN, 15);
            } else {
                subMenu.hide();
            }
        });

        applyStyle(menu.getOwner().getStyl());
    }

    /**
     * Clears the items in the submenu.
     */
    public void clear() {
        subMenu.clear();
    }

    /**
     * Adds a menu item to the submenu.
     *
     * @param i The menu item to add.
     */
    public void addMenuItem(MenuItem i) {
        subMenu.addMenuItem(i);
    }

    /**
     * Adds a menu item with the specified key to the submenu.
     *
     * @param key The key or text associated with the menu item.
     */
    public void addMenuItem(String key) {
        subMenu.addMenuItem(key);
    }

    /**
     * Adds a menu item with the specified key and action to the submenu.
     *
     * @param key     The key or text associated with the menu item.
     * @param action  The action to be executed when the menu item is selected.
     */
    public void addMenuItem(String key, Runnable action) {
        subMenu.addMenuItem(key, action);
    }

    /**
     * Gets the submenu associated with this menu item.
     *
     * @return The submenu associated with this menu item.
     */
    public ContextMenu getSubMenu() {
        return subMenu;
    }
}
