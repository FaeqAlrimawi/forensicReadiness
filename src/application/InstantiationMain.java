package application;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import core.brs.parser.ActionChangeHolder;
import core.brs.parser.ActionWrapper;
import core.brs.parser.BRSParser;
import core.brs.parser.BRSWrapper;
import core.brs.parser.BigraphWrapper;
import core.brs.parser.utilities.BigraphNode;
import core.instantiation.analysis.BigraphStructure;
import core.instantiation.analysis.BigraphStructureAnalyser;
import core.instantiation.analysis.TraceMiner;
import it.uniud.mads.jlibbig.core.Node;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Matcher;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class InstantiationMain extends Application {

//	private Pane layout;

//	private Pane pane;

	@Override
	public void start(Stage primaryStage) {
//		try {

//			FXMLLoader loader = new FXMLLoader();

			//select viewer to run
			boolean runTraceViewer = false;
			boolean runInstatiator = !runTraceViewer;
			
//			String traceViewerGUI = "TraceViewer.fxml";
//			
//			String instantiatorGui = "instantiator.fxml";
			
			if(runTraceViewer) {
				runTraceViewer(primaryStage);
			} else {
				runInstantiatorViewer(primaryStage);
			}
			
//			URL url = ActionCatalogueEditorMain.class.getClassLoader().getResource("fxml/" + viewerToRun);
//
//			if (url != null) {
//				System.out.println(url.getPath());
//			} else {
//				System.err.println("url is null");
//			}
//
//			loader.setLocation(url);
//
//			// Platform.setImplicitExit(false);
//
//			layout = loader.load();
//
//			Scene scene = new Scene(layout);
//
//			// scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
//			primaryStage.setScene(scene);
//			
//			primaryStage.setTitle("Incident Filter");
//			primaryStage.show();
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	protected void runTraceViewer(Stage primaryStage) {
		
		FXMLLoader loader = new FXMLLoader();
		
		String traceViewerGUI = "TraceViewer.fxml";
		
		
		URL url = InstantiationMain.class.getClassLoader().getResource("fxml/" + traceViewerGUI);

		if (url != null) {
			System.out.println(url.getPath());
		} else {
			System.err.println("url is null");
			return;
		}

		loader.setLocation(url);

		// Platform.setImplicitExit(false);

		try {
			Pane layout = loader.load();
			Scene scene = new Scene(layout);
			
			primaryStage.setScene(scene);
			
			primaryStage.setTitle("Incident Filter");
			primaryStage.show();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	protected void runInstantiatorViewer(Stage primaryStage) {
		
	
	FXMLLoader loader = new FXMLLoader();
		

		String InstantiatorViewerGUI = "instantiator.fxml";
		
		
		
		URL url = InstantiationMain.class.getClassLoader().getResource("fxml/" + InstantiatorViewerGUI);

		if (url != null) {
			System.out.println(url.getPath());
		} else {
			System.err.println("url is null");
			return;
		}

		loader.setLocation(url);

		// Platform.setImplicitExit(false);

		try {
			SplitPane layout = loader.load();
			Scene scene = new Scene(layout);
			
			primaryStage.setScene(scene);
			
			
			
			primaryStage.setTitle("Incident Pattern Instantiation Executor");
			primaryStage.show();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			System.out.println("Here's the exception");
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) {
		
		 launch(args);
		 
		 
		TraceMiner miner = new TraceMiner();
		
		//set file
		
		
		String exprs = "Room{con1}.Actor | Room{con1}.(Actor | Actor | id) | id || Room.Device.id || id";
		String exprs2 = "Room{con1}.Actor | Room{con1}.(Actor  id) || Room ";
		String action = "react enter_room = " + exprs + "->" + exprs2 + "@ [1,2,3];";
		String jsonState = "D:/Bigrapher data/lero/lero1/0.json";
		
		URL bigFileURL = ActionCatalogueEditorMain.class.getResource("../resources/example/systemBigraphER.big");
		URL statesFolderURL = ActionCatalogueEditorMain.class.getResource("../resources/example/states_1000");
		
		String bigFile = null;
		String statesFolder = null; 
		
		if(bigFileURL!=null) {
			bigFile = bigFileURL.getPath();
		BRSParser parser = new BRSParser();
		BRSWrapper brsWrapper = parser.parseBigraphERFile(bigFile);
		ActionWrapper act = brsWrapper.getActions().get("VisitorEnterRoom");
		
		BigraphWrapper pre = act.getPrecondition();
		BigraphWrapper post = act.getPostcondition();
		
		BigraphStructureAnalyser analyser = new BigraphStructureAnalyser();
		
		List<BigraphWrapper> cons = new LinkedList<BigraphWrapper>();
		
		cons.add(pre);
		cons.add(post);
		
		List<BigraphStructure> res = analyser.findCommonalities(cons, null);
		
		if(res!=null) {
			for(BigraphStructure str : res) {
				System.out.println(str);
			}
		} else{
			System.out.println("result is null");
		}
		
//		Map<String, List<ActionChangeHolder>> changes = act.findChanges();
//		
//		for(Entry<String, List<ActionChangeHolder>> entry : changes.entrySet()) {
//			
//			System.out.println("Entity: " + entry.getKey());
//			System.out.println("Changes in containment:");
//			for(ActionChangeHolder change : entry.getValue()) {
//				System.out.println("\t"+change.toString());
//			}
//			
//			System.out.println("=============================\n");
//		}
		}
		
//		bigFile = bigFileURL.getPath();
////		BRSParser parser = new BRSParser();
////		BRSWrapper brsWrapper = parser.parseBigraphERFile(bigFile);
////		ActionWrapper act = brsWrapper.getActions().get("EnterRoom");
////		BigraphWrapper pre = act.getPrecondition();
//		TraceMiner miner = new TraceMiner();
//		
//		//set bigrapher file. Does parsing at the same time
//		miner.setBigraphERFile(bigFile); 
//		
//		//set states folder
//		statesFolder = statesFolderURL.getPath();
//		miner.setStatesFolder(statesFolder);
//		
//		String action2 = "ConnectBusDevice";
//		String action1 = "EnterRoom";
//		int preState = 1;
//		
//		int res = miner.areActionsCausallyDependent(action2, action1, preState);
//		
//		switch (res) {
//		case TraceMiner.ACTIONS_CAUSAL_DEPENDENCY_ERROR:
//			System.err.println("There's an error");
//			break;
//
//		case TraceMiner.ACTIONS_CAUSALLY_DEPENDENT: //dependent
//			System.out.println("[" + action2 + "] causally depends on [" + action1 + "] with pre-state ["+preState+"]");
//			break;
//
//		case TraceMiner.ACTIONS_NOT_CAUSALLY_DEPENDENT: //independent
//			System.out.println("[" + action2 + "] does NOT causally depend on [" + action1 + "] with pre-state ["+preState+"]");
//			break;
//			
//		default:
//			break;
//		}
////		ActionWrapper act = miner.getActionWrapper("EnterRoom");
////		BigraphWrapper pre = act.getPrecondition();
////		System.out.println(pre.getBigraphERString());
//		} else {
//			System.out.println("file not found");
//		}
		
		
		// String exprs2 = "Room{con1}.Actor | Room{con1}.(Actor | Actor)";
		//// System.out.println("expression: "+exprs+"\n\n");
//		BRSParser parser = new BRSParser();
		//
//		 BRSWrapper brsWrapper = parser.parseBigraphERFile(bigFile);
		 
//		 ActionWrapper act = brsWrapper.getActions().get("EnterRoom");
//		 ActionWrapper act2 = brsWrapper.getActions().get("ConnectBusDevice");
//		 
//		 if(act!= null) {
//			 BigraphWrapper pre = act.getPrecondition();
			 
//			 BigraphWrapper post = act.getPostcondition();
//			 
//			 Bigraph bigPre = pre.createBigraph(true, brsWrapper.getSignature());
//			System.out.println(pre.getBigraphERString());
//			 Bigraph bigPost = post.createBigraph(true, brsWrapper.getSignature());
//			 
//			 BigraphWrapper pre2 = act2.getPrecondition();
//			 BigraphWrapper post2 = act2.getPostcondition();
//			 
//			 Bigraph bigPre2 = pre2.createBigraph(true, brsWrapper.getSignature());
//			 Bigraph bigPost2 = post2.createBigraph(true, brsWrapper.getSignature());
//			 
//			 
//			 Matcher matcher = new Matcher();
//			 if(matcher.match(bigPre2, bigPost).iterator().hasNext()) {
//				 System.out.println("matched!");
//			 } else {
//				 System.out.println("NOT matched!");
//			 }
////			 System.out.println(bigPre);
////			 System.out.println("\n"+post.getBigraphERString());
////			 System.out.println(bigPost);
//		 }
		// ActionWrapper action = parser.parseBigraphERAction(action);
		// BigraphWrapper big = parser.parseBigraphERState(jsonState);
//		Map<String, ActionWrapper> actions = parser.parseBigraphERFile(bigFile);
		//
		// System.out.println(a.getActionName());
		// condition.printAll();
//		for (ActionWrapper a : actions.values()) {
//			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
//			System.out.println(a.getActionName() + " reactName: " + a.getReactName());
//			System.out.println(a.getPrecondition().getBigraphERString());
//			System.out.println(a.getPostcondition().getBigraphERString());
//			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
//		}
		// Bigraph bigraph = big.getBigraphObject();
		//
		// Collection<Node> nodes = (Collection<Node>) bigraph.getNodes();
		// for(Node n : nodes) {
		// System.out.println("control: " + n + " parent: "+n.getParent());
		// }

		// System.out.println(big.getBigraphObject());
		// ActionWrapper w = parser.parseBigraphERAction(action);

		// System.out.println(w.getActionName());
		// BigraphWrapper pre = w.getPrecondition();
		// BigraphWrapper post = w.getPostcondition();
		//
		// pre.printAll();
		// post.printAll();
		// BigraphWrapper r = parser.parseBigraph(exprs);
		//
		// BigraphExpression big = r.getBigraphExpression();
		//
		// BigraphWrapper r2 = parser.parseBigraph(big);
		//
		//// Bigraph b = r.getBigraphObject();
		//
		//// System.out.println(b);
		//// r.printAll();
		// String str = r2.generateBigraphERState();
		// System.out.println(str);
		// r2.printAll();

		// parser.parseBigraph(exprs2);
		// parser.printAll();
	}

	@Override
	public void stop() {
		System.exit(0);
	}
}
