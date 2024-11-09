package org.luke.jwin.app.param.rootFiles;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.*;
import org.luke.gui.controls.Font;
import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.controls.label.unkeyed.Text;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.controls.popup.tooltip.KeyedTooltip;
import org.luke.gui.controls.popup.tooltip.Tooltip;
import org.luke.gui.controls.space.ExpandingHSpace;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.factory.Borders;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;
import org.luke.jwin.app.file.RootFileScanner;
import org.luke.jwin.app.layout.JwinUi;

import java.io.File;
import java.util.function.Consumer;

public class RootFileEntry extends VBox implements Styleable {
    private RootFileType type;
    private RootFileState state;

    private final ColorIcon stateButton;
    private final ColorIcon categIcon;

    private final Text fileName;
    private final Label categName;
    public RootFileEntry(Window window, JwinUi config, File file, RootFileScanner.DetectedFile df) {
        super(10);
        setPadding(new Insets(10));
        fileName = new Text(file.getName(), new Font(14));
        categName = new Label(window, df.category(), new Font(12));
        type = RootFileScanner.type(file);
        state = RootFileScanner.state(file, config);

        setOpacity(type.getOpacity());

        stateButton = new ColorIcon(state.getText(), 32, 20, true);
        stateButton.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        stateButton.setCursor(Cursor.HAND);

        KeyedTooltip ttp = new KeyedTooltip(window, state.getText(), Direction.UP, 0,15);
        Tooltip.install(stateButton, ttp);;

        categIcon = new ColorIcon(df.iconName(), 32, 16, false);

        RootFileStateMenu menu = new RootFileStateMenu(window, state -> {
            if(state == RootFileState.UNSET) {
                config.getRootFiles().getExclude().remove(file);
                config.getRootFiles().getFiles().remove(file);
            }
            if(state == RootFileState.INCLUDED && !config.getRootFiles().getFiles().contains(file)) {
                config.getRootFiles().getExclude().remove(file);
                config.getRootFiles().getFiles().add(file);
            }
            if(state == RootFileState.EXCLUDED && !config.getRootFiles().getExclude().contains(file)) {
                config.getRootFiles().getFiles().remove(file);
                config.getRootFiles().getExclude().add(file);
            }
            stateButton.setImage(state.getText());
            ttp.setKey(state.getText());
        });
        stateButton.setAction(() -> {
            menu.showPop(stateButton, Direction.DOWN, 0, 15);
        });

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(categIcon, categName);

        HBox bottom = new HBox(5, fileName, new ExpandingHSpace(), stateButton);
        bottom.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(top, bottom);

        applyStyle(window.getStyl());
    }

    @Override
    public void applyStyle(Style style) {
        fileName.setFill(style.getTextNormal());
        categName.setFill(style.getTextMuted());
        stateButton.setFill(style.getTextNormal());
        categIcon.setFill(style.getTextMuted());
        setBackground(Backgrounds.make(style.getBackgroundTertiary(), 8));
        setBorder(Borders.make(type.getColor().apply(style), 8));

        stateButton.backgroundProperty().bind(Bindings.when(stateButton.hoverProperty())
                .then(Backgrounds.make(style.getBackgroundSecondary(), 6))
                .otherwise(Background.EMPTY));
    }

    @Override
    public void applyStyle(ObjectProperty<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
