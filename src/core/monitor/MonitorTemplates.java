package core.monitor;

public enum MonitorTemplates {

	VISITOR_ENTER_ROOM("CCTV", "Room", "VisitorEnterRoom",
			"Hallway{hallway}.(id | CCTV{ipNet}) | Room{hallway}.(Visitor.id)");
	//=== need other types

	// type of monitor
	String type;

	// target to monitor
	String targetType;

	// expression that indicates what it can monitor. The expression is BigraphER
	// expression
	String monitoringExpression;

	// the action that it can monitor
	String actionMonitored;

	MonitorTemplates(String type, String targetType, String actionMonitored, String monitoringExpression) {
		this.type = type;
		this.targetType = targetType;
		this.monitoringExpression = monitoringExpression;
		this.actionMonitored = actionMonitored;
	}

	MonitorTemplates(String type, String actionMonitored, String monitoringExpression) {
		this.type = type;
		this.monitoringExpression = monitoringExpression;
		this.actionMonitored = actionMonitored;
	}

	MonitorTemplates(String type, String monitoringExpression) {
		this.type = type;
		this.monitoringExpression = monitoringExpression;
	}

	MonitorTemplates(String type) {
		this.type = type;
	}

	String getType() {
		return type;
	}

	String getTargetType() {
		return targetType;
	}

	String getMonitoringExpression() {
		return monitoringExpression;
	}

	String getActionMonitored() {
		return actionMonitored;
	}
}
