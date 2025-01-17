import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedList;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class Main extends Application{
	/*
	 * Create Class Variables
	 */
	private AnchorPane mainPane;
	private Pane gameField;
	private HBox[] topScoreHBoxes;
	private Character player;
	LinkedList<Bullet> bulletList;
	private Scene scene;
	private AnimationTimer gameLoop;
	private DatabaseDriver databaseDriver;
	private BulletSpawner bulletSpawner;
	private ImageView hellTitle; 
	private ImageView girlTitle;
	private ImageView startTitle;
	private ImageView gameOverTitle;
	private ImageView topScoresTitle;
	private AudioPlayer audioPlayer;
	private Text scoreText;
	private int score;
	private ImageView[] livesImageList;
	private boolean gameOver;
	
	@Override
	public void start(Stage stage) throws URISyntaxException {
		initialize(stage);
		createGameInputs();
		createGameLoop();
		createScoreHBox();
		createLivesHBox();
		createTitles();
		startingTitleTransition();	
		//Play wind for menu screen
		audioPlayer.playWind();
		//Create Thread to monitor if player is still alive to continue
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(!gameOver) {try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}}
				audioPlayer.playGameOver();
				databaseDriver.addScore(score);
				createTopScoresPane();
			}
		}).start();
		
	}
	 
	/*
	 * Initialize variables and wire up panes
	 */
	public void initialize(Stage stage) {
		//Create AnchorPane for root and link to scene then stage
		mainPane = new AnchorPane();
		mainPane.setStyle("-fx-background-color: black;");
		scene = new Scene(mainPane,500, 600);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setTitle("Bullet Hell!");
		stage.show();
		
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				System.exit(0);}
		});
		
		//Create a Pane for our game
		gameField = new Pane();
		gameField.setPrefSize(400, 500);
		mainPane.getChildren().add(gameField);
		gameField.setStyle("-fx-background-color: rgb(230, 96, 221, 0.2);");
		AnchorPane.setTopAnchor(gameField, 30.0);
		AnchorPane.setLeftAnchor(gameField, 50.0);
		
		//Create the player object and add to game pane
		player = new Character(gameField);
		gameField.getChildren().add(player);
		
		//Create our list of bullets the player will have to dodge
		bulletList = new LinkedList<Bullet>();
		gameField.getChildren().addAll(bulletList);
		
		//Create a BulletSpawer to spawn bullets
		bulletSpawner = new BulletSpawner(gameField);
		
		//Create Audio Player for game sounds
		audioPlayer = new AudioPlayer();
		
		//Initialize score to zero
		score = 0;
		
		//Initialize game over boolean
		gameOver = false;
		
		//Initialize database driver 
		databaseDriver = new DatabaseDriver();
	}
	
	/*
	 * Create HBox Pane to house Score Image and Value
	 */
	public void createScoreHBox() {
		HBox scoreHBox = new HBox();
		scoreText = new Text("0");
		scoreText.setFont(new Font("Arial Black", 30));
		scoreText.setFill(Color.DARKMAGENTA);
		try {
			ImageView scoreTitle = new ImageView(new Image(getClass().getResource("images/score.png").toURI().toString()));
			scoreHBox.getChildren().addAll(scoreTitle, scoreText);
	
			mainPane.getChildren().add(scoreHBox);
			AnchorPane.setLeftAnchor(scoreHBox, 50.0);
			AnchorPane.setBottomAnchor(scoreHBox, 17.0);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public void createTopScoresPane(){
		//Create our Text for our actual scores and style them
		int [] topScores = databaseDriver.getTopScores();
		Text[] topScoreTexts = new Text[3];
		for(int i =0; i<3; i++) {
			topScoreTexts[i] = new Text(String.valueOf(topScores[i]));
			topScoreTexts[i].setFont(new Font("Arial Black", 20));
			topScoreTexts[i].setFill(Color.HOTPINK);
		}
		
		//Create our Image and Image Views that will follow the score text
		//We will put both these components in a HBox to group them
		topScoreHBoxes = new HBox[3];
		try {
			Image roundsImage = new Image(getClass().getResource("images/rounds.png").toURI().toString());
			
			//Create a Thread to make UI Changes and Start Game Over Sequence
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					for(int i = 0; i<3; i++) {
						//Add ImageView to our Top Score Hboxes and Put them in place
						ImageView currImageView = new ImageView(roundsImage);
						topScoreHBoxes[i] = new HBox(topScoreTexts[i], currImageView);
						HBox.setMargin(currImageView, new Insets(5, 0, 0, 5));
						topScoreHBoxes[i].setOpacity(0);
						mainPane.getChildren().add(topScoreHBoxes[i]);
						AnchorPane.setTopAnchor(topScoreHBoxes[i], 255+ i*40.0);
						AnchorPane.setLeftAnchor(topScoreHBoxes[i], 185.0);
					}
					//Start Game Over Transition
					gameOverTransition();
					
				}
			});
			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	/*
	 * Change Player Score value on screen
	 */
	public void changeScore(int score) {
		scoreText.setText(String.valueOf(score));
	}
	
	/*
	 * Create HBox Pane for displaying number of lives left
	 */
	public void createLivesHBox() {
		HBox livesHBox = new HBox();
		//Create List of Lives Images to Place in our HBox
		livesImageList = new ImageView[3];
		try {
			Image chibiImage = new Image(getClass().getResource("images/chibi.png").toURI().toString());
			ImageView scoreTitle = new ImageView(new Image(getClass().getResource("images/lives.png").toURI().toString()));
			//Create 3 of the same ImageView for each life
			for(int i =0; i<livesImageList.length; i++) {
				livesImageList[i] = new ImageView(chibiImage);
				HBox.setMargin(livesImageList[i],	new Insets(5, 4, 0, 0));
			}
			livesHBox.getChildren().add(scoreTitle);
			livesHBox.getChildren().addAll(livesImageList);
			mainPane.getChildren().add(livesHBox);
			AnchorPane.setRightAnchor(livesHBox, 50.0);
			AnchorPane.setBottomAnchor(livesHBox, 17.0);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	
	/*
	 * Create all of our ImageView Titles and Place them in our Main window
	 * This Function takes care of positioning, opacities, and Event Handlers.
	 */
	public void createTitles()
	{
		try {
			hellTitle = new ImageView(new Image(getClass().getResource("images/hellTitle.png").toURI().toString()));
			girlTitle = new ImageView(new Image(getClass().getResource("images/girlTitle.png").toURI().toString()));
			startTitle = new ImageView(new Image(getClass().getResource("images/startGlow.png").toURI().toString()));
			gameOverTitle = new ImageView(new Image(getClass().getResource("images/gameOver.png").toURI().toString()));
			topScoresTitle = new ImageView(new Image(getClass().getResource("images/topScores.png").toURI().toString()));
			topScoresTitle.setOpacity(0);
			mainPane.getChildren().addAll(hellTitle, girlTitle, startTitle, gameOverTitle, topScoresTitle);
			AnchorPane.setTopAnchor(hellTitle, 90.0);
			AnchorPane.setLeftAnchor(hellTitle, -950.0);
			AnchorPane.setTopAnchor(girlTitle, 180.0);
			AnchorPane.setLeftAnchor(girlTitle, 1245.0);
			AnchorPane.setTopAnchor(startTitle, 350.0);
			AnchorPane.setLeftAnchor(startTitle, 110.0);
			AnchorPane.setTopAnchor(gameOverTitle, -50.0);
			AnchorPane.setLeftAnchor(gameOverTitle, 70.0);
			AnchorPane.setTopAnchor(topScoresTitle, 220.0);
			AnchorPane.setLeftAnchor(topScoresTitle, 112.0);
			

			/*
			 * Glow Start Title when hovered over
			 */
			startTitle.setOnMouseEntered(ev ->
			{
				startTitle.setEffect(new Glow(0.9));
				audioPlayer.playCursorHover();
			});
			
			startTitle.setOnMouseExited(ev->{
				startTitle.setEffect(null);
			});
			
			/*
			 * When Start Game clicked play selected sounds, Start a Thread to make UI changes
			 * Make Audio Changes, and Start Game Loop
			 */
			startTitle.addEventHandler(MouseEvent.MOUSE_CLICKED, 
					ev->{
						audioPlayer.playSelect();
						startTitle.setVisible(false);
						leavingTitleTransition();
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								audioPlayer.stopWind();
								audioPlayer.playMusic();
								gameLoop.start();
							}
						}).start();
					});
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Create Starting Transition for the Title When Application is opened
	 */
	public void startingTitleTransition() {
		//Create Hell title transitions
		TranslateTransition hellTransition = new TranslateTransition();
		hellTransition.setNode(hellTitle);
		hellTransition.setByX(1000);
		hellTransition.setDuration(Duration.seconds(0.5));
		FadeTransition hellFadeTransition = new FadeTransition(Duration.millis(100));
		hellFadeTransition.setNode(hellTitle);
		hellFadeTransition.setDelay(Duration.seconds(0.1));
		hellFadeTransition.setFromValue(1);
		hellFadeTransition.setToValue(0);
		hellFadeTransition.setAutoReverse(true);
		hellFadeTransition.setCycleCount(10);
		//Create Girl title 
		TranslateTransition girlTransition = new TranslateTransition();
		girlTransition.setNode(girlTitle);
		girlTransition.setByX(-1000);
		girlTransition.setDuration(Duration.seconds(0.5));
		FadeTransition girlFadeTransition = new FadeTransition(Duration.millis(100));
		girlFadeTransition.setNode(girlTitle);
		girlFadeTransition.setDelay(Duration.seconds(0.1));
		girlFadeTransition.setFromValue(1);
		girlFadeTransition.setToValue(0);
		girlFadeTransition.setAutoReverse(true);
		girlFadeTransition.setCycleCount(10);
		
		//Make Transition for Start Game Title
		FadeTransition startTransition = new FadeTransition(Duration.millis(1000), startTitle);
		startTransition.setDelay(Duration.seconds(0.2));
		startTransition.setFromValue(1);
		startTransition.setToValue(0);
		startTransition.setAutoReverse(true);
		startTransition.setCycleCount(10);
		
		//Make Sequential Transitions and play them
		SequentialTransition hellSequen = new SequentialTransition();
		hellSequen.getChildren().addAll(hellTransition, hellFadeTransition);
		SequentialTransition girlSequen = new SequentialTransition();
		girlSequen.getChildren().addAll(girlTransition, girlFadeTransition);
		
		hellSequen.play();
		girlSequen.play();
		startTransition.play();
	}
	
	/*
	 * Create Transition for when game starting
	 */
	public void leavingTitleTransition() {
		TranslateTransition hellTransition = new TranslateTransition();
		hellTransition.setNode(hellTitle);
		hellTransition.setByX(1000);
		hellTransition.setDuration(Duration.seconds(0.5));
		TranslateTransition girlTransition = new TranslateTransition();
		girlTransition.setNode(girlTitle);
		girlTransition.setByX(-1000);
		girlTransition.setDuration(Duration.seconds(0.5));
		hellTransition.play();
		girlTransition.play();
	}
	
	/*
	 * Create Transtitions for when the game has ended
	 */
	public void gameOverTransition() {
		//GameOverTransitions - Game Over Title Descent
		TranslateTransition gameOverTransition0 = new TranslateTransition(Duration.seconds(0.9), gameOverTitle);
		gameOverTransition0.setByY(220);
		TranslateTransition gameOverTransition1 = new TranslateTransition(Duration.seconds(0.2), gameOverTitle);
		gameOverTransition1.setByY(-20);
		TranslateTransition gameOverTransition2 = new TranslateTransition(Duration.seconds(0.4), gameOverTitle);
		gameOverTransition2.setByY(20);
		
		//TopScoreTransitions - Fade in of Top 3 Score Title
		FadeTransition topScoreTransition0 = new FadeTransition(Duration.seconds(0.7), topScoresTitle);
		topScoreTransition0.setByValue(1);
		FadeTransition topScoreTransition1 = new FadeTransition(Duration.seconds(0.5), topScoresTitle);
		topScoreTransition1.setByValue(1);
		topScoreTransition1.setCycleCount(2);
		topScoreTransition1.setAutoReverse(true);
		topScoreTransition1.setToValue(0);
		
		//ScoresSequentialTransition- Fade in each score one at a time
		SequentialTransition scoresSequentialTransition = new SequentialTransition();
		for(int i = 0; i<topScoreHBoxes.length; i++) {
			FadeTransition scoreBoardFadeIn = new FadeTransition(Duration.seconds(0.7), topScoreHBoxes[i]);
			scoreBoardFadeIn.setByValue(1);
			scoreBoardFadeIn.setCycleCount(1);
			scoresSequentialTransition.getChildren().add(scoreBoardFadeIn);
		}
		
		//Set Sequence of Above Transitions
		SequentialTransition gameOverSequenTransition = new SequentialTransition();
		gameOverSequenTransition.getChildren().addAll(gameOverTransition0,gameOverTransition1, gameOverTransition2,
				topScoreTransition0, topScoreTransition1, scoresSequentialTransition);
		gameOverSequenTransition.play();
	}
	
	/*
	 * Remove Life Image from Screen
	 */
	public void removeLife() {
		int lives = player.getLives();
		if(lives>=0) {
			livesImageList[lives].setVisible(false);	
		}
	}
	
	/*
	 * Create our inputs to move the Player 
	 */
	public void createGameInputs() {
		//Move player in direction based on what key pressed
		scene.addEventHandler(KeyEvent.KEY_PRESSED, 
				ev->{
					KeyCode code = ev.getCode();
					if(code==KeyCode.UP || code==KeyCode.W) {
						player.moveUp();
					}
					if(code==KeyCode.DOWN || code==KeyCode.S) {
						player.moveDown();
					}
					if(code==KeyCode.LEFT || code==KeyCode.A) {
						player.moveLeft();
					}
					if(code==KeyCode.RIGHT|| code==KeyCode.D) {
						player.moveRight();
					}
				}
		);
		
		//When keys are released stop its directional movement
		scene.addEventHandler(KeyEvent.KEY_RELEASED, 
				ev->{
					KeyCode code = ev.getCode();
					if(code==KeyCode.UP || code==KeyCode.W) {
						player.stopYMovement();
					}
					if(code==KeyCode.DOWN || code==KeyCode.S) {
						player.stopYMovement();
					}
					if(code==KeyCode.LEFT || code==KeyCode.A) {
						player.stopXMovement();
					}
					if(code==KeyCode.RIGHT || code==KeyCode.D) {
						player.stopXMovement();
					}
				});
	}
	
	/*
	 * Remove all bullets from field if player hit or game over
	 */
	public void removeAllBullets() {
		gameField.getChildren().removeAll(bulletList);
		bulletList.clear();
	}
	
	/*
	 * Create Loop that will run game
	 */
	public void createGameLoop() {
		//Create new Animation Timer 
		gameLoop = new AnimationTimer() {
			//Create variables to keep track of time so we don't spawn to often
		    private long spawnTimer = 1000 * 1_000_000;
		    private long prevTime = 0;
			@Override
			public void handle(long now) {
				/*
				 * Update player and bullet sprites by calling their update methods
				 */
				player.update();
				Iterator<Bullet> bulletIterator = bulletList.iterator();
				
				//Iterate over bullet list
				while(bulletIterator.hasNext()) {
					Bullet bullet = bulletIterator.next();
					//If Bullet out of bounds remove it
					if(bullet.isOutOfBounds()) {
						gameField.getChildren().remove(bullet);
						bulletIterator.remove();
					}
					else {
						//Move Bullet
						bullet.update();
						/*
						 * If bullet hits play take away a life, add temporary invincibility, 
						 * and stop iterating over bullets
						 */
						if(bullet.getHitBoxBounds().intersects(player.getHitBoxBounds()) && !player.isInvincible()) {
							audioPlayer.playDeath();
							player.addInvincibility();
							player.loseLife();
							removeLife();
							removeAllBullets();
							if(player.isDead()) {
								gameOver = true;
								this.stop();
							}
							break;
						}	
					}
				}
				//If player invisible remove it and do not spawn bullets
				if(player.isInvincible()) {
					player.removeInvincibility();
				}
				//Spawn bullets if timer allows it
				else if (now-prevTime > spawnTimer) {
					LinkedList<Bullet> newBullets = bulletSpawner.spawnRandomBullets();
					bulletList.addAll(newBullets);
					gameField.getChildren().addAll(newBullets);
					newBullets.clear();
					prevTime = now;
					changeScore(++score);
				}
				
			}
		};
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
