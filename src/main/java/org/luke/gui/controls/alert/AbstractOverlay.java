package org.luke.gui.controls.alert;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.button.Button;
import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.controls.label.MultiText;
import org.luke.gui.controls.space.ExpandingHSpace;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Page;

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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontWeight;

/**
 * Base class for creating customizable overlay dialogs. It provides a framework
 * for creating overlays with a close icon, top text, customizable content in
 * the center, and optional buttons at the bottom. Subclasses must implement
 * specific content and actions.
 *
 * @author SDIDSA
 */
public abstract class AbstractOverlay extends Overlay implements Styleable {
	private final StackPane preRoot;
	private final HBox bottom;
	private final Button cancel;

	protected ColorIcon closeIcon;
	protected VBox root;
	protected Button done;
	protected MultiText top;
	protected VBox center;

	/**
	 * Constructs an AbstractOverlay with the specified Page and width.
	 *
	 * @param session The Page to which the overlay belongs.
	 * @param width   The preferred width of the overlay.
	 */
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
		closeIcon.setOpacity(.8);

		StackPane.setMargin(closeIcon, new Insets(10));

		top = new MultiText(getWindow(), "", new Font(Font.DEFAULT_FAMILY_MEDIUM, 16));
		top.setMouseTransparent(true);
		top.setPadding(new Insets(16, 20, 24, 16));

		center = new VBox(16);
		center.setMinWidth(width);
		center.setMaxWidth(width);
		center.setPadding(new Insets(0, 16, 16, 16));

		Rectangle clip = new Rectangle();
		clip.widthProperty().bind(center.widthProperty());
		clip.heightProperty().bind(center.heightProperty());
		center.setClip(clip);

		root.getChildren().addAll(top, center);

		root.setPickOnBounds(false);
		center.setPickOnBounds(false);

		preRoot.getChildren().addAll(closeIcon, root);

		bottom = new HBox(8);
		bottom.setMaxWidth(width);
		bottom.setPadding(new Insets(16));
		bottom.setAlignment(Pos.CENTER);

		cancel = new Button(session.getWindow(), "cancel", 5.0, 16, 38);
		cancel.setFont(new Font(Font.DEFAULT_FAMILY_MEDIUM, 14));
		cancel.setFill(Color.TRANSPARENT);
		cancel.setUlOnHover(true);
		cancel.setAction(this::hide);

		done = new AlertButton(this, ButtonType.DONE);
		done.setFont(new Font(14, FontWeight.BOLD));

		bottom.getChildren().addAll(new ExpandingHSpace(), cancel, done);

		setContent(preRoot, bottom);
	}

	/**
	 * Constructs an AbstractOverlay with a default width of 440.
	 *
	 * @param session The Page to which the overlay belongs.
	 */
	protected AbstractOverlay(Page session) {
		this(session, 440);
	}

	/**
	 * Removes the bottom section containing buttons from the overlay.
	 */
	public void removeBottom() {
		removeContent(bottom);
	}

	public void removeDone() {
		bottom.getChildren().remove(done);
	}

	public void removeClose() {
		preRoot.getChildren().remove(closeIcon);
	}

	/**
	 * Adds a node to the bottom section of the overlay at the specified index.
	 *
	 * @param index The index at which to add the node.
	 * @param node  The node to add to the bottom section.
	 */
	public void addToBottom(int index, Node node) {
		bottom.getChildren().add(index, node);
	}

	/**
	 * Removes the top text section from the overlay.
	 */
	public void removeTop() {
		root.getChildren().remove(top);
	}

	/**
	 * Removes the cancel button from the bottom section.
	 */
	public void removeCancel() {
		bottom.getChildren().remove(cancel);
	}

	/**
	 * Sets the top text of the overlay.
	 *
	 * @param text The text to set as the top text.
	 */
	public void setTop(String text) {
		top.setKey(text);
	}

	/**
	 * Gets a BooleanProperty representing the disabled state of the 'Done' button.
	 *
	 * @return The BooleanProperty representing the disabled state of the 'Done'
	 *         button.
	 */
	public BooleanProperty doneDisabled() {
		return done.disableProperty();
	}

	/**
	 * Sets the text of the 'Done' button.
	 *
	 * @param text The text to set for the 'Done' button.
	 */
	public void setButtonText(String text) {
		done.setKey(text);
	}

	/**
	 * Sets the action to be performed when the 'Done' button is clicked.
	 *
	 * @param run The action to be performed.
	 */
	public void setAction(Runnable run) {
		done.setAction(run);
	}

	/**
	 * Sets the action to be performed when the overlay is canceled (closed).
	 *
	 * @param action The action to be performed.
	 */
	public void setOnCancel(Runnable action) {
		Runnable onClose = () -> {
			hide();
			action.run();
		};
		closeIcon.setAction(onClose);
		cancel.setAction(onClose);
	}

	/**
	 * Initiates the loading state of the 'Done' button.
	 */
	public void startLoading() {
		done.startLoading();
	}

	/**
	 * Stops the loading state of the 'Done' button.
	 */
	public void stopLoading() {
		done.stopLoading();
	}

	@Override
	public void applyStyle(Style style) {
		preRoot.backgroundProperty()
				.bind(Bindings.when(bottom.sceneProperty().isNull())
						.then(Backgrounds.make(style.getBackgroundPrimaryOr(), 8)).otherwise(
								Backgrounds.make(style.getBackgroundPrimaryOr(), new CornerRadii(8, 8, 0, 0, false))));

		bottom.setBackground(Backgrounds.make(style.getBackgroundSecondary(), new CornerRadii(0, 0, 8, 8, false)));

		top.setFill(style.getTextNormal());

		cancel.setTextFill(style.getLinkButtonText());

		done.setFill(style.getAccent());
		done.setTextFill(style.getTextOnAccent());

		closeIcon.fillProperty().bind(Bindings.when(closeIcon.hoverProperty()).then(style.getHeaderPrimary())
				.otherwise(style.getHeaderSecondary()));
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
