package org.luke.jwin.app.layout.settings.content.display.layout;

import org.luke.gui.controls.check.KeyedRadio;
import org.luke.gui.controls.check.Radio;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.factory.Borders;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;

public class UiLayoutOption extends VBox implements Styleable {
	private KeyedRadio top;

	public UiLayoutOption(Window window, String text, String img) {
		super(15);
		setPadding(new Insets(15));

		top = new KeyedRadio(window, text, 16);
		
		setCursor(Cursor.HAND);
		
		setOnMousePressed(e -> {
			getCheck().checkedProperty().set(true);
		});
		
		ImageView iv = new ImageView(new Image(getClass().getResourceAsStream("/images/" + img + ".png")));

		getChildren().setAll(top, iv);
		

		applyStyle(window.getStyl());
	}

	public Radio getCheck() {
		return top.getCheck();
	}

	@Override
	public void applyStyle(Style style) {
		backgroundProperty().bind(
				Bindings.when(hoverProperty()).then(Backgrounds.make(style.getBackgroundModifierHover(), 15)).otherwise(Bindings.when(top.checkedProperty())
						.then(Backgrounds.make(style.getBackgroundModifierSelected(), 15)).otherwise(Background.EMPTY)));

		setBorder(Borders.make(style.getBackgroundModifierActive(), 15));
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
