package core.monitor;

import java.util.LinkedList;
import java.util.List;

import core.brs.parser.utilities.JSONTerms;

public class MonitorTerms {
	
	
	//indicates the target control in a given bigraph e.g., Server.Target
	public static final String MONITOR_TARGET_ASSET = "MonitorTarget";
	
	public static final List<String> MONITOR_TERMS = new LinkedList<String>(){{
		add(MONITOR_TARGET_ASSET);
		add(JSONTerms.CONTROL_ASSET_ID);}
	};
	
	public static final List<String> MONITOR_TERMS_TO_IGNORE = new LinkedList<String>(){{
		add(MONITOR_TARGET_ASSET);
		}
	};

}
