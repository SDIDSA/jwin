package org.luke.jwin.app.more;

import java.io.File;

import org.luke.jwin.app.Jwin.TextVal;
import org.luke.jwin.app.file.FileTypeAssociation;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
		selectIcon.setOnAction(e -> {
			((Stage) getScene().getWindow()).setAlwaysOnTop(false);
			select(ps);
			((Stage) getScene().getWindow()).setAlwaysOnTop(true);
		});

		HBox line1 = new HBox(10, typeName, typeExtension);
		HBox line2 = new HBox(10, iconPath, selectIcon);
		line2.setAlignment(Pos.BOTTOM_CENTER);

		enable = new CheckBox("enable");

		line1.disableProperty().bind(enable.selectedProperty().not());
		line2.disableProperty().bind(enable.selectedProperty().not());

		getChildren().addAll(line1, line2, enable);
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
}
