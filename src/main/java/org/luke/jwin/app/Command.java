package org.luke.jwin.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.function.Consumer;

public class Command {
	private String[] command;

	private Runnable onFinished;
	
	private OutputStreamWriter input;

	private ArrayList<Consumer<String>> inputHandlers;
	private ArrayList<Consumer<String>> errorHandlers;
	
	private ArrayList<Runnable> onExit;

	public Command(Consumer<String> inputHandler, Consumer<String> errorHandler, String... command) {
		this.command = command;
		this.inputHandlers = new ArrayList<>();
		this.errorHandlers = new ArrayList<>();
		this.onExit = new ArrayList<>();

		if (inputHandler != null)
			inputHandlers.add(inputHandler);
		if (errorHandler != null)
			errorHandlers.add(errorHandler);

		for (int i = 0; i < command.length; i++) {
			command[i] = URLDecoder.decode(command[i], Charset.defaultCharset());
		}
	}

	public Command(Consumer<String> inputHandler, String... command) {
		this(inputHandler, null, command);
	}

	public Command(String... command) {
		this(null, null, command);
	}

	public void setOnFinished(Runnable onFinished) {
		this.onFinished = onFinished;
	}
	
	public void addInputHandler(Consumer<String> inputHandler) {
		inputHandlers.add(inputHandler);
	}
	
	public void addErrorHandler(Consumer<String> errorHandler) {
		errorHandlers.add(errorHandler);
	}
	
	public void write(String b) {
		try {
			input.append(b);
			input.append(System.lineSeparator());
			input.flush();
		} catch (IOException e) {
			System.out.println("failed to write to process input due to " + e.getMessage());
		}
	}
	
	public void addOnExit(Runnable r) {
		onExit.add(r);
	}

	public Process execute(File root, Runnable... periodicals) {
		try {
			Process process = new ProcessBuilder(command).directory(root).start();
			
			new Thread(() -> {
				while(process.isAlive()) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				onExit.forEach(Runnable::run);
			}).start();
			
			input = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8);
			
			new Thread(() -> {
				InputStream is = process.getInputStream();

				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String line;
				try {
					while ((line = br.readLine()) != null) {
						String fline = line;
						inputHandlers.forEach(inputHandler -> {
							if (inputHandler != null)
								inputHandler.accept(fline);
						});
						System.out.println("Std : " + line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();

			new Thread(() -> {
				InputStream is = process.getErrorStream();

				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String line;
				try {
					while ((line = br.readLine()) != null) {
						String fline = line;
						errorHandlers.forEach(errorHandler -> {
							if (errorHandler != null)
								errorHandler.accept(fline);
						});
						System.out.println("Err : " + line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();

			new Thread(() -> {
				while (process.isAlive()) {
					for (Runnable periodical : periodicals) {
						periodical.run();
					}

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
						Thread.currentThread().interrupt();
					}
				}

				if (onFinished != null) {
					onFinished.run();
				}

			}).start();
			return process;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}
}
