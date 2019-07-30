package core.brs.parser;

import java.util.HashMap;
import java.util.Map;

import it.uniud.mads.jlibbig.core.std.Control;

public class BRSWrapper {

	enum BigraphType {
		BRS, PBRS, // propabilistic
		SBRS; // stochastic
	}

	// name, if any
	private String name;

	// type (BRS, PBRS, SBRS)
	private BigraphType type;

	// controls defined
	//key is control name, value is the object representation
	private Map<String, Control> controls;

	// actions (or reactions) of the brs
	//key is react name, value is an ActionWrapper object
	private Map<String, ActionWrapper> actions;

	public BRSWrapper() {
		controls = new HashMap<String, Control>();
		actions = new HashMap<String, ActionWrapper>();
	}

	public Map<String, ActionWrapper> getActions() {
		return actions;
	}

	public void setActions(Map<String, ActionWrapper> actions) {
		this.actions = actions;
	}

	public Map<String, Control> getControls() {
		return controls;
	}

	public void setControls(Map<String, Control> controls) {
		this.controls = controls;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigraphType getType() {
		return type;
	}

	public void setType(BigraphType type) {
		this.type = type;
	}

	public void addControl(Control ctrl) {
		
		controls.put(ctrl.getName(), ctrl);
	}
	
	public void addControl(String name, int arity) {
		//by defualt the control is active (i.e. can be part of reaction rules)
		addControl(name, true, arity);
		
	}
	
	public void addControl(String name, boolean isActive, int arity) {
		//by defualt the control is active (i.e. can be part of reaction rules)
		Control ctrl = new Control(name, isActive, arity);
		controls.put(name, ctrl);
	}

}
