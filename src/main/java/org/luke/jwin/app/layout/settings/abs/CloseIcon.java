package org.luke.jwin.app.layout.settings.abs;

import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.factory.Borders;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;

public class CloseIcon extends StackPane implements Styleable {
	private final ColorIcon icon;
	
	public CloseIcon(Settings settings) {
		setMinSize(36, 36);
		setMaxSize(36, 36);
		setCursor(Cursor.HAND);
		
		icon = new ColorIcon("delete", 18);
		
		getChildren().add(icon);
		
		setOnMouseClicked(e-> {
			settings.getOwner().hide();
		});
		
		applyStyle(settings.getWindow().getStyl());
	}

	@Override
	public void applyStyle(Style style) {
		setBorder(Borders.make(style.getTextMuted(), 18, 2));
		backgroundProperty().bind(Bindings.when(hoverProperty()).then(Backgrounds.make(style.getCloseIconActive(), 36)).otherwise(Background.EMPTY));
		
		icon.setFill(style.getInteractiveNormal());
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
