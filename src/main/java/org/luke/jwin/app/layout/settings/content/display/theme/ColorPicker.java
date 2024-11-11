package org.luke.jwin.app.layout.settings.content.display.theme;

import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class ColorPicker extends HBox {
	private final ObjectProperty<Color> value;

	private final SBPicker sbPicker;
	private final HuePicker huePicker;

	public ColorPicker(Window window, int size) {
		super(15);

		value = new SimpleObjectProperty<Color>();
		value.set(Color.WHITE);

		setMinHeight(size);
		setMaxHeight(size);

		sbPicker = new SBPicker(window, size);

		huePicker = new HuePicker(window, size);

		huePicker.hueProperty().addListener((obs, ov, nv) -> {
			sbPicker.update(nv.doubleValue());
		});

		sbPicker.update(huePicker.getHue());

		Runnable updatePreview = () -> {
			Color nc = Color.hsb(huePicker.getHue(), sbPicker.getSaturation(), sbPicker.getBrightness());
			Color old = value.get();
			int oldH = (int) old.getHue();
			int oldS = (int) (old.getSaturation() * 100);
			int oldB = (int) (old.getBrightness() * 100);
			
			int h = (int) nc.getHue();
			int s = (int) (nc.getSaturation() * 100);
			int b = (int) (nc.getBrightness() * 100);
			
			if(oldH == h && oldS == s && oldB == b) {
				return;
			}
			value.set(nc);
		};

		sbPicker.onChange(updatePreview);
		huePicker.onChange(updatePreview);

		value.addListener((_, ov, nv) -> {
			if (nv.equals(ov))
				return;

			double hue = nv.getHue();
			double sat = nv.getSaturation();
			double bri = nv.getBrightness();

			if (hue != huePicker.getHue() && sat != 0)
				huePicker.hueProperty().set(hue);
			if (sat != sbPicker.getSaturation())
				sbPicker.saturationProperty().set(sat);
			if (bri != sbPicker.getBrightness())
				sbPicker.brightnessProperty().set(bri);
		});

		updatePreview.run();

		getChildren().addAll(sbPicker, huePicker);
	}

	public HuePicker getHuePicker() {
		return huePicker;
	}

	public SBPicker getSbPicker() {
		return sbPicker;
	}

	public ObjectProperty<Color> valueProperty() {
		return value;
	}

	public Color getValue() {
		return value.get();
	}
}
