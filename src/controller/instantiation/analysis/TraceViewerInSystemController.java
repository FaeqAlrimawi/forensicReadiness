package controller.instantiation.analysis;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import core.brs.parser.utilities.JSONTerms;
import core.instantiation.analysis.TraceMiner;
import ie.lero.spare.franalyser.utility.Digraph;
import ie.lero.spare.pattern_instantiation.GraphPath;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.stage.Window;

public class TraceViewerInSystemController {

	@FXML
	private HBox hboxTraceDetails;
	
	@FXML
	private ComboBox<Integer> comboBoxAddedTraces;

	@FXML
	private HBox hboxTraceNavigator;

	// @FXML
	// private HBox hboxBottomPart;

	@FXML
	private HBox hboxIndicator;

	@FXML
	private Label lblProgressIndicator;

	@FXML
	private VBox vBoxCommands;

	@FXML
	private ScrollPane scrollPaneEntities;

	@FXML
	private HBox hboxShowEntities;

	@FXML
	private StackPane mainStackPane;

	@FXML
	private ProgressIndicator progressIndicator;

	@FXML
	private Button btnShowPreviousStates;

	@FXML
	private Button btnLoadTransitionSystem;

	@FXML
	private TextField txtFieldCurrentShownTrace;

	@FXML
	private Button btnSaveTrace;

	@FXML
	private Label lblNumberOfShownTraces;

	@FXML
	private ScrollPane scrollPaneTraceViewer;

	@FXML
	private Spinner<Integer> spinnerTopK;

	@FXML
	private HBox hboxEntities;

	private int minEntityNum = 1;
	private int maxEntityNum = 100;

	// key is entity name, value is occurrence
	private List<Map.Entry<String, Long>> topEntities;

	// used for common entities
	private int topK = 3;

	private Stage currentStage;

	private GraphPath trace;

	// holds the nodes of the trace (and any other added nodes e.g., prev/next)
	private Pane tracePane;

	// used to distinugish between drag and click
	private boolean isDragging = false;

	// reference to the task cell that contains the trace
	private TaskCell traceCell;

	// nodes of the trace
	private List<StackPane> traceNodes;

	// nodes of previous states (previous to the initial)
	private Map<Integer, List<StackPane>> mapPreviousNodes;

	// nodes of next states (next to the final state)
	// key is state, value is the list of nodes (as stackPane)
	private Map<Integer, List<StackPane>> mapNextNodes;

	// key is state, stackpane is graphical node
	private Map<Integer, StackPane> statesNodes;

	// key is state, value is a list of stackpanes that represent the outgoing
	// arrows from the state
	private Map<Integer, List<StackPane>> statesOutgoingArrows;
	private Map<Integer, List<StackPane>> statesIngoingArrows;

	// key is arrow, value is the arrow line
	private Map<StackPane, Line> arrowsLines;

	// key is arrow, value is the arrow label
	private Map<StackPane, StackPane> arrowsLabels;

	// added trace ids
	List<Integer> addedTracesIDs;
	List<Integer> highLightedTracesIDs;

	// private ContextMenu nodeContextMenu;

	private double sceneX, sceneY, layoutX, layoutY;

	private TraceMiner miner;

	private URL defualtTransitionSystemFilePath = getClass()
			.getResource("../../../resources/example/transitions_labelled.json");

	private static final String NODE_COLOUR = "white";
	private static final String DEFAULT_ARROW_COLOUR = "#333333";
	private static final Color TRACE_ARROW_COLOUR = Color.BLUE;
	private static final Color HIGHLIGHT_TRACE_ARROW_COLOUR = Color.GREEN;
	private static final Color ADDED_NODES_ARROW_COLOUR = Color.GREY;
	private static final double NODE_RADIUS = 25;
	private static final String STATE_STYLE = "-fx-font-size:18px;-fx-font-weight:bold;";
	private static final String EXTRA_STATE_STYLE = "-fx-font-size:18px;-fx-font-weight:bold; -fx-text-fill:grey";
	private static final String STATE_PERC_STYLE = "-fx-font-size:10px;-fx-text-fill:red;";
	private static final String ACTION_NAME_STYLE = "-fx-font-size:13px;";
	private static final String ACTION_PERC_STYLE = "-fx-font-size:10px;-fx-text-fill:red;";
	private static final String NOT_FOUND = "---";
	private static final String ARROW_ID_SEPARATOR = "-";

	private static final String HIGHLIGHT_STYLE = "-fx-stroke-width:3px;-fx-stroke:blue;-fx-opacity:1;";
	private static final String NORMAL_HIGHLIGHT_STYLE = "-fx-stroke-width:2px;-fx-stroke:grey;-fx-opacity:1;";
	private static final String ARROW_NORMAL_HIGHLIGHT_STYLE = "-fx-stroke-width:2px;-fx-stroke:grey;-fx-opacity:1;";
	private static final String TRACE_ARROW_HIGHLIGHT_STYLE = "-fx-stroke-width:2px;-fx-stroke:red;-fx-opacity:1;";

	// node context menu items
	private static final String MENU_ITEM_SHOW_NEXT = "Show Next";
	private static final String MENU_ITEM_HIDE_NEXT = "Hide Next";
	private static final String MENU_ITEM_SHOW_PREVIOUS = "Show Previous";
	private static final String MENU_ITEM_HIDE_PREVIOUS = "Hide Previous";
	private static final String MENU_ITEM_SHOW_OTHERS = "Show other Paths";

	private static final String[] NODE_CONTEXT_MENU_ITEMS = new String[] { MENU_ITEM_SHOW_NEXT, MENU_ITEM_SHOW_PREVIOUS,
			MENU_ITEM_HIDE_NEXT, MENU_ITEM_HIDE_PREVIOUS, MENU_ITEM_SHOW_OTHERS };

	private static final List<String> NODE_CONTEXT_MENU_IGNORE_ITEMS = new LinkedList<String>() {
		{
			add(MENU_ITEM_HIDE_NEXT);
			add(MENU_ITEM_HIDE_PREVIOUS);
		}
	};

	// key is state, value is the label for its percentage
	private Map<Integer, Label> mapStatePerc;

	// key is action, value is the label for its percentage
	private Map<String, List<Label>> mapActionPerc;

	// private static final int previousStateNum = 2;

	private int currentNumberOfShownTraces = 0;

