package org.luke.gui.style;

import java.util.HashMap;

import org.apache.commons.text.CaseUtils;
import org.luke.gui.file.FileUtils;

import javafx.scene.paint.Color;

public class Style {
	public static final Style DARK = new Style("dark");
	public static final Style LIGHT = new Style("light");

	private Color accent;
	private HashMap<String, Color> colors;

	private Style(String theme) {
		colors = new HashMap<>();

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

		accent = Color.web("#6C7356");
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

	public Color getAccent() {
		return accent;
	}

	public void setAccent(Color accent) {
		this.accent = accent;
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

	public Color getTextLinkLowSaturation() {
		return colors.get("textLinkLowSaturation");
	}

	public Color getTextPositive() {
		return colors.get("textPositive");
	}

	public Color getTextWarning() {
		return colors.get("textWarning");
	}

	public Color getTextDanger() {
		return colors.get("textDanger");
	}

	public Color getTextBrand() {
		return colors.get("textBrand");
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

	public Color getInteractiveMuted() {
		return colors.get("interactiveMuted");
	}

	public Color getBackgroundPrimary() {
		return colors.get("backgroundPrimary");
	}

	public Color getBackgroundSecondary() {
		return colors.get("backgroundSecondary");
	}

	public Color getBackgroundSecondaryAlt() {
		return colors.get("backgroundSecondaryAlt");
	}

	public Color getBackgroundTertiary() {
		return colors.get("backgroundTertiary");
	}

	public Color getBackgroundAccent() {
		return colors.get("backgroundAccent");
	}

	public Color getBackgroundFloating() {
		return colors.get("backgroundFloating");
	}

	public Color getBackgroundNestedFloating() {
		return colors.get("backgroundNestedFloating");
	}

	public Color getBackgroundMobilePrimary() {
		return colors.get("backgroundMobilePrimary");
	}

	public Color getBackgroundMobileSecondary() {
		return colors.get("backgroundMobileSecondary");
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

	public Color getInfoPositiveText() {
		return colors.get("infoPositiveText");
	}

	public Color getInfoWarningText() {
		return colors.get("infoWarningText");
	}

	public Color getInfoDangerText() {
		return colors.get("infoDangerText");
	}

	public Color getInfoHelpBackground() {
		return colors.get("infoHelpBackground");
	}

	public Color getInfoHelpForeground() {
		return colors.get("infoHelpForeground");
	}

	public Color getInfoHelpText() {
		return colors.get("infoHelpText");
	}

	public Color getStatusWarningText() {
		return colors.get("statusWarningText");
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

	public Color getScrollbarAutoScrollbarColorThumb() {
		return colors.get("scrollbarAutoScrollbarColorThumb");
	}

	public Color getScrollbarAutoScrollbarColorTrack() {
		return colors.get("scrollbarAutoScrollbarColorTrack");
	}

	public Color getLogoPrimary() {
		return colors.get("logoPrimary");
	}

	public Color getControlBrandForeground() {
		return colors.get("controlBrandForeground");
	}

	public Color getControlBrandForegroundNew() {
		return colors.get("controlBrandForegroundNew");
	}

	public Color getBackgroundMentioned() {
		return colors.get("backgroundMentioned");
	}

	public Color getBackgroundMentionedHover() {
		return colors.get("backgroundMentionedHover");
	}

	public Color getBackgroundMessageHover() {
		return colors.get("backgroundMessageHover");
	}

	public Color getChannelsDefault() {
		return colors.get("channelsDefault");
	}

	public Color getChanneltextareaBackground() {
		return colors.get("channeltextareaBackground");
	}

	public Color getActivityCardBackground() {
		return colors.get("activityCardBackground");
	}

	public Color getTextboxMarkdownSyntax() {
		return colors.get("textboxMarkdownSyntax");
	}

	public Color getDeprecatedCardBg() {
		return colors.get("deprecatedCardBg");
	}

	public Color getDeprecatedCardEditableBg() {
		return colors.get("deprecatedCardEditableBg");
	}

	public Color getDeprecatedStoreBg() {
		return colors.get("deprecatedStoreBg");
	}

	public Color getDeprecatedQuickswitcherInputBackground() {
		return colors.get("deprecatedQuickswitcherInputBackground");
	}

	public Color getDeprecatedQuickswitcherInputPlaceholder() {
		return colors.get("deprecatedQuickswitcherInputPlaceholder");
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

	public Color getDeprecatedTextInputBorderDisabled() {
		return colors.get("deprecatedTextInputBorderDisabled");
	}

	public Color getDeprecatedTextInputPrefix() {
		return colors.get("deprecatedTextInputPrefix");
	}

	public Color getCloseIconActive() {
		return colors.get("closeIconActive");
	}

	public Color getCountryCodeItemHover() {
		return colors.get("countryCodeItemHover");
	}

	public Color getSecondaryButtonBack() {
		return colors.get("secondaryButtonBack");
	}

	public Color getLinkButtonText() {
		return colors.get("linkButtonText");
	}

	public Color getCountryCodeItemText() {
		return colors.get("countryCodeItemText");
	}

	public Color getCountryNameItemText() {
		return colors.get("countryNameItemText");
	}

	public Color getSessionWindowBorder() {
		return colors.get("sessionWindowBorder");
	}
}
