package org.luke.gui.window.content;

import java.awt.Dimension;

import org.luke.gui.window.Page;
import org.luke.gui.window.Window;
import org.luke.gui.window.content.app_bar.AppBar;
import org.luke.gui.window.content.app_bar.AppBarButton;
import org.luke.gui.window.helpers.TileHint.Tile;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;

public class AppPreRoot extends StackPane {
	public static final double DEFAULT_PADDING = 15;

	private BooleanProperty padded;
	private DoubleProperty padding;
	
	private AppRoot root;
	
	public AppPreRoot(Window window) {
		setBackground(Background.EMPTY);
		padded = new SimpleBooleanProperty(true);
		padding = new SimpleDoubleProperty(DEFAULT_PADDING);
		
		padding.bind(Bindings.when(padded).then(DEFAULT_PADDING).otherwise(0));
		
		paddingProperty().bind(Bindings.createObjectBinding(()-> new Insets(padding.get()), padding));
		
		root = new AppRoot(window, this);
		getChildren().setAll(root);
	}
	
	public void addBarButton(AppBarButton button) {
		addBarButton(0, button);
	}
	
	public void addBarButton(int index, AppBarButton button) {
		root.addBarButton(index, button);
	}

	public void setOnInfo(Runnable runnable) {
		root.setOnInfo(runnable);
	}
	
	public AppBarButton getInfo() {
		return root.getInfo();
	} 
	
	public DoubleProperty paddingProp() {
		return padding;
	}
	
	public boolean isPadded() {
		return padded.get();
	}
	
	public void setPadded(boolean padded) {
		this.padded.set(padded);
	}
	
	public BooleanProperty paddedProperty() {
		return padded;
	}
	
	public void setFill(Paint fill) {
		root.setFill(fill);
	}
	
	public void setBorder(Paint fill, double width) {
		root.setBorderFill(fill, width);
	}
	
	public void setContent(Page page) {
		root.setContent(page);
	}
	
	public void applyTile(Tile tile) {
		root.applyTile(tile);
	}

	public void unTile() {
		root.unTile();
	}
	
	public boolean isTiled() {
		return root.isTiled();
	}

	public void setMinSize(Dimension d) {
		root.setMinSize(d);
	}

	public AppBar getAppBar() {
		return root.getAppBar();
	}

	public void setIcon(String image) {
		root.setIcon(image);
	}
}
