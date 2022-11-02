package org.luke.jwin.app.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.luke.jwin.app.Command;

public class ClassCompiler {
	public static Class<?> compile(Map.Entry<String, File> launcher, List<File> cp, File preBuild, File jdk, File preBuildLibs) {
		
		File preBuildBin = new File(preBuild.getAbsolutePath().concat("/bin"));
		preBuildBin.mkdir();
		File binDir = new File(jdk.getAbsolutePath().concat("/bin"));
		StringBuilder cpc = new StringBuilder();
		Consumer<String> append = path -> cpc.append(cpc.isEmpty() ? "" : ";").append(path);
		append.accept(preBuildLibs.getAbsolutePath().concat("/*"));
		cp.forEach(file -> append.accept(file.getAbsolutePath()));
		ArrayList<String> x = new ArrayList<>();
		Command compileCommand = new Command(line -> {
			if (!line.isBlank()) {
				x.add(line);
			}
		}, "cmd.exe", "/C", "javac -cp \"" + cpc + "\" -d \"" + preBuildBin.getAbsolutePath() + "\" \""
				+ launcher.getValue().getAbsolutePath() + "\"");
		try {
			compileCommand.execute(binDir).waitFor();

			if (!x.isEmpty()) {
				IllegalStateException ex = new IllegalStateException("Failed to Compile");
				ex.initCause(new IllegalStateException(String.join("\n", x)));

				throw ex;
			}

			return MyClassLoader.loadClass(launcher.getKey(), preBuildBin);
		}catch(Exception xx) {
			
		}
		
		return null;
	}
}
