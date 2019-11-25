package core.monitor;

import java.util.LinkedList;
import java.util.List;

public class MonitorManager {

	//list of monitors in the system
	//the string represents the ID of the monitor in the system model
	protected static List<String> monitorsList = new LinkedList<String>();
	
	public static boolean addMonitor(String monitorID) {
		
		if(monitorID == null) {
			return false;
		}
		
		if(!monitorsList.contains(monitorID)) {
			monitorsList.add(monitorID);
		}
		
		return true;
	}
	
	public static boolean removeMonitor(String monitorID) {
		
		if(monitorID == null) {
			return false;
		}
		
		if(monitorsList.contains(monitorID)) {
			monitorsList.remove(monitorID);
			return true;
		}
		
		return false;
	}
	
	public static boolean hasMonitors() {
		
		return !monitorsList.isEmpty();
	}
	
	public static boolean hasMonitor(String monitorID) {
		
		return monitorsList.contains(monitorID);
	}
	
	/**
	 * Returns a copy of the list of the monitors IDs available
	 * @return A copy of the list of monitor IDs
	 */
	public static List<String> getMonitorsList() {
		
		if(monitorsList!=null) {
			return new LinkedList<String>(monitorsList);
		}
		
		return null;
	}
	
	
	
}
