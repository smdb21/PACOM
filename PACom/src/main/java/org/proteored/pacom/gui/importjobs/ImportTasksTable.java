package org.proteored.pacom.gui.importjobs;

import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker.StateValue;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.text.tsv.msi.TableTextFileSeparator;
import org.proteored.pacom.gui.tasks.MiapeExtractionTask;
import org.proteored.pacom.utils.MiapeExtractionRunParametersImpl;

public class ImportTasksTable extends JTable {
	private final static Logger log = Logger.getLogger(ImportTasksTable.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -541964131701232924L;

	public ImportTasksTable() {
		this.setModel(new ImportTaskDataModel());
		// Set renderer for painting different background colors
		this.setDefaultRenderer(Object.class, new ImportTaskTableCellRenderer());

		// progress column
		TableColumn progressColumn = this.getColumn(ImportTaskColumns.PROGRESS.getName());
		if (progressColumn != null) {
			progressColumn.setCellRenderer(new ProgressCellRender());
		}
		// file type
		TableColumn fileTypeColumn = this.getColumn(ImportTaskColumns.FILETYPE.getName());
		if (fileTypeColumn != null) {
			List<InputFileType> primaryFileTypesNames = InputFileType.primaryFileTypesNames();
			primaryFileTypesNames.add(0, null);
			UpdatableComboBoxEditor<InputFileType> fileTypeComboBoxEditor = new UpdatableComboBoxEditor<InputFileType>(
					primaryFileTypesNames);
			fileTypeColumn.setCellEditor(fileTypeComboBoxEditor);
		}
		// separator
		TableColumn separatorColumn = this.getColumn(ImportTaskColumns.SEPARATOR.getName());
		if (separatorColumn != null) {
			UpdatableComboBoxEditor<TableTextFileSeparator> separatorComboBoxEditor = new UpdatableComboBoxEditor<TableTextFileSeparator>(
					TableTextFileSeparator.valuesWithBlank());
			separatorColumn.setCellEditor(separatorComboBoxEditor);
		}
		// associated MS File Type
		TableColumn associatedMSFileTypeColumn = this.getColumn(ImportTaskColumns.ASSOCIATEDMSFILETYPE.getName());
		if (associatedMSFileTypeColumn != null) {
			UpdatableComboBoxEditor<AssociatedMSInputFileType> associatedMSFileTypeComboBoxEditor = new UpdatableComboBoxEditor<AssociatedMSInputFileType>(
					AssociatedMSInputFileType.valuesWithBlank());
			associatedMSFileTypeColumn.setCellEditor(associatedMSFileTypeComboBoxEditor);
		}
		// metadata template
		TableColumn metadataColumn = this.getColumn(ImportTaskColumns.METADATA_TEMPLATE.getName());
		if (metadataColumn != null) {
			UpdatableComboBoxEditor<String> metadataColumnEditor = new UpdatableComboBoxEditor<String>();
			metadataColumn.setCellEditor(metadataColumnEditor);
		}
		// add editor listener to fire tableDataChenged event
		CellEditorListener cellEditorListener = new CellEditorListener() {
			@Override
			public void editingStopped(ChangeEvent e) {
				ImportTasksTable.this.getImportTaskTableModel().fireTableDataChanged();
			}

			@Override
			public void editingCanceled(ChangeEvent e) {
			}
		};

		for (ImportTaskColumns column : ImportTaskColumns.values()) {
			TableColumn tableColumn = getColumn(column.getName());
			TableCellEditor cellEditor2 = tableColumn.getCellEditor();
			if (cellEditor2 != null) {
				cellEditor2.addCellEditorListener(cellEditorListener);
			}
		}
	}

	@Override
	protected JTableHeader createDefaultTableHeader() {
		JTableHeader jTableHeader = new JTableHeader(columnModel) {
			private static final long serialVersionUID = 3243436419296546234L;

			@Override
			public String getToolTipText(MouseEvent e) {
				java.awt.Point p = e.getPoint();
				int index = columnModel.getColumnIndexAtX(p.x);
				// int realIndex =
				// columnModel.getColumn(index).getModelIndex();
				// String columnName = (String)
				// columnModel.getColumn(index).getHeaderValue();
				String tip = ImportTaskColumns.getColumns().get(index).getDescription();
				// log.info("Tip = " + tip);
				if (tip != null)
					return tip;
				else
					return super.getToolTipText(e);
			}
		};
		DefaultTableCellRenderer defaultRenderer = (DefaultTableCellRenderer) jTableHeader.getDefaultRenderer();
		if (defaultRenderer instanceof DefaultTableCellRenderer) {
			defaultRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return jTableHeader;
	}

	public void clearData() {
		log.info("Clearing data of the table");
		TableModel model = getModel();
		if (model instanceof ImportTaskDataModel) {
			((ImportTaskDataModel) model).clearRows();
		}

	}

	public void addRow(MiapeExtractionTask importTask) {
		ImportTaskDataModel model = getImportTaskTableModel();
		if (model != null) {
			model.addImportTask(importTask);
		}
	}

	/**
	 * Looks for a mzML file in the table, just in case can be used to extract
	 * MIAPE MS information
	 * 
	 * @return
	 */
	public File searchForMzMLInTable() {
		ImportTaskDataModel model = (ImportTaskDataModel) this.getModel();
		int associatedMSfileColumnIndex = ImportTaskColumns.getColumns().indexOf(ImportTaskColumns.ASSOCIATEDMSFILE);
		int inputFileTypeIndex = ImportTaskColumns.getColumns().indexOf(ImportTaskColumns.FILETYPE);
		for (int row = 0; row < model.getRowCount(); row++) {
			Object inputFileTypeObj = model.getValueAt(row, inputFileTypeIndex);
			if (inputFileTypeObj != null && inputFileTypeObj instanceof InputFileType) {
				InputFileType inputFileType = (InputFileType) inputFileTypeObj;
				if (inputFileType == InputFileType.MZIDENTMLPLUSMZML) {
					Object attachedfileObj = model.getValueAt(row, associatedMSfileColumnIndex);
					if (attachedfileObj != null && attachedfileObj instanceof File) {
						return (File) attachedfileObj;
					}
				}
			}

		}
		return null;
	}

	public void associateMSFileToJobID(File file, int runIdentifier) {
		for (MiapeExtractionTask importTask : getImportTasks()) {
			if (importTask.getRunIdentifier() == runIdentifier) {
				((MiapeExtractionRunParametersImpl) importTask.getParameters()).setAssociatedMSFile(file);
			}
		}
		getImportTaskTableModel().fireTableDataChanged();
	}

	public void associateMSFileToAll(File file) {
		for (MiapeExtractionTask importTask : getImportTasks()) {
			((MiapeExtractionRunParametersImpl) importTask.getParameters()).setAssociatedMSFile(file);
		}
		getImportTaskTableModel().fireTableDataChanged();
	}

	public List<MiapeExtractionTask> getSelectedImportTasks() {
		List<MiapeExtractionTask> ret = new ArrayList<MiapeExtractionTask>();
		int[] selectedRows = getSelectedRows();
		if (selectedRows.length > 0) {
			for (int rowIndex : selectedRows) {
				MiapeExtractionTask importTask = getImportTaskTableModel().getTaskByRowIndex(rowIndex);
				if (importTask != null) {
					ret.add(importTask);
				}
			}
		}
		return ret;
	}

	public ImportTaskDataModel getImportTaskTableModel() {
		if (getModel() instanceof ImportTaskDataModel) {
			return (ImportTaskDataModel) this.getModel();
		}
		return null;
	}

	public List<MiapeExtractionTask> getImportTasks() {
		ImportTaskDataModel importTaskTableModel = getImportTaskTableModel();
		if (importTaskTableModel != null) {
			return importTaskTableModel.getImportTasks();
		}
		return null;
	}

	public void startThreadToUpdateProgressOnTask(int jobID) {
		MiapeExtractionTask task = getImportTaskTableModel().getTaskByID(jobID);
		if (task.getState() == StateValue.STARTED) {

			Runnable runnable = new Runnable() {

				@Override
				public void run() {
					log.info("Starting progress updater of task " + jobID);
					int progressColumnIndex = ImportTaskColumns.getColumns().indexOf(ImportTaskColumns.PROGRESS);
					int rowIndex = getImportTaskTableModel().indexOf(task);
					try {
						while (task.getState() == StateValue.STARTED) {
							Thread.currentThread().sleep(200);
							int progress = task.getProgress();
							if (progress == 100) {
								progress = 0;
							} else {
								progress += 5;
							}
							ImportTasksTable.this.getImportTaskTableModel().setValueAt(progress, rowIndex,
									progressColumnIndex);
							ImportTasksTable.this.getImportTaskTableModel().fireTableCellUpdated(rowIndex,
									progressColumnIndex);
						}
					} catch (InterruptedException e) {
						log.info(e.getMessage());
					}
					log.info("Progress updater of task " + jobID + " FINISHED");
				}
			};
			Thread thread = new Thread(runnable);
			thread.start();
		}

	}

}
