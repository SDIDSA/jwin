package org.luke.jwin.app.param;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.luke.jwin.app.Jwin;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class JreParam extends JavaParam {

	public JreParam(Stage ps) {
		super("Jre (will be packed with your app)");

		DirectoryChooser dc = new DirectoryChooser();
		addButton("directory", e -> {
			File dir = dc.showDialog(ps);
			if (dir != null) {
				set(dir);
			}
		});

		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("archive", "*.zip"));
		addButton("archive", e -> {
			File file = fc.showOpenDialog(ps);
			if (file != null) {
				set(file);
			}
		});
	}
	
	public void copy(File preBuild, ProgressBar progress) {
		File preBuildRt = new File(preBuild.getAbsolutePath().concat("/rt"));
		preBuildRt.mkdir();
		if (value.isFile()) {
			try {
				ZipFile zip = new ZipFile(value);

				String[] rootEntryPath = new String[1];

				Iterator<? extends ZipEntry> it = zip.entries().asIterator();
				int entryCount = 0;
				while (it.hasNext()) {
					entryCount++;
					Path path = Path.of(it.next().getName());
					if (path.endsWith("java.exe")) {
						String rootPath = "";
						for (int i = 0; i < path.getNameCount() - 2; i++) {
							rootPath = rootPath.concat(path.getName(i).toString()).concat("/");
						}
						rootEntryPath[0] = rootPath;
					}
				}

				String rootPath = rootEntryPath[0];
				if (rootPath != null) {
					final int ec = entryCount;
					int[] copyCount = new int[] { 0 };
					zip.entries().asIterator().forEachRemaining(entry -> {
						copyCount[0]++;
						String newName = entry.getName().replace(rootPath, "");

						if (!newName.isBlank()) {
							if (entry.isDirectory()) {
								File entryDir = new File(
										preBuildRt.getAbsolutePath().concat("/").concat(newName));
								entryDir.mkdir();
							} else {
								try {
									InputStream src = zip.getInputStream(entry);
									FileOutputStream dest = new FileOutputStream(
											preBuildRt.getAbsolutePath().concat("/").concat(newName));
									dest.write(src.readAllBytes());
									dest.close();
								} catch (IOException x) {
									x.printStackTrace();
								}
							}
						}

						Platform.runLater(
								() -> progress.setProgress(.2 + (copyCount[0] / (double) ec) * .2));
					});
				}

				zip.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else {
			int count = Jwin.countDir(value);
			int[] copyCount = new int[] { 0 };
			Jwin.copyDirCont(value, preBuildRt, () -> {
				copyCount[0]++;
				Platform.runLater(() -> progress.setProgress(.2 + (copyCount[0] / (double) count) * .2));
			});
		}
	}

}
