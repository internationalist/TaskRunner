package org.aguntuk.threadengine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.aguntuk.resource.Resource;

public class TaskThread<T> implements Runnable, Resource {
	
	private Consumer<T> task;
	private T data;
	private List<TaskThreadEventListener<T>> listeners;
	private String threadName;

	public String getThreadName() {
		return threadName;
	}

	private boolean jobAssigned = false;
	

	public void init() throws Throwable {
		
	}
	
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

	public synchronized void setData(T data) {
		this.data = data;
		jobAssigned=true;
		notifyAll();
	}

	public TaskThread(String name, Consumer<T> task) {
		this.task = task;
		listeners = new ArrayList<TaskThreadEventListener<T>>();
	}
	
	public void addListener(TaskThreadEventListener<T> listener) {
		this.listeners.add(listener);
	}

	public void service() throws Throwable {
		task.accept(this.data);
	}

	public void shutdown() throws Throwable {
		// TODO Auto-generated method stub
		
	}
	
	public boolean isBusy() {
		return jobAssigned;
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
                    //while
                    service();
                    jobAssigned=false; 
                    for(TaskThreadEventListener<T> listener:listeners) {
                    	TaskThreadEvent<T> event = new TaskThreadEvent<T>(this);
                    	listener.onServiceEnd(event);
                    }
                }
            }
	        //while
	    //if bSetupDone
	    } catch(Throwable t) {
	    	t.printStackTrace();
	    }
    }

}
