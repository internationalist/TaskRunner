package org.aguntuk.threadengine;

class TaskThreadEvent<T> {
	private TaskThread<T> source;
	
	public TaskThreadEvent(TaskThread<T> source) {
		this.source = source;
	}

	public TaskThread<T> getSource() {
		return source;
	}
}
