package org.luke.jwin.app.file;

import org.json.JSONObject;
import org.json.JSONArray;
import org.luke.jwin.app.Jwin;
import org.luke.jwin.app.layout.JwinUi;
import org.luke.jwin.app.param.rootFiles.RootFileState;
import org.luke.jwin.app.param.rootFiles.RootFileType;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class RootFileScanner {
    private final Path rootFolder;
    private static final JSONObject config = loadConfig();

    public RootFileScanner(Path rootFolder) {
        this.rootFolder = rootFolder;
    }

    private static JSONObject loadConfig() {
        return new JSONObject(FileDealer.read("/root_copy.json"));
    }

    public record DetectedFile(File file, String category, String iconName, boolean isSensitive) {}

    public List<DetectedFile> scanRootFiles() throws IOException {
        List<DetectedFile> detectedFiles = new ArrayList<>();
        Set<Path> excludedPaths = getExcludedPaths();

        JSONObject categories = config.getJSONObject("categories");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootFolder)) {
            for (Path path : stream) {
                if (excludedPaths.contains(path)) {
                    continue;
                }

                Path relativePath = rootFolder.relativize(path);
                String pathStr = relativePath.toString().replace('\\', '/');

                for (String categoryName : categories.keySet()) {
                    JSONObject category = categories.getJSONObject(categoryName);
                    JSONArray patterns = category.getJSONArray("patterns");
                    boolean isSensitive = category.optBoolean("sensitive", false);
                    String icon = category.optString("icon", "file");

                    if (matchesAnyPattern(pathStr, patterns)) {
                        detectedFiles.add(new DetectedFile(
                                path.toFile(),
                                "category_" + categoryName,
                                icon,
                                isSensitive || pathStr.toLowerCase().contains("secret")
                        ));
                        break;
                    }
                }
            }
        }

        return detectedFiles;
    }

    public RootFileType type(Path file) {
        Path relativePath = rootFolder.relativize(file);
        String pathStr = relativePath.toString().replace('\\', '/');
        Set<Path> excludedPaths = getExcludedPaths();

        if (excludedPaths.contains(file)) {
            return RootFileType.EXCLUDE;
        }

        JSONObject categories = config.getJSONObject("categories");

        for (String categoryName : categories.keySet()) {
            JSONObject category = categories.getJSONObject(categoryName);
            JSONArray patterns = category.getJSONArray("patterns");
            boolean isSensitive = category.optBoolean("sensitive", false);

            if (matchesAnyPattern(pathStr, patterns)) {
                if(isSensitive) {
                    return RootFileType.SENSITIVE;
                }else {
                    return RootFileType.INCLUDE;
                }
            }
        }

        return RootFileType.UNKNOWN;
    }

    private boolean matchesAnyPattern(String path, JSONArray patterns) {
        for (int i = 0; i < patterns.length(); i++) {
            String pattern = patterns.getString(i);
            if (matchesGlobPattern(path, pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesGlobPattern(String path, String pattern) {
        pattern = pattern.replace(".", "\\.")
                .replace("*", ".*")
                .replace("/", "\\/");
        return path.matches(pattern);
    }

    private Set<Path> getExcludedPaths() {
        Set<Path> excludedPaths = new HashSet<>();
        JSONArray excludedPathPatterns = config.getJSONArray("excludedPaths");

        for (int i = 0; i < excludedPathPatterns.length(); i++) {
            String pattern = excludedPathPatterns.getString(i);
            Path path = rootFolder.resolve(pattern);
            if (Files.exists(path)) {
                excludedPaths.add(path);
            }
        }

        return excludedPaths;
    }

    public static List<DetectedFile> scanRoot(File file) throws IOException {
        if(file == null || !file.exists()) {
            Jwin.instance.getConfig().logErr("no_root");
            return null;
        }
        RootFileScanner scanner = new RootFileScanner(file.toPath());
        return scanner.scanRootFiles();
    }

    public static RootFileType type(File file) {
        return new RootFileScanner(file.getParentFile().toPath()).type(file.toPath());
    }

    public static RootFileState state(File file, JwinUi config) {
        if(config.getRootFiles().getRun().contains(file)) {
            return RootFileState.RUN;
        }

        if(config.getRootFiles().getInclude().contains(file)) {
            return RootFileState.INCLUDED;
        }

        if(config.getRootFiles().getExclude().contains(file)) {
            return RootFileState.EXCLUDED;
        }

        return RootFileState.UNSET;
    }
}