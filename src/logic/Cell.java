package logic;

import java.io.Serializable;
import model.Entity;

/**
 * Represents a single tile (unit) on the game grid.
 * It acts as a container that can hold exactly one Entity (Animal or Food) or be empty.
 * * Task: Manages the occupancy state at specific coordinates.
 * Methods: isEmpty, removeEntity, getters/setters.
 */
public class Cell implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Entity entity; 
	private int x;
	private int y;

	public Cell(int x, int y) {
		this.x = x;
		this.y = y;
		this.entity = null;
	}


	/**
	 * Checks if the cell is currently unoccupied.
	 * @return true if no entity is present (null).
	 */
	public boolean isEmpty() {
		return entity == null;
	}

	/**
	 * Clears the cell, effectively removing any entity within it.
	 */
	public void removeEntity() {
		this.entity = null;
	}


	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	public int getX() { 
		return x; 
	}
	
	public int getY() { 
		return y; 
	}
}
