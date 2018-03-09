package org.proteored.pacom.gui.importjobs;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker.StateValue;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.proteored.miapeapi.exceptions.MiapeDataInconsistencyException;
import org.proteored.pacom.analysis.exporters.util.ExporterUtil;
import org.proteored.pacom.gui.ImageManager;
import org.proteored.pacom.gui.tasks.MiapeExtractionTask;

import com.lowagie.text.Font;

public class ImportTaskTableCellRenderer extends DefaultTableCellRenderer {
	private static final Color selectedRowColor = new Color(48, 143, 255);
	private static final Color errorRowColor = new Color(255, 161, 161);
	private static final Color errorRowSelectedColor = new Color(255, 90, 90);

	/**
	 * 
	 */
	private static final long serialVersionUID = -4904825470475737392L;
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		MiapeExtractionTask task = ((ImportTasksTable) table).getImportTaskTableModel().getTaskByRowIndex(row);
		String inconsistencyError = null;
		try {
			task.checkConsistency();
		} catch (MiapeDataInconsistencyException e) {
			inconsistencyError = e.getMessage();
		}
		ImportTaskColumns importTaskColumn = ImportTaskColumns.getColumns().get(column);
		log.debug("Rendering column " + importTaskColumn.getName() + " in row " + row);
		String defaultToolTip = toHtml(getToolTipByValue(value, importTaskColumn, task));
		setToolTipText(defaultToolTip);
		setIcon(null);
		setHorizontalAlignment(SwingConstants.CENTER);

		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		if (importTaskColumn == ImportTaskColumns.ASSOCIATEDMSFILETYPE) {
			if (isSelected) {
				c.setForeground(ImportTaskTableCellRenderer.selectedRowColor);
			}
		} else if (importTaskColumn == ImportTaskColumns.VALID) {
			if (c instanceof JLabel) {

				((JLabel) c).setText(null);
				// check first if it has an error
				if (task.getResult() != null && task.getResult().getErrorMessage() != null) {
					((JLabel) c).setToolTipText(toHtml(task.getResult().getErrorMessage()));
					((JLabel) c).setIcon(ImageManager.getImageIcon(ImageManager.EXCLAMATION_SMALL));
				} else
				// check then if it has an inconsistency
				if (inconsistencyError != null) {
					((JLabel) c).setToolTipText(toHtml(inconsistencyError));
					((JLabel) c).setIcon(ImageManager.getImageIcon(ImageManager.EXCLAMATION_SMALL));

				} else {
					((JLabel) c).setToolTipText("Ready to submit!");
					((JLabel) c).setIcon(ImageManager.getImageIcon(ImageManager.VALID_SMALL));
				}
			}
		}

