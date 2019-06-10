package controller.utlities;

import javafx.scene.control.cell.PropertyValueFactory;

public class IncidentAssetMap {
	 
    private String incidentName;
    private String assetNames;
    
    public IncidentAssetMap(String fName, String lNames) {
       incidentName = fName;
       assetNames = lNames;
    }

	public String getIncidentName() {
		return incidentName;
	}

	public String getAssetNames() {
		return assetNames;
	}

	public static PropertyValueFactory<IncidentAssetMap, String> getIncidentNameVariable() {
		return new PropertyValueFactory<IncidentAssetMap, String>("incidentName");
	}

	public static PropertyValueFactory<IncidentAssetMap, String> getAssetNamesVariable() {
		return new PropertyValueFactory<IncidentAssetMap, String>("assetNames");
	}
}

