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

public class MonitorSelectionSolver {

	// result containing integer which indicates solution id and list of
	// integers that
	// are the pattern maps
	protected Map<Integer, List<int[]>> allSolutions = new HashMap<Integer, List<int[]>>();

	// list that contains the patterns mapped in each solution
	// list index is a solution id and the integer array are the patterns
	protected List<int[]> patternIDs;
	List<int[]> mapIDs;

	// list that contains the sum severity for each solution
	// index is a solution id and the integer value is the severity sum
	protected List<Integer> allSolutionsSeverity;

	protected List<int[]> optimalSolution;
	protected int[] optimalSolutionPatternsID;
	protected int[] optimalSolutionMapsID;
	protected int optimalSolutionSeverity;
	protected int[] patternSeverityLevel;

	// given
//	protected Map<Integer, List<int[]>> patternMaps;

	// convert the given map into a map that can be used by the solver
	// key is an integer indicating the position (index) referring to action
	// the value is a list containing monitor indices referring to all monitor that
	// can monitor the given action index
	Map<Integer, List<Integer>> convertedMap = new HashMap<Integer, List<Integer>>();

	// key is a monitor and value is its id
	Map<Monitor, Integer> monitorToIDMap = new HashMap<Monitor, Integer>();

	// key is an action and the value is its id
	Map<String, Integer> actionToIDMap = new HashMap<String, Integer>();

	static boolean MAXIMISE = false;

	public List<Monitor> solve(Map<String, List<Monitor>> monitors) {

		if (monitors == null || monitors.isEmpty()) {
			return null;
		}

		convertedMap.clear();
		monitorToIDMap.clear();
		actionToIDMap.clear();

		// ====create ids for actions and monitors
		int actionID = 0;
		int monitorID = 0;

		for (Entry<String, List<Monitor>> entry : monitors.entrySet()) {
			String action = entry.getKey();
			List<Monitor> actionMonitors = entry.getValue();

			List<Integer> actionMonitorIDs = new LinkedList<Integer>();

			actionToIDMap.put(action, actionID);

			convertedMap.put(actionID, actionMonitorIDs);

			actionID++;

			for (Monitor mon : actionMonitors) {
				// if the monitor has no id, then create one and then add the id to the list of
				// monitor ids for the current action
				if (!monitorToIDMap.containsKey(mon)) {
					monitorToIDMap.put(mon, monitorID);
					actionMonitorIDs.add(monitorID);
					monitorID++;
					// if the monitor id already exists, then just add it to the list
				} else {
					actionMonitorIDs.add(monitorID);
				}
			}

		}

		// ==dummy cost
		int size = monitors.size();
		int[] cost = new int[size];
		Random rand = new Random();

		for (int i = 0; i < cost.length; i++) {
			cost[i] = rand.nextInt(100);
		}

		// find solutions
		findSolutions(cost);

		List<Monitor> solution = new LinkedList<Monitor>();

//		CPSolver solver = new CPSolver();

		return solution;
	}

