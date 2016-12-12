package org.aguntuk.threadengine;

public interface TaskThreadEventListener<T> {
	
	public void onServiceEnd(TaskThreadEvent<T> event);

}
