package org.luke.jwin.app.param;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainClassParam extends Param {
	private Entry<String, File> value;

	boolean bound = false;

	private Map<String, File> classNames;
	
	private Thread searcher;

	public MainClassParam(Stage ps, Supplier<Map<String, File>> classLister) {
		super("Main class");

		Stage classChooser = new Stage(StageStyle.UTILITY);
		classChooser.setTitle("Main class");

		VBox root = new VBox(15);
		root.setPadding(new Insets(15));

		classChooser.setScene(new Scene(root, 400, 400));

		Runnable adapt = () -> {
			classChooser.setY(ps.getY());
			classChooser.setX(ps.getX() + ps.getWidth() + 10);
		};

		ps.widthProperty().addListener((obs, ov, nv) -> adapt.run());

		ps.xProperty().addListener((obs, ov, nv) -> adapt.run());

		ps.yProperty().addListener((obs, ov, nv) -> adapt.run());

		TextField search = new TextField();
		search.setPromptText("search...");

		VBox results = new VBox();

		Effect ef = new ColorAdjust(0, 0, -.5, 0);
		
		search.textProperty().addListener((obs, ov, nv) -> {

			if(searcher != null) {
				searcher.interrupt();
			}
			
			results.getChildren().clear();
			
			if (nv.length() >= 3) {
				searcher = new Thread(()-> {	
					List<String> found = classNames.keySet().stream().filter(item -> item.toLowerCase().contains(nv.toLowerCase())).toList();
				
					found.forEach(e-> {
						Hyperlink className = new Hyperlink(e.replace("/", ".").replace("\\", ".").replace(".java", ""));
						className.setEffect(ef);
						className.setOnAction(a-> {
							File file = new File(classNames.get(e).getAbsolutePath().concat("/").concat(e));
							Entry<String, File> preVal = Map.entry(className.getText(), file);
							classChooser.close();
							set(preVal);
						});
						Platform.runLater(()-> results.getChildren().add(className));
					});
				});
				searcher.start();
			}
		});

		root.getChildren().addAll(search, new Separator(), results);

		addButton("select", e -> {
			if (!bound) {
				ps.getScene().getRoot().disableProperty().bind(classChooser.showingProperty());

				bound = true;
			}
			results.getChildren().clear();
			search.clear();
			classChooser.show();
			classNames = classLister.get();
		});
	}
	
	public void set(Entry<String, File> value) {
		this.value = value;
		list.getChildren().clear();
		addFile(value.getValue(), value.getKey());
	}

	public Entry<String, File> getValue() {
		return value;
	}

}
