package org.luke.jwin.app.layout;

import java.io.File;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.scene.Node;
import org.luke.gui.controls.alert.AlertType;
import org.luke.gui.controls.alert.ButtonType;
import org.luke.gui.controls.alert.Overlay;
import org.luke.gui.controls.check.KeyedCheck;
import org.luke.gui.controls.space.ExpandingVSpace;
import org.luke.gui.exception.ErrorHandler;
import org.luke.gui.locale.Locale;
import org.luke.gui.window.Page;
import org.luke.gui.window.Window;
import org.luke.jwin.app.Jwin;
import org.luke.jwin.app.JwinActions;
import org.luke.jwin.app.file.FileDealer;
import org.luke.jwin.app.file.JWinProject;
import org.luke.jwin.app.layout.ui2.JwinUi2;
import org.luke.jwin.app.more.MoreSettings;
import org.luke.jwin.app.param.*;
import org.luke.jwin.app.param.deps.DependenciesParam;
import org.luke.jwin.app.param.main.MainClassParam;
import org.luke.jwin.app.param.rootFiles.RootFilesParam;
import org.luke.jwin.ui.TextField;
import org.luke.jwin.ui.TextVal;

import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
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
	protected RootFilesParam rootFiles;
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

		classpath = new ClasspathParam(ps.getWindow(), null);
		rootFiles = new RootFilesParam(ps.getWindow(), this);

		mainClass = new MainClassParam(ps, classpath::listClasses);

		jdk = new JdkParam(ps.getWindow());

		dependencies = new DependenciesParam(ps.getWindow(), classpath::getRoot, this);

		jre = new JreParam(ps.getWindow(), this);

		icon = new IconParam(ps.getWindow());

		appName = new TextVal(ps.getWindow(), "app_name");
		version = new TextVal(ps.getWindow(), "app_version");
		publisher = new TextVal(ps.getWindow(), "app_publisher");

		console = new KeyedCheck(ps.getWindow(), "console", 16);
		admin = new KeyedCheck(ps.getWindow(), "admin", 16);

		guid = new TextField(ps.getWindow());
		guid.setEditable(false);
		HBox.setHgrow(guid, Priority.ALWAYS);

		moreSettings = new MoreSettings(ps);

		saver = new FileChooser();
		saver.getExtensionFilters().add(new ExtensionFilter("jWin Project", "*.jwp"));

		dsaver = new DirectoryChooser();
	}

	public JwinUi(JwinUi1 source) {
		this((JwinUi) source);
	}

	public JwinUi(JwinUi2 source) {
		this((JwinUi) source);
	}

	public JwinUi(JwinUi source) {
		this.ps = source.ps;

		classpath = source.classpath;
		rootFiles = source.rootFiles;
		mainClass = source.mainClass;
		jdk = source.jdk;

		dependencies = source.dependencies;
		jre = source.jre;

		icon = source.icon;
		appName = source.appName;
		version = source.version;
		publisher = source.publisher;

		console = source.console;
		admin = source.admin;

		guid = source.guid;

		moreSettings = source.moreSettings;
		saver = source.saver;
		dsaver = source.dsaver;

		onRun = source.onRun;
		onCompile = source.onCompile;
		fileInUse = source.fileInUse;
		projectInUse = source.projectInUse;
		stopped = source.stopped;
	}

	public JWinProject export() {
		return new JWinProject(this);
	}

	public ClasspathParam getClasspath() {
		return classpath;
	}

	public RootFilesParam getRootFiles() {
		return rootFiles;
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

	public void importJavaProject(File loadFrom) {
		preImport();
		fileInUse = null;
		JWinProject proj = JWinProject.fromJavaProject(loadFrom);
		loadProject(proj);
	}

	public void loadProject(JWinProject project) {
		projectInUse = project;
		Page p = Jwin.winstance.getLoadedPage();
		if(p != null) {
			for(Node node : p.getChildren()) {
				if(node instanceof Overlay ov) ov.hide();
			}
		}
		clearLogs();
		logStd("loading_project");
		setState("loading_project");
		rootFiles.getInclude().clear();
		rootFiles.getExclude().clear();
		console.unset();
		new Thread(() -> {
			runOnUiThread(Param::clearAll);
			project.getClasspath().forEach(f -> runOnUiThread(() -> classpath.add(f)));
			runOnUiThread(() -> classpath.setRoot(project.getRoot()));
			runOnUiThread(() -> rootFiles.getInclude().addAll(project.getRootFilesInclude()));
			runOnUiThread(() -> rootFiles.getExclude().addAll(project.getRootFilesExclude()));
			runOnUiThread(() -> rootFiles.getRun().addAll(project.getRootFilesRun()));
			mainClass.setAltMain(null);
			runOnUiThread(() -> mainClass.set(project.getMainClass()));
			runOnUiThread(() -> jdk.setFile(project.getJdk()));
			runOnUiThread(() -> jre.setFile(project.getJre()));
			runOnUiThread(() -> icon.set(project.getIcon()));
			runOnUiThread(() -> appName.setValue(project.getAppName()));
			runOnUiThread(() -> version.setValue(project.getAppVersion()));
			runOnUiThread(() -> publisher.setValue(project.getAppPublisher()));
			runOnUiThread(() -> jre.setJvmArgs(project.getJvmArgs()));
			if(project.isConsole() != null) {
				runOnUiThread(() -> {
					console.set(project.isConsole());
				});
			}
			runOnUiThread(() -> admin.property().set(project.isAdmin() != null && project.isAdmin()));
			runOnUiThread(() -> guid.setValue(project.getGuid()));
			project.getManualJars().forEach(f -> runOnUiThread(() -> dependencies.addManualJar(f)));

			runOnUiThread(() -> moreSettings.setFileTypeAssociation(project.getFileTypeAsso()));
			runOnUiThread(() -> moreSettings.setUrlProtocolAssociation(project.getUrlProtocolAsso()));

			runOnUiThread(() -> {
				setState("resolving_dependencies");
				dependencies.resolve(classpath.getRoot(), (r) -> {
					postImport(r);
					setState("idle");
				});
			});
		}).start();
	}

	private final AtomicBoolean ran = new AtomicBoolean(false);

	private boolean stopped = false;

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
				JwinActions.alert((exit != 0 ? "non_0_exit_code_head" : "error_logs_head"),
						exit != 0 ? Locale.key("non_0_exit_code_body", "code", exit) : "error_logs_body",
						AlertType.INFO, res -> {
							if (res == ButtonType.VIEW_LOG) {
								showLog.run();
							}
							if (res == ButtonType.IGNORE) {
								ran.set(true);
							}
						}, s::release, ButtonType.VIEW_LOG, ButtonType.IGNORE);

				s.acquireUninterruptibly();
			} else {
				ran.set(true);
			}
		} catch (InterruptedException e) {
			ErrorHandler.handle(e, "run application");
			Thread.currentThread().interrupt();
		}

		stopped = false;
		final int fe = exit;
		Platform.runLater(() -> {
			postRun(ran.get());
			setState("idle");
			logStd(Locale.key("exited_with_code", "code", fe));
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
		if (fileInUse != null) {
			FileDealer.write(project.serialize(), fileInUse);
			projectInUse = project;
		}
	}

	private static void runOnUiThread(Runnable r) {
		Platform.runLater(r);
		sleep();
	}

	private static void sleep() {
		try {
			Thread.sleep(50);
		} catch (InterruptedException e1) {
			ErrorHandler.handle(e1, "delay operation");
			Thread.currentThread().interrupt();
		}
	}

	public static Pane vSpace() {
		return new ExpandingVSpace();
	}

	public void setOnCompile(Runnable onCompile) {
		this.onCompile = onCompile;
	}

	public void setOnRun(Runnable onRun) {
		this.onRun = onRun;
	}

	public void onErr() {
		disable(false, false);
		setState("failed_to_run");
		setProgress(-1);
	}
	
	public void separate() {
		logStd("console_hr");
	}

	public abstract void preImport();

	public abstract void postImport(boolean success);

	public abstract void preRun(Process p);

	public abstract void postRun(boolean ran);

	public abstract void disable(boolean b, boolean ran);

	public abstract void setProgress(double val);

	public abstract void setState(String val);

	public abstract void incrementProgress(double max);

	public abstract void logStd(String line);

	public abstract void logStd(String line, boolean keyed);

	public abstract void logStdOver(String line);

	public abstract void logErr(String line);

	public abstract void clearLogs();
}
