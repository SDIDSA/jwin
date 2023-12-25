package org.luke.jwin.local.ui;

import java.io.File;
import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.controls.label.unkeyed.Text;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LocalInstallUi extends VBox implements Styleable {
	protected HBox root;
	
	protected String version;
	protected File targetDir;

	protected Text verLab;
	protected Label stateLabl;

	public LocalInstallUi(Window win, String version, File targetDir) {
		super(5);
		setAlignment(Pos.CENTER);

		root = new HBox(10);
		root.setAlignment(Pos.CENTER);
		
		this.version = version;
		this.targetDir = targetDir;

		verLab = new Text(version, new Font(Font.DEFAULT_MONO_FAMILY,14));
		stateLabl = new Label(win, "", new Font(Font.DEFAULT_MONO_FAMILY,14));

		StackPane preVer = new StackPane(verLab);
		preVer.setAlignment(Pos.CENTER_LEFT);
		preVer.setMinWidth(100);

		root.getChildren().addAll(preVer ,stateLabl);

		getChildren().add(root);
		
		applyStyle(win.getStyl());
	}

	public File getTargetDir() {
		return targetDir;
	}

	public String getVersion() {
		return version;
	}

	@Override
	public void applyStyle(Style style) {
		verLab.setFill(style.getHeaderSecondary());
		stateLabl.setFill(style.getHeaderSecondary());
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

}
