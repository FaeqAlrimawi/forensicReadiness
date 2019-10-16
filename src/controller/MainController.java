package controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import com.eteks.sweethome3d.SweetHome3D;
import com.eteks.sweethome3d.adaptive.forensics.BigrapherStatesChecker;
import com.eteks.sweethome3d.adaptive.forensics.SystemHandler;

import application.ActionCatalogueEditorMain;
import cyberPhysical_Incident.IncidentDiagram;
import environment.EnvironmentDiagram;
import ie.lero.spare.franalyser.utility.ModelsHandler;
import ie.lero.spare.pattern_extraction.IncidentPatternExtractor;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainController {

	@FXML
	private TextField textFieldSelectedStatesFolder;

	@FXML
	private TextField textFieldSystemFile;

	@FXML
	private TextField textFieldIncidentInstance;

	@FXML
	private ProgressBar progressBarAnalyse;

	// @FXML
	// private ProgressIndicator progressIndicatorGeneration;

	@FXML
	private Label lblTransitionCheck;

	@FXML
	private ImageView imgTransitionCheck;

	@FXML
	private ImageView imgSystemFileCheck;

	@FXML
	private ImageView imgOpenBigrapher;

	@FXML
	private ImageView imgOpenBigrapherEmpty;

	@FXML
	private ImageView imgOpenIncidentInstanceFile;

	@FXML
	private ImageView imgSelectIncidentInstance;

	@FXML
	private ImageView imgOpenIncidentInstanceFileEmpty;

	@FXML
	private ImageView imgSelectSystemFile;

	@FXML
	private ImageView imgOpenFolder;

	@FXML
	private ImageView imgRefresh;

	@FXML
	private ImageView imgRefreshEmpty;
	
	@FXML
	private ImageView imgIncidentInstanceFileCheck;
	

	@FXML
	private Label lblStatesCheck;
	
	@FXML
	private Label lblIncidentInstanceFileCheck;

	@FXML
	private Label lblSystemFileCheck;

	@FXML
	private Button btnUpdateSystemModel;

	@FXML
	private Button btnEditActions;

	@FXML
	private Button btnGenerateIncidentPattern;

	@FXML
	private ImageView imgStatesCheck;

	@FXML
	private MenuBar menuBar;
	
	@FXML
	private ProgressIndicator progressBarGenerateIncidentPattern;
	

	private static final String IMAGES_FOLDER = "resources/images/";
	private static final String IMAGE_CORRECT = IMAGES_FOLDER + "correct.png";
	private static final String IMAGE_WRONG = IMAGES_FOLDER + "wrong.png";
	private static final int INTERVAL = 3000;
	private static final int FILE_MENU = 0;

	private BigrapherStatesChecker checker;

	private File selectedStatesDirectory;
	private File selectedSystemFile;
	private File selectedIncidentInstanceFile;
	private EnvironmentDiagram systemModel;

	private SweetHome3D sh3d;

	@FXML
	public void initialize() {

		// textFieldSystemFile.requestFocus();

		updateMenuBar();

	}

	protected void updateMenuBar() {

		Menu fileMenu = menuBar.getMenus().get(FILE_MENU);

		// add open file system menu item
		MenuItem openSystemFileItem = new MenuItem("Open System File");

		openSystemFileItem.setOnAction((e) -> {
			selectSystemFile(null);
		});

		fileMenu.getItems().add(0, openSystemFileItem);

		// set on close
		MenuItem close = fileMenu.getItems().get(fileMenu.getItems().size() - 1);

		close.setOnAction((e) -> {
			System.exit(0);
		});
	}

	@FXML
	void modelSystem(ActionEvent event) {

		if (sh3d == null) {
			sh3d = new SweetHome3D();
			sh3d.init(new String[] {});

		} else {
			// bring focus to it

		}

	}

	@FXML
	void selectStatesFolder(MouseEvent event) {

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
	void selectSystemFile(MouseEvent event) {

		FileChooser fileChooser = new FileChooser();

		if (selectedSystemFile != null) {
			fileChooser.setInitialFileName(selectedSystemFile.getName());
		}

		// set extension to be of system model (.cps)
		// fileChooser.setSelectedExtensionFilter(new ExtensionFilter("System
		// model files (*.cps)",".cps"));
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Cyber Physical System files (*.cps)",
				"*.cps");

		fileChooser.getExtensionFilters().add(extFilter);

		selectedSystemFile = fileChooser.showOpenDialog(null);

		if (selectedSystemFile != null) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					textFieldSystemFile.setText(selectedSystemFile.getAbsolutePath());
				}
			});

			if (isSystemFileValid()) {

				updateImage(IMAGE_CORRECT, imgSystemFileCheck);
				updateText("System model is valid", lblSystemFileCheck);
				btnUpdateSystemModel.setDisable(false);
				btnEditActions.setDisable(false);
				imgOpenBigrapher.setVisible(true);
				imgOpenBigrapherEmpty.setVisible(false);

				if (selectedIncidentInstanceFile != null) {
					btnGenerateIncidentPattern.setDisable(false);
				}

			} else {
				updateImage(IMAGE_WRONG, imgSystemFileCheck);
				updateText("System model is not valid", lblSystemFileCheck);
				btnUpdateSystemModel.setDisable(true);
				imgOpenBigrapher.setVisible(false);
				imgOpenBigrapherEmpty.setVisible(true);
				imgRefresh.setVisible(false);
				imgRefreshEmpty.setVisible(true);

				btnGenerateIncidentPattern.setDisable(true);

			}

			// remove the check image and text after a few secs
			new Timer().schedule(new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					updateImage(null, imgSystemFileCheck);
					updateText("", lblSystemFileCheck);
				}
			}, INTERVAL);
		}

		textFieldSystemFile.requestFocus();

	}

	@FXML
	void selectIncidentInstanceFile(MouseEvent event) {

		FileChooser fileChooser = new FileChooser();

		if (selectedIncidentInstanceFile != null) {
			fileChooser.setInitialFileName(selectedIncidentInstanceFile.getName());
		}

		// set extension to be of system model (.cps)
		// fileChooser.setSelectedExtensionFilter(new ExtensionFilter("System
		// model files (*.cps)",".cps"));
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Incident Instance files (*.cpi)",
				"*.cpi");

		fileChooser.getExtensionFilters().add(extFilter);

		selectedIncidentInstanceFile = fileChooser.showOpenDialog(null);

		if (selectedIncidentInstanceFile != null) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					textFieldIncidentInstance.setText(selectedIncidentInstanceFile.getAbsolutePath());
				}
			});

			if (isIncidentInstanceFileValid()) {

				 updateImage(IMAGE_CORRECT, imgIncidentInstanceFileCheck);
				 updateText("Incident Instance model is valid", lblIncidentInstanceFileCheck);
				// btnUpdateSystemModel.setDisable(false);
				// btnEditActions.setDisable(false);
				imgOpenIncidentInstanceFile.setVisible(true);
				imgOpenIncidentInstanceFileEmpty.setVisible(false);

				if (selectedSystemFile != null) {
					btnGenerateIncidentPattern.setDisable(false);
				}

			} else {
				 updateImage(IMAGE_WRONG, imgIncidentInstanceFileCheck);
				 updateText("Incident Instance model is not valid", lblIncidentInstanceFileCheck);
				// btnUpdateSystemModel.setDisable(true);
				imgOpenIncidentInstanceFile.setVisible(false);
				imgOpenIncidentInstanceFileEmpty.setVisible(true);
				// imgRefresh.setVisible(false);
				// imgRefreshEmpty.setVisible(true);

				btnGenerateIncidentPattern.setDisable(true);

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
		}

		textFieldSystemFile.requestFocus();

	}

	@FXML
	void openSystemFile(MouseEvent event) {
		// open bigrapher model file
		if (SystemHandler.isSystemModelGenerated()) {
			String path = SystemHandler.getFilePath();

			if (path != null) {
				try {
					Desktop.getDesktop().open(new File(path));
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			}

		}
	}

	@FXML
	void openIncidentInstanceFile(MouseEvent event) {
		// open bigrapher model file
		if (selectedIncidentInstanceFile != null) {
			String path = selectedIncidentInstanceFile.getAbsolutePath();

			if (path != null) {
				try {
					Desktop.getDesktop().open(new File(path));
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			}

		}
	}

	@FXML
	void refresh(MouseEvent event) {

		boolean isRefershed = SystemHandler.updateAndSaveSystemModel();

		if (isRefershed) {
			imgRefresh.setVisible(false);
			imgRefreshEmpty.setVisible(true);
		}

	}

	@FXML
	void updateSystemModel(ActionEvent event) {

		// to be done
		// create a model in SH3D based on the system model
	}

	@FXML
	void createIncidentInstance(ActionEvent event) {

		// incident.design.Activator activitor = new Activator();

		try {
			// Activator.getDefault().start(null);
			// BundleContext bundleContext =
			// FrameworkUtil.
			// getBundle().
			// getBundleContext();
			// activitor.start(bundleContext);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@FXML
	void generateLTS(ActionEvent event) {

		try {

			Pane layout;
			FXMLLoader loader = new FXMLLoader();

			URL url = ActionCatalogueEditorMain.class.getResource("../fxml/LTSView.fxml");

			if (url != null) {
				System.out.println(url.getPath());
			} else {
				System.out.println("url is null");
			}

			loader.setLocation(url);

			// Platform.setImplicitExit(false);

			layout = loader.load();

			Scene scene = new Scene(layout);

			Stage newStage = new Stage();
			newStage.setScene(scene);
			newStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	void openActionPanel(ActionEvent event) {

		try {

			Pane layout;
			FXMLLoader loader = new FXMLLoader();

			URL url = ActionCatalogueEditorMain.class.getResource("../fxml/SystemActionView.fxml");

			if (url != null) {
				System.out.println(url.getPath());
			} else {
				System.out.println("url is null");
			}

			loader.setLocation(url);

			// Platform.setImplicitExit(false);

			layout = loader.load();

			Scene scene = new Scene(layout);

			Stage newStage = new Stage();
			newStage.setScene(scene);
			newStage.show();

			newStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

				@Override
				public void handle(WindowEvent event) {
					// TODO Auto-generated method stub
					imgRefresh.setVisible(true);
					imgRefreshEmpty.setVisible(false);
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@FXML
	void generateIncidentPattern(ActionEvent event) {

		IncidentPatternExtractor extractor = new IncidentPatternExtractor();

		progressBarGenerateIncidentPattern.setVisible(true);
		IncidentDiagram diagram = extractor.extract(selectedIncidentInstanceFile.getAbsolutePath(),
				selectedSystemFile.getAbsolutePath());
		
		progressBarGenerateIncidentPattern.setVisible(false);

	}

	protected boolean isSystemFileValid() {

		// try creating an object from the file
		SystemHandler.setFilePath(selectedSystemFile.getAbsolutePath());

		boolean isValid = SystemHandler.generateSystemModel();

		return isValid;

	}

	protected boolean isIncidentInstanceFileValid() {

		// try creating an object from the file
		// SystemHandler.setFilePath(selectedSystemFile.getAbsolutePath());

		boolean isValid = ModelsHandler.isValidIncidentModel(selectedIncidentInstanceFile.getAbsolutePath());
		
		
		// return SystemHandler.generateSystemModel();

		return isValid;

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
