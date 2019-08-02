package controller.utlities;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import core.SystemMetaModelHelper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.input.KeyCode;

public class ConditionAreaHandler {

	private static Collection<String> systemTypes = SystemMetaModelHelper.getSystemTypes();
	private static String[] SYSTEM_KEYWORDS = systemTypes.toArray(new String[systemTypes.size()]);

	private static final String[] BIGRAPHER_KEYWORDS = new String[] { "id"

	};

	private static final String SYSTEM_KEYWORD_PATTERN = "\\b(" + String.join("|", SYSTEM_KEYWORDS) + ")\\b";
	private static final String BIGRAPHER_KEYWORD_PATTERN = "\\b(" + String.join("|", BIGRAPHER_KEYWORDS) + ")\\b";
	private static final String PAREN_PATTERN = "\\(|\\)";
	private static final String BRACE_PATTERN = "\\{|\\}";
	private static final String BRACKET_PATTERN = "\\[|\\]";
	private static final String SEMICOLON_PATTERN = "\\;";
	private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
	private static final String COMMENT_PATTERN = "#[^\n]*";// + "|" +
															// "/\\*(.|\\R)*?\\*/";

	private static final Pattern PATTERN = Pattern.compile("(?<SYSTEMKEYWORD>" + SYSTEM_KEYWORD_PATTERN + ")"
			+ "|(?<BIGRAPHERKEYWORD>" + BIGRAPHER_KEYWORD_PATTERN + ")" + "|(?<PAREN>" + PAREN_PATTERN + ")"
			+ "|(?<BRACE>" + BRACE_PATTERN + ")" + "|(?<BRACKET>" + BRACKET_PATTERN + ")" + "|(?<SEMICOLON>"
			+ SEMICOLON_PATTERN + ")" + "|(?<STRING>" + STRING_PATTERN + ")" + "|(?<COMMENT>" + COMMENT_PATTERN + ")");

	CodeArea codeArea;
	Subscription cleanupWhenNoLongerNeedIt;
	AutoCompleteCodeArea autoComplete;

	public CodeArea createCodeArea() {

		codeArea = new CodeArea();
		autoComplete = new AutoCompleteCodeArea();

		// add line numbers to the left of area
//		codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

		// recompute the syntax highlighting 500 ms after user stops editing
		// area
		cleanupWhenNoLongerNeedIt = codeArea

				// plain changes = ignore style changes that are emitted when
				// syntax highlighting is reapplied
				// multi plain changes = save computation by not rerunning the
				// code multiple times
				// when making multiple changes (e.g. renaming a method at
				// multiple parts in file)
				.multiPlainChanges()

				// do not emit an event until 500 ms have passed since the last
				// emission of previous stream
				.successionEnds(Duration.ofMillis(500))

				// run the following code block when previous stream emits an
				// event
				.subscribe(ignore -> codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText())));

		// autocomplete
		codeArea.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// TODO Auto-generated method stub
				autoComplete.autoComplete(codeArea, newValue, oldValue);
			}
		});

		codeArea.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				// TODO Auto-generated method stub
				autoComplete.hidePopup();
			}
		});

		// show all enteries when space-ctrl is pressed
		codeArea.setOnKeyPressed(e -> {

			if (e.getCode() == KeyCode.SPACE && e.isControlDown()) {
				autoComplete.showAllEntries(codeArea);
			}
		});

		codeArea.setStyle("-fx-font-size: 14px;");
		
		return codeArea;
	}

	private static StyleSpans<Collection<String>> computeHighlighting(String text) {
		Matcher matcher = PATTERN.matcher(text);
		int lastKwEnd = 0;
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		while (matcher.find()) {
			String styleClass = matcher.group("SYSTEMKEYWORD") != null ? "keyword"
					: matcher.group("BIGRAPHERKEYWORD") != null ? "bigrapher"
							: matcher.group("PAREN") != null ? "paren"
									: matcher.group("BRACE") != null ? "brace"
											: matcher.group("BRACKET") != null ? "bracket"
													: matcher.group("SEMICOLON") != null ? "semicolon"
															: matcher.group("STRING") != null ? "string"
																	: matcher.group("COMMENT") != null ? "comment"
																			: null;
			/* never happens */ assert styleClass != null;
			spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
			spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
			lastKwEnd = matcher.end();
		}
		spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
		return spansBuilder.create();
	}

	public String getText() {

		if (codeArea != null) {
			return codeArea.getText();
		}

		return null;
	}

	public void setText(String text) {

		if (codeArea != null) {
			codeArea.replaceText(0, codeArea.getLength(), text);
		}
	}

	public boolean stopHighlighting() {

		// when no longer need syntax highlighting and wish to clean up memory
		// leaks
		// run: `cleanupWhenNoLongerNeedIt.unsubscribe();`
		cleanupWhenNoLongerNeedIt.unsubscribe();

		return true;
	}

	public boolean startHighlighting() {

		cleanupWhenNoLongerNeedIt = codeArea

				// plain changes = ignore style changes that are emitted when
				// syntax highlighting is reapplied
				// multi plain changes = save computation by not rerunning the
				// code multiple times
				// when making multiple changes (e.g. renaming a method at
				// multiple parts in file)
				.multiPlainChanges()

				// do not emit an event until 500 ms have passed since the last
				// emission of previous stream
				.successionEnds(Duration.ofMillis(500))

				// run the following code block when previous stream emits an
				// event
				.subscribe(ignore -> codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText())));

		// codeArea.replaceText(0,0,codeArea.getText());

		return true;
	}

}
