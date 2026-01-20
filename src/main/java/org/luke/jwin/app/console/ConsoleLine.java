package org.luke.jwin.app.console;

public interface ConsoleLine {
    void setWrappingWidth(double v);

    ConsoleLineType getType();

    void setKey(String content);

    void setText(String content);

    String getText();
}
