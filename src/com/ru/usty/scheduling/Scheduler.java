package com.ru.usty.scheduling;

import com.ru.usty.scheduling.process.ProcessExecution;
import java.util.*;

public class  Scheduler implements Runnable  {

	ProcessExecution processExecution;
	Policy policy;
	int quantum; 
	int procID; 
	long startedProcess; 
	
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
		
			//starta þræði til að interrupta
			Thread thread = new Thread(this);
			thread.start();
			
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
		
		//get processId to a global scope - maybe this is not he right way to do this.
		//procID = processID;
		
		q.add(processID);
		
			if(noProcessRunning == true){
				procID = q.peek();
				processExecution.switchToProcess(q.remove()); 
				startedProcess = System.currentTimeMillis();
				noProcessRunning = false;
			}
	}

	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */ 
	public void processFinished(int processID) {
		
		if(!q.isEmpty()){
			procID = q.peek();
			processExecution.switchToProcess(q.remove());
			startedProcess = System.currentTimeMillis();
		}
		else{
			noProcessRunning = true;
		}
	}

	@Override
	public void run() {
		
		int i = 0; 
		
		
		while(i < 10){
			
			System.out.println("run:" + i);
			
			i++;
			
			try {
				Thread.sleep(quantum);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//sofa aftur ef einhver hefur verið startað aftur, kannski ekki réttir útreiknignar
			/*if(System.currentTimeMillis() - startedProcess > quantum){
				try {
					Thread.sleep(quantum - startedProcess);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}*/
			
			long elapsed = processExecution.getProcessInfo(procID).elapsedExecutionTime;
			long totalNeeded = processExecution.getProcessInfo(procID).totalServiceTime;
					
			System.out.println(elapsed);
			System.out.println(totalNeeded);
			if(elapsed != totalNeeded){
				processAdded(procID);
				System.out.println("Add back to queue" + procID);
			}
			else{
				processFinished(procID);
			}
			
			if(!q.isEmpty()){
				processExecution.switchToProcess(q.remove());
				startedProcess = System.currentTimeMillis();
			}
			else{
				noProcessRunning = true;
			}
			
			
		}
		
			
			
			
			
			
		}
	
	
}
