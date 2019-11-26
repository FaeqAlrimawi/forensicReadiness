package core.monitor;

public class MonitorTemplateFactory {

	/**
	 * Creates a Monitor object with the given type
	 * 
	 * @param monType The type of monitor to create
	 * @return A Monitor object
	 */
	public static Monitor createMonitor(MonitorTemplates monitorTemplate) {

		if (monitorTemplate == null) {
			return null;
		}

		Monitor mon = new Monitor();

		mon.setMonitorType(monitorTemplate.getType());
		mon.setTargetType(monitorTemplate.getTargetType());
		mon.setActionMonitored(monitorTemplate.getActionMonitored());
		mon.setBigraphERStatment(monitorTemplate.getMonitoringExpression());

		return mon;
	}

}
