package org.luke.jwin.app;

import org.luke.gui.app.pages.Page;
import org.luke.gui.controls.Font;
import org.luke.gui.controls.alert.BasicOverlay;
import org.luke.gui.controls.image.ColorIcon;
import org.luke.gui.controls.image.ImageProxy;
import org.luke.gui.controls.label.MultiText;
import org.luke.gui.controls.label.unkeyed.Text;
import org.luke.gui.controls.space.ExpandingHSpace;
import org.luke.gui.controls.space.Separator;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;
import org.luke.jwin.app.utils.Backgrounds;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;

public class Credits extends BasicOverlay {
	private Text version;

	private Text copyRighted;
	private MultiText license;

	private MultiText thirdParties;

	private MultiText madeBy;

	public Credits(Page ps) {
		super(ps);
		removeTop();
		removeSubHead();
		removeCancel();

		head.setKey("About JWin");

		version = new Text("1.0.3", new Font("monospace", 16));

		copyRighted = new Text("Copyright © 2022\nThis product is copyrighted by Zinelabidine Teyar", new Font(12));
		copyRighted.setLineSpacing(10);
		copyRighted.setTextAlignment(TextAlignment.CENTER);

		license = new MultiText(ps.getWindow(),
				"This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.\n\rYou should have received a copy of the GNU General Public License along with this program. If not, see ",
				new Font(12));
		license.addLink("GNU General Public License v3.0", new Font(12));
		license.setLineSpacing(3);
		license.setAction(1, () -> ps.getWindow().openLink("https://www.gnu.org/licenses/gpl-3.0.txt"));

		thirdParties = new MultiText(ps.getWindow(), "JWin uses and heavily relies on ", new Font(14));
		thirdParties.addLink("other open-source projects");

		madeBy = new MultiText(ps.getWindow(), "Developed and maintained by", new Font(14));

		center.getChildren().setAll(copyRighted, license, new Separator(ps.getWindow(), Orientation.HORIZONTAL),
				thirdParties, new Separator(ps.getWindow(), Orientation.HORIZONTAL), madeBy, new Me(ps.getWindow()));

		ImageView icon = new ImageView(ImageProxy.resize(ImageProxy.load("jwin-task-icon", 256), 38));
		addToBottom(0, version);
		addToBottom(0, new ExpandingHSpace());
		addToBottom(0, icon);

		done.setAction(this::hide);
		done.setKey("Close");

		center.setAlignment(Pos.CENTER);

		applyStyle(ps.getWindow().getStyl());
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
		license.setFill(style.getTextNormal());
		thirdParties.setFill(style.getTextNormal());
		madeBy.setFill(style.getTextNormal());

		super.applyStyle(style);
	}
}
