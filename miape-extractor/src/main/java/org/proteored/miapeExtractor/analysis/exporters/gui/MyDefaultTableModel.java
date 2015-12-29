package org.proteored.miapeExtractor.analysis.exporters.gui;

import javax.swing.table.DefaultTableModel;

public class MyDefaultTableModel extends DefaultTableModel {

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

}
