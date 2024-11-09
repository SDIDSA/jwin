package org.luke.jwin.app.console;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.alert.BasicOverlay;
import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.locale.Locale;
import org.luke.gui.style.Style;
import org.luke.gui.window.Page;
import org.luke.jwin.app.Command;
import org.luke.jwin.ui.TextVal;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.HBox;

public class Console extends BasicOverlay {
	
	private final ColorIcon send;
	private final HBox preInput;
	private final ConsoleOutput output;
	
	public Console(Page ps, Command c) {
		super(ps, 550);
		removeTop();
		removeSubHead();
		
		head.setKey("application_io");

		output = new ConsoleOutput(ps.getWindow());
		
		TextVal input = new TextVal(getWindow(), "application_input");
		input.setInputFont(new Font(Font.DEFAULT_MONO_FAMILY, 16));

		send = new ColorIcon("send", 96, 24);
		send.setTranslateY(-9);
		send.setCursor(Cursor.HAND);
		send.opacityProperty().bind(Bindings.when(send.hoverProperty()).then(1).otherwise(.6));
		
		send.setAction(() -> {
			String line = input.getValue();
			output.addInput(line, false);
			c.write(line);
			input.setValue("");
		});

		input.setAction(send::fire);

		
		c.addInputHandler(line -> {
			if(line.endsWith("com.sun.javafx.application.PlatformImpl startup")) return;
			if(line.startsWith("WARNING: Unsupported JavaFX configuration: classes were loaded from 'unnamed module")) return;
			Platform.runLater(() -> {
				output.addOutput(line, false);
			});
		});
		
		c.addErrorHandler(line -> {
			if(line.endsWith("com.sun.javafx.application.PlatformImpl startup")) return;
			if(line.startsWith("WARNING: Unsupported JavaFX configuration: classes were loaded from 'unnamed module")) return;
			Platform.runLater(() -> {
				output.addError(line, false);
			});
		});

		output.setMinHeight(230);
		output.setMaxHeight(230);

		output.setMinWidth(0);
		output.maxWidthProperty().bind(center.widthProperty().subtract(32));
		
		preInput = new HBox(10, input, send);
		preInput.setAlignment(Pos.BOTTOM_CENTER);
		
		center.getChildren().addAll(output, preInput);
		
		removeBottom();

		applyStyle(ps.getWindow().getStyl());
	}
	
	public void setOnStop(Runnable onStop) {
		output.setOnStop(onStop);
	}
	
	public void exited(int ec) {
		output.addOutput(Locale.key("exited_with_code", "code", ec));
		preInput.setDisable(true);
	}

	@Override
	public void applyStyle(Style style) {
		send.setFill(style.getHeaderPrimary());
		super.applyStyle(style);
	}
}
