package org.luke.gui.controls.image;

import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.style.StyledColor;
import org.luke.gui.window.Window;

public class ColoredIcon extends ColorIcon implements Styleable {

	private StyledColor fill;
	public ColoredIcon(Window win, String name, double size, StyledColor fill) {
		super(name, size);
		
		this.fill = fill;
		
		applyStyle(win.getStyl());
	}

	@Override
	public void applyStyle(Style style) {
		setFill(fill.apply(style));
		super.applyStyle(style);
	}
}
