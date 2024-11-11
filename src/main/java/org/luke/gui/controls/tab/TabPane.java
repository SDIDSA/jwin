package org.luke.gui.controls.tab;

import java.util.ArrayList;

import org.luke.gui.controls.space.Separator;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;

/**
 * extends the BorderPane class and represents a container for managing tabs.
 * It provides methods for adding tabs, selecting a tab, and applying styles.
 * <p>
 * Implements the Styleable interface to enable styling based on the current style settings.
 * </p>
 *
 * @author SDIDSA
 */
public class TabPane extends BorderPane implements Styleable {
    private final Window window;
    private final TabTitles titles;
    private final ArrayList<Tab> tabs;

    /**
     * Constructs a TabPane with the specified window.
     *
     * @param window The window associated with the TabPane.
     */
    public TabPane(Window window) {
        this.window = window;
        tabs = new ArrayList<>();
        titles = new TabTitles(window);
        setTop(titles);
        applyStyle(window.getStyl());
    }

    /**
     * Adds a tab to the TabPane.
     *
     * @param tab The tab to be added.
     */
    public void addTab(Tab tab) {
        tab.setOwner(this);
        tabs.add(tab);
        titles.getChildren().clear();
        new TabTitle(window, tab);

        for (int i = 0; i < tabs.size(); i++) {
            Tab t = tabs.get(i);
            if (i != 0) {
                titles.getChildren().add(new Separator(window, Orientation.VERTICAL));
            }

            titles.getChildren().add(t.getTitleDisp());
        }
    }

    private Tab selected = null;

    /**
     * Selects a tab in the TabPane.
     *
     * @param select The tab to be selected.
     * @throws IllegalArgumentException if the tab doesn't belong to this TabPane.
     */
    public void select(Tab select) {
        if (!tabs.contains(select)) {
            throw new IllegalArgumentException("The Tab doesn't belong to this TabPane");
        }
        if (selected != null) {
            selected.unselect();
        }
        setCenter(select.getContent());
        select.select();
        selected = select;
    }

    @Override
    public void applyStyle(Style style) {
        setBorder(Border.EMPTY);
    }

    @Override
    public void applyStyle(ObjectProperty<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
