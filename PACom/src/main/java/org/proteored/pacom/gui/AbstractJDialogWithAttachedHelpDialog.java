package org.proteored.pacom.gui;

import javax.swing.JDialog;
import javax.swing.JFrame;

public abstract class AbstractJDialogWithAttachedHelpDialog extends JDialog implements HasHelp {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3356292958061016878L;
	private AttachedHelpDialog help;
	private final int maxCharactersInHelpMessageRow;

	public AbstractJDialogWithAttachedHelpDialog(JDialog parentDialog, int maxCharactersInHelpMessageRow) {
		super(parentDialog);
		this.maxCharactersInHelpMessageRow = maxCharactersInHelpMessageRow;
	}

	public AbstractJDialogWithAttachedHelpDialog(JFrame parentJFrame, int maxCharactersInHelpMessageRow) {
		super(parentJFrame);
		this.maxCharactersInHelpMessageRow = maxCharactersInHelpMessageRow;
	}

	public AbstractJDialogWithAttachedHelpDialog(JFrame parentJFrame, boolean modal,
			int maxCharactersInHelpMessageRow) {
		super(parentJFrame, modal);
		this.maxCharactersInHelpMessageRow = maxCharactersInHelpMessageRow;
	}

	@Override
	public void showAttachedHelpDialog() {
		getHelpDialog().forceVisible();
	}

	@Override
	public AttachedHelpDialog getHelpDialog() {
		if (help == null) {
			help = new AttachedHelpDialog(this, this.maxCharactersInHelpMessageRow);
		}
		return help;
	}
}
