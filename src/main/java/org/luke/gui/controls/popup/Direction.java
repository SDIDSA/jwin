package org.luke.gui.controls.popup;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.PopupControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public enum Direction {
	UP(), RIGHT(), DOWN(), LEFT(), 
	DOWN_RIGHT(), DOWN_LEFT(), 
	RIGHT_DOWN(), RIGHT_UP(),
	UP_RIGHT(), UP_LEFT(), 
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

	public double[] calcPos(PopupControl popup, Node node, double offset) {
		Bounds bounds = node.getBoundsInLocal();
		Bounds screenBounds = node.localToScreen(bounds);
		double[] res = new double[2];

		double xHor = (isArrowFirst() ? (screenBounds.getMaxX() + offset) : (screenBounds.getMinX() - offset));
		double x = isHorizontal() ? xHor : screenBounds.getCenterX();

		double yVer = (isArrowFirst() ? (screenBounds.getMaxY() + offset) : (screenBounds.getMinY() - offset));
		double y = isVertical() ? yVer : screenBounds.getCenterY();

		double pxHor = (isArrowFirst() ? 0 : popup.getWidth());
		double pyVer = (isArrowFirst() ? 0 : popup.getHeight());

		res[0] = x - (isHorizontal() ? pxHor
				: (isSecondFirst() ? (((Region) node).getWidth() / 2) + 14
						: isSecondLast() ? (popup.getWidth() - ((Region) node).getWidth()) + 28
								: popup.getWidth() / 2));
		res[1] = y - (isVertical() ? pyVer
				: (isSecondFirst() ? (((Region) node).getHeight() / 2) + 14
						: isSecondLast() ? (popup.getHeight() - ((Region) node).getHeight()) + 30
								: popup.getHeight() / 2));

		return res;
	}
}