package com.ru.usty.scheduling.process;

/**
 * 
 * DO NOT CHANGE THIS CLASS
 *
 */

public class ProcessInfo {

	public long elapsedWaitingTime;
	public long elapsedExecutionTime;
	public long totalServiceTime;

	public ProcessInfo(long elapsedWaitingTime, long elapsedExecutionTime, long totalServiceTime) {
		this.elapsedWaitingTime = elapsedWaitingTime;
		this.elapsedExecutionTime = elapsedExecutionTime;
		this.totalServiceTime = totalServiceTime;
	}
}
