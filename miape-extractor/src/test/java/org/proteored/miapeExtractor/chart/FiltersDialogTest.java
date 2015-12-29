package org.proteored.miapeExtractor.chart;

import org.proteored.miapeExtractor.analysis.gui.FiltersDialog;

public class FiltersDialogTest {

	public static void main(String[] args) {
		FiltersDialog dialog = FiltersDialog.getInstance(null,
				ExperimentsUtilTest.createExperiments(null));

		dialog.setVisible(true);
	}
}
