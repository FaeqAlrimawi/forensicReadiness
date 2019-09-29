package controller.instantiation.analysis;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import core.brs.parser.utilities.JSONTerms;
import core.instantiation.analysis.TraceMiner;
import ie.lero.spare.pattern_instantiation.GraphPath;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class InstantiationDetailsController {

	// private ComboBox<Integer> comboBoxTopK;
//	@FXML
//	private HBox hboxEntities;
	
	@FXML
	private FlowPane flowPaneEntities;

	@FXML
	private Spinner<Integer> spinnerTopK;
	
//	@FXML
//	private ComboBox<Integer> comboBoxTopK;

	@FXML
	private Button btnHide;

	@FXML
	private AnchorPane anchorPaneMain;

//	@FXML
//	private Button btnSaveTrace;
	
	// for testing
//	private String bigFile = "D:/Bigrapher data/lero/example/lero.big";

	private GraphPath trace;
	private TraceMiner miner;
	
	//key is entity name, value is occurrence
	private List<Map.Entry<String, Long>> topEntities;

	// used for common entities
	private int topK = 3;
//	private int topKMax = 10;

	private Pane vboxMain;

	private int minEntityNum = 1;
	private int maxEntityNum = 100;
	
	@FXML
	public void initialize() {

		//set up spinner
		SpinnerValueFactory<Integer> valueFactory = //
				new SpinnerValueFactory.IntegerSpinnerValueFactory(minEntityNum,
						maxEntityNum, topK);

		spinnerTopK.setValueFactory(valueFactory);
		
		// add listener for when changed
		spinnerTopK.valueProperty().addListener(new ChangeListener<Integer>() {

			@Override
			public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
				// TODO Auto-generated method stub
				topK = newValue;
				showEntities(trace);
			}
		});

	}

//	@FXML
//	public void saveTrace(ActionEvent e) {
//	
//		
//	}
	
	public void setVBox(Pane main) {
		vboxMain = main;
	}

	public AnchorPane getMainLayout() {
		return anchorPaneMain;
	}

	void showEntities(GraphPath trace) {

		this.trace = trace;

//		if (miner == null) {
//			miner = new TraceMiner();
//		}
//
//		if (!miner.isBigraphERFileSet()) {
//			miner.setBigraphERFile(bigFile);
//		}

		if(flowPaneEntities.getChildren().size()>0) {
			flowPaneEntities.getChildren().clear();
		}
		
		StringBuilder bldrStyle = new StringBuilder();

		// add style to labels
		// font: 14, color: black, weight: bold
		bldrStyle.append("-fx-text-fill: black; -fx-font-size:14px; -fx-font-weight: bold;")
				// background
				.append("-fx-background-color: white;")
				// border
				.append("-fx-border-color: grey;");

		String style = bldrStyle.toString();

		// get common entities

		List<GraphPath> traces = new LinkedList<GraphPath>();
		traces.add(trace);

		topEntities = miner.findTopCommonEntities(traces, JSONTerms.BIG_IRRELEVANT_TERMS, topK);

		// create labels for each entity
		List<Label> resLbls = new LinkedList<Label>();

		for (Map.Entry<String, Long> entry : topEntities) {
			Label lbl = new Label(" " + entry.getKey() + " <" + entry.getValue() + "> ");
			lbl.setStyle(style);
			resLbls.add(lbl);
		}

		// set selected value
//		comboBoxTopK.getSelectionModel().select(topK - 1);
		spinnerTopK.getValueFactory().setValue(topK);
		// add labels to hbox
		flowPaneEntities.getChildren().addAll(resLbls);


	}

	public void setTraceMiner(TraceMiner miner) {
		this.miner = miner;
	}
	
	@FXML
	void hide(ActionEvent e) {

		vboxMain.getChildren().remove(vboxMain.getChildren().size() - 1);
	}

}
