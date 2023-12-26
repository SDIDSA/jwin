package org.luke.gui.controls.alert;

import java.util.Arrays;
import java.util.List;

/**
 * The AlertType enum represents different types of alerts that can be
 * displayed. Each alert type is associated with specific button types, defining
 * the available user actions for that alert.
 *
 * Supported AlertTypes:<br>
 * - INFO: Informational alert with a close button.<br>
 * - DELETE: Alert for deletion with cancel and delete buttons.<br>
 * - ERROR: Error alert with a close button.<br>
 * - CONFIRM: Confirmation alert with cancel, no, and yes buttons.
 *
 * @author SDIDSA
 */
public enum AlertType {
	INFO(ButtonType.CLOSE), DELETE(ButtonType.CANCEL, ButtonType.DELETE), ERROR(ButtonType.CLOSE),
	CONFIRM(ButtonType.CANCEL, ButtonType.NO, ButtonType.YES);

	private List<ButtonType> buttons;

	/**
	 * Constructs an AlertType with the specified button types.
	 *
	 * @param buttonTypes The associated button types.
	 */
	private AlertType(ButtonType... buttonTypes) {
		buttons = Arrays.asList(buttonTypes);
	}

	/**
	 * Gets the list of button types associated with the alert type.
	 *
	 * @return The list of button types.
	 */
	public List<ButtonType> getButtons() {
		return buttons;
	}
}
