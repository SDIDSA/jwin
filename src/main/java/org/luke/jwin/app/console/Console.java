package org.luke.jwin.app.console;

import org.luke.gui.app.pages.Page;
import org.luke.gui.controls.Font;
import org.luke.gui.controls.alert.BasicOverlay;
import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.style.Style;
import org.luke.jwin.app.Command;
import org.luke.jwin.ui.TextVal;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.HBox;

public class Console extends BasicOverlay {
	
	ColorIcon send;
	
	public Console(Page ps, Command c) {
		super(ps, 550);
		removeTop();
		removeSubHead();
		
		head.setKey("Application Console");

		ConsoleOutput output = new ConsoleOutput(ps.getWindow());
		
		TextVal input = new TextVal(getWindow(), "Input");
		input.setInputFont(new Font(Font.DEFAULT_MONO_FAMILY, 16));

		send = new ColorIcon("send", 96, 24);
		send.setTranslateY(-9);
		send.setCursor(Cursor.HAND);
		send.opacityProperty().bind(Bindings.when(send.hoverProperty()).then(1).otherwise(.6));
		
		send.setAction(() -> {
			String line = input.getValue();
			output.addInput(line);
			c.write(line);
			input.setValue("");
		});

		input.setAction(send::fire);

		
		c.addInputHandler(line -> {
			Platform.runLater(() -> {
				output.addOutput(line);
			});
		});
		
		c.addErrorHandler(line -> {
			Platform.runLater(() -> {
				output.addError(line);
			});
		});

		output.setMinHeight(230);
		output.setMaxHeight(230);

		output.setMinWidth(0);
		output.maxWidthProperty().bind(center.widthProperty().subtract(32));
		
		HBox preInput = new HBox(10, input, send);
		preInput.setAlignment(Pos.BOTTOM_CENTER);
		
		center.getChildren().addAll(output, preInput);

		addOnShown(() -> {
			
		});

		applyStyle(ps.getWindow().getStyl());
	}

	@Override
	public void applyStyle(Style style) {
		send.setFill(style.getHeaderPrimary());
		super.applyStyle(style);
	}
}
