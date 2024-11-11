package org.luke.jwin.app.more;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.alert.BasicOverlay;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.controls.space.Separator;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Page;
import org.luke.jwin.app.Jwin;
import org.luke.jwin.app.file.FileDealer;
import org.luke.jwin.app.file.FileTypeAssociation;
import org.luke.jwin.app.file.UrlProtocolAssociation;
import org.luke.jwin.ui.Button;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MoreSettings extends BasicOverlay {
	private final FileTypeParam fileTypeParam;
	private final UrlProtocolParam urlProtocolParam;

	private final Button clearTemp;
	
	public MoreSettings(Page ps) {
		super(ps);
		removeTop();
		removeSubHead();
		
		head.setKey("more_settings");

		fileTypeParam = new FileTypeParam(ps.getWindow());

		urlProtocolParam = new UrlProtocolParam(ps.getWindow());

		clearTemp = new Button(ps.getWindow(), "clear_jwin_temp_files", 20, 38);

		clearTemp.setAction(() -> {
			clearTemp.startLoading();

			new Thread(() -> {
				long size = FileDealer.clearTemp();
				Jwin.instance.getConfig().logStd("cleared " + FileDealer.formatSize(size) + " of cache files");
				Platform.runLater(clearTemp::stopLoading);
			}, "cache cleaner").start();
		});

		center.getChildren().setAll(new MoreParam(ps, "associate_file_type", fileTypeParam),
				new MoreParam(ps, "associate_url_protocol", urlProtocolParam));
		
		addToBottom(0, clearTemp);
		
		done.setAction(this::hide);
		
		center.setAlignment(Pos.CENTER);
		
		applyStyle(ps.getWindow().getStyl());
	}

	public FileTypeAssociation getFileTypeAssociation() {
		return fileTypeParam.getValue();
	}

	public UrlProtocolAssociation getUrlProtocolAssociation() {
		return urlProtocolParam.getValue();
	}

	public void setFileTypeAssociation(FileTypeAssociation fileTypeAsso) {
		fileTypeParam.set(fileTypeAsso);
	}

	public void setUrlProtocolAssociation(UrlProtocolAssociation urlProtocolAsso) {
		urlProtocolParam.set(urlProtocolAsso);
	}
	
	@Override
	public void applyStyle(Style style) {
		clearTemp.setFill(style.getSecondaryButtonBack());
		super.applyStyle(style);
	}

	private static class MoreParam extends VBox implements Styleable {
		private final Label lab;
		public MoreParam(Page page, String name, Node node) {
			super(20);

			lab = new Label(page.getWindow(), name, new Font(14));

			Separator sep = new Separator(page.getWindow(), Orientation.HORIZONTAL);
			HBox top = new HBox(10, lab, sep);
			top.setAlignment(Pos.CENTER);

			getChildren().addAll(top, node);
			
			applyStyle(page.getWindow().getStyl());
		}

		@Override
		public void applyStyle(Style style) {
			lab.setFill(style.getTextNormal());
		}

		@Override
		public void applyStyle(ObjectProperty<Style> style) {
			Styleable.bindStyle(this, style);
		}
	}
}
