package org.luke.gui.controls.tab;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

public class Tab {
	private TabPane owner;
	private TabTitle titleDisp;
	
	private final StringProperty title;
	private final ObjectProperty<Node> content;
	
	public Tab(String title) {
		this.title = new SimpleStringProperty(title);
		content = new SimpleObjectProperty<>();
	}
	
	public TabTitle getTitleDisp() {
		return titleDisp;
	}
	
	public void setTitleDisp(TabTitle titleDisp) {
		this.titleDisp = titleDisp;
	}
	
	public void setOwner(TabPane owner) {
		this.owner = owner;
	}
	
	public TabPane getOwner() {
		return owner;
	}
	
	public void select() {
		titleDisp.select();
	}
	
	public void unselect() {
		titleDisp.unselect();
	}
	
	public void setTitle(String title) {
		this.title.set(title);
	}
	
	public String getTitle() {
		return title.get();
	}
	
	public StringProperty titleProperty() {
		return title;
	}
	
	public void setContent(Node content) {
		this.content.set(content);
	}
	
	public Node getContent() {
		return content.get();
	}
	
	public ObjectProperty<Node> contentProperty() {
		return content;
	}
}
