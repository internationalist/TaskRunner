package org.aguntuk.threadengine;

public enum Utils {
	instance;
	
	public String getStackTrace(Throwable t) {
		StringBuilder sb = new StringBuilder("\n");
		for (StackTraceElement element : t.getStackTrace()) {
		   sb.append(element.toString()).append("\n");
		}
		return sb.toString();
	}
}
