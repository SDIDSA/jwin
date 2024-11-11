package org.luke.jwin.app.more;

import javafx.beans.binding.Bindings;
import org.luke.gui.controls.check.KeyedCheck;
import org.luke.gui.window.Window;
import org.luke.jwin.app.file.UrlProtocolAssociation;
import org.luke.jwin.ui.TextVal;

import javafx.scene.layout.VBox;

public class UrlProtocolParam extends VBox {
	private final TextVal protocol;

	private final KeyedCheck enable;

	public UrlProtocolParam(Window window) {
		super(10);

		protocol = new TextVal(window, "url_protocol");
		enable = new KeyedCheck(window, "enable", 16);

		protocol.disableProperty().bind(
				Bindings.createObjectBinding(() -> !enable.get(),
					enable.property()));

		getChildren().addAll(protocol, enable);
	}

	public boolean isEnabled() {
		return enable.get();
	}

	public UrlProtocolAssociation getValue() {
		return isEnabled() ? new UrlProtocolAssociation(protocol.getValue()) : null;
	}

	public void set(UrlProtocolAssociation urlProtocol) {
		if (urlProtocol != null) {
			protocol.setValue(urlProtocol.getProtocol());
			enable.property().set(true);
		} else {
			protocol.setValue("");
			enable.property().set(false);
		}
	}
}
