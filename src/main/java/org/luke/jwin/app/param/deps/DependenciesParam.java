package org.luke.jwin.app.param.deps;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;

import org.luke.gui.controls.tab.TabPane;
import org.luke.gui.window.Window;
import org.luke.jwin.app.Command;
import org.luke.jwin.app.JwinActions;
import org.luke.jwin.app.display.JwinUi;
import org.luke.jwin.app.file.FileDealer;
import org.luke.jwin.app.param.Param;
import javafx.application.Platform;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class DependenciesParam extends Param {

	private TabPane disp;
	
	private DepTab resolved;
	private DepTab manual;

	private boolean resolving = false;

	private FileChooser fc;
	public DependenciesParam(Window ps, Supplier<List<File>> pomSupplier, JwinUi config) {
		super(ps, "Dependencies");
		VBox.setVgrow(this, Priority.SOMETIMES);

		disp = new TabPane(ps);
		VBox.setVgrow(listCont, Priority.ALWAYS);
		
		resolved = new DepTab(this, "Resolved Dependencies");
		manual = new DepTab(this, "Manual Dependencies");
		
		disp.addTab(resolved);
		disp.addTab(manual);
	
		list.getChildren().add(disp);

		fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("jar files", "*.jar"));
		addButton(ps, "add jars", () -> {
			List<File> files = fc.showOpenMultipleDialog(ps);
			if (files != null && !files.isEmpty()) {
				files.forEach(this::addManualJar);
				disp.select(manual);
			}
		});
		
		disp.select(manual);

		addButton(ps, "resolve", () -> resolve(pomSupplier, config, true));
		
		listCont.getChildren().remove(sp);
		listCont.getChildren().add(disp);
	}
	
	public void addJars(JwinUi config) {
		List<File> files = fc.showOpenMultipleDialog(getWindow());
		if (files != null && !files.isEmpty()) {
			files.forEach(f -> {
				addManualJar(f);
				config.logStd(f.getName() + " was added to the project's dependencies");
			});
			disp.select(manual);
		}
	}

	public void resolve(Supplier<List<File>> pomSupplier, JwinUi config, boolean alert) {
		resolve(pomSupplier, config, alert, null);
	}

	public void resolve(Supplier<List<File>> pomSupplier, JwinUi config, boolean alert, Runnable onFinish) {
		resolved.clear();
		List<File> poms = pomSupplier.get();
		if (poms.isEmpty()) {
			if (alert) {
				JwinActions.warn("Failed to resolve dependencies", "no pom.xml files found for the selected classpath");
			}

			if(onFinish != null) {
				Platform.runLater(onFinish);
			}
		} else {
			File dk = config.getJdk().getValue();

			if (dk == null) {
				config.logStd("jdk was not set, attempting to detect from system...");
				File f = config.getJdk().detectJdk();
				if (f != null) {
					final Long key = new Random().nextLong();
					config.getJdk().set(f, " (found in your system)", () -> {
						config.logStd("jdk " + config.getJdk().getVersion() + " was detected, using that...");
						Platform.exitNestedEventLoop(key, null);
					});
					Platform.enterNestedEventLoop(key);
				} else {
					JwinActions.error("Missing Jdk", "You didn't select the jdk to compile your application");
					return;
				}
			}
			
			startLoading();
			new Thread(() -> {
				resolving = true;
				
				Consumer<File> pomResolver = pom -> {
					File temp = new File(
							System.getProperty("java.io.tmpdir") + "/jwin_lib_dep_" + new Random().nextInt(999999));
					temp.mkdir();

					JwinActions.deleteDirOnShutdown(temp);

					File mvn = new File(URLDecoder.decode(getClass().getResource("/mvn/bin/mvn.cmd").getFile(),
							Charset.defaultCharset()));

					File mvnRoot = new File(
							System.getProperty("java.io.tmpdir") + "/jwin_mvn_root_" + new Random().nextInt(9999));
					mvnRoot.mkdir();
					JwinActions.deleteDirOnShutdown(mvnRoot);
					
					JwinActions.copyDirCont(mvn.getParentFile().getParentFile(), mvnRoot, null);
					
					File tempMvn = new File(mvnRoot.getAbsolutePath().concat("/bin/mvn.cmd"));
					
					String toReplace = "@REM ==== START VALIDATION ====";
					FileDealer.write(
							FileDealer.read(tempMvn).replace(toReplace,
									toReplace + "\nset JAVA_HOME=" + config.getJdk().getValue().getAbsolutePath()),
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
				};
				
				try {
					poms.forEach(pomResolver);
				}catch(Exception x) {
					x.printStackTrace();
					JwinActions.error("Operation Failed", "Failed to resolve dependencies, make sure the jdk you selected is valid");
				}

				Platform.runLater(() -> {
					stopLoading();
					disp.select(resolved);
				});
				resolving = false;

				if(onFinish != null) {
					Platform.runLater(onFinish);
				}
			}).start();
		}
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
			
			final int fi = i;
			if (onProgress != null) {
				onProgress.accept((fi / (double) deps.size()) * .2);
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

}
