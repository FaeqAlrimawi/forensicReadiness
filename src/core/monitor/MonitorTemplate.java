package core.monitor;

import cyberPhysical_Incident.BigraphExpression;

public class MonitorTemplate {

//	VISITOR_ENTER_ROOM("CCTV", "Room", "VisitorEnterRoom",
//			"Hallway{hallway}.(id | CCTV{ipNet}) | Room{hallway}.(Visitor.id)");

	// type of monitor
	String type;

	// target to monitor
	String targetType;

	// expression that indicates what it can monitor. The expression is BigraphER
	// expression
	String bigraphERmonitoringExpression;

	BigraphExpression ownMonitoringExpression;

	// the action that it can monitor
	String actionMonitored;

	// cost
	double cost;

	public MonitorTemplate(String type, String actionMonitored, String targetType, String monitoringExpression,
			double cost) {
		this.type = type;
		this.targetType = targetType;
		this.bigraphERmonitoringExpression = monitoringExpression;
		this.actionMonitored = actionMonitored;
		this.cost = cost;
	}

	public MonitorTemplate(String type, String actionMonitored, String targetType, String monitoringExpression) {
		this(type, actionMonitored, targetType, monitoringExpression, 0);
	}

//	public MonitorTemplate(String type, String actionMonitored, String targetType,
//			BigraphExpression monitoringExpression) {
//		this.type = type;
//		this.targetType = targetType;
//		this.ownMonitoringExpression = monitoringExpression;
//		this.actionMonitored = actionMonitored;
//	}

	public MonitorTemplate(String type, String actionMonitored, String monitoringExpression) {

		this(type, actionMonitored, null, monitoringExpression, 0);
	}

//	public MonitorTemplate(String type, String monitoringExpression) {
//		this.type = type;
//		this.bigraphERmonitoringExpression = monitoringExpression;
//	}

//	public MonitorTemplate(String type) {
//		this.type = type;
//	}

	String getType() {
		return type;
	}

	String getTargetType() {
		return targetType;
	}

	String getBigraphERMonitoringExpression() {
		return bigraphERmonitoringExpression;
	}

	String getActionMonitored() {
		return actionMonitored;
	}

	public BigraphExpression getOwnMonitoringExpression() {
		return ownMonitoringExpression;
	}
	
	public double getCost() {
		return cost;
	}

}
