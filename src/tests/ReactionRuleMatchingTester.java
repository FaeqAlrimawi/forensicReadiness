package tests;

import java.net.URL;

import core.instantiation.analysis.TraceMiner;
import core.monitor.Monitor;
import core.monitor.MonitorTerms;
import it.uniud.mads.jlibbig.core.std.Bigraph;

public class ReactionRuleMatchingTester {

	TraceMiner miner;

	public void setTraceMiner(TraceMiner traceMiner) {
		miner = traceMiner;
	}

	/**
	 * Tests the number of times the redex is matched in the pre state and the
	 * number of times it is matched in the post state
	 */
	protected void testReactionRuleMatching() {

		if (miner == null) {
			System.err.println("TraceMiner object is null");
			return;
		}

		String action = "EmployeeEnterRoom";
		int preState = 11;
		int postState = 63;
		boolean isRedex = true;

		int diff = miner.getNumberOfRedexMatches(action, preState, postState, !isRedex);

		System.out.println("Difference: " + diff);

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

		// reactum of enter room action
		String actionMonitored = "VisitorEnterRoom";
		String postActionBig = "Hallway{hallway}.id | Room{hallway}.(Visitor.id | id)";

		String monitorType = "CCTV";
		String targetType = "Room";

		Monitor mon = new Monitor();

		// ===set attributes of monitor
		mon.setMonitorType(monitorType);
		mon.setTargetType(targetType);
		mon.setActionMonitored(actionMonitored);
		mon.setTraceMiner(miner);

		/*===Important: set the partial-state, which the monitor can monitor
		 *=== This partial-state can be annotated with [monitor tags] such as
		 *Monitor_Tag, which is used to indicate the monitor in the partial-state by
		 *containing the tag. More tags are available in the MonitorTerms class
		 */
		postActionBig = "Hallway{hallway}.(id |" + mon.getMonitorType() + "{ipNet}) | Room{hallway}.(Visitor.id )";

		System.out.println("\nBigraphER Statement: [" + postActionBig + "]\n");

		mon.setBigraphERStatment(postActionBig);

		//===test if the monitor can monitor the specified action pre and post states
		// in a trace
		String targetAssetID = "Office_T24";
		String monitorID = "CCTV1";

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
		 * === This checks a monitor with the given type can monitor the target with the
		 * given ID (targetAssetID), when the action takes place.
		 */
//		boolean isMonitorable = mon.canMonitor(null, targetAssetID, preState, postState);

		/*
		 * === This checks a monitor with the given type can monitor the target with the
		 * given ID (targetAssetID), when the action takes place.
		 */
		boolean isMonitorable = mon.canMonitor(null, null, preState, postState);

		if (isMonitorable) {
			System.out.println("Yes, can monitor...");
		} else {
			System.out.println("NO, cannot monitor...");
		}

	}

	public static void main(String[] args) {

		String ltsLocationStr = "resources/example/states";
		String ltsLocationExternalStr = "resources/example/states5K_reduced";

		String bigFileStr = "resources/example/systemBigraphER.big";
		String bigFileExternalStr = "resources/example/lero_uniqueAssetID.big";

		URL ltsLocation = ReactionRuleMatchingTester.class.getClassLoader().getResource(ltsLocationExternalStr);
		URL bigFileLocation = ReactionRuleMatchingTester.class.getClassLoader().getResource(bigFileExternalStr);

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

		ReactionRuleMatchingTester tester = new ReactionRuleMatchingTester();

		tester.setTraceMiner(miner);

//		tester.testReactionRuleMatching();

		tester.testIdentificationOfMatchID();
	}

}
