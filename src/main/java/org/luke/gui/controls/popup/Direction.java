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

/**
 * The {@code Direction} enum represents different directional orientations,
 * such as UP, DOWN, LEFT, RIGHT, and various combinations. It provides utility
 * methods for flipping directions and calculating positions for pop-up
 * controls.
 * <p>
 * The enum includes methods to determine the directionality (horizontal or
 * vertical), the position of the pop-up relative to a node, and flipping the
 * direction.
 * </p>
 * 
 * @author SDIDSA
 */
public enum Direction {
	UP, RIGHT, DOWN, LEFT, DOWN_RIGHT, DOWN_LEFT, RIGHT_DOWN, RIGHT_UP, UP_RIGHT, UP_LEFT, LEFT_DOWN, LEFT_UP;

	private static final ArrayList<Direction> horizontal = new ArrayList<>();
	private static final ArrayList<Direction> arrowFirst = new ArrayList<>();
	private static final ArrayList<Direction> secondFirst = new ArrayList<>();
	private static final ArrayList<Direction> secondLast = new ArrayList<>();

	private static final HashMap<Direction, Direction> flipsToRight = new HashMap<>();
	private static final HashMap<Direction, Direction> flipHorizontal = new HashMap<>();
	private static final HashMap<Direction, Direction> flipsToLeft = new HashMap<>();
	private static final HashMap<Direction, Direction> flipsToTop = new HashMap<>();
	private static final HashMap<Direction, Direction> flipsToBottom = new HashMap<>();

	private static final HashMap<Direction, HashMap<Direction, Direction>> flipsTo = new HashMap<>();

	// Static initialization block
	static {
		// Populate direction lists
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

		// Populate flip maps
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

		flipHorizontal.put(Direction.RIGHT, Direction.LEFT);
		flipHorizontal.put(Direction.LEFT, Direction.RIGHT);
		flipHorizontal.put(Direction.UP, Direction.UP);
		flipHorizontal.put(Direction.DOWN, Direction.DOWN);

		flipHorizontal.put(Direction.UP_RIGHT, Direction.UP_LEFT);
		flipHorizontal.put(Direction.UP_LEFT, Direction.UP_RIGHT);
		flipHorizontal.put(Direction.DOWN_RIGHT, Direction.DOWN_LEFT);
		flipHorizontal.put(Direction.DOWN_LEFT, Direction.DOWN_RIGHT);

		flipHorizontal.put(Direction.RIGHT_UP, Direction.LEFT_UP);
		flipHorizontal.put(Direction.LEFT_UP, Direction.RIGHT_UP);
		flipHorizontal.put(Direction.RIGHT_DOWN, Direction.LEFT_DOWN);
		flipHorizontal.put(Direction.LEFT_DOWN, Direction.RIGHT_DOWN);
	}

	/**
	 * Flips the direction to the right.
	 * 
	 * @return The flipped direction.
	 */
	public Direction flipToRight() {
		return flipTo(RIGHT);
	}

	/**
	 * Flips the direction to the left.
	 * 
	 * @return The flipped direction.
	 */
	public Direction flipToLeft() {
		return flipTo(LEFT);
	}

	/**
	 * Flips the direction to the top.
	 * 
	 * @return The flipped direction.
	 */
	public Direction flipToTop() {
		return flipTo(UP);
	}

	/**
	 * Flips the direction to the bottom.
	 * 
	 * @return The flipped direction.
	 */
	public Direction flipToBottom() {
		return flipTo(DOWN);
	}

	/**
	 * Private method to perform the actual flip operation.
	 * 
	 * @param to The direction to flip to.
	 * @return The flipped direction.
	 */
	private Direction flipTo(Direction to) {
		return flipsTo.get(to).get(this);
	}

	public Direction flipHorizontal() {
		return flipHorizontal.get(this);
	}

	/**
	 * Checks if the direction is horizontal.
	 * 
	 * @return {@code true} if the direction is horizontal, {@code false} otherwise.
	 */
	public boolean isHorizontal() {
		return horizontal.contains(this);
	}

	/**
	 * Checks if the direction is vertical.
	 * 
	 * @return {@code true} if the direction is vertical, {@code false} otherwise.
	 */
	public boolean isVertical() {
		return !isHorizontal();
	}

	/**
	 * Checks if the direction is arrow-first.
	 * 
	 * @return {@code true} if the direction is arrow-first, {@code false}
	 *         otherwise.
	 */
	public boolean isArrowFirst() {
		return arrowFirst.contains(this);
	}

	/**
	 * Checks if the direction is second-last.
	 * 
	 * @return {@code true} if the direction is second-last, {@code false}
	 *         otherwise.
	 */
	public boolean isSecondLast() {
		return secondLast.contains(this);
	}

	/**
	 * Checks if the direction is second-first.
	 * 
	 * @return {@code true} if the direction is second-first, {@code false}
	 *         otherwise.
	 */
	public boolean isSecondFirst() {
		return secondFirst.contains(this);
	}

	/**
	 * Converts the direction to a horizontal or vertical {@code Pane}.
	 * 
	 * @return The converted {@code Pane}.
	 */
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

	/**
	 * Calculates the position for a {@code PopupControl} relative to a
	 * {@code Node}.
	 * 
	 * @param popup   The pop-up control.
	 * @param node    The reference node.
	 * @param offsetX The horizontal offset.
	 * @param offsetY The vertical offset.
	 * @return An array containing the X and Y coordinates of the pop-up control.
	 */
	public double[] calcPos(PopupControl popup, Node node, double offsetX, double offsetY) {
		Bounds bounds = node.getBoundsInLocal();
		Bounds screenBounds = node.localToScreen(bounds);

		return new double[] { calcX(popup, screenBounds, offsetX), calcY(popup, screenBounds, offsetY) };
	}

	/**
	 * Calculates the X-coordinate for a {@code PopupControl} relative to a
	 * {@code Node}.
	 * 
	 * @param popup  The pop-up control.
	 * @param node   The reference node.
	 * @param offset The horizontal offset.
	 * @return The calculated X-coordinate.
	 */
	public double calcX(PopupControl popup, Bounds node, double offset) {
        return switch (this) {
            case DOWN, UP -> node.getCenterX() - popup.getWidth() / 2;
            case LEFT, LEFT_DOWN, LEFT_UP -> node.getMinX() - popup.getWidth() + 15 - offset;
            case RIGHT, RIGHT_DOWN, RIGHT_UP -> node.getMaxX() - 15 + offset;
            case UP_LEFT, DOWN_LEFT -> node.getMaxX() - popup.getWidth() + 15;
            case UP_RIGHT, DOWN_RIGHT -> node.getMinX() - 15;
        };
    }

	/**
	 * Calculates the Y-coordinate for a {@code PopupControl} relative to a
	 * {@code Node}.
	 * 
	 * @param popup  The pop-up control.
	 * @param node   The reference node.
	 * @param offset The vertical offset.
	 * @return The calculated Y-coordinate.
	 */
	public double calcY(PopupControl popup, Bounds node, double offset) {
        return switch (this) {
            case LEFT, RIGHT -> node.getCenterY() - popup.getHeight() / 2;
            case UP, UP_LEFT, UP_RIGHT -> node.getMinY() - popup.getHeight() + 15 - offset;
            case DOWN, DOWN_LEFT, DOWN_RIGHT -> node.getMaxY() - 15 + offset;
            case LEFT_UP, RIGHT_UP -> node.getMaxY() - popup.getHeight() + 15;
            case LEFT_DOWN, RIGHT_DOWN -> node.getMinY() - 15;
        };
    }
}
