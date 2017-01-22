package org.aguntuk.threadengine;

import java.util.Iterator;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

public class Engine<T> extends Thread implements TaskThreadEventListener<T> {
	private final static Logger logger = Logger.getLogger(Engine.class);
	private Queue<T> jobQueue;
	private volatile boolean running = false;
	private long lastRunTime;
	private Queue<TaskThread<T>> freeThreads;
	private Queue<TaskThread<T>> busyThreads;
	private Supplier<Queue<T>> populator;
	
	private Consumer<T> task;
	private Configuration config = new Configuration() {
		@Override
		public void init() {
			this.intervalInMillis = 5000;
			this.minThreadCount = 2;
			this.maxThreadCount = 5;
			this.threadIncrementSize=2;
		}
	};

	@Override
	public void run() {
		try {
			String methodName = "run()";
			running = true;
			long interval;
			logger.info("Max jobs per cycle is " + config.maxJobsPerCycle);
			while(running) {
				interval = System.currentTimeMillis() - lastRunTime;
				if(interval > config.intervalInMillis) {
					logger.debug(methodName + " Free Thread Size: " + freeThreads.size());
					logger.debug(methodName + " Busy Thread Size: " + busyThreads.size());
					int numJobsAssigned=0;
					//invoke the job populator if it is not null
					invokeJobPopulator(this.populator);
					for(Iterator<T> iterator = jobQueue.iterator(); iterator.hasNext();) {
						if(config.maxJobsPerCycle==0 || numJobsAssigned < config.maxJobsPerCycle) {
							T data = iterator.next();
							TaskThread<T> thread = freeThreads.poll();
							if(thread != null) {
								assignJobToThread(iterator, data, thread);						
							} else {
								//out of threads increment it.
								if(freeThreads.size() + busyThreads.size() < config.maxThreadCount) {
									int toMaxThreadCount = config.maxThreadCount - (freeThreads.size() + busyThreads.size());
									int newThreads = toMaxThreadCount < config.threadIncrementSize?toMaxThreadCount:config.threadIncrementSize;
									createNewTreads(newThreads);
									thread = freeThreads.poll();
									assignJobToThread(iterator, data, thread);							
								} else {
									logger.debug(methodName + " Max thread number reached. No more free threads.");
									break;							
								}
							}
							++numJobsAssigned;
						} else {
							logger.debug(methodName + " Max jobs being processed!");
							break;
						}

					}
					lastRunTime = System.currentTimeMillis();
				}
			}
		} catch(Throwable t) {
			//catch any and all exceptions and report it
			logger.error(Utils.instance.getStackTrace(t));
		}
	}

	private void assignJobToThread(Iterator<T> iterator, T data, TaskThread<T> thread) {
		thread.setData(data);
		busyThreads.add(thread);
		iterator.remove();
	}
	
	public Engine(Consumer<T> c, Supplier<Queue<T>> populator, Configuration config) {
		if(config != null) {
			this.config=config;
		}
		this.jobQueue = new ConcurrentLinkedQueue<T>();
		this.populator=populator;
		invokeJobPopulator(this.populator);
		this.task = c;
		freeThreads = new ConcurrentLinkedQueue<TaskThread<T>>();
		busyThreads = new ConcurrentLinkedQueue<TaskThread<T>>();
		createNewTreads(this.config.minThreadCount);
		logger.info("##############################TASKRUNNER STARTING WITH CONFIGURATION###################################");
		logger.info(this.config);
		logger.info("#######################################################################################################");
		this.start();
	}

	private void invokeJobPopulator(Supplier<Queue<T>> populator) {
		if(populator != null) {
			Queue<T> assignedJobs = populator.get();
			if(assignedJobs != null && !(assignedJobs instanceof ConcurrentLinkedQueue)) {
				this.jobQueue=new ConcurrentLinkedQueue<T>(assignedJobs);
			} else if(assignedJobs != null && this.jobQueue == null) {
				this.jobQueue=assignedJobs;
			} else if(assignedJobs != null && this.jobQueue != null) {
				this.jobQueue.addAll(assignedJobs);
			}
		}
	}
	
	public Engine(Consumer<T> c) {
		this(c, null, null);
	}
	
	public Engine(Consumer<T> c, Configuration config) {
		this(c, null, config);
	}	
	
	public Engine(Consumer<T> c, Supplier<Queue<T>> populator) {
		this(c, populator, null);
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
	

	public long getIntervalInMillis() {
		return config.intervalInMillis;
	}

	public void setIntervalInMillis(long intervalInMillis) {
		this.config.intervalInMillis = intervalInMillis;
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
		}, new Configuration() {
			@Override
			public void init() {
				this.intervalInMillis=1000;
				this.minThreadCount=50;
				this.maxThreadCount=50;
				this.maxJobsPerCycle=50;
			}		
		});

		
		for(int i = 0; i < 500; i++) {
			engine.addJob("Job " + i);
		}
		engine.addJob("New Job Added!");
	}

	public final void onServiceEnd(TaskThreadEvent<T> event) {
		String methodName = "onServiceEnd";
		TaskThread<T> tt = event.getSource();
		logger.trace(methodName + " Before freeing thread Free:" + freeThreads.size() + " busy:" + busyThreads.size());
		busyThreads.remove(tt);
		freeThreads.add(tt);
		logger.trace(methodName + " After freeing thread Free:" + freeThreads.size() + " busy:" + busyThreads.size());		
	}

}
