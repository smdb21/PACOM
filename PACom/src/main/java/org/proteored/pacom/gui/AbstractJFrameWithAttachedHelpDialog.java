package org.proteored.pacom.gui;

import javax.swing.JFrame;

public abstract class AbstractJFrameWithAttachedHelpDialog extends JFrame implements HasHelp {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3356292958061016878L;
	private AttachedHelpDialog help;
	private final int maxCharactersInHelpMessageRow;

	public AbstractJFrameWithAttachedHelpDialog(int maxCharactersInHelpMessageRow) {
		this.maxCharactersInHelpMessageRow = maxCharactersInHelpMessageRow;
	}

	public void showAttachedHelpDialog() {
		getHelpDialog().forceVisible();
	}

	protected AttachedHelpDialog getHelpDialog() {
		if (help == null) {
			help = new AttachedHelpDialog(this, this.maxCharactersInHelpMessageRow);
		}
		return help;
	}
}
