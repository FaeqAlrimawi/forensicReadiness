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

		Monitor mon = new Monitor();

		mon.setTraceMiner(miner);
		mon.setBigraphERStatment(postActionBig);
		mon.setMonitorType(monitorType);
		mon.setActionMonitored(actionMonitored);

//		Bigraph big = mon.generateBigraph();
//
//		System.out.println(big);

		// update the action reactum with asset id for the room
		// first update the reacutm with MonitorTarget tag
		postActionBig = "Hallway{hallway}.id | Room{hallway}.(Visitor.id |" + MonitorTerms.MONITOR_TARGET_ASSET
				+ " | id)";

		mon.setBigraphERStatment(postActionBig);

		// update with asset id
//		String assetID = "Room1";
//		mon.canMonitor(assetID, 0, 1);

//		Bigraph big = mon.generateBigraph();

//		System.out.println(big);

		// update reacutm with monitor info. the monitor type can be enriched with any
		// connections needed by checking the signature for the type to get any
		// outernames
		postActionBig = "Hallway{hallway}.(id |" + mon.getMonitorType() + "{ipNet}) | Room{hallway}.(Visitor.id |"
				+ MonitorTerms.MONITOR_TARGET_ASSET + " | id)";

		mon.setBigraphERStatment(postActionBig);

//		big = mon.generateBigraph();
//
//		System.out.println(big);

//		String targetAssetID = "Office_T24";
//		
		int preState = 1;
		int postState = 238;

		boolean isMonitorable = mon.canMonitor(preState, postState);
//		
		if (isMonitorable) {
			System.out.println("Yes, can monitor...");
		} else {
			System.out.println("NO, cannot monitor...");
		}

//		 big = mon.generateBigraph();
//
//		System.out.println("\n\n"+big);

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
