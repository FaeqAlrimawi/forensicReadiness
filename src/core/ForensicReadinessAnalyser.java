package core;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import core.instantiation.analysis.TraceMiner;

public class ForensicReadinessAnalyser {

	// in this class, methods for identifying [relevant] actions and components
	// to an incident pattern (or patterns) that should be monitored in order to
	// facilitate the investigation of incidents that might be similar to the
	// pattern

	private TraceMiner tracesMiner;

	// ===Inputs
	// traces files (PATH/*.json)
	private String tracesFilePath;
	// LTS folder (contains states [PATH/*.json] and transition
	// [PATH/transitions.json]
	private String LTsFolder;
	// System model file (PATH/*.)
	private String systemModelFilePath;

	public ForensicReadinessAnalyser() {
		tracesMiner = new TraceMiner();
	}

	public ForensicReadinessAnalyser(String tracesFilePath, String LTSfolder, String systemModelFilePath) {
		this();
		this.tracesFilePath = tracesFilePath;

		tracesMiner.setTracesFile(tracesFilePath);

		LTsFolder = LTSfolder;

		this.systemModelFilePath = systemModelFilePath;

	}

	/**
	 * Returns most common Actions in all traces
	 * 
	 * @return the list of all common action names
	 */
	public List<String> getCommonActionsForAllTraces() {

		List<String> actions = new LinkedList<String>();

		Map<String, Integer> actionsOccur = tracesMiner.getHighestActionOccurrence();

		if (actionsOccur == null) {
			return null;
		}

		actions.addAll(actionsOccur.keySet());

		return actions;
	}

	/**
	 * Returns most common Components in all traces
	 * 
	 * @return the list of all common action names
	 */
	public List<String> getCommonComponentsForAllTraces() {

		List<String> components = new LinkedList<String>();

		return components;
	}
	
	public void setTracesFile(String newTracesFile) {
		this.tracesFilePath = newTracesFile;
		
		tracesMiner.setTracesFile(tracesFilePath);
	
	}
	
	public boolean loadTraces() {
		
		return loadTraces(tracesFilePath);
	}
	
	public boolean loadTraces(String tracesFilePath) {
		
		if(tracesFilePath == null || tracesFilePath.isEmpty()) {
			return false;
		}
		
		tracesMiner.setTracesFile(tracesFilePath);
		
		int result = tracesMiner.loadTracesFromFile();
		
		if(result == TraceMiner.TRACES_NOT_LOADED) {
			return false;
		}
		
		return true;
	}

}
