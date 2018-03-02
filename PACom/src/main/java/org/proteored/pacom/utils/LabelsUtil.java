package org.proteored.pacom.utils;

import java.awt.Font;

import javax.swing.JLabel;

public class LabelsUtil {
	public static JLabel setFontBOLD(JLabel label) {
		Font previousFont = label.getFont();
		Font newFont = new Font(previousFont.getFontName(), Font.BOLD, previousFont.getSize());
		label.setFont(newFont);
		return label;
	}
}
