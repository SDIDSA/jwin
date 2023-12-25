package org.luke.jwin.app.layout.settings.abs.left;

import java.util.ArrayList;

import org.luke.gui.NodeUtils;
import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.MultiText;
import org.luke.gui.controls.label.TextTransform;
import org.luke.gui.style.ColorItem;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.jwin.app.layout.settings.abs.Settings;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;

public class Section extends VBox implements Styleable {

	private MultiText title;
	private VBox items;
	
	private ArrayList<SectionItem> secItems;

	public Section(Settings settings, String titleKey, boolean first) {

		items = new VBox(2);
		NodeUtils.nestedFocus(items);

		if (titleKey != null) {
			title = new MultiText(settings.getWindow(), titleKey, new Font(16, FontWeight.BOLD));
			title.setTransform(TextTransform.UPPERCASE);
			StackPane titCont = new StackPane(title);
			titCont.setAlignment(Pos.CENTER_LEFT);
			titCont.setPadding(new Insets(0, 0, 15, 10));
			titCont.setMaxWidth(208);
			titCont.setMinWidth(0);
			getChildren().add(titCont);
		}

		getChildren().add(items);
		secItems = new ArrayList<>();

		applyStyle(settings.getWindow().getStyl());
	}

	public void addPreTitle(ColorItem node) {
		title.addNode(0, node);
	}

	public Section(Settings settings) {
		this(settings, null);
	}

	public void addItem(SectionItem item) {
		items.getChildren().add(item);
		secItems.add(item);
	}
	
	public boolean fire(String match) {
		for(SectionItem item : secItems) {
			String k = item.getKey().toLowerCase().trim();
			String m = match.toLowerCase().trim();
			
			if(m.contains(k) || k.contains(m)) {
				item.fire();
				return true;
			}
		}
		
		return false;
	}

	public Section(Settings settings, String titleKey) {
		this(settings, titleKey, false);
	}

	@Override
	public void applyStyle(Style style) {
		if (title != null)
			title.setFill(style.getChannelsDefault());
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

}
