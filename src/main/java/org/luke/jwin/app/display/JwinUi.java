package org.luke.jwin.app.display;

import java.io.File;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.luke.gui.app.pages.Page;
import org.luke.gui.controls.alert.AlertType;
import org.luke.gui.controls.alert.ButtonType;
import org.luke.gui.controls.check.KeyedCheck;
import org.luke.gui.window.Window;
import org.luke.jwin.app.JwinActions;
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
import org.luke.jwin.ui.TextField;
import org.luke.jwin.ui.TextVal;

import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public abstract class JwinUi extends StackPane {
	protected Window ps;

	// Data
	protected JWinProject projectInUse;
	protected File fileInUse;

	// Project Params
	protected ClasspathParam classpath;
	protected MainClassParam mainClass;
	protected JdkParam jdk;
	protected DependenciesParam dependencies;
	protected JreParam jre;
	protected IconParam icon;

	protected TextVal appName;
	protected TextVal version;
	protected TextVal publisher;
	protected TextField guid;

	protected KeyedCheck admin;
	protected KeyedCheck console;
	protected MoreSettings moreSettings;

	// Action Handlers
	protected Runnable onRun;
	protected Runnable onCompile;

	// Utilities
	protected FileChooser saver;
	protected DirectoryChooser dsaver;

	public JwinUi(Page ps) {
		this.ps = ps.getWindow();

		classpath = new ClasspathParam(ps.getWindow());

		mainClass = new MainClassParam(ps, classpath::listClasses);

		jdk = new JdkParam(ps.getWindow());

		dependencies = new DependenciesParam(ps.getWindow(), classpath::getPom, this);

		jre = new JreParam(ps.getWindow(), this);

		icon = new IconParam(ps.getWindow());

		appName = new TextVal(ps.getWindow(), "App name");
		version = new TextVal(ps.getWindow(), "Version");
		publisher = new TextVal(ps.getWindow(), "Publisher");

		console = new KeyedCheck(ps.getWindow(), "Console", 16);
		admin = new KeyedCheck(ps.getWindow(), "Admin", 16);

		guid = new TextField(ps.getWindow());
		guid.setEditable(false);
		HBox.setHgrow(guid, Priority.ALWAYS);

		moreSettings = new MoreSettings(ps);

		saver = new FileChooser();
		saver.getExtensionFilters().add(new ExtensionFilter("jWin Project", "*.jwp"));
		
		dsaver = new DirectoryChooser();
	}

	public JWinProject export() {
		return new JWinProject(this);
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
	
	public void importProject(Window win) {
		File loadFrom = saver.showOpenDialog(win);
		if (loadFrom != null) {
			importProject(loadFrom);
		}
	}

	public void importProject(File loadFrom) {
		preImport();
		fileInUse = loadFrom;
		loadProject(JWinProject.deserialize(FileDealer.read(loadFrom)));
	}
	
	public void importJavaProject(Window win) {
		File loadFrom = dsaver.showDialog(win);
		if (loadFrom != null) {
			importJavaProject(loadFrom);
		}
	}
	
	private void importJavaProject(File loadFrom) {
		preImport();
		JWinProject proj = JWinProject.fromJavaProject(loadFrom);
		System.out.println(proj.serialize());
		loadProject(proj);
	}

	public void loadProject(JWinProject project) {
		logStd("Loading project...");
		new Thread(() -> {
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
			runOnUiThread(() -> console.checkedProperty().set(project.isConsole() != null && project.isConsole()));
			runOnUiThread(() -> admin.checkedProperty().set(project.isAdmin() != null && project.isAdmin()));
			runOnUiThread(() -> guid.setValue(project.getGuid()));
			project.getManualJars().forEach(f -> runOnUiThread(() -> dependencies.addManualJar(f)));

			runOnUiThread(() -> moreSettings.setFileTypeAssociation(project.getFileTypeAsso()));
			runOnUiThread(() -> moreSettings.setUrlProtocolAssociation(project.getUrlProtocolAsso()));

			runOnUiThread(() -> {
				logStd("resolving dependencies...");
				dependencies.resolve(classpath::getPom, this, false, this::postImport);
			});

			projectInUse = project;
		}).start();
	}

	private AtomicBoolean ran = new AtomicBoolean(false);

	private boolean stopped = false;
	
	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}

	public void run(Process p, Runnable showLog, StringBuilder errBuilder) {
		Platform.runLater(() -> {
			preRun(p);
		});
		int exit = 0;
		try {
			exit = p.waitFor();
			if ((exit != 0 && !stopped) || !errBuilder.isEmpty()) {
				ran.set(false);
				Semaphore s = new Semaphore(0);
				JwinActions.alert((exit != 0 ? "Non 0 exit code" : "Error Logs"),
						"your project might not have executed correctly, "
								+ (exit != 0 ? ("it exited with code " + exit) : "the error logs were not empty"),
						AlertType.INFO, res -> {
							if (res == ButtonType.VIEW_LOG) {
								showLog.run();
							}
							if (res == ButtonType.IGNORE) {
								ran.set(true);
							}
						}, () -> s.release(), ButtonType.VIEW_LOG, ButtonType.IGNORE);

				s.acquireUninterruptibly();
			} else {
				ran.set(true);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}

		stopped = false;
		final int fe = exit;
		Platform.runLater(() -> {
			postRun(ran.get());
			setState("idle");
			logStd("your app exited with code " + fe);
		});
	}

	public void stop(Process p) {
		stopped = true;
		p.descendants().forEach(ProcessHandle::destroyForcibly);
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

	public boolean saveAs() {
		JWinProject project = export();
		saver.setInitialFileName(appName.getValue() + "_jWin_Project");
		File saveTo = saver.showSaveDialog(ps);
		if (saveTo != null) {
			FileDealer.write(project.serialize(), saveTo);
			projectInUse = project;
			fileInUse = saveTo;
			return true;
		}
		return false;
	}
	
	public void save() {
		JWinProject project = export();
		if(fileInUse != null) {
			FileDealer.write(project.serialize(), fileInUse);
		}
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

	public static Pane vSpace() {
		Pane space = new Pane();
		VBox.setVgrow(space, Priority.ALWAYS);
		return space;
	}

	public void setOnCompile(Runnable onCompile) {
		this.onCompile = onCompile;
	}

	public void setOnRun(Runnable onRun) {
		this.onRun = onRun;
	}

	public void onErr() {
		disable(false, false);
		setState("Failed to run :(");
		setProgress(-1);
	}

	public abstract void preImport();

	public abstract void postImport();

	public abstract void preRun(Process p);

	public abstract void postRun(boolean ran);

	public abstract void disable(boolean b, boolean ran);

	public abstract void setProgress(double val);

	public abstract void setState(String val);

	public abstract void incrementProgress(double max);
	
	public abstract void logStd(String line);
	
	public abstract void logErr(String line);
}
