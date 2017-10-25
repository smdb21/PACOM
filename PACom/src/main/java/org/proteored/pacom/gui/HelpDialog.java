package org.proteored.pacom.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class HelpDialog extends AbstractJDialogWithAttachedHelpDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -605019076274458268L;

	public HelpDialog(JFrame parentFrame, String title, String label1Text) {
		super(parentFrame, 40);
		setIconImage(ImageManager.getImageIcon(ImageManager.HELP_ICON).getImage());
		setResizable(false);
		this.setTitle(title);
		setModal(true);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0 };
		gridBagLayout.rowHeights = new int[] { 0 };
		gridBagLayout.columnWeights = new double[] { Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 20, 5, 5);
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		JButton b = new JButton();
		b.setBorderPainted(false);
		b.setContentAreaFilled(false);
		b.setFocusable(false);
		b.setHorizontalAlignment(SwingConstants.LEFT);
		b.setIcon(ImageManager.getImageIcon(ImageManager.HELP_ICON_64));
		getContentPane().add(b, c);
		//
		GridBagConstraints c2 = new GridBagConstraints();
		c2.anchor = GridBagConstraints.WEST;
		c2.insets = new Insets(20, 20, 20, 20);
		c2.gridx = 1;
		c2.gridy = 0;
		c2.gridheight = 1;
		JLabel label1 = new JLabel(label1Text);
		getContentPane().add(label1, c2);
		//
		GridBagConstraints c4 = new GridBagConstraints();
		c4.insets = new Insets(0, 0, 20, 0);
		c4.gridx = 0;
		c4.gridy = 1;
		c4.gridheight = 1;
		c4.gridwidth = 2;
		JButton closeButton = new JButton("OK");
		closeButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				HelpDialog.this.setVisible(false);
			}
		});
		getContentPane().add(closeButton, c4);

		pack();
		java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		java.awt.Dimension dialogSize = getSize();
		setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);
	}

	@Override
	public List<String> getHelpMessages() {
		// TODO Auto-generated method stub
		return null;
	}

}
