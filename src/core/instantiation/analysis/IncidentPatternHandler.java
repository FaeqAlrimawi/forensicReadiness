package core.instantiation.analysis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import core.brs.parser.BigraphWrapper;
import core.instantiation.analysis.utilities.IncidentModelHandler;
import core.instantiation.analysis.utilities.SystemModelHandler;
import cyberPhysical_Incident.Activity;
import cyberPhysical_Incident.BigraphExpression;
import cyberPhysical_Incident.Condition;
import cyberPhysical_Incident.Entity;
import cyberPhysical_Incident.IncidentDiagram;
import cyberPhysical_Incident.Precondition;
import environment.Asset;
import environment.EnvironmentDiagram;
import ie.lero.spare.pattern_instantiation.GraphPath;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Matcher;

public class IncidentPatternHandler {

	private IncidentDiagram incidentPattern;
	private EnvironmentDiagram systemModel;

	// private IncidentDiagram incidentPatternConcrete;
	private String incidentPatternFilePath;
	private String systemModelFilePath;
	private String tracesFilePath;

	// key is entity name, value is asset name
	private Map<String, String> entityAssetMap;

	// key is asset name, value is the Asset object from system model
	private Map<String, Asset> nameToAssetMap;

	private final static String ENTITY_NAME = "incident_entity_name";
	private final static String ASSET_NAME = "system_asset_name";
	private final static String MAPS = "maps";

	private TraceMiner miner;

	public IncidentPatternHandler(TraceMiner traceMiner) {
		entityAssetMap = new HashMap<String, String>();
		nameToAssetMap = new HashMap<String, Asset>();
		miner = traceMiner;

	}

	public void setBigraphFilePath(String bigFilePath) {

		if (miner != null) {
			miner.setBigraphERFile(bigFilePath);
		}
	}

	public String getTracesFilePath() {
		return tracesFilePath;
	}

	public void setTracesFilePath(String tracesFilePath) {
		this.tracesFilePath = tracesFilePath;
	}

	public TraceMiner getMiner() {
		return miner;
	}

	public void setMiner(TraceMiner miner) {
		this.miner = miner;
	}

	public String getIncidentPatternFilePath() {
		return incidentPatternFilePath;
	}

	public void setIncidentPatternFilePath(String incidentPatternFilePath) {
		this.incidentPatternFilePath = incidentPatternFilePath;
	}

	public String getSystemModelFilePath() {
		return systemModelFilePath;
	}

	public void setSystemModelFilePath(String systemModelFilePath) {
		this.systemModelFilePath = systemModelFilePath;
	}

	public Map<String, String> getEntityAssetMap() {
		return entityAssetMap;
	}

	public void setEntityAssetMap(Map<String, String> entityAssetMap) {
		this.entityAssetMap = entityAssetMap;
	}

	public IncidentDiagram loadIncidentPattern(String filePath) {

		if (filePath == null) {
			return null;
		}

		incidentPattern = IncidentModelHandler.loadIncidentFromFile(filePath);

		if (incidentPattern != null) {
			incidentPatternFilePath = filePath;
		}

		return incidentPattern;
	}

	public EnvironmentDiagram loadSystemModel(String systemFilePath) {

		if (systemFilePath == null) {
			return null;
		}

		systemModel = SystemModelHandler.loadSystemFromFile(systemFilePath);

		if (systemModel != null) {
			systemModelFilePath = systemFilePath;
		}

		return systemModel;
	}

