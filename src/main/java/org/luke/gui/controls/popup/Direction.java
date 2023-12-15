package org.luke.gui.controls.popup;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.PopupControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public enum Direction {
	UP(), RIGHT(), DOWN(), LEFT(), DOWN_RIGHT(), DOWN_LEFT(), RIGHT_DOWN(), RIGHT_UP(), UP_RIGHT(), UP_LEFT(),
	LEFT_DOWN(), LEFT_UP();

	private static ArrayList<Direction> horizontal = new ArrayList<>();
	private static ArrayList<Direction> arrowFirst = new ArrayList<>();
	private static ArrayList<Direction> secondFirst = new ArrayList<>();
	private static ArrayList<Direction> secondLast = new ArrayList<>();

	private static HashMap<Direction, Direction> flipsToRight = new HashMap<>();
	private static HashMap<Direction, Direction> flipsToLeft = new HashMap<>();
	private static HashMap<Direction, Direction> flipsToTop = new HashMap<>();
	private static HashMap<Direction, Direction> flipsToBottom = new HashMap<>();

	private static HashMap<Direction, HashMap<Direction, Direction>> flipsTo = new HashMap<>();

	static {
		horizontal.add(RIGHT);
		horizontal.add(LEFT);
		horizontal.add(RIGHT_DOWN);
		horizontal.add(RIGHT_UP);
		horizontal.add(LEFT_DOWN);
		horizontal.add(LEFT_UP);

		arrowFirst.add(RIGHT);
		arrowFirst.add(DOWN);
		arrowFirst.add(DOWN_RIGHT);
		arrowFirst.add(DOWN_LEFT);
		arrowFirst.add(RIGHT_DOWN);
		arrowFirst.add(RIGHT_UP);

		secondFirst.add(DOWN_RIGHT);
		secondFirst.add(UP_RIGHT);
		secondFirst.add(RIGHT_DOWN);
		secondFirst.add(LEFT_DOWN);

		secondLast.add(DOWN_LEFT);
		secondLast.add(UP_LEFT);
		secondLast.add(RIGHT_UP);
		secondLast.add(LEFT_UP);

		flipsToRight.put(DOWN_LEFT, DOWN_RIGHT);
		flipsToRight.put(UP_LEFT, UP_RIGHT);
		flipsToRight.put(LEFT_UP, RIGHT_UP);
		flipsToRight.put(LEFT_DOWN, RIGHT_DOWN);
		flipsToRight.put(LEFT, RIGHT);
		flipsToRight.put(DOWN, DOWN_RIGHT);
		flipsToRight.put(UP, UP_RIGHT);

		flipsToRight.put(DOWN_RIGHT, RIGHT_DOWN);
		flipsToRight.put(UP_RIGHT, RIGHT_UP);

		flipsToLeft.put(DOWN_RIGHT, DOWN_LEFT);
		flipsToLeft.put(UP_RIGHT, UP_LEFT);
		flipsToLeft.put(RIGHT_UP, LEFT_UP);
		flipsToLeft.put(RIGHT_DOWN, LEFT_DOWN);
		flipsToLeft.put(RIGHT, LEFT);
		flipsToLeft.put(DOWN, DOWN_LEFT);
		flipsToLeft.put(UP, UP_LEFT);

		flipsToLeft.put(DOWN_LEFT, LEFT_DOWN);
		flipsToLeft.put(UP_LEFT, LEFT_UP);

		flipsToTop.put(RIGHT_DOWN, RIGHT_UP);
		flipsToTop.put(LEFT_DOWN, LEFT_UP);
		flipsToTop.put(DOWN_RIGHT, UP_RIGHT);
		flipsToTop.put(DOWN_LEFT, UP_LEFT);
		flipsToTop.put(DOWN, UP);
		flipsToTop.put(RIGHT, RIGHT_UP);
		flipsToTop.put(LEFT, LEFT_UP);

		flipsToTop.put(LEFT_UP, UP_LEFT);
		flipsToTop.put(RIGHT_UP, UP_RIGHT);

		flipsToBottom.put(RIGHT_UP, RIGHT_DOWN);
		flipsToBottom.put(LEFT_UP, LEFT_DOWN);
		flipsToBottom.put(UP_RIGHT, DOWN_RIGHT);
		flipsToBottom.put(UP_LEFT, DOWN_LEFT);
		flipsToBottom.put(UP, DOWN);
		flipsToBottom.put(RIGHT, RIGHT_DOWN);
		flipsToBottom.put(LEFT, LEFT_DOWN);

		flipsToBottom.put(LEFT_DOWN, DOWN_LEFT);
		flipsToBottom.put(RIGHT_DOWN, DOWN_RIGHT);

		flipsTo.put(DOWN, flipsToBottom);
		flipsTo.put(UP, flipsToTop);
		flipsTo.put(RIGHT, flipsToRight);
		flipsTo.put(LEFT, flipsToLeft);
	}

	public Direction flipToRight() {
		return flipTo(RIGHT);
	}

	public Direction flipToLeft() {
		return flipTo(LEFT);
	}

	public Direction flipToTop() {
		return flipTo(UP);
	}

	public Direction flipToBottom() {
		return flipTo(DOWN);
	}

	private Direction flipTo(Direction to) {
		return flipsTo.get(to).get(this);
	}

	public boolean isHorizontal() {
		return horizontal.contains(this);
	}

	public boolean isVertical() {
		return !isHorizontal();
	}

	public boolean isArrowFirst() {
		return arrowFirst.contains(this);
	}

	public boolean isSecondLast() {
		return secondLast.contains(this);
	}

	public boolean isSecondFirst() {
		return secondFirst.contains(this);
	}

	public Pane toPane() {
		if (isHorizontal()) {
			HBox res = new HBox();
			res.setAlignment(Pos.CENTER);
			return res;
		} else {
			VBox res = new VBox();
			res.setAlignment(Pos.CENTER);
			return res;
		}
	}

	public double[] calcPos(PopupControl popup, Node node, double offsetX, double offsetY) {
		Bounds bounds = node.getBoundsInLocal();
		Bounds screenBounds = node.localToScreen(bounds);
		
		return new double[] {calcX(popup, screenBounds, offsetX), calcY(popup, screenBounds, offsetY)};
	}

	public double calcX(PopupControl popup, Bounds node, double offset) {
		switch (this) {
		case DOWN, UP:
			return node.getCenterX() - popup.getWidth() / 2;
		case LEFT, LEFT_DOWN, LEFT_UP:
			return node.getMinX() - popup.getWidth() + 15 - offset;
		case RIGHT, RIGHT_DOWN, RIGHT_UP:
			return node.getMaxX() - 15 + offset;
		case UP_LEFT, DOWN_LEFT:
			return node.getMaxX() - popup.getWidth() + 15;
		case UP_RIGHT, DOWN_RIGHT:
			return node.getMinX() - 15;
		}
		return 0;
	}

	public double calcY(PopupControl popup, Bounds node, double offset) {
		switch (this) {
		case LEFT,RIGHT:
			return node.getCenterY() - popup.getHeight() / 2;
		case UP,UP_LEFT,UP_RIGHT:
			return node.getMinY() - popup.getHeight() + 15 - offset;
		case DOWN,DOWN_LEFT,DOWN_RIGHT:
			return node.getMaxY() - 15 + offset;
		case LEFT_UP,RIGHT_UP:
			return node.getMaxY() - popup.getHeight() + 15;
		case LEFT_DOWN,RIGHT_DOWN:
			return node.getMinY() - 15;
		}
		return 0;
	}
}