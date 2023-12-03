package org.luke.jwin.app.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class MyClassLoader {
	private MyClassLoader() {

	}

	public static Class<?> loadClass(String name, File... classPath) throws UnsupportedClassVersionError, ClassNotFoundException, IOException {
		URL[] urls = new URL[classPath.length];
		for (int i = 0; i < classPath.length; i++) {
			urls[i] = classPath[i].toURI().toURL();
		}
		
		try (URLClassLoader cl = new URLClassLoader(urls)) {
			return cl.loadClass(name);
		}
	}
}