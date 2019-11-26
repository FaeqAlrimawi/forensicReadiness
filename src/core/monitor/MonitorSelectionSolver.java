package core.monitor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
	protected Map<Integer, List<int[]>> allSolutions;

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
	protected Map<Integer, List<int[]>> patternMaps;

	static boolean MAXIMISE = false;

	
	public List<Monitor> solve(Map<String, List<Monitor>> monitors) {
		
		if(monitors == null || monitors.isEmpty()) {
			return null;
		}
		
		List<Monitor> solution = new LinkedList<Monitor>();
		
//		CPSolver solver = new CPSolver();
		
		return solution;
	}
	
	protected int[] getActionsArray(Map<Integer, List<int[]>> patternMaps) {

		List<Integer> actionsArray = new LinkedList<Integer>();

		for (List<int[]> list : patternMaps.values()) {
			for (int[] ary : list) {
				for (int action : ary) {
					if (!actionsArray.contains(action)) {
						actionsArray.add(action);
					}

				}
			}
		}

		return actionsArray.stream().mapToInt(i -> i).toArray();
	}
	
	public Map<Integer, List<int[]>> findSolutions(Map<Integer, List<int[]>> patternMaps, int[] patternSeverityLevels) {

		// numberOfActions could be defined as the max number of the maps

		this.patternMaps = patternMaps;
		this.patternSeverityLevel = patternSeverityLevels;

		int[] actionsArray;
		actionsArray = getActionsArray(patternMaps);

		int numOfAllMaps = 0;

		for (List<int[]> list : patternMaps.values()) {
			numOfAllMaps += list.size();
		}

		int currentNumOfPatterns = numOfAllMaps;
		Model model = null;
		List<Solution> solutions = null;
		Solver solver = null;
		IntVar severitySum = null;

		int numOfPatterns = patternMaps.keySet().size();
		SetVar[][] possiblePatternsMaps = new SetVar[numOfPatterns][];

		SetVar[] patterns = null;
		boolean isSolutionfound = false;

		// actual severity array, assuming its embedded in the argument
		// variable
		int minSeverity = 0;
		int maxSeverity = 0;
		int sumSeverity = 0;

		// set the max severity level (in this case it is the sum of the pattern
		// severity levels)

		int ind = 0;

		for (Integer pattern : patternMaps.keySet()) {
			sumSeverity += patternMaps.get(pattern).size() * patternSeverityLevels[ind];

			if (patternSeverityLevels[ind] > maxSeverity) {
				maxSeverity = patternSeverityLevels[ind];
			}

			ind++;
		}

		maxSeverity++;

		if (sumSeverity == 0) {
			sumSeverity = 1;
		}

		// =============look for
		// solution==========================================
		while (currentNumOfPatterns > 0) {

			model = new Model("Pattern-Map Model");

			// ============Defining Variables======================//
			patterns = new SetVar[currentNumOfPatterns];
			IntVar[] patternseverity = new IntVar[currentNumOfPatterns];
			int[] coeffs = null;
			if (MonitorSelectionSolver.MAXIMISE) {
				// used to update severity values
				coeffs = new int[currentNumOfPatterns];
				Arrays.fill(coeffs, 1); // coeff is 1

				// defines severity for a solution
				severitySum = model.intVar("severity_sum", 0, sumSeverity);
			}
			// each pattern has as domain values the range from {} to
			// {actions in maps}
			for (int i = 0; i < currentNumOfPatterns; i++) {
				patterns[i] = model.setVar("pattern-" + i, new int[] {}, actionsArray);

				if (MonitorSelectionSolver.MAXIMISE) {
					patternseverity[i] = model.intVar("pattern_" + i + "_severity", minSeverity, maxSeverity);
				}
			}

			int indexPattern = 0;

			for (Entry<Integer, List<int[]>> entry : patternMaps.entrySet()) {
				List<int[]> list = entry.getValue();
				possiblePatternsMaps[indexPattern] = new SetVar[list.size()];
				for (int j = 0; j < list.size(); j++) {
					possiblePatternsMaps[indexPattern][j] = model.setVar("map" + indexPattern + "-" + j, list.get(j));
					// index++;
				}
				indexPattern++;
			}

			// ============Defining Constraints======================//
			// ===1-No overlapping between maps
			// ===2-A map should be one of the defined maps by the variable
			// possiblePatternMaps
			// ===3-at least 1 map for each pattern

			// 1-no overlapping
			model.allDisjoint(patterns).post();

			List<Constraint> consList = new LinkedList<Constraint>();
			// essential: at least 1 map for each pattern
			for (int i = 0; i < patterns.length; i++) {
				for (int j = 0; j < possiblePatternsMaps.length; j++) {

					// pattern map should be a one of the found maps
					Constraint patternMember = model.member(possiblePatternsMaps[j], patterns[i]);
					consList.add(patternMember);

					// the severity of the pattern should equal to the pattern
					// severity specified in the argument
					if (MonitorSelectionSolver.MAXIMISE) {
						model.ifThen(patternMember, model.arithm(patternseverity[i], "=", patternSeverityLevels[j]));
					}
				}

				Constraint[] res = consList.stream().toArray(size -> new Constraint[size]);
				model.or(res).post();
				consList.clear();
			}

			if (MonitorSelectionSolver.MAXIMISE) {
				model.scalar(patternseverity, coeffs, "=", severitySum).post();
				model.setObjective(Model.MAXIMIZE, severitySum);
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
				for (int i = 0; i < currentNumOfPatterns; i++) {
					vals.addAll(Arrays.stream(patterns[i].getValue().toArray()).boxed().collect(Collectors.toList()));
				}

				// create a setVar of the new solution
				// uniq = model.setVar(vals.stream().mapToInt(i ->
				// i).toArray());

				// add a constraint that next solution should be different from
				// this
				// model.not(model.union(patterns, uniq)).post();

				// add a constraint that next solution should have equal or more
				// actions
				// could be implemented..?

				isSolutionfound = true;
				// break;
			}

			if (isSolutionfound) {
				break;
			}

			currentNumOfPatterns--;
		}

		analyseSolutions(solutions, patterns, severitySum);

		return this.allSolutions;
	}
	
	protected Map<Integer, List<int[]>> analyseSolutions(List<Solution> solutions, SetVar[] patterns,
			IntVar severitySum) {

		for (int j = 0; j < solutions.size(); j++) {

			Solution sol = solutions.get(j);

			List<int[]> solVals = new LinkedList<int[]>();

			for (int i = 0; i < patterns.length; i++) {
				solVals.add(sol.getSetVal(patterns[i]));
			}

			// get patterns and maps ids used in this solution
			getPatternAndMapIDs(solVals);

			// add to solutions
			this.allSolutions.put(j, solVals);

			// add severity
			if (severitySum != null) {
				allSolutionsSeverity.add(sol.getIntVal(severitySum));
			}

		}

		return this.allSolutions;
	}
	
	protected void getPatternAndMapIDs(List<int[]> maps) {

		List<Integer> tmpPatternIDs = new LinkedList<Integer>();
		List<Integer> tmpMapIDs = new LinkedList<Integer>();

		for (int[] map : maps) {
			loop_map: for (Entry<Integer, List<int[]>> entry : patternMaps.entrySet()) {
				for (int j = 0; j < entry.getValue().size(); j++) {
					if (Arrays.equals(entry.getValue().get(j), map)) {
						// it could be the case that one map belong to two
						// patterns
						// currently select the first pattern matched
						tmpPatternIDs.add(entry.getKey());
						tmpMapIDs.add(j);
						break loop_map;
					}
				}
			}
		}

		patternIDs.add(tmpPatternIDs.stream().mapToInt(i -> i).toArray());
		mapIDs.add(tmpMapIDs.stream().mapToInt(i -> i).toArray());
	}


}
