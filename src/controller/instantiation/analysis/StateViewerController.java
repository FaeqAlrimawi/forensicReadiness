package controller.instantiation.analysis;

import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class StateViewerController {

	@FXML
	private WebView webView;

	private String svgPath;

	@FXML
	public void initialize() {

		
	}

	public void updateSVGPath(String svgPath) {

		this.svgPath = svgPath;
		final WebEngine eng = webView.getEngine();
//		System.out.println(svgPath);
		eng.load(svgPath);
	}
}
