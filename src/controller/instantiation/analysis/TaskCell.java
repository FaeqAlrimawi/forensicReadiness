
package controller.instantiation.analysis;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import core.brs.parser.utilities.JSONTerms;
import core.instantiation.analysis.TraceMiner;
import ie.lero.spare.pattern_instantiation.GraphPath;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TaskCell extends ListCell<GraphPath> {

	@FXML
	private Label lblTraceID;

	@FXML
	private HBox hbox;

	@FXML
	private Pane rootPane;

	@FXML
	private ScrollPane scrollPaneTrace;

	@FXML
	private SplitPane splitPaneTrace;

	@FXML
	private HBox hboxOptions;

	// @FXML
	// private HBox hboxEntities;

	@FXML
	private VBox vboxMain;

	//for viewing a state
	StateViewerController stateController;

	//for viewing details of a trace
	InstantiationDetailsController traceDetailController;
	AnchorPane traceDetailsMainPane;
	
	Stage stateViewerStage;

	private ComboBox<Integer> comboBoxTopK;

	// for testing
	private String bigFile = "D:/Bigrapher data/lero/example/lero.big";

	private GraphPath trace;
	private TraceMiner miner;
	private List<Map.Entry<String, Long>> topEntities;

	//used for common entities
	private int topK = 3;
	private int topKMax = 10;

	public TaskCell() {

		loadStateController();
		loadFXML();
	}

	private void loadFXML() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../fxml/task_cell.fxml"));
			loader.setController(this);
			// loader.setRoot(this);
			loader.load();

			if (rootPane != null) {
				rootPane.setOnMouseEntered(e -> {
					hboxOptions.setVisible(true);
				});

				rootPane.setOnMouseExited(e -> {
					hboxOptions.setVisible(false);
				});
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void loadStateController() {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../../../fxml/state_viewer.fxml"));
		Parent root;
		try {
			root = (Parent) fxmlLoader.load();
			stateViewerStage = new Stage();
			stateViewerStage.setScene(new Scene(root));

			// get controller
			stateController = fxmlLoader.<StateViewerController>getController();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadTraceDetailsController() {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../../../fxml/InstantiationDetails.fxml"));
		Parent root;
		try {
			root = (Parent) fxmlLoader.load();
//			stateViewerStage = new Stage();
//			stateViewerStage.setScene(new Scene(root));

			// get controller
		traceDetailController = fxmlLoader.<InstantiationDetailsController>getController();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	void showEntities(ActionEvent event) {

		// if (topEntities != null) {
		// return;
		// }

//		if (miner == null) {
//			miner = new TraceMiner();
//		}
//
//		if (!miner.isBigraphERFileSet()) {
//			miner.setBigraphERFile(bigFile);
//		}
//
//		StringBuilder bldrStyle = new StringBuilder();
//
//		// add style to labels
//		// font: 14, color: black, weight: bold
//		bldrStyle.append("-fx-text-fill: black; -fx-font-size:14px; -fx-font-weight: bold;")
//				// background
//				.append("-fx-background-color: white;")
//				// border
//				.append("-fx-border-color: grey;");
//
//		String style = bldrStyle.toString();
//
//		// get common entities
//
//		List<GraphPath> traces = new LinkedList<GraphPath>();
//		traces.add(trace);
//
//		topEntities = miner.findCommonEntities(traces, JSONTerms.BIG_IRRELEVANT_TERMS, topK);
//
//		// System.out.println(res);
//
//		// create a holder (HBox)
//		HBox hbox = new HBox();
//		hbox.setPrefHeight(25);
//		hbox.setPrefWidth(vboxMain.getPrefWidth());
//		hbox.setSpacing(5);
//		hbox.setAlignment(Pos.CENTER);
//
//		// create labels for each entity
//		List<Label> resLbls = new LinkedList<Label>();
//
//		for (Map.Entry<String, Long> entry : topEntities) {
//			Label lbl = new Label(" " + entry.getKey() + " <" + entry.getValue() + "> ");
//			lbl.setStyle(style);
//			// lbl.setStyle("-fx-font-weight: bold;");
//			// lbl.setStyle("-fx-background-color: grey;");
//			// lbl.setStyle("-fx-border-color: grey;");
//			resLbls.add(lbl);
//		}
//
//		// ===add label as identifier:
//		Label lblId = new Label("Common Entities: ");
//		// lblId.setStyle(style);
//		hbox.getChildren().add(lblId);
//
//		// ===add a combo box
//		if (comboBoxTopK == null) {
//			comboBoxTopK = new ComboBox<Integer>();
//			List<Integer> topKValues = new LinkedList<Integer>();
//
//			for (int i = 1; i <= topKMax; i++) {
//				topKValues.add(i);
//			}
//
//			ObservableList<Integer> values = FXCollections.observableArrayList(topKValues);
//			comboBoxTopK.setItems(values);
//
//			// add listener for when changed
//			comboBoxTopK.setOnAction(e -> {
//				int selection = comboBoxTopK.getSelectionModel().getSelectedItem();
//
//				topK = selection;
//				showEntities(null);
//			});
//		}
//
//		// set selected value
//		comboBoxTopK.getSelectionModel().select(topK - 1);
//
//		// add to hbox
//		hbox.getChildren().add(comboBoxTopK);
//
//		// ===add hid button
//		Button btnHide = new Button("Hide");
//		btnHide.setOnAction(e -> {
//			vboxMain.getChildren().remove(vboxMain.getChildren().size() - 1);
//		});
//
//		// add labels to hbox
//		hbox.getChildren().addAll(resLbls);
//
//		// add hide button to hbox
//		hbox.getChildren().add(btnHide);

		//load trace details view if not loaded
		if(traceDetailsMainPane == null) {
			loadTraceDetailsController();
			if(traceDetailController != null) {
				traceDetailsMainPane = traceDetailController.getMainLayout();
				
				traceDetailController.setVBox(vboxMain);
				//show default value for entities
				traceDetailController.showEntities(trace);
			}
		}
		
		// add hbox to the vboxmain
		if (vboxMain.getChildren().size() == 2) {
			// if hbox is already added
			// System.out.println("Renew");
			vboxMain.getChildren().remove(vboxMain.getChildren().size() - 1);
			vboxMain.getChildren().add(traceDetailsMainPane);
		} else {
			// System.out.println("Add new");
			vboxMain.getChildren().add(traceDetailsMainPane);
			// updateItem(trace, false);
		}

	}

	@Override
	protected void updateItem(GraphPath trace, boolean empty) {
		super.updateItem(trace, empty);

		if (empty || trace == null) {

			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					setText(null);
					setContentDisplay(ContentDisplay.TEXT_ONLY);

				}
			});

		} else {

			// set trace id
			String id = lblTraceID.getText();

			// if new
			if (id == null || id.isEmpty()) {
				// lblTraceID.setText(trace.getInstanceID()+"");
				//
				// int index = 0;
				// int size = trace.getStateTransitions().size()-1;
				// //set states
				// for(Integer state : trace.getStateTransitions()) {
				//// Circle circle = new Circle(hbox.getHeight()-2);
				// Label stateLbl;
				// if(index != size) {
				// stateLbl = new Label(state+" -> ");
				// } else {
				// stateLbl = new Label(state+"");
				// }
				//
				// index++;
				// hbox.getChildren().add(stateLbl);
				//
				// }

				populateCell(trace);

				// if already exist, check the id of the trace if different
			} else {
				int currentTraceID = Integer.parseInt(id);

				if (currentTraceID != trace.getInstanceID()) {
					populateCell(trace);
				}
			}

			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					// splitPaneTrace.setPrefWidth(hboxRoot.getPrefWidth());
					// hboxRoot.prefWidthProperty().bind(hboxRoot.prefWidthProperty());
					// TODO Auto-generated method stub
					setText(null);
					setGraphic(rootPane);
					setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				}
			});

			this.trace = trace;
		}
	}

	protected void populateCell(GraphPath trace) {

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				lblTraceID.setText(trace.getInstanceID() + "");
				hbox.getChildren().clear();
				Pane pane = new Pane();
				pane.setPrefWidth(10);
				hbox.getChildren().add(pane);

			}
		});

		int index = 0;
		int size = trace.getStateTransitions().size() - 1;
		List<Integer> states = trace.getStateTransitions();
		List<String> actions = trace.getTransitionActions();
		StringBuilder strBldr = new StringBuilder();

		// set states
		for (Integer state : states) {
			// Circle circle = new Circle(hbox.getHeight()-2);
			Label lblState;
			Label lblAction;
			if (index != size) {

				lblState = new Label(state + "");

				strBldr.append(state);

				lblAction = new Label(" =[" + actions.get(index) + "]=> ");
				lblAction.setStyle("-fx-text-fill: black; -fx-font-size:14px");

				lblAction.setStyle("-fx-font-weight: bold;");
				strBldr.append(" =[" + actions.get(index) + "]=> ");
			} else {
				lblState = new Label(state + "");
				strBldr.append(state);
				lblAction = null;
			}

			lblState.setStyle("-fx-text-fill:black; -fx-font-size:15px");

			// setup a way to find available files for the state (svg or json)

			// set color to blue if found
			lblState.setStyle("-fx-text-fill:blue; -fx-font-size:15px");

			// open state svg
			lblState.setOnMouseClicked(e -> {

				// open viewer
				String path = "C:\\Users\\Faeq\\Desktop\\svg\\0.svg";
				int tries = 10000;

				while (path.contains("\\") && tries > 0) {
					path = path.replace("\\", "/");
					tries--;
				}

				String svgPath = "file:///" + path;

				stateController.updateSVGPath(svgPath);

				stateViewerStage.setTitle("State " + state);

				if (!stateViewerStage.isShowing()) {
					stateViewerStage.show();
				}

			});

			lblState.setOnMouseEntered(e -> {
				lblState.setCursor(Cursor.HAND);
			});

			index++;

			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					hbox.getChildren().add(lblState);
					if (lblAction != null) {
						hbox.getChildren().add(lblAction);
					}

					// add tooltip
					Tooltip tip = new Tooltip(strBldr.toString());
					lblTraceID.setTooltip(tip);

				}
			});

		}
	}
}