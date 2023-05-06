package org.luke.jwin.app;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.luke.gui.app.pages.Page;
import org.luke.gui.controls.Loading;
import org.luke.gui.controls.alert.AlertType;
import org.luke.gui.controls.alert.ButtonType;
import org.luke.gui.controls.check.KeyedCheck;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.controls.space.Separator;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;
import org.luke.jwin.app.file.FileDealer;
import org.luke.jwin.app.file.JWinProject;
import org.luke.jwin.app.more.MoreSettings;
import org.luke.jwin.app.param.ClasspathParam;
import org.luke.jwin.app.param.IconParam;
import org.luke.jwin.app.param.JdkParam;
import org.luke.jwin.app.param.JreParam;
import org.luke.jwin.app.param.Param;
import org.luke.jwin.app.param.deps.DependenciesParam;
import org.luke.jwin.app.param.main.MainClassParam;
import org.luke.jwin.ui.Button;
import org.luke.jwin.ui.ProgressBar;
import org.luke.jwin.ui.TextField;
import org.luke.jwin.ui.TextVal;

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
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class JwinUi extends StackPane implements Styleable {
	private Window ps;

	// Data
	private JWinProject projectInUse;
	private File fileInUse;

	// Project Params
	private ClasspathParam classpath;
	private MainClassParam mainClass;
	private JdkParam jdk;
	private DependenciesParam dependencies;
	private JreParam jre;
	private IconParam icon;

	private TextVal appName;
	private TextVal version;
	private TextVal publisher;
	private TextField guid;

	private KeyedCheck admin;
	private KeyedCheck console;
	private MoreSettings moreSettings;

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

	// Action Handlers
	private Runnable onRun;
	private Runnable onCompile;

	// Utilities
	private FileChooser saver;

	private Label guidLab;

	public JwinUi(Page ps) {
		this.ps = ps.getWindow();

		loading = new Loading(15);
		loading.setMouseTransparent(true);
		loading.setVisible(false);

		preRoot = new HBox(15);
		preRoot.setPadding(new Insets(15));

		VBox root1 = new VBox(15);

		VBox root2 = new VBox(15);

		classpath = new ClasspathParam(ps.getWindow());

		mainClass = new MainClassParam(ps, classpath::listClasses);

		jdk = new JdkParam(ps.getWindow());

		dependencies = new DependenciesParam(ps.getWindow(), classpath::getPom, jdk);

		jre = new JreParam(ps.getWindow(), classpath, jdk, dependencies, mainClass);

		icon = new IconParam(ps.getWindow());

		run = new Button(ps.getWindow(), "Run", -1);
		run.setMinWidth(60);
		run.setAction(() -> {
			if (onRun != null) {
				onRun.run();
			}
		});

		compile = new Button(ps.getWindow(), "Build", -1);
		compile.setMinWidth(60);
		compile.setDisable(true);
		compile.setAction(() -> {
			if (onCompile != null) {
				onCompile.run();
			}
		});

		advanced = new Button(ps.getWindow(), "more\nsettings", -1);
		advanced.setMinWidth(80);
		advanced.setTextAlignment(TextAlignment.CENTER);

		Button save = new Button(ps.getWindow(), "Save", -1);
		save.setMinWidth(60);
		save.setAction(this::saveProject);

		Button load = new Button(ps.getWindow(), "Load", -1);
		load.setMinWidth(60);
		load.setAction(() -> {
			File loadFrom = saver.showOpenDialog(ps.getWindow());
			if (loadFrom != null) {
				importProject(loadFrom);
			}
		});

		saveLoad = new VBox(10, save, load);
		buildRun = new VBox(10, run, compile);

		advanced.minHeightProperty().bind(saveLoad.heightProperty());

		HBox bottom = new HBox(10);
		bottom.setAlignment(Pos.BOTTOM_CENTER);

		progress = new ProgressBar(ps.getWindow());
		state = new Label(ps.getWindow(), "doing nothing");

		StackPane progressCont = new StackPane(progress, state);
		progressCont.setAlignment(Pos.CENTER);
		progress.prefWidthProperty().bind(progressCont.widthProperty());

		HBox.setHgrow(progressCont, Priority.ALWAYS);

		progress.minHeightProperty().bind(saveLoad.heightProperty());

		appName = new TextVal(ps.getWindow(), "App name");
		version = new TextVal(ps.getWindow(), "Version");
		publisher = new TextVal(ps.getWindow(), "Publisher");

		console = new KeyedCheck(ps.getWindow(), "Console", 16);
		admin = new KeyedCheck(ps.getWindow(), "Admin", 16);

		guid = new TextField(ps.getWindow());
		guid.setEditable(false);
		HBox.setHgrow(guid, Priority.ALWAYS);

		Button generate = new Button(ps.getWindow(), "Generate", 100, 40);
		generate.setAction(() -> guid.setValue(UUID.randomUUID().toString()));

		moreSettings = new MoreSettings(ps);

		advanced.setAction(moreSettings::show);

		preConsole = new HBox(10);
		preConsole.setAlignment(Pos.CENTER);

		guidLab = new Label(ps.getWindow(), "GUID");
		preConsole.getChildren().addAll(console, admin, new Separator(ps.getWindow(), Orientation.VERTICAL), guidLab, guid,
				generate);

		preBottom = new HBox(15, appName, version, publisher);

		bottom.getChildren().addAll(progressCont, buildRun, saveLoad, advanced);

		root1.getChildren().addAll(classpath, new Separator(ps.getWindow(), Orientation.HORIZONTAL), mainClass,
				vSpace(), jdk, jre);

		root2.getChildren().addAll(icon, new Separator(ps.getWindow(), Orientation.HORIZONTAL), dependencies, preBottom,
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

		// Project Saver

		saver = new FileChooser();
		saver.getExtensionFilters().add(new ExtensionFilter("jWin Project", "*.jwp"));

		applyStyle(ps.getWindow().getStyl());
	}

	public JWinProject getProjectInUse() {
		return projectInUse;
	}

	public File getFileInUse() {
		return fileInUse;
	}

	public void setProjectInUse(JWinProject projectInUse) {
		this.projectInUse = projectInUse;
	}

	public void saveProject() {
		JWinProject project = export();
		saver.setInitialFileName(appName.getValue() + "_jWin_Project");
		File saveTo = saver.showSaveDialog(ps);
		if (saveTo != null) {
			FileDealer.write(project.serialize(), saveTo);
			projectInUse = project;
			fileInUse = saveTo;
		}
	}

	public void importProject(File loadFrom) {
		preRoot.setDisable(true);
		loading.setVisible(true);
		loading.play();
		new Thread(() -> {
			JWinProject project = JWinProject.deserialize(FileDealer.read(loadFrom));

			runOnUiThread(Param::clearAll);
			project.getClasspath().forEach(f -> runOnUiThread(() -> classpath.add(f)));
			mainClass.setAltMain(null);
			runOnUiThread(() -> mainClass.set(project.getMainClass()));
			runOnUiThread(() -> jdk.set(project.getJdk()));
			runOnUiThread(() -> jre.set(project.getJre()));
			runOnUiThread(() -> icon.set(project.getIcon()));
			runOnUiThread(() -> appName.setValue(project.getAppName()));
			runOnUiThread(() -> version.setValue(project.getAppVersion()));
			runOnUiThread(() -> publisher.setValue(project.getAppPublisher()));
			runOnUiThread(() -> console.checkedProperty().set(project.isConsole()));
			runOnUiThread(() -> admin.checkedProperty().set(project.isAdmin()));
			runOnUiThread(() -> guid.setValue(project.getGuid()));
			project.getManualJars().forEach(f -> runOnUiThread(() -> dependencies.addManualJar(f)));

			runOnUiThread(() -> moreSettings.setFileTypeAssociation(project.getFileTypeAsso()));
			runOnUiThread(() -> moreSettings.setUrlProtocolAssociation(project.getUrlProtocolAsso()));

			projectInUse = project;
			fileInUse = loadFrom;

			runOnUiThread(() -> dependencies.resolve(classpath::getPom, jdk, false));

			Platform.runLater(() -> {
				preRoot.setDisable(false);
				loading.setVisible(false);
				loading.stop();
				compile.setDisable(true);
			});
		}).start();
	}

	private static void runOnUiThread(Runnable r) {
		Platform.runLater(r);
		sleep(50);
	}

	private static void sleep(long dur) {
		try {
			Thread.sleep(dur);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	public void setOnRun(Runnable onRun) {
		this.onRun = onRun;
	}

	public void setOnCompile(Runnable onCompile) {
		this.onCompile = onCompile;
	}

	private AtomicBoolean ran = new AtomicBoolean(false);

	public void run(Process p, Runnable showLog) {
		Platform.runLater(() -> {
			buildRun.setDisable(false);
			run.setKey("Stop");
			run.setDisable(false);
			run.setAction(() -> stop(p));
		});

		try {
			int exit = p.waitFor();
			if (exit != 0 && !stopped) {
				ran.set(false);
				JwinActions.alert("Non 0 exit code",
						"your project might not have executed correctly, it exited with code " + exit, AlertType.INFO,
						res -> {
							if (res == ButtonType.VIEW_LOG) {
								showLog.run();
							}
						}, ButtonType.VIEW_LOG, ButtonType.CLOSE);
			} else {
				ran.set(true);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}

		stopped = false;
		Platform.runLater(() -> {
			run.setKey("Run");
			progress.setProgress(-1);
			state.setText("doing nothing");
			disable(false);
		});
		run.setAction(onRun);
	}

	private boolean stopped = false;
	public void stop(Process p) {
		stopped = true;
		p.descendants().forEach(ProcessHandle::destroyForcibly);
	}

	public void onErr() {
		disable(false);
		setState("Failed to run :(");
		setProgress(-1);
	}

	public JWinProject export() {
		return new JWinProject(this);
	}

	public void setState(String s) {
		Platform.runLater(() -> state.setText(s));
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

	public void disable(boolean b) {
		Platform.runLater(() -> {
			Param.disable(b);
			preBottom.setDisable(b);
			preConsole.setDisable(b);
			saveLoad.setDisable(b);
			buildRun.setDisable(b);
			advanced.setDisable(b);
			compile.setDisable(!ran.get());
		});
	}

	public ClasspathParam getClasspath() {
		return classpath;
	}

	public MainClassParam getMainClass() {
		return mainClass;
	}

	public JdkParam getJdk() {
		return jdk;
	}

	public DependenciesParam getDependencies() {
		return dependencies;
	}

	public JreParam getJre() {
		return jre;
	}

	public IconParam getIcon() {
		return icon;
	}

	public TextVal getAppName() {
		return appName;
	}

	public TextVal getVersion() {
		return version;
	}

	public TextVal getPublisher() {
		return publisher;
	}

	public TextField getGuid() {
		return guid;
	}

	public KeyedCheck getConsole() {
		return console;
	}

	public KeyedCheck getAdmin() {
		return admin;
	}

	public MoreSettings getMoreSettings() {
		return moreSettings;
	}

	public static Pane vSpace() {
		Pane space = new Pane();
		VBox.setVgrow(space, Priority.ALWAYS);
		return space;
	}

	@Override
	public void applyStyle(Style style) {
		console.setTextFill(style.getTextNormal());
		admin.setTextFill(style.getTextNormal());
		state.setFill(style.getTextNormal());
		guidLab.setFill(style.getTextNormal());
		loading.setFill(style.getTextNormal());
		progress.setStyle("-fx-accent: " + Styleable.colorToCss(style.getAccent()));
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
