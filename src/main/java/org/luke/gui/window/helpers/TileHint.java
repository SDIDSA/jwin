package org.luke.gui.window.helpers;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import org.luke.gui.factory.Backgrounds;
import org.luke.gui.factory.Borders;
import org.luke.gui.window.Window;

import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class TileHint extends Stage {
	private static final double PADDING = 10;
	private static final Rectangle screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

    private final Timeline show;
	private final Timeline hide;

	private Tile tile;

	public TileHint(Window owner) {
		initOwner(owner);

		initStyle(StageStyle.TRANSPARENT);

		StackPane root = new StackPane();
		root.setBackground(Backgrounds.make(Color.gray(0.7, .1)));
		root.setBorder(Borders.make(Color.LIGHTGRAY));

		Scene scene = new Scene(root);
		scene.setFill(Color.TRANSPARENT);
		setScene(scene);

		setOnShown(e -> owner.setAlwaysOnTop(true));
		
		setOnHidden(e-> owner.setAlwaysOnTop(false));

        DoubleProperty opac = new SimpleDoubleProperty(0);
		opac.addListener((obs, ov, nv) -> setOpacity(nv.doubleValue()));

		show = new Timeline(60.0, new KeyFrame(Duration.seconds(.1), new KeyValue(opac, 1)));
		hide = new Timeline(60.0, new KeyFrame(Duration.seconds(.1), new KeyValue(opac, 0)));
	}

	public void show(State state) {
		Runnable run = () -> {
			tile = tileForState(state);
			Rectangle rect = tile.rect;
			setX(rect.x + PADDING);
			setY(rect.y + PADDING);
			setWidth(rect.width - PADDING * 2);
			setHeight(rect.height - PADDING * 2);

			if (!isShowing())
				super.show();

			if (show.getStatus().equals(Status.RUNNING)) {
				return;
			}
			show.playFromStart();
		};

		if (hide.getStatus().equals(Status.RUNNING)) {
			hide.stop();
		}
		run.run();
	}
	
	public static Tile tileForState(State state) {
		double x = 0;
		double y = 0;
		double w = 0;
		double h = 0;
		double cx = screenSize.getCenterX();
		double cy = screenSize.getCenterY();
		double mw = screenSize.getWidth();
		double mh = screenSize.getHeight();
		switch (state) {
		case N:
			w = mw;
			h = mh;
			break;
		case E:
			x = cx;
			w = mw / 2;
			h = mh;
			break;
		case W:
			w = mw / 2;
			h = mh;
			break;
		case NE:
			w = mw / 2;
			h = mh / 2;
			x = cx;
			break;
		case NW:
			w = mw / 2;
			h = mh / 2;
			break;
		case SE:
			w = mw / 2;
			h = mh / 2;
			x = cx;
			y = cy;
			break;
		case SW:
			w = mw / 2;
			h = mh / 2;
			y = cy;
			break;
		default:
			break;
		}

		return new Tile((int) x, (int) y, (int) w, (int) h, state != State.N);
	}

	public State getState(double x, double y) {
		return State.stateForCoords(x, y, screenSize.getWidth(), screenSize.getHeight(), 100, 15);
	}

	@Override
	public void hide() {
		tile = null;
		hide.setOnFinished(e -> super.hide());
		if (!isShowing()) {
			return;
		}
		if (hide.getStatus().equals(Status.RUNNING)) {
			return;
		}
		hide.playFromStart();
	}

	public Tile getTile() {
		return tile;
	}
	
	public static class Tile {
		Rectangle rect;
		boolean padded;
		
		public Tile(int x, int y, int w, int h, boolean padded) {
			this.rect = new Rectangle(x, y, w, h);
			this.padded = padded;
		}
	}
}
