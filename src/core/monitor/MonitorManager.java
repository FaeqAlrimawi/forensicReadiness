package core.monitor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import environment.EnvironmentDiagram;

public class MonitorManager {

	// list of monitors in the system
	// the string represents the ID of the monitor in the system model
//	protected static List<String> monitorsList = new LinkedList<String>();

	// holds all available monitors
	// key is monitor ID (unique name), and value is a Monitor object
	protected Map<String, Monitor> monitors;

	// system model
	// can be used to fill the monitors list
	EnvironmentDiagram systemModel;

	// values for canMonitor method
	public static final int CAN_MONITOR = 1;
	public static final int UNDETERMINED = 0;
	public static final int CANNOT_MONITOR = -1;
	public static final int NO_MONITORS_AVAILABLE = -2;

	public MonitorManager() {

		monitors = new HashMap<String, Monitor>();
	}

	public void setSystemModel(EnvironmentDiagram sysModel) {

		systemModel = sysModel;
	}

	public EnvironmentDiagram getSystemModel() {
		return systemModel;
	}

	public boolean addMonitor(Monitor monitor) {

		if (monitor == null) {
			return false;
		}

		String monitorID = monitor.getMonitorID();

		if (monitorID == null) {
			return false;
		}

		if (!monitors.containsKey(monitorID)) {
			monitors.put(monitorID, monitor);
		}

		return true;
	}

	public boolean removeMonitor(String monitorID) {

		if (monitorID == null) {
			return false;
		}

		if (monitors.containsKey(monitorID)) {
			monitors.remove(monitorID);
			return true;
		}

		return false;
	}

	public boolean hasMonitors() {

		return !monitors.isEmpty();
	}

	public boolean hasMonitor(String monitorID) {

		return monitors.containsKey(monitorID);
	}

	/**
	 * Returns a copy of the list of the monitors IDs available
	 * 
	 * @return A copy of the list of monitor IDs
	 */
	public List<String> getMonitorsIDs() {

		if (monitors != null) {
			return new LinkedList<String>(monitors.keySet());
		}

		return null;
	}

	public void extractMonitorsFromSystemModel(EnvironmentDiagram sysModel) {

		if (sysModel == null) {
			return;
		}

		systemModel = sysModel;

		// TBI...
		// == this method should look for monitors in the system model and add them to
		// the list of current models

	}

	/**
	 * Determines whether the monitor with the given ID can monitor the given action
	 * and assetID in the change (pre and post states)
	 * 
	 * @param monitorId Monitor ID that is required to monitor the given action and
	 *                  asset in the change
	 * @param action    Action to monitor
	 * @param assetID   target asset ID to monitor
	 * @param preState  the state of the system BEFORE the action takes place
	 * @param postState the state of the system AFTER the action takes place
	 * @return The list of monitors that are capable of monitoring the given action,
	 *         asset, and change
	 */
	public int canMonitor(String monitorID, String action, String assetID, int preState, int postState) {

		// if there are no monitors then return no monitors available
		if (monitors == null || monitors.isEmpty()) {
			return NO_MONITORS_AVAILABLE;
		}

		// if the monitor id is null then check all monitors
		if (monitorID == null) {
			for (Monitor monitor : monitors.values()) {

				// if the monitor can monitor the given action then, check the states
				if (action.equals(monitor.getActionMonitored())) {
					boolean canMon = monitor.canMonitor(assetID, preState, postState);

					if (canMon) {
						return CAN_MONITOR;
					}
				}

			}
		}
		// else, check if the monitor that has the given ID can monitor the given
		// action, asset, and change
		else {
			Monitor mon = monitors.get(monitorID);

			// if monitor is not found
			if (mon == null) {
				return UNDETERMINED;
			}

			// check if the monitor can monitor
			if (action.equals(mon.getActionMonitored())) {
				boolean canMon = mon.canMonitor(assetID, preState, postState);

				if (canMon) {
					return CAN_MONITOR;
				}
			}
		}

		return CANNOT_MONITOR;
	}

	/**
	 * Determines whether the given action and assetID in the change (pre and post
	 * states) can be monitored by at least 1 monitor
	 * 
	 * @param action    Action to monitor
	 * @param assetID   target asset ID to monitor
	 * @param preState  the state of the system BEFORE the action takes place
	 * @param postState the state of the system AFTER the action takes place
	 * @return The list of monitors that are capable of monitoring the given action,
	 *         asset, and change
	 */
	public int canMonitor(String action, String assetID, int preState, int postState) {

		// if there are no monitors then return no monitors available
		return canMonitor(null, action, assetID, preState, postState);
	}

	public int canMonitor(String action, int preState, int postState) {

		return canMonitor(null, action, null, preState, postState);

	}

	/**
	 * Returns all Monitors that can monitor the given action and assetID in the
	 * change (pre and post states)
	 * 
	 * @param action    Action to monitor
	 * @param assetID   target asset ID to monitor
	 * @param preState  the state of the system BEFORE the action takes place
	 * @param postState the state of the system AFTER the action takes place
	 * @return The list of monitors that are capable of monitoring the given action,
	 *         asset, and change
	 */
	public List<Monitor> getCapableMonitors(String action, String assetID, int preState, int postState) {

		List<Monitor> capableMonitors = new LinkedList<Monitor>();

		for (Monitor monitor : monitors.values()) {

			// if the monitor can monitor the given action then, check the states
			if (action.equals(monitor.getActionMonitored())) {
				boolean canMon = monitor.canMonitor(assetID, preState, postState);

				if (canMon) {
					capableMonitors.add(monitor);
				}
			}

		}

		return capableMonitors;

	}

	/**
	 * Returns all Monitors that can monitor the given action in the change (pre and
	 * post states)
	 * 
	 * @param action    Action to monitor
	 * @param assetID   target asset ID to monitor
	 * @param preState  the state of the system BEFORE the action takes place
	 * @param postState the state of the system AFTER the action takes place
	 * @return The list of monitors that are capable of monitoring the given action
	 *         and change
	 */
	public List<Monitor> getCapableMonitors(String action, int preState, int postState) {

		return getCapableMonitors(action, null, preState, postState);

	}
}
