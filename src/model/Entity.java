package model;

import java.io.Serializable;

/**
 * Represents the base class for all objects on the grid (Animals and Food).
 * It holds the fundamental coordinate data and a display symbol.
 * * Task: Provides a common structure for position and serialization.
 * Methods: Position getters/setters.
 */
public abstract class Entity implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected int x;
	protected int y;
	protected char symbol; 
	public Entity(int x, int y, char symbol) {
		this.x = x;
		this.y = y;
		this.symbol = symbol;
	}

	/**
	 * Updates the entity's coordinates directly.
	 * @param x The new X coordinate.
	 * @param y The new Y coordinate.
	 */
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}


	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public char getSymbol() {
		return symbol;
	}
}
