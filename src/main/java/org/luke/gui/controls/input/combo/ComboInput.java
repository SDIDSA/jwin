package org.luke.gui.controls.input.combo;

import java.util.ArrayList;
import java.util.function.BiFunction;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.input.Input;
import org.luke.gui.controls.input.styles.DeprecatedInputStyle;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.shape.Triangle;
import org.luke.gui.locale.Locale;
import org.luke.gui.locale.Localized;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class ComboInput extends Input implements Styleable, Localized {
	private final String key;

	private final Text prompt;
	private final Text base;

	private final Triangle arrow;

	private final ArrayList<ComboItem> items;
	private final ContextMenu popup;

	private ComboItem selected;

	private long lastKey = 0;
	private String acc = "";

	public ComboInput(Window window, Font font, String key) {
		super(key);

		this.key = key;
		
		inputStyle = new DeprecatedInputStyle(this);

		setFocusTraversable(true);
		setAlignment(Pos.CENTER_LEFT);

		base = new Text();

		prompt = new Text();

		prompt.opacityProperty().bind(Bindings.when(base.textProperty().isEmpty()).then(.5).otherwise(0));

		setPadding(new Insets(10,15,10,15));

		StackPane over = new StackPane();
		over.setAlignment(Pos.CENTER_RIGHT);

		arrow = new Triangle(8);
		arrow.setRotate(-90);
		arrow.setOpacity(.7);

		over.getChildren().add(arrow);

		getChildren().addAll(base, prompt, over);

		setFont(font);

		items = new ArrayList<>();
		popup = new ContextMenu(window);
		setOnMouseClicked(_ -> {
			requestFocus();
			popup.showPop(this, Direction.DOWN_LEFT, 10);
		});

		focusedProperty().addListener((_, _, nv) -> {
			inputStyle.focus(nv);
			if (!nv) {
				popup.hide();
			}
		});

		setOnKeyPressed(e -> {
			int i = items.indexOf(selected);
			switch (e.getCode()) {
			case UP:
				setValue(items.get((i + 1) % items.size()));
				e.consume();
				break;
			case DOWN:
				setValue(items.get(i > 0 ? (i - 1) : items.size() - 1));
				e.consume();
				break;
			default:
				break;
			}
		});

		setOnKeyTyped(e -> {
			char c = e.getCharacter().charAt(0);
			if (Character.isAlphabetic(c) || Character.isDigit(c)) {
				long now = System.currentTimeMillis();

				if (now - lastKey > 500) {
					acc = "";
				}

				acc += c;
				lastKey = now;

				search(acc);
			}
		});

		setCursor(Cursor.HAND);

		applyStyle(window.getStyl());
		applyLocale(window.getLocale());
	}

	public BiFunction<ContextMenu, String, ComboItem> creator = null;
	
	public void setCreator(BiFunction<ContextMenu, String, ComboItem> creator) {
		this.creator = creator;
	}

	public void addItem(ComboItem item) {
		if (items.contains(item)) {
			return;
		}
		items.add(item);
		item.menuItem().setAction(() -> setValue(item));
		popup.addMenuItem(item.menuItem());
	}

	public void addItems(ComboItem... items) {
		for (ComboItem item : items) {
			addItem(item);
		}
	}
	
	public void clearItems() {
		items.clear();
		popup.clear();
	}

	public void setValue(ComboItem value) {
		selected = value;

		base.setText(value.getDisplay());
		this.value.set(value.getValue());
	}

	private void search(String value) {
		for (ComboItem item : items) {
			if (item.match(value)) {
				setValue(item);
				return;
			}
		}
	}
	
	public ContextMenu getPopup() {
		return popup;
	}

	@Override
	public void setFont(Font font) {
		base.setFont(font.getFont());
		prompt.setFont(font.getFont());
	}

	@Override
	public boolean isFocus() {
		return isFocused();
	}

	@Override
	public String getValue() {
		return selected != null ? selected.getValue() : "";
	}

	@Override
	public void setValue(String value) {
		if (value.isEmpty()) {
			selected = null;
			base.textProperty().unbind();
			base.setText("--");
			this.value.unbind();
			this.value.set("--");
		} else {
			for (ComboItem item : items) {
				if (item.getValue().equals(value)) {
					setValue(item);
					return;
				}
			}
		}
	}

	@Override
	public void clear() {
		setValue("");
	}

	@Override
	public void applyStyle(Style style) {
		inputStyle.applyStyle(style);
		popup.applyStyle(style);

		base.setFill(style.getTextNormal());
		prompt.setFill(style.getHeaderSecondary());
		arrow.setFill(style.getTextNormal());
	}

	@Override
	public void applyLocale(Locale locale) {
		prompt.setText(locale.get(key));
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
	
	@Override
	public void applyLocale(ObjectProperty<Locale> locale) {
		Localized.bindLocale(this, locale);
	}

}
