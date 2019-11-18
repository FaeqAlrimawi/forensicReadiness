package tests;

import java.net.URL;

import core.instantiation.analysis.TraceMiner;

public class ReactionRuleMatchingTester {
	
	
	/**
	 * Tests the number of times the redex is matched in the pre state and the number of times it is matched in the post state 
	 */
	protected void testReactionRuleMatching() {
		
		
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
		
		//LTS
		if(ltsLocation != null) {
			LTS = ltsLocation.getPath();
		} else {
			System.err.println("LTS location is not found");
			return;
		}
		
		
		//bigrapher file
		if(bigFileLocation != null) {
			bigFile = bigFileLocation.getPath();
		} else {
			System.err.println("Bigapher file is not found");
			return;
		}
		
		TraceMiner miner = new TraceMiner();
		
		miner.setBigraphERFile(bigFile);
		miner.setStatesFolder(LTS);
		
		String action = "EmployeeEnterRoom";
		int preState = 11;
		int postState = 63;
		boolean isRedex = true;

		
		int diff = miner.getNumberOfRedexMatches(action, preState, postState, !isRedex);
		
		System.out.println("Difference: " + diff);
		
	}

}
