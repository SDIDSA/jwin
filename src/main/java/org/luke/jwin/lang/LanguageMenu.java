package org.luke.jwin.lang;

import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.locale.Locale;
import org.luke.gui.window.Window;

import java.util.ArrayList;
import java.util.List;

public class LanguageMenu extends ContextMenu {
    public LanguageMenu(Window window) {
        super(window);

        List<Locale> locales = new ArrayList<>();
        locales.add(Locale.AR_DZ);
        locales.add(Locale.EN_US);
        locales.add(Locale.FR_FR);

        locales.forEach(locale -> addMenuItem(new LanguageItem(this, locale)));
    }
}
