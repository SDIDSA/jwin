package org.luke.jwin.ui;

import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import jna.ITaskbarList3;
import jna.TaskbarPeer;

public class Progress extends StackPane implements Styleable {
	
	private BooleanProperty indeterminate;
	private DoubleProperty val;
	private Rectangle track;
	private Rectangle thumb;
	
	
	public Progress(Window win) {
		super();
		setAlignment(Pos.CENTER_LEFT);
		setMinHeight(7);
		
		val = new SimpleDoubleProperty(-1);
		indeterminate = new SimpleBooleanProperty(true);
		
		track = new Rectangle();
		thumb = new Rectangle();
		
		track.widthProperty().bind(widthProperty());
		
		track.heightProperty().bind(heightProperty());
		thumb.heightProperty().bind(heightProperty());
		
		Rectangle clip = new Rectangle();
		
		clip.widthProperty().bind(widthProperty());
		clip.heightProperty().bind(heightProperty());
		
		setClip(clip);
		
		ChangeListener<Number> listener = (obs, ov, nv) -> {
			double nvd = val.get();
			
			if(nvd < 0 && !isIndeterminate()) {
				setIndeterminate(true);
			}
			
			if(nvd > 0 && isIndeterminate()) {
				setIndeterminate(false);
			}

			if(!isIndeterminate()) {
				thumb.setTranslateX(0);
				thumb.setWidth(getWidth() * val.get());
				
				TaskbarPeer.setProgressState(win, ITaskbarList3.TBPF_NORMAL);
				TaskbarPeer.setProgress(win, nv.doubleValue());
			}else {
				thumb.setWidth(getWidth() / 2);
				
				TaskbarPeer.setProgressState(win, ITaskbarList3.TBPF_NOPROGRESS);
			}
		};
		
		AnimationTimer timer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				double x = thumb.getTranslateX() + 4;
				if(x > getWidth()) {
					x = -getWidth() / 2;
				}
				thumb.setTranslateX(x);
			}
		};
		
		indeterminate.addListener((obs, ov, nv) -> {
			if(nv) {
				setOpacity(0.2);
				timer.start();
			}
			
			if(!nv) {
				timer.stop();
				setOpacity(1);
			}
		});
		
		timer.start();
		setOpacity(0.2);
		
		val.addListener(listener);
		widthProperty().addListener(listener);
		
		getChildren().addAll(track, thumb);
		
		applyStyle(win.getStyl());
	}
	
	public void setProgress(double v) {
		val.set(v);
	}

	public double getProgress() {
		return val.get();
	}
	
	public boolean isIndeterminate() {
		return indeterminate.get();
	}
	
	private void setIndeterminate(boolean indeterminate) {
		this.indeterminate.set(indeterminate);
	}

	@Override
	public void applyStyle(Style style) {
		track.setFill(style.getBackgroundTertiary());
		thumb.setFill(style.getAccent());
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
