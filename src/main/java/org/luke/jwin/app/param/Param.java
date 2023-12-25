package org.luke.jwin.app.param;

import java.io.File;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.Loading;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.controls.label.unkeyed.Text;
import org.luke.gui.controls.scroll.Scrollable;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.factory.Borders;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;
import org.luke.jwin.ui.Button;

import java.awt.Graphics2D;
import java.awt.image.*;

import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;

public abstract class Param extends StackPane implements Styleable {
	private static ArrayList<Param> all = new ArrayList<>();
	public static void clearAll() {
		all.forEach(Param::clear);
	}
	
	public static void disable(boolean val) {
		all.forEach(p -> p.setDisable(val));
	}

	protected VBox list;

	protected HBox top;

	protected VBox root;
	private Loading pi;
	private Label loadingLabel;
	private Label head;
	private VBox loadingRoot;

	protected Scrollable sp;
	protected StackPane listCont;
	
	private Window window;
	
	protected Param(Window window, String name) {
		this.window = window;
		
		all.add(this);
		
		root = new VBox(10);

		top = new HBox(10);
		top.setAlignment(Pos.CENTER);

		head = new Label(window, name);

		top.getChildren().addAll(head, hSpace());

		list = new VBox(10);
		list.setAlignment(Pos.CENTER);
		list.setPadding(new Insets(10,15,10,10));

		sp = new Scrollable();
		sp.setContent(list);

		sp.setMinHeight(47);

		listCont = new StackPane(sp);

		root.getChildren().addAll(top, listCont);
		
		pi = new Loading(10);
		
		loadingLabel = new Label(window, "");
		loadingLabel.setFont(new Font("", 16, FontWeight.BOLD));

		loadingRoot = new VBox(15, pi, loadingLabel);
		loadingRoot.setAlignment(Pos.CENTER);
		loadingRoot.setVisible(false);
		
		getChildren().addAll(root, loadingRoot);
		
		applyStyle(window.getStyl());
	}
	
	public Window getWindow() {
		return window;
	}

	protected void startLoading() {
		startLoading("Working on it ...");
	}
	
	protected void startLoading(String loadingText) {
		pi.play();
		loadingLabel.setText(loadingText);
		loadingRoot.setVisible(true);
		root.setDisable(true);
		root.setOpacity(.3);
	}

	protected void stopLoading() {
		pi.stop();
		loadingRoot.setVisible(false);
		root.setDisable(false);
		root.setOpacity(1);
	}

	protected void addButton(Window window, String text, Runnable onAction) {
		Button button = new Button(window, text, 100);
		button.setAction(onAction);

		top.getChildren().add(button);
	}

	private ArrayList<Text> files = new ArrayList<>();
	public HBox addFile(Window window, File file, String name, Node... post) {
		Text lab = new Text(name, new Font(12));
		files.add(lab);
		lab.setFill(window.getStyl().get().getTextNormal());
		
		HBox line = new HBox(10, new ImageView(typeIcon(file)), lab, hSpace());
		line.setAlignment(Pos.CENTER);
		for (Node inf : post) {
			line.getChildren().add(inf);
			
			if(inf instanceof Label alab) {
				alab.setFill(window.getStyl().get().getTextNormal());
				files.add(alab);
			}
		}
		list.getChildren().addAll(line);
		return line;
	}

	public HBox generateLine(Window window, File file, String name, Node... post) {
		Text lab = new Text(name, new Font(12));
		files.add(lab);
		lab.setFill(window.getStyl().get().getTextNormal());
		
		HBox line = new HBox(10, new ImageView(typeIcon(file)), lab, hSpace());
		line.setAlignment(Pos.CENTER);

		for (Node inf : post) {
			line.getChildren().add(inf);
			
			if(inf instanceof Label alab) {
				alab.setFill(window.getStyl().get().getTextNormal());
				files.add(alab);
			}
		}

		return line;
	}

	public static Image typeIcon(File file) {
		Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);
		BufferedImage bImg = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);

		Graphics2D graphics = bImg.createGraphics();
		graphics.drawImage(((ImageIcon) icon).getImage(), 0, 0, null);
		graphics.dispose();
		return SwingFXUtils.toFXImage(bImg, null);
	}

	public static Image typeIcon(File file, int size) {
		Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file, size, size);
		BufferedImage bImg = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);

		Graphics2D graphics = bImg.createGraphics();
		graphics.drawImage(((ImageIcon) icon).getImage(), 0, 0, null);
		graphics.dispose();
		return SwingFXUtils.toFXImage(bImg, null);
	}

	private static Pane hSpace() {
		Pane space = new Pane();
		HBox.setHgrow(space, Priority.ALWAYS);
		return space;
	}
	
	@Override
	public void applyStyle(Style style) {
		head.setFill(style.getTextNormal());
		loadingLabel.setFill(style.getTextNormal());
		pi.setFill(style.getTextNormal());
		
		sp.setBackground(Backgrounds.make(style.getBackgroundTertiaryOr(), 5.0));
		sp.setBorder(Borders.make(style.getDeprecatedTextInputBorder(), Borders.OUTSIDE, 5.0));
		sp.getScrollBar().setThumbFill(style.getChannelsDefault());
		
		files.removeIf(lab -> lab.getScene() == null);
		files.forEach(file -> file.setFill(style.getTextNormal()));
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
	
	public abstract void clear();

	public void clearList() {
		list.getChildren().clear();
	}
}
