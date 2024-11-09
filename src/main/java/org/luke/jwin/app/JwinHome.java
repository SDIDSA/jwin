package org.luke.jwin.app;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.luke.gui.exception.ErrorHandler;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Page;
import org.luke.gui.window.Window;
import org.luke.jwin.app.layout.JwinUi;
import org.luke.jwin.app.layout.JwinUi1;
import org.luke.jwin.app.layout.ui2.JwinUi2;
import org.luke.jwin.local.LocalStore;

import javafx.beans.property.ObjectProperty;

public class JwinHome extends Page {

	private JwinUi config;

	protected JwinHome(Window window) {
		super(window, new Dimension(500 * 2 + 15 * 4 + 30, 600));

		setConfig(LocalStore.getUiLayout().equals("sim") ? JwinUi2.class : JwinUi1.class);
	}
	
	public void setUiLayout(String uiLayout) {
		setConfig(uiLayout.equals("sim") ? JwinUi2.class : JwinUi1.class);
		LocalStore.setUiLayout(uiLayout);
	}
	
	private void setConfig(Class<? extends JwinUi> uiType) {
		JwinUi old = config;
		
		try {
			config = uiType.getConstructor(Page.class).newInstance(this);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			ErrorHandler.handle(e, "create config object");
			config = new JwinUi1(this);
		}
		JwinActions actions = new JwinActions(window, config);

		config.setOnRun(actions::run);
		config.setOnCompile(actions::compile);

		if(old == null) {
			for (String param : window.getApp().getParameters().getRaw()) {
				String ext = param.substring(param.lastIndexOf(".") + 1);
				if (ext.equalsIgnoreCase("jwp")) {
					config.importProject(new File(param));
				}
			}
		}else {
			getChildren().remove(old);
			if (old.getFileInUse() != null)
				config.importProject(old.getFileInUse());
			else if(old.getClasspath().getRoot() != null)
				config.loadProject(old.export());
		}
		
		getChildren().addFirst(config);
	}
	
	public JwinUi getConfig() {
		return config;
	}

	@Override
	public void applyStyle(Style style) {

	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

}
