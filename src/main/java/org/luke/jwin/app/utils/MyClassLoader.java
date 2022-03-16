package org.luke.jwin.app.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class MyClassLoader {
	private MyClassLoader() {

	}

	public static Class<?> loadClass(String name, File... classPath) throws MalformedURLException {
		URL[] urls = new URL[classPath.length];
		for (int i = 0; i < classPath.length; i++) {
			urls[i] = classPath[i].toURI().toURL();
		}
		try (URLClassLoader cl = new URLClassLoader(urls)) {
			return cl.loadClass(name);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}
}