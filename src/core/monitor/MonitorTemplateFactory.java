package core.monitor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cyberPhysical_Incident.BigraphExpression;

public class MonitorTemplateFactory {

	public static final MonitorTemplateFactory eInstance = new MonitorTemplateFactory();

	protected Map<String, MonitorTemplate> templates;

	protected MonitorTemplateFactory() {
		createTemplates();
	}

	protected void createTemplates() {

		templates = new HashMap<String, MonitorTemplate>();

		// create templates
		String monitorType = null;
		String targetType = null;
		String action = null;
		String stateToMonitor = null;

		// === visitor enter room template
		monitorType = "CCTV";
		targetType = "Room";
		action = "VisitorEnterRoom";
		stateToMonitor = "Hallway{hallway}.(id | CCTV{ipNet}) | Room{hallway}.(Visitor.id)";

		createTemplate(monitorType, action, targetType, stateToMonitor);

		// monitor data sent to a bus network.
		// Monitor type is DigitalProcess
		// the monitor can monitor the busnetwork if it can get a copy of the data
		// received by the busnetwork, then analyse it
		monitorType = "DigitalProcess";
		targetType = "BusNetwork";
		action = "SendData";
		stateToMonitor = "BusNetwor{bus}.Data | DigitalProcess{bus}.Data";

		createTemplate(monitorType, action, targetType, stateToMonitor);

	}

	/**
	 * Creates a new monitor template with the given parameters.
	 * 
	 * @param monitorType     The type of the monitor e.g., CCTV
	 * @param actionMonitored the action name the monitor template can monitor
	 * @param stateToMonitor  and the state to monitor expressed as a BigraphER
	 *                        expression
	 * @return {@value TemplateID} if the new template is created. {@value Null} if
	 *         the new template could not be created.
	 */
	public String createTemplate(String monitorType, String actionMonitored, String stateToMonitor) {

		return createTemplate(monitorType, actionMonitored, null, stateToMonitor, 0);
	}

	/**
	 * Creates a new monitor template with the given parameters.
	 * 
	 * @param monitorType     The type of the monitor e.g., CCTV
	 * @param actionMonitored the action name the monitor template can monitor
	 * @param targetType      the target type to monitor e.g., Room
	 * @param stateToMonitor  and the state to monitor expressed as a BigraphER
	 *                        expression
	 * @return {@value TemplateID} if the new template is created. {@value Null} if
	 *         the new template could not be created.
	 */
	public String createTemplate(String monitorType, String actionMonitored, String targetType, String stateToMonitor) {

		return createTemplate(monitorType, actionMonitored, targetType, stateToMonitor, 0);
	}

	/**
	 * Creates a new monitor template with the given parameters.
	 * 
	 * @param monitorType     The type of the monitor e.g., CCTV
	 * @param actionMonitored the action name the monitor template can monitor
	 * @param targetType      the target type to monitor e.g., Room
	 * @param stateToMonitor  and the state to monitor expressed as a BigraphER
	 *                        expression
	 * @param cost            The cost of monitoring
	 * @return {@value TemplateID} if the new template is created. {@value Null} if
	 *         the new template could not be created.
	 */
	public String createTemplate(String monitorType, String actionMonitored, String targetType, String stateToMonitor,
			double cost) {

		int tries = 100;
		String templateID = null;

		// ==create unique template id
		while (tries > 0) {
			templateID = createUniqueTemplateName(-1);

			if (templateID != null) {
				break;
			}

			tries--;
		}

		if (templateID == null) {
			return null;
		}

		MonitorTemplate monitorTemplate = new MonitorTemplate(templateID, monitorType, actionMonitored, targetType,
				stateToMonitor, 0);

		templates.put(templateID, monitorTemplate);

		return templateID;
	}

	protected String createUniqueTemplateName(int upperBound) {

		// create name
		Random rand = new Random();
		String name = null;
		int tries = 1000;

		int max = 100000;

		if (upperBound < 0) {
			upperBound = max;
		}

		while (tries > 0) {
			name = "MT-" + rand.nextInt(upperBound);

			if (!templates.containsKey(name)) {
				break;
			}
		}

		return name;
	}

