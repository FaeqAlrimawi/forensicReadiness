package core.monitor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MonitorTemplateFactory {

	public static final MonitorTemplateFactory eInstance = new MonitorTemplateFactory();

	protected Map<String, MonitorTemplate> templates;

	protected MonitorTemplateFactory() {
		createTemplates();
	}

	protected void createTemplates() {

		templates = new HashMap<String, MonitorTemplate>();

		// create templates

		// === visitor enter room template
		MonitorTemplate temp = new MonitorTemplate("CCTV", "Room", "VisitorEnterRoom",
				"Hallway{hallway}.(id | CCTV{ipNet}) | Room{hallway}.(Visitor.id)");

		templates.put("VisitorEnterRoom", temp);

	} 

	public boolean addTemplate(MonitorTemplate template) {

		if (template == null) {
			return false;
		}

		// create name
		Random rand = new Random();
		String name = null;
		int tries = 1000;

		while (tries > 0) {
			name = "monitor-Template-" + rand.nextInt(10000);

			if (!templates.containsKey(name)) {
				break;
			}
		}

		if (name == null) {
			return false;
		}

		templates.put(name, template);

		return true;
	}

	public List<String> getAvailableTemplateNames() {

		return new LinkedList<String>(templates.keySet());
	}

	public Map<String, Monitor> createAllMonitors() {
		
		Map<String, Monitor> monitors = new HashMap<String, Monitor>();
		
		for(String monName : templates.keySet()) {
			Monitor mon = createMonitor(monName);
			
			monitors.put(monName, mon);
		}
		
		return monitors;
	}
	
	/**
	 * Creates a Monitor object with the given type
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
		mon.setBigraphERStatment(monitorTemplate.getMonitoringExpression());

		return mon;
	}

	/**
	 * Creates a Monitor object with the given type
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
		mon.setBigraphERStatment(monitorTemplate.getMonitoringExpression());

		return mon;
	}
}
