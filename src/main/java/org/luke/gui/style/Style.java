package org.luke.gui.style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.text.CaseUtils;
import org.luke.gui.file.FileUtils;

import javafx.scene.paint.Color;

public class Style {
	public static final Style DARK_1 = new Style("dark", Color.web("#666666"), 0.5);
	public static final Style DARK_2 = new Style("dark", Color.web("#367640"), 0.5);
	public static final Style DARK_3 = new Style("dark", Color.web("#D33232"), 0.5);
	public static final Style DARK_4 = new Style("dark", Color.web("#027BC6"), 0.5);

	public static final Style GRAY_1 = new Style("dark", Color.web("#666666"), 1);
	public static final Style GRAY_2 = new Style("dark", Color.web("#367640"), 1);
	public static final Style GRAY_3 = new Style("dark", Color.web("#D33232"), 1);
	public static final Style GRAY_4 = new Style("dark", Color.web("#027BC6"), 1);

	public static final Style LIGHT_1 = new Style("light", Color.web("#666666"), 1);
	public static final Style LIGHT_2 = new Style("light", Color.web("#367640"), 1);
	public static final Style LIGHT_3 = new Style("light", Color.web("#D33232"), 1);
	public static final Style LIGHT_4 = new Style("light", Color.web("#027BC6"), 1);

	public static final List<Style> DARK = new ArrayList<>(
			Arrays.asList(new Style[] { DARK_1, DARK_2, DARK_3, DARK_4 }));

	public static final List<Style> GRAY = new ArrayList<>(
			Arrays.asList(new Style[] { GRAY_1, GRAY_2, GRAY_3, GRAY_4 }));

	public static final List<Style> LIGHT = new ArrayList<>(
			Arrays.asList(new Style[] { LIGHT_1, LIGHT_2, LIGHT_3, LIGHT_4 }));

	public static final List<Style> FEW_STYLES = new ArrayList<>(
			Arrays.asList(new Style[] { DARK_1, GRAY_1, LIGHT_1 }));

	public static final List<Style> ALL_STYLES = new ArrayList<>();

	static {
		ALL_STYLES.addAll(DARK);
		ALL_STYLES.addAll(GRAY);
		ALL_STYLES.addAll(LIGHT);
	}

	private Color accent;
	private HashMap<String, Color> colors;

	private String themeName;
	private double brightnessModifier;

	public Style(String theme, Color accent, double brightnessModifier) {
		colors = new HashMap<>();
		this.themeName = theme;
		this.accent = accent;
		this.brightnessModifier = brightnessModifier;

		String content = FileUtils.readFile("/themes/" + theme + ".txt");

		for (String preLine : content.split("\n")) {
			String[] line = preLine.trim().split(":");
			if (line.length != 2) {
				continue;
			}
			String key = line[0].replace("-", " ").trim().replace(" ", "_");
			String value = line[1].replace(";", "").trim().replace("var(--saturation-factor, 1)*", "")
					.replaceAll("\\bcalc\\(\\b([0-9]+\\.?[0-9]+%)\\)", "$1");

			key = CaseUtils.toCamelCase(key, false, '_');

			try {
				Color c = value.startsWith("hsl") ? parseHSL(value) : Color.web(value);
				colors.put(key, c);
			} catch (Exception x) {
				// IGNORE
			}
		}
	}
	
	public String getThemeName() {
		return themeName;
	}
	
	public boolean isDark() {
		return themeName.equals("dark") && brightnessModifier == 0.5;
	}
	
	public boolean isGray() {
		return themeName.equals("dark") && brightnessModifier == 1;
	}
	
	public boolean isLight() {
		return themeName.equals("light");
	}

	private static Color parseHSL(String hslString) {
		double[] vals = new double[4];

		int pos = 0;
		for (String preVal : hslString.substring(hslString.indexOf("(") + 1, hslString.indexOf(")")).split(",")) {
			vals[pos++] = Double.parseDouble(preVal.replace("%", ""));
		}

		double hue = vals[0];
		double saturation = vals[1] / 100;
		double lightness = vals[2] / 100;
		double alpha = vals[3] == 0 ? 1.0 : vals[3];

		double c = (1 - Math.abs((2 * lightness) - 1)) * saturation;
		double x = c * (1 - Math.abs((hue / 60) % 2 - 1));
		double m = lightness - c / 2;

		double[] dRgb = null;

		int div = (int) hue / 60;
		switch (div) {
		case 0:
			dRgb = new double[] { c, x, 0 };
			break;
		case 1:
			dRgb = new double[] { x, c, 0 };
			break;
		case 2:
			dRgb = new double[] { 0, c, x };
			break;
		case 3:
			dRgb = new double[] { 0, x, c };
			break;
		case 4:
			dRgb = new double[] { x, 0, c };
			break;
		case 5:
			dRgb = new double[] { c, 0, x };
			break;
		default:
			dRgb = new double[3];
		}

		int[] rgb = new int[dRgb.length];
		for (int i = 0; i < dRgb.length; i++) {
			rgb[i] = (int) ((dRgb[i] + m) * 255);
		}

		return Color.rgb(rgb[0], rgb[1], rgb[2], alpha);
	}

