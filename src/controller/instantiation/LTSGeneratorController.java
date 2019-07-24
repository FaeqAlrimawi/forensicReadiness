package controller.instantiation;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

//import com.eteks.sweethome3d.adaptive.forensics.BigraphERHandler;
import com.eteks.sweethome3d.adaptive.forensics.SystemHandler;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import system.BigraphERHandler;

public class LTSGeneratorController {

	@FXML
	private ImageView imgOpenFolder;

	@FXML
	private CheckBox checkBoxSVG;

	@FXML
	private CheckBox checkBoxText;

	@FXML
	private CheckBox checkBoxDeclarations;

	@FXML
	private Button btnGenerateBigrapher;

	@FXML
	private Label lblBigraphERName;

	@FXML
	private Label lblOnlyNumbers;

	@FXML
	private ImageView imgOpenBigrapher;

	@FXML
	private ImageView imgOpenBigrapherEmpty;

	@FXML
	private Button btnGenerateStatement;

	@FXML
	private TextArea textAreaBigraphERStatement;

	@FXML
	private TextField textFieldFolder;

	@FXML
	private TextField textFieldStatesnumber;

	@FXML
	private Button btnRunCommand;

	private File selectedDirectory;
	private static final int INTERVAL = 3000;
	// private String bigraphERStatement;
	// private boolean isTesting = true;

	// private String bigrapherOutputFolder;

