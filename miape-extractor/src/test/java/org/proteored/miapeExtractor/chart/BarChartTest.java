/*
 * BarChartTest.java Created on __DATE__, __TIME__
 */

package org.proteored.miapeExtractor.chart;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RefineryUtilities;
import org.proteored.miapeExtractor.analysis.charts.BarChart;
import org.proteored.miapeExtractor.analysis.gui.tasks.DatasetFactory;
import org.proteored.miapeExtractor.analysis.util.Util;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.IdentificationItemEnum;
import org.proteored.miapeapi.experiment.model.IdentificationSet;
import org.proteored.miapeapi.experiment.model.filters.ComparatorOperator;
import org.proteored.miapeapi.experiment.model.filters.Filter;
import org.proteored.miapeapi.experiment.model.filters.ScoreFilter;

/**
 * 
 * @author __USER__
 */
public class BarChartTest extends JFrame {

	/**
	 * Creates new form BarChartTest
	 * 
	 * @param title
	 */
	public BarChartTest(String title, String subtitle, String xLabel,
			String yLabel, IdentificationItemEnum plotItem, Boolean distPepMod,
			Filter filter) {
		super(title);

		ExperimentList experiments = ExperimentsUtilTest
				.createExperiments(null);

		List<Filter> filters = new ArrayList<Filter>();
		filters.add(filter);
		experiments.setFilters(filters);

		List<IdentificationSet> listOfIdSets = Util.getListOfIdSets(experiments
				.getExperiments());
		CategoryDataset dataset = DatasetFactory
				.createNumberIdentificationCategoryDataSet(listOfIdSets,
						IdentificationItemEnum.PEPTIDE, true, false, false,
						false);
		final BarChart proteinNumberBarChart = new BarChart(title, subtitle,
				xLabel, yLabel, dataset, PlotOrientation.VERTICAL);

		setContentPane(proteinNumberBarChart.getChartPanel());
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		// FDRFilter fdrFilter = ExperimentsUtilTest.getFDRFilter(0.8, true,
		// ExperimentsUtilTest.getDefaultSortingProteins(),
		// IdentificationItem.PROTEIN);
		// BarChartTest demo = new BarChartTest("Protein Histogram",
		// "Number of proteins identified from each replicate of each lab",
		// "Experiment",
		// "# proteins", IdentificationItem.PROTEIN, null, fdrFilter);
		// demo.pack();
		// RefineryUtilities.centerFrameOnScreen(demo);
		// demo.setVisible(true);
		//
		// BarChartTest demo2 = new BarChartTest("Peptide Histogram",
		// "Number of proteins identified from each replicate of each lab",
		// "Experiment",
		// "# peptides", IdentificationItem.PEPTIDE, false, fdrFilter);
		// demo2.pack();
		// RefineryUtilities.centerFrameOnScreen(demo2);
		// demo2.setVisible(true);

		ScoreFilter scoreFilter = new ScoreFilter(0.5f,
				ExperimentsUtilTest.PEPTIDE_SCORE_NAME,
				ComparatorOperator.LESS_OR_EQUAL,
				IdentificationItemEnum.PEPTIDE, null);
		BarChartTest demo3 = new BarChartTest(
				"Peptide Histogram",
				"Number of proteins identified from each replicate of each lab",
				"Experiment", "# peptides", IdentificationItemEnum.PEPTIDE,
				false, scoreFilter);
		demo3.pack();
		RefineryUtilities.centerFrameOnScreen(demo3);
		demo3.setVisible(true);
	}

}