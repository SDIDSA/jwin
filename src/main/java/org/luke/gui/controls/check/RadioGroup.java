package org.luke.gui.controls.check;

import java.util.ArrayList;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class RadioGroup {
	private ArrayList<Radio> radios;

	private ObjectProperty<Radio> value;
	
	public RadioGroup(Radio... items) {
		radios = new ArrayList<>();
		
		value = new SimpleObjectProperty<>();

		for (Radio radio : items) {
			add(radio);
		}
	}

	public RadioGroup(KeyedRadio...items) {
		radios = new ArrayList<>();
		
		value = new SimpleObjectProperty<>();

		for (KeyedRadio radio : items) {
			add(radio.getCheck());
		}
	}

	public void add(Radio radio) {
		radio.checkedProperty().addListener((obs, ov, nv) -> {
			if (nv.booleanValue()) {
				value.set(radio);
				radios.forEach(e -> {
					if (e != radio) {
						e.setChecked(false);
					}
				});
			}
		});

		radios.add(radio);
	}
	
	public Radio getValue() {
		return value.get();
	}
	
	public ObjectProperty<Radio> valueProperty() {
		return value;
	}
}
