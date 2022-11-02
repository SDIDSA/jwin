package org.luke.gui.controls.popup.context.items;

import org.luke.gui.controls.check.Radio;
import org.luke.gui.controls.popup.context.ContextMenu;

import javafx.beans.property.BooleanProperty;
import javafx.scene.paint.Color;

public class RadioMenuItem extends KeyedMenuItem {

	private Radio check;

	public RadioMenuItem(ContextMenu menu, String key, Color fill) {
		super(menu, key, fill);

		check = new Radio(menu.getOwner(), 14);

		check.invertedProperty().bind(active);

		check.setMouseTransparent(true);
		
		getChildren().add(check);

		setAction(() -> check.flip());

		setHideOnAction(false);
	}
	
	public Radio getCheck() {
		return check;
	}
	
	public void setChecked(boolean checked) {
		check.setChecked(checked);
	}

	public BooleanProperty checkedProperty() {
		return check.checkedProperty();
	}

}
