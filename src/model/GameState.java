package model;

import java.io.Serializable;
import java.util.List;
import logic.Grid;
import model.animals.Animal;

/**
 * A Data Transfer Object (DTO) that encapsulates the entire state of the game
 * for saving and loading purposes.
 * * Task: Bundles the round number, game mode, grid layout, and entity lists into one object.
 */
public class GameState implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private int round;
	private int maxRounds;
	private String mode;
	private Grid grid;
	private List<Animal> animals;

	public GameState(int round, int maxRounds, String mode, Grid grid, List<Animal> animals) {
		this.round = round;
		this.maxRounds = maxRounds;
		this.mode = mode;
		this.grid = grid;
		this.animals = animals;
	}


	public int getRound() { 
		return round; 
	}
	
	public int getMaxRounds() { 
		return maxRounds; 
	}
	
	public String getMode() { 
		return mode; 
	}
	
	public Grid getGrid() { 
		return grid; 
	}
	
	public List<Animal> getAnimals() { 
		return animals; 
	}
}
