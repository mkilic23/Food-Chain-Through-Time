package gui;

import java.util.List;
import logic.GameEngine;
import logic.Grid;
import logic.Cell;
import model.Entity;
import model.Food;
import model.animals.Animal;

import javax.swing.JPanel;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class GamePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private GameEngine engine;
	
	private Map<String, BufferedImage> imageCache;

	private int cellSize;

	public GamePanel(GameEngine engine) {
		this.engine = engine;
		this.imageCache = new HashMap<>();
		
		this.setBackground(new Color(240, 240, 230));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Grid grid = engine.getGrid();
		int gridSize = grid.getSize();

		int panelWidth = getWidth();
		int panelHeight = getHeight();
		
		if (gridSize > 0) {
			cellSize = Math.min(panelWidth, panelHeight) / gridSize;
		} else {
			cellSize = 50;
		}

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int totalGridSize = gridSize * cellSize;
		
		int xOffset = (panelWidth - totalGridSize) / 2;
		int yOffset = (panelHeight - totalGridSize) / 2;
		
		boolean[][] normalMask = new boolean[gridSize][gridSize];
		boolean[][] specialMask = new boolean[gridSize][gridSize];
		
		if (!engine.isGameOver() && engine.getPlayer() != null && engine.getPlayer().isAlive()) {
			List<Point> normals = engine.getPlayerNormalMoveTargets();
			for (Point p : normals) {
				if (grid.isValidPosition(p.x, p.y)) normalMask[p.x][p.y] = true;
			}

			List<Point> specials = engine.getPlayerSpecialMoveTargets();
			for (Point p : specials) {
				if (grid.isValidPosition(p.x, p.y)) specialMask[p.x][p.y] = true;
			}
			
			Animal player = engine.getPlayer();
			int pxP = player.getX();
			int pyP = player.getY();
			if (grid.isValidPosition(pxP, pyP)) {
				normalMask[pxP][pyP] = false;
				specialMask[pxP][pyP] = false;
			}
		}

		for (int x = 0; x < gridSize; x++) {
			for (int y = 0; y < gridSize; y++) {
				
				int px = xOffset + (x * cellSize); 
				int py = yOffset + (y * cellSize); 
				
				if (specialMask[x][y]) {
					g2d.setColor(new Color(255, 215, 0, 90)); 
					g2d.fillRect(px + 1, py + 1, cellSize - 1, cellSize - 1);
				} else if (normalMask[x][y]) {
					g2d.setColor(new Color(46, 204, 113, 90)); 
					g2d.fillRect(px + 1, py + 1, cellSize - 1, cellSize - 1);
				}

				g2d.setColor(Color.LIGHT_GRAY);
				g2d.drawRect(px, py, cellSize, cellSize);

				Cell cell = grid.getCell(x, y);
				if (!cell.isEmpty()) {
					Entity entity = cell.getEntity();
					BufferedImage img = getImageFor(entity);

					if (img != null) {
						g2d.drawImage(img, px + 2, py + 2, cellSize - 4, cellSize - 4, this);
					} else {
						drawFallbackShape(g2d, entity, px, py, cellSize);
					}
				}
			}
		}
	}

	@Override
	public Dimension getPreferredSize() {
		int size = engine.getGrid().getSize() * 40; 
		return new Dimension(size, size);
	}
	

	private BufferedImage getImageFor(Entity entity) {
		String key;

		if (entity instanceof Food) {
			Food food = (Food) entity;
			
			key = food.getName().toLowerCase().replaceAll("[\\s-]", "");
		} else if (entity instanceof Animal) {
			Animal animal = (Animal) entity;
			key = animal.getName().toLowerCase().replaceAll("[\\s-]", "");
		} else {
			return null;
		}

		if (imageCache.containsKey(key)) {
			return imageCache.get(key);
		}

		BufferedImage img = loadImage(key);
		if (img != null) {
			imageCache.put(key, img);
		}
		return img;
	}

	private BufferedImage loadImage(String key) {
		String path = "/images/" + key + ".png"; 
		try {
			URL url = getClass().getResource(path);
			if (url == null) {
				System.err.println("Image not found: " + path);
				return null;
			}
			return ImageIO.read(url);
		} catch (IOException e) {
			return null;
		}
	}

	private void drawFallbackShape(Graphics2D g2d, Entity entity, int px, int py, int size) {
		int padding = 4;
		int shapeSize = size - (padding * 2);

		if (entity instanceof Animal) {
			Animal animal = (Animal) entity;
			String type = animal.getType(); 

			switch (type) {
				case "Apex":
					g2d.setColor(new Color(220, 20, 60)); 
					g2d.fillRect(px + padding, py + padding, shapeSize, shapeSize);
					break;
				case "Predator":
					g2d.setColor(new Color(30, 144, 255)); 
					g2d.fillRect(px + padding, py + padding, shapeSize, shapeSize);
					break;
				case "Prey":
					g2d.setColor(new Color(50, 205, 50)); 
					g2d.fillOval(px + padding, py + padding, shapeSize, shapeSize); 
					break;
				default:
					g2d.setColor(Color.GRAY);
					g2d.fillRect(px + padding, py + padding, shapeSize, shapeSize);
			}
		} 
		else if (entity instanceof Food) {
			g2d.setColor(new Color(255, 165, 0)); 
			g2d.fillOval(px + size/3, py + size/3, size/3, size/3); 
		} 
		else {
			g2d.setColor(Color.BLACK);
			g2d.fillRect(px + padding, py + padding, shapeSize, shapeSize);
		}
	}

	public Point getGridCoordinates(int pixelX, int pixelY) {
		if (cellSize == 0) return null;
		
		int gridSize = engine.getGrid().getSize();
		int totalGridSize = gridSize * cellSize;
		
		int xOffset = (getWidth() - totalGridSize) / 2;
		int yOffset = (getHeight() - totalGridSize) / 2;
		
		int gridX = (pixelX - xOffset) / cellSize;
		int gridY = (pixelY - yOffset) / cellSize;
		
		if (engine.getGrid().isValidPosition(gridX, gridY)) {
			return new Point(gridX, gridY);
		}
		return null;
	}
	
	public GameEngine getEngine() {
		return engine;
	}

	public void setEngine(GameEngine engine) {
		this.engine = engine;
	}
}