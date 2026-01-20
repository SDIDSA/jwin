package org.luke.jwin.app.console;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;

public class ConsoleIOLine extends Label implements ConsoleLine, Styleable {
	private static final Font f = new Font(Font.DEFAULT_MONO_FAMILY, 16);
	
	private final ConsoleLineType type;

	public ConsoleIOLine(Window window, String content, ConsoleLineType type) {
		this(window, content, type, true);
	}

	public ConsoleIOLine(Window window, String content, ConsoleLineType type, boolean keyed) {
		super(window, keyed ? content : "", f);

		if(!keyed)
			setText(content);

		this.type = type;
		
		setLineSpacing(4);
		
		applyStyle(window.getStyl());
	}
	
	public ConsoleLineType getType() {
		return type;
	}
	
	@Override
	public void applyStyle(Style style) {
		setFill(type == ConsoleLineType.IN ? style.getTextPositive()
				: (type == ConsoleLineType.ERROUT ? style.getTextDanger() : style.getHeaderSecondary()));
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
