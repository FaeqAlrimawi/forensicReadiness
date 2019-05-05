//package application;
//
//import java.awt.Desktop;
//import java.io.File;
//import java.io.IOException;
//import java.net.URL;
//import java.util.Timer;
//import java.util.TimerTask;
//
//import com.eteks.sweethome3d.adaptive.forensics.BigrapherStatesChecker;
//import com.eteks.sweethome3d.adaptive.forensics.SystemHandler;
//
//import javafx.application.Platform;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.scene.control.Button;
//import javafx.scene.control.CheckBox;
//import javafx.scene.control.Label;
//import javafx.scene.control.ProgressBar;
//import javafx.scene.control.ProgressIndicator;
//import javafx.scene.control.Separator;
//import javafx.scene.control.TextField;
//import javafx.scene.control.Tooltip;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.input.MouseEvent;
//import javafx.stage.DirectoryChooser;
//
//public class MainScreenController {
//
//  @FXML
//  private Button              btnEditActions;
//
//  @FXML
//  private Button              btnGenerateSystemModel;
//
//  @FXML
//  private Button              btnSelectFolder;
//
//  @FXML
//  private CheckBox            checkBoxGenerateBig;
//
//  @FXML
//  private Label               lblSystemModelName;
//
//  @FXML
//  private ImageView           imgSuccessful;
//
//  @FXML
//  private ImageView           imgWrong;
//
//  @FXML
//  private ImageView           imgOpenFile;
//
//  @FXML
//  private ImageView           imgOpenBigFile;
//
//  @FXML
//  private ImageView           imgDelete;
//
//  @FXML
//  private TextField           textFieldSelectedFolder;
//
//  // @FXML
//  // private ImageView imgTrash;
//  //
//  // @FXML
//  // private ImageView imgTrashEmpty;
//
//  @FXML
//  private Separator           separatorLine;
//
//  @FXML
//  private ProgressBar         progressBarAnalyse;
//
//  @FXML
//  private ProgressIndicator   progressIndicatorGeneration;
//
//  @FXML
//  private Label               lblTransitionCheck;
//
//  @FXML
//  private ImageView           imgTransitionCheck;
//
//  @FXML
//  private Label               lblStatesCheck;
//
//  @FXML
//  private ImageView           imgStatesCheck;
//
//  BigrapherStatesChecker      checker;
//
//  private File                selectedDirectory;
//
//  private GeneralFXFrame      frameEditActions;
//  private GeneralFXFrame      frameLTSGenerator;
//
//  private static final String IMAGES_FOLDER = "com/eteks/sweethome3d/swing/resources/system_icons/";
//  private static final String IMAGE_CORRECT = IMAGES_FOLDER + "correct.png";
//  private static final String IMAGE_WRONG   = IMAGES_FOLDER + "wrong.png";
//
//  private static final int    INTERVAL      = 2000;
//
//  
//  @FXML
//  public void initialize() {
//
//    final boolean isAlreadyGenerated = SystemHandler.isSystemModelGenerated();
//    Platform.runLater(new Runnable() {
//
//      @Override
//      public void run() {
//        // TODO Auto-generated method stub
//
//        if (isAlreadyGenerated) {
//          updateModel(true);
//        }
//      }
//
//    });
//
//    selectedDirectory = new File("D:/Bigrapher data/lero");
//   
//  }
//
//  @FXML
//  void openActionPanel(ActionEvent event) {
//
//    // open action panel
//    if (frameEditActions == null) {
//      frameEditActions = new GeneralFXFrame("System Actions", GeneralFXFrame.JFXPanel.ADD_ACTIONS_PANEL);
//
//      frameEditActions.setVisible(true);
//    } else {
//      if (frameEditActions.isClosed()) {
//        frameEditActions = null;
//        frameEditActions = new GeneralFXFrame("System Actions", GeneralFXFrame.JFXPanel.ADD_ACTIONS_PANEL);
//        frameEditActions.setVisible(true);
//      }
//
//    }
//
//  }
//  
//  @FXML
//  void openLTSGenerator(ActionEvent event) {
//
//    // open lts panel
//    if (frameLTSGenerator == null) {
//      frameLTSGenerator = new GeneralFXFrame("LTS Generator", GeneralFXFrame.JFXPanel.LTS_VIEW_PANEL);
//
//      frameLTSGenerator.setVisible(true);
//    } else {
//      if (frameLTSGenerator.isClosed()) {
//        frameLTSGenerator = null;
//        frameLTSGenerator = new GeneralFXFrame("LTS Generator", GeneralFXFrame.JFXPanel.LTS_VIEW_PANEL);
//        frameLTSGenerator.setVisible(true);
//      }
//
//    }
//
//  }
//
//  @FXML
//  void generateSystemModel(ActionEvent event) {
//
//    final boolean isAlreadyGenerated = SystemHandler.isSystemModelGenerated();
//
//    // just update
//    if (isAlreadyGenerated) {
//      updatesystemModel();
//      return;
//    }
//
//    // save to local
//    String filePath = GeneralFXFrame.getHomeView().showSaveSystemModelDialog("homeModel");
//
//    // if cancelled
//    if (filePath == null || filePath.isEmpty()) {
//
//      Platform.runLater(new Runnable() {
//
//        @Override
//        public void run() {
//          // TODO Auto-generated method stub
//          progressIndicatorGeneration.setVisible(false);
//          btnGenerateSystemModel.setText("Generate Model");
//        }
//
//      });
//
//      return;
//    }
//
//    // generate model
//    final boolean isGenerated = SystemHandler.generateSystemModel(filePath);
//
//    // check if bigrapher generation is needed
//    boolean isGenerateBig = checkBoxGenerateBig.isSelected();
//
//    if (isGenerateBig) {
//      SystemHandler.extractAndSaveBigraphERFile(filePath);
//    }
//
//    if (isGenerated) {
//      updateModel(isGenerated);
//      updateGenerateModelImage(true);
//    } else {
//      updateModel(false);
//      updateGenerateModelImage(false);
//    }
//
//  }
//
//  protected void updatesystemModel() {
//
//    final boolean isModelUpdated = SystemHandler.updateAndSaveSystemModel();
//
//    final boolean isGenerateBig = checkBoxGenerateBig.isSelected();
//
//    if (isGenerateBig) {
//      final boolean isBigUpdated = SystemHandler.extractAndSaveBigraphERFile();
//
//      if (isModelUpdated && isBigUpdated) {
//        updateGenerateModelImage(true);
//      } else {
//        updateGenerateModelImage(false);
//      }
//
//    } else {
//
//      if (isModelUpdated) {
//        updateGenerateModelImage(true);
//      } else {
//        updateGenerateModelImage(false);
//      }
//    }
//
//  }
//
//  protected void updateGenerateModelImage(boolean showCorrect) {
//
//    Platform.runLater(new Runnable() {
//
//      @Override
//      public void run() {
//
//        progressIndicatorGeneration.setVisible(false);
//
//        if (showCorrect) {
//          // TODO Auto-generated method stub
//          imgSuccessful.setVisible(true);
//          imgWrong.setVisible(false);
//
//          Timer timer = new Timer();
//          timer.schedule(new TimerTask() {
//
//            @Override
//            public void run() {
//              // TODO Auto-generated method stub
//              imgSuccessful.setVisible(false);
//            }
//          }, INTERVAL);
//        } else {
//          imgSuccessful.setVisible(false);
//          imgWrong.setVisible(true);
//          Timer timer = new Timer();
//          timer.schedule(new TimerTask() {
//
//            @Override
//            public void run() {
//              // TODO Auto-generated method stub
//              imgWrong.setVisible(false);
//            }
//          }, INTERVAL);
//        }
//      }
//    });
//  }
//
//  @FXML
//  void openSystemModelFile(MouseEvent event) {
//
//    // open system model file
//    if (SystemHandler.isSystemModelGenerated()) {
//      String path = SystemHandler.getFilePath();
//
//      if (path != null) {
//        try {
//          Desktop.getDesktop().open(new File(path));
//        } catch (IOException ex) {
//          // TODO Auto-generated catch block
//          ex.printStackTrace();
//        }
//      }
//
//    }
//  }
//
//  @FXML
//  void deleteModel(MouseEvent event) {
//
//    if (!SystemHandler.isSystemModelGenerated()) {
//      return;
//    }
//
//    final boolean isDeleted = SystemHandler.deleteSystemModel();
//
//    if (isDeleted) {
//      Platform.runLater(new Runnable() {
//
//        @Override
//        public void run() {
//          // TODO Auto-generated method stub
//          imgDelete.setVisible(false);
//          imgOpenFile.setVisible(false);
//          imgOpenBigFile.setVisible(false);
//          btnGenerateSystemModel.setText("Generate Model");
//          lblSystemModelName.setText("[ ]");
//          lblSystemModelName.setTooltip(new Tooltip("No system model is generated. "));
//        }
//      });
//    }
//
//  }
//
//  @FXML
//  void openBigraphERFile(MouseEvent event) {
//
//    // open system model file
//    if (SystemHandler.isSystemModelGenerated()) {
//      String path = SystemHandler.getBigraphERFilePath();
//
//      if (path != null) {
//        try {
//          Desktop.getDesktop().open(new File(path));
//        } catch (IOException ex) {
//          // TODO Auto-generated catch block
//          ex.printStackTrace();
//        }
//      }
//
//    }
//
//  }
//
//  protected void updateModel(boolean hasModel) {
//
//    Platform.runLater(new Runnable() {
//
//      @Override
//      public void run() {
//        // TODO Auto-generated method stub
//
//        progressIndicatorGeneration.setVisible(false);
//
//        if (hasModel) {
//          // set name of model
//          lblSystemModelName.setText("[" + SystemHandler.getSystemModelName() + "]");
//          lblSystemModelName.setTooltip(new Tooltip(SystemHandler.getFilePath()));
//
//          // show open file image
//          imgOpenFile.setVisible(true);
//
//          // show line image
//          separatorLine.setVisible(true);
//
//          // show delete image
//          imgDelete.setVisible(true);
//
//          // show open file for big
//          if (checkBoxGenerateBig.isSelected()) {
//            imgOpenBigFile.setVisible(true);
//          }
//
//          // update button text
//          btnGenerateSystemModel.setText("Update Model");
//
//        } else {
//          lblSystemModelName.setText("[ ]");
//          imgOpenFile.setVisible(false);
//          separatorLine.setVisible(false);
//          separatorLine.setVisible(false);
//          imgOpenBigFile.setVisible(false);
//          // updateGenerateModelImage(false);
//          imgDelete.setVisible(false);
//        }
//
//      }
//    });
//  }
//
//  @FXML
//  void selectFolder(ActionEvent event) {
//
//    DirectoryChooser chooser = new DirectoryChooser();
//    // chooser.setTitle("Select Folder");
//    if (selectedDirectory != null) {
//      chooser.setInitialDirectory(selectedDirectory);
//    }
//
//    selectedDirectory = chooser.showDialog(null);
//
//    if (selectedDirectory != null) {
//      
//      Platform.runLater(new Runnable() {
//
//        @Override
//        public void run() {
//          // TODO Auto-generated method stub
//          textFieldSelectedFolder.setText(selectedDirectory.getAbsolutePath());
//        }
//      });
//
//      //clear check pane
//      updateTransitionCheckingPane(null, null);
//      updateStatesCheckingPane(null, null);
//      
//      // check states and transitions
//      checkStates();
//    }
//
//  }
//
//  protected void checkStates() {
//
//    if (selectedDirectory == null) {
//      return;
//    }
//
//    new Thread(new Runnable() {
//
//      @Override
//      public void run() {
//        // TODO Auto-generated method stub
//
//        // analyse folder for states and transition system
//        checker = new BigrapherStatesChecker();
//        int result = checker.checkStates(selectedDirectory.getAbsolutePath());
//        doneStateChecking(result);
//      }
//    }).start();
//
//    progressBarAnalyse.setVisible(true);
//
//  }
//
//  protected void doneStateChecking(int result) {
//
//    progressBarAnalyse.setVisible(false);
//
//    switch (result) {
//      //everything is fine
//      case BigrapherStatesChecker.PASS:
//        updateTransitionCheckingPane(IMAGE_CORRECT, "Transitions #: " + checker.getTransitionsNumber());
//        updateStatesCheckingPane(IMAGE_CORRECT, "States #: " + checker.getStatesNumber());
//        break;
//
//        //no transition file
//      case BigrapherStatesChecker.TRANSITION_FILE_MISSING:
//        updateTransitionCheckingPane(IMAGE_WRONG, "[transitions.json] File is missing");
//        break;
//
//        //missing states
//      case BigrapherStatesChecker.STATES_MISSING:
//        updateTransitionCheckingPane(IMAGE_CORRECT, "Transitions #: " + checker.getTransitionsNumber());
//        updateStatesCheckingPane(IMAGE_WRONG, "Some states are missing: " + checker.getStatesNotFound());
//        break;
//        
//      default:
//        break;
//    }
//
//  }
//
//  protected void updateTransitionCheckingPane(String transitionImg, String transitionMsg) {
//
//    updateImage(transitionImg, imgTransitionCheck);
//
//    Platform.runLater(new Runnable() {
//
//      @Override
//      public void run() {
//        // TODO Auto-generated method stub
//        if(transitionMsg != null) {
//          lblTransitionCheck.setText(transitionMsg);  
//          lblTransitionCheck.setTooltip(new Tooltip(transitionMsg));
//        } else{
//          lblTransitionCheck.setText("");
//        }
//        
//      }
//    });
//
//  }
//
//  protected void updateStatesCheckingPane(String statesImg, String statesMsg) {
//
//    updateImage(statesImg, imgStatesCheck);
//
//    Platform.runLater(new Runnable() {
//
//      @Override
//      public void run() {
//        // TODO Auto-generated method stub
//        if(statesMsg != null) {
//          lblStatesCheck.setText(statesMsg);
//          lblStatesCheck.setTooltip(new Tooltip(statesMsg));
//        } else{
//          lblStatesCheck.setText("");
//        }
//  
//      }
//    });
//
//  }
//
////  protected void updateCheckingPane(String transitionImg, String statesImg) {
////
////    updateImage(transitionImg, imgTransitionCheck);
////    updateImage(statesImg, imgStatesCheck);
////
////    Platform.runLater(new Runnable() {
////
////      @Override
////      public void run() {
////        // TODO Auto-generated method stub
////        lblTransitionCheck.setText("Transitions #: " + checker.getTransitionsNumber());
////        lblStatesCheck.setText("States #: " + checker.getStatesNumber());
////      }
////    });
////
////  }
//
//  protected void updateImage(String imgPath, ImageView imgView) {
//
//    if(imgPath == null) {
//      imgView.setVisible(false);
//      
//    } else {
//      
//      imgView.setVisible(true);
//      
//      URL urlImage = getClass().getClassLoader().getResource(imgPath);
//
//      if (urlImage != null) {
//        Image img;
//        try {
//          img = new Image(urlImage.openStream());
//          imgView.setImage(img);
//        } catch (IOException ex) {
//          // TODO Auto-generated catch block
//          ex.printStackTrace();
//        }
//
//      } else {
//        System.out.println(imgPath + " Not found!");
//      }
//      
//    }
//    
//  }
//
//}
