

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JButton;

public class GOGUI extends JFrame {

	private JPanel contentPane;
	private JTextField npcids;
	private JTextField foodid;
	private JTextField mineat;
	public boolean confirmed = false;
	private JTextField atkRadius;
	private JTextField lootIds;

	/**
	 * Create the frame.
	 */
	public GOGUI() {
		super("GOFighter V1.0 - By RSGO (PowerBot.com)");
		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblEnterNpcIds = new JLabel("Enter NPC ID's Separated With \",\":");
		lblEnterNpcIds.setBounds(10, 11, 179, 14);
		contentPane.add(lblEnterNpcIds);

		npcids = new JTextField();
		npcids.setBounds(10, 36, 414, 20);
		contentPane.add(npcids);
		npcids.setColumns(10);

		JLabel lblEnterFoodId = new JLabel("Enter Food ID:");
		lblEnterFoodId.setBounds(10, 67, 128, 14);
		contentPane.add(lblEnterFoodId);

		foodid = new JTextField();
		foodid.setBounds(10, 92, 128, 20);
		contentPane.add(foodid);
		foodid.setColumns(10);

		JLabel lblEnterMinHp = new JLabel("Enter Min. HP to Eat:");
		lblEnterMinHp.setBounds(10, 123, 128, 14);
		contentPane.add(lblEnterMinHp);

		mineat = new JTextField();
		mineat.setBounds(10, 148, 128, 20);
		contentPane.add(mineat);
		mineat.setColumns(10);

		JButton startButton = new JButton("GO Fight!");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				if (npcids.getText() == null || foodid.getText() == null || mineat.getText() == null
						|| atkRadius.getText() == null) {
					JOptionPane.showMessageDialog(null, "All Fields Must Be Filled In!");
					return;
				}

				String npcIdz = npcids.getText();
				if (npcIdz.contains(",")) {
					final String s[] = npcIdz.split(",");
					GOFighter.npcIds = new int[s.length];
					for (int i = 0; i < s.length; i++) {
						GOFighter.npcIds[i] = Integer.parseInt(s[i]);
					}
				} else
					GOFighter.npcIds = new int[] { Integer.parseInt(npcIdz) };
				/*
				String lootIdz = lootIds.getText();
				if (lootIdz != null) {
					GOFighter.lootEnabled = true;
					if (lootIdz.contains(",")) {
						final String s[] = lootIdz.split(",");
						GOFighter.lootIds = new int[s.length];
						for (int i = 0; i < s.length; i++) {
							GOFighter.lootIds[i] = Integer.parseInt(s[i]);
						}
					} else
						GOFighter.lootIds = new int[] { Integer.parseInt(lootIdz) };
				}
				*/
				// GOFighter.npcIds = {Integer.parseInt(npcIdz)};
				// GOFighter.
				final String foodidz = foodid.getText();
				GOFighter.foodId = Integer.parseInt(foodidz);
				final String minToEat = mineat.getText();
				GOFighter.minHpToEat = Integer.parseInt(minToEat);
				GOFighter.maxDist = Integer.parseInt(atkRadius.getText());
				confirmed = true;
			}
		});
		startButton.setBounds(10, 179, 414, 72);
		contentPane.add(startButton);

		JLabel lblTileRadius = new JLabel("Max Attack Radius:");
		lblTileRadius.setBounds(148, 67, 96, 14);
		contentPane.add(lblTileRadius);

		atkRadius = new JTextField();
		atkRadius.setBounds(148, 92, 128, 20);
		contentPane.add(atkRadius);
		atkRadius.setColumns(10);

		lootIds = new JTextField();
		lootIds.setEnabled(false);
		lootIds.setBounds(148, 148, 276, 20);
		contentPane.add(lootIds);
		lootIds.setColumns(10);

		JLabel lblLootId = new JLabel("Enter Loot ID's Separated With \",\":");
		lblLootId.setEnabled(false);
		lblLootId.setBounds(148, 123, 224, 14);
		contentPane.add(lblLootId);
		setVisible(true);
	}
}