	public List<String> getAvailableTemplateNames() {

		return new LinkedList<String>(templates.keySet());
	}

	public Map<String, Monitor> createAllMonitors() {

		Map<String, Monitor> monitors = new HashMap<String, Monitor>();

		for (String monName : templates.keySet()) {
			Monitor mon = createMonitor(monName);

			monitors.put(monName, mon);
		}

		return monitors;
	}

	/**
	 * Creates a Monitor object with the given monitor template name
	 * 
	 * @param monType The type of monitor to create
	 * @return A Monitor object
	 */
	public Monitor createMonitor(String templateName) {

		if (templateName == null || !templates.containsKey(templateName)) {
			return null;
		}

		MonitorTemplate monitorTemplate = templates.get(templateName);

		Monitor mon = new Monitor();

		mon.setMonitorType(monitorTemplate.getType());
		mon.setTargetType(monitorTemplate.getTargetType());
		mon.setActionMonitored(monitorTemplate.getActionMonitored());
		mon.setBigraphERStatment(monitorTemplate.getBigraphERMonitoringExpression());
		mon.setCost(monitorTemplate.getCost());

		return mon;
	}

	/**
	 * Creates a Monitor object with the given monitor template name and monitor ID
	 * 
	 * @param monType The type of monitor to create
	 * @param id      The monitor ID, which can be an asset name in a system
	 * @return A Monitor object
	 */
	public Monitor createMonitor(String templateName, String id) {

		if (templateName == null || !templates.containsKey(templateName)) {
			return null;
		}

		MonitorTemplate monitorTemplate = templates.get(templateName);

		Monitor mon = new Monitor();

		mon.setMonitorID(id);
		mon.setMonitorType(monitorTemplate.getType());
		mon.setTargetType(monitorTemplate.getTargetType());
		mon.setActionMonitored(monitorTemplate.getActionMonitored());
		mon.setBigraphERStatment(monitorTemplate.getBigraphERMonitoringExpression());

		return mon;
	}
}

class MonitorTemplate {

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

	// monitor ID
	String monitorTemplateID;

	protected MonitorTemplate(String monitorTemplateID, String type, String actionMonitored, String targetType,
			String monitoringExpression, double cost) {
		this.type = type;
		this.targetType = targetType;
		this.bigraphERmonitoringExpression = monitoringExpression;
		this.actionMonitored = actionMonitored;
		this.cost = cost;
		this.monitorTemplateID = monitorTemplateID;
	}

//	protected MonitorTemplate(String type, String actionMonitored, String targetType, String monitoringExpression,
//			double cost) {
//		this.type = type;
//		this.targetType = targetType;
//		this.bigraphERmonitoringExpression = monitoringExpression;
//		this.actionMonitored = actionMonitored;
//		this.cost = cost;
//	}

//	protected MonitorTemplate(String type, String actionMonitored, String targetType, String monitoringExpression) {
//		this(type, actionMonitored, targetType, monitoringExpression, 0);
//	}

//	public MonitorTemplate(String type, String actionMonitored, String targetType,
//			BigraphExpression monitoringExpression) {
//		this.type = type;
//		this.targetType = targetType;
//		this.ownMonitoringExpression = monitoringExpression;
//		this.actionMonitored = actionMonitored;
//	}

//	protected MonitorTemplate(String type, String actionMonitored, String monitoringExpression) {
//
//		this(type, actionMonitored, null, monitoringExpression, 0);
//	}

//	public MonitorTemplate(String type, String monitoringExpression) {
//		this.type = type;
//		this.bigraphERmonitoringExpression = monitoringExpression;
//	}

//	public MonitorTemplate(String type) {
//		this.type = type;
//	}

	protected String getType() {
		return type;
	}

	protected String getTargetType() {
		return targetType;
	}

	protected String getBigraphERMonitoringExpression() {
		return bigraphERmonitoringExpression;
	}

	protected String getActionMonitored() {
		return actionMonitored;
	}

//	protected  BigraphExpression getOwnMonitoringExpression() {
//		return ownMonitoringExpression;
//	}

	protected double getCost() {
		return cost;
	}

}
