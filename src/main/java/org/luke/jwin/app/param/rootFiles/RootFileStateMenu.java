package org.luke.jwin.app.param.rootFiles;

import javafx.geometry.NodeOrientation;
import org.luke.gui.controls.image.ColoredIcon;
import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.KeyedMenuItem;
import org.luke.gui.controls.popup.context.items.MenuItem;
import org.luke.gui.controls.space.ExpandingHSpace;
import org.luke.gui.style.Style;
import org.luke.gui.window.Window;

import java.util.function.Consumer;

public class RootFileStateMenu extends ContextMenu {
    public RootFileStateMenu(Window window, Consumer<RootFileState> onState) {
        super(window);

        for(RootFileState state : RootFileState.values()) {
            if(state == RootFileState.UNSET) continue;
            MenuItem stateItem = new KeyedMenuItem(this, state.getText());

            ColoredIcon ic = new ColoredIcon(window, state.getText(), 32, 16, Style::getTextNormal);
            ic.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            stateItem.getChildren().addAll(new ExpandingHSpace(), ic);

            stateItem.setAction(() -> {
                if(onState != null)
                    onState.accept(state);
            });

            addMenuItem(stateItem);
        }
    }
}
