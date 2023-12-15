package org.luke.gui.controls.scroll;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class Scrollable extends StackPane {
	private StackPane contentCont;

	private ScrollBar sb;
	
	public Scrollable() {
		setAlignment(Pos.TOP_LEFT);
		setMinHeight(0);

		contentCont = new StackPane();
		StackPane scrollBarCont = new StackPane();
		scrollBarCont.setAlignment(Pos.CENTER_RIGHT);

		sb = new ScrollBar(15, 5);
		scrollBarCont.setPickOnBounds(false);
		scrollBarCont.getChildren().add(sb);
		sb.install(this, contentCont);

		sb.opacityProperty().bind(Bindings.when(hoverProperty().or(sb.pressedProperty())).then(1).otherwise(.4));

		contentCont.translateYProperty().bind(
				sb.positionProperty().multiply(contentCont.heightProperty().subtract(heightProperty())).multiply(-1));

		Rectangle clip = new Rectangle();
		clip.widthProperty().bind(widthProperty());
		clip.heightProperty().bind(heightProperty());
		clip.yProperty().bind(contentCont.translateYProperty().negate());

		contentCont.setClip(clip);
		getChildren().addAll(contentCont, scrollBarCont);
	}

	public ScrollBar getScrollBar() {
		return sb;
	}

	public void setContent(Parent content) {
		contentCont.getChildren().setAll(content);
	}

	public ReadOnlyDoubleProperty contentHeightProperty() {
		return contentCont.heightProperty();
	}
}
