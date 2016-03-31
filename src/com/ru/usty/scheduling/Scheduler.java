package com.ru.usty.scheduling;

import com.ru.usty.scheduling.process.ProcessExecution;
import com.ru.usty.scheduling.process.ProcessInfo;

import java.util.*;

public class Scheduler implements Runnable  {

	ProcessExecution processExecution;
	Policy policy;
	int quantum;
	int procID; 

	//Variables for timing
	long totalResponseTime = 0;
	long totalTurnaroundTime = 0;
	long averageResponseTime = 0;
	long averageTurnaroundTime = 0;
	
	//Arrays for timing
	long[] arriving = new long[15];
	long[] finished = new long[15];
	long[] starting = new long[15];
	
	//Variables for the process
	ProcessOnQueue processOut;
	long startedProcess; 
	boolean noProcessRunning = true;
	int processCount = 0;
	
	//Thread for RR
	Thread threadRR;
	boolean oneThreadRR = true;
	
	//Queues for each policy
	Queue<Integer> queueFCFS = new LinkedList<Integer>();
	Queue<Integer> queueRR = new LinkedList<Integer>();
	Queue<ProcessOnQueue> allFBQueues[];

	PriorityQueue<ProcessOnQueue> queueSPN = new PriorityQueue<ProcessOnQueue>(10, new Comparator<ProcessOnQueue>(){

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

	PriorityQueue<ProcessOnQueue> queueHRRN = new PriorityQueue<ProcessOnQueue>(10, new Comparator<ProcessOnQueue>(){

		@Override
		public int compare(ProcessOnQueue p1, ProcessOnQueue p2){

			if((p1.waitingTime + p1.totalService)/p1.totalService > (p2.waitingTime + p2.totalService)/p2.totalService){
				return -1;
			}
			if((p1.waitingTime + p1.totalService)/p1.totalService < (p2.waitingTime + p2.totalService)/p2.totalService){
				return 1;
			}
			return 0;
		}});
	
	
	
	public Scheduler(ProcessExecution processExecution) {
		this.processExecution = processExecution;
	}

	//Starts scheduling according to the policy
	public void startScheduling(Policy policy, int quantum) {

		this.policy = policy;
		this.quantum = quantum;

		//Initialize arrays that manage arrival, starting and finished time
		for(int i = 0; i <= 14; i++){
			arriving[i] = 0;
			finished[i] = 0;
			starting[i] = 0;	
		}

		//Set all timer variables to zero for new policy
		totalResponseTime = 0;
		totalTurnaroundTime = 0;
		averageResponseTime = 0;
		averageTurnaroundTime = 0;
		processCount = 0;

		switch(policy) {
		case FCFS:	//First-come-first-served
			
			System.out.println("Starting new scheduling task: First-come-first-served");

			break;
		case RR:	//Round robin
			
			System.out.println("Starting new scheduling task: Round robin, quantum = " + quantum);

			//Only use one thread for RR
			if(oneThreadRR){
				threadRR = new Thread(this);
				threadRR.start();
			}
			oneThreadRR = false;

			break;
		case SPN:	//Shortest process next
			
			//Join the thread from the RR before starting SPN
			try {
				threadRR.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
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
			
			//Initialize the array
			allFBQueues = new Queue[7];
			for(int i = 0; i < 7; i++){
				allFBQueues[i] = new LinkedList<ProcessOnQueue>();
			}
			
			//Start the thread
			Thread threadFB = new Thread(this);
			threadFB.start();

			break;
		}
	}
	
	public void calculateTime (){
		for(int i = 0; i<this.processCount ; i++){
			
			this.totalResponseTime += (this.starting[i] - this.arriving[i]);
			this.totalTurnaroundTime += (this.finished[i] - this.arriving[i]);
		}
		
		this.averageResponseTime = (this.totalResponseTime/15);
		this.averageTurnaroundTime = (this.totalTurnaroundTime/15);
		System.out.println("Average Response Time: " + this.averageResponseTime);
		System.out.println("Average Turnaround Time: " + this.averageTurnaroundTime);	
	}

	//When processes are added to the system
	public void processAdded(int processID) {

		arriving[processID] = System.currentTimeMillis();

		switch(this.policy) {
		case FCFS:	

			queueFCFS.add(processID);
			
			if(noProcessRunning == true){
				
				procID = queueFCFS.remove();
				processExecution.switchToProcess(procID); 
				starting[procID] = System.currentTimeMillis();
				noProcessRunning = false;
			}
			
			break;
		case RR:

			queueRR.add(processID);

			if(noProcessRunning == true){
				
				procID = queueRR.remove();
				startedProcess = System.currentTimeMillis();
				processExecution.switchToProcess(procID); 
				noProcessRunning = false;
				
				if(starting[processID] == 0){
					starting[processID] = System.currentTimeMillis();
				}
			}
			
			break;
		case SPN:

			ProcessInfo info = processExecution.getProcessInfo(processID);
			ProcessOnQueue process = new ProcessOnQueue();
			process.processID = processID;
			process.totalService = info.totalServiceTime;

			if(noProcessRunning == true){
				
				queueSPN.add(process);
				processOut = queueSPN.remove();
				processExecution.switchToProcess(processOut.processID); 
				noProcessRunning = false;
				starting[processOut.processID] = System.currentTimeMillis();
			}
			else{
				queueSPN.add(process);
			}

			break;
		case SRT:

			ProcessInfo infoAdd = processExecution.getProcessInfo(processID);
			ProcessOnQueue processAdding = new ProcessOnQueue();
			processAdding.processID = processID;
			processAdding.totalService = infoAdd.totalServiceTime;
			processAdding.executing = infoAdd.elapsedExecutionTime;

			if(noProcessRunning == true){
				
				queueSRT.add(processAdding);
				processOut = queueSRT.remove();
				processExecution.switchToProcess(processOut.processID); 
				noProcessRunning = false;
				starting[processOut.processID] = System.currentTimeMillis();
			}
			else{

				ProcessInfo infoRun = processExecution.getProcessInfo(processOut.processID);

				if((infoRun.totalServiceTime - infoRun.elapsedExecutionTime) > (infoAdd.totalServiceTime - infoAdd.elapsedExecutionTime)){
					
					ProcessOnQueue processStopped = new ProcessOnQueue();
					processStopped.processID = processOut.processID;
					processStopped.totalService = infoAdd.totalServiceTime;
					processStopped.executing = infoAdd.elapsedExecutionTime;
					queueSRT.add(processStopped);
					processOut = processAdding;
					processExecution.switchToProcess(processAdding.processID);
					
					if(starting[processAdding.processID] == 0){
						starting[processAdding.processID] = System.currentTimeMillis();
					}
				}
				else{
					queueSRT.add(processAdding);
				}
			}
			
			break;	
		case HRRN:

			ProcessInfo infoAdding = processExecution.getProcessInfo(processID);
			ProcessOnQueue processAdd = new ProcessOnQueue();
			processAdd.processID = processID;
			processAdd.totalService = infoAdding.totalServiceTime;
			processAdd.waitingTime = infoAdding.elapsedWaitingTime;

			queueHRRN.add(processAdd);

			if(noProcessRunning == true){
				
				processOut = queueHRRN.remove();
				processExecution.switchToProcess(processOut.processID); 
				noProcessRunning = false;
				starting[processOut.processID] = System.currentTimeMillis();
			}
			
			break;	
		case FB:
			
			ProcessOnQueue firstTime = new ProcessOnQueue();
			firstTime.processID = processID;
			firstTime.lastQueue = 0; 

			allFBQueues[0].add(firstTime);

			if(noProcessRunning == true){
				
				processOut = allFBQueues[0].remove();
				startedProcess = System.currentTimeMillis();
				processExecution.switchToProcess(processOut.processID); 
				noProcessRunning = false;
				
				if(starting[processOut.processID] == 0){
					starting[processOut.processID] = System.currentTimeMillis();
				}
			}
			
			break;
		default:
			break;
		}
	}

	//When processes finish
	public void processFinished(int processID) {

		finished[processID] = System.currentTimeMillis();
		processCount++;

		switch(this.policy) {
		case FCFS:	

			if(!queueFCFS.isEmpty()){
				
				procID = queueFCFS.remove();
				processExecution.switchToProcess(procID);
				starting[procID] = System.currentTimeMillis();
			}
			else{
				noProcessRunning = true;
			}
			
			if(processCount == 15){
				calculateTime ();
			}

			break;
		case RR:
			
			if(!queueRR.isEmpty()){
				
				procID = queueRR.remove();
				startedProcess = System.currentTimeMillis();
				processExecution.switchToProcess(procID);
				
				if(starting[procID] == 0){
					starting[procID] = System.currentTimeMillis();
				}
			}
			else{
				noProcessRunning = true;
			}
			if(processCount == 15){
				calculateTime();
			}	
			
			break;
		case SPN:
			
			if(!queueSPN.isEmpty()){
				
				processOut = queueSPN.remove();
				processExecution.switchToProcess(processOut.processID);
				starting[processOut.processID] = System.currentTimeMillis();
			}
			else{
				noProcessRunning = true;
			}
			if(processCount == 15){
				calculateTime ();
			}
			
			break;
		case SRT:
			
			if(!queueSRT.isEmpty()){
				
				processOut = queueSRT.remove();
				processExecution.switchToProcess(processOut.processID);
				
				if(starting[processOut.processID] == 0){
					starting[processOut.processID] = System.currentTimeMillis();
				}
			}
			else{
				noProcessRunning = true;
			}
			if(processCount == 15){
				calculateTime ();
			}
			
			break;
		case HRRN:
			
			if(!queueHRRN.isEmpty()){
				
				processOut = queueHRRN.remove();
				processExecution.switchToProcess(processOut.processID);
				starting[processOut.processID] = System.currentTimeMillis();
			}
			else{
				noProcessRunning = true;
			}
			if(processCount == 15){
				calculateTime ();
			}
			
			break;
		case FB:
			
			if(!allFBQueues[0].isEmpty()){
				
				processOut = allFBQueues[0].remove();
				processOut.lastQueue = 0;
				startedProcess = System.currentTimeMillis();
				processExecution.switchToProcess(processOut.processID);
				
				if(starting[processOut.processID] == 0){
					starting[processOut.processID] = System.currentTimeMillis();
				}
			}
			else if(!allFBQueues[1].isEmpty()){
				
				processOut = allFBQueues[1].remove();
				processOut.lastQueue = 1;
				startedProcess = System.currentTimeMillis();
				processExecution.switchToProcess(processOut.processID);
			}
			else if(!allFBQueues[2].isEmpty()){
				
				processOut = allFBQueues[2].remove();
				processOut.lastQueue = 2; 
				startedProcess = System.currentTimeMillis();
				processExecution.switchToProcess(processOut.processID);
			}
			else if(!allFBQueues[3].isEmpty()){
				
				processOut = allFBQueues[3].remove();
				processOut.lastQueue = 3;
				startedProcess = System.currentTimeMillis();
				processExecution.switchToProcess(processOut.processID);		
			}
			else if(!allFBQueues[4].isEmpty()){
				
				processOut = allFBQueues[4].remove();
				processOut.lastQueue = 4; 
				startedProcess = System.currentTimeMillis();
				processExecution.switchToProcess(processOut.processID);
			}
			else if(!allFBQueues[5].isEmpty()){
				
				processOut = allFBQueues[5].remove();
				processOut.lastQueue = 5;
				startedProcess = System.currentTimeMillis();
				processExecution.switchToProcess(processOut.processID);
			}
			else if(!allFBQueues[6].isEmpty()){
				
				processOut = allFBQueues[6].remove();
				processOut.lastQueue = 6; 
				startedProcess = System.currentTimeMillis();
				processExecution.switchToProcess(processOut.processID);
			}
			else{
				noProcessRunning = true;
			}
			if(processCount == 15){
				calculateTime ();
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
				
				//Sleep again if another process has been started
				long timeToSleepAgain = (quantum - (System.currentTimeMillis() - startedProcess));
				while((System.currentTimeMillis() - startedProcess) <= quantum){
					try{
						Thread.sleep(timeToSleepAgain);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					timeToSleepAgain = (quantum - (System.currentTimeMillis() - startedProcess));
				}

				queueRR.add(procID);

				if(!queueRR.isEmpty()){
					
					procID = queueRR.remove();
					startedProcess = System.currentTimeMillis();
					processExecution.switchToProcess(procID);
					
					if(starting[procID] == 0){
						starting[procID] = System.currentTimeMillis();
					}
				}
				else{
					noProcessRunning = true;
				}

				if(this.policy != Policy.RR){
					return; 
				}
			}
			
		case FB:
			
			while(true){

				try {
					Thread.sleep(quantum);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				//Sleep again if another process has been started
				long timeToSleepAgain = (quantum - (System.currentTimeMillis() - startedProcess));
				while((System.currentTimeMillis() - startedProcess) <= quantum){
					try{
						Thread.sleep(timeToSleepAgain);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					timeToSleepAgain = (quantum - (System.currentTimeMillis() - startedProcess));
				}

				//Switch to the next queue
				if(processOut.lastQueue < 6){
					allFBQueues[processOut.lastQueue+1].add(processOut);
				}
				else if (processOut.lastQueue == 6){
					allFBQueues[6].add(processOut);
				}

				if(this.policy != Policy.FB){
					return; 
				}

				if(!allFBQueues[0].isEmpty()){

					processOut = allFBQueues[0].remove();
					processOut.lastQueue = 0;
					startedProcess = System.currentTimeMillis();
					processExecution.switchToProcess(processOut.processID);
					
					if(starting[processOut.processID] == 0){
						starting[processOut.processID] = System.currentTimeMillis();
					}

				}
				else if(!allFBQueues[1].isEmpty()){

					processOut = allFBQueues[1].remove();
					processOut.lastQueue = 1;
					startedProcess = System.currentTimeMillis();
					processExecution.switchToProcess(processOut.processID);
				}
				else if(!allFBQueues[2].isEmpty()){

					processOut = allFBQueues[2].remove();
					processOut.lastQueue = 2; 
					startedProcess = System.currentTimeMillis();
					processExecution.switchToProcess(processOut.processID);

				}
				else if(!allFBQueues[3].isEmpty()){

					processOut = allFBQueues[3].remove();
					processOut.lastQueue = 3;
					startedProcess = System.currentTimeMillis();
					processExecution.switchToProcess(processOut.processID);	
				}
				else if(!allFBQueues[4].isEmpty()){

					processOut = allFBQueues[4].remove();
					processOut.lastQueue = 4; 
					startedProcess = System.currentTimeMillis();
					processExecution.switchToProcess(processOut.processID);
				}
				else if(!allFBQueues[5].isEmpty()){

					processOut = allFBQueues[5].remove();
					processOut.lastQueue = 5;
					startedProcess = System.currentTimeMillis();
					processExecution.switchToProcess(processOut.processID);
				}
				else if(!allFBQueues[6].isEmpty()){

					processOut = allFBQueues[6].remove();
					processOut.lastQueue = 6; 
					startedProcess = System.currentTimeMillis();
					processExecution.switchToProcess(processOut.processID);
				}					
				else{
					noProcessRunning = true;
				}
			}
			
		default:
			break;

		}
	}	
}
