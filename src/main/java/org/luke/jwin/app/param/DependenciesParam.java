package org.luke.jwin.app.param;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.luke.jwin.app.Command;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class DependenciesParam extends Param {

	private ArrayList<File> jars;

	public DependenciesParam(Supplier<List<File>> pomSupplier) {
		super("Dependencies");

		jars = new ArrayList<>();

		addButton("resolve", e -> {
			list.getChildren().clear();
			jars.clear();
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

						Command command = new Command("cmd.exe", "/C", "mvn dependency:copy-dependencies -DoutputDirectory=\""
										+ temp.getAbsolutePath() + "\" -Dhttps.protocols=TLSv1.2");

						try {
							command.execute(pom.getParentFile()).waitFor();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
							Thread.currentThread().interrupt();
						}

						for (File f : temp.listFiles()) {
							Platform.runLater(() -> {
								jars.add(f);
								addFile(f, f.getName());
							});
						}
					});

					Platform.runLater(() -> {
						stopLoading();
					});
				}).start();

			}
		});
	}

	public List<File> getJars() {
		return jars;
	}

}
