package controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

public class BRSDrawController {

	@FXML
	private AnchorPane drawPane;

	@FXML
	private Button btnCreateCircle;

	double orgSceneX, orgSceneY;
	int radius = 50;
	Line line;
	List<Circle> circles;
	Circle orgCircle;
	private boolean isKeyPressed = false;
	private static final String SYSTEM_IMAGES_PATH = "resources/systemImages/icons32/";

	private List<String> entitiesAdded = new LinkedList<String>();
	private Map<String, String> containment = new HashMap<String, String>();
	private Map<String, ImageView> entityViews = new HashMap<String, ImageView>();
	private ContextMenu contextMenu;
	private DropShadow entitySelectionDropShadow;
	private ImageView selectedEntity;
	
	
	@FXML
	public void initialize() {

		circles = new LinkedList<Circle>();

		drawPane.setOnMouseMoved((m) -> {
			if (line != null) {
				line.setEndX(m.getX());
				line.setEndY(m.getY());
			}
		});

		drawPane.setOnMouseClicked((c) -> {
			if (line != null) {

				double x = c.getSceneX();
				double y = c.getSceneY();
				// check that the x,y is within a circle
				for (ImageView img : entityViews.values()) {
					System.out
							.println(img.getX() + " " + img.getY());
					System.out.println(x + " " + y);
					if (img.contains(x, y)) {
						if (orgCircle != null) {
//							Line li = connect(orgCircle, circle);
//							drawPane.getChildren().add(li);
//							drawPane.getChildren().remove(line);
							System.out.println("image found!" + img);
						}
						break;
					}
				}
			}
		});

		drawPane.setOnKeyPressed((k) -> {

			// drawing line
			if (line != null) {
				drawPane.getChildren().remove(line);
				line = null;
				isKeyPressed = true;
			}
		});
		//
		// drawPane.setOnMouseClicked((e)->{
		// isKeyPressed = true;
		// });
		
		entitySelectionDropShadow = new DropShadow(10, Color.BLUE);
		
		createContextMenu();
	}

	@FXML
	void createEntityShape(ActionEvent event) {

		Circle circle = createCircle(0, 0, radius, Color.BLUE);

		drawPane.getChildren().add(circle);

		drawPane.requestFocus();
	}

	private Circle createCircle(double x, double y, double r, Color color) {
		Circle circle = new Circle(x + radius, y + radius, r, color);
		circles.add(circle);
		circle.setCursor(Cursor.HAND);

		circle.setOnMousePressed((t) -> {
			orgSceneX = t.getSceneX();
			orgSceneY = t.getSceneY();

			Circle c = (Circle) (t.getSource());
			c.toFront();
		});
		circle.setOnMouseDragged((t) -> {
			double offsetX = t.getSceneX() - orgSceneX;
			double offsetY = t.getSceneY() - orgSceneY;

			Circle c = (Circle) (t.getSource());

			c.setCenterX(c.getCenterX() + offsetX);
			c.setCenterY(c.getCenterY() + offsetY);

			orgSceneX = t.getSceneX();
			orgSceneY = t.getSceneY();
		});

		circle.setOnMouseClicked((t) -> {

			line = new Line(t.getX(), t.getY(), t.getX(), t.getY());
			orgCircle = circle;
			// line.startXProperty().bind(circle.centerXProperty());
			// line.startYProperty().bind(circle.centerYProperty());
			//
			// line.endXProperty().bind(circle.centerXProperty());
			// line.endYProperty().bind(circle.centerYProperty());

			drawPane.getChildren().remove(line);
			drawPane.getChildren().add(line);

		});
		return circle;
	}

	private Line connect(Circle c1, Circle c2) {
		Line line = new Line();

		line.startXProperty().bind(c1.centerXProperty());
		line.startYProperty().bind(c1.centerYProperty());

		line.endXProperty().bind(c2.centerXProperty());
		line.endYProperty().bind(c2.centerYProperty());

		line.setStrokeWidth(1);
		line.setStrokeLineCap(StrokeLineCap.BUTT);
		line.getStrokeDashArray().setAll(1.0, 4.0);

		return line;
	}

	@FXML
	void createEntity(ActionEvent event) {

		// get image clicked and create entity based on that
		Button btnClicked = (Button) event.getSource();

		String imgID = btnClicked.getId();

		entitiesAdded.add(imgID);
		//change to image (add .png)
		String imgName = imgID + ".png";
		
//		System.out.println(imgID);
		
		try {
			Image newImage = new Image(
					this.getClass().getClassLoader().getResource(SYSTEM_IMAGES_PATH + imgName).openStream());

			ImageView newImg = new ImageView(newImage);

			newImg.setCursor(Cursor.HAND);

			newImg.setOnMousePressed((t) -> {
				orgSceneX = t.getSceneX();
				orgSceneY = t.getSceneY();

				ImageView c = (ImageView) (t.getSource());
				
				//set border
				if(selectedEntity != null) {
					selectedEntity.setEffect(null);
					c.setEffect(entitySelectionDropShadow);
					selectedEntity = c;
				} else {
					newImg.setEffect(entitySelectionDropShadow);
					selectedEntity = c;
				}
				
				c.toFront();
			});

			newImg.setOnMouseDragged((t) -> {

				double offsetX = t.getSceneX() - orgSceneX;
				double offsetY = t.getSceneY() - orgSceneY;

				ImageView c = (ImageView) (t.getSource());

				c.setX(c.getX() + offsetX);
				c.setY(c.getY() + offsetY);

				orgSceneX = t.getSceneX();
				orgSceneY = t.getSceneY();

			});

			
			newImg.setOnContextMenuRequested((e)-> {
			
				contextMenu.show(newImg, e.getScreenX(), e.getScreenY());
			});
			
//			newImg.setOnMousePressed((e)->{
//				
//				
//			});
			
//			newImg.seton
			drawPane.getChildren().add(newImg);
			entityViews.put(imgID, newImg);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	protected void createContextMenu() {
		
		contextMenu  =new ContextMenu();
		MenuItem itemContainment = new MenuItem("Contains");
		
		itemContainment.setOnAction((e)->{
//			ImageView img = entity
			line = new Line(selectedEntity.getX(), selectedEntity.getY(), selectedEntity.getX(), selectedEntity.getY());
			drawPane.getChildren().remove(line);
			drawPane.getChildren().add(line);
			
		});
		
		contextMenu.getItems().add(itemContainment);
	}

}
