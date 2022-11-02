package org.luke.gui.controls.popup;

import org.luke.gui.factory.Backgrounds;
import org.luke.gui.factory.Borders;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.PopupControl;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class NodePopup extends PopupControl implements Styleable {
	protected Window owner;

	protected VBox root;

	public NodePopup(Window window) {
		this.owner = window;

		root = new VBox();

		setAutoHide(true);

		StackPane preroot = new StackPane();
		preroot.setPadding(new Insets(10));

		DropShadow ds = new DropShadow(15, Color.gray(0, .25));

		StackPane clipped = new StackPane();
		clipped.setEffect(ds);

		Rectangle clip = new Rectangle();
		clip.setArcHeight(20);
		clip.setArcWidth(20);
		clip.widthProperty().bind(root.widthProperty());
		clip.heightProperty().bind(root.heightProperty());

		root.setClip(clip);
		clipped.getChildren().add(root);

		preroot.getChildren().add(clipped);
		getScene().setRoot(preroot);

		applyStyle(window.getStyl());
	}

	public void showPop(Node node) {
		setOnShown(e -> {
			Bounds bounds = node.getBoundsInLocal();
			Bounds screenBounds = node.localToScreen(bounds);
			int x = (int) screenBounds.getMinX();
			int y = (int) screenBounds.getMinY();

			double px = x - getWidth() / 2 + (node.getBoundsInLocal().getMinX() + node.getBoundsInLocal().getMaxX()) / 2;

			setX(px);
			setY(y - getHeight());
		});
		this.show(owner);
	}

	@Override
	public void applyStyle(Style style) {
		root.setBackground(Backgrounds.make(style.getBackgroundPrimary(), 11.0));
		root.setBorder(Borders.make(style.getBackgroundFloating(), 10.0));
	}
	
	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
