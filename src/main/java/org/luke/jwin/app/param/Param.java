package org.luke.jwin.app.param;

import java.io.File;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import org.luke.jwin.app.utils.Backgrounds;
import org.luke.jwin.app.utils.Borders;
import org.luke.jwin.ui.Button;

import java.awt.Graphics2D;
import java.awt.image.*;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public abstract class Param extends StackPane {
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
	private ProgressIndicator pi;
	private Label loadingLabel;
	private HBox loadingRoot;

	protected ScrollPane sp;

	protected Param(String name) {
		all.add(this);

		setMinWidth(424);
		
		root = new VBox(10);

		top = new HBox(10);
		top.setAlignment(Pos.CENTER);

		Label head = new Label(name);
		head.setFont(Font.font("Segoe UI", 12));

		top.getChildren().addAll(head, hSpace());

		list = new VBox(10);
		list.setBackground(Backgrounds.make(Color.WHITE));
		list.setPadding(new Insets(10));
		list.setAlignment(Pos.CENTER);

		sp = new ScrollPane(list);
		sp.setFitToWidth(true);

		list.minHeightProperty().bind(sp.heightProperty().subtract(4));

		sp.setBackground(Backgrounds.make(Color.WHITE));
		sp.setBorder(Borders.make(Color.LIGHTGRAY));
		sp.setMinHeight(47);

		StackPane listCont = new StackPane(sp);

		root.getChildren().addAll(top, listCont);
		
		pi = new ProgressIndicator();
		pi.setMinSize(0, 0);
		pi.setMaxSize(40, 40);
		
		loadingLabel = new Label("");
		loadingLabel.setFont(Font.font("", FontWeight.BOLD, 16));

		loadingRoot = new HBox(15, pi, loadingLabel);
		loadingRoot.setAlignment(Pos.CENTER);
		loadingRoot.setVisible(false);
		
		getChildren().addAll(root, loadingRoot);
	}

	protected void startLoading() {
		startLoading("Working on it ...");
	}
	
	protected void startLoading(String loadingText) {
		loadingLabel.setText(loadingText);
		loadingRoot.setVisible(true);
		root.setDisable(true);
	}

	protected void stopLoading() {
		loadingRoot.setVisible(false);
		root.setDisable(false);
	}

	protected void addButton(String text, EventHandler<ActionEvent> onAction) {
		Button button = new Button(text);
		button.setMinSize(100, 26);
		button.setOnAction(onAction);

		top.getChildren().add(button);
	}

	protected HBox addFile(File file, String name, Node... post) {
		HBox line = new HBox(10, new ImageView(typeIcon(file)), new Label(name), hSpace());
		line.setAlignment(Pos.CENTER);

		for (Node inf : post) {
			line.getChildren().add(inf);
		}

		list.getChildren().addAll(line);

		return line;
	}

	protected static HBox generateLine(File file, String name, Node... post) {
		HBox line = new HBox(10, new ImageView(typeIcon(file)), new Label(name), hSpace());
		line.setAlignment(Pos.CENTER);

		for (Node inf : post) {
			line.getChildren().add(inf);
		}

		return line;
	}

	private static Image typeIcon(File file) {
		Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);
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

	public abstract void clear();
}
