package core.monitor;

public class MonitorTemplate {

	/**
	 * Creates a Monitor object with the given type
	 * 
	 * @param monType The type of monitor to create
	 * @return A Monitor object
	 */
	public static Monitor createMonitor(MonitorType monType) {

		if (monType == null) {
			return null;
		}

		Monitor mon = new Monitor();

		mon.setMonitorType(monType.getType());
		mon.setTargetType(monType.getTargetType());
		mon.setActionMonitored(monType.getActionMonitored());
		mon.setBigraphERStatment(monType.getMonitoringExpression());

		return mon;
	}

}
