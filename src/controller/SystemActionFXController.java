package controller;

import java.util.LinkedList;
import java.util.List;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import com.eteks.sweethome3d.adaptive.forensics.SystemHandler;

import controller.instantiation.analysis.TraceViewerController;
import controller.utilities.ConditionAreaHandler;
import controller.utlities.AutoCompleteTextArea;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Callback;

public class SystemActionFXController {
	@FXML
	private HBox hBoxButtons;

	@FXML
	private VBox vBoxActionDetails;

	@FXML
	private TextField txtFieldName;

//	@FXML
//	private TextArea txtFieldPre;
//
//	@FXML
//	private TextArea txtFieldPost;

	@FXML
	private Button btnAdd;

	@FXML
	private Button btnRemove;

	@FXML
	private Label lblEmpty;

	// @FXML
	// private Tab tabSysActions;

	// @FXML
	// private TabPane actionsTabPane;

	@FXML
	private ListView<String> listActions;

	@FXML
	private ListView<String> listCatalogActions;

	private ObservableList<String> actions;

	private ObservableList<String> originalActions;

	@FXML
	private ObservableList<String> catalogActions;

	private ConditionAreaHandler preCodeArea;
	private ConditionAreaHandler postCodeArea;
	private static final String STYLE_SHEET = "resources/styles/java-keywords.css";

	private List<String> actionsToBeAddedFromCatalog;
	private List<String> actionsToBeDeletedFromSystem;
	private AutoCompleteTextArea autoCompleter;

	// private static final String ADD_ALL = "Select All";

	// private static boolean isAllSelected = false;

	@FXML
	public void initialize() {

		actions = FXCollections.observableArrayList();
		catalogActions = FXCollections.observableArrayList();

		// add all checkbox
		// catalogActions.add(ADD_ALL);

		// set system actions list
		listActions.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {

			public ListCell<String> call(ListView<String> param) {
				// TODO Auto-generated method stub
				ActionCell actCell = new ActionCell();
				return actCell;
			}
		});

		// add listener to select item
		listActions.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// TODO Auto-generated method stub
				showAction(newValue);

			}
		});

		// set catalog actions list
		listCatalogActions.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {

			public ListCell<String> call(ListView<String> param) {
				// TODO Auto-generated method stub
				CatalogActionCell catCell = new CatalogActionCell();
				return catCell;
			}
		});

		// add listener to select item
		listCatalogActions.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// TODO Auto-generated method stub
				showAction(newValue);

			}
		});

		// update action list
		String[] sysActions = SystemHandler.getActions();

		if (sysActions != null && sysActions.length > 0) {
			actions.addAll(sysActions);
			originalActions.addAll(sysActions);
			lblEmpty.setVisible(false);
		} else {
			lblEmpty.setVisible(true);
		}

		listActions.setItems(actions);

		// update catalog
		String[] catActions = SystemHandler.getCatalogActions();

		if (catActions != null) {
			catalogActions.addAll(catActions);
			listCatalogActions.setItems(catalogActions);
		}

		actionsToBeAddedFromCatalog = new LinkedList<String>();
		actionsToBeDeletedFromSystem = new LinkedList<String>();

		// autocomplete
//		autoCompleter = new AutoCompleteTextArea();

