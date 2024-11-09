package org.luke.jwin.app.layout.settings.abs;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.alert.Overlay;
import org.luke.gui.controls.scroll.VerticalScrollBar;
import org.luke.gui.controls.space.ExpandingHSpace;
import org.luke.gui.controls.space.FixedHSpace;
import org.luke.gui.controls.space.FixedVSpace;
import org.luke.gui.factory.Backgrounds;
import org.luke.gui.factory.Borders;
import org.luke.gui.style.Style;
import org.luke.gui.style.Styleable;
import org.luke.gui.window.Window;
import org.luke.jwin.app.layout.settings.abs.left.SettingsMenu;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class Settings extends StackPane implements Styleable {
	private Window win;
	private ExpandingHSpace leftBack;
	private ExpandingHSpace rightBack;

	private StackPane content;
	protected SettingsMenu sideBar;

	private VerticalScrollBar sideSb;
	private VerticalScrollBar mainSb;
	private HBox root;
	
	private Text esc;

	public Settings(Window win) {
		this.win = win;
		setMinHeight(0);
		setMaxHeight(-1);
		
		HBox back = new HBox();
		leftBack = new ExpandingHSpace();
		rightBack = new ExpandingHSpace();
		back.getChildren().setAll(leftBack, rightBack);
		getChildren().add(back);
		root = new HBox();
		root.setMinHeight(0);
		root.setMaxHeight(-1);
		root.setAlignment(Pos.TOP_CENTER);
		root.setPadding(new Insets(0, 8, 0, 0));

		content = new StackPane();
		HBox.setHgrow(content, Priority.ALWAYS);

		sideBar = new SettingsMenu(this);
		
		DoubleBinding w = win.widthProperty().subtract(15).subtract(sideBar.widthProperty());

		content.setMinWidth(0);
		content.maxWidthProperty().bind(Bindings.when(w.lessThan(740)).then(w).otherwise(740));

		sideSb = new VerticalScrollBar(6, 1);
		sideSb.install(root, sideBar);

		sideSb.bindOpacityToHover(sideBar);

		VBox exitCont = new VBox();
		exitCont.setAlignment(Pos.TOP_CENTER);

		CloseIcon close = new CloseIcon(this);
		
		esc = new Text("ESC");
		esc.setFont(new Font(Font.DEFAULT_FAMILY_MEDIUM, 13).getFont());
		
		exitCont.getChildren().addAll(new FixedVSpace(15), close, new FixedVSpace(8), esc);

		mainSb = new VerticalScrollBar(16, 4);
		mainSb.install(root, content);
		
		StackPane.setAlignment(mainSb, Pos.CENTER_RIGHT);

		root.getChildren().addAll(sideBar, sideSb, content, exitCont, new FixedHSpace(15));
		
		Rectangle clip = new Rectangle();
		clip.widthProperty().bind(root.widthProperty());
		clip.heightProperty().bind(root.heightProperty());
		
		root.setClip(clip);

		getChildren().addAll(root, mainSb);

		setOnMousePressed(e-> requestFocus());
		
		applyStyle(win.getStyl());
	}
	
	public HBox getRoot() {
		return root;
	}
	
	public void loadContent(SettingsContent content) {
		this.content.getChildren().setAll(content);
	}
	
	public ReadOnlyDoubleProperty contentWidth() {
		return content.widthProperty();
	}
	
	public Window getWindow() {
		return win;
	}
	
	public Overlay getOwner() {
		return getOwner(this);
	}
	
	private static Overlay getOwner(Node node) {
		Node parent = node.getParent();
		if(parent == null) return null;
		if(parent instanceof Overlay overlay) {
			return overlay;
		}else {
			return getOwner(parent);
		}
	}

	@Override
	public void applyStyle(Style style) {
		rightBack.setBackground(Backgrounds.make(style.getBackgroundTertiaryOr(), new CornerRadii(0, 5, 5, 0, false)));
		leftBack.setBackground(Backgrounds.make(style.getBackgroundSecondary(), new CornerRadii(5, 0, 0, 5, false)));

		sideSb.setThumbFill(style.getScrollbarThinThumb());
		sideSb.setTrackFill(style.getScrollbarThinTrack());

		mainSb.setThumbFill(style.getBackgroundFloatingOr());
		mainSb.setTrackFill(style.getBackgroundPrimary());

		content.setBackground(rightBack.getBackground());
		content.setBorder(Borders.make(style.getBackgroundModifierSelected(), new BorderWidths(0, 0, 0, 1)));
		
		esc.setFill(style.getTextMuted());
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}
}
