package core.monitor.test;

import java.net.URL;

import core.instantiation.analysis.TraceMiner;
import core.monitor.MonitorManager;
import core.monitor.MonitorTemplate;
import core.monitor.MonitorTemplateFactory;

public class MonitorTester {

	TraceMiner miner;

	public void setTraceMiner(TraceMiner traceMiner) {
		miner = traceMiner;
	}

	/**
	 * A functionality to test how to identify which part of a system is being
	 * affected by an action One way to do this is by instantiating the action with
	 * IDs. Add IDs to the controls/classes of the action redex
	 * 
	 * @param args
	 */
	protected void testIdentificationOfMatchID() {

		if (miner == null) {
			System.err.println("TraceMiner object is null");
			return;
		}

		// === Add monitor templates to Factory (optional)
		String monitorTempType = "MotionSensor";
		String actionToMonitor = "Move";
		String monitorExpression = "Room{con}.Visitor | Room.{con}.MotionSensor";

		MonitorTemplate temp = new MonitorTemplate(monitorTempType, actionToMonitor, monitorExpression);

		MonitorTemplateFactory instance = MonitorTemplateFactory.eInstance;

		instance.addTemplate(temp);

		// === create a monitor manager which can be used to find monitors
		MonitorManager mngr = new MonitorManager();

//		mngr.addMonitor(mon);

		// or you can just load monitors defined by the factory
		mngr.loadFactoryMonitors();
		mngr.setTraceMiner(miner);

		mngr.printMonitors();

		// ===test if the monitor can monitor the specified action pre and post states
		// in a trace
		String actionMonitored = "VisitorEnterRoom";
		String targetAssetID = "Office_T24";

		int preState = 1;
		int postState = 237;

		/*
		 * === This checks whether the monitor with the given ID (monitorID) can monitor
		 * the target with the given ID (targetAssetID) when the action takes place
		 * through the pre and post states. can monitor is evaluated by checking whether
		 * the monitor's partial-state is satisfied in the post-state more than in the
		 * pre-state.
		 */
//		boolean isMonitorable = mon.canMonitor(monitorID, targetAssetID, preState, postState);

		/*
		 * === This checks whether the monitor with the given ID (monitorID) can monitor
		 * an asset with the given target type, when the action takes place.
		 */
//		boolean isMonitorable = mon.canMonitor(monitorID, null, preState, postState);

		/*
		 * === This checks if the monitor can monitor the target with the given ID
		 * (targetAssetID), when the action takes place.
		 */
//		boolean isMonitorable = mon.canMonitor(targetAssetID, preState, postState);

		/*
		 * === This checks if the monitor can monitor the set target, when the action
		 * takes place.
		 */
//		boolean isMonitorable = mon.canMonitor(targetAssetID, preState, postState);

		int monitorResult = mngr.canMonitor(actionMonitored, targetAssetID, preState, postState);

		switch (monitorResult) {
		case MonitorManager.CAN_MONITOR:
			System.out.println("Yes, can monitor...");
			break;

		case MonitorManager.CANNOT_MONITOR:
			System.out.println("NO, cannot monitor...");
			break;

		case MonitorManager.NO_MONITORS_AVAILABLE:
			System.out.println("NO monitors available to monitor the given action...");
			break;

		case MonitorManager.UNDETERMINED:
			System.out.println("Cannot determine...");
			break;

		case MonitorManager.ERROR:
			System.out.println("Error occurred");
			break;

		default:
			break;
		}

	}

	public static void main(String[] args) {

		String ltsLocationStr = "resources/example/states";
		String ltsLocationExternalStr = "resources/example/states5K_reduced";

		String bigFileStr = "resources/example/systemBigraphER.big";
		String bigFileExternalStr = "resources/example/lero_uniqueAssetID.big";

		URL ltsLocation = MonitorTester.class.getClassLoader().getResource(ltsLocationExternalStr);
		URL bigFileLocation = MonitorTester.class.getClassLoader().getResource(bigFileExternalStr);

		String LTS = ltsLocationExternalStr;
		String bigFile = bigFileExternalStr;

		// LTS
		if (ltsLocation != null) {
			LTS = ltsLocation.getPath();
		} else {
			System.err.println("LTS location is not found");
			return;
		}

		// bigrapher file
		if (bigFileLocation != null) {
			bigFile = bigFileLocation.getPath();
		} else {
			System.err.println("Bigapher file is not found");
			return;
		}

		TraceMiner miner = new TraceMiner();

		miner.setBigraphERFile(bigFile);
		miner.setStatesFolder(LTS);

		MonitorTester tester = new MonitorTester();

		tester.setTraceMiner(miner);

//		tester.testReactionRuleMatching();

		tester.testIdentificationOfMatchID();
	}

}