	public Style setBrightnessModifier(double brightnessModifier) {
		return new Style(themeName, accent, brightnessModifier);
	}

	public Color getAccent() {
		return accent;
	}

	public void setAccent(Color accent) {
		this.accent = accent;
	}

	public Color getTextOnAccent() {
		return getContrastColor(accent);
	}

	public static Color getContrastColor(Color backgroundColor) {
		double luminance = 0.299 * backgroundColor.getRed() + 0.587 * backgroundColor.getGreen()
				+ 0.114 * backgroundColor.getBlue();
		return luminance > 0.5 ? Color.BLACK : Color.WHITE;
	}

	private Color mix(Color brightness, Color hue) {
		return brightness.interpolate(hue, 0.12);
	}

	/* ********************************** */

	public Color getHeaderPrimary() {
		return colors.get("headerPrimary");
	}

	public Color getHeaderSecondary() {
		return colors.get("headerSecondary");
	}

	public Color getTextNormal() {
		return colors.get("textNormal");
	}

	public Color getTextMuted() {
		return colors.get("textMuted");
	}

	public Color getTextLink() {
		return colors.get("textLink");
	}

	public Color getTextPositive() {
		return colors.get("textPositive");
	}

	public Color getTextDanger() {
		return colors.get("textDanger");
	}

	public Color getInteractiveNormal() {
		return colors.get("interactiveNormal");
	}

	public Color getInteractiveHover() {
		return colors.get("interactiveHover");
	}

	public Color getInteractiveActive() {
		return colors.get("interactiveActive");
	}

	public Color getBackgroundPrimaryOr() {
		return colors.get("backgroundPrimary").deriveColor(0, 1, brightnessModifier, 1);
	}

	public Color getBackgroundPrimary() {
		return mix(getBackgroundPrimaryOr(), accent);
	}

	public Color getBackgroundSecondaryOr() {
		return colors.get("backgroundSecondary").deriveColor(0, 1, brightnessModifier, 1);
	}

	public Color getBackgroundSecondary() {
		return mix(getBackgroundSecondaryOr(), accent);
	}

	public Color getBackgroundTertiaryOr() {
		return colors.get("backgroundTertiary").deriveColor(0, 1, brightnessModifier, 1);
	}

	public Color getBackgroundTertiary() {
		return mix(getBackgroundTertiaryOr(), accent);
	}

	public Color getBackgroundFloatingOr() {
		return colors.get("backgroundFloating").deriveColor(0, 1, brightnessModifier, 1);
	}

	public Color getBackgroundFloating() {
		return mix(getBackgroundFloatingOr(), accent);
	}

	public Color getBackgroundModifierHover() {
		return colors.get("backgroundModifierHover");
	}

	public Color getBackgroundModifierActive() {
		return colors.get("backgroundModifierActive");
	}

	public Color getBackgroundModifierSelected() {
		return colors.get("backgroundModifierSelected");
	}

	public Color getBackgroundModifierAccent() {
		return colors.get("backgroundModifierAccent");
	}

	public Color getScrollbarThinThumb() {
		return colors.get("scrollbarThinThumb");
	}

	public Color getScrollbarThinTrack() {
		return colors.get("scrollbarThinTrack");
	}

	public Color getScrollbarAutoThumb() {
		return colors.get("scrollbarAutoThumb");
	}

	public Color getScrollbarAutoTrack() {
		return colors.get("scrollbarAutoTrack");
	}

	public Color getChannelsDefault() {
		return colors.get("channelsDefault");
	}

	public Color getDeprecatedTextInputBg() {
		return colors.get("deprecatedTextInputBg");
	}

	public Color getDeprecatedTextInputBorder() {
		return colors.get("deprecatedTextInputBorder");
	}

	public Color getDeprecatedTextInputBorderHover() {
		return colors.get("deprecatedTextInputBorderHover");
	}

	public Color getCloseIconActive() {
		return colors.get("closeIconActive");
	}

	public Color getSecondaryButtonBack() {
		return colors.get("secondaryButtonBack");
	}

	public Color getLinkButtonText() {
		return colors.get("linkButtonText");
	}
}
