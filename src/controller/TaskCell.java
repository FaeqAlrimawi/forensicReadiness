package controller;

import java.io.IOException;

import ie.lero.spare.pattern_instantiation.GraphPath;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;

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
            setText(null);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
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
           
            
            setText(null);
            setGraphic(hboxRoot);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            
        }
    }
    
    protected void populateCell(GraphPath trace) {
    	
    	lblTraceID.setText(trace.getInstanceID()+"");

    	hbox.getChildren().clear();
        int index = 0;
        int size = trace.getStateTransitions().size()-1;
        //set states
        for(Integer state : trace.getStateTransitions()) {
//        	Circle circle = new Circle(hbox.getHeight()-2);
        	Label stateLbl;
        	if(index != size) {
        		stateLbl = new Label(state+" -> ");
        	} else {
        		stateLbl = new Label(state+"");
        	}
        	
        	index++;
        	hbox.getChildren().add(stateLbl);
        	                 	
        }
    }
}