package controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import controller.utlities.AutoCompleteTextField;
import core.TracesMiner;
import core.TracesMinerListener;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

public class TraceViewerController implements TracesMinerListener {

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

	@FXML
	private ChoiceBox<String> choiceboxFilter;

	@FXML
	private ImageView imgFilter;

	@FXML
	private Label lblFilter;

	@FXML
	private ProgressIndicator progressIndicatorFilter;

	@FXML
	private ProgressIndicator progressIndicatorLoader;

	@FXML
	private Pane customisePane;

	@FXML
	private ChoiceBox<String> choiceboxSeqLengthComparator;

	@FXML
	private ChoiceBox<String> choiceboxOccurrenceComparator;

	@FXML
	private TextField txtFieldLength;

	@FXML
	private TextField textFieldActions;

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
	private ChoiceBox<String> choiceBoxOccurrences;

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

	private static final String IMAGES_FOLDER = "resources/images/";
	private static final String IMAGE_CORRECT = IMAGES_FOLDER + "correct.png";
	private static final String IMAGE_WRONG = IMAGES_FOLDER + "wrong.png";
	private static final int INTERVAL = 3000;
	// private static final int FILE_MENU = 0;

	private File selectedTracesFile;
	// private JSONObject jsonTraces;
	private TracesMiner tracesMiner;

	private int numberOfTraces = -1;

	// used for progress bar
	private double singleTraceProgressValue = 0.1;
	private int currentTraceNumber = 0;

	private ExecutorService executor = Executors.newFixedThreadPool(3);

	private static final String SHORTEST = "Shortest Only";
	private static final String SHORTEST_CLASP = "Shortest length & Share longest partial sequence (ClaSP)";
	private static final String CUSTOMISE = "Customise";
	private static final String HIGHEST = "Highest";
	private static final String LOWEST = "Lowest";
	private static final String PERCENTAGE = "percent";
	public static final String EQUAL = "=";
	public static final String MORE_THAN = ">";
	public static final String LESS_THAN = "<";

	private AutoCompleteTextField autoCompleteActionsFiled;

	private final String[] filters = { SHORTEST, SHORTEST_CLASP, CUSTOMISE };

	private final String[] compartiveOperators = { MORE_THAN, LESS_THAN, EQUAL, };

	private final String[] occurrencesOptions = { HIGHEST, LOWEST };

	String chartTitle = "";

	@FXML
	public void initialize() {

		tracesMiner = new TracesMiner();

		// set miner listener
		tracesMiner.setListener(this);

		// update filters in choice box
		choiceboxFilter.setItems(FXCollections.observableArrayList(filters));

		choiceboxFilter.valueProperty().addListener(new ChangeListener<String>() {

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
		choiceboxOccurrenceComparator.setItems(FXCollections.observableArrayList(compartiveOperators));

		choiceboxSeqLengthComparator.setItems(FXCollections.observableArrayList(compartiveOperators));

		choiceBoxOccurrenceFilterPercentage.setItems(FXCollections.observableArrayList(compartiveOperators));

		choiceBoxOccurrences.setItems(FXCollections.observableArrayList(occurrencesOptions));

		// defualt selection
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				choiceboxOccurrenceComparator.getSelectionModel().select(0);
				choiceboxFilter.getSelectionModel().select(0);
				choiceBoxOccurrences.getSelectionModel().select(0);
				choiceBoxOccurrenceFilterPercentage.getSelectionModel().select(0);
				choiceboxSeqLengthComparator.getSelectionModel().select(0);
			}
		});

		// auto complete
		autoCompleteActionsFiled = new AutoCompleteTextField();

		textFieldNumofOccurrences.setOnKeyPressed(e -> {

			// if enter is pressed then refersh
			if (e.getCode() == KeyCode.ENTER) {
				String selectedOccurrenceType = choiceBoxOccurrences.getSelectionModel().getSelectedItem();
				setupTopActionsChart(selectedOccurrenceType);
			}
		});

		textFieldSystemFile.setOnKeyPressed(e -> {

			// if enter is pressed then refersh
			if (e.getCode() == KeyCode.ENTER) {
				String filePath = textFieldSystemFile.getText();
				
				//check it is a file
				boolean isValid = tracesMiner.checkFile(filePath);
				
				if(isValid) {
					loadTracesFile(filePath);
				} else {
					showFiledError(textFieldSystemFile);
				}
			}
		});

		textFieldOccurrenceFilterPercentage.setOnKeyPressed(e -> {
			// if enter is pressed then refersh
			if (e.getCode() == KeyCode.ENTER) {
				setupTopActionsChart(PERCENTAGE);
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
		}

		// set extension to be of system model (.cps)
		// fileChooser.setSelectedExtensionFilter(new ExtensionFilter("System
		// model files (*.cps)",".cps"));
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");

		fileChooser.getExtensionFilters().add(extFilter);

		selectedTracesFile = fileChooser.showOpenDialog(null);

		if(selectedTracesFile != null) {
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

		String selectedFilter = choiceboxFilter.getSelectionModel().getSelectedItem();

		switch (selectedFilter) {

		case SHORTEST:

			executor.submit(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					findShortestTraces();
				}
			});
			break;

		case SHORTEST_CLASP:

			executor.submit(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mineShortestTracesUsingClaSP();
				}
			});

