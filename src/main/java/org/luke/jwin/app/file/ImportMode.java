package org.luke.jwin.app.file;

public enum ImportMode {
    JAVA("drag_java_project", "import-java"),
    JWIN("drag_jwin_project", "import-jwin");

    private final String text;
    private final String icon;

    ImportMode(String text, String icon) {
        this.text = text;
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public String getIcon() {
        return icon;
    }
}
