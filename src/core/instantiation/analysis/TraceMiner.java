package core.instantiation.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//import com.beust.jcommander.internal.Lists;

import ca.pfv.spmf.algorithms.clustering.dbscan.AlgoDBSCAN;
import ca.pfv.spmf.algorithms.clustering.distanceFunctions.DistanceFunction;
import ca.pfv.spmf.algorithms.clustering.kmeans.AlgoBisectingKMeans;
import ca.pfv.spmf.algorithms.clustering.kmeans.AlgoKMeans;
import ca.pfv.spmf.algorithms.clustering.optics.AlgoOPTICS;
import ca.pfv.spmf.algorithms.clustering.optics.DoubleArrayOPTICS;
import ca.pfv.spmf.algorithms.clustering.text_clusterer.TextClusterAlgo;
import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.AlgoClaSP;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.database.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.creators.IdListCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.creators.IdListCreatorStandard_Map;
import ca.pfv.spmf.algorithms.sequentialpatterns.occur.AlgoOccur;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan.AlgoPrefixSpan;
import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoTKS;
import ca.pfv.spmf.algorithms.sequentialpatterns.spam.PatternTKS;
import ca.pfv.spmf.patterns.cluster.Cluster;
import ca.pfv.spmf.patterns.cluster.ClusterWithMean;
import controller.instantiation.analysis.TraceViewerController;
import core.brs.parser.ActionWrapper;
import core.brs.parser.BRSParser;
import core.brs.parser.BigraphWrapper;
import core.brs.parser.utilities.JSONTerms;
import core.utilities.Query;
import cyberPhysical_Incident.Entity;
//import ie.lero.spare.franalyser.utility.FileManipulator;
//import ie.lero.spare.franalyser.utility.JSONTerms;
import ie.lero.spare.pattern_instantiation.GraphPath;
import ie.lero.spare.pattern_instantiation.IncidentPatternInstantiator;
import ie.lero.spare.pattern_instantiation.IncidentPatternInstantiator.InstancesSaver;

public class TraceMiner {

	// instances (traces). key is trace ID value is the trace as GraphPath
	// object
	Map<Integer, GraphPath> instances;

	String instanceFileName;
	String convertedInstancesFileName = "convertedInstances.txt";
	int numberOFClusters = 10;
	DistanceFunction distanceFunction;
	// AlgoKMeans kmean;

	// space is the separator
	public final static String DATA_SEPARATOR = ",";
	public final static String WEKA_DATA_SEPARATOR = ",";
	// cloud be the number of states
	public static int PADDING_STATE = -1;
	public static String PADDING_ACTION = "NULL";
	public static int PADDING_ACTION_INT = -1;
	// what value to give? probably larger would be better to get a noticable
	// difference
	public final static int ACTION_PERFORMED = 1;
	public final static int ACTION_NOT_PERFORMED = 0;
	public final static String ATTRIBUTE_STATE_NAME = "state-";
	public final static String ATTRIBUTE_ACTION_NAME = "action-";

	// errors
	public final static int TRACES_NOT_LOADED = -1;
	public final static int SHORTEST_FILE_NOT_SAVED = -2;

	// constants for mining operators
	public final static int SHORTEST = 0;
	public final static int ALL = 1;
	// public final static int CLASP = 2;

	String clusterFolder = "clusters generated";
	String clustersOutputFileName = "clustersGenerated.txt";
	String clustersOutputFolder;

	int longestTransition = -1;
	int shortestTransition = -1;
	int numberOfTraces = -1;

	// notify listener every n traces loaded
	int tracesLoadedNotifierNumber = 30;

	// key:action name, value: id
	Map<String, Integer> tracesActions;

	// key:action name, value: occurrence
	Map<String, Integer> tracesActionsOccurence;

	// key:state number, value: occurrence
	Map<Integer, Integer> statesOccurrences;

	// key:action name, value: occurrence
	Map<String, Integer> shortestActionsOccurence;

	// key:state, value: occurrence
	Map<Integer, Integer> shortestStatesOccurence;

	// shortest traces
	Map<Integer, GraphPath> shortestTraces;

	// prints a number of instances for each cluster
	int lengthToPrint = 5;

	// weka attributes
	String wekaInstancesFilePath = "wekaInstances.arff";

	// holds clusters generated
	List<ClusterWithMean> clusters;

	boolean isAlreadyChecked = false;

	String shortestTracesFileName;

	List<Integer> shortestTraceIDs;
	List<Integer> claSPTraceIDs;
	List<Integer> customeFilteringTraceIDs;

	String outputFolder;

	// min action length
	static final int MIN_LENGTH_INITIAL_VALUE = 1000000;
	int minimumTraceLength = MIN_LENGTH_INITIAL_VALUE;

	// max action length
	static final int MAX_LENGTH_INITIAL_VALUE = -1;
	int maximumTraceLength = -1;

	// system data
	int numberOfStates = 10000; // should be adjusted

	// listener (from GUI)
	TraceMinerListener listener;

	boolean isLoaded = false;

	// used for analysing common entities
	BRSParser brsParser;

	// map of all actions in the bigraphER file
	// key is action name, value is an ActionWrapper object containing action
	// info (pre, post)
	Map<String, ActionWrapper> bigraphERActions;

	public TraceMiner() {

		tracesActions = new HashMap<String, Integer>();
		tracesActionsOccurence = new HashMap<String, Integer>();
		shortestActionsOccurence = new HashMap<String, Integer>();

		statesOccurrences = new HashMap<Integer, Integer>();

		shortestTraces = new HashMap<Integer, GraphPath>();
		shortestStatesOccurence = new HashMap<Integer, Integer>();

		// instances = new HashMap<Integer, GraphPath>();

		shortestTraceIDs = new LinkedList<Integer>();
		claSPTraceIDs = new LinkedList<Integer>();
		customeFilteringTraceIDs = new LinkedList<Integer>();

		numberOfStates = 10000;

		PADDING_STATE = -1 * numberOfStates;

		PADDING_ACTION_INT = -1; // initial. should be changed according to
									// actions in the system or actions in the
									// traces

		/** Need to set system actions (all possible system actions) **/
		// some actions
		// systemActions.put("EnterRoom", 0);
		// systemActions.put("ConnectIPDevice", 1);
		// systemActions.put("DisconnectIPDevice", 2);
		// systemActions.put("ConnectBusDevice", 3);
		// systemActions.put("DisconnectBusDevice", 4);
		// systemActions.put("SendData", 5);
		// systemActions.put("SendMalware", 6);
		// systemActions.put("DisableHVAC", 7);
		// systemActions.put("EnterRoomWithoutCardReader", 8);
		// systemActions.put("ChangeAccessToCardRequired", 9);
		// systemActions.put("ChangeAccessToCardNotRequired", 10);
		// systemActions.put("ChangeContextToOutSideWorkingHours", 11);
		// systemActions.put("ChangeContextToWorkingHours", 12);
		// systemActions.put("TurnOnHVAC", 13);
		// systemActions.put("TurnOffHVAC", 14);
		// systemActions.put("TurnOnSmartTV", 15);
		// systemActions.put("TurnOffSmartTV", 16);
		// systemActions.put("GenerateData", 17);
		// systemActions.put("CollectData", 18);
		// systemActions.put("TurnONTVMicrophone", 19);
		// systemActions.put("TurnOffTVMicrophone", 20);
		// systemActions.put("TurnONTVCamera", 21);
		// systemActions.put("TurnOffTVCamera", 22);

		// set value of actions as more than the max number of states. This is
		// done to avoid mixing with states numbering

		// int index = 1;
		// int increment = (int) (numberOfStates * .05); // 1% of the number of
		// // states
		//
		// if (increment == 0) {
		// increment = 1;
		// }

	}

	public boolean checkFile(String fileName) {

		if (fileName == null || fileName.isEmpty()) {
			System.err.println("Given file name is NULL");
			return false;
		}

		if (!fileName.endsWith("json")) {
			System.err.println("file should be in JSON format (i.e. *.json)");
			return false;
		}

		File file = new File(fileName);

		if (!file.exists()) {
			System.err.println("file [" + fileName + "] does NOT exist");
			return false;
		}

		if (!file.isFile()) {
			System.err.println("[" + fileName + "] is NOT a file");
			return false;
		}

		return true;
	}

	/**
	 * Identify relevant traces (currently defined as shortest and has common
	 * patterns)
	 * 
	 * @param fileName
	 *            given file path (*.json or a folder containing JSON files)
	 */
	void identifyRelevantTraces(String fileName) {

		File inputFile = new File(fileName);

		if (inputFile.isFile()) {

			identifyRelevantTracesFromFile(fileName);

		} else if (inputFile.isDirectory()) {

			identifyRelevantTracesFromFolder(fileName);

		} else {
			System.err.println(fileName + " given file name is niether a FILE nor a FOLDER. Exiting");
			return;
		}

	}

	void identifyRelevantTracesFromFolder(String folderPath) {

		File inputFolder = new File(folderPath);

		StringBuilder content = new StringBuilder();

		// numberOfRelevantTraces = new LinkedList<Integer>();

		String linSeparator = System.getProperty("line.separator");

		content.append(
				"*Summary containing relevant traces (shortest & have common sequential patterns) identified from each file in folder ["
						+ folderPath + "]*")
				.append(linSeparator).append(linSeparator);
		content.append("Format: ").append(linSeparator);
		content.append("[File-name]").append(linSeparator);
		content.append("[# of relevant traces]").append(linSeparator);
		content.append("[relevant traces IDs]").append(linSeparator).append(linSeparator);

		int numOfRelevantTraces = 0;
		for (File file : inputFolder.listFiles()) {

			if (file.isFile()) {
				String filePath = file.getAbsolutePath();

				if (filePath.endsWith(".json")) {
					System.out.println("###### Identifying Relevant Traces ######");
					System.out.println("*File: " + filePath);
					System.out.println("\n");

					// identify relevant traces
					numOfRelevantTraces = identifyRelevantTracesFromFile(filePath);

					// file path
					content.append("#").append(filePath).append(linSeparator)
							// number of relevant traces
							.append(numOfRelevantTraces).append(linSeparator);
					// IDs
					// Integer[] ary = traceIDs.toArray(new
					// Integer[traceIDs.size()]);
					// content.append(Arrays.toString(ary)).append(linSeparator).append(linSeparator);

				}
			}
		}

		// store a summary into file
		String outputFile = folderPath + "/relevantTraces_Summary.txt";

		writeToFile(content.toString(), outputFile);

	}

