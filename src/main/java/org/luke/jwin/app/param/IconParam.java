package org.luke.jwin.app.param;

import java.io.File;
import java.util.function.Consumer;

import org.luke.gui.window.Window;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class IconParam extends Param {

	private File value;
	
	private Consumer<File> onSet;
	
	private FileChooser fc;
	public IconParam(Window ps) {
		super(ps, "executable_icon");

		fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("icon", "*.ico"));
		
		addButton(ps, "select", () -> {
			select();
		});
	}
	
	public void select() {
		File ico = fc.showOpenDialog(getWindow());
		if(ico != null) {
			set(ico);
		}
	}
	
	public void setOnSet(Consumer<File> onSet) {
		this.onSet = onSet;
	}
	
	public void set(File ico) {
		if(ico == null || !ico.exists()) {
			return;
		}
		list.getChildren().clear();
		value = ico;
		if(onSet != null) onSet.accept(ico);
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
