package org.luke.jwin.app.param.deps;

import java.util.ArrayList;

import org.luke.gui.factory.Borders;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.BorderPane;

public class TabPane extends BorderPane implements Styleable {
	private Window window;

	private TabTitles titles;
	private ArrayList<Tab> tabs;

	public TabPane(Window window) {
		this.window = window;
		tabs = new ArrayList<>();

		titles = new TabTitles(window);
		setTop(titles);
		
		applyStyle(window.getStyl());
	}

	public void addTab(Tab tab) {
		tab.setOwner(this);
		tabs.add(tab);
		titles.getChildren().add(new TabTitle(window, tab));
	}

	private Tab selected = null;

	public void select(Tab select) {
		if (!tabs.contains(select)) {
			throw new IllegalArgumentException("the Tab doesn't belong to this TabPane");
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
		setBorder(Borders.make(style.getBackgroundFloating(), 5.0, 2.0));
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
