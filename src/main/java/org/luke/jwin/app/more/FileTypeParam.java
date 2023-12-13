package org.luke.jwin.app.more;

import java.io.File;

import org.luke.gui.controls.check.KeyedCheck;
import org.luke.gui.window.Window;
import org.luke.jwin.app.file.FileTypeAssociation;
import org.luke.jwin.ui.Button;
import org.luke.jwin.ui.TextVal;

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

	private KeyedCheck enable;

	public FileTypeParam(Window ps) {
		super(10);

		fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("icon", "*.ico"));

		typeName = new TextVal(ps, "Type name");
		typeName.setPrompt("My Special Extension");
		typeExtension = new TextVal(ps, "Type extension");
		typeExtension.setPrompt(".ext");

		iconPath = new TextVal(ps, "Type icon");
		iconPath.setPrompt("type_icon.ico");
		Button selectIcon = new Button(ps, "select", 20, 40);
		selectIcon.setMinWidth(100);
		selectIcon.setAction(() -> {
			((Stage) getScene().getWindow()).setAlwaysOnTop(false);
			select(ps);
			((Stage) getScene().getWindow()).setAlwaysOnTop(true);
		});

		HBox line1 = new HBox(10, typeName, typeExtension);

		enable = new KeyedCheck(ps, "enable", 16);

		line1.disableProperty().bind(enable.checkedProperty().not());
		iconPath.disableProperty().bind(enable.checkedProperty().not());
		iconPath.setEditable(false);
		iconPath.addToBottom(selectIcon);
		
		getChildren().addAll(line1, iconPath, enable);
	}
	
	public boolean isEnabled() {
		return enable.checkedProperty().get();
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
			enable.checkedProperty().set(true);
		}else {
			typeName.setValue("");
			typeExtension.setValue("");
			icon = null;
			iconPath.setValue("");
			enable.checkedProperty().set(false);
		}
	}
}
