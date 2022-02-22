package org.luke.jwin.app.more;

import java.io.File;

import org.luke.jwin.app.Jwin.TextVal;
import org.luke.jwin.app.file.FileTypeAssociation;
import org.luke.jwin.ui.Button;
import org.luke.jwin.ui.CheckBox;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class FileTypeParam extends VBox {
	private FileChooser fc;

	private TextVal typeName;
	private TextVal typeExtension;
	private TextVal iconPath;
	private File icon;

	private CheckBox enable;

	public FileTypeParam(Stage ps) {
		super(10);

		fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("icon", "*.ico"));

		typeName = new TextVal("Type name");
		typeExtension = new TextVal("Type extension");

		iconPath = new TextVal("Type icon");
		Button selectIcon = new Button("select");
		selectIcon.setMinWidth(100);
		selectIcon.setOnAction(e -> {
			((Stage) getScene().getWindow()).setAlwaysOnTop(false);
			select(ps);
			((Stage) getScene().getWindow()).setAlwaysOnTop(true);
		});

		HBox line1 = new HBox(10, typeName, typeExtension);

		enable = new CheckBox("enable");

		line1.disableProperty().bind(enable.selectedProperty().not());
		iconPath.disableProperty().bind(enable.selectedProperty().not());

		iconPath.addToBottom(selectIcon);
		
		getChildren().addAll(line1, iconPath, enable);
	}
	
	public boolean isEnabled() {
		return enable.isSelected();
	}
	
	public FileTypeAssociation getValue() {
		return isEnabled() ? new FileTypeAssociation(typeName.getValue(), typeExtension.getValue(), icon) : null;
	}

	private void select(Stage ps) {
		File ico = fc.showOpenDialog(ps);
		if (ico != null) {
			set(ico);
		}
	}

	private void set(File ico) {
		icon = ico;
		iconPath.setValue(icon.getName());
	}

	public File getIcon() {
		return icon;
	}
	
	public void set(FileTypeAssociation fileTypeAsso) {
		if(fileTypeAsso != null) {
			typeName.setValue(fileTypeAsso.getTypeName());
			typeExtension.setValue(fileTypeAsso.getTypeExtension());
			set(fileTypeAsso.getIcon());
			enable.setSelected(true);
		}
	}
}
