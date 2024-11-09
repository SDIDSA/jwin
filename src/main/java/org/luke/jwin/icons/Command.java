package org.luke.jwin.icons;

import java.io.*;
import java.util.Arrays;
import java.util.function.Consumer;

public class Command {
    private final String[] command;

    private final Consumer<String> inputHandler;
    private final Consumer<String> errorHandler;

    public Command(Consumer<String> inputHandler, Consumer<String> errorHandler, String... command) {
        this.command = command;
        this.inputHandler = inputHandler;
        this.errorHandler = errorHandler;
    }

    public Command(Consumer<String> inputHandler, String... command) {
        this(inputHandler, inputHandler, command);
    }

    public Command(String... command) {
        this(null, null, command);
    }

    public Process execute(File root, Runnable...periodicals) {
        try {
            Process process = Runtime.getRuntime().exec(command, null, root);

            new Thread(() -> {
                InputStream is = process.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                try {
                    while ((line = br.readLine()) != null) {
                        if (inputHandler != null)
                            inputHandler.accept(line);
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
                        if (errorHandler != null)
                            errorHandler.accept(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            new Thread(()-> {
                while(process.isAlive()) {
                    for(Runnable periodical : periodicals) {
                        periodical.run();
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    }
                }

            }).start();
            return process;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}