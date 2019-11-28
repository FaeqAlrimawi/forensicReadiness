package core.monitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MonitorSolution {

	// solution id
	protected int solutionID;

	// map in which the key is the action name, and value is the monitor that can
	// monitor that action
	protected Map<String, Monitor> actionMonitors;

	// solution cost
	int cost;

	public MonitorSolution() {
		actionMonitors = new HashMap<String, Monitor>();
	}

	public int getSolutionID() {
		return solutionID;
	}

	public void setSolutionID(int solutionID) {
		this.solutionID = solutionID;
	}

	public Map<String, Monitor> getActionMonitors() {
		return actionMonitors;
	}

	public void setActionMonitors(Map<String, Monitor> actionMonitors) {
		this.actionMonitors = actionMonitors;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public void addActionMonitor(String actionName, Monitor monitor) {

		if (actionName == null || actionName.isEmpty()) {
			return;
		}

		actionMonitors.put(actionName, monitor);
	}

	public boolean removeActionMonitor(String actionName) {

		if (actionName == null || actionName.isEmpty()) {
			return false;
		}

		if (actionMonitors.containsKey(actionName)) {
			actionMonitors.remove(actionName);
			return true;
		}

		return false;
	}

	public String toString() {

		StringBuilder bldr = new StringBuilder();

		String newLine = System.getProperty("line.separator");

		// id and cost
		bldr.append("Solution ID: ").append(solutionID).append(" Cost: ").append(cost).append(newLine).append(newLine);

		// solution (e.g., *Action: action1 ^Monitor: monitor1 (cost: 43)
		bldr.append("{MonitorID ==> ActionName (cost)}").append(newLine);

		for (Entry<String, Monitor> entry : actionMonitors.entrySet()) {
			String actionName = entry.getKey();
			Monitor monitor = entry.getValue();

			bldr.append(monitor.getMonitorID()).append(" ==> ").append(actionName).append(" (")
					.append(monitor.getCost()).append(")").append(newLine);
		}

		return bldr.toString();
	}

	public void print() {

		String solStr = toString();

		System.out.println("=====================================================");

		System.out.println(solStr);

		System.out.println("=====================================================");
	}

}
