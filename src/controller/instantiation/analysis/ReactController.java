package controller.instantiation.analysis;

import java.net.URL;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import controller.utlities.ConditionAreaHandler;
import core.brs.parser.ActionWrapper;
import core.brs.parser.BigraphWrapper;
import core.brs.parser.utilities.JSONTerms;
import core.instantiation.analysis.TraceMiner;
import javafx.application.Platform;
//import controller.utilities.ConditionAreaHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class ReactController {

	@FXML
	private HBox hboxCodeArea;

	@FXML
	private Label lblActionName;

	private static final String STYLE_SHEET = "../../../resources/styles/java-keywords.css";

	private ConditionAreaHandler reactArea;

	private TraceMiner miner;

	@FXML
	public void initialize() {

		reactArea = new ConditionAreaHandler();
		setConditionsCodeArea(reactArea);

	}

	protected void setConditionsCodeArea(ConditionAreaHandler conditionArea) {

		// preCodeArea = new ConditionAreaHandler();

		CodeArea pCodeArea = conditionArea.createCodeArea();
		
		pCodeArea.setWrapText(true);
//		pCodeArea.setEditable(false);
		
		StackPane codeAreaPane = new StackPane(new VirtualizedScrollPane<>(pCodeArea));

		URL style = getClass().getResource(STYLE_SHEET);

		if (style != null) {
			codeAreaPane.getStylesheets().add(style.toExternalForm());
		}

		codeAreaPane.setPrefWidth(843);
		codeAreaPane.setPrefHeight(265);

		codeAreaPane.setBorder(new Border(
				new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

		// VBox vbox = new VBox();
		//
		// // label for the code area
		// Label label = new Label(lbl);
		// label.setFont(new Font(14));
		//
		// vbox.getChildren().add(label);

		// HBox hbox = new HBox();
		// Pane pane = new Pane();
		// pane.setPrefWidth(100);

		hboxCodeArea.getChildren().add(codeAreaPane);
		
		// hboxCodeArea.getChildren().add(pane);

		// vbox.getChildren().add(hbox);

		// vBoxActionDetails.getChildren().add(vbox);
	}

	public void setTraceMiner(TraceMiner miner) {
		this.miner = miner;
	}

	public void showReact(String actionName) {

		if (miner == null) {
			System.out.println("miner == null");
			return;
		}

		ActionWrapper act = miner.getActionWrapper(actionName);

		StringBuilder strBldr = new StringBuilder();

		if (act == null) {
			System.err.println("act == null");
			return;
		}

		// set title
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				lblActionName.setText("Name: "+act.getReactName() + " (" + act.getActionName() + ")");
			}
		});

		String newLine = System.getProperty("line.separator");

		// get pre
		BigraphWrapper pre = act.getPrecondition();

		if (pre != null) {
			strBldr.append(JSONTerms.BIG_COMMENT).append("Precondition").append(newLine);

			strBldr.append(pre.getBigraphERString()).append(newLine).append(newLine);
			// ->
			strBldr.append(JSONTerms.BIG_IMPLY).append(newLine).append(newLine);
		}

		// get post
		BigraphWrapper post = act.getPostcondition();

		if (post != null) {
			strBldr.append(JSONTerms.BIG_COMMENT).append("Postcondition").append(newLine);
			strBldr.append(post.getBigraphERString());
		}

		// set condition
		reactArea.setText(strBldr.toString());
	}

}
