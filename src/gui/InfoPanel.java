package gui;

import model.animals.Animal;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class InfoPanel extends JPanel {

	private JLabel lblRound, lblEra;
	private JLabel lblApexName, lblApexScore, lblApexCooldown;
	private JLabel lblPlayerName, lblPlayerScore, lblPlayerCooldown;
	private JLabel lblPreyName, lblPreyScore, lblPreyCooldown;
	private JLabel lblRemaining;

	public InfoPanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(Color.LIGHT_GRAY);
		setPreferredSize(new Dimension(220, 0)); 
		setBorder(new EmptyBorder(10, 10, 10, 10)); 

		add(createGameInfoPanel());
		add(Box.createRigidArea(new Dimension(0, 10))); 

		add(createApexPanel());
		
		
		
		add(Box.createRigidArea(new Dimension(0, 10)));

		add(createPlayerPanel());
		add(Box.createRigidArea(new Dimension(0, 10)));

		add(createPreyPanel());
	}

	private JPanel createGameInfoPanel() {
		JPanel panel = createStyledPanel(Color.WHITE, "GAME STATUS");
		
		lblRound = new JLabel("Round: 0");
		lblEra = new JLabel("Era: -");
		lblRemaining = new JLabel("Remaining: 0");
		
		styleLabel(lblRemaining);
		styleLabel(lblRound);
		styleLabel(lblEra);
		
		panel.add(lblRemaining);
		panel.add(lblRound);
		panel.add(lblEra);
		return panel;
	}

	private JPanel createApexPanel() {
		JPanel panel = createStyledPanel(new Color(255, 200, 200), "APEX PREDATOR"); 
		
		lblApexName = new JLabel("Name: -");
		lblApexScore = new JLabel("Score: -");
		lblApexCooldown = new JLabel("Ability Cooldown: -");

		styleLabel(lblApexName);
		styleLabel(lblApexScore);
		styleLabel(lblApexCooldown);

		panel.add(lblApexName);
		panel.add(lblApexScore);
		panel.add(lblApexCooldown);
		return panel;
	}

	private JPanel createPlayerPanel() {
		JPanel panel = createStyledPanel(new Color(255, 220, 180), "PREDATOR"); 
		
		lblPlayerName = new JLabel("Name: -");
		lblPlayerScore = new JLabel("Score: -");
		lblPlayerCooldown = new JLabel("Ability Cooldown: -");

		styleLabel(lblPlayerName);
		styleLabel(lblPlayerScore);
		styleLabel(lblPlayerCooldown);

		panel.add(lblPlayerName);
		panel.add(lblPlayerScore);
		panel.add(lblPlayerCooldown);
		return panel;
	}

	private JPanel createPreyPanel() {
		JPanel panel = createStyledPanel(new Color(200, 230, 255), "PREY"); 
		
		lblPreyName = new JLabel("Name: -");
		lblPreyScore = new JLabel("Score: -");
		lblPreyCooldown = new JLabel("Ability Cooldown: -");

		styleLabel(lblPreyName);
		styleLabel(lblPreyScore);
		styleLabel(lblPreyCooldown);

		panel.add(lblPreyName);
		panel.add(lblPreyScore);
		panel.add(lblPreyCooldown);
		return panel;
	}

	private JPanel createStyledPanel(Color bgColor, String title) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(bgColor);
		
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
		
		Border line = new LineBorder(Color.BLACK, 2);
		Border margin = new EmptyBorder(15, 15, 15, 15); 
		panel.setBorder(new CompoundBorder(line, margin));
		
		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(titleLabel);
		panel.add(Box.createRigidArea(new Dimension(0, 10))); 
		
		return panel;
	}

	private void styleLabel(JLabel label) {
		label.setFont(new Font("Monospaced", Font.PLAIN, 12));
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
	}

	public void updateStats(int round, int maxRounds, String era, Animal apex, Animal player, Animal prey) {
		
		lblRound.setText("Round: " + round);
		lblEra.setText("Era: " + era);

		int remaining = Math.max(0, maxRounds - round);
		lblRemaining.setText("Remaining: " + remaining);

		if (apex != null) {
			lblApexName.setText("Name: " + apex.getName());
			lblApexScore.setText("Score: " + apex.getScore());
			lblApexCooldown.setText(
					"Ability Cooldown: " + apex.getAbilityCooldown() + "/" + apex.getMaxAbilityCooldown()
				);
		} else {
			lblApexName.setText("Name: None");
			lblApexScore.setText("-");
			lblApexCooldown.setText("-");
		}

		
		
		
		if (player != null) {
			lblPlayerName.setText("Name: " + player.getName());
			lblPlayerScore.setText("Score: " + player.getScore());
			
			
			
			lblPlayerCooldown.setText(
					"Ability Cooldown: " + player.getAbilityCooldown() + "/" + player.getMaxAbilityCooldown()
				);
		} else {
			lblPlayerName.setText("Name: None");
			lblPlayerScore.setText("-");
			lblPlayerCooldown.setText("-");
		}
		
		if (prey != null) {
			lblPreyName.setText("Name: " + prey.getName());
			lblPreyScore.setText("Score: " + prey.getScore());
			
			lblPreyCooldown.setText(
					"Ability Cooldown: " + prey.getAbilityCooldown() + "/" + prey.getMaxAbilityCooldown()
				);
		} else {
			lblPreyName.setText("Name: None");
			lblPreyScore.setText("-");
			lblPreyCooldown.setText("-");
		}
	}
}
