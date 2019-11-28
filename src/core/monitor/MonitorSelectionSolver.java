package core.monitor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.collections.Lists;

public class MonitorSelectionSolver {

	// list that contains the sum of cost for each solution
	// index is a solution id and the integer value is the cost sum
	protected List<Integer> allSolutionsCost = new LinkedList<Integer>();

	protected List<int[]> optimalSolution;
	protected int[] optimalSolutionPatternsID;
	protected int[] optimalSolutionMapsID;
	protected int optimalSolutionSeverity;
	protected int[] patternSeverityLevel;

	// input
	Map<String, List<Monitor>> monitors;

	// convert the given map into a map that can be used by the solver
	// key is an integer indicating the position (index) referring to action
	// the value is a list containing monitor indices referring to all monitor that
	// can monitor the given action index
	Map<Integer, List<Integer>> convertedMap = new HashMap<Integer, List<Integer>>();

	// the first index indicates an action and the second is the monitor. The value
	// held is a monitor ID
	int[][] actionMonitorMatrix;

	// key is a monitor and value is its id
	Map<Monitor, Integer> monitorToIDMap = new HashMap<Monitor, Integer>();

	// result containing integer which indicates solution id and list of
	// integers that
	// are the pattern maps
	protected Map<Integer, List<Integer>> allSolutions = new HashMap<Integer, List<Integer>>();

	// all solutions. Same as allSolutions map but converted into MonitorSolution
	// list
	List<MonitorSolution> solutionsFound = new LinkedList<MonitorSolution>();

	// key is an action and the value is its id
	Map<Integer, String> actionToIDMap = new HashMap<Integer, String>();

	// key is monitor id and value is cost
	Map<Integer, Integer> monitorsCosts = new HashMap<Integer, Integer>();

	// if true then it minimises the cost
	static boolean MINIMISE = true;

	// if true then it finds different monitors for different actions
	static boolean ALLDIFFIERENT = true;

	public List<MonitorSolution> solve(Map<String, List<Monitor>> monitors) {

		if (monitors == null || monitors.isEmpty()) {
			return null;
		}

		// reset variables
		reset();

		this.monitors = monitors;

		// ====create ids for actions and monitors
		int actionID = 0;
		int monitorID = 0;

		for (Entry<String, List<Monitor>> entry : monitors.entrySet()) {

			String action = entry.getKey();
			List<Monitor> actionMonitors = entry.getValue();

			List<Integer> actionMonitorIDs = new LinkedList<Integer>();

			actionToIDMap.put(actionID, action);

			convertedMap.put(actionID, actionMonitorIDs);

			actionID++;

			// monitor id and cost
			for (Monitor mon : actionMonitors) {
				// if the monitor has no id, then create one and then add the id to the list of
				// monitor ids for the current action
				if (!monitorToIDMap.containsKey(mon)) {
					monitorToIDMap.put(mon, monitorID);
					actionMonitorIDs.add(monitorID);

					// cost
					monitorsCosts.put(monitorID, (int) mon.getCost());

					monitorID++;
					// if the monitor id already exists, then just add it to the list
				} else {
					actionMonitorIDs.add(monitorToIDMap.get(mon));

					// cost
					monitorsCosts.put(monitorID, (int) mon.getCost());
				}

				// cost

			}

		}

		// get action-monitor matrix
		actionMonitorMatrix = generateActionMonitorMatrix();

		// find solutions
		// key is solution id, value is the id of the monitor
		findSolutions();

		getFoundSolutions();

		return solutionsFound;
	}

	protected void reset() {
		allSolutions.clear();
		solutionsFound.clear();
		convertedMap.clear();
		monitorToIDMap.clear();
		actionToIDMap.clear();

	}

	protected int[][] generateActionMonitorMatrix() {

		int numOfActions = convertedMap.size();
		int[][] possibleMonitorsPerActionMaps = new int[numOfActions][];

		int indexAction = 0;

		for (Entry<Integer, List<Integer>> entry : convertedMap.entrySet()) {
			List<Integer> list = entry.getValue();
			possibleMonitorsPerActionMaps[indexAction] = new int[list.size()];
			for (int indexMonitor = 0; indexMonitor < list.size(); indexMonitor++) {
//			System.out.println("indexAction = " + indexAction + " indexMonitor = " + indexMonitor + " value = " + list.get(indexMonitor));
				possibleMonitorsPerActionMaps[indexAction][indexMonitor] = list.get(indexMonitor);
				// index++;
			}

			indexAction++;
		}

		return possibleMonitorsPerActionMaps;
	}

