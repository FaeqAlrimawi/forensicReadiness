package core.brs.parser;

public class ActionWrapper {
	
	private String actionName;
	
	private String reactName;
	
	private BigraphWrapper precondition;
	
	private BigraphWrapper postcondition;

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public BigraphWrapper getPrecondition() {
		return precondition;
	}

	public void setPrecondition(BigraphWrapper precondition) {
		this.precondition = precondition;
	}

	public BigraphWrapper getPostcondition() {
		return postcondition;
	}

	public void setPostcondition(BigraphWrapper postcondition) {
		this.postcondition = postcondition;
	}

	public String getReactName() {
		return reactName;
	}

	public void setReactName(String reactName) {
		this.reactName = reactName;
	}

	
}
