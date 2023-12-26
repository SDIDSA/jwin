package org.luke.gui.controls.input;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.KeyedMenuItem;
import org.luke.gui.locale.Locale;
import org.luke.gui.locale.Localized;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;


/**
 * Represents a text input control with additional functionality. Provides
 * features like copying, cutting, pasting, context menu, etc. Extends {@link Input} and
 * implements {@link Styleable}, {@link Localized}.
 * 
 * @author SDIDSA
 */
public class TextInput extends Input implements Styleable, Localized {
	private Window window;
	private TextInputControl field;
	private HBox preField;

	private AtomicInteger caretPos;

	private BooleanProperty notSelected;
	private ObjectProperty<IndexRange> selection;

	private ContextMenu menu;

	private Runnable action;

	public TextInput(Window window, Font font, String key, boolean hidden) {
		super(key);

		this.window = window;

		field = hidden ? new PasswordField() : new TextField();
		field.setBackground(Background.EMPTY);
		field.setBorder(Border.EMPTY);
		field.setPadding(new Insets(10));

		field.focusedProperty().addListener((obs, ov, nv) -> inputStyle.focus(nv));

		caretPos = new AtomicInteger();
		field.caretPositionProperty().addListener((obs, ov, nv) -> {
			if (field.isFocused()) {
				caretPos.set(nv.intValue());
			}
		});

		notSelected = new SimpleBooleanProperty(true);
		selection = new SimpleObjectProperty<>(field.getSelection());
		field.selectionProperty().addListener((obs, ov, nv) -> {
			if (field.isFocused() && !hidden) {
				selection.set(nv);
				notSelected.set(nv.getLength() == 0);
			}
		});

		value.bind(field.textProperty());

		preField = new HBox();
		preField.setAlignment(Pos.CENTER);
		preField.getChildren().add(field);

		HBox.setHgrow(field, Priority.ALWAYS);

		getChildren().add(preField);

		field.setCache(true);
		field.setCacheHint(CacheHint.SPEED);

		field.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER && action != null) {
				e.consume();
				action.run();
			}
		});

		prepareMenu(window);

		setFont(font);

		applyLocale(window.getLocale());
	}

	public void setAction(Runnable action) {
		this.action = action;
	}

	public void setEditable(boolean editable) {
		field.setEditable(editable);
	}

	public void setRadius(double radius) {
		inputStyle.setRadius(radius);
	}

	public void setFieldPadding(Insets insets) {
		field.setPadding(insets);
	}

	public boolean isMenuShowing() {
		return menu.isShowing();
	}

	public ReadOnlyBooleanProperty menuShowingProperty() {
		return menu.showingProperty();
	}

	private void prepareMenu(Window window) {
		menu = new ContextMenu(window);
		KeyedMenuItem copy = new KeyedMenuItem(menu, "copy", null);
		copy.setAccelerator("ctrl+c");
		copy.setAction(this::copy);

		KeyedMenuItem cut = new KeyedMenuItem(menu, "cut", null);
		cut.setAccelerator("ctrl+x");
		cut.setAction(this::cut);

		KeyedMenuItem paste = new KeyedMenuItem(menu, "paste", null);
		paste.setAccelerator("ctrl+v");
		paste.setAction(this::paste);

		menu.addMenuItem(copy);
		menu.addMenuItem(cut);
		menu.addMenuItem(paste);

		Consumer<Boolean> checkSelection = nv -> {
			if (nv.booleanValue()) {
				menu.disable(cut);
				menu.disable(copy);
			} else {
				menu.enableFirst(copy);
				menu.enableAfter(cut, copy);
			}
		};

		notSelected().addListener((obs, ov, nv) -> checkSelection.accept(nv));

		checkSelection.accept(notSelected().getValue());

		field.setOnContextMenuRequested(e -> menu.showPop(this, e));

		addEventFilter(MouseEvent.MOUSE_PRESSED, e -> menu.hide());
	}

	public void align(Pos pos) {
		if (field instanceof PasswordField passwordField) {
			passwordField.setAlignment(pos);
		} else if (field instanceof TextField textField) {
			textField.setAlignment(pos);
		}
	}

	public void setFocusable(boolean focusable) {
		field.setFocusTraversable(focusable);
		if (!focusable) {
			setMouseTransparent(true);
		}
	}

	boolean keyedPrompt = false;
	String promptKey;

	public void setPrompt(String key, boolean keyedPrompt) {
		this.keyedPrompt = keyedPrompt;
		if (keyedPrompt) {
			promptKey = key;
			applyLocale(window.getLocale().get());
		} else {
			field.setPromptText(key);
		}
	}

	public void setPrompt(String key) {
		setPrompt(key, false);
	}

	public void setKeyedPrompt(String key) {
		setPrompt(key, true);
	}

	public void addPostField(Node... nodes) {
		preField.getChildren().addAll(nodes);
	}

	public void addPreField(Node node) {
		preField.getChildren().add(0, node);
	}

	public void addPreField(Node... nodes) {
		for (Node node : nodes) {
			addPreField(node);
		}
	}

	public TextInput(Window window, Font font, String key) {
		this(window, font, key, false);
	}

	public void positionCaret(int pos) {
		field.positionCaret(pos);
	}

	@Override
	public void setFont(Font font) {
		field.setFont(font.getFont());
	}

	@Override
	public boolean isFocus() {
		return field.isFocused();
	}

	public ReadOnlyBooleanProperty focusProperty() {
		return field.focusedProperty();
	}

	@Override
	public String getValue() {
		return field.getText();
	}

	@Override
	public void setValue(String value) {
		field.setText(value);
	}

	@Override
	public void clear() {
		setValue("");
	}

	@Override
	public void requestFocus() {
		int pos = caretPos.get();
		field.requestFocus();
		field.deselect();
		field.positionCaret(pos);
	}

	public void copy() {
		String selected = field.getText().substring(selection.get().getStart(), selection.get().getEnd());
		ClipboardContent cpc = new ClipboardContent();
		cpc.putString(selected);
		Clipboard.getSystemClipboard().setContent(cpc);
		caretPos.set(selection.get().getEnd());
	}

	public void cut() {
		copy();
		field.replaceText(selection.get(), "");
		caretPos.set(selection.get().getStart());

		selection.set(field.getSelection());
		notSelected.set(true);
	}

	public void paste() {
		if (notSelected.get()) {
			field.positionCaret(caretPos.getAndAdd(Clipboard.getSystemClipboard().getString().length()));
			field.paste();
		} else {
			field.replaceText(selection.get(), Clipboard.getSystemClipboard().getString());
		}

		selection.set(field.getSelection());
		notSelected.set(true);
	}

	public BooleanProperty notSelected() {
		return notSelected;
	}

	@Override
	public void applyStyle(Style style) {
		inputStyle.applyStyle(style);

		Color tx = style.getTextNormal();
		field.setStyle("-fx-text-fill: " + Styleable.colorToCss(tx) + ";-fx-prompt-text-fill: "
				+ Styleable.colorToCss(tx.deriveColor(0, 1, 1, .3))
				+ ";-fx-background-color:transparent;-fx-text-box-border: transparent;");
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

	@Override
	public void applyLocale(Locale locale) {
		if (keyedPrompt)
			field.setPromptText(locale.get(promptKey));
	}

	@Override
	public void applyLocale(ObjectProperty<Locale> locale) {
		Localized.bindLocale(this, locale);
	}

}
