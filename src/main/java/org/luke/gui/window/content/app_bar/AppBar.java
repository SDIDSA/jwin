package org.luke.gui.window.content.app_bar;

import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.controls.space.ExpandingHSpace;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;
import org.luke.gui.window.helpers.MoveResizeHelper;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

public class AppBar extends HBox implements Styleable {
	private AppBarButton info;
	private ColorIcon icon;

	private HBox buttons;
	
	public AppBar(Window window, MoveResizeHelper helper) {
		setPadding(new Insets(0, 15, 0, 15));
		setMinHeight(40);
		setAlignment(Pos.CENTER);

		icon = new ColorIcon(null, 20);
		icon.setMouseTransparent(true);

		buttons = new HBox(4);
		buttons.setAlignment(Pos.CENTER);

		AppBarButton minimize = new AppBarButton(window, "minimize");
		minimize.setAction(() -> window.setIconified(true));

		AppBarButton maxRest = new AppBarButton(window, "maximize");
		maxRest.setAction(window::maxRestore);

		AppBarButton exit = new AppBarButton(window, "close");
		exit.setAction(window::close);

		info = new AppBarButton(window, "info");
		HBox.setMargin(info, new Insets(0, 8, 0, 0));
		
		buttons.getChildren().addAll(info, minimize, maxRest, exit);

		getChildren().addAll(icon, new ExpandingHSpace(), buttons);

		helper.addOnTile(() -> maxRest.setIcon("restore"));

		helper.addOnUnTile(() -> maxRest.setIcon("maximize"));
		
		applyStyle(window.getStyl());
	}

	public void setOnInfo(Runnable action) {
		info.setAction(action);
	}
	
	public void addButton(int index, AppBarButton button) {
		HBox.setMargin(button, new Insets(0, 8, 0, 0));
		buttons.getChildren().add(index, button);
	}
	
	public void addButton(AppBarButton button) {
		addButton(0, button);
	}
	
	public AppBarButton getInfo() {
		return info;
	}
	
	@Override
	public void applyStyle(Style style) {
		icon.setFill(style.getAccent());
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

	public void setIcon(String image) {
		icon.setImage(image, 20);
	}
}
