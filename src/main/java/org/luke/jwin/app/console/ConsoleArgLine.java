package org.luke.jwin.app.console;

import javafx.beans.property.ObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextFlow;
import org.luke.gui.controls.Font;
import org.luke.gui.controls.image.ColoredIcon;
import org.luke.gui.controls.label.unkeyed.Text;
import org.luke.gui.controls.space.ExpandingHSpace;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

public class ConsoleArgLine extends HBox implements ConsoleLine, Styleable {
    private static final Font f = new Font(Font.DEFAULT_MONO_FAMILY, 16);

    private final Text key, eq, value;

    private final ColoredIcon remove;

    public ConsoleArgLine(Window window, String content) {

            key = new Text("", f);
            eq = new Text("", f);
            value = new Text("", f);

            remove = new ColoredIcon(window, "delete", 18, 16, Style::getTextNormal);
            remove.setCursor(Cursor.HAND);

            setText(content);

            getChildren().addAll(key,eq,value, new ExpandingHSpace(), remove );

            key.setLineSpacing(4);
            eq.setLineSpacing(4);
            value.setLineSpacing(4);

            applyStyle(window.getStyl());
    }

    public void setOnRemove(Runnable action) {
        remove.setAction(action);
    }

    @Override
    public void setWrappingWidth(double v) {
        //ignore
    }

    public ConsoleLineType getType() {
        return ConsoleLineType.ARG;
    }

    @Override
    public void setKey(String content) {
        setText(content);
    }

    @Override
    public void setText(String content) {
        String[] parts = content.split("=");
        String keyStr = parts.length == 2 ? parts[0] : content;
        String eqStr = parts.length == 2 ? "=" : "";
        String valueStr = parts.length == 2 ? parts[1] : "";

        key.set(keyStr);
        eq.set(eqStr);
        value.set(valueStr);
    }

    @Override
    public String getText() {
        return key.getText() +  eq.getText() + value.getText();
    }

    @Override
    public void applyStyle(Style style) {
        key.setFill(style.getTextNormal());
        eq.setFill(style.getTextDanger());
        value.setFill(style.getTextPositive());
    }

    @Override
    public void applyStyle(ObjectProperty<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
