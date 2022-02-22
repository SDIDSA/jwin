package org.luke.jwin.app.more;

import org.luke.jwin.app.Jwin.TextVal;
import org.luke.jwin.app.file.UrlProtocolAssociation;
import org.luke.jwin.ui.CheckBox;

import javafx.scene.layout.VBox;

public class UrlProtocolParam extends VBox {
	private TextVal protocol;

	private CheckBox enable;

	public UrlProtocolParam() {
		super(10);

		protocol = new TextVal("URL Protocol");

		enable = new CheckBox("enable");

		protocol.disableProperty().bind(enable.selectedProperty().not());
		
		getChildren().addAll(protocol, enable);
	}
	
	public boolean isEnabled() {
		return enable.isSelected();
	}
	
	public UrlProtocolAssociation getValue() {
		return isEnabled() ? new UrlProtocolAssociation(protocol.getValue()) : null;
	}
	
	public void set(UrlProtocolAssociation urlProtocol) {
		if(urlProtocol != null) {
			protocol.setValue(urlProtocol.getProtocol());
			enable.setSelected(true);
		}
	}
}
