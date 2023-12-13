package org.luke.jwin.app.param.deps;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.unkeyed.Link;
import org.luke.gui.controls.scroll.Scrollable;
import org.luke.gui.controls.tab.Tab;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.jwin.app.param.Param;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DepTab extends Tab implements Styleable {
	private Param param;

	private ArrayList<File> files;

	private Scrollable scrollable;
	private VBox list;

	public DepTab(Param param, String title) {
		super(title);
		this.param = param;

		files = new ArrayList<>();

		list = new VBox(10);
		list.setPadding(new Insets(10, 15, 10, 10));

		scrollable = new Scrollable();
		scrollable.setContent(list);

		setContent(scrollable);

		applyStyle(param.getWindow().getStyl());
	}

	public void clear() {
		files.clear();
		list.getChildren().clear();
	}

	public List<File> getFiles() {
		return files;
	}

	public void addJar(File jar) {
		if (!jar.exists()) {
			return;
		}
		files.add(jar);
		Link remove = new Link(param.getWindow(), "remove", new Font(12));
		HBox line = param.generateLine(param.getWindow(), jar, jar.getName(), remove);
		list.getChildren().add(line);
		remove.setAction(() -> {
			list.getChildren().remove(line);
			files.remove(jar);
		});
	}

	@Override
	public void applyStyle(Style style) {
		scrollable.getScrollBar().setThumbFill(style.getChannelsDefault());
		scrollable
				.setBackground(Backgrounds.make(style.getBackgroundTertiary(), new CornerRadii(0, 0, 5, 5, false)));
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

}