//		txtFieldPre.textProperty().addListener(new ChangeListener<String>() {
//			public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
//
//				autoCompleter.autoComplete(txtFieldPre, s2, s);
//			}
//		});
//
//		txtFieldPre.focusedProperty().addListener(new ChangeListener<Boolean>() {
//			public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean,
//					Boolean aBoolean2) {
//				autoCompleter.hidePopup();
//			}
//		});
//
//		txtFieldPost.textProperty().addListener(new ChangeListener<String>() {
//			public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
//
//				autoCompleter.autoComplete(txtFieldPost, s2, s);
//			}
//		});
//
//		txtFieldPost.focusedProperty().addListener(new ChangeListener<Boolean>() {
//			public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean,
//					Boolean aBoolean2) {
//				autoCompleter.hidePopup();
//			}
//		});

		// set focus on text field of name
		txtFieldName.requestFocus();

		// init conditions
		preCodeArea = new ConditionAreaHandler();
		setConditionsCodeArea(preCodeArea, "Precondition");

		postCodeArea = new ConditionAreaHandler();
		setConditionsCodeArea(postCodeArea, "Postcondition");

		updateButtonsPane();
	}

	protected void setConditionsCodeArea(ConditionAreaHandler conditionArea, String lbl) {

		// preCodeArea = new ConditionAreaHandler();

		CodeArea pCodeArea = conditionArea.createCodeArea();
		StackPane codeAreaPane = new StackPane(new VirtualizedScrollPane<>(pCodeArea));

		codeAreaPane.getStylesheets()
				.add(TraceViewerController.class.getClassLoader().getResource(STYLE_SHEET).toExternalForm());

		codeAreaPane.setPrefWidth(650);
		codeAreaPane.setPrefHeight(150);

		codeAreaPane.setBorder(new Border(
				new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

		VBox vbox = new VBox();

		// label for the code area
		Label label = new Label(lbl);
		label.setFont(new Font(14));

		vbox.getChildren().add(label);

		HBox hbox = new HBox();
		Pane pane = new Pane();
		pane.setPrefWidth(100);

		hbox.getChildren().add(codeAreaPane);
		hbox.getChildren().add(pane);

		vbox.getChildren().add(hbox);

		vBoxActionDetails.getChildren().add(vbox);
	}

	protected void updateButtonsPane() {

		vBoxActionDetails.getChildren().remove(hBoxButtons);

		vBoxActionDetails.getChildren().add(hBoxButtons);
	}

	/**
	 * add new action
	 * 
	 * @param event
	 */
	@FXML
	void addNewAction(ActionEvent event) {

		String actionName = txtFieldName.getText();
		String pre = preCodeArea.getText();
		String post = postCodeArea.getText();

		addActionToSysList(actionName, pre, post);
	}

	@FXML
	void removeAction(ActionEvent event) {

		String actionName = txtFieldName.getText();

		removeActionFromSysList(actionName);

	}

	// void autoComplete(String oldStr, String newStr, int pos, Bounds bound) {
	//
	// AutoCompleteTextArea auto = new AutoCompleteTextArea(oldStr, newStr, pos,
	// bound);
	//
	//
	// }
	//
	/**
	 * shows action details
	 * 
	 * @param actionName
	 */
	void showAction(final String actionName) {

		Platform.runLater(new Runnable() {

			public void run() {
				String pre = "";
				String post = "";

				// if system action then view it by retrieving it pre and post
				// from sys
				// handler
				if (actions.contains(actionName)) {
					pre = SystemHandler.getActionPre(actionName);
					post = SystemHandler.getActionPost(actionName);

					// enable remove button
					btnRemove.setDisable(false);
					// get action details from catalog
				} else if (catalogActions.contains(actionName)) {
					pre = SystemHandler.getCatalogActionPrecondition(actionName);
					post = SystemHandler.getCatalogActionPostcondition(actionName);

					// disable remove button
					btnRemove.setDisable(true);
				}

				// update name
				txtFieldName.setText(actionName);

				// update pre
				preCodeArea.setText(pre);

				// update post
				postCodeArea.setText(post);
			}
		});

	}

	protected void addActionToSysList(String actionName, String pre, String post) {

		// add to sys
		boolean isAdded = SystemHandler.addAction(actionName, pre, post);

		// add to list
		if (isAdded) {
			if (!actions.contains(actionName)) {
				actions.add(actionName);
				listActions.setItems(actions);

				// update catalog if needed
				if (catalogActions.contains(actionName)) {
					updateSelectedActionsInCatalog();
				}

				// check if empty label if visible
				if (lblEmpty.isVisible()) {
					lblEmpty.setVisible(false);
				}
			}
		}
	}

	protected void removeActionFromSysList(String actionName) {

		// remvoe from sys
		boolean isRemoved = SystemHandler.removeAction(actionName);

		// remove from list
		if (isRemoved) {
			if (actions.contains(actionName)) {
				actions.remove(actionName);
				listActions.setItems(actions);

				// update catalog if needed
				if (catalogActions.contains(actionName)) {
					updateSelectedActionsInCatalog();
				}

				// check if actions list is empty, if so, show empty label
				if (actions.size() == 0) {
					lblEmpty.setVisible(true);
				}
			}
		}
	}

	protected void updateSystemActions() {

		// update actions list in System Action tab
		List<String> toRemove = new LinkedList<String>();

		// remove actions
		for (String act : actions) {
			if (SystemHandler.isRemoved(act)) {
				toRemove.add(act);
			}
		}

		actions.removeAll(toRemove);

		// add new actions
		for (String act : SystemHandler.getActions()) {
			if (!actions.contains(act)) {
				actions.add(act);

			}
		}

		listActions.setItems(actions);

		// update catalog selected actions
		// updateSelectedActionsInCatalog();

	}

	protected void updateSelectedActionsInCatalog() {

		List<String> newActions = new LinkedList<String>(catalogActions);

		catalogActions.removeAll(newActions);
		catalogActions.addAll(newActions);

		// if(isAllSelected) {
		// addAllCatalogActions();
		// isAllSelected = false;
		// }
		//
		// catalogActions.add(ADD_ALL);
		listCatalogActions.setItems(catalogActions);

	}

	/**
	 * updates the list without adding actions to system
	 * 
	 */
	protected void updateListActionsInCatalog() {

		List<String> newActions = new LinkedList<String>(catalogActions);

		catalogActions.removeAll(newActions);
		catalogActions.addAll(newActions);
		// catalogActions.add(ADD_ALL);
		listCatalogActions.setItems(catalogActions);

	}

	// protected void addAllCatalogActions() {
	//
	// for(String act : catalogActions) {
	// if(act.equalsIgnoreCase(ADD_ALL)) {
	// continue;
	// }
	// SystemHandler.addActionFromCatalog(act);
	// }
	// }
	//
	// protected void removeAllCatalogActions() {
	//
	// for(String act : catalogActions) {
	// if(act.equalsIgnoreCase(ADD_ALL)) {
	// continue;
	// }
	// SystemHandler.deleteAction(act);
	// }
	// }

	public class ActionCell extends ListCell<String> {
		// private Button btnDel;
		// private Label name ;
		private HBox pane;
		private CheckBox checkBox;

		public ActionCell() {
			super();

			setOnMouseClicked(new EventHandler<MouseEvent>() {

				public void handle(MouseEvent event) {
					// do something
				}
			});

			// btnDel = new Button("Remove");
			checkBox = new CheckBox();
			checkBox.setSelected(false);
			checkBox.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent event) {
					// TODO Auto-generated method stub

					// if not selected
					if (!checkBox.isSelected()) {
						String actionName = checkBox.getText();

						actionsToBeDeletedFromSystem.remove(actionName);
						// if the system has the action as remvoed but it is
						// checked then
						// restore the action
						// if (SystemHandler.isRemoved(actionName)) {
						// boolean isRestored =
						// SystemHandler.restoreAction(actionName);
						// if (isRestored) {
						// System.out.println("action [" + actionName + "] is
						// restored");
						// }
						// }

					} else {
						String actionName = checkBox.getText();

						actionsToBeDeletedFromSystem.add(actionName);
						// if the system has the action but it is not checked
						// then delete
						// the action
						// if (SystemHandler.hasAction(actionName)) {
						// boolean isRemoved =
						// SystemHandler.deleteAction(actionName);
						//
						// if (isRemoved) {
						// System.out.println("action [" + actionName + "] is
						// removed");
						//
						// }
						// }
					}
				}
			});

			// btnDel.setOnAction(new EventHandler<ActionEvent>() {
			// public void handle(ActionEvent event) {
			// System.out.println("Action: "+getItem());
			// }
			// });
			// name = new Label();
			pane = new HBox(checkBox);
			pane.setSpacing(10);
			setText(null);
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			setEditable(false);
			if (!empty && item != null) {
				// name.setText(item);
				checkBox.setText(item);

				if (SystemHandler.hasAction(item)) {
					checkBox.setSelected(false);
				} else if (SystemHandler.isRemoved(item)) {
					checkBox.setSelected(true);
				}
				// if (empty || item == null) {
				//// checkBox.setSelected(false);
				//// checkBox.setText("");
				// setText(null);
				// setGraphic(null);
				// } else {
				// setText(item);
				// }
				setGraphic(pane);
			} else {
				setText(null);
				checkBox.setText(null);
				setGraphic(null);
			}
		}
	}

	public class CatalogActionCell extends ListCell<String> {
		// private Button btnDel;
		// private Label name ;
		private HBox pane;
		private CheckBox checkBox;

		public CatalogActionCell() {
			super();

			setOnMouseClicked(new EventHandler<MouseEvent>() {

				public void handle(MouseEvent event) {
					// do something
				}
			});

			// btnDel = new Button("Remove");
			checkBox = new CheckBox();
			checkBox.setSelected(false);
			checkBox.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent event) {
					// TODO Auto-generated method stub

					// if selected
					if (checkBox.isSelected()) {

						String actionName = checkBox.getText();

						// if(actionName.equalsIgnoreCase(ADD_ALL)) {
						// isAllSelected = true;
						// updateListActionsInCatalog();
						// } else
						// if the system has the action as remvoed but it is
						// checked then
						// restore the action
						if (!SystemHandler.hasAction(actionName)) {
							actionsToBeAddedFromCatalog.add(actionName);
							// String pre =
							// SystemHandler.getCatalogActionPrecondition(actionName);
							// String post =
							// SystemHandler.getCatalogActionPostcondition(actionName);
							//
							// boolean isAdded =
							// SystemHandler.addAction(actionName, pre,
							// post);
							// if (isAdded) {
							// System.out.println("action [" + actionName + "]
							// is Added");
							// }
						}
						// else {
						// checkBox.setDisable(true);
						// }

						// unchecked
					} else {
						String actionName = checkBox.getText();

						actionsToBeAddedFromCatalog.remove(actionName);
						// if(actionName.equalsIgnoreCase(ADD_ALL)) {
						// isAllSelected = false;
						// updateListActionsInCatalog();
						// } else
						// if the system has the action but it is not checked
						// then delete
						// the action
						// if (SystemHandler.hasAction(actionName)) {
						// boolean isRemoved =
						// SystemHandler.deleteAction(actionName);
						//
						// if (isRemoved) {
						// System.out.println("action [" + actionName + "] is
						// Removed");
						//
						// }
						// }
					}
				}
			});

			// btnDel.setOnAction(new EventHandler<ActionEvent>() {
			// public void handle(ActionEvent event) {
			// System.out.println("Action: "+getItem());
			// }
			// });
			// name = new Label();
			pane = new HBox(checkBox);
			pane.setSpacing(10);
			setText(null);
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			setEditable(false);

			// name.setText(item);
			checkBox.setText(item);

			if (!empty && item != null) {

				// if add all
				// if(item.equalsIgnoreCase(ADD_ALL)) {
				// if(checkBox.isSelected()) {
				// isAllSelected = true;
				//// addAllCatalogActions();
				// } else {
				// isAllSelected = false;
				//// removeAllCatalogActions();
				// }
				//
				// updateIndex(0);
				// //if action
				// } else {
				boolean hasAction = SystemHandler.hasAction(item);

				if (hasAction) { // add to system actions
					checkBox.setSelected(true);
					checkBox.setDisable(true);
					// }
					// else if(isAllSelected){ //selected but not added
					// checkBox.setSelected(true);
					// checkBox.setDisable(false);
				} else { // not selected
					checkBox.setSelected(false);
					checkBox.setDisable(false);
				}

				setGraphic(pane);

			} else {
				setText(null);
				checkBox.setText(null);
				setGraphic(null);
			}
		}

	}

}
