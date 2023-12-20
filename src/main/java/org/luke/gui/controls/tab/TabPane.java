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
		titles.getChildren().clear();
		new TabTitle(window, tab);
		
		for(int i = 0; i < tabs.size(); i++) {
			Tab t = tabs.get(i);
			if(i != 0) {
				titles.getChildren().add(new Separator(window, Orientation.VERTICAL));
			}
			
			titles.getChildren().add(t.getTitleDisp());
		}
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
		setBorder(Border.EMPTY);
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
