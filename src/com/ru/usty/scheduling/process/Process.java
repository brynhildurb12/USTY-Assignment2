package com.ru.usty.scheduling.process;

/**
 * 
 * DO NOT CHANGE THIS CLASS
 *
 */

public class Process {

	private int processID;
	private boolean running = false;
	private boolean finished = false;
	private long timeCreated;
	private long totalServiceTime;
	private long timeLastUpdated;
	private long elapsedExecutionTime;

	public Process(int id, long timeToRun) {
		this.processID = id;
		this.timeCreated = System.currentTimeMillis();
		this.totalServiceTime = timeToRun;
		this.timeLastUpdated = -1;
		this.elapsedExecutionTime = 0;

		this.running = false;
		this.finished = false;
	}

	public int getID() {
		return processID;
	}

	public void startRunning() {
		if(!running) {
			timeLastUpdated = System.currentTimeMillis();
			running = true;
		}
		else {
			update();
		}
	}

	public void stopRunning() {
		update();
		running = false;
	}

	public void update() {
		if(running && !finished) {
			long currentTime = System.currentTimeMillis();
			elapsedExecutionTime += (currentTime - timeLastUpdated);
			if(elapsedExecutionTime >= totalServiceTime) {
				elapsedExecutionTime = totalServiceTime;
				finished = true;
			}
			timeLastUpdated = currentTime;
		}
	}

	public long getTotalServiceTime() {
		return totalServiceTime;
	}

	public long getElapsedExecutionTime() {
		return elapsedExecutionTime;
	}

	public long getElapsedWaitingTime() {
		return System.currentTimeMillis() - timeCreated - elapsedExecutionTime;
	}

	public boolean isFinished() {
		return finished;
	}
}
