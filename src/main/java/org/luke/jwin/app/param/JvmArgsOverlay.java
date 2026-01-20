package org.luke.jwin.app.param;

import javafx.beans.binding.Bindings;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.HBox;
import org.apache.commons.exec.CommandLine;
import org.luke.gui.controls.Font;
import org.luke.gui.controls.alert.BasicOverlay;
import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.style.Style;
import org.luke.gui.window.Page;
import org.luke.jwin.app.console.ConsoleLine;
import org.luke.jwin.app.console.ConsoleLineType;
import org.luke.jwin.app.console.ConsoleOutput;
import org.luke.jwin.app.layout.JwinUi;
import org.luke.jwin.ui.TextVal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class JvmArgsOverlay extends BasicOverlay {
    private final ColorIcon send;
    private final ConsoleOutput output;

    public JvmArgsOverlay(Page ps, JwinUi config) {
        super(ps, 550);
        removeTop();
        removeSubHead();

        head.setKey("jvm_args_overlay");

        output = new ConsoleOutput(ps.getWindow());
        output.hideControls();
        output.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);

        TextVal input = new TextVal(getWindow(), "jvm_args_add");
        input.setInputFont(new Font(Font.DEFAULT_MONO_FAMILY, 16));

        send = new ColorIcon("upload", 24);
        send.setTranslateY(-8);
        send.setCursor(Cursor.HAND);
        send.opacityProperty().bind(Bindings.when(send.hoverProperty()).then(1).otherwise(.6));

        send.setAction(() -> {
            String line = input.getValue();
            for (String argument : parse(line)) {
                output.addLine(argument, ConsoleLineType.ARG);
            }
            input.setValue("");
        });

        input.setAction(send::fire);

        output.setMinHeight(230);
        output.setMaxHeight(230);

        output.setMinWidth(0);
        output.maxWidthProperty().bind(center.widthProperty().subtract(32));

        HBox preInput = new HBox(10, input, send);
        preInput.setAlignment(Pos.BOTTOM_CENTER);

        center.getChildren().addAll(output, preInput);

        addOnShowing(() -> {
            output.clear();
            String vmArgs = config.getJre().getJvmArgs();
            for (String arg : parse(vmArgs)) {
                output.addLine(arg, ConsoleLineType.ARG);
            }
        });

        send.disableProperty().bind(input.valueProperty().length().lessThan(3));
        send.disableProperty().addListener(_ -> {
            applyStyle(ps.getWindow().getStyl().get());
        });

        done.setDisable(false);

        done.setAction(() -> {
            hide();
            config.getJre().setJvmArgs(stringify());
        });

        applyStyle(ps.getWindow().getStyl());
    }

    public List<String> parse(String args) {
        return new ArrayList<>(
                Arrays.asList(
                        CommandLine.parse("java " + args)
                                .getArguments()
                )
        );
    }

    public String stringify() {
        return String.join(" ", output.getAllLines().stream().map(ConsoleLine::getText).toList());
    }

    @Override
    public void applyStyle(Style style) {
        send.setFill(send.isDisable() ? style.getTextDanger() : style.getHeaderPrimary());
        super.applyStyle(style);
    }
}