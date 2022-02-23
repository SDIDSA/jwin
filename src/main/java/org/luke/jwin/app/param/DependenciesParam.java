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
import org.luke.jwin.app.Jwin;
import org.luke.jwin.app.file.FileDealer;
import org.luke.jwin.app.utils.Backgrounds;
import org.luke.jwin.app.utils.Borders;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class DependenciesParam extends Param {

	private ArrayList<File> manualJars;
	private ArrayList<File> resolvedJars;

	private Accordion disp;
	private TitledPane resolvedDisp;
	private TitledPane manualDisp;

	private VBox resolvedList;
	private VBox manualList;

	private boolean resolving = false;

	public DependenciesParam(Stage ps, Supplier<List<File>> pomSupplier, JdkParam jdk) {
		super("Dependencies");

		list.setPadding(Insets.EMPTY);
		sp.setBorder(null);
		list.setBackground(null);
		VBox.setVgrow(this, Priority.ALWAYS);

		manualJars = new ArrayList<>();
		resolvedJars = new ArrayList<>();

		resolvedList = new VBox(5);
		resolvedList.setBackground(Backgrounds.make(Color.WHITE));
		resolvedDisp = new TitledPane("Resolved Dependencies", resolvedList);
		style(resolvedDisp);

		manualList = new VBox(5);
		manualList.setBackground(Backgrounds.make(Color.WHITE));
		manualDisp = new TitledPane("Manual Dependencies", manualList);
		style(manualDisp);

		disp = new Accordion(resolvedDisp, manualDisp);
		disp.setBorder(Borders.make(Color.LIGHTGRAY));
		disp.setBackground(Backgrounds.make(Color.WHITE));

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

		addButton("resolve", e -> resolve(pomSupplier, jdk, true));
	}

	public void resolve(Supplier<List<File>> pomSupplier, JdkParam jdk, boolean alert) {
		resolvedList.getChildren().clear();
		resolvedJars.clear();
		List<File> poms = pomSupplier.get();
		if (poms.isEmpty()) {
			if (alert) {
				Alert al = new Alert(AlertType.WARNING);
				al.setContentText("no pom.xml files found for the selected classpath");
				al.showAndWait();
			}
		} else {
			startLoading();
			new Thread(() -> {
				resolving = true;
				poms.forEach(pom -> {
					File temp = new File(
							System.getProperty("java.io.tmpdir") + "/lib_dep_" + new Random().nextInt(999999));
					temp.mkdir();

					Jwin.deleteDirOnShutdown(temp);

					File mvn = new File(URLDecoder.decode(getClass().getResource("/mvn/bin/mvn.cmd").getFile(),
							Charset.defaultCharset()));

					File mvnRoot = new File(
							System.getProperty("java.io.tmpdir") + "/mvn_root_" + new Random().nextInt(9999));
					mvnRoot.mkdir();
					Jwin.deleteDirOnShutdown(mvnRoot);
					
					Jwin.copyDirCont(mvn.getParentFile().getParentFile(), mvnRoot, null);
					
					File tempMvn = new File(mvnRoot.getAbsolutePath().concat("/bin/mvn.cmd"));
					
					String toReplace = "@REM ==== START VALIDATION ====";
					FileDealer.write(
							FileDealer.read(tempMvn).replace(toReplace,
									toReplace + "\nset JAVA_HOME=" + jdk.getValue().getAbsolutePath()),
							new File(tempMvn.getAbsolutePath().replace("mvn.cmd", "cmvn.cmd")));
					Command command = new Command("cmd.exe", "/C",
							"cmvn -f \"" + pom.getAbsolutePath()
									+ "\" dependency:copy-dependencies -DoutputDirectory=\"" + temp.getAbsolutePath()
									+ "\" -Dhttps.protocols=TLSv1.2");

					try {
						command.execute(tempMvn.getParentFile()).waitFor();
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
				resolving = false;
			}).start();
		}
	}

	public boolean isResolving() {
		return resolving;
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
		if (!jar.exists()) {
			return;
		}
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
			if (progress != null) {
				Platform.runLater(() -> progress.setProgress((fi / (double) deps.size()) * .1));
			}
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

	private void style(TitledPane tp) {
		Platform.runLater(() -> {
			Region content = (Region) tp.getChildrenUnmodifiable().get(0);
			Region title = (Region) tp.getChildrenUnmodifiable().get(1);

			title.setBackground(Backgrounds.make(Color.WHITE));
			content.setBorder(Borders.make(Color.LIGHTGRAY, new BorderWidths(1, 0, 0, 0)));
		});
	}

}
