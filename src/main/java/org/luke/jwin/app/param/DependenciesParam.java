package org.luke.jwin.app.param;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.luke.jwin.app.Command;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;

public class DependenciesParam extends Param {

	private ArrayList<File> manualJars;
	private ArrayList<File> resolvedJars;

	private Accordion disp;
	private TitledPane resolvedDisp;
	private TitledPane manualDisp;

	private VBox resolvedList;
	private VBox manualList;

	public DependenciesParam(Stage ps, Supplier<List<File>> pomSupplier) {
		super("Dependencies");

		list.setPadding(Insets.EMPTY);
		sp.setBorder(null);
		list.setBackground(null);
		VBox.setVgrow(this, Priority.ALWAYS);

		manualJars = new ArrayList<>();
		resolvedJars = new ArrayList<>();

		resolvedList = new VBox(5);
		resolvedDisp = new TitledPane("Resolved Dependencies", resolvedList);

		manualList = new VBox(5);
		manualDisp = new TitledPane("Manual Dependencies", manualList);

		disp = new Accordion(resolvedDisp, manualDisp);

		list.getChildren().add(disp);

		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("jar files", "*.jar"));
		addButton("add jars", e -> {
			List<File> files = fc.showOpenMultipleDialog(ps);
			if (files != null && !files.isEmpty()) {
				files.forEach(this::addManualJar);
				disp.setExpandedPane(manualDisp);
			}
		});

		addButton("resolve", e -> {
			resolve(pomSupplier);
		});
	}

	public void resolve(Supplier<List<File>> pomSupplier) {
		resolvedList.getChildren().clear();
		resolvedJars.clear();
		List<File> poms = pomSupplier.get();
		if (poms.isEmpty()) {
			Alert al = new Alert(AlertType.WARNING);
			al.setContentText("no pom.xml files found for the selected classpath");
			al.showAndWait();
		} else {
			startLoading();
			new Thread(() -> {
				poms.forEach(pom -> {
					File temp = new File(
							System.getProperty("java.io.tmpdir") + "/lib_dep_" + new Random().nextInt(999999));
					temp.mkdir();

					File mvn = new File(URLDecoder.decode(getClass().getResource("/mvn/bin/mvn").getFile(), Charset.defaultCharset()));
					
					Command command = new Command("cmd.exe", "/C",
							"mvn -f \"" + pom.getAbsolutePath() + "\" dependency:copy-dependencies -DoutputDirectory=\"" + temp.getAbsolutePath()
									+ "\" -Dhttps.protocols=TLSv1.2");

					try {
						command.execute(mvn.getParentFile()).waitFor();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
						Thread.currentThread().interrupt();
					}

					for (File f : temp.listFiles()) {
						Platform.runLater(() -> addResolvedJar(f));
					}
				});

				Platform.runLater(() -> {
					stopLoading();
					disp.setExpandedPane(resolvedDisp);
				});
			}).start();
		}
	}

	public void addResolvedJar(File jar) {
		resolvedJars.add(jar);

		Hyperlink remove = new Hyperlink("remove");

		HBox line = generateLine(jar, jar.getName(), remove);

		resolvedList.getChildren().add(line);

		remove.setOnAction(ev -> {
			resolvedList.getChildren().remove(line);
			resolvedJars.remove(jar);
		});

		if (disp.getExpandedPane() != resolvedDisp) {
			disp.setExpandedPane(resolvedDisp);
		}
	}

	public void addManualJar(File jar) {
		manualJars.add(jar);
		Hyperlink remove = new Hyperlink("remove");
		HBox line = generateLine(jar, jar.getName(), remove);
		manualList.getChildren().add(line);
		remove.setOnAction(ev -> {
			manualList.getChildren().remove(line);
			manualJars.remove(jar);
		});

		if (disp.getExpandedPane() != manualDisp) {
			disp.setExpandedPane(manualDisp);
		}
	}

	public List<File> getJars() {
		ArrayList<File> allJars = new ArrayList<>();
		allJars.addAll(manualJars);
		allJars.addAll(resolvedJars);

		return allJars;
	}

	public List<File> getResolvedJars() {
		return resolvedJars;
	}

	public List<File> getManualJars() {
		return manualJars;
	}

	public File copy(File preBuild, ProgressBar progress) {
		List<File> deps = getJars();

		File preBuildLibs = new File(preBuild.getAbsolutePath().concat("/lib"));
		preBuildLibs.mkdir();

		for (int i = 0; i < deps.size(); i++) {
			File dep = deps.get(i);
			try {
				Files.copy(dep.toPath(), Path.of(preBuildLibs.getAbsolutePath().concat("/").concat(dep.getName())));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			final int fi = i;
			Platform.runLater(() -> progress.setProgress((fi / (double) deps.size()) * .2));
		}

		return preBuildLibs;
	}

	@Override
	public void clear() {
		manualJars.clear();
		resolvedJars.clear();

		manualList.getChildren().clear();
		resolvedList.getChildren().clear();
	}

}
