package org.luke.gui.controls;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;

import org.luke.gui.exception.ErrorHandler;

import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 * provides a convenient way to manage and use fonts in JavaFX applications. It
 * supports various font properties such as family, size, weight, and posture.
 * <p>
 * It includes default font settings and a cache to efficiently manage loaded
 * fonts.
 * </p>
 * <p>
 * Fonts are loaded from the "fonts" directory in the classpath based on the
 * specified font family.
 * </p>
 * <p>
 * Implements the equals method to check for font equality.
 * </p>
 * <p>
 * The static initializer initializes the default fonts.
 * </p>
 *
 * @author SDIDSA
 */
public class Font {

	public static final String DEFAULT_MONO_FAMILY = "Hasubi Mono";
	public static final String DEFAULT_FAMILY = "Readex Pro";
	public static final String DEFAULT_FAMILY_MEDIUM = DEFAULT_FAMILY + " Medium";

	public static final FontWeight DEFAULT_WEIGHT = FontWeight.NORMAL;
	public static final FontPosture DEFAULT_POSTURE = FontPosture.REGULAR;
	public static final double DEFAULT_SIZE = 14;

	public static final Font DEFAULT = new Font();

	private final String family;
	private final double size;
	private final FontWeight weight;
	private final FontPosture posture;

	public Font(String family, double size, FontWeight weight, FontPosture posture) {
		this.family = family;
		this.size = size;
		this.weight = weight;
		this.posture = posture;
	}

	public Font(String family, double size, FontWeight weight) {
		this(family, size, weight, DEFAULT_POSTURE);
	}

	public Font(String family, double size) {
		this(family, size, DEFAULT_WEIGHT, DEFAULT_POSTURE);
	}

	public Font(String family) {
		this(family, DEFAULT_SIZE, DEFAULT_WEIGHT, DEFAULT_POSTURE);
	}

	public Font(double size) {
		this(DEFAULT_FAMILY, size, DEFAULT_WEIGHT, DEFAULT_POSTURE);
	}

	public Font(FontWeight weight) {
		this(DEFAULT_FAMILY, DEFAULT_SIZE, weight, DEFAULT_POSTURE);
	}

	public Font(double size, FontWeight weight) {
		this(DEFAULT_FAMILY, size, weight, DEFAULT_POSTURE);
	}

	private Font() {
		this(DEFAULT_FAMILY, DEFAULT_SIZE, DEFAULT_WEIGHT, DEFAULT_POSTURE);
	}

	public Font(double size, FontPosture posture) {
		this(DEFAULT_FAMILY, size, DEFAULT_WEIGHT, posture);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Font otherFont) {
			return family.equals(otherFont.family) && size == otherFont.size && weight.equals(otherFont.weight)
					&& posture.equals(otherFont.posture);
		} else {
			return false;
		}
	}

	private static final HashMap<Font, javafx.scene.text.Font> cache = new HashMap<>();

	public javafx.scene.text.Font getFont() {
		javafx.scene.text.Font found = cache.get(this);

		if (found == null) {
			found = javafx.scene.text.Font.font(family, weight, posture, size);
			cache.put(this, found);
		}

		return found;
	}

	static {
		init();
	}

	private static void init() {
		loadFont(DEFAULT_FAMILY);
		loadFont(DEFAULT_MONO_FAMILY);
	}

	private static void loadFont(String name) {
		try {
			File parent = new File(URLDecoder.decode(Objects.requireNonNull(Font.class
                            .getResource(
                                    String.join("/", "/fonts", name.replace(" ", ""),
                                            name.replace(" ", "") + "-Regular.ttf")))
					.getFile(), StandardCharsets.UTF_8)).getParentFile();
			for (File font : Objects.requireNonNull(parent.listFiles())) {
				javafx.scene.text.Font.loadFont(new FileInputStream(font), 14);
			}
		} catch (Exception x) {
			ErrorHandler.handle(x, "load font [" + name + "]");
		}
	}
}
