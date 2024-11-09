package org.luke.gui;

import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.luke.gui.factory.Borders;

import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Orientation;
import javafx.scene.Node;
/**
 * Utility class for working with JavaFX nodes.
 * 
 * @author SDIDSA
 */

public class NodeUtils {
	private NodeUtils() {

	}

	private static Node getLast(ObservableList<Node> children) {
		for (int i = children.size() - 1; i >= 0; i--) {
			if (children.get(i).isFocusTraversable()) {
				return children.get(i);
			}
		}
		return null;
	}

	private static Node getFirst(ObservableList<Node> children) {
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i).isFocusTraversable()) {
				return children.get(i);
			}
		}
		return null;
	}

	private static void handle(Node node, KeyEvent event, Orientation orientation, ObservableList<Node> children) {
		Node last = getLast(children);
		Node first = getFirst(children);

		boolean isLast = node == last;
		boolean isFirst = node == first;

		KeyCode next = null;
		KeyCode previous = null;
		KeyCode[] ignore = null;

		if (orientation == Orientation.HORIZONTAL) {
			next = KeyCode.RIGHT;
			previous = KeyCode.LEFT;
			ignore = new KeyCode[] { KeyCode.DOWN, KeyCode.UP };
		} else {
			next = KeyCode.DOWN;
			previous = KeyCode.UP;
			ignore = new KeyCode[] { KeyCode.RIGHT, KeyCode.LEFT };
		}

		if (event.getCode().equals(KeyCode.TAB)) {
			if (event.isShiftDown() && !isFirst) {
				event.consume();
				Event.fireEvent(first, event.copyFor(null, first));
			} else if (!event.isShiftDown() && !isLast) {
				event.consume();
				Event.fireEvent(last, event.copyFor(null, last));
			}
		} else if ((event.getCode().equals(previous) && isFirst) || (event.getCode().equals(next) && isLast)
				|| event.getCode().equals(ignore[0]) || event.getCode().equals(ignore[1])) {
			event.consume();
		}
	}

	public static void nestedFocus(Region parent, Orientation orientation) {
		ObservableList<Node> children = parent.getChildrenUnmodifiable();

		Consumer<Node> prepare = node -> node.addEventHandler(KeyEvent.KEY_PRESSED,
				event -> handle(node, event, orientation, children));

		children.addListener((ListChangeListener<Node>) c -> {
			while (c.next()) {
				for (Node n : c.getAddedSubList()) {
					if (n.isFocusTraversable() && !n.getStyleClass().contains("nf")) {
						n.getStyleClass().add("nf");
						prepare.accept(n);
					}
				}
			}
		});
	}

	public static boolean isChildOf(Node child, Parent parent) {
		if (parent.getChildrenUnmodifiable().contains(child)) {
			return true;
		} else {
			for (Node n : parent.getChildrenUnmodifiable()) {
				if (n instanceof Parent p && isChildOf(child, p)) {
					return true;
				}
			}
			return false;
		}
	}

	public static boolean isParentOf(Node child, Parent parent) {
		if (child == null || child.getParent() == null) {
			return false;
		}

		if (child.getParent() == parent) {
			return true;
		} else {
			return isParentOf(child.getParent(), parent);
		}
	}

	public static void nestedFocus(Region parent) {
		nestedFocus(parent, null);
	}

	public static void focusBorder(Region node, Color in, Color out, double radius) {
		focusBorder(node, in, out, new CornerRadii(radius));
	}

	public static void focusBorder(Region node, Color in, Color out, CornerRadii radius) {
		CornerRadii toUse = new CornerRadii(
				radius.getTopLeftHorizontalRadius() + 2, radius.getTopLeftVerticalRadius() + 2,
				radius.getTopRightVerticalRadius() + 2, radius.getTopRightHorizontalRadius() + 2,
				radius.getBottomRightHorizontalRadius() + 2, radius.getBottomRightVerticalRadius() + 2,
				radius.getBottomLeftVerticalRadius() + 2, radius.getBottomLeftHorizontalRadius() + 2, 
				false, false, false, false, 
				false, false, false, false);
		node.borderProperty().bind(Bindings.when(node.focusedProperty()).then(Borders.make(in, toUse))
				.otherwise(out == null ? Border.EMPTY : Borders.make(out, toUse)));
	}

	public static void focusBorder(Region node, Color color, double radius) {
		focusBorder(node, color, new CornerRadii(radius));
	}

	public static void focusBorder(Region node, Color color, CornerRadii radius) {
		focusBorder(node, color, Color.TRANSPARENT, radius);
	}

	public static void focusBorder(Region node, Color color) {
		focusBorder(node, color, Color.TRANSPARENT, 4.0);
	}

	public static <T> List<T> getNodesOfType(Node node, Class<T> type) {
		ArrayList<T> res = new ArrayList<>();

		if (node instanceof Parent parent) {
			for (Node n : parent.getChildrenUnmodifiable()) {
				res.addAll(getNodesOfType(n, type));
			}
		}

		if (type.isInstance(node)) {
			res.add(type.cast(node));
		}

		return res;
	}
}
