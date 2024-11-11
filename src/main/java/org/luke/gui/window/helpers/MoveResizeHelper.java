package org.luke.gui.window.helpers;

import java.awt.Dimension;
import java.util.ArrayList;

import org.luke.gui.window.Window;
import org.luke.gui.window.content.AppPreRoot;
import org.luke.gui.window.content.AppRoot;
import org.luke.gui.window.content.app_bar.AppBar;
import org.luke.gui.window.helpers.TileHint.Tile;

import javafx.scene.input.MouseEvent;

public class MoveResizeHelper {
	private double minH;
	private double minW;

	private double initwX;
	private double initwY;
	private double initX;
	private double initY;
	private double initW;
	private double initH;

	private final TileHint hint;
	private boolean tiled = false;
	private double tiledW;
	private double tiledH;
	private double tiledX;
	private double tiledY;

	private final int range;
	private double padding;

	private State state;

	private final Window win;
	private final AppPreRoot parent;

	private final ArrayList<Runnable> onTile = new ArrayList<>();
	private final ArrayList<Runnable> onUnTile = new ArrayList<>();
	
	private boolean pressed = false;

	public MoveResizeHelper(Window win, AppPreRoot parent, int range) {
		this.win = win;
		this.parent = parent;

		hint = new TileHint(win);

		this.range = range;
		padding = parent.paddingProp().get();
		parent.paddingProp().addListener((obs, ov, nv) -> padding = nv.doubleValue());
	}

	public void onMove(MouseEvent e) {
		if (parent.isPadded() && e.getTarget() instanceof AppRoot) {
			state = State.stateForCoords(e.getSceneX() - padding, e.getSceneY() - padding, win.getWidth() - padding * 2,
					win.getHeight() - padding * 2, range, range);
		} else {
			state = State.D;
		}
		parent.setCursor(state.curs);

	}

	public void onClick(MouseEvent e) {
		if (e.getTarget() instanceof AppBar && e.getClickCount() == 2) {
			win.maxRestore();
		}
	}

	private void preTile() {
		tiledX = win.getX();
		tiledY = win.getY();
		tiledW = win.getWidth();
		tiledH = win.getHeight();
	}

	public void onPress(MouseEvent e) {
		if (state == State.D && !(e.getTarget() instanceof AppBar)) {
			return;
		}
		initwX = win.getX();
		initwY = win.getY();
		initW = win.getWidth();
		initH = win.getHeight();
		initX = e.getScreenX();
		initY = e.getScreenY();

		if (!tiled) {
			preTile();
		}
		
		pressed = true;
	}

	public void applyTile(Tile tile) {
		for (Runnable r : onTile) {
			r.run();
		}

		if (tiledW == 0) {
			preTile();
		}

		win.setX(tile.rect.getX() - (tile.padded ? padding : 0));
		win.setY(tile.rect.getY() - (tile.padded ? padding : 0));
		win.setWidth(tile.rect.getWidth() + (tile.padded ? padding * 2 : 0));
		win.setHeight(tile.rect.getHeight() + (tile.padded ? padding * 2 : 0));
		parent.setPadded(tile.padded);
		tiled = true;
	}

	public void onRelease() {
		if (hint.getTile() != null) {
			applyTile(hint.getTile());
		}
		hint.hide();
		pressed = false;
	}

	public void unTile() {
		for (Runnable r : onUnTile) {
			r.run();
		}

		win.setWidth(tiledW);
		win.setHeight(tiledH);

		win.setX(tiledX);
		win.setY(tiledY);

		initwX = initX - tiledW / 2;
		initwY = initwY + parent.paddingProp().get();
		tiled = false;
		parent.setPadded(true);
	}

	public void onDrag(MouseEvent e) {
		if(!pressed) {
			return;
		}
		if (state == State.D && !(e.getTarget() instanceof AppBar)) {
			return;
		}

		if (tiled) {
			unTile();
		}
		if (state == State.D) {
			applyMove(e);
		} else {
			applyResize(e);
		}
	}

	private void applyMove(MouseEvent e) {
		win.setX(initwX + (e.getScreenX() - initX));
		win.setY(initwY + (e.getScreenY() - initY));

		State s = hint.getState(e.getScreenX(), e.getScreenY());
		if (s != State.D && s != State.S) {
			hint.show(s);
		} else {
			hint.hide();
		}
	}

	private void applyResize(MouseEvent e) {
		switch (state) {
		case N: {
			resN(e);
			break;
		}
		case S: {
			resS(e);
			break;
		}
		case E: {
			resE(e);
			break;
		}
		case W: {
			resW(e);
			break;
		}
		case NE: {
			resNE(e);
			break;
		}
		case NW: {
			resNW(e);
			break;
		}
		case SE: {
			resSE(e);
			break;
		}
		case SW: {
			resSW(e);
			break;
		}
		default:
			break;
		}
	}

	private void resN(MouseEvent e) {
		double dy = e.getScreenY() - initY;
		double nh = initH - dy;

		if (nh < minH) {
			win.setHeight(minH);
		} else {
			win.setY(initwY + dy);
			win.setHeight(nh);
		}

	}

	private void resS(MouseEvent e) {
		double nh = initH + (e.getScreenY() - initY);

		if (nh < minH) {
			win.setHeight(minH);
		} else {
			win.setHeight(nh);
		}
	}

	private void resE(MouseEvent e) {
		double nw = initW + (e.getScreenX() - initX);

		if (nw < minW) {
			win.setWidth(minW);
		} else {
			win.setWidth(nw);
		}
	}

	private void resW(MouseEvent e) {
		double dx = e.getScreenX() - initX;
		double nw = initW - dx;

		if (nw < minW) {
			win.setWidth(minW);
		} else {
			win.setX(initwX + dx);
			win.setWidth(nw);
		}

	}

	private void resNE(MouseEvent e) {
		resN(e);
		resE(e);
	}

	private void resNW(MouseEvent e) {
		resN(e);
		resW(e);
	}

	private void resSE(MouseEvent e) {
		resS(e);
		resE(e);
	}

	private void resSW(MouseEvent e) {
		resS(e);
		resW(e);
	}

	public boolean isTiled() {
		return tiled;
	}

	public void addOnTile(Runnable r) {
		onTile.add(r);
	}

	public void addOnUnTile(Runnable r) {
		onUnTile.add(r);
	}

	public void setMinSize(Dimension minSize) {
		minH = minSize.getHeight();
		minW = minSize.getWidth();

		if (win.getWidth() < minW) {
			win.setWidth(minW);
		}

		if (win.getHeight() < minH) {
			win.setHeight(minH);
		}
	}
}
