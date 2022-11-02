package org.luke.jwin.app;

import java.awt.Dimension;
import java.io.File;

import org.luke.gui.app.pages.Page;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;

public class JwinHome extends Page {

	protected JwinHome(Window window) {
		super(window, new Dimension(500 * 2 + 15 * 4 + 30, 600));

		JwinUi config = new JwinUi(this);
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

	@Override
	public void applyStyle(Style style) {
		
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

}
