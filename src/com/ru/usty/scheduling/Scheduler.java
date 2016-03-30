package com.ru.usty.scheduling;

import com.ru.usty.scheduling.process.ProcessExecution;
import com.ru.usty.scheduling.process.ProcessInfo;

import java.util.*;

public class Scheduler implements Runnable  {

	ProcessExecution processExecution;
	Policy policy;
	int quantum;
	int procID; 
	//FBQueue fbqueue;
	Queue<ProcessOnQueue> allFBQueues[];
	long totalResponseTime = 0;
	long totalTurnaroundTime = 0;
	long averageResponseTime = 0;
	long averageTurnaroundTime = 0;
	ProcessOnQueue processOut;
	long startedProcess; 
	boolean noProcessRunning = true;
	long[] arriving = new long[15];
	long[] finished = new long[15];
	long[] starting = new long[15];
	int processCount = 0;

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
		
		noProcessRunning = true;
				
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
			allFBQueues = new Queue[7];
			for(int i = 0; i < 7; i++){
				allFBQueues[i] = new LinkedList<ProcessOnQueue>();
				//System.out.println(i);
			}
			Thread threadFB = new Thread(this);
			threadFB.start();

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


		arriving[processID] = System.currentTimeMillis();

		switch(this.policy) {
		case FCFS:	
			q.add(processID);
			if(noProcessRunning == true){
				procID = q.remove();
				//Er þetta ekki óþarfi í FCFS? Við þurfum ekki að halda utan um þennan tíma, er þetta ekki bara notað í tímamælingunum?
				//	startedProcess = System.currentTimeMillis();
				processExecution.switchToProcess(procID); 
				starting[procID] = System.currentTimeMillis();
				noProcessRunning = false;
				//Held að þetta sé óþarfi í FCFS þar sem allir processar eiga að klára strax og fari því aldrei aftur á röðina
				/*if(starting[processID] == 0){
					starting[processID] = System.currentTimeMillis();
				}*/
			}
			break;
		case RR:
			q.add(processID);
			if(noProcessRunning == true){
				procID = q.remove();
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
				queue.add(process);
				processOut = queue.remove();
				processExecution.switchToProcess(processOut.processID); 
				noProcessRunning = false;
				starting[processOut.processID] = System.currentTimeMillis();
			}
			else{
				queue.add(process);
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
				//System.out.println("Running: " + (infoRun.totalServiceTime - infoRun.elapsedExecutionTime));
				//System.out.println("Adding: " + (infoAdd.totalServiceTime - infoAdd.elapsedExecutionTime));

				if((infoRun.totalServiceTime - infoRun.elapsedExecutionTime) > (infoAdd.totalServiceTime - infoAdd.elapsedExecutionTime)){
					//System.out.println("Swissa processum");
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
				starting[processOut.processID] = System.currentTimeMillis();
				noProcessRunning = false;
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

	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */ 
	public void processFinished(int processID) {

		finished[processID] = System.currentTimeMillis();
		processCount++;

		switch(this.policy) {
		case FCFS:	

			if(!q.isEmpty()){
				procID = q.remove();
				//Er þetta ekki óþarfi í FCFS? Við þurfum ekki að halda utan um þennan tíma, er þetta ekki bara notað í tímamælingunum?
				//startedProcess = System.currentTimeMillis();
				processExecution.switchToProcess(procID);
				starting[procID] = System.currentTimeMillis();
				//Held að þetta sé óþarfi í FCFS þar sem allir processar eiga að klára strax og fari því aldrei aftur á röðina
				/*if(starting[procID] == 0){
					starting[procID] = System.currentTimeMillis();
				}*/
			}
			else{
				noProcessRunning = true;
			}
			if(processCount == 15){
				for(int i = 0; i<processCount ; i++){
					totalResponseTime += (starting[i] - arriving[i]);
					totalTurnaroundTime += (finished[i] - arriving[i]);
				}
				averageResponseTime = (totalResponseTime/15);
				averageTurnaroundTime = (totalTurnaroundTime/15);
				System.out.println("Average Response Time: " + averageResponseTime);
				System.out.println("Average Turnaround Time: " + averageTurnaroundTime);
			}

			break;
		case RR:
			//System.out.println("Process finished: " + procID);

			if(!q.isEmpty()){
				procID = q.remove();
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
				for(int i = 0; i< processCount; i++){
					totalResponseTime += (starting[i] - arriving[i]);
					totalTurnaroundTime += (finished[i] - arriving[i]);
				}
				averageResponseTime = (totalResponseTime/15);
				averageTurnaroundTime = (totalTurnaroundTime/15);
				System.out.println("Average Response Time: " + averageResponseTime);
				System.out.println("Average Turnaround Time: " + averageTurnaroundTime);
			}
			break;
		case SPN:
			if(!queue.isEmpty()){
				processOut = queue.remove();
				processExecution.switchToProcess(processOut.processID);
				starting[processOut.processID] = System.currentTimeMillis();
			}
			else{
				noProcessRunning = true;
			}
			if(processCount == 15){
				for(int i = 0; i< processCount; i++){
					totalResponseTime += (starting[i] - arriving[i]);
					totalTurnaroundTime += (finished[i] - arriving[i]);
				}
				averageResponseTime = (totalResponseTime/15);
				averageTurnaroundTime = (totalTurnaroundTime/15);
				System.out.println("Average Response Time: " + averageResponseTime);
				System.out.println("Average Turnaround Time: " + averageTurnaroundTime);
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
				for(int i = 0; i< processCount; i++){
					totalResponseTime += (starting[i] - arriving[i]);
					totalTurnaroundTime += (finished[i] - arriving[i]);
				}
				averageResponseTime = (totalResponseTime/15);
				averageTurnaroundTime = (totalTurnaroundTime/15);
				System.out.println("Average Response Time: " + averageResponseTime);
				System.out.println("Average Turnaround Time: " + averageTurnaroundTime);
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
				for(int i = 0; i< processCount; i++){
					totalResponseTime += (starting[i] - arriving[i]);
					totalTurnaroundTime += (finished[i] - arriving[i]);
				}
				averageResponseTime = (totalResponseTime/15);
				averageTurnaroundTime = (totalTurnaroundTime/15);
				System.out.println("Average Response Time: " + averageResponseTime);
				System.out.println("Average Turnaround Time: " + averageTurnaroundTime);
			}
			break;
		case FB:
			//System.out.println("Process finishing: " + processID);
			if(!allFBQueues[0].isEmpty()){

				//System.out.println("remove" + processOut.processID);
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
				for(int i = 0; i< processCount; i++){
					totalResponseTime += (starting[i] - arriving[i]);
					totalTurnaroundTime += (finished[i] - arriving[i]);
				}
				averageResponseTime = (totalResponseTime/15);
				averageTurnaroundTime = (totalTurnaroundTime/15);
				System.out.println("Average Response Time: " + averageResponseTime);
				System.out.println("Average Turnaround Time: " + averageTurnaroundTime);
			}
			break;	
		default:
			break;
		}

	}

	@Override
	public void run() {

		//ProcessInfo i = processExecution.getProcessInfo(procID);
		//System.out.println("Intertupr:" + procID);
		//System.out.println("Need: " + i.totalServiceTime);
		//System.out.println("finished: " + i.elapsedExecutionTime);

		switch(this.policy) {
		case RR:
			while(true){
				long lastProcessStarted = 0;
				try {
					lastProcessStarted = startedProcess;
					//System.out.println("Thread sleeps at: " + System.currentTimeMillis());
					Thread.sleep(quantum);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//System.out.println("Thread wakes up at: " + System.currentTimeMillis());

				/*Það var alltaf verið að fara inní þetta while, sama þó að enginn process hafi verið að klára 
				 * í sleepinu. Kári talaði einhvers staðar um að halda utan um tímann á síðasta process sem startaði 
				 * og gera eitthvað ef sá tími væri ekki sá sami þegar þráðurinn vaknaði. 
				 * Þess vegna setti ég þessa IF setningu og þetta virðist virka. 
				 * Processarnir eru reyndar ekki að klára í réttri röð frekar en fyrri daginn :(
				 */
				//sofa aftur ef einhver process hefur verið startað aftur
				if((lastProcessStarted != startedProcess) && (System.currentTimeMillis() - startedProcess <= quantum) && (System.currentTimeMillis() - startedProcess != 0)){
					//while(System.currentTimeMillis() - startedProcess <= quantum){

					//System.out.println("How often does this happen?");
					try{
						Thread.sleep(System.currentTimeMillis() - startedProcess);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				//}
				q.add(procID); //fullt með mismuandi q

				if(!q.isEmpty()){ //fullt af línum að starta úr réttri q
					procID = q.remove();
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
				long lastProcessStarted = 0;
				//System.out.println("While true");
				try {
					lastProcessStarted = startedProcess;
					Thread.sleep(quantum);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				//sofa aftur ef einhver process hefur verið startað aftur
				if((lastProcessStarted != startedProcess) && (System.currentTimeMillis() - startedProcess <= quantum) && (System.currentTimeMillis() - startedProcess != 0)){
				//while(System.currentTimeMillis() - startedProcess <= quantum){

					try{
						Thread.sleep(System.currentTimeMillis() - startedProcess);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				//This code for array queue instead of the one commented below?
				if(processOut.lastQueue < 6){
					allFBQueues[processOut.lastQueue+1].add(processOut);
				}
				else if (processOut.lastQueue == 6){
					allFBQueues[6].add(processOut);
				}

				/*if(processOut.lastQueue == 0){
						System.out.println("Add to queue 0");
						allFBQueues[1].add(processOut);
					}
					else if(processOut.lastQueue == 1){
						System.out.println("Queue 1 - Process nr:" + processOut.processID);
						allFBQueues[2].add(processOut);
					}
					else if(processOut.lastQueue == 2){
						System.out.println("Queue 2 - Process nr:" + processOut.processID);
						allFBQueues[3].add(processOut);
					}
					else if(processOut.lastQueue == 3){
						System.out.println("Queue 3 - Process nr:" + processOut.processID);
						allFBQueues[4].add(processOut);
					}
					else if(processOut.lastQueue == 4){
						allFBQueues[5].add(processOut);
					}
					else if(processOut.lastQueue == 5){
						allFBQueues[6].add(processOut);
					}
					else if(processOut.lastQueue == 6){
						allFBQueues[6].add(processOut);
					}*/

				if(this.policy != Policy.FB){
					return; 
				}

				if(!allFBQueues[0].isEmpty()){

					//System.out.println("remove" + processOut.processID);
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
