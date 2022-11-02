package org.luke.gui.app.components.input;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.input.TextInput;
import org.luke.gui.controls.input.styles.InputStyle;
import org.luke.gui.style.Style;
import org.luke.gui.window.Window;

import javafx.scene.Node;

public class TextInputField extends InputField {
	protected TextInput input;

	public TextInputField(Window window, String key, double width, boolean hidden) {
		super(window, key, width);

		input = new TextInput(window, new Font(14), key, hidden);

		value.bind(input.valueProperty());

		addInput(input);
	}

	public void setPrompt(String prompt) {
		input.setPrompt(prompt);
	}

	public void setInputStyle(InputStyle style) {
		input.setInputStyle(style);
	}

	public TextInputField(Window window, String key, boolean hidden) {
		this(window, key, 200, hidden);
	}

	public TextInputField(Window window, String key, double width) {
		this(window, key, width, false);
	}

	public TextInputField(Window window, String key) {
		this(window, key, 200, false);
	}

	public void addPostField(Node... nodes) {
		input.addPostField(nodes);
	}

	public void addPreField(Node... nodes) {
		input.addPreField(nodes);
	}

	public void positionCaret(int pos) {
		input.positionCaret(pos);
	}

	@Override
	public void setValue(String value) {
		input.setValue(value);
	}

	@Override
	public void requestFocus() {
		input.requestFocus();
	}

	@Override
	public void applyStyle(Style style) {
		input.applyStyle(style);
		super.applyStyle(style);
	}

}
