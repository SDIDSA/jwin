package org.luke.jwin.app.more;

import org.luke.jwin.app.Jwin;
import org.luke.jwin.app.file.FileDealer;
import org.luke.jwin.app.file.FileTypeAssociation;
import org.luke.jwin.app.file.UrlProtocolAssociation;
import org.luke.jwin.ui.Button;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MoreSettings extends Stage {
	private FileTypeParam fileTypeParam;
	private UrlProtocolParam urlProtocolParam;

	public MoreSettings(Stage ps) {
		super(StageStyle.UTILITY);
		setTitle("More Settings");
		setResizable(false);
		setAlwaysOnTop(true);

		Runnable adapt = () -> {
			setY(ps.getY() + ps.getHeight() / 2 - getHeight() / 2);
			setX(ps.getX() + ps.getWidth() / 2 - getWidth() / 2);
		};
		ChangeListener<Number> listener = (obs, ov, nv) -> adapt.run();

		setOnShown(e -> {
			ps.getScene().getRoot().setDisable(true);
			adapt.run();
		});

		setOnHidden(e -> ps.getScene().getRoot().setDisable(false));

		ps.widthProperty().addListener(listener);
		ps.heightProperty().addListener(listener);
		ps.xProperty().addListener(listener);
		ps.yProperty().addListener(listener);

		fileTypeParam = new FileTypeParam(ps);

		urlProtocolParam = new UrlProtocolParam();
		
		Button clearTemp = new Button("Clear jwin temp files");
		clearTemp.setMinHeight(30);
		
		clearTemp.setOnAction(e-> {
			clearTemp.setDisable(true);
			
			new Thread(()-> {
				FileDealer.clearTemp();
				Platform.runLater(() -> clearTemp.setDisable(false));
			}).start();
		});
		
		VBox root = new VBox(10, makeParam("Associate file type", fileTypeParam), makeParam("Associate URL protocol", urlProtocolParam), Jwin.vSpace(), clearTemp);
		root.setPadding(new Insets(10));

		setScene(new Scene(root, 400, 400));
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

	private VBox makeParam(String name, Node node) {
		VBox root = new VBox(10);

		Label lab = new Label(name);
		lab.setFont(Font.font(14));
		
		Separator sep = new Separator();
		HBox.setHgrow(sep, Priority.ALWAYS);
		HBox top = new HBox(10, lab, sep);
		top.setAlignment(Pos.CENTER);
		
		
		root.getChildren().addAll(top, node, new Separator());

		return root;
	}
}