		final Color backgroundColor = getBackgroundColor(row, isSelected, task, inconsistencyError);
		c.setBackground(backgroundColor);
		this.setForeground(Color.black);
		if (isSelected) {
			if (hasFocus) {
				// this.setForeground(Color.getHSBColor(216, 59, 45));
				java.awt.Font bold = new java.awt.Font(table.getFont().getName(), Font.BOLD, table.getFont().getSize());
				this.setFont(bold);
			}

		}
		return c;
	}

	private String toHtml(String errorMessage) {
		StringBuilder sb = new StringBuilder();
		if (!errorMessage.startsWith("<html>")) {
			sb.append("<html>");
		}
		sb.append(errorMessage.replace("\n", "<br>"));
		if (!errorMessage.endsWith("</html>")) {
			sb.append("</html>");
		}
		return sb.toString();
	}

	private String getToolTipByValue(Object value, ImportTaskColumns importTaskColumn, MiapeExtractionTask task) {
		String defaultToolTip = null;

		if (value == null || "".equals(value) || "-".equals(value)) {
			value = "-";
			switch (importTaskColumn) {
			case PROJECT:
				defaultToolTip = "<html>You need to enter a project name.<br>"
						+ "Double click to directly enter the project name.</html>";
				break;
			case ASSOCIATEDMSFILE:
				defaultToolTip = "<html>No MS file associated.<br>Double click to select one.</html>";
				break;
			case FILE:
				defaultToolTip = "<html>No input file.<br>Double click to select one.</html>";
				break;
			default:
				break;
			}
		}
		if (defaultToolTip == null) {
			defaultToolTip = getToolTip(value.toString(), null);
			switch (importTaskColumn) {
			case JOBID:
				defaultToolTip = "Identifier of the import task: " + value;
				break;
			case PROGRESS:
				defaultToolTip = "Progress bar of the import task";
				break;
			case PROJECT:
				defaultToolTip = "<html><b>" + value + "</b><br>Double click to directly edit the project name.</html>";
				break;
			case FILE:
				defaultToolTip = "<html>Input file path:<br>"
						+ FilenameUtils.getPath(task.getParameters().getInputFile().getAbsolutePath()) + "<b>"
						+ FilenameUtils.getName(task.getParameters().getInputFile().getAbsolutePath()) + "</b>"
						+ "<br>Double click to select a different file.</html>";
				break;
			case ASSOCIATEDMSFILE:
				defaultToolTip = "<html>Associated MS File path:<br>"
						+ FilenameUtils.getPath(task.getParameters().getAssociatedMSFile().getAbsolutePath()) + "<b>"
						+ FilenameUtils.getName(task.getParameters().getAssociatedMSFile().getAbsolutePath()) + "</b>"
						+ "<br>Double click to select a different file.</html>";
				break;
			case FILETYPE:
				if (!value.equals("-")) {
					defaultToolTip = "<html>" + ((InputFileType) value).getPrimaryFileDescription()
							+ "<br>Click to change the input file type.</html>";
				} else {
					defaultToolTip = "Click to change the input file type.";
				}

				break;
			case ASSOCIATEDMSFILETYPE:
				if (!value.equals("-")) {
					defaultToolTip = "<html>" + ((AssociatedMSInputFileType) value).getDescription()
							+ "<br>Click to change the file type of the associated MS file.</html>";
				} else {
					defaultToolTip = "Click to change the file type of the associated MS file.";
				}
				break;
			case METADATA_TEMPLATE:
				defaultToolTip = "Click to select a MS metadata";
				break;
			case SEPARATOR:
				defaultToolTip = "Text separator (tab, space or comma) used in a table text input file";
				break;
			default:
				break;
			}
		}
		if (defaultToolTip == null && value != null) {
			defaultToolTip = value.toString();
		}
		return defaultToolTip;
	}

	private String getToolTip(String value, ImportTaskColumns column) {
		if (value == null || "".equals(value))
			return "";

		String[] splited = null;
		if (value.contains(ExporterUtil.VALUE_SEPARATOR))
			splited = value.split(ExporterUtil.VALUE_SEPARATOR);

		if (value.contains("\n"))
			splited = value.split("\n");

		String tmp = value;
		if (splited != null && splited.length > 0)
			tmp = getSplitedInNewLines(splited);
		return "<html>" + tmp + "</html>";

	}

	private String getSplitedInNewLines(String[] splited) {
		String ret = "";
		if (splited.length > 0)
			for (String string : splited) {
				if (!"".equals(ret))
					ret = ret + "<br>";
				ret = ret + string;
			}
		return ret;
	}

	private Color getBackgroundColor(int row, boolean isSelected, MiapeExtractionTask task, String inconsistencyError) {
		// if task has error
		if (task.getState() == StateValue.DONE && !task.isCancelled() && task.getResult().getErrorMessage() != null) {
			if (isSelected) {
				return errorRowSelectedColor;
			} else {
				return new Color(252, 194, 198);
			}
		}
		// if task is inconsistent
		if (inconsistencyError != null) {
			if (isSelected) {
				return errorRowSelectedColor;
			}
			return errorRowColor;
		}
		if (isSelected) {
			return selectedRowColor;
		}
		if (row % 2 == 1) { // impar
			return new Color(233, 248, 253);
		} else { // par
			return Color.white;
		}
	}
}
