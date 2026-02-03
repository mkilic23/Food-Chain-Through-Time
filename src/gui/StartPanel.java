package gui;

import io.FileManager;
import javax.swing.*;
import java.awt.*;

public class StartPanel extends JPanel {
	
	private GameFrame gameFrame;
	private JTextField txtGridSize;
	private JTextField txtRoundCount;
	private JComboBox<String> cmbMode;
	private java.awt.image.BufferedImage bg;


	public StartPanel(GameFrame frame) {
		this.gameFrame = frame;
		try {
	        bg = javax.imageio.ImageIO.read(getClass().getResource("/images/start.png"));
	    } catch (Exception e) {
	        bg = null;
	    }
		setLayout(new GridBagLayout()); 
		setBackground(new Color(230, 240, 250)); 
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel lblTitle = new JLabel("FOOD CHAIN GAME", SwingConstants.CENTER);
		lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
		lblTitle.setForeground(Color.WHITE);
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		add(lblTitle, gbc);

		gbc.gridwidth = 1;

		gbc.gridy++;
		gbc.gridx = 0;
		JLabel lblGrid = new JLabel("Grid Size:");
		lblGrid.setForeground(Color.WHITE);
		add(lblGrid, gbc);

		gbc.gridx = 1;
		txtGridSize = new JTextField("20");
		txtGridSize.setForeground(Color.WHITE);
		txtGridSize.setBackground(new Color(0, 0, 0, 140));
		txtGridSize.setCaretColor(Color.WHITE);
		add(txtGridSize, gbc);

		gbc.gridy++;
		gbc.gridx = 0;
		JLabel lblRounds = new JLabel("Round Number:");
		lblRounds.setForeground(Color.WHITE);
		add(lblRounds, gbc);

		gbc.gridx = 1;
		txtRoundCount = new JTextField("30");
		txtRoundCount.setForeground(Color.WHITE);
		txtRoundCount.setBackground(new Color(0, 0, 0, 140));
		txtRoundCount.setCaretColor(Color.WHITE);
		add(txtRoundCount, gbc);

		gbc.gridy++;
		gbc.gridx = 0;
		JLabel lblEra = new JLabel("Era:");
		lblEra.setForeground(Color.WHITE);
		add(lblEra, gbc);

		gbc.gridx = 1;
		cmbMode = new JComboBox<>(new String[]{"Past", "Present", "Future"});
		cmbMode.setSelectedIndex(1);
		cmbMode.setForeground(Color.RED);
		cmbMode.setBackground(Color.white);
			
		add(cmbMode, gbc);

		gbc.gridx = 0; 
		gbc.gridy++; 
		gbc.gridwidth = 2;
		JButton btnNewGame = new JButton("New Game");
		
		btnNewGame.setBackground(new Color(46, 204, 113));
		btnNewGame.setForeground(Color.WHITE);
		btnNewGame.setFocusPainted(false);
		btnNewGame.setBorderPainted(false); 
		btnNewGame.setOpaque(true);
		btnNewGame.setPreferredSize(new Dimension(200, 40));

		btnNewGame.addActionListener(e -> {
			try {
				int size = Integer.parseInt(txtGridSize.getText().trim());
				int rounds = Integer.parseInt(txtRoundCount.getText().trim());
					
				if (rounds < 10) {
					JOptionPane.showMessageDialog(this, "Total rounds must be at least 10.");
					return;
				}
				if (size < 10) {
					JOptionPane.showMessageDialog(this, "Grid size must be at least 10.");
					return;
				}

				String mode = (String) cmbMode.getSelectedItem();
				gameFrame.startGame(size, rounds, mode);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "Please enter numeric values.");
			}
		});
		add(btnNewGame, gbc);

		gbc.gridy++;
		JButton btnContinue = new JButton("Resume");

		btnContinue.setBackground(new Color(41, 128, 185));
		btnContinue.setForeground(Color.WHITE);
		btnContinue.setFocusPainted(false);
		btnContinue.setBorderPainted(false);
		btnContinue.setOpaque(true);
		btnContinue.setPreferredSize(new Dimension(200, 40));
		
		if (FileManager.isSaveFileAvailable()) {
			btnContinue.setEnabled(true);
		} else {
			btnContinue.setEnabled(false);
			btnContinue.setBackground(Color.GRAY);
			btnContinue.setText("No Saved Game");
		}

		btnContinue.addActionListener(e -> gameFrame.loadSavedGame());
		add(btnContinue, gbc);
		
		gbc.gridy++;
		JButton btnExit = new JButton("Exit");
		
		btnExit.setBackground(new Color(192, 57, 43));
		btnExit.setForeground(Color.WHITE);
		btnExit.setFocusPainted(false);
		btnExit.setBorderPainted(false);
		btnExit.setOpaque(true);
		
		btnExit.addActionListener(e -> System.exit(0));
		add(btnExit, gbc);
	}

	@Override
	protected void paintComponent(java.awt.Graphics g) {
	    super.paintComponent(g);
	    if (bg != null) g.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
	}
}