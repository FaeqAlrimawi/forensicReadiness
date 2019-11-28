package core.monitor;

import java.util.HashMap;
import java.util.Map;

public class MonitorSolution {
	
	
	//solution id
	protected int solutionID;
	
	//map in which the key is the action name, and value is the monitor that can monitor that action
	protected Map<String, Monitor> actionMonitors;
	
	//solution cost
	int cost;
	
	public MonitorSolution() {
		actionMonitors = new HashMap<String, Monitor>();
	}
	
	
	

}
