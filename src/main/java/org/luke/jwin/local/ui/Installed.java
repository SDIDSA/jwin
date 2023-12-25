package org.luke.jwin.local.ui;

import java.io.File;
import org.luke.gui.controls.space.ExpandingHSpace;
import org.luke.gui.locale.Locale;
import org.luke.gui.window.Window;

public class Installed extends LocalInstallUi {

	private Runnable onRemove;

	public Installed(Window win, String version, File targetDir, Runnable onChange) {
		super(win, version, targetDir);

		ManagerButton remove = new ManagerButton(win, "close", Locale.key("remove_version", "ver", version));

		remove.setAction(() -> {
			if (onRemove != null)
				onRemove.run();
			if (onChange != null)
				onChange.run();
		});

		stateLabl.setKey("installed");

		root.getChildren().addAll(new ExpandingHSpace(), remove);

		applyStyle(win.getStyl());
	}

	public void setOnRemove(Runnable onRemove) {
		this.onRemove = onRemove;
	}
}
