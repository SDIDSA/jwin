package org.luke.gui.window;

import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.geometry.NodeOrientation;
import org.json.JSONObject;
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

/**
 * custom JavaFX Stage with additional functionality.
 * 
 * @author SDIDSA
 */
public class Window extends Stage {
	// Data associated with the window
	private final HashMap<String, Object> data = new HashMap<>();

	// Listeners to be executed on window close
	private final ArrayList<Runnable> onClose = new ArrayList<>();

	// Width of the window border
	private final DoubleProperty borderWidth;

	// Object properties for style and locale
	private final ObjectProperty<Style> style;
	private final ObjectProperty<Locale> locale;

	// Root content of the window
	private final AppPreRoot root;

	// Reference to the JavaFX application
	private final Application app;

	// Constructor to initialize the window with a specified style and locale
	public Window(Application app, Style style, Locale locale) {
		super();
		this.app = app;
		this.style = new SimpleObjectProperty<>(style);
		this.locale = new SimpleObjectProperty<>(locale);

		borderWidth = new SimpleDoubleProperty(0);

		initStyle(StageStyle.TRANSPARENT);

		root = new AppPreRoot(this);

		setStyle(style);
		setLocale(locale);

		TransparentScene scene = new TransparentScene(root, 500, 500);

		setScene(scene);

		// Request focus when the window is shown
		setOnShown(e -> root.requestFocus());

		// Handle close request to execute custom close actions
		setOnCloseRequest(e -> {
			e.consume();
			close();
		});
	}

	// Getter for the JavaFX application
	public Application getApp() {
		return app;
	}

	// Add a button to the app bar
	public void addBarButton(AppBarButton button) {
		addBarButton(0, button);
	}

	// Add a button to the app bar at a specified index
	public void addBarButton(int index, AppBarButton button) {
		root.addBarButton(index, button);
	}

	// Set a custom action for the info button
	public void setOnInfo(Runnable runnable) {
		root.setOnInfo(runnable);
	}

	// Get the info button from the app bar
	public AppBarButton getInfo() {
		return root.getInfo();
	}

	// Open a link in the default browser
	public void openLink(String link) {
		app.getHostServices().showDocument(link);
	}

	// Add a custom action to be executed on window close
	public void addOnClose(Runnable runnable) {
		onClose.add(runnable);
	}

	// Get the operating system name
	public String getOsName() {
		String osName = System.getProperty("os.name").toLowerCase();
		return osName.indexOf("win") == 0 ? "Windows" : ""; // Will handle other operating systems when targeting them
	}

	// Get the app bar from the window
	public AppBar getAppBar() {
		return root.getAppBar();
	}

	// Load a page asynchronously
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
		}, "page loader for " + type.getName()).start();
	}

	// Load a page asynchronously
	public void loadPageAsync(Class<? extends Page> type) {
		loadPageAsync(type, null);
	}

	// Load a page into the window
	private Page loadedPage;

	public void loadPage(Page page) {
		loadedPage = page;
		root.setContent(page);
		centerOnScreen();
	}

	// Get the currently loaded page
	public Page getLoadedPage() {
		return loadedPage;
	}

	// Set the fill color of the window
	public void setFill(Paint fill) {
		root.setFill(fill);
	}

	// Set the border color and width of the window
	public void setBorder(Paint fill, double width) {
		root.setBorder(fill, width);
		borderWidth.set(width);
	}

	// Get the window border width property
	public DoubleProperty getBorderWidth() {
		return borderWidth;
	}

	// Get the padded property of the root content
	public BooleanProperty paddedProperty() {
		return root.paddedProperty();
	}

	// Get the style property of the window
	public ObjectProperty<Style> getStyl() {
		return style;
	}

	// Get the locale property of the window
	public ObjectProperty<Locale> getLocale() {
		return locale;
	}

	// Set the style of the window
	public void setStyle(Style style) {
		this.style.set(style);
	}

	// Set the locale of the window
	public void setLocale(Locale locale) {
		this.locale.set(locale);
		root.setNodeOrientation(locale.isRtl() ?
				NodeOrientation.RIGHT_TO_LEFT :
				NodeOrientation.LEFT_TO_RIGHT);
	}

	// Get the root content of the window
	public AppPreRoot getRoot() {
		return root;
	}

	// Maximize or restore the window
	public void maxRestore() {
		if (root.isTiled()) {
			restore();
		} else {
			maximize();
		}
	}

	// Set the minimum size of the window
	public void setMinSize(Dimension d) {
		root.setMinSize(d);
	}

	// Maximize the window
	private void maximize() {
		root.applyTile(TileHint.tileForState(State.N));
	}

	// Restore the window
	private void restore() {
		root.unTile();
	}

	// Close the window
	@Override
	public void close() {
		onClose.forEach(Runnable::run);
		super.close();
	}

	// Put data into the window data map
	public void putData(String key, Object value) {
		data.put(key, value);
	}

	// Get JSON data from the window data map
	public JSONObject getJsonData(String key) throws IllegalStateException {
		return getOfType(key, JSONObject.class);
	}

	// Get data of a specific type from the window data map
	private <T> T getOfType(String key, Class<? extends T> type) {
		Object obj = data.get(key);

		if (type.isInstance(obj)) {
			return type.cast(obj);
		} else {
			throw new IllegalStateException("no " + type.getSimpleName() + " was found at key " + key);
		}
	}

	// Set the taskbar icon of the window
	public void setTaskIcon(String image) {
		Image m = ImageProxy.load(image, 256);
		for (int i = 16; i <= 128; i *= 2) {
			getIcons().add(ImageProxy.resize(m, i));
		}
	}

	// Set the window icon
	public void setWindowIcon(String image) {
		root.setIcon(image);
	}
}