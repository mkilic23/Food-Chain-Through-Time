package logic;

import java.util.Collections;
import java.awt.Point;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import exceptions.InvalidMoveException;
import io.FileManager;
import io.GameLogger;
import model.Entity;
import model.Food;
import model.animals.Animal;

/**
 * The core controller of the game.
 * This class manages the game loop, turn order, rule enforcement, AI triggering,
 * and interactions between entities (eating, moving, scoring).
 * * Task: Orchestrates the game flow from initialization to game over.
 */
public class GameEngine {

	private Grid grid;
	private int currentRound;
	private int maxRounds;
	private String currentMode; 
	private boolean isGameOver;

	private List<Animal> animals;
	
	private Animal player;
	private Animal apex;
	private Animal prey;

	private final SecureRandom random = new SecureRandom();

	/**
	 * Initializes the engine and sets up the game board.
	 * * @param gridSize  The dimension of the square grid (e.g., 5 for 5x5).
	 * @param maxRounds The total number of rounds before the game ends.
	 * @param mode      The selected Era ("Past", "Present", "Future").
	 */
	public GameEngine(int gridSize, int maxRounds, String mode) {
		GameLogger.init(); 
		
		this.currentRound = 0;
		this.maxRounds = maxRounds;
		this.currentMode = mode;
		this.isGameOver = false;
		
		this.grid = new Grid(gridSize);
		this.animals = new ArrayList<>();
		
		initializeGame();
	}

