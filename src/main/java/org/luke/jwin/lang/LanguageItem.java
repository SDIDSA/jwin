package org.luke.jwin.lang;

import javafx.scene.image.ImageView;
import org.luke.gui.controls.image.ImageProxy;
import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.KeyedMenuItem;
import org.luke.gui.locale.Locale;
import org.luke.jwin.local.LocalStore;

public class LanguageItem extends KeyedMenuItem {
    public LanguageItem(ContextMenu menu, Locale locale) {
        super(menu, locale.getName().toLowerCase());
        setSpacing(10);
        getChildren().addFirst(new ImageView(
                ImageProxy.loadResize(locale.getName().toLowerCase(), 24, 16)));
        setAction(() -> {
            menu.getOwner().setLocale(locale);
            LocalStore.setLanguage(locale);
        });
    }
}
