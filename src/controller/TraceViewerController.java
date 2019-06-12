package controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import controller.utlities.AutoCompleteTextArea;
import core.TracesMiner;
import core.TracesMinerListener;
import ie.lero.spare.pattern_instantiation.GraphPath;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class TraceViewerController implements TracesMinerListener {

	@FXML
	private Label lblSaved;

	@FXML
	private ImageView imgSavedTraces;

	@FXML
	private Button btnSaveFilteredTraces;

	@FXML
	private TextField textFieldSystemFile;

	@FXML
	private ImageView imgSelectSystemFile;

	@FXML
	private ImageView imgOpentracesFileEmpty;

	@FXML
	private ImageView imgOpentracesFile;

	@FXML
	private ImageView imgSystemFileCheck;

	@FXML
	private Label lblSystemFileCheck;

	@FXML
	private Button btnAnalyse;

	// @FXML
	// private ChoiceBox<String> choiceboxFilter;

	@FXML
	private ComboBox<String> comboBoxFilterSelector;

	@FXML
	private ImageView imgFilter;

	@FXML
	private Label lblFilter;

	@FXML
	private Label lblListViewTracesEmpty;

	@FXML
	private ProgressIndicator progressIndicatorFilter;

	@FXML
	private ProgressIndicator progressIndicatorLoader;

	@FXML
	private Pane customisePane;

	// @FXML
	// private ChoiceBox<String> choiceboxSeqLengthComparator;

	@FXML
	private ComboBox<String> comboboxOccurrenceComparator;

	// @FXML
	// private TextField txtFieldLength;

	// @FXML
	// private TextField textFieldActions;

	@FXML
	private TextArea textAreaActions;

	@FXML
	private TextField textFieldActionOccurrence;

	@FXML
	private CheckBox checkBoxInOrder;

	@FXML
	private CheckBox checkboxFromStart;

	@FXML
	private BarChart<String, Integer> barChartActions;

	@FXML
	private CategoryAxis categoryAxis;

	@FXML
	private NumberAxis numberAxis;

	@FXML
	private TextField textFieldNumofOccurrences;

	@FXML
	private Button btnRefresh;

	@FXML
	private ComboBox<String> comboBoxOccurrences;

	@FXML
	private ImageView imgNumOfActions;

	@FXML
	private Label lblNumOfActions;

	@FXML
	private Label lblNumOfStates;

	@FXML
	private ImageView imgNumOfStates;

	@FXML
	private ChoiceBox<String> choiceBoxOccurrenceFilterPercentage;

	@FXML
	private TextField textFieldOccurrenceFilterPercentage;

	@FXML
	private ProgressBar progressBarTraces;

	@FXML
	private Label lblProgressTraces;

	@FXML
	private Spinner<Integer> spinnerFilterLength;

	@FXML
	private ComboBox<String> comboBoxChartFilterTraces;

	@FXML
	private ComboBox<String> comboBoxFilter;

	@FXML
	private ComboBox<String> comboboxSeqLengthComparator;

	@FXML
	private ListView<GraphPath> listViewTraces;

	private static final String IMAGES_FOLDER = "resources/images/";
	private static final String IMAGE_CORRECT = IMAGES_FOLDER + "correct.png";
	private static final String IMAGE_WRONG = IMAGES_FOLDER + "wrong.png";
	private static final int INTERVAL = 3000;
	// private static final int FILE_MENU = 0;

	private File selectedTracesFile;
	private File selectedFilteredTracesFile;

	// private JSONObject jsonTraces;
	private TracesMiner tracesMiner;

	private int numberOfTraces = -1;

	// used for progress bar
	// private double singleTraceProgressValue = 0.1;
	private int currentTraceNumber = 0;

	private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private static final String SHORTEST = "Shortest";
	private static final String SHORTEST_CLASP = "Shortest length & Share longest partial sequence (ClaSP)";
	private static final String CUSTOMISE = "Customise";
	private static final String HIGHEST = "Highest";
	private static final String LOWEST = "Lowest";
	private static final String PERCENTAGE = "percent";
	public static final String EQUAL = "=";
	public static final String MORE_THAN_EQUAL = ">=";
	// public static final String MORE_THAN = ">";
	// public static final String LESS_THAN = "<";
	public static final String LESS_THAN_EQUAL = "<=";
	// private static final int FILTER_LENGTH = 1;
	// private static final int FILTER_OCCURRENCE = 3;
	// private static final int FILTER_ACTIONS = 6;

	public static final String ACTIONS = "Actions";
	public static final String STATES = "States";
	public static final String ALL_TRACES = "All Traces";
	public static final String SHORTEST_TRACES = "Shortest Traces";
	public static final String SHORTEST_CLASP_TRACES = "Shortest ClaSP Traces";
	public static final String CUSTOMISED_TRACES = "Customised Traces";

	private List<Integer> shownFitleredTraces;

	// private AutoCompleteTextField autoCompleteActionsFiled;
	private AutoCompleteTextArea autoCompleteActionsArea;

	private final String[] filters = { SHORTEST, SHORTEST_CLASP, CUSTOMISE };

	private final String[] compartiveOperators = { MORE_THAN_EQUAL, LESS_THAN_EQUAL, EQUAL, };

	private final String[] occurrencesOptions = { HIGHEST, LOWEST };

	private final String[] FilterSelectors = { ACTIONS, STATES };

	private List<String> chartFilterTraces = new ArrayList<String>();

	String chartTitle = "";

	StateViewerController stateViewerController;
	
	@FXML
	public void initialize() {

		tracesMiner = new TracesMiner();

		shownFitleredTraces = new LinkedList<Integer>();
		// set miner listener
		tracesMiner.setListener(this);

		// update filters in combo box
		comboBoxFilter.setItems(FXCollections.observableArrayList(filters));

		// // update filters in choice box
		// choiceboxFilter.setItems(FXCollections.observableArrayList(filters));

		comboBoxFilter.valueProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String oldValue, String newValue) {
				// TODO Auto-generated method stub
				if (newValue.equals(CUSTOMISE)) {
					// enable customise pane
					setupCustomisePane();
				} else {
					if (!customisePane.isDisable()) {
						customisePane.setDisable(true);
					}

				}

			}
		});

		// set compartive operators
		comboboxOccurrenceComparator.setItems(FXCollections.observableArrayList(compartiveOperators));

		comboboxSeqLengthComparator.setItems(FXCollections.observableArrayList(compartiveOperators));

		choiceBoxOccurrenceFilterPercentage.setItems(FXCollections.observableArrayList(compartiveOperators));

		comboBoxOccurrences.setItems(FXCollections.observableArrayList(occurrencesOptions));

		// default selection
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				comboboxOccurrenceComparator.getSelectionModel().select(0);
				// choiceboxFilter.getSelectionModel().select(0);
				comboBoxOccurrences.getSelectionModel().select(0);
				choiceBoxOccurrenceFilterPercentage.getSelectionModel().select(0);
				comboboxSeqLengthComparator.getSelectionModel().select(0);

				//
				comboBoxFilter.getSelectionModel().select(0);
			}
		});

		// auto complete
		// autoCompleteActionsFiled = new AutoCompleteTextField();
		autoCompleteActionsArea = new AutoCompleteTextArea();

		textFieldSystemFile.setOnKeyPressed(e -> {

			// if enter is pressed then refersh
			if (e.getCode() == KeyCode.ENTER) {
				String filePath = textFieldSystemFile.getText();

				// check it is a file
				boolean isValid = tracesMiner.checkFile(filePath);

				if (isValid) {
					loadTracesFile(filePath);
				} else {
					showFiledError(textFieldSystemFile);
				}
			}
		});

		// filter actions/states in chart
		textFieldNumofOccurrences.setOnKeyPressed(e -> {

			// if enter is pressed then refersh
			if (e.getCode() == KeyCode.ENTER) {

				String selectedOccurrenceType = comboBoxOccurrences.getSelectionModel().getSelectedItem();

				String selection = comboBoxFilterSelector.getSelectionModel().getSelectedItem();

				switch (selection) {
				case ACTIONS:
					setupTopActionsChart(selectedOccurrenceType);
					break;

				case STATES:
					setupTopStatesChart(selectedOccurrenceType);
				default:
					break;
				}

			}
		});

		// filter actions/states in chart
		textFieldOccurrenceFilterPercentage.setOnKeyPressed(e -> {
			// if enter is pressed then refersh
			if (e.getCode() == KeyCode.ENTER) {
				String selection = comboBoxFilterSelector.getSelectionModel().getSelectedItem();

				switch (selection) {
				case ACTIONS:
					setupTopActionsChart(PERCENTAGE);
					break;

				case STATES:
					setupTopStatesChart(PERCENTAGE);
				default:
					break;
				}

			}
		});

		// setup choicebox filter selector
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				comboBoxFilterSelector.setItems(FXCollections.observableArrayList(FilterSelectors));
				comboBoxFilterSelector.getSelectionModel().select(0);
			}
		});

		// checks input to be digital
		checkInputAsDigital(textFieldNumofOccurrences);
		checkInputAsDigital(textFieldOccurrenceFilterPercentage);
		checkInputAsDigital(textFieldActionOccurrence);

	}

	protected void checkInputAsDigital(TextField textField) {

		textField.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// TODO Auto-generated method stub
				if (!newValue.matches("\\d*")) {
					textField.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}

		});

	}

	@FXML
	void openSystemFile(MouseEvent event) {

		if (selectedTracesFile != null) {
			try {
				Desktop.getDesktop().open(new File(selectedTracesFile.getAbsolutePath()));
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}

	}

	@FXML
	void selectTracesFile(MouseEvent event) {

		FileChooser fileChooser = new FileChooser();

		if (selectedTracesFile != null) {
			fileChooser.setInitialFileName(selectedTracesFile.getName());

			String folder = selectedTracesFile.getAbsolutePath().substring(0,
					selectedTracesFile.getAbsolutePath().lastIndexOf(File.separator));
			File folderF = new File(folder);

			if (folderF.isDirectory()) {
				fileChooser.setInitialDirectory(folderF);
			}
		}

		// set extension to be of system model (.cps)
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");

		fileChooser.getExtensionFilters().add(extFilter);

		selectedTracesFile = fileChooser.showOpenDialog(null);

		if (selectedTracesFile != null) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					textFieldSystemFile.setText(selectedTracesFile.getAbsolutePath());
				}
			});

			loadTracesFile(selectedTracesFile.getAbsolutePath());
		}

	}

	@FXML
	public void mineTraces(ActionEvent event) {

		String selectedFilter = comboBoxFilter.getSelectionModel().getSelectedItem();

		switch (selectedFilter) {

		case SHORTEST:

			executor.submit(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					toggleButtonDisability(btnAnalyse, true);
					findShortestTraces();
					toggleButtonDisability(btnAnalyse, false);
				}
			});
			break;

		case SHORTEST_CLASP:

			executor.submit(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					toggleButtonDisability(btnAnalyse, true);
					mineShortestTracesUsingClaSP();
					toggleButtonDisability(btnAnalyse, false);
				}
			});

			break;

		case CUSTOMISE:
			executor.submit(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					toggleButtonDisability(btnAnalyse, true);
					mineBasedOnCustomisedFilter();
					toggleButtonDisability(btnAnalyse, false);
				}
			});

			break;

		default:
			// shortest
		}
	}

	@FXML
	public void refreshGraph(ActionEvent event) {

		String operation = "";

		String perc = textFieldOccurrenceFilterPercentage.getText();

		String selection = comboBoxFilterSelector.getSelectionModel().getSelectedItem();

		// higher precedence for percentage
		if (perc != null && !perc.isEmpty()) {
			operation = PERCENTAGE;
		} else {
			String selectedOccurrenceType = comboBoxOccurrences.getSelectionModel().getSelectedItem();
			operation = selectedOccurrenceType;
		}

		switch (selection) {
		case ACTIONS:
			setupTopActionsChart(operation);
			break;

		case STATES:
			setupTopStatesChart(operation);
			break;

		default:
			break;
		}

	}

	@FXML
	public void saveFilteredTraces(ActionEvent event) {

		// save filtered traces
		FileChooser fileChooser = new FileChooser();

		if (selectedFilteredTracesFile != null) {
			fileChooser.setInitialFileName(selectedFilteredTracesFile.getName());
			String folder = selectedFilteredTracesFile.getAbsolutePath().substring(0,
					selectedFilteredTracesFile.getAbsolutePath().lastIndexOf(File.separator));
			File folderF = new File(folder);

			if (folderF.isDirectory()) {
				fileChooser.setInitialDirectory(folderF);
			}

		}

		// set extension to be of system model (.cps)
		// fileChooser.setSelectedExtensionFilter(new ExtensionFilter("System
		// model files (*.cps)",".cps"));
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");

		fileChooser.getExtensionFilters().add(extFilter);

		selectedFilteredTracesFile = fileChooser.showSaveDialog(null);

		if (selectedFilteredTracesFile != null) {
			// Platform.runLater(new Runnable() {
			//
			// @Override
			// public void run() {
			// // TODO Auto-generated method stub
			//// textFieldSystemFile.setText(selectedTracesFile.getAbsolutePath());
			// }
			// });

			saveTraces(selectedFilteredTracesFile.getAbsolutePath(), shownFitleredTraces);
		}

	}

	protected void saveTraces(String fileName, List<Integer> tracesIDs) {

		if (fileName == null || tracesIDs == null) {
			return;
		}

		executor.submit(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				tracesMiner.saveTraces(fileName, tracesIDs);
			}
		});

	}

