package com.ru.usty.scheduling;

import com.ru.usty.scheduling.visualization.TestSuite;

public class SchedulingMainProgram {
	public static void main(String[] args) {

		//Visualization starts at stated policy and then continues to the
		//end in order FCFS, RR(500), RR(2000), SPN, SRT, HRRN, FB
		TestSuite.runVisualization(Policy.SPN);

	}
}