	/**
	 * returns all solutions found
	 * 
	 * @return A map in which the key is a solution number (or ID) and the value is
	 *         a list of the monitors
	 */
	public List<MonitorSolution> getFoundSolutions() {

		if (!solutionsFound.isEmpty()) {
			return solutionsFound;
		}

		int actionIndex = 0;
//		int solutionIndex = 0;

		List<String> actions = new LinkedList<String>();

		for (Integer actionID : convertedMap.keySet()) {
			actions.add(actionToIDMap.get(actionID));
		}

		for (Entry<Integer, List<Integer>> solution : allSolutions.entrySet()) {

			int solID = solution.getKey();
			List<Integer> monitorIDs = solution.getValue();

			// new solution
			MonitorSolution monSol = new MonitorSolution();

			// reset
			actionIndex = 0;

			// set id
			monSol.setSolutionID(solID);

			// get action name and monitor for the given monitor ID
			for (Integer monID : monitorIDs) {
				// **sequence of monitors indicate sequence of actions

				String actionName = actions.get(actionIndex);
				Monitor mon = getMonitor(monID);

				monSol.addActionMonitor(actionName, mon);

				actionIndex++;
			}

			// set cost
			if (allSolutionsCost.size() > solID) {
				monSol.setCost(allSolutionsCost.get(solID));
			}

			solutionsFound.add(monSol);
		}

		return solutionsFound;
	}

	protected Monitor getMonitor(int monitorID) {

		for (Entry<Monitor, Integer> entry : monitorToIDMap.entrySet()) {
			if (entry.getValue().equals(monitorID)) {
				return entry.getKey();
			}
		}

		return null;
	}

	protected int[] getActionsArray() {

		return actionToIDMap.keySet().stream().mapToInt(i -> i).toArray();

//		List<Integer> actionsArray = new LinkedList<Integer>();
//
//		for (List<int[]> list : patternMaps.values()) {
//			for (int[] ary : list) {
//				for (int action : ary) {
//					if (!actionsArray.contains(action)) {
//						actionsArray.add(action);
//					}
//
//				}
//			}
//		}
//
//		return actionsArray.stream().mapToInt(i -> i).toArray();
	}

