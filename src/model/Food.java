package model;

/**
 * Represents a passive food item on the grid.
 * Types vary based on the game Era (e.g., Grass, Corn, Energy Node).
 * * Task: Stores the specific name/type of the food source.
 */
public class Food extends Entity {
	
	private static final long serialVersionUID = 1L;
	
	private String name;

	public Food(int x, int y, String name) {
		super(x, y, 'F'); 
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
