package io.transwarp.thread;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

public class CountThread implements Runnable {
    public static AtomicLong count = new AtomicLong(0);
    public static AtomicLong latency = new AtomicLong(0);
    
    private static final Logger LOG = Logger.getLogger(CountThread.class);
    
    private long interval;
    private long stopTime;
    
    public CountThread(long interval, long stopTime) {
    	this.interval = interval;
    	this.stopTime = stopTime;
    }
    
    @Override
    public void run() {
    	boolean premanent = false;
    	if(stopTime == -1) {
    		premanent = true;
    	}
    	long preCount = 0;
    	long preLatency = 0;
    	while(true) {
    		long newCount = count.get();
    		long newLatency = latency.get();
    		System.out.println("count:" + (newCount - preCount) + "\t"
                    + "avg latency:" + ((newCount - preCount) == 0 ? -1 : (newLatency - preLatency)/(newCount - preCount)));
    		preCount = newCount;
    		preLatency = newLatency;
    		try {
    			Thread.sleep(interval);
    		}catch(Exception e) {
    			LOG.error("sleep thread error : " + e.getMessage());
    		}
    		stopTime -= interval;
    		if(!premanent && stopTime <= 0) {
    			break;
    		}
    	}
    }
}
