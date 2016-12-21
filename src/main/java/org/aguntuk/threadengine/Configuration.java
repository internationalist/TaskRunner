package org.aguntuk.threadengine;

public abstract class Configuration {
	public long intervalInMillis=5000;
	public int minThreadCount=2;
	public int maxThreadCount=5;
	public int threadIncrementSize=2;
	public int maxJobsPerCycle;
	public Configuration() {
		init();
	}

	void validate() throws Exception {
		if(minThreadCount > maxThreadCount) {
			throw new Exception("Minimum thread count cannot be greater then maximum thread count");
		}
	}
	public abstract void init();
	
	@Override
	public String toString() {
		return "Configuration [intervalInMillis=" + intervalInMillis + ", minThreadCount=" + minThreadCount
				+ ", maxThreadCount=" + maxThreadCount + ", threadIncrementSize=" + threadIncrementSize + "]";
	}	
}
