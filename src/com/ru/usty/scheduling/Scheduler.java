package com.ru.usty.scheduling;

import com.ru.usty.scheduling.process.ProcessExecution;
import com.ru.usty.scheduling.process.ProcessInfo;

import java.util.*;

public class  Scheduler implements Runnable  {

	ProcessExecution processExecution;
	Policy policy;
	int quantum; 
	int procID; 
	ProcessOnQueue processOut;
	long startedProcess; 
	
	Queue<Integer> q = new LinkedList<Integer>();
	PriorityQueue<ProcessOnQueue> queue = new PriorityQueue<ProcessOnQueue>(10, new Comparator<ProcessOnQueue>(){
		
		@Override
		public int compare(ProcessOnQueue p1, ProcessOnQueue p2){
			
			if(p1.totalService < p2.totalService){
				return -1;
			}
			if(p1.totalService > p2.totalService){
				return 1;
			}
			return 0;
				
	}});
	
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
			
			break;
		case RR:	//Round robin
			System.out.println("Starting new scheduling task: Round robin, quantum = " + quantum);
			
			Thread thread = new Thread(this);
			thread.start();
			
			break;
		case SPN:	//Shortest process next
			System.out.println("Starting new scheduling task: Shortest process next");
			
			break;
		case SRT:	//Shortest remaining time
			System.out.println("Starting new scheduling task: Shortest remaining time");
			
			break;
		case HRRN:	//Highest response ratio next
			System.out.println("Starting new scheduling task: Highest response ratio next");
			
			break;
		case FB:	//Feedback
			System.out.println("Starting new scheduling task: Feedback, quantum = " + quantum);
			
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
		

		switch(this.policy) {
		case FCFS:	
			q.add(processID);
			if(noProcessRunning == true){
				procID = q.remove();
				startedProcess = System.currentTimeMillis();
				processExecution.switchToProcess(procID); 
				noProcessRunning = false;
			}
			break;
		case RR:
			q.add(processID);
			if(noProcessRunning == true){
				procID = q.remove();
				startedProcess = System.currentTimeMillis();
				processExecution.switchToProcess(procID); 
				noProcessRunning = false;
			}
			
			break;
		case SPN:
			
			ProcessInfo info = processExecution.getProcessInfo(processID);
			
			ProcessOnQueue process = new ProcessOnQueue();
			
			process.processID = processID;
			process.totalService = info.totalServiceTime;
			
			queue.add(process);
			
				if(noProcessRunning == true){
				processOut = queue.remove();
				processExecution.switchToProcess(processOut.processID); 
				noProcessRunning = false;
			}
			
			break;
		
		default:
			break;
		}
	}

	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */ 
	public void processFinished(int processID) {
		
		switch(this.policy) {
		case FCFS:	
			if(!q.isEmpty()){
				procID = q.remove();
				startedProcess = System.currentTimeMillis();
				processExecution.switchToProcess(procID);
			}
			else{
				noProcessRunning = true;
			}
			break;
		case RR:
			if(!q.isEmpty()){
				procID = q.remove();
				processExecution.switchToProcess(procID);
			}
			else{
				noProcessRunning = true;
			}
			
			break;
		case SPN:
			if(!queue.isEmpty()){
				processOut = queue.remove();
				processExecution.switchToProcess(processOut.processID);
			}
			else{
				noProcessRunning = true;
			}
			break;
		default:
			break;
		}
		
		
	}

	@Override
	public void run() {
		
		while(true){
			try {
				Thread.sleep(quantum);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
		//sofa aftur ef einhver hefur verið startað aftur
		while(System.currentTimeMillis() - startedProcess < quantum){
			
			try {
				Thread.sleep(System.currentTimeMillis() - startedProcess);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		q.add(procID);
			
			if(!q.isEmpty()){
				
				procID = q.remove();
				startedProcess = System.currentTimeMillis();
				processExecution.switchToProcess(procID);
			}
			else{
				noProcessRunning = true;
			}
			
			if(this.policy != Policy.RR){
				return; 
			}
		}	
	}
}

