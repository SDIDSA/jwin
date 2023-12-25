package org.luke.jwin.app.param.main;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.luke.gui.window.Page;
import org.luke.jwin.app.param.Param;

public class MainClassParam extends Param {
	private String altMain = null;

	private ClassChooser mclassChooser;

	public MainClassParam(Page ps, Supplier<Map<String, File>> classLister) {
		super(ps.getWindow(), "main_class");
		mclassChooser = new ClassChooser(ps, classLister, this);
		addButton(ps.getWindow(), "select", () -> showChooser());
	}
	
	public String showChooser() {
		Entry<String, File> oldVal = mclassChooser.getValue();
		mclassChooser.showAndWait();
		Entry<String, File> newVal = mclassChooser.getValue();
		
		if(newVal != null && (oldVal == null || !newVal.getValue().equals(oldVal.getValue()))) {
			return newVal.getKey();
		}
		return null;
	}
	
	public Map<String, File> listMainClasses() {
		return mclassChooser.listMainClasses();
	}

	public Entry<String, File> getValue() {
		return mclassChooser.getValue();
	}

	public void set(Entry<String, File> mainClass) {
		mclassChooser.set(mainClass);
	}

	public void setAltMain(String altMain) {
		this.altMain = altMain;
	}

	public String getAltMain() {
		return altMain;
	}

	@Override
	public void clear() {
		altMain = null;
		mclassChooser.clear();
		list.getChildren().clear();
	}

}
