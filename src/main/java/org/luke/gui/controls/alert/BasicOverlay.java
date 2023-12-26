package org.luke.gui.controls.alert;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.label.TextTransform;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.style.Style;
import org.luke.gui.window.Page;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;

/**
 * serves as the foundation for overlay components in the application. It
 * extends AbstractOverlay and provides a simple layout structure with a main
 * VBox containing a head and subhead label. Subclasses can extend this class to
 * create specific overlays with customized content.
 *
 * @author SDIDSA
 */
public abstract class BasicOverlay extends AbstractOverlay {

	protected Label head;
	protected Label subHead;
	private VBox mtop;

	/**
	 * Constructs a BasicOverlay with the specified session and width.
	 *
	 * @param session The Page session associated with the overlay.
	 * @param width   The width of the overlay.
	 */
	protected BasicOverlay(Page session, double width) {
		super(session, width);

		StackPane.setMargin(closeIcon, new Insets(16));

		mtop = new VBox(8);
		mtop.setPadding(new Insets(26, 16, 26, 16));
		mtop.setAlignment(Pos.CENTER);
		mtop.setMouseTransparent(true);

		head = new Label(session.getWindow(), "", new Font(24, FontWeight.BOLD));
		head.setTransform(TextTransform.CAPITALIZE_PHRASE);

		subHead = new Label(session.getWindow(), "", new Font(15));
		subHead.setTransform(TextTransform.CAPITALIZE_PHRASE);

		mtop.getChildren().addAll(head, subHead);

		root.getChildren().add(0, mtop);
	}

	/**
	 * Constructs a BasicOverlay with the specified session and default width.
	 *
	 * @param session The Page session associated with the overlay.
	 */
	protected BasicOverlay(Page session) {
		this(session, 460);
	}

	/**
	 * Removes the subHead label from the overlay.
	 */
	public void removeSubHead() {
		mtop.getChildren().remove(subHead);
	}
	
	@Override
	public void applyStyle(Style style) {
		super.applyStyle(style);

		head.setFill(style.getHeaderPrimary());
		subHead.setFill(style.getHeaderSecondary());
	}
}
