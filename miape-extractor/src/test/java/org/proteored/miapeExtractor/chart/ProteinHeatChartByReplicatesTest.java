/*
 * HeatChartTest.java Created on __DATE__, __TIME__
 */

package org.proteored.miapeExtractor.chart;

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
public class ProteinHeatChartByReplicatesTest extends ApplicationFrame {

	/** Creates new form HeatChartTest */
	public ProteinHeatChartByReplicatesTest(String title,
			IdentificationItemEnum plotItem, Boolean distModPep) {
		super(title);
		final ExperimentList experiments = ExperimentsUtilTest
				.createExperiments(null);

		List<String> rowList = new ArrayList<String>();
		List<String> columnList = new ArrayList<String>();
		Double min = null;
		Double max = null;
		List<IdentificationSet> listOfIdSets = Util.getListOfIdSets(experiments
				.getExperiments());
		double[][] dataset = DatasetFactory.createHeapMapDataSet(experiments,
				listOfIdSets, rowList, columnList,
				IdentificationItemEnum.PROTEIN, null, 3, min, max, false);

		final HeatMapChart heatMapChart = new HeatMapChart(title, dataset,
				rowList, columnList, HeatChart.SCALE_EXPONENTIAL);

		setContentPane(heatMapChart.getjPanel());
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		ProteinHeatChartByReplicatesTest demo = new ProteinHeatChartByReplicatesTest(
				"Protein HeatMap by Experiments",
				IdentificationItemEnum.PROTEIN, null);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

		ProteinHeatChartByReplicatesTest demo2 = new ProteinHeatChartByReplicatesTest(
				"Peptide HeatMap by Experiments",
				IdentificationItemEnum.PEPTIDE, false);
		demo2.pack();
		RefineryUtilities.centerFrameOnScreen(demo2);
		demo2.setVisible(true);
	}

}