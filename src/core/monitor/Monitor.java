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

	// a string that is used to identify the target control in the given
	// stateToMonitor
	String monitorTypeIdentificationName = null;

	// a string that is used to identify the Asset Id control in the given
	// stateToMonitor
	String monitorTypeAssetIDIdentificationName = null;

	// action to monitor (required)
	String actionMonitored = "VisitorEnterRoom";

	// target type (or class) to monitor (required)
	String targetType = "Server";

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

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
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

		boolean isMonitorTagAvailable = findMonitorAssetIDUniqueName();
		boolean isTargetTagAvailable = findTragetAssetIDUniqueName();

		if (!isMonitorTagAvailable) {
			System.out.println("*Monitor Warning! Monitor tag is not found in the given bigraphER statement");
		}

		if (!isTargetTagAvailable) {
			System.out.println("*Monitor Warning! Target tag is not found in the given bigraphER statement");
		}
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

		Signature sig = updateSignatureWithMonitorControls();

		Bigraph big = stateToMonitor.createBigraph(false, sig);

		System.out.println(big);

		int diff = miner.getNumberOfBigraphMatches(big, preState, postState);

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

	/**
	 * Checks whether the monitor, with the given ID, can monitor the asset, with
	 * the given ID if the monitor ID is NULL, then the monitor will be general
	 * (i.e. Just monitor by type) if the asset ID (or the target ID) is NULL, then
	 * the monitor will monitor any asset with a type matching the type of the
	 * target
	 */
	public boolean canMonitor(String monitorID, String assetID, int preState, int postState) {

		if (!checkTraceMiner()) {
			return false;
		}

		if (stateToMonitor == null) {
			return false;
		}

		// === update monitor id, if given

		// identify the unique name of the target

		if (monitorID != null && !findMonitorAssetIDUniqueName()) {
			System.out.println("*Monitor Warning! Missing a Monitor Tag. The given Monitor ID [" + monitorID
					+ "] will be ignored.");
//			return false;
		}

		// update monitor id
		monitorTypeAssetIDIdentificationName = updateEntityID(monitorID, monitorTypeIdentificationName,
				monitorTypeAssetIDIdentificationName);

		monitorAssetRef = monitorID;

		// === update asset id, if given
		if (assetID != null && !findTragetAssetIDUniqueName()) {
			System.out.println(
					"*Monitor Warning! Missing a Target Tag. The given Asset ID [" + assetID + "] will be ignored.\n");
//			return false;
		}

		// update target id
		targetTypeAssetIDIdentificationName = updateEntityID(assetID, targetTypeIdentificationName,
				targetTypeAssetIDIdentificationName);

		targetEntityRef = assetID;

		return canMonitor(preState, postState);
	}

	/**
	 * Updates the given entityAssetID with the given entityID. if the entityID is
	 * NULL then the current id is removed
	 * 
	 * @param entityID
	 * @param entityAssetIDIdentificationName
	 * @return
	 */
	protected String updateEntityID(String entityID, String entityTypeIdentificationName,
			String entityAssetIDIdentificationName) {

		CyberPhysicalIncidentFactory instance = CyberPhysicalIncidentFactory.eINSTANCE;

		// if not found then create a new one
		if (entityAssetIDIdentificationName == null) {

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
				return null;
			}

			entityAssetIDIdentificationName = newID;

			// add new id to map of controls
			Entity newEnt = instance.createEntity();
			newEnt.setName(JSONTerms.CONTROL_ASSET_ID);

			stateToMonitor.addControl(newEnt, newID);

			// add to target
			stateToMonitor.addContainedEntity(entityTypeIdentificationName, newID);

		} // If it exists, then update info
		else {
			// previous ID, if any
			List<String> prevContainedEnts = stateToMonitor.getContainedEntitiesMap()
					.get(entityAssetIDIdentificationName);

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

//				System.out.println("removing contained entities of... " + targetTypeAssetIDIdentificationName);
				stateToMonitor.getContainedEntitiesMap().put(entityAssetIDIdentificationName, new LinkedList<String>());

//				System.out.println("removing... " + prevID);
				stateToMonitor.getEntities().remove(prevID);
			}
		}

		// update to given asset id
		// set contained entities of the AssetID control
		if (entityID != null) {
			List<String> containedEnts = new LinkedList<String>();
			containedEnts.add(entityID);

			Entity ent = instance.createEntity();

			ent.setName(entityID);

			// update the map of all entities
			stateToMonitor.addControl(ent, entityID);

			// update AssetID contained entities
			stateToMonitor.addContainedEntity(entityAssetIDIdentificationName, entityID);
		}

		return entityAssetIDIdentificationName;

	}

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

			// add asset ref, if anny
			if (targetEntityRef != null && !sig.contains(targetEntityRef)) {
				sigBldr.add(targetEntityRef, false, 0);
			}

			// add asset ref, if anny
			if (monitorAssetRef != null && !sig.contains(monitorAssetRef)) {
				sigBldr.add(monitorAssetRef, false, 0);
			}

			// create new sig
			sig = sigBldr.makeSignature();
		}

		return sig;
	}

	/**
	 * Finds the unique name for the target type and also the unique name for its
	 * AssetID control
	 * 
	 * @return false if target monitor tag is missing. True otherwise.
	 */
	protected boolean findTragetAssetIDUniqueName() {

		// identify the unique name of the target tag in the wrapper
		if (targetTypeIdentificationName == null) {
			for (String entityID : stateToMonitor.getControlMap().values()) {
				String control = stateToMonitor.getControl(entityID);
				if (control.equalsIgnoreCase(MonitorTerms.TAG_MONITOR_TARGET)) {
					// then the parent of that entity is the target id
					String parentID = stateToMonitor.getContainerEntitiesMap().get(entityID);
					targetTypeIdentificationName = parentID;
					break;
				}
			}

			if (targetTypeIdentificationName == null) {

				boolean isUnique = true;

				if (targetType != null) {
					for (Entry<Entity, String> entry : stateToMonitor.getControlMap().entrySet()) {
						String uniqueName = entry.getValue();
						String control = entry.getKey() != null ? entry.getKey().getName() : null;

						if (control != null && control.equalsIgnoreCase(targetType)) {

							if (targetTypeIdentificationName == null) {
								targetTypeIdentificationName = uniqueName;
							} else {
								// if found again then break and return that the monitor could not be identified
								isUnique = false;
								break;
							}
						}
					}
				}

				// System.err.println("\"" + MonitorTerms.TAG_MONITOR + "\" tag NOT found");
				if (!isUnique) {
					return false;
				}

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

	/**
	 * find the unique name of the monitor in the bigraph
	 * 
	 * @return false if Monitor tag is missing. True otherwise
	 */
	protected boolean findMonitorAssetIDUniqueName() {

		// identify the unique name of the target tag in the wrapper

		// try to find the monitor in the bigraph by looking for a monitor tag
		if (monitorTypeIdentificationName == null) {
			for (String entityID : stateToMonitor.getControlMap().values()) {
				String control = stateToMonitor.getControl(entityID);
				if (control.equalsIgnoreCase(MonitorTerms.TAG_MONITOR)) {
					// then the parent of that entity is the target id
					String parentID = stateToMonitor.getContainerEntitiesMap().get(entityID);
					monitorTypeIdentificationName = parentID;
					break;
				}
			}

			// if a monitor tag is not found, then identify the monitor by finding only one
			// control with the type of the monitor
			if (monitorTypeIdentificationName == null) {

				boolean isUnique = true;

				if (monitorType != null) {
					for (Entry<Entity, String> entry : stateToMonitor.getControlMap().entrySet()) {
						String uniqueName = entry.getValue();
						String control = entry.getKey() != null ? entry.getKey().getName() : null;

						if (control != null && control.equalsIgnoreCase(monitorType)) {

							if (monitorTypeIdentificationName == null) {
								monitorTypeIdentificationName = uniqueName;
							} else {
								// if found again then break and return that the monitor could not be identified
								isUnique = false;
								break;
							}
						}
					}
				}
				// System.err.println("\"" + MonitorTerms.TAG_MONITOR + "\" tag NOT found");
				if (!isUnique) {
					return false;
				}

			}
		}

		// try to find the id of the AssetID control of the target
		if (monitorTypeAssetIDIdentificationName == null) {
			// get parent entity id
			String parentEntityID = stateToMonitor.getContainerEntitiesMap().get(monitorTypeIdentificationName);

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
							monitorTypeAssetIDIdentificationName = containedEntityID;
							break;
						}
					}
				}
			}
		}

		return true;
	}

	/**
	 * Returns the unique name of the AssetID control for the given [asset ID]
	 * 
	 * @param assetID
	 * @return
	 */
	protected String findAssetIDUniqueName(String assetID) {

		if (stateToMonitor == null) {
			return null;
		}

		String assetIDParent = null;

		for (String entityID : stateToMonitor.getControlMap().values()) {
			String control = stateToMonitor.getControl(entityID);
			if (control.equalsIgnoreCase(assetID)) {
				// then the parent of that entity is the target id
				assetIDParent = stateToMonitor.getContainerEntitiesMap().get(entityID);
				break;
			}
		}

		return assetIDParent;
	}

	protected boolean checkTraceMiner() {

		if (miner == null) {
			System.err.println("Trace miner instance is missing.");
			return false;
		}

		return true;
	}
}
