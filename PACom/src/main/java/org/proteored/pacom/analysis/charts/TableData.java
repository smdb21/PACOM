package org.proteored.pacom.analysis.charts;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.proteored.pacom.analysis.exporters.gui.ScrollableJTable;
import org.proteored.pacom.analysis.exporters.util.ExportedColumns;

public class TableData extends JPanel {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");
	private final ScrollableJTable scrollableJTable;
	private final List<List<String>> tableData;
	private JTable table = new JTable(new DefaultTableModel());

	public TableData(List<List<String>> tableData) {
		super();
		this.tableData = tableData;
		int wide = 200 * tableData.get(0).size();
		this.scrollableJTable = new ScrollableJTable(table, wide);
		add(scrollableJTable);
		loadData();
	}

	private void loadData() {
		((DefaultTableModel) this.table.getModel()).getDataVector()
				.removeAllElements();

		List<String> columnsStringList = this.tableData.get(0);

		addColumns(columnsStringList);
		for (int i = 1; i < this.tableData.size(); i++) {
			final List<String> lineStringList = this.tableData.get(i);
			addNewRow(lineStringList);
		}

	}

	private void addNewRow(List<String> lineStringList) {
		DefaultTableModel defaultModel = getTableModel();

		defaultModel.addRow(lineStringList.toArray());

		// this.table.repaint();

	}

	private void addColumns(List<String> columnsStringList) {
		DefaultTableModel defaultModel = getTableModel();
		log.info("Adding colums " + columnsStringList.size() + " columns");
		if (columnsStringList != null) {

			for (Object columnName : columnsStringList) {
				defaultModel.addColumn(columnName);
			}
			log.info("Added " + this.table.getColumnCount() + " colums");
			for (int i = 0; i < this.table.getColumnCount(); i++) {
				TableColumn column = this.table.getColumnModel().getColumn(i);
				final ExportedColumns[] columHeaders = ExportedColumns.values();
				for (ExportedColumns header : columHeaders) {
					if (column.getHeaderValue().equals(header.getName()))
						column.setPreferredWidth(header.getDefaultWidth());
				}
				column.setResizable(true);
			}
		}
	}

	private DefaultTableModel getTableModel() {
		if (this.table == null)
			return null;

		TableModel model = this.table.getModel();
		if (model == null)
			model = new DefaultTableModel();
		final DefaultTableModel defaultModel = (DefaultTableModel) model;
		return defaultModel;
	}

}
