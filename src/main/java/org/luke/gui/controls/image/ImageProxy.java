package org.luke.gui.controls.image;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import net.coobird.thumbnailator.makers.FixedSizeThumbnailMaker;
import net.coobird.thumbnailator.resizers.DefaultResizerFactory;
import net.coobird.thumbnailator.resizers.Resizer;

/**
 * The {@code ImageProxy} class provides utility methods for loading, caching,
 * and resizing images in a JavaFX application.
 *
 * @author SDIDSA
 */
public class ImageProxy {
	private static HashMap<String, Image> cache = new HashMap<>();

	private ImageProxy() {

	}

	/**
	 * Loads an image based on the given name, size, and whether it's a full path or
	 * a resource path within the JAR.
	 *
	 * @param name     The name or path of the image.
	 * @param size     The desired size of the image.
	 * @param fullPath Indicates whether the name is a full path (true) or a
	 *                 resource path (false).
	 * @return An {@code Image} object.
	 */
	public static Image load(String name, double size, boolean fullPath) {
		String path = fullPath ? name
				: new StringBuilder().append("/images/icons/").append(name).append('_').append((int) size)
						.append(".png").toString();

		Image found = cache.get(size + "_" + path);

		if (found == null) {
			if (fullPath) {
				found = new Image(path);
			} else {
				found = new Image(ImageProxy.class.getResourceAsStream(path));
			}

			if (found.getHeight() != size) {
				found = resize(found, size);
			}
			cache.put(size + "_" + path, found);
		}

		return found;
	}

	/**
	 * Asynchronously loads an image from a URL, invoking a callback when the image
	 * is loaded.
	 *
	 * @param path   The URL or path of the image.
	 * @param size   The desired size of the image.
	 * @param onLoad Callback function to handle the loaded image.
	 */
	public static void asyncLoad(String path, double size, Consumer<Image> onLoad) {
		new Thread() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				Image found = cache.get(size + "_" + path);
				if (found == null) {
					try {
						found = SwingFXUtils.toFXImage(ImageIO.read(new URL(path)), null);
					} catch (IOException e) {
						found = new Image(path);
					}
					if (found.getHeight() == 0) {
						asyncLoad(path, size, onLoad);
						return;
					}
					if (found.getHeight() != size) {
						found = resize(found, size);
					}
					cache.put(size + "_" + path, found);
				}
				onLoad.accept(found);
			}
		}.start();
	}

	/**
	 * Loads and resizes an image based on the given name, initial size, and target
	 * size.
	 *
	 * @param name     The name or path of the image.
	 * @param loadSize The initial size of the image.
	 * @param resizeTo The target size to resize the image.
	 * @return An {@code Image} object.
	 */
	public static Image loadResize(String name, double loadSize, double resizeTo) {
		return resize(load(name, loadSize), resizeTo);
	}

	/**
	 * Loads an image with a specified size.
	 *
	 * @param name The name or path of the image.
	 * @param size The desired size of the image.
	 * @return An {@code Image} object.
	 */
	public static Image load(String name, double size) {
		return load(name, size, false);
	}

	/**
	 * Resizes the given image while maintaining its aspect ratio.
	 *
	 * @param img  The image to resize.
	 * @param size The target size for the image.
	 * @return The resized {@code Image} object.
	 */
	public static Image resize(Image img, double size) {
		double ratio = img.getWidth() / img.getHeight();
		double nh = 0;
		double nw = 0;
		if (ratio >= 1) {
			nh = size;
			nw = size * ratio;
		} else {
			nw = size;
			nh = size / ratio;
		}

		Resizer resizer = DefaultResizerFactory.getInstance().getResizer(
				new Dimension((int) img.getWidth(), (int) img.getHeight()), new Dimension((int) nw, (int) nh));

		BufferedImage o = SwingFXUtils.fromFXImage(img, null);

		BufferedImage scaledImage = new FixedSizeThumbnailMaker((int) nw, (int) nh, true, true).resizer(resizer)
				.make(o);

		return SwingFXUtils.toFXImage(scaledImage, null);
	}
}
