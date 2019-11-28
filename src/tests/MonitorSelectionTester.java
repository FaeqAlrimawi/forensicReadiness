package tests;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import core.monitor.Monitor;
import core.monitor.MonitorSelectionSolver;
import core.monitor.MonitorSolution;

public class MonitorSelectionTester {

	protected List<Monitor> monitors = new LinkedList<Monitor>();

	protected void testMonitorSelectionSolver() {

		MonitorSelectionSolver solver = new MonitorSelectionSolver();

		// dummy map...
		// key is action, value is the list of monitors that can monitor that action
		int numOfMonitors = 7;
		int numOfActions = 3;

		Map<String, List<Monitor>> actionsMonitors = createDummyActionMonitorMap(numOfActions, numOfMonitors);

		printMap(actionsMonitors);

		boolean isOptimal = true;
		boolean allDifferent = true;
		boolean isMinimum = true;
		
		int tries = 2;

		for (int i = 0; i < tries; i++) {
			System.out.println("Try [" + i + "]");
			
			List<MonitorSolution> solutions = solver.solve(actionsMonitors, isOptimal, allDifferent, isMinimum);

			if (solutions != null && !solutions.isEmpty()) {
				for (MonitorSolution sol : solutions) {
					sol.print();
				}
			} else {
				System.out.println("No solution found!");
			}

			actionsMonitors = createDummyActionMonitorMap(numOfActions, numOfMonitors);

		}

	}

	protected Map<String, List<Monitor>> createDummyActionMonitorMap(int numOfActions, int numOfMonitors) {

		// dummy map...
		monitors.clear();
		
		// key is action, value is the list of monitors that can monitor that action
		Map<String, List<Monitor>> actionsMonitors = new HashMap<String, List<Monitor>>();

		Random rand = new Random();

		int maxCost = 100;

		// create monitors
		for (int i = 0; i < numOfMonitors; i++) {
			Monitor mon = new Monitor();

			mon.setMonitorID("monitor-" + i);

			// set random cost
			int randCost = rand.nextInt(maxCost);
			mon.setCost(randCost);

			monitors.add(mon);

		}

		// create the actions and their dummy map to monitors
		for (int i = 0; i < numOfActions; i++) {

			String actionName = "action-" + i;

			List<Monitor> mons = new LinkedList<Monitor>();

			actionsMonitors.put(actionName, mons);

			// create monitors to map to
			// the length of the list of monitors is randomly assigned a length between 1
			// and the number of monitors
			int listSize = 1 + rand.nextInt(numOfMonitors);

			// the monitor can be selected randomly from the list
			// for now it is determinstically assigned
			for (int j = 0; j < listSize; j++) {
				mons.add(monitors.get(j));
			}

		}

		return actionsMonitors;
	}

	protected void printMap(Map<String, List<Monitor>> actionsMonitors) {
		
		for (Entry<String, List<Monitor>> entry : actionsMonitors.entrySet()) {

			// action
			System.out.println("Action: " + entry.getKey());
//			System.out.println("\tMonitors: ");
			// monitors
			for (Monitor mon : entry.getValue()) {
				System.out.println("\t" + mon.getMonitorID() + " (" + mon.getCost()+")");
			}

			System.out.println();
		}
	}
	
	public static void main(String[] args) {

		MonitorSelectionTester tester = new MonitorSelectionTester();

		tester.testMonitorSelectionSolver();

	}

}
