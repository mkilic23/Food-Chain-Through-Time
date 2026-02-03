package model.animals;

import java.awt.Point;
import java.io.Serializable;
import model.Entity;

/**
 * Represents an active agent in the game (Apex, Predator, or Prey).
 * Handles movement, abilities, cooldowns, and interactions.
 * * Task: Manages the state and behavior of animals on the grid.
 * Methods: checkMoveType, isValidAbilityGeometry, canEat, triggerAbilityCooldown
 */
public class Animal extends Entity implements Serializable {

	private static final long serialVersionUID = 1L;

	protected int score;
	protected boolean isAlive;
	protected String name; 
	
	private int cooldown; 
	private int maxAbilityCooldown;
	
	private Point position; 
	private String type; 
	private String era;  

	public Animal(String name, String type, String era, int x, int y) {
		super(x, y, name.charAt(0));
		
		this.name = name;
		this.type = type;
		this.era = era;
		this.position = new Point(x, y);
		this.score = 0;
		this.isAlive = true;

		assignProperties();
	
		this.cooldown = this.maxAbilityCooldown;	
	}

	private void assignProperties() {
		
		this.maxAbilityCooldown = 2;

		
		if (era.equals("Present") && type.equals("Predator")) {
			this.maxAbilityCooldown = 0;
			return;
		}

		if (era.equals("Present") && (type.equals("Apex") || type.equals("Prey"))) {
			this.maxAbilityCooldown = 3;
		}
		else if (era.equals("Future") && type.equals("Apex")) {
			this.maxAbilityCooldown = 3;
		}
	}


	/**
	 * Determines the type of move based on the target position.
	 * @param target The destination point.
	 * @return 0: Invalid, 1: Walk, 2: Ability, 3: Stay
	 */
	public int checkMoveType(Point target) {
		if (!isAlive) return 0;
		if (this.position == null) return 0;

		if (target.equals(this.position)) {
			return 3; 
		}

		int dx = Math.abs(target.x - position.x);
		int dy = Math.abs(target.y - position.y);
		int distance = Math.max(dx, dy);

		if (distance == 1) {
			return 1; 
		}

		if (cooldown > 0) {
			return 0; 
		}

		if (isValidAbilityGeometry(dx, dy)) {
			return 2; 
		}

		return 0;
	}

	/**
	 * Validates if the move matches the specific geometry of the animal's ability.
	 * @param dx Distance in X
	 * @param dy Distance in Y
	 * @return true if geometry is valid
	 */
	private boolean isValidAbilityGeometry(int dx, int dy) {
		int distance = Math.max(dx, dy);

		switch (era) {
			case "Past":
				if (type.equals("Predator")) {
					return (dx == 0 && dy == 2) || (dx == 2 && dy == 0);
				}
				if (type.equals("Apex")) {
					return (dx == 0 && dy == 2) || (dx == 2 && dy == 0);
				}
				if (type.equals("Prey")) {
					return (dx == 1 && dy == 1) || (dx == 2 && dy == 1) || (dx == 1 && dy == 2);
				}
				break;

			case "Present":
				if (type.equals("Apex")) {
					return (dx == 0 && dy >= 1 && dy <= 3)
						|| (dy == 0 && dx >= 1 && dx <= 3)
						|| (dx == dy && dx >= 1 && dx <= 3);
				}
				if (type.equals("Prey")) { 
					return (dx == 2 && dy == 0)
						|| (dx == 0 && dy == 2)
						|| (dx == 2 && dy == 2);
				}
				if (type.equals("Predator")) {
					return Math.max(dx, dy) <= 2;
				}  
				break;

			case "Future":
				if (type.equals("Apex")) return distance <= 3;
				
				
				if (type.equals("Prey")) {
					return (dx == 3 && dy == 0)
						|| (dx == 0 && dy == 3)
						|| (dx == 3 && dy == 3);
				}  
				if (type.equals("Predator")) { 
					return (dx == 2 && dy == 0)
						|| (dx == 0 && dy == 2)
						|| (dx == 2 && dy == 2);
				}
				break;
		}
		return false;
	}


	/**
	 * Checks if this entity is allowed to eat the target.
	 * @param target The entity to be eaten
	 * @return true if edible
	 */
	public boolean canEat(Entity target) {
		if (this.type.equals("Prey")) {
			return target.getClass().getSimpleName().equals("Food");
		}

		if (target instanceof Animal) {
			Animal victim = (Animal) target;
			
			if (this.type.equals("Apex")) {
				return !victim.getType().equals("Apex");
			}

			if (this.type.equals("Predator")) {
				return victim.getType().equals("Prey");
			}
		}

		return false;
	}


	public int getAbilityRange() {
		if (era.equals("Present") && type.equals("Apex")) return 3;
		if (era.equals("Future") && (type.equals("Apex") || type.equals("Prey"))) return 3;
		return 2;
	}

	public String getAbilityName() {
		if (type.equals("Apex")) { 
			return "Sprint";
		}
		if (type.equals("Predator")) {
			return "Dash";

		}
		return "Hop"; 
	}


	/**
	 * Sets cooldown to max after ability usage.
	 */
	public void triggerAbilityCooldown() {
		if (this.maxAbilityCooldown <= 0) return;
		this.cooldown = this.maxAbilityCooldown;
	}

	/**
	 * Reduces cooldown by 1 turn.
	 */
	public void reduceCooldown() { 
		if (this.cooldown > 0) {
			this.cooldown--;
		}
	}

	/**
	 * Checks if ability is ready.
	 * @return true if available
	 */
	public boolean isAbilityAvailable() {
		if (era.equals("Present") && type.equals("Predator")) {
			return true;
		}
		return cooldown == 0;
	}


	public void die() {
		this.isAlive = false;
	}

	public void respawn(int newX, int newY) {
		this.isAlive = true;
		this.position = new Point(newX, newY); 
	}


	public void addScore(int points) { this.score += points; }
	public void decreaseScore(int points) { this.score -= points; }
	public int getScore() { return score; }
	
	public boolean isAlive() { return isAlive; }
	public void setAlive(boolean alive) { this.isAlive = alive; }

	public int getAbilityCooldown() { return cooldown; } 
	public void setCooldown(int cooldown) {	this.cooldown = cooldown; }
	
	public int getMaxAbilityCooldown() { return maxAbilityCooldown; }

	public String getName() { return name; }
	public String getType() { return type; }
	public String getEra() { return era; }

	public Point getPosition() { return position; }
	public void setPosition(Point position) { this.position = position; }
}
 