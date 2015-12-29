package org.proteored.miapeExtractor.analysis.gui.components;

import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;

public class MyJTable extends JTable {
	private HashMap<Integer, List<DefaultCellEditor>> editors;
	private static final Logger log = Logger.getLogger("log4j.logger.org.proteored");

	public MyJTable() {
		super();
	}

	/**
	 * Sets the editors of the table
	 * 
	 * @param editors
	 *            a {@link HashMap} in which keys are columns and the value is
	 *            the list of {@link DefaultCellEditor} in a column
	 */
	public void setEditors(HashMap<Integer, List<DefaultCellEditor>> editors) {
		this.editors = editors;
	}

	@Override
	public TableCellEditor getCellEditor(int row, int column) {
		log.info("Getting cell editor (" + row + ", " + column + ")");
		int modelColumn = convertColumnIndexToModel(column);

		List<DefaultCellEditor> list = this.editors.get(modelColumn);
		if (list != null)
			if (row <= list.size() - 1) {
				return (TableCellEditor) list.get(row);

			} else if (list.size() == 1) {
				return (TableCellEditor) list.get(0);
			} else {
				return null;
			}
		return null;
	}
}
