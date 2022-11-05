package org.luke.jwin.app.about;

import org.luke.gui.NodeUtils;
import org.luke.gui.app.pages.Page;
import org.luke.gui.controls.Font;
import org.luke.gui.controls.SplineInterpolator;
import org.luke.gui.controls.alert.BasicOverlay;
import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.controls.image.ImageProxy;
import org.luke.gui.controls.label.MultiText;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.controls.label.unkeyed.Link;
import org.luke.gui.controls.label.unkeyed.Text;
import org.luke.gui.controls.space.ExpandingHSpace;
import org.luke.gui.controls.space.Separator;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;
import org.luke.jwin.app.utils.Backgrounds;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class Credits extends BasicOverlay {
	private Text version;

	private Text copyRighted;
	private MultiText license1;
	private MultiText license2;

	private MultiText thirdParties;

	private MultiText madeBy;

	private Back back;

	public Credits(Page ps) {
		super(ps);
		removeTop();
		removeSubHead();
		removeCancel();

		head.setKey("About JWin");

		version = new Text("1.0.3", new Font("monospace", 16));

		copyRighted = new Text("Copyright � 2022\nThis product is copyrighted by Zinelabidine Teyar", new Font(12));
		copyRighted.setLineSpacing(5);
		copyRighted.setTextAlignment(TextAlignment.CENTER);

		license1 = new MultiText(ps.getWindow(),
				"This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.",
				new Font(12));
		license1.setLineSpacing(3);

		license2 = new MultiText(ps.getWindow(),
				"You should have received a copy of the GNU General Public License along with this program. If not, see ",
				new Font(12));
		license2.addLink("GNU General Public License v3.0", new Font(12));
		license2.setLineSpacing(3);
		license2.setAction(1, () -> ps.getWindow().openLink("https://www.gnu.org/licenses/gpl-3.0.txt"));

		thirdParties = new MultiText(ps.getWindow(), "JWin uses and heavily relies on ", new Font(14));
		thirdParties.addLink("other open-source projects");

		madeBy = new MultiText(ps.getWindow(), "Developed and maintained by", new Font(14));

		VBox about = new VBox(10, copyRighted, new Separator(ps.getWindow(), Orientation.HORIZONTAL), license1,
				license2, new Separator(ps.getWindow(), Orientation.HORIZONTAL), thirdParties,
				new Separator(ps.getWindow(), Orientation.HORIZONTAL), madeBy, new Me(ps.getWindow()));

		
		back = new Back(ps.getWindow());
		

		VBox projects = new VBox(15, back,
				new Project(ps.getWindow(), "Apache Maven", "maven", "https://maven.apache.org/", "Apache License 2.0",
						"https://maven.apache.org/ref/3.0/license.html"),
				new Project(ps.getWindow(), "inno Setup", "innosetup", "https://jrsoftware.org/isinfo.php",
						"Modified BSD license", "https://jrsoftware.org/files/is/license.txt"),
				new Project(ps.getWindow(), "Bat To Exe Converter", "b2e", "https://github.com/99fk", null, null),
				new Project(ps.getWindow(), "thumbnailator", "coobird", "https://github.com/coobird/thumbnailator",
						"MIT License", "https://github.com/coobird/thumbnailator/blob/master/LICENSE"));
		projects.setOpacity(0);
		projects.setDisable(true);
		projects.setMouseTransparent(true);
		projects.setAlignment(Pos.TOP_CENTER);

		StackPane preCenter = new StackPane(about, projects);

		Runnable showProjects = () -> {
			Timeline animation = new Timeline(new KeyFrame(Duration.seconds(.5),
					new KeyValue(projects.translateXProperty(), 0, SplineInterpolator.OVERSHOOT),
					new KeyValue(about.translateXProperty(), -center.getWidth(), SplineInterpolator.OVERSHOOT)));

			projects.setOpacity(1);
			
			projects.setMouseTransparent(false);
			projects.setDisable(false);
			
			animation.setOnFinished(e -> {
				about.setMouseTransparent(true);
				about.setDisable(true);
			});

			projects.setTranslateX(preCenter.getWidth());

			animation.playFromStart();
		};

		Runnable hideProjects = () -> {
			Timeline animation = new Timeline(new KeyFrame(Duration.seconds(.5),
					new KeyValue(projects.translateXProperty(), center.getWidth(), SplineInterpolator.OVERSHOOT),
					new KeyValue(about.translateXProperty(), 0, SplineInterpolator.OVERSHOOT)));

			about.setMouseTransparent(false);
			about.setDisable(false);
			
			animation.setOnFinished(e -> {
				projects.setMouseTransparent(true);
				projects.setDisable(true);
			});

			animation.playFromStart();
		};

		thirdParties.setAction(1, showProjects);
		back.setAction(hideProjects);

		center.getChildren().setAll(preCenter);

		ImageView icon = new ImageView(ImageProxy.resize(ImageProxy.load("jwin-task-icon", 256), 38));
		addToBottom(0, version);
		addToBottom(0, new ExpandingHSpace());
		addToBottom(0, icon);

		done.setAction(this::hide);
		done.setKey("Close");

		about.setAlignment(Pos.CENTER);

		applyStyle(ps.getWindow().getStyl());
	}
	
	private static class Back extends HBox implements Styleable {

		private ColorIcon backIcon;
		private Label backLab;
		
		private Label thirdParties;

		private Runnable action;
		
		public Back(Window window) {
			super(10);
			
			setAlignment(Pos.CENTER_LEFT);
			setPadding(new Insets(10));
			setCursor(Cursor.HAND);
			
			thirdParties = new Label(window, "Third-Party Software", new Font(Font.DEFAULT_FAMILY_MEDIUM, 14));
			
			backIcon = new ColorIcon("back", 18, 16);
			backLab = new Label(window, "back", new Font(12));

			setFocusTraversable(true);

			setOnMouseClicked(this::fire);
			setOnKeyPressed(this::fire);
			
			getChildren().addAll(backIcon, backLab, new ExpandingHSpace(), thirdParties);
			
			applyStyle(window.getStyl());
		}

		protected void fire(MouseEvent dismiss) {
			fire();
		}

		protected void fire(KeyEvent e) {
			if (e.getCode().equals(KeyCode.SPACE)) {
				fire();
				e.consume();
			}
		}

		public void fire() {
			if (action != null) {
				action.run();
			}
		}

		public void setAction(Runnable action) {
			this.action = action;
		}
		
		@Override
		public void applyStyle(Style style) {
			backIcon.setFill(style.getTextNormal());
			backLab.setFill(style.getTextNormal());
			thirdParties.setFill(style.getTextNormal());
			backgroundProperty().bind(
					Bindings.when(hoverProperty()).then(Backgrounds.make(style.getBackgroundModifierSelected(), 7.0))
							.otherwise(Backgrounds.make(style.getBackgroundModifierAccent(), 7.0)));
			
			NodeUtils.focusBorder(this, style.getTextLink(), 7.0);
		}

		@Override
		public void applyStyle(ObjectProperty<Style> style) {
			Styleable.bindStyle(this, style);
		}
		
	}

	private static class Project extends VBox implements Styleable {
		private Text name;
		private MultiText license;

		public Project(Window window, String nameString, String iconString, String websiteString, String licenseName,
				String licenselink) {
			super(7);

			HBox top = new HBox(15);
			top.setAlignment(Pos.CENTER);

			VBox topLeft = new VBox(3);

			int size = 48;
			ImageView icon = new ImageView(ImageProxy.resize(ImageProxy.load(iconString, 128), size));
			Rectangle clip = new Rectangle(size, size);
			clip.setArcHeight(size / 3.0);
			clip.setArcWidth(size / 3.0);
			icon.setClip(clip);

			name = new Text(nameString, new Font(Font.DEFAULT_FAMILY_MEDIUM, 14));

			Link website = new Link(window, websiteString, new Font(12));
			website.setAction(() -> window.openLink(websiteString));

			license = new MultiText(window, licenseName == null ? "Unknow license type" : "Licensed under ",
					new Font(12));
			if (licenseName != null) {
				license.addLink(licenseName, new Font(12));
				license.setAction(1, () -> window.openLink(licenselink));
			}

			topLeft.getChildren().addAll(name, website, license);

			top.getChildren().addAll(topLeft, new Separator(window, Orientation.HORIZONTAL), icon);

			getChildren().add(top);

			applyStyle(window.getStyl());
		}

		@Override
		public void applyStyle(Style style) {
			name.setFill(style.getTextNormal());
			license.setFill(style.getTextNormal());
		}

		@Override
		public void applyStyle(ObjectProperty<Style> style) {
			Styleable.bindStyle(this, style);
		}
	}

	private static class Me extends HBox implements Styleable {

		private ImageView picture;
		private VBox info;
		private Text name;
		private Text username;

		private ColorIcon external;

		public Me(Window window) {
			super(15);
			setAlignment(Pos.CENTER_LEFT);
			setPadding(new Insets(10, 15, 10, 10));
			setCursor(Cursor.HAND);
			setOnMouseClicked(e -> window.openLink("https://github.com/SDIDSA"));

			name = new Text("Zinelabidine Teyar", new Font(Font.DEFAULT_FAMILY_MEDIUM, 16));
			username = new Text("SDIDSA", new Font(14));
			info = new VBox(5, name, username);
			info.setAlignment(Pos.CENTER_LEFT);

			picture = new ImageView();
			picture.setFitWidth(48);
			picture.setFitHeight(48);

			Rectangle clip = new Rectangle(48, 48);
			clip.setArcHeight(15);
			clip.setArcWidth(15);

			picture.setClip(clip);

			external = new ColorIcon("external", 24);

			ImageProxy.asyncLoad("https://avatars.githubusercontent.com/u/34898903", 48, picture::setImage);

			getChildren().addAll(picture, info, new ExpandingHSpace(), external);

			applyStyle(window.getStyl());
		}

		@Override
		public void applyStyle(Style style) {
			name.setFill(style.getTextNormal());
			username.setFill(style.getTextNormal());

			external.setFill(style.getTextMuted());

			backgroundProperty().bind(
					Bindings.when(hoverProperty()).then(Backgrounds.make(style.getBackgroundModifierSelected(), 10.0))
							.otherwise(Backgrounds.make(style.getBackgroundModifierAccent(), 10.0)));
		}

		@Override
		public void applyStyle(ObjectProperty<Style> style) {
			Styleable.bindStyle(this, style);
		}

	}

	@Override
	public void applyStyle(Style style) {
		version.setFill(style.getChannelsDefault());

		copyRighted.setFill(style.getTextNormal());
		license1.setFill(style.getTextNormal());
		license2.setFill(style.getTextNormal());
		thirdParties.setFill(style.getTextNormal());
		madeBy.setFill(style.getTextNormal());

		

		super.applyStyle(style);
	}
}
