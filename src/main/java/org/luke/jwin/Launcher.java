package org.luke.jwin;

import javafx.application.Platform;
import org.json.JSONArray;
import org.luke.gui.exception.ErrorHandler;
import org.luke.jwin.app.Jwin;

import javafx.application.Application;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Launcher {
	public static void main(String[] args) {
		int port = 1301;
		try (Socket clientSocket = new Socket("127.0.0.1" ,port)){
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8));
			JSONArray arr = new JSONArray();
			for(String arg : args) {
				arr.put(arg);
			}
			bw.write(arr + System.lineSeparator());
			bw.flush();
		}catch (Exception x) {
			Thread t = new Thread(() -> {
                try (ServerSocket server = new ServerSocket(port)){
					while(true) {
						Socket socket = server.accept();
						BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
						String data = br.readLine();
						JSONArray arr = new JSONArray(data);
						Platform.runLater(() -> {
							Jwin.winstance.setIconified(false);
							Jwin.winstance.setAlwaysOnTop(true);
							Jwin.winstance.setAlwaysOnTop(false);
						});
						for(int i = 0; i < arr.length(); i++) {
							String s = arr.getString(i);
							if(s.toLowerCase().endsWith(".jwp")) {
								Platform.runLater(() -> Jwin.instance.getConfig().importProject(new File(s)));
								break;
							}
						}
					}
                } catch (IOException e) {
					ErrorHandler.handle(e, "start socket server");
                }
            });
			t.setDaemon(true);
			t.start();
			Application.launch(Jwin.class, args);
		}
	}
}
