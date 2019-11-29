package core.monitor;

public class MonitorTemplateFactory {

	public static final  MonitorTemplateFactory  eInstance = new MonitorTemplateFactory();
	
	/**
	 * Creates a Monitor object with the given type
	 * 
	 * @param monType The type of monitor to create
	 * @return A Monitor object
	 */
	public Monitor createMonitor(MonitorTemplate monitorTemplate) {

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

	/**
	 * Creates a Monitor object with the given type
	 * 
	 * @param monType The type of monitor to create
	 * @param id The monitor ID, which can be an asset name in a system
	 * @return A Monitor object
	 */
	public Monitor createMonitor(MonitorTemplate monitorTemplate, String id) {

		if (monitorTemplate == null) {
			return null;
		}

		Monitor mon = new Monitor();

		mon.setMonitorID(id);
		mon.setMonitorType(monitorTemplate.getType());
		mon.setTargetType(monitorTemplate.getTargetType());
		mon.setActionMonitored(monitorTemplate.getActionMonitored());
		mon.setBigraphERStatment(monitorTemplate.getMonitoringExpression());

		return mon;
	}
}
