package com.ru.usty.scheduling.visualization;

import java.util.ArrayList;
import java.util.Collection;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.ru.usty.scheduling.process.ProcessHandler;
import com.ru.usty.scheduling.Policy;
import com.ru.usty.scheduling.process.Process;

public class TestSuite {

	private static ProcessHandler processHandler;
	private static ArrayList<ProcessDescription> testProcesses;
	private static int currentTestProcessIndex;
	private static long schedulingStartTime;
	private static boolean schedulingStarted;
	private static Policy nextPolicy;
	private static int nextQuantum;

	public static void runVisualization(Policy startingPolicy) {

		processHandler = new ProcessHandler();
		initTestProcesses();
		schedulingStartTime = System.currentTimeMillis();
		schedulingStarted = false;
		nextPolicy = startingPolicy;
		nextQuantum = 500;


		Thread graphicsThread = new Thread(new Runnable() {

			@Override
			public void run() {
				new LwjglApplication(new SchedulingGraphics(), "The Process Scheduling Machine", 480, 560, false);
			}
		});
		graphicsThread.start();

		try {
			graphicsThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void initTestProcesses() {
		testProcesses = new ArrayList<ProcessDescription>();

		testProcesses.add(new ProcessDescription(0, 1500));
		testProcesses.add(new ProcessDescription(1000, 3000));
		testProcesses.add(new ProcessDescription(2000, 2000));
		testProcesses.add(new ProcessDescription(3000, 2500));
		testProcesses.add(new ProcessDescription(4000, 1000));
		testProcesses.add(new ProcessDescription(10500, 1000));
		testProcesses.add(new ProcessDescription(10850, 4000));
		testProcesses.add(new ProcessDescription(11500, 3000));
		testProcesses.add(new ProcessDescription(11550, 5000));
		testProcesses.add(new ProcessDescription(12400, 2000));
		testProcesses.add(new ProcessDescription(14000, 1000));
		testProcesses.add(new ProcessDescription(17500, 1500));
		testProcesses.add(new ProcessDescription(18000, 500));
		testProcesses.add(new ProcessDescription(18000, 1500));
		testProcesses.add(new ProcessDescription(18500, 500));
	}

	private static void startScheduling(Policy policy, int quantum) {

		currentTestProcessIndex = 0;
		schedulingStartTime = System.currentTimeMillis();
		schedulingStarted = true;
		processHandler.startSceduling(policy, quantum);
	}

	private static void stopScheduling() {
		schedulingStartTime = System.currentTimeMillis();
		schedulingStarted = false;
	}

	public static Collection<Process> getProcesses() {
		return processHandler.getProcesses();
	}

	public static boolean update() {

		if(schedulingStarted) {
			long currentTime = System.currentTimeMillis() - schedulingStartTime;
			while(currentTestProcessIndex < testProcesses.size()
			   && testProcesses.get(currentTestProcessIndex).arrivalTime < currentTime) {
				processHandler.addProcess(currentTestProcessIndex,
						testProcesses.get(currentTestProcessIndex).serviceTime);
				currentTestProcessIndex++;
			}

			processHandler.update();

			if(currentTestProcessIndex >= testProcesses.size()
			&& processHandler.getProcesses().size() == 0) {
				stopScheduling();
			}
		}
		else {
			if(System.currentTimeMillis() - schedulingStartTime > 2000) {

				if(nextQuantum == 50) {
					return false;
				}

				startScheduling(nextPolicy, nextQuantum);
				switch(nextPolicy) {
				case FCFS:
					nextPolicy = Policy.RR;
					break;
				case RR:
					if(nextQuantum == 500) {
						nextPolicy = Policy.RR;
						nextQuantum = 2000;
					}
					else {
						nextPolicy = Policy.SPN;
						nextQuantum = 500;
					}
					break;
				case SPN:
					nextPolicy = Policy.SRT;
					break;
				case SRT:
					nextPolicy = Policy.HRRN;
					break;
				case HRRN:
					nextPolicy = Policy.FB;
					break;
				case FB:
					nextPolicy = Policy.RR;
					nextQuantum = 50;
					break;
				}
			}
		}
		return true;
	}
}
