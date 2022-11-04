package org.luke.gui.window;

import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;
import org.luke.gui.app.pages.Page;
import org.luke.gui.controls.image.ImageProxy;
import org.luke.gui.exception.ErrorHandler;
import org.luke.gui.locale.Locale;
import org.luke.gui.style.Style;
import org.luke.gui.window.content.AppPreRoot;
import org.luke.gui.window.content.TransparentScene;
import org.luke.gui.window.content.app_bar.AppBar;
import org.luke.gui.window.content.app_bar.AppBarButton;
import org.luke.gui.window.helpers.State;
import org.luke.gui.window.helpers.TileHint;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Window extends Stage {
	private HashMap<String, Object> data = new HashMap<>();

	private ArrayList<Runnable> onClose = new ArrayList<>();

	private DoubleProperty borderWidth;

	private ObjectProperty<Style> style;
	private ObjectProperty<Locale> locale;

	private AppPreRoot root;

	private Application app;

	public Window(Application app, Style style, Locale locale) {
		super();
		this.app = app;
		this.style = new SimpleObjectProperty<>(style);
		this.locale = new SimpleObjectProperty<>(locale);

		borderWidth = new SimpleDoubleProperty(0);

		initStyle(StageStyle.TRANSPARENT);
		setStyle(style);
		setLocale(locale);

		root = new AppPreRoot(this);

		TransparentScene scene = new TransparentScene(root, 500, 500);

		setScene(scene);

		setOnShown(e -> root.requestFocus());

		setOnCloseRequest(e -> {
			e.consume();
			close();
		});
	}

	public Application getApp() {
		return app;
	}
	
	public void addBarButton(AppBarButton button) {
		addBarButton(0, button);
	}
	
	public void addBarButton(int index, AppBarButton button) {
		root.addBarButton(index, button);
	}

	public void setOnInfo(Runnable runnable) {
		root.setOnInfo(runnable);
	}
	
	public AppBarButton getInfo() {
		return root.getInfo();
	} 

	public void openLink(String link) {
		app.getHostServices().showDocument(link);
	}

	public void addOnClose(Runnable runnable) {
		onClose.add(runnable);
	}

	public String getOsName() {
		String osName = System.getProperty("os.name").toLowerCase();
		return osName.indexOf("win") == 0 ? "Windows" : ""; // will handle other operating systems when targetting them
	}

	public AppBar getAppBar() {
		return root.getAppBar();
	}

	public void loadPageAsync(Class<? extends Page> type, Runnable onFinish) {
		new Thread(() -> {
			try {
				Page page = type.getConstructor(Window.class).newInstance(this);
				Platform.runLater(() -> {
					loadPage(page);
					if (onFinish != null) {
						onFinish.run();
					}
				});
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException x) {
				ErrorHandler.handle(x, "create page (" + type.getSimpleName() + ".java:0)");
			}
		}).start();
	}

	public void loadPageAsync(Class<? extends Page> type) {
		loadPageAsync(type, null);
	}

	private Page loadedPage;

	public void loadPage(Page page) {
		loadedPage = page;
		root.setContent(page);
		centerOnScreen();
	}

	public Page getLoadedPage() {
		return loadedPage;
	}

	public void setFill(Paint fill) {
		root.setFill(fill);
	}

	public void setBorder(Paint fill, double width) {
		root.setBorder(fill, width);
		borderWidth.set(width);
	}

	public DoubleProperty getBorderWidth() {
		return borderWidth;
	}

	public BooleanProperty paddedProperty() {
		return root.paddedProperty();
	}

	public ObjectProperty<Style> getStyl() {
		return style;
	}

	public ObjectProperty<Locale> getLocale() {
		return locale;
	}

	public void setStyle(Style style) {
		this.style.set(style);
	}

	public void setLocale(Locale locale) {
		this.locale.set(locale);
	}

	public AppPreRoot getRoot() {
		return root;
	}

	public void maxRestore() {
		if (root.isTiled()) {
			restore();
		} else {
			maximize();
		}
	}

	public void setMinSize(Dimension d) {
		root.setMinSize(d);
	}

	private void maximize() {
		root.applyTile(TileHint.tileForState(State.N));
	}

	private void restore() {
		root.unTile();
	}

	@Override
	public void close() {
		onClose.forEach(Runnable::run);
		super.close();
	}

	public void putData(String key, Object value) {
		data.put(key, value);
	}

	public JSONObject getJsonData(String key) throws IllegalStateException {
		return getOfType(key, JSONObject.class);
	}

	private <T> T getOfType(String key, Class<? extends T> type) {
		Object obj = data.get(key);

		if (type.isInstance(obj)) {
			return type.cast(obj);
		} else {
			throw new IllegalStateException("no " + type.getSimpleName() + " was found at key " + key);
		}
	}

	public void setTaskIcon(String image) {
		Image m = ImageProxy.load(image, 256);
		for (int i = 16; i <= 128; i *= 2) {
			getIcons().add(ImageProxy.resize(m, i));
		}
	}

	public void setWindowIcon(String image) {
		root.setIcon(image);
	}

}
