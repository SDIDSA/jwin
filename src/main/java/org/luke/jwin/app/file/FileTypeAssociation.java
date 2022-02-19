package org.luke.jwin.app.file;

import java.io.File;

public class FileTypeAssociation {
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
}
