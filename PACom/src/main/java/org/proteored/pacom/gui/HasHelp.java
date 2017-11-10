package org.proteored.pacom.gui;

import java.util.List;

public interface HasHelp {
	public List<String> getHelpMessages();

	public void showAttachedHelpDialog();

	public AttachedHelpDialog getHelpDialog();
}
