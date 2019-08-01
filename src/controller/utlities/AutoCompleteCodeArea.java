package controller.utlities;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.fxmisc.richtext.CodeArea;

import core.SystemMetaModelHelper;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;

/**
 * This class is a TextField which implements an "autocomplete" functionality,
 * based on a supplied list of entries.
 * 
 * @author Caleb Brinkman
 */
public class AutoCompleteCodeArea {
	/** The existing autocomplete entries. */
	private SortedSet<String> entries;
	/** The popup used to select an entry. */
	private ContextMenu entriesPopup;

	private String subWord;
	private String text;
	private String oldText;
	private int pos;
	private CodeArea codeArea;

	private Bounds bound;
	private List<Character> stopChars;
	private char openConnectivityChar = '{';
	private char closeConnectivityChar = '}';
	private char newLine = '\n';
	private final int lengthForPrediction = 2;
	private boolean isConnectivity = false;
	private int numOfOpenBrackets = 0;
	private int openBracketIndex = -1;
	// key is index of open bracket "{", value is close bracket index "}"
	private Map<Integer, Integer> openCloseBracketMap;
	// private int numOfCloseBracket = 0;

	private Map<String, String> modifiedOriginalEntriesMap;

	// public AutoCompleteTextArea(String oldStr, String newStr, int pos, Bounds
	// bound) {
	//
	// text = newStr;
	// oldText = oldStr;
	// this.pos = pos;
	// this.bound = bound;
	//
	// }
	//
	// public void setAutoCompleteData(String oldStr, String newStr, int pos,
	// Bounds bound) {
	// text = newStr;
	// oldText = oldStr;
	// this.pos = pos;
	// this.bound = bound;
	// }

	public AutoCompleteCodeArea(CodeArea area, String text) {
		codeArea = area;
		this.text = text;
	}

	/** Construct a new AutoCompleteTextField. */
	public AutoCompleteCodeArea() {
		super();

		openCloseBracketMap = new HashMap<Integer, Integer>();
		// set system types as entries to the auto complete
		setEntriesFromSystemTypes();

		// set stop chars
		setStopChars();

		entriesPopup = new ContextMenu();

		// textProperty().addListener(new ChangeListener<String>() {
		// public void changed(ObservableValue<? extends String>
		// observableValue,
		// String s, String s2) {
		//
		// if (getText().length() == 0) {
		// entriesPopup.hide();
		// } else {
		//
		// //check that no new text is copied
		// if(!s2.contains(s)) {
		// return;
		// }
		//
		// // check if connectivity
		// char lastChar = s2.charAt(s2.length() - 1);
		//
		// // if starting a connecitivty then stop auto-complete unitl closing
		// if (lastChar == openConnectivityChar) {
		// isConnectivity = true;
		// return;
		// }
		//
		// if (lastChar == closeConnectivityChar) {
		// isConnectivity = false;
		// return;
		// }
		//
		// // if is connectivity then return (no autoComplete)
		// if (isConnectivity) {
		// return;
		// }
		//
		// pos = getCaretPosition();
		//
		// // if new longer than older
		// if (s2.length() > s.length()) {
		// pos++;
		// } else {
		// pos--;
		// }
		//
		// // System.out.println("s ["+s + "] s2 [" + s2+"]");
		// text = s2;// getText();
		// int start = Math.max(0, pos - 1);
		//
		// // System.out.println("caret " + pos + " txt: " + text.length() + "
		// // start " + start);
		// while (start > 0) {
		// // System.out.println("txt " + text.length() + " start " + start);
		// // char ch = text.charAt(start);
		// if (!stopChars.contains(text.charAt(start))) {
		// start--;
		// } else {
		// start++;
		// break;
		// }
		// }
		// if (start > pos) {
		// return;
		// }
		//
		// subWord = text.substring(start, pos);
		// if (subWord.length() < 2) {
		// return;
		// }
		//
		// LinkedList<String> searchResult = new LinkedList<String>();
		// subWord = subWord.toLowerCase(); // make sure all small
		// // capitalise first letter
		//// subWord = subWord.substring(0, 1).toUpperCase() +
		// subWord.substring(1);
		// SortedSet<String> res = entries.subSet(subWord, subWord +
		// Character.MAX_VALUE);
		// for(String t : res) {
		// String type = SystemHandler.hasType(t);
		// searchResult.add(type);
		// }
		//// searchResult.addAll();
		// if (entries.size() > 0) {
		// populatePopup(searchResult);
		// if (!entriesPopup.isShowing()) {
		// Bounds position = ((TextAreaSkin)getSkin()).getCaretBounds();
		// entriesPopup.show(AutoCompleteTextArea.this, Side.TOP,
		// position.getMaxX()
		// + 3, position.getMaxY() + 3);
		// }
		// } else {
		// entriesPopup.hide();
		// }
		// }
		// }
		// });
		//
		// focusedProperty().addListener(new ChangeListener<Boolean>() {
		// public void changed(ObservableValue<? extends Boolean>
		// observableValue,
		// Boolean aBoolean, Boolean aBoolean2) {
		// entriesPopup.hide();
		// }
		// });

	}

