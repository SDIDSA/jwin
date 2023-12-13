package org.luke.jwin.app;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.luke.gui.app.pages.Page;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;
import org.luke.jwin.app.display.JwinUi;
import org.luke.jwin.app.display.JwinUi1;

import javafx.beans.property.ObjectProperty;

public class JwinHome extends Page {

	private JwinUi config;

	protected JwinHome(Window window, Class<? extends JwinUi> uiType) {
		super(window, new Dimension(500 * 2 + 15 * 4 + 30, 600));

		try {
			config = uiType.getConstructor(Page.class).newInstance(this);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			config = new JwinUi1(this);
		}
		JwinActions actions = new JwinActions(window, config);

		config.setOnRun(actions::run);
		config.setOnCompile(actions::compile);

		getChildren().add(config);

		for (String param : window.getApp().getParameters().getRaw()) {
			String ext = param.substring(param.lastIndexOf(".") + 1);
			if (ext.equalsIgnoreCase("jwp")) {
				config.importProject(new File(param));
				return;
			}
		}
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