//	@FXML
//	public void viewSVG(ActionEvent event) {
//
//		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/state_viewer.fxml"));
//		Parent root;
//		try {
//			root = (Parent) fxmlLoader.load();
//			Stage stage = new Stage();
//			stage.setScene(new Scene(root));
//			
//			//get controller
//			stateViewerController = fxmlLoader.<StateViewerController>getController();
//			
//			String path = "C:\\Users\\Faeq\\Desktop\\svg\\0.svg";
//			int tries = 10000;
//			
//			while(path.contains("\\") && tries > 0) {
//				path = path.replace("\\", "/");
//				tries--;
//			}
//			
//			String svgPath = "file:///"+path;
//			stateViewerController.updateSVGPath(svgPath);
//			stage.show();
////			   main.stg.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}

	protected void mineBasedOnCustomisedFilter() {

		progressIndicatorFilter.setVisible(true);

		List<Integer> tracesIDs = null;
		// int operationToPerform = -1;

		// check sequence length
		int length = spinnerFilterLength.getValue();
		String lengthOp = comboboxSeqLengthComparator.getSelectionModel().getSelectedItem();
		// operationToPerform = FILTER_LENGTH;

		// check actions occurrence percentage
		String percStr = textFieldActionOccurrence.getText();
		int perc = -1;
		String occurOp = null;

		if (percStr != null && !percStr.isEmpty()) {
			perc = Integer.parseInt(percStr);
			occurOp = comboboxOccurrenceComparator.getSelectionModel().getSelectedItem();
			// if (perc != -1) {
			// operationToPerform+=FILTER_OCCURRENCE;
			// }

		}

		// check action names
		// String actions = textFieldActions.getText();
		String actions = textAreaActions.getText();

		if (actions != null && actions.isEmpty()) {
			actions = null;
		}

		tracesIDs = tracesMiner.getTracesWithFilters(lengthOp, length, occurOp, perc, actions);

		if (tracesIDs != null) {
			updateImage(IMAGE_CORRECT, imgFilter);
			updateText("number of retrieved traces is: " + tracesIDs.size(), lblFilter);

			Platform.runLater(new Runnable() {

				@Override
				public void run() {

					if (!chartFilterTraces.contains(CUSTOMISED_TRACES)) {
						chartFilterTraces.add(CUSTOMISED_TRACES);
					}
					comboBoxChartFilterTraces.setItems(FXCollections.observableArrayList(chartFilterTraces));
					comboBoxChartFilterTraces.getSelectionModel().select(0);

				}
			});

		} else {
			updateImage(IMAGE_WRONG, imgFilter);
			updateText("Problem occurred", lblFilter);
		}

		progressIndicatorFilter.setVisible(false);

		// view traces
		viewTraces(tracesMiner.getCustomisedTracesIDs());

	}

	protected List<String> parseQuery(String query) {

		// actions separated by comma
		query = query.trim();

		// remove all space
		query = query.replaceAll(" ", "");

		List<String> result = Arrays.asList(query.split(","));

		Iterator<String> it = result.iterator();

		// List<Integer> indexToRemove = new LinkedList<Integer>();

		// for(int i=0;i<result.size();i++) {
		// String act = result.get(i);
		// if(act.isEmpty() || act.equals(" ")) {
		// indexToRemove.add(i);
		// }
		// }

		while (it.hasNext()) {
			String act = it.next();
			if (act.isEmpty() || act.equals(" ")) {
				it.remove();
			}
		}

		System.out.println(result);

		return result;
	}

	protected void loadTracesFile(String filePath) {

		if (filePath != null) {

			// show progress indicatior
			// progressIndicatorLoader.setVisible(true);

			executor.submit(new Runnable() {

				@Override
				public void run() {
					loadTraces(filePath);
				}
			});

		}

		textFieldSystemFile.requestFocus();
	}

	/**
	 * checks if the selected file can be read as a json file
	 * 
	 * @return
	 */
	protected boolean loadTraces(String filePath) {

		// reset if already loaded something before
		resetLoadingGUI();

		resetFilterGUI();

		// reset data
		shownFitleredTraces = null;

		// set file in miner
		tracesMiner.setTracesFile(filePath);

		System.out.println("set file path " + filePath);
		numberOfTraces = tracesMiner.loadTracesFromFile();

		boolean isLoaded = false;
		if (numberOfTraces == TracesMiner.TRACES_NOT_LOADED) {

			isLoaded = false;
		} else {
			isLoaded = true;
		}

		setupGUIpostLoading(isLoaded);

		return isLoaded;
	}

	protected void setupGUIpostLoading(boolean isLoaded) {

		if (isLoaded) {

			// update number of traces
			updateImage(IMAGE_CORRECT, imgSystemFileCheck);
			updateText("Number of Traces = " + tracesMiner.getNumberOfTraces(), lblSystemFileCheck);

			// updated number of actions used
			updateImage(IMAGE_CORRECT, imgNumOfActions);
			updateText("Total Number of Actions: " + tracesMiner.getNumberOfActions(), lblNumOfActions);

			// updated number of states used
			updateImage(IMAGE_CORRECT, imgNumOfStates);
			updateText("Total Number of States: " + tracesMiner.getNumberOfStates(), lblNumOfStates);

			imgOpentracesFile.setVisible(true);
			imgOpentracesFileEmpty.setVisible(false);
			btnAnalyse.setDisable(false);
			btnRefresh.setDisable(false);

			/**** set chart filter **/
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					// set traces filter to be All
					if (!chartFilterTraces.contains(ALL_TRACES)) {
						chartFilterTraces.add(ALL_TRACES);
						comboBoxChartFilterTraces.setItems(FXCollections.observableArrayList(chartFilterTraces));
						comboBoxChartFilterTraces.getSelectionModel().select(0);
					}

				}
			});

			// display highest occurrence
			setupTopActionsChart("Highest-Ranked");

			/**** set customise filter **/
			setupCustomisePane();

		} else {
			updateImage(IMAGE_WRONG, imgSystemFileCheck);
			updateText("Traces file is not valid", lblSystemFileCheck);
			imgOpentracesFile.setVisible(false);
			imgOpentracesFileEmpty.setVisible(true);

		}

		progressIndicatorLoader.setVisible(false);
	}

	protected void resetLoadingGUI() {

		updateImage(null, imgSystemFileCheck);
		updateImage(null, imgNumOfActions);
		updateImage(null, imgNumOfStates);

		updateText(null, lblSystemFileCheck);
		updateText(null, lblNumOfActions);
		updateText(null, lblNumOfStates);
	}

	protected void resetFilterGUI() {

		updateImage(null, imgFilter);

		updateText(null, lblFilter);

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				// clear combobox for filtering chart
				chartFilterTraces.clear();
				chartFilterTraces.add(ALL_TRACES);
				comboBoxChartFilterTraces.setItems(FXCollections.observableArrayList(chartFilterTraces));

				// clear chart

			}
		});
	}

	protected void findShortestTraces() {

		int numOfShortestTraces = 0;

		// show
		progressIndicatorFilter.setVisible(true);

		numOfShortestTraces = tracesMiner.findShortestTraces();

		// hide progress indicator
		progressIndicatorFilter.setVisible(false);

		updateImage(IMAGE_CORRECT, imgFilter);
		updateText("# of shortest traces = " + numOfShortestTraces, lblFilter);

		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				// add a shortest traces entry to chart filter choice box
				if (!chartFilterTraces.contains(SHORTEST_TRACES)) {
					chartFilterTraces.add(SHORTEST_TRACES);
					comboBoxChartFilterTraces.setItems(FXCollections.observableArrayList(chartFilterTraces));
					comboBoxChartFilterTraces.getSelectionModel().select(0);
				}

			}
		});

		// show traces
		viewTraces(tracesMiner.getShortestTracesIDs());

	}

	protected void mineShortestTracesUsingClaSP() {

		int numofTraces = 0;

		// show
		progressIndicatorFilter.setVisible(true);

		numofTraces = tracesMiner.mineClosedSequencesUsingClaSPAlgo(TracesMiner.SHORTEST);

		// hide progress indicator
		progressIndicatorFilter.setVisible(false);

		updateImage(IMAGE_CORRECT, imgFilter);
		updateText("# of retrieved traces = " + numofTraces, lblFilter);

		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				// add a shortest traces entry to chart filter choice box
				if (!chartFilterTraces.contains(SHORTEST_CLASP_TRACES)) {
					chartFilterTraces.add(SHORTEST_CLASP_TRACES);
					comboBoxChartFilterTraces.setItems(FXCollections.observableArrayList(chartFilterTraces));
					comboBoxChartFilterTraces.getSelectionModel().select(0);
				}

			}
		});

		// show traces
		viewTraces(tracesMiner.getShortestClaSPTracesIDs());

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

	protected void updateText(String msg, final Label label) {

		if (label == null) {
			return;

		}

		if (msg == null) {
			label.setVisible(false);
		} else {

			label.setVisible(true);
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					label.setText(msg);
				}
			});
		}

	}

	protected void setupCustomisePane() {

		if (selectedTracesFile == null) {
			return;
		}

		String selectedFilter = comboBoxFilter.getSelectionModel().getSelectedItem();

		if (selectedFilter.equals(CUSTOMISE)) {
			customisePane.setDisable(false);
		}

		// set actions in auto completer
		// autoCompleteActionsFiled.setEntries(tracesMiner.getTracesActions());

		autoCompleteActionsArea.setEntries(tracesMiner.getTracesActions());

		// Value factory
		SpinnerValueFactory<Integer> valueFactory = //
				new SpinnerValueFactory.IntegerSpinnerValueFactory(tracesMiner.getMinimumTraceLength(),
						tracesMiner.getMaximumTraceLength(), tracesMiner.getMinimumTraceLength());

		// setup sequence length bounds
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				// set length limits
				spinnerFilterLength.setValueFactory(valueFactory);
				// txtFieldLength.setPromptText("length [min: " +
				// tracesMiner.getMinimumTraceLength() + ", max: "
				// + tracesMiner.getMaximumTraceLength() + "]");

				// set actions filed autoComplete
				// textFieldActions.textProperty().addListener(new
				// ChangeListener<String>() {
				//
				// @Override
				// public void changed(ObservableValue<? extends String>
				// observable, String oldValue,
				// String newValue) {
				// // TODO Auto-generated method stub
				// autoCompleteActionsFiled.autoComplete(textFieldActions,
				// newValue, oldValue);
				// }
				// });
				//
				// textFieldActions.focusedProperty().addListener(new
				// ChangeListener<Boolean>() {
				//
				// @Override
				// public void changed(ObservableValue<? extends Boolean>
				// observable, Boolean oldValue,
				// Boolean newValue) {
				// // TODO Auto-generated method stub
				// autoCompleteActionsFiled.hidePopup();
				// }
				// });
				//
				// textFieldActions.setOnKeyPressed(e -> {
				// // if control+space pressed, then show the list of possible
				// // actions
				// if (e.getCode() == KeyCode.SPACE && e.isControlDown()) {
				// autoCompleteActionsFiled.showAllEntries(textFieldActions);
				// }
				// });

				textAreaActions.textProperty().addListener(new ChangeListener<String>() {

					@Override
					public void changed(ObservableValue<? extends String> observable, String oldValue,
							String newValue) {
						// TODO Auto-generated method stub
						autoCompleteActionsArea.autoComplete(textAreaActions, newValue, oldValue);
					}
				});

				textAreaActions.focusedProperty().addListener(new ChangeListener<Boolean>() {

					@Override
					public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
							Boolean newValue) {
						// TODO Auto-generated method stub
						autoCompleteActionsArea.hidePopup();
					}
				});

				textAreaActions.setOnKeyPressed(e -> {
					// if control+space pressed, then show the list of possible
					// actions
					if (e.getCode() == KeyCode.SPACE && e.isControlDown()) {
						autoCompleteActionsArea.showAllEntries(textAreaActions);
					}
				});
			}
		});

	}

	protected void setupTopActionsChart(String selectedOccurrenceType) {

		// int numOfOccurrences = 10;
		// int numOfSeries = 0;

		// get actions from miner
		Map<String, Integer> topActions;
		int num = 0;

		String tracesToFilter = comboBoxChartFilterTraces.getSelectionModel().getSelectedItem() + "";

		int numberOfTracesInSelection = -1;

		// set number of actions based on selection
		switch (tracesToFilter) {
		case ALL_TRACES:
			numberOfTracesInSelection = numberOfTraces;
			break;

		case SHORTEST_TRACES:
			numberOfTracesInSelection = tracesMiner.getShortestTracesNumber();
			break;

		case SHORTEST_CLASP_TRACES:
			numberOfTracesInSelection = tracesMiner.getShortestClaSPTracesIDs().size();
			break;

		case CUSTOMISED_TRACES:
			numberOfTracesInSelection = tracesMiner.getCustomisedTracesNumber();
			break;

		default:
			numberOfTracesInSelection = numberOfTraces;
			break;
		}

		// invoke operation from the miner based on selection
		switch (selectedOccurrenceType) {

		case HIGHEST:
			num = Integer.parseInt(textFieldNumofOccurrences.getText());
			chartTitle = "Actions with " + selectedOccurrenceType + " " + num + " Occurrences in " + tracesToFilter;

			topActions = tracesMiner.getTopActionOccurrences(num, tracesToFilter);
			break;

		case LOWEST:
			num = Integer.parseInt(textFieldNumofOccurrences.getText());
			chartTitle = "Actions with " + selectedOccurrenceType + " " + num + " Occurrences in " + tracesToFilter;

			topActions = tracesMiner.getLowestActionOccurrences(num, tracesToFilter);

			break;

		case PERCENTAGE:
			num = Integer.parseInt(textFieldOccurrenceFilterPercentage.getText());
			double perc = num * 1.0 / 100;
			String op = choiceBoxOccurrenceFilterPercentage.getSelectionModel().getSelectedItem();
			chartTitle = "Actions with Occurrence% " + op + " " + num + "% in " + tracesToFilter;

			topActions = tracesMiner.getActionsWithOccurrencePercentage(perc, op, tracesToFilter);

			System.out.println("actions: " + topActions);
			break;

		default:
			// highest occurrence in all
			chartTitle = "Actions with " + selectedOccurrenceType + " Occurrence in All Traces";
			topActions = tracesMiner.getHighestActionOccurrence();

			break;
		}

		if (topActions == null) {
			return;
		}

		List<String> actions = Arrays.asList(topActions.keySet().toArray(new String[topActions.size()]));
		List<Integer> occurrences = Arrays.asList(topActions.values().toArray(new Integer[topActions.size()]));

		final int numOfActions = actions.size();

		List<XYChart.Series<String, Integer>> series = new LinkedList<XYChart.Series<String, Integer>>();

		// XYChart.Series<String, Integer> series1 = new XYChart.Series<String,
		// Integer>();

		for (int i = 0; i < numOfActions; i++) {
			XYChart.Series<String, Integer> series1 = new XYChart.Series<String, Integer>();

			// bug does not allow the correct order of labels
			int occur = occurrences.get(i);

			// convert occurrence into percentage
			int occurPerc = (int) Math.floor((occur * 1.0 / numberOfTracesInSelection) * 100);
			series1.getData().add(new XYChart.Data<String, Integer>("", occurPerc));

			series1.setName(actions.get(i));

			series.add(series1);
		}

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// Defining the x axis
				categoryAxis.setLabel("Action");

				// Defining the y axis
				numberAxis.setLabel("Occurrence %");

				barChartActions.setTitle(chartTitle);

				barChartActions.getData().clear();
				// TODO Auto-generated method stub
				barChartActions.getData().addAll(series);
			}
		});

	}

	protected void setupTopStatesChart(String selectedOccurrenceType) {

		// int numOfOccurrences = 10;
		// int numOfSeries = 0;

		// get actions from miner
		Map<Integer, Integer> topStates = null;
		int num = 0;

		String tracesToFilter = comboBoxChartFilterTraces.getSelectionModel().getSelectedItem() + "";

		int numberOfTracesInSelection = -1;

		// set number of actions based on selection
		switch (tracesToFilter) {
		case ALL_TRACES:
			numberOfTracesInSelection = numberOfTraces;
			break;

		case SHORTEST_TRACES:
			numberOfTracesInSelection = tracesMiner.getShortestTracesNumber();
			break;

		case CUSTOMISED_TRACES:
			numberOfTracesInSelection = tracesMiner.getCustomisedTracesNumber();
			break;

		default:
			numberOfTracesInSelection = numberOfTraces;
			break;
		}

		switch (selectedOccurrenceType) {
		case HIGHEST:
			num = Integer.parseInt(textFieldNumofOccurrences.getText());
			chartTitle = "States with " + selectedOccurrenceType + " " + num + " Occurrences in " + tracesToFilter;

			topStates = tracesMiner.getTopStatesOccurrences(num, tracesToFilter);
			break;

		case LOWEST:
			num = Integer.parseInt(textFieldNumofOccurrences.getText());
			chartTitle = "States with " + selectedOccurrenceType + " " + num + " Occurrences in " + tracesToFilter;

			topStates = tracesMiner.getLowestStateOccurrences(num, tracesToFilter);

			break;

		case PERCENTAGE:
			num = Integer.parseInt(textFieldOccurrenceFilterPercentage.getText());
			double perc = num * 1.0 / 100;
			String op = choiceBoxOccurrenceFilterPercentage.getSelectionModel().getSelectedItem();
			chartTitle = "States with Occurrence% " + op + " " + num + "% in " + tracesToFilter;

			topStates = tracesMiner.getStatesWithOccurrencePercentage(perc, op, tracesToFilter);

			break;

		default:
			// highest occurrence
			chartTitle = "States with " + selectedOccurrenceType + " Occurrence in All Traces";
			// topActions = tracesMiner.getHighestActionOccurrence();

			break;
		}

		if (topStates == null) {
			return;
		}

		List<Integer> states = Arrays.asList(topStates.keySet().toArray(new Integer[topStates.size()]));
		List<Integer> occurrences = Arrays.asList(topStates.values().toArray(new Integer[topStates.size()]));

		final int numOfActions = states.size();

		List<XYChart.Series<String, Integer>> series = new LinkedList<XYChart.Series<String, Integer>>();

		// XYChart.Series<String, Integer> series1 = new XYChart.Series<String,
		// Integer>();

		for (int i = 0; i < numOfActions; i++) {
			XYChart.Series<String, Integer> series1 = new XYChart.Series<String, Integer>();

			// bug does not allow the correct order of labels
			int occur = occurrences.get(i);

			// convert occurrence into percentage
			int occurPerc = (int) Math.floor((occur * 1.0 / numberOfTracesInSelection) * 100);

			series1.getData().add(new XYChart.Data<String, Integer>("", occurPerc));

			series1.setName("" + states.get(i));

			series.add(series1);
		}

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// Defining the x axis
				categoryAxis.setLabel("State");

				// Defining the y axis
				numberAxis.setLabel("Occurrence %");

				barChartActions.setTitle(chartTitle);

				barChartActions.getData().clear();
				// TODO Auto-generated method stub
				barChartActions.getData().addAll(series);
			}
		});

	}

	@Override
	public void onNumberOfTracesRead(int numOfTraces) {

		// number of traces is read by the traces miner

		numberOfTraces = numOfTraces;

		// set progress value for a single trace read
		// singleTraceProgressValue = 1.0/numberOfTraces;
		currentTraceNumber = 0;

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				// show progress bar and its label
				progressBarTraces.setVisible(true);
				lblProgressTraces.setVisible(true);

				lblProgressTraces.setText(currentTraceNumber + "/" + numberOfTraces);

				// hide the the indefinit indicator
				progressIndicatorLoader.setVisible(false);
			}
		});

	}

	@Override
	public void onTracesLoaded(int numberOfTracesLoaded) {
		// TODO Auto-generated method stub
		// update progress bar and its label

		currentTraceNumber += numberOfTracesLoaded;
		double progressValue = currentTraceNumber * 1.0 / numberOfTraces;

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				progressBarTraces.setProgress(progressValue);
				lblProgressTraces.setText(currentTraceNumber + "/" + numberOfTraces);

				// if complete
				if (currentTraceNumber == numberOfTraces) {
					progressBarTraces.setVisible(false);
					lblProgressTraces.setVisible(false);
				}
			}
		});
	}

	@Override
	public void onLoadingJSONFile() {
		// TODO Auto-generated method stub

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				lblProgressTraces.setVisible(true);
				lblProgressTraces.setText("Loading File...");
				progressIndicatorLoader.setVisible(true);
			}
		});
	}

	protected DropShadow createBorderGlow(int depth, Color color) {

		DropShadow borderGlow = new DropShadow();
		borderGlow.setOffsetY(0f);
		borderGlow.setOffsetX(0f);
		borderGlow.setColor(color);
		borderGlow.setWidth(depth);
		borderGlow.setHeight(depth);

		return borderGlow;
	}

	protected void showFiledError(TextField textField) {

		DropShadow borderGlow = createBorderGlow(35, Color.RED);

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				textField.setEffect(borderGlow);
				Timer t = new Timer();
				t.schedule(new TimerTask() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						textField.setEffect(null);
					}
				}, INTERVAL);
			}
		});

	}

	protected void toggleButtonDisability(Button button, boolean isDisabled) {
	
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(isDisabled){
					button.setDisable(true);	
				} else{
					button.setDisable(false);
				}
				
			}
		});
	}
	
	protected void viewTraces(List<Integer> tracesIDs) {

		if (tracesIDs == null) {
			System.err.println("traces ids list is null");
			return;
		}

		if (tracesIDs.isEmpty()) {
			lblListViewTracesEmpty.setVisible(true);
			shownFitleredTraces = tracesIDs;
			// clear list
			listViewTraces.setItems(null);
			return;
		}

		// used for saving
		shownFitleredTraces = tracesIDs;

		Map<Integer, GraphPath> traces = tracesMiner.getTraces(tracesIDs);

		ObservableList<GraphPath> tracesObservableList;

		tracesObservableList = FXCollections.observableArrayList();

		tracesObservableList.addAll(traces.values());

		listViewTraces.setCellFactory(tracesListView -> new TaskCell());
		listViewTraces.setItems(tracesObservableList);

		lblListViewTracesEmpty.setVisible(false);

	}

	@Override
	public void onSavingFilteredTracesComplete(boolean isSuccessful) {
		// TODO Auto-generated method stub

		if (isSuccessful) {
			updateImage(IMAGE_CORRECT, imgSavedTraces);
			updateText("Saved!", lblSaved);
		} else {
			updateImage(IMAGE_WRONG, imgSavedTraces);
			updateText("didn't save!", lblSaved);
		}
	}
	
	

}
