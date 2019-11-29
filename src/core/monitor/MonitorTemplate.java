package core.monitor;

public class MonitorTemplate {

//	VISITOR_ENTER_ROOM("CCTV", "Room", "VisitorEnterRoom",
//			"Hallway{hallway}.(id | CCTV{ipNet}) | Room{hallway}.(Visitor.id)");
	
	
	// type of monitor
	String type;

	// target to monitor
	String targetType;

	// expression that indicates what it can monitor. The expression is BigraphER
	// expression
	String monitoringExpression;

	// the action that it can monitor
	String actionMonitored;

	public MonitorTemplate(String type, String targetType, String actionMonitored, String monitoringExpression) {
		this.type = type;
		this.targetType = targetType;
		this.monitoringExpression = monitoringExpression;
		this.actionMonitored = actionMonitored;
	}

	public MonitorTemplate(String type, String actionMonitored, String monitoringExpression) {
		this.type = type;
		this.monitoringExpression = monitoringExpression;
		this.actionMonitored = actionMonitored;
	}

	public MonitorTemplate(String type, String monitoringExpression) {
		this.type = type;
		this.monitoringExpression = monitoringExpression;
	}

	public MonitorTemplate(String type) {
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