	@FXML
	public void initialize() {

		// check that bigrapher file already exists
		String filePath = SystemHandler.getBigraphERFilePath();

		if (filePath != null) {
			updateGenerateFileGUI(true, filePath);
		}

		// set listener for text field of states number
		textFieldStatesnumber.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					textFieldStatesnumber.setText(oldValue);
					lblOnlyNumbers.setVisible(true);
					Timer t = new Timer();
					t.schedule(new TimerTask() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							lblOnlyNumbers.setVisible(false);
						}
					}, INTERVAL);
				}
			}
		});

	}

	@FXML
	void generateBigraphERStatement(ActionEvent event) {

		String bigrapherFile = SystemHandler.getBigraphERFilePath();
		String strStatesNum = textFieldStatesnumber.getText();
		String bigrapherFilePath = lblBigraphERName.getText();

		boolean noBigraphFile = false;
		if (bigrapherFilePath == null || bigrapherFilePath.isEmpty()) {
			noBigraphFile = true;
		}

		boolean noStates = false;
		if (strStatesNum == null || strStatesNum.isEmpty()) {
			noStates = true;

		}

		String outputFolder = textFieldFolder.getText();

		boolean noOutputFolder = false;
		if (outputFolder == null || outputFolder.isEmpty()) {
			noOutputFolder = true;

		}

		if (noBigraphFile) {
			showNoBigraphFileError();
		}

		if (noStates) {
			showStatesError();
		}

		if (noOutputFolder) {
			showOutPutFolderError();
		}

		if (noBigraphFile || noStates || noOutputFolder) {
			return;
		}

		int numOfStates = Integer.parseInt(strStatesNum);

		boolean isSVG = checkBoxSVG.isSelected();
		boolean isTxt = checkBoxText.isSelected();
		boolean isDeclarations = checkBoxDeclarations.isSelected();

		if (bigrapherFile != null && numOfStates > 0 && (outputFolder != null && !outputFolder.isEmpty())) {

			BigraphERHandler handler = new BigraphERHandler();
			// generate statement
			String stmt = handler.createBigrapherExecutionCmd(bigrapherFile, numOfStates, outputFolder, isSVG, isTxt,
					isDeclarations);

			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					textAreaBigraphERStatement.setText(stmt);
				}
			});
		}

	}

	@FXML
	void openBigrapherFile(MouseEvent event) {

		// open bigrapher model file
		if (SystemHandler.isSystemModelGenerated()) {
			String path = SystemHandler.getBigraphERFilePath();

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
	void runBigrapherCommand(ActionEvent event) {

	}

	@FXML
	void selectFolder(MouseEvent event) {

		DirectoryChooser chooser = new DirectoryChooser();
		// chooser.setTitle("Select Folder");
		if (selectedDirectory != null) {
			chooser.setInitialDirectory(selectedDirectory);
		}

		selectedDirectory = chooser.showDialog(null);

		if (selectedDirectory != null) {

			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					textFieldFolder.setText(selectedDirectory.getAbsolutePath());
					textFieldFolder.setTooltip(new Tooltip(selectedDirectory.getAbsolutePath()));
				}
			});
		}

	}

	@FXML
	void generateBigrapherFile(ActionEvent event) {

		// check if it already exists
		String filePath = SystemHandler.getBigraphERFilePath();

		if (filePath != null) {
			updateGenerateFileGUI(true, filePath);
		}
		// generate system and bigrapher
		else {
			boolean isGenerated = generateSystemAndBigraphERFiles();
			if (isGenerated) {
				updateGenerateFileGUI(true, SystemHandler.getBigraphERFilePath());
			}

		}
	}

	protected boolean generateSystemAndBigraphERFiles() {

		// save to local
		boolean isGenerated = SystemHandler.isSystemModelGenerated();

		if (!isGenerated) {
			// generate system
			isGenerated = SystemHandler.generateSystemModel();

			// if (isGenerated) {
			// // generate bigrapher
			// isGenerated = SystemHandler.generateBigraphER();
			// }
		}

		// failed to create a system model
		if (!isGenerated) {
			return false;
		}

		FileChooser fileChooser = new FileChooser();

		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("BigraphER files (*.big)", "*.big");

		fileChooser.getExtensionFilters().add(extFilter);

		File savedFile = fileChooser.showSaveDialog(null);
		// if cancelled
		if (savedFile == null) {
			return false;
		}

		String filePath = savedFile.getAbsolutePath();

		return SystemHandler.extractAndSaveBigraphERFile(filePath);

		// // generate model
		// boolean isGenerated = SystemHandler.generateSystemModel(filePath);
		//
		// boolean isBRSGenerated =
		// SystemHandler.extractAndSaveBigraphERFile(filePath);

		// return true;
	}

	protected void updateGenerateFileGUI(boolean isGenerated, String fileName) {

		btnGenerateBigrapher.setVisible(!isGenerated);
		imgOpenBigrapher.setVisible(isGenerated);
		imgOpenBigrapherEmpty.setVisible(!isGenerated);
		lblBigraphERName.setVisible(isGenerated);
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				lblBigraphERName.setText(fileName);
				lblBigraphERName.setTooltip(new Tooltip(fileName));
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

	protected void showNoBigraphFileError() {

//		DropShadow borderGlow = createBorderGlow(50, Color.RED);
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// lblBigraphERName.setEffect(borderGlow);
				lblBigraphERName.setStyle("-fx-border-color: red;");
				
				Timer t = new Timer();
				t.schedule(new TimerTask() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						// lblBigraphERName.setEffect(null);
						lblBigraphERName.setStyle("-fx-border-color: black;");						
					}
				}, INTERVAL);
			}
		});
	}

	protected void showStatesError() {

		DropShadow borderGlow = createBorderGlow(35, Color.RED);
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				textFieldStatesnumber.setEffect(borderGlow);
				Timer t = new Timer();
				t.schedule(new TimerTask() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						textFieldStatesnumber.setEffect(null);
					}
				}, INTERVAL);
			}
		});

	}

	protected void showOutPutFolderError() {

		DropShadow borderGlow = createBorderGlow(35, Color.RED);
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				textFieldFolder.setEffect(borderGlow);
				Timer t = new Timer();
				t.schedule(new TimerTask() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						textFieldFolder.setEffect(null);
					}
				}, INTERVAL);
			}
		});

	}
}
