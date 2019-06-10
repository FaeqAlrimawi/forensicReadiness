package application;

import java.net.URL;

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

			URL url = Main.class.getResource("../fxml/instantiator.fxml");
			
			if(url!=null) {
				System.out.println(url.getPath());	
			} else{ 
				System.out.println("url is null");
			}
			
			
			loader.setLocation(url);

			// Platform.setImplicitExit(false);

			splitPaneInstantiator = loader.load();

			Scene scene = new Scene(splitPaneInstantiator);

			// scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle("Incident Pattern Instantiator");
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	  @Override
	    public void stop() {
	        System.exit(0);
	    }
}
