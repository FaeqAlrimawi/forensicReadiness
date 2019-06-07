package controller;

import java.io.IOException;
import java.util.List;

import ie.lero.spare.pattern_instantiation.GraphPath;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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

	// @FXML
	// private Circle state;
	//
	// @FXML
	// private Label commentLabel;
	//
	// @FXML
	// private Label descriptionLabel;

	StateViewerController stateController;

	Stage stateViewerStage;
	
	public TaskCell() {

		loadStateController();
		loadFXML();
	}

	private void loadFXML() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/task_cell.fxml"));
			loader.setController(this);
			// loader.setRoot(this);
			loader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void loadStateController() {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/state_viewer.fxml"));
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
				lblAction.setStyle("-fx-text-fill: black; -fx-font-size:10px");

				strBldr.append(" =[" + actions.get(index) + "]=> ");
			} else {
				lblState = new Label(state + "");
				strBldr.append(state);
				lblAction = null;
			}

			lblState.setStyle("-fx-text-fill:black; -fx-font-size:12px");
			
			//setup a way to find available files for the state (svg or json)
			
			//set color to blue if found
			lblState.setStyle("-fx-text-fill:blue; -fx-font-size:12px");
			
			//open state svg
			lblState.setOnMouseClicked(e->{
				
				//open viewer
				String path = "C:\\Users\\Faeq\\Desktop\\svg\\0.svg";
				int tries = 10000;
				
				while(path.contains("\\") && tries > 0) {
					path = path.replace("\\", "/");
					tries--;
				}
				
				String svgPath = "file:///"+path;
				
				stateController.updateSVGPath(svgPath);
				
				stateViewerStage.setTitle("State " + state);
				
				if(!stateViewerStage.isShowing()) {
					stateViewerStage.show();
				}
				
			});
			
			lblState.setOnMouseEntered(e->{
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