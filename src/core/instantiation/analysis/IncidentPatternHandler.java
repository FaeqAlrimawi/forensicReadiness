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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import core.instantiation.analysis.utilities.IncidentModelHandler;
import core.instantiation.analysis.utilities.SystemModelHandler;
import cyberPhysical_Incident.IncidentDiagram;
import environment.EnvironmentDiagram;
import ie.lero.spare.pattern_instantiation.GraphPath;

public class IncidentPatternHandler {

	private IncidentDiagram incidentPattern;
	private EnvironmentDiagram systemModel;
	
	// private IncidentDiagram incidentPatternConcrete;
	private String incidentPatternFilePath;
	private String systemModelFilePath;

	// key is entity name, value is asset name
	private Map<String, String> entityAssetMap;

	private final static String ENTITY_NAME = "incident_entity_name";
	private final static String ASSET_NAME = "system_asset_name";
	private final static String MAPS = "maps";

	public IncidentPatternHandler() {
		entityAssetMap = new HashMap<String, String>();
	}

	public IncidentDiagram loadIncidentPattern(String filePath) {

		if (filePath != null) {
			return null;
		}

		incidentPattern = IncidentModelHandler.loadIncidentFromFile(filePath);

		if (incidentPattern != null) {
			incidentPatternFilePath = filePath;
		}

		return incidentPattern;
	}

	public EnvironmentDiagram loadSystemModel(String systemFilePath) {
		
		if (systemFilePath != null) {
			return null;
		}

		systemModel = SystemModelHandler.loadSystemFromFile(systemFilePath);

		if (incidentPattern != null) {
			systemModelFilePath = systemFilePath;
		}

		return systemModel;
	}
	
	
	public void createConcreteConditions(String tracesFilePath) {

		// update the incident pattern with concerte entities
//		incidentPattern = loadIncidentPattern(incidentPatternFilePath);

		JSONParser parser = new JSONParser();

		FileReader reader;
		try {
			reader = new FileReader(tracesFilePath);
			JSONObject tracesObj = (JSONObject) parser.parse(reader);

			fillEntityAssetMap(tracesObj);

		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

		System.out.println("Map:\n" + entityAssetMap);
	}

	public List<Integer> findMatchingStates(GraphPath trace) {

		// ==== finds the sequence of states from the given trace that match to
		// the conditions of the incident pattern activities
		if (trace != null) {
			return null;
		}

		List<Integer> matchingStates = new LinkedList<Integer>();

		return matchingStates;
	}

	public static void main(String[]args) {
		
		IncidentPatternHandler inc = new IncidentPatternHandler();
		
		String traceExampleFile = "resources/example/traces_10K.json";
		URL url = IncidentPatternHandler.class.getClassLoader().getResource(traceExampleFile);
		
		
		if(url!=null) {
			String filePath = url.getPath();
			
			inc.createConcreteConditions(filePath);
		} else {
			System.out.println("url is null");
		}
		
	}
}
