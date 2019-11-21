package core.monitor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import core.brs.parser.BigraphWrapper;
import core.brs.parser.utilities.JSONTerms;
import core.instantiation.analysis.TraceMiner;
import cyberPhysical_Incident.CyberPhysicalIncidentFactory;
import cyberPhysical_Incident.Entity;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Control;
import it.uniud.mads.jlibbig.core.std.Signature;
import it.uniud.mads.jlibbig.core.std.SignatureBuilder;

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

	// a string that is used to identify the target control in the given
	// stateToMonitor
	String targetTypeIdentificationName = null;

	// a string that is used to identify the Asset Id control in the given
	// stateToMonitor
	String targetTypeAssetIDIdentificationName = null;

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

	// original state string
	String originalStateToMonitor;

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

	public void setTraceMiner(TraceMiner traceMinor) {

		miner = traceMinor;
	}

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

		if (bigStmt == null) {
			return;
		}

		if (stateToMonitor == null) {
			stateToMonitor = new BigraphWrapper();
		}

		originalStateToMonitor = bigStmt;

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
		if (!checkTraceMiner()) {
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
	 * 
	 * @param bigStmt
	 * @return
	 */
	public Bigraph generateBigraph(String bigStmt) {

		// trace miner is needed
		if (!checkTraceMiner()) {
			return null;
		}

		if (stateToMonitor == null) {
			System.err.println("BigraphER Wrapper instance is missing.");
			return null;
		}

		Bigraph res = null;

		// parses the given statement
		stateToMonitor.parseBigraphERCondition(bigStmt);

		res = stateToMonitor.createBigraph(false, miner.getSignature());

		return res;
	}

	/**
	 * Generates a Bigraph object based on set bigraphER statement
	 * 
	 * @return Bigraph object if successful, null otherwise
	 */
	public Bigraph generateBigraph() {

		// trace miner is needed
		if (!checkTraceMiner()) {
			return null;
		}

		if (stateToMonitor == null) {
			System.err.println("BigraphER Wrapper instance is missing.");
			return null;
		}

		Bigraph res = null;

		Signature sig = updateSignatureWithMonitorControls();

		res = stateToMonitor.createBigraph(false, sig);

		return res;
	}

	/**
	 * Updates the signature found in trace miner with monitor controls if they do
	 * not exist
	 */
	public Signature updateSignatureWithMonitorControls() {

		if (!checkTraceMiner()) {
			return null;
		}

		Signature sig = miner.getSignature();

		// if the monitor control are not added then add them to the sig by creating a
		// new one

		List<String> missingMonitorControls = new LinkedList<String>();

		for (String monitorCtrl : MonitorTerms.MONITOR_TERMS) {
			if (!sig.contains(monitorCtrl)) {
				missingMonitorControls.add(monitorCtrl);
			}
		}

		// there are missing controls for monitoring
		if (!missingMonitorControls.isEmpty()) {
			SignatureBuilder sigBldr = new SignatureBuilder();

			Iterator<Control> it = sig.iterator();
			while (it.hasNext()) {
				Control ctrl = it.next();
				sigBldr.add(ctrl);
			}

			// add monitor controls
			for (String monitorCtrl : MonitorTerms.MONITOR_TERMS) {
				sigBldr.add(monitorCtrl, false, 0);
			}

			//add asset ref, if anny
			if(targetEntityRef !=null && !sig.contains(targetEntityRef)) {
				sigBldr.add(targetEntityRef, false, 0);
			}
			
			// create new sig
			sig = sigBldr.makeSignature();
		}

		return sig;
	}

	/**
	 * Checks whether the given ID for the target asset can by monitored or not.
	 */
	public boolean canMonitorTargetAssetWithID(String assetID, int preState, int postState) {

		if (assetID == null || assetID.isEmpty()) {
			return false;
		}

		if (!checkTraceMiner()) {
			return false;
		}

		// look for the control that contains the target
		// then add the asset id to the control by adding "AssetID.{assetID}"

		if (stateToMonitor == null) {
			return false;
		}

		CyberPhysicalIncidentFactory instance = CyberPhysicalIncidentFactory.eINSTANCE;

		if (!findTragetAssetIDUniqueName()) {
			return false;
		}

		// if not found then create a new one
		if (targetTypeAssetIDIdentificationName == null) {

			Random rand = new Random();
			int tries = 10000;
			String newID = null;

			// create new id
			while (tries > 0) {
				int id = rand.nextInt(10000);

				newID = JSONTerms.CONTROL_ASSET_ID + "" + id;

				// check if it exists
				if (stateToMonitor.getControl(newID) == null) {
					break;
				}

				tries--;
			}

			if (newID == null) {
				System.err.println("Could not create a new Asset ID.");
				return false;
			}

			targetTypeAssetIDIdentificationName = newID;

			// add new id to map of controls
			Entity newEnt = instance.createEntity();
			newEnt.setName(JSONTerms.CONTROL_ASSET_ID);
			
			stateToMonitor.addControl(newEnt, newID);

			// add to target
			stateToMonitor.addContainedEntity(targetTypeIdentificationName, newID);

		} // If it exists, then update info
		else {
			// previous ID, if any
			List<String> prevContainedEnts = stateToMonitor.getContainedEntitiesMap()
					.get(targetTypeAssetIDIdentificationName);

			// remove from the map of controls
			if (prevContainedEnts != null && prevContainedEnts.size() > 0) {
				String prevID = prevContainedEnts.get(0);

				Entity prevEntity = null;

				for (Entry<Entity, String> entry : stateToMonitor.getControlMap().entrySet()) {

					String id = entry.getValue();

					if (id.equalsIgnoreCase(prevID)) {
						prevEntity = entry.getKey();
						break;
					}
				}

				if (prevEntity != null) {
//					System.out.println("removing... " + prevEntity.getName());
					stateToMonitor.getControlMap().remove(prevEntity);
				}

//				System.out.println("removing contianed entities of... " + targetTypeAssetIDIdentificationName);
				stateToMonitor.getContainedEntitiesMap().put(targetTypeAssetIDIdentificationName, new LinkedList<String>());
				
//				System.out.println("removing... " + prevID);
				stateToMonitor.getEntities().remove(prevID);
			}
		}

		// update to given asset id
		// set contained entities of the AssetID control
		List<String> containedEnts = new LinkedList<String>();
		containedEnts.add(assetID);

		Entity ent = instance.createEntity();

		ent.setName(assetID);

		// update the map of all entities
		stateToMonitor.addControl(ent, assetID);

		// update AssetID contained entities
		stateToMonitor.addContainedEntity(targetTypeAssetIDIdentificationName, assetID);
			
		targetEntityRef = assetID;
		
		return canMonitor(preState, postState);

//		return false;
	}

	protected boolean findTragetAssetIDUniqueName() {
		
		// identify the unique name of the target tag in the wrapper
		if (targetTypeIdentificationName == null) {
			for (String entityID : stateToMonitor.getControlMap().values()) {
				String control = stateToMonitor.getControl(entityID);
				if (control.equalsIgnoreCase(MonitorTerms.MONITOR_TARGET_ASSET)) {
					//then the parent of that entity is the target id
					String parentID = stateToMonitor.getContainerEntitiesMap().get(entityID);
					targetTypeIdentificationName = parentID;
					break;
				}
			}

			if (targetTypeIdentificationName == null) {
				System.err.println("\"" + MonitorTerms.MONITOR_TARGET_ASSET + "\" tag NOT found");
				return false;
			}
		}

		// try to find the id of the AssetID control of the target
		if (targetTypeAssetIDIdentificationName == null) {
			// get parent entity id
			String parentEntityID = stateToMonitor.getContainerEntitiesMap().get(targetTypeIdentificationName);

			if (parentEntityID == null) {
				return false;
			}

			// get contained entities in parent
			// then identify the AssetID control, if available. If not, then add it to the
			// parent and to the map of all entities
			List<String> containedEntitiesIDs = stateToMonitor.getContainedEntitiesMap().get(parentEntityID);

			if (containedEntitiesIDs != null) {
				for (String containedEntityID : containedEntitiesIDs) {
					String cntrl = stateToMonitor.getControl(containedEntityID);

					if (cntrl != null) {
						if (cntrl.equalsIgnoreCase(JSONTerms.CONTROL_ASSET_ID)) {
							targetTypeAssetIDIdentificationName = containedEntityID;
							break;
						}
					}
				}
			}
		}

		return true;
	}

	protected boolean checkTraceMiner() {

		if (miner == null) {
			System.err.println("Trace miner instance is missing.");
			return false;
		}

		return true;
	}
}
