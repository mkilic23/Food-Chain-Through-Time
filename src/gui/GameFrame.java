package gui;

import logic.GameEngine;
import io.FileManager;
import io.GameLogger;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;

public class GameFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;

	private JPanel mainContainer;
	private GamePanel gamePanel;
	private InfoPanel infoPanel;
	private StartPanel startPanel;
	
	private GameEngine engine;
	private CardLayout cardLayout;

	private int currentGridSize = 20; 

	public GameFrame() {
		setTitle("Food Chain Game");
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				confirmAndExit();
			}
		});

		setSize(1000, 800);
		setLayout(new BorderLayout());

		JMenuBar menuBar = new JMenuBar();
		JMenu menuFile = new JMenu("Choices");
		
		JMenuItem newGameItem = new JMenuItem("New Game");
		newGameItem.addActionListener(e -> confirmAndReturnToStart());
		menuFile.add(newGameItem);
		menuFile.addSeparator();

		JMenuItem saveItem = new JMenuItem("Save");
		saveItem.addActionListener(e -> performManualSave());

		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(e -> confirmAndExit());

		menuFile.add(saveItem);
		menuFile.addSeparator();
		menuFile.add(exitItem);
		
		menuBar.add(menuFile);
		setJMenuBar(menuBar);

		cardLayout = new CardLayout();
		mainContainer = new JPanel(cardLayout);

		startPanel = new StartPanel(this);
		mainContainer.add(startPanel, "START");

		JPanel gameContainer = new JPanel(new BorderLayout());
		infoPanel = new InfoPanel(); 
		gameContainer.add(infoPanel, BorderLayout.EAST);
		mainContainer.add(gameContainer, "GAME");

		add(mainContainer, BorderLayout.CENTER);
		
		cardLayout.show(mainContainer, "START");
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public void startGame(int size, int rounds, String mode) {
		try {
			this.currentGridSize = size;
			GameEngine newEngine = new GameEngine(size, rounds, mode);
			initGameGUI(newEngine);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Exception: " + e.getMessage());
		}
	}

	public void loadSavedGame() {
		if (!FileManager.isSaveFileAvailable()) return;

		try {
			GameEngine loadedEngine = FileManager.loadGame(); 
			
			initGameGUI(loadedEngine); 
			JOptionPane.showMessageDialog(this, "Game Loaded!");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initGameGUI(GameEngine newEngine) {
		this.engine = newEngine; 

		gamePanel = new GamePanel(engine);
		gamePanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				handleMouseClick(e.getX(), e.getY());
			}
		});

		JPanel gameContainer = (JPanel) ((Container)mainContainer.getComponent(1)); 
		BorderLayout layout = (BorderLayout) gameContainer.getLayout();
		Component oldCenter = layout.getLayoutComponent(BorderLayout.CENTER);
		if (oldCenter != null) gameContainer.remove(oldCenter);
		
		gameContainer.add(gamePanel, BorderLayout.CENTER);
		updateInfoLabels();
		
		gameContainer.revalidate();
		gameContainer.repaint();
		cardLayout.show(mainContainer, "GAME");
		gamePanel.requestFocusInWindow();
	}


	private void performManualSave() {
		if (engine == null || engine.isGameOver()) return;
		FileManager.saveGame(engine);
		JOptionPane.showMessageDialog(this, "Game Saved successfully.");
	}

	private void confirmAndExit() {
		if (engine == null || engine.isGameOver()) {
			GameLogger.close();
			System.exit(0);
			return;
		}

		int choice = JOptionPane.showConfirmDialog(
			this, 
			"Do you want save before exit?", 
			"Exit Confirmation", 
			JOptionPane.YES_NO_CANCEL_OPTION
		);

		if (choice == JOptionPane.YES_OPTION) {
			FileManager.saveGame(engine);
			JOptionPane.showMessageDialog(this, "Saved. See you soon!");
			System.exit(0);
		} else if (choice == JOptionPane.NO_OPTION) {
			System.exit(0);
		}
	}

	private void handleMouseClick(int pixelX, int pixelY) {
		if (engine == null || engine.isGameOver()) return;
			
		Point gridPoint = gamePanel.getGridCoordinates(pixelX, pixelY);
		
		if (gridPoint != null) {
			try {
				if (!engine.isValidPlayerTarget(gridPoint.x, gridPoint.y)) {
					return; 
				}
				
				engine.processPlayerMove(gridPoint.x, gridPoint.y);
				gamePanel.repaint();
				updateInfoLabels();
				
				if (engine.isGameOver()) {
					JOptionPane.showMessageDialog(this, "Game Over! Winner: " + engine.getWinner());
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	public void updateInfoLabels() {
		if (engine != null && infoPanel != null) {
			infoPanel.updateStats(
					engine.getCurrentRound(),
					engine.getMaxRounds(),
					engine.getCurrentMode(),
					engine.getApex(),
					engine.getPlayer(),
					engine.getPrey()
				);        
		}
	}

	private void confirmAndReturnToStart() {
		if (engine == null || engine.isGameOver()) {
			backToStartScreen();
			return;
		}

		int choice = JOptionPane.showConfirmDialog(
			this,
			"Do you want to save before starting a new game?",
			"New Game",
			JOptionPane.YES_NO_CANCEL_OPTION
		);

		if (choice == JOptionPane.YES_OPTION) {
			FileManager.saveGame(engine);
			backToStartScreen();
		} else if (choice == JOptionPane.NO_OPTION) {
			backToStartScreen();
		}
	}

	public void backToStartScreen() {
		engine = null;
		gamePanel = null;

		cardLayout.show(mainContainer, "START");
	}
}