package application;

import java.net.URL;

import core.brs.parser.ActionWrapper;
import core.brs.parser.BRSParser;
import core.brs.parser.BigraphWrapper;
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
		launch(args)	;
		String exprs = "Room{con1}.Actor | Room{con1}.(Actor | Actor | id) || Room.Device.id || id";
		String exprs2 = "Room{con1}.Actor | Room{con1}.(Actor  id) || Room ";
		String action = "react enter_room = " + exprs + "->" + exprs2 + "[1,2,3];";
		
//		String exprs2 = "Room{con1}.Actor | Room{con1}.(Actor | Actor)";
////		System.out.println("expression: "+exprs+"\n\n");
		BRSParser parser = new BRSParser();
//		
		ActionWrapper w = parser.parseBigraphERAction(action);
		
		System.out.println(w.getActionName());
		BigraphWrapper pre = w.getPrecondition();
		BigraphWrapper post = w.getPostcondition();
		
		pre.printAll();
		post.printAll();
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
