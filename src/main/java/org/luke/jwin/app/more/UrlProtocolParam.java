package org.luke.jwin.app.more;

import org.luke.gui.controls.check.KeyedCheck;
import org.luke.gui.window.Window;
import org.luke.jwin.app.file.UrlProtocolAssociation;
import org.luke.jwin.ui.TextVal;

import javafx.scene.layout.VBox;

public class UrlProtocolParam extends VBox {
	private TextVal protocol;

	private KeyedCheck enable;

	public UrlProtocolParam(Window window) {
		super(10);

		protocol = new TextVal(window, "URL Protocol");
		enable = new KeyedCheck(window, "enable", 16);

		protocol.disableProperty().bind(enable.checkedProperty().not());

		getChildren().addAll(protocol, enable);
	}

	public boolean isEnabled() {
		return enable.checkedProperty().get();
	}

	public UrlProtocolAssociation getValue() {
		return isEnabled() ? new UrlProtocolAssociation(protocol.getValue()) : null;
	}

	public void set(UrlProtocolAssociation urlProtocol) {
		if (urlProtocol != null) {
			protocol.setValue(urlProtocol.getProtocol());
			enable.checkedProperty().set(true);
		} else {
			protocol.setValue("");
			enable.checkedProperty().set(false);
		}
	}
}
