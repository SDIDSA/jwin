package org.luke.jwin.app.file;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import org.luke.gui.controls.alert.Overlay;
import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Page;
import org.luke.gui.window.Window;

public class DragDropOverlay extends Overlay implements Styleable {
    private final VBox root;
    private final ColorIcon icon;
    private final Label text;

    public DragDropOverlay(Page owner, Window window) {
        super(owner, window);

        root = new VBox(60);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setMaxWidth(600);
        icon = new ColorIcon("import-java", 256, 128);
        text = new Label(window, "");

        root.getChildren().addAll(icon, text);

        addContent(root);

        applyStyle(window.getStyl());
    }

    public void setMode(ImportMode mode) {
        text.setKey(mode.getText());
        icon.setImage(mode.getIcon());
    }

    @Override
    public void applyStyle(Style style) {
        icon.setFill(style.getTextMuted());
        text.setFill(style.getHeaderSecondary());
        root.setBackground(Backgrounds.make(style.getBackgroundFloating(), 10));
    }

    @Override
    public void applyStyle(ObjectProperty<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
