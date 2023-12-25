package org.luke.jwin.app.layout.settings.content.display.theme;

import java.util.function.Consumer;

import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.controls.image.layer_icon.UiLayoutIcon;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.controls.popup.tooltip.Tooltip;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.factory.Borders;
import org.luke.gui.style.Style;
import org.luke.gui.window.Window;

import javafx.scene.Cursor;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class ThemeOption extends ColorIcon {

	public ThemeOption(Window win, Style style) {
		super("app-theme", 48, 32);

		setBackground(Backgrounds.make(style.getBackgroundSecondary(), 48));
		setFill(style.getAccent());

		setCursor(Cursor.HAND);

		Consumer<Style> onChange = (nv) -> {
			setBorder(nv.equals(style) ? Borders.make(nv.getTextLink(), 48, 1)
					: Borders.make(nv.getBackgroundModifierActive(), 48));
		};

		onChange.accept(win.getStyl().get());

		win.getStyl().addListener((obs, ov, nv) -> onChange.accept(nv));

		setPadding(10);

		Tooltip tt = new Tooltip(win, Direction.UP, 20, 20, 15);

		UiLayoutIcon simPrev = new UiLayoutIcon(win, "sim", 96, style);
		UiLayoutIcon advPrev = new UiLayoutIcon(win, "adv", 96, style);

		StackPane ttRoot = new StackPane(simPrev, advPrev);
		
		Rectangle clipAdv = new Rectangle();
		clipAdv.heightProperty().bind(advPrev.heightProperty().multiply(2));
		clipAdv.widthProperty().bind(advPrev.widthProperty());
		clipAdv.xProperty().bind(advPrev.widthProperty().divide(2).negate().subtract(2));
		clipAdv.yProperty().bind(advPrev.heightProperty().divide(2).negate());
		clipAdv.setRotate(10);
		

		
		Rectangle clipSim = new Rectangle();
		clipSim.heightProperty().bind(advPrev.heightProperty().multiply(2));
		clipSim.widthProperty().bind(advPrev.widthProperty());
		clipSim.xProperty().bind(advPrev.widthProperty().divide(2).add(2));
		clipSim.yProperty().bind(advPrev.heightProperty().divide(2).negate());
		clipSim.setRotate(10);
		
		advPrev.setClip(clipSim);
		simPrev.setClip(clipAdv);
		
		tt.add(ttRoot);

		Tooltip.install(this, tt);

		setAction(() -> win.setStyle(style));
	}

}
