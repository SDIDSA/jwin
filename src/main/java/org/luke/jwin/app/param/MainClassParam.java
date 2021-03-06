package org.luke.jwin.app.param;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.luke.jwin.ui.TextField;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Separator;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainClassParam extends Param {
	private Entry<String, File> value;

	private String altMain = null;
	
	private Map<String, File> classNames;

	private Thread searcher;

	public MainClassParam(Stage ps, Supplier<Map<String, File>> classLister) {
		super("Main class");

		Stage classChooser = new Stage(StageStyle.UTILITY);
		classChooser.setTitle("Main class");
		classChooser.setAlwaysOnTop(true);

		VBox root = new VBox(15);
		root.setPadding(new Insets(15));

		classChooser.setScene(new Scene(root, 400, 400));

		Runnable adapt = () -> {
			classChooser.setY(ps.getY() + ps.getHeight() / 2 - classChooser.getHeight() / 2);
			classChooser.setX(ps.getX() + ps.getWidth() / 2 - classChooser.getWidth() / 2);
		};
		ChangeListener<Number> listener = (obs, ov, nv) -> adapt.run();

		classChooser.setOnShown(e -> {
			ps.getScene().getRoot().setDisable(true);
			adapt.run();
		});
		
		classChooser.setOnHidden(e-> ps.getScene().getRoot().setDisable(false));

		ps.widthProperty().addListener(listener);
		ps.heightProperty().addListener(listener);
		ps.xProperty().addListener(listener);
		ps.yProperty().addListener(listener);

		TextField search = new TextField();
		search.setPromptText("search...");

		VBox results = new VBox();

		Effect ef = new ColorAdjust(0, 0, -.5, 0);

		search.textProperty().addListener((obs, ov, nv) -> {

			if (searcher != null) {
				searcher.interrupt();
			}

			results.getChildren().clear();

			if (nv.length() >= 3) {
				searcher = new Thread(() -> {
					List<String> found = classNames.keySet().stream()
							.filter(item -> item.toLowerCase().contains(nv.toLowerCase())).toList();

					found.forEach(e -> {
						Hyperlink className = new Hyperlink(
								e.replace("/", ".").replace("\\", ".").replace(".java", ""));
						className.setEffect(ef);
						className.setOnAction(a -> {
							File file = new File(classNames.get(e).getAbsolutePath().concat("/").concat(e));
							Entry<String, File> preVal = Map.entry(className.getText(), file);
							classChooser.close();
							set(preVal);
						});
						Platform.runLater(() -> results.getChildren().add(className));
					});
				});
				searcher.start();
			}
		});

		root.getChildren().addAll(search, new Separator(), results);

		addButton("select", e -> {
			results.getChildren().clear();
			search.clear();
			adapt.run();
			classChooser.show();
			classNames = classLister.get();
		});
	}

	public void set(Entry<String, File> value) {
		if(!value.getValue().exists()) {
			return;
		}
		this.value = value;
		list.getChildren().clear();
		addFile(value.getValue(), value.getKey());
	}

	public Entry<String, File> getValue() {
		return value;
	}
	
	public void setAltMain(String altMain) {
		this.altMain = altMain;
	}
	
	public String getAltMain() {
		return altMain;
	}

	@Override
	public void clear() {
		value = null;
		list.getChildren().clear();
	}

}
