package org.luke.jwin.app.layout.settings.content.display.theme;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.SplineInterpolator;
import org.luke.gui.controls.check.KeyedRadio;
import org.luke.gui.controls.check.Radio;
import org.luke.gui.controls.check.RadioGroup;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.factory.Borders;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.jwin.app.layout.settings.abs.Settings;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ThemeSetting extends VBox implements Styleable {
	private final StackPane disp;

	public ThemeSetting(Settings settings) {

		HBox few = new HBox(20);
		few.setPadding(new Insets(20));
		few.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
		few.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
		few.setPrefSize(52 * 4 + 20 * 5, 52 + 20 * 2);
		few.setAlignment(Pos.CENTER_LEFT);

		for (Style s : Style.FEW_STYLES) {
			ThemeOption to = new ThemeOption(settings.getWindow(), s);
			few.getChildren().add(to);
		}

		ThemeButton showMore = new ThemeButton(settings.getWindow(), "sort-down", "more_options");
		few.getChildren().add(showMore);

		GridPane all = new GridPane();
		all.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
		all.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
		all.setPrefSize(52 * 5 + 20 * 6, 52 * 3 + 20 * 4);
		all.setPadding(new Insets(20));
		all.setHgap(20);
		all.setVgap(15);

		int x = 0;
		int y = 0;
		int count = 0;
		for (Style s : Style.ALL_STYLES) {
			ThemeOption to = new ThemeOption(settings.getWindow(), s);
			all.add(to, x, y);
			count++;
			x++;
			if (count >= 4) {
				count = 0;
				x = 0;
				y++;
			}
		}
		all.setOpacity(0);
		all.setTranslateY(100);
		all.setMouseTransparent(true);

		ThemeButton showLess = new ThemeButton(settings.getWindow(), "sort-down", "less_options");
		showLess.setRotate(180);

		ThemeButton customTheme = new ThemeButton(settings.getWindow(), "palette", "custom_colors");

		all.add(showLess, 4, 0);
		all.add(customTheme, 4, 1);

		HBox cust = new HBox(25);
		cust.setPadding(new Insets(20));
		cust.setOpacity(0);
		cust.setTranslateY(100);
		cust.setMouseTransparent(true);

		ColorPicker cp = new ColorPicker(settings.getWindow(), 160);

		ThemeButton backToMore = new ThemeButton(settings.getWindow(), "sort-down", "go_back_to_options");
		backToMore.setRotate(180);

		ThemeButton useThisColor = new ThemeButton(settings.getWindow(), "done", "use_this_color", Direction.DOWN, 20, 20);

		HBox action = new HBox(15, useThisColor, backToMore);
		action.setAlignment(Pos.CENTER_RIGHT);

		Label text = new Label(settings.getWindow(), "text_preview", new Font(16));

		StackPane cPrev = new StackPane(text);
		cPrev.setMinSize(200, 60);
		cPrev.setMaxSize(200, 60);

		cPrev.setBackground(Backgrounds.make(cp.getValue(), 20));
		text.setFill(Style.getContrastColor(cp.getValue()));

		KeyedRadio dark = new KeyedRadio(settings.getWindow(), "dark", 16);
		KeyedRadio gray = new KeyedRadio(settings.getWindow(), "gray", 16);
		KeyedRadio light = new KeyedRadio(settings.getWindow(), "light", 16);

		RadioGroup group = new RadioGroup(dark, gray, light);
		dark.getCheck().flip();

		HBox mode = new HBox(10, dark, gray, light);

		VBox right = new VBox(15, cPrev, mode, action);

		Runnable styleUpdate = () -> {
			cPrev.setBackground(Backgrounds.make(cp.getValue(), 20));
			text.setFill(Style.getContrastColor(cp.getValue()));

			Radio m = group.getValue();
			Style s = new Style((m == dark.getCheck() || m == gray.getCheck()) ? "dark" : "light", cp.getValue(),
					(m == dark.getCheck() ? 0.5 : 1));

			applyStyle(s);
			cp.getHuePicker().applyStyle(s);
			cp.getSbPicker().applyStyle(s);
			dark.applyStyle(s);
			dark.getCheck().applyStyle(s);
			gray.applyStyle(s);
			gray.getCheck().applyStyle(s);
			light.applyStyle(s);
			light.getCheck().applyStyle(s);
			
			useThisColor.applyStyle(s);
			backToMore.applyStyle(s);
		};
		
		cp.valueProperty().addListener((obs, ov, nv) -> {
			styleUpdate.run();
		});
		group.valueProperty().addListener((obs, ov, nv) -> {
			styleUpdate.run();
		});

		cust.getChildren().addAll(cp, right);

		disp = new StackPane(few, all, cust);
		disp.setAlignment(Pos.TOP_LEFT);

		disp.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
		disp.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
		disp.prefWidthProperty().bind(few.widthProperty());
		disp.prefHeightProperty().bind(few.heightProperty());

		Rectangle clip = new Rectangle();
		clip.setArcWidth(30);
		clip.setArcHeight(30);

		clip.widthProperty().bind(disp.widthProperty());
		clip.heightProperty().bind(disp.heightProperty());

		disp.setClip(clip);

		showMore.setAction(() -> {
			
			few.setMaxWidth(few.getWidth());
			all.setMaxWidth(all.getWidth());
			Timeline sma = new Timeline(new KeyFrame(Duration.millis(300),
					new KeyValue(disp.prefHeightProperty(), all.getHeight(), SplineInterpolator.OVERSHOOT),
					new KeyValue(disp.prefWidthProperty(), all.getWidth(), SplineInterpolator.OVERSHOOT),
					new KeyValue(all.translateYProperty(), 0, SplineInterpolator.OVERSHOOT),
					new KeyValue(few.translateYProperty(), -100, SplineInterpolator.OVERSHOOT),
					new KeyValue(all.opacityProperty(), 1, SplineInterpolator.OVERSHOOT),
					new KeyValue(few.opacityProperty(), 0, SplineInterpolator.OVERSHOOT)));
			sma.setOnFinished(e -> {
				all.setMouseTransparent(false);
				few.setMouseTransparent(true);
			});
			disp.prefHeightProperty().unbind();
			disp.prefWidthProperty().unbind();
			sma.playFromStart();
		});

		showLess.setAction(() -> {
			Timeline sla = new Timeline(new KeyFrame(Duration.millis(300),
					new KeyValue(disp.prefHeightProperty(), few.getHeight(), SplineInterpolator.OVERSHOOT),
					new KeyValue(disp.prefWidthProperty(), few.getWidth(), SplineInterpolator.OVERSHOOT),
					new KeyValue(few.translateYProperty(), 0, SplineInterpolator.OVERSHOOT),
					new KeyValue(all.translateYProperty(), 100, SplineInterpolator.OVERSHOOT),
					new KeyValue(few.opacityProperty(), 1, SplineInterpolator.OVERSHOOT),
					new KeyValue(all.opacityProperty(), 0, SplineInterpolator.OVERSHOOT)));

			sla.setOnFinished(e -> {
				all.setMouseTransparent(true);
				few.setMouseTransparent(false);
			});
			disp.prefHeightProperty().unbind();
			disp.prefWidthProperty().unbind();
			sla.playFromStart();
		});

		customTheme.setAction(() -> {
			Style s = settings.getWindow().getStyl().get();
			cp.valueProperty().set(s.getAccent());
			if(s.isDark()) dark.getCheck().checkedProperty().set(true);
			else if(s.isGray()) gray.getCheck().checkedProperty().set(true);
			else if(s.isLight()) light.getCheck().checkedProperty().set(true);
			styleUpdate.run();
			Timeline sca = new Timeline(new KeyFrame(Duration.millis(300),
					new KeyValue(disp.prefHeightProperty(), cp.getHeight() + 40, SplineInterpolator.OVERSHOOT),
					new KeyValue(disp.prefWidthProperty(), cust.getWidth(), SplineInterpolator.OVERSHOOT),
					new KeyValue(cust.translateYProperty(), 0, SplineInterpolator.OVERSHOOT),
					new KeyValue(all.translateYProperty(), -100, SplineInterpolator.OVERSHOOT),
					new KeyValue(cust.opacityProperty(), 1, SplineInterpolator.OVERSHOOT),
					new KeyValue(all.opacityProperty(), 0, SplineInterpolator.OVERSHOOT)));

			sca.setOnFinished(e -> {
				all.setMouseTransparent(true);
				cust.setMouseTransparent(false);
			});
			disp.prefHeightProperty().unbind();
			disp.prefWidthProperty().unbind();
			sca.playFromStart();
		});

		backToMore.setAction(() -> {
			applyStyle(settings.getWindow().getStyl().get());
			Timeline sba = new Timeline(new KeyFrame(Duration.millis(300),
					new KeyValue(disp.prefHeightProperty(), all.getHeight(), SplineInterpolator.OVERSHOOT),
					new KeyValue(disp.prefWidthProperty(), all.getWidth(), SplineInterpolator.OVERSHOOT),
					new KeyValue(all.translateYProperty(), 0, SplineInterpolator.OVERSHOOT),
					new KeyValue(cust.translateYProperty(), 100, SplineInterpolator.OVERSHOOT),
					new KeyValue(all.opacityProperty(), 1, SplineInterpolator.OVERSHOOT),
					new KeyValue(cust.opacityProperty(), 0, SplineInterpolator.OVERSHOOT)));

			sba.setOnFinished(e -> {
				cust.setMouseTransparent(true);
				all.setMouseTransparent(false);
			});
			disp.prefHeightProperty().unbind();
			disp.prefWidthProperty().unbind();
			sba.playFromStart();
		});

		useThisColor.setAction(() -> {
			Radio m = group.getValue();
			Style s = new Style((m == dark.getCheck() || m == gray.getCheck()) ? "dark" : "light", cp.getValue(),
					(m == dark.getCheck() ? 0.5 : 1));
			settings.getWindow().setStyle(s);
		});

		getChildren().addAll(disp);
		
		minHeightProperty().bind(disp.prefHeightProperty());

		applyStyle(settings.getWindow().getStyl());
	}

	@Override
	public void applyStyle(Style style) {
		disp.setBackground(Backgrounds.make(style.getBackgroundSecondary(), 15));
		disp.setBorder(Borders.make(style.getBackgroundModifierActive(), 15));
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

}
