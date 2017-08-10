package org.proteored.pacom.analysis.gui.tasks;

import java.awt.Color;
import java.awt.Panel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.XYDataset;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.experiment.VennData;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.IdentificationItemEnum;
import org.proteored.miapeapi.experiment.model.IdentificationSet;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.miapeapi.experiment.model.filters.FDRFilter;
import org.proteored.miapeapi.experiment.model.sort.ProteinGroupComparisonType;
import org.proteored.pacom.analysis.charts.BarChart;
import org.proteored.pacom.analysis.charts.CombinedChart;
import org.proteored.pacom.analysis.charts.HeatMapChart;
import org.proteored.pacom.analysis.charts.HistogramChart;
import org.proteored.pacom.analysis.charts.LineCategoryChart;
import org.proteored.pacom.analysis.charts.PieChart;
import org.proteored.pacom.analysis.charts.SpiderChart;
import org.proteored.pacom.analysis.charts.StackedBarChart;
import org.proteored.pacom.analysis.charts.TableData;
import org.proteored.pacom.analysis.charts.VennChart;
import org.proteored.pacom.analysis.charts.WordCramChart;
import org.proteored.pacom.analysis.charts.XYLineChart;
import org.proteored.pacom.analysis.charts.XYPointChart;
import org.proteored.pacom.analysis.gui.AdditionalOptionsPanelFactory;
import org.proteored.pacom.analysis.gui.ChartManagerFrame;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

public class ChartCreatorTask extends SwingWorker<Object, Void> {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");
	public static final String CHART_GENERATED = "Chart generated";
	public static final String CHART_ERROR_GENERATED = "Chart error generated";
	public static final String DATASET_PROGRESS = "Data set progress";
	private final String chartType;
	private final String option;
	private final ChartManagerFrame parent;
	private final ExperimentList experimentList;
	private String error;
	private final AdditionalOptionsPanelFactory optionsFactory;
	private final boolean countNonConclusiveProteins;
	private final Map<String, VennChart> vennChartMap = new THashMap<String, VennChart>();

	public ChartCreatorTask(ChartManagerFrame parent, String chartType, String optionParam,
			ExperimentList experimentList) {
		this.chartType = chartType;
		if (optionParam == null)
			option = "";
		else
			option = optionParam;
		this.experimentList = experimentList;
		this.parent = parent;
		optionsFactory = parent.getOptionsFactory();
		countNonConclusiveProteins = parent.countNonConclusiveProteins();
		log.info("Creating a new chart creator: CountNonConclusive:" + countNonConclusiveProteins);
	}

	@Override
	protected Object doInBackground() throws Exception {
		parent.setToolTipInformation3("");
		error = null;
		try {
			Object ret = null;
			if (ChartManagerFrame.PEPTIDE_SCORE_DISTRIBUTION.equals(chartType)) {
				ret = showScoreDistributionHistogramLineChart(IdentificationItemEnum.PEPTIDE);
			} else if (ChartManagerFrame.PROTEIN_SCORE_DISTRIBUTION.equals(chartType)) {
				ret = showScoreDistributionHistogramLineChart(IdentificationItemEnum.PROTEIN);
			} else if (ChartManagerFrame.PEPTIDE_SCORE_COMPARISON.equals(chartType)) {
				ret = showScoreComparisonScatterChart(IdentificationItemEnum.PEPTIDE);
			} else if (ChartManagerFrame.PROTEIN_SCORE_COMPARISON.equals(chartType)) {
				ret = showScoreComparisonScatterChart(IdentificationItemEnum.PROTEIN);
			} else if (ChartManagerFrame.PEPTIDE_NUMBER_HISTOGRAM.equals(chartType)) {
				ret = showHistogramChart(IdentificationItemEnum.PEPTIDE);
			} else if (ChartManagerFrame.PROTEIN_NUMBER_HISTOGRAM.equals(chartType)) {
				ret = showHistogramChart(IdentificationItemEnum.PROTEIN);
			} else if (ChartManagerFrame.PROTEIN_REPEATABILITY.equals(chartType)) {
				ret = showRepeatabilityHistogramStackedChart(IdentificationItemEnum.PROTEIN);
			} else if (ChartManagerFrame.PEPTIDE_REPEATABILITY.equals(chartType)) {
				ret = showRepeatabilityHistogramStackedChart(IdentificationItemEnum.PEPTIDE);
			} else if (ChartManagerFrame.PEPTIDE_OVERLAPING.equals(chartType)) {
				ret = showOverlappingChart(IdentificationItemEnum.PEPTIDE);
			} else if (ChartManagerFrame.PROTEIN_OVERLAPING.equals(chartType)) {
				ret = showOverlappingChart(IdentificationItemEnum.PROTEIN);
			} else if (ChartManagerFrame.PEPTIDE_OCCURRENCE_HEATMAP.equals(chartType)) {
				ret = showPeptideOccurrenceHeatMapChart(false);
			} else if (ChartManagerFrame.PSMS_PER_PEPTIDE_HEATMAP.equals(chartType)) {
				ret = showPeptideOccurrenceHeatMapChart(true);
			} else if (ChartManagerFrame.PROTEIN_OCURRENCE_HEATMAP.equals(chartType)) {
				ret = showProteinOccurrenceHeatMapChart();
			} else if (ChartManagerFrame.PEPTIDES_PER_PROTEIN_HEATMAP.equals(chartType)) {
				ret = showPeptidesPerProteinHeatMapChart(false);
			} else if (ChartManagerFrame.PSMS_PER_PROTEIN_HEATMAP.equals(chartType)) {
				ret = showPeptidesPerProteinHeatMapChart(true);
			} else if (ChartManagerFrame.MODIFICATED_PEPTIDE_NUMBER.equals(chartType)) {
				ret = showModificatedPeptidesBarChart();
			} else if (ChartManagerFrame.MODIFICATION_SITES_NUMBER.equals(chartType)) {
				ret = showModificationsSitesBarChart();
			} else if (ChartManagerFrame.PEPTIDE_MODIFICATION_DISTRIBUTION.equals(chartType)) {
				ret = showModificationsNumberDistributionBarChart();
			} else if (ChartManagerFrame.PEPTIDE_MONITORING.equals(chartType)) {
				ret = showPeptideMonitoringBarChart();
			} else if (ChartManagerFrame.PROTEIN_COVERAGE_DISTRIBUTION.equals(chartType)) {
				ret = showProteinCoverageDistributionChart();
			} else if (ChartManagerFrame.PROTEIN_COVERAGE.equals(chartType)) {
				ret = showProteinCoverageHistogramChart();
			} else if (ChartManagerFrame.FDR.equals(chartType)) {
				ret = showFDRChart();
			} else if (ChartManagerFrame.PROTEIN_SENSITIVITY_SPECIFICITY.equals(chartType)) {
				ret = showProteinSensitivitySpecificityChart();
			} else if (ChartManagerFrame.MISSEDCLEAVAGE_DISTRIBUTION.equals(chartType)) {
				ret = showMissedCleavagesNumberDistributionBarChart();
			} else if (ChartManagerFrame.PROTEIN_GROUP_TYPES.equals(chartType)) {
				ret = showProteinGroupTypesDistributionBarChart();
			} else if (ChartManagerFrame.PEPTIDE_MASS_DISTRIBUTION.equals(chartType)) {
				ret = showPeptideMassDistributionChart();
			} else if (ChartManagerFrame.PEPTIDE_LENGTH_DISTRIBUTION.equals(chartType)) {
				ret = showPeptideLengthDistributionChart();
			} else if (ChartManagerFrame.PEPTIDE_CHARGE_HISTOGRAM.equals(chartType)) {
				ret = showPeptideChargeBarChart();
				// } else if (ChartManagerFrame.CHR16_MAPPING.equals(chartType))
				// {
				// ret = showChr16MappingBarChart();
			} else if (ChartManagerFrame.SINGLE_HIT_PROTEINS.equals(chartType)) {
				ret = showSingleHitProteinsChart();
			} else if (ChartManagerFrame.PEPTIDE_NUMBER_IN_PROTEINS.equals(chartType)) {
				ret = showNumberPeptidesInProteinsChart();
			} else if (ChartManagerFrame.DELTA_MZ_OVER_MZ.equals(chartType)) {
				ret = showDeltaMzScatterChart();
			} else if (ChartManagerFrame.PSM_PEP_PROT.equals(chartType)) {
				ret = shotPSMPEPPROT_LineChart();
			} else if (ChartManagerFrame.FDR_VS_SCORE.equals(chartType)) {
				ret = showFDRPlots();
			} else if (ChartManagerFrame.CHR_MAPPING.equals(chartType)) {
				ret = showAllChrMappingBarChart();
			} else if (ChartManagerFrame.CHR_PEPTIDES_MAPPING.equals(chartType)) {
				ret = showAllChrPeptideMappingBarChart();
			} else if (ChartManagerFrame.HUMAN_CHROMOSOME_COVERAGE.equals(chartType)) {
				ret = showAllChrCoverageSpiderChart();
			} else if (ChartManagerFrame.EXCLUSIVE_PROTEIN_NUMBER.equals(chartType)) {
				ret = showExclusiveIdentificationNumberChart(IdentificationItemEnum.PROTEIN);
			} else if (ChartManagerFrame.EXCLUSIVE_PEPTIDE_NUMBER.equals(chartType)) {
				ret = showExclusiveIdentificationNumberChart(IdentificationItemEnum.PEPTIDE);
			} else if (ChartManagerFrame.PEPTIDE_PRESENCY_HEATMAP.equals(chartType)) {
				ret = showPeptidePresencyHeatMapChart();
			} else if (ChartManagerFrame.PEPTIDE_NUM_PER_PROTEIN_MASS.equals(chartType)) {
				ret = showPeptidePerProteinMassDistributionChart();
			} else if (ChartManagerFrame.PROTEIN_NAME_CLOUD.equals(chartType)) {
				ret = showWordCramChart();
			} else if (ChartManagerFrame.PEPTIDE_RT.equals(chartType)) {
				ret = showRetentionTimeDistributionChart();
			} else if (ChartManagerFrame.PEPTIDE_RT_COMPARISON.equals(chartType)) {
				ret = showRetentionTimeComparisonChart();
			} else if (ChartManagerFrame.SINGLE_RT_COMPARISON.equals(chartType)) {
				ret = showSingleRetentiontimeChart();
			} else if (ChartManagerFrame.SPECTROMETERS.equals(chartType)) {
				ret = showSpectrometersChart();
			} else if (ChartManagerFrame.INPUT_PARAMETERS.equals(chartType)) {
				ret = showInputParametersChart();
			} else if (ChartManagerFrame.PEPTIDE_COUNTING_HISTOGRAM.equals(chartType)) {
				ret = showPeptideCountingHistogramChart();
			} else if (ChartManagerFrame.PEPTIDE_COUNTING_VS_SCORE.equals(chartType)) {
				ret = showPeptideCountingVsScoreScatterChart();
			}
			if (ret != null)
				return ret;
		} catch (IllegalMiapeArgumentException e) {
			e.printStackTrace();
			error = e.getMessage();
			log.warn(e.getMessage());
			JPanel jpanel = new JPanel();
			jpanel.add(new JLabel(e.getMessage()));
			return jpanel;
		}
		if (error == null)
			error = "Error generating chart.";
		JPanel jpanel = new JPanel();
		jpanel.add(new JLabel(error));
		return jpanel;
		// firePropertyChange(ChartManagerDialog.CHART_GENERATED_ERROR, null,
		// error);
		// return null;
	}

	private Object showSingleRetentiontimeChart() {
		parent.setInformation1(parent.getCurrentChartType());
		String xAxisLabel;
		final List<String> sequences = optionsFactory.getSelectedPeptides();
		if (sequences == null)
			throw new IllegalMiapeArgumentException("Select a peptide to show the chart");
		boolean showInMinutes = optionsFactory.showInMinutes();
		String yAxisLabel = "RT (min)";
		if (!showInMinutes)
			yAxisLabel = "RT (sg)";

		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();

		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			xAxisLabel = "level 2";

			CategoryDataset dataset = DatasetFactory.createSinglePeptideRTMonitoringCategoryDataSet(idSets, sequences,
					showInMinutes);
			BarChart chart = new BarChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
					xAxisLabel, yAxisLabel, dataset, plotOrientation);
			chart.setNonIntegerItemLabels();
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			xAxisLabel = "experiment list";

