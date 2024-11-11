package org.luke.gui.controls.image.layer_icon;

import java.util.ArrayList;

import org.luke.gui.controls.image.ColorIcon;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.effect.Effect;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * The {@code LayerIcon} class represents an icon composed of multiple layers,
 * each represented by a {@link ColorIcon}.
 *
 * @author SDIDSA
 */
public class LayerIcon extends StackPane {
	private final ArrayList<ColorIcon> layers;
	private final double size;

	/**
	 * Constructs a {@code LayerIcon} with the specified size and adds layers based
	 * on the given names.
	 *
	 * @param size   The size of the icon.
	 * @param layers Names of the layers to be added.
	 */
	public LayerIcon(double size, String... layers) {
		this.size = size;

		this.layers = new ArrayList<>();

		for (String layer : layers) {
			addLayer(layer);
		}
	}

	/**
	 * Adds a new layer to the icon with the specified name and default size.
	 *
	 * @param name The name of the layer.
	 */
	public void addLayer(String name) {
		addLayer(name, size);
	}

	/**
	 * Adds a new layer to the icon with the specified name and size.
	 *
	 * @param name The name of the layer.
	 * @param size The size of the layer.
	 */
	public void addLayer(String name, double size) {
		ColorIcon layer = new ColorIcon(name, size);

		setMaxSize(layer.getMaxWidth(), layer.getMaxHeight());

		layers.add(layer);
		getChildren().add(layer);
	}

	/**
	 * Adds a new layer to the icon with the specified name, load size, and display
	 * size.
	 *
	 * @param name        The name of the layer.
	 * @param loadSize    The load size of the layer.
	 * @param displaySize The display size of the layer.
	 */
	public void addLayer(String name, double loadSize, double displaySize) {
		ColorIcon layer = new ColorIcon(name, loadSize, displaySize);

		setMaxSize(layer.getMaxWidth(), layer.getMaxHeight());

		layers.add(layer);
		getChildren().add(layer);
	}

	/**
	 * Sets the alignment of a specific layer within the icon.
	 *
	 * @param layer     The index of the layer.
	 * @param alignment The desired alignment for the layer.
	 */
	public void setAlignment(int layer, Pos alignment) {
		setAlignment(layers.get(layer), alignment);
	}

	/**
	 * Sets an effect for a specific layer within the icon.
	 *
	 * @param layer  The index of the layer.
	 * @param effect The effect to be applied to the layer.
	 */
	public void setEffect(int layer, Effect effect) {
		layers.get(layer).setEffect(effect);
	}

	/**
	 * Sets the fill color for a specific layer within the icon.
	 *
	 * @param layer The index of the layer.
	 * @param fill  The fill color to be set for the layer.
	 */
	public void setFill(int layer, Color fill) {
		if (layers.size() <= layer) {
			return;
		}
		layers.get(layer).setFill(fill);
	}

	/**
	 * Sets the translation along the X-axis for a specific layer within the icon.
	 *
	 * @param layer The index of the layer.
	 * @param val   The translation value along the X-axis.
	 */
	public void setTranslateX(int layer, double val) {
		layers.get(layer).setTranslateX(val);
	}

	/**
	 * Sets the translation along the Y-axis for a specific layer within the icon.
	 *
	 * @param layer The index of the layer.
	 * @param val   The translation value along the Y-axis.
	 */
	public void setTranslateY(int layer, double val) {
		layers.get(layer).setTranslateY(val);
	}

	/**
	 * Sets the opacity for a specific layer within the icon.
	 *
	 * @param layer The index of the layer.
	 * @param value The opacity value to be set for the layer.
	 */
	public void setOpacity(int layer, double value) {
		layers.get(layer).setOpacity(value);
	}

	/**
	 * Returns the fill property of a specific layer within the icon.
	 *
	 * @param layer The index of the layer.
	 * @return The fill property of the layer.
	 */
	public ObjectProperty<Paint> getFillProperty(int layer) {
		return layers.get(layer).fillProperty();
	}

	/**
	 * Returns the translation along the X-axis property of a specific layer within
	 * the icon.
	 *
	 * @param layer The index of the layer.
	 * @return The translation along the X-axis property of the layer.
	 */
	public DoubleProperty translateXProperty(int layer) {
		return layers.get(layer).translateXProperty();
	}

	/**
	 * Returns the translation along the Y-axis property of a specific layer within
	 * the icon.
	 *
	 * @param layer The index of the layer.
	 * @return The translation along the Y-axis property of the layer.
	 */
	public DoubleProperty translateYProperty(int layer) {
		return layers.get(layer).translateYProperty();
	}

	/**
	 * Returns the opacity property of a specific layer within the icon.
	 *
	 * @param layer The index of the layer.
	 * @return The opacity property of the layer.
	 */
	public DoubleProperty opacityProperty(int layer) {
		return layers.get(layer).opacityProperty();
	}

	/**
	 * Enables caching for a specific layer before a transition.
	 *
	 * @param layer The index of the layer.
	 */
	public void preTransition(int layer) {
		layers.get(layer).setCache(true);
		layers.get(layer).setCacheHint(CacheHint.SPEED);
	}

	/**
	 * Disables caching for a specific layer after a transition.
	 *
	 * @param layer The index of the layer.
	 */
	public void postTransition(int layer) {
		layers.get(layer).setCache(false);
		layers.get(layer).setCacheHint(CacheHint.DEFAULT);
	}

	/**
	 * Sets the scale for a specific layer within the icon.
	 *
	 * @param layer The index of the layer.
	 * @param scale The scale value to be set for the layer.
	 */
	public void setScale(int layer, double scale) {
		layers.get(layer).setScaleX(scale);
		layers.get(layer).setScaleY(scale);
	}
}