	@FXML
	public void initialize() {

		tracePane = new Pane();
		mainStackPane.getChildren().add(tracePane);
		mainStackPane.setPadding(new Insets(20));

		// show trace by pressing enter
		txtFieldCurrentShownTrace.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				String strTraceIndex = txtFieldCurrentShownTrace.getText();

				try {
					int traceIndex = Integer.parseInt(strTraceIndex);

					List<Integer> currentTracesIDs = miner.getCurrentShownTraces();

					if (traceIndex > 0 && traceIndex <= currentTracesIDs.size()) {
						int traceID = currentTracesIDs.get(traceIndex - 1);
						GraphPath trace = miner.getTrace(traceID);

						if (trace != null) {
							this.trace = trace;
							reset(null);
						}
					} else {
						// it's a trace that's not in the filtered
						// do nothing at the moment.
						traceIndex = miner.getCurrentShownTraces().indexOf(trace.getInstanceID()) + 1;
						txtFieldCurrentShownTrace.setText(traceIndex + "");
					}

				} catch (NumberFormatException excp) {
					// set text back to current trace
					int traceIndex = miner.getCurrentShownTraces().indexOf(trace.getInstanceID()) + 1;
					txtFieldCurrentShownTrace.setText(traceIndex + "");
				}
			}
		});

		// allow only numbers
		txtFieldCurrentShownTrace.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// TODO Auto-generated method stub
				if (!newValue.matches("\\d*")) {
					// txtFieldCurrentShownTrace.setText(newValue.replaceAll("[^\\d]",
					// ""));
					int traceIndex = miner.getCurrentShownTraces().indexOf(trace.getInstanceID()) + 1;
					txtFieldCurrentShownTrace.setText(traceIndex + "/" + currentNumberOfShownTraces);
				}
			}
		});

		// scrollpane for trace
		// bind width and height to the scroll
		mainStackPane.prefHeightProperty().bind(Bindings.add(-5, scrollPaneTraceViewer.heightProperty()));
		mainStackPane.prefWidthProperty().bind(Bindings.add(-5, scrollPaneTraceViewer.widthProperty()));

		scrollPaneEntities.prefWidthProperty()
				.bind(Bindings.add(-1 * hboxShowEntities.getPrefWidth(), vBoxCommands.widthProperty()));

		// bind indicator width
		// hboxIndicator.prefWidthProperty().bind(Bindings.add(-1*hboxBottomPart.getPrefWidth()/2-hboxTraceNavigator.getPrefWidth()*2,
		// hboxBottomPart.widthProperty()));

		// holds info about percentage of states and actions
		mapStatePerc = new HashMap<Integer, Label>();
		mapActionPerc = new HashMap<String, List<Label>>();

		// init maps for next and previous fo the states
		mapNextNodes = new HashMap<Integer, List<StackPane>>();
		mapPreviousNodes = new HashMap<Integer, List<StackPane>>();
		statesNodes = new HashMap<Integer, StackPane>();
		statesOutgoingArrows = new HashMap<Integer, List<StackPane>>();
		statesIngoingArrows = new HashMap<Integer, List<StackPane>>();

		arrowsLines = new HashMap<StackPane, Line>();
		arrowsLabels = new HashMap<StackPane, StackPane>();

		// added traces ids
		addedTracesIDs = new LinkedList<Integer>();
		highLightedTracesIDs = new LinkedList<Integer>();

		// set top entities
		// set up spinner
		SpinnerValueFactory<Integer> valueFactory = //
				new SpinnerValueFactory.IntegerSpinnerValueFactory(minEntityNum, maxEntityNum, topK);

		spinnerTopK.setValueFactory(valueFactory);

		// add listener for when changed
		spinnerTopK.valueProperty().addListener(new ChangeListener<Integer>() {
			//
			// @Override
			public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
				// TODO Auto-generated method stub
				topK = newValue;

				if (miner != null && miner.isBigraphERFileSet()) {
					showEntities(null);
				}
			}
		});

		// set up the combobox for the added traces
		comboBoxAddedTraces.setOnAction(e -> {

			Integer selectedTraceID = comboBoxAddedTraces.getValue();
			
			if(selectedTraceID == null) {
				return;
			}
			
			clearHighlights();
			
			highlightTrace(selectedTraceID, HIGHLIGHT_STYLE, HIGHLIGHT_STYLE);
			highLightedTracesIDs.add(selectedTraceID);
			populateCell(selectedTraceID);
			// removeTrace(selectedTraceID);

		});
	}

	protected ContextMenu createNodeContextMenu(StackPane stateStack) {

		List<MenuItem> items = new LinkedList<MenuItem>();
		// int state = -1;

		for (String item : NODE_CONTEXT_MENU_ITEMS) {

			if (NODE_CONTEXT_MENU_IGNORE_ITEMS.contains(item)) {
				continue;
			}

			final MenuItem itm = new MenuItem(item);
			items.add(itm);

			itm.setOnAction(e -> {
				switch (itm.getText()) {
				// show next states
				case MENU_ITEM_SHOW_NEXT:
					// get state
					int state = getStateFromNode(stateStack);
					// show next states
					showNextStates(state);
					itm.setText(MENU_ITEM_HIDE_NEXT);
					break;

				case MENU_ITEM_HIDE_NEXT:
					int st = getStateFromNode(stateStack);
					removeNextStates(st);
					itm.setText(MENU_ITEM_SHOW_NEXT);
					break;

				// show previous
				case MENU_ITEM_SHOW_PREVIOUS:
					int stat = getStateFromNode(stateStack);
					showPreviousStates(stat);
					itm.setText(MENU_ITEM_HIDE_PREVIOUS);

					break;

				// hide previous:
				case MENU_ITEM_HIDE_PREVIOUS:
					int stt = getStateFromNode(stateStack);
					removePreviousStates(stt);
					itm.setText(MENU_ITEM_SHOW_PREVIOUS);
					break;

				// show other paths to the end trace:
				case MENU_ITEM_SHOW_OTHERS:
					int sttt = getStateFromNode(stateStack);
					int lastState = -1;
					if (trace != null) {
						lastState = trace.getStateTransitions().get(trace.getStateTransitions().size() - 1);
					}

					getAllTracesFromTo(sttt, lastState);

				default:
					break;
				}
			});

		}

		ContextMenu conMenu = new ContextMenu();
		conMenu.getItems().addAll(items);

		return conMenu;
	}

	@FXML
	void showEntities(ActionEvent e) {

		if (trace == null) {
			System.err.println("Trace is null");
			return;
		}

		if (miner == null) {
			System.err.println("Trace miner is null");
			return;
		}

		// this.trace = trace;

		if (!miner.isBigraphERFileSet()) {
			traceCell.selectBigraphERFile();
		}

		if (!miner.isBigraphERFileSet()) {
			return;
		}

		// get common entities
		List<GraphPath> traces = new LinkedList<GraphPath>();
		traces.add(trace);

		topK = spinnerTopK.getValue();

		topEntities = miner.findTopCommonEntities(traces, JSONTerms.BIG_IRRELEVANT_TERMS, topK);

		if (hboxEntities.getChildren().size() > 0) {
			hboxEntities.getChildren().clear();
		}

		StringBuilder bldrStyle = new StringBuilder();

		// add style to labels
		// font: 14, color: black, weight: bold
		bldrStyle.append("-fx-text-fill: black; -fx-font-size:14px; -fx-font-weight: bold;")
				// background
				.append("-fx-background-color: white;")
				// border
				.append("-fx-border-color: grey;");

		String style = bldrStyle.toString();

		// create labels for each entity
		List<Label> resLbls = new LinkedList<Label>();

		for (Map.Entry<String, Long> entry : topEntities) {
			Label lbl = new Label(" " + entry.getKey() + " <" + entry.getValue() + "> ");
			lbl.setStyle(style);
			resLbls.add(lbl);
		}

		// set selected value
		// comboBoxTopK.getSelectionModel().select(topK - 1);
		spinnerTopK.getValueFactory().setValue(topK);
		// add labels to hbox
		hboxEntities.getChildren().addAll(resLbls);

	}

	protected int getStateFromNode(StackPane node) {

		int state = -1;

		String stateStr = node.getId();

		try {
			state = Integer.parseInt(stateStr);
			// showNextStates(state);
		} catch (Exception exp) {

		}

		return state;
	}

	/**
	 * gets the states (start:index-0, end:index-1) from the given arrow
	 * 
	 * @param arrow
	 * @return
	 */
	protected List<Integer> getStatesFromArrow(StackPane arrow) {

		List<Integer> states = new LinkedList<Integer>();

		String stateStr = arrow.getId();

		try {

			String[] parts = stateStr.split(ARROW_ID_SEPARATOR);

			// first part is the start state, the 2nd is the end state
			int startState = Integer.parseInt(parts[0]);
			int endState = Integer.parseInt(parts[1]);

			states.add(startState);
			states.add(endState);

		} catch (Exception exp) {
			return null;
		}

		return states;
	}

	protected Integer getEndStateFromArrow(StackPane arrow) {

		List<Integer> states = getStatesFromArrow(arrow);

		if (states == null) {
			return -1;
		}

		if (states.size() > 1) {
			return states.get(1);
		}

		return -1;
	}

	protected Integer getStartStateFromArrow(StackPane arrow) {

		List<Integer> states = getStatesFromArrow(arrow);

		if (states == null) {
			return -1;
		}

		if (states.size() > 1) {
			return states.get(0);
		}

		return -1;
	}

	/**
	 * Gets the graphical node from the given state
	 * 
	 * @param state
	 * @return
	 */
	protected StackPane getNodeFromState(int state) {

		// check trace nodes
		for (StackPane stateNode : traceNodes) {
			int ndState = getStateFromNode(stateNode);

			if (state == ndState) {
				return stateNode;
			}
		}

		// check next states
		for (List<StackPane> nodes : mapNextNodes.values()) {
			for (StackPane stateNode : nodes) {
				int ndState = getStateFromNode(stateNode);

				if (state == ndState) {
					return stateNode;

				}
			}
		}

		// check previous states
		for (List<StackPane> nodes : mapPreviousNodes.values()) {
			for (StackPane stateNode : nodes) {
				int ndState = getStateFromNode(stateNode);

				if (state == ndState) {
					return stateNode;

				}
			}

		}

		return null;
	}

	@FXML
	void loadTransitionSystem(ActionEvent e) {

		// progressIndicator.setVisible(true);

		setIndicator(true, "Loading transition system");

		if (miner != null) {
			if (defualtTransitionSystemFilePath != null) {
				System.out.println("loading sys from " + defualtTransitionSystemFilePath.getPath());
				miner.setTransitionSystemFilePath(defualtTransitionSystemFilePath.getPath());
				miner.loadTransitionSystem();
				System.out.println("Done loading");
			} else {

			}

		}

		// progressIndicator.setVisible(false);
		setIndicator(false, "");

		toggleButtonActivity(btnLoadTransitionSystem, true);

	}

	@FXML
	void reset(ActionEvent e) {

		if (mapPreviousNodes != null) {
			mapPreviousNodes.clear();
			// previousNodes = null;
		}

		if (mapNextNodes != null) {
			mapNextNodes.clear();
			// mapNextNodes = null;
		}

		if (statesNodes != null) {
			statesNodes.clear();
		}

		if (statesOutgoingArrows != null) {
			statesOutgoingArrows.clear();
		}

		if (statesIngoingArrows != null) {
			statesIngoingArrows.clear();
		}

		if (highLightedTracesIDs != null) {
			highLightedTracesIDs.clear();
		}

		if (addedTracesIDs != null) {
			addedTracesIDs.clear();
		}

		comboBoxAddedTraces.getItems().clear();
		hboxTraceDetails.getChildren().clear();
		
		tracePane.getChildren().clear();

		// check if saved
		// used for prev/next trace showing
		if (trace != null && miner != null) {
			boolean isSaved = miner.isTraceSaved(trace.getInstanceID());
			if (isSaved) {
				toggleButtonActivity(btnSaveTrace, true);
			} else {
				toggleButtonActivity(btnSaveTrace, false);
			}
		}

		// reset entities
		hboxEntities.getChildren().clear();

		showTrace(trace);
	}

	/**
	 * Checks whether the given two states have an arrow drawn between them in
	 * the view
	 * 
	 * @param startState
	 * @param endState
	 * @return
	 */
	protected boolean areConnected(int startState, int endState) {

		// checks if there's an arrow drawn between the given two states
		if (statesOutgoingArrows.containsKey(startState)) {

			// check each arrow for the start state
			for (StackPane arw : statesOutgoingArrows.get(startState)) {
				List<Integer> arrowEnds = getStatesFromArrow(arw);

				if (arrowEnds != null && arrowEnds.size() > 1) {
					int arwEndState = arrowEnds.get(1);

					if (endState == arwEndState) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Shows the next states of the given state
	 * 
	 * @param state
	 */
	void showNextStates(int state) {

		if (miner == null) {
			System.err.println("Trace miner is NULL");
		}

		// shows previous states in the system
		if (miner.getTransitionSystem() == null) {
			loadTransitionSystem(null);
		}

		if (miner.getTransitionSystem() == null) {
			return;
		}

		if (trace == null) {
			return;
		}

		StackPane stateNode = null;

		stateNode = getNodeFromState(state);

		if (stateNode == null) {
			System.err.println("Couldn't find state " + state);
			return;
		}

		Digraph<Integer> digraph = miner.getTransitionSystem().getDigraph();

		// System.out.println(digraph);
		// get in bound states (going to initial state)
		List<Integer> outBoundStates = digraph.outboundNeighbors(state);

		List<StackPane> nextNodes = new LinkedList<StackPane>();

		// create nodes for each inbound
		for (Integer outBoundState : outBoundStates) {

			StackPane node = null;
			/// if the inbound state is already drawn then create an arrow to
			/// the given state
			if (statesNodes.containsKey(outBoundState)) {
				node = statesNodes.get(outBoundState);

				// check if arrow is alread drawn
				if (!areConnected(state, outBoundState)) {
					String actionName = digraph.getLabel(state, outBoundState);

					buildSingleDirectionalLine(stateNode, node, tracePane, true, false, ADDED_NODES_ARROW_COLOUR,
							actionName);
				}

			} else {
				// a new node is created with the arrow
				node = getDot(NODE_COLOUR, "" + outBoundState, EXTRA_STATE_STYLE, NODE_RADIUS);
				node.setId("" + outBoundState);
				nextNodes.add(node);
				// statesNodes.put(outBoundState, node);
				String actionName = digraph.getLabel(state, outBoundState);

				buildSingleDirectionalLine(stateNode, node, tracePane, true, false, ADDED_NODES_ARROW_COLOUR,
						actionName);
			}

		}

		mapNextNodes.put(state, nextNodes);

		// remove and re-add nodes
		setNodes();

		// position new nodes
		double xOffest = NODE_RADIUS * 2 + 50;
		double yOffset = NODE_RADIUS * 2;

		locatedNodesRelativeTo(stateNode, nextNodes, xOffest, yOffset);

	}

	void removeNextStates(int state) {

		// remove all arrows and states that goes out of the given state

		if (!statesOutgoingArrows.containsKey(state)) {
			return;
		}

		// remove next states
		List<StackPane> nextStates = mapNextNodes.get(state);

		// remove all next next
		for (StackPane node : nextStates) {
			int endState = getStateFromNode(node);

			// if (statesArrows.containsKey(endState)) {
			removeNextStates(endState);

			System.out.println("removing pre: " + endState);
			removePreviousStates(endState);
			// }
		}

		// remove arrows
		List<StackPane> nextArrows = statesOutgoingArrows.get(state);

		List<StackPane> arrowsToRemove = new LinkedList<StackPane>();

		for (StackPane arw : nextArrows) {

			// get end states
			int endState = getEndStateFromArrow(arw);

			if (endState != -1) {

				// //if the next state is part of the original trace then ignore
				if (trace.getStateTransitions().contains(endState)) {
					continue;

				}

				// remove node
				statesNodes.remove(endState);

				// remove line
				if (arrowsLines.containsKey(arw)) {
					tracePane.getChildren().remove(arrowsLines.get(arw));
					arrowsLines.remove(arw);

				}

				// remove labels
				if (arrowsLabels.containsKey(arw)) {
					tracePane.getChildren().remove(arrowsLabels.get(arw));
					arrowsLabels.remove(arw);

				}

				// to remove arrow head
				arrowsToRemove.add(arw);

			}

		}

		// remove arrow head
		statesOutgoingArrows.get(state).removeAll(arrowsToRemove);
		tracePane.getChildren().removeAll(arrowsToRemove);

		tracePane.getChildren().removeAll(nextStates);

		mapNextNodes.remove(state);

	}

	/**
	 * Shows the previous states of the given state
	 * 
	 * @param state
	 */
	void showPreviousStates(int state) {

		if (miner == null) {
			System.err.println("Trace miner is NULL");
		}

		// shows previous states in the system
		if (miner.getTransitionSystem() == null) {
			loadTransitionSystem(null);
		}

		if (miner.getTransitionSystem() == null) {
			return;
		}

		if (trace == null) {
			return;
		}

		StackPane stateNode = getNodeFromState(state);

		if (stateNode == null) {
			System.err.println("Couldn't find state " + state);
			return;
		}

		Digraph<Integer> digraph = miner.getTransitionSystem().getDigraph();

		// System.out.println(digraph);
		// get in bound states (going to initial state)
		List<Integer> inBoundStates = digraph.inboundNeighbors(state);

		List<StackPane> previousNodes = new LinkedList<StackPane>();

		// create nodes for each inbound
		for (Integer inBoundState : inBoundStates) {

			StackPane node = null;

			/// if the inbound state is already drawn then create an arrow to
			/// the given state
			if (statesNodes.containsKey(inBoundState)) {
				node = statesNodes.get(inBoundState);

				// check if arrow is alread drawn
				if (!areConnected(inBoundState, state)) {
					String actionName = digraph.getLabel(inBoundState, state);

					buildSingleDirectionalLine(node, stateNode, tracePane, true, false, ADDED_NODES_ARROW_COLOUR,
							actionName);
				}

			} else {
				// a new node is created with the arrow
				node = getDot(NODE_COLOUR, "" + inBoundState, EXTRA_STATE_STYLE, NODE_RADIUS);
				node.setId("" + inBoundState);
				previousNodes.add(node);
				// statesNodes.put(outBoundState, node);
				String actionName = digraph.getLabel(inBoundState, state);

				buildSingleDirectionalLine(node, stateNode, tracePane, true, false, ADDED_NODES_ARROW_COLOUR,
						actionName);
			}

		}

		mapPreviousNodes.put(state, previousNodes);

		// remove and re-add nodes
		setNodes();

		double xOffset = -60;
		double yOffset = NODE_RADIUS * 2;

		// locate new nodes in the view
		locatedNodesRelativeTo(stateNode, previousNodes, xOffset, yOffset);

	}

	void removePreviousStates(int state) {

		// remove all arrows and states that goes in the given state

		// remove next states
		List<StackPane> preStates = mapPreviousNodes.get(state);

		// remove previous previous

		Digraph<Integer> graph = miner.getTransitionSystem() != null ? miner.getTransitionSystem().getDigraph() : null;

		if (graph == null) {
			return;
		}

		List<Integer> inBoundStates = graph.inboundNeighbors(state);

		List<StackPane> arrowsToRemove = new LinkedList<StackPane>();

		for (Integer preState : inBoundStates) {

			// check the previous previous
			for (Integer prePreState : graph.inboundNeighbors(preState)) {
				if (statesOutgoingArrows.containsKey(prePreState)
						&& !trace.getStateTransitions().contains(prePreState)) {
					removePreviousStates(preState);
				}
			}

			List<StackPane> preArws = statesOutgoingArrows.get(preState);

			if (preArws == null) {
				return;
			}

			for (StackPane arw : preArws) {
				int endState = getEndStateFromArrow(arw);

				// remove state and arrow
				if (endState == state && !trace.getStateTransitions().contains(preState)) {

					// remove node
					statesNodes.remove(preState);

					// remove line
					if (arrowsLines.containsKey(arw)) {
						tracePane.getChildren().remove(arrowsLines.get(arw));
						arrowsLines.remove(arw);

					}

					// remove labels
					if (arrowsLabels.containsKey(arw)) {
						tracePane.getChildren().remove(arrowsLabels.get(arw));
						arrowsLabels.remove(arw);

					}

					arrowsToRemove.add(arw);
				}
			}

			// remove arrow head
			statesOutgoingArrows.get(preState).removeAll(arrowsToRemove);
			tracePane.getChildren().removeAll(arrowsToRemove);

			arrowsToRemove.clear();

		}

		if (preStates != null) {
			tracePane.getChildren().removeAll(preStates);
		}

		mapPreviousNodes.remove(state);
	}

	protected boolean isNodeShown(int state) {

		// check trace if the node exist

		if (statesNodes.containsKey(state)) {
			// already done
			return true;
		}

		return false;
	}

	protected void locatedNodesRelativeTo(StackPane mainNode, List<StackPane> nodesToLocate, double xOffest,
			double yOffset) {

		// int xOffest = 60;
		double posX = mainNode.getLayoutX() + xOffest;

		if (posX < 0) {
			posX = NODE_RADIUS;
		}

		checkForHorizontalScrolling(posX);

		// int yOffest = (int) (NODE_RADIUS * 2);
		double posY = mainNode.getLayoutY() + yOffset + (NODE_RADIUS * 3);

		for (StackPane node : nodesToLocate) {
			node.setLayoutX(posX);
			node.setLayoutY(posY);

			// same place
			// posX+=xOffest;

			posY += yOffset;

			if (posY < 0) {
				posY = NODE_RADIUS;
			}

			checkForVerticalScrolling(posY);
		}

	}

	protected void checkForVerticalScrolling(double posY) {

		// check if the given posY goes beyond the height of the main stack
		if (posY > mainStackPane.getPrefHeight()) {

			mainStackPane.prefHeightProperty().unbind();

			double newHeight = posY + NODE_RADIUS * 2;
			mainStackPane.setPrefHeight(newHeight);
			// scrollPaneTraceViewer.setVvalue(newHeight);
		}

		// mainStackPane.prefHeightProperty().bind(Bindings.add(-5,
		// scrollPaneTraceViewer.heightProperty()));
	}

	protected void checkForHorizontalScrolling(double posX) {

		// check if the given posY goes beyond the height of the main stack
		System.out.println("posX: " + posX + "\nstack width: " + mainStackPane.getPrefWidth() + "\nminWidth: "
				+ mainStackPane.getMinWidth());
		if (posX > mainStackPane.getPrefWidth()) {

			mainStackPane.prefWidthProperty().unbind();
			mainStackPane.setPrefWidth(posX + (NODE_RADIUS * 4));
		} else if (posX < 0) {
			System.out.println("posx: " + posX + "\nminWidth: " + mainStackPane.getMinWidth());
			mainStackPane.prefWidthProperty().unbind();
			mainStackPane.setLayoutX(posX - NODE_RADIUS * 2);
			// mainStackPane.setPrefWidth(posX + (NODE_RADIUS * 4));
		}

	}

	@FXML
	void showStatesPercentages(ActionEvent e) {

		if (miner == null || trace == null) {
			return;
		}

		// miner.getStateOccurrence(traces);

		int numOfTraces = miner.getNumberOfTraces();

		// show states perc
		for (Entry<Integer, Label> entry : mapStatePerc.entrySet()) {
			Label lbl = entry.getValue();
			double perc = miner.getStatePercentage(entry.getKey(), TraceMiner.ALL);
			int stateOccur = miner.getStateOccurrence(entry.getKey(), TraceMiner.ALL);

			if (perc == -1) {
				lbl.setText(NOT_FOUND);
				lbl.setTooltip(new Tooltip("State does not occur in any trace"));
				continue;
			}

			int precision = 1000;
			int percn = precision / 100;

			// conver to a DD.D%
			double percDbl = ((int) (Math.round(perc * precision))) * 1.0 / percn;

			// set label
			lbl.setText(percDbl + "%");
			lbl.setTooltip(new Tooltip("Occurrence: " + stateOccur + "/" + numOfTraces));
		}

		// show actions perc
		for (Entry<String, List<Label>> entry : mapActionPerc.entrySet()) {

			String actionName = entry.getKey();
			List<Label> lbls = entry.getValue();

			double perc = miner.getActionOccurrencePercentage(actionName, TraceMiner.ALL);
			int actionOccur = miner.getActionOccurrence(actionName, TraceMiner.ALL);

			if (perc == -1) {
				for (Label lbl : lbls) {
					lbl.setText(NOT_FOUND);
					lbl.setTooltip(new Tooltip("Action does not occur in any trace"));
				}

				continue;
			}

			int precision = 1000;
			int percn = precision / 100;

			// conver to a DD.D%
			double percDbl = ((int) (Math.round(perc * precision))) * 1.0 / percn;

			// set label
			for (Label lbl : lbls) {
				lbl.setText(percDbl + "%");
				lbl.setTooltip(new Tooltip("Occurrence: " + actionOccur + "/" + numOfTraces));
			}
		}

	}

	@FXML
	void showPreviousTrace(ActionEvent e) {

		if (miner == null) {
			return;
		}

		// get trace
		List<Integer> currentShownTraces = miner.getCurrentShownTraces();

		if (currentShownTraces == null) {
			return;
		}

		// update value
		currentNumberOfShownTraces = currentShownTraces.size();

		int currentTraceIndex = currentShownTraces.indexOf(trace.getInstanceID());

		if (currentTraceIndex > 0) {
			int prevTraceIndex = currentShownTraces.get(currentTraceIndex - 1);
			GraphPath prevTrace = miner.getTrace(prevTraceIndex);

			if (prevTrace != null) {
				trace = prevTrace;
				reset(null);
			}
		}
	}

	@FXML
	void showNextTrace(ActionEvent e) {

		if (miner == null) {
			return;
		}

		// get trace
		List<Integer> currentShownTraces = miner.getCurrentShownTraces();

		if (currentShownTraces == null) {
			return;
		}

		// update value
		currentNumberOfShownTraces = currentShownTraces.size();

		int currentTraceIndex = currentShownTraces.indexOf(trace.getInstanceID());

		if (currentTraceIndex < currentNumberOfShownTraces - 1) {
			int nextTraceIndex = currentShownTraces.get(currentTraceIndex + 1);
			GraphPath nextTrace = miner.getTrace(nextTraceIndex);

			if (nextTrace != null) {
				trace = nextTrace;
				reset(null);
			}
		}
	}

	@FXML
	void saveTrace(ActionEvent e) {

		if (traceCell == null) {
			return;
		}

		String path = traceCell.saveTrace(trace);

		if (path != null) {
			toggleButtonActivity(btnSaveTrace, true);
		}

	}

	protected void setNodes() {

		tracePane.getChildren().removeAll(statesNodes.values());
		tracePane.getChildren().addAll(statesNodes.values());

	}

	public void showTrace(GraphPath trace) {

		if (trace == null) {
			System.err.println("Trace is NULL");
			return;
		}

		// tracePane.getChildren().clear();

		this.trace = trace;

		traceNodes = createTraceNodes(trace);

		if (traceNodes == null) {
			return;
		}

		tracePane.getChildren().addAll(traceNodes);

		// set arrow lines style
		highlightTrace(trace.getInstanceID(), NORMAL_HIGHLIGHT_STYLE, TRACE_ARROW_HIGHLIGHT_STYLE);
		// position each node
		double posX = 250;
		double posY = 150;

		for (StackPane node : traceNodes) {

			node.setLayoutX(posX);
			node.setLayoutY(posY);

			// update x axis to be the circle plus some gap
			posX += NODE_RADIUS * 2 + 100;
			posY += NODE_RADIUS * 2 + 20;

		}

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				int traceIndex = miner.getCurrentShownTraces().indexOf(trace.getInstanceID()) + 1;
				txtFieldCurrentShownTrace.setText(traceIndex + "");
				txtFieldCurrentShownTrace.setTooltip(new Tooltip("Showing trace with ID: " + trace.getInstanceID()));

				// change window title
				if (currentStage == null) {
					Window wind = txtFieldCurrentShownTrace.getScene().getWindow();

					if (wind instanceof Stage) {
						currentStage = (Stage) wind;
					}
				} else {
					currentStage.setTitle("Trace " + trace.getInstanceID());
				}

			}
		});
		
		if(!addedTracesIDs.contains(trace.getInstanceID())) {
			addedTracesIDs.add(trace.getInstanceID());
			updateAddedTracesIDsComboBox(addedTracesIDs);
		}

	}

	protected void updateAddedTracesIDsComboBox(List<Integer> tracesIDs) {
	
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				ObservableList<Integer> list = FXCollections.observableArrayList(tracesIDs);
				comboBoxAddedTraces.setItems(list);
			}
		});
	}
	
	/**
	 * adds all partial traces between from and end states
	 * 
	 * @param fromState
	 *            start state
	 * @param endState
	 *            end state
	 */
	protected void getAllTracesFromTo(int fromState, int endState) {

		List<Integer> states = new LinkedList<Integer>();
		states.add(fromState);
		states.add(endState);

		// get ids of all traces that contain the two given states
		List<Integer> tracesIDs = miner.findTracesContainingStates(states, miner.getCurrentShownTraces());

		// if(tracesIDs.isEmpty()) {
		// System.out.println("No traces identified");
		// } else {
		// System.out.println("traces identified: " + tracesIDs);
		// }

		Map<Integer, GraphPath> traces = miner.getTraces(tracesIDs);

		// filter to get the last state to be the end state
		for (GraphPath trace : traces.values()) {
			LinkedList<Integer> traceStates = trace.getStateTransitions();

			if (traceStates != null && traceStates.getLast() == endState) {
				// a trace is identified then add
				// System.out.println("traces identified: " + tracesIDs);
				addTrace(trace);
			}
		}

	}

	/**
	 * adds the given trace to the trace view. IF new states and actions are
	 * added then shown, if already exist then not changed
	 * 
	 * @param newTrace
	 */
	protected void addTrace(GraphPath newTrace) {

		List<StackPane> statesNodes = createTraceNodes(newTrace);

		if (statesNodes == null) {
			return;
		}

		double xOffest = NODE_RADIUS * 2 + 50;
		double yOffest = NODE_RADIUS * 2 + 50;

		List<Node> nodes = tracePane.getChildren();
		// add new nodes to the trace pane
		for (StackPane stateNode : statesNodes) {

			if (!nodes.contains(stateNode)) {
				nodes.add(stateNode);
				stateNode.setLayoutX(xOffest);
				stateNode.setLayoutX(yOffest);
			} else {
				xOffest += stateNode.getLayoutX();
				yOffest += stateNode.getLayoutY();
			}

		}

		// used to put nodes on top
		setNodes();

		// add trace id
		if (!addedTracesIDs.contains(newTrace.getInstanceID())) {
			addedTracesIDs.add(newTrace.getInstanceID());
			updateAddedTracesIDsComboBox(addedTracesIDs);
		}

//		// update combobox
//		Platform.runLater(new Runnable() {
//
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				ObservableList<Integer> items = FXCollections.observableArrayList(addedTracesIDs);
//
//				comboBoxAddedTraces.setItems(items);
//			}
//		});

	}

	protected void showAddedTrace(int traceID) {
		// shows only the given trace and the original trace

		// reset(null);

	}

	protected void removeTrace(int traceID) {

		// removes the given trace

		if (miner == null) {
			return;
		}

		GraphPath trace = miner.getTrace(traceID);

		if (trace == null) {
			return;
		}

		List<Integer> states = trace.getStateTransitions();
		// List<String> actions = trace.getTransitionActions();

		int index = 1;
		for (Integer state : states) {

			if (index <= states.size() - 1) {
				StackPane node = statesNodes.get(state);

				if (node != null) {
					List<StackPane> arrowHeads = statesOutgoingArrows.get(state);

					// find the arrow to the next state
					if (arrowHeads != null) {
						for (StackPane arrowHead : arrowHeads) {
							int endState = getEndStateFromArrow(arrowHead);
							if (endState == states.get(index)) {
								removeArrow(state, arrowHead);
								break;
							}

						}
					}

				}
			}

		}

		// remove states if they have no outgoing arrows

		// to be done
	}

	protected void removeState(int state) {

		// removes the given state and any associated arrows in or out

		// if the node does not exist then no need to go for the rest
		if (!statesNodes.containsKey(state)) {
			return;
		}

		// if state is one of the original then keep it
		if (trace != null && trace.getStateTransitions().contains(state)) {
			return;
		}

		// remove any arrows

		// remove next arrows
		// List<StackPane> arrowOutgoingHeads = statesOutgoingArrows.get(state);
		// List<StackPane> arrowIngoingHeads = statesIngoingArrows.get(state);
		//
		// // if(arrowOutgoingHeads.size()> 1 && arrowIngoingHeads.size() > 1) {
		// // return;
		// // }
		//
		// List<Node> nodesToRemove = new LinkedList<Node>();
		//
		// // if it is not null and there's only one outgoing arrow then delete
		// if (arrowOutgoingHeads != null) {
		// for (StackPane arrowHead : arrowOutgoingHeads) {
		// nodesToRemove.add(arrowHead);
		//
		// // get line
		// Line arrowLine = arrowsLines.get(arrowHead);
		// if (arrowLine != null) {
		// arrowsLines.remove(arrowHead);
		// nodesToRemove.add(arrowLine);
		// }
		//
		// // get label
		// StackPane arrowLabel = arrowsLabels.get(arrowHead);
		//
		// if (arrowLabel != null) {
		// arrowsLabels.remove(arrowLabel);
		// nodesToRemove.add(arrowLabel);
		// }
		//
		// }
		//
		// // remove arrows heads
		// statesOutgoingArrows.remove(state);
		// }
		//
		// // remove previous
		// if (arrowIngoingHeads != null) {
		// for (StackPane arrowHead : arrowIngoingHeads) {
		//
		// // if it is already visited then continue
		// if (nodesToRemove.contains(arrowHead)) {
		// continue;
		// }
		//
		// nodesToRemove.add(arrowHead);
		//
		// // get line
		// Line arrowLine = arrowsLines.get(arrowHead);
		// if (arrowLine != null) {
		// arrowsLines.remove(arrowHead);
		// nodesToRemove.add(arrowLine);
		// }
		//
		// // get label
		// StackPane arrowLabel = arrowsLabels.get(arrowHead);
		//
		// if (arrowLabel != null) {
		// arrowsLabels.remove(arrowLabel);
		// nodesToRemove.add(arrowLabel);
		// }
		//
		// }
		//
		// // remove arrows heads
		// statesIngoingArrows.remove(state);
		// }

		// remove from pane
		// remove state
		if (statesNodes.containsKey(state)) {
			tracePane.getChildren().remove(statesNodes.get(state));
			statesNodes.remove(state);
		}

		// remove arrows from pane
		// tracePane.getChildren().removeAll(nodesToRemove);
	}

	protected void removeArrow(int startState, StackPane arrow) {

		// get line
		Line arrowLine = arrowsLines.get(arrow);
		List<Node> nodesToRemove = new LinkedList<Node>();

		if (arrowLine != null) {
			arrowsLines.remove(arrow);
			nodesToRemove.add(arrowLine);
		}

		// get label
		StackPane arrowLabel = arrowsLabels.get(arrow);

		if (arrowLabel != null) {
			arrowsLabels.remove(arrowLabel);
			nodesToRemove.add(arrowLabel);
		}

		// remove arrows heads
		List<StackPane> arrows = statesOutgoingArrows.get(startState);

		if (arrows != null) {
			arrows.remove(arrow);
		}

	}

	protected void highlightTrace(int traceID, String nodeHighLightStyle, String arrowHighLightStyle) {

		// removes the given trace

		if (miner == null) {
			return;
		}

		GraphPath trace = miner.getTrace(traceID);

		if (trace == null) {
			return;
		}

		List<Integer> states = trace.getStateTransitions();
		// List<String> actions = trace.getTransitionActions();

		int index = 1;

		for (Integer state : states) {
			// removeState(state);
			highlightState(state, nodeHighLightStyle);

			// highlight arrow to next state
			if (index <= states.size() - 1) {
				StackPane node = statesNodes.get(state);

				if (node != null) {
					List<StackPane> arrowHeads = statesOutgoingArrows.get(state);

					// find the arrow to the next state
					if (arrowHeads != null) {
						for (StackPane arrowHead : arrowHeads) {
							int endState = getEndStateFromArrow(arrowHead);
							if (endState == states.get(index)) {
								//highlight arrow
								highlightArrow(arrowHead, arrowHighLightStyle);
							}

						}
					}

				}
			}

			index++;
		}

		// highLightedTracesIDs.add(traceID);
		// remove states

	}

	protected void highlightState(int state, String highLightStyle) {

		StackPane stateNode = statesNodes.get(state);

		if (stateNode == null) {
			return;
		}

		stateNode.setStyle(highLightStyle);
	}

	protected void highlightArrow(StackPane arrowHead, String highLightStyle) {

		Line arrowLine = arrowsLines.get(arrowHead);
		StackPane label = arrowsLabels.get(arrowHead);
		
		if (arrowLine == null) {
			return;
		}

		List<Integer> states = getStatesFromArrow(arrowHead);

		if (states != null) {

			//set line style
			if (trace != null && trace.getStateTransitions().containsAll(states)
					&& highLightStyle.equals(NORMAL_HIGHLIGHT_STYLE)) {
				arrowLine.setStyle(TRACE_ARROW_HIGHLIGHT_STYLE);

			} else {
				arrowLine.setStyle(highLightStyle);
			}
			
			//set label style
			if(label!=null) {
				if(!highLightStyle.contains("-fx-background-color")) {
					
					label.setStyle(highLightStyle+";-fx-background-color:white;");	
				}
				
			}

		}

		
		
		 arrowHead.setOpacity(1);
		 
	}

	protected void clearHighlights() {
		// remove highlight from other higlighted traces
		if (!highLightedTracesIDs.isEmpty()) {

			for (Integer highlightedTraceID : highLightedTracesIDs) {
				highlightTrace(highlightedTraceID, ARROW_NORMAL_HIGHLIGHT_STYLE, ARROW_NORMAL_HIGHLIGHT_STYLE);
			}

			highLightedTracesIDs.clear();
//			setOpacityForAll(0.4);
		}
	}

	protected void setOpacityForAll(double opacity) {
		
		
		//set opacity for nodes
		for(StackPane node:  statesNodes.values()) {
			node.setOpacity(opacity);
		}
		
		//set opacity for arrows
		for(List<StackPane> arrows: statesOutgoingArrows.values()){
			for(StackPane arw:  arrows) {
				//set for arrow head
				arw.setOpacity(opacity);
				
				//set for line
				Line line = arrowsLines.get(arw);
				if(line!=null) {
					line.setOpacity(opacity);
				}
				
				//set for label
				StackPane label = arrowsLabels.get(arw);
				if(label!=null) {
					label.setOpacity(opacity);
				}
				
			}
		}
	}
	/**
	 * add a partial-trace for the given trace states
	 * 
	 * @param traceState
	 */
	protected void addTrace(List<Integer> traceState) {

		List<StackPane> statesNodes = createTraceNodes(traceState);

		if (statesNodes == null) {
			return;
		}

		double xOffest = NODE_RADIUS * 2 + 50;
		double yOffest = NODE_RADIUS * 2 + 50;

		List<Node> nodes = tracePane.getChildren();
		// add new nodes to the trace pane
		for (StackPane stateNode : statesNodes) {

			if (!nodes.contains(stateNode)) {
				nodes.add(stateNode);
				stateNode.setLayoutX(xOffest);
				stateNode.setLayoutX(yOffest);
			} else {
				xOffest += stateNode.getLayoutX();
				yOffest += stateNode.getLayoutY();
			}

		}

		// used to put nodes on top
		setNodes();

	}

	public List<StackPane> createTraceNodes(GraphPath trace) {

		if (trace == null) {
			return null;
		}

		List<String> actions = trace.getTransitionActions();
		List<Integer> states = trace.getStateTransitions();

		List<StackPane> stateNodes = new LinkedList<StackPane>();

		// create nodes and edges
		int index = 0;
		// int count = 2;
		for (Integer state : states) {
			StackPane node = null;

			if (statesNodes.containsKey(state)) {
				node = statesNodes.get(state);

				if (index > 0) {

					int previousState = states.get(index - 1);

					// if they don't already have a connection (i.e. arrow)
					// drawn
					if (!areConnected(previousState, state)) {
						buildSingleDirectionalLine(stateNodes.get(index - 1), node, tracePane, true, false,
								ADDED_NODES_ARROW_COLOUR, actions.get(index - 1));
					}
				}

			} else {
				node = getDot(NODE_COLOUR, "" + state, STATE_STYLE, NODE_RADIUS);
				// a new node is created with the arrow
				node = getDot(NODE_COLOUR, "" + state, EXTRA_STATE_STYLE, NODE_RADIUS);

				node.setId("" + state);

				if (index > 0) {
					buildSingleDirectionalLine(stateNodes.get(index - 1), node, tracePane, true, false,
							ADDED_NODES_ARROW_COLOUR, actions.get(index - 1));
				}

			}

			stateNodes.add(node);

			index++;
		}

		return stateNodes;
	}

	public List<StackPane> createTraceNodes(List<Integer> traceStates) {

		if (trace == null) {
			return null;
		}

		if (miner == null) {
			return null;
		}

		if (miner.getTransitionSystem() == null) {
			loadTransitionSystem(null);
		}

		if (miner.getTransitionSystem() == null) {
			return null;
		}

		Digraph<Integer> graph = miner.getTransitionSystem().getDigraph();

		if (graph == null) {
			return null;
		}

		// List<String> actions = new LinkedList<String>();
		// List<Integer> states = trace.getStateTransitions();

		List<StackPane> stateNodes = new LinkedList<StackPane>();

		// create nodes and edges
		int index = 0;
		// int count = 2;
		for (Integer state : traceStates) {
			StackPane node = null;

			if (statesNodes.containsKey(state)) {
				node = statesNodes.get(state);

				if (index > 0) {

					int previousState = traceStates.get(index - 1);

					String actionName = graph.getLabel(previousState, state);
					// if they don't already have a connection (i.e. arrow)
					// drawn
					if (!areConnected(previousState, state)) {
						buildSingleDirectionalLine(stateNodes.get(index - 1), node, tracePane, true, false,
								ADDED_NODES_ARROW_COLOUR, actionName);
					}
				}

			} else {
				node = getDot(NODE_COLOUR, "" + state, STATE_STYLE, NODE_RADIUS);
				// a new node is created with the arrow
				node = getDot(NODE_COLOUR, "" + state, EXTRA_STATE_STYLE, NODE_RADIUS);

				node.setId("" + state);

				if (index > 0) {
					int previousState = traceStates.get(index - 1);
					String actionName = graph.getLabel(previousState, state);
					buildSingleDirectionalLine(stateNodes.get(index - 1), node, tracePane, true, false,
							TRACE_ARROW_COLOUR, actionName);
				}

			}

			stateNodes.add(node);

			index++;
		}

		return stateNodes;
	}

	public void setTraceMiner(TraceMiner traceMiner, GraphPath trace) {
		miner = traceMiner;
		this.trace = trace;

		if (miner != null) {
			// set transition system button
			if (miner.getTransitionSystem() != null) {
				toggleButtonActivity(btnLoadTransitionSystem, true);
			}

			// set current list of traces
			currentNumberOfShownTraces = miner.getCurrentShownTraces().size();

			lblNumberOfShownTraces.setText("/" + currentNumberOfShownTraces);

			// set save button
			if (trace != null) {
				boolean isSaved = miner.isTraceSaved(trace.getInstanceID());

				if (isSaved) {
					toggleButtonActivity(btnSaveTrace, true);
				}
			}

		}
	}

	protected void toggleButtonActivity(Button btn, boolean isDisabled) {
		if (btn == null) {
			return;
		}

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				btn.setDisable(isDisabled);
			}
		});
	}

	public TraceMiner getTraceMiner() {
		return miner;
	}

	public void setTaskCell(TaskCell traceCell) {
		this.traceCell = traceCell;
	}

	protected void setIndicator(boolean showIndicator, String msg) {

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				progressIndicator.setVisible(showIndicator);
				lblProgressIndicator.setText(msg);
			}
		});

	}

	protected void populateCell(int traceID) {

		hboxTraceDetails.getChildren().clear();
		if (trace == null) {
			return;
		}

		if(miner == null) {
			return;
		}
		
		GraphPath trace = miner.getTrace(traceID);

		int index = 0;
		int size = trace.getStateTransitions().size() - 1;
		List<Integer> states = trace.getStateTransitions();
		List<String> actions = trace.getTransitionActions();
		StringBuilder strBldr = new StringBuilder();

		for (Integer state : states) {
			// Circle circle = new Circle(hbox.getHeight()-2);
			Label lblState;
			Label lblAction;
			if (index != size) {

				lblState = new Label(state + "");

				strBldr.append(state);

				String act = actions.get(index);
				lblAction = new Label(" =[" + act + "]=> ");

				strBldr.append(" =[" + actions.get(index) + "]=> ");
			} else {
				lblState = new Label(state + "");
				strBldr.append(state);
				lblAction = null;
			}

			index++;

			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					hboxTraceDetails.getChildren().add(lblState);
					if (lblAction != null) {
						hboxTraceDetails.getChildren().add(lblAction);
					}

				}
			});

		}
	}
	
	/**
	 * Builds a pane consisting of circle with the provided specifications.
	 *
	 * @param color
	 *            Color of the circle
	 * @param text
	 *            Text inside the circle
	 * @return Draggable pane consisting a circle.
	 */
	private StackPane getDot(String color, String state, String stateLabelStyle, double radius) {
		// double radius = 50;
		double paneSize = 2 * radius;
		StackPane dotPane = new StackPane();
		Circle dot = new Circle();
		dot.setRadius(radius);
		dot.setStyle("-fx-fill:" + color + ";-fx-stroke-width:2px;-fx-stroke:black;");

		// state label
		Label lblState = new Label(state);
		lblState.setStyle(stateLabelStyle);
		lblState.setTooltip(new Tooltip(state));

		// open state if it exists
		lblState.setOnMouseEntered(e -> {
			lblState.setCursor(Cursor.HAND);
		});

		lblState.setOnMouseClicked(e -> {
			try {
				if (!isDragging) {
					int stat = Integer.parseInt(state);
					if (traceCell != null && trace != null) {
						traceCell.showState(stat);
					}
				}

			} catch (NumberFormatException excp) {
				// txt is not state
				// nothing happens
			}

		});

		lblState.setOnMouseDragged(e -> {
			isDragging = true;
		});

		lblState.setOnMousePressed(e -> {
			isDragging = false;
		});

		// state percentage
		Label lblStatePerc = new Label();
		lblStatePerc.setStyle(STATE_PERC_STYLE);

		try {
			int stat = Integer.parseInt(state);
			// add to the map
			mapStatePerc.put(stat, lblStatePerc);
		} catch (NumberFormatException excp) {
			// txt is not state
			// nothing happens
		}

		Pane p = new Pane();
		p.setPrefSize(3, 45);

		VBox vboxLbl = new VBox();

		vboxLbl.getChildren().add(p);
		vboxLbl.getChildren().add(lblStatePerc);
		vboxLbl.getChildren().add(lblState);
		vboxLbl.setAlignment(Pos.CENTER);

		dotPane.getChildren().addAll(dot, vboxLbl, lblState);

		dotPane.setPrefSize(paneSize, paneSize);
		dotPane.setMaxSize(paneSize, paneSize);
		dotPane.setMinSize(paneSize, paneSize);

		dotPane.setOnMousePressed(e -> {
			sceneX = e.getSceneX();
			sceneY = e.getSceneY();
			layoutX = dotPane.getLayoutX();
			layoutY = dotPane.getLayoutY();
		});

		ContextMenu nodeContextMenu = createNodeContextMenu(dotPane);

		// set context menu for the node
		dotPane.setOnContextMenuRequested(e -> {
			nodeContextMenu.setY(e.getScreenY());
			nodeContextMenu.setX(e.getScreenX());
			nodeContextMenu.show(dotPane.getScene().getWindow());
		});

		EventHandler<MouseEvent> dotOnMouseDraggedEventHandler = e -> {
			// Offset of drag
			double offsetX = e.getSceneX() - sceneX;
			double offsetY = e.getSceneY() - sceneY;

			// Taking parent bounds
			Bounds parentBounds = dotPane.getParent().getLayoutBounds();

			// Drag node bounds
			double currPaneLayoutX = dotPane.getLayoutX();
			double currPaneWidth = dotPane.getWidth();
			double currPaneLayoutY = dotPane.getLayoutY();
			double currPaneHeight = dotPane.getHeight();

			if ((currPaneLayoutX + offsetX < parentBounds.getWidth() - currPaneWidth)
					&& (currPaneLayoutX + offsetX > -1)) {
				// If the dragNode bounds is within the parent bounds, then you
				// can set the offset value.
				dotPane.setTranslateX(offsetX);
			} else if (currPaneLayoutX + offsetX < 0) {
				// If the sum of your offset and current layout position is
				// negative, then you ALWAYS update your translate to negative
				// layout value
				// which makes the final layout position to 0 in mouse released
				// event.
				dotPane.setTranslateX(-currPaneLayoutX);
			} else {
				// If your dragNode bounds are outside parent bounds,ALWAYS
				// setting the translate value that fits your node at end.
				dotPane.setTranslateX(parentBounds.getWidth() - currPaneLayoutX - currPaneWidth);
			}

			if ((currPaneLayoutY + offsetY < parentBounds.getHeight() - currPaneHeight)
					&& (currPaneLayoutY + offsetY > -1)) {
				dotPane.setTranslateY(offsetY);
			} else if (currPaneLayoutY + offsetY < 0) {
				dotPane.setTranslateY(-currPaneLayoutY);
			} else {
				dotPane.setTranslateY(parentBounds.getHeight() - currPaneLayoutY - currPaneHeight);
			}
		};
		dotPane.setOnMouseDragged(dotOnMouseDraggedEventHandler);
		dotPane.setOnMouseReleased(e -> {
			// Updating the new layout positions
			dotPane.setLayoutX(layoutX + dotPane.getTranslateX());
			dotPane.setLayoutY(layoutY + dotPane.getTranslateY());

			// Resetting the translate positions
			dotPane.setTranslateX(0);
			dotPane.setTranslateY(0);
		});

		// add new node to current nodes
		int stat = Integer.parseInt(state);
		statesNodes.put(stat, dotPane);

		return dotPane;
	}

	/**
	 * Builds the single directional line with pointing arrows at each end.
	 * 
	 * @param startDot
	 *            Pane for considering start point
	 * @param endDot
	 *            Pane for considering end point
	 * @param parent
	 *            Parent container
	 * @param hasEndArrow
	 *            Specifies whether to show arrow towards end
	 * @param hasStartArrow
	 *            Specifies whether to show arrow towards start
	 */
	private void buildSingleDirectionalLine(StackPane startDot, StackPane endDot, Pane parent, boolean hasEndArrow,
			boolean hasStartArrow, Color color, String actionName) {

		// line
		Line line = getLine(startDot, endDot, color);

		// label
		StackPane weightAB = getWeight(line, actionName);

		// arrow head
		StackPane arrowAB = getArrow(true, line, startDot, endDot);

		// set id of arrow to startID-endID
		arrowAB.setId(startDot.getId() + ARROW_ID_SEPARATOR + endDot.getId());
		if (!hasEndArrow) {
			arrowAB.setOpacity(0);
		}

		// add arrow to all arrows
		int startState = getStateFromNode(startDot);
		int endState = getStateFromNode(endDot);

		if (startState != -1) {
			if (statesOutgoingArrows.containsKey(startState)) {
				List<StackPane> arws = statesOutgoingArrows.get(startState);
				arws.add(arrowAB);
			} else {
				List<StackPane> arws = new LinkedList<StackPane>();
				arws.add(arrowAB);
				statesOutgoingArrows.put(startState, arws);
			}

		}

		if (endState != -1) {
			if (statesIngoingArrows.containsKey(endState)) {
				List<StackPane> arws = statesIngoingArrows.get(endState);
				arws.add(arrowAB);
			} else {
				List<StackPane> arws = new LinkedList<StackPane>();
				arws.add(arrowAB);
				statesIngoingArrows.put(endState, arws);
			}

		}

		// add arrow's components
		arrowsLines.put(arrowAB, line);
		arrowsLabels.put(arrowAB, weightAB);
		// List<Object> arwComps = new LinkedList<Object>(){{add(line);
		// add(weightAB);}};
		// arrowsComponents.put(arrowAB, arwComps);

		parent.getChildren().addAll(line, weightAB, arrowAB);
	}

	/**
	 * Builds a line between the provided start and end panes center point.
	 *
	 * @param startDot
	 *            Pane for considering start point
	 * @param endDot
	 *            Pane for considering end point
	 * @return Line joining the layout center points of the provided panes.
	 */
	private Line getLine(StackPane startDot, StackPane endDot, Color color) {
		Line line = new Line();
		line.setStroke(color);
		line.setStrokeWidth(2);
		line.startXProperty().bind(
				startDot.layoutXProperty().add(startDot.translateXProperty()).add(startDot.widthProperty().divide(2)));
		line.startYProperty().bind(
				startDot.layoutYProperty().add(startDot.translateYProperty()).add(startDot.heightProperty().divide(2)));
		line.endXProperty()
				.bind(endDot.layoutXProperty().add(endDot.translateXProperty()).add(endDot.widthProperty().divide(2)));
		line.endYProperty()
				.bind(endDot.layoutYProperty().add(endDot.translateYProperty()).add(endDot.heightProperty().divide(2)));
		return line;
	}

	/**
	 * Builds an arrow on the provided line pointing towards the specified pane.
	 *
	 * @param toLineEnd
	 *            Specifies whether the arrow to point towards end pane or start
	 *            pane.
	 * @param line
	 *            Line joining the layout center points of the provided panes.
	 * @param startDot
	 *            Pane which is considered as start point of line
	 * @param endDot
	 *            Pane which is considered as end point of line
	 * @return Arrow towards the specified pane.
	 */
	private StackPane getArrow(boolean toLineEnd, Line line, StackPane startDot, StackPane endDot) {
		double size = 12; // Arrow size
		StackPane arrow = new StackPane();
		arrow.setStyle("-fx-background-color:" + DEFAULT_ARROW_COLOUR
				+ ";-fx-border-width:1px;-fx-border-color:black;-fx-shape: \"M0,-4L4,0L0,4Z\"");//
		arrow.setPrefSize(size, size);
		arrow.setMaxSize(size, size);
		arrow.setMinSize(size, size);

		// Determining the arrow visibility unless there is enough space between
		// dots.
		DoubleBinding xDiff = line.endXProperty().subtract(line.startXProperty());
		DoubleBinding yDiff = line.endYProperty().subtract(line.startYProperty());
		BooleanBinding visible = (xDiff.lessThanOrEqualTo(size).and(xDiff.greaterThanOrEqualTo(-size))
				.and(yDiff.greaterThanOrEqualTo(-size)).and(yDiff.lessThanOrEqualTo(size))).not();
		arrow.visibleProperty().bind(visible);

		// Determining the x point on the line which is at a certain distance.
		DoubleBinding tX = Bindings.createDoubleBinding(() -> {
			double xDiffSqu = (line.getEndX() - line.getStartX()) * (line.getEndX() - line.getStartX());
			double yDiffSqu = (line.getEndY() - line.getStartY()) * (line.getEndY() - line.getStartY());
			double lineLength = Math.sqrt(xDiffSqu + yDiffSqu);
			double dt;
			if (toLineEnd) {
				// When determining the point towards end, the required distance
				// is total length minus (radius + arrow half width)
				dt = lineLength - (endDot.getWidth() / 2) - (arrow.getWidth() / 2);
			} else {
				// When determining the point towards start, the required
				// distance is just (radius + arrow half width)
				dt = (startDot.getWidth() / 2) + (arrow.getWidth() / 2);
			}

			double t = dt / lineLength;
			double dx = ((1 - t) * line.getStartX()) + (t * line.getEndX());
			return dx;
		}, line.startXProperty(), line.endXProperty(), line.startYProperty(), line.endYProperty());

		// Determining the y point on the line which is at a certain distance.
		DoubleBinding tY = Bindings.createDoubleBinding(() -> {
			double xDiffSqu = (line.getEndX() - line.getStartX()) * (line.getEndX() - line.getStartX());
			double yDiffSqu = (line.getEndY() - line.getStartY()) * (line.getEndY() - line.getStartY());
			double lineLength = Math.sqrt(xDiffSqu + yDiffSqu);
			double dt;
			if (toLineEnd) {
				dt = lineLength - (endDot.getHeight() / 2) - (arrow.getHeight() / 2);
			} else {
				dt = (startDot.getHeight() / 2) + (arrow.getHeight() / 2);
			}
			double t = dt / lineLength;
			double dy = ((1 - t) * line.getStartY()) + (t * line.getEndY());
			return dy;
		}, line.startXProperty(), line.endXProperty(), line.startYProperty(), line.endYProperty());

		arrow.layoutXProperty().bind(tX.subtract(arrow.widthProperty().divide(2)));
		arrow.layoutYProperty().bind(tY.subtract(arrow.heightProperty().divide(2)));

		DoubleBinding endArrowAngle = Bindings.createDoubleBinding(() -> {
			double stX = toLineEnd ? line.getStartX() : line.getEndX();
			double stY = toLineEnd ? line.getStartY() : line.getEndY();
			double enX = toLineEnd ? line.getEndX() : line.getStartX();
			double enY = toLineEnd ? line.getEndY() : line.getStartY();
			double angle = Math.toDegrees(Math.atan2(enY - stY, enX - stX));
			if (angle < 0) {
				angle += 360;
			}
			return angle;
		}, line.startXProperty(), line.endXProperty(), line.startYProperty(), line.endYProperty());
		arrow.rotateProperty().bind(endArrowAngle);

		return arrow;
	}

	/**
	 * Builds a pane at the center of the provided line.
	 *
	 * @param line
	 *            Line on which the pane need to be set.
	 * @return Pane located at the center of the provided line.
	 */
	private StackPane getWeight(Line line, String actionName) {

		Label lblAction = new Label(actionName);
		lblAction.setStyle(ACTION_NAME_STYLE);
		lblAction.setOnMouseClicked(e -> {
			if (traceCell != null && trace != null) {
				traceCell.showReact(actionName);
			}
		});

		lblAction.setOnMouseEntered(e -> {
			lblAction.setCursor(Cursor.HAND);
		});

		Label lblActionPerc = new Label();
		lblActionPerc.setStyle(ACTION_PERC_STYLE);

		// add to the map
		if (mapActionPerc.containsKey(actionName)) {
			List<Label> lbls = mapActionPerc.get(actionName);
			lbls.add(lblActionPerc);
		} else {
			List<Label> lbls = new LinkedList<Label>();
			lbls.add(lblActionPerc);
			mapActionPerc.put(actionName, lbls);
		}

		VBox vboxAction = new VBox();
		vboxAction.setAlignment(Pos.CENTER);
		vboxAction.getChildren().addAll(lblAction, lblActionPerc);

		StackPane weight = new StackPane();
		weight.setStyle("-fx-background-color:white");
		weight.getChildren().add(vboxAction);
		DoubleBinding wgtSqrHalfWidth = weight.widthProperty().divide(2);
		DoubleBinding wgtSqrHalfHeight = weight.heightProperty().divide(2);
		DoubleBinding lineXHalfLength = line.endXProperty().subtract(line.startXProperty()).divide(2);
		DoubleBinding lineYHalfLength = line.endYProperty().subtract(line.startYProperty()).divide(2);

		weight.layoutXProperty().bind(line.startXProperty().add(lineXHalfLength.subtract(wgtSqrHalfWidth)));
		weight.layoutYProperty().bind(line.startYProperty().add(lineYHalfLength.subtract(wgtSqrHalfHeight)));
		return weight;
	}

}