			break;

		case CUSTOMISE:

			break;

		default:
			// shortest
		}
	}

	@FXML
	public void refreshGraph(ActionEvent event) {

		String perc = textFieldOccurrenceFilterPercentage.getText();

		// higher precedence for percentage
		if (perc != null && !perc.isEmpty()) {
			setupTopActionsChart(PERCENTAGE);
		} else {
			String selectedOccurrenceType = choiceBoxOccurrences.getSelectionModel().getSelectedItem();
			setupTopActionsChart(selectedOccurrenceType);
		}

	}

	protected void loadTracesFile(String filePath) {
		
		if (filePath != null) {

			// show progress indicatior
			progressIndicatorLoader.setVisible(true);

			executor.submit(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (loadTraces(filePath)) {

						// update number of traces
						updateImage(IMAGE_CORRECT, imgSystemFileCheck);
						updateText("Number of Traces = " + tracesMiner.getNumberOfTraces(), lblSystemFileCheck);

						// updated number of actions used
						updateImage(IMAGE_CORRECT, imgNumOfActions);
						updateText("Total Number of Actions: " + tracesMiner.getNumberOfActions(), lblNumOfActions);

						// updated number of states used
						updateImage(IMAGE_CORRECT, imgNumOfStates);
						updateText("Total Number of States: " + tracesMiner.getNumberOfStates(), lblNumOfStates);

						// display highest occurrence
						setupTopActionsChart("1st-Highest");

						imgOpentracesFile.setVisible(true);
						imgOpentracesFileEmpty.setVisible(false);
						btnAnalyse.setDisable(false);
						btnRefresh.setDisable(false);
						// show top actions
						// setupTopActionsChart();

					} else {
						updateImage(IMAGE_WRONG, imgSystemFileCheck);
						updateText("Traces file is not valid", lblSystemFileCheck);
						imgOpentracesFile.setVisible(false);
						imgOpentracesFileEmpty.setVisible(true);

					}

					progressIndicatorLoader.setVisible(false);

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
		// set file in miner
		tracesMiner.setTracesFile(filePath);

		int numberOfTraces = tracesMiner.readTracesFromFile();

		if (numberOfTraces == TracesMiner.TRACES_NOT_LOADED) {

			return false;
		}

		return true;
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

		customisePane.setDisable(false);

		// set actions in auto completer
		autoCompleteActionsFiled.setEntries(tracesMiner.getTracesActions());

		// setup sequence length bounds
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				// set length limits
				txtFieldLength.setPromptText("length [min: " + tracesMiner.getMinimumTraceLength() + ", max: "
						+ tracesMiner.getMaximumTraceLength() + "]");

				// set actions filed autoComplete
				textFieldActions.textProperty().addListener(new ChangeListener<String>() {

					@Override
					public void changed(ObservableValue<? extends String> observable, String oldValue,
							String newValue) {
						// TODO Auto-generated method stub
						autoCompleteActionsFiled.autoComplete(textFieldActions, newValue, oldValue);
					}
				});

				textFieldActions.focusedProperty().addListener(new ChangeListener<Boolean>() {

					@Override
					public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
							Boolean newValue) {
						// TODO Auto-generated method stub
						autoCompleteActionsFiled.hidePopup();
					}
				});

				textFieldActions.setOnKeyPressed(e -> {
					// if control+space pressed, then show the list of possible
					// actions
					if (e.getCode() == KeyCode.SPACE && e.isControlDown()) {
						autoCompleteActionsFiled.showAllEntries(textFieldActions);
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

		switch (selectedOccurrenceType) {
		case HIGHEST:
			num = Integer.parseInt(textFieldNumofOccurrences.getText());
			chartTitle = "Actions with " + selectedOccurrenceType + " " + num + " Occurrences";

			topActions = tracesMiner.getTopActionOccurrences(num);
			break;

		case LOWEST:
			num = Integer.parseInt(textFieldNumofOccurrences.getText());
			chartTitle = "Actions with " + selectedOccurrenceType + " " + num + " Occurrences";

			topActions = tracesMiner.getLowestActionOccurrences(num);

			break;

		case PERCENTAGE:
			num = Integer.parseInt(textFieldOccurrenceFilterPercentage.getText());
			double perc = num * 1.0 / 100;
			String op = choiceBoxOccurrenceFilterPercentage.getSelectionModel().getSelectedItem();
			chartTitle = "Actions with Occurrence % " + op + " " + num + "%";

			topActions = tracesMiner.getActionsWithOccurrencePercentage(perc, op);

			break;

		default:
			// highest occurrence
			chartTitle = "Actions with " + selectedOccurrenceType + " Occurrence";
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
			series1.getData().add(new XYChart.Data<String, Integer>("", occur));

			series1.setName(actions.get(i));

			series.add(series1);
		}

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// Defining the x axis
				categoryAxis.setLabel("Action");

				// Defining the y axis
				numberAxis.setLabel("Occurrence");

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
}
