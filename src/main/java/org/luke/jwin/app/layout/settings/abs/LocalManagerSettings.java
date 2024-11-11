package org.luke.jwin.app.layout.settings.abs;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.luke.gui.controls.Font;
import org.luke.gui.controls.alert.AlertButton;
import org.luke.gui.controls.alert.ButtonType;
import org.luke.gui.controls.image.ColoredIcon;
import org.luke.gui.controls.input.combo.ComboInput;
import org.luke.gui.controls.input.combo.ComboItem;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.controls.popup.Direction;
import org.luke.gui.controls.popup.context.ContextMenu;
import org.luke.gui.controls.popup.context.items.MenuItem;
import org.luke.gui.controls.popup.tooltip.KeyedTooltip;
import org.luke.gui.controls.popup.tooltip.TextTooltip;
import org.luke.gui.controls.space.ExpandingHSpace;
import org.luke.gui.controls.space.Separator;
import org.luke.gui.style.Style;
import org.luke.gui.window.Window;
import org.luke.jwin.local.managers.LocalInstall;
import org.luke.jwin.local.ui.DownloadJob;
import org.luke.jwin.local.ui.DownloadState;

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;

public abstract class LocalManagerSettings extends SettingsContent {

	private final VersionsDisplay managed;
	private final VersionsDisplay local;

	private final Label defLab;

	private final Settings settings;

	protected final ComboInput defCombo;

	private final DirectoryChooser dc;

	public LocalManagerSettings(Settings settings, String name) {
		super(settings);
		this.settings = settings;

		ContextMenu addList = makeContextMenu(settings);

		dc = new DirectoryChooser();

		defLab = new Label(settings.getWindow(), "default_" + name.toLowerCase() + "_version");

		defCombo = new ComboInput(settings.getWindow(), new Font(14), "unset");

		defCombo.valueProperty().addListener((_, _, nv) -> setDefaultVersion(nv));

		defCombo.setMinWidth(250);

		defCombo.setCreator(this::createComboItem);

		AlertButton clear = new AlertButton(settings.getWindow(), ButtonType.CANCEL);
		clear.setKey("clear");
		clear.setAction(this::clearDefault);

		HBox def = new HBox(10, defLab, new ExpandingHSpace(), defCombo, clear);
		def.setAlignment(Pos.CENTER_LEFT);

		ColoredIcon addManaged = new ColoredIcon(settings.getWindow(), "add", 24, Style::getTextMuted);
		addManaged.setAction(() -> addList.showPop(addManaged, Direction.DOWN_LEFT, 10));
		addManaged.setCursor(Cursor.HAND);

		KeyedTooltip addVersion = new KeyedTooltip(settings.getWindow(), "add", Direction.UP, 15, 15);
		TextTooltip.install(addManaged, addVersion);

		ColoredIcon addLocal = new ColoredIcon(settings.getWindow(), "add", 24, Style::getTextMuted);
		addLocal.setAction(() -> {
			File f = dc.showDialog(settings.getWindow());
			if (f != null) {
				addInst(f);
				refreshLocal();
			}
		});
		addLocal.setCursor(Cursor.HAND);

		KeyedTooltip browse = new KeyedTooltip(settings.getWindow(), "browse", Direction.UP, 15, 15);
		TextTooltip.install(addLocal, browse);

		managed = new VersionsDisplay(settings.getWindow(), "managed_by_jwin", addManaged);

		local = new VersionsDisplay(settings.getWindow(), "in_your_system", addLocal);

		managed.maxWidthProperty().bind(widthProperty().subtract(111).divide(2));

		local.maxWidthProperty().bind(widthProperty().subtract(111).divide(2));

		getChildren().add(def);

		separate(settings.getWindow(), 20);

		getChildren().addAll(new HBox(15, managed, new Separator(settings.getWindow(), Orientation.VERTICAL), local));

		refresh();
		applyStyle(settings.getWindow().getStyl());
	}

	private ContextMenu makeContextMenu(Settings settings) {
		ContextMenu addList = new ContextMenu(settings.getWindow());
		addList.setVScrollable(300);
		addList.addOnShowing(() -> {
			addList.clear();
			addList.addMenuItems(installableVersions().stream().map(v -> {
				MenuItem i = new MenuItem(addList, v);
				i.setAction(() -> {
					DownloadJob job = install(settings.getWindow(), v);

					job.addOnStateChanged(s -> {
						if (s == DownloadState.DONE || s == DownloadState.CANCELED || s == DownloadState.FAILED) {
							Platform.runLater(this::refreshManaged);
						}
					});

					refresh();
				});
				return i;
			}).collect(Collectors.toList()));
		});
		return addList;
	}

	private void refresh() {
		refreshManaged();
		refreshLocal();
	}

	private void refreshLocal() {
		local.clear();

		HashMap<String, Node> lines = new HashMap<>();

		localInstalls().forEach(ver ->
				lines.put(ver.getVersion(), localUi(settings.getWindow(), ver, this::refreshLocal)));

		List<String> versions = new ArrayList<>(lines.keySet());
		versions.sort(comparator());

		versions.forEach(v -> local.addLine(lines.get(v)));

		refreshCombo();

		local.empty();
	}

	private void refreshManaged() {
		managed.clear();

		HashMap<String, Node> lines = new HashMap<>();

		managedInstalls().forEach(installed -> {
			String ver = installed.getName();
			lines.put(ver, managedUi(settings.getWindow(), ver, this::refresh));
		});

		downloadJobs().forEach(job -> {
			String ver = job.getVersion();
			lines.put(ver, job);
		});

		List<String> versions = lines.keySet().stream().sorted(comparator()).toList();

        versions.forEach(v -> managed.addLine(lines.get(v)));

		refreshCombo();

		managed.empty();
	}

	private void refreshCombo() {
		defCombo.clearItems();

		ArrayList<ComboItem> items = new ArrayList<>();
		managedInstalls().forEach(f ->
				items.add(createComboItem(defCombo.getPopup(), f.getAbsolutePath())));
		localInstalls().forEach(f ->
				items.add(createComboItem(defCombo.getPopup(), f.getRoot().getAbsolutePath())));

		items.sort((p1, p2) -> comparator().compare(p1.getDisplay(), p2.getDisplay()));

		defCombo.addItems(items.toArray(new ComboItem[0]));

		String defVer = getDefaultVersion();
		if (isValid(defVer)) {
			defCombo.setValue(defVer);
		} else {
			defCombo.setValue("");
		}
	}

	public abstract Comparator<String> comparator();

	public abstract List<DownloadJob> downloadJobs();

	public abstract Node managedUi(Window win, String version, Runnable refresh);

	public abstract Node localUi(Window win, LocalInstall version, Runnable refresh);

	public abstract List<File> managedInstalls();

	public abstract List<LocalInstall> localInstalls();

	public abstract List<String> installableVersions();

	public abstract DownloadJob install(Window win, String version);

	public abstract void setDefaultVersion(String version);

	public abstract String getDefaultVersion();

	public abstract boolean isValid(String version);

	public abstract void addInst(File dir);

	public abstract void clearDefault();

	public abstract ComboItem createComboItem(ContextMenu men, String key);

	@Override
	public void applyStyle(Style style) {
		defLab.setFill(style.getHeaderSecondary());

		super.applyStyle(style);
	}
}
