package com.ru.usty.scheduling.process;

/**
 * 
 * DO NOT CHANGE THIS INTERFACE
 *
 */

public interface ProcessExecution {

	public ProcessInfo getProcessInfo(int processID);
	public void switchToProcess(int processID);
}