	protected Map<Integer, List<Integer>> findSolutions() {

		// numberOfActions could be defined as the max number of the maps

//		this.patternMaps = patternMaps;
//		this.patternSeverityLevel = cost;

//		int[] actionsArray = actionToIDMap.values().stream().mapToInt(i -> i).toArray();

		int numOfAllMaps = 0;

		for (List<Integer> list : convertedMap.values()) {
			numOfAllMaps += list.size();
		}

		int currentNumOfMonitors = numOfAllMaps;
		Model model = null;
		List<Solution> solutions = null;
		Solver solver = null;
		IntVar costSum = null;

		int numOfActions = actionToIDMap.size();
//		int[][] possibleMonitorsPerActionMaps = new int[numOfActions][];

		IntVar[] monitors = null;
		boolean isSolutionfound = false;

		// actual severity array, assuming its embedded in the argument
		// variable
		int minCost = 0;
		int maxCost = 0;
		int sumCost = 0;

		// key is monitor id and value is cost
//		Map<Integer, Integer> costMap = new HashMap<Integer, Integer>();

		// set the max severity level (in this case it is the sum of the pattern
		// severity levels)

//		int ind = 0;

		for (Integer monitorCost : monitorsCosts.values()) {
			sumCost += monitorCost;

			if (monitorCost > maxCost) {
				maxCost = monitorCost;
			}
		}

		maxCost++;

		if (sumCost == 0) {
			sumCost = 1;
		}

		// =============look for
		// solution==========================================
//		while (currentNumOfMonitors > 0) {

		model = new Model("Action-Monitor Model");

		// ============Defining Variables======================//
		monitors = new IntVar[numOfActions];
		IntVar[] monitorCost = new IntVar[numOfActions];
		int[] coeffs = null;
		if (MonitorSelectionSolver.MINIMISE) {
			// used to update severity values
			coeffs = new int[numOfActions];
			Arrays.fill(coeffs, 1); // coeff is 1

			// defines severity for a solution
			costSum = model.intVar("cost_sum", 0, sumCost);
		}
		// each pattern has as domain values the range from {} to
		// {actions in maps}
		int[] monsIDs = monitorToIDMap.values().stream().mapToInt((Integer k) -> k.intValue()).toArray();
//		for (int i = 0; i < monitorToIDMap.size(); i++) {
//			System.out.print(monsIDs[i]+"==");
//		}

		for (int i = 0; i < numOfActions; i++) {
			monitors[i] = model.intVar("monitor-" + i, monsIDs);

			if (MonitorSelectionSolver.MINIMISE) {
				monitorCost[i] = model.intVar("monitor_" + i + "_cost", minCost, maxCost);
			}
		}

//		for(int i=0;i<possibleMonitorsPerActionMaps.length;i++) {
//			for(int j=0;j<possibleMonitorsPerActionMaps[i].length;j++) {
//				System.out.print(possibleMonitorsPerActionMaps[i][j]+"-");
//			}
//			System.out.println();
//		}

		// ============Defining Constraints======================//
		// ===1-No overlapping between maps
		// ===2-A map should be one of the defined maps by the variable
		// possiblePatternMaps
		// ===3-at least 1 map for each pattern

		// 1-no overlapping
		if (ALLDIFFIERENT) {
			model.allDifferent(monitors).post();
		}

		List<Constraint> consList = new LinkedList<Constraint>();
		// essential: at least 1 map for each pattern
		for (int i = 0; i < monitors.length; i++) {
			for (int j = 0; j < actionMonitorMatrix[i].length; j++) {

				// pattern map should be a one of the found maps

				Constraint correctActionMonitor = model.element(monitors[i], actionMonitorMatrix[i], model.intVar(j));

				consList.add(correctActionMonitor);

				// the severity of the pattern should equal to the pattern
				// severity specified in the argument
				if (MonitorSelectionSolver.MINIMISE) {
					model.ifThen(correctActionMonitor,
							model.arithm(monitorCost[i], "=", monitorsCosts.get(actionMonitorMatrix[i][j])));
				}
			}

			Constraint[] res = consList.stream().toArray(size -> new Constraint[size]);
			model.or(res).post();
			consList.clear();
		}

		if (MonitorSelectionSolver.MINIMISE) {
			model.scalar(monitorCost, coeffs, "=", costSum).post();
			model.setObjective(Model.MINIMIZE, costSum);
		}

		// ============Finding solutions======================//
		solver = model.getSolver();
		SetVar uniq;
		solutions = new LinkedList<Solution>();
		List<Integer> vals = new LinkedList<Integer>();

		while (solver.solve()) {

			vals.clear();

			// add the current solution to the solutions list
			solutions.add(new Solution(model).record());

			// get the new solution
//			for (int i = 0; i < numOfActions; i++) {
//				vals.add(monitors[i].getValue());
//			}

			// create a setVar of the new solution
			// uniq = model.setVar(vals.stream().mapToInt(i ->
			// i).toArray());

			// add a constraint that next solution should be different from
			// this
			// model.not(model.union(monitors, uniq)).post();

			// add a constraint that next solution should have equal or more
			// actions
			// could be implemented..?

			isSolutionfound = true;
			// break;
		}

//			if (isSolutionfound) {
//				break;
//			}

		currentNumOfMonitors--;
//		}

		analyseSolutions(solutions, monitors, costSum);

		return this.allSolutions;
	}

	protected Map<Integer, List<Integer>> analyseSolutions(List<Solution> solutions, IntVar[] monitors,
			IntVar severitySum) {

		for (int j = 0; j < solutions.size(); j++) {

			Solution sol = solutions.get(j);

			List<Integer> solVals = new LinkedList<Integer>();

			for (int i = 0; i < monitors.length; i++) {
				solVals.add(sol.getIntVal(monitors[i]));
			}

			// get patterns and maps ids used in this solution
//			getPatternAndMapIDs(solVals);

			// add to solutions
			this.allSolutions.put(j, solVals);

			// add severity
			if (severitySum != null) {
				allSolutionsCost.add(sol.getIntVal(severitySum));
			}

		}

		return this.allSolutions;
	}

//	protected void getPatternAndMapIDs(List<Integer> maps) {
//
//		List<Integer> tmpPatternIDs = new LinkedList<Integer>();
//		List<Integer> tmpMapIDs = new LinkedList<Integer>();
//
//		for (int[] map : maps) {
//			loop_map: for (Entry<Integer, List<Integer>> entry : convertedMap.entrySet()) {
//				for (int j = 0; j < entry.getValue().size(); j++) {
//					if (Arrays.equals(entry.getValue().get(j), map)) {
//						// it could be the case that one map belong to two
//						// patterns
//						// currently select the first pattern matched
//						tmpPatternIDs.add(entry.getKey());
//						tmpMapIDs.add(j);
//						break loop_map;
//					}
//				}
//			}
//		}
//
//		patternIDs.add(tmpPatternIDs.stream().mapToInt(i -> i).toArray());
//		mapIDs.add(tmpMapIDs.stream().mapToInt(i -> i).toArray());
//	}

}
