package org.luke.jwin.app.param;

import java.io.File;

import org.luke.gui.window.Window;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class IconParam extends Param {

	private File value;
	
	private FileChooser fc;
	public IconParam(Window ps) {
		super(ps, "Executable Icon");

		fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("icon", "*.ico"));
		
		addButton(ps, "select", () -> {
			select(ps);
		});
	}
	
	public void select(Window ps) {
		File ico = fc.showOpenDialog(ps);
		if(ico != null) {
			set(ico);
		}
	}
	
	public void set(File ico) {
		if(ico == null || !ico.exists()) {
			return;
		}
		list.getChildren().clear();
		value = ico;
		addFile(getWindow(), ico, ico.getName());
	}
	
	public File getValue() {
		return value;
	}

	@Override
	public void clear() {
		value = null;
		list.getChildren().clear();
	}

}
