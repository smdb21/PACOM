package org.proteored.miapeExtractor.chart;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.proteored.miapeExtractor.analysis.charts.XYPointChart;
import org.proteored.miapeExtractor.analysis.gui.tasks.DatasetFactory;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.IdentificationItemEnum;
import org.proteored.miapeapi.experiment.model.IdentificationSet;
import org.proteored.miapeapi.experiment.model.filters.FDRFilter;
import org.proteored.miapeapi.experiment.model.filters.Filter;
import org.proteored.miapeapi.experiment.model.sort.Order;
import org.proteored.miapeapi.experiment.model.sort.SortingParameters;

public class ScatterChartTest extends ApplicationFrame {

	public ScatterChartTest(String title, String subtitle, String scoreName,
			IdentificationItemEnum plotItem, Boolean distModPep) {
		super(title);
		SortingParameters spProteins = new SortingParameters(
				ExperimentsUtilTest.PROTEIN_SCORE_NAME, Order.DESCENDANT);
		SortingParameters spPeptides = new SortingParameters(
				ExperimentsUtilTest.PEPTIDE_SCORE_NAME, Order.DESCENDANT);
		;

		FDRFilter fdrFilter = ExperimentsUtilTest.getFDRFilter(80, true,
				spPeptides, IdentificationItemEnum.PEPTIDE);
		FDRFilter fdrFilter2 = ExperimentsUtilTest.getFDRFilter(80, true,
				spProteins, IdentificationItemEnum.PROTEIN);
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(fdrFilter);
		filters.add(fdrFilter2);

		Experiment replicate1 = ExperimentsUtilTest.createExperiment("exp1",
				null);
		Experiment replicate2 = ExperimentsUtilTest.createExperiment("exp2",
				null);
		List<IdentificationSet> list = new ArrayList<IdentificationSet>();
		list.add(replicate1);
		list.add(replicate2);
		XYDataset dataset = null;
		if (plotItem.equals(IdentificationItemEnum.PEPTIDE))
			dataset = DatasetFactory.createScoreXYDataSet(list, scoreName,
					plotItem, distModPep, distModPep, true, false);
		else
			dataset = DatasetFactory.createScoreXYDataSet(list, scoreName,
					plotItem, distModPep, distModPep, true, false);
		final XYPointChart scatterChart = new XYPointChart(title, subtitle,
				dataset, scoreName, scoreName);

		setContentPane(scatterChart.getChartPanel());
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		// ScatterChartTest demo = new ScatterChartTest("Scatter plot1",
		// "comparing protein scores from two exp",
		// ExperimentsUtilTest.PROTEIN_SCORE_NAME,
		// IdentificationItem.PROTEIN, null);
		// demo.pack();
		// RefineryUtilities.centerFrameOnScreen(demo);
		// demo.setVisible(true);

		ScatterChartTest demo2 = new ScatterChartTest("Scatter plot2",
				"comparing peptides scores from two exp",
				ExperimentsUtilTest.PEPTIDE_SCORE_NAME,
				IdentificationItemEnum.PEPTIDE, false);
		demo2.pack();
		RefineryUtilities.centerFrameOnScreen(demo2);
		demo2.setVisible(true);
	}
}
