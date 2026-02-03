package logic;

import model.Entity;
import model.Food;
import model.animals.Animal;

import java.awt.Point;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class AIController {

	private static final SecureRandom random = new SecureRandom();

	private static int calculateDistance(int x1, int y1, int x2, int y2) {
		return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
	}

	public static int[] getNextMoveForPrey(Animal prey, Grid grid) {
		List<int[]> possibleMoves = getValidMoves(prey, grid);
		List<Entity> allEntities = grid.getEntities();
		
		int[] bestMove = {prey.getX(), prey.getY()};
		double bestScore = -Double.MAX_VALUE;

		for (int[] move : possibleMoves) {
			int mx = move[0];
			int my = move[1];
			
			double score = 0;
			double minDistToThreat = Double.MAX_VALUE;
			double minDistToFood = Double.MAX_VALUE;

			for (Entity e : allEntities) {
				if (e == prey) continue; 
				
				int dist = calculateDistance(mx, my, e.getX(), e.getY());

				if (e instanceof Animal) {
					String type = ((Animal) e).getType();
					
					if (type.equals("Predator") || type.equals("Apex")) {
						if (dist < minDistToThreat) minDistToThreat = dist;
					}
				} 
				else if (e instanceof Food) {
					if (dist < minDistToFood) minDistToFood = dist;
				}
			}

			if (minDistToThreat == Double.MAX_VALUE) minDistToThreat = 100; 
			if (minDistToFood == Double.MAX_VALUE) minDistToFood = 100;

			score = (minDistToThreat * 3.0) - minDistToFood;

			Cell targetCell = grid.getCell(mx, my);
			if (!targetCell.isEmpty() && targetCell.getEntity() instanceof Food) {
				score += 50.0;
			}

			if (score > bestScore) {
				bestScore = score;
				bestMove = move;
			} else if (score == bestScore) {
				if (random.nextBoolean()) bestMove = move;
			}
		}
		return bestMove;
	}

	public static int[] getNextMoveForApex(Animal apex, Grid grid) {
		List<Entity> allEntities = grid.getEntities();
		Entity closestTarget = null;
		int minGlobalDist = Integer.MAX_VALUE;

		for (Entity e : allEntities) {
			if (e == apex) continue;

			if (e instanceof Animal) {
				String type = ((Animal) e).getType();
				
				if (type.equals("Prey") || type.equals("Predator")) {
					int dist = calculateDistance(apex.getX(), apex.getY(), e.getX(), e.getY());
					if (dist < minGlobalDist) {
						minGlobalDist = dist;
						closestTarget = e;
					}
				}
			}
		}

		if (closestTarget == null) {
			return getRandomValidMove(apex, grid);
		}

		List<int[]> possibleMoves = getValidMoves(apex, grid);
		int[] bestMove = {apex.getX(), apex.getY()};
		int minMoveDist = Integer.MAX_VALUE;

		for (int[] move : possibleMoves) {
			int distToTarget = calculateDistance(move[0], move[1], closestTarget.getX(), closestTarget.getY());
			
			if (distToTarget < minMoveDist) {
				minMoveDist = distToTarget;
				bestMove = move;
			} else if (distToTarget == minMoveDist) {
				if (random.nextBoolean()) bestMove = move;
			}
		}

		return bestMove;
	}

	private static List<int[]> getValidMoves(Animal animal, Grid grid) {
		List<int[]> moves = new ArrayList<>();
		int cx = animal.getX();
		int cy = animal.getY();

		int range = animal.getAbilityRange();
		if (!animal.isAbilityAvailable()) {
			range = 1;
		}

		for (int x = cx - range; x <= cx + range; x++) {
			for (int y = cy - range; y <= cy + range; y++) {

				if (!grid.isValidPosition(x, y)) continue;

				Point p = new Point(x, y);
				int moveType = animal.checkMoveType(p);
				if (moveType == 0) continue;

				boolean isSpecial = (moveType == 2);

				Cell cell = grid.getCell(x, y);
				if (cell.isEmpty()) {
					moves.add(new int[]{x, y});
					continue;
				}

				Entity e = cell.getEntity();

				if (e instanceof Food) {
					if (isSpecial && animal.getEra().equals("Future") && animal.getType().equals("Prey")) {
						continue;
					}
					moves.add(new int[]{x, y});
					continue;
				}

				if (animal.canEat(e)) {
					moves.add(new int[]{x, y});
				}
			}
		}

		return moves;
	}

	private static int[] getRandomValidMove(Animal animal, Grid grid) {
		List<int[]> moves = getValidMoves(animal, grid);
		if (moves.isEmpty()) return new int[]{animal.getX(), animal.getY()};
		return moves.get(random.nextInt(moves.size()));
	}
}