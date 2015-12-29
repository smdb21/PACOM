package org.proteored.miapeExtractor.chart;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.proteored.miapeExtractor.analysis.charts.HistogramChart;
import org.proteored.miapeExtractor.analysis.gui.tasks.DatasetFactory;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.IdentificationItemEnum;
import org.proteored.miapeapi.experiment.model.IdentificationSet;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.miapeapi.experiment.model.filters.FDRFilter;
import org.proteored.miapeapi.experiment.model.filters.Filter;
import org.proteored.miapeapi.experiment.model.sort.Order;
import org.proteored.miapeapi.experiment.model.sort.SortingParameters;

public class ScoreDistributionsChartTest extends ApplicationFrame {

	public ScoreDistributionsChartTest(String title, String subtitle,
			String scoreName, IdentificationItemEnum plotItem) {
		super(title);
		SortingParameters spProteins = new SortingParameters(
				ExperimentsUtilTest.PROTEIN_SCORE_NAME, Order.DESCENDANT);
		SortingParameters spPeptides = new SortingParameters(
				ExperimentsUtilTest.PEPTIDE_SCORE_NAME, Order.DESCENDANT);
		;
		ExperimentList experiments = ExperimentsUtilTest
				.createExperiments(null);
		final List<Replicate> replicates = experiments.getExperiments()
				.iterator().next().getReplicates();
		List<IdentificationSet> idSets = new ArrayList<IdentificationSet>();
		for (Replicate identificationSet : replicates) {
			idSets.add(identificationSet);
		}
		FDRFilter fdrFilter = ExperimentsUtilTest.getFDRFilter(80, true,
				spPeptides, IdentificationItemEnum.PEPTIDE);
		FDRFilter fdrFilterpro = ExperimentsUtilTest.getFDRFilter(80, true,
				spProteins, IdentificationItemEnum.PROTEIN);
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(fdrFilterpro);
		filters.add(fdrFilter);
		experiments.setFilters(filters);

		HistogramDataset dataset = DatasetFactory.createScoreHistogramDataSet(
				idSets, scoreName, plotItem, 40, true,
				HistogramType.RELATIVE_FREQUENCY, rootPaneCheckingEnabled,
				false, false);

		final HistogramChart histogramChart = new HistogramChart(title,
				subtitle, dataset, scoreName);

		setContentPane(histogramChart.getChartPanel());
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		ScoreDistributionsChartTest demo = new ScoreDistributionsChartTest(
				"histogram plot",
				"comparing protein score distributions from a list of replicates",
				ExperimentsUtilTest.PROTEIN_SCORE_NAME,
				IdentificationItemEnum.PROTEIN);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

		ScoreDistributionsChartTest demo2 = new ScoreDistributionsChartTest(
				"histogram plot",
				"comparing peptide score distributions from a list of replicates",
				ExperimentsUtilTest.PEPTIDE_SCORE_NAME,
				IdentificationItemEnum.PEPTIDE);
		demo2.pack();
		RefineryUtilities.centerFrameOnScreen(demo2);
		demo2.setVisible(true);
	}
}
