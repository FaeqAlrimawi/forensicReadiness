
package controller.instantiation.analysis;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import core.instantiation.analysis.TraceMiner;
import ie.lero.spare.pattern_instantiation.GraphPath;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
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

	// @FXML
	// private SplitPane splitPaneTrace;

	@FXML
	private HBox hboxTrace;

	// @FXML
	// private HBox hboxOptions;

	// @FXML
	// private HBox hboxEntities;

	// @FXML
	// private VBox vboxMain;

	@FXML
	private MenuButton menuButtonOptions;

	// for viewing details of a trace
	private InstantiationDetailsController traceDetailController;

	// for viewing a state
	private StateViewerController stateController;

	private ReactController reactController;

	// for viewing a trace
	private TraceViewerInSystemController traceViewerController;

	// shows state as svg
	private Stage stateViewerStage;

	// shows reaction rules in editor
	private Stage reactViewerStage;

	// shows trace in system viewer sa graphical nodes
	private Stage traceViewerStage;

	AnchorPane traceDetailsMainPane;

	private GraphPath trace;

	// private Trace newTrace;

	private static final URL defaultStatesFolder = TaskCell.class.getClassLoader()
			.getResource("resources/example/states");

	private static final URL defaultBigraphERFile = TaskCell.class.getClassLoader()
			.getResource("resources/example/systemBigraphER.big");

	private static final URL defaultIncidentPatternFile = TaskCell.class.getClassLoader()
			.getResource("resources/example/incidentPattern.cpi");

	private static final URL defaultSystemModelFile = TaskCell.class.getClassLoader()
			.getResource("resources/example/systemModel.cps");

	private TraceMiner traceMiner;

	// holds the file path for saved trace
	// private String traceFilePath;

	private String boldStyle = "-fx-text-fill: black; -fx-font-size:14px; -fx-font-weight:bold;";
	private String normalStyle = "-fx-text-fill: black; -fx-font-size:14px;";
	private String mouseEnteredStyle = "-fx-text-fill: blue; -fx-font-size:14px;";

	private static final String SVG_EXT = ".svg";
	private static final String JSON_EXT = ".json";
	private static final String TXT_EXT = ".txt";

	private static final String[] stateExtensions = new String[] { SVG_EXT, JSON_EXT, TXT_EXT };

	// options
	private static final String DETAILS = "Show Details";
	private static final String SHOW_ENTITIES = "Show Entities";
	private static final String SAVE = "Save";

	private static final String[] OPTIONS = new String[] { DETAILS, SHOW_ENTITIES, SAVE };

	public TaskCell(TraceMiner miner, ListView<GraphPath> list) {
		this(list);
		// loadFXML();

		traceMiner = miner;
	}

	public TaskCell(ListView<GraphPath> list) {

		// loadStateController();
		loadFXML(list);
		// loadTraceDetailsController();
		traceMiner = null;
	}

	private void loadFXML(ListView<GraphPath> list) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../fxml/task_cell.fxml"));
			loader.setController(this);
			// loader.setRoot(this);
			loader.load();

			if (rootPane != null) {
				rootPane.setOnMouseEntered(e -> {
					// hboxOptions.setVisible(true);
					// if (!menuButtonOptions.isShowing()) {
					// menuButtonOptions.show();
					// }
					menuButtonOptions.setVisible(true);
				});

				rootPane.setOnMouseExited(e -> {
					if (!menuButtonOptions.isShowing()) {
						menuButtonOptions.setVisible(false);
					}

				});

				// set options menu
				menuButtonOptions.getItems().clear();
				for (String opt : OPTIONS) {
					MenuItem item = new MenuItem(opt);
					item.setOnAction(e -> {
						selectOption(opt);
					});
					menuButtonOptions.getItems().add(item);
				}

				if (list != null) {
					rootPane.prefWidthProperty().bind(list.widthProperty().subtract(30));
					rootPane.setMaxWidth(Control.USE_PREF_SIZE);
				}

				// if(traceDetailsMainPane!=null){
				// rootPane.prefHeightProperty().bind(vboxMain.heightProperty().add(15));
				// }

				scrollPaneTrace.prefWidthProperty().bind(rootPane.widthProperty());
				hbox.prefHeightProperty().bind(scrollPaneTrace.heightProperty());

				lblTraceID.prefHeightProperty().bind(scrollPaneTrace.heightProperty());
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

	private void loadTraceViewerController() {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../../../fxml/ShowTraceInSystem.fxml"));
		Parent root;
		try {
			root = (Parent) fxmlLoader.load();
			traceViewerStage = new Stage();
			traceViewerStage.setScene(new Scene(root));

			// get controller
			traceViewerController = fxmlLoader.<TraceViewerInSystemController>getController();

			if (traceViewerController != null) {

				traceViewerController.setTraceMiner(traceMiner, trace);
				traceViewerController.setTaskCell(this);
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

	void selectOption(String selectedOption) {

		switch (selectedOption) {
		case DETAILS:
			showTrace();
			break;
		case SAVE:
			String path = saveTrace(trace);
			// traceFilePath = path;
			if (path != null) {
				lblTraceID.setStyle(boldStyle);

			}

			break;

		case SHOW_ENTITIES:
			showDetails();

		default:
			break;
		}
	}

	/**
	 * show the trace in the traceviewer in system transition
	 */
	void showTrace() {

		if (traceViewerController == null) {
			loadTraceViewerController();
			traceViewerController.showTrace(trace);
		}

		if (traceViewerController == null) {
			return;
		}

		traceViewerStage.setTitle("Trace " + trace.getInstanceID());

		if (!traceViewerStage.isShowing()) {
			traceViewerStage.show();
		}
	}

	// @FXML
	String saveTrace(GraphPath trace) {
		if (trace == null) {
			System.err.println("Trace is NULL");
			return null;
		}

		if (traceMiner == null) {
			System.err.println("Trace Miner is NULL");
			return null;
		}

		// check if trace folder is selected
		String traceFolder = traceMiner.getTraceFolder();
		if (traceFolder == null || traceFolder.isEmpty()) {
			selectTraceFolder();
			traceFolder = traceMiner.getTraceFolder();
			if (traceFolder == null || traceFolder.isEmpty()) {
				return null;
			}
		} else {
			System.out.println(traceFolder);
		}

		String path = traceMiner.saveTrace(trace.getInstanceID());

		return path;
	}

	// @FXML
	void showDetails() {

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
			traceDetailController.setVBox(rootPane);
			// //show default value for entities
			traceDetailController.showEntities(trace);
		}

		// add hbox to the vboxmain
		if (rootPane.getChildren().size() == 2) {
			// if hbox is already added
			// System.out.println("Renew");
			rootPane.getChildren().remove(rootPane.getChildren().size() - 1);
			rootPane.getChildren().add(traceDetailsMainPane);
		} else {
			// System.out.println("Add new");
			rootPane.getChildren().add(traceDetailsMainPane);
			// updateItem(trace, false);
		}

		// rootPane.setPrefHeight(vboxMain.getPrefHeight()+10);

	}

	/**
	 * Select system model file (*.cps)
	 */
	public void selectSystemModelFile() {

		FileChooser fileChooser = new FileChooser();

		// if a file already chosen
		if (traceMiner != null
				&& (traceMiner.getSystemModelFilePath() != null && !traceMiner.getSystemModelFilePath().isEmpty())) {
			String systemModelFile = traceMiner.getSystemModelFilePath();
			File selectedSystemModelFileFile = new File(systemModelFile);

			fileChooser.setInitialFileName(selectedSystemModelFileFile.getName());

			String folder = selectedSystemModelFileFile.getAbsolutePath().substring(0,
					selectedSystemModelFileFile.getAbsolutePath().lastIndexOf(File.separator));
			File folderF = new File(folder);

			if (folderF.isDirectory()) {
				fileChooser.setInitialDirectory(folderF);
			}

		} else if (defaultSystemModelFile != null) {
			File selectedBigraphERFile = new File(defaultSystemModelFile.getPath());
			fileChooser.setInitialFileName(selectedBigraphERFile.getName());

			String folder = selectedBigraphERFile.getAbsolutePath().substring(0,
					selectedBigraphERFile.getAbsolutePath().lastIndexOf(File.separator));
			File folderF = new File(folder);

			if (folderF.isDirectory()) {
				fileChooser.setInitialDirectory(folderF);
			}
		}

		// if first time
		if (traceMiner != null
				&& (traceMiner.getSystemModelFilePath() == null || traceMiner.getSystemModelFilePath().isEmpty())) {
			ButtonType result = showDialog("Select System Model File", "System Model file is needed",
					"Please select [system model file] (*.cps)", AlertType.CONFIRMATION, true);

			if (result == ButtonType.CANCEL) {
				return;
			}
		}

		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("System Model (*.cps)", "*.cps");

		fileChooser.getExtensionFilters().add(extFilter);

		File selectedTracesFile = fileChooser.showOpenDialog(null);

		if (selectedTracesFile != null) {
			traceMiner.setSystemModelFilePath(selectedTracesFile.getAbsolutePath());
		}
	}

	/**
	 * Select incident pattern file (*.cpi)
	 */
	public void selectIncidentPatternFile() {
		FileChooser fileChooser = new FileChooser();

		// System.out.println("selecting pattern file");
		// if a file already chosen
		if (traceMiner != null && (traceMiner.getIncidentPatternFilePath() != null
				&& !traceMiner.getIncidentPatternFilePath().isEmpty())) {
			String incidentPatternFile = traceMiner.getIncidentPatternFilePath();

			// System.out.println("there's exisitng pattern file: " +
			// incidentPatternFile);
			File selectedincidentPatternFile = new File(incidentPatternFile);

			fileChooser.setInitialFileName(selectedincidentPatternFile.getName());

			String folder = selectedincidentPatternFile.getAbsolutePath().substring(0,
					selectedincidentPatternFile.getAbsolutePath().lastIndexOf(File.separator));
			File folderF = new File(folder);

			if (folderF.isDirectory()) {
				fileChooser.setInitialDirectory(folderF);
			}

		} else if (defaultIncidentPatternFile != null) {
			// System.out.println("trying default: " +
			// defaultIncidentPatternFile.getPath());
			File selectedBigraphERFile = new File(defaultIncidentPatternFile.getPath());
			fileChooser.setInitialFileName(selectedBigraphERFile.getName());

			String folder = selectedBigraphERFile.getAbsolutePath().substring(0,
					selectedBigraphERFile.getAbsolutePath().lastIndexOf(File.separator));
			File folderF = new File(folder);

			if (folderF.isDirectory()) {
				fileChooser.setInitialDirectory(folderF);
			}
		}

		// if first time
		if (traceMiner != null && (traceMiner.getIncidentPatternFilePath() == null
				|| traceMiner.getIncidentPatternFilePath().isEmpty())) {
			ButtonType result = showDialog("Select Incident Pattern File", "Incident Pattern file is needed",
					"Please select [incident pattern file] (*.cpi)", AlertType.CONFIRMATION, true);

			if (result == ButtonType.CANCEL) {
				return;
			}
		}

		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Incident Pattern (*.cpi)", "*.cpi");

		fileChooser.getExtensionFilters().add(extFilter);

		File selectedTracesFile = fileChooser.showOpenDialog(null);

		if (selectedTracesFile != null) {
			traceMiner.setIncidentPatternFilePath(selectedTracesFile.getAbsolutePath());
		}
	}

	/**
	 * Select BigraphER file (*.big)
	 */
	public void selectBigraphERFile() {
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
			ButtonType result = showDialog("Select BigraphER File", "BigraphER file needed",
					"Please select [BigraphER file] (*.big)", AlertType.CONFIRMATION, true);

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
	public void selectStatesFolder() {
		DirectoryChooser dirChooser = new DirectoryChooser();

		// show folder if any
		if (traceMiner != null && traceMiner.getStatesFolder() != null) {
			File selectedStatesFolder = new File(traceMiner.getStatesFolder());

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

		if (traceMiner.getStatesFolder() == null) {

			ButtonType result = showDialog("Select States Folder", "States Folder is needed",
					"Please select a Folder which contains the states representations (e.g., *.svg, *.json, *.txt)",
					AlertType.CONFIRMATION, true);

			if (result == ButtonType.CANCEL) {
				return;
			}
		}

		File selectedStatesFolder = dirChooser.showDialog(null);

		if (selectedStatesFolder != null) {
			traceMiner.setStatesFolder(selectedStatesFolder.getAbsolutePath());
			// statesFolder = selectedStatesFolder.getAbsolutePath();
		}
		// }

	}

	public void selectTraceFolder() {
		DirectoryChooser dirChooser = new DirectoryChooser();

		// show folder if any
		if (traceMiner != null && traceMiner.getTraceFolder() != null) {
			File selectedStatesFolder = new File(traceMiner.getTraceFolder());

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

		File selectedStatesFolder = dirChooser.showDialog(null);

		if (selectedStatesFolder != null) {
			traceMiner.setTraceFolder(selectedStatesFolder.getAbsolutePath());
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
		lblTraceID.setStyle(normalStyle);

		hbox.getChildren().clear();
		// vboxMain.getChildren().clear();
		rootPane.getChildren().clear();

		// re-add
		// vboxMain.getChildren().add(splitPaneTrace);
		rootPane.getChildren().add(hboxTrace);
		if (traceDetailsMainPane != null) {
			rootPane.getChildren().add(traceDetailsMainPane);
		}
		// rootPane.getChildren().add(vboxMain);
		// rootPane.getChildren().add(hboxOptions);
	}

	protected void populateCell(GraphPath trace) {

		clearData();
		// traceDetailsMainPane = null;

		if (trace == null) {
			return;
		}

		this.trace = trace;

		int index = 0;
		int size = trace.getStateTransitions().size() - 1;
		List<Integer> states = trace.getStateTransitions();
		List<String> actions = trace.getTransitionActions();
		StringBuilder strBldr = new StringBuilder();

		// System.out.println("states: "+states);
		// System.out.println("actions: "+actions);
		// set states

		// trace id
		strBldr.append("Trace [" + trace.getInstanceID() + "]: ");

		for (Integer state : states) {
			// Circle circle = new Circle(hbox.getHeight()-2);
			Label lblState;
			Label lblAction;
			if (index != size) {

				lblState = new Label(state + "");

				strBldr.append(state);

				String act = actions.get(index);
				lblAction = new Label(" =[" + act + "]=> ");

				lblAction.setStyle(normalStyle);

				lblAction.setOnMouseClicked(e -> {
					showReact(act);
				});

				lblAction.setOnMouseEntered(e -> {
					// lblAction.setStyle("-fx-font-weight: bold;");
					// lblAction.setStyle("-fx-text-fill: black;
					// -fx-font-size:14px;");
					lblAction.setStyle(mouseEnteredStyle);
					lblAction.setCursor(Cursor.HAND);
				});

				lblAction.setOnMouseExited(e -> {
					lblAction.setStyle(normalStyle);
					// lblAction.setStyle("-fx-font-weight: normal;");
				});

				strBldr.append(" =[" + actions.get(index) + "]=> ");
			} else {
				lblState = new Label(state + "");
				strBldr.append(state);
				lblAction = null;
			}

			lblState.setStyle(normalStyle);

			// open state .svg, .json ...
			lblState.setOnMouseClicked(e -> {

				showState(state);

			});

			lblState.setOnMouseEntered(e -> {
				lblState.setStyle(mouseEnteredStyle);
				lblState.setCursor(Cursor.HAND);
			});

			lblState.setOnMouseExited(e -> {
				lblState.setStyle(normalStyle);
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

		lblTraceID.setOnMouseEntered(e -> {
			lblTraceID.setStyle(mouseEnteredStyle);
			lblTraceID.setCursor(Cursor.HAND);
		});

		lblTraceID.setOnMouseExited(e -> {
			lblTraceID.setStyle(normalStyle);
		});

		lblTraceID.setOnMouseClicked(e -> {
			showTrace();
		});
		// id
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				lblTraceID.setText(trace.getInstanceID() + "");

				// tooltip
				Tooltip tip = new Tooltip(strBldr.toString());
				lblTraceID.setTooltip(tip);

				if (traceMiner != null && traceMiner.isTraceSaved(trace.getInstanceID())) {
					lblTraceID.setStyle(boldStyle);
				}

				// hbox.getChildren().clear();
				Pane pane = new Pane();
				pane.setPrefWidth(10);
				hbox.getChildren().add(pane);

			}
		});

		// tooltip that holds the whole trace states and actions
		// Platform.runLater(new Runnable() {
		//
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		// // add tooltip
		//
		// // set the location of the option box
		// // hboxOptions.set
		// Tooltip tip = new Tooltip(strBldr.toString());
		// lblTraceID.setTooltip(tip);
		// }
		// });

	}

	protected void showState(int state) {

		if (traceMiner == null) {
			return;
		}

		if (stateController == null) {
			loadStateController();
		}

		if (traceMiner.getStatesFolder() == null) {
			selectStatesFolder();

		}

		// if folder not set return
		if (traceMiner.getStatesFolder() == null) {
			return;
		}

		// try to find a state representation as in stateExtension
		String statesFolder = traceMiner.getStatesFolder();
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
			ButtonType res = showDialog("File not found", "State [" + state + "] file is missing",
					"File not found for state [" + state + "]. Would you Like to select another Folder?",
					AlertType.CONFIRMATION, true);

			if (res == ButtonType.OK) {
				selectStatesFolder();
				showState(state);
				return;
			} else {
				return;
			}

			// return;
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

		if (traceMiner == null) {
			System.err.println("Trace Miner is NUll");
			return;
		}

		if (!traceMiner.isBigraphERFileSet()) {
			selectBigraphERFile();
		}

		if (!traceMiner.isBigraphERFileSet()) {
			return;
		}

		if (reactController == null) {
			loadReactController();
		}

		if (reactController == null) {
			return;
		}

		reactController.showReact(actionName);

		reactViewerStage.setTitle("Action Viewer");

		if (!reactViewerStage.isShowing()) {
			reactViewerStage.show();
			reactViewerStage.setAlwaysOnTop(true);
		}
	}

	protected ButtonType showDialog(String title, String headerMsg, String msg, AlertType type, boolean showAndWait) {

		Alert alert = new Alert(type);

		alert.setTitle(title);

		alert.setHeaderText(headerMsg);

		alert.setContentText(msg);

		if (showAndWait) {
			alert.showAndWait();
		} else {
			alert.show();
		}

		return alert.getResult();
	}

}