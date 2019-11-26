package core.monitor;

public class MonitorTemplate {

	/**
	 * Creates a Monitor object with the given type
	 * 
	 * @param monType The type of monitor to create
	 * @return A Monitor object
	 */
	public Monitor createMonitor(MonitorType monType) {

		Monitor mon = new Monitor();

		mon.setMonitorType(monType.getType());
		mon.setActionMonitored(monType.getActionMonitored());
		mon.setBigraphERStatment(monType.getMonitoringExpression());

		return mon;
	}

}
