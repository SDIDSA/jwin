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
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.luke.gui.exception.ErrorHandler;

public class Command {
	private final String[] command;

	private OutputStreamWriter input;

	private final ArrayList<Consumer<String>> inputHandlers;
	private final ArrayList<Consumer<String>> errorHandlers;
	
	private final ArrayList<Consumer<Integer>> onExit;

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
			ErrorHandler.handle(e, "write to process input");
		}
	}
	
	public void addOnExit(Consumer<Integer> r) {
		onExit.add(r);
	}

	public Process execute(File root, Runnable... periodicals) {
		try {
			Process process = new ProcessBuilder(command).directory(root).start();

			input = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8);

			registerHandler(process.getInputStream(), inputHandlers);
			registerHandler(process.getErrorStream(), errorHandlers);

			ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor((r) -> {
				Thread t = new Thread(r, "exit checker for process");
				t.setDaemon(true);
				return t;
			});

			Runnable onExitCheck = () -> {
				if(process.isAlive()) {
					for (Runnable periodical : periodicals) {
						periodical.run();
					}
				}else {
					onExit.forEach(oe -> oe.accept(process.exitValue()));
					exec.shutdown();
					exec.close();
				}
			};

			exec.scheduleAtFixedRate(onExitCheck, 100, 100, TimeUnit.MILLISECONDS);
			return process;
		} catch (IOException e) {
			ErrorHandler.handle(e, "executing process");
			return null;
		}
	}

	private void registerHandler(InputStream stream, List<Consumer<String>> handlers) {
		new Thread(() -> {
			BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
			String line;
			try {
				while ((line = br.readLine()) != null) {
					String fline = line;
					handlers.forEach(handler -> {
						if (handler != null)
							handler.accept(fline);
					});
				}
			} catch (IOException e) {
				ErrorHandler.handle(e, "handling output");
			}
		}, "command output handler for " + String.join(" ", command)).start();
	}
}
