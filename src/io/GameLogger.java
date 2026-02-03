package io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handles the logging of game events to a file.
 * Creates a timestamped record of actions in "game_log.txt".
 */
public class GameLogger {

	private static BufferedWriter writer;
	private static boolean isInitialized = false;

	/**
	 * Initializes the log file.
	 * Creates (or overwrites) "game_log.txt" to start with a clean slate.
	 */
	public static void init() {
		try {
			writer = new BufferedWriter(new FileWriter("game_log.txt"));
			isInitialized = true;
		} catch (IOException e) {
			System.err.println("Failed to create log file: " + e.getMessage());
		}
	}

	/**
	 * Writes a message to the log file with a timestamp.
	 * IMPORTANT: Flushes the stream immediately to ensure data is written to disk.
	 * * @param message The text content to record in the log.
	 */
	public static void log(String message) {
		if (!isInitialized) {
			System.out.println("[Log (No File)]: " + message);
			return;
		}

		try {
			String timestamp = getCurrentTime();
			String formattedMessage = String.format("[%s] %s", timestamp, message);

			writer.write(formattedMessage);
			writer.newLine();
			
			writer.flush(); 

		} catch (IOException e) {
			System.err.println("Error writing to log: " + e.getMessage());
		}
	}

	/**
	 * Closes the log file securely when the game ends.
	 * Prevents resource leaks.
	 */
	public static void close() {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Helper method to get the current system time.
	 * * @return Current time formatted as "HH:mm:ss".
	 */
	private static String getCurrentTime() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
		return LocalDateTime.now().format(dtf);
	}
}