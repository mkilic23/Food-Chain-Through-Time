package io;

import logic.GameEngine;
import logic.Grid;
import model.Entity;
import model.Food;
import model.animals.Animal;

import java.io.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all file input/output operations for the game.
 * Responsibilities include loading configuration files (food chains)
 * and managing the Save/Load game functionality.
 */
public class FileManager {

	private static final String SAVE_FILE = "savegame.txt";


	/**
	 * Loads the ecosystem names (food chain) from a text file based on the game mode.
	 * The file is expected to be in the classpath (e.g., /past.txt).
	 * * @param mode The game era (Past, Present, Future).
	 * @return A String array containing {Apex, Predator, Prey, Food}.
	 * @throws IOException If the file is missing or the format is invalid.
	 */
	public static String[] loadFoodChainNames(String mode) throws IOException {
		String fileName = mode.toLowerCase() + ".txt"; // e.g., past.txt

		InputStream is = FileManager.class.getResourceAsStream("/" + fileName);
		if (is == null) {
			
			throw new IOException("File not found: /" + fileName + " (Must be in classpath)");
		}

		List<String> chains = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("Food Chain")) {
					chains.add(line);
				}
			}
		}

		if (chains.isEmpty()) {
			throw new IOException("No 'Food Chain' lines found in: " + fileName);
		}

		SecureRandom rnd = new SecureRandom();
		String selected = chains.get(rnd.nextInt(chains.size()));

		int colon = selected.indexOf(':');
		if (colon < 0) {
			throw new IOException("Invalid format (missing ':'): " + selected);
		}

		String payload = selected.substring(colon + 1).trim(); 
		String[] parts = payload.split(",");

		if (parts.length != 4) {
			throw new IOException("Expected 4 names (Apex,Predator,Prey,Food), found: " + selected);
		}

		return new String[]{
			parts[0].trim(), 
			parts[1].trim(), 
			parts[2].trim(), 
			parts[3].trim()  
		};
	}


	/**
	 * Saves the current game state to a local file (savegame.txt).
	 * Serializes metadata (Round, Mode, GridSize) and all entities.
	 * * @param engine The GameEngine instance containing the current state.
	 */
	public static void saveGame(GameEngine engine) {
		try (PrintWriter out = new PrintWriter(new FileWriter(SAVE_FILE))) {
			
			out.println("MODE:" + engine.getCurrentMode());
			out.println("GRID_SIZE:" + engine.getGrid().getSize());
			out.println("ROUND:" + engine.getCurrentRound());
			out.println("MAX_ROUNDS:" + engine.getMaxRounds());

			List<Entity> entities = engine.getGrid().getEntities();
			
			for (Entity e : entities) {
				if (e instanceof Animal) {
					Animal a = (Animal) e;
					out.println(String.format("ENTITY:%s,%s,%d,%d,%d,%d",
							a.getType(), a.getName(), a.getX(), a.getY(), a.getScore(), a.getAbilityCooldown()));
				} 
				else if (e instanceof Food) {
					Food f = (Food) e;
					out.println(String.format("ENTITY:FOOD,%s,%d,%d", f.getName(), f.getX(), f.getY()));
				}
			}
			
			out.flush();
			System.out.println("Game saved successfully.");
			
		} catch (IOException e) {
			System.err.println("Save failed: " + e.getMessage());
		}
	}


	/**
	 * Loads the game state from the save file.
	 * Reconstructs the GameEngine, Grid, and all Entities.
	 * Includes logic to handle dynamic Grid Sizes.
	 * * @return A fully restored GameEngine instance.
	 * @throws IOException If the file is missing or unreadable.
	 */
	public static GameEngine loadGame() throws IOException {
		File file = new File(SAVE_FILE);
		if (!file.exists()) {
			throw new FileNotFoundException("Save file not found.");
		}

		String mode = "Present"; 
		int round = 0;
		int maxRounds = 30;
		int gridSize = 20; 
		List<String> entityLines = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				
				if (line.startsWith("MODE:")) {
					mode = line.split(":")[1].trim();
				} 
				else if (line.startsWith("ROUND:")) {
					round = Integer.parseInt(line.split(":")[1].trim());
				} 
				else if (line.startsWith("MAX_ROUNDS:")) {
					maxRounds = Integer.parseInt(line.split(":")[1].trim());
				} 
				else if (line.startsWith("GRID_SIZE:")) {
					gridSize = Integer.parseInt(line.split(":")[1].trim());
				}
				else if (line.startsWith("ENTITY:")) {
					entityLines.add(line.substring(7)); 
				}
			}
		}

		GameEngine engine = new GameEngine(gridSize, maxRounds, mode);
		engine.setCurrentRound(round);
		
		engine.clearAllEntities(); 

		Grid grid = engine.getGrid();

		for (String data : entityLines) {
			try {
				String[] parts = data.split(",");
				
				String type = parts[0];
				String name = parts[1];
				int x = Integer.parseInt(parts[2]);
				int y = Integer.parseInt(parts[3]);

				if (type.equals("FOOD")) {
					grid.placeEntity(new Food(x, y, name), x, y);
				} else {
					int score = Integer.parseInt(parts[4]);
					int cooldown = Integer.parseInt(parts[5]);
					
					Animal animal = new Animal(name, type, mode, x, y);
					animal.addScore(score);
					animal.setCooldown(cooldown); 

					grid.placeEntity(animal, x, y);
					
					engine.addLoadedAnimal(animal); 
				}
			} catch (Exception e) {
				System.err.println("Error parsing entity line: " + data);
			}
		}
		
		return engine;
	}

	/**
	 * Checks if a valid save file exists.
	 * Used by the GUI to enable/disable the "Load Game" button.
	 * * @return true if "savegame.txt" exists and is not empty.
	 */
	public static boolean isSaveFileAvailable() {
		File file = new File(SAVE_FILE);
		return file.exists() && file.length() > 0;
	}
}