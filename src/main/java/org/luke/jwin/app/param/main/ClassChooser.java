package org.luke.jwin.app.param.main;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.luke.gui.controls.alert.AlertType;
import org.luke.gui.controls.alert.BasicOverlay;
import org.luke.gui.controls.alert.ButtonType;
import org.luke.gui.controls.check.KeyedCheck;
import org.luke.gui.controls.label.unkeyed.Link;
import org.luke.gui.controls.scroll.VerticalScrollable;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.file.FileUtils;
import org.luke.gui.style.Style;
import org.luke.gui.window.Page;
import org.luke.jwin.app.JwinActions;
import org.luke.jwin.app.param.Param;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ClassChooser extends BasicOverlay {
	private final Param param;
	Supplier<Map<String, File>> classLister;

	private Entry<String, File> value;
	private Map<String, File> classNames;
	private Thread searcher;

	private final VerticalScrollable preResults;
	private final VBox results;
	private final KeyedCheck onlyMainClasses;

	public ClassChooser(Page ps, Supplier<Map<String, File>> classLister, Param param) {
		super(ps);
		removeTop();
		removeSubHead();
		removeDone();

		this.classLister = classLister;
		this.param = param;

		head.setKey("main_class_selection");

		MainClassSearch search = new MainClassSearch(ps.getWindow());

		onlyMainClasses = new KeyedCheck(ps.getWindow(), "only_main", 16);
		onlyMainClasses.property().set(true);

		results = new VBox();
		results.setPadding(new Insets(10));
		preResults = new VerticalScrollable();
		VBox.setVgrow(preResults, Priority.ALWAYS);

		preResults.setMinHeight(250);
		preResults.setMaxHeight(250);

		preResults.setMinWidth(0);
		preResults.maxWidthProperty().bind(center.widthProperty().subtract(32));

		preResults.setContent(results);

		BiConsumer<String, Boolean> searchFor = this::searchFor;

		search.valueProperty().addListener((_, _, nv) -> searchFor.accept(nv.trim(), onlyMainClasses.get()));

		center.setAlignment(Pos.CENTER);
		center.getChildren().addAll(search, onlyMainClasses, preResults);

		addOnShown(() -> {
			search.requestFocus();
			results.getChildren().clear();
			search.clear();
			classNames = classLister.get();
			searchFor.accept("", onlyMainClasses.get());
		});

		onlyMainClasses.property().addListener((_,_,_) ->
				searchFor.accept(search.getValue().trim(), onlyMainClasses.get()));

		applyStyle(ps.getWindow().getStyl());
	}

	private void searchFor(String nv, boolean onlyMain) {
		if (searcher != null) {
			searcher.interrupt();
		}

		preResults.getScrollBar().positionProperty().set(0);

		results.getChildren().clear();
		preResults.clearContent();
        searcher = new Thread(() -> {
            ArrayList<String> found = new ArrayList<>(classNames.keySet().stream()
					.filter(item -> {
						File itemFile = classNames.get(item);
						File f = new File(itemFile.getAbsolutePath().concat("/").concat(item));
						return item.toLowerCase().contains(nv.toLowerCase()) && (!onlyMain || isMainClass(f));
					}).toList());

			found.sort(Comparator.naturalOrder());

            found.forEach(e -> results.getChildren().add(treatFound(e)));
			Platform.runLater(() -> preResults.setContent(results));
        });
        searcher.start();
    }
	
	public Map<String, File> listMainClasses() {
		Map<String, File> classes = classLister.get();
		HashMap<String, File> res = new HashMap<>();
		
		classes.forEach((e, file) -> {
			File f = new File(file.getAbsolutePath().concat("/").concat(e));
			if(isMainClass(f)) {
				String name = e.replace(".java", "")
						.replace("/", ".")
						.replace("\\", ".");
				res.put(name, f);
			}
        });
		
		return res;
	}
	
	private boolean isMainClass(File f) {
		
		String content = FileUtils.readFile(f);

		String formattedSource = content.replace(" ", "").replace("\t", "").replace("\n", "");

		return formattedSource.contains("publicstati" + "cvoidmain(String");
	}

	private Link treatFound(String e) {
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
        return makeLink(e, displayName, name);
	}

	private Link makeLink(String e, StringBuilder displayName, String name) {
		Link className = new Link(getWindow(), displayName.toString());
		className.setAction(() -> {
			File file = new File(classNames.get(e).getAbsolutePath().concat("/").concat(e));

			if (isMainClass(file)) {
				Entry<String, File> preVal = Map.entry(name, file);
				hide();
				set(preVal);
			} else {
				JwinActions.alert("no_main_head",
						"no_main_body",
						AlertType.INFO, res -> {
							if (res == ButtonType.YES) {
								Entry<String, File> preVal = Map.entry(name, file);
								hide();
								set(preVal);
							}
						}, ButtonType.NO, ButtonType.YES);
			}

		});
		return className;
	}

	public void set(Entry<String, File> value) {
		if (value == null || !value.getValue().exists()) {
			return;
		}
		this.value = value;
		Platform.runLater(() -> {
			param.clearList();
			param.addFile(getWindow(), value.getValue(), value.getKey());
		});
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
		preResults.setBackground(Backgrounds.make(style.getBackgroundTertiaryOr(), 5.0));
		super.applyStyle(style);
	}
}
