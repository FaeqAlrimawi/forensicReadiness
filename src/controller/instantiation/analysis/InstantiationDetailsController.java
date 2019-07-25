package controller.instantiation.analysis;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import core.brs.parser.utilities.JSONTerms;
import core.instantiation.analysis.TraceMiner;
import ie.lero.spare.pattern_instantiation.GraphPath;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class InstantiationDetailsController {

	// private ComboBox<Integer> comboBoxTopK;
	@FXML
	private HBox hboxEntities;

	@FXML
	private ComboBox<Integer> comboBoxTopK;

	@FXML
	private Button btnHide;

	@FXML
	private AnchorPane anchorPaneMain;

	// for testing
	private String bigFile = "D:/Bigrapher data/lero/example/lero.big";

	private GraphPath trace;
	private TraceMiner miner;
	private List<Map.Entry<String, Long>> topEntities;

	// used for common entities
	private int topK = 3;
	private int topKMax = 10;

	private VBox vboxMain;

	@FXML
	public void initialize() {

		// ===add a combo box
		List<Integer> topKValues = new LinkedList<Integer>();

		for (int i = 1; i <= topKMax; i++) {
			topKValues.add(i);
		}

		ObservableList<Integer> values = FXCollections.observableArrayList(topKValues);
		comboBoxTopK.setItems(values);

		// add listener for when changed
		comboBoxTopK.setOnAction(e -> {
			int selection = comboBoxTopK.getSelectionModel().getSelectedItem();

			topK = selection;
			showEntities(trace);
		});

	}

	public void setVBox(VBox main) {
		vboxMain = main;
	}

	public AnchorPane getMainLayout() {
		return anchorPaneMain;
	}

	void showEntities(GraphPath trace) {

		this.trace = trace;
		
		// if(vboxMain == null) {
		// return;
		// }

		// if (topEntities != null) {
		// return;
		// }

		if (miner == null) {
			miner = new TraceMiner();
		}

		if (!miner.isBigraphERFileSet()) {
			miner.setBigraphERFile(bigFile);
		}

		if(hboxEntities.getChildren().size()>0) {
			hboxEntities.getChildren().clear();
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

		topEntities = miner.findCommonEntities(traces, JSONTerms.BIG_IRRELEVANT_TERMS, topK);

		// System.out.println(res);

		// create a holder (HBox)
		// HBox hbox = new HBox();
		// hbox.setPrefHeight(25);
		// hbox.setPrefWidth(vboxMain.getPrefWidth());
		// hbox.setSpacing(5);
		// hbox.setAlignment(Pos.CENTER);

		// create labels for each entity
		List<Label> resLbls = new LinkedList<Label>();

		for (Map.Entry<String, Long> entry : topEntities) {
			Label lbl = new Label(" " + entry.getKey() + " <" + entry.getValue() + "> ");
			lbl.setStyle(style);
			// lbl.setStyle("-fx-font-weight: bold;");
			// lbl.setStyle("-fx-background-color: grey;");
			// lbl.setStyle("-fx-border-color: grey;");
			resLbls.add(lbl);
		}

		// ===add label as identifier:
		// Label lblId = new Label("Common Entities: ");
		// lblId.setStyle(style);
		// hboxEntities.getChildren().add(lblId);

		// set selected value
		comboBoxTopK.getSelectionModel().select(topK - 1);

		// add to hbox
		// hboxEntities.getChildren().add(comboBoxTopK);

		// ===add hid button
		// Button btnHide = new Button("Hide");
		// btnHide.setOnAction(e -> {
		// vboxMain.getChildren().remove(vboxMain.getChildren().size() - 1);
		// });

		// add labels to hbox
		hboxEntities.getChildren().addAll(resLbls);

		// add hide button to hbox
		// hbox.getChildren().add(btnHide);

		// add hbox to the vboxmain
		// if (vboxMain.getChildren().size() == 2) {
		// // if hbox is already added
		// // System.out.println("Renew");
		// vboxMain.getChildren().remove(vboxMain.getChildren().size() - 1);
		// vboxMain.getChildren().add(anchorPaneMain);
		// } else {
		// // System.out.println("Add new");
		// vboxMain.getChildren().add(anchorPaneMain);
		// // updateItem(trace, false);
		// }

	}

	@FXML
	void hide(ActionEvent e) {

		vboxMain.getChildren().remove(vboxMain.getChildren().size() - 1);
	}

}
