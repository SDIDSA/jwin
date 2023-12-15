package org.luke.gui.controls.image.layer_icon;

import java.util.ArrayList;

import org.luke.gui.controls.image.ColorIcon;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class LayerIcon extends StackPane {
	private ArrayList<ColorIcon> layers;
	private double size;
	
	public LayerIcon(double size, String...layers) {
		this.size = size;
		
		this.layers = new ArrayList<>();
		
		for(String layer:layers) {
			addLayer(layer);
		}
	}
	
	public void addLayer(String name) {
		addLayer(name, size);
	}
	
	public void addLayer(String name, double size) {
		ColorIcon layer = new ColorIcon(name, size);
		
		setMaxSize(layer.getMaxWidth(), layer.getMaxHeight());
		
		layers.add(layer);
		getChildren().add(layer);
	}
	
	public void setAlignment(int layer, Pos alignment) {
		setAlignment(layers.get(layer), alignment);
	}
	
	public void setFill(int layer, Color fill) {
		if(layers.size() <= layer) {
			return;
		}
		layers.get(layer).setFill(fill);
	}
	
	public void setTranslateX(int layer, double val) {
		layers.get(layer).setTranslateX(val);
	}
	
	public void setTranslateY(int layer, double val) {
		layers.get(layer).setTranslateY(val);
	}
	
	public void setOpacity(int layer, double value) {
		layers.get(layer).setOpacity(value);
	}
	
	public ObjectProperty<Paint> getFillProperty(int layer) {
		return layers.get(layer).fillProperty();
	}
	
	public DoubleProperty translateXProperty(int layer) {
		return layers.get(layer).translateXProperty();
	}
	
	public DoubleProperty translateYProperty(int layer) {
		return layers.get(layer).translateYProperty();
	}
	
	public DoubleProperty opacityProperty(int layer) {
		return layers.get(layer).opacityProperty();
	}
	
	public void preTransition(int layer) {
		layers.get(layer).setCache(true);
		layers.get(layer).setCacheHint(CacheHint.SPEED);
	}
	
	public void postTransition(int layer) {
		layers.get(layer).setCache(false);
		layers.get(layer).setCacheHint(CacheHint.DEFAULT);
	}
	
	public void setScale(int layer, double scale) {
		layers.get(layer).setScaleX(scale);
		layers.get(layer).setScaleY(scale);
	}
}
