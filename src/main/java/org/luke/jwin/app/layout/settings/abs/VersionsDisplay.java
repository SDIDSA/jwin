package org.luke.jwin.app.layout.settings.abs;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.controls.scroll.VerticalScrollable;
import org.luke.gui.controls.space.ExpandingHSpace;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.factory.Borders;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class VersionsDisplay extends VBox implements Styleable {

	private VerticalScrollable preVDisp;
	private VBox vDisp;

	private Label empty;
	
	private Label titLab;

	public VersionsDisplay(Window win, String title, Node...nodes) {
		super(20);
		
		vDisp = new VBox(15);
		vDisp.setPrefHeight(0);
		vDisp.setPadding(new Insets(15));

		empty = new Label(win, "nothing_to_show_here", new Font(14));

		preVDisp = new VerticalScrollable();
		preVDisp.setContent(vDisp);

		preVDisp.setMinHeight(250);
		preVDisp.setMaxHeight(250);
		
		titLab = new Label(win, title, new Font(16));
		HBox top = new HBox(15, titLab, new ExpandingHSpace());
		top.setAlignment(Pos.CENTER);
		top.getChildren().addAll(nodes);
		
		getChildren().addAll(top, preVDisp);
		
		HBox.setHgrow(this, Priority.ALWAYS);

		applyStyle(win.getStyl());
	}

	public void clear() {
		vDisp.getChildren().clear();
		vDisp.setAlignment(Pos.TOP_CENTER);
	}

	public void addLine(Node line) {
		vDisp.getChildren().add(line);
	}

	public void empty() {
		if (vDisp.getChildren().isEmpty()) {
			vDisp.setAlignment(Pos.CENTER);
			vDisp.getChildren().add(empty);
		}
	}

	@Override
	public void applyStyle(Style style) {
		preVDisp.setBackground(Backgrounds.make(style.getBackgroundSecondary(), 15));
		preVDisp.setBorder(Borders.make(style.getBackgroundModifierActive(), Borders.OUTSIDE, 15));
		
		empty.setFill(style.getHeaderSecondary());
		titLab.setFill(style.getHeaderPrimary());
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

}
