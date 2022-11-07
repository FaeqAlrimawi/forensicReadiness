package controller.instantiation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.eteks.sweethome3d.adaptive.forensics.BigrapherStatesChecker;
import com.eteks.sweethome3d.adaptive.forensics.SystemHandler;

import controller.instantiation.analysis.TaskCell;
import controller.instantiation.analysis.TraceViewerController;
import controller.utlities.IncidentAssetMap;
import core.instantiation.analysis.TraceMiner;
import ie.lero.spare.pattern_instantiation.GraphPath;
import ie.lero.spare.pattern_instantiation.GraphPathsAnalyser;
import ie.lero.spare.pattern_instantiation.IncidentPatternInstantiationListener;
import ie.lero.spare.pattern_instantiation.IncidentPatternInstantiator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class InstantiatorController
		implements ie.lero.spare.pattern_instantiation.IncidentPatternInstantiationListener {

	@FXML
	private Button btnTraceAnalyser;

	@FXML
	private Label lblLastMsg;

	@FXML
	private Label lblSave;

	@FXML
	private Button btnSaveGeneratedTraces;

	@FXML
	private Label lblNumOfTraces;

	@FXML
	private HBox hboxLblEntityAssetSets;

	@FXML
	private HBox hboxLblEntityAssetMap;

	@FXML
	private Label lblListViewTracesEmpty;

	@FXML
	private ListView<GraphPath> listViewTraces;

//	@FXML
//	private ImageView imgOpenStatesFolder;
	@FXML
	private Button btnOpenStatesFolder;

	@FXML
	private CheckBox checkboxLTSSame;

	@FXML
	private ProgressBar progressBarAnalyse;

	@FXML
	private Label lblTransitionCheck;

	@FXML
	private Label lblStatesCheck;

	@FXML
	private ImageView imgStatesCheck;

	@FXML
	private ImageView imgTransitionCheck;

	@FXML
	private TextField textFieldSelectedStatesFolder;

//	@FXML
//	private ImageView imgSelectIncidentPattern;

	@FXML
	private TextField txtFieldIncidentPattern;

	@FXML
	private Button btnIncidentPatternBrowse;

	@FXML
	private TextField txtFieldSystemModel;

	@FXML
	private Button btnSystemModelBrowse;

	@FXML
	private Spinner<Integer> instancesSpinner;

	@FXML
	private Spinner<Integer> activitiesSpinner;

	@FXML
	private Spinner<Integer> matchingSpinner;

	@FXML
	private Button btnGenerateInstances;

	@FXML
	private TextArea txtAreaLog;

	@FXML
	private ProgressIndicator progressBar;

	@FXML
	private Label lblProgressBar;

	@FXML
	private TableView<IncidentAssetMap> tableViewMap;

//	@FXML
//	private Label lblEntityAssetMap;

	// @FXML
	// private Pane panelEntityAssetMap;

	@FXML
	private ListView<CheckBox> listViewSets;

	@FXML
	private Button btnAnalyse;

	@FXML
	private Label lblInputFiles;

	@FXML
	private ListView<String> listResults;

//	private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private static final String IMAGES_FOLDER = "resources/images/";
	private static final String IMAGE_CORRECT = IMAGES_FOLDER + "correct.png";
	private static final String IMAGE_WRONG = IMAGES_FOLDER + "wrong.png";
	private static final int INTERVAL = 3000;
	private static final int FILE_MENU = 0;

	private File incidentPatternFile;
	private File systemFile;
	private File selectedStatesDirectory;
	private File selectedSaveGeneratedTracesFile;

	private String incidentPatternFilePath = "F:/Bigrapher data/lero/example/int.cpi";
	private String systemModelFilePath = "F:/Bigrapher data/lero/example/lero.cps";
	private IncidentPatternInstantiator incidentInstantiator;
	private int numOfParallelInstances = 1;
	private Thread instanceThread;
	private InstantiatorController instance;
	private LinkedList<String> incidentNames;
	private LinkedList<String[]> assetNames;
	private ObservableList<IncidentAssetMap> data;
	private int multiplicationValue = 100;
	private String newLine = "\n";
	private CheckBox[] checkBoxsAssetSets;
	private HashMap<Integer, GraphPathsAnalyser> results = new HashMap<Integer, GraphPathsAnalyser>();

	private int currentSetID = -1;

	private BigrapherStatesChecker checker;

	private List<GraphPath> generatedTraces;

	private String msgSelectAssetSet = "Please select Asset Sets to instantiate";
	private String msgDone = "Done";
	private static final String INITIAL_LABEL = "Instantiating... ";

	private TraceMiner miner;
	private TraceViewerController traceViewerController;
	private Stage traceViewerStage;

	public InstantiatorController() {
		incidentNames = new LinkedList<String>();
		assetNames = new LinkedList<String[]>();

	}

	@FXML
	public void initialize() {

		// initialize gui if necessary
		// ObservableList<String> items = FXCollections.observableArrayList();
		// listResults.setItems(items);

		progressBar.setVisible(false);

		checkboxLTSSame.setOnAction(e -> {
			if (checkboxLTSSame.isSelected()) {
				textFieldSelectedStatesFolder.setDisable(true);
				btnOpenStatesFolder.setDisable(true);
			} else {
				textFieldSelectedStatesFolder.setDisable(false);
				btnOpenStatesFolder.setDisable(false);
			}
		});

		// === for testing
		incidentPatternFile = new File(incidentPatternFilePath);
		systemFile = new File(systemModelFilePath);
		txtFieldIncidentPattern.setText(incidentPatternFilePath);
		txtFieldSystemModel.setText(systemModelFilePath);

	}

	@FXML
	void saveGeneratedTraces(ActionEvent event) {

		// save filtered traces
		FileChooser fileChooser = new FileChooser();

		if (selectedSaveGeneratedTracesFile != null) {
			fileChooser.setInitialFileName(selectedSaveGeneratedTracesFile.getName());
			String folder = selectedSaveGeneratedTracesFile.getAbsolutePath().substring(0,
					selectedSaveGeneratedTracesFile.getAbsolutePath().lastIndexOf(File.separator));
			File folderF = new File(folder);

			if (folderF.isDirectory()) {
				fileChooser.setInitialDirectory(folderF);
			}

		} else if (systemFile != null) {
			if (systemFile != null) {
				fileChooser.setInitialFileName("traces");
				String folder = systemFile.getAbsolutePath().substring(0,
						systemFile.getAbsolutePath().lastIndexOf(File.separator));
				File folderF = new File(folder);

				if (folderF.isDirectory()) {
					fileChooser.setInitialDirectory(folderF);
				}

			}

		}

		// set extension to be of system model (.cps)
		// fileChooser.setSelectedExtensionFilter(new ExtensionFilter("System
		// model files (*.cps)",".cps"));
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");

		fileChooser.getExtensionFilters().add(extFilter);

		selectedSaveGeneratedTracesFile = fileChooser.showSaveDialog(null);

		if (selectedSaveGeneratedTracesFile != null) {
			// Platform.runLater(new Runnable() {
			//
			// @Override
			// public void run() {
			// // TODO Auto-generated method stub
			//// textFieldSystemFile.setText(selectedTracesFile.getAbsolutePath());
			// }
			// });

			saveTraces(currentSetID, generatedTraces, selectedSaveGeneratedTracesFile.getAbsolutePath());
		}

	}

	@FXML
	void selectIncidentPattern(ActionEvent event) {

		FileChooser fileChooser = new FileChooser();

		if (incidentPatternFile != null) {
			fileChooser.setInitialFileName(incidentPatternFile.getAbsolutePath());

			String folder = incidentPatternFile.getAbsolutePath().substring(0,
					incidentPatternFile.getAbsolutePath().lastIndexOf(File.separator));
			File folderF = new File(folder);

			if (folderF.isDirectory()) {
				fileChooser.setInitialDirectory(folderF);
			}
		}

		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Cyber Physical Incident files (*.cpi)",
				"*.cpi");

		fileChooser.getExtensionFilters().add(extFilter);

		incidentPatternFile = fileChooser.showOpenDialog(null);

		if (incidentPatternFile != null) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					txtFieldIncidentPattern.setText(incidentPatternFile.getAbsolutePath());
				}
			});

			loadIncidentPattern(incidentPatternFile.getAbsolutePath());
		}
	}

	@FXML
	void instantiateIncidentPattern(ActionEvent event) {

		createInstance(this);

		btnGenerateInstances.setDisable(true);
	}

	@FXML
	void selectSystemFile(ActionEvent event) {

		FileChooser fileChooser = new FileChooser();

		if (systemFile != null) {
			fileChooser.setInitialFileName(systemFile.getName());

			String folder = systemFile.getAbsolutePath().substring(0,
					systemFile.getAbsolutePath().lastIndexOf(File.separator));
			File folderF = new File(folder);

			if (folderF.isDirectory()) {
				fileChooser.setInitialDirectory(folderF);
			}

		}

		// set extension to be of system model (.cps)
		// fileChooser.setSelectedExtensionFilter(new ExtensionFilter("System
		// model files (*.cps)",".cps"));
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Cyber Physical System files (*.cps)",
				"*.cps");

		fileChooser.getExtensionFilters().add(extFilter);

		systemFile = fileChooser.showOpenDialog(null);

		if (systemFile != null) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					txtFieldSystemModel.setText(systemFile.getAbsolutePath());
				}
			});

			if (isSystemFileValid()) {

				// updateImage(IMAGE_CORRECT, imgSystemFileCheck);
				// updateText("System model is valid", lblSystemFileCheck);
				// btnUpdateSystemModel.setDisable(false);
				// btnEditActions.setDisable(false);
				// imgOpenBigrapher.setVisible(true);
				// imgOpenBigrapherEmpty.setVisible(false);

				if (incidentPatternFile != null) {
					btnGenerateInstances.setDisable(false);
				}

			} else {
				// updateImage(IMAGE_WRONG, imgSystemFileCheck);
				// updateText("System model is not valid", lblSystemFileCheck);
				// btnUpdateSystemModel.setDisable(true);
				// imgOpenBigrapher.setVisible(false);
				// imgOpenBigrapherEmpty.setVisible(true);
				// imgRefresh.setVisible(false);
				// imgRefreshEmpty.setVisible(true);

				// btnGenerateIncidentPattern.setDisable(true);

			}

			// remove the check image and text after a few secs
			// new Timer().schedule(new TimerTask() {
			//
			// @Override
			// public void run() {
			// // TODO Auto-generated method stub
			// updateImage(null, imgSystemFileCheck);
			// updateText("", lblSystemFileCheck);
			// }
			// }, INTERVAL);
			// }

			txtFieldSystemModel.requestFocus();
		}

	}

	protected boolean isSystemFileValid() {

		// try creating an object from the file
		SystemHandler.setFilePath(systemFile.getAbsolutePath());

		boolean isValid = SystemHandler.generateSystemModel();

		return isValid;

	}

	// @FXML
	// void browse(ActionEvent event) {
	//
	// // if browse incident pattern button is pressed
	// if (((Button)
	// event.getSource()).getId().equals(btnIncidentPatternBrowse.getId())) {
	// JFileChooser fc = new JFileChooser();
	// int returnVal = fc.showOpenDialog(null);
	//
	// if (returnVal == JFileChooser.APPROVE_OPTION) {
	// File file = fc.getSelectedFile();
	// incidentPatternFilePath = file.toURI().toString();
	// txtFieldIncidentPattern.setText(file.getName());
	// }
	//
	// // if browse system model button is pressed
	// } else if (((Button)
	// event.getSource()).getId().equals(btnSystemModelBrowse.getId())) {
	// JFileChooser fc = new JFileChooser();
	// int returnVal = fc.showOpenDialog(null);
	//
	// if (returnVal == JFileChooser.APPROVE_OPTION) {
	// File file = fc.getSelectedFile();
	// systemModelFilePath = file.toURI().toString();
	// txtFieldSystemModel.setText(file.getName());
	// }
	// // if generate instances button is pressed
	// } else if (((Button)
	// event.getSource()).getId().equals(btnGenerateInstances.getId())) {
	//
	// createInstance(this);
	// }
	//
	// }

	protected void loadIncidentPattern(String filePath) {

	}

	public void createInstance(IncidentPatternInstantiationListener listener) {

		if (incidentPatternFile == null) {
			showDialog(AlertType.ERROR, "Error", "Incident pattern file is not set",
					"Please select an incident pattern file");
			return;
		} else if (systemFile == null) {
			showDialog(AlertType.ERROR, "Error", "System file is not set", "Please select a system file");
		} else {
			// File tmp;
			incidentPatternFilePath = incidentPatternFile.getAbsolutePath();
			systemModelFilePath = systemFile.getAbsolutePath();

			// try {
			//
			// // check if incident pattern file exists
			// tmp = new File(incidentPatternFilePath);
			//
			// if (!tmp.exists()) {
			// showDialog(AlertType.ERROR, "Error", "Incident pattern file Not
			// Found",
			// "Please check that the file path for the incident pattern is
			// correct. \nIncident pattern file path: "
			// + incidentPatternFilePath);
			// return;
			// }
			//
			// // check if system model file exists
			// tmp = new File(systemModelFilePath);
			//
			// if (!tmp.exists()) {
			// showDialog(AlertType.ERROR, "Error", "System model file Not
			// Found",
			// "Please check that the file path for the system model is correct.
			// \nSystem model file path: "
			// + systemModelFilePath);
			// return;
			// }
			// } catch (Exception e) {
			//
			// }
		}

		Runnable instance2 = new Runnable() {
			@Override
			public void run() {
				incidentInstantiator = new IncidentPatternInstantiator();
				if (checkboxLTSSame.isSelected()) {
					incidentInstantiator.execute(incidentPatternFilePath, systemModelFilePath, listener);
				} else {
					String ltsFolder = selectedStatesDirectory.getAbsolutePath();
//					int index = systemModelFilePath.lastIndexOf(File.separator);

					String systemName = systemFile.getName();
					String bigrapherFile = null;

					if (systemName.contains(".cps")) {
						bigrapherFile = systemFile.getName().replace(".cps", ".big");
					}

//					if(index>0) {
//						bigrapherFile = systemModelFilePath.replace(".cps", ".big")	
//					}
//					 
					incidentInstantiator.execute(incidentPatternFilePath, systemModelFilePath, bigrapherFile, ltsFolder,
							listener);
				}

			}

		};

		instanceThread = new Thread(instance2);
		instanceThread.start();

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				txtAreaLog.appendText("\n");
//				progressBar.setProgress(0);
				progressBar.setVisible(true);
				updateProgressMessage("Preparing Instantiation...");
				// lblInputFiles.setText("[Input incident file]: " +
				// incidentPatternFilePath + " "
				// + "[Input system file]: " + systemModelFilePath);
			}
		});

	}

	@FXML
	void selectStatesFolder(ActionEvent event) {

		DirectoryChooser chooser = new DirectoryChooser();
		// chooser.setTitle("Select Folder");
		if (selectedStatesDirectory != null) {
			chooser.setInitialDirectory(selectedStatesDirectory);
		}

		selectedStatesDirectory = chooser.showDialog(null);

		if (selectedStatesDirectory != null) {

			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					textFieldSelectedStatesFolder.setText(selectedStatesDirectory.getAbsolutePath());
				}
			});

			// clear check pane
			updateTransitionCheckingPane(null, null);
			updateStatesCheckingPane(null, null);

			// check states and transitions
			checkStates();
		}

	}

	@FXML
	void openInTraceAnalyser(ActionEvent e) {

		
		if (traceViewerStage == null) {
			loadTraceViewerController();

			if (traceViewerStage != null) {
				if (miner != null) {
					traceViewerController.setTraceMiner(miner);	
					traceViewerController.loadTraces();
//					traceViewerController.viewTraces(miner.getAllTracesIDs());
				}

				traceViewerStage.show();
			}
		} else {
			if (!traceViewerStage.isShowing()) {
				traceViewerStage.show();
			}
		}

	}

	private void loadTraceViewerController() {

		FXMLLoader fxmlLoader = new FXMLLoader(
				InstantiatorController.class.getClassLoader().getResource("fxml/TraceViewer.fxml"));
		Parent root;
		try {
			root = (Parent) fxmlLoader.load();
			traceViewerStage = new Stage();
			traceViewerStage.setScene(new Scene(root));

			// get controller
			traceViewerController = fxmlLoader.<TraceViewerController>getController();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void checkStates() {

		if (selectedStatesDirectory == null) {
			return;
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				// analyse folder for states and transition system
				checker = new BigrapherStatesChecker();
				int result = checker.checkStates(selectedStatesDirectory.getAbsolutePath());
				doneStateChecking(result);
			}
		}).start();

		progressBarAnalyse.setVisible(true);

	}

	protected void doneStateChecking(int result) {

		progressBarAnalyse.setVisible(false);

		switch (result) {
		// everything is fine
		case BigrapherStatesChecker.PASS:
			updateTransitionCheckingPane(IMAGE_CORRECT, "Transitions #: " + checker.getTransitionsNumber());
			updateStatesCheckingPane(IMAGE_CORRECT, "States #: " + checker.getStatesNumber());
			break;

		// no transition file
		case BigrapherStatesChecker.TRANSITION_FILE_MISSING:
			updateTransitionCheckingPane(IMAGE_WRONG, "[transitions.json] File is missing");
			break;

		// missing states
		case BigrapherStatesChecker.STATES_MISSING:
			updateTransitionCheckingPane(IMAGE_CORRECT, "Transitions #: " + checker.getTransitionsNumber());
			updateStatesCheckingPane(IMAGE_WRONG, "Some states are missing: " + checker.getStatesNotFound());
			break;

		default:
			break;
		}

	}

	@Override
	public void updateProgress(int progress) {
		// TODO Auto-generated method stub
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
//				double newProgress = progressBar.getProgress() + ((double) progress / 100.0);
//				progressBar.setProgress(newProgress);
//
//				if (newProgress >= 1) {
//					lblProgressBar.setTextFill(Color.GREEN);
//					lblProgressBar.setText("Execution Completed Successfully");
//				}
			}
		});

	}

	@Override
	public void updateLogger(String msg) {
		// TODO Auto-generated method stub
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				lblLastMsg.setText(msg);
				lblLastMsg.setTooltip(new Tooltip(msg));
//				lblProgressBar.setText(msg);
//				lblProgressBar.setTooltip(new Tooltip(msg));
				updateProgressMessage(msg);
				txtAreaLog.appendText(msg + newLine);
			}
		});

	}

	protected void updateProgressMessage(String msg) {

		String currentText = lblProgressBar.getText();

		currentText = currentText.replace(INITIAL_LABEL, "");

		if (msg.contains(">>")) {
			// process msg to get last part
			String[] parts = msg.split(">>");

			msg = parts[parts.length - 1];
		}

		if (currentText != null && !(currentText.equals(msgSelectAssetSet) || currentText.equals(msgDone))) {
			lblProgressBar.setText(INITIAL_LABEL + msg);
		}
		
		//if select asset set, then make list border red
		if(currentText.equals(msgSelectAssetSet)) {
			listViewSets.setStyle("-fx-border-color:red;");
		}

	}

	@Override
	public void updateAssetMapInfo(String msg) {

		hboxLblEntityAssetMap.setVisible(false);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {

				// hide map pane
				// panelEntityAssetMap.setVisible(false);

				TableColumn<IncidentAssetMap, String> colInc = new TableColumn<IncidentAssetMap, String>();
				colInc.setCellValueFactory(IncidentAssetMap.getIncidentNameVariable());
				colInc.setText("Incident Entity");
				colInc.setPrefWidth((tableViewMap.getPrefWidth() * 0.40));

				TableColumn<IncidentAssetMap, String> colAst = new TableColumn<IncidentAssetMap, String>();
				colAst.setCellValueFactory(IncidentAssetMap.getAssetNamesVariable());
				colAst.setText("System Asset");
				colAst.setPrefWidth(tableViewMap.getPrefWidth() - colInc.getPrefWidth());

				tableViewMap.getColumns().add(colInc);
				tableViewMap.getColumns().add(colAst);
				tableViewMap.setVisible(true);
			}
		});

		String[] tmp = msg.split(";");
		ObservableList<IncidentAssetMap> data = FXCollections.observableArrayList();

		for (String t : tmp) {
			String[] tmp2 = t.split(":");
			if (tmp2 != null && tmp2.length > 1) {
				incidentNames.add(tmp2[0]);
				String[] tmp3 = tmp2[1].split(",");
				assetNames.add(tmp3);
				// tmp2[1] = tmp2[1].replaceAll(",", "::");
				data.add(new IncidentAssetMap(tmp2[0], tmp2[1]));
			}
		}

		if (data.size() > 0) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					// tableViewMap.setit

					tableViewMap.setItems(data);

				}
			});
		}
	}

	@Override
	public void updateAssetSetInfo(LinkedList<String[]> assetSets) {
		// TODO Auto-generated method stub

		// hide label
//		lbl.setVisible(false);
		hboxLblEntityAssetSets.setVisible(false);

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				ObservableList<CheckBox> data = FXCollections.observableArrayList();

				// add all & none checkboxes
				checkBoxsAssetSets = new CheckBox[assetSets.size() + 2];

				checkBoxsAssetSets[0] = new CheckBox("All (" + assetSets.size() + ")");
				checkBoxsAssetSets[1] = new CheckBox("None");

				checkBoxsAssetSets[0].setOnAction(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent event) {
						// TODO Auto-generated method stub
						if (checkBoxsAssetSets[0].isSelected()) {
							checkBoxsAssetSets[1].setSelected(false);
							for (int i = 2; i < checkBoxsAssetSets.length; i++) {
								checkBoxsAssetSets[i].setSelected(true);
							}
						}
					}
				});

				checkBoxsAssetSets[1].setOnAction(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent event) {
						// TODO Auto-generated method stub
						if (checkBoxsAssetSets[1].isSelected()) {
							checkBoxsAssetSets[0].setSelected(false);
							for (int i = 2; i < checkBoxsAssetSets.length; i++) {
								checkBoxsAssetSets[i].setSelected(false);
							}
						}
					}
				});

				// checkBoxsAssetSets[0].setSelected(true);
				data.add(checkBoxsAssetSets[0]);
				data.add(checkBoxsAssetSets[1]);

				for (int i = 0; i < assetSets.size(); i++) {
					checkBoxsAssetSets[i + 2] = new CheckBox("set[" + i + "]: " + Arrays.toString(assetSets.get(i)));

					checkBoxsAssetSets[i + 2].setOnAction(new EventHandler<ActionEvent>() {

						@Override
						public void handle(ActionEvent event) {
							// TODO Auto-generated method stub
							if (((CheckBox) event.getSource()).isSelected()) {
								checkBoxsAssetSets[1].setSelected(false);
							} else if (!((CheckBox) event.getSource()).isSelected()) {
								checkBoxsAssetSets[0].setSelected(false);
							}

						}
					});

					data.add(checkBoxsAssetSets[i + 2]);
				}

				// panelAssetSets.setVisible(false);
				// TODO Auto-generated method stub
				listViewSets.setItems(data);
			}
		});

		// enable spinners and analyse button
		enableAnalysisGUI();
	}

	private void enableAnalysisGUI() {

		int minValue = 1;
		int maxValue = Runtime.getRuntime().availableProcessors();
		int initialValue = 1;

		int minMatchingValue = 1;
		int maxMatchingValue = 20;
		int initialMatchingValue = 1;

		SpinnerValueFactory<Integer> instancesValue = new IntegerSpinnerValueFactory(minValue, maxValue, initialValue);
		SpinnerValueFactory<Integer> activitiesValue = new IntegerSpinnerValueFactory(minValue, maxValue, initialValue);
		SpinnerValueFactory<Integer> matchingValue = new IntegerSpinnerValueFactory(minMatchingValue, maxMatchingValue,
				initialMatchingValue);

		instancesSpinner.setValueFactory(instancesValue);
		instancesSpinner.setDisable(false);

		activitiesSpinner.setValueFactory(activitiesValue);
		activitiesSpinner.setDisable(false);

		matchingSpinner.setValueFactory(matchingValue);
		matchingSpinner.setDisable(false);

		btnAnalyse.setDisable(false);

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				lblProgressBar.setTextFill(Color.RED);
				updateProgressMessage(msgSelectAssetSet);
				listViewSets.setStyle("-fx-border-color:red;");
				progressBar.setVisible(false);
			}
		});
	}

	@FXML
	void analyseInstances(ActionEvent event) {

		LinkedList<Integer> selectedSets = new LinkedList<Integer>();

		// starts from two to skip All & None options
		for (int i = 2; i < listViewSets.getItems().size(); i++) {
			CheckBox item = listViewSets.getItems().get(i);

			if (item.isSelected()) {
				selectedSets.add(i - 2);
			}
		}

		if (selectedSets.isEmpty()) {
			// show warning
			showDialog(AlertType.WARNING, "Warning", "No sets were selected",
					"You have not selected any sets. Please select at least one set");
			return;
		}

		incidentInstantiator.setAssetSetsSelected(selectedSets);
		incidentInstantiator.setThreadPoolSize(instancesSpinner.getValue());
		incidentInstantiator.setNumberOfParallelActivities(activitiesSpinner.getValue());
		// incidentInstantiator.setMatchingThreshold(matchingSpinner.getValue()
		// * multiplicationValue);
		incidentInstantiator.setSetsSelected(true);

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				lblProgressBar.setTextFill(Color.BLUE);
				lblProgressBar.setText("Instantiating... ");
				listViewSets.setStyle(null);
				progressBar.setVisible(true);
				btnAnalyse.setDisable(true);
			}
		});

	}

	// private void showDialog(String title, String) {
	//
	// Alert alert = new Alert(AlertType.WARNING);
	// alert.setTitle("Warning");
	// alert.setHeaderText("No sets are selected");
	// alert.setContentText("You have not selected any sets. Please select at
	// least one set");
	//
	// alert.showAndWait();
	//
	// }

	private void showDialog(AlertType alertType, String title, String header, String content) {

		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);

		alert.showAndWait();
	}

	public InstantiatorController getInstance() {
		return instance;
	}

	public void setInstance(InstantiatorController instance) {
		this.instance = instance;
	}

	// @Override
	// public void start(Stage primaryStage) throws Exception {
	//
	// try {
	// // TODO Auto-generated method stub
	// /*
	// * BorderPane root = new BorderPane(); Scene scene = new
	// * Scene(root,400,400);
	// * scene.getStylesheets().add(getClass().getResource(
	// * "application.css").toExternalForm());
	// */
	// FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
	//
	// Parent root = loader.load();
	//
	// setInstance(loader.<InstantiatorController>getController());
	//
	// // intiate the spinners
	//
	// // Platform.setImplicitExit(false);
	// Scene scene = new Scene(root);
	// // scene.setFill(Color.TRANSPARENT);
	// primaryStage.setScene(scene);
	// primaryStage.initStyle(StageStyle.DECORATED);
	// primaryStage.setTitle("Potential Incident Generator");
	// primaryStage.show();
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	//
	// public static void main(String[] args) {
	//
	// launch(args);
	//
	// }

	public void stop() {

	}

	@Override
	public void updateResult(int setID, GraphPathsAnalyser graphAnalyser, String outputFile, String timeConsumed) {
		// TODO Auto-generated method stub

		results.put(setID, graphAnalyser);
		currentSetID = setID;

		progressBar.setVisible(false);

		String strResult = "Set[" + setID + "] finished execution successfully. Execution time: " + timeConsumed;

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// lblProgressBar.setTextFill(Color.BLACK);
//				listResults.getItems().add(strResult);
				if (graphAnalyser != null) {
					lblNumOfTraces.setText("[" + graphAnalyser.getPaths().size() + "]");
					populateTracesList(graphAnalyser.getPaths());
					btnSaveGeneratedTraces.setDisable(false);
					btnTraceAnalyser.setDisable(false);
				}

				updateProgressMessage(msgDone);
				btnGenerateInstances.setDisable(false);

			}
		});

	}

	protected void updateTransitionCheckingPane(String transitionImg, String transitionMsg) {

		updateImage(transitionImg, imgTransitionCheck);

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (transitionMsg != null) {
					lblTransitionCheck.setText(transitionMsg);
					lblTransitionCheck.setTooltip(new Tooltip(transitionMsg));
				} else {
					lblTransitionCheck.setText("");
				}

			}
		});

	}

	protected void updateStatesCheckingPane(String statesImg, String statesMsg) {

		updateImage(statesImg, imgStatesCheck);

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (statesMsg != null) {
					lblStatesCheck.setText(statesMsg);
					lblStatesCheck.setTooltip(new Tooltip(statesMsg));
				} else {
					lblStatesCheck.setText("");
				}

			}
		});

	}

	protected void updateImage(String imgPath, ImageView imgView) {

		if (imgView == null) {
			return;
		}

		if (imgPath == null) {
			imgView.setVisible(false);

		} else {

			imgView.setVisible(true);

			URL urlImage = getClass().getClassLoader().getResource(imgPath);

			if (urlImage != null) {
				Image img;
				try {
					img = new Image(urlImage.openStream());
					imgView.setImage(img);
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}

			} else {
				System.out.println(imgPath + " Not found!");
			}

		}

	}

	protected void populateTracesList(List<GraphPath> traces) {

		if (traces == null || traces.isEmpty()) {
			lblListViewTracesEmpty.setVisible(true);
			generatedTraces = traces;
			// clear list
			listViewTraces.setItems(null);
			return;
		}

		// create a trace miner
		miner = new TraceMiner();
		miner.loadTracesFromList(traces);
		miner.setCurrentShownTraces(miner.getAllTracesIDs());

		ObservableList<GraphPath> tracesObservableList;

		tracesObservableList = FXCollections.observableArrayList();

		tracesObservableList.addAll(traces);

		listViewTraces.setCellFactory(tracesListView -> new TaskCell(miner, listViewTraces));
		listViewTraces.setItems(tracesObservableList);

		lblListViewTracesEmpty.setVisible(false);

		generatedTraces = traces;

	}

	protected void saveTraces(int setID, List<GraphPath> traces, String fileName) {

		if (fileName == null || traces == null) {
			return;
		}

		final boolean isSuccessful = incidentInstantiator.saveGeneratedTraces(setID, traces, fileName);

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				String result = "";
				if (isSuccessful) {
					result = "Saved!";
				} else {
					result = "Not saved!";
				}

				// TODO Auto-generated method stub
				lblSave.setText(result);
			}
		});
		Timer t = new Timer();
		t.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						lblSave.setText("");
					}
				});
			}
		}, 3000);

//		executor.submit(new Runnable() {
//
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				
//				
//				
//			}
//		});

	}

}
