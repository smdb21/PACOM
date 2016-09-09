package org.proteored.pacom.analysis.exporters.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.experiment.model.grouping.ProteinEvidence;
import org.proteored.pacom.analysis.exporters.util.ExportedColumns;
import org.proteored.pacom.analysis.exporters.util.ExporterUtil;

import com.lowagie.text.Font;

public class MyTableCellRenderer extends DefaultTableCellRenderer {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		Component c = super.getTableCellRendererComponent(table, value,
				isSelected, hasFocus, row, column);
		final Color defaultColor = getColor(row);
		c.setBackground(defaultColor);
		String defaultToolTip = null;
		if (value == null)
			value = "";
		try {
			// protein ACC column
			final int proteinAccColumnIndex = table.getColumnModel()
					.getColumnIndex(ExportedColumns.PROTEIN_ACC.toString());
			if (proteinAccColumnIndex > 0 && proteinAccColumnIndex == column) {
				defaultToolTip = getToolTip(value.toString(),
						ExportedColumns.PROTEIN_ACC);
				// log.info("tooltip: " + defaultToolTip);
			}
		} catch (IllegalArgumentException e) {

		}
		if (defaultToolTip == null) {
			try {
				final int proteinGrouptTypeColumnIndex = table.getColumnModel()
						.getColumnIndex(
								ExportedColumns.PROTEIN_GROUP_TYPE.toString());
				if (proteinGrouptTypeColumnIndex > 0
						&& proteinGrouptTypeColumnIndex == column) {
					defaultToolTip = getToolTip(value.toString(),
							ExportedColumns.PROTEIN_GROUP_TYPE);
				}
			} catch (IllegalArgumentException e) {

			}
		}
		try {
			final int proteinDescColumnIndex = table.getColumnModel()
					.getColumnIndex(ExportedColumns.PROTEIN_DESC.toString());
			if (proteinDescColumnIndex > 0 && proteinDescColumnIndex == column) {
				defaultToolTip = getToolTip(value.toString(),
						ExportedColumns.PROTEIN_DESC);
			}
		} catch (IllegalArgumentException e) {

		}

		try {
			if (defaultToolTip == null)
				defaultToolTip = getToolTip(value.toString(), null);

		} catch (IllegalArgumentException e) {

		}

		if (defaultToolTip == null && value != null)
			defaultToolTip = value.toString();

		setToolTipText(defaultToolTip);

		if (isSelected) {
			this.setForeground(Color.RED);
			this.setBackground(Color.YELLOW);
			if (hasFocus) {
				java.awt.Font bold = new java.awt.Font(table.getFont()
						.getName(), Font.BOLD, table.getFont().getSize());

				this.setFont(bold);
			}

		} else {
			this.setForeground(Color.BLACK);
		}
		return c;
	}

	private String getToolTip(String value, ExportedColumns column) {
		if (value == null || "".equals(value))
			return "";

		String[] splited = null;
		if (value.contains(ExporterUtil.VALUE_SEPARATOR))
			splited = value.split(ExporterUtil.VALUE_SEPARATOR);

		if (value.contains("\n"))
			splited = value.split("\n");

		if (ExportedColumns.PROTEIN_ACC.equals(column)) {
			String tmp = value;
			if (splited != null && splited.length > 0)
				tmp = getSplitedInNewLines(splited);
			return "<html>"
					+ tmp
					+ "<br>Click twice for open protein on the Internet.</html>";
		} else if (ExportedColumns.PROTEIN_GROUP_TYPE.equals(column)) {
			String termDefinition = "";
			if (value.toString().equals(
					ProteinEvidence.AMBIGUOUSGROUP.toString()))
				termDefinition = "A protein sharing at least one peptide not matched to either conclusive or indistinguishable proteins.";
			else if (value.toString().equals(
					ProteinEvidence.CONCLUSIVE.toString()))
				termDefinition = "A protein identified by at least one unique (distinct, discrete) peptide.";
			else if (value.toString().equals(
					ProteinEvidence.INDISTINGUISHABLE.toString()))
				termDefinition = "A member of a group of proteins sharing all peptides that are exclusive to the group.";
			else if (value.toString().equals(
					ProteinEvidence.NONCONCLUSIVE.toString()))
				termDefinition = "A protein sharing all its matched peptides with either conclusive or indistinguishable proteins.";
			return "<html>" + value + "<br>" + termDefinition + "</html>";
		} else {
			String tmp = value;
			if (splited != null && splited.length > 0)
				tmp = getSplitedInNewLines(splited);
			return "<html>" + tmp + "</html>";
		}

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

	private Color getColor(int row) {
		if (row % 2 == 1) { // impar
			return new Color(233, 248, 253);
		} else { // par
			return Color.white;
		}
	}
}
