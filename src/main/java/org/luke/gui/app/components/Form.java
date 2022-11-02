package org.luke.gui.app.components;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.luke.gui.app.components.input.InputField;
import org.luke.gui.controls.button.AbstractButton;

import javafx.scene.input.KeyCode;

public class Form {
	private ArrayList<InputField> fields;

	private AbstractButton defaultButton;

	public Form() {
		super();

		fields = new ArrayList<>();
	}

	public Form(List<InputField> nodesOfType) {
		this();
		fields.addAll(nodesOfType);
		nodesOfType.forEach(this::prepareField);
	}

	private void prepareField(InputField input) {
		input.addOnKeyPressed(pressed -> {
			if (pressed.equals(KeyCode.ENTER) && defaultButton != null) {
				defaultButton.fire();
			}
		});
	}

	public void addAll(InputField... fields) {
		for (InputField field : fields) {
			this.fields.add(field);
			prepareField(field);
		}
	}

	public boolean check() {
		boolean success = true;
		for (InputField field : fields) {
			if (field.getValue().isEmpty()) {
				field.setError("field_required");
				success = false;
			} else if (field.getKey().equals("confirm_new_password") && !field.getValue().equals(get("new_password"))) {
				field.setError("passwords_no_match");
				success = false;
			} else if (field.getKey().contains("password") && field.getValue().length() < 6) {
				field.setError("password_short");
				success = false;
			} else {
				field.removeError();
			}
		}
		return success;
	}

	public void applyErrors(JSONArray errors) {
		for (Object obj : errors) {
			JSONObject err = (JSONObject) obj;

			String key = err.getString("key");

			for (InputField field : fields) {
				if (field.getKey().equals(key)) {
					String val = err.getString("value");
					String plus = err.has("plus") ? err.getString("plus") : null;

					if (plus != null && val.equals("username_invalid_char")) {
						plus = " " + plus + "  " + Character.getName(plus.charAt(0));
					}

					field.setError(val, plus);
				}
			}
		}
	}

	public void clearErrors() {
		fields.forEach(InputField::removeError);
	}

	public void setDefaultButton(AbstractButton button) {
		defaultButton = button;
	}

	public void loadData(JSONArray data) {
		for (Object obj : data) {
			JSONObject item = (JSONObject) obj;
			setField(item.getString("key"), item.getString("value"));
		}
	}

	public void setField(String key, String value) {
		for (InputField field : fields) {
			if (field.getKey().equals(key)) {
				field.setValue(value);
				field.removeError();
			}
		}
	}

	public String get(String key) {
		for (InputField field : fields) {
			if (field.getKey().equals(key)) {
				return field.getValue();
			}
		}

		return null;
	}

	/**
	 * Clear the content of all the input fields in this form using
	 * {@link InputField#clear()}
	 */
	public void clear() {
		fields.forEach(InputField::clear);
	}
}
