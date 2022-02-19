package org.luke.jwin.app.param;

import java.io.File;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class IconParam extends Param {

	private File value;
	
	private FileChooser fc;
	public IconParam(Stage ps) {
		super("Executable Icon");

		fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("icon", "*.ico"));
		
		addButton("select", e-> {
			select(ps);
		});
	}
	
	public void select(Stage ps) {
		File ico = fc.showOpenDialog(ps);
		if(ico != null) {
			set(ico);
		}
	}
	
	public void set(File ico) {
		list.getChildren().clear();
		value = ico;
		addFile(ico, ico.getName());
	}
	
	public File getValue() {
		return value;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

}