	public void autoComplete(CodeArea codeArea, String text, String oldText) {

		this.codeArea = codeArea;
		this.text = text;
		this.oldText = oldText;// textArea.getText();
		
		this.pos = codeArea.getCaretPosition();

		// if new longer than older
		// if (text.length() > oldText.length()) {
		// pos++; //= pos + (text.length() - oldText.length());
		// } else {
		// pos--; //= pos - (oldText.length() - text.length());
		// }

		// System.out.println("txt: " + text + " old: " + oldText + " pos: " +
		// pos);

		if (!canAutoComplete()) {
			return;
		}

		// String oldText =textArea.getText();

		// int pos = textArea.getCaretPosition();
		// Bounds bound = ((TextAreaSkin) codeArea.getSkin()).getCaretBounds();
		// Optional<Bounds> bound = codeArea.getCaretBounds();

		int row = codeArea.getCaretPosition();
		int column = codeArea.getCaretColumn();
		// System.out.println("s ["+s + "] s2 [" + s2+"]");
		// text = s2;// getText();
		int start = Math.max(0, pos - 1);

		// System.out.println("caret " + pos + " txt: " + text.length() + "
		// start " + start);
		while (start > 0) {
			// System.out.println("txt " + text.length() + " start " + start);
			// char ch = text.charAt(start);
			if (!stopChars.contains(text.charAt(start))) {
				start--;
			} else {
				start++;
				break;
			}
		}
		if (start > pos) {
			return;
		}

		subWord = text.substring(start, pos);

		if (subWord.length() < lengthForPrediction) {
			entriesPopup.hide();
			return;
		}

		LinkedList<String> searchResult = new LinkedList<String>();
		subWord = subWord.toLowerCase(); // make sure all small
		// capitalise first letter
		// subWord = subWord.substring(0, 1).toUpperCase() +
		// subWord.substring(1);
		SortedSet<String> res = entries.subSet(subWord, subWord + Character.MAX_VALUE);

		// get original entry
		for (String t : res) {
			String type = modifiedOriginalEntriesMap.get(t);
			searchResult.add(type);
		}

		// searchResult.addAll();
		if (searchResult.size() > 0) {

			populatePopup(searchResult);
			if (!entriesPopup.isShowing()) {
				// Bounds position = ((TextAreaSkin)getSkin()).getCaretBounds();
				if (row > 0 && column > 0) {
					entriesPopup.show(codeArea, Side.TOP, row + 3, column + 3);
				} else {
					entriesPopup.show(codeArea, Side.TOP, 0, 0);
				}

			}
		} else {
			entriesPopup.hide();
		}

	}

	public void showAllEntries(CodeArea codeArea) {

		this.pos = codeArea.getCaretPosition();
		this.text = codeArea.getText();
		this.codeArea = codeArea;

		// Optional<Bounds> bound = codeArea.getCaretBounds();
		int row = codeArea.getCaretPosition();
		int column = codeArea.getCaretColumn();

		this.subWord = "";

		populatePopup(Arrays
				.asList(modifiedOriginalEntriesMap.values().toArray(new String[modifiedOriginalEntriesMap.size()])));

		if (!entriesPopup.isShowing()) {
			// Bounds position = ((TextAreaSkin)getSkin()).getCaretBounds();
			if (row > 0 && column > 0) {
				entriesPopup.show(codeArea, Side.TOP, row + 3, column + 3);
			} else {
				entriesPopup.show(codeArea, Side.TOP, 0, 0);
			}

		} else {
			entriesPopup.hide();
		}

	}