			CategoryDataset dataset = DatasetFactory.createSinglePeptideRTMonitoringCategoryDataSet(idSets, sequences,
					showInMinutes);
			BarChart chart = new BarChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
					xAxisLabel, yAxisLabel, dataset, plotOrientation);
			chart.setNonIntegerItemLabels();

			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			xAxisLabel = "experiment";

			CategoryDataset dataset = DatasetFactory.createSinglePeptideRTMonitoringCategoryDataSet(idSets, sequences,
					showInMinutes);
			BarChart chart = new BarChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
					xAxisLabel, yAxisLabel, dataset, plotOrientation);
			chart.setNonIntegerItemLabels();

			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				xAxisLabel = experiment.getName();

				CategoryDataset dataset = DatasetFactory.createSinglePeptideRTMonitoringCategoryDataSet(idSets,
						sequences, showInMinutes);
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				chart.setNonIntegerItemLabels();

				chartList.add(chart.getChartPanel());
			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	private Object showInputParametersChart() {
		parent.setInformation1(parent.getCurrentChartType());

		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);

		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			List<List<String>> tableData = DatasetFactory.createInputParametersTableData(idSets);
			JPanel panel = new TableData(tableData);
			return panel;
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			List<List<String>> tableData = DatasetFactory.createInputParametersTableData(idSets);
			JPanel panel = new TableData(tableData);
			return panel;
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				try {
					idSets = getIdentificationSets(experiment.getFullName(), null, false);

					if (!idSets.isEmpty()) {
						List<List<String>> tableData = DatasetFactory.createInputParametersTableData(idSets);
						JPanel panel = new TableData(tableData);
						chartList.add(panel);
					}
				} catch (IllegalMiapeArgumentException e) {
					JPanel jpanel = new JPanel();
					jpanel.add(new JLabel("<html>Error generating table for " + experiment.getName() + "<br>"
							+ e.getMessage() + "</html>"));
					chartList.add(jpanel);
				}

			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			List<List<String>> tableData = DatasetFactory.createInputParametersTableData(idSets);
			JPanel panel = new TableData(tableData);
			return panel;
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());

		}
		return null;
	}

	private Object showSpectrometersChart() {
		parent.setInformation1(parent.getCurrentChartType());

		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);

		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			List<List<String>> tableData = DatasetFactory.createSpectrometersTableData(idSets);
			JPanel panel = new TableData(tableData);
			return panel;
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			List<List<String>> tableData = DatasetFactory.createSpectrometersTableData(idSets);
			JPanel panel = new TableData(tableData);
			return panel;
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				try {
					idSets = getIdentificationSets(experiment.getFullName(), null, false);

					if (!idSets.isEmpty()) {
						List<List<String>> tableData = DatasetFactory.createSpectrometersTableData(idSets);
						JPanel panel = new TableData(tableData);
						chartList.add(panel);
					}
				} catch (IllegalMiapeArgumentException e) {
					JPanel jpanel = new JPanel();
					jpanel.add(new JLabel("<html>Error generating table for " + experiment.getName() + "<br>"
							+ e.getMessage() + "</html>"));
					chartList.add(jpanel);
				}

			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			List<List<String>> tableData = DatasetFactory.createSpectrometersTableData(idSets);
			JPanel panel = new TableData(tableData);
			return panel;
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());

		}
		return null;
	}

	private Object showRetentionTimeComparisonChart() {
		parent.setInformation1(parent.getCurrentChartType());

		final Map<String, JCheckBox> scoreComparisonJCheckBoxes = optionsFactory.getIdSetsJCheckBoxes();

		String xAxisLabel = "RT (sg)";
		String yAxisLabel = "RT (sg)";

		boolean showRegressionLine = optionsFactory.showRegressionLine();
		boolean showDiagonalLine = optionsFactory.showDiagonalLine();
		boolean inMinutes = optionsFactory.showInMinutes();
		if (inMinutes) {
			xAxisLabel = "RT (min)";
			yAxisLabel = "RT (min)";
		}
		List<IdentificationSet> idSets = getIdentificationSets(null, scoreComparisonJCheckBoxes, false);

		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			XYDataset dataset = DatasetFactory.createRTXYDataSet(idSets, inMinutes);
			XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
			if (showRegressionLine)
				chart.addRegressionLine(true);
			if (showDiagonalLine)
				chart.addDiagonalLine();
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			XYDataset dataset = DatasetFactory.createRTXYDataSet(idSets, inMinutes);
			XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
			if (showRegressionLine)
				chart.addRegressionLine(true);
			if (showDiagonalLine)
				chart.addDiagonalLine();
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				try {
					idSets = getIdentificationSets(experiment.getFullName(), scoreComparisonJCheckBoxes, false);

					if (!idSets.isEmpty()) {
						XYDataset dataset = DatasetFactory.createRTXYDataSet(idSets, inMinutes);
						XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
								parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
						if (showRegressionLine)
							chart.addRegressionLine(true);
						if (showDiagonalLine)
							chart.addDiagonalLine();
						chartList.add(chart.getChartPanel());
					}
				} catch (IllegalMiapeArgumentException e) {
					JPanel jpanel = new JPanel();
					jpanel.add(new JLabel("<html>Error generating chart for " + experiment.getName() + "<br>"
							+ e.getMessage() + "</html>"));
					chartList.add(jpanel);
				}

			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			throw new IllegalMiapeArgumentException(
					"At least two series are needed to generate the chart. Select a lower level in the chart option");
		}
		return null;
	}

	private Object showRetentionTimeDistributionChart() {
		parent.setInformation1(parent.getCurrentChartType());
		int bins = optionsFactory.getHistogramBins();
		HistogramType histogramType = optionsFactory.getHistogramType();
		String xAxisLabel;
		xAxisLabel = "RT (sg)";

		boolean showParent = optionsFactory.isTotalSerieShown();
		boolean inMinutes = optionsFactory.showInMinutes();
		final Map<String, JCheckBox> idSetsJCheckBoxes = optionsFactory.getIdSetsJCheckBoxes();
		if (inMinutes)
			xAxisLabel = "RT (min)";
		List<IdentificationSet> idSets = getIdentificationSets(null, idSetsJCheckBoxes, showParent);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			HistogramDataset dataset = DatasetFactory.createPeptideRTHistogramDataSet(idSets, bins, histogramType,
					inMinutes);
			HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			HistogramDataset dataset = DatasetFactory.createPeptideRTHistogramDataSet(idSets, bins, histogramType,
					inMinutes);
			HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			HistogramDataset dataset = DatasetFactory.createPeptideRTHistogramDataSet(idSets, bins, histogramType,
					inMinutes);
			HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), idSetsJCheckBoxes, showParent);
				HistogramDataset dataset = DatasetFactory.createPeptideRTHistogramDataSet(idSets, bins, histogramType,
						inMinutes);
				HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType), experiment.getName(),
						dataset, xAxisLabel);
				// chart.setXRangeValues(0, 100);
				chartList.add(chart.getChartPanel());
			}
			return chartList;
			// this.jPanelChart.addGraphicPanel(chartList);
		}
		return null;
	}

	private Object showWordCramChart() {
		parent.setInformation1(parent.getCurrentChartType());
		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		int maxNumWords = optionsFactory.getMaxNumberWords();
		final JLabel label = optionsFactory.getJLabelSelectedWord();
		final int minWordLength = optionsFactory.getMinWordLength();
		// final JLabel label2 = optionsFactory.getJLabelSelectedProteins();

		List<String> skipWords = optionsFactory.getSkipWords();
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST)) {
			WordCramChart chart = new WordCramChart(idSets.get(0), skipWords, minWordLength);
			chart.selectedWordLabel(label).selectedProteinsLabel(parent.getTextAreaStatus())
					.font(optionsFactory.getFont()).maximumNumberOfWords(maxNumWords);
			return chart;
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<Panel> chartList = new ArrayList<Panel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				WordCramChart chart = new WordCramChart(experiment, skipWords, minWordLength);
				chart.selectedWordLabel(label).selectedProteinsLabel(parent.getTextAreaStatus())
						.font(optionsFactory.getFont()).maximumNumberOfWords(maxNumWords);
				chartList.add(chart);
			}
			return chartList;

		}
		return null;
	}

	private Object showPeptidePerProteinMassDistributionChart() {
		parent.setInformation1(parent.getCurrentChartType());
		int bins = optionsFactory.getHistogramBins();
		HistogramType histogramType = optionsFactory.getHistogramType();
		boolean totalSerieShown = optionsFactory.isTotalSerieShown();
		String xAxisLabel;

		xAxisLabel = "log2 (nº pept per protein / protein MW (Da))";
		boolean retrieveProteinSeqs = false;
		if (!parent.isProteinSequencesRetrieved()) {
			final int selectedOption = JOptionPane.showConfirmDialog(parent,
					"<html>In order to build this chart, the program will retrieve the protein sequence from the Internet,<br>which can take several minutes, depending on the number of proteins.<br>Are you sure you want to continue?</html>",
					"Warning", JOptionPane.YES_NO_OPTION);

			if (selectedOption == JOptionPane.YES_OPTION)
				retrieveProteinSeqs = true;
			parent.setProteinSequencesRetrieved(retrieveProteinSeqs);
		}
		List<IdentificationSet> idSets = getIdentificationSets(null, null, totalSerieShown);

		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			HistogramDataset dataset = DatasetFactory.createNumPeptidesPerProteinMassDistribution(idSets, bins,
					histogramType, retrieveProteinSeqs, countNonConclusiveProteins);
			HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			HistogramDataset dataset = DatasetFactory.createNumPeptidesPerProteinMassDistribution(idSets, bins,
					histogramType, retrieveProteinSeqs, countNonConclusiveProteins);

			HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			HistogramDataset dataset = DatasetFactory.createNumPeptidesPerProteinMassDistribution(idSets, bins,
					histogramType, retrieveProteinSeqs, countNonConclusiveProteins);
			HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				HistogramDataset dataset = DatasetFactory.createNumPeptidesPerProteinMassDistribution(idSets, bins,
						histogramType, retrieveProteinSeqs, countNonConclusiveProteins);
				HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType), experiment.getName(),
						dataset, xAxisLabel);
				// chart.setXRangeValues(0, 100);
				chartList.add(chart.getChartPanel());
			}
			return chartList;
			// this.jPanelChart.addGraphicPanel(chartList);
		}
		return null;
	}

	private Object showPeptidePerProteinMassHistogramChart() {
		if (true)
			throw new UnsupportedOperationException();
		// TODO is not finished
		parent.setInformation1(parent.getCurrentChartType());
		int bins = optionsFactory.getHistogramBins();
		HistogramType histogramType = optionsFactory.getHistogramType();
		boolean totalSerieShown = optionsFactory.isTotalSerieShown();
		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		String xAxisLabel = "log2 (nº pept per protein / protein MW (Da))";
		String yAxisLabel = "Avg number of peptides";
		boolean retrieveProteinSeqs = false;
		if (!parent.isProteinSequencesRetrieved()) {
			final int selectedOption = JOptionPane.showConfirmDialog(parent,
					"<html>In order to build this chart, the program will retrieve the protein sequence from the Internet,<br>which can take several minutes, depending on the number of proteins.<br>Are you sure you want to continue?</html>",
					"Warning", JOptionPane.YES_NO_OPTION);

			if (selectedOption == JOptionPane.YES_OPTION)
				retrieveProteinSeqs = true;
			parent.setProteinSequencesRetrieved(retrieveProteinSeqs);
		}
		List<IdentificationSet> idSets = getIdentificationSets(null, null, totalSerieShown);

		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			CategoryDataset dataset = DatasetFactory.createNumPeptidesPerProteinMassHistogram(idSets, bins,
					histogramType, retrieveProteinSeqs, countNonConclusiveProteins);
			BarChart chart = new BarChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
					xAxisLabel, yAxisLabel, dataset, plotOrientation);

			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			HistogramDataset dataset = DatasetFactory.createNumPeptidesPerProteinMassDistribution(idSets, bins,
					histogramType, retrieveProteinSeqs, countNonConclusiveProteins);

			HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			HistogramDataset dataset = DatasetFactory.createNumPeptidesPerProteinMassDistribution(idSets, bins,
					histogramType, retrieveProteinSeqs, countNonConclusiveProteins);
			HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				HistogramDataset dataset = DatasetFactory.createNumPeptidesPerProteinMassDistribution(idSets, bins,
						histogramType, retrieveProteinSeqs, countNonConclusiveProteins);
				HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType), experiment.getName(),
						dataset, xAxisLabel);
				// chart.setXRangeValues(0, 100);
				chartList.add(chart.getChartPanel());
			}
			return chartList;
			// this.jPanelChart.addGraphicPanel(chartList);
		}
		return null;
	}

	private Object showPeptidePresencyHeatMapChart() {
		parent.setInformation1(parent.getCurrentChartType());
		String xAxisLabel;
		final List<String> sequences = optionsFactory.getUserPeptideList();
		if (sequences == null)
			throw new IllegalMiapeArgumentException("Insert at least one peptide sequence to show the chart");
		String yAxisLabel = "peptides sequences";

		Boolean distinguishModPeptides = parent.distinguishModifiedPeptides();
		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();

		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		double colorScale = optionsFactory.getColorScale();
		boolean binary = optionsFactory.isHeatMapBinary();
		final Color highColor = optionsFactory.getHighColorScale();
		final Color lowColor = optionsFactory.getLowColorScale();
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			List<String> columnList = getIdSetNames(idSets);

			double[][] dataset = DatasetFactory.createPeptidePresencyHeapMapDataSet(idSets, sequences,
					parent.distinguishModifiedPeptides(), binary);

			HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, sequences, columnList,
					colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);

			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			List<String> columnList = getIdSetNames(idSets);

			double[][] dataset = DatasetFactory.createPeptidePresencyHeapMapDataSet(idSets, sequences,
					parent.distinguishModifiedPeptides(), binary);

			HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, sequences, columnList,
					colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);

			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			List<String> columnList = getIdSetNames(idSets);

			double[][] dataset = DatasetFactory.createPeptidePresencyHeapMapDataSet(idSets, sequences,
					parent.distinguishModifiedPeptides(), binary);

			HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, sequences, columnList,
					colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);

			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, false);
				List<String> columnList = getIdSetNames(idSets);
				double[][] dataset = DatasetFactory.createPeptidePresencyHeapMapDataSet(idSets, sequences,
						parent.distinguishModifiedPeptides(), binary);

				HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, sequences, columnList,
						colorScale);
				chart.setHighValueColor(highColor);
				chart.setLowValueColor(lowColor);

				chartList.add(chart.getjPanel());
			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;

	}

	private List<String> getIdSetNames(List<IdentificationSet> idSets) {
		List<String> ret = new ArrayList<String>();
		for (IdentificationSet idSet : idSets) {
			ret.add(idSet.getName());
		}
		return ret;
	}

	private Object showExclusiveIdentificationNumberChart(IdentificationItemEnum plotItem) {
		parent.setInformation1(parent.getCurrentChartType() + " / " + plotItem);
		String xAxisLabel = "";
		String yAxisLabel = "";
		String yAxisLabelAccumulative = "";
		if (IdentificationItemEnum.PROTEIN.equals(plotItem)) {
			yAxisLabel = "# proteins";
			yAxisLabelAccumulative = "Accumulative # proteins";
		} else if (IdentificationItemEnum.PEPTIDE.equals(plotItem)) {
			yAxisLabel = "# peptides";
			yAxisLabelAccumulative = "Accumulative # peptides";
		}
		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();

		boolean showAccumulativeTrend = optionsFactory.isAccumulativeTrendSelected();
		final Map<String, JCheckBox> checkBoxControls = optionsFactory.getIdSetsJCheckBoxes();
		ProteinGroupComparisonType proteinGroupComparisonType = optionsFactory.getProteinGroupComparisonType();
		boolean distModPeptides = parent.distinguishModifiedPeptides();
		List<IdentificationSet> idSets = getIdentificationSets(null, checkBoxControls, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			List<DefaultCategoryDataset> datasets = null;
			if (IdentificationItemEnum.PEPTIDE == plotItem) {
				datasets = DatasetFactory.createExclusiveNumberIdentificationCategoryDataSetForPeptides(idSets,
						distModPeptides, showAccumulativeTrend);
			} else {
				datasets = DatasetFactory.createExclusiveNumberIdentificationCategoryDataSetForProteins(idSets,
						proteinGroupComparisonType, showAccumulativeTrend, countNonConclusiveProteins);
			}
			if (showAccumulativeTrend) {
				CombinedChart chart = new CombinedChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, yAxisLabelAccumulative,
						datasets.get(0), datasets.get(1));
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, datasets.get(0),
						plotOrientation);
				return chart.getChartPanel();
			}

		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			List<DefaultCategoryDataset> datasets = null;
			if (IdentificationItemEnum.PEPTIDE == plotItem) {
				datasets = DatasetFactory.createExclusiveNumberIdentificationCategoryDataSetForPeptides(idSets,
						distModPeptides, showAccumulativeTrend);
			} else {
				datasets = DatasetFactory.createExclusiveNumberIdentificationCategoryDataSetForProteins(idSets,
						proteinGroupComparisonType, showAccumulativeTrend, countNonConclusiveProteins);
			}
			if (showAccumulativeTrend) {
				CombinedChart chart = new CombinedChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, yAxisLabelAccumulative,
						datasets.get(0), datasets.get(1));
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, datasets.get(0),
						plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			List<DefaultCategoryDataset> datasets = null;
			if (IdentificationItemEnum.PEPTIDE == plotItem) {
				datasets = DatasetFactory.createExclusiveNumberIdentificationCategoryDataSetForPeptides(idSets,
						distModPeptides, showAccumulativeTrend);
			} else {
				datasets = DatasetFactory.createExclusiveNumberIdentificationCategoryDataSetForProteins(idSets,
						proteinGroupComparisonType, showAccumulativeTrend, countNonConclusiveProteins);
			}
			if (showAccumulativeTrend) {
				CombinedChart chart = new CombinedChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, yAxisLabelAccumulative,
						datasets.get(0), datasets.get(1));
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, datasets.get(0),
						plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();

			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), checkBoxControls, false);
				List<DefaultCategoryDataset> datasets = null;
				if (IdentificationItemEnum.PEPTIDE == plotItem) {
					datasets = DatasetFactory.createExclusiveNumberIdentificationCategoryDataSetForPeptides(idSets,
							distModPeptides, showAccumulativeTrend);
				} else {
					datasets = DatasetFactory.createExclusiveNumberIdentificationCategoryDataSetForProteins(idSets,
							proteinGroupComparisonType, showAccumulativeTrend, countNonConclusiveProteins);
				}
				if (showAccumulativeTrend) {
					CombinedChart chart = new CombinedChart(
							parent.getChartTitle(chartType) + ": " + experiment.getName(),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, yAxisLabelAccumulative,
							datasets.get(0), datasets.get(1));
					chartList.add(chart.getChartPanel());
				} else {
					BarChart chart = new BarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, datasets.get(0),
							plotOrientation);
					chartList.add(chart.getChartPanel());
				}

			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	private List<IdentificationSet> getIdentificationSets(String experimentName,
			Map<String, JCheckBox> idSetsCheckBoxes, boolean addParentLevelIdSet) {
		List<IdentificationSet> idSets = new ArrayList<IdentificationSet>();
		if (ChartManagerFrame.ONE_SERIES_PER_REPLICATE.equals(option)) {
			final List<Experiment> experiments = experimentList.getExperiments();
			for (Experiment experiment : experiments) {
				for (Replicate replicate : experiment.getNextLevelIdentificationSetList()) {

					// String repName = experiment.getName() + " / " +
					// replicate.getName();
					String repName = replicate.getFullName();
					if (idSetsCheckBoxes != null) {
						if (idSetsCheckBoxes.containsKey(repName) && idSetsCheckBoxes.get(repName).isSelected())
							idSets.add(replicate);
					} else {
						idSets.add(replicate);
					}
				}
			}
			// if (experiments.size() == 1) {
			if (addParentLevelIdSet)
				idSets.add(experimentList);
			// }
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<Experiment> experiments = experimentList.getExperiments();
			for (Experiment experiment : experiments) {
				String expName = experiment.getFullName();
				if (expName.equals(experimentName)) {
					for (Replicate replicate : experiment.getNextLevelIdentificationSetList()) {

						String repName = replicate.getFullName();
						if (idSetsCheckBoxes != null) {
							if (idSetsCheckBoxes.containsKey(repName)

									&& idSetsCheckBoxes.get(repName).isSelected()) {
								idSets.add(replicate);
							}
						} else {
							idSets.add(replicate);
						}
					}
					if (addParentLevelIdSet)
						idSets.add(experiment);
				}
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final List<Experiment> experiments = experimentList.getExperiments();
			for (Experiment identificationSet : experiments) {
				Experiment experiment = identificationSet;
				String expName = experiment.getFullName();
				if (idSetsCheckBoxes != null) {
					if (idSetsCheckBoxes.containsKey(expName) && idSetsCheckBoxes.get(expName).isSelected()) {
						idSets.add(experiment);
					}
				} else {
					log.debug("EXP=" + experiment.getFullName());
					idSets.add(experiment);
				}
			}
			if (addParentLevelIdSet)
				idSets.add(experimentList);
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			// String expListName = this.experimentList.getName();
			// if (idSetsCheckBoxes != null) {
			// if (idSetsCheckBoxes.containsKey(expListName)
			// && idSetsCheckBoxes.get(expListName).isSelected())
			// idSets.add(this.experimentList);
			// } else {
			idSets.add(experimentList);
			// }

		}
		return idSets;
	}

	private Object showAllChrMappingBarChart() {

		parent.setInformation1(parent.getCurrentChartType());
		String xAxisLabel;

		String proteinOrGene = optionsFactory.getProteinOrGene();
		String yAxisLabel;
		if (AdditionalOptionsPanelFactory.PROTEIN.equals(proteinOrGene))
			yAxisLabel = "# proteins";
		else if (AdditionalOptionsPanelFactory.GENES.equals(proteinOrGene))
			yAxisLabel = "# genes";
		else
			yAxisLabel = "# proteins and # genes";

		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		final boolean takeGeneFromFirstProteinSelected = optionsFactory.isTakeGeneFromFirstProteinSelected();
		final boolean pieChart = optionsFactory.showAsPieChart();

		if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			xAxisLabel = "experiment list";
			if (!pieChart) {
				CategoryDataset dataset = DatasetFactory.createAllHumanChromosomeMappingCategoryDataSet(experimentList,
						proteinOrGene, true, takeGeneFromFirstProteinSelected, countNonConclusiveProteins);
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);

				return chart.getChartPanel();
			} else {
				PieDataset dataset = DatasetFactory.createAllHumanChromosomeMappingPieDataSet(experimentList,
						proteinOrGene, takeGeneFromFirstProteinSelected, countNonConclusiveProteins);
				PieChart chart = new PieChart(parent.getChartTitle(chartType) + " - " + proteinOrGene,
						parent.getChartSubtitle(chartType, option), dataset);
				return chart.getChartPanel();
			}

		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {

				if (!pieChart) {
					xAxisLabel = experiment.getName();

					CategoryDataset dataset = DatasetFactory.createAllHumanChromosomeMappingCategoryDataSet(experiment,
							proteinOrGene, experimentList.getExperiments().size() == 1,
							takeGeneFromFirstProteinSelected, countNonConclusiveProteins);

					BarChart chart = new BarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation);

					chartList.add(chart.getChartPanel());

				} else {
					PieDataset dataset = DatasetFactory.createAllHumanChromosomeMappingPieDataSet(experiment,
							proteinOrGene, takeGeneFromFirstProteinSelected, countNonConclusiveProteins);
					PieChart chart = new PieChart(parent.getChartTitle(chartType) + " - " + proteinOrGene,
							experiment.getName(), dataset);
					chartList.add(chart.getChartPanel());
				}

			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	private Object showAllChrPeptideMappingBarChart() {

		parent.setInformation1(parent.getCurrentChartType());
		String xAxisLabel;

		String peptideOrPSM = optionsFactory.getPeptideOrPSM();
		String yAxisLabel;
		if (AdditionalOptionsPanelFactory.PSM.equals(peptideOrPSM))
			yAxisLabel = "# PSM";
		else if (AdditionalOptionsPanelFactory.PEPTIDE.equals(peptideOrPSM))
			yAxisLabel = "# peptides";
		else
			yAxisLabel = "# peptides and # PSM";

		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		final boolean distinguisModPep = parent.distinguishModifiedPeptides();
		final boolean pieChart = optionsFactory.showAsPieChart();

		if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			xAxisLabel = "experiment list";
			if (!pieChart) {
				CategoryDataset dataset = DatasetFactory.createAllHumanChromosomePeptideMappingCategoryDataSet(
						experimentList, peptideOrPSM, true, distinguisModPep);
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);

				return chart.getChartPanel();
			} else {
				PieDataset dataset = DatasetFactory.createAllHumanChromosomePeptideMappingPieDataSet(experimentList,
						peptideOrPSM, distinguisModPep);
				PieChart chart = new PieChart(parent.getChartTitle(chartType) + " - " + peptideOrPSM,
						parent.getChartSubtitle(chartType, option), dataset);
				return chart.getChartPanel();
			}

		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {

				if (!pieChart) {
					xAxisLabel = experiment.getName();

					CategoryDataset dataset = DatasetFactory.createAllHumanChromosomePeptideMappingCategoryDataSet(
							experiment, peptideOrPSM, true, distinguisModPep);

					BarChart chart = new BarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation);

					chartList.add(chart.getChartPanel());

				} else {
					PieDataset dataset = DatasetFactory.createAllHumanChromosomePeptideMappingPieDataSet(experiment,
							peptideOrPSM, distinguisModPep);
					PieChart chart = new PieChart(parent.getChartTitle(chartType) + " - " + peptideOrPSM,
							experiment.getName(), dataset);
					chartList.add(chart.getChartPanel());
				}

			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	private Object showAllChrCoverageSpiderChart() {

		parent.setInformation1(parent.getCurrentChartType());
		String xAxisLabel;

		String yAxisLabel = "# genes";

		boolean showSpiderPlot = optionsFactory.isShowAsSpiderPlot();
		boolean justPercentage = optionsFactory.getAsPercentage();
		boolean showTotal = optionsFactory.isTotalSerieShown();
		if (justPercentage) {
			yAxisLabel += " (%)";
		}
		final boolean takeGeneFromFirstProteinSelected = optionsFactory.isTakeGeneFromFirstProteinSelected();
		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();

		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			xAxisLabel = "experiment list";

			CategoryDataset dataset = DatasetFactory.createAllHumanChromosomeGeneCoverageCategoryDataSet(idSets,
					showSpiderPlot, justPercentage, showTotal, takeGeneFromFirstProteinSelected,
					countNonConclusiveProteins);

			if (showSpiderPlot) {
				SpiderChart chart = new SpiderChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), dataset);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				if (justPercentage)
					chart.setNonIntegerItemLabels();
				return chart.getChartPanel();
			}

		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			xAxisLabel = "experiment";

			CategoryDataset dataset = DatasetFactory.createAllHumanChromosomeGeneCoverageCategoryDataSet(idSets,
					showSpiderPlot, justPercentage, showTotal, takeGeneFromFirstProteinSelected,
					countNonConclusiveProteins);
			if (showSpiderPlot) {
				SpiderChart chart = new SpiderChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), dataset);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				if (justPercentage)
					chart.setNonIntegerItemLabels();
				return chart.getChartPanel();
			}

		} else if (ChartManagerFrame.ONE_SERIES_PER_REPLICATE.equals(option)) {

			xAxisLabel = "level 2";

			CategoryDataset dataset = DatasetFactory.createAllHumanChromosomeGeneCoverageCategoryDataSet(idSets,
					showSpiderPlot, justPercentage, showTotal, takeGeneFromFirstProteinSelected,
					countNonConclusiveProteins);
			if (showSpiderPlot) {
				SpiderChart chart = new SpiderChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), dataset);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				if (justPercentage)
					chart.setNonIntegerItemLabels();
				return chart.getChartPanel();
			}

		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getFullName(), null, false);
				xAxisLabel = experiment.getName();
				CategoryDataset dataset = DatasetFactory.createAllHumanChromosomeGeneCoverageCategoryDataSet(idSets,
						showSpiderPlot, justPercentage, showTotal, takeGeneFromFirstProteinSelected,
						countNonConclusiveProteins);

				if (showSpiderPlot) {
					SpiderChart chart = new SpiderChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), dataset);
					chartList.add(chart.getChartPanel());
				} else {
					BarChart chart = new BarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation);
					if (justPercentage)
						chart.setNonIntegerItemLabels();
					chartList.add(chart.getChartPanel());
				}

			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	private Object showChr16MappingBarChart() {

		parent.setInformation1(parent.getCurrentChartType());
		String xAxisLabel;

		String yAxisLabel = "# proteins / # genes";

		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();

		xAxisLabel = "researcher";
		List<String> groupsToShow = optionsFactory.getGroupToShow();
		boolean showNotAssigned = optionsFactory.isNotAssignedShowed();
		String proteinOrGene = optionsFactory.getProteinOrGene();
		String knownUnknown = optionsFactory.getChr16KnownOrUnknown();
		final boolean takeGeneFromFirstProteinSelected = optionsFactory.isTakeGeneFromFirstProteinSelected();
		if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			CategoryDataset dataset = DatasetFactory.createChr16MappingCategoryDataSet(experimentList, groupsToShow,
					showNotAssigned, proteinOrGene, knownUnknown, takeGeneFromFirstProteinSelected,
					countNonConclusiveProteins);

			BarChart chart = new BarChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
					xAxisLabel, yAxisLabel, dataset, plotOrientation);
			return chart.getChartPanel();

		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {

				CategoryDataset dataset = DatasetFactory.createChr16MappingCategoryDataSet(experiment, groupsToShow,
						showNotAssigned, proteinOrGene, knownUnknown, takeGeneFromFirstProteinSelected,
						countNonConclusiveProteins);

				BarChart chart = new BarChart(parent.getChartTitle(chartType), experiment.getName(), xAxisLabel,
						yAxisLabel, dataset, plotOrientation);
				chartList.add(chart.getChartPanel());

			}
			return chartList;
		}
		return null;
	}

	private Object shotPSMPEPPROT_LineChart() {
		parent.setInformation1(parent.getCurrentChartType());

		String xAxisLabel = "";
		boolean showPSMs = optionsFactory.showPSMs();
		boolean showPeptides = optionsFactory.showPeptides();
		boolean showPeptidesPlusCharge = optionsFactory.showPeptidesPlusCharge();
		boolean showProteins = optionsFactory.showProteins();
		boolean distinguishModificatedPeptides = parent.distinguishModifiedPeptides();
		String yAxisLabel = "";

		parent.addCustomizationControls();

		// if (parent.isFDRThresholdEnabled()) {
		// showPSMs = false;
		// parent.addCustomizationControls();
		// }
		if (showPSMs)
			yAxisLabel = yAxisLabel + "PSMs";
		if (showPeptides) {
			if (!"".equals(yAxisLabel))
				yAxisLabel += " / ";
			yAxisLabel += "Peptides";
		}
		if (showPeptidesPlusCharge) {
			if (!"".equals(yAxisLabel))
				yAxisLabel += " / ";
			yAxisLabel += "Peptides (diff by z)";
		}
		if (showProteins) {
			if (!"".equals(yAxisLabel))
				yAxisLabel += " / ";
			yAxisLabel += "Proteins";
		}
		if (showPSMs || showPeptides || showProteins || showPeptidesPlusCharge)
			yAxisLabel = "# of " + yAxisLabel;
		else
			throw new IllegalMiapeArgumentException("Select PSM, Peptides or Proteins to generate the chart");

		List<IdentificationSet> idSets = getIdentificationSets(null, null, optionsFactory.isTotalSerieShown());
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			xAxisLabel = "level 2";
			CategoryDataset dataset = DatasetFactory.createPSM_PEP_PROT_DataSet(idSets, showPSMs, showPeptides,
					showPeptidesPlusCharge, showProteins, distinguishModificatedPeptides, countNonConclusiveProteins);
			LineCategoryChart chart = new LineCategoryChart(yAxisLabel, parent.getChartSubtitle(chartType, option),
					xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL);

			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			CategoryDataset dataset = DatasetFactory.createPSM_PEP_PROT_DataSet(idSets, showPSMs, showPeptides,
					showPeptidesPlusCharge, showProteins, distinguishModificatedPeptides, countNonConclusiveProteins);
			LineCategoryChart chart = new LineCategoryChart(yAxisLabel, parent.getChartSubtitle(chartType, option),
					xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL);
			xAxisLabel = "Experiment";
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			CategoryDataset dataset = DatasetFactory.createPSM_PEP_PROT_DataSet(idSets, showPSMs, showPeptides,
					showPeptidesPlusCharge, showProteins, distinguishModificatedPeptides, countNonConclusiveProteins);
			LineCategoryChart chart = new LineCategoryChart(yAxisLabel, parent.getChartSubtitle(chartType, option),
					xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL);
			xAxisLabel = "Experiment";
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			xAxisLabel = "level 2";
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, optionsFactory.isTotalSerieShown());

				CategoryDataset dataset = DatasetFactory.createPSM_PEP_PROT_DataSet(idSets, showPSMs, showPeptides,
						showPeptidesPlusCharge, showProteins, distinguishModificatedPeptides,
						countNonConclusiveProteins);
				LineCategoryChart chart = new LineCategoryChart(yAxisLabel, parent.getChartSubtitle(chartType, option),
						xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL);
				chartList.add(chart.getChartPanel());
			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	private Object showDeltaMzScatterChart() {
		parent.setInformation1(parent.getCurrentChartType());

		String xAxisLabel = "m/z";
		String yAxisLabel = "Delta(m/z)";
		boolean showRegressionLine = optionsFactory.showRegressionLine();
		final Map<String, JCheckBox> experimentJCheckBoxes = optionsFactory.getIdSetsJCheckBoxes();
		List<IdentificationSet> idSets = getIdentificationSets(null, experimentJCheckBoxes, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			XYDataset dataset = DatasetFactory.createDeltaMzOverMzXYDataSet(idSets);
			XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
			chart.setTinnySeriesShape();
			chart.setAutomaticScales();
			if (showRegressionLine)
				chart.addRegressionLine(true);
			chart.addHorizontalLine(0);
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			XYDataset dataset = DatasetFactory.createDeltaMzOverMzXYDataSet(idSets);
			XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
			chart.setTinnySeriesShape();
			chart.setAutomaticScales();
			if (showRegressionLine)
				chart.addRegressionLine(true);
			chart.addHorizontalLine(0);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			XYDataset dataset = DatasetFactory.createDeltaMzOverMzXYDataSet(idSets);
			XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
			chart.setTinnySeriesShape();
			chart.setAutomaticScales();
			if (showRegressionLine)
				chart.addRegressionLine(true);
			chart.addHorizontalLine(0);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				try {
					idSets = getIdentificationSets(experiment.getName(), experimentJCheckBoxes, false);
					XYDataset dataset = DatasetFactory.createDeltaMzOverMzXYDataSet(idSets);
					XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
					chart.setTinnySeriesShape();
					chart.setAutomaticScales();
					if (showRegressionLine)
						chart.addRegressionLine(true);
					chart.addHorizontalLine(0);
					chartList.add(chart.getChartPanel());
				} catch (IllegalMiapeArgumentException e) {
					JPanel jpanel = new JPanel();
					jpanel.add(new JLabel("<html>Error generating chart for " + experiment.getName() + "<br>"
							+ e.getMessage() + "</html>"));
					chartList.add(jpanel);
				}

			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		try {
			if (!isCancelled()) {
				Object object = get();
				if (object != null && error == null)
					firePropertyChange(CHART_GENERATED, null, object);
				if (error != null)
					firePropertyChange(CHART_ERROR_GENERATED, null, object);
				log.info("chart passed to the dialog");
			} else {
				log.info("Cancelled by user");
			}
			return;
		} catch (InterruptedException e) {
			log.warn(e.getMessage());
			e.printStackTrace();
		} catch (ExecutionException e) {
			log.warn(e.getMessage());
			e.printStackTrace();
		}
		firePropertyChange(CHART_ERROR_GENERATED, null, null);
		super.done();
	}

	private Object showProteinSensitivitySpecificityChart() {
		parent.setInformation1(parent.getCurrentChartType());
		String xAxisLabel;
		String yAxisLabel = "0-1";

		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		Set<String> proteinsInSample = optionsFactory.getProteinsInSample();
		if (proteinsInSample == null || proteinsInSample.isEmpty())
			throw new IllegalMiapeArgumentException(
					"In order to show this chart, it is necessary to define the proteins in sample that will be the positives hits");

		// parent.setInformation3("sensitivity=tp/(tp+fn),
		// specificity=tn/(tn+fp), precision=tp/total");
		// parent.setToolTipInformation3("here the explanation of the values");

		xAxisLabel = "level 2";
		boolean sensitivity = optionsFactory.isSensitivity();

		boolean accuracy = optionsFactory.isAccuracy();
		boolean specificity = optionsFactory.isSpecificity();
		boolean precision = optionsFactory.isPrecision();
		boolean npv = optionsFactory.isNPV();
		boolean fdr = optionsFactory.isFDR();
		// if (!parent.isFDRThresholdEnabled())
		// throw new IllegalMiapeArgumentException(
		// "<html>In order to show this chart, it is necessary to define an FDR
		// threshold<br>that provides which proteins have been considered as
		// positives after the filter</html>");
		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			xAxisLabel = "experiment";
			CategoryDataset dataset = DatasetFactory.createProteinSensitivityCategoryDataSet(idSets, proteinsInSample,
					countNonConclusiveProteins, sensitivity, accuracy, specificity, precision, npv, fdr);
			BarChart chart = new BarChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
					xAxisLabel, yAxisLabel, dataset, plotOrientation);
			chart.setNonIntegerItemLabels();
			CategoryPlot plot = (CategoryPlot) chart.getChart().getPlot();
			// plot.getRangeAxis().setUpperBound(1.0);
			plot.getRangeAxis().setUpperMargin(0.1);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			xAxisLabel = "experiment list";
			CategoryDataset dataset = DatasetFactory.createProteinSensitivityCategoryDataSet(idSets, proteinsInSample,
					countNonConclusiveProteins, sensitivity, accuracy, specificity, precision, npv, fdr);
			BarChart chart = new BarChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
					xAxisLabel, yAxisLabel, dataset, plotOrientation);
			chart.setNonIntegerItemLabels();
			CategoryPlot plot = (CategoryPlot) chart.getChart().getPlot();
			// plot.getRangeAxis().setUpperBound(1.0);
			plot.getRangeAxis().setUpperMargin(0.1);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_REPLICATE.equals(option)) {

			xAxisLabel = "level 2";
			CategoryDataset dataset = DatasetFactory.createProteinSensitivityCategoryDataSet(idSets, proteinsInSample,
					countNonConclusiveProteins, sensitivity, accuracy, specificity, precision, npv, fdr);
			BarChart chart = new BarChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
					xAxisLabel, yAxisLabel, dataset, plotOrientation);
			chart.setNonIntegerItemLabels();
			CategoryPlot plot = (CategoryPlot) chart.getChart().getPlot();
			// plot.getRangeAxis().setUpperBound(1.0);
			plot.getRangeAxis().setUpperMargin(0.1);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);

				CategoryDataset dataset = DatasetFactory.createProteinSensitivityCategoryDataSet(idSets,
						proteinsInSample, countNonConclusiveProteins, sensitivity, accuracy, specificity, precision,
						npv, fdr);
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				chart.setNonIntegerItemLabels();
				CategoryPlot plot = (CategoryPlot) chart.getChart().getPlot();
				// plot.getRangeAxis().setUpperBound(1.0);
				plot.getRangeAxis().setUpperMargin(0.1);
				chartList.add(chart.getChartPanel());
			}
			return chartList;
			// this.jPanelChart.addGraphicPanel(chartList);
		}
		return null;
	}

	private Object showPeptideMassDistributionChart() {
		parent.setInformation1(parent.getCurrentChartType());
		int bins = optionsFactory.getHistogramBins();
		boolean mOverZ = optionsFactory.getMOverZ();
		boolean addZeroZeroValue = true;
		HistogramType histogramType = optionsFactory.getHistogramType();
		String xAxisLabel;
		if (mOverZ)
			xAxisLabel = "m/z";
		else
			xAxisLabel = "Da";

		boolean showParent = optionsFactory.isTotalSerieShown();
		List<IdentificationSet> idSets = getIdentificationSets(null, null, showParent);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			HistogramDataset dataset = DatasetFactory.createPeptideMassHistogramDataSet(idSets, bins, histogramType,
					mOverZ);
			HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			HistogramDataset dataset = DatasetFactory.createPeptideMassHistogramDataSet(idSets, bins, histogramType,
					mOverZ);
			HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			HistogramDataset dataset = DatasetFactory.createPeptideMassHistogramDataSet(idSets, bins, histogramType,
					mOverZ);
			HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				HistogramDataset dataset = DatasetFactory.createPeptideMassHistogramDataSet(idSets, bins, histogramType,
						mOverZ);
				HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType), experiment.getName(),
						dataset, xAxisLabel);
				// chart.setXRangeValues(0, 100);
				chartList.add(chart.getChartPanel());
			}
			return chartList;
			// this.jPanelChart.addGraphicPanel(chartList);
		}
		return null;
	}

	private Object showPeptideLengthDistributionChart() {
		parent.setInformation1(parent.getCurrentChartType());
		String xAxisLabel = "Peptide length";
		String yAxisLabel = "# peptides";
		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		boolean stacked = optionsFactory.showAsStackedChart();
		boolean normalize = optionsFactory.getAsPercentage();
		boolean showParent = optionsFactory.isTotalSerieShown();
		final int maximum = optionsFactory.getMaximumOccurrence();
		final int minimum = optionsFactory.getMinimumOccurrence();
		if (minimum > maximum)
			throw new IllegalMiapeArgumentException("The minimum length cannot be higher than the maximum");

		List<IdentificationSet> idSets = getIdentificationSets(null, null, showParent);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			CategoryDataset dataset = DatasetFactory.createPeptideLengthHistogramDataSet(idSets, minimum, maximum);
			if (stacked) {
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						normalize);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			CategoryDataset dataset = DatasetFactory.createPeptideLengthHistogramDataSet(idSets, minimum, maximum);
			if (stacked) {
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						normalize);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			CategoryDataset dataset = DatasetFactory.createPeptideLengthHistogramDataSet(idSets, minimum, maximum);
			if (stacked) {
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						normalize);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				CategoryDataset dataset = DatasetFactory.createPeptideLengthHistogramDataSet(idSets, minimum, maximum);
				if (stacked) {
					StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation, normalize);
					chartList.add(chart.getChartPanel());
				} else {
					BarChart chart = new BarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation);
					chartList.add(chart.getChartPanel());
				}
			}
			return chartList;
			// this.jPanelChart.addGraphicPanel(chartList);
		}
		return null;
	}

	private Object showPeptideMonitoringBarChart() {
		parent.setInformation1(parent.getCurrentChartType());
		String xAxisLabel;
		final List<String> sequences = optionsFactory.getSelectedPeptides();
		if (sequences == null)
			throw new IllegalMiapeArgumentException("Select a peptide to show the chart");
		String yAxisLabel = "# peptides";

		Boolean distinguishModPeptides = parent.distinguishModifiedPeptides();
		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();

		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			xAxisLabel = "level 2";

			CategoryDataset dataset = DatasetFactory.createPeptideMonitoringCategoryDataSet(idSets, sequences,
					distinguishModPeptides);
			BarChart chart = new BarChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
					xAxisLabel, yAxisLabel, dataset, plotOrientation);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			xAxisLabel = "experiment list";

			CategoryDataset dataset = DatasetFactory.createPeptideMonitoringCategoryDataSet(idSets, sequences,
					distinguishModPeptides);
			BarChart chart = new BarChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
					xAxisLabel, yAxisLabel, dataset, plotOrientation);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			xAxisLabel = "experiment";

			CategoryDataset dataset = DatasetFactory.createPeptideMonitoringCategoryDataSet(idSets, sequences,
					distinguishModPeptides);
			BarChart chart = new BarChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
					xAxisLabel, yAxisLabel, dataset, plotOrientation);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				xAxisLabel = experiment.getName();

				CategoryDataset dataset = DatasetFactory.createPeptideMonitoringCategoryDataSet(idSets, sequences,
						distinguishModPeptides);
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				chartList.add(chart.getChartPanel());
			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	private Object showModificationsNumberDistributionBarChart() {
		parent.setInformation1(parent.getCurrentChartType());
		String xAxisLabel;
		final String[] modifications = optionsFactory.getSelectedModifications();
		if (modifications == null || modifications.length == 0)
			throw new IllegalMiapeArgumentException("Select at least one modification");
		String yAxisLabel = "# different peptides containing: " + modifications[0];

		int maximum = optionsFactory.getMaximumOccurrence();
		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		final boolean showAsStackedChartPanel = optionsFactory.showAsStackedChart();
		final boolean asPercentage = optionsFactory.getAsPercentage();
		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			xAxisLabel = "level 2";
			CategoryDataset dataset = DatasetFactory.createModificationDistributionCategoryDataSet(idSets,
					modifications, maximum);
			if (showAsStackedChartPanel) {
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}

		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			xAxisLabel = "experiment list";
			CategoryDataset dataset = DatasetFactory.createModificationDistributionCategoryDataSet(idSets,
					modifications, maximum);
			if (showAsStackedChartPanel) {
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}

		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			xAxisLabel = "experiment";
			CategoryDataset dataset = DatasetFactory.createModificationDistributionCategoryDataSet(idSets,
					modifications, maximum);
			if (showAsStackedChartPanel) {
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}

		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			xAxisLabel = "level 2";
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				CategoryDataset dataset = DatasetFactory.createModificationDistributionCategoryDataSet(idSets,
						modifications, maximum);
				if (showAsStackedChartPanel) {
					StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation, asPercentage);
					chartList.add(chart.getChartPanel());
				} else {
					BarChart chart = new BarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation);
					chartList.add(chart.getChartPanel());
				}

			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	private Object showMissedCleavagesNumberDistributionBarChart() {
		parent.setInformation1(parent.getCurrentChartType());
		String xAxisLabel;

		String yAxisLabel = "# peptides";
		xAxisLabel = "# missedcleavages sites";
		int maximum = optionsFactory.getMaximumOccurrence();
		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		boolean stackedChart = optionsFactory.showAsStackedChart();
		boolean asPercentage = optionsFactory.getAsPercentage();
		List<IdentificationSet> idSets = getIdentificationSets(null, null, optionsFactory.isTotalSerieShown());
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			CategoryDataset dataset = DatasetFactory.createMissedCleavagesDistributionCategoryDataSet(idSets, maximum);
			if (stackedChart) {
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			CategoryDataset dataset = DatasetFactory.createMissedCleavagesDistributionCategoryDataSet(idSets, maximum);
			if (stackedChart) {
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			CategoryDataset dataset = DatasetFactory.createMissedCleavagesDistributionCategoryDataSet(idSets, maximum);
			if (stackedChart) {
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, optionsFactory.isTotalSerieShown());
				CategoryDataset dataset = DatasetFactory.createMissedCleavagesDistributionCategoryDataSet(idSets,
						maximum);
				if (stackedChart) {
					StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation, asPercentage);
					chartList.add(chart.getChartPanel());
				} else {
					BarChart chart = new BarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation);
					chartList.add(chart.getChartPanel());
				}

			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	private Object showProteinGroupTypesDistributionBarChart() {
		parent.setInformation1(parent.getCurrentChartType());
		String xAxisLabel;

		String yAxisLabel = "# proteinGroups";
		xAxisLabel = "Protein Group Type";
		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		boolean stackedChart = optionsFactory.showAsStackedChart();
		boolean asPercentage = optionsFactory.getAsPercentage();
		List<IdentificationSet> idSets = getIdentificationSets(null, null, optionsFactory.isTotalSerieShown());
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			CategoryDataset dataset = DatasetFactory.createProteinGroupTypesDistributionCategoryDataSet(idSets);
			if (stackedChart) {
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			CategoryDataset dataset = DatasetFactory.createProteinGroupTypesDistributionCategoryDataSet(idSets);
			if (stackedChart) {
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			CategoryDataset dataset = DatasetFactory.createProteinGroupTypesDistributionCategoryDataSet(idSets);
			if (stackedChart) {
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, optionsFactory.isTotalSerieShown());
				CategoryDataset dataset = DatasetFactory.createProteinGroupTypesDistributionCategoryDataSet(idSets);
				if (stackedChart) {
					StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation, asPercentage);
					chartList.add(chart.getChartPanel());
				} else {
					BarChart chart = new BarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation);
					chartList.add(chart.getChartPanel());
				}

			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	private Object showModificationsSitesBarChart() {
		parent.setInformation1(parent.getCurrentChartType());
		final String[] modifications = optionsFactory.getSelectedModifications();
		if (modifications == null)
			return null;

		String yAxisLabel = "# of modificated sites ";
		String xAxisLabel = "PTM";
		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		boolean stackedChart = optionsFactory.showAsStackedChart();
		boolean asPercentage = optionsFactory.getAsPercentage();
		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			CategoryDataset dataset = DatasetFactory.createNumberModificationSitesCategoryDataSet(idSets,
					modifications);
			if (stackedChart) {
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			CategoryDataset dataset = DatasetFactory.createNumberModificationSitesCategoryDataSet(idSets,
					modifications);
			if (stackedChart) {
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			CategoryDataset dataset = DatasetFactory.createNumberModificationSitesCategoryDataSet(idSets,
					modifications);
			if (stackedChart) {
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, false);

				if (!stackedChart)
					idSets.add(experiment);
				CategoryDataset dataset = DatasetFactory.createNumberModificationSitesCategoryDataSet(idSets,
						modifications);
				if (stackedChart) {
					StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation, asPercentage);
					chartList.add(chart.getChartPanel());
				} else {
					BarChart chart = new BarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation);
					chartList.add(chart.getChartPanel());
				}
			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	private Object showModificatedPeptidesBarChart() {
		parent.setInformation1(parent.getCurrentChartType());

		final String[] modifications = optionsFactory.getSelectedModifications();
		if (modifications == null || modifications.length == 0)
			throw new IllegalMiapeArgumentException("Select at least one modification");

		String yAxisLabel = "# of different peptides containing each PTM";
		String xAxisLabel = "PTM";
		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		boolean stackedChart = optionsFactory.showAsStackedChart();
		boolean asPercentage = optionsFactory.getAsPercentage();
		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			CategoryDataset dataset = DatasetFactory.createNumberModificatedPeptidesCategoryDataSet(idSets,
					modifications);
			if (stackedChart) {
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				chart.setHorizontalXLabel();
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			CategoryDataset dataset = DatasetFactory.createNumberModificatedPeptidesCategoryDataSet(idSets,
					modifications);
			if (stackedChart) {
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				chart.setHorizontalXLabel();
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			CategoryDataset dataset = DatasetFactory.createNumberModificatedPeptidesCategoryDataSet(idSets,
					modifications);
			if (stackedChart) {
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				chart.setHorizontalXLabel();
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, false);
				if (!stackedChart)
					idSets.add(experiment);
				CategoryDataset dataset = DatasetFactory.createNumberModificatedPeptidesCategoryDataSet(idSets,
						modifications);
				if (stackedChart) {
					StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation, asPercentage);
					chartList.add(chart.getChartPanel());
				} else {
					BarChart chart = new BarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation);
					chart.setHorizontalXLabel();
					chartList.add(chart.getChartPanel());
				}
			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	private Object showPeptideChargeBarChart() {
		parent.setInformation1(parent.getCurrentChartType());

		String xAxisLabel;
		String yAxisLabel = "frecuency";

		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		boolean stackedChart = optionsFactory.showAsStackedChart();
		boolean asPercentage = optionsFactory.getAsPercentage();
		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			xAxisLabel = "level 2";

			CategoryDataset dataset = DatasetFactory.createPeptideChargeHistogramDataSet(idSets);
			if (stackedChart) {
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			xAxisLabel = "experiment list";

			CategoryDataset dataset = DatasetFactory.createPeptideChargeHistogramDataSet(idSets);
			if (stackedChart) {
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			xAxisLabel = "experiment";

			CategoryDataset dataset = DatasetFactory.createPeptideChargeHistogramDataSet(idSets);
			if (stackedChart) {
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				xAxisLabel = experiment.getName();

				CategoryDataset dataset = DatasetFactory.createPeptideChargeHistogramDataSet(idSets);
				if (stackedChart) {
					StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation, asPercentage);
					chartList.add(chart.getChartPanel());
				} else {
					BarChart chart = new BarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation);
					chartList.add(chart.getChartPanel());
				}
			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	/**
	 *
	 * @param plotItem
	 * @param peptidesPerProtein
	 *            only applicable when plotItem is a protein
	 * @return
	 */
	private Object showPeptideOccurrenceHeatMapChart(boolean isPSMs) {
		parent.setInformation1(parent.getCurrentChartType() + " / " + IdentificationItemEnum.PEPTIDE);

		double colorScale = optionsFactory.getColorScale();
		int minThreshold = optionsFactory.getHeatMapThreshold();
		List<String> peptideSequenceOrder = parent.getPeptideSequencesFromPeptideSequenceFilter();

		//
		// double[][] dataset =
		// DatasetFactory.createHeapMapDataSet(experimentList, rowList,
		// columnList, plotItem, parent.distinguishModifiedPeptides(),
		// minOccurrenceThreshold);
		//
		// HeatMapChart chart = new
		// HeatMapChart(parent.getChartTitle(chartType), dataset, rowList,
		// columnList, colorScale);
		// return chart.getjPanel();

		final Color highColor = optionsFactory.getHighColorScale();
		final Color lowColor = optionsFactory.getLowColorScale();
		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		// if (!isPSMs && minThreshold > idSets.size())
		// throw new IllegalMiapeArgumentException(
		// "The occurrence threshold cannot be higher than the maximum number of
		// identification sets ("
		// + idSets.size() + ")");
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			List<String> rowList = new ArrayList<String>();
			List<String> columnList = new ArrayList<String>();
			double[][] dataset = null;
			if (isPSMs) {
				dataset = DatasetFactory.createPSMsPerPeptidesHeapMapDataSet(experimentList, idSets, rowList,
						columnList, peptideSequenceOrder, parent.distinguishModifiedPeptides(), minThreshold);
			} else {
				dataset = DatasetFactory.createPeptideOccurrenceHeapMapDataSet(experimentList, idSets, rowList,
						columnList, peptideSequenceOrder, parent.distinguishModifiedPeptides(), minThreshold);
			}
			parent.addMinMaxHeatMapValues(dataset);

			HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, rowList, columnList,
					colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);
			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			List<String> rowList = new ArrayList<String>();
			List<String> columnList = new ArrayList<String>();

			double[][] dataset = DatasetFactory.createPeptideOccurrenceHeapMapDataSet(experimentList, idSets, rowList,
					columnList, peptideSequenceOrder, parent.distinguishModifiedPeptides(), minThreshold);
			parent.addMinMaxHeatMapValues(dataset);

			HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, rowList, columnList,
					colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);
			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			List<String> rowList = new ArrayList<String>();
			List<String> columnList = new ArrayList<String>();

			double[][] dataset = DatasetFactory.createPeptideOccurrenceHeapMapDataSet(experimentList, idSets, rowList,
					columnList, peptideSequenceOrder, parent.distinguishModifiedPeptides(), minThreshold);
			parent.addMinMaxHeatMapValues(dataset);

			HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, rowList, columnList,
					colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);
			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				List<String> rowList = new ArrayList<String>();
				List<String> columnList = new ArrayList<String>();
				idSets = getIdentificationSets(experiment.getName(), null, false);
				double[][] dataset = DatasetFactory.createPeptideOccurrenceHeapMapDataSet(experiment, idSets, rowList,
						columnList, peptideSequenceOrder, parent.distinguishModifiedPeptides(), minThreshold);
				HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, rowList, columnList,
						colorScale);
				chart.setHighValueColor(highColor);
				chart.setLowValueColor(lowColor);
				chartList.add(chart.getjPanel());
			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	/**
	 *
	 * @param plotItem
	 * @param peptidesPerProtein
	 *            only applicable when plotItem is a protein
	 * @return
	 */
	private Object showProteinOccurrenceHeatMapChart() {
		parent.setInformation1(parent.getCurrentChartType() + " / " + IdentificationItemEnum.PROTEIN);

		double colorScale = optionsFactory.getColorScale();
		int minOccurrenceThreshold = optionsFactory.getHeatMapThreshold();
		List<String> proteinACCOrder = parent.getProteinAccsFromACCFilter();

		//
		// double[][] dataset =
		// DatasetFactory.createHeapMapDataSet(experimentList, rowList,
		// columnList, plotItem, parent.distinguishModifiedPeptides(),
		// minOccurrenceThreshold);
		//
		// HeatMapChart chart = new
		// HeatMapChart(parent.getChartTitle(chartType), dataset, rowList,
		// columnList, colorScale);
		// return chart.getjPanel();

		final Color highColor = optionsFactory.getHighColorScale();
		final Color lowColor = optionsFactory.getLowColorScale();
		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (minOccurrenceThreshold > idSets.size())
			throw new IllegalMiapeArgumentException(
					"The occurrence threshold cannot be higher than the maximum number of identification sets ("
							+ idSets.size() + ")");
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			List<String> rowList = new ArrayList<String>();
			List<String> columnList = new ArrayList<String>();

			double[][] dataset = DatasetFactory.createProteinOccurrenceHeapMapDataSet(experimentList, idSets, rowList,
					columnList, proteinACCOrder, minOccurrenceThreshold, countNonConclusiveProteins);
			parent.addMinMaxHeatMapValues(dataset);

			HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, rowList, columnList,
					colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);
			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			List<String> rowList = new ArrayList<String>();
			List<String> columnList = new ArrayList<String>();

			double[][] dataset = DatasetFactory.createProteinOccurrenceHeapMapDataSet(experimentList, idSets, rowList,
					columnList, proteinACCOrder, minOccurrenceThreshold, countNonConclusiveProteins);
			parent.addMinMaxHeatMapValues(dataset);

			HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, rowList, columnList,
					colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);
			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			List<String> rowList = new ArrayList<String>();
			List<String> columnList = new ArrayList<String>();

			double[][] dataset = DatasetFactory.createProteinOccurrenceHeapMapDataSet(experimentList, idSets, rowList,
					columnList, proteinACCOrder, minOccurrenceThreshold, countNonConclusiveProteins);
			parent.addMinMaxHeatMapValues(dataset);

			HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, rowList, columnList,
					colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);
			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				List<String> rowList = new ArrayList<String>();
				List<String> columnList = new ArrayList<String>();
				idSets = getIdentificationSets(experiment.getName(), null, false);
				double[][] dataset = DatasetFactory.createProteinOccurrenceHeapMapDataSet(experiment, idSets, rowList,
						columnList, proteinACCOrder, minOccurrenceThreshold, countNonConclusiveProteins);

				HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, rowList, columnList,
						colorScale);
				chart.setHighValueColor(highColor);
				chart.setLowValueColor(lowColor);
				chartList.add(chart.getjPanel());
			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	/**
	 *
	 * @param plotItem
	 * @param peptidesPerProtein
	 *            only applicable when plotItem is a protein
	 * @return
	 */
	private Object showPeptidesPerProteinHeatMapChart(boolean isPSMs) {
		parent.setInformation1(parent.getCurrentChartType());

		double colorScale = optionsFactory.getColorScale();
		int minThreshold = optionsFactory.getHeatMapThreshold();
		//
		// double[][] dataset =
		// DatasetFactory.createHeapMapDataSet(experimentList, rowList,
		// columnList, plotItem, parent.distinguishModifiedPeptides(),
		// minOccurrenceThreshold);
		//
		// HeatMapChart chart = new
		// HeatMapChart(parent.getChartTitle(chartType), dataset, rowList,
		// columnList, colorScale);
		// return chart.getjPanel();

		final Color highColor = optionsFactory.getHighColorScale();
		final Color lowColor = optionsFactory.getLowColorScale();
		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		List<String> proteinACCOrder = parent.getProteinAccsFromACCFilter();
		// if (minOccurrenceThreshold > idSets.size())
		// throw new IllegalMiapeArgumentException(
		// "The occurrence threshold cannot be higher than the maximum number of
		// identification sets ("
		// + idSets.size() + ")");
		String title = "Peptides per protein heatmap";
		if (isPSMs) {
			title = "PSMs per protein heatmap";
		}
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			List<String> rowList = new ArrayList<String>();
			List<String> columnList = new ArrayList<String>();

			double[][] dataset = DatasetFactory.createPeptidesPerProteinHeapMapDataSet(experimentList, idSets, rowList,
					columnList, proteinACCOrder, parent.distinguishModifiedPeptides(), minThreshold,
					countNonConclusiveProteins, isPSMs);
			parent.addMinMaxHeatMapValues(dataset);

			HeatMapChart chart = new HeatMapChart(title, dataset, rowList, columnList, colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);
			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			List<String> rowList = new ArrayList<String>();
			List<String> columnList = new ArrayList<String>();

			double[][] dataset = DatasetFactory.createPeptidesPerProteinHeapMapDataSet(experimentList, idSets, rowList,
					columnList, proteinACCOrder, parent.distinguishModifiedPeptides(), minThreshold,
					countNonConclusiveProteins, isPSMs);
			parent.addMinMaxHeatMapValues(dataset);

			HeatMapChart chart = new HeatMapChart(title, dataset, rowList, columnList, colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);
			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			List<String> rowList = new ArrayList<String>();
			List<String> columnList = new ArrayList<String>();

			double[][] dataset = DatasetFactory.createPeptidesPerProteinHeapMapDataSet(experimentList, idSets, rowList,
					columnList, proteinACCOrder, parent.distinguishModifiedPeptides(), minThreshold,
					countNonConclusiveProteins, isPSMs);
			parent.addMinMaxHeatMapValues(dataset);

			HeatMapChart chart = new HeatMapChart(title, dataset, rowList, columnList, colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);
			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				List<String> rowList = new ArrayList<String>();
				List<String> columnList = new ArrayList<String>();
				idSets = getIdentificationSets(experiment.getName(), null, false);
				double[][] dataset = DatasetFactory.createPeptidesPerProteinHeapMapDataSet(experiment, idSets, rowList,
						columnList, proteinACCOrder, parent.distinguishModifiedPeptides(), minThreshold,
						countNonConclusiveProteins, isPSMs);

				HeatMapChart chart = new HeatMapChart(title, dataset, rowList, columnList, colorScale);
				chart.setHighValueColor(highColor);
				chart.setLowValueColor(lowColor);
				chartList.add(chart.getjPanel());
			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	// }

	private Object showOverlappingChart(IdentificationItemEnum plotItem) {
		this.vennChartMap.clear();
		parent.setInformation1(parent.getCurrentChartType() + " / " + plotItem);
		IdentificationSet idSet1 = null;
		String label1 = null;
		IdentificationSet idSet2 = null;
		String label2 = null;
		IdentificationSet idSet3 = null;
		String label3 = null;
		Color color1 = null;
		Color color2 = null;
		Color color3 = null;
		final Map<String, JCheckBox> checkBoxControls = optionsFactory.getIdSetsJCheckBoxes();
		// optionsFactory.setIntersectionText(null);
		ProteinGroupComparisonType proteinSelection = null;
		if (IdentificationItemEnum.PROTEIN.equals(plotItem)) {
			proteinSelection = optionsFactory.getProteinGroupComparisonType();
		}
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			final List<Experiment> experiments = experimentList.getExperiments();
			for (Experiment experiment : experiments) {
				for (Object identificationSet : experiment.getNextLevelIdentificationSetList()) {
					Replicate replicate = (Replicate) identificationSet;
					String repName = replicate.getFullName();
					if (checkBoxControls.containsKey(repName) && checkBoxControls.get(repName).isSelected()) {
						if (idSet1 == null) {
							idSet1 = replicate;
							label1 = replicate.getName();
							color1 = optionsFactory.getIdSetsColors().get(repName);
						} else if (idSet2 == null) {
							idSet2 = replicate;
							label2 = replicate.getName();
							color2 = optionsFactory.getIdSetsColors().get(repName);
						} else if (idSet3 == null) {
							idSet3 = replicate;
							label3 = replicate.getName();
							color3 = optionsFactory.getIdSetsColors().get(repName);
						} else {
							throw new IllegalMiapeArgumentException(
									"PACom can only represent Venn diagrams for up to 3 datasets");
						}
					}
				}
			}
			if (idSet1 == null || idSet2 == null) {
				throw new IllegalMiapeArgumentException("Please, select at least 2 datasets to show the diagram");
			}
			VennChart chart = new VennChart(parent.getChartTitle(chartType), idSet1, label1, idSet2, label2, idSet3,
					label3, plotItem, parent.distinguishModifiedPeptides(), countNonConclusiveProteins,
					proteinSelection, color1, color2, color3);

			this.vennChartMap.put(null, chart);
			String intersectionsText = chart.getIntersectionsText(null);
			optionsFactory.setIntersectionText(intersectionsText);
			return chart.getChartPanel();

		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final List<Experiment> experiments = experimentList.getExperiments();

			for (Experiment experiment : experiments) {
				String expName = experiment.getFullName();
				if (checkBoxControls.containsKey(expName) && checkBoxControls.get(expName).isSelected()) {
					if (idSet1 == null) {
						idSet1 = experiment;
						label1 = expName;
						color1 = optionsFactory.getIdSetsColors().get(expName);
					} else if (idSet2 == null) {
						idSet2 = experiment;
						label2 = expName;
						color2 = optionsFactory.getIdSetsColors().get(expName);
					} else if (idSet3 == null) {
						idSet3 = experiment;
						label3 = expName;
						color3 = optionsFactory.getIdSetsColors().get(expName);
					} else {
						throw new IllegalMiapeArgumentException(
								"PACom can only represent Venn diagrams for up to 3 datasets");
					}
				}
			}
			if (idSet1 == null || idSet2 == null) {
				throw new IllegalMiapeArgumentException("Please, select at least 2 datasets to show the diagram");
			}
			VennChart chart = new VennChart(parent.getChartTitle(chartType), idSet1, label1, idSet2, label2, idSet3,
					label3, plotItem, parent.distinguishModifiedPeptides(), countNonConclusiveProteins,
					proteinSelection, color1, color2, color3);
			this.vennChartMap.put(null, chart);
			optionsFactory.setIntersectionText(chart.getIntersectionsText(null));
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			idSet1 = experimentList;
			label1 = experimentList.getName();
			color1 = optionsFactory.getIdSetsColors().get(idSet1.getFullName());

			if (idSet1 == null) {
				throw new IllegalMiapeArgumentException(
						"Please, select another comparison level to have at least 2 datasets to show the diagram");
			}
			VennChart chart = new VennChart(parent.getChartTitle(chartType), idSet1, label1, idSet2, label2, idSet3,
					label3, plotItem, parent.distinguishModifiedPeptides(), countNonConclusiveProteins,
					proteinSelection, color1, color2, color3);
			this.vennChartMap.put(null, chart);
			optionsFactory.setIntersectionText(chart.getIntersectionsText(null));
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			String intersectionText = "";
			for (Experiment experiment : experimentList.getExperiments()) {
				int numReplicates = 1;
				idSet1 = null;
				label1 = null;
				idSet2 = null;
				label2 = null;
				idSet3 = null;
				label3 = null;
				for (Replicate replicate : experiment.getNextLevelIdentificationSetList()) {

					String repName = replicate.getFullName();
					if (checkBoxControls.containsKey(repName) && checkBoxControls.get(repName).isSelected()) {
						if (idSet1 == null) {
							idSet1 = replicate;
							label1 = replicate.getName();
							color1 = optionsFactory.getIdSetsColors().get(repName);
						} else if (idSet2 == null) {
							idSet2 = replicate;
							label2 = replicate.getName();
							color3 = optionsFactory.getIdSetsColors().get(repName);
						} else if (idSet3 == null) {
							idSet3 = replicate;
							label3 = replicate.getName();
							color3 = optionsFactory.getIdSetsColors().get(repName);
						} else {
							throw new IllegalMiapeArgumentException(
									"PACom can only represent Venn diagrams for up to 3 datasets");
						}
					}
				}
				if (idSet1 == null || idSet2 == null) {
					JLabel label = new JLabel("Please, select at least 2 datasets to show the Venn diagram for '"
							+ experiment.getName() + "'");
					JPanel panel = new JPanel();
					panel.add(label);
					chartList.add(panel);
					continue;
				}
				VennChart chart = new VennChart(parent.getChartTitle(chartType) + " (" + experiment.getName() + ")",
						idSet1, label1, idSet2, label2, idSet3, label3, plotItem, parent.distinguishModifiedPeptides(),
						countNonConclusiveProteins, proteinSelection, color1, color2, color3);
				this.vennChartMap.put(experiment.getName(), chart);

				intersectionText += chart.getIntersectionsText(experiment.getName());
				chartList.add(chart.getChartPanel());
			}
			optionsFactory.setIntersectionText(intersectionText);
			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}

		return null;
	}

	private Object showHistogramChart(IdentificationItemEnum plotItem) {
		parent.setInformation1(parent.getCurrentChartType() + " / " + plotItem);
		String xAxisLabel;
		String yAxisLabel = "";

		String scoreName = null;
		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();

		final boolean stackedChart = optionsFactory.showAsStackedChart();
		final boolean asPercentage = optionsFactory.getAsPercentage();
		final boolean pieChart = optionsFactory.showAsPieChart();
		final boolean average = optionsFactory.showAverageOverReplicates();
		final boolean occurrenceFilterEnabled = parent.isOccurrenceFilterEnabled();
		final boolean isTotalSerieShown = optionsFactory.isTotalSerieShown();
		final boolean differentIdentificationsShown = !optionsFactory.isDifferentIdentificationsShown();
		final boolean totalVersusDifferentSelected = optionsFactory.isTotalVersusDifferentSelected();
		String title = parent.getChartTitle(chartType);
		if (totalVersusDifferentSelected) {
			if (plotItem.equals(IdentificationItemEnum.PEPTIDE)) {
				yAxisLabel = "# peptides / # PSMs";
			} else {
				yAxisLabel = "# diff proteins / # total proteins";
			}
		} else if (differentIdentificationsShown) {
			if (plotItem.equals(IdentificationItemEnum.PEPTIDE)) {
				yAxisLabel = "# different peptides";
			} else {
				yAxisLabel = "# different proteins";
			}
		} else {
			if (plotItem.equals(IdentificationItemEnum.PEPTIDE)) {
				yAxisLabel = "# PSMs";
			} else {
				yAxisLabel = "# total proteins";
			}
		}

		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			if (!pieChart && !stackedChart && !occurrenceFilterEnabled && isTotalSerieShown)
				idSets.add(experimentList);
			// System.out.println("ha");
			xAxisLabel = "level 2";

			if (pieChart) {
				PieDataset dataset = null;
				if (totalVersusDifferentSelected) {
					dataset = DatasetFactory.createTotalVsDifferentNumberIdentificationPieDataSet(idSets, plotItem,
							parent.distinguishModifiedPeptides(), average, countNonConclusiveProteins);
				} else {
					dataset = DatasetFactory.createNumberIdentificationPieDataSet(idSets, plotItem,
							parent.distinguishModifiedPeptides(), average, differentIdentificationsShown,
							countNonConclusiveProteins);
				}
				PieChart chart = new PieChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), dataset);
				return chart.getChartPanel();
			} else if (stackedChart) {
				CategoryDataset dataset = null;
				if (totalVersusDifferentSelected) {
					dataset = DatasetFactory.createTotalVsDifferentNumberIdentificationCategoryDataSet(idSets, plotItem,
							parent.distinguishModifiedPeptides(), average, countNonConclusiveProteins);
				} else {
					dataset = DatasetFactory.createNumberIdentificationCategoryDataSet(idSets, plotItem,
							parent.distinguishModifiedPeptides(), average, differentIdentificationsShown,
							countNonConclusiveProteins);
				}
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				if (!totalVersusDifferentSelected) {
					chart.setIntegerItemLabels();
				}
				return chart.getChartPanel();
			} else {
				CategoryDataset dataset = null;
				if (!average) {
					if (totalVersusDifferentSelected) {
						dataset = DatasetFactory.createTotalVsDifferentNumberIdentificationCategoryDataSet(idSets,
								plotItem, parent.distinguishModifiedPeptides(), average, countNonConclusiveProteins);
					} else {
						dataset = DatasetFactory.createNumberIdentificationCategoryDataSet(idSets, plotItem,
								parent.distinguishModifiedPeptides(), average, differentIdentificationsShown,
								countNonConclusiveProteins);
					}
				} else {
					if (totalVersusDifferentSelected) {
						dataset = DatasetFactory.createTotalVsDifferentNumberIdentificationStatisticalCategoryDataSet(
								idSets, plotItem, parent.distinguishModifiedPeptides(), countNonConclusiveProteins);
					} else {

						dataset = DatasetFactory.createNumberIdentificationStatisticalCategoryDataSet(idSets, plotItem,
								parent.distinguishModifiedPeptides(), differentIdentificationsShown,
								countNonConclusiveProteins);
					}
				}
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				if (totalVersusDifferentSelected) {
					chart.setNonIntegerItemLabels();
				}
				// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			// System.out.println("sd");
			if (average) {
				if (plotItem.equals(plotItem.PEPTIDE)) {
					yAxisLabel = "average " + yAxisLabel;
				} else {
					yAxisLabel = "average " + yAxisLabel;
				}
			}
			xAxisLabel = "experiment list";
			if (pieChart) {
				PieDataset dataset = null;
				if (totalVersusDifferentSelected) {
					dataset = DatasetFactory.createTotalVsDifferentNumberIdentificationPieDataSet(idSets, plotItem,
							parent.distinguishModifiedPeptides(), average, countNonConclusiveProteins);
				} else {
					dataset = DatasetFactory.createNumberIdentificationPieDataSet(idSets, plotItem,
							parent.distinguishModifiedPeptides(), average, differentIdentificationsShown,
							countNonConclusiveProteins);
				}
				PieChart chart = new PieChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), dataset);
				return chart.getChartPanel();
			} else if (stackedChart) {
				CategoryDataset dataset = null;
				if (totalVersusDifferentSelected) {
					dataset = DatasetFactory.createTotalVsDifferentNumberIdentificationCategoryDataSet(idSets, plotItem,
							parent.distinguishModifiedPeptides(), average, countNonConclusiveProteins);
				} else {
					dataset = DatasetFactory.createNumberIdentificationCategoryDataSet(idSets, plotItem,
							parent.distinguishModifiedPeptides(), average, differentIdentificationsShown,
							countNonConclusiveProteins);
				}
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				if (!totalVersusDifferentSelected) {
					chart.setIntegerItemLabels();
				}
				return chart.getChartPanel();
			} else {
				CategoryDataset dataset = null;
				// if (isOccurrenceByReplicatesEnabled)
				if (!average) {
					if (totalVersusDifferentSelected) {
						dataset = DatasetFactory.createTotalVsDifferentNumberIdentificationCategoryDataSet(idSets,
								plotItem, parent.distinguishModifiedPeptides(), average, countNonConclusiveProteins);
					} else {
						dataset = DatasetFactory.createNumberIdentificationCategoryDataSet(idSets, plotItem,
								parent.distinguishModifiedPeptides(), average, differentIdentificationsShown,
								countNonConclusiveProteins);
					}
				} else {
					if (totalVersusDifferentSelected) {
						dataset = DatasetFactory.createTotalVsDifferentNumberIdentificationCategoryDataSet(idSets,
								plotItem, parent.distinguishModifiedPeptides(), average, countNonConclusiveProteins);
					} else {
						dataset = DatasetFactory.createNumberIdentificationStatisticalCategoryDataSet(idSets, plotItem,
								parent.distinguishModifiedPeptides(), differentIdentificationsShown,
								countNonConclusiveProteins);
					}
				}
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				if (totalVersusDifferentSelected) {
					chart.setNonIntegerItemLabels();
				}
				// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			if (!pieChart && !stackedChart && idSets.size() > 1 && !occurrenceFilterEnabled && isTotalSerieShown) {
				idSets.add(experimentList);
			}
			// System.out.println("sd");
			if (average) {
				if (plotItem.equals(plotItem.PEPTIDE)) {
					yAxisLabel = "average " + yAxisLabel;
				} else {
					yAxisLabel = "average " + yAxisLabel;
				}
			}
			xAxisLabel = "experiment";
			if (pieChart) {
				PieDataset dataset = null;
				if (totalVersusDifferentSelected) {
					dataset = DatasetFactory.createTotalVsDifferentNumberIdentificationPieDataSet(idSets, plotItem,
							parent.distinguishModifiedPeptides(), average, countNonConclusiveProteins);
				} else {
					dataset = DatasetFactory.createNumberIdentificationPieDataSet(idSets, plotItem,
							parent.distinguishModifiedPeptides(), average, differentIdentificationsShown,
							countNonConclusiveProteins);
				}
				PieChart chart = new PieChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), dataset);
				return chart.getChartPanel();
			} else if (stackedChart) {
				CategoryDataset dataset = null;
				if (totalVersusDifferentSelected) {
					dataset = DatasetFactory.createTotalVsDifferentNumberIdentificationCategoryDataSet(idSets, plotItem,
							parent.distinguishModifiedPeptides(), average, countNonConclusiveProteins);
				} else {
					dataset = DatasetFactory.createNumberIdentificationCategoryDataSet(idSets, plotItem,
							parent.distinguishModifiedPeptides(), average, differentIdentificationsShown,
							countNonConclusiveProteins);
				}
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				if (!totalVersusDifferentSelected) {
					chart.setIntegerItemLabels();
				}
				return chart.getChartPanel();
			} else {
				CategoryDataset dataset = null;
				// if (isOccurrenceByReplicatesEnabled)
				if (!average) {
					if (totalVersusDifferentSelected) {
						dataset = DatasetFactory.createTotalVsDifferentNumberIdentificationCategoryDataSet(idSets,
								plotItem, parent.distinguishModifiedPeptides(), average, countNonConclusiveProteins);
					} else {
						dataset = DatasetFactory.createNumberIdentificationCategoryDataSet(idSets, plotItem,
								parent.distinguishModifiedPeptides(), average, differentIdentificationsShown,
								countNonConclusiveProteins);
					}
				} else {
					if (totalVersusDifferentSelected) {
						dataset = DatasetFactory.createTotalVsDifferentNumberIdentificationCategoryDataSet(idSets,
								plotItem, parent.distinguishModifiedPeptides(), average, countNonConclusiveProteins);
					} else {
						dataset = DatasetFactory.createNumberIdentificationStatisticalCategoryDataSet(idSets, plotItem,
								parent.distinguishModifiedPeptides(), differentIdentificationsShown,
								countNonConclusiveProteins);
					}
				}
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				if (totalVersusDifferentSelected) {
					chart.setNonIntegerItemLabels();
				}
				// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, false);
				if (!pieChart && !stackedChart && idSets.size() > 1 && !occurrenceFilterEnabled && isTotalSerieShown) {
					idSets.add(experiment);
				}

				xAxisLabel = experiment.getName();
				if (pieChart) {
					PieDataset dataset = null;
					if (totalVersusDifferentSelected) {
						dataset = DatasetFactory.createTotalVsDifferentNumberIdentificationPieDataSet(idSets, plotItem,
								parent.distinguishModifiedPeptides(), average, countNonConclusiveProteins);
					} else {
						dataset = DatasetFactory.createNumberIdentificationPieDataSet(idSets, plotItem,
								parent.distinguishModifiedPeptides(), average, differentIdentificationsShown,
								countNonConclusiveProteins);
					}
					PieChart chart = new PieChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), dataset);

					chartList.add(chart.getChartPanel());
				} else if (stackedChart) {
					CategoryDataset dataset = null;
					if (totalVersusDifferentSelected) {
						dataset = DatasetFactory.createTotalVsDifferentNumberIdentificationCategoryDataSet(idSets,
								plotItem, parent.distinguishModifiedPeptides(), average, countNonConclusiveProteins);
					} else {
						dataset = DatasetFactory.createNumberIdentificationCategoryDataSet(idSets, plotItem,
								parent.distinguishModifiedPeptides(), average, differentIdentificationsShown,
								countNonConclusiveProteins);
					}
					StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation, asPercentage);
					if (!totalVersusDifferentSelected) {
						chart.setIntegerItemLabels();
					}
					chartList.add(chart.getChartPanel());
				} else {
					CategoryDataset dataset = null;
					if (!average) {
						if (totalVersusDifferentSelected) {
							dataset = DatasetFactory.createTotalVsDifferentNumberIdentificationCategoryDataSet(idSets,
									plotItem, parent.distinguishModifiedPeptides(), average,
									countNonConclusiveProteins);
						} else {
							dataset = DatasetFactory.createNumberIdentificationCategoryDataSet(idSets, plotItem,
									parent.distinguishModifiedPeptides(), average, differentIdentificationsShown,
									countNonConclusiveProteins);
						}
					} else {
						if (totalVersusDifferentSelected) {
							dataset = DatasetFactory.createTotalVsDifferentNumberIdentificationCategoryDataSet(idSets,
									plotItem, parent.distinguishModifiedPeptides(), average,
									countNonConclusiveProteins);
						} else {
							dataset = DatasetFactory.createNumberIdentificationStatisticalCategoryDataSet(idSets,
									plotItem, parent.distinguishModifiedPeptides(), differentIdentificationsShown,
									countNonConclusiveProteins);
						}
					}
					BarChart chart = new BarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation);
					if (totalVersusDifferentSelected) {
						chart.setNonIntegerItemLabels();
					}
					chartList.add(chart.getChartPanel());
				}
			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}

		return null;
	}

	private Object showSingleHitProteinsChart() {
		parent.setInformation1(parent.getCurrentChartType());
		String xAxisLabel;
		String yAxisLabel = "";

		String scoreName = null;
		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();

		final boolean stackedChart = optionsFactory.showAsStackedChart();
		final boolean asPercentage = optionsFactory.getAsPercentage();
		final boolean pieChart = optionsFactory.showAsPieChart();
		final boolean isTotalSerieShown = optionsFactory.isTotalSerieShown();
		final boolean differentIdentificationsShown = optionsFactory.isDifferentIdentificationsShown();
		final boolean occurrenceFilterEnabled = parent.isOccurrenceFilterEnabled();

		if (differentIdentificationsShown)
			yAxisLabel = "# different single hit proteins";
		else
			yAxisLabel = "# single hit proteins";

		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			if (!pieChart && !stackedChart && !occurrenceFilterEnabled && isTotalSerieShown)
				idSets.add(experimentList);
			// System.out.println("ha");
			xAxisLabel = "level 2";

			if (pieChart) {
				PieDataset dataset = null;

				dataset = DatasetFactory.createNumberSingleHitProteinsPieDataSet(idSets,

						differentIdentificationsShown, countNonConclusiveProteins);
				PieChart chart = new PieChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), dataset);
				return chart.getChartPanel();
			} else if (stackedChart) {
				CategoryDataset dataset = null;
				dataset = DatasetFactory.createNumberSingleHitProteinsCategoryDataSet(idSets,
						differentIdentificationsShown, countNonConclusiveProteins);
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				chart.setIntegerItemLabels();
				return chart.getChartPanel();
			} else {
				CategoryDataset dataset = null;

				dataset = DatasetFactory.createNumberSingleHitProteinsCategoryDataSet(idSets,
						differentIdentificationsShown, countNonConclusiveProteins);
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			if (!pieChart && !stackedChart && idSets.size() > 1 && !occurrenceFilterEnabled && isTotalSerieShown)
				idSets.add(experimentList);
			// System.out.println("sd");

			xAxisLabel = "experiment list";
			if (pieChart) {
				PieDataset dataset = null;
				dataset = DatasetFactory.createNumberSingleHitProteinsPieDataSet(idSets, differentIdentificationsShown,
						countNonConclusiveProteins);
				PieChart chart = new PieChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), dataset);
				return chart.getChartPanel();
			} else if (stackedChart) {
				CategoryDataset dataset = null;

				dataset = DatasetFactory.createNumberSingleHitProteinsCategoryDataSet(idSets,
						differentIdentificationsShown, countNonConclusiveProteins);
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				chart.setIntegerItemLabels();
				return chart.getChartPanel();
			} else {
				CategoryDataset dataset = null;
				// if (isOccurrenceByReplicatesEnabled)

				dataset = DatasetFactory.createNumberSingleHitProteinsCategoryDataSet(idSets,
						differentIdentificationsShown, countNonConclusiveProteins);
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);

				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			if (!pieChart && !stackedChart && idSets.size() > 1 && !occurrenceFilterEnabled && isTotalSerieShown)
				idSets.add(experimentList);
			// System.out.println("sd");

			xAxisLabel = "experiment";
			if (pieChart) {
				PieDataset dataset = null;
				dataset = DatasetFactory.createNumberSingleHitProteinsPieDataSet(idSets, differentIdentificationsShown,
						countNonConclusiveProteins);
				PieChart chart = new PieChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), dataset);
				return chart.getChartPanel();
			} else if (stackedChart) {
				CategoryDataset dataset = null;

				dataset = DatasetFactory.createNumberSingleHitProteinsCategoryDataSet(idSets,
						differentIdentificationsShown, countNonConclusiveProteins);
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				chart.setIntegerItemLabels();
				return chart.getChartPanel();
			} else {
				CategoryDataset dataset = null;
				// if (isOccurrenceByReplicatesEnabled)

				dataset = DatasetFactory.createNumberSingleHitProteinsCategoryDataSet(idSets,
						differentIdentificationsShown, countNonConclusiveProteins);
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);

				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, false);
				if (!pieChart && !stackedChart && idSets.size() > 1 && !occurrenceFilterEnabled && isTotalSerieShown)
					idSets.add(experiment);
				xAxisLabel = experiment.getName();
				if (pieChart) {
					PieDataset dataset = DatasetFactory.createNumberSingleHitProteinsPieDataSet(idSets,
							differentIdentificationsShown, countNonConclusiveProteins);
					PieChart chart = new PieChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), dataset);

					chartList.add(chart.getChartPanel());
				} else if (stackedChart) {
					CategoryDataset dataset = null;
					dataset = DatasetFactory.createNumberSingleHitProteinsCategoryDataSet(idSets,
							differentIdentificationsShown, countNonConclusiveProteins);
					StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation, asPercentage);

					chart.setIntegerItemLabels();

					chartList.add(chart.getChartPanel());
				} else {
					CategoryDataset dataset = null;

					dataset = DatasetFactory.createNumberSingleHitProteinsCategoryDataSet(idSets,
							differentIdentificationsShown, countNonConclusiveProteins);

					BarChart chart = new BarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation);

					chartList.add(chart.getChartPanel());
				}
			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	private Object showNumberPeptidesInProteinsChart() {
		parent.setInformation1(parent.getCurrentChartType());
		String xAxisLabel;
		String yAxisLabel = "";

		String scoreName = null;
		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();

		final boolean stackedChart = optionsFactory.showAsStackedChart();
		final boolean asPercentage = optionsFactory.getAsPercentage();
		final boolean isTotalSerieShown = optionsFactory.isTotalSerieShown();
		final boolean showPSMsorPeptides = optionsFactory.isDifferentIdentificationsShown();
		final boolean occurrenceFilterEnabled = parent.isOccurrenceFilterEnabled();
		final int maximum = optionsFactory.getMaximumOccurrence();

		// if (showPSMsorPeptides)
		yAxisLabel = "# different proteins";
		// else
		// yAxisLabel = "# proteins";

		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			if (!stackedChart && idSets.size() > 1 && !occurrenceFilterEnabled && isTotalSerieShown)
				idSets.add(experimentList);
			// System.out.println("ha");
			xAxisLabel = "level 2";

			if (stackedChart) {
				CategoryDataset dataset = null;
				dataset = DatasetFactory.createPeptideNumberInProteinsCategoryDataSet(idSets, maximum,
						showPSMsorPeptides, countNonConclusiveProteins);
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				chart.setIntegerItemLabels();
				return chart.getChartPanel();
			} else {
				CategoryDataset dataset = null;

				dataset = DatasetFactory.createPeptideNumberInProteinsCategoryDataSet(idSets, maximum,
						showPSMsorPeptides, countNonConclusiveProteins);
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			if (!stackedChart && idSets.size() > 1 && !occurrenceFilterEnabled && isTotalSerieShown)
				idSets.add(experimentList);
			// System.out.println("sd");

			xAxisLabel = "experiment list";
			if (stackedChart) {
				CategoryDataset dataset = null;

				dataset = DatasetFactory.createPeptideNumberInProteinsCategoryDataSet(idSets, maximum,
						showPSMsorPeptides, countNonConclusiveProteins);
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				chart.setIntegerItemLabels();
				return chart.getChartPanel();
			} else {
				CategoryDataset dataset = null;
				// if (isOccurrenceByReplicatesEnabled)

				dataset = DatasetFactory.createPeptideNumberInProteinsCategoryDataSet(idSets, maximum,
						showPSMsorPeptides, countNonConclusiveProteins);
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);

				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			if (!stackedChart && idSets.size() > 1 && !occurrenceFilterEnabled && isTotalSerieShown)
				idSets.add(experimentList);
			// System.out.println("sd");

			xAxisLabel = "experiment";
			if (stackedChart) {
				CategoryDataset dataset = null;

				dataset = DatasetFactory.createPeptideNumberInProteinsCategoryDataSet(idSets, maximum,
						showPSMsorPeptides, countNonConclusiveProteins);
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				chart.setIntegerItemLabels();
				return chart.getChartPanel();
			} else {
				CategoryDataset dataset = null;
				// if (isOccurrenceByReplicatesEnabled)

				dataset = DatasetFactory.createPeptideNumberInProteinsCategoryDataSet(idSets, maximum,
						showPSMsorPeptides, countNonConclusiveProteins);
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);

				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, false);
				if (!stackedChart && idSets.size() > 1 && !occurrenceFilterEnabled && isTotalSerieShown)
					idSets.add(experiment);
				xAxisLabel = experiment.getName();
				if (stackedChart) {
					CategoryDataset dataset = null;
					dataset = DatasetFactory.createPeptideNumberInProteinsCategoryDataSet(idSets, maximum,
							showPSMsorPeptides, countNonConclusiveProteins);
					StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation, asPercentage);

					chart.setIntegerItemLabels();

					chartList.add(chart.getChartPanel());
				} else {
					CategoryDataset dataset = null;

					dataset = DatasetFactory.createPeptideNumberInProteinsCategoryDataSet(idSets, maximum,
							showPSMsorPeptides, countNonConclusiveProteins);

					BarChart chart = new BarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation);

					chartList.add(chart.getChartPanel());
				}
			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	private Object showProteinCoverageHistogramChart() {
		parent.setInformation1(parent.getCurrentChartType());
		String xAxisLabel;
		String yAxisLabel = "Average Protein Coverage";

		String scoreName = null;
		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		boolean retrieveProteinSeqs = false;
		if (!parent.isProteinSequencesRetrieved()) {
			final int selectedOption = JOptionPane.showConfirmDialog(parent,
					"<html>In order to calculate protein coverage, the program will retrieve the protein sequence from the Internet,<br>which can take several minutes, depending on the number of proteins.<br>Are you sure you want to continue?</html>",
					"Warning", JOptionPane.YES_NO_OPTION);

			if (selectedOption == JOptionPane.YES_OPTION)
				retrieveProteinSeqs = true;
			parent.setProteinSequencesRetrieved(retrieveProteinSeqs);
		}
		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			xAxisLabel = "level 2";

			CategoryDataset dataset = DatasetFactory.createAverageProteinCoverageStatisticalCategoryDataSet(idSets,
					retrieveProteinSeqs, countNonConclusiveProteins);
			BarChart chart = new BarChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
					xAxisLabel, yAxisLabel, dataset, plotOrientation);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			xAxisLabel = "experiment";

			CategoryDataset dataset = DatasetFactory.createAverageProteinCoverageStatisticalCategoryDataSet(idSets,
					retrieveProteinSeqs, countNonConclusiveProteins);
			BarChart chart = new BarChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
					xAxisLabel, yAxisLabel, dataset, plotOrientation);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			xAxisLabel = "experiment list";

			CategoryDataset dataset = DatasetFactory.createAverageProteinCoverageStatisticalCategoryDataSet(idSets,
					retrieveProteinSeqs, countNonConclusiveProteins);
			BarChart chart = new BarChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
					xAxisLabel, yAxisLabel, dataset, plotOrientation);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				xAxisLabel = experiment.getName();
				CategoryDataset dataset = DatasetFactory.createAverageProteinCoverageStatisticalCategoryDataSet(idSets,
						retrieveProteinSeqs, countNonConclusiveProteins);
				BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				chartList.add(chart.getChartPanel());
			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	private Object showProteinCoverageDistributionChart() {
		parent.setInformation1(parent.getCurrentChartType());
		int bins = optionsFactory.getHistogramBins();
		boolean addZeroZeroValue = true;
		HistogramType histogramType = optionsFactory.getHistogramType();

		String xAxisLabel = "Protein coverage (%)";
		boolean retrieveProteinSeqs = false;
		if (!parent.isProteinSequencesRetrieved()) {
			final int selectedOption = JOptionPane.showConfirmDialog(parent,
					"<html>In order to calculate protein coverage, the program will retrieve the protein sequence from the Internet,<br>which can take several minutes, depending on the number of proteins.<br>Are you sure you want to continue?</html>",
					"Warning", JOptionPane.YES_NO_OPTION);

			if (selectedOption == JOptionPane.YES_OPTION)
				retrieveProteinSeqs = true;
			parent.setProteinSequencesRetrieved(retrieveProteinSeqs);
		}
		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			HistogramDataset dataset = DatasetFactory.createProteinCoverageHistogramDataSet(idSets, bins, histogramType,
					retrieveProteinSeqs, countNonConclusiveProteins);
			HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			HistogramDataset dataset = DatasetFactory.createProteinCoverageHistogramDataSet(idSets, bins, histogramType,
					retrieveProteinSeqs, countNonConclusiveProteins);
			HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			HistogramDataset dataset = DatasetFactory.createProteinCoverageHistogramDataSet(idSets, bins, histogramType,
					retrieveProteinSeqs, countNonConclusiveProteins);
			HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(null, null, true);
				HistogramDataset dataset = DatasetFactory.createProteinCoverageHistogramDataSet(idSets, bins,
						histogramType, retrieveProteinSeqs, countNonConclusiveProteins);
				HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType), experiment.getName(),
						dataset, xAxisLabel);
				// chart.setXRangeValues(0, 100);
				chartList.add(chart.getChartPanel());
			}
			return chartList;
			// this.jPanelChart.addGraphicPanel(chartList);
		}
		return null;
	}

	private Object showRepeatabilityHistogramStackedChart(IdentificationItemEnum plotItem) {
		parent.setInformation1(parent.getCurrentChartType() + " / " + plotItem);
		String xAxisLabel;
		String yAxisLabel = "";
		boolean asPercentage = optionsFactory.getAsPercentage();
		PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		if (plotItem.equals(IdentificationItemEnum.PEPTIDE))
			yAxisLabel = "# peptides";
		else
			yAxisLabel = "# proteins";
		int maximum = optionsFactory.getMaximumOccurrence();
		boolean overReplicates = optionsFactory.getOverReplicates();
		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			xAxisLabel = "experiment";
			CategoryDataset dataset = null;
			if (!overReplicates)
				dataset = DatasetFactory.createRepeatabilityCategoryDataSet(idSets, plotItem,
						parent.distinguishModifiedPeptides(), maximum, countNonConclusiveProteins);
			else
				dataset = DatasetFactory.createRepeatabilityOverReplicatesCategoryDataSet(idSets, plotItem,
						parent.distinguishModifiedPeptides(), maximum, countNonConclusiveProteins);
			StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
					asPercentage);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			xAxisLabel = "experiment list";
			CategoryDataset dataset = null;
			if (!overReplicates)
				dataset = DatasetFactory.createRepeatabilityCategoryDataSet(idSets, plotItem,
						parent.distinguishModifiedPeptides(), maximum, countNonConclusiveProteins);
			else
				dataset = DatasetFactory.createRepeatabilityOverReplicatesCategoryDataSet(idSets, plotItem,
						parent.distinguishModifiedPeptides(), maximum, countNonConclusiveProteins);
			StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
					asPercentage);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_REPLICATE.equals(option)) {

			xAxisLabel = "experiment";

			CategoryDataset dataset = null;
			if (!overReplicates)
				dataset = DatasetFactory.createRepeatabilityCategoryDataSet(idSets, plotItem,
						parent.distinguishModifiedPeptides(), maximum, countNonConclusiveProteins);
			else
				dataset = DatasetFactory.createRepeatabilityOverReplicatesCategoryDataSet(idSets, plotItem,
						parent.distinguishModifiedPeptides(), maximum, countNonConclusiveProteins);
			StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
					asPercentage);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				xAxisLabel = "experiment";

				CategoryDataset dataset = null;
				if (!overReplicates)
					dataset = DatasetFactory.createRepeatabilityCategoryDataSet(idSets, plotItem,
							parent.distinguishModifiedPeptides(), maximum, countNonConclusiveProteins);
				else
					dataset = DatasetFactory.createRepeatabilityOverReplicatesCategoryDataSet(idSets, plotItem,
							parent.distinguishModifiedPeptides(), maximum, countNonConclusiveProteins);
				StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				chartList.add(chart.getChartPanel());
			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	private Object showScoreComparisonScatterChart(IdentificationItemEnum plotItem) {
		parent.setInformation1(parent.getCurrentChartType() + " / " + plotItem);
		String scoreName = null;
		boolean distinguish = parent.distinguishModifiedPeptides();
		if (plotItem.equals(IdentificationItemEnum.PEPTIDE)) {
			scoreName = optionsFactory.getPeptideScoreName();

		} else {
			scoreName = optionsFactory.getProteinScoreName();
		}
		final Map<String, JCheckBox> scoreComparisonJCheckBoxes = optionsFactory.getIdSetsJCheckBoxes();

		String xAxisLabel = scoreName;
		String yAxisLabel = scoreName;

		boolean showRegressionLine = optionsFactory.showRegressionLine();
		boolean showDiagonalLine = optionsFactory.showDiagonalLine();
		boolean applyLog = optionsFactory.isApplyLog();
		boolean separateDecoyHits = optionsFactory.isSeparatedDecoyHits();
		List<IdentificationSet> idSets = getIdentificationSets(null, scoreComparisonJCheckBoxes, false);
		if (idSets.size() == 2) {
			xAxisLabel = scoreName + "(" + idSets.get(0).getFullName() + ")";
			yAxisLabel = scoreName + "(" + idSets.get(1).getFullName() + ")";
		}
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			XYDataset dataset = DatasetFactory.createScoreXYDataSet(idSets, scoreName, plotItem, distinguish, applyLog,
					separateDecoyHits, countNonConclusiveProteins);
			XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
			if (showRegressionLine)
				chart.addRegressionLine(true);
			if (showDiagonalLine)
				chart.addDiagonalLine();
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			XYDataset dataset = DatasetFactory.createScoreXYDataSet(idSets, scoreName, plotItem, distinguish, applyLog,
					separateDecoyHits, countNonConclusiveProteins);
			XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
			if (showRegressionLine)
				chart.addRegressionLine(true);
			if (showDiagonalLine)
				chart.addDiagonalLine();
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				try {
					idSets = getIdentificationSets(experiment.getFullName(), scoreComparisonJCheckBoxes, false);

					if (!idSets.isEmpty()) {
						XYDataset dataset = DatasetFactory.createScoreXYDataSet(idSets, scoreName, plotItem,
								distinguish, applyLog, separateDecoyHits, countNonConclusiveProteins);
						XYPointChart chart = new XYPointChart(
								parent.getChartTitle(chartType) + ": " + experiment.getName(),
								parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
						if (showRegressionLine)
							chart.addRegressionLine(true);
						if (showDiagonalLine)
							chart.addDiagonalLine();
						chartList.add(chart.getChartPanel());
					}
				} catch (IllegalMiapeArgumentException e) {
					JPanel jpanel = new JPanel();
					jpanel.add(new JLabel("<html>Error generating chart for " + experiment.getName() + "<br>"
							+ e.getMessage() + "</html>"));
					chartList.add(jpanel);
				}

			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			throw new IllegalMiapeArgumentException(
					"At least two series are needed to generate the chart. Select a lower level in the chart option");
		}
		return null;
	}

	private Object showScoreDistributionHistogramLineChart(IdentificationItemEnum plotItem) {
		parent.setInformation1(parent.getCurrentChartType() + " / " + plotItem);
		String scoreName = null;
		if (plotItem.equals(IdentificationItemEnum.PEPTIDE)) {
			scoreName = optionsFactory.getPeptideScoreName();
			if (scoreName == null)
				throw new IllegalMiapeArgumentException("There is not a peptide score available");
		} else {
			scoreName = optionsFactory.getProteinScoreName();
			if (scoreName == null)
				throw new IllegalMiapeArgumentException("There is not a protein score available");
		}

		int bins = optionsFactory.getHistogramBins();
		boolean addTotal = optionsFactory.isTotalSerieShown();
		boolean addZeroZeroValue = false;
		HistogramType histogramType = optionsFactory.getHistogramType();
		boolean applyLog = optionsFactory.isApplyLog();
		boolean separateDecoyHits = optionsFactory.isSeparatedDecoyHits();
		String xAxisLabel = scoreName;

		List<IdentificationSet> idSets = getIdentificationSets(null, null, addTotal);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			HistogramDataset dataset = DatasetFactory.createScoreHistogramDataSet(idSets, scoreName, plotItem, bins,
					addZeroZeroValue, histogramType, applyLog, separateDecoyHits, countNonConclusiveProteins);
			HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			HistogramDataset dataset = DatasetFactory.createScoreHistogramDataSet(idSets, scoreName, plotItem, bins,
					addZeroZeroValue, histogramType, applyLog, separateDecoyHits, countNonConclusiveProteins);
			HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			HistogramDataset dataset = DatasetFactory.createScoreHistogramDataSet(idSets, scoreName, plotItem, bins,
					addZeroZeroValue, histogramType, applyLog, separateDecoyHits, countNonConclusiveProteins);
			HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {

				idSets = getIdentificationSets(experiment.getName(), null, addTotal);
				HistogramDataset dataset = DatasetFactory.createScoreHistogramDataSet(idSets, scoreName, plotItem, bins,
						addZeroZeroValue, histogramType, applyLog, separateDecoyHits, countNonConclusiveProteins);
				HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType), experiment.getName(),
						dataset, xAxisLabel);
				chartList.add(chart.getChartPanel());
			}
			return chartList;
			// this.jPanelChart.addGraphicPanel(chartList);
		}
		return null;
	}

	private Object showFDRChart() {
		parent.setInformation1(parent.getCurrentChartType());
		String yAxisLabel = null;
		boolean showProteinLevel = optionsFactory.showProteinFDRLevel();
		boolean showPeptideLevel = optionsFactory.showPeptideFDRLevel();
		boolean showPSMLevel = optionsFactory.showPSMFDRLevel();

		if (showProteinLevel && !showPeptideLevel && !showPSMLevel) {
			yAxisLabel = "# proteins";
		} else if (!showProteinLevel && showPeptideLevel && !showPSMLevel) {
			yAxisLabel = "# peptides";
		} else if (!showProteinLevel && !showPeptideLevel && showPSMLevel) {
			yAxisLabel = "# PSMs";
		} else {
			yAxisLabel = "# (Proteins/Peptides/PSMs)";
		}
		String xAxisLabel = "FDR (%)";
		final boolean isTotalSerieShown = optionsFactory.isTotalSerieShown();
		List<IdentificationSet> idSets = getIdentificationSets(null, null, isTotalSerieShown);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			XYDataset dataset = DatasetFactory.createFDRDataSet(idSets, showProteinLevel, showPeptideLevel,
					showPSMLevel, countNonConclusiveProteins);
			XYLineChart chart = new XYLineChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			XYDataset dataset = DatasetFactory.createFDRDataSet(idSets, showProteinLevel, showPeptideLevel,
					showPSMLevel, countNonConclusiveProteins);
			XYLineChart chart = new XYLineChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);

			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			XYDataset dataset = DatasetFactory.createFDRDataSet(idSets, showProteinLevel, showPeptideLevel,
					showPSMLevel, countNonConclusiveProteins);
			XYLineChart chart = new XYLineChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);

			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, isTotalSerieShown);
				if (isTotalSerieShown)
					idSets.add(experiment);
				XYDataset dataset = DatasetFactory.createFDRDataSet(idSets, showProteinLevel, showPeptideLevel,
						showPSMLevel, countNonConclusiveProteins);
				XYLineChart chart = new XYLineChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
				chartList.add(chart.getChartPanel());
			}
			return chartList;
			// this.jPanelChart.addGraphicPanel(chartList);
		}

		return null;
	}

	private Object showFDRPlots() {
		parent.setInformation1(parent.getCurrentChartType());
		String yAxisLabel = "FDR (%)";

		boolean showPSMs = optionsFactory.showPSMs();
		boolean showPeptides = optionsFactory.showPeptides();
		boolean showProteins = optionsFactory.showProteins();
		boolean showScoreVsFDR = optionsFactory.showScoreVsFDR();
		if (!showPSMs && !showPeptides && !showProteins && !showScoreVsFDR)
			throw new IllegalMiapeArgumentException(
					"Select PSM, Peptides, Proteins or Score vs FDR to generate the chart");
		if (!showPSMs && !showPeptides && !showProteins && showScoreVsFDR)
			yAxisLabel = "# proteins";

		List<IdentificationSet> idSets = getIdentificationSets(null, optionsFactory.getIdSetsJCheckBoxes(), false);
		java.util.Set<String> scoreNames = new THashSet<String>();
		for (IdentificationSet identificationSet : idSets) {
			FDRFilter fdrFilter = identificationSet.getFDRFilter();
			if (fdrFilter != null)
				if (!scoreNames.contains(fdrFilter.getSortingParameters().getScoreName()))
					scoreNames.add(fdrFilter.getSortingParameters().getScoreName());
		}
		String scoreNamesString = "";
		for (String string : scoreNames) {
			if (!"".equals(scoreNamesString))
				scoreNamesString = scoreNamesString + ", ";
			scoreNamesString = scoreNamesString + string;
		}
		String xAxisLabel = "Peptide score (" + scoreNamesString + ")";

		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			XYLineChart chart = null;
			try {
				XYDataset dataset = DatasetFactory.createFDRvsScoreDataSet(idSets, showPSMs, showPeptides, showProteins,
						countNonConclusiveProteins);

				chart = new XYLineChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
						dataset, xAxisLabel, yAxisLabel);
			} catch (IllegalMiapeArgumentException e) {

			}
			if (showScoreVsFDR) {
				XYDataset dataset2 = null;
				try {
					dataset2 = DatasetFactory.createScoreVsNumProteinsDataSet(idSets, countNonConclusiveProteins);
				} catch (IllegalMiapeArgumentException e) {

				}
				if (chart != null)
					chart.addNewAxis(dataset2, "# proteins");
				else {
					chart = new XYLineChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
							dataset2, xAxisLabel, yAxisLabel);
				}
			}
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			XYLineChart chart = null;
			try {
				XYDataset dataset = DatasetFactory.createFDRvsScoreDataSet(idSets, showPSMs, showPeptides, showProteins,
						countNonConclusiveProteins);

				chart = new XYLineChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
						dataset, xAxisLabel, yAxisLabel);
			} catch (IllegalMiapeArgumentException e) {

			}
			if (showScoreVsFDR) {
				XYDataset dataset2 = null;
				try {
					dataset2 = DatasetFactory.createScoreVsNumProteinsDataSet(idSets, countNonConclusiveProteins);
				} catch (IllegalMiapeArgumentException e) {

				}
				if (chart != null)
					chart.addNewAxis(dataset2, "# proteins");
				else {
					chart = new XYLineChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
							dataset2, xAxisLabel, yAxisLabel);
				}
			}
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			XYLineChart chart = null;
			try {

				XYDataset dataset = DatasetFactory.createFDRvsScoreDataSet(idSets, showPSMs, showPeptides, showProteins,
						countNonConclusiveProteins);

				chart = new XYLineChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
						dataset, xAxisLabel, yAxisLabel);

			} catch (IllegalMiapeArgumentException e) {

			}
			if (showScoreVsFDR) {
				XYDataset dataset2 = null;
				try {
					dataset2 = DatasetFactory.createScoreVsNumProteinsDataSet(idSets, countNonConclusiveProteins);
				} catch (IllegalMiapeArgumentException e) {

				}
				if (chart != null)
					chart.addNewAxis(dataset2, "num proteins");
				else {
					chart = new XYLineChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
							dataset2, xAxisLabel, yAxisLabel);
				}
			}
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), optionsFactory.getIdSetsJCheckBoxes(), false);
				XYLineChart chart = null;

				// if (fdrFilter != null)
				// xAxisLabel = xAxisLabel + ": "
				// + fdrFilter.getSortingParameters().getScoreName();
				try {
					XYDataset dataset = DatasetFactory.createFDRvsScoreDataSet(idSets, showPSMs, showPeptides,
							showProteins, countNonConclusiveProteins);

					chart = new XYLineChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
							dataset, xAxisLabel, yAxisLabel);
				} catch (IllegalMiapeArgumentException e) {

				}
				if (showScoreVsFDR) {
					XYDataset dataset2 = DatasetFactory.createScoreVsNumProteinsDataSet(idSets,
							countNonConclusiveProteins);
					if (chart != null)
						chart.addNewAxis(dataset2, "num proteins");
					else
						chart = new XYLineChart(parent.getChartTitle(chartType),
								parent.getChartSubtitle(chartType, option), dataset2, xAxisLabel, yAxisLabel);
				}
				// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
				chartList.add(chart.getChartPanel());
			}
			return chartList;
		}
		return null;

	}

	private Object showPeptideCountingHistogramChart() {
		parent.setInformation1(parent.getCurrentChartType());
		int bins = optionsFactory.getHistogramBins();
		HistogramType histogramType = optionsFactory.getHistogramType();
		String xAxisLabel = "log (A/B)";
		Map<String, JCheckBox> idSetsCheckBoxes = optionsFactory.getIdSetsJCheckBoxes();
		ProteinGroupComparisonType proteinGroupComparisonType = optionsFactory.getProteinGroupComparisonType();
		boolean distinguish = parent.distinguishModifiedPeptides();
		List<IdentificationSet> idSets = getIdentificationSets(null, idSetsCheckBoxes, false);
		if (idSets.size() != 2)
			throw new IllegalMiapeArgumentException("Select two datasets");
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			HistogramDataset dataset = DatasetFactory.createPeptideCountingHistogramDataSet(idSets.get(0),
					idSets.get(1), proteinGroupComparisonType, histogramType, distinguish, countNonConclusiveProteins,
					bins);
			HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, true);
			chart.centerRangeAxisOnZero();
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			HistogramDataset dataset = DatasetFactory.createPeptideCountingHistogramDataSet(idSets.get(0),
					idSets.get(1), proteinGroupComparisonType, histogramType, distinguish, countNonConclusiveProteins,
					bins);

			HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, true);
			chart.centerRangeAxisOnZero();
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			HistogramDataset dataset = DatasetFactory.createPeptideCountingHistogramDataSet(idSets.get(0),
					idSets.get(1), proteinGroupComparisonType, histogramType, distinguish, countNonConclusiveProteins,
					bins);
			HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, true);
			chart.centerRangeAxisOnZero();
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				HistogramDataset dataset = DatasetFactory.createPeptideCountingHistogramDataSet(idSets.get(0),
						idSets.get(1), proteinGroupComparisonType, histogramType, distinguish,
						countNonConclusiveProteins, bins);
				HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, true);
				chart.centerRangeAxisOnZero();
				// chart.setXRangeValues(0, 100);
				chartList.add(chart.getChartPanel());
			}
			return chartList;
			// this.jPanelChart.addGraphicPanel(chartList);
		}
		return null;
	}

	private Object showPeptideCountingVsScoreScatterChart() {
		parent.setInformation1(parent.getCurrentChartType());
		int bins = optionsFactory.getHistogramBins();
		String selectedScoreName = optionsFactory.getPeptideScoreName();
		String xAxisLabel = "log (A/B)";
		String yAxisLabel = selectedScoreName;
		Map<String, JCheckBox> idSetsCheckBoxes = optionsFactory.getIdSetsJCheckBoxes();
		ProteinGroupComparisonType proteinGroupComparisonType = optionsFactory.getProteinGroupComparisonType();
		boolean distinguish = parent.distinguishModifiedPeptides();
		List<IdentificationSet> idSets = getIdentificationSets(null, idSetsCheckBoxes, false);
		if (idSets.size() != 2)
			throw new IllegalMiapeArgumentException("Select two datasets");
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			XYDataset dataset = DatasetFactory.createPeptideCountingVsScoreXYDataSet(idSets.get(0), idSets.get(1),
					proteinGroupComparisonType, distinguish, countNonConclusiveProteins, selectedScoreName);
			XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
			chart.centerRangeAxisOnZero();
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			XYDataset dataset = DatasetFactory.createPeptideCountingVsScoreXYDataSet(idSets.get(0), idSets.get(1),
					proteinGroupComparisonType, distinguish, countNonConclusiveProteins, selectedScoreName);

			XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
			chart.centerRangeAxisOnZero();
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			XYDataset dataset = DatasetFactory.createPeptideCountingVsScoreXYDataSet(idSets.get(0), idSets.get(1),
					proteinGroupComparisonType, distinguish, countNonConclusiveProteins, selectedScoreName);
			XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
			chart.centerRangeAxisOnZero();
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			List<JPanel> chartList = new ArrayList<JPanel>();
			for (Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				XYDataset dataset = DatasetFactory.createPeptideCountingVsScoreXYDataSet(idSets.get(0), idSets.get(1),
						proteinGroupComparisonType, distinguish, countNonConclusiveProteins, selectedScoreName);
				XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
				chart.centerRangeAxisOnZero();
				// chart.setXRangeValues(0, 100);
				chartList.add(chart.getChartPanel());
			}
			return chartList;
			// this.jPanelChart.addGraphicPanel(chartList);
		}
		return null;
	}

	public void notifyProgress(String message) {
		firePropertyChange(DATASET_PROGRESS, null, message);
	}

	/**
	 * @param experimentName
	 * @return
	 * 
	 */
	public VennData getVennData(String experimentName) {
		return this.vennChartMap.get(experimentName).getVennData();
	}

	/**
	 * @param experimentName
	 * @return
	 * 
	 */
	public VennChart getVennChart(String experimentName) {
		return this.vennChartMap.get(experimentName);
	}

	/**
	 * @return
	 */
	public String getChartType() {
		return this.chartType + " (" + this.option + ")";
	}
}
