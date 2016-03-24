package com.ru.usty.scheduling;

import com.ru.usty.scheduling.process.ProcessExecution;
import java.util.*;

public class Scheduler {

	ProcessExecution processExecution;
	Policy policy;
	int quantum;

	/**
	 * Add any objects and variables here (if needed)
	 */
	Queue<Integer> q = new LinkedList<Integer>();
	boolean noProcessRunning = true;


	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	public Scheduler(ProcessExecution processExecution) {
		this.processExecution = processExecution;

		/**
		 * Add general initialization code here (if needed)
		 */
	}

	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	public void startScheduling(Policy policy, int quantum) {

		this.policy = policy;
		this.quantum = quantum;

		/**
		 * Add general initialization code here (if needed)
		 */

		switch(policy) {
		case FCFS:	//First-come-first-served
			System.out.println("Starting new scheduling task: First-come-first-served");
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			break;
		case RR:	//Round robin
			System.out.println("Starting new scheduling task: Round robin, quantum = " + quantum);
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			break;
		case SPN:	//Shortest process next
			System.out.println("Starting new scheduling task: Shortest process next");
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			break;
		case SRT:	//Shortest remaining time
			System.out.println("Starting new scheduling task: Shortest remaining time");
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			break;
		case HRRN:	//Highest response ratio next
			System.out.println("Starting new scheduling task: Highest response ratio next");
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			break;
		case FB:	//Feedback
			System.out.println("Starting new scheduling task: Feedback, quantum = " + quantum);
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			break;
		}

		/**
		 * Add general scheduling or initialization code here (if needed)
		 */

	}

	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	public void processAdded(int processID) {

		/**
		 * Add scheduling code here
		 */
		q.add(processID);
		
		if(noProcessRunning == true)
		{
			processExecution.switchToProcess(q.remove()); 
			noProcessRunning = false;
		}
	}

	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */ 
	public void processFinished(int processID) {

		/**
		 * Add scheduling code here
		 */
		if(!q.isEmpty())
		{
			processExecution.switchToProcess(q.remove());
		}
		else
		{
			noProcessRunning = true;
		}
	}
}
