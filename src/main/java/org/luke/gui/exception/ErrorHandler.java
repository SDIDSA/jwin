package org.luke.gui.exception;

/**
 * Utility class for handling exceptions in a standardized way.
 * This class provides a static method for handling exceptions by printing
 * relevant information to the standard error stream, including the exception
 * class name, the task being performed, and the exception message.
 */
public class ErrorHandler {

    private ErrorHandler() {
        // Private constructor to prevent instantiation; this class is intended for static use only.
    }

    /**
     * Handles an exception by printing relevant information to the standard error stream.
     * @param x The exception to handle.
     * @param task A description of the task or operation being performed when the exception occurred.
     */
    public static void handle(Throwable x, String task) {
        RuntimeException rx = new RuntimeException(x.getClass().getSimpleName() +
                " occurred while trying to " + task + "\ncause: " + x.getMessage(), x);
        rx.printStackTrace(System.err);
    }
}
