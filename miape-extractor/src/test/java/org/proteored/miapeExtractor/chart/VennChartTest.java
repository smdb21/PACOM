/*
 * BarChartTest.java Created on __DATE__, __TIME__
 */

package org.proteored.miapeExtractor.chart;

import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.proteored.miapeExtractor.analysis.charts.VennChart;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.IdentificationItemEnum;
import org.proteored.miapeapi.xml.util.ProteinGroupComparisonType;

/**
 * 
 * @author __USER__
 */
public class VennChartTest extends ApplicationFrame {

	/**
	 * Creates new form BarChartTest
	 * 
	 * @param title
	 */
	public VennChartTest(String title, IdentificationItemEnum plotItem,
			Boolean distModPEp, Boolean countNonConclusive) {
		super(title);

		final Experiment list1 = ExperimentsUtilTest.createExperiment("NAME1",
				null);
		final Experiment list2 = ExperimentsUtilTest.createExperiment("NAME2",
				null);
		final Experiment list3 = ExperimentsUtilTest.createExperiment("NAME3",
				null);

		final VennChart venChart = new VennChart("Intersections", list1, title
				+ "1", list2, title + "2", list3, title + "3", plotItem,
				distModPEp, countNonConclusive,
				ProteinGroupComparisonType.ALL_PROTEINS);

		setContentPane(venChart.getChartPanel());
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		VennChartTest demo = new VennChartTest("proteins",
				IdentificationItemEnum.PROTEIN, null, null);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

		VennChartTest demo2 = new VennChartTest("peptides",
				IdentificationItemEnum.PEPTIDE, false, true);
		demo2.pack();
		RefineryUtilities.centerFrameOnScreen(demo2);
		demo2.setVisible(true);
	}

}