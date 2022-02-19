package org.luke.jwin.app.file;

import java.io.File;

import org.json.JSONObject;

public class FileTypeAssociation {
	private static final String TYPE_NAME = "typeName";
	private static final String TYPE_EXTENSION = "typeExtension";
	private static final String TYPE_ICON = "typeIcon";

	private String typeName;
	private String typeExtension;
	private File icon;

	public FileTypeAssociation(String typeName, String typeExtension, File icon) {
		this.typeName = typeName;
		this.typeExtension = typeExtension;
		this.icon = icon;
	}

	public String getTypeName() {
		return typeName;
	}

	public String getTypeExtension() {
		return typeExtension;
	}

	public File getIcon() {
		return icon;
	}

	public void setIcon(File icon) {
		this.icon = icon;
	}

	public JSONObject serialize() {
		JSONObject res = new JSONObject();

		res.put(TYPE_NAME, typeName);
		res.put(TYPE_EXTENSION, typeExtension);

		if (icon != null)
			res.put(TYPE_ICON, icon.getAbsolutePath());

		return res;
	}

	public static FileTypeAssociation deserialize(JSONObject obj) {
		return new FileTypeAssociation(obj.getString(TYPE_NAME), obj.getString(TYPE_EXTENSION),
				obj.has(TYPE_ICON) ? new File(obj.getString(TYPE_ICON)) : null);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FileTypeAssociation other) {
			return typeName.equals(other.typeName) && typeExtension.equals(other.typeExtension)
					&& icon.equals(other.icon);
		}
		return super.equals(obj);
	}
}
