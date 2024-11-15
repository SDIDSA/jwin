package org.luke.jwin.app.layout;

import java.util.UUID;

import org.luke.gui.controls.Loading;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.controls.space.Separator;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Page;
import org.luke.jwin.app.layout.ui2.JwinUi2;
import org.luke.jwin.app.param.Param;
import org.luke.jwin.ui.Button;
import org.luke.jwin.ui.ProgressBar;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class JwinUi1 extends JwinUi implements Styleable {
	// Layouts
	private HBox preBottom;
	private HBox preConsole;
	private VBox saveLoad;
	private VBox buildRun;
	private Button advanced;
	private Button run;
	private Button compile;

	// MultiThread
	private ProgressBar progress;
	private Label state;
	private HBox preRoot;
	private Loading loading;

	private Label guidLab;

	public JwinUi1(Page ps) {
		super(ps);
		init();
	}

	public JwinUi1(JwinUi source) {
		super(source);
		init();
	}

	public JwinUi1(JwinUi1 source) {
		super(source);
		init();
	}

	public JwinUi1(JwinUi2 source) {
		super(source);
		init();
	}

	public void init() {
		loading = new Loading(15);
		loading.setMouseTransparent(true);
		loading.setVisible(false);

		preRoot = new HBox(15);
		preRoot.setPadding(new Insets(15));

		VBox root1 = new VBox(15);

		VBox root2 = new VBox(15);

		run = new Button(ps, "run", -1);
		run.setMinWidth(80);
		run.setAction(() -> {
			if (onRun != null) {
				onRun.run();
			}
		});

		compile = new Button(ps, "build", -1);
		compile.setMinWidth(80);
		compile.setDisable(true);
		compile.setAction(() -> {
			if (onCompile != null) {
				onCompile.run();
			}
		});

		advanced = new Button(ps, "more_n_settings", -1);
		advanced.setMinWidth(80);
		advanced.setTextAlignment(TextAlignment.CENTER);

		Button save = new Button(ps, "save", -1);
		save.setMinWidth(80);
		save.setAction(this::saveAs);

		Button load = new Button(ps, "load", -1);
		load.setMinWidth(80);
		load.setAction(() -> {
			importProject(ps);
		});

		saveLoad = new VBox(10, save, load);
		buildRun = new VBox(10, run, compile);

		advanced.minHeightProperty().bind(saveLoad.heightProperty());

		HBox bottom = new HBox(10);
		bottom.setAlignment(Pos.BOTTOM_CENTER);

		progress = new ProgressBar(ps);
		state = new Label(ps, "doing_nothing");

		StackPane progressCont = new StackPane(progress, state);
		progressCont.setAlignment(Pos.CENTER);
		progress.prefWidthProperty().bind(progressCont.widthProperty());

		HBox.setHgrow(progressCont, Priority.ALWAYS);

		progress.minHeightProperty().bind(saveLoad.heightProperty());


		Button generate = new Button(ps, "generate", 100, 40);
		generate.setAction(() -> guid.setValue(UUID.randomUUID().toString()));

		advanced.setAction(moreSettings::show);

		preConsole = new HBox(10);
		preConsole.setAlignment(Pos.CENTER);

		guidLab = new Label(ps, "guid");
		preConsole.getChildren().addAll(new VBox(5, console, admin), new Separator(ps, Orientation.VERTICAL), guidLab,
				guid, generate);

		preBottom = new HBox(15, appName, version, publisher);

		bottom.getChildren().addAll(progressCont, buildRun, saveLoad, advanced);

		root1.getChildren().addAll(classpath, new Separator(ps, Orientation.HORIZONTAL), mainClass,
				vSpace(), jdk, jre);

		root2.getChildren().addAll(icon, new Separator(ps, Orientation.HORIZONTAL), dependencies, preBottom,
				preConsole, bottom);

		preRoot.getChildren().addAll(root1, root2);

		DoubleBinding width = preRoot.widthProperty().subtract(45).divide(2);
		root1.maxWidthProperty().bind(width);
		root1.minWidthProperty().bind(width);
		root2.maxWidthProperty().bind(width);
		root2.minWidthProperty().bind(width);

		preRoot.setMinWidth(0);
		preRoot.maxWidthProperty().bind(widthProperty());

		getChildren().addAll(preRoot, loading);

		applyStyle(ps.getStyl());
	}
	
	public void preImport() {
		preRoot.setDisable(true);
		loading.setVisible(true);
		loading.play();
	}
	
	public void postImport(boolean success) {
		preRoot.setDisable(false);
		loading.setVisible(false);
		loading.stop();
		compile.setDisable(true);
	}

	@Override
	public void preRun(Process p) {
		buildRun.setDisable(false);
		run.setKey("stop");
		run.setDisable(false);
		run.setAction(() -> stop(p));
	}
	
	@Override
	public void postRun(boolean ran) {
		run.setKey("run");
		progress.setProgress(-1);
		state.setKey("idle");
		disable(false, ran);
		run.setAction(onRun);
	}

	public void setState(String s) {
		Platform.runLater(() -> state.setKey(s));
	}

	private long lastUpdate = 0;

	public void setProgress(double p) {
		long now = System.currentTimeMillis();
		if (now - lastUpdate > 10 || p == -1) {
			Platform.runLater(() -> progress.setProgress(p));
			lastUpdate = now;
		}
	}

	public void incrementProgress(double max) {
		Platform.runLater(() -> progress.setProgress(Math.min(max, progress.getProgress() + .005)));
	}

	public void disable(boolean b, boolean ran) {
		Platform.runLater(() -> {
			Param.disable(b);
			preBottom.setDisable(b);
			preConsole.setDisable(b);
			saveLoad.setDisable(b);
			buildRun.setDisable(b);
			advanced.setDisable(b);
			compile.setDisable(!ran);
		});
	}

	public static Pane vSpace() {
		Pane space = new Pane();
		VBox.setVgrow(space, Priority.ALWAYS);
		return space;
	}

	@Override
	public void applyStyle(Style style) {
		state.setFill(style.getTextNormal());
		guidLab.setFill(style.getTextNormal());
		loading.setFill(style.getTextNormal());
		progress.setStyle("-fx-accent: " + Styleable.colorToCss(style.getAccent()));
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

	@Override
	public void logStd(String line) {
		//Do Nothing
	}

	@Override
	public void logStd(String line, boolean keyed) {
		//Do Nothing
	}

	@Override
	public void logErr(String line) {
		//Do Nothing
	}

	@Override
	public void clearLogs() {
		//DO NOTHING
	}

	@Override
	public void logStdOver(String line) {
		logStd(line);
	}
}
