package org.luke.jwin.app.file;

import org.json.JSONObject;

public class UrlProtocolAssociation {
	private static final String PROTOCOL = "protocol";

	private final String protocol;

	public UrlProtocolAssociation(String protocol) {
		this.protocol = protocol;
	}

	public String getProtocol() {
		return protocol;
	}

	public JSONObject serialize() {
		JSONObject res = new JSONObject();

		res.put(PROTOCOL, protocol);

		return res;
	}

	public static UrlProtocolAssociation deserialize(JSONObject obj) {
		return new UrlProtocolAssociation(obj.getString(PROTOCOL));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UrlProtocolAssociation other) {
			return protocol.equals(other.protocol);
		}
		return super.equals(obj);
	}
}
