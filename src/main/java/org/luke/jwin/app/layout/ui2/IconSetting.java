package org.luke.jwin.app.layout.ui2;

import java.io.File;

import javafx.geometry.NodeOrientation;
import org.luke.gui.controls.Font;
import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.controls.input.Input;
import org.luke.gui.controls.input.styles.DeprecatedInputStyle;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;
import org.luke.jwin.app.param.Param;

import javafx.beans.property.ObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;

public class IconSetting extends Input implements Styleable {	
	private final ColorIcon empty;
	
	public IconSetting(Window win, Runnable fire) {
		super("app_icon");

		setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
		
		setMinWidth(USE_PREF_SIZE);
		setMaxWidth(USE_PREF_SIZE);
		prefWidthProperty().bind(heightProperty());
		
		inputStyle = new DeprecatedInputStyle(this);

		setFocusTraversable(true);
		
		setOnMouseClicked(e -> {
			requestFocus();
			fire.run();
		});

		focusedProperty().addListener((obs, ov, nv) -> {
			inputStyle.focus(nv);
		});
		
		setOnKeyPressed(ke -> {
			if(ke.getCode().equals(KeyCode.SPACE)) {
				fire.run();
			}
		});
		
		empty = new ColorIcon("icon", 48);
		
		getChildren().setAll(empty);

		setCursor(Cursor.HAND);

		applyStyle(win.getStyl());
	}
	
	public void set(File f) {
		Image i = Param.typeIcon(f, 48);
		
		ImageView disp = new ImageView();
		
		disp.setFitWidth(48);
		disp.setFitHeight(48);
		
		getChildren().setAll(disp);
		
		disp.setImage(i);
	}

	public void unset() {
		getChildren().setAll(empty);
	}

	@Override
	public boolean isFocus() {
		return isFocused();
	}

	@Override
	public void setFont(Font font) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setValue(String value) {
		File f = new File(value);
        set(f);
	}

	@Override
	public void clear() {
		getChildren().setAll(empty);
	}

	@Override
	public void applyStyle(Style style) {
		inputStyle.applyStyle(style);
		empty.setFill(style.getHeaderSecondary());
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
