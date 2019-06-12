package core;

import java.util.LinkedList;
import java.util.List;

public class ForensicReadinessAnalyser {

	// in this class, methods for identifying [relevant] actions and components
	// to an incident pattern (or patterns) that should be monitored in order to
	// facilitate the investigation of incidents that might be similar to the
	// pattern

	private TracesMiner tracesMiner;
	private String tracesFilePath;
	private String LTsFolder;
	private String systemModelFilePath;

	public ForensicReadinessAnalyser() {
		tracesMiner = new TracesMiner();
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
	 * @return the list of all common action names 
	 */
	public List<String> getCommonActionsForAllTraces() {

		List<String> actions = new LinkedList<String>();

		return actions;
	}

	/**
	 * Returns most common Components in all traces
	 * @return the list of all common action names 
	 */
	public List<String> getCommonComponentsForAllTraces() {
		
		List<String> components = new LinkedList<String>();

		return components;
	}
	
}
