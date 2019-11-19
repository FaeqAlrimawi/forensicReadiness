package core.monitor;

import core.brs.parser.BigraphWrapper;
import core.instantiation.analysis.TraceMiner;
import it.uniud.mads.jlibbig.core.std.Bigraph;

public class Monitor {

	// monitor type (or Class) (required)
	String monitorType = "CCTV";

	// monitor asset i.e. reference to an asset in the system model
	// if NULL, then all assets of monitor Type are monitors
	String monitorAssetRef = null;

	// action to monitor (required)
	String actionMonitored = "VisitorEnterRoom";

	// target type (or class) to monitor (required)
	String targetTypeMonitored = "Server";

	// target asset to monitor i.e. specific entity
	// if NULL, then all entities of target Type are monitored
	String targetEntityRef = null;

	// data to collect if need to monitor
	// data represents what pieces of information need to be collected by the
	// monitor in order to create a "record" of the monitoring instance
	String dataToCollect;

	// cost of monitoring
	// can include the cost to operate the monitor (keep the monitor on),
	// cost of collecting data (info from the system)
	double cost;

	/**
	 * (required) what system state(s) that need to be monitored this determines
	 * what partial-state of the system needs to exist in order to be able to
	 * monitor the action and/or target it includes: monitor type/asset, and target
	 * type/asset currently 1-change per monitor
	 */
	BigraphWrapper stateToMonitor;

	// =====other attributes

	// state of the monitor
	// states: Monitoring, Idle, Off, Unknown
	String monitorState = "MONITORING";

	// monitoring type: how monitoring takes place (e.g., always monitoring i.e.
	// collecting data about its target and action)
	// monitoring: ALWAYS, OnMATCH, OnMATCHandAFTER, OnMATCHunitlNORMAL
	String monitoringType = "ALWAYS";

	// ======trace miner, which can have information about the states
	TraceMiner miner;

	public String getMonitorType() {
		return monitorType;
	}

	public void setMonitorType(String monitorType) {
		this.monitorType = monitorType;
	}

	public String getMonitorAssetRef() {
		return monitorAssetRef;
	}

	public void setMonitorAssetRef(String monitorAssetRef) {
		this.monitorAssetRef = monitorAssetRef;
	}

	public String getActionMonitored() {
		return actionMonitored;
	}

	public void setActionMonitored(String actionMonitored) {
		this.actionMonitored = actionMonitored;
	}

	public String getTargetTypeMonitored() {
		return targetTypeMonitored;
	}

	public void setTargetTypeMonitored(String targetTypeMonitored) {
		this.targetTypeMonitored = targetTypeMonitored;
	}

	public String getTargetEntityRef() {
		return targetEntityRef;
	}

	public void setTargetEntityRef(String targetEntityRef) {
		this.targetEntityRef = targetEntityRef;
	}

	public String getDataToCollect() {
		return dataToCollect;
	}

	public void setDataToCollect(String dataToCollect) {
		this.dataToCollect = dataToCollect;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public BigraphWrapper getSystemStateToMonitor() {
		return stateToMonitor;
	}

	public void setSystemStateToMonitor(BigraphWrapper systemStateToMonitor) {
		this.stateToMonitor = systemStateToMonitor;
	}

	public String getMonitorState() {
		return monitorState;
	}

	public void setMonitorState(String monitorState) {
		this.monitorState = monitorState;
	}

	public String getMonitoringType() {
		return monitoringType;
	}

	public void setMonitoringType(String monitoringType) {
		this.monitoringType = monitoringType;
	}

	public TraceMiner getMiner() {
		return miner;
	}

	public void setMiner(TraceMiner miner) {
		this.miner = miner;
	}

	public void setBigraphERStatment(String bigStmt) {

		if(bigStmt == null) {
			return;
		}
		
		if (stateToMonitor == null) {
			stateToMonitor = new BigraphWrapper();
		}

		stateToMonitor.parseBigraphERCondition(bigStmt);
	}

	/**
	 * ============= ============= MAIN METHODS =============
	 */

	// ========= Method to assess if this monitor can monitor the given actions and
	// its per & post system states
	// ===canMonitor():Integer. A main functionality is that to determine if this
	// monitor can monitor a given states. To determine this is implemented by
	// matching two states to the [systemStateToMonitor]. The two states are usually
	// (should be?) the pre and post states (of the system) that satisfy the
	// [action] of this monitor.
	// the result is determined by comparing the number of times the
	// [systemStateToMonitor] is matched in the pre and post states.
	// result > 0: indicates it can monitor.
	// result <= 0: indicates it cannot monitor the action
	// the result is then converted to Boolean (true if >0, false otherwise)

	public boolean canMonitor(int preState, int postState) {

//		boolean canMonitor = false;

		// action is needed
		if (actionMonitored == null || actionMonitored.isEmpty()) {
			System.err.println("There's no action specified to monitor.");
			return false;
		}

		// trace miner is needed
		if (miner == null) {
			System.err.println("Trace miner instance is missing.");
			return false;
		}

		// state to monitor is needed
		if (stateToMonitor == null) {
			System.err.println("No state to monitor is found.");
			return false;
		}

		// from the action we can identify the pre and post states in a given trace. So
		// providing them as parameters may not be needed

		int diff = miner.getNumberOfBigraphMatches(stateToMonitor.getBigraphObject(), preState, postState);

		// if there's an error
		if (diff == TraceMiner.ACTIONS_CAUSAL_DEPENDENCY_ERROR) {
			return false;
		}

		// if the number of times the given state to monitor is more in the post than in
		// the pre, then we consider that the monitor can monitor the change between the
		// two states
		if (diff > 0) {
			return true;
		}

		// else it cannot

		return false;
	}

	// ========= Method to identify specific parts of a Bigraph
	// ===

	/**
	 * Converts the given bigraph statement into a Bigraph Object
	 * @param bigStmt
	 * @return
	 */
	public Bigraph generateBigraph(String bigStmt) {

		//trace miner is needed
		if (miner == null) {
			System.err.println("Trace miner instance is missing.");
			return null;
		}
		
		if (stateToMonitor == null) {
			System.err.println("BigraphER Wrapper instance is missing.");
			return null;
		}
		
		Bigraph res = null;
	
		//parses the given statement
		stateToMonitor.parseBigraphERCondition(bigStmt);
		
		res = stateToMonitor.createBigraph(false, miner.getSignature());
		
		return res;
	}
	
	/**
	 * Generates a Bigraph object based on set bigraphER statement
	 * @return Bigraph object if successful, null otherwise
	 */
	public Bigraph generateBigraph() {

		//trace miner is needed
		if (miner == null) {
			System.err.println("Trace miner instance is missing.");
			return null;
		}
		
		if (stateToMonitor == null) {
			System.err.println("BigraphER Wrapper instance is missing.");
			return null;
		}
		
		Bigraph res = null;
		
		res = stateToMonitor.createBigraph(false, miner.getSignature());
		
		return res;
	}

}