	/**
	 * Loads entity names from files, creates objects, and places them on the grid.
	 * Also handles the initial logging and the first move of the Prey AI.
	 */
	public void initializeGame() {
		try {
			String[] names = FileManager.loadFoodChainNames(currentMode);
			String apexName = names[0];
			String predatorName = names[1];
			String preyName = names[2];
			String foodName = names[3];

			System.out.println("MODE=" + currentMode);
			System.out.println("Loaded names: " + apexName + ", " + predatorName + ", " + preyName + ", " + foodName);

			apex = new Animal(apexName, "Apex", currentMode, 0, 0);
			player = new Animal(predatorName, "Predator", currentMode, 0, 0);
			prey = new Animal(preyName, "Prey", currentMode, 0, 0);

			animals.clear();
			animals.add(apex);
			animals.add(player);
			animals.add(prey);

			spawnEntityRandomly(apex);
			spawnEntityRandomly(player);
			spawnEntityRandomly(prey);

			Food food = new Food(0, 0, foodName);
			spawnEntityRandomly(food);
			
			GameLogger.log(String.format(
				"GAME_START era=%s totalRounds=%d playerRole=%s",
				currentMode, maxRounds, (player != null ? player.getType() : "N/A")
			));

			if (player != null && apex != null && prey != null) {
				GameLogger.log(String.format(
					"SPAWN player=%s(%s) (x=%d,y=%d)- apex=%s(%s) (x=%d,y=%d)- prey=%s(%s) (x=%d,y=%d)",
					player.getName(), player.getType(), player.getX(), player.getY(),
					apex.getName(), apex.getType(), apex.getX(), apex.getY(),
					prey.getName(), prey.getType(), prey.getX(), prey.getY()
				));
			}
			
			GameLogger.log(String.format(
				"ROUND_BEGIN r=%d/%d era=%s playerRole=%s",
				currentRound, maxRounds, currentMode, (player != null ? player.getType() : "N/A")
			));

			performAiMove(prey, AIController.getNextMoveForPrey(prey, grid));

		} catch (IOException e) {
			System.err.println("Game cannot started: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Processes the player's click as a full round with fixed turn order:
	 * Prey (AI)  Player  Apex (AI) End Round.
	 * * Validates rules including boundaries, cooldowns, and era-specific restrictions.
	 * * @param targetX The target X coordinate on the grid.
	 * @param targetY The target Y coordinate on the grid.
	 * @throws InvalidMoveException If the move is illegal (out of bounds, cooldown active, etc.).
	 */
	public void processPlayerMove(int targetX, int targetY) throws InvalidMoveException {
		if (isGameOver) return;

		if (!grid.isValidPosition(targetX, targetY)) {
			throw new InvalidMoveException("You cannot go beyond the map boundaries!");
		}

		Point target = new Point(targetX, targetY);
		int fromX = player.getX();
		int fromY = player.getY();

		int moveType = player.checkMoveType(target);
		if (moveType == 0) {
			throw new InvalidMoveException("Invalid Move! (Out of range or Ability on cooldown)");
		}

		boolean isPresentPredator = currentMode.equals("Present") && player.getType().equals("Predator");
		if (isPresentPredator && moveType == 2) {
			if (!isAdjacent(player, apex)) {
				throw new InvalidMoveException("This ability can only be used when near the Apex!");
			}
		}

		boolean canEatFood = true;
		if (currentMode.equals("Future") && player.getType().equals("Prey") && moveType == 2) {
			canEatFood = false;
		}

		if (moveType == 3) {
			GameLogger.log(player.getName() + " stayed same location.");
			GameLogger.log(String.format("MOVE PLAYER actor=%s(%s) from=(%d,%d) to=(%d,%d)", 
					player.getName(), player.getType(), player.getX(), player.getY(), player.getX(), player.getY()));
		} 
		else {
			Cell targetCell = grid.getCell(targetX, targetY);
			
			if (!canEatFood && targetCell != null && !targetCell.isEmpty() && targetCell.getEntity() instanceof Food) {
				throw new InvalidMoveException("Food cannot be consumed while this special ability is active!");
			}

			if (isPresentPredator && moveType == 2) {
				executePresentPredatorDash(targetX, targetY);
				GameLogger.log(String.format(
					"MOVE PLAYER actor=%s(%s) from=(%d,%d) to=(%d,%d) ",
					player.getName(), player.getType(), fromX, fromY, targetX, targetY
				));
			} else {
				moveActor(player, targetX, targetY);
				GameLogger.log(String.format(
					"MOVE PLAYER actor=%s(%s) from=(%d,%d) to=(%d,%d) ",
					player.getName(), player.getType(), fromX, fromY, targetX, targetY
				));

				if (moveType == 2) {
					player.triggerAbilityCooldown();
					GameLogger.log(player.getName() + " used special ability (" + player.getAbilityName() + ")!");
				}
			}
		}

		performAiMove(apex, AIController.getNextMoveForApex(apex, grid));

		endRound();
	}

	/**
	 * Execute movement logic for AI controlled entities.
	 * Handles ability usage detection and cooldowns automatically.
	 * * @param actor      The AI animal (Apex or Prey) that is moving.
	 * @param moveCoords An integer array {x, y} representing the target coordinates.
	 */
	private void performAiMove(Animal actor, int[] moveCoords) {
		if (!actor.isAlive()) return;
		
		int fromX = actor.getX();
		int fromY = actor.getY();
		int targetX = moveCoords[0];
		int targetY = moveCoords[1];
		
		Point target = new Point(targetX, targetY);
		int moveType = actor.checkMoveType(target);

		if (moveType == 3) {
			GameLogger.log(String.format(
				"MOVE AI actor=%s(%s) from=(%d,%d) to=(%d,%d) ",
				actor.getName(), actor.getType(), fromX, fromY, fromX, fromY
			));
			return;
		}

		if (moveType == 2 && currentMode.equals("Future") && actor.getType().equals("Prey")) {
			Cell tc = grid.getCell(targetX, targetY);
			if (tc != null && !tc.isEmpty() && tc.getEntity() instanceof Food) {
				return;
			}
		}

		moveActor(actor, targetX, targetY);
		
		GameLogger.log(String.format(
			"MOVE AI actor=%s(%s) from=(%d,%d) to=(%d,%d) ",
			actor.getName(), actor.getType(), fromX, fromY, targetX, targetY 
		));

		if (moveType == 2) {
			actor.triggerAbilityCooldown();
			GameLogger.log(actor.getName() + " used " + actor.getAbilityName());
		}
	}

	/**
	 * Low-level method to update entity position on the grid.
	 * Handles the "Eating" logic if the target cell is occupied.
	 * * @param actor   The animal performing the move.
	 * @param targetX The target X coordinate.
	 * @param targetY The target Y coordinate.
	 */
	private void moveActor(Animal actor, int targetX, int targetY) {
		Cell targetCell = grid.getCell(targetX, targetY);
		
		if (!targetCell.isEmpty()) {
			Entity victim = targetCell.getEntity();
			
			if (actor.canEat(victim)) {
				handleEating(actor, victim);
				
				grid.moveEntity(actor, targetX, targetY);
				actor.setPosition(new Point(targetX, targetY));
			} 
		} else {
			grid.moveEntity(actor, targetX, targetY);
			actor.setPosition(new Point(targetX, targetY));
		}
	}

	/**
	 * Handles the scoring, logging, and respawning when an entity is consumed.
	 * * @param attacker The animal that is eating.
	 * @param victim   The entity (Animal or Food) being eaten.
	 */
	private void handleEating(Animal attacker, Entity victim) {
		String attType = attacker.getType();

		if (attType.equals("Prey") && victim instanceof Food) {
			attacker.addScore(3);
			GameLogger.log(String.format(
				"SCORE_GAIN %s(%s) gain 3 points reason:EAT_FOOD",
				attacker.getName(), attacker.getType()
			));
		}
		
		else if (attType.equals("Predator") && victim instanceof Animal) {
			Animal vAnimal = (Animal) victim;
			attacker.addScore(3);
			
			GameLogger.log(String.format(
				"SCORE_GAIN %s(%s) gain 3 points reason:PREDATOR_EATS_PREY",
				attacker.getName(), attacker.getType()
			));
			
			vAnimal.addScore(-1);
			GameLogger.log(String.format(
				"SCORE_LOSS %s(%s) loss 1 point reason:BE_EATEN",
				vAnimal.getName(), vAnimal.getType()
			));
		}
		
		else if (attType.equals("Apex") && victim instanceof Animal) {
			Animal vAnimal = (Animal) victim;
			attacker.addScore(1);
			
			GameLogger.log(String.format(
				"SCORE_GAIN %s(%s) gains 1 point reason:APEX_EATS_ANIMAL",
				attacker.getName(), attacker.getType()
			));

			vAnimal.addScore(-1);
			GameLogger.log(String.format(
				"SCORE_LOSS %s(%s) loss 1 point reason:BE_EATEN",
				vAnimal.getName(), vAnimal.getType()
			));
		}

		grid.removeEntity(victim);

		if (victim instanceof Animal) {
			Animal victimAnimal = (Animal) victim;
			victimAnimal.die();
			spawnEntityRandomly(victimAnimal);
			GameLogger.log(victimAnimal.getName() + " respawns");
		} 
		else if (victim instanceof Food) {
			Food eatenFood = (Food) victim;
			spawnEntityRandomly(new Food(0, 0, eatenFood.getName()));
			GameLogger.log(eatenFood.getName() + " respawns");
		}
	}

	/**
	 * Finds a random empty cell on the grid and places the entity there.
	 * * @param e The entity to spawn.
	 */
	private void spawnEntityRandomly(Entity e) {
		int size = grid.getSize();
		int x, y;
		
		do {
			x = random.nextInt(size);
			y = random.nextInt(size);
		} while (!grid.getCell(x, y).isEmpty()); 
		
		grid.placeEntity(e, x, y);
		
		if (e instanceof Animal) {
			Animal animal = (Animal) e;
			animal.setPosition(new Point(x, y));
			animal.respawn(x, y); 
		}
	}

	private void checkGameOver() {
		if (currentRound >= maxRounds) {
			isGameOver = true;
			String winner = getWinner();
			
			GameLogger.log(String.format(
				"GAME_OVER era=%s totalRounds=%d winner=%s",
				currentMode, maxRounds, winner
			));

			if (winner.equals(player.getName())) {
				io.SoundManager.playWinSound();
			} else {
				io.SoundManager.playLoseSound();
			}
		}
	}

	public String getWinner() {
		if (!player.isAlive()) {
			return apex.getName() + " (Player Eliminated)";
		}

		int pScore = player.getScore();
		int aScore = apex.getScore();
		int prScore = prey.getScore();

		if (pScore > aScore && pScore > prScore) return player.getName();
		if (aScore > pScore && aScore > prScore) return apex.getName();
		if (prScore > pScore && prScore > aScore) return prey.getName();
		
		return "Draw";
	}

	/**
	 * Finalizes the current round, reduces cooldowns, and prepares the next round.
	 */
	private void endRound() {
		GameLogger.log(String.format(
			"ROUND_END r=%d/%d era=%s scores: player=%d apex=%d prey=%d",
			currentRound, maxRounds, currentMode,
			player.getScore(),
			apex.getScore(),
			prey.getScore()
		));
		
		if (player.isAlive()) player.reduceCooldown();
		if (apex.isAlive()) apex.reduceCooldown();
		if (prey.isAlive()) prey.reduceCooldown();
		
		currentRound++;
		checkGameOver();
	  
		if (!isGameOver) {
			GameLogger.log(String.format(
				"ROUND_BEGIN r=%d/%d era=%s playerRole=%s",
				currentRound, maxRounds, currentMode,
				(player != null ? player.getType() : "N/A")
			));

			performAiMove(prey, AIController.getNextMoveForPrey(prey, grid));
		}
	}

	public void clearAllEntities() {
		this.grid.clear(); 
		if (this.animals != null) {
			this.animals.clear();
		}
		this.player = null;
		this.apex = null;
		this.prey = null;
	}

	/**
	 * Returns valid targets for normal movement (Walk).
	 * Used by the UI to highlight cells.
	 * * @return List of Points for valid walk destinations.
	 */
	public List<Point> getPlayerNormalMoveTargets() {
		if (isGameOver || player == null || !player.isAlive()) return Collections.emptyList();

		List<Point> targets = new ArrayList<>();
		int cx = player.getX();
		int cy = player.getY();

		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				if (dx == 0 && dy == 0) continue;

				int tx = cx + dx;
				int ty = cy + dy;

				if (!grid.isValidPosition(tx, ty)) continue;

				int moveType = player.checkMoveType(new Point(tx, ty));
				if (moveType != 1) continue;

				if (isCellMovableFor(player, tx, ty, false)) {
					targets.add(new Point(tx, ty));
				}
			}
		}
		return targets;
	}

	/**
	 * Returns valid targets for special ability movement.
	 * Checks geometry, cooldowns, and engine-specific rules (like adjacency).
	 * * @return List of Points for valid ability destinations.
	 */
	public List<Point> getPlayerSpecialMoveTargets() {
		if (isGameOver || player == null || !player.isAlive()) return Collections.emptyList();
		if (!player.isAbilityAvailable()) return Collections.emptyList();

		if (currentMode.equals("Present") && player.getType().equals("Predator")) {
			if (!isAdjacent(player, apex)) {
				return Collections.emptyList();
			}
		}

		List<Point> targets = new ArrayList<>();
		int cx = player.getX();
		int cy = player.getY();

		int range = Math.max(2, player.getAbilityRange());
		for (int x = cx - range; x <= cx + range; x++) {
			for (int y = cy - range; y <= cy + range; y++) {

				if (!grid.isValidPosition(x, y)) continue;

				int moveType = player.checkMoveType(new Point(x, y));
				if (moveType != 2) continue;

				if (isCellMovableFor(player, x, y, true)) {
					targets.add(new Point(x, y));
				}
			}
		}
		return targets;
	}

	/**
	 * Validates if a click on a specific coordinate is a legal move.
	 * Used by GameFrame input listener.
	 * * @param x The clicked X coordinate.
	 * @param y The clicked Y coordinate.
	 * @return true if the move is valid for the player.
	 */
	public boolean isValidPlayerTarget(int x, int y) {
		if (isGameOver || player == null || !player.isAlive()) return false;
		if (!grid.isValidPosition(x, y)) return false;

		if (player.getX() == x && player.getY() == y) return true;

		for (Point p : getPlayerNormalMoveTargets()) {
			if (p.x == x && p.y == y) return true;
		}
		for (Point p : getPlayerSpecialMoveTargets()) {
			if (p.x == x && p.y == y) return true;
		}
		return false;
	}

	/**
	 * Internal helper to check if an actor can logically enter a cell.
	 * * @param actor     The animal attempting to move.
	 * @param tx        Target X.
	 * @param ty        Target Y.
	 * @param isSpecial Set true to apply specific Ability-based restrictions (e.g. Future Prey).
	 * @return true if the cell is movable (empty or edible).
	 */
	private boolean isCellMovableFor(Animal actor, int tx, int ty, boolean isSpecial) {
		Cell targetCell = grid.getCell(tx, ty);
		if (targetCell == null) return false;

		if (targetCell.isEmpty()) return true;

		Entity e = targetCell.getEntity();

		if (isSpecial && currentMode.equals("Future") && actor.getType().equals("Prey") && (e instanceof Food)) {
			return false;
		}

		return actor.canEat(e);
	}


	public void addLoadedAnimal(Animal animal) {
		this.animals.add(animal);
		String type = animal.getType();
		
		if (type.equals("Predator")) {
			this.player = animal;
		} else if (type.equals("Apex")) {
			this.apex = animal;
		} else if (type.equals("Prey")) {
			this.prey = animal;
		}
	}

	/**
	 * Executes the instant move for Present Predator (Cheetah).
	 * * @param targetX Destination X.
	 * @param targetY Destination Y.
	 */
	private void executePresentPredatorDash(int targetX, int targetY) {
		moveActor(player, targetX, targetY);
		GameLogger.log(player.getName() + " used Dash");
	}

	/**
	 * Checks if two entities are adjacent (Chebyshev distance of 1).
	 * * @param e1 First entity.
	 * @param e2 Second entity.
	 * @return true if they are neighbors.
	 */
	private boolean isAdjacent(Entity e1, Entity e2) {
		if (e1 == null || e2 == null) return false;
		
		int dx = Math.abs(e1.getX() -  e2.getX());
		int dy = Math.abs(e1.getY() - e2.getY());
		
		int distance = Math.max(dx, dy);
		return distance == 1;
	}


	public Grid getGrid() { return grid; }
	public int getCurrentRound() { return currentRound; }
	public void setCurrentRound(int currentRound) {	this.currentRound = currentRound; }
	
	public int getMaxRounds() { return maxRounds; }
	public String getCurrentMode() { return currentMode; }
	public List<Animal> getAnimals() { return animals; }
	public boolean isGameOver() { return isGameOver; }
	
	public Animal getPlayer() { return player; }
	public Animal getApex() { return apex; }
	public Animal getPrey() { return prey; }
}