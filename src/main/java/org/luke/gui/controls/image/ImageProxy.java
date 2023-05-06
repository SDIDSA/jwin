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

public class ImageProxy {
	private static HashMap<String, Image> cache = new HashMap<>();

	private ImageProxy() {
		
	}
	
	public static Image load(String name, double size, boolean fullPath) {
		String path = fullPath ? name
				: new StringBuilder().append("/images/icons/").append(name).append('_').append((int) size).append(".png")
						.toString();

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
	
	public static void asyncLoad(String path, double size, Consumer<Image> onLoad) {
		new Thread() {
			@Override
			public void run() {
				Image found = cache.get(size + "_" + path);
				if(found == null) {
					try {
						found = SwingFXUtils.toFXImage(ImageIO.read(new URL(path)), null);
					} catch (IOException e) {
						found = new Image(path);
					}
					if(found.getHeight() == 0) {
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
	
	public static Image loadResize(String name, double loadSize, double resizeTo) {
		return resize(load(name, loadSize), resizeTo);
	}

	public static Image load(String name, double size) {
		return load(name, size, false);
	}

	public static Image resize(Image img, double size) {
		double ratio = img.getWidth() / img.getHeight();
		double nh = 0;
		double nw = 0;
		if(ratio >= 1) {
			nh = size;
			nw = size * ratio;
		}else {
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
