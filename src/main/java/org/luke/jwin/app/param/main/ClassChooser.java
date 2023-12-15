package org.luke.jwin.app.param.main;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.luke.gui.app.pages.Page;
import org.luke.gui.controls.alert.AlertType;
import org.luke.gui.controls.alert.BasicOverlay;
import org.luke.gui.controls.alert.ButtonType;
import org.luke.gui.controls.label.unkeyed.Link;
import org.luke.gui.controls.scroll.Scrollable;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.file.FileUtils;
import org.luke.gui.style.Style;
import org.luke.jwin.app.JwinActions;
import org.luke.jwin.app.param.Param;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ClassChooser extends BasicOverlay {
	private Param param;
	Supplier<Map<String, File>> classLister;

	private Entry<String, File> value;
	private Map<String, File> classNames;
	private Thread searcher;

	private Scrollable preResults;
	private VBox results;

	public ClassChooser(Page ps, Supplier<Map<String, File>> classLister, Param param) {
		super(ps);
		removeTop();
		removeSubHead();

		this.classLister = classLister;
		this.param = param;

		head.setKey("Main class selection");

		MainClassSearch search = new MainClassSearch(ps.getWindow());

		results = new VBox();
		results.setPadding(new Insets(10));
		preResults = new Scrollable();
		VBox.setVgrow(preResults, Priority.ALWAYS);

		preResults.setMinHeight(250);
		preResults.setMaxHeight(250);

		preResults.setMinWidth(0);
		preResults.maxWidthProperty().bind(center.widthProperty().subtract(32));

		preResults.setContent(results);

		Consumer<String> searchFor = this::searchFor;

		search.valueProperty().addListener((obs, ov, nv) -> searchFor.accept(nv));

		center.setAlignment(Pos.CENTER);
		center.getChildren().addAll(search, preResults);

		addOnShown(() -> {
			search.requestFocus();
			results.getChildren().clear();
			search.clear();
			classNames = classLister.get();
			searchFor.accept("");
		});

		applyStyle(ps.getWindow().getStyl());
	}

	private void searchFor(String nv) {
		preResults.getScrollBar();
		if (searcher != null) {
			searcher.interrupt();
		}

		preResults.getScrollBar().positionProperty().set(0);

		results.getChildren().clear();

		if (nv.length() >= 0) {
			searcher = new Thread(() -> {
				List<String> found = classNames.keySet().stream()
						.filter(item -> item.toLowerCase().contains(nv.toLowerCase())).toList();

				found.forEach(e -> {
					treatFound(e);
					try {
						Thread.sleep(10);
					} catch (InterruptedException e1) {
						Thread.currentThread().interrupt();
					}
				});
			});
			searcher.start();
		}
	}
	
	public Map<String, File> listMainClasses() {
		Map<String, File> classes = classLister.get();
		System.out.println(classes);
		HashMap<String, File> res = new HashMap<>();
		
		classes.forEach((e, file) -> {
			File f = new File(file.getAbsolutePath().concat("/").concat(e));
			if(isMainClass(f)) {
				String name = e.replace(".java", "").replace("/", ".").replace("\\", ".");
				StringBuilder displayName = new StringBuilder();
				String[] parts = name.split("\\.");
				for (int i = parts.length - 1; i >= 0 && i >= parts.length - 3; i--) {
					if (!displayName.isEmpty()) {
						displayName.insert(0, '.');
					}
					displayName.insert(0, parts[i]);
				}
				if (parts.length > 3) {
					displayName.insert(0, "... .");
				}
				
				res.put(name, f);
			};
		});
		
		return res;
	}
	
	private boolean isMainClass(File f) {
		
		String content = FileUtils.readFile(f);

		String formattedSource = content.replace(" ", "").replace("\t", "").replace("\n", "");

		return formattedSource.contains("publicstaticvoidmain(String");
	}

	private void treatFound(String e) {
		String name = e.replace(".java", "").replace("/", ".").replace("\\", ".");
		StringBuilder displayName = new StringBuilder();
		String[] parts = name.split("\\.");
		for (int i = parts.length - 1; i >= 0 && i >= parts.length - 3; i--) {
			if (!displayName.isEmpty()) {
				displayName.insert(0, '.');
			}
			displayName.insert(0, parts[i]);
		}
		if (parts.length > 3) {
			displayName.insert(0, "... .");
		}
		Link className = new Link(getWindow(), displayName.toString());
		className.setAction(() -> {
			File file = new File(classNames.get(e).getAbsolutePath().concat("/").concat(e));
			String content = FileUtils.readFile(file);

			String formattedSource = content.replace(" ", "").replace("\t", "").replace("\n", "");

			if (formattedSource.contains("publicstaticvoidmain(String")) {
				Entry<String, File> preVal = Map.entry(name, file);
				hide();
				set(preVal);
			} else {
				JwinActions.alert("Probably not a main class",
						"The class you have selected doesn't seem to define a main method, do you want to use it anyway ?",
						AlertType.INFO, res -> {
							if (res == ButtonType.YES) {
								Entry<String, File> preVal = Map.entry(name, file);
								hide();
								set(preVal);
							}
						}, ButtonType.NO, ButtonType.YES);
			}

		});
		Platform.runLater(() -> results.getChildren().add(className));
	}

	public void set(Entry<String, File> value) {
		if (value == null || !value.getValue().exists()) {
			return;
		}
		this.value = value;
		param.clearList();
		param.addFile(getWindow(), value.getValue(), value.getKey());
	}

	public Entry<String, File> getValue() {
		return value;
	}

	public void clear() {
		value = null;
	}

	@Override
	public void applyStyle(Style style) {
		preResults.getScrollBar().setThumbFill(style.getChannelsDefault());
		preResults.setBackground(Backgrounds.make(style.getBackgroundTertiary(), 5.0));
		super.applyStyle(style);
	}
}