	/**
	 * Replaces incident pattern entities in conditions with asset class names
	 * 
	 * @param tracesFilePath
	 *            traces file path which contains the map (entity name to asset
	 *            name)
	 */
	public void createConcreteConditions(String tracesFilePath) {

		// update the incident pattern with concerte entities

		// load incident pattern
		if (incidentPattern == null) {
			loadIncidentPattern(incidentPatternFilePath);

			if (incidentPattern == null) {
				return;
			}
		}

		// load system model
		if (systemModel == null) {
			loadSystemModel(systemModelFilePath);

			if (systemModel == null) {
				return;
			}
		}

		JSONParser parser = new JSONParser();

		FileReader reader;
		try {

			reader = new FileReader(tracesFilePath);
			JSONObject tracesObj = (JSONObject) parser.parse(reader);

			// ===get the entity-asset map from the traces file
			fillEntityAssetMap(tracesObj);

			if (entityAssetMap == null || entityAssetMap.isEmpty()) {
				return;
			}

			// ===get asset class from system model

			Activity act = incidentPattern.getInitialActivity();

			if (act == null) {
				System.out.println("IncidentPAtternHandler:: Initial Activity is not found");
				return;
			}

			while (act != null) {
				List<Condition> conditions = new LinkedList<Condition>();
				conditions.add(act.getPrecondition());
				conditions.add(act.getPostcondition());

				// for each condition replace each
				for (Condition cond : conditions) {

					if (cond == null) {
						continue;
					}

					for (Entry<String, String> entry : entityAssetMap.entrySet()) {

						String entityName = entry.getKey();
						String assetName = entry.getValue();

						Asset ast = systemModel.getAsset(entry.getValue());

						if (ast == null) {
							System.out.println("asset : " + assetName + " is not in the system model.");
							continue;
						}

						String className = ast.getClass().getSimpleName();

						// remove Impl if exists
						if (className.contains("Impl")) {
							className = className.replace("Impl", "");
						}

						// replace the entity name with a class name
						replaceEntityNameToAssetClass(cond, entityName, className);
					}
				}

				// get next act
				act = (act.getNextActivities() != null && act.getNextActivities().size() > 0)
						? act.getNextActivities().get(0) : null;

			}

			// === replace entity name with asset class
			// for(Entry<String, String> entry : entityAssetMap)

		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void replaceEntityNameToAssetClass(Condition condition, String entityName, String assetClass) {

		if (condition == null) {
			return;
		}

		BigraphExpression bigExp = (BigraphExpression) condition.getExpression();

		bigExp.replaceEntityName(entityName, assetClass);

		// System.out.println(bigExp.getEntity());
		//
		// Entity entity = bigExp.getEntity(entityName);
		//
		// if (entity != null) {
		//// entity.
		//
		// System.out.println("replaced entity name [" + entityName + "] with
		// asset class [" + assetClass + "]");
		// } else {
		// System.out.println(
		// "entity name [" + entityName + "] does not exist in the condition ["
		// + condition.getName() + "]");
		// }
	}

	public void fillEntityAssetMap(JSONObject traces) {

		// reads the entity-asset map from the traces file
		if (traces == null) {
			System.err.println("traces json object is NULL");
			return;
		}

		if (!traces.containsKey(MAPS)) {
			System.err.println("Key error: " + MAPS);
			return;
		}

		JSONArray ary = (JSONArray) traces.get(MAPS);

		if (ary == null) {
			System.err.println("json array from traces file is null");
			return;
		}

		Iterator it = ary.iterator();

		// get entity asset map
		while (it != null && it.hasNext()) {

			JSONObject entityAssetObj = (JSONObject) it.next();

			if (entityAssetObj == null) {
				continue;
			}

			String entityName = entityAssetObj.get(ENTITY_NAME).toString();
			String assetName = entityAssetObj.get(ASSET_NAME).toString();

			if (entityName != null && assetName != null) {
				entityAssetMap.put(entityName, assetName);
			}
		}

		// System.out.println("Map:\n" + entityAssetMap);
	}

	public Map<String, Integer> findMatchingStates(GraphPath trace) {

		// ==== finds the sequence of states from the given trace that match to
		// the conditions of the incident pattern activities
		if (trace == null || miner == null) {
			System.err.println("Miner is null");
			return null;
		}

		// ===create concrete conditions
		createConcreteConditions(tracesFilePath);

		if (incidentPattern == null) {
			return null;
		}

		// key is condition name, value is state id it matches to
		Map<String, Integer> matchingStates = new HashMap<String, Integer>();

		int currentIndex = 0;
		List<Integer> traceStates = trace.getStateTransitions();

		for (Activity act : incidentPattern.getActivity()) {

			List<Condition> conditions = new LinkedList<Condition>();
			conditions.add(act.getPrecondition());
			conditions.add(act.getPostcondition());

			for (Condition cond : conditions) {

				if (cond == null) {
					continue;
				}

				// get bigraph object of condition
				BigraphExpression bigExp = (BigraphExpression) cond.getExpression();

				if (bigExp == null) {
					System.err.println("bigraph expression is null of condition [" + cond.getName() + "]");
					continue;
				}

				// BigraphWrapper wrapper = new BigraphWrapper();
				// wrapper.setBigraphExpression(bigExp);
				// wrapper.createBigraph(false, miner.gets)
				Bigraph condBig = bigExp.createBigraph(false, miner.getSignature());

				if (condBig == null) {
					System.err.println("Bigraph object of condition [" + cond.getName() + "] is null");
					continue;
				}

				// match condition to a state
				for (; currentIndex < traceStates.size(); currentIndex++) {

					int stateID = traceStates.get(currentIndex);

					// get bigraph representation
					Bigraph stateBig = miner.loadState(stateID);

					if (stateBig == null) {
						System.err.println("System state [" + stateID + "] is null");
						continue;
					}

					// match state to condition
					Matcher matcher = new Matcher();

					// if it matches then add to the result
					if (matcher.match(stateBig, condBig).iterator().hasNext()) {
						matchingStates.put(cond.getName(), stateID);

						// increment index if the condition is pre
						if (cond instanceof Precondition) {
							currentIndex++;
							break;
						}
					}

				}
			}

		}

		return matchingStates;
	}

	public static void main(String[] args) {

		TraceMiner miner = new TraceMiner();

		IncidentPatternHandler inc = new IncidentPatternHandler(miner);

		String traceExampleFile = "resources/example/traces_10K.json";
		String sysModelFilePath = "D:/Bigrapher data/lero/big with unique action names/lero.cps";
		String bigrapherFilePath = "D:/Bigrapher data/lero/big with unique action names/lero.big";
		String incidentPatternModelFilePath = "D:/Bigrapher data/lero/big with unique action names/incidentPattern.cpi";

		URL url = IncidentPatternHandler.class.getClassLoader().getResource(traceExampleFile);

		if (url != null) {
			String filePath = url.getPath();

			// set incident and system model files
			inc.setIncidentPatternFilePath(incidentPatternModelFilePath);
			inc.setSystemModelFilePath(sysModelFilePath);

			//set traces file
			inc.setTracesFilePath(filePath);
			
			// set bigrapher file if not set by the miner
			if (miner.getBigraphERFile() == null || miner.getBigraphERFile().isEmpty()) {
				inc.setBigraphFilePath(bigrapherFilePath);
			}

			GraphPath testTrace = new GraphPath();
			List<Integer> states = new LinkedList<Integer>();
			states.add(1);
			states.add(61);
			states.add(174);
			states.add(396);
			states.add(1699);
			states.add(6689);
			
			testTrace.setInstanceID(100);
			testTrace.setStateTransitions(states);
			
			Map<String, Integer> res = inc.findMatchingStates(testTrace);
			
			System.out.println(res);
			// replaces entity names with asset class names
//			inc.createConcreteConditions(filePath);

		} else {
			System.out.println("url is null");
		}

	}
}