	public int identifyRelevantTracesFromFile(String fileName) {

		if (!checkFile(fileName)) {
			return -1;
		}

		int numOfRelevantTraces = 0;

		instanceFileName = fileName;

		// clustersOutputFileName = instanceFileName.replace(".json",
		// "_relevantTraces.txt");// clustersOutputFolder
		// // clustersOutputFileName;
		// convertedInstancesFileName = instanceFileName.replace(".json",
		// "_convertedInstances.txt");// clustersOutputFolder
		// // +
		// shortestTracesFileName = instanceFileName.replace(".json",
		// "_shortestTracesIDs.txt");

		// loads instances(or traces) from given file name
		// and finds shortest transitions
		loadTracesFromFile();

		// find shortest traces
		findShortestTraces();

		/**
		 * ======Mine Frequent sequential patterns using the prefixspan algo
		 **/
		// numOfRelevantTraces =
		// mineSequencesUsingPrefixSpanAlgo(shortestTraces.values());

		// int minimumNumOfTracesForPattern = 5;
		// numOfRelevantTraces =
		// mineSequencesUsingPrefixSpanAlgo(shortestTraces.values(),minimumNumOfTracesForPattern);

		/** ======Mine Closed Frequent sequential patterns using the ClaSP **/
		// numOfRelevantTraces =
		// mineClosedSequencesUsingClaSPAlgo(shortestTraces.values());

		int minimumNumOfTracesForPattern = 5;
		numOfRelevantTraces = mineClosedSequencesUsingClaSPAlgo(shortestTraces.values(), minimumNumOfTracesForPattern);

		/** ======Mine Frequent sequential patterns using the SPADE algo **/
		// mineSequentialPatternsUsingSPADE();

		/** ======Mine Frequent sequential patterns using the TKS **/
		// finds top-k sequential patterns
		// allows to find contiguous sequence patterns
		// mineSequentialPatternsUsingTKSAlgo(shortestTraces.values());

		/** ======Mine Frequent Itemsets using FP-Growth algo **/
		// mineFrequentItemsetsUsingFP_GrowthAlgo();

		System.out.println("\n>>DONE");

		return numOfRelevantTraces;

	}

	public int loadTracesFromFile() {

		System.out.println(">>Reading instances from [" + instanceFileName + "]");

		// load instances from file
		// List<Integer> minMaxLengths = new LinkedList<Integer>();
		// List<String> tracesActs = new LinkedList<String>();

		// reset
		tracesActionsOccurence.clear();
		minimumTraceLength = 100000;
		maximumTraceLength = -1;

		instances = readInstantiatorInstancesFile(instanceFileName);

		// set traces actions
		if (tracesActionsOccurence.size() > 0) {
			tracesActions.clear();
			int index = 0;
			for (String action : tracesActionsOccurence.keySet()) {
				tracesActions.put(action, index);
				index++;
			}
		}

		System.out.println(">>Number of instances read = " + instances.size() + "\n>>Min trace length: "
				+ minimumTraceLength + "\n>>Max trace length: " + maximumTraceLength + "\n>>Actions: " + tracesActions
				+ "\n>>Occurrences: " + tracesActionsOccurence);

		// used when converting traces to mining format
		PADDING_ACTION_INT = -1 * tracesActions.size();

		// System.out.println(instances.get(0).getTransitionActions());
		if (instances == null) {
			System.out.println("Instances are null! Exiting");
			isLoaded = false;
			return TRACES_NOT_LOADED;

		}

		isLoaded = true;

		return instances.size();

	}

	/**
	 * Return Actions with the highest occurrence in all given traces
	 * 
	 * @return Map with the key as action name and value as occurrence
	 */
	public Map<String, Integer> getHighestActionOccurrence() {

		if (tracesActionsOccurence == null || tracesActionsOccurence.isEmpty()) {
			return null;
		}

		Map<String, Integer> result = new HashMap<String, Integer>();

		int index = 0;

		// sort occurrences
		Collection<Integer> values = tracesActionsOccurence.values();
		List<Integer> list = new LinkedList<Integer>(values);
		Collections.sort(list);// ascending order

		// List<Integer> topN = new LinkedList<Integer>();

		if (list.size() == 0) {
			return null;
		}

		int highestOccur = list.get(list.size() - 1);

		// for now get the first n
		for (Entry<String, Integer> entry : tracesActionsOccurence.entrySet()) {

			String action = entry.getKey();
			int occur = entry.getValue();

			if (occur == highestOccur) {
				result.put(action, occur);
				index++;
			}

		}

		return result;
	}

	public Map<String, Integer> getTopActionOccurrences(int numberofActions, String tracesToFilter) {

		Map<String, Integer> result = new HashMap<String, Integer>();
		Map<String, Integer> occurrences = null;

		int index = 0;

		// for now get the first n from traces
		switch (tracesToFilter) {

		case TraceViewerController.ALL_TRACES:
			occurrences = tracesActionsOccurence;
			break;

		case TraceViewerController.SHORTEST_TRACES:
			if (shortestActionsOccurence != null && !shortestActionsOccurence.isEmpty()) {
				occurrences = shortestActionsOccurence;
			} else {
				occurrences = getOccurrence(shortestTraces);
				shortestActionsOccurence = occurrences;
			}

			break;

		case TraceViewerController.SHORTEST_CLASP_TRACES:
			if (claSPTraceIDs != null && !claSPTraceIDs.isEmpty()) {
				Map<Integer, GraphPath> traces = getTraces(claSPTraceIDs);
				occurrences = getOccurrence(traces);
			}
			break;

		case TraceViewerController.CUSTOMISED_TRACES:
			if (customeFilteringTraceIDs != null && !customeFilteringTraceIDs.isEmpty()) {
				Map<Integer, GraphPath> traces = getTraces(customeFilteringTraceIDs);
				occurrences = getOccurrence(traces);
			}
			break;

		default:
			break;
		}

		if (occurrences != null) {
			// sort occurrences
			Collection<Integer> values = occurrences.values();
			List<Integer> list = new LinkedList<Integer>(values);
			Collections.sort(list);// ascending order

			List<Integer> topN = new LinkedList<Integer>();

			for (int i = list.size() - 1; i > 0; i--) {

				if (!topN.contains(list.get(i))) {
					topN.add(list.get(i));
					index++;

					if (index == numberofActions) {
						break;
					}
				}

			}

			for (Entry<String, Integer> entry : occurrences.entrySet()) {

				String action = entry.getKey();
				int occur = entry.getValue();

				if (topN.contains(occur)) {
					result.put(action, occur);
					index++;
				}

			}
		}

		return result;
	}

	public Map<Integer, Integer> getTopStatesOccurrences(int numberofActions, String tracesToFilter) {

		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		Map<Integer, Integer> occurrences = null;

		int index = 0;

		// for now get the first n from traces
		switch (tracesToFilter) {

		case TraceViewerController.ALL_TRACES:
			occurrences = statesOccurrences;
			break;

		case TraceViewerController.SHORTEST_TRACES:
			if (shortestStatesOccurence != null && !shortestStatesOccurence.isEmpty()) {
				occurrences = shortestStatesOccurence;
			} else {
				occurrences = getStateOccurrence(shortestTraces);
				shortestStatesOccurence = occurrences;
			}

			break;

		case TraceViewerController.SHORTEST_CLASP_TRACES:
			if (claSPTraceIDs != null && !claSPTraceIDs.isEmpty()) {
				Map<Integer, GraphPath> traces = getTraces(claSPTraceIDs);
				occurrences = getStateOccurrence(traces);
			}
			break;

		case TraceViewerController.CUSTOMISED_TRACES:
			if (customeFilteringTraceIDs != null && !customeFilteringTraceIDs.isEmpty()) {
				Map<Integer, GraphPath> traces = getTraces(customeFilteringTraceIDs);
				occurrences = getStateOccurrence(traces);
			}
			break;

		default:
			break;
		}

		if (occurrences != null) {
			// sort occurrences
			Collection<Integer> values = occurrences.values();
			List<Integer> list = new LinkedList<Integer>(values);
			Collections.sort(list);// ascending order

			List<Integer> topN = new LinkedList<Integer>();

			for (int i = list.size() - 1; i > 0; i--) {

				if (!topN.contains(list.get(i))) {
					topN.add(list.get(i));
					index++;

					if (index == numberofActions) {
						break;
					}
				}

			}

			// for now get the first n
			for (Entry<Integer, Integer> entry : occurrences.entrySet()) {

				Integer state = entry.getKey();
				int occur = entry.getValue();

				if (topN.contains(occur)) {
					result.put(state, occur);
					index++;
				}

			}

		}

		return result;
	}

	public Map<String, Integer> getActionsWithOccurrencePercentage(double percentage, String operation,
			String tracesToFilter) {

		Map<String, Integer> result = new HashMap<String, Integer>();
		Map<String, Integer> occurrences = null;

		double localPerc = 0;
		int numOfTraces = -1;

		switch (tracesToFilter) {
		case TraceViewerController.ALL_TRACES:
			occurrences = tracesActionsOccurence;
			numOfTraces = instances.size();
			break;

		case TraceViewerController.SHORTEST_TRACES:
			if (shortestActionsOccurence != null && !shortestActionsOccurence.isEmpty()) {
				occurrences = shortestActionsOccurence;
			} else {
				occurrences = getOccurrence(shortestTraces);
				shortestActionsOccurence = occurrences;
			}

			numOfTraces = shortestTraces.size();
			break;

		case TraceViewerController.SHORTEST_CLASP_TRACES:
			Map<Integer, GraphPath> traces1 = null;
			if (claSPTraceIDs != null && !claSPTraceIDs.isEmpty()) {
				traces1 = getTraces(claSPTraceIDs);
				occurrences = getOccurrence(traces1);
			}

			if (traces1 != null) {
				numOfTraces = traces1.size();
			}

			break;

		case TraceViewerController.CUSTOMISED_TRACES:
			Map<Integer, GraphPath> traces = null;
			if (customeFilteringTraceIDs != null && !customeFilteringTraceIDs.isEmpty()) {
				traces = getTraces(customeFilteringTraceIDs);
				occurrences = getOccurrence(traces);
			}

			if (traces != null) {
				numOfTraces = traces.size();
			}

			break;
		default:
			break;
		}

		if (occurrences != null && !occurrences.isEmpty() && numOfTraces > 0) {

			// numOfTraces = occurrences.size();
			System.out.println("miner " + occurrences);
			// for now get the first n
			for (Entry<String, Integer> entry : occurrences.entrySet()) {

				String action = entry.getKey();
				int occur = entry.getValue();

				localPerc = occur * 1.0 / numOfTraces;

				switch (operation) {
				case TraceViewerController.EQUAL:
					if (localPerc == percentage) {
						result.put(action, occur);
						// index++;
					}
					break;

				case TraceViewerController.MORE_THAN_EQUAL:
					if (localPerc >= percentage) {
						result.put(action, occur);
						// index++;
					}
					break;

				case TraceViewerController.LESS_THAN_EQUAL:
					if (localPerc <= percentage) {
						result.put(action, occur);
						// index++;
					}
					break;

				default:
					break;
				}

			}
		}

		// tracesActionsOccurence.s
		return result;
	}

