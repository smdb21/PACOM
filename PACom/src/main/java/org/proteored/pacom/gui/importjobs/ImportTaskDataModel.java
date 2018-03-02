package org.proteored.pacom.gui.importjobs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JProgressBar;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.proteored.miapeapi.text.tsv.msi.TableTextFileSeparator;
import org.proteored.pacom.analysis.util.FileManager;
import org.proteored.pacom.gui.tasks.MiapeExtractionTask;
import org.proteored.pacom.utils.MiapeExtractionRunParameters;
import org.proteored.pacom.utils.MiapeExtractionRunParametersImpl;

public class ImportTaskDataModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2012588394900131630L;
	private List<MiapeExtractionTask> rows;
	private Map<Integer, MiapeExtractionTask> importTasksByID;
	private final static Logger log = Logger.getLogger(ImportTaskDataModel.class);

	public ImportTaskDataModel() {
		rows = new ArrayList<MiapeExtractionTask>();
		importTasksByID = new HashMap<Integer, MiapeExtractionTask>();
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	public int getColumnCount() {
		return ImportTaskColumns.values().length;
	}

	@Override
	public String getColumnName(int column) {
		return ImportTaskColumns.values()[column].getName();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rows.isEmpty()) {
			return null;
		}
		MiapeExtractionTask miapeExtractionTask = rows.get(rowIndex);
		Object value = "-";
		MiapeExtractionRunParameters parameters = miapeExtractionTask.getParameters();
		InputFileType inputFileType = parameters.getInputFileType();
		switch (ImportTaskColumns.values()[columnIndex]) {
		case FILE:
			if (parameters.getInputFile() != null) {
				value = FilenameUtils.getName(parameters.getInputFile().getAbsolutePath());
			}
			break;
		case FILETYPE:
			value = inputFileType;
			break;
		case JOBID:
			value = miapeExtractionTask.getRunIdentifier();
			break;
		case VALID:
			// this is the work of the renderer
			break;
		case PROGRESS:
			value = miapeExtractionTask.getProgress();
			break;
		case ASSOCIATEDMSFILE:
			if (parameters.getAssociatedMSFile() != null) {
				value = FilenameUtils.getName(parameters.getAssociatedMSFile().getAbsolutePath());
			}
			break;
		case ASSOCIATEDMSFILETYPE:
			value = parameters.getAssociatedMSFileType();
			break;
		case PROJECT:
			value = parameters.getProjectName();
			break;
		case METADATA_TEMPLATE:
			value = parameters.getMSMetadataTemplateName();
			// if the metadata templates are not loaded yet, return "Loading..."
			if (!FileManager.isMetadataTemplatesLoaded()) {
				return "Loading...";
			}
			break;
		case SEPARATOR:

			value = parameters.getSeparator();
			break;
		default:
			break;

		}
		return value;
	}

	/*
	 * JTable uses this method to determine the default renderer/ editor for
	 * each cell. If we didn't implement this method, then having a table with
	 * boolean values contain text ("true"/"false"), rather than a check box.
	 */
	@Override
	public Class getColumnClass(int c) {
		ImportTaskColumns importTaskColumn = ImportTaskColumns.values()[c];
		switch (importTaskColumn) {
		case FILETYPE:
		case ASSOCIATEDMSFILETYPE:
		case METADATA_TEMPLATE:
		case SEPARATOR:
			return JComboBox.class;
		// case PROJECT:
		// return JTextField.class;
		case PROGRESS:
			return JProgressBar.class;
		default:

			return String.class;
		}

	}

	/*
	 * Don't need to implement this method unless your table's editable.
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		// Note that the data/cell address is constant,
		// no matter where the cell appears onscreen.
		ImportTaskColumns importTaskColumn = ImportTaskColumns.values()[col];
		switch (importTaskColumn) {
		case FILE:
		case JOBID:
		case PROGRESS:
		case ASSOCIATEDMSFILE:
			return false;
		case SEPARATOR:
		case FILETYPE:
		case METADATA_TEMPLATE:
		case ASSOCIATEDMSFILETYPE:
		case PROJECT:
			return true;

		default:
			return false;

		}

	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (rows.size() > rowIndex) {
			MiapeExtractionTask importTask = rows.get(rowIndex);
			ImportTaskColumns column = ImportTaskColumns.values()[columnIndex];
			switch (column) {
			case PROGRESS:
				importTask.setTaskProgress((int) aValue);
				break;
			case FILETYPE:
				if (aValue instanceof InputFileType) {
					MiapeExtractionRunParameters parameters = importTask.getParameters();
					if (parameters instanceof MiapeExtractionRunParametersImpl) {
						InputFileType inputFileType = (InputFileType) aValue;
						AssociatedMSInputFileType associatedMSInputFileType = parameters.getAssociatedMSFileType();
						inputFileType = inputFileType.getCorrespondingInputFileType(associatedMSInputFileType);
						((MiapeExtractionRunParametersImpl) parameters).setInputFileType(inputFileType);
						if (!inputFileType.isHasAssociatedMS()) {
							((MiapeExtractionRunParametersImpl) parameters).setAssociatedMSFileType(null);
							fireTableCellUpdated(rowIndex,
									ImportTaskColumns.getColumns().indexOf(ImportTaskColumns.ASSOCIATEDMSFILETYPE));
						}

					}
				}
				break;
			case ASSOCIATEDMSFILETYPE:
				if (aValue instanceof AssociatedMSInputFileType) {
					MiapeExtractionRunParameters parameters = importTask.getParameters();
					if (parameters instanceof MiapeExtractionRunParametersImpl) {
						AssociatedMSInputFileType associatedMSInputType = (AssociatedMSInputFileType) aValue;
						((MiapeExtractionRunParametersImpl) parameters).setAssociatedMSFileType(associatedMSInputType);
						// update file type depending on this value
						updateFileTypeByAssociatedMSFileType(associatedMSInputType,
								(MiapeExtractionRunParametersImpl) parameters);

					}
				}
				break;
			case FILE:
				if (aValue instanceof File) {
					MiapeExtractionRunParameters parameters = importTask.getParameters();
					if (parameters instanceof MiapeExtractionRunParametersImpl) {
						((MiapeExtractionRunParametersImpl) parameters).setInputFile((File) aValue);

					}
				}
				break;
			case ASSOCIATEDMSFILE:
				if (aValue instanceof File) {
					MiapeExtractionRunParameters parameters = importTask.getParameters();
					if (parameters instanceof MiapeExtractionRunParametersImpl) {
						((MiapeExtractionRunParametersImpl) parameters).setAssociatedMSFile((File) aValue);

					}
				}
				break;
			case PROJECT:
				if (aValue instanceof String) {
					MiapeExtractionRunParameters parameters = importTask.getParameters();
					if (parameters instanceof MiapeExtractionRunParametersImpl) {
						((MiapeExtractionRunParametersImpl) parameters).setProjectName((String) aValue);

					}
				}
				break;
			case METADATA_TEMPLATE:
				if (aValue instanceof String) {
					MiapeExtractionRunParameters parameters = importTask.getParameters();
					if (parameters instanceof MiapeExtractionRunParametersImpl) {
						((MiapeExtractionRunParametersImpl) parameters).setTemplateName((String) aValue);
					}
				}
				break;
			case SEPARATOR:
				if (aValue instanceof TableTextFileSeparator) {
					MiapeExtractionRunParameters parameters = importTask.getParameters();
					if (parameters instanceof MiapeExtractionRunParametersImpl) {
						((MiapeExtractionRunParametersImpl) parameters).setSeparator((TableTextFileSeparator) aValue);
					}
				}
				break;
			default:
				break;
			}
			fireTableCellUpdated(rowIndex, columnIndex);
		}
	}

	/**
	 * This method will switch the {@link InputFileType} depending on the
	 * {@link AssociatedMSInputFileType} value
	 * 
	 * @param aValue
	 * @param parameters
	 */
	private void updateFileTypeByAssociatedMSFileType(AssociatedMSInputFileType associatedMSInputFileType,
			MiapeExtractionRunParametersImpl parameters) {
		if (associatedMSInputFileType != null) {
			InputFileType inputFileType = parameters.getInputFileType();
			InputFileType newInputFileType = inputFileType.getCorrespondingInputFileType(associatedMSInputFileType);
			parameters.setInputFileType(newInputFileType);
		}
	}

	public void addImportTask(MiapeExtractionTask importTask) {
		importTasksByID.put(importTask.getRunIdentifier(), importTask);
		rows.add(importTask);
		fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
	}

	public void clearRows() {
		rows.clear();
	}

	public MiapeExtractionTask getTaskByRowIndex(int rowIndex) {
		if (rows.size() > rowIndex && rowIndex >= 0) {
			return rows.get(rowIndex);
		}
		return null;
	}

	public List<MiapeExtractionTask> getImportTasks() {
		return this.rows;
	}

	public MiapeExtractionTask getTaskByID(int jobID) {
		for (MiapeExtractionTask miapeExtractionTask : rows) {
			if (miapeExtractionTask.getRunIdentifier() == jobID) {
				return miapeExtractionTask;
			}
		}
		return null;
	}

	public int indexOf(MiapeExtractionTask task) {
		return rows.indexOf(task);
	}

	/**
	 * Remove the task in row rowindex and returns its run identifier
	 * 
	 * @param rowIndex
	 * @return
	 */
	public int[] removeRows(int[] rowIndexes) {
		List<MiapeExtractionTask> toDelete = new ArrayList<MiapeExtractionTask>();
		int[] ret = new int[rowIndexes.length];
		for (int i = 0; i < rowIndexes.length; i++) {
			ret[i] = rows.get(rowIndexes[i]).getRunIdentifier();
			toDelete.add(rows.get(rowIndexes[i]));
		}
		for (MiapeExtractionTask task : toDelete) {
			rows.remove(task);
		}
		int firstRow = rowIndexes[0];
		int lastRow = rowIndexes[rowIndexes.length - 1];

		fireTableRowsDeleted(firstRow, lastRow);
		return ret;
	}

	public void replaceImportTask(MiapeExtractionTask task, MiapeExtractionTask newTask) {
		int index = this.rows.indexOf(task);
		this.rows.set(index, newTask);
		fireTableRowsUpdated(index, index);
	}

	public void fireTableRowsUpdated(MiapeExtractionTask task) {
		int indexOf = indexOf(task);
		if (indexOf >= 0) {
			fireTableRowsUpdated(indexOf, indexOf);
		}
	}

}
