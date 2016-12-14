package org.aguntuk.threadengine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

class TaskThread<T> implements Runnable {
	private final static Logger logger = Logger.getLogger(TaskThread.class);	
	
	private Consumer<T> task;
	private T data;
	private List<TaskThreadEventListener<T>> listeners;
	private String threadName;

	String getThreadName() {
		return threadName;
	}

	private boolean jobAssigned = false;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((threadName == null) ? 0 : threadName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		TaskThread<T> other = (TaskThread<T>) obj;
		if (threadName == null) {
			if (other.threadName != null)
				return false;
		} else if (!threadName.equals(other.threadName))
			return false;
		return true;
	}

	synchronized void setData(T data) {
		this.data = data;
		jobAssigned=true;
		notifyAll();
	}

	TaskThread(String name, Consumer<T> task) {
		this.threadName = name;
		this.task = task;
		listeners = new ArrayList<TaskThreadEventListener<T>>();
	}
	
	void addListener(TaskThreadEventListener<T> listener) {
		this.listeners.add(listener);
	}

	void service() throws Throwable {
		String methodname = "service()";
		logger.trace("ThreadName::" + threadName + "::" + methodname + " starting");
		task.accept(this.data);
		logger.trace("ThreadName::" + threadName + "::" + methodname + " exiting");		
	}
	
    /**
     *  Main processing method for the TaskThread object
     */
    public void run() {
    	try {
            while(true) {
                synchronized(this) {
                    //wait until assigned a new robot job
                    while(jobAssigned==false) {
                        try {
                            wait();
                        } catch(InterruptedException e) {
                        }
                    }
                    service();
                    jobAssigned=false; 
                    fireThreadEndEvent();
                }
            }
	    } catch(Throwable t) {
	    	logger.error("ThreadName::" + threadName + " Error occured. " + Utils.instance.getStackTrace(t));
	    }
    }

	private void fireThreadEndEvent() {
		for(TaskThreadEventListener<T> listener:listeners) {
			TaskThreadEvent<T> event = new TaskThreadEvent<T>(this);
			listener.onServiceEnd(event);
		}
	}

}