	protected int[] getActionsArray() {

		return actionToIDMap.values().stream().mapToInt(i -> i).toArray();

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

	public Map<Integer, List<int[]>> findSolutions(int[] cost) {

		// numberOfActions could be defined as the max number of the maps

//		this.patternMaps = patternMaps;
		this.patternSeverityLevel = cost;

		int[] actionsArray;
//		actionsArray = getActionsArray();
		actionsArray = actionToIDMap.values().stream().mapToInt(i -> i).toArray();

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
		SetVar[][] possibleActionsMaps = new SetVar[numOfActions][];

		SetVar[] monitors = null;
		boolean isSolutionfound = false;

		// actual severity array, assuming its embedded in the argument
		// variable
		int minCost = 0;
		int maxCost = 0;
		int sumCost = 0;

		// set the max severity level (in this case it is the sum of the pattern
		// severity levels)

		int ind = 0;

		for (Integer action : convertedMap.keySet()) {
			sumCost += convertedMap.get(action).size() * cost[ind];

			if (cost[ind] > maxCost) {
				maxCost = cost[ind];
			}

			ind++;
		}

		maxCost++;

		if (sumCost == 0) {
			sumCost = 1;
		}

		// =============look for
		// solution==========================================
		while (currentNumOfMonitors > 0) {

			model = new Model("Action-Monitor Model");

			// ============Defining Variables======================//
			monitors = new SetVar[currentNumOfMonitors];
			IntVar[] monitorCost = new IntVar[currentNumOfMonitors];
			int[] coeffs = null;
			if (MonitorSelectionSolver.MAXIMISE) {
				// used to update severity values
				coeffs = new int[currentNumOfMonitors];
				Arrays.fill(coeffs, 1); // coeff is 1

				// defines severity for a solution
				costSum = model.intVar("cost_sum", 0, sumCost);
			}
			// each pattern has as domain values the range from {} to
			// {actions in maps}
			for (int i = 0; i < currentNumOfMonitors; i++) {
				monitors[i] = model.setVar("monitor-" + i, new int[] {}, actionsArray);

				if (MonitorSelectionSolver.MAXIMISE) {
					monitorCost[i] = model.intVar("monitor_" + i + "_cost", minCost, maxCost);
				}
			}

			int indexAction = 0;

			for (Entry<Integer, List<Integer>> entry : convertedMap.entrySet()) {
				List<Integer> list = entry.getValue();
				possibleActionsMaps[indexAction] = new SetVar[list.size()];
				for (int indexMonitor = 0; indexMonitor < list.size(); indexMonitor++) {
					possibleActionsMaps[indexAction][indexMonitor] = model
							.setVar("map" + indexAction + "-" + indexMonitor, list.get(indexMonitor));
					// index++;
				}
				indexAction++;
			}

			// ============Defining Constraints======================//
			// ===1-No overlapping between maps
			// ===2-A map should be one of the defined maps by the variable
			// possiblePatternMaps
			// ===3-at least 1 map for each pattern

			// 1-no overlapping
			model.allDisjoint(monitors).post();

			List<Constraint> consList = new LinkedList<Constraint>();
			// essential: at least 1 map for each pattern
			for (int i = 0; i < monitors.length; i++) {
				for (int j = 0; j < possibleActionsMaps.length; j++) {

					// pattern map should be a one of the found maps
					Constraint patternMember = model.member(possibleActionsMaps[j], monitors[i]);
					consList.add(patternMember);

					// the severity of the pattern should equal to the pattern
					// severity specified in the argument
					if (MonitorSelectionSolver.MAXIMISE) {
						model.ifThen(patternMember, model.arithm(monitorCost[i], "=", cost[j]));
					}
				}

				Constraint[] res = consList.stream().toArray(size -> new Constraint[size]);
				model.or(res).post();
				consList.clear();
			}

			if (MonitorSelectionSolver.MAXIMISE) {
				model.scalar(monitorCost, coeffs, "=", costSum).post();
				model.setObjective(Model.MAXIMIZE, costSum);
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
				for (int i = 0; i < currentNumOfMonitors; i++) {
					vals.addAll(Arrays.stream(monitors[i].getValue().toArray()).boxed().collect(Collectors.toList()));
				}

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

			if (isSolutionfound) {
				break;
			}

			currentNumOfMonitors--;
		}

		analyseSolutions(solutions, monitors, costSum);

		return this.allSolutions;
	}

//	public Map<Integer, List<int[]>> findSolutions(Map<Integer, List<int[]>> patternMaps, int[] patternSeverityLevels) {
//
//		// numberOfActions could be defined as the max number of the maps
//
//		this.patternMaps = patternMaps;
//		this.patternSeverityLevel = patternSeverityLevels;
//
//		int[] actionsArray;
//		actionsArray = getActionsArray(patternMaps);
//
//		int numOfAllMaps = 0;
//
//		for (List<int[]> list : patternMaps.values()) {
//			numOfAllMaps += list.size();
//		}
//
//		int currentNumOfPatterns = numOfAllMaps;
//		Model model = null;
//		List<Solution> solutions = null;
//		Solver solver = null;
//		IntVar severitySum = null;
//
//		int numOfPatterns = patternMaps.keySet().size();
//		SetVar[][] possiblePatternsMaps = new SetVar[numOfPatterns][];
//
//		SetVar[] patterns = null;
//		boolean isSolutionfound = false;
//
//		// actual severity array, assuming its embedded in the argument
//		// variable
//		int minSeverity = 0;
//		int maxSeverity = 0;
//		int sumSeverity = 0;
//
//		// set the max severity level (in this case it is the sum of the pattern
//		// severity levels)
//
//		int ind = 0;
//
//		for (Integer pattern : patternMaps.keySet()) {
//			sumSeverity += patternMaps.get(pattern).size() * patternSeverityLevels[ind];
//
//			if (patternSeverityLevels[ind] > maxSeverity) {
//				maxSeverity = patternSeverityLevels[ind];
//			}
//
//			ind++;
//		}
//
//		maxSeverity++;
//
//		if (sumSeverity == 0) {
//			sumSeverity = 1;
//		}
//
//		// =============look for
//		// solution==========================================
//		while (currentNumOfPatterns > 0) {
//
//			model = new Model("Pattern-Map Model");
//
//			// ============Defining Variables======================//
//			patterns = new SetVar[currentNumOfPatterns];
//			IntVar[] patternseverity = new IntVar[currentNumOfPatterns];
//			int[] coeffs = null;
//			if (MonitorSelectionSolver.MAXIMISE) {
//				// used to update severity values
//				coeffs = new int[currentNumOfPatterns];
//				Arrays.fill(coeffs, 1); // coeff is 1
//
//				// defines severity for a solution
//				severitySum = model.intVar("severity_sum", 0, sumSeverity);
//			}
//			// each pattern has as domain values the range from {} to
//			// {actions in maps}
//			for (int i = 0; i < currentNumOfPatterns; i++) {
//				patterns[i] = model.setVar("pattern-" + i, new int[] {}, actionsArray);
//
//				if (MonitorSelectionSolver.MAXIMISE) {
//					patternseverity[i] = model.intVar("pattern_" + i + "_severity", minSeverity, maxSeverity);
//				}
//			}
//
//			int indexPattern = 0;
//
//			for (Entry<Integer, List<int[]>> entry : patternMaps.entrySet()) {
//				List<int[]> list = entry.getValue();
//				possiblePatternsMaps[indexPattern] = new SetVar[list.size()];
//				for (int j = 0; j < list.size(); j++) {
//					possiblePatternsMaps[indexPattern][j] = model.setVar("map" + indexPattern + "-" + j, list.get(j));
//					// index++;
//				}
//				indexPattern++;
//			}
//
//			// ============Defining Constraints======================//
//			// ===1-No overlapping between maps
//			// ===2-A map should be one of the defined maps by the variable
//			// possiblePatternMaps
//			// ===3-at least 1 map for each pattern
//
//			// 1-no overlapping
//			model.allDisjoint(patterns).post();
//
//			List<Constraint> consList = new LinkedList<Constraint>();
//			// essential: at least 1 map for each pattern
//			for (int i = 0; i < patterns.length; i++) {
//				for (int j = 0; j < possiblePatternsMaps.length; j++) {
//
//					// pattern map should be a one of the found maps
//					Constraint patternMember = model.member(possiblePatternsMaps[j], patterns[i]);
//					consList.add(patternMember);
//
//					// the severity of the pattern should equal to the pattern
//					// severity specified in the argument
//					if (MonitorSelectionSolver.MAXIMISE) {
//						model.ifThen(patternMember, model.arithm(patternseverity[i], "=", patternSeverityLevels[j]));
//					}
//				}
//
//				Constraint[] res = consList.stream().toArray(size -> new Constraint[size]);
//				model.or(res).post();
//				consList.clear();
//			}
//
//			if (MonitorSelectionSolver.MAXIMISE) {
//				model.scalar(patternseverity, coeffs, "=", severitySum).post();
//				model.setObjective(Model.MAXIMIZE, severitySum);
//			}
//
//			// ============Finding solutions======================//
//			solver = model.getSolver();
//			SetVar uniq;
//			solutions = new LinkedList<Solution>();
//			List<Integer> vals = new LinkedList<Integer>();
//
//			while (solver.solve()) {
//
//				vals.clear();
//
//				// add the current solution to the solutions list
//				solutions.add(new Solution(model).record());
//
//				// get the new solution
//				for (int i = 0; i < currentNumOfPatterns; i++) {
//					vals.addAll(Arrays.stream(patterns[i].getValue().toArray()).boxed().collect(Collectors.toList()));
//				}
//
//				// create a setVar of the new solution
//				// uniq = model.setVar(vals.stream().mapToInt(i ->
//				// i).toArray());
//
//				// add a constraint that next solution should be different from
//				// this
//				// model.not(model.union(patterns, uniq)).post();
//
//				// add a constraint that next solution should have equal or more
//				// actions
//				// could be implemented..?
//
//				isSolutionfound = true;
//				// break;
//			}
//
//			if (isSolutionfound) {
//				break;
//			}
//
//			currentNumOfPatterns--;
//		}
//
//		analyseSolutions(solutions, patterns, severitySum);
//
//		return this.allSolutions;
//	}

	protected Map<Integer, List<int[]>> analyseSolutions(List<Solution> solutions, SetVar[] monitors,
			IntVar severitySum) {

		for (int j = 0; j < solutions.size(); j++) {

			Solution sol = solutions.get(j);

			List<int[]> solVals = new LinkedList<int[]>();

			for (int i = 0; i < monitors.length; i++) {
				solVals.add(sol.getSetVal(monitors[i]));
			}

			// get patterns and maps ids used in this solution
//			getPatternAndMapIDs(solVals);

			// add to solutions
			this.allSolutions.put(j, solVals);

			// add severity
			if (severitySum != null) {
				allSolutionsSeverity.add(sol.getIntVal(severitySum));
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
