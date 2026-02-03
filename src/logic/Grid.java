package logic;

import model.Entity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the game board (grid) consisting of a 2D array of Cells.
 * This class manages the placement, movement, and removal of entities.
 * * Task: Acts as the container for the game world and handles coordinate validation.
 * Methods: placeEntity, moveEntity, removeEntity, clear, getEntities, isValidPosition.
 */
public class Grid implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Cell[][] cells;
	private int size;

	public Grid(int size) {
		this.size = size;
		this.cells = new Cell[size][size];
		
		// Initialize the grid with empty cells
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				cells[i][j] = new Cell(i, j);
			}
		}
	}

	// --- MANIPULATION METHODS ---

	/**
	 * Places a specific entity onto the grid at the given coordinates.
	 * Updates both the cell's content and the entity's internal position.
	 * @param e The entity to place.
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 */
	public void placeEntity(Entity e, int x, int y) {
		if (isValidPosition(x, y)) {
			cells[x][y].setEntity(e);
			e.setPosition(x, y);
		}
	}

	/**
	 * Moves an entity from its current position to a new position.
	 * Clears the old cell and occupies the new one.
	 * @param e The entity to move.
	 * @param newX The new X coordinate.
	 * @param newY The new Y coordinate.
	 */
	public void moveEntity(Entity e, int newX, int newY) {
		if (isValidPosition(newX, newY)) {
			// 1. Clear the old position
			cells[e.getX()][e.getY()].removeEntity();
			
			// 2. Set entity in the new position
			cells[newX][newY].setEntity(e);
			
			// 3. Update the entity's internal coordinates
			e.setPosition(newX, newY);
		}
	}

	/**
	 * Removes an entity completely from the grid (e.g., when eaten).
	 * @param e The entity to remove.
	 */
	public void removeEntity(Entity e) {
		if (isValidPosition(e.getX(), e.getY())) {
			cells[e.getX()][e.getY()].removeEntity();
		}
	}

	/**
	 * Wipes the entire grid clean.
	 * Used when resetting the game or loading a new state.
	 */
	public void clear() {
		// Iterate through every cell and remove its entity
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				cells[i][j].setEntity(null);
			}
		}
	}

	// --- DATA ACCESS & VALIDATION ---

	/**
	 * Scans the entire grid to return a list of all active entities.
	 * Used by the GameEngine/AI to know where everyone is.
	 * @return A list of all entities currently on the grid.
	 */
	public List<Entity> getEntities() { 
		List<Entity> list = new ArrayList<>();
		
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (!cells[i][j].isEmpty()) {
					list.add(cells[i][j].getEntity());
				}
			}
		}
		return list;
	}

	/**
	 * Checks if the given coordinates are within the grid boundaries.
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @return true if the position exists on the grid.
	 */
	public boolean isValidPosition(int x, int y) {
		return x >= 0 && x < size && y >= 0 && y < size;
	}

	public Cell getCell(int x, int y) {
		if (isValidPosition(x, y)) {
			return cells[x][y];
		}
		return null;
	}

	public int getSize() {
		return size;
	}
}
