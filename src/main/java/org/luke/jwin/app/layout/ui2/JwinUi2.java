package org.luke.jwin.app.layout.ui2;

import org.luke.gui.controls.input.InputIconButton;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.controls.popup.tooltip.TextTooltip;
import org.luke.gui.controls.space.Separator;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Page;
import org.luke.jwin.app.console.ConsoleOutput;
import org.luke.jwin.app.layout.JwinUi;
import org.luke.jwin.app.layout.JwinUi1;
import org.luke.jwin.ui.Button;
import org.luke.jwin.ui.MultiButton;
import org.luke.jwin.ui.Progress;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class JwinUi2 extends JwinUi implements Styleable {

	private HBox top;
	private Progress p;
	private Label state;

    private ConsoleOutput logs;

	public JwinUi2(Page ps) {
		super(ps);
		init();
	}

	public JwinUi2(JwinUi source) {
		super(source);
		init();
	}

	public JwinUi2(JwinUi1 source) {
		super(source);
		init();
	}

	public JwinUi2(JwinUi2 source) {
		super(source);
		init();
	}

	private void init() {
		MultiButton run = new MultiButton(ps, "run");
		run.addAction("build_installer", () -> {
			if (onCompile != null) {
				logStd("building_installer");
				onCompile.run();
			}
		});

		run.setAction(() -> {
			if (onRun != null) {
				onRun.run();
			}
		});

		Button settings = new Button(ps, "settings", 100, 40);

		SettingsMenu settingsMen = new SettingsMenu(ps, this);

		settings.setAction(() -> settingsMen.showPop(settings, Direction.DOWN_LEFT, 0, 10));

		HBox.setHgrow(version, Priority.SOMETIMES);
		HBox.setHgrow(publisher, Priority.SOMETIMES);

		IconSetting icon = new IconSetting(ps, this.icon::select);

		TextTooltip.install(icon, Direction.UP, "select_app_icon", 15, 15, true);

        this.icon.setOnSet(icon::set);
		this.icon.setOnUnSet(icon::unset);

		InputIconButton imp = new InputIconButton(ps, "java", 24, "import_java_project", () -> {
			importJavaProject(ps);
		});

		InputIconButton impJw = new InputIconButton(ps, "jwin", 24, "import_jwin_project", () -> {
			importProject(ps);
		});

		top = new HBox(10, icon, appName, imp, impJw, version, publisher, settings, run);
		top.setAlignment(Pos.BOTTOM_CENTER);

		logs = new ConsoleOutput(ps);

		VBox.setVgrow(logs, Priority.ALWAYS);

		p = new Progress(ps);
		p.setProgress(-1);

		HBox.setHgrow(p, Priority.ALWAYS);
		state = new Label(ps, "idle");

		StackPane preState = new StackPane(state);
		preState.setMinWidth(150);

		HBox bottom = new HBox(15, p, new Separator(ps, Orientation.VERTICAL), preState);
		bottom.setAlignment(Pos.CENTER);

		VBox root = new VBox(15, top, logs, new Separator(ps, Orientation.HORIZONTAL), bottom);
		root.setPadding(new Insets(15));

		logs.prefWidthProperty().bind(root.widthProperty());

		p.setMinWidth(0);
		p.maxWidthProperty().bind(root.widthProperty());

		root.setMinWidth(0);
		root.maxWidthProperty().bind(ps.widthProperty().subtract(31));

		getChildren().add(root);

		applyStyle(ps.getStyl());
	}

	@Override
	public void preImport() {
		top.setDisable(true);
	}

	@Override
	public void postImport(boolean success) {
		top.setDisable(false);
		if(success)
			logStd("project_ready");
	}

	@Override
	public void preRun(Process p) {
		top.setDisable(true);
		logs.setOnStop(() -> {
			stop(p);
		});
	}

	@Override
	public void postRun(boolean ran) {
		top.setDisable(false);
		logs.setOnStop(null);
	}

	@Override
	public void disable(boolean b, boolean ran) {
		top.setDisable(b);
	}

	private long lastUpdate = 0;

	public void setProgress(double p) {
		long now = System.currentTimeMillis();
		if (now - lastUpdate > 10 || p == -1) {
			Platform.runLater(() -> this.p.setProgress(p));
			lastUpdate = now;
		}
	}

	@Override
	public void setState(String val) {
		Platform.runLater(() -> state.setKey(val));
	}

	@Override
	public void incrementProgress(double max) {
		Platform.runLater(() -> p.setProgress(Math.min(max, p.getProgress() + .005)));

	}

	@Override
	public void logStd(String line) {
		logStd(line, true);
	}

	@Override
	public void logStd(String line, boolean keyed) {
		Platform.runLater(() -> logs.addOutput(line, keyed));
	}

	@Override
	public void logErr(String line) {
		Platform.runLater(() -> logs.addError(line));
	}

	@Override
	public void clearLogs() {
		logs.clear();
	}

	@Override
	public void logStdOver(String line) {
		Platform.runLater(() -> logs.overrideLast(line));
	}

	@Override
	public void applyStyle(Style style) {
		state.setFill(style.getHeaderSecondary());
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

}
