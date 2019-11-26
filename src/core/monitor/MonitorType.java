package core.monitor;

public enum MonitorType {

	VISITOR_ENTER_ROOM("CCTV", "VisitorEnterRoom", "Hallway{hallway}.(id | CCTV{ipNet}) | Room{hallway}.(Visitor.id)");
	// need other types

	// type of monitor
	String type;

	// expression that indicates what it can monitor. The expression is BigraphER
	// expression
	String monitoringExpression;

	// the action that it can monitor
	String actionMonitored;

	MonitorType(String type, String actionMonitored, String monitoringExpression) {
		this.type = type;
		this.monitoringExpression = monitoringExpression;
		this.actionMonitored = actionMonitored;
	}

	MonitorType(String type, String monitoringExpression) {
		this.type = type;
		this.monitoringExpression = monitoringExpression;
	}

	MonitorType(String type) {
		this.type = type;
	}

	String getType() {
		return type;
	}

	String getMonitoringExpression() {
		return monitoringExpression;
	}

	String getActionMonitored() {
		return actionMonitored;
	}
}
