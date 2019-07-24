package application;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import core.brs.parser.ActionWrapper;
import core.brs.parser.BRSParser;
import core.brs.parser.BigraphWrapper;
import core.brs.parser.utilities.BigraphNode;
import it.uniud.mads.jlibbig.core.Node;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

	private Pane layout;

	private SplitPane splitPaneInstantiator;
	

	@Override
	public void start(Stage primaryStage) {
		try {

			FXMLLoader loader = new FXMLLoader();

			String traceViewerGUI = "TraceViewer.fxml";
			String instantiatorGui = "instantiator.fxml";
			
			URL url = Main.class.getClassLoader().getResource("fxml/"+traceViewerGUI);
			
			if(url!=null) {
				System.out.println(url.getPath());	
			} else{ 
				System.err.println("url is null");
			}
			
	
			loader.setLocation(url);

			// Platform.setImplicitExit(false);

			layout = loader.load();

			Scene scene = new Scene(layout);

			// scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle("Incident Filter");
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
//		launch(args)	;
		String exprs = "Room{con1}.Actor | Room{con1}.(Actor | Actor | id) | id || Room.Device.id || id";
		String exprs2 = "Room{con1}.Actor | Room{con1}.(Actor  id) || Room ";
		String action = "react enter_room = " + exprs + "->" + exprs2 + "[1,2,3];";
		String jsonState = "D:/Bigrapher data/lero/lero1/0.json";
		String bigFile = "D:/Bigrapher data/lero/example/lero.big";
		
//		String exprs2 = "Room{con1}.Actor | Room{con1}.(Actor | Actor)";
////		System.out.println("expression: "+exprs+"\n\n");
		BRSParser parser = new BRSParser();
//		
		BigraphWrapper condition = parser.parseBigraphERCondition(exprs);
//		ActionWrapper action = parser.parseBigraphERAction(action);
//		BigraphWrapper big = parser.parseBigraphERState(jsonState);
//		Map<String, ActionWrapper> actions = parser.parseBigraphERFile(bigFile);
//		
//		System.out.println(a.getActionName());
		condition.printAll();
//		for(ActionWrapper a : actions.values()) {
//			System.out.println(a.getActionName());
//		}
//		Bigraph bigraph = big.getBigraphObject();
//		
//		Collection<Node> nodes = (Collection<Node>) bigraph.getNodes();
//		for(Node n : nodes) {
//			System.out.println("control: " + n + " parent: "+n.getParent());	
//		}
		
//		System.out.println(big.getBigraphObject());
//		ActionWrapper w = parser.parseBigraphERAction(action);
		
		
//		System.out.println(w.getActionName());
//		BigraphWrapper pre = w.getPrecondition();
//		BigraphWrapper post = w.getPostcondition();
//		
//		pre.printAll();
//		post.printAll();
//		BigraphWrapper r = parser.parseBigraph(exprs);
//
//		BigraphExpression big = r.getBigraphExpression();
//		
//		BigraphWrapper r2 = parser.parseBigraph(big);
//		
////		Bigraph b = r.getBigraphObject();
//
////		System.out.println(b);
////		r.printAll();
//		String str = r2.generateBigraphERState();
//		System.out.println(str);
//		r2.printAll();
		
//		parser.parseBigraph(exprs2);
//		parser.printAll();
	}
	
	  @Override
	    public void stop() {
	        System.exit(0);
	    }
}
