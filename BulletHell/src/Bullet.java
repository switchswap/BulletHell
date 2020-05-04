import java.net.URISyntaxException;

import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Bullet extends Pane {
	
	private Image bulletImage;
	private ImageView bulletImageView;
	private Circle hitCircle;
	private boolean outOfBounds;
	private double x;
	private double y;
	private double xVelocity;
	private double yVelocity;
	private double gamePaneWidth;
	private double gamePaneHeight;
	private Pane gamePane;
	
	public Bullet(Pane gameField, double xTrajectory, double yTrajectory) {
		gamePane = gameField;
		randomGenerateSprite();
		hitCircle = new Circle(4, Color.TRANSPARENT);
		this.getChildren().add(hitCircle);
		
		x = 50.0;
		y = 50.0;
		xVelocity = xTrajectory;
		yVelocity = yTrajectory;
		refreshLocation();
		
		gamePaneHeight = gameField.getPrefHeight();
		gamePaneWidth = gameField.getPrefWidth();
		outOfBounds = false;
	}
	
	public void update() {
		x += xVelocity;
		y += yVelocity;
		if(x>gamePaneWidth-hitCircle.getRadius()) {
			outOfBounds = true;
			this.getChildren().remove(bulletImageView);
		}
		else if(y>gamePaneHeight-hitCircle.getRadius()) {
			outOfBounds = true;
			this.getChildren().remove(bulletImageView);
		}
		refreshLocation();
	}
	
	public void randomGenerateSprite() {
		double randomNumber = Math.random()*100;
		if(randomNumber < 14) {
			createImageView("blueBullet.png");
		}
		else if(randomNumber < 28) {
			createImageView("cyanBullet.png");
		}
		else if(randomNumber < 42) {
			createImageView("greenBullet.png");
		}
		else if(randomNumber < 56) {
			createImageView("orangeBullet.png");
		}
		else if(randomNumber < 70) {
			createImageView("redBullet.png");
		}
		else if(randomNumber < 84) {
			createImageView("yellowBullet.png");
		}
		else if(randomNumber < 98) {
			createImageView("whiteBullet.png");
		}
		
	}
	
	public void createImageView(String filename) {
		try {
			bulletImage= new Image(getClass().getResource(filename).toURI().toString());
			bulletImageView = new ImageView(bulletImage);
			this.getChildren().add(bulletImageView);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public void refreshLocation() {
		this.relocate(x, y);
        hitCircle.setCenterX(bulletImageView.getImage().getWidth()/2);
        hitCircle.setCenterY(bulletImageView.getImage().getHeight()/2);
	}
	
	public Bounds getHitBoxBounds() {
		return gamePane.sceneToLocal(this.localToScene(hitCircle.getBoundsInLocal()));
	}
}
