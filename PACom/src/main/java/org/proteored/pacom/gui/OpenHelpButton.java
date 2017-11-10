package org.proteored.pacom.gui;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public class OpenHelpButton extends JButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8984493608993576796L;

	private OpenHelpButton() {
		setIcon(ImageManager.getImageIcon(ImageManager.HELP_ICON));
		setPressedIcon(ImageManager.getImageIcon(ImageManager.HELP_ICON_CLICKED));
		setRolloverIcon(ImageManager.getImageIcon(ImageManager.HELP_ICON_HOVER));
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		setToolTipText("Click here to open the help window");
	}

	public OpenHelpButton(HasHelp hasHelp) {
		this();
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (hasHelp.getHelpDialog().isVisible()) {
					hasHelp.getHelpDialog().setMinimized(true);
					hasHelp.getHelpDialog().setVisible(false);
				} else {
					hasHelp.showAttachedHelpDialog();
				}
			}
		});
	}

	public OpenHelpButton(AbstractJDialogWithAttachedHelpDialog jDialogWithHelp) {
		this();
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (jDialogWithHelp.getHelpDialog().isVisible()) {
					jDialogWithHelp.getHelpDialog().setMinimized(true);
					jDialogWithHelp.getHelpDialog().setVisible(false);
				} else {
					jDialogWithHelp.showAttachedHelpDialog();
				}
			}
		});
	}
}
