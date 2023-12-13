package org.luke.jwin.app.console;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.unkeyed.Text;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;

public class ConsoleLine extends Text implements Styleable {

	private ConsoleLineType type;

	public ConsoleLine(Window window, String content, ConsoleLineType type) {
		super(content);

		this.type = type;

		setFont(new Font(Font.DEFAULT_MONO_FAMILY, 16));
		
		setLineSpacing(4);
		
		applyStyle(window.getStyl());
	}

	@Override
	public void applyStyle(Style style) {
		setFill(type == ConsoleLineType.IN ? style.getTextPositive()
				: (type == ConsoleLineType.ERROUT ? style.getTextWarning() : style.getHeaderSecondary()));
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
