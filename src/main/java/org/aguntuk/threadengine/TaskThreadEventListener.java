package org.aguntuk.threadengine;

interface TaskThreadEventListener<T> {
	
	void onServiceEnd(TaskThreadEvent<T> event);

}
