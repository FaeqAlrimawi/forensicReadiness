package core.utilities;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class Query {
	
	// original query
	protected String query;
	// actions
	protected List<String> queryActions;
	
	// generated regex
	protected String regex;
	//pattern generated from regex
	protected Pattern pattern;

	public final static String ACTIONS_SEPARATOR = "";

	// query special chars
	public final static String SINGLE_ACTION = "?";
	public final static String ANY_ACTIONS = "*";

	// === regex counterparts
	// a word or more
	public final static String ANY_ACTIONS_REGEX = "[a-zA-Z_0-9]+";
	// a word
	public final static String SINGLE_ACTION_REGEX = "[a-zA-Z_0-9]";

	public Query() {

	}

	public Query(String query) {
		this.query = query;
		queryActions = new LinkedList<String>();
	}

	public Pattern generatePattern() {
		
		return generatePattern(query);
	}
	
	public Pattern generatePattern(String query) {

		StringBuilder strBldr = new StringBuilder();

		List<String> actions = parseQuery(query);

		if (actions == null) {
			return null;
		}

		for (String act : actions) {

			switch (act) {
			case ANY_ACTIONS:
				strBldr.append(ANY_ACTIONS_REGEX).append(ACTIONS_SEPARATOR);
				break;

			case SINGLE_ACTION:
				strBldr.append(SINGLE_ACTION_REGEX).append(ACTIONS_SEPARATOR);
				break;

			default:
				strBldr.append(act);//.append(ACTIONS_SEPARATOR);
				queryActions.add(act);
				break;
			}

		}

		// remove last separator
//		if (strBldr.length() > ACTIONS_SEPARATOR.length()) {
//			strBldr.delete(strBldr.length() - ACTIONS_SEPARATOR.length() - 1, strBldr.length());
//		}

		// generate pattern
		regex = Pattern.quote(strBldr.toString());
		pattern = Pattern.compile(regex);

		System.out.println("regex "+regex);
		return pattern;
	}

	public boolean matches(List<String> traceActions) {
		
		if (pattern == null) {
			System.err.println("Query: Pattern is Null");
			return false;
		}

		//compare lengths
		if (queryActions.size() > traceActions.size()) {
//			System.out.println("Query actions ar more than trace actions");
			return false;
		}
		
		// convert to format for matching
		StringBuilder strBldr = new StringBuilder();

		for (String action : traceActions) {
			strBldr.append(action);//.append(ACTIONS_SEPARATOR);
		}

		// remove last separator
//		if (strBldr.length() > ACTIONS_SEPARATOR.length()) {
//			strBldr.delete(strBldr.length() - ACTIONS_SEPARATOR.length() - 1, strBldr.length());
//		}

		
		
		// match
		boolean isMatched = pattern.matcher(strBldr.toString()).matches();
		
		if(isMatched) {
			System.out.println("Matched to: " + strBldr.toString());
		}
		return isMatched;

	}

	protected List<String> parseQuery(String query) {

		// actions separated by comma
		query = query.trim();

		// remove all space
		query = query.replaceAll(" ", "");

		List<String> result = Arrays.asList(query.split(","));

		Iterator<String> it = result.iterator();

		// List<Integer> indexToRemove = new LinkedList<Integer>();

		// for(int i=0;i<result.size();i++) {
		// String act = result.get(i);
		// if(act.isEmpty() || act.equals(" ")) {
		// indexToRemove.add(i);
		// }
		// }

		while (it.hasNext()) {
			String act = it.next();
			if (act.isEmpty() || act.equals(" ")) {
				it.remove();
			}
		}

		System.out.println(result);

		return result;
	}

}
