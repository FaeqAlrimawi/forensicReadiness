package core;

public interface TracesMinerListener {
	
	//number of traces is read from files
	public void onNumberOfTracesRead(int numOfTraces);
	
	//a single trace is loaded from file
	public void onTracesLoaded(int numOfTracesLoaded);

}
