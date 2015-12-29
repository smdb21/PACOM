package org.proteored.miapeExtractor.analysis.gui;

import java.awt.Cursor;

import javax.swing.JProgressBar;

public interface TreeDialog {
	// public void disableControls(boolean b);

	public String getUserName();

	public String getPassword();

	public JProgressBar getProgressBar();

	public void setCursor(Cursor cursor);

	// public void setLoadedMSIDocumentsNumber(String value);
}
