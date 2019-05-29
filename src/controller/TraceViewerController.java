package controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;


import core.TracesMiner;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

public class TraceViewerController {

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
    
    private static final String IMAGES_FOLDER = "resources/images/";
	private static final String IMAGE_CORRECT = IMAGES_FOLDER + "correct.png";
	private static final String IMAGE_WRONG = IMAGES_FOLDER + "wrong.png";
	private static final int INTERVAL = 3000;
//	private static final int FILE_MENU = 0;
	
    private File selectedTracesFile;
//    private JSONObject jsonTraces;
    private TracesMiner tracesMiner;
    
    
    private static final int SHORTEST = 0;
    private static final int SHORTEST_CLASP = 1;
    private final String[] filters = {
    		"Shortest Only",
    		"Shortest & [Frequent Sequential Pattern using ClaSP]",
    		"Set length manually"
    };
    
    
    @FXML
	public void initialize() {
    	
    	tracesMiner = new TracesMiner();
    	
    	//update filters in choice box
    	choiceboxFilter.setItems(FXCollections.observableArrayList(filters));
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
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)",
				"*.json");

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

			//set file in miner
			tracesMiner.setTracesFile(selectedTracesFile.getAbsolutePath());
			
			if (isTracesFileValid()) {

				updateImage(IMAGE_CORRECT, imgSystemFileCheck);
				updateText("Number of Traces = " + tracesMiner.getNumberOfTraces(), lblSystemFileCheck);
				imgOpentracesFile.setVisible(true);
				imgOpentracesFileEmpty.setVisible(false);
				btnAnalyse.setDisable(false);

			} else {
				updateImage(IMAGE_WRONG, imgSystemFileCheck);
				updateText("Traces file is not valid", lblSystemFileCheck);
				imgOpentracesFile.setVisible(false);
				imgOpentracesFileEmpty.setVisible(true);

			}
			
//			updateImage(null, imgSystemFileCheck);
//			updateText("", lblSystemFileCheck);

		}

		textFieldSystemFile.requestFocus();


    }
    
    @FXML
    public void mineTraces(ActionEvent event) {
    	
    	int selectedFilter = choiceboxFilter.getSelectionModel().getSelectedIndex();
    	
    	switch(selectedFilter) {
    	
    	case SHORTEST:
    		progressIndicatorFilter.setVisible(true);
    		findShortestTraces();
    		break;
    		
    	case SHORTEST_CLASP:
    		break;
    		default:
    			//shortest
    	}
    }
    
    /**
     * checks if the selected file can be read as a json file
     * @return
     */
    protected boolean isTracesFileValid(){
    	
    	
    	int numberOfTraces = tracesMiner.readTracesFromFile();
    	
    	if(numberOfTraces == TracesMiner.TRACES_NOT_LOADED) {
    		
    		return false;
    	}
    	
    	return true;
    }
    
    protected void findShortestTraces() {
    
    	int numOfShortestTraces = 0;
    	
    	numOfShortestTraces = tracesMiner.findShortestTraces();
    	
    	//hid progress indicator
    	progressIndicatorFilter.setVisible(false);
    	
    	updateImage(IMAGE_CORRECT, imgFilter);
    	updateText("# of shortest traces = " + numOfShortestTraces, lblFilter);
    	
    	
    	
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

}