	public Map<Integer, Integer> getStatesWithOccurrencePercentage(double percentage, String operation,
			String tracesToFilter) {

		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		Map<Integer, Integer> occurrences = null;

		int index = 0;
		int numOfTraces = -1;

		switch (tracesToFilter) {

		case TraceViewerController.ALL_TRACES:
			occurrences = statesOccurrences;
			numOfTraces = instances.size();
			break;

		case TraceViewerController.SHORTEST_TRACES:
			if (shortestStatesOccurence != null && !shortestStatesOccurence.isEmpty()) {
				occurrences = shortestStatesOccurence;
			} else {
				occurrences = getStateOccurrence(shortestTraces);
				shortestStatesOccurence = occurrences;
			}

			numOfTraces = shortestTraces.size();

			break;

		case TraceViewerController.SHORTEST_CLASP_TRACES:
			Map<Integer, GraphPath> traces1 = null;
			if (claSPTraceIDs != null && !claSPTraceIDs.isEmpty()) {
				traces1 = getTraces(claSPTraceIDs);
				occurrences = getStateOccurrence(traces1);
			}
			if (traces1 != null) {
				numOfTraces = traces1.size();
			}

			break;

		case TraceViewerController.CUSTOMISED_TRACES:
			Map<Integer, GraphPath> traces = null;
			if (customeFilteringTraceIDs != null && !customeFilteringTraceIDs.isEmpty()) {
				traces = getTraces(customeFilteringTraceIDs);
				occurrences = getStateOccurrence(traces);
			}
			if (traces != null) {
				numOfTraces = traces.size();
			}

			break;

		default:
			break;
		}

		if (occurrences != null && !occurrences.isEmpty() && numOfTraces > 0) {

			// numOfTraces = occurrences.size();
			double localPerc = 0;

			// for now get the first n
			for (Entry<Integer, Integer> entry : occurrences.entrySet()) {

				Integer state = entry.getKey();
				int occur = entry.getValue();

				localPerc = occur * 1.0 / numOfTraces;

				switch (operation) {
				case TraceViewerController.EQUAL:
					if (localPerc == percentage) {
						result.put(state, occur);
						index++;
					}
					break;

				case TraceViewerController.MORE_THAN_EQUAL:
					if (localPerc >= percentage) {
						result.put(state, occur);
						index++;
					}
					break;

				case TraceViewerController.LESS_THAN_EQUAL:
					if (localPerc <= percentage) {
						result.put(state, occur);
						index++;
					}
					break;

				default:
					break;
				}

			}
		}

		return result;
	}

	public Map<String, Integer> getLowestActionOccurrences(int numberofActions, String tracesToFilter) {

		Map<String, Integer> result = new HashMap<String, Integer>();
		Map<String, Integer> occurrences = null;
		int index = 0;

		// for now get the first n from traces
		switch (tracesToFilter) {

		case TraceViewerController.ALL_TRACES:
			occurrences = tracesActionsOccurence;
			break;

		case TraceViewerController.SHORTEST_TRACES:
			if (shortestActionsOccurence != null && !shortestActionsOccurence.isEmpty()) {
				occurrences = shortestActionsOccurence;
			} else {
				occurrences = getOccurrence(shortestTraces);
				shortestActionsOccurence = occurrences;
			}

			break;

		case TraceViewerController.SHORTEST_CLASP_TRACES:
			if (claSPTraceIDs != null && !claSPTraceIDs.isEmpty()) {
				Map<Integer, GraphPath> traces = getTraces(claSPTraceIDs);
				occurrences = getOccurrence(traces);
			}
			break;

		case TraceViewerController.CUSTOMISED_TRACES:
			if (customeFilteringTraceIDs != null && !customeFilteringTraceIDs.isEmpty()) {
				Map<Integer, GraphPath> traces = getTraces(customeFilteringTraceIDs);
				occurrences = getOccurrence(traces);
			}
			break;
		default:
			break;
		}

		if (occurrences != null) {
			Collection<Integer> values = occurrences.values();
			List<Integer> list = new LinkedList<Integer>(values);
			Collections.sort(list);// ascending order

			List<Integer> topN = new LinkedList<Integer>();
			int size = list.size();

			for (int i = 0; i < list.size(); i++) {

				if (!topN.contains(list.get(i))) {
					topN.add(list.get(i));
					index++;

					if (index == numberofActions) {
						break;
					}
				}

			}

			// for now get the first n
			for (Entry<String, Integer> entry : occurrences.entrySet()) {

				String action = entry.getKey();
				int occur = entry.getValue();

				if (topN.contains(occur)) {
					result.put(action, occur);
					index++;
				}

			}
		}

		return result;
	}

	public Map<Integer, Integer> getLowestStateOccurrences(int numberofStates, String tracesToFilter) {

		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		Map<Integer, Integer> occurrences = null;

		int index = 0;

		switch (tracesToFilter) {

		case TraceViewerController.ALL_TRACES:
			occurrences = statesOccurrences;
			break;

		case TraceViewerController.SHORTEST_TRACES:
			if (shortestStatesOccurence != null && !shortestStatesOccurence.isEmpty()) {
				occurrences = shortestStatesOccurence;
			} else {
				occurrences = getStateOccurrence(shortestTraces);
				shortestStatesOccurence = occurrences;
			}

			break;

		case TraceViewerController.SHORTEST_CLASP_TRACES:
			if (claSPTraceIDs != null && !claSPTraceIDs.isEmpty()) {
				Map<Integer, GraphPath> traces = getTraces(claSPTraceIDs);
				occurrences = getStateOccurrence(traces);
			}
			break;

		case TraceViewerController.CUSTOMISED_TRACES:
			if (customeFilteringTraceIDs != null && !customeFilteringTraceIDs.isEmpty()) {
				Map<Integer, GraphPath> traces = getTraces(customeFilteringTraceIDs);
				occurrences = getStateOccurrence(traces);
			}
			break;

		default:
			break;
		}

		if (occurrences != null) {
			// sort occurrences
			Collection<Integer> values = occurrences.values();
			List<Integer> list = new LinkedList<Integer>(values);
			Collections.sort(list);// ascending order

			List<Integer> topN = new LinkedList<Integer>();
			int size = list.size();

			for (int i = 0; i < list.size(); i++) {

				if (!topN.contains(list.get(i))) {
					topN.add(list.get(i));
					index++;

					if (index == numberofStates) {
						break;
					}
				}

			}

			// for now get the first n
			for (Entry<Integer, Integer> entry : occurrences.entrySet()) {

				Integer state = entry.getKey();
				int occur = entry.getValue();

				if (topN.contains(occur)) {
					result.put(state, occur);
					index++;
				}

				// if (index == numberofActions) {
				// break;
				// }
			}
		}

		// tracesActionsOccurence.s
		return result;
	}

	/**
	 * Finds Action occurrrences for the given traces
	 * 
	 * @param traces
	 * @return
	 */
	public Map<String, Integer> getOccurrence(Map<Integer, GraphPath> traces) {

		Map<String, Integer> result = new HashMap<String, Integer>();

		for (GraphPath trace : traces.values()) {

			// get actions
			List<String> actions = trace.getTransitionActions();

			for (String action : actions) {

				// if action already exist then add one to current occurrence
				// value
				if (result.containsKey(action)) {
					int oldValue = result.get(action);
					oldValue++;
					result.put(action, oldValue);

					// else create new entry for the action
				} else {
					result.put(action, 1);
				}
			}
		}

		return result;
	}

	/**
	 * Finds States occurrences for the given traces
	 * 
	 * @param traces
	 * @return
	 */
	public Map<Integer, Integer> getStateOccurrence(Map<Integer, GraphPath> traces) {

		Map<Integer, Integer> result = new HashMap<Integer, Integer>();

		for (GraphPath trace : traces.values()) {

			// get actions
			List<Integer> states = trace.getStateTransitions();

			for (Integer state : states) {

				// if action already exist then add one to current occurrence
				// value
				if (result.containsKey(state)) {
					int oldValue = result.get(state);
					oldValue++;
					result.put(state, oldValue);

					// else create new entry for the action
				} else {
					result.put(state, 1);
				}
			}
		}

		for (Entry<Integer, Integer> entr : result.entrySet()) {
			if (entr.getValue() == 5) {
				System.out.println(entr);
			}

		}
		// System.out.println(result);

		return result;
	}

	public int findShortestTraces() {

		// shortest trace is set to be 3 actions (or 4 states (i.e. actions+1)

		int minimumStates = minimumTraceLength + 1;

		String separator = " ";
		StringBuilder bldr = new StringBuilder();

		if (shortestTraces != null) {
			shortestTraces.clear();
		} else {
			shortestTraces = new HashMap<Integer, GraphPath>();
		}

		System.out.println(">>Identifying shortest traces in [" + instanceFileName + "]");
		for (GraphPath trace : instances.values()) {

			if (trace.getStateTransitions().size() == minimumStates) {
				shortestTraces.put(trace.getInstanceID(), trace);
				bldr.append(trace.getInstanceID()).append(separator);
			}
		}

		if (bldr.length() > 0) {
			bldr.deleteCharAt(bldr.length() - 1);// remove extra space
		}

		// store to file
		if (shortestTracesFileName != null) {
			writeToFile(bldr.toString(), shortestTracesFileName);
			System.out.println(">>Shortest traces IDs are stored in [" + shortestTracesFileName + "]");
		}

		shortestTraceIDs = Arrays.asList(shortestTraces.keySet().toArray(new Integer[shortestTraces.size()]));

		return shortestTraces.size();

	}

	public int findShortestTraces(boolean saveToFile) {

		// shortest trace is set to be 3 actions (or 4 states (i.e. actions+1)

		int numberOfStates = 4;

		String separator = " ";
		StringBuilder bldr = new StringBuilder();

		if (shortestTraces != null) {
			shortestTraces.clear();
		}

		System.out.println(">>Identifying shortest traces in [" + instanceFileName + "]");
		for (GraphPath trace : instances.values()) {

			if (trace.getStateTransitions().size() == numberOfStates) {
				shortestTraces.put(trace.getInstanceID(), trace);
				bldr.append(trace.getInstanceID()).append(separator);
			}
		}

		if (bldr.length() > 0) {
			bldr.deleteCharAt(bldr.length() - 1);// remove extra space
		}

		// store to file
		if (saveToFile) {

			if (shortestTracesFileName == null) {
				if (instanceFileName != null) {
					shortestTracesFileName = instanceFileName.replace(".json", "_shortestTracesIDs.txt");
				}

			}

			if (shortestTracesFileName == null) {
				return SHORTEST_FILE_NOT_SAVED;
			}

			writeToFile(bldr.toString(), shortestTracesFileName);
			System.out.println(">>Shortest traces IDs are stored in [" + shortestTracesFileName + "]");
		}

		return shortestTraces.size();

	}

