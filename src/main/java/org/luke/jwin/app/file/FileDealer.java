package org.luke.jwin.app.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;

import org.luke.jwin.app.JwinActions;

public class FileDealer {
	private FileDealer() {
	}

	public static String read(String path) {
		return read(FileDealer.class.getResourceAsStream(path));
	}

	public static String read(File file) {
		try {
			return read(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String read(InputStream is) {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))){
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	public static void write(String content, File dest) {
		try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dest)))){
			bw.append(content);
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void clearTemp() {
		File temp = new File(
				System.getProperty("java.io.tmpdir"));
		
		for(File file : temp.listFiles()) {
			if(file.getName().indexOf("jwin_") == 0) {
				if(file.isFile()) {
					try {
						Files.delete(file.toPath());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else {
					JwinActions.deleteDir(file);
				}
			}
		}
	}
}
