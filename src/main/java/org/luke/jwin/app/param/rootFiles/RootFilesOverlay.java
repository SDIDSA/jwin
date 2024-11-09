package org.luke.jwin.app.param.rootFiles;

import javafx.geometry.Pos;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import org.luke.gui.controls.Font;
import org.luke.gui.controls.alert.BasicOverlay;
import org.luke.gui.controls.check.KeyedCheck;
import org.luke.gui.controls.label.keyed.Label;
import org.luke.gui.controls.space.ExpandingHSpace;
import org.luke.gui.style.Style;
import org.luke.gui.window.Page;
import org.luke.jwin.app.file.RootFileScanner;
import org.luke.jwin.app.layout.JwinUi;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class RootFilesOverlay extends BasicOverlay {
    private final Page ps;
    private final JwinUi config;

    private final FlowPane root;

    private final Label topLab;
    private final KeyedCheck showAll;
    private Runnable onShowallChanged;

    public RootFilesOverlay(Page ps, JwinUi config) {
        super(ps, 800);
        this.ps = ps;
        this.config = config;
        head.setKey("root_files_head");

        setAutoHide(true);
        removeCancel();

        topLab = new Label(ps.getWindow(), "root_files_subhead",
                new Font(16));
        showAll = new KeyedCheck(ps.getWindow(), "root_files_show_all", 16);
        showAll.checkedProperty().addListener((_,_,_) -> {
            if(onShowallChanged != null) onShowallChanged.run();
        });

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(topLab, new ExpandingHSpace(), showAll);

        removeTop();

        removeSubHead();

        root = new FlowPane(10, 10);

        center.getChildren().addAll(top, root);

        setAction(this::hide);

        applyStyle(ps.getWindow().getStyl());
    }

    public void show(List<RootFileScanner.DetectedFile> detectedFiles) {
        if(detectedFiles == null) return;
        onShowallChanged = null;
        showAll.checkedProperty().set(false);
        onShowallChanged = () -> refreshFiles(detectedFiles);
        refreshFiles(detectedFiles);
        super.show();
    }

    private void refreshFiles(List<RootFileScanner.DetectedFile> detectedFiles) {
        root.getChildren().clear();
        for(File f : Objects.requireNonNull(config.getClasspath().getRoot().listFiles())) {
            if(f.isFile()) {
                RootFileScanner.DetectedFile df = null;
                for(RootFileScanner.DetectedFile pdf : detectedFiles) {
                    if(pdf.file().equals(f)) {
                        df = pdf;
                        break;
                    }
                }
                if(df == null) {
                    if(!showAll.checkedProperty().get()) {
                        continue;
                    }
                    df = new RootFileScanner.DetectedFile(f, "category_unknown", "file", false);
                }
                RootFileEntry rfe = new RootFileEntry(ps.getWindow(), config, f, df);
                root.getChildren().add(rfe);
            }
        }
    }

    @Override
    public void applyStyle(Style style) {
        topLab.setFill(style.getHeaderPrimary());
        super.applyStyle(style);
    }
}
