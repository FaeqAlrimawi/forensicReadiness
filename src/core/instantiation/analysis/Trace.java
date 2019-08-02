package core.instantiation.analysis;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import core.brs.parser.ActionWrapper;
import core.brs.parser.BigraphWrapper;

public class Trace implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -29617119168807943L;

	// key is action name, value is the object rep. the action
	private Map<String, ActionWrapper> actions;
	
	//id
	private String traceID;
	

	public Trace() {
		actions = new HashMap<String, ActionWrapper>();
	}

	
	public String getTraceID() {
		return traceID;
	}

	public void setTraceID(String traceID) {
		this.traceID = traceID;
	}


	public Map<String, ActionWrapper> getActions() {
		return actions;
	}

	public void setActions(Map<String, ActionWrapper> actions) {
		this.actions = actions;
	}

	public void addAction(ActionWrapper newAction) {

		if (newAction == null) {
			return;
		}

		String actName = newAction.getActionName();

		actions.put(actName, newAction);
	}

	public void addAction(ActionWrapper newAction, int originalPreState, int originalPostState) {

		if (newAction == null) {
			return;
		}

		String actName = newAction.getActionName();

		BigraphWrapper pre = newAction.getPrecondition();

		if (pre != null) {
			pre.setOriginalState(originalPreState);
		}

		BigraphWrapper post = newAction.getPrecondition();

		if (post != null) {
			post.setOriginalState(originalPreState);
		}

		actions.put(actName, newAction);
	}

	public boolean save(String filePath) {
		
		 FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(filePath);
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
	         objectOut.writeObject(this);
	         objectOut.close();
	         
	         return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
         
	}
}
