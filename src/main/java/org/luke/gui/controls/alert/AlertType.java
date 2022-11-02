package org.luke.gui.controls.alert;

import java.util.Arrays;
import java.util.List;

public enum AlertType {
	INFO(ButtonType.CLOSE),
	DELETE(ButtonType.CANCEL, ButtonType.DELETE), 
	ERROR(ButtonType.CLOSE), CONFIRM(ButtonType.CANCEL, ButtonType.NO, ButtonType.YES);
	
	private List<ButtonType> buttons;
	
	private AlertType(ButtonType...buttonTypes) {
		buttons = Arrays.asList(buttonTypes);
	}
	
	public List<ButtonType> getButtons() {
		return buttons;
	}
}