	protected boolean canAutoComplete() {

		if (text.length() < lengthForPrediction - 1) {
			entriesPopup.hide();
			return false;
		}

		// if(pos < text.length() && text.charAt(pos) == ' ') {
		// entriesPopup.hide();
		// }
		// check that no new text is copied
		// if ((text.length()>oldText.length() && !text.contains(oldText)) ||
		// (oldText.length()>text.length() && !oldText.contains(text))) {
		// numOfOpenBrackets = 0;
		// return false;
		// }

		// // check if connectivity
		// int index = pos - 1;
		// char lastChar = text.charAt(index);
		//
		// System.out.println(lastChar);
		//
		// //
		// // // if starting a connecitivty then stop auto-complete unitl
		// closing
		// if (lastChar == openConnectivityChar) {
		// numOfOpenBrackets++;
		// openBracketIndex = index;
		// openCloseBracketMap.put(openBracketIndex, -1);
		// isConnectivity = true;
		// return false;
		// }
		// //
		// // // remove connectivity if it is closed
		// if (lastChar == closeConnectivityChar) {
		// numOfOpenBrackets--;
		//
		// List<Integer> openInds = Arrays
		// .asList(openCloseBracketMap.keySet().toArray(new Integer
		// [openCloseBracketMap.size()]));
		// Collections.sort(openInds);
		//
		// for (int i = openInds.size() - 1; i >= 0; i--) {
		//
		// int in = openInds.get(i);
		// System.out.println("in = " + in);
		// // search for an index of open bracket that has no close and less
		// than
		// // the close bracket index
		// if (in < index && openCloseBracketMap.get(in) == -1) {
		// System.out.println("index = " + index);
		// openCloseBracketMap.put(openBracketIndex, index);
		// break;
		// }
		// }
		//
		// isConnectivity = false;
		// return false;
		// }

		//
		// // remove connectivity if there's no open bracket
		//
		// if (!text.contains(openConnectivityChar + "")) {
		// isConnectivity = false;
		// }
		//
		// // if is connectivity then return (no autoComplete)
		// // if (numOfOpenBrackets > 0 ) {
		// // return false;
		// // }
		// for (Entry<Integer, Integer> entry : openCloseBracketMap.entrySet())
		// {
		// int openIndex = entry.getKey();
		// int closeIndex = entry.getValue();
		//
		// // if current position is between the open and close then no
		// autocomplete
		// if (index > openIndex && index < closeIndex)
		// return false;
		// }

		return true;
	}

	public void hidePopup() {
		entriesPopup.hide();
	}

	/**
	 * Get the existing set of autocomplete entries.
	 * 
	 * @return The existing autocomplete entries.
	 */
	public SortedSet<String> getEntries() {
		return entries;
	}

	protected void setEntriesFromSystemTypes() {

		entries = new TreeSet<String>();

		Collection<String> types = SystemMetaModelHelper.getSystemTypes();

		List<String> suggestions = Arrays.asList(types.toArray(new String[types.size()]));

		setEntries(suggestions);
		// for (String type : types) {
		// entries.add(type.toLowerCase());
		// }

	}

	public void setEntries(List<String> allSuggestions) {

		entries = new TreeSet<String>();
		modifiedOriginalEntriesMap = new HashMap<String, String>();

		for (String type : allSuggestions) {
			String newType = type.toLowerCase();
			entries.add(newType);

			// add to the map
			modifiedOriginalEntriesMap.put(newType, type);

		}

	}

	protected void setStopChars() {
		stopChars = new LinkedList<Character>();

		// fill in for any stop chars
		stopChars.add(' ');// space
		stopChars.add(','); // comma
		stopChars.add('.');// containment
		stopChars.add('|'); // connectivity
		stopChars.add('(');
		stopChars.add(')');
		stopChars.add(openConnectivityChar);
		stopChars.add(closeConnectivityChar);
		stopChars.add(newLine);
	}

	/**
	 * Populate the entry set with the given search results. Display is limited
	 * to 10 entries, for performance.
	 * 
	 * @param searchResult
	 *            The set of matching strings.
	 */
	private void populatePopup(List<String> searchResult) {

		List<CustomMenuItem> menuItems = new LinkedList<CustomMenuItem>();

		String separator = " ";
		// If you'd like more entries, modify this line.
		int maxEntries = modifiedOriginalEntriesMap.size();

		int count = Math.min(searchResult.size(), maxEntries);

		for (int i = 0; i < count; i++) {

			final String result = searchResult.get(i);
			Label entryLabel = new Label(result);

			CustomMenuItem item = new CustomMenuItem(entryLabel, true);

			item.setOnAction(new EventHandler<ActionEvent>() {
				public void handle(ActionEvent actionEvent) {

					StringBuilder builder = new StringBuilder();
					// int newIndex = pos - subWord.length();
					// divide into two parts and insert choice
					builder.append(text.substring(0, pos - subWord.length())).append(result).append(separator)
							.append(text.substring(pos, text.length()));

					codeArea.replaceText(0, codeArea.getLength(), builder.toString());
					// codeArea.displaceCaret(pos + (result.length() +
					// separator.length() - subWord.length()));
					// codeArea.positionCaret(pos + (result.length() +
					// separator.length() - subWord.length()));
					entriesPopup.hide();
				}
			});
			menuItems.add(item);
		}
		entriesPopup.getItems().clear();
		entriesPopup.getItems().addAll(menuItems);

	}
}
