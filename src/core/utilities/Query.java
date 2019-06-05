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

	public final static String ACTIONS_SEPARATOR = "\\s";

	// query special chars
	public final static String SINGLE_ACTION = "?";
	public final static String ANY_ACTIONS = "*";

	// === regex counterparts
	// a word or more
	public final static String ANY_ACTIONS_REGEX = "[a-zA-Z_0-9]+";
	// a word
	public final static String SINGLE_ACTION_REGEX = "\\w";//"[a-zA-Z_0-9]";
	
	//word boundary
	public final static String WORD_BOUNDARY = "\\b";
	
	public final static String FROM_START = "^";
	
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

		int index = 0;
		
//		strBldr.append(FROM_START);
		
		for (String act : actions) {

			switch (act) {
			case ANY_ACTIONS:
				strBldr.append(ANY_ACTIONS_REGEX);
				break;

			case SINGLE_ACTION:
				strBldr.append(WORD_BOUNDARY).append(SINGLE_ACTION_REGEX).append(WORD_BOUNDARY);
				break;

			default:
				strBldr.append(act.toLowerCase());
				queryActions.add(act);
				break;
			}
			
		if(index != actions.size()-1) {
			strBldr.append(ACTIONS_SEPARATOR);
		}
		
		index++;

		}

		// remove last separator
//		if (strBldr.length() > ACTIONS_SEPARATOR.length()) {
//			strBldr.delete(strBldr.length() - ACTIONS_SEPARATOR.length() - 1, strBldr.length());
//		}

		// generate pattern
		String regexStr = strBldr.toString();
		regex = Pattern.quote(regexStr);
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

		int index = 0;
		
		for (String action : traceActions) {
			strBldr.append(action.toLowerCase());//.append(ACTIONS_SEPARATOR);
			
			if(index != traceActions.size()-1) {
				strBldr.append(ACTIONS_SEPARATOR);
			}
			
			index++;
		}

		// remove last separator
//		if (strBldr.length() > ACTIONS_SEPARATOR.length()) {
//			strBldr.delete(strBldr.length() - ACTIONS_SEPARATOR.length() - 1, strBldr.length());
//		}


		// match
		boolean isMatched = pattern.matcher(strBldr.toString()).lookingAt();
		
		
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
