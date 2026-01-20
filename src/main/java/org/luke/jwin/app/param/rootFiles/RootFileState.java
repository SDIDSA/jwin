package org.luke.jwin.app.param.rootFiles;

public enum RootFileState {
    UNSET("root_file_unset"),
    RUN("root_file_run"),
    EXCLUDED("root_file_excluded"),
    INCLUDED("root_file_included");

    private final String text;

    RootFileState(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
