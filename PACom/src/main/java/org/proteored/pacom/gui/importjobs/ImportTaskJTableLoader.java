package org.proteored.pacom.gui.importjobs;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.pacom.analysis.exporters.Exporter;
import org.proteored.pacom.analysis.exporters.util.ExporterUtil;
import org.proteored.pacom.gui.tasks.MiapeExtractionTask;

import gnu.trove.set.hash.THashSet;

public class ImportTaskJTableLoader extends SwingWorker<Void, Void> implements Exporter<JTable> {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private JTable table;

	private final Set<MiapeExtractionTask> importTasks = new THashSet<MiapeExtractionTask>();
	private String error;

	private static boolean running = false;

	public ImportTaskJTableLoader(Collection<MiapeExtractionTask> importTasks, JTable table2) {

		if (table2 == null) {
			throw new IllegalMiapeArgumentException("Table is null!!");
		}
		this.table = table2;

		this.importTasks.addAll(importTasks);

	}

	public ImportTaskJTableLoader(Collection<MiapeExtractionTask> importTasks, JTable table,
			boolean useTestOntologies) {
		this(importTasks, table);
		ExporterUtil.setTestOntologies(useTestOntologies);
	}

	@Override
	public JTable export() {
		synchronized (this) {
			if (running) {
				log.info("TableLoader already running. Cancelling this thread.");
				return this.table;
			}
			running = true;
		}
		firePropertyChange(DATA_EXPORTING_STARTING, null, null);
		if (this.importTasks.isEmpty()) {
			return this.table;
		}

		log.info("Starting JTable exporting");
		try {
			// ((DefaultTableModel)
			// this.table.getModel()).getDataVector().removeAllElements();
			((ImportTasksTable) this.table).clearData();

			List<String> columnsStringList = ImportTaskColumns.getColumnsString();

			addColumnsInTable(columnsStringList);
			int loadingProgress = 0;
			int total = importTasks.size();
			loadingProgress = 0;

			for (MiapeExtractionTask importTask : importTasks) {

				addNewRow(importTask);

				loadingProgress++;
				if (total > 0) {
					final int percentage = loadingProgress * 100 / total;
					// log.info(percentage + " %");
					setProgress(percentage);
				}
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						ImportTaskJTableLoader.this.table.repaint();
					}
				});

			}
		} catch (Exception e) {
			if (!(e instanceof InterruptedException)) {
				e.printStackTrace();
				error = e.getMessage();
			} else {
				log.info("Table Loading stopped");
			}
		} finally {
			this.table.repaint();
			if (error != null)
				this.cancel(true);
		}

		return table;
	}

	private void addColumnsInTable(List<String> columnsStringList) {
		ImportTaskDataModel defaultModel = getTableModel();
		log.info("Adding colums " + columnsStringList.size() + " columns");
		if (columnsStringList != null) {
			log.info("Added " + this.table.getColumnCount() + " colums");
			for (int i = 0; i < this.table.getColumnCount(); i++) {
				TableColumn column = this.table.getColumnModel().getColumn(i);
				final ImportTaskColumns[] columHeaders = ImportTaskColumns.values();
				for (ImportTaskColumns header : columHeaders) {
					if (column.getHeaderValue().equals(header.getName()))
						column.setPreferredWidth(header.getDefaultWidth());
				}
				column.setResizable(true);
			}
		}
	}

	private void addNewRow(MiapeExtractionTask importTask) {
		ImportTaskDataModel model = getTableModel();
		model.addImportTask(importTask);
	}

	private ImportTaskDataModel getTableModel() {
		if (this.table == null) {
			return null;
		}
		TableModel model = this.table.getModel();
		if (model == null) {
			model = new DefaultTableModel();
		}
		final ImportTaskDataModel defaultModel = (ImportTaskDataModel) model;
		return defaultModel;
	}

	@Override
	protected Void doInBackground() throws Exception {
		export();
		return null;
	}

	@Override
	protected void done() {
		synchronized (this) {
			running = false;
		}
		if (!this.isCancelled())
			firePropertyChange(DATA_EXPORTING_DONE, null, this.table.getRowCount());
		else {
			if (error != null) {
				firePropertyChange(DATA_EXPORTING_ERROR, null, error);
			} else {
				int num = 0;
				if (this.table != null)
					num = this.table.getRowCount();
				firePropertyChange(DATA_EXPORTING_CANCELED, null, num);
			}
		}
	}

	public JTable getTable() {
		return this.table;
	}

}
