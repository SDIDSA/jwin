package org.luke.gui.exception;

public class ErrorHandler {
	private ErrorHandler() {
		
	}
	
	public static void handle(Exception x, String task) {
		System.err.println(x.getClass().getSimpleName() + " occured while trying to " + task + "\ncause : " + x.getMessage());
		x.printStackTrace();
	}
}
