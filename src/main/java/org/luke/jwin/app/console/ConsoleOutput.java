package org.luke.jwin.app.console;

import org.luke.gui.controls.scroll.Scrollable;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;

public class ConsoleOutput extends Scrollable implements Styleable {
	private Window win;
	private VBox lines;
	
	public ConsoleOutput(Window win) {
		this.win = win;
		lines = new VBox(6);
		lines.setPadding(new Insets(10));
		
		setContent(lines);
		
		applyStyle(win.getStyl());
	}
	
	public void addLine(String line, ConsoleLineType type) {
		lines.getChildren().add(new ConsoleLine(win, line, type));
		getScrollBar().positionProperty().set(1);
	}
	
	public void addError(String line) {
		addLine(line, ConsoleLineType.ERROUT);
	}
	
	public void addOutput(String line) {
		addLine(line, ConsoleLineType.STDOUT);
	}
	
	public void addInput(String line) {
		addLine(line, ConsoleLineType.IN);
	}
	
	@Override
	public void applyStyle(Style style) {
		setBackground(Backgrounds.make(style.getBackgroundTertiary(), 8));
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

}
