package io;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Manages audio playback for specific game events (Win/Lose).
 * Uses the standard Java Sound API to play .wav files from the file system.
 */
public class SoundManager {

	private static final String WIN_SOUND_PATH = "src/sounds/win.wav";
	private static final String LOSE_SOUND_PATH = "src/sounds/lose.wav";

	
	public static void playWinSound() {
		playSound(WIN_SOUND_PATH);
	}
	
	
	public static void playLoseSound() {
		playSound(LOSE_SOUND_PATH);
	}

	/**
	 * Internal helper method to load and play an audio file.
	 * Handles standard AudioSystem exceptions.
	 * * @param filePath The relative or absolute path to the .wav file.
	 */
	private static void playSound(String filePath) {
		try {
			File soundFile = new File(filePath);
			
			if (soundFile.exists()) {
				AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundFile);
				Clip clip = AudioSystem.getClip();
				clip.open(audioInput);
				clip.start();
			} else {
				System.err.println("Sound file not found: " + filePath);
			}
			
		} catch (UnsupportedAudioFileException e) {
			System.err.println("Unsupported audio format! Please use .WAV files.");
		} catch (IOException | LineUnavailableException e) {
			e.printStackTrace();
		}
	}
}
