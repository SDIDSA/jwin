package org.luke.gui.controls.popup.context;

import java.util.ArrayList;
import java.util.Collection;

import org.luke.gui.controls.SplineInterpolator;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.controls.popup.context.items.MenuItem;
import org.luke.gui.controls.popup.context.items.MenuMenuItem;
import org.luke.gui.controls.scroll.Scrollable;
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
	protected Scrollable sroll;
	protected VBox root;
	protected ArrayList<MenuItem> items;

	private ArrayList<StackPane> separators;

	private Timeline show;

	private Scale scale;

	protected MenuItem selected;

	private ArrayList<Runnable> onShowing;

	private ArrayList<Runnable> onHiding;

	private static ArrayList<ContextMenu> open;
	private static int focused = 0;

	static {
		open = new ArrayList<>();
	}

	public ContextMenu(Window window) {
		this.owner = window;

		onShowing = new ArrayList<>();
		onHiding = new ArrayList<>();

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

		sroll = new Scrollable();
		
		Rectangle clip = new Rectangle();
		clip.setArcHeight(4);
		clip.setArcWidth(4);
		clip.widthProperty().bind(sroll.widthProperty());
		clip.heightProperty().bind(sroll.heightProperty());

		sroll.setClip(clip);
		clipped.getChildren().add(sroll);
		
		sroll.setContent(root);

		preroot.getChildren().add(clipped);
		getScene().setRoot(preroot);

		getScene().setOnKeyPressed(this::handlePress);
		getScene().setOnKeyReleased(this::handleRelease);

		setOnHiding(e -> {
			onHiding.forEach(Runnable::run);
			if (selected != null) {
				selected.setActive(false);
				selected = null;
			}
		});

		scale = new Scale(.5, .5);

		sroll.getTransforms().add(scale);

		show = new Timeline(new KeyFrame(Duration.seconds(.2),
				new KeyValue(sroll.opacityProperty(), 1, SplineInterpolator.OVERSHOOT),
				new KeyValue(scale.xProperty(), 1, SplineInterpolator.OVERSHOOT),
				new KeyValue(scale.yProperty(), 1, SplineInterpolator.OVERSHOOT)));

		show.setOnFinished(e -> {
			sroll.setCache(false);
			sroll.setCacheHint(CacheHint.DEFAULT);
		});

		addOnShowing(() -> {
			open.add(this);
			if (open.size() == 1) {
				focused = 0;
			}
		});

		addOnHiding(() -> {
			open.remove(this);
			if (open.size() == 0) {
				focused = 0;
			}
		});
		
		setVScrollable(500);

		applyStyle(window.getStyl());
	}
	
	public void setVScrollable(double maxHeight) {
		sroll.setMaxHeight(maxHeight);
	}

	public void addOnShowing(Runnable r) {
		onShowing.add(r);
	}

	public void addOnHiding(Runnable r) {
		onHiding.add(r);
	}

	private void handleRelease(KeyEvent e) {
		ContextMenu focused = open.get(ContextMenu.focused);
		if (focused.checkForAccelerator(e)) {
			return;
		}
		switch (e.getCode()) {
		case ENTER, SPACE: {
			if (focused.selected != null) {
				focused.selected.fire();
			}
		}
			break;
		case ESCAPE:
			focused.hide();
			ContextMenu.focused--;
			break;
		default:
			break;
		}
		e.consume();
	}

	private void handlePress(KeyEvent e) {
		ContextMenu focused = open.get(ContextMenu.focused);
		switch (e.getCode()) {
		case UP: {
			focused.up();
		}
			break;
		case DOWN: {
			focused.down();
		}
			break;
		case RIGHT, LEFT: {
			if (focused.selected instanceof MenuMenuItem mmItem) {
				mmItem.getSubMenu().down();
				ContextMenu.focused++;
			}
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

	@Override
	public void show(javafx.stage.Window owner) {
		onShowing.forEach(Runnable::run);
		super.show(owner);
	}

	@Override
	public void show(Node ownerNode, double anchorX, double anchorY) {
		onShowing.forEach(Runnable::run);
		super.show(ownerNode, anchorX, anchorY);
	}

	public void showPop(MouseEvent ev) {
		if (ev.getButton() == MouseButton.SECONDARY) {
			setOnShown(e -> {
				scale.setPivotY(0);
				scale.setPivotX(0);
				sroll.setOpacity(0);
				scale.setX(.5);
				scale.setY(.5);
				show.playFromStart();
			});

			setX(ev.getScreenX() - 15);
			setY(ev.getScreenY() - 15);
			this.show(owner);
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

	public void addMenuItems(MenuItem...is) {
		for(MenuItem i : is) {
			addMenuItem(i);
		}
	}

	public void addMenuItems(Collection<? extends MenuItem> is) {
		for(MenuItem i : is) {
			addMenuItem(i);
		}
	}

	public void addMenuItem(MenuItem i) {
		i.setOnMouseClicked(e -> i.fire());
		i.setOnMouseEntered(e -> select(i));
		root.getChildren().add(i);
		items.add(i);
	}

	public void clear() {
		root.getChildren().clear();
		separators.clear();
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

	public void showPop(Node node, Direction preD, int offsetX, int offsetY) {
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
				px = screenBounds.getMaxX() - 15 + offsetX;
				py = screenBounds.getMinY() - 15;
			} else {
				boolean ouverflow = true;
				while (ouverflow) {
					double[] pos = direction.calcPos(this, node, offsetX, offsetY);
					px = pos[0];
					py = pos[1];

					ouverflow = false;

					if (px + sroll.getWidth() + 15 > scrW) {
						ouverflow = true;
						direction = direction.flipToLeft();
					}

					if (px < -10) {
						ouverflow = true;
						direction = direction.flipToRight();
					}

					if (py + sroll.getHeight() + 15 > scrH) {
						ouverflow = true;
						direction = direction.flipToTop();
					}

					if (py < -10) {
						ouverflow = true;
						direction = direction.flipToBottom();
					}
				}
			}

			scale.setPivotY(direction.isVertical() ? direction.isArrowFirst() ? 0 : sroll.getHeight() - 15
					: direction.isSecondFirst() ? 0
							: direction.isSecondLast() ? sroll.getHeight() : sroll.getHeight() / 2);
			scale.setPivotX(direction.isHorizontal() ? direction.isArrowFirst() ? 0 : sroll.getWidth() - 15
					: direction.isSecondFirst() ? 0 : direction.isSecondLast() ? sroll.getWidth() : sroll.getWidth() / 2);

			sroll.setCache(true);
			sroll.setCacheHint(CacheHint.SPEED);

			sroll.setOpacity(0);
			scale.setX(.5);
			scale.setY(.5);
			show.playFromStart();

			setX(px);
			setY(py);
		});
		this.show(owner);
	}

	public void showPop(Node node, Direction dir) {
		showPop(node, dir, 0, 0);
	}

	public void showPop(Node node, Direction dir, int offset) {
		showPop(node, dir, offset, offset);
	}

	public void showPop(Node node, int offset) {
		showPop(node, null, offset, offset);
	}

	public void showPop(Node node) {
		showPop(node, null, 0, 0);
	}

	public void showPop(Node node, ContextMenuEvent event) {
		setOnShown(e -> {
			node.getScene().getRoot().requestFocus();

			sroll.setCache(true);
			sroll.setCacheHint(CacheHint.SPEED);

			scale.setPivotY(0);
			scale.setPivotX(0);

			sroll.setOpacity(0);
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
		sroll.setBackground(Backgrounds.make(style.getBackgroundFloatingOr(), 10));

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
