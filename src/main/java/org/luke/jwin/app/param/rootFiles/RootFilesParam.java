package org.luke.jwin.app.param.rootFiles;

import org.luke.gui.window.Window;
import org.luke.jwin.app.file.RootFileScanner;
import org.luke.jwin.app.layout.JwinUi;
import org.luke.jwin.app.param.JavaParam;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RootFilesParam extends JavaParam {
    private RootFilesOverlay overlay;
    private final ArrayList<File> include;
    private final ArrayList<File> exclude;
    private final ArrayList<File> run;
    private final JwinUi config;

    public RootFilesParam(Window window, JwinUi config) {
        super(window, "root_files");
        this.config = config;

        include = new ArrayList<>();
        exclude = new ArrayList<>();
        run = new ArrayList<>();
    }

    public void showOverlay(List<RootFileScanner.DetectedFile> detectedFiles) {
        showOverlay(detectedFiles, null);
    }

    public void showOverlay(List<RootFileScanner.DetectedFile> detectedFiles, Runnable onHidden) {
        if(overlay == null) {
            overlay = new RootFilesOverlay(getWindow().getLoadedPage(), config);
        }
        if(onHidden != null) {
            overlay.addOnHiddenOnce(onHidden);
        }
        overlay.show(detectedFiles);
    }

    public ArrayList<File> getInclude() {
        return include;
    }

    public ArrayList<File> getExclude() {
        return exclude;
    }

    public ArrayList<File> getRun() {
        return run;
    }
}
