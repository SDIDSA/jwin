package org.luke.gui.controls.alert;

import org.json.JSONArray;
import org.luke.gui.app.components.Form;
import org.luke.gui.app.pages.Page;
import org.luke.gui.controls.Font;
import org.luke.gui.controls.button.Button;
import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.controls.label.MultiText;
import org.luke.gui.controls.space.ExpandingHSpace;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;

public abstract class AbstractOverlay extends Overlay implements Styleable {
	private StackPane preRoot;
	private HBox bottom;
	private Button cancel;

	protected ColorIcon closeIcon;
	protected VBox root;
	protected Button done;
	protected MultiText top;
	protected VBox center;

	protected Form form;

	protected AbstractOverlay(Page session, double width) {
		super(session);

		root = new VBox();
		root.setMinWidth(width);
		root.setMaxWidth(width);

		preRoot = new StackPane();
		preRoot.setAlignment(Pos.TOP_RIGHT);
		preRoot.setMinWidth(width);
		preRoot.setMaxWidth(width);

		closeIcon = new ColorIcon("close", 16, true);
		closeIcon.setPadding(8);
		closeIcon.setAction(this::hide);
		closeIcon.setCursor(Cursor.HAND);
		closeIcon.applyStyle(session.getWindow().getStyl());

		StackPane.setMargin(closeIcon, new Insets(10));

		top = new MultiText(getWindow(), "", new Font(Font.DEFAULT_FAMILY_MEDIUM, 16));
		top.setMouseTransparent(true);
		top.setPadding(new Insets(16, 20, 24, 16));

		center = new VBox(16);
		center.setMinWidth(width);
		center.setMaxWidth(width);
		center.setPadding(new Insets(0, 16, 16, 16));

		root.getChildren().addAll(top, center);

		root.setPickOnBounds(false);
		center.setPickOnBounds(false);

		preRoot.getChildren().addAll(closeIcon, root);

		bottom = new HBox(8);
		bottom.setMaxWidth(width);
		bottom.setPadding(new Insets(16));

		cancel = new Button(session.getWindow(), "cancel", 7.0, 16, 38);
		cancel.setFont(new Font(Font.DEFAULT_FAMILY_MEDIUM, 14));
		cancel.setFill(Color.TRANSPARENT);
		cancel.setUlOnHover(true);
		cancel.setAction(this::hide);

		done = new Button(session.getWindow(), "done", 7.0, 16 , 38);
		done.setFont(new Font(14, FontWeight.BOLD));

		bottom.getChildren().addAll(new ExpandingHSpace(), cancel, done);

		setContent(preRoot, bottom);

		form = new Form();
		form.setDefaultButton(done);
	}
	
	public void addToBottom(int index, Node node) {
		bottom.getChildren().add(index, node);
	}
	
	public void removeTop() {
		root.getChildren().remove(top);
	}

	public void setTop(String text) {
		top.setKey(text);
	}

	protected AbstractOverlay(Page session) {
		this(session, 440);
	}

	public void applyErrors(JSONArray errors) {
		form.applyErrors(errors);
	}

	public BooleanProperty doneDisabled() {
		return done.disableProperty();
	}

	public void setButtonText(String text) {
		done.setKey(text);
	}

	public void setAction(Runnable run) {
		done.setAction(() -> {
			if (form.check())
				run.run();
		});
	}

	public void setOnCancel(Runnable action) {
		Runnable onClose = () -> {
			hide();
			action.run();
		};
		closeIcon.setAction(onClose);
		cancel.setAction(onClose);
	}

	public void startLoading() {
		done.startLoading();
	}

	public void stopLoading() {
		done.stopLoading();
	}

	@Override
	public void hide() {
		form.clearErrors();
		super.hide();
	}

	@Override
	public void applyStyle(Style style) {
		preRoot.setBackground(Backgrounds.make(style.getBackgroundPrimary(), new CornerRadii(8, 8, 0, 0, false)));
		bottom.setBackground(Backgrounds.make(style.getBackgroundSecondary(), new CornerRadii(0, 0, 8, 8, false)));

		top.setFill(style.getTextNormal());

		cancel.setTextFill(style.getLinkButtonText());

		done.setFill(style.getAccent());
		done.setTextFill(Color.WHITE);

		closeIcon.fillProperty().bind(Bindings.when(closeIcon.hoverProperty()).then(style.getHeaderPrimary())
				.otherwise(style.getHeaderSecondary()));
	}
	
	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
