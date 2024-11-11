package org.luke.jwin.app.console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.Priority;
import org.luke.gui.controls.scroll.Scrollable;
import org.luke.gui.controls.space.ExpandingHSpace;
import org.luke.gui.controls.space.Separator;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class ConsoleOutput extends VBox implements Styleable {
	private final AtomicBoolean ignoreChanges = new AtomicBoolean(false);

	private final Window win;
	private final VBox lines;

	private final Scrollable sc;

	ConsoleToggleAction wrap;
	ConsoleToggleAction autoScroll;

	ArrayList<ConsoleLine> allLines;

	private final ConsoleAction stop;
	private final ConsoleLine emptyLine;

	public ConsoleOutput(Window win) {
		super(5);
		this.win = win;

		allLines = new ArrayList<>();

		lines = new VBox(6);
		setPadding(new Insets(5));
		lines.setPadding(new Insets(5));

		sc = new Scrollable();
		VBox.setVgrow(sc, Priority.ALWAYS);
		sc.setContent(lines);

		sc.setMinWidth(0);
		sc.maxWidthProperty().bind(widthProperty().subtract(10));

		wrap = new ConsoleToggleAction(win, "wrap", "soft_wrap");
		wrap.enabledProperty().addListener((_, ov, nv) -> {
			if (ov.booleanValue() != nv.booleanValue()) {
				allLines.forEach(l ->
						l.setWrappingWidth(wrap.isEnabled() ? (sc.getWidth() - 15) : 0));
			}
		});

		emptyLine = new ConsoleLine(win, "", ConsoleLineType.STDOUT, false);

		autoScroll = new ConsoleToggleAction(win, "auto-scroll", "auto_scroll_on_output");
		autoScroll.setEnabled(true);

		ConsoleAction erase = new ConsoleAction(win, "erase", "clear_all");
		ConsoleAction gup = new ConsoleAction(win, "scroll-up", "go_up");
		ConsoleAction gdown = new ConsoleAction(win, "scroll-up", "go_down");
		gdown.setRotate(180);

		erase.setDisable(true);
		lines.getChildren().addListener((ListChangeListener<? super Node>) _ -> {
			if(ignoreChanges.get()) {
				return;
			}
			erase.setDisable(allLines.isEmpty());
			Platform.runLater(() -> {
				ignoreChanges.set(true);
				lines.getChildren().remove(emptyLine);
				lines.getChildren().add(emptyLine);
				ignoreChanges.set(false);
			});
		});

		erase.setAction(() -> {
			allLines.clear();
			lines.getChildren().clear();
		});

		gup.setAction(() -> sc.getVerticalScrollBar().positionProperty().set(0));
		gdown.setAction(() -> sc.getVerticalScrollBar().positionProperty().set(1));

		ConsoleToggleAction consIn = new ConsoleToggleAction(win, "consin", "display_input_lines");
		consIn.setEnabled(true);
		ConsoleToggleAction consOut = new ConsoleToggleAction(win, "consout", "display_output_lines");
		consOut.setEnabled(true);
		ConsoleToggleAction consErr = new ConsoleToggleAction(win, "conserr", "display_error_lines");
		consErr.setEnabled(true);

		stop = new ConsoleAction(win, "stop", "stop_running_app");
		stop.setDisable(true);

		ChangeListener<Boolean> refreshLines = (_, _, _) ->
				refreshLines(consIn.isEnabled(), consOut.isEnabled(), consErr.isEnabled());

		consIn.enabledProperty().addListener(refreshLines);
		consOut.enabledProperty().addListener(refreshLines);
		consErr.enabledProperty().addListener(refreshLines);

		HBox top = new HBox(5, new ExpandingHSpace(), consIn, consOut, consErr,
				new Separator(win, Orientation.VERTICAL), wrap, autoScroll, new Separator(win, Orientation.VERTICAL),
				new HBox(gup, gdown), erase, stop);

		getChildren().addAll(top, new Separator(win, Orientation.HORIZONTAL), sc);

		applyStyle(win.getStyl());
	}

	public void setOnStop(Runnable onStop) {
		stop.setAction(onStop);
		stop.setDisable(onStop == null);
	}

	public void refreshLines(boolean input, boolean output, boolean err) {
		lines.getChildren().clear();

		HashMap<ConsoleLineType, Boolean> enabled = new HashMap<>();
		enabled.put(ConsoleLineType.IN, input);
		enabled.put(ConsoleLineType.STDOUT, output);
		enabled.put(ConsoleLineType.ERROUT, err);
		allLines.forEach(line -> {
			if (enabled.get(line.getType()))
				lines.getChildren().add(line);
		});
	}

	public void addLine(String line, ConsoleLineType type) {
		addLine(line, type, true);
	}

	public void addLine(String line, ConsoleLineType type, boolean keyed) {
		ConsoleLine l = new ConsoleLine(win, line, type, keyed);
		allLines.add(l);
		l.setWrappingWidth(wrap.isEnabled() ? (sc.getWidth() - 20) : 0);
		lines.getChildren().add(l);

		if (autoScroll.isEnabled()) {
			sc.getVerticalScrollBar().positionProperty().set(1);
		}
	}

	public void overrideLast(String content) {
		overrideLast(content, true);
	}

	public void overrideLast(String content, boolean keyed) {
		if (allLines.isEmpty()) {
			addOutput(content);
		} else {
			if(keyed) {
				allLines.getLast().setKey(content);
			}else {
				allLines.getLast().setText(content);
			}
		}
	}

	public void addError(String line, boolean keyed) {
		addLine(line, ConsoleLineType.ERROUT, keyed);
	}

	public void addOutput(String line, boolean keyed) {
		addLine(line, ConsoleLineType.STDOUT, keyed);
	}

	public void addInput(String line, boolean keyed) {
		addLine(line, ConsoleLineType.IN, keyed);
	}

	public void addError(String line) {
		addError(line, true);
	}

	public void addOutput(String line) {
		addOutput(line, true);
	}

	public void addInput(String line) {
		addInput(line, true);
	}

	@Override
	public void applyStyle(Style style) {
		setBackground(Backgrounds.make(style.getBackgroundTertiaryOr(), 8));
		setEffect(new DropShadow(10, Color.gray(0, .3)));
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

	public void clear() {
		allLines.clear();
		lines.getChildren().clear();
	}
}
