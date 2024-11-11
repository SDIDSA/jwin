package org.luke.gui.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.luke.gui.exception.ErrorHandler;
import org.luke.gui.window.Window;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
/**
 * Utility class for reading and writing files, and selecting files in a GUI.
 * Provides methods for reading file contents, writing to files, and selecting image files.
 * 
 * @author SDIDSA
 */
public class FileUtils {
	private FileUtils() {

	}

	/**
	 * Reads the contents of a resource file located at the specified path.
	 *
	 * @param path The path of the resource file.
	 * @return The content of the resource file as a string.
	 */
	public static String readFile(String path) {
		StringBuilder sb = new StringBuilder();

		try (BufferedReader br = new BufferedReader(new InputStreamReader(FileUtils.class.getResourceAsStream(path)))) {
			String line;
			boolean first = true;
			while ((line = br.readLine()) != null) {
				if (first) {
					first = false;
				} else {
					sb.append("\n");
				}
				sb.append(line);
			}
		} catch (Exception x) {
			ErrorHandler.handle(x, "read file " + path);
		}

		return sb.toString();
	}

	/**
	 * Reads the contents of the specified file.
	 *
	 * @param file The file to read.
	 * @return The content of the file as a string.
	 */
	public static String readFile(File file) {
		StringBuilder sb = new StringBuilder();

		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
			String line;
			boolean first = true;
			while ((line = br.readLine()) != null) {
				if (first) {
					first = false;
				} else {
					sb.append("\n");
				}
				sb.append(line);
			}
		} catch (Exception x) {
			ErrorHandler.handle(x, "read file " + file.getAbsolutePath());
		}

		return sb.toString();
	}

	/**
	 * Writes the given string to the specified file.
	 *
	 * @param f      The file to write to.
	 * @param string The string to write.
	 * @throws IOException If an I/O error occurs while writing the file.
	 */
	public static void write(File f, String string) throws IOException {
		try (BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
			bw.write(string);
		}
	}

	/**
	 * Opens a file chooser dialog to select an image file.
	 *
	 * @param window The parent window for the file chooser dialog.
	 * @return The selected image file, or null if no file is selected.
	 */
	public static File selectImage(Window window) {
		try {
			return selectFile(window, "Image", "*.png", "*.jpg");
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Opens a file chooser dialog to select a file with the specified type and
	 * extensions.
	 *
	 * @param window     The parent window for the file chooser dialog.
	 * @param type       The type of the file (e.g., "Image").
	 * @param extensions The allowed file extensions.
	 * @return The selected file, or null if no file is selected.
	 */
	private static File selectFile(Window window, String type, String... extensions) {
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter(type, extensions));
		return fc.showOpenDialog(window);
	}
}
