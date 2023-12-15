package org.luke.gui.exception;

public class ErrorHandler {
	private ErrorHandler() {
		
	}
	
	public static void handle(Exception x, String task) {
		System.err.println(x.getClass().getSimpleName() + " occured while trying to " + task + "\ncause : " + x.getMessage());
		for(StackTraceElement el : x.getStackTrace()) {
			if(el.getClassName().indexOf("mesa.") == 0) {
				System.err.println("\tat " + el.getClassName() + "." + el.getMethodName()+ "("+el.getFileName()+":"+el.getLineNumber()+")");
			}
		}
	}
}
