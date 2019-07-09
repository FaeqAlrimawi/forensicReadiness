package application;

import java.net.URL;

import core.brs.parser.BRSParser;
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
//		launch(args);
		String exprs = "Room{con1}.Actor | Room{con1}.(Actor | Actor) || Room";
		String exprs2 = "Room{con1}.Actor | Room{con1}.(Actor | Actor)";
		System.out.println("expression: "+exprs+"\n\n");
		BRSParser parser = new BRSParser();
		
		parser.parseBigraph(exprs);
//		parser.clear();
		parser.parseBigraph(exprs2);
	}
	
	  @Override
	    public void stop() {
	        System.exit(0);
	    }
}
