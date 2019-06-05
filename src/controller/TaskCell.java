package controller;

import java.io.IOException;
import java.util.List;

import ie.lero.spare.pattern_instantiation.GraphPath;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class TaskCell extends ListCell<GraphPath> {

    @FXML
    private Label lblTraceID;

    @FXML
    private HBox hbox;
    
    @FXML
    private HBox hboxRoot;
    
//    @FXML 
//    private Circle state;
//    
//    @FXML
//    private Label commentLabel;
//
//    @FXML
//    private Label descriptionLabel;

    public TaskCell() {
        loadFXML();
    }

    private void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/task_cell.fxml"));
            loader.setController(this);
//            loader.setRoot(this);
            loader.load();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(GraphPath trace, boolean empty) {
        super.updateItem(trace, empty);

        if(empty || trace == null) {
   
            Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
			         setText(null);
					setContentDisplay(ContentDisplay.TEXT_ONLY);
					
				}
			});
            
        }
        else {

        	//set trace id
        	String id = lblTraceID.getText();
        	
        	//if new
        	if(id == null || id.isEmpty()) {
//        		 lblTraceID.setText(trace.getInstanceID()+"");
//
//                 int index = 0;
//                 int size = trace.getStateTransitions().size()-1;
//                 //set states
//                 for(Integer state : trace.getStateTransitions()) {
////                 	Circle circle = new Circle(hbox.getHeight()-2);
//                 	Label stateLbl;
//                 	if(index != size) {
//                 		stateLbl = new Label(state+" -> ");
//                 	} else {
//                 		stateLbl = new Label(state+"");
//                 	}
//                 	
//                 	index++;
//                 	hbox.getChildren().add(stateLbl);
//                 	                 	
//                 }

        		populateCell(trace);
        		
                 //if already exist, check the id of the trace if different
        	} else{
        		int currentTraceID = Integer.parseInt(id);
        		
        		if(currentTraceID != trace.getInstanceID()) {
        		populateCell(trace);	
        		}
        	}
           
            Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					setText(null);
		            setGraphic(hboxRoot);
		            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				}
			});
            
            
        }
    }
    
    protected void populateCell(GraphPath trace) {
    	
    	Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				lblTraceID.setText(trace.getInstanceID()+"");
				hbox.getChildren().clear();
				Pane pane = new Pane();
				pane.setPrefWidth(10);
				hbox.getChildren().add(pane);
				
			}
		});
    	

    	
        int index = 0;
        int size = trace.getStateTransitions().size()-1;
        List<Integer> states = trace.getStateTransitions();
        List<String> actions = trace.getTransitionActions();
        StringBuilder strBldr = new StringBuilder();
        
        
        //set states
        for(Integer state : states) {
//        	Circle circle = new Circle(hbox.getHeight()-2);
        	Label lblState;
        	Label lblAction;
        	if(index != size) {
        		lblState = new Label(state+"");
        		lblState.setStyle("-fx-text-fill:black; -fx-font-size:12px");
        		strBldr.append(state);
        		
        		lblAction = new Label(" =[" + actions.get(index)+"]=> ");
        		lblAction.setStyle("-fx-text-fill: grey; -fx-font-size:10px");
        	
        		strBldr.append(" =[" + actions.get(index)+"]=> ");
        	} else {
        		lblState = new Label(state+"");
        		strBldr.append(state);
        		lblAction = null;
        	}
        	
        	index++;
        	
        	Platform.runLater(new Runnable() {
    			
    			@Override
    			public void run() {
    				// TODO Auto-generated method stub
    				hbox.getChildren().add(lblState);
    				if(lblAction!=null) {
    					hbox.getChildren().add(lblAction);	
    				}
    				
    				//add tooltip
    				Tooltip tip = new Tooltip(strBldr.toString());
    				lblTraceID.setTooltip(tip);
    				
    			}
    		});
        	
        	                 	
        }
    }
}