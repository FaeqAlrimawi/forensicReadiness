
package controller.instantiation.analysis;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import core.instantiation.analysis.TraceMiner;
import ie.lero.spare.pattern_instantiation.GraphPath;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
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

	// for viewing details of a trace
	InstantiationDetailsController traceDetailController;
	AnchorPane traceDetailsMainPane;

	private Stage stateViewerStage;
	private Stage reactViewerStage;

	// for viewing a state
	StateViewerController stateController;

	ReactController reactController;

	private GraphPath trace;

	private final URL defaultStatesFolder = getClass().getResource("../../../resources/example/states_1000");

	private String statesFolder;

	private final URL defaultBigraphERFile = getClass().getResource("../../../resources/example/systemBigraphER.big");

	private TraceMiner traceMiner;

	private static final String SVG_EXT = ".svg";
	private static final String JSON_EXT = ".json";
	private static final String TXT_EXT = ".txt";

	private String[] stateExtensions = new String[] { SVG_EXT, JSON_EXT, TXT_EXT };

	public TaskCell(TraceMiner miner) {
		this();
		// loadFXML();

		traceMiner = miner;
	}

	public TaskCell() {

		// loadStateController();
		loadFXML();
		// loadTraceDetailsController();
		traceMiner = null;
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

	private void loadReactController() {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../../../fxml/ReactView.fxml"));
		Parent root;
		try {
			root = (Parent) fxmlLoader.load();
			reactViewerStage = new Stage();
			reactViewerStage.setScene(new Scene(root));

			// get controller
			reactController = fxmlLoader.<ReactController>getController();

			if (reactController != null) {
				reactController.setTraceMiner(traceMiner);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void loadTraceDetailsController() {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../../../fxml/InstantiationDetails.fxml"));
		// Parent root;
		try {
			traceDetailsMainPane = fxmlLoader.load();
			// stateViewerStage = new Stage();
			// stateViewerStage.setScene(new Scene(root));

			// get controller
			traceDetailController = fxmlLoader.<InstantiationDetailsController>getController();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	void showEntities(ActionEvent event) {

		// check that the .big is loaded
		if (traceMiner != null && !traceMiner.isBigraphERFileSet()) {
			// choose bigrapher file
			selectBigraphERFile();

			if (!traceMiner.isBigraphERFileSet()) {
				return;
			}
		}

		// load trace details view if not loaded
		if (traceDetailController == null) {
			loadTraceDetailsController();

			traceDetailController.setTraceMiner(traceMiner);
			traceDetailController.setVBox(vboxMain);
			// //show default value for entities
			traceDetailController.showEntities(trace);
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

	/**
	 * Select BigraphER file (*.big)
	 */
	protected void selectBigraphERFile() {
		FileChooser fileChooser = new FileChooser();

		// if a file already chosen
		if (traceMiner != null && traceMiner.isBigraphERFileSet()) {
			String bigFile = traceMiner.getBigraphERFile();
			File selectedBigraphERFile = new File(bigFile);
			fileChooser.setInitialFileName(selectedBigraphERFile.getName());

			String folder = selectedBigraphERFile.getAbsolutePath().substring(0,
					selectedBigraphERFile.getAbsolutePath().lastIndexOf(File.separator));
			File folderF = new File(folder);

			if (folderF.isDirectory()) {
				fileChooser.setInitialDirectory(folderF);
			}
		} else if (defaultBigraphERFile != null) {
			File selectedBigraphERFile = new File(defaultBigraphERFile.getPath());
			fileChooser.setInitialFileName(selectedBigraphERFile.getName());

			String folder = selectedBigraphERFile.getAbsolutePath().substring(0,
					selectedBigraphERFile.getAbsolutePath().lastIndexOf(File.separator));
			File folderF = new File(folder);

			if (folderF.isDirectory()) {
				fileChooser.setInitialDirectory(folderF);
			}
		}

		// if first time
		if (traceMiner != null && !traceMiner.isBigraphERFileSet()) {
			ButtonType result = showDialog("Select BigraphER File", "Please select BigraphER file (*.big)",
					AlertType.CONFIRMATION);

			if (result == ButtonType.CANCEL) {
				return;
			}
		}

		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("BigraphER files (*.big)", "*.big");

		fileChooser.getExtensionFilters().add(extFilter);

		File selectedTracesFile = fileChooser.showOpenDialog(null);

		if (selectedTracesFile != null) {
			traceMiner.setBigraphERFile(selectedTracesFile.getAbsolutePath());
		}
	}

	/**
	 * Select states folder (which contains the .svg representation)
	 */
	protected void selectStatesFolder() {
		DirectoryChooser dirChooser = new DirectoryChooser();

		// show folder if any
		if (statesFolder != null) {
			File selectedStatesFolder = new File(statesFolder);

			if (selectedStatesFolder.isDirectory()) {
				dirChooser.setInitialDirectory(selectedStatesFolder);
			}
		} else
		// show default folder
		if (defaultStatesFolder != null) {
			File selectedStatesFolder = new File(defaultStatesFolder.getPath());

			if (selectedStatesFolder.isDirectory()) {
				dirChooser.setInitialDirectory(selectedStatesFolder);
			}
		}

		if (statesFolder == null) {

			ButtonType result = showDialog("Select States Folder",
					"Please select a Folder which contains the states representations (e.g., *.svg, *.json, *.txt)",
					AlertType.CONFIRMATION);

			if (result == ButtonType.CANCEL) {
				return;
			}
		}
		// ButtonType result = showDialog("Select States Folder",
		// "Please select a Folder which contains the states representations
		// (e.g., *.svg, *.json, *.txt)",
		// AlertType.CONFIRMATION);
		//
		// if (result == ButtonType.CANCEL) {
		// return;
		// } else {
		File selectedStatesFolder = dirChooser.showDialog(null);

		if (selectedStatesFolder != null) {
			traceMiner.setStatesFolder(selectedStatesFolder.getAbsolutePath());
			statesFolder = selectedStatesFolder.getAbsolutePath();
		}
		// }

	}

	@Override
	protected void updateItem(GraphPath trace, boolean empty) {
		super.updateItem(trace, empty);

		if (empty || trace == null) {

			// populateCell(null);
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
					// TODO Auto-generated method stub
					setText(null);
					setGraphic(rootPane);
					setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				}
			});

		}
	}

	protected void clearData() {
		traceDetailController = null;
		traceDetailsMainPane = null;
		trace = null;
		stateController = null;
		stateViewerStage = null;
		reactController = null;
		reactViewerStage = null;

		lblTraceID.setText("");
		hbox.getChildren().clear();
		vboxMain.getChildren().clear();
		rootPane.getChildren().clear();

		// re-add
		vboxMain.getChildren().add(splitPaneTrace);
		rootPane.getChildren().add(vboxMain);
		rootPane.getChildren().add(hboxOptions);
	}

	protected void populateCell(GraphPath trace) {

		clearData();
		// traceDetailsMainPane = null;

		if (trace == null) {
			return;
		}

		this.trace = trace;
		// id
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				lblTraceID.setText(trace.getInstanceID() + "");
				// hbox.getChildren().clear();
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

		// System.out.println("states: "+states);
		// System.out.println("actions: "+actions);
		// set states
		for (Integer state : states) {
			// Circle circle = new Circle(hbox.getHeight()-2);
			Label lblState;
			Label lblAction;
			if (index != size) {

				lblState = new Label(state + "");

				strBldr.append(state);

				String act = actions.get(index);
				lblAction = new Label(" =[" + act + "]=> ");
				
				lblAction.setStyle("-fx-text-fill: black; -fx-font-size:14px;");

				lblAction.setOnMouseClicked(e -> {
					showReact(act);
				});

				lblAction.setOnMouseEntered(e -> {
//					lblAction.setStyle("-fx-font-weight: bold;");
//					lblAction.setStyle("-fx-text-fill: black; -fx-font-size:14px;");
					lblAction.setStyle("-fx-text-fill: black; -fx-font-size:14px; -fx-font-weight: bold;");
					lblAction.setCursor(Cursor.HAND);
				});
				
				lblAction.setOnMouseExited(e->{
					lblAction.setStyle("-fx-text-fill: black; -fx-font-size:14px; -fx-font-weight: normal;");
//					lblAction.setStyle("-fx-font-weight: normal;");
				});
				
				strBldr.append(" =[" + actions.get(index) + "]=> ");
			} else {
				lblState = new Label(state + "");
				strBldr.append(state);
				lblAction = null;
			}

//			lblState.setStyle("-fx-text-fill:black; -fx-font-size:15px");

			// setup a way to find available files for the state (svg or json)

			// set color to blue if found
			lblState.setStyle("-fx-text-fill:blue; -fx-font-size:15px;");

			// open state svg
			lblState.setOnMouseClicked(e -> {

				showState(state);

			});

			lblState.setOnMouseEntered(e -> {
				lblState.setStyle("-fx-text-fill:blue; -fx-font-size:15px; -fx-font-weight: bold;");
//				lblState.setStyle("-fx-font-weight: bold;");
				lblState.setCursor(Cursor.HAND);
			});

			lblState.setOnMouseExited(e->{
//				lblState.setStyle("-fx-text-fill:blue; -fx-font-size:15px");
//				lblState.setStyle("-fx-font-weight: normal;");
				lblState.setStyle("-fx-text-fill:blue; -fx-font-size:15px; -fx-font-weight: normal;");
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

				}
			});

		}

		// tooltip that holds the whole trace states and actions
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// add tooltip
				Tooltip tip = new Tooltip(strBldr.toString());
				lblTraceID.setTooltip(tip);
			}
		});

	}

	protected void showState(int state) {

		if (stateController == null) {
			loadStateController();
		}

		if (statesFolder == null || statesFolder.isEmpty()) {
			// try to get it from the traceMiner
			if (traceMiner != null) {
				String statesFolder = traceMiner.getStatesFolder();

				if (statesFolder == null || statesFolder.isEmpty()) {
					// prompt the user to select a folder containing the states
					selectStatesFolder();
				} else {
					this.statesFolder = statesFolder;
				}
			}

		}

		// if folder not set return
		if (statesFolder == null || statesFolder.isEmpty()) {
			return;
		}

		// try to find a state representation as in stateExtension
		String path = null;
		File file = null;
		String fileExt = null;

		for (String ext : stateExtensions) {
			path = statesFolder + File.separator + state + ext;
			file = new File(path);

			if (file.exists()) {
				fileExt = ext;
				break;
			}

		}

		// no state found
		if (fileExt == null) {
			showDialog("Not found", "File not found for state [" + state + "].", AlertType.ERROR);
			selectStatesFolder();
			return;
		}

		// if state found
		switch (fileExt) {
		case SVG_EXT:
			// show svg
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

			break;

		case JSON_EXT:
		case TXT_EXT:
			// both extensions are shown by opening the file in default editor
			try {
				Desktop.getDesktop().open(file);
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
			break;
		default:
			break;
		}

	}

	protected void showReact(String actionName) {

		if(traceMiner == null) {
			System.err.println("Trace Miner is NUll");
			return;
		}
		
		if(!traceMiner.isBigraphERFileSet()) {
			selectBigraphERFile();
		}
		
		if(!traceMiner.isBigraphERFileSet()) {
			return;
		}
		
		if (reactController == null) {
			loadReactController();
		}

		if (reactController == null) {
			return;
		}

		reactController.showReact(actionName);
		
		reactViewerStage.setTitle("Action " + actionName);

		if (!reactViewerStage.isShowing()) {
			reactViewerStage.show();
		}
	}

	protected ButtonType showDialog(String title, String msg, AlertType type) {

		Alert alert = new Alert(type);

		alert.setTitle(title);

		alert.setContentText(msg);

		alert.showAndWait();

		return alert.getResult();
	}

}