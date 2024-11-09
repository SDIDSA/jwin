package org.luke.gui.controls.tab;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

/**
 * extends the StackPane class and represents the title of a tab in a
 * TabPane. It provides methods for handling selection state and applying
 * styles.
 * <p>
 * Implements the Styleable interface to enable styling based on the current
 * style settings.
 * </p>
 *
 * @author SDIDSA
 */
public class TabTitle extends StackPane implements Styleable {
	private Label title;
	private BooleanProperty selected;

	/**
	 * Constructs a TabTitle with the specified window and tab.
	 *
	 * @param window The window associated with the TabTitle.
	 * @param tab    The tab associated with the TabTitle.
	 */
	public TabTitle(Window window, Tab tab) {
		setPadding(new Insets(5));
		tab.setTitleDisp(this);
		selected = new SimpleBooleanProperty(false);

		title = new Label(window, tab.getTitle(), new Font(12));
		tab.titleProperty().addListener((obs, ov, nv) -> title.setKey(nv));

		HBox.setHgrow(this, Priority.ALWAYS);

		getChildren().add(title);

		setCursor(Cursor.HAND);
		setOnMouseClicked(e -> tab.getOwner().select(tab));

		title.opacityProperty().bind(Bindings.when(selected).then(1).otherwise(.6));

		applyStyle(window.getStyl());
	}

	/**
	 * Selects the TabTitle.
	 */
	public void select() {
		selected.set(true);
	}

	/**
	 * Unselects the TabTitle.
	 */
	public void unselect() {
		selected.set(false);
	}

	@Override
	public void applyStyle(Style style) {
		title.setFill(style.getTextNormal());
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
