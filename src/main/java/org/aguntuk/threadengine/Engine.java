package org.aguntuk.threadengine;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Consumer;

public class Engine<T> extends Thread implements TaskThreadEventListener<T> {
	private Queue<T> jobQueue;
	private volatile boolean running = false;
	private long lastRunTime;
	private long intervalInMillis = 5000;
	private Queue<TaskThread<T>> freeThreads;
	private Queue<TaskThread<T>> busyThreads;
	
	private int minThreadCount=2;
	private int maxThreadCount=5;
	private int threadIncrementSize=2;
	private Consumer<T> task;

	@Override
	public void run() {
		running = true;
		//lastRunTime=System.currentTimeMillis();
		long interval;
		while(running) {
			interval = System.currentTimeMillis() - lastRunTime;
			if(interval > intervalInMillis) {
				System.out.println("Free Thread Size: " + freeThreads.size());
				System.out.println("Busy Thread Size: " + busyThreads.size());				
				for(Iterator<T> iterator = jobQueue.iterator(); iterator.hasNext();) {
					T data = iterator.next();
					TaskThread<T> thread = freeThreads.poll();
					if(thread != null) {
						assignJobToThread(iterator, data, thread);						
					} else {
						//out of threads increment it.
						if(freeThreads.size() + busyThreads.size() < maxThreadCount) {
							int toMaxThreadCount = maxThreadCount - (freeThreads.size() + busyThreads.size());
							int newThreads = toMaxThreadCount < threadIncrementSize?toMaxThreadCount:threadIncrementSize;
							createNewTreads(newThreads);
							thread = freeThreads.poll();
							assignJobToThread(iterator, data, thread);							
						} else {
							System.out.println("Max thread number reached. No more free threads.");
							break;							
						}
					}					
				}
				lastRunTime = System.currentTimeMillis();
			}
		}
	}

	private void assignJobToThread(Iterator<T> iterator, T data, TaskThread<T> thread) {
		thread.setData(data);
		busyThreads.add(thread);
		iterator.remove();
	}
	
	public Engine(Consumer<T> c) {
		this.task = c;
		freeThreads = new LinkedList<TaskThread<T>>();
		busyThreads = new LinkedList<TaskThread<T>>();
		createNewTreads(minThreadCount);
	}

	private void createNewTreads(int newThreads) {
		for(int i = 0; i < newThreads; i++) {
			TaskThread<T> thread = new TaskThread<T>(UUID.randomUUID().toString(), task);
			Thread t = new Thread(thread);
			thread.addListener(this);
			t.start();
			freeThreads.add(thread);
		}
	}
	
	public Queue<T> getJobQueue() {
		return jobQueue;
	}

	public void setJobQueue(Queue<T> jobQueue) {
		this.jobQueue = jobQueue;
	}

	public long getLastRunTime() {
		return lastRunTime;
	}

	public void setLastRunTime(long lastRunTime) {
		this.lastRunTime = lastRunTime;
	}

	public long getIntervalInMillis() {
		return intervalInMillis;
	}

	public void setIntervalInMillis(long intervalInMillis) {
		this.intervalInMillis = intervalInMillis;
	}

	public boolean isRunning() {
		return running;
	}
	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public void addJob(T data) {
		this.jobQueue.add(data);
	}
	
	public static void main(String[] args) throws InterruptedException {
		Engine<String> engine = new Engine<String>((String s)->{
			try {
				Thread.sleep(8000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(s);
		});
		Queue<String> queue = new LinkedList<String>();
		for(int i = 0; i < 100; i++) {
			queue.add("data " + i);
		}
		engine.setJobQueue(queue);
		engine.start();
		engine.addJob("New Job Added!");
		//engine.setRunning(false);
		
	}

	public void onServiceEnd(TaskThreadEvent<T> event) {
		TaskThread<T> tt = event.getSource();
		System.out.println("Before freeing thread Free:" + freeThreads.size() + " busy:" + busyThreads.size());
		busyThreads.remove(tt);
		freeThreads.add(tt);
		System.out.println("After freeing thread Free:" + freeThreads.size() + " busy:" + busyThreads.size());		
	}

}
