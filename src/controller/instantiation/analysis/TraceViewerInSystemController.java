package controller.instantiation.analysis;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import core.instantiation.analysis.TraceMiner;
import ie.lero.spare.franalyser.utility.Digraph;
import ie.lero.spare.franalyser.utility.TransitionSystem;
import ie.lero.spare.pattern_instantiation.GraphPath;
import ie.lero.spare.pattern_instantiation.LabelExtractor;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class TraceViewerInSystemController {

	@FXML
	private StackPane mainStackPane;

	@FXML
	private ProgressIndicator progressIndicator;

	@FXML
	private Button btnShowPreviousStates;
	
	@FXML
	private Button btnLoadTransitionSystem;
	
	private GraphPath trace;

	private Pane tracePane;

	//nodes of the trace
	private List<StackPane> traceNodes;
	
	//nodes of previous states (previous to the initial)
	private List<StackPane> previousNodes;
	
	private double sceneX, sceneY, layoutX, layoutY;

	private TraceMiner miner;

	private URL defualtTransitionSystemFilePath = getClass().getResource("../../../resources/example/transitions_labelled.json");
//	private URL defualtTransitionSystemFolder = getClass().getResource("../../../resources/example");

//	private TransitionSystem tranSys;

	private static final String NODE_COLOUR = "white";
	private static final String DEFAULT_ARROW_COLOUR = "#333333";
	private static final Color TRACE_ARROW_COLOUR = Color.BLUE;
	private static final Color ADDED_NODES_ARROW_COLOUR = Color.GREY;
	private static final double NODE_RADIUS = 25;
	private static final String STATE_STYLE = "-fx-font-size:18px;-fx-font-weight:bold;";
	private static final String ACTION_NAME_STYLE = "-fx-font-size:13px;";

	private static final int previousStateNum = 2;

	@FXML
	public void initialize() {

		tracePane = new Pane();
		mainStackPane.getChildren().add(tracePane);
		mainStackPane.setPadding(new Insets(20));
	}

	@FXML
	void loadTransitionSystem(ActionEvent e) {

		progressIndicator.setVisible(true);

		if (miner != null) {
			if (defualtTransitionSystemFilePath != null) {
				System.out.println("loading sys from " + defualtTransitionSystemFilePath.getPath());
				miner.setTransitionSystemFilePath(defualtTransitionSystemFilePath.getPath());
				miner.loadTransitionSystem();
				System.out.println("Done loading");
			} else {

			}

		}

		progressIndicator.setVisible(false);
		
		toggleButtonActivity(btnLoadTransitionSystem, true);

	}

	@FXML
	void showPreviousStates(ActionEvent e) {

		if(miner == null) {
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

		// get initial state
		List<Integer> states = trace.getStateTransitions();

		if (states == null) {
			return;
		}

		

		// get node
		StackPane initNode = (StackPane) traceNodes.get(0);

		int initialState = Integer.parseInt(initNode.getId());
		
		Digraph<Integer> digraph = miner.getTransitionSystem().getDigraph();

//		System.out.println(digraph);
		// get in bound states (going to initial state)
		List<Integer> inBoundStates = digraph.inboundNeighbors(initialState);

		
		if(previousNodes!= null) {
			//remove from the curren pane
//			tracePane.getChildren().removeAll(previousNodes);
			return;
		}
		
		previousNodes = new LinkedList<StackPane>();

		// create nodes for each inbound
		for (Integer inBoundState : inBoundStates) {
			StackPane node = getDot(NODE_COLOUR, "" + inBoundState, NODE_RADIUS);
			node.setId("" + inBoundState);
			previousNodes.add(node);

			String actionName = digraph.getLabel(inBoundState, initialState);
			
			buildSingleDirectionalLine(node, initNode, tracePane, true, false, ADDED_NODES_ARROW_COLOUR, actionName );
		}

		// add to the pane
		tracePane.getChildren().removeAll(traceNodes);
		tracePane.getChildren().addAll(traceNodes);
		tracePane.getChildren().addAll(previousNodes);

		// position new nodes
		double posX = initNode.getLayoutX() - (NODE_RADIUS * 2 + 30);
		int yOffest = (int) (NODE_RADIUS*2);
		double posY = initNode.getLayoutY()-yOffest*2;

		for(StackPane node : previousNodes) {
			node.setLayoutX(posX);
			node.setLayoutY(posY);
			
			//same place
//			posX
			
			posY+=yOffest*2;
		}
		
//		toggleButtonActivity(btnShowPreviousStates, true);
	}

	public void showTrace(GraphPath trace) {

		if (trace == null) {
			System.err.println("Trace is NULL");
			return;
		}

		tracePane.getChildren().clear();

		this.trace = trace;

		traceNodes = createTraceNodes(trace);

		tracePane.getChildren().addAll(traceNodes);

		// position each node
		double posX = 150;
		double posY = 150;
		for (StackPane node : traceNodes) {

			node.setLayoutX(posX);
			node.setLayoutY(posY);

			// update x axis to be the circle plus some gap
			posX += NODE_RADIUS * 2 + 100;
			posY += NODE_RADIUS * 2 + 20;

		}

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
			StackPane node = getDot(NODE_COLOUR, "" + state, NODE_RADIUS);
			node.setId(state + "");
			// count--;

			// create edge between current and previous
			if (index > 0) {
				buildSingleDirectionalLine(stateNodes.get(index - 1), node, tracePane, true, false, TRACE_ARROW_COLOUR,
						actions.get(index - 1));
				// count = 1;
			}

			stateNodes.add(node);

			index++;
		}

		return stateNodes;
	}

	public void setTraceMiner(TraceMiner traceMiner) {
		miner = traceMiner;
		
		if(miner != null) {
			if(miner.getTransitionSystem() != null) {
				toggleButtonActivity(btnLoadTransitionSystem, true);
			}
		}
	}

	
	protected void toggleButtonActivity(Button btn, boolean isDisabled) {
		if(btn == null) {
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

	/**
	 * Builds a pane consisting of circle with the provided specifications.
	 *
	 * @param color
	 *            Color of the circle
	 * @param text
	 *            Text inside the circle
	 * @return Draggable pane consisting a circle.
	 */
	private StackPane getDot(String color, String text, double radius) {
		// double radius = 50;
		double paneSize = 2 * radius;
		StackPane dotPane = new StackPane();
		Circle dot = new Circle();
		dot.setRadius(radius);
		dot.setStyle("-fx-fill:" + color + ";-fx-stroke-width:2px;-fx-stroke:black;");

		Label txt = new Label(text);
		txt.setStyle(STATE_STYLE);
		txt.setTooltip(new Tooltip(text));
		
		dotPane.getChildren().addAll(dot, txt);
		dotPane.setPrefSize(paneSize, paneSize);
		dotPane.setMaxSize(paneSize, paneSize);
		dotPane.setMinSize(paneSize, paneSize);
		dotPane.setOnMousePressed(e -> {
			sceneX = e.getSceneX();
			sceneY = e.getSceneY();
			layoutX = dotPane.getLayoutX();
			layoutY = dotPane.getLayoutY();
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
		Line line = getLine(startDot, endDot, color);
		StackPane arrowAB = getArrow(true, line, startDot, endDot);
		if (!hasEndArrow) {
			arrowAB.setOpacity(0);
		}
		StackPane arrowBA = getArrow(false, line, startDot, endDot);
		if (!hasStartArrow) {
			arrowBA.setOpacity(0);
		}
		StackPane weightAB = getWeight(line, actionName);
		parent.getChildren().addAll(line, weightAB, arrowBA, arrowAB);
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
		arrow.setStyle(
				"-fx-background-color:"+DEFAULT_ARROW_COLOUR+";-fx-border-width:1px;-fx-border-color:black;-fx-shape: \"M0,-4L4,0L0,4Z\"");//
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
		// double size = 20;
		// String actionName = "enterRoom";
		Label lbl = new Label(actionName);
		lbl.setStyle(ACTION_NAME_STYLE);
		StackPane weight = new StackPane();
		weight.setStyle("-fx-background-color:white");
		// weight.setPrefSize(size, size);
		// weight.setMaxSize(size, size);
		// weight.setMinSize(size, size);
		weight.getChildren().add(lbl);
		DoubleBinding wgtSqrHalfWidth = weight.widthProperty().divide(2);
		DoubleBinding wgtSqrHalfHeight = weight.heightProperty().divide(2);
		DoubleBinding lineXHalfLength = line.endXProperty().subtract(line.startXProperty()).divide(2);
		DoubleBinding lineYHalfLength = line.endYProperty().subtract(line.startYProperty()).divide(2);

		weight.layoutXProperty().bind(line.startXProperty().add(lineXHalfLength.subtract(wgtSqrHalfWidth)));
		weight.layoutYProperty().bind(line.startYProperty().add(lineYHalfLength.subtract(wgtSqrHalfHeight)));
		return weight;
	}

}