	public List<ClusterWithMean> generateClustersUsingKMean() {

		AlgoKMeans kmean = new AlgoKMeans();

		try {

			numberOFClusters = 6;

			System.out.println(">>Generating clusters using K-mean algorithm" + " with K = " + numberOFClusters
					+ ", distance function is " + distanceFunction.getName());

			// generate clusters
			clusters = kmean.runAlgorithm(convertedInstancesFileName, numberOFClusters, distanceFunction,
					DATA_SEPARATOR);

			// store clusters (each line is a cluster in the output file)
			kmean.saveToFile(clustersOutputFileName);

			kmean.printStatistics();

			return clusters;
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public List<ClusterWithMean> generateClustersUsingKMeanUsingBiSect() {

		AlgoBisectingKMeans kmean = new AlgoBisectingKMeans();

		int iteratorForSplit = numberOFClusters * 2;

		try {
			System.out.println(">>Generating clusters using K-mean algorithm with K = " + numberOFClusters
					+ ", distance function is " + distanceFunction.getName());
			clusters = kmean.runAlgorithm(convertedInstancesFileName, numberOFClusters, distanceFunction,
					iteratorForSplit, DATA_SEPARATOR);

			kmean.saveToFile(clustersOutputFileName);

			return clusters;
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public List<Cluster> generateClustersUsingDBSCAN() {

		AlgoDBSCAN algo = new AlgoDBSCAN();

		// minimum number of points/instances in a cluster
		int minPoints = 10;
		// distance between points/instances in a cluster
		double epsilon = 10d;
		// double epsilonPrime = epsilon;

		try {
			System.out.println(">>Generating clusters using DBSCAN algorithm");

			// generate clusters
			List<Cluster> clusters = algo.runAlgorithm(convertedInstancesFileName, minPoints, epsilon, DATA_SEPARATOR);

			// store clusters (each line is a cluster in the output file)
			algo.saveToFile(clustersOutputFileName);

			algo.printStatistics();

			return clusters;
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public List<Cluster> generateClustersUsingOPTICS() {

		AlgoOPTICS algo = new AlgoOPTICS();

		// minimum number of points/instances in a cluster
		int minPoints = 10;
		// distance between points/instances in a cluster
		double epsilon = 2d;
		double epsilonPrime = epsilon;

		try {
			System.out.println(">>Generating clusters using OPTIC algorithm");

			// generate clusters
			List<DoubleArrayOPTICS> clusters = algo.computerClusterOrdering(convertedInstancesFileName, minPoints,
					epsilon, DATA_SEPARATOR);

			// generate dbscan clusters from the cluster ordering:
			List<Cluster> dbScanClusters = algo.extractDBScan(minPoints, epsilonPrime);

			// store clusters (each line is a cluster in the output file)
			algo.saveToFile(clustersOutputFileName);

			algo.printStatistics();

			return dbScanClusters;
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public void generateClustersUsingTextMining() {

		TextClusterAlgo algo = new TextClusterAlgo();

		boolean stemFlag = true;
		boolean stopWordFlag = true;

		System.out.println(">>Generating clusters using Text Clustering algorithm");

		// generate clusters
		algo.runAlgorithm(convertedInstancesFileName, clustersOutputFileName, stemFlag, stopWordFlag);

		// clusters generated are stored in a file (e.g.,
		// clustersOutputFileName)

		algo.printStatistics();
	}

	public String convertInstancesToMiningFormat(List<GraphPath> instances) {

		// convert instances to a format compatible with that of the data mining
		// library used (i.e. SPMF)
		// line format could have:
		// @NAME="instance name"
		String instanceName = "@NAME=";
		// @ATTRIBUTEDEF="attribute name"
		String attributeName = "@ATTRIBUTEDEF=";
		// #, % are used for comments and any meta-data respectively
		// data1 [separator] data2 [separator] data3 ... (actual data treated as
		// double array)

		/**
		 * all data array should be of the same length so states that are short
		 * than the longest are padded with -1
		 **/
		// create a text file to hold the data

		String fileLinSeparator = System.getProperty("line.separator");

		StringBuilder builder = new StringBuilder();

		if (instances != null && !instances.isEmpty()) {
			shortestTransition = instances.get(0).getStateTransitions().size();
		}

		// find longest and shortest transitions
		for (GraphPath path : instances) {
			List<Integer> tmp = path.getStateTransitions();

			if (tmp.size() > longestTransition) {
				longestTransition = tmp.size();
			} else if (tmp.size() < shortestTransition) {
				shortestTransition = tmp.size();
			}
		}

		// numberOFClusters = (longestTransition - shortestTransition) + 1;

		int numberOfActions = longestTransition - 1;

		int i = 0;

		// ========states attributes (state-0, state-1, number of maximum
		// states)
		for (i = 0; i < longestTransition - 1; i++) {

			// add attribute name e.g., "state-0 state-1 ..."
			builder.append(attributeName).append(ATTRIBUTE_STATE_NAME).append(i).append(fileLinSeparator);
			builder.append(attributeName).append(ATTRIBUTE_ACTION_NAME).append(i).append(fileLinSeparator);
		}

		builder.append(attributeName).append(ATTRIBUTE_STATE_NAME).append(i).append(fileLinSeparator);

		// ========actions attribute (actions names)
		// for (String action : systemActions.keySet()) {
		// builder.append(attributeName).append(action).append(fileLinSeparator);
		// }

		// ========set data
		for (GraphPath path : instances) {

			// set instance name to be the instance id
			builder.append(instanceName).append(path.getInstanceID()).append(fileLinSeparator);

			// set data to be the states and actions of transitions
			List<Integer> states = path.getStateTransitions();
			List<String> transitionActions = path.getTransitionActions();

			for (i = 0; i < states.size() - 1; i++) {

				// add state
				builder.append(states.get(i)).append(DATA_SEPARATOR);

				// add action
				builder.append(tracesActions.get(transitionActions.get(i))).append(DATA_SEPARATOR);
			}

			// add last state
			builder.append(states.get(i));
			// builder.append(systemActions.get(transitionActions.get(i)));

			// pad the transition with -1
			if (states.size() < longestTransition) {

				int numOfExtraStates = longestTransition - states.size();

				builder.append(DATA_SEPARATOR);

				// add dummy action
				builder.append(PADDING_ACTION_INT).append(DATA_SEPARATOR);

				for (i = 0; i < numOfExtraStates - 1; i++) {

					builder.append(PADDING_STATE).append(DATA_SEPARATOR);
					builder.append(PADDING_ACTION_INT).append(DATA_SEPARATOR);
				}

				builder.append(PADDING_STATE);
				// builder.append(PADDING_ACTION);
			}

			// add action data
			// 0 for missing the action from the transition actions. 1 if it
			// exists

			// if (transitionActions != null && !transitionActions.isEmpty()) {
			//
			// builder.append(DATA_SEPARATOR);
			//
			//// for (i = 0; i < systemActions.size() - 1; i++) {
			////
			//// if (transitionActions.contains(systemActions.get(i))) {
			//// builder.append(ACTION_PERFORMED).append(DATA_SEPARATOR);
			//// } else {
			//// builder.append(ACTION_NOT_PERFORMED).append(DATA_SEPARATOR);
			//// }
			//// }
			//
			// //add action as an index in the system actions
			// for(i=0;i< transitionActions.size()-1;i++) {
			// builder.append(systemActions.get(transitionActions.get(i))).append(DATA_SEPARATOR);
			// }
			//
			// // check last action
			//// if (transitionActions.contains(systemActions.get(i))) {
			//// builder.append(ACTION_PERFORMED);
			//// } else {
			//// builder.append(ACTION_NOT_PERFORMED);
			//// }
			//
			// builder.append(systemActions.get(transitionActions.get(i)));
			// }

			builder.append(fileLinSeparator);
		}

		// save string to file

		writeToFile(builder.toString(), convertedInstancesFileName);

		return convertedInstancesFileName;
	}

	public String convertInstancesToTextMiningFormat(List<GraphPath> instances) {

		String fileLinSeparator = System.getProperty("line.separator");

		StringBuilder builder = new StringBuilder();

		// ========set data
		for (GraphPath path : instances) {

			// === get states as string
			// String statesStr = path.getStateTransitions().toString();
			// // remove brackets
			// statesStr = statesStr.replaceAll("\\[", "");
			// statesStr = statesStr.replaceAll("\\]", "");
			// // remove commas
			// statesStr = statesStr.replaceAll(",", "");
			// statesStr = statesStr.trim();

			// === get actions as string
			String actionsStr = path.getTransitionActions().toString();
			actionsStr = actionsStr.replaceAll("\\[", "");
			actionsStr = actionsStr.replaceAll("\\]", "");
			actionsStr = actionsStr.replaceAll(",", "");
			actionsStr = actionsStr.trim();

			// === set record(instance_id [states (1 2 3) actions (enterRoom)]
			builder.append(path.getInstanceID()).append("\t")
					// .append(statesStr)
					// .append(" ")
					.append(actionsStr).append(fileLinSeparator);

		}
		writeToFile(builder.toString(), convertedInstancesFileName);

		return convertedInstancesFileName;
	}

	public String convertUsingActionEntities(List<GraphPath> instances) {

		String fileLinSeparator = System.getProperty("line.separator");

		StringBuilder builder = new StringBuilder();

		// ========set data
		for (GraphPath path : instances) {

			// === get states as string
			// String statesStr = path.getStateTransitions().toString();
			// // remove brackets
			// statesStr = statesStr.replaceAll("\\[", "");
			// statesStr = statesStr.replaceAll("\\]", "");
			// // remove commas
			// statesStr = statesStr.replaceAll(",", "");
			// statesStr = statesStr.trim();

			// === get actions as string
			List<String> actions = path.getTransitionActions();

			int count = 0;

			for (String act : actions) {
				List<String> actionEntities = new LinkedList<String>();

				ActionWrapper actionDetails = bigraphERActions.get(act);

				// allow repetition of entities

				if (actionDetails != null) {

					// add entities from pre
					BigraphWrapper pre = actionDetails.getPrecondition();

					if (pre != null) {
						Set<Entity> ents = pre.getControlMap().keySet();

						for (Entity ent : ents) {
							actionEntities.add(ent.getName());
						}
					}

					BigraphWrapper post = actionDetails.getPostcondition();

					if (post != null) {
						Set<Entity> ents = post.getControlMap().keySet();

						for (Entity ent : ents) {
							actionEntities.add(ent.getName());
						}
					}
				}

				if (!actionEntities.isEmpty()) {
					// === set record(instance_id [states (1 2 3) actions
					// (enterRoom)]
					String actionsStr = actionEntities.toString();

					actionsStr = actionsStr.replaceAll("\\[", "");
					actionsStr = actionsStr.replaceAll("\\]", "");
					actionsStr = actionsStr.replaceAll(",", "");
					actionsStr = actionsStr.trim();

					// id
					builder.append(count).append("\t");
					// data separated by space
					builder.append(actionsStr).append(fileLinSeparator);
				}

				count++;

			}

		}
		writeToFile(builder.toString(), convertedInstancesFileName);

		return convertedInstancesFileName;
	}

	protected void writeToFile(String text, String fileName) {

		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));

			writer.write(text);

			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**************************************/
	/***** SEQUENTIAL PATTERN MINING ******/
	/**************************************/

	public int mineSequencesUsingPrefixSpanAlgo(Collection<GraphPath> traces) {

		convertedInstancesFileName = toSPMFsequentialPatternFormat(traces);

		// Create an instance of the algorithm with minsup = 50 %
		AlgoPrefixSpan algo = new AlgoPrefixSpan();

		try {

			// run several times till you find max minsup that which after there
			// will be no result

			boolean isMaxFound = false;
			int tries = traces.size() / 2 + 1;

			// binary search
			int left = 0;
			int right = traces.size() - 1;
			int mid = -1;

			while ((left <= right) & tries > 0) {

				mid = (int) Math.floor((left + right) / 2);

				algo.runAlgorithm(convertedInstancesFileName, clustersOutputFileName, mid);
				String[] lines = FileManipulator.readFileNewLine(clustersOutputFileName);

				// if there is output, then increase minsup. Else, decrease
				if (lines != null && lines.length > 0 && !lines[0].isEmpty()) {
					left = mid + 1;
					System.out
							.println(">>[L] trying min-traces (i.e. minsup) " + mid + " l = " + left + " r = " + right);
					isMaxFound = true;
				} else {
					isMaxFound = false;
					right = mid - 1;
					System.out
							.println(">>[R] trying min-traces (i.e. minsup) " + mid + " l = " + left + " r = " + right);
				}

				tries--;
			}

			// if the last mid (or minimum trace/minsup) has zero patterns, then
			// decrement till a value is returned
			if (!isMaxFound) {
				while (mid > 0) {

					mid--;

					algo.runAlgorithm(convertedInstancesFileName, clustersOutputFileName, mid);
					String[] lines = FileManipulator.readFileNewLine(clustersOutputFileName);

					// if there is output, then increase minsup. Else, decrease
					if (lines != null && lines.length > 0 && !lines[0].isEmpty()) {
						break;
					}
				}
			} // else increase mid until there's no output.
			else {
				while (mid < right) {

					mid++;

					algo.runAlgorithm(convertedInstancesFileName, clustersOutputFileName, mid);
					String[] lines = FileManipulator.readFileNewLine(clustersOutputFileName);

					// if there is output, then increase minsup. Else, decrease
					if (lines == null || lines.length == 0 || lines[0].isEmpty()) {
						mid--;
						break;
					}
				}
			}

			System.out.println(">>Minimum traces is " + mid);

			return mineSequencesUsingPrefixSpanAlgo(traces, mid);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}

	public int mineSequencesUsingPrefixSpanAlgo(Collection<GraphPath> traces, int minimumTraces) {

		File file = new File(convertedInstancesFileName);

		if (!file.exists()) {
			convertedInstancesFileName = toSPMFsequentialPatternFormat(traces);
		}

		AlgoPrefixSpan algo = new AlgoPrefixSpan();

		int minsup = minimumTraces; // use a minimum support of x sequences.

		// if you set the following parameter to true, the sequence ids of the
		// sequences where
		// each pattern appears will be shown in the result
		algo.setShowSequenceIdentifiers(true);

		// execute the algorithm
		try {

			algo.runAlgorithm(convertedInstancesFileName, clustersOutputFileName, minsup);
			algo.printStatistics();

			// analysis of the generated sequential patterns
			String analysisFile = instanceFileName.replace(".json", "_PerfixSpan_analysis.txt");

			analyseGeneratedSequencePatterns(convertedInstancesFileName, clustersOutputFileName, analysisFile);

			claSPTraceIDs = getTracesIDsFromOutputFile(clustersOutputFileName);

			if (claSPTraceIDs != null) {
				return claSPTraceIDs.size();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}

	/**
	 * Finds all traces using the ClaSP algorithm (sequential mining algorithm)
	 * 
	 * @param tracesToAnalyse
	 *            (indicates which traces to use for analysis
	 * @return
	 */
	public int mineClosedSequencesUsingClaSPAlgo(int tracesToAnalyse) {

		switch (tracesToAnalyse) {
		case SHORTEST: // shortest
			findShortestTraces();
			return mineClosedSequencesUsingClaSPAlgo(shortestTraces.values());

		case ALL: // all
			return mineClosedSequencesUsingClaSPAlgo(instances.values());

		default:
			return -1;

		}

	}

	public int mineClosedSequencesUsingClaSPAlgo(int tracesToAnalyse, int minimumTraces) {

		switch (tracesToAnalyse) {
		case SHORTEST:
			findShortestTraces();
			return mineClosedSequencesUsingClaSPAlgo(shortestTraces.values(), minimumTraces);

		case ALL:
			return mineClosedSequencesUsingClaSPAlgo(instances.values(), minimumTraces);

		default:
			return -1;

		}

	}

	public int mineClosedSequencesUsingClaSPAlgo(Collection<GraphPath> traces, int minimumTraces) {

		convertedInstancesFileName = toSPMFsequentialPatternFormat(traces);

		int numberOfTraces = minimumTraces;
		// int numOfRelevantTraces = 0;

		// Load a sequence database
		double support = numberOfTraces * 1.0 / traces.size();

		boolean keepPatterns = true; // if set to true, then generated result
										// is stored in the given file
		boolean verbose = true;
		boolean findClosedPatterns = true;
		boolean executePruningMethods = true;
		// if you set the following parameter to true, the sequence ids of the
		// sequences where
		// each pattern appears will be shown in the result
		boolean outputSequenceIdentifiers = true;

		AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
		IdListCreator idListCreator = IdListCreatorStandard_Map.getInstance();

		SequenceDatabase sequenceDatabase = new SequenceDatabase(abstractionCreator, idListCreator);

		double relativeSupport;

		try {

			relativeSupport = sequenceDatabase.loadFile(convertedInstancesFileName, support);

			// double relativeSupport =
			// sequenceDatabase.loadFile(fileToPath("gazelle.txt"), support);

			AlgoClaSP algorithm = new AlgoClaSP(relativeSupport, abstractionCreator, findClosedPatterns,
					executePruningMethods);

			algorithm.runAlgorithm(sequenceDatabase, keepPatterns, verbose, clustersOutputFileName,
					outputSequenceIdentifiers);

			String msg = "Minimum percentage of traces to appear in: ";
			if (isAlreadyChecked) {
				System.out.println(msg + support);
			} else {
				System.out.println(msg + support + " [" + Math.ceil(support * traces.size()) + "]");
			}

			System.out.println(algorithm.getNumberOfFrequentPatterns() + "patterns found.");

			if (verbose && keepPatterns) {
				System.out.println(algorithm.printStatistics());
			}

			// extracts traces ids from generated file
			claSPTraceIDs = getTracesIDsFromOutputFile(clustersOutputFileName);

			if (claSPTraceIDs != null) {
				return claSPTraceIDs.size();
			}

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;

	}

	public int mineClosedSequencesUsingClaSPAlgo(Collection<GraphPath> traces) {

		convertedInstancesFileName = toSPMFsequentialPatternFormat(traces);

		// Load a sequence database
		double support = 0;

		boolean keepPatterns = true;
		boolean verbose = false;
		boolean findClosedPatterns = true;
		boolean executePruningMethods = true;
		// if you set the following parameter to true, the sequence ids of the
		// sequences where
		// each pattern appears will be shown in the result
		boolean outputSequenceIdentifiers = true;

		double relativeSupport;

		int size = traces.size();
		try {

			// run several times till you find max minsup that which after there
			// will be no result

			boolean isMaxFound = false;
			int tries = traces.size() / 2 + 1;

			// binary search
			int left = 0;
			int right = traces.size() - 1;
			int mid = -1;

			while ((left <= right) & tries > 0) {

				mid = (int) Math.floor((left + right) / 2);

				support = mid * 1.0 / size;

				AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
				IdListCreator idListCreator = IdListCreatorStandard_Map.getInstance();

				SequenceDatabase sequenceDatabase = new SequenceDatabase(abstractionCreator, idListCreator);

				relativeSupport = sequenceDatabase.loadFile(convertedInstancesFileName, support);

				AlgoClaSP algo = new AlgoClaSP(relativeSupport, abstractionCreator, findClosedPatterns,
						executePruningMethods);

				algo.runAlgorithm(sequenceDatabase, keepPatterns, verbose, clustersOutputFileName,
						outputSequenceIdentifiers);
				// String[] lines =
				// FileManipulator.readFileNewLine(clustersOutputFileName);
				//
				// // if there is output, then increase minsup. Else, decrease
				// if (lines != null && lines.length > 0 && !lines[0].isEmpty())
				// {
				// left = mid + 1;
				// System.out
				// .println(">>[L] trying min-traces (i.e. minsup) " + mid + " l
				// = " + left + " r = " + right);
				// isMaxFound = true;
				// } else {
				// isMaxFound = false;
				// right = mid - 1;
				// System.out
				// .println(">>[R] trying min-traces (i.e. minsup) " + mid + " l
				// = " + left + " r = " + right);
				// }

				if (findClosedPatterns) {
					if (algo.getNumberOfFrequentClosedPatterns() > 0) {
						left = mid + 1;
						System.out.println(
								">>[L] trying min-traces (i.e. minsup) " + mid + " l = " + left + " r = " + right);
						isMaxFound = true;
					} else {
						isMaxFound = false;
						right = mid - 1;
						System.out.println(
								">>[R] trying min-traces (i.e. minsup) " + mid + " l = " + left + " r = " + right);
					}
				} else {
					if (algo.getNumberOfFrequentPatterns() > 0) {
						left = mid + 1;
						System.out.println(
								">>[L] trying min-traces (i.e. minsup) " + mid + " l = " + left + " r = " + right);
						isMaxFound = true;
					} else {
						isMaxFound = false;
						right = mid - 1;
						System.out.println(
								">>[R] trying min-traces (i.e. minsup) " + mid + " l = " + left + " r = " + right);
					}
				}

				tries--;
			}

			// if the last mid (or minimum trace/minsup) has zero patterns, then
			// decrement till a value is returned
			if (!isMaxFound) {
				while (mid > 0) {

					mid--;

					support = mid * 1.0 / size;

					AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
					IdListCreator idListCreator = IdListCreatorStandard_Map.getInstance();

					SequenceDatabase sequenceDatabase = new SequenceDatabase(abstractionCreator, idListCreator);

					relativeSupport = sequenceDatabase.loadFile(convertedInstancesFileName, support);

					AlgoClaSP algo = new AlgoClaSP(relativeSupport, abstractionCreator, findClosedPatterns,
							executePruningMethods);

					algo.runAlgorithm(sequenceDatabase, keepPatterns, verbose, clustersOutputFileName,
							outputSequenceIdentifiers);
					String[] lines = FileManipulator.readFileNewLine(clustersOutputFileName);

					// if there is output, then increase minsup. Else, decrease
					if (lines != null && lines.length > 0 && !lines[0].isEmpty()) {
						break;
					}
				}
			} // else increase mid until there's no output.
			else {
				while (mid < right) {

					mid++;

					support = mid * 1.0 / size;

					AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
					IdListCreator idListCreator = IdListCreatorStandard_Map.getInstance();

					SequenceDatabase sequenceDatabase = new SequenceDatabase(abstractionCreator, idListCreator);

					relativeSupport = sequenceDatabase.loadFile(convertedInstancesFileName, support);

					AlgoClaSP algo = new AlgoClaSP(relativeSupport, abstractionCreator, findClosedPatterns,
							executePruningMethods);

					algo.runAlgorithm(sequenceDatabase, keepPatterns, verbose, clustersOutputFileName,
							outputSequenceIdentifiers);
					String[] lines = FileManipulator.readFileNewLine(clustersOutputFileName);

					// if there is output, then increase minsup. Else, decrease
					if (lines == null || lines.length == 0 || lines[0].isEmpty()) {
						mid--;
						break;
					}
				}
			}

			System.out.println(">>Minimum traces is " + mid);

			return mineClosedSequencesUsingClaSPAlgo(traces, mid);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}

	protected List<Integer> getTracesIDsFromOutputFile(String fileName) {

		// read the file generated by sequence pattern mining algorithm

		List<Integer> traceIDs = new LinkedList<Integer>();

		String outputFile = fileName.replace(".txt", "_IDs.txt");

		String[] lines = FileManipulator.readFileNewLine(fileName);

		StringBuilder str = new StringBuilder();
		// String separator = " ";
		String fileLinSeparator = System.getProperty("line.separator");
		// traceIDs = new LinkedList<Integer>();

		str.append("trace-ID").append(fileLinSeparator);

		for (String line : lines) {
			// itemSet -1(separator between item sets) #SUP: num(number of
			// traces repeated in) #SID: ids(traces ids)
			// e.g., 36 -1 #SUP: 5 #SID: 403 404 406 409 413

			if (line.isEmpty()) {
				continue;
			}

			// get ids
			String tracesIDsSet = line.split(":")[2];

			String[] tracesIDs = tracesIDsSet.trim().split(" ");

			for (String id : tracesIDs) {
				int idInt = Integer.parseInt(id);

				if (!traceIDs.contains(idInt)) {
					traceIDs.add(idInt);
					str.append(id).append(fileLinSeparator);
				}

			}
		}

		System.out
				.println(">>Number of traces identified as relevant (Shortest & has common partial-traces or states) = "
						+ traceIDs.size());

		writeToFile(str.toString(), outputFile);

		return traceIDs;

	}

	protected void mineSequentialPatternsUsingSPADE(Collection<GraphPath> traces) {

		// convertedInstancesFileName =
		// toSPMFsequentialPatternFormat(traces);
		//
		// String outputPath = clustersOutputFileName;
		// // Load a sequence database
		// double support = 0.01;
		//
		// boolean keepPatterns = true;
		// boolean verbose = false;
		//
		// AbstractionCreator abstractionCreator =
		// AbstractionCreator_Qualitative.getInstance();
		// boolean dfs = true;
		//
		// // if you set the following parameter to true, the sequence ids of
		// the
		// // sequences where
		// // each pattern appears will be shown in the result
		// boolean outputSequenceIdentifiers = true;
		//
		// IdListCreator idListCreator = IdListCreator_FatBitmap.getInstance();
		//
		// CandidateGenerator candidateGenerator =
		// CandidateGenerator_Qualitative.getInstance();
		//
		// SequenceDatabase sequenceDatabase = new
		// SequenceDatabase(abstractionCreator, idListCreator);
		//
		// try {
		//
		// sequenceDatabase.loadFile(convertedInstancesFileName, support);
		//
		//// System.out.println(sequenceDatabase.toString());
		//
		// AlgoSPADE algorithm = new AlgoSPADE(support, dfs,
		// abstractionCreator);
		//
		// algorithm.runAlgorithm(sequenceDatabase, candidateGenerator,
		// keepPatterns, verbose, outputPath,
		// outputSequenceIdentifiers);
		// System.out.println("Minimum support (relative) = " + support);
		// System.out.println(algorithm.getNumberOfFrequentPatterns() + "
		// frequent patterns.");
		//
		// System.out.println(algorithm.printStatistics());
		//
		// // analysis of the generated sequential patterns
		// String analysisFile = clustersOutputFolder +
		// "/sequentialPatternAnalysis.txt";
		//
		// analyseGeneratedSequencePatterns(convertedInstancesFileName,
		// clustersOutputFileName, analysisFile);
		//
		// } catch (UnsupportedEncodingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	protected void mineSequentialPatternsUsingTKSAlgo(Collection<GraphPath> traces) {

		/**
		 * http://www.philippe-fournier-viger.com/spmf/TKS.php fastest Top-K
		 * sequential pattern recognition algo
		 */

		convertedInstancesFileName = toSPMFsequentialPatternFormat(traces);

		// Load a sequence database
		String input = convertedInstancesFileName;
		String output = clustersOutputFileName;

		int k = 10; // number of sequential patterns to find

		// Create an instance of the algorithm
		AlgoTKS algo = new AlgoTKS();

		// This optional parameter allows to specify the minimum pattern length:
		algo.setMinimumPatternLength(4); // optional

		// This optional parameter allows to specify the maximum pattern length:
		// algo.setMaximumPatternLength(4); // optional

		// This optional parameter allows to specify constraints that some
		// items MUST appear in the patterns found by TKS
		// E.g.: This requires that items 1 and 3 appears in every patterns
		// found
		// algo.setMustAppearItems(new int[] {63});

		// This optional parameter allows to specify the max gap between two
		// itemsets in a pattern. If set to 1, only patterns of contiguous
		// itemsets
		// will be found (no gap).
		algo.setMaxGap(1);

		// if you set the following parameter to true, the sequence ids of the
		// sequences where
		// each pattern appears will be shown in the result
		algo.showSequenceIdentifiersInOutput(true);

		// execute the algorithm, which returns some patterns
		try {

			PriorityQueue<PatternTKS> patterns = algo.runAlgorithm(input, output, k);

			// save results to file
			algo.writeResultTofile(output);
			algo.printStatistics();

			// analysis of the generated sequential patterns
			String analysisFile = clustersOutputFolder + "/sequentialPatternAnalysis.txt";

			analyseGeneratedSequencePatterns(convertedInstancesFileName, clustersOutputFileName, analysisFile);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected void analyseGeneratedSequencePatterns(String convertedInstancesFile, String patternsFile,
			String outputFile) {

		// post analysis of output generated by a sequential pattern mining
		// algorithm such as PrefixSpan

		// Create an instance of the algorithm with minsup = 50 %
		AlgoOccur algo = new AlgoOccur();

		// execute the algorithm
		try {

			File file = new File(outputFile);

			if (!file.exists()) {
				file.createNewFile();
			}

			algo.runAlgorithm(convertedInstancesFile, patternsFile, outputFile);
			algo.printStatistics();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// public static String fileToPath(String filename) throws
	// UnsupportedEncodingException {
	// URL url = IncidentInstancesClusterGenerator.class.getResource(filename);
	// System.out.println("tst " + url.toExternalForm());
	// return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	// }

	protected String toSPMFsequentialPatternFormat(Collection<GraphPath> instances) {

		// the format is as follows:
		// state-0 <space> -1 <space> state-1 ... state-n -2
		// where: -1 is a separator between states and -2 indicates the end of
		// sequence

		int stateSeparator = -1;
		int sequenceEndIndicator = -2;
		int i = 0;
		String fileLinSeparator = System.getProperty("line.separator");
		final String DATA_SEPARATOR = " ";

		StringBuilder str = new StringBuilder();

		for (GraphPath sequence : instances) {

			List<Integer> states = sequence.getStateTransitions();

			for (i = 0; i < states.size(); i++) {
				// add state
				str.append(states.get(i)).append(DATA_SEPARATOR).append(stateSeparator).append(DATA_SEPARATOR);
			}

			// add end of sequence
			str.append(sequenceEndIndicator).append(fileLinSeparator);
		}

		writeToFile(str.toString(), convertedInstancesFileName);

		return convertedInstancesFileName;
	}

	/**************************************/
	/****** FREQUENT ITEMSETS MINING *******/
	/**************************************/

	protected void mineFrequentItemsetsUsingFP_GrowthAlgo() {

		convertedInstancesFileName = toSPMFFrequentItemsetsFormat(instances.values());

		// percentage of traces in which the item set appears
		// e.g., 10% in a 100 traces means that an item set should appear in at
		// least 10 traces out of the 100
		double minsup = 0.5; // means a minsup of 2 transaction (we used a
								// relative support)

		// Applying the FPGROWTH
		AlgoFPGrowth algo = new AlgoFPGrowth();

		// Uncomment the following line to set the maximum pattern length
		// (number of items per itemset, e.g. 3 )
		// algo.setMaximumPatternLength(3);

		try {

			algo.runAlgorithm(convertedInstancesFileName, clustersOutputFileName, minsup);
			algo.printStats();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected String toSPMFFrequentItemsetsFormat(Collection<GraphPath> instances) {

		// the format is as follows:
		// state-0 <space> state-1 ... state-n

		int i = 0;
		String fileLinSeparator = System.getProperty("line.separator");
		final String DATA_SEPARATOR = " ";

		StringBuilder str = new StringBuilder();

		for (GraphPath sequence : instances) {

			List<Integer> states = sequence.getStateTransitions();

			for (i = 0; i < states.size() - 1; i++) {
				// add state
				str.append(states.get(i)).append(DATA_SEPARATOR);
			}

			// add last state
			str.append(states.get(i)).append(fileLinSeparator);
		}

		writeToFile(str.toString(), convertedInstancesFileName);

		return convertedInstancesFileName;
	}

	/**************** WEKA *********************/
	/*******************************************/
	/*******************************************/

	/********* Utilities *********/
	/**************************/
	/**************************/
	/**************************/

	/******* printers *********/
	/**************************/
	/**************************/
	/**************************/

	public void setTracesFile(String filePath) {

		// System.out.println(filePath);
		// if file path contains \, convert to /
		int tries = 10000;

		while (filePath.contains("\\") && tries > 0) {

			// remove \
			filePath = filePath.replace("\\", File.separator);

			tries--;
		}

		// System.out.println(filePath);

		instanceFileName = filePath;

		if (instanceFileName == null) {
			return;
		}
		String onlyPath = "";
		String onlyName = "";
		boolean isFolderCreated = false;

		int index = instanceFileName.lastIndexOf(File.separator);

		if (index >= 0) {
			onlyPath = instanceFileName.substring(0, instanceFileName.lastIndexOf(File.separator) + 1);
			onlyName = instanceFileName.substring(instanceFileName.lastIndexOf(File.separator) + 1,
					instanceFileName.lastIndexOf("."));
			// set folder
			outputFolder = onlyPath + "miningOutput" + File.separator;

			// make sure output folder is created
			if (outputFolder != null && !outputFolder.isEmpty()) {
				File out = new File(outputFolder);

				if (!out.isDirectory()) {
					isFolderCreated = out.mkdir();
				} else {
					isFolderCreated = true;
				}
			}

		}

		// create output files in output folder
		if (outputFolder != null && !outputFolder.isEmpty() && isFolderCreated) {
			clustersOutputFileName = outputFolder + onlyName + "_relevantTraces.txt";// instanceFileName.replace(".json",
																						// "_relevantTraces.txt");
			// clustersOutputFileName;
			convertedInstancesFileName = outputFolder + onlyName + "_convertedInstances.txt";
			// +
			shortestTracesFileName = outputFolder + onlyName + "_shortestTracesIDs.txt";
		}
		// store files where the traces are
		else {
			clustersOutputFileName = instanceFileName.replace(".json", "_relevantTraces.txt");
			// clustersOutputFileName;
			convertedInstancesFileName = instanceFileName.replace(".json", "_convertedInstances.txt");
			// +
			shortestTracesFileName = instanceFileName.replace(".json", "_shortestTracesIDs.txt");

		}

		// reset data
		if (instances != null) {
			instances.clear();
		}

		resetMiner();
	}

	public void resetMiner() {
		shortestTraceIDs.clear();
		claSPTraceIDs.clear();
		tracesActionsOccurence.clear();
		shortestTraces.clear();
		shortestActionsOccurence.clear();
		shortestStatesOccurence.clear();
		isLoaded = false;
	}

	public int getMinimumTraceLength() {

		if (minimumTraceLength == MIN_LENGTH_INITIAL_VALUE) {
			return 0;
		}

		return minimumTraceLength;
	}

	public int getMaximumTraceLength() {

		if (maximumTraceLength == MAX_LENGTH_INITIAL_VALUE) {
			return 0;
		}

		return maximumTraceLength;
	}

	public int getNumberOfTraces() {

		if (numberOfTraces != -1) {
			return numberOfTraces;
		} else if (instances != null) {
			numberOfTraces = instances.size();
			return numberOfTraces;
		}

		return -1;
	}

	public List<String> getTracesActions() {

		if (tracesActions != null) {
			return Arrays.asList(tracesActions.keySet().toArray(new String[tracesActions.size()]));
		}

		return null;
	}

	public Map<Integer, GraphPath> readInstantiatorInstancesFile(String fileName) {

		if (fileName == null || fileName.isEmpty()) {
			System.err.println("Error reading file: " + fileName + ". File name is empty.");
			return null;
		}

		if (!fileName.endsWith(".json")) {
			System.err.println("Error reading file: " + fileName + ". File should be in JSON format.");
			return null;
		}

		File instancesFile = new File(fileName);

		if (!instancesFile.isFile()) {
			System.err.println(fileName + " is not a file");
			return null;
		}

		Map<Integer, GraphPath> instances = new HashMap<Integer, GraphPath>();

		// int minTraceLength = 1000000;
		// int maxTraceLength = -1;
		// int currentLoadedTracesNum = 0;

		FileReader reader;
		boolean isCompactFormat = true;

		try {

			reader = new FileReader(instancesFile);

			// reading the json file and converting each instance into a
			// GraphPath object
			JSONParser parser = new JSONParser();

			// notify listener of loading json file
			if (listener != null) {
				listener.onLoadingJSONFile();
			}

			JSONObject obj = (JSONObject) parser.parse(reader);

			// check if there are instance generated
			if (obj.containsKey(JSONTerms.INSTANCE_POTENTIAL)) {
				JSONObject objInstances = (JSONObject) obj.get(JSONTerms.INSTANCE_POTENTIAL);

				if (objInstances.containsKey(JSONTerms.INSTANCE_POTENTIAL_COUNT)) {
					// get instances number
					numberOfTraces = Integer.parseInt(objInstances.get(JSONTerms.INSTANCE_POTENTIAL_COUNT).toString());

					// if there's a listener, then notify
					if (listener != null) {
						listener.onNumberOfTracesRead(numberOfTraces);
					}

				}

				// System.out.println("nuuum " + numberOfTraces);
				// check the instances again. if there are instances then read
				// them
				if (objInstances.containsKey(JSONTerms.INSTANCE_POTENTIAL_INSTANCES)) {

					// get instances
					JSONArray aryInstances = (JSONArray) objInstances.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES);

					// each instance currently has an instance_id (integer),
					// transitions (array of integers of states), and actions
					// (sequence of strings that correspond to the sequence of
					// transitions)
					// e.g., {
					// "instance_id":0,
					// "transitions":[1,64,271,937],
					// "actions":["EnterRoom","ConnectBusDevice","CollectData"]
					// }
					// this is a compact format. Another format exists in which
					// transitions are in the format
					// "transitions": [{"action": "EnterRoom", "source":
					// 1,"target": 64},

					// get transitions
					ListIterator<JSONObject> instancesList = aryInstances.listIterator();

					while (instancesList.hasNext()) {
						JSONObject instance = instancesList.next();

						// get instance id
						int instanceID = Integer
								.parseInt(instance.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_ID).toString());

						// get transitions
						JSONArray transitions = (JSONArray) instance
								.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS);

						List<Integer> states = new LinkedList<Integer>();
						List<String> actions = new LinkedList<String>();

						for (int i = 0; i < transitions.size(); i++) {

							Object objState = transitions.get(i);

							try {

								if (isCompactFormat) {
									Integer state = Integer.parseInt(objState.toString());
									// compact format
									states.add(state);
								} else {
									JSONObject objTransition = (JSONObject) objState;
									// expanded format
									// transition=[{src,trg, action}]
									Integer srcState = Integer.parseInt(objTransition
											.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS_SOURCE).toString());
									Integer tgtState = Integer.parseInt(objTransition
											.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS_TARGET).toString());
									String actionState = objTransition
											.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS_ACTION).toString();

									// add state
									if (!states.contains(srcState)) {
										states.add(srcState);
									}

									if (!states.contains(tgtState)) {
										states.add(tgtState);
									}

									// add action
									actions.add(actionState);

									// check state occurence
									if (statesOccurrences.containsKey(srcState)) {
										int oldOccurrence = statesOccurrences.get(srcState);
										oldOccurrence++;
										statesOccurrences.put(srcState, oldOccurrence);
									} else { // if not, then create a new entry
										statesOccurrences.put(srcState, 1);
									}

									// check state occurence
									if (statesOccurrences.containsKey(tgtState)) {
										int oldOccurrence = statesOccurrences.get(tgtState);
										oldOccurrence++;
										statesOccurrences.put(tgtState, oldOccurrence);
									} else { // if not, then create a new entry
										statesOccurrences.put(tgtState, 1);
									}

									// check action occurence
									if (tracesActionsOccurence.containsKey(actionState)) {
										int oldOccurrence = tracesActionsOccurence.get(actionState);
										oldOccurrence++;
										tracesActionsOccurence.put(actionState, oldOccurrence);
									} else { // if not, then create a new entry
										tracesActionsOccurence.put(actionState, 1);
									}
								}

							} catch (NumberFormatException e) {
								isCompactFormat = false;

								// to account for the lost iteration from
								// exception
								i = -1;
							}
						}

						// get actions (if compact)
						if (instance.containsKey(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS_ACTIONS)) {
							JSONArray actionsAry = (JSONArray) instance
									.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS_ACTIONS);

							for (Object objAction : actionsAry) {

								String tmp = objAction.toString();
								// System.out.println(tmp);

								actions.add(tmp);

								// add to the list of all actions
								// if (tracesActions != null &&
								// !tracesActions.contains(tmp)) {
								// // System.out.println("adding: "+tmp);
								// tracesActions.add(tmp);
								// }

								// check action occurence
								if (tracesActionsOccurence.containsKey(tmp)) {
									int oldOccurrence = tracesActionsOccurence.get(tmp);
									oldOccurrence++;
									tracesActionsOccurence.put(tmp, oldOccurrence);
								} else { // if not, then create a new entry
									tracesActionsOccurence.put(tmp, 1);
								}
							}
						}

						// create a new path/incident
						GraphPath tmpPath = new GraphPath();
						tmpPath.setInstanceID(instanceID);
						tmpPath.setStateTransitions(states);
						tmpPath.setTransitionActions(actions);

						// add to the list
						instances.put(instanceID, tmpPath);

						// notify listener
						if (listener != null) {

							if (instances.size() % tracesLoadedNotifierNumber == 0) {
								listener.onTracesLoaded(tracesLoadedNotifierNumber);
							} else if (instances.size() == numberOfTraces) {
								listener.onTracesLoaded(instances.size() % tracesLoadedNotifierNumber);
							}

						}

						if (actions.size() == 2) {
							System.out.println(instanceID + " " + actions);
						}

						int size = actions.size();

						// set min and max trace lengths
						if (minimumTraceLength > size) {
							minimumTraceLength = size;
						}

						if (maximumTraceLength < size) {
							maximumTraceLength = size;
						}

					}

					reader.close();
				}
			}
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return instances;

	}

	public int getNumberOfActions() {

		if (tracesActionsOccurence != null) {
			return tracesActionsOccurence.size();
		}

		return -1;
	}

	public int getNumberOfStates() {

		if (statesOccurrences != null) {
			return statesOccurrences.size();
		}

		return -1;
	}

	public void setListener(TraceMinerListener listener) {
		this.listener = listener;
	}

	public int getShortestTracesNumber() {

		if (shortestTraces != null) {
			return shortestTraces.size();
		}

		return -1;
	}

	public int getCustomisedTracesNumber() {

		if (customeFilteringTraceIDs != null) {
			return customeFilteringTraceIDs.size();
		}

		return -1;
	}

	public List<Integer> getTracesWithLength(String op, int length) {

		return getTracesWithLength(op, length, instances);
	}

	public List<Integer> getTracesWithLength(String op, int length, Map<Integer, GraphPath> traces) {

		List<Integer> result = new LinkedList<Integer>();

		if (length <= 0) {
			return null;
		}

		if (traces == null) {
			return null;
		}

		if (traces.isEmpty()) {
			return result;
		}

		// if less than minimum
		if (minimumTraceLength != MIN_LENGTH_INITIAL_VALUE && length < minimumTraceLength) {
			return result;
		}

		// if more than maximum
		if (maximumTraceLength != MAX_LENGTH_INITIAL_VALUE && length > maximumTraceLength) {
			return result;
		}

		switch (op) {
		case TraceViewerController.MORE_THAN_EQUAL:
			for (Entry<Integer, GraphPath> entry : traces.entrySet()) {
				int numOfActions = entry.getValue().getTransitionActions().size();

				if (numOfActions >= length) {
					result.add(entry.getKey());
				}
			}
			break;

		case TraceViewerController.LESS_THAN_EQUAL:
			for (Entry<Integer, GraphPath> entry : traces.entrySet()) {
				int numOfActions = entry.getValue().getTransitionActions().size();

				if (numOfActions <= length) {
					result.add(entry.getKey());
				}
			}
			break;

		case TraceViewerController.EQUAL:
			for (Entry<Integer, GraphPath> entry : traces.entrySet()) {
				int numOfActions = entry.getValue().getTransitionActions().size();

				if (numOfActions == length) {
					result.add(entry.getKey());
				}
			}
			break;

		default:
			break;
		}

		customeFilteringTraceIDs = result;

		return result;
	}

	public List<Integer> getTracesWithOccurrencePercentage(String op, int percentage) {

		return getTracesWithOccurrencePercentage(op, percentage, instances);

	}

	public List<Integer> getTracesWithOccurrencePercentage(String op, int percentage, Map<Integer, GraphPath> traces) {

		List<Integer> result = new LinkedList<Integer>();

		if (percentage < 0 || percentage > 100) {
			return null;
		}

		if (traces == null || instances == null) {
			return null;
		}

		if (traces.isEmpty() || percentage == 0 || instances.isEmpty()) {
			return result;
		}

		double percGiven = (percentage * 1.0 / 100);
		int index = 0;

		System.out.println("given perc: " + percGiven);
		// all actions of a trace should have a percentage that is [op, e.g.,
		// more than] the given
		switch (op) {
		case TraceViewerController.MORE_THAN_EQUAL:
			next_trace: for (Entry<Integer, GraphPath> entry : traces.entrySet()) {

				for (String action : entry.getValue().getTransitionActions()) {
					int occurrence = tracesActionsOccurence.get(action);
					double perc = (occurrence * 1.0 / instances.size());
					if (index != 100) {
						System.out.println("perc: " + perc);
						index++;
					}

					// if an action does not satisfy the criterion, then skip to
					// next trace
					if (!(perc >= percGiven)) {
						continue next_trace;
					}
				}
				result.add(entry.getKey());

			}
			break;

		case TraceViewerController.LESS_THAN_EQUAL:
			next_trace: for (Entry<Integer, GraphPath> entry : traces.entrySet()) {

				for (String action : entry.getValue().getTransitionActions()) {
					int occurrence = tracesActionsOccurence.get(action);
					int perc = (int) Math.floor((occurrence * 1.0 / instances.size()) * 100);

					if (!(perc <= percentage)) {
						continue next_trace;
					}
				}
				result.add(entry.getKey());

			}
			break;

		case TraceViewerController.EQUAL:
			next_trace: for (Entry<Integer, GraphPath> entry : traces.entrySet()) {

				for (String action : entry.getValue().getTransitionActions()) {
					int occurrence = tracesActionsOccurence.get(action);
					int perc = (int) Math.floor((occurrence * 1.0 / instances.size()) * 100);

					if (!(perc == percentage)) {
						continue next_trace;
					}
				}
				result.add(entry.getKey());

			}
			break;

		default:
			break;
		}

		customeFilteringTraceIDs = result;

		return result;
	}

	public List<Integer> getTracesWithLengthAndPerc(String lengthOp, int length, String occurOp, int perc) {

		// get traces that satisfy the length first
		List<Integer> lengthTracesIDs = getTracesWithLength(lengthOp, length);

		Map<Integer, GraphPath> lengthTraces = getTraces(lengthTracesIDs);

		// get the traces that saitsfy the perc using the traces that satisfy
		// the length
		List<Integer> result = getTracesWithOccurrencePercentage(occurOp, perc, lengthTraces);

		customeFilteringTraceIDs = result;

		return result;

	}

	public List<Integer> getTracesWithFilters(String lengthOp, int length, String occurOp, int perc, String query) {

		Map<Integer, GraphPath> traces = null;

		List<Integer> lengthTracesIDs;
		List<Integer> occurTracesIDs;
		List<Integer> queryTracesIDs;

		// check length
		lengthTracesIDs = getTracesWithLength(lengthOp, length);

		// check occurrence
		if (lengthTracesIDs != null) {
			// use result from length
			traces = getTraces(lengthTracesIDs);
			occurTracesIDs = getTracesWithOccurrencePercentage(occurOp, perc, traces);
		} else {
			// use all traces
			occurTracesIDs = getTracesWithOccurrencePercentage(occurOp, perc);
		}

		// check actions
		if (occurTracesIDs != null) {
			// if occurrence was set
			traces = getTraces(occurTracesIDs);
			queryTracesIDs = getTracesWithActions(query, traces);
		} else if (lengthTracesIDs != null) {
			// if length was set
			queryTracesIDs = getTracesWithActions(query, traces);
		} else {
			// if no length or occurrence is set
			queryTracesIDs = getTracesWithActions(query);
		}

		if (queryTracesIDs != null) {
			return queryTracesIDs;
		} else if (occurTracesIDs != null) {
			return occurTracesIDs;
		} else {
			return lengthTracesIDs;
		}

		// return tracesIDs;
	}

	public List<Integer> getTracesWithLengthAndPercAndActions(String lengthOp, int length, String occurOp, int perc,
			String query) {

		List<Integer> tracesIDs = getTracesWithLengthAndPerc(lengthOp, length, occurOp, perc);

		if (tracesIDs == null) {
			return null;
		}

		// get the traces
		Map<Integer, GraphPath> traces = getTraces(tracesIDs);

		// find traces that match the query
		List<Integer> result = getTracesWithActions(query, traces);

		return result;
	}

	public Map<Integer, GraphPath> getTraces(List<Integer> tracesIDs) {

		Map<Integer, GraphPath> result = new HashMap<Integer, GraphPath>();

		if (tracesIDs == null || instances == null) {
			return null;
		}

		if (tracesIDs.isEmpty() || instances.isEmpty()) {
			return result;
		}

		for (Integer id : tracesIDs) {

			if (instances.containsKey(id)) {
				result.put(id, instances.get(id));
			}
		}

		return result;
	}

	public List<Integer> getTracesWithActions(String query) {

		return getTracesWithActions(query, instances);
	}

	public List<Integer> getTracesWithActions(String query, Map<Integer, GraphPath> traces) {

		List<Integer> result = new LinkedList<Integer>();

		if (query == null || traces == null) {
			return null;
		}

		if (query.isEmpty() || traces.isEmpty()) {
			return result;
		}

		// create a query object
		Query queryEvaluator = new Query(query);

		// generate pattern in query
		queryEvaluator.generatePattern();

		// int actionsLength = actions.size();
		// String singleAction = "";
		// StringBuilder strBldr = new StringBuilder();

		// for(String act: actions) {
		// strBldr.append(act);
		// }

		// singleAction = strBldr.toString();

		// String regexString = Pattern.quote(singleAction);

		// Pattern pattern = Pattern.compile(regexString); //+ "(.*?)" +
		// Pattern.quote(pattern2); ^:new line

		// ==== need to update for ? *
		for (Entry<Integer, GraphPath> entry : traces.entrySet()) {

			List<String> traceActions = entry.getValue().getTransitionActions();

			if (queryEvaluator.matches(traceActions)) {
				result.add(entry.getKey());
			}
			// if the trace sequence is shorter than given actions
			// if (actions.size() > traceActions.size()) {
			// continue;
			// }

			// strBldr.setLength(0);

			// create one string of the actions in sequence from the beginning
			// for (int i = 0; i < actionsLength; i++) {
			// strBldr.append(traceActions.get(i));
			// }

			// if(pattern.matcher(strBldr.toString()).matches()) {
			// result.add(entry.getKey());
			// }
			// check if both string are equal
			// if(singleAction.equalsIgnoreCase(strBldr.toString())) {
			// result.add(entry.getKey());
			// }

			// check that all actions in the list exist in the trace
			// if (traceActions.containsAll(actions)) {
			// result.add(entry.getKey());
			// }

		}

		customeFilteringTraceIDs = result;

		return result;
	}

	public List<Integer> getShortestTracesIDs() {

		return shortestTraceIDs;
	}

	public List<Integer> getShortestClaSPTracesIDs() {

		return claSPTraceIDs;
	}

	public List<Integer> getCustomisedTracesIDs() {
		return customeFilteringTraceIDs;
	}

	public boolean saveTraces(String fileName, List<Integer> tracesIDs) {

		if (fileName == null || tracesIDs == null) {
			return false;
		}

		boolean isSaved = false;

		Map<Integer, GraphPath> traces = getTraces(tracesIDs);

		String[] dummy = new String[0];
		// dummy[0] = "dummy";
		List<GraphPath> paths = Arrays.asList(traces.values().toArray(new GraphPath[traces.size()]));

		IncidentPatternInstantiator ins = new IncidentPatternInstantiator();

		InstancesSaver tracesSaver = ins.new InstancesSaver(-1, fileName, dummy, dummy, paths);
		try {
			// ForkJoinPool mainPool = new ForkJoinPool();
			// int res = mainPool.submit(tracesSaver).get();
			int res = tracesSaver.call();

			if (res == InstancesSaver.SUCCESSFUL) {
				isSaved = true;
			} else if (res == InstancesSaver.UNSUCCESSFUL) {
				isSaved = false;

			}

			// mainPool.shutdown();

			// if it returns false then maximum waiting time is reached
			// if (!mainPool.awaitTermination(24, TimeUnit.DAYS)) {
			// System.err.println("Time out! saving instances took more than
			// specified maximum time ["
			// + 24 + " " + TimeUnit.DAYS + "]");
			// }

			if (listener != null) {
				listener.onSavingFilteredTracesComplete(isSaved);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return isSaved;
	}

	public boolean areTracesLoaded() {

		return isLoaded;
	}

//	public List<Map.Entry<String, Long>> findCommonEntities(GraphPath trace, int Occurrence) {
//
//		// finds the top (with Occurrence) in the given trace
//		List<GraphPath> traces = new LinkedList<GraphPath>();
//		traces.add(trace);
//
//		List<String> actionsEntities = convertToEntities(trace, null);
//
//		Map<String, Long> map = actionsEntities.stream().collect(Collectors.groupingBy(w -> w, Collectors.counting()));
//
//		List<Map.Entry<String, Long>> result = map.entrySet().stream()
//				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(Occurrence)
//				.collect(Collectors.toList());
//
//		// convertedInstancesFileName = convertUsingActionEntities(traces);
//
//		// generateClustersUsingTextMining();
//		//
//
//		return result;
//	}

	/**
	 * Finds common entities (i.e. Classes/Controls) between all traces. It excludes entities given in the excluding list. It limits to topK entities
	 * @param traces The list of traces to search for common entities
	 * @param excluding The list of entities to exclude
	 * @param topK The top K entities to find
	 * @return The list of top K entities in the given traces with their occurrence
	 */
	public List<Map.Entry<String, Long>> findCommonEntities(List<GraphPath> traces, List<String> excluding,
			int topK) {

		// finds the top (with Occurrence) in the given trace
		// List<GraphPath> traces = new LinkedList<GraphPath>();
		// traces.add(trace);
		List<String> actionsEntities = new LinkedList<String>();

		for (GraphPath trace : traces) {
			actionsEntities.addAll(convertToEntities(trace, excluding));
		}

		Map<String, Long> map = actionsEntities.stream().collect(Collectors.groupingBy(w -> w, Collectors.counting()));

		List<Map.Entry<String, Long>> result = map.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(topK)
				.collect(Collectors.toList());

		// convertedInstancesFileName = convertUsingActionEntities(traces);

		// generateClustersUsingTextMining();
		//

		return result;
	}

	/**
	 * converts the given trace into a list of entities that it contains
	 * @param trace
	 * @param excluding The list of entities to exclude from the list
	 * @return List of entities (Classes/Controls)
	 */
	public List<String> convertToEntities(GraphPath trace, List<String> excluding) {

		List<String> actions = trace.getTransitionActions();

		List<String> actionEntities = new LinkedList<String>();

		for (String act : actions) {

			ActionWrapper actionDetails = bigraphERActions.get(act);

			// allow repetition of entities

			if (actionDetails != null) {

				// add entities from pre
				BigraphWrapper pre = actionDetails.getPrecondition();

				if (pre != null) {
					Set<Entity> ents = pre.getControlMap().keySet();

					for (Entity ent : ents) {

						String name = ent.getName();

						if (excluding != null && excluding.contains(name)) {
							continue;
						}

						actionEntities.add(name);
					}
				}

				//add entities from post
				BigraphWrapper post = actionDetails.getPostcondition();

				if (post != null) {
					Set<Entity> ents = post.getControlMap().keySet();

					for (Entity ent : ents) {

						String name = ent.getName();

						if (excluding != null && excluding.contains(name)) {
							continue;
						}

						actionEntities.add(name);
					}
				}
			}
		}

		return actionEntities;

	}

	public void setBigraphERFile(String bigraphERFile) {

		brsParser = new BRSParser();

		bigraphERActions = brsParser.parseBigraphERFile(bigraphERFile);
	}

	public static void main(String[] args) {

		TraceMiner m = new TraceMiner();

		String bigraphERFile = "D:/Bigrapher data/lero/example/lero.big";

		m.setBigraphERFile(bigraphERFile);

		List<String> actions = new LinkedList<String>();

		actions.add("enter_room_during_working_hours");
		actions.add("disable_hvac");
		actions.add("connect_to_hvac");

		GraphPath p = new GraphPath();

		p.setTransitionActions(actions);

		List<GraphPath> traces = new LinkedList<GraphPath>();

		traces.add(p);

		//finds the top k
		int topK = 10;
		
		//exclude terms from this list
		List<String> excluding = new LinkedList<String>();
		
		excluding.add("RulesKeywords");
		
		List<Map.Entry<String, Long>> ents = m.findCommonEntities(traces, excluding, topK);

		System.out.println(ents);
	}

}
