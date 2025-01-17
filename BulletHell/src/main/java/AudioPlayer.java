import javafx.scene.media.AudioClip;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

public class AudioPlayer {
	private AudioClip cursorHover;
	private AudioClip gameMusic;
	private AudioClip wind;
	private AudioClip death;
	private AudioClip select;
	
	public AudioPlayer() {
		cursorHover = new AudioClip(getClass().getResource("sounds/cursorHover.wav").toString());
		gameMusic = new AudioClip(getClass().getResource("sounds/gameMusic.wav").toString());
		wind = new AudioClip(getClass().getResource("sounds/wind.wav").toString());
		death = new AudioClip(getClass().getResource("sounds/death.wav").toString());
		select = new AudioClip(getClass().getResource("sounds/select.wav").toString());
		cursorHover.setRate(2);
		death.setRate(2);
	}

	public void playCursorHover() {
		cursorHover.play();
	}
	
	public void playWind() {
		wind.setCycleCount(AudioClip.INDEFINITE);
		wind.play();
	}
	
	public void stopWind() {
		if(wind.isPlaying()) {
			wind.stop();
		}
	}
	
	public void playMusic() {
		gameMusic.setCycleCount(AudioClip.INDEFINITE);
		gameMusic.setRate(1);
		gameMusic.play();
	}
	
	public void stopMusic() {
		if(gameMusic.isPlaying()) {
			gameMusic.stop();
		}
	}
	
	public void playDeath() {
		death.play();
	}
	
	public void playSelect() {
		select.play();
	}
	
	public void playGameOver() {
		if(gameMusic.isPlaying()) {
			gameMusic.stop();
		}
		gameMusic.setCycleCount(AudioClip.INDEFINITE);
		gameMusic.setRate(0.5);
		gameMusic.play();
	}
}
