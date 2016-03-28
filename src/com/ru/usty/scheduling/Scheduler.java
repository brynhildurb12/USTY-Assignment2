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
	boolean noProcessRunning = true;
	
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
	
	PriorityQueue<ProcessOnQueue> queueSRT = new PriorityQueue<ProcessOnQueue>(10, new Comparator<ProcessOnQueue>(){
		
		@Override
		public int compare(ProcessOnQueue p1, ProcessOnQueue p2){
			
			if((p1.totalService - p1.executing) < (p2.totalService - p2.executing)){
				return -1;
			}
			if((p1.totalService - p1.executing) > (p2.totalService - p2.executing)){
				return 1;
			}
			return 0;
				
	}});
	
	

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
			
		case SRT:
			
			ProcessInfo infoAdd = processExecution.getProcessInfo(processID);
			ProcessOnQueue processAdd = new ProcessOnQueue();
			processAdd.processID = processID;
			processAdd.totalService = infoAdd.totalServiceTime;
			processAdd.totalService = infoAdd.elapsedExecutionTime;
			
			//ef engin að keyra þá öddum við honum og setjum af stað
			if(noProcessRunning == true){
				processOut = processAdd;
				processExecution.switchToProcess(processOut.processID); 
				noProcessRunning = false;
				
			}
			//annars athugum við á keyrandi process
			else {
				
				ProcessInfo infoRun = processExecution.getProcessInfo(processOut.processID);
				ProcessOnQueue processStopped = new ProcessOnQueue();
				processStopped.processID = processOut.processID;
				processStopped.executing = infoRun.elapsedExecutionTime;
				processStopped.totalService = infoRun.totalServiceTime;
				
				if((infoAdd.totalServiceTime - infoAdd.elapsedExecutionTime) < (infoRun.totalServiceTime - infoRun.elapsedExecutionTime)){
					
					processOut = processAdd;
					processExecution.switchToProcess(processOut.processID);
					queueSRT.add(processStopped);
					
				}
				else{
					queueSRT.add(processAdd);
					
					if(noProcessRunning == true){
						processOut = queueSRT.remove();
						processExecution.switchToProcess(processAdd.processID); 
						noProcessRunning = false;
					}
				}
			}
			
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
				processExecution.switchToProcess(procID);
			}
			else{
				noProcessRunning = true;
			}
			break;
		case RR:
			if(!q.isEmpty()){
				procID = q.remove();
				startedProcess = System.currentTimeMillis();
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
		case SRT:
			if(!queue.isEmpty()){
				processOut = queueSRT.remove();
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
		
		switch(this.policy) {
	
		case RR:
		
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
		case SRT:
			
			/*while(true){
				
				if(!queueSRT.isEmpty() && noProcessRunning == true){
					System.out.println("hallo while");
					processOut = queueSRT.remove();
					processExecution.switchToProcess(processOut.processID);
				}
				if(this.policy != Policy.SRT){
					return; 
				}
			}*/
		default:
			break;
				
			}
	}	
}

