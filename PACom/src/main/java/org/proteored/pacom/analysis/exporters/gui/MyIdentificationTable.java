package org.proteored.pacom.analysis.exporters.gui;

import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.proteored.pacom.analysis.exporters.util.ExportedColumns;

public class MyIdentificationTable extends JTable {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	@Override
	protected JTableHeader createDefaultTableHeader() {
		return new JTableHeader(columnModel) {
			@Override
			public String getToolTipText(MouseEvent e) {
				java.awt.Point p = e.getPoint();
				int index = columnModel.getColumnIndexAtX(p.x);
				// int realIndex =
				// columnModel.getColumn(index).getModelIndex();
				String columnName = (String) columnModel.getColumn(index).getHeaderValue();
				String tip = getToolTipTextForColumn(columnName);
				// log.info("Tip = " + tip);
				if (tip != null)
					return tip;
				else
					return super.getToolTipText(e);
			}
		};
	}

	private String getToolTipTextForColumn(String columnName) {
		final ExportedColumns[] values = ExportedColumns.values();
		for (ExportedColumns exportedColumns : values) {
			if (exportedColumns.getName().equals(columnName)) {
				return exportedColumns.getDescription();
			}
		}
		return null;
	}

	public void clearData() {
		log.info("Clearing data of the table");
		TableModel model = getModel();
		if (model instanceof MyDefaultTableModel) {
			((MyDefaultTableModel) model).setRowCount(0);
			((MyDefaultTableModel) model).setColumnCount(0);
		}

	}
}
