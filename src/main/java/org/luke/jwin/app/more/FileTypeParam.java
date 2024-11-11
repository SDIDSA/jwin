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
	private final FileChooser fc;

	private final TextVal typeName;
	private final TextVal typeExtension;
	private final TextVal iconPath;
	private File icon;

	private final KeyedCheck enable;

	public FileTypeParam(Window ps) {
		super(10);

		fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("icon", "*.ico"));

		typeName = new TextVal(ps, "type_name");
		typeName.setKeyedPrompt("my_special_extention");
		typeExtension = new TextVal(ps, "type_extension");
		typeExtension.setPrompt(".ext");

		iconPath = new TextVal(ps, "type_icon");
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

		line1.disableProperty().bind(enable.property().not());
		iconPath.disableProperty().bind(enable.property().not());
		iconPath.setEditable(false);
		iconPath.addToBottom(selectIcon);
		
		getChildren().addAll(line1, iconPath, enable);
	}
	
	public boolean isEnabled() {
		return enable.get();
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
			enable.property().set(true);
		}else {
			typeName.setValue("");
			typeExtension.setValue("");
			icon = null;
			iconPath.setValue("");
			enable.property().set(false);
		}
	}
}
