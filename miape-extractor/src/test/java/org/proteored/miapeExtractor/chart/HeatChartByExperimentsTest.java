/*
 * HeatChartTest.java Created on __DATE__, __TIME__
 */

package org.proteored.miapeExtractor.chart;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.proteored.miapeExtractor.analysis.charts.HeatChart;
import org.proteored.miapeExtractor.analysis.charts.HeatMapChart;
import org.proteored.miapeExtractor.analysis.gui.tasks.DatasetFactory;
import org.proteored.miapeExtractor.analysis.util.Util;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.IdentificationItemEnum;
import org.proteored.miapeapi.experiment.model.IdentificationSet;

/**
 * 
 * @author __USER__
 */
public class HeatChartByExperimentsTest extends ApplicationFrame {

	/** Creates new form HeatChartTest */
	public HeatChartByExperimentsTest(String title,
			IdentificationItemEnum plotItem, Boolean distPepMod) {
		super(title);
		final ExperimentList experiments = ExperimentsUtilTest
				.createExperiments(null);
		System.out.println(experiments);

		List<String> rowList = new ArrayList<String>();
		List<String> columnList = new ArrayList<String>();
		Double min = null;
		Double max = null;
		List<IdentificationSet> listOfIdSets = Util.getListOfIdSets(experiments
				.getExperiments());

		double[][] dataset = DatasetFactory.createHeapMapDataSet(experiments,
				listOfIdSets, rowList, columnList, plotItem, distPepMod, 4,
				min, max, false);
		final HeatMapChart heatMapChart = new HeatMapChart(
				"Protein occurrence", dataset, rowList, columnList,
				HeatChart.SCALE_LINEAR);

		this.getContentPane()
				.add(heatMapChart.getjPanel(), BorderLayout.CENTER);
		pack();
		this.setVisible(true);
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		HeatChartByExperimentsTest demo = new HeatChartByExperimentsTest(
				"Protein HeatMap", IdentificationItemEnum.PROTEIN, null);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

		HeatChartByExperimentsTest demo2 = new HeatChartByExperimentsTest(
				"Peptide HeatMap by replicates",
				IdentificationItemEnum.PEPTIDE, false);
		demo2.pack();
		RefineryUtilities.centerFrameOnScreen(demo2);
		demo2.setVisible(true);
	}

}