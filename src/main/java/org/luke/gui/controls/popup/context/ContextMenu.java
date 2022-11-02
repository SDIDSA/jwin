package org.luke.gui.controls.popup.context;

import java.util.ArrayList;

import org.luke.gui.controls.SplineInterpolator;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.controls.popup.context.items.MenuItem;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.PopupControl;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.util.Duration;

public class ContextMenu extends PopupControl implements Styleable {
	protected Window owner;
	protected VBox root;
	protected ArrayList<MenuItem> items;

	private ArrayList<StackPane> separators;

	private Timeline show;

	private Scale scale;

	protected MenuItem selected;

	public ContextMenu(Window window) {
		this.owner = window;

		items = new ArrayList<>();
		separators = new ArrayList<>();

		root = new VBox(3);
		root.setPadding(new Insets(8));
		root.setMinWidth(188);
		root.setMaxWidth(320);

		setAutoHide(true);

		StackPane preroot = new StackPane();

		DropShadow ds = new DropShadow(15, Color.gray(0, .25));

		StackPane clipped = new StackPane();
		clipped.setEffect(ds);

		Rectangle clip = new Rectangle();
		clip.setArcHeight(4);
		clip.setArcWidth(4);
		clip.widthProperty().bind(root.widthProperty());
		clip.heightProperty().bind(root.heightProperty());

		root.setClip(clip);
		clipped.getChildren().add(root);

		preroot.getChildren().add(clipped);
		getScene().setRoot(preroot);

		root.setOnMouseExited(e -> {
//			if (selected != null) {
//				selected.setActive(false);
//				selected = null;
//			}
		});

		getScene().setOnKeyPressed(this::handlePress);
		getScene().setOnKeyReleased(this::handleRelease);

		setOnHiding(e -> {
			if (selected != null) {
				selected.setActive(false);
				selected = null;
			}
		});

		scale = new Scale(.5, .5);

		root.getTransforms().add(scale);

		show = new Timeline(new KeyFrame(Duration.seconds(.2),
				new KeyValue(root.opacityProperty(), 1, SplineInterpolator.OVERSHOOT),
				new KeyValue(scale.xProperty(), 1, SplineInterpolator.OVERSHOOT),
				new KeyValue(scale.yProperty(), 1, SplineInterpolator.OVERSHOOT)));

		show.setOnFinished(e -> {
			root.setCache(false);
			root.setCacheHint(CacheHint.DEFAULT);
		});

		applyStyle(window.getStyl());
	}

	private void handleRelease(KeyEvent e) {
		if (checkForAccelerator(e)) {
			return;
		}
		switch (e.getCode()) {
		case ENTER, SPACE: {
			if (selected != null) {
				selected.fire();
			}
		}
			break;
		case ESCAPE:
			this.hide();
			break;
		default:
			break;
		}
		e.consume();
	}

	private void handlePress(KeyEvent e) {
		switch (e.getCode()) {
		case UP: {
			up();
		}
			break;
		case DOWN: {
			down();
		}
			break;
		default:
			break;
		}
		e.consume();
	}

	private boolean checkForAccelerator(KeyEvent e) {
		for (MenuItem item : items) {
			KeyCombination accelerator = item.getAccelerator();
			if (accelerator != null && accelerator.match(e) && !item.isDisabled()) {
				item.fire();
				return true;
			}
		}

		return false;
	}

	private void up() {
		MenuItem nextItem = selected;

		int i = (nextItem == null ? items.size() - 1 : (items.indexOf(nextItem) - 1 + items.size()) % items.size());
		nextItem = items.get(i);

		while (nextItem == null || nextItem.isDisabled()) {
			i = (nextItem == null ? items.size() - 1 : (items.indexOf(nextItem) - 1 + items.size()) % items.size());
			nextItem = items.get(i);
		}

		select(nextItem);
	}

	public void down() {
		MenuItem nextItem = selected;

		int i = (nextItem == null ? 0 : (items.indexOf(nextItem) + 1) % items.size());
		nextItem = items.get(i);

		while (nextItem == null || nextItem.isDisabled()) {
			i = (nextItem == null ? 0 : (items.indexOf(nextItem) + 1) % items.size());
			nextItem = items.get(i);
		}

		select(nextItem);
	}

	public void install(Node node) {
		node.addEventFilter(MouseEvent.MOUSE_PRESSED, this::showPop);
	}

	public void showPop(MouseEvent ev) {
		if (ev.getButton() == MouseButton.SECONDARY) {
			setOnShown(e -> {
				scale.setPivotY(0);
				scale.setPivotX(0);
				root.setOpacity(0);
				scale.setX(.5);
				scale.setY(.5);
				show.playFromStart();
			});

			setX(ev.getScreenX() - 15);
			setY(ev.getScreenY() - 15);
			show(owner);
		}
	}

	public void addMenuItem(String item, Runnable onAction, Color fill, boolean keyed) {
		MenuItem i = new MenuItem(this, item, fill, keyed);
		i.setAction(onAction);
		addMenuItem(i);
	}

	public void addMenuItem(String item, Runnable onAction, Color fill) {
		addMenuItem(item, onAction, fill, true);
	}

	public void addMenuItem(String item, Color fill) {
		addMenuItem(item, null, fill);
	}

	public void addMenuItem(String item, Runnable onAction) {
		addMenuItem(item, onAction, null);
	}

	public void addMenuItem(String item) {
		addMenuItem(item, null, null);
	}

	public void addMenuItem(MenuItem i) {
		i.setOnMouseClicked(e -> i.fire());
		i.setOnMouseEntered(e -> select(i));
		root.getChildren().add(i);
		items.add(i);
	}

	public void clear() {
		root.getChildren().clear();
		items.clear();
	}

	public void disable(MenuItem item) {
		if (item.isDisable()) {
			return;
		}
		item.setDisable(true);
		root.getChildren().remove(item);
	}

	public void enableAfter(MenuItem item, MenuItem after) {
		if (!item.isDisable()) {
			return;
		}
		item.setDisable(false);
		root.getChildren().add(root.getChildren().indexOf(after) + 1, item);
	}

	public void enableBefore(MenuItem item, MenuItem before) {
		if (!item.isDisable()) {
			return;
		}
		item.setDisable(false);
		root.getChildren().add(root.getChildren().indexOf(before), item);
	}

	public void enableFirst(MenuItem item) {
		if (!item.isDisable()) {
			return;
		}
		item.setDisable(false);
		root.getChildren().add(0, item);
	}

	public void enableLast(MenuItem item) {
		if (!item.isDisable()) {
			return;
		}
		item.setDisable(false);
		root.getChildren().add(item);
	}

	public void separate() {
		StackPane sep = new StackPane();
		sep.setMinHeight(1);
		sep.setMaxHeight(1);

		StackPane preSep = new StackPane(sep);
		preSep.setPadding(new Insets(2, 4, 2, 4));

		root.getChildren().add(preSep);
		separators.add(sep);

		applyStyle(owner.getStyl().get());
	}

	private void select(MenuItem item) {
		boolean select = selected != item;
		boolean deselect = select && selected != null;

		if (deselect) {
			selected.setActive(false);
		}

		if (select) {
			item.setActive(true);
			selected = item;
		}
	}

	public void showPop(Node node, Direction preD, int offset) {
		if (node.getScene() == null) {
			return;
		}
		setOnShown(e -> {
			Bounds bounds = node.getBoundsInLocal();
			Bounds screenBounds = node.localToScreen(bounds);
			double px = 0;
			double py = 0;

			double scrW = Screen.getPrimary().getVisualBounds().getWidth();
			double scrH = Screen.getPrimary().getVisualBounds().getHeight();

			Direction direction = preD;
			if (preD == null) {
				px = screenBounds.getMaxX() - 15 + offset;
				py = screenBounds.getMinY() - 15;
			} else {
				boolean ouverflow = true;
				while (ouverflow) {
					double[] pos = direction.calcPos(this, node, offset);
					px = pos[0];
					py = pos[1];

					ouverflow = false;

					if (px + root.getWidth() + 15 > scrW) {
						ouverflow = true;
						direction = direction.flipToLeft();
					}

					if (px < -10) {
						ouverflow = true;
						direction = direction.flipToRight();
					}

					if (py + root.getHeight() + 15 > scrH) {
						ouverflow = true;
						direction = direction.flipToTop();
					}

					if (py < -10) {
						ouverflow = true;
						direction = direction.flipToBottom();
					}
				}
			}

			scale.setPivotY(direction.isVertical() ? direction.isArrowFirst() ? 0 : root.getHeight()
					: direction.isSecondFirst() ? 0
							: direction.isSecondLast() ? root.getHeight() : root.getHeight() / 2);
			scale.setPivotX(direction.isHorizontal() ? direction.isArrowFirst() ? 0 : root.getWidth()
					: direction.isSecondFirst() ? 0 : direction.isSecondLast() ? root.getWidth() : root.getWidth() / 2);

			root.setCache(true);
			root.setCacheHint(CacheHint.SPEED);

			root.setOpacity(0);
			scale.setX(.5);
			scale.setY(.5);
			show.playFromStart();

			setX(px);
			setY(py);
		});
		this.show(owner);
	}

	public void showPop(Node node, Direction dir) {
		showPop(node, dir, 0);
	}

	public void showPop(Node node, int offset) {
		showPop(node, null, offset);
	}

	public void showPop(Node node) {
		showPop(node, null, 0);
	}

	public void showPop(Node node, ContextMenuEvent event) {
		setOnShown(e -> {
			node.getScene().getRoot().requestFocus();

			root.setCache(true);
			root.setCacheHint(CacheHint.SPEED);

			scale.setPivotY(0);
			scale.setPivotX(0);

			root.setOpacity(0);
			scale.setX(.5);
			scale.setY(.5);

			show.playFromStart();
		});

		setOnHidden(e -> node.requestFocus());

		if (event.isKeyboardTrigger()) {
			Bounds bounds = node.getBoundsInLocal();
			Bounds screenBounds = node.localToScreen(bounds);
			double px = screenBounds.getMinX();
			double py = screenBounds.getMaxY() + 5;

			this.show(node, px, py);
		} else {
			this.show(node, event.getScreenX(), event.getScreenY());
		}

	}

	public Window getOwner() {
		return owner;
	}

	@Override
	public void applyStyle(Style style) {
		root.setBackground(Backgrounds.make(style.getBackgroundPrimary(), 10));

		if (!separators.isEmpty()) {
			Background sepBac = Backgrounds.make(style.getBackgroundModifierAccent());
			for (StackPane sep : separators) {
				sep.setBackground(sepBac);
			}
		}
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
