package org.luke.jwin.app.param;

import java.io.File;
import java.util.function.Consumer;

import org.luke.gui.window.Window;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.luke.jwin.icons.IconUtils;

public class IconParam extends Param {

	private File value;
	
	private Consumer<File> onSet;
	private Runnable onUnSet;
	
	private final FileChooser fc;
	public IconParam(Window ps) {
		super(ps, "executable_icon");

		fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("icon/image file", "*.ico", "*.png"));
		
		addButton(ps, "select", this::select);
	}
	
	public void select() {
		File file = fc.showOpenDialog(getWindow());
		if(file != null) {
			if(file.getName().toLowerCase().endsWith(".png")) {
				set(IconUtils.toIco(file));
			}else {
				set(file);
			}
		}
	}
	
	public void setOnSet(Consumer<File> onSet) {
		this.onSet = onSet;
	}

	public void setOnUnSet(Runnable onUnSet) {
		this.onUnSet = onUnSet;
	}

	public void set(File ico) {
		if(ico == null || !ico.exists()) {
			if(onUnSet != null) {
				onUnSet.run();
			}
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
