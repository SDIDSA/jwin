package org.luke.jwin.app.param.deps;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;

import org.luke.gui.controls.tab.TabPane;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.factory.Borders;
import org.luke.gui.locale.Locale;
import org.luke.gui.style.Style;
import org.luke.gui.window.Window;
import org.luke.jwin.app.Jwin;
import org.luke.jwin.app.layout.JwinUi;
import org.luke.jwin.app.param.Param;
import javafx.application.Platform;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class DependenciesParam extends Param {

	private final TabPane disp;

	private final DepTab resolved;
	private final DepTab manual;

	private boolean resolving = false;

	private final FileChooser fc;

	public DependenciesParam(Window ps, Supplier<File> rootSupplier, JwinUi config) {
		super(ps, "dependencies");
		VBox.setVgrow(this, Priority.SOMETIMES);

		disp = new TabPane(ps);
		VBox.setVgrow(listCont, Priority.ALWAYS);

		resolved = new DepTab(this, "resolved_dependencies");
		manual = new DepTab(this, "manual_dependencies");

		disp.addTab(resolved);
		disp.addTab(manual);

		list.getChildren().add(disp);

		fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("jar files", "*.jar"));
		addButton(ps, "add_jars", () -> {
			List<File> files = fc.showOpenMultipleDialog(ps);
			if (files != null && !files.isEmpty()) {
				files.forEach(this::addManualJar);
				disp.select(manual);
			}
		});

		disp.select(manual);

		addButton(ps, "resolve", () -> resolve(rootSupplier.get()));

		listCont.getChildren().remove(sp);
		listCont.getChildren().add(disp);

		applyStyle(ps.getStyl());
	}

	public void addJars(JwinUi config) {
		List<File> files = fc.showOpenMultipleDialog(getWindow());
		if (files != null && !files.isEmpty()) {
			files.forEach(f -> {
				addManualJar(f);
				config.logStd(Locale.key("jar_added", "name", f.getName()));
			});
			disp.select(manual);
		}
	}

	private final Consumer<Boolean> defOnFin = (r) -> {
		if (r) {
			Jwin.instance.getConfig()
					.logStd(Locale.key("finished_resolving", "count", getResolvedJars().size()));
		} else {
			Jwin.instance.getConfig().logErr("failed_to_resolve");
		}
	};

	public void resolve(File root) {
		resolve(root, null);
	}

	public void resolve(File root, Consumer<Boolean> onFinish) {
		Jwin.instance.getConfig().logStd("resolving_dependencies");

		resolved.clear();

		if (root == null) {
			Jwin.instance.getConfig().logErr("no_build_tool");
			defOnFin.accept(false);
			if (onFinish != null)
				onFinish.accept(false);

			return;
		}

		File pom = pom(root);
		File grad = grad(root);
		if (pom.exists()) {
			Jwin.instance.getConfig().logStd(Locale.key("detected_file", "file", "pom.xml"));
			resolveMaven(pom, onFinish);
		} else if(grad.exists()){
			Jwin.instance.getConfig().logStd(Locale.key("detected_file", "file", grad.getName()));
			resolveGradle(grad, onFinish);
		}else {
			Jwin.instance.getConfig().logErr("no_build_tool");
			defOnFin.accept(false);
			if (onFinish != null)
				onFinish.accept(false);
		}
	}

	private void resolveMaven(File pom, Consumer<Boolean> onFinish) {
		startLoading();

		new Thread(() -> {
			resolving = true;

			List<File> jars = MavenResolver.resolve(pom);

			if (jars != null) {
				Platform.runLater(() -> jars.forEach(resolved::addJar));
			}else {
				onFinish.accept(false);
				return;
			}
			resolving = false;

			Platform.runLater(() -> {
				stopLoading();
				disp.select(resolved);
			});

			Platform.runLater(() -> {
				defOnFin.accept(true);
			});
			if (onFinish != null) {
				Platform.runLater(() -> onFinish.accept(true));
			}
		}, "maven resolver").start();
	}

	private void resolveGradle(File grad, Consumer<Boolean> onFinish) {
		startLoading();

		new Thread(() -> {
			resolving = true;

			List<File> jars = GradleResolver.resolve(grad);

			if (jars != null) {
				Platform.runLater(() -> jars.forEach(resolved::addJar));
			} else {
				onFinish.accept(false);
				return;
			}
			resolving = false;

			Platform.runLater(() -> {
				stopLoading();
				disp.select(resolved);
			});

			Platform.runLater(() -> {
				defOnFin.accept(true);
			});
			if (onFinish != null) {
				Platform.runLater(() -> onFinish.accept(true));
			}
		}, "gradle resolver").start();
	}

	private File pom(File root) {
		return new File(root.getAbsolutePath() + "\\pom.xml");
	}

	private File grad(File root) {
		File grv = new File(root.getAbsolutePath() + "\\build.gradle");
		File ktl = new File(root.getAbsolutePath() + "\\build.gradle.kts");

		return grv.exists() ? grv : ktl;
	}

	public boolean isResolving() {
		return resolving;
	}

	public void addResolvedJar(File jar) {
		resolved.addJar(jar);
	}

	public void addManualJar(File jar) {
		manual.addJar(jar);
	}

	public List<File> getJars() {
		ArrayList<File> allJars = new ArrayList<>(getResolvedJars());
		allJars.addAll(getManualJars());

		return allJars;
	}

	public List<File> getResolvedJars() {
		return resolved.getFiles();
	}

	public List<File> getManualJars() {
		return manual.getFiles();
	}

	public File copy(File preBuild, DoubleConsumer onProgress) throws IOException {
		List<File> deps = getJars();

		File preBuildLibs = new File(preBuild.getAbsolutePath().concat("/lib"));
		preBuildLibs.mkdir();

		for (int i = 0; i < deps.size(); i++) {
			File dep = deps.get(i);

			Files.copy(dep.toPath(), Path.of(preBuildLibs.getAbsolutePath().concat("/").concat(dep.getName())));

            if (onProgress != null) {
				onProgress.accept((i / (double) deps.size()) * .2);
			}
		}

		return preBuildLibs;
	}

	public void clearManuals() {
		manual.clear();
	}

	@Override
	public void clear() {
		resolved.clear();
		manual.clear();
	}

	@Override
	public void applyStyle(Style style) {
		super.applyStyle(style);

		listCont.setBackground(Backgrounds.make(style.getBackgroundTertiaryOr(), 5.0));
		listCont.setBorder(Borders.make(style.getDeprecatedTextInputBorder(), 5.0));
	}

}
