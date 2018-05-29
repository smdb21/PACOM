package org.proteored.pacom.analysis.gui.tasks;

import java.awt.Color;
import java.awt.Font;
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
import org.jfree.chart.ChartPanel;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
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
import org.proteored.pacom.analysis.charts.MyXYItemLabelGenerator;
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
import org.proteored.pacom.analysis.gui.ChartType;

import edu.scripps.yates.utilities.util.Pair;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

public class ChartCreatorTask extends SwingWorker<Object, Void> {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");
	public static final String CHART_GENERATED = "Chart generated";
	public static final String CHART_GENERATION_STARTED = "Chart started";
	public static final String CHART_ERROR_GENERATED = "Chart error generated";
	public static final String DATASET_PROGRESS = "Data set progress";
	public static final String CHART_CANCELED = "Chart creation cancelled";
	private final ChartType chartType;
	private final String option;
	private final ChartManagerFrame parent;
	private final ExperimentList experimentList;
	private String error;
	private final AdditionalOptionsPanelFactory optionsFactory;
	private final boolean countNonConclusiveProteins;
	private final Map<String, VennChart> vennChartMap = new THashMap<String, VennChart>();

	public ChartCreatorTask(ChartManagerFrame parent, ChartType chartType, String optionParam,
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
		firePropertyChange(CHART_GENERATION_STARTED, null, null);
		parent.setToolTipInformation3("");
		error = null;
		try {
			Object ret = null;
			if (ChartType.PEPTIDE_SCORE_DISTRIBUTION.equals(chartType)) {
				ret = showScoreDistributionHistogramLineChart(IdentificationItemEnum.PEPTIDE);
			} else if (ChartType.PROTEIN_SCORE_DISTRIBUTION.equals(chartType)) {
				ret = showScoreDistributionHistogramLineChart(IdentificationItemEnum.PROTEIN);
			} else if (ChartType.PEPTIDE_SCORE_COMPARISON.equals(chartType)) {
				ret = showScoreComparisonScatterChart(IdentificationItemEnum.PEPTIDE);
			} else if (ChartType.PROTEIN_SCORE_COMPARISON.equals(chartType)) {
				ret = showScoreComparisonScatterChart(IdentificationItemEnum.PROTEIN);
			} else if (ChartType.PEPTIDE_NUMBER_HISTOGRAM.equals(chartType)) {
				ret = showHistogramChart(IdentificationItemEnum.PEPTIDE);
			} else if (ChartType.PROTEIN_NUMBER_HISTOGRAM.equals(chartType)) {
				ret = showHistogramChart(IdentificationItemEnum.PROTEIN);
			} else if (ChartType.PROTEIN_REPEATABILITY.equals(chartType)) {
				ret = showRepeatabilityHistogramStackedChart(IdentificationItemEnum.PROTEIN);
			} else if (ChartType.PEPTIDE_REPEATABILITY.equals(chartType)) {
				ret = showRepeatabilityHistogramStackedChart(IdentificationItemEnum.PEPTIDE);
			} else if (ChartType.PEPTIDE_OVERLAPING.equals(chartType)) {
				ret = showOverlappingChart(IdentificationItemEnum.PEPTIDE);
			} else if (ChartType.PROTEIN_OVERLAPING.equals(chartType)) {
				ret = showOverlappingChart(IdentificationItemEnum.PROTEIN);
			} else if (ChartType.PEPTIDE_OCCURRENCE_HEATMAP.equals(chartType)) {
				ret = showPeptideOccurrenceHeatMapChart(false);
			} else if (ChartType.PSMS_PER_PEPTIDE_HEATMAP.equals(chartType)) {
				ret = showPeptideOccurrenceHeatMapChart(true);
			} else if (ChartType.PROTEIN_OCURRENCE_HEATMAP.equals(chartType)) {
				ret = showProteinOccurrenceHeatMapChart();
			} else if (ChartType.PEPTIDES_PER_PROTEIN_HEATMAP.equals(chartType)) {
				ret = showPeptidesPerProteinHeatMapChart(false);
			} else if (ChartType.PSMS_PER_PROTEIN_HEATMAP.equals(chartType)) {
				ret = showPeptidesPerProteinHeatMapChart(true);
			} else if (ChartType.MODIFICATED_PEPTIDE_NUMBER.equals(chartType)) {
				ret = showModificatedPeptidesBarChart();
			} else if (ChartType.MODIFICATION_SITES_NUMBER.equals(chartType)) {
				ret = showModificationsSitesBarChart();
			} else if (ChartType.PEPTIDE_MODIFICATION_DISTRIBUTION.equals(chartType)) {
				ret = showModificationsNumberDistributionBarChart();
			} else if (ChartType.PEPTIDE_MONITORING.equals(chartType)) {
				ret = showPeptideMonitoringBarChart();
			} else if (ChartType.PROTEIN_COVERAGE_DISTRIBUTION.equals(chartType)) {
				ret = showProteinCoverageDistributionChart();
			} else if (ChartType.PROTEIN_COVERAGE.equals(chartType)) {
				ret = showProteinCoverageHistogramChart();
			} else if (ChartType.FDR.equals(chartType)) {
				ret = showFDRChart();
			} else if (ChartType.PROTEIN_SENSITIVITY_SPECIFICITY.equals(chartType)) {
				ret = showProteinSensitivitySpecificityChart();
			} else if (ChartType.MISSEDCLEAVAGE_DISTRIBUTION.equals(chartType)) {
				ret = showMissedCleavagesNumberDistributionBarChart();
			} else if (ChartType.PROTEIN_GROUP_TYPES.equals(chartType)) {
				ret = showProteinGroupTypesDistributionBarChart();
			} else if (ChartType.PEPTIDE_MASS_DISTRIBUTION.equals(chartType)) {
				ret = showPeptideMassDistributionChart();
			} else if (ChartType.PEPTIDE_LENGTH_DISTRIBUTION.equals(chartType)) {
				ret = showPeptideLengthDistributionChart();
			} else if (ChartType.PEPTIDE_CHARGE_HISTOGRAM.equals(chartType)) {
				ret = showPeptideChargeBarChart();
				// } else if (ChartType.CHR16_MAPPING.equals(chartType))
				// {
				// ret = showChr16MappingBarChart();
			} else if (ChartType.SINGLE_HIT_PROTEINS.equals(chartType)) {
				ret = showSingleHitProteinsChart();
			} else if (ChartType.PEPTIDE_NUMBER_IN_PROTEINS.equals(chartType)) {
				ret = showNumberPeptidesInProteinsChart();
			} else if (ChartType.DELTA_MZ_OVER_MZ.equals(chartType)) {
				ret = showDeltaMzScatterChart();
			} else if (ChartType.PSM_PEP_PROT.equals(chartType)) {
				ret = shotPSMPEPPROT_LineChart();
			} else if (ChartType.FDR_VS_SCORE.equals(chartType)) {
				ret = showFDRPlots();
			} else if (ChartType.CHR_MAPPING.equals(chartType)) {
				ret = showAllChrMappingBarChart();
			} else if (ChartType.CHR_PEPTIDES_MAPPING.equals(chartType)) {
				ret = showAllChrPeptideMappingBarChart();
			} else if (ChartType.HUMAN_CHROMOSOME_COVERAGE.equals(chartType)) {
				ret = showAllChrCoverageSpiderChart();
			} else if (ChartType.EXCLUSIVE_PROTEIN_NUMBER.equals(chartType)) {
				ret = showExclusiveIdentificationNumberChart(IdentificationItemEnum.PROTEIN);
			} else if (ChartType.EXCLUSIVE_PEPTIDE_NUMBER.equals(chartType)) {
				ret = showExclusiveIdentificationNumberChart(IdentificationItemEnum.PEPTIDE);
			} else if (ChartType.PEPTIDE_PRESENCY_HEATMAP.equals(chartType)) {
				ret = showPeptidePresencyHeatMapChart();
			} else if (ChartType.PEPTIDE_NUM_PER_PROTEIN_MASS.equals(chartType)) {
				ret = showPeptidePerProteinMassDistributionChart();
			} else if (ChartType.PROTEIN_NAME_CLOUD.equals(chartType)) {
				ret = showWordCramChart();
			} else if (ChartType.PEPTIDE_RT.equals(chartType)) {
				ret = showRetentionTimeDistributionChart();
			} else if (ChartType.PEPTIDE_RT_COMPARISON.equals(chartType)) {
				ret = showRetentionTimeComparisonChart();
			} else if (ChartType.SINGLE_RT_COMPARISON.equals(chartType)) {
				ret = showSingleRetentiontimeChart();
			} else if (ChartType.SPECTROMETERS.equals(chartType)) {
				ret = showSpectrometersChart();
			} else if (ChartType.INPUT_PARAMETERS.equals(chartType)) {
				ret = showInputParametersChart();
			} else if (ChartType.PEPTIDE_COUNTING_HISTOGRAM.equals(chartType)) {
				ret = showPeptideCountingHistogramChart();
			} else if (ChartType.PEPTIDE_COUNTING_VS_SCORE.equals(chartType)) {
				ret = showPeptideCountingVsScoreScatterChart();
			}
			if (ret != null) {
				if (ret instanceof ChartPanel) {
					log.info("Chart creator task, creating a panel with dimension: " + ((ChartPanel) ret).getSize());
				}
				return ret;
			}
		} catch (final IllegalMiapeArgumentException e) {
			e.printStackTrace();
			String message = e.getMessage();
			error = message;
			log.warn(message);
			final JPanel jpanel = new JPanel();
			jpanel.setBackground(Color.white);
			if (message.contains("\n")) {
				message = "<html>" + message.replace("\n", "<br>") + "</html>";
			}
			final JLabel comp = new JLabel(message);
			comp.setToolTipText(message);
			final Font font = comp.getFont();
			final Font newFont = font.deriveFont(14f);
			comp.setFont(newFont);
			jpanel.add(comp);
			jpanel.setToolTipText(message);
			return jpanel;
		}
		if (error == null) {
			error = "Error generating chart.";
		}
		final JPanel jpanel = new JPanel();
		jpanel.setBackground(Color.white);
		final JLabel comp = new JLabel(error);
		comp.setToolTipText(error);
		final Font font = comp.getFont();
		final Font newFont = font.deriveFont(14f);
		comp.setFont(newFont);
		jpanel.add(comp);
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
		final boolean showInMinutes = optionsFactory.showInMinutes();
		String yAxisLabel = "RT (min)";
		if (!showInMinutes)
			yAxisLabel = "RT (sg)";

		final PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();

		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			xAxisLabel = "level 2";

			final CategoryDataset dataset = DatasetFactory.createSinglePeptideRTMonitoringCategoryDataSet(idSets,
					sequences, showInMinutes);
			final BarChart chart = new BarChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
			chart.setNonIntegerItemLabels(StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING, "#.##");
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			xAxisLabel = "experiment list";

			final CategoryDataset dataset = DatasetFactory.createSinglePeptideRTMonitoringCategoryDataSet(idSets,
					sequences, showInMinutes);
			final BarChart chart = new BarChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
			chart.setNonIntegerItemLabels(StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING, "#.##");

			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			xAxisLabel = "experiment";

			final CategoryDataset dataset = DatasetFactory.createSinglePeptideRTMonitoringCategoryDataSet(idSets,
					sequences, showInMinutes);
			final BarChart chart = new BarChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
			chart.setNonIntegerItemLabels(StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING, "#.##");

			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				xAxisLabel = experiment.getName();

				final CategoryDataset dataset = DatasetFactory.createSinglePeptideRTMonitoringCategoryDataSet(idSets,
						sequences, showInMinutes);
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				chart.setNonIntegerItemLabels(StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING, "#.##");

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
			final List<List<String>> tableData = DatasetFactory.createInputParametersTableData(idSets);
			final JPanel panel = new TableData(tableData);
			return panel;
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final List<List<String>> tableData = DatasetFactory.createInputParametersTableData(idSets);
			final JPanel panel = new TableData(tableData);
			return panel;
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				try {
					idSets = getIdentificationSets(experiment.getFullName(), null, false);

					if (!idSets.isEmpty()) {
						final List<List<String>> tableData = DatasetFactory.createInputParametersTableData(idSets);
						final JPanel panel = new TableData(tableData);
						chartList.add(panel);
					}
				} catch (final IllegalMiapeArgumentException e) {
					final JPanel jpanel = new JPanel();
					jpanel.add(new JLabel("<html>Error generating table for " + experiment.getName() + "<br>"
							+ e.getMessage() + "</html>"));
					chartList.add(jpanel);
				}

			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			final List<List<String>> tableData = DatasetFactory.createInputParametersTableData(idSets);
			final JPanel panel = new TableData(tableData);
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
			final List<List<String>> tableData = DatasetFactory.createSpectrometersTableData(idSets);
			final JPanel panel = new TableData(tableData);
			return panel;
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final List<List<String>> tableData = DatasetFactory.createSpectrometersTableData(idSets);
			final JPanel panel = new TableData(tableData);
			return panel;
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				try {
					idSets = getIdentificationSets(experiment.getFullName(), null, false);

					if (!idSets.isEmpty()) {
						final List<List<String>> tableData = DatasetFactory.createSpectrometersTableData(idSets);
						final JPanel panel = new TableData(tableData);
						chartList.add(panel);
					}
				} catch (final IllegalMiapeArgumentException e) {
					final JPanel jpanel = new JPanel();
					jpanel.add(new JLabel("<html>Error generating table for " + experiment.getName() + "<br>"
							+ e.getMessage() + "</html>"));
					chartList.add(jpanel);
				}

			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			final List<List<String>> tableData = DatasetFactory.createSpectrometersTableData(idSets);
			final JPanel panel = new TableData(tableData);
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

		final boolean showRegressionLine = optionsFactory.showRegressionLine();
		final boolean showDiagonalLine = optionsFactory.showDiagonalLine();
		final boolean inMinutes = optionsFactory.showInMinutes();
		if (inMinutes) {
			xAxisLabel = "RT (min)";
			yAxisLabel = "RT (min)";
		}
		List<IdentificationSet> idSets = getIdentificationSets(null, scoreComparisonJCheckBoxes, false);

		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			final Pair<XYDataset, MyXYItemLabelGenerator> dataset = DatasetFactory.createRTXYDataSet(idSets, inMinutes);
			final XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
			if (showRegressionLine)
				chart.addRegressionLine(true);
			if (showDiagonalLine)
				chart.addDiagonalLine();
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			final Pair<XYDataset, MyXYItemLabelGenerator> dataset = DatasetFactory.createRTXYDataSet(idSets, inMinutes);
			final XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
			if (showRegressionLine)
				chart.addRegressionLine(true);
			if (showDiagonalLine)
				chart.addDiagonalLine();
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				try {
					idSets = getIdentificationSets(experiment.getFullName(), scoreComparisonJCheckBoxes, false);

					if (!idSets.isEmpty()) {
						final Pair<XYDataset, MyXYItemLabelGenerator> dataset = DatasetFactory.createRTXYDataSet(idSets,
								inMinutes);
						final XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
								parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
						if (showRegressionLine)
							chart.addRegressionLine(true);
						if (showDiagonalLine)
							chart.addDiagonalLine();
						chartList.add(chart.getChartPanel());
					}
				} catch (final IllegalMiapeArgumentException e) {
					final JPanel jpanel = new JPanel();
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
		final int bins = optionsFactory.getHistogramBins();
		final HistogramType histogramType = optionsFactory.getHistogramType();
		String xAxisLabel;
		xAxisLabel = "RT (sg)";
		final String frequencyType = "PSMs";
		final boolean showParent = optionsFactory.isTotalSerieShown();
		final boolean inMinutes = optionsFactory.showInMinutes();
		final Map<String, JCheckBox> idSetsJCheckBoxes = optionsFactory.getIdSetsJCheckBoxes();
		if (inMinutes)
			xAxisLabel = "RT (min)";
		List<IdentificationSet> idSets = getIdentificationSets(null, idSetsJCheckBoxes, showParent);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			final HistogramDataset dataset = DatasetFactory.createPeptideRTHistogramDataSet(idSets, bins, histogramType,
					inMinutes);

			final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, frequencyType);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			final HistogramDataset dataset = DatasetFactory.createPeptideRTHistogramDataSet(idSets, bins, histogramType,
					inMinutes);
			final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, frequencyType);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final HistogramDataset dataset = DatasetFactory.createPeptideRTHistogramDataSet(idSets, bins, histogramType,
					inMinutes);
			final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, frequencyType);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), idSetsJCheckBoxes, showParent);
				final HistogramDataset dataset = DatasetFactory.createPeptideRTHistogramDataSet(idSets, bins,
						histogramType, inMinutes);
				final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType), experiment.getName(),
						dataset, xAxisLabel, frequencyType);
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
		final List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		final int maxNumWords = optionsFactory.getMaxNumberWords();
		final JLabel label = optionsFactory.getJLabelSelectedWord();
		final int minWordLength = optionsFactory.getMinWordLength();
		// final JLabel label2 = optionsFactory.getJLabelSelectedProteins();

		final List<String> skipWords = optionsFactory.getSkipWords();
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST)) {
			final WordCramChart chart = new WordCramChart(idSets.get(0), skipWords, minWordLength, parent);
			chart.selectedWordLabel(label).selectedProteinsLabel(parent.getTextAreaStatus())
					.font(optionsFactory.getFont()).maximumNumberOfWords(maxNumWords);
			return chart;
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<Panel> chartList = new ArrayList<Panel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				final WordCramChart chart = new WordCramChart(experiment, skipWords, minWordLength, parent);
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
		final int bins = optionsFactory.getHistogramBins();
		final HistogramType histogramType = optionsFactory.getHistogramType();
		final boolean totalSerieShown = optionsFactory.isTotalSerieShown();
		String xAxisLabel;
		final String frequencyType = "peptides";
		xAxisLabel = "log2 (nº pept per protein / protein MW (Da))";
		final boolean retrieveProteinSeqs = true;
		final int selectedOption = JOptionPane.showConfirmDialog(parent,
				"<html>In order to build this chart, the program will retrieve the protein sequence from the Internet,<br>which can take several minutes, depending on the number of proteins.<br>Are you sure you want to continue?</html>",
				"Warning", JOptionPane.YES_NO_OPTION);

		if (selectedOption == JOptionPane.NO_OPTION)
			throw new IllegalMiapeArgumentException(
					"In order to build this chart, the program will retrieve the protein sequence from the Internet");
		parent.setProteinSequencesRetrieved(true);

		List<IdentificationSet> idSets = getIdentificationSets(null, null, totalSerieShown);

		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			final HistogramDataset dataset = DatasetFactory.createNumPeptidesPerProteinMassDistribution(idSets, bins,
					histogramType, retrieveProteinSeqs, countNonConclusiveProteins);
			final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, frequencyType);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			final HistogramDataset dataset = DatasetFactory.createNumPeptidesPerProteinMassDistribution(idSets, bins,
					histogramType, retrieveProteinSeqs, countNonConclusiveProteins);

			final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, frequencyType);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final HistogramDataset dataset = DatasetFactory.createNumPeptidesPerProteinMassDistribution(idSets, bins,
					histogramType, retrieveProteinSeqs, countNonConclusiveProteins);
			final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, frequencyType);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				final HistogramDataset dataset = DatasetFactory.createNumPeptidesPerProteinMassDistribution(idSets,
						bins, histogramType, retrieveProteinSeqs, countNonConclusiveProteins);
				final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType), experiment.getName(),
						dataset, xAxisLabel, frequencyType);
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
		final int bins = optionsFactory.getHistogramBins();
		final HistogramType histogramType = optionsFactory.getHistogramType();
		final boolean totalSerieShown = optionsFactory.isTotalSerieShown();
		final PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		final String xAxisLabel = "log2 (nº pept per protein / protein MW (Da))";
		final String yAxisLabel = "Avg number of peptides";
		final String frequencyType = "peptides";

		final boolean retrieveProteinSeqs = true;
		final int selectedOption = JOptionPane.showConfirmDialog(parent,
				"<html>In order to build this chart, the program will retrieve the protein sequence from the Internet,<br>which can take several minutes, depending on the number of proteins.<br>Are you sure you want to continue?</html>",
				"Warning", JOptionPane.YES_NO_OPTION);

		if (selectedOption == JOptionPane.NO_OPTION)
			throw new IllegalMiapeArgumentException(
					"In order to build this chart, the program will retrieve the protein sequence from the Internet");
		parent.setProteinSequencesRetrieved(true);
		List<IdentificationSet> idSets = getIdentificationSets(null, null, totalSerieShown);

		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			final CategoryDataset dataset = DatasetFactory.createNumPeptidesPerProteinMassHistogram(idSets, bins,
					histogramType, retrieveProteinSeqs, countNonConclusiveProteins);
			final BarChart chart = new BarChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);

			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			final HistogramDataset dataset = DatasetFactory.createNumPeptidesPerProteinMassDistribution(idSets, bins,
					histogramType, retrieveProteinSeqs, countNonConclusiveProteins);

			final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, frequencyType);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final HistogramDataset dataset = DatasetFactory.createNumPeptidesPerProteinMassDistribution(idSets, bins,
					histogramType, retrieveProteinSeqs, countNonConclusiveProteins);
			final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, frequencyType);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				final HistogramDataset dataset = DatasetFactory.createNumPeptidesPerProteinMassDistribution(idSets,
						bins, histogramType, retrieveProteinSeqs, countNonConclusiveProteins);
				final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType), experiment.getName(),
						dataset, xAxisLabel, frequencyType);
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
		final String xAxisLabel;
		final List<String> sequences = optionsFactory.getUserPeptideList();
		if (sequences == null)
			throw new IllegalMiapeArgumentException("Insert at least one peptide sequence to show the chart");
		final String yAxisLabel = "peptides sequences";

		final Boolean distinguishModPeptides = parent.distinguishModifiedPeptides();
		final PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();

		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		final double colorScale = optionsFactory.getColorScale();
		final boolean binary = optionsFactory.isHeatMapBinary();
		final Color highColor = optionsFactory.getHighColorScale();
		final Color lowColor = optionsFactory.getLowColorScale();
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			final List<String> columnList = getIdSetNames(idSets);

			final double[][] dataset = DatasetFactory.createPeptidePresencyHeapMapDataSet(idSets, sequences,
					parent.distinguishModifiedPeptides(), binary);

			final HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, sequences, columnList,
					colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);

			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			final List<String> columnList = getIdSetNames(idSets);

			final double[][] dataset = DatasetFactory.createPeptidePresencyHeapMapDataSet(idSets, sequences,
					parent.distinguishModifiedPeptides(), binary);

			final HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, sequences, columnList,
					colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);

			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final List<String> columnList = getIdSetNames(idSets);

			final double[][] dataset = DatasetFactory.createPeptidePresencyHeapMapDataSet(idSets, sequences,
					parent.distinguishModifiedPeptides(), binary);

			final HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, sequences, columnList,
					colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);

			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, false);
				final List<String> columnList = getIdSetNames(idSets);
				final double[][] dataset = DatasetFactory.createPeptidePresencyHeapMapDataSet(idSets, sequences,
						parent.distinguishModifiedPeptides(), binary);

				final HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, sequences,
						columnList, colorScale);
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
		final List<String> ret = new ArrayList<String>();
		for (final IdentificationSet idSet : idSets) {
			ret.add(idSet.getName());
		}
		return ret;
	}

	private Object showExclusiveIdentificationNumberChart(IdentificationItemEnum plotItem) {
		parent.setInformation1(parent.getCurrentChartType().getName() + " / " + plotItem);
		final String xAxisLabel = "";
		String yAxisLabel = "";
		String yAxisLabelAccumulative = "";
		if (IdentificationItemEnum.PROTEIN.equals(plotItem)) {
			yAxisLabel = "# proteins";
			yAxisLabelAccumulative = "Accumulative # proteins";
		} else if (IdentificationItemEnum.PEPTIDE.equals(plotItem)) {
			yAxisLabel = "# peptides";
			yAxisLabelAccumulative = "Accumulative # peptides";
		}
		final PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();

		final boolean showAccumulativeTrend = optionsFactory.isAccumulativeTrendSelected();
		final Map<String, JCheckBox> checkBoxControls = optionsFactory.getIdSetsJCheckBoxes();
		final ProteinGroupComparisonType proteinGroupComparisonType = optionsFactory.getProteinGroupComparisonType();
		final boolean distModPeptides = parent.distinguishModifiedPeptides();
		List<IdentificationSet> idSets = getIdentificationSets(null, checkBoxControls, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			List<DefaultCategoryDataset> datasets = null;
			if (IdentificationItemEnum.PEPTIDE == plotItem) {
				datasets = DatasetFactory.createExclusiveNumberIdentificationCategoryDataSetForPeptides(idSets,
						distModPeptides, showAccumulativeTrend);
			} else {
				datasets = DatasetFactory.createExclusiveNumberIdentificationCategoryDataSetForProteins(idSets,
						proteinGroupComparisonType, showAccumulativeTrend);
			}
			if (showAccumulativeTrend) {
				final CombinedChart chart = new CombinedChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, yAxisLabelAccumulative,
						datasets.get(0), datasets.get(1));
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
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
						proteinGroupComparisonType, showAccumulativeTrend);
			}
			if (showAccumulativeTrend) {
				final CombinedChart chart = new CombinedChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, yAxisLabelAccumulative,
						datasets.get(0), datasets.get(1));
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
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
						proteinGroupComparisonType, showAccumulativeTrend);
			}
			if (showAccumulativeTrend) {
				final CombinedChart chart = new CombinedChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, yAxisLabelAccumulative,
						datasets.get(0), datasets.get(1));
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, datasets.get(0),
						plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();

			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), checkBoxControls, false);
				List<DefaultCategoryDataset> datasets = null;
				if (IdentificationItemEnum.PEPTIDE == plotItem) {
					datasets = DatasetFactory.createExclusiveNumberIdentificationCategoryDataSetForPeptides(idSets,
							distModPeptides, showAccumulativeTrend);
				} else {
					datasets = DatasetFactory.createExclusiveNumberIdentificationCategoryDataSetForProteins(idSets,
							proteinGroupComparisonType, showAccumulativeTrend);
				}
				if (showAccumulativeTrend) {
					final CombinedChart chart = new CombinedChart(
							parent.getChartTitle(chartType) + ": " + experiment.getName(),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, yAxisLabelAccumulative,
							datasets.get(0), datasets.get(1));
					chartList.add(chart.getChartPanel());
				} else {
					final BarChart chart = new BarChart(parent.getChartTitle(chartType),
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
		final List<IdentificationSet> idSets = new ArrayList<IdentificationSet>();
		if (ChartManagerFrame.ONE_SERIES_PER_REPLICATE.equals(option)) {
			final List<Experiment> experiments = experimentList.getExperiments();
			for (final Experiment experiment : experiments) {
				for (final Replicate replicate : experiment.getNextLevelIdentificationSetList()) {

					// String repName = experiment.getName() + " / " +
					// replicate.getName();
					final String repName = replicate.getFullName();
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
			for (final Experiment experiment : experiments) {
				final String expName = experiment.getFullName();
				if (expName.equals(experimentName)) {
					for (final Replicate replicate : experiment.getNextLevelIdentificationSetList()) {

						final String repName = replicate.getFullName();
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
			for (final Experiment identificationSet : experiments) {
				final Experiment experiment = identificationSet;
				final String expName = experiment.getFullName();
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
		final String xAxisLabel = "Human chromosomes";

		final String proteinOrGene = optionsFactory.getProteinOrGene();
		String yAxisLabel;
		if (AdditionalOptionsPanelFactory.PROTEIN.equals(proteinOrGene))
			yAxisLabel = "# proteins";
		else if (AdditionalOptionsPanelFactory.GENES.equals(proteinOrGene))
			yAxisLabel = "# genes";
		else
			yAxisLabel = "# proteins and # genes";

		final PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		final boolean takeGeneFromFirstProteinSelected = optionsFactory.isTakeGeneFromFirstProteinSelected();
		final boolean pieChart = optionsFactory.showAsPieChart();

		if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			if (!pieChart) {
				final CategoryDataset dataset = DatasetFactory.createAllHumanChromosomeMappingCategoryDataSet(
						experimentList, proteinOrGene, true, takeGeneFromFirstProteinSelected,
						countNonConclusiveProteins);
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);

				return chart.getChartPanel();
			} else {
				final PieDataset dataset = DatasetFactory.createAllHumanChromosomeMappingPieDataSet(experimentList,
						proteinOrGene, takeGeneFromFirstProteinSelected, countNonConclusiveProteins);
				final PieChart chart = new PieChart(parent.getChartTitle(chartType) + " - " + proteinOrGene,
						parent.getChartSubtitle(chartType, option), dataset);
				return chart.getChartPanel();
			}

		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {

				if (!pieChart) {

					final CategoryDataset dataset = DatasetFactory.createAllHumanChromosomeMappingCategoryDataSet(
							experiment, proteinOrGene, experimentList.getExperiments().size() == 1,
							takeGeneFromFirstProteinSelected, countNonConclusiveProteins);

					final BarChart chart = new BarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation);

					chartList.add(chart.getChartPanel());

				} else {
					final PieDataset dataset = DatasetFactory.createAllHumanChromosomeMappingPieDataSet(experiment,
							proteinOrGene, takeGeneFromFirstProteinSelected, countNonConclusiveProteins);
					final PieChart chart = new PieChart(parent.getChartTitle(chartType) + " - " + proteinOrGene,
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
		final String xAxisLabel = "Human chromosomes";

		final String peptideOrPSM = optionsFactory.getPeptideOrPSM();
		String yAxisLabel;
		if (AdditionalOptionsPanelFactory.PSM.equals(peptideOrPSM))
			yAxisLabel = "# PSM";
		else if (AdditionalOptionsPanelFactory.PEPTIDE.equals(peptideOrPSM))
			yAxisLabel = "# peptides";
		else
			yAxisLabel = "# peptides and # PSM";

		final PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		final boolean distinguisModPep = parent.distinguishModifiedPeptides();
		final boolean pieChart = optionsFactory.showAsPieChart();

		if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			if (!pieChart) {
				final CategoryDataset dataset = DatasetFactory.createAllHumanChromosomePeptideMappingCategoryDataSet(
						experimentList, peptideOrPSM, true, distinguisModPep);
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);

				return chart.getChartPanel();
			} else {
				final PieDataset dataset = DatasetFactory.createAllHumanChromosomePeptideMappingPieDataSet(
						experimentList, peptideOrPSM, distinguisModPep);
				final PieChart chart = new PieChart(parent.getChartTitle(chartType) + " - " + peptideOrPSM,
						parent.getChartSubtitle(chartType, option), dataset);
				return chart.getChartPanel();
			}

		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {

				if (!pieChart) {

					final CategoryDataset dataset = DatasetFactory
							.createAllHumanChromosomePeptideMappingCategoryDataSet(experiment, peptideOrPSM, true,
									distinguisModPep);

					final BarChart chart = new BarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation);

					chartList.add(chart.getChartPanel());

				} else {
					final PieDataset dataset = DatasetFactory.createAllHumanChromosomePeptideMappingPieDataSet(
							experiment, peptideOrPSM, distinguisModPep);
					final PieChart chart = new PieChart(parent.getChartTitle(chartType) + " - " + peptideOrPSM,
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

		final boolean showSpiderPlot = optionsFactory.isShowAsSpiderPlot();
		final boolean justPercentage = optionsFactory.getAsPercentage();
		final boolean showTotal = optionsFactory.isTotalSerieShown();
		if (justPercentage) {
			yAxisLabel += " (%)";
		}
		final boolean takeGeneFromFirstProteinSelected = optionsFactory.isTakeGeneFromFirstProteinSelected();
		final PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();

		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			xAxisLabel = "experiment list";

			final CategoryDataset dataset = DatasetFactory.createAllHumanChromosomeGeneCoverageCategoryDataSet(idSets,
					showSpiderPlot, justPercentage, showTotal, takeGeneFromFirstProteinSelected,
					countNonConclusiveProteins);

			if (showSpiderPlot) {
				final SpiderChart chart = new SpiderChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), dataset);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				if (justPercentage)
					chart.setNonIntegerItemLabels(StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING + "%",
							"#.##");
				return chart.getChartPanel();
			}

		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			xAxisLabel = "experiment";

			final CategoryDataset dataset = DatasetFactory.createAllHumanChromosomeGeneCoverageCategoryDataSet(idSets,
					showSpiderPlot, justPercentage, showTotal, takeGeneFromFirstProteinSelected,
					countNonConclusiveProteins);
			if (showSpiderPlot) {
				final SpiderChart chart = new SpiderChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), dataset);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				if (justPercentage)
					chart.setNonIntegerItemLabels(StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING + "%",
							"#.##");
				return chart.getChartPanel();
			}

		} else if (ChartManagerFrame.ONE_SERIES_PER_REPLICATE.equals(option)) {

			xAxisLabel = "level 2";

			final CategoryDataset dataset = DatasetFactory.createAllHumanChromosomeGeneCoverageCategoryDataSet(idSets,
					showSpiderPlot, justPercentage, showTotal, takeGeneFromFirstProteinSelected,
					countNonConclusiveProteins);
			if (showSpiderPlot) {
				final SpiderChart chart = new SpiderChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), dataset);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				if (justPercentage)
					chart.setNonIntegerItemLabels(StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING + "%",
							"#.##");
				return chart.getChartPanel();
			}

		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getFullName(), null, false);
				xAxisLabel = experiment.getName();
				final CategoryDataset dataset = DatasetFactory.createAllHumanChromosomeGeneCoverageCategoryDataSet(
						idSets, showSpiderPlot, justPercentage, showTotal, takeGeneFromFirstProteinSelected,
						countNonConclusiveProteins);

				if (showSpiderPlot) {
					final SpiderChart chart = new SpiderChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), dataset);
					chartList.add(chart.getChartPanel());
				} else {
					final BarChart chart = new BarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation);
					if (justPercentage)
						chart.setNonIntegerItemLabels(
								StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING + "%", "#.##");
					chartList.add(chart.getChartPanel());
				}

			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	private Object shotPSMPEPPROT_LineChart() {
		parent.setInformation1(parent.getCurrentChartType());

		String xAxisLabel = "";
		final boolean showPSMs = optionsFactory.showPSMs();
		final boolean showPeptides = optionsFactory.showPeptides();
		final boolean showPeptidesPlusCharge = optionsFactory.showPeptidesPlusCharge();
		final boolean showProteins = optionsFactory.showProteins();
		final boolean distinguishModificatedPeptides = parent.distinguishModifiedPeptides();
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
			final CategoryDataset dataset = DatasetFactory.createPSM_PEP_PROT_DataSet(idSets, showPSMs, showPeptides,
					showPeptidesPlusCharge, showProteins, distinguishModificatedPeptides, countNonConclusiveProteins);
			final LineCategoryChart chart = new LineCategoryChart(yAxisLabel,
					parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
					PlotOrientation.VERTICAL);

			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			final CategoryDataset dataset = DatasetFactory.createPSM_PEP_PROT_DataSet(idSets, showPSMs, showPeptides,
					showPeptidesPlusCharge, showProteins, distinguishModificatedPeptides, countNonConclusiveProteins);
			final LineCategoryChart chart = new LineCategoryChart(yAxisLabel,
					parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
					PlotOrientation.VERTICAL);
			xAxisLabel = "Experiment";
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final CategoryDataset dataset = DatasetFactory.createPSM_PEP_PROT_DataSet(idSets, showPSMs, showPeptides,
					showPeptidesPlusCharge, showProteins, distinguishModificatedPeptides, countNonConclusiveProteins);
			final LineCategoryChart chart = new LineCategoryChart(yAxisLabel,
					parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
					PlotOrientation.VERTICAL);
			xAxisLabel = "Experiment";
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			xAxisLabel = "level 2";
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, optionsFactory.isTotalSerieShown());

				final CategoryDataset dataset = DatasetFactory.createPSM_PEP_PROT_DataSet(idSets, showPSMs,
						showPeptides, showPeptidesPlusCharge, showProteins, distinguishModificatedPeptides,
						countNonConclusiveProteins);
				final LineCategoryChart chart = new LineCategoryChart(yAxisLabel,
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
						PlotOrientation.VERTICAL);
				chartList.add(chart.getChartPanel());
			}

			// this.jPanelChart.addGraphicPanel(chartList);
			return chartList;
		}
		return null;
	}

	private Object showDeltaMzScatterChart() {
		parent.setInformation1(parent.getCurrentChartType());

		final String xAxisLabel = "m/z";
		final String yAxisLabel = "Delta(m/z)";
		final boolean showRegressionLine = optionsFactory.showRegressionLine();
		final Map<String, JCheckBox> experimentJCheckBoxes = optionsFactory.getIdSetsJCheckBoxes();
		List<IdentificationSet> idSets = getIdentificationSets(null, experimentJCheckBoxes, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			final Pair<XYDataset, MyXYItemLabelGenerator> dataset = DatasetFactory.createDeltaMzOverMzXYDataSet(idSets);
			final XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
			chart.setTinnySeriesShape();
			chart.setAutomaticScales();
			if (showRegressionLine)
				chart.addRegressionLine(true);
			chart.addHorizontalLine(0);
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			final Pair<XYDataset, MyXYItemLabelGenerator> dataset = DatasetFactory.createDeltaMzOverMzXYDataSet(idSets);
			final XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
			chart.setTinnySeriesShape();
			chart.setAutomaticScales();
			if (showRegressionLine)
				chart.addRegressionLine(true);
			chart.addHorizontalLine(0);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final Pair<XYDataset, MyXYItemLabelGenerator> dataset = DatasetFactory.createDeltaMzOverMzXYDataSet(idSets);
			final XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
			chart.setTinnySeriesShape();
			chart.setAutomaticScales();
			if (showRegressionLine)
				chart.addRegressionLine(true);
			chart.addHorizontalLine(0);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				try {
					idSets = getIdentificationSets(experiment.getName(), experimentJCheckBoxes, false);
					final Pair<XYDataset, MyXYItemLabelGenerator> dataset = DatasetFactory
							.createDeltaMzOverMzXYDataSet(idSets);
					final XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
					chart.setTinnySeriesShape();
					chart.setAutomaticScales();
					if (showRegressionLine)
						chart.addRegressionLine(true);
					chart.addHorizontalLine(0);
					chartList.add(chart.getChartPanel());
				} catch (final IllegalMiapeArgumentException e) {
					final JPanel jpanel = new JPanel();
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
				final Object object = get();
				if (object != null && error == null)
					firePropertyChange(CHART_GENERATED, null, object);
				if (error != null)
					firePropertyChange(CHART_ERROR_GENERATED, null, object);
				log.info("chart passed to the dialog");
			} else {
				log.info("Cancelled by user");
				firePropertyChange(CHART_CANCELED, null, null);
			}
			return;
		} catch (final InterruptedException e) {
			log.warn(e.getMessage());
			e.printStackTrace();
		} catch (final ExecutionException e) {
			log.warn(e.getMessage());
			e.printStackTrace();
		}
		firePropertyChange(CHART_ERROR_GENERATED, null, null);
		super.done();
	}

	private Object showProteinSensitivitySpecificityChart() {
		parent.setInformation1(parent.getCurrentChartType());
		String xAxisLabel;
		final String yAxisLabel = "Percentage";

		final PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		final Set<String> proteinsInSample = optionsFactory.getProteinsInSample();
		if (proteinsInSample == null || proteinsInSample.isEmpty())
			throw new IllegalMiapeArgumentException(
					"In order to show this chart, it is necessary to define the proteins in sample that will be the positives hits");

		// parent.setInformation3("sensitivity=tp/(tp+fn),
		// specificity=tn/(tn+fp), precision=tp/total");
		// parent.setToolTipInformation3("here the explanation of the values");

		xAxisLabel = "level 2";
		final boolean sensitivity = optionsFactory.isSensitivity();

		final boolean accuracy = optionsFactory.isAccuracy();
		final boolean specificity = optionsFactory.isSpecificity();
		final boolean precision = optionsFactory.isPrecision();
		final boolean npv = optionsFactory.isNPV();
		final boolean fdr = optionsFactory.isFDR();
		// if (!parent.isFDRThresholdEnabled())
		// throw new IllegalMiapeArgumentException(
		// "<html>In order to show this chart, it is necessary to define an FDR
		// threshold<br>that provides which proteins have been considered as
		// positives after the filter</html>");
		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			xAxisLabel = "experiment";
			final CategoryDataset dataset = DatasetFactory.createProteinSensitivityCategoryDataSet(idSets,
					proteinsInSample, countNonConclusiveProteins, sensitivity, accuracy, specificity, precision, npv,
					fdr);
			final BarChart chart = new BarChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
			chart.setNonIntegerItemLabels(StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING, "#.##");
			final CategoryPlot plot = (CategoryPlot) chart.getChart().getPlot();
			// plot.getRangeAxis().setUpperBound(1.0);
			plot.getRangeAxis().setUpperMargin(0.1);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			xAxisLabel = "experiment list";
			final CategoryDataset dataset = DatasetFactory.createProteinSensitivityCategoryDataSet(idSets,
					proteinsInSample, countNonConclusiveProteins, sensitivity, accuracy, specificity, precision, npv,
					fdr);
			final BarChart chart = new BarChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
			chart.setNonIntegerItemLabels(StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING, "#.##");
			final CategoryPlot plot = (CategoryPlot) chart.getChart().getPlot();
			// plot.getRangeAxis().setUpperBound(1.0);
			plot.getRangeAxis().setUpperMargin(0.1);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_REPLICATE.equals(option)) {

			xAxisLabel = "level 2";
			final CategoryDataset dataset = DatasetFactory.createProteinSensitivityCategoryDataSet(idSets,
					proteinsInSample, countNonConclusiveProteins, sensitivity, accuracy, specificity, precision, npv,
					fdr);
			final BarChart chart = new BarChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
			chart.setNonIntegerItemLabels(StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING, "#.##");
			final CategoryPlot plot = (CategoryPlot) chart.getChart().getPlot();
			// plot.getRangeAxis().setUpperBound(1.0);
			plot.getRangeAxis().setUpperMargin(0.1);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);

				final CategoryDataset dataset = DatasetFactory.createProteinSensitivityCategoryDataSet(idSets,
						proteinsInSample, countNonConclusiveProteins, sensitivity, accuracy, specificity, precision,
						npv, fdr);
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				chart.setNonIntegerItemLabels(StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING, "#.##");
				final CategoryPlot plot = (CategoryPlot) chart.getChart().getPlot();
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
		final int bins = optionsFactory.getHistogramBins();
		final boolean mOverZ = optionsFactory.getMOverZ();
		final boolean addZeroZeroValue = true;
		final HistogramType histogramType = optionsFactory.getHistogramType();
		String xAxisLabel;
		if (mOverZ)
			xAxisLabel = "m/z";
		else
			xAxisLabel = "Da";
		final String frequencyType = "PSMs";

		final boolean showParent = optionsFactory.isTotalSerieShown();
		List<IdentificationSet> idSets = getIdentificationSets(null, null, showParent);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			final HistogramDataset dataset = DatasetFactory.createPeptideMassHistogramDataSet(idSets, bins,
					histogramType, mOverZ);
			final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, frequencyType);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			final HistogramDataset dataset = DatasetFactory.createPeptideMassHistogramDataSet(idSets, bins,
					histogramType, mOverZ);
			final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, frequencyType);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final HistogramDataset dataset = DatasetFactory.createPeptideMassHistogramDataSet(idSets, bins,
					histogramType, mOverZ);
			final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, frequencyType);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				final HistogramDataset dataset = DatasetFactory.createPeptideMassHistogramDataSet(idSets, bins,
						histogramType, mOverZ);
				final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType), experiment.getName(),
						dataset, xAxisLabel, frequencyType);
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
		final String xAxisLabel = "Peptide length";
		final String yAxisLabel = "# PSMs";
		final PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		final boolean stacked = optionsFactory.showAsStackedChart();
		final boolean normalize = optionsFactory.getAsPercentage();
		final boolean showParent = optionsFactory.isTotalSerieShown();
		final int maximum = optionsFactory.getMaximumOccurrence();
		final int minimum = optionsFactory.getMinimumOccurrence();
		if (minimum > maximum)
			throw new IllegalMiapeArgumentException("The minimum length cannot be higher than the maximum");

		List<IdentificationSet> idSets = getIdentificationSets(null, null, showParent);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			final CategoryDataset dataset = DatasetFactory.createPeptideLengthHistogramDataSet(idSets, minimum,
					maximum);
			if (stacked) {
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						normalize);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			final CategoryDataset dataset = DatasetFactory.createPeptideLengthHistogramDataSet(idSets, minimum,
					maximum);
			if (stacked) {
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						normalize);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final CategoryDataset dataset = DatasetFactory.createPeptideLengthHistogramDataSet(idSets, minimum,
					maximum);
			if (stacked) {
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						normalize);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				final CategoryDataset dataset = DatasetFactory.createPeptideLengthHistogramDataSet(idSets, minimum,
						maximum);
				if (stacked) {
					final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation, normalize);
					chartList.add(chart.getChartPanel());
				} else {
					final BarChart chart = new BarChart(parent.getChartTitle(chartType),
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
		final String yAxisLabel = "# peptides";

		final Boolean distinguishModPeptides = parent.distinguishModifiedPeptides();
		final PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();

		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			xAxisLabel = "level 2";

			final CategoryDataset dataset = DatasetFactory.createPeptideMonitoringCategoryDataSet(idSets, sequences,
					distinguishModPeptides);
			final BarChart chart = new BarChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			xAxisLabel = "experiment list";

			final CategoryDataset dataset = DatasetFactory.createPeptideMonitoringCategoryDataSet(idSets, sequences,
					distinguishModPeptides);
			final BarChart chart = new BarChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			xAxisLabel = "experiment";

			final CategoryDataset dataset = DatasetFactory.createPeptideMonitoringCategoryDataSet(idSets, sequences,
					distinguishModPeptides);
			final BarChart chart = new BarChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				xAxisLabel = experiment.getName();

				final CategoryDataset dataset = DatasetFactory.createPeptideMonitoringCategoryDataSet(idSets, sequences,
						distinguishModPeptides);
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
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
		final String yAxisLabel = "# different peptides containing: " + modifications[0];

		final int maximum = optionsFactory.getMaximumOccurrence();
		final PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		final boolean showAsStackedChartPanel = optionsFactory.showAsStackedChart();
		final boolean asPercentage = optionsFactory.getAsPercentage();
		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			xAxisLabel = "level 2";
			final CategoryDataset dataset = DatasetFactory.createModificationDistributionCategoryDataSet(idSets,
					modifications, maximum);
			if (showAsStackedChartPanel) {
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}

		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			xAxisLabel = "experiment list";
			final CategoryDataset dataset = DatasetFactory.createModificationDistributionCategoryDataSet(idSets,
					modifications, maximum);
			if (showAsStackedChartPanel) {
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}

		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			xAxisLabel = "experiment";
			final CategoryDataset dataset = DatasetFactory.createModificationDistributionCategoryDataSet(idSets,
					modifications, maximum);
			if (showAsStackedChartPanel) {
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}

		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			xAxisLabel = "level 2";
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				final CategoryDataset dataset = DatasetFactory.createModificationDistributionCategoryDataSet(idSets,
						modifications, maximum);
				if (showAsStackedChartPanel) {
					final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation, asPercentage);
					chartList.add(chart.getChartPanel());
				} else {
					final BarChart chart = new BarChart(parent.getChartTitle(chartType),
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

		final String yAxisLabel = "# PSMs";
		xAxisLabel = "# missedcleavages sites";
		final int maximum = optionsFactory.getMaximumOccurrence();
		final String cleavageAminoacids = optionsFactory.getCleavageAminoacids();
		final PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		final boolean stackedChart = optionsFactory.showAsStackedChart();
		final boolean asPercentage = optionsFactory.getAsPercentage();
		List<IdentificationSet> idSets = getIdentificationSets(null, null, optionsFactory.isTotalSerieShown());
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			final CategoryDataset dataset = DatasetFactory.createMissedCleavagesDistributionCategoryDataSet(idSets,
					cleavageAminoacids, maximum);
			if (stackedChart) {
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			final CategoryDataset dataset = DatasetFactory.createMissedCleavagesDistributionCategoryDataSet(idSets,
					cleavageAminoacids, maximum);
			if (stackedChart) {
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final CategoryDataset dataset = DatasetFactory.createMissedCleavagesDistributionCategoryDataSet(idSets,
					cleavageAminoacids, maximum);
			if (stackedChart) {
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, optionsFactory.isTotalSerieShown());
				final CategoryDataset dataset = DatasetFactory.createMissedCleavagesDistributionCategoryDataSet(idSets,
						cleavageAminoacids, maximum);
				if (stackedChart) {
					final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation, asPercentage);
					chartList.add(chart.getChartPanel());
				} else {
					final BarChart chart = new BarChart(parent.getChartTitle(chartType),
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

		final String yAxisLabel = "# proteinGroups";
		xAxisLabel = "Protein Group Type";
		final PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		final boolean stackedChart = optionsFactory.showAsStackedChart();
		final boolean asPercentage = optionsFactory.getAsPercentage();
		List<IdentificationSet> idSets = getIdentificationSets(null, null, optionsFactory.isTotalSerieShown());
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			final CategoryDataset dataset = DatasetFactory.createProteinGroupTypesDistributionCategoryDataSet(idSets);
			if (stackedChart) {
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			final CategoryDataset dataset = DatasetFactory.createProteinGroupTypesDistributionCategoryDataSet(idSets);
			if (stackedChart) {
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final CategoryDataset dataset = DatasetFactory.createProteinGroupTypesDistributionCategoryDataSet(idSets);
			if (stackedChart) {
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, optionsFactory.isTotalSerieShown());
				final CategoryDataset dataset = DatasetFactory
						.createProteinGroupTypesDistributionCategoryDataSet(idSets);
				if (stackedChart) {
					final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation, asPercentage);
					chartList.add(chart.getChartPanel());
				} else {
					final BarChart chart = new BarChart(parent.getChartTitle(chartType),
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

		final String yAxisLabel = "# of modificated sites ";
		final String xAxisLabel = "PTM";
		final PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		final boolean stackedChart = optionsFactory.showAsStackedChart();
		final boolean asPercentage = optionsFactory.getAsPercentage();
		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			final CategoryDataset dataset = DatasetFactory.createNumberModificationSitesCategoryDataSet(idSets,
					modifications);
			if (stackedChart) {
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			final CategoryDataset dataset = DatasetFactory.createNumberModificationSitesCategoryDataSet(idSets,
					modifications);
			if (stackedChart) {
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final CategoryDataset dataset = DatasetFactory.createNumberModificationSitesCategoryDataSet(idSets,
					modifications);
			if (stackedChart) {
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, false);

				if (!stackedChart)
					idSets.add(experiment);
				final CategoryDataset dataset = DatasetFactory.createNumberModificationSitesCategoryDataSet(idSets,
						modifications);
				if (stackedChart) {
					final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation, asPercentage);
					chartList.add(chart.getChartPanel());
				} else {
					final BarChart chart = new BarChart(parent.getChartTitle(chartType),
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

		final String yAxisLabel = "# of different peptides containing each PTM";
		final String xAxisLabel = "PTM";
		final PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		final boolean stackedChart = optionsFactory.showAsStackedChart();
		final boolean asPercentage = optionsFactory.getAsPercentage();
		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			final CategoryDataset dataset = DatasetFactory.createNumberModificatedPeptidesCategoryDataSet(idSets,
					modifications);
			if (stackedChart) {
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				chart.setHorizontalXLabel();
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			final CategoryDataset dataset = DatasetFactory.createNumberModificatedPeptidesCategoryDataSet(idSets,
					modifications);
			if (stackedChart) {
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				chart.setHorizontalXLabel();
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final CategoryDataset dataset = DatasetFactory.createNumberModificatedPeptidesCategoryDataSet(idSets,
					modifications);
			if (stackedChart) {
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				chart.setHorizontalXLabel();
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, false);
				if (!stackedChart)
					idSets.add(experiment);
				final CategoryDataset dataset = DatasetFactory.createNumberModificatedPeptidesCategoryDataSet(idSets,
						modifications);
				if (stackedChart) {
					final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation, asPercentage);
					chartList.add(chart.getChartPanel());
				} else {
					final BarChart chart = new BarChart(parent.getChartTitle(chartType),
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
		final String yAxisLabel = "# PSMs";

		final PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		final boolean stackedChart = optionsFactory.showAsStackedChart();
		final boolean asPercentage = optionsFactory.getAsPercentage();
		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			xAxisLabel = "level 2";

			final CategoryDataset dataset = DatasetFactory.createPeptideChargeHistogramDataSet(idSets);
			if (stackedChart) {
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			xAxisLabel = "experiment list";

			final CategoryDataset dataset = DatasetFactory.createPeptideChargeHistogramDataSet(idSets);
			if (stackedChart) {
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			xAxisLabel = "experiment";

			final CategoryDataset dataset = DatasetFactory.createPeptideChargeHistogramDataSet(idSets);
			if (stackedChart) {
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				return chart.getChartPanel();
			} else {
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
				// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				xAxisLabel = experiment.getName();

				final CategoryDataset dataset = DatasetFactory.createPeptideChargeHistogramDataSet(idSets);
				if (stackedChart) {
					final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation, asPercentage);
					chartList.add(chart.getChartPanel());
				} else {
					final BarChart chart = new BarChart(parent.getChartTitle(chartType),
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
		parent.setInformation1(parent.getCurrentChartType().getName() + " / " + IdentificationItemEnum.PEPTIDE);

		final double colorScale = optionsFactory.getColorScale();
		final int minThreshold = optionsFactory.getHeatMapThreshold();
		final List<String> peptideSequenceOrder = parent.getPeptideSequencesFromPeptideSequenceFilter();

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
			final List<String> rowList = new ArrayList<String>();
			final List<String> columnList = new ArrayList<String>();
			double[][] dataset = null;
			if (isPSMs) {
				dataset = DatasetFactory.createPSMsPerPeptidesHeapMapDataSet(experimentList, idSets, rowList,
						columnList, peptideSequenceOrder, parent.distinguishModifiedPeptides(), minThreshold);
			} else {
				dataset = DatasetFactory.createPeptideOccurrenceHeapMapDataSet(experimentList, idSets, rowList,
						columnList, peptideSequenceOrder, parent.distinguishModifiedPeptides(), minThreshold);
			}
			parent.addMinMaxHeatMapValues(dataset);

			final HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, rowList, columnList,
					colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);
			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			final List<String> rowList = new ArrayList<String>();
			final List<String> columnList = new ArrayList<String>();

			final double[][] dataset = DatasetFactory.createPeptideOccurrenceHeapMapDataSet(experimentList, idSets,
					rowList, columnList, peptideSequenceOrder, parent.distinguishModifiedPeptides(), minThreshold);
			parent.addMinMaxHeatMapValues(dataset);

			final HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, rowList, columnList,
					colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);
			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final List<String> rowList = new ArrayList<String>();
			final List<String> columnList = new ArrayList<String>();

			final double[][] dataset = DatasetFactory.createPeptideOccurrenceHeapMapDataSet(experimentList, idSets,
					rowList, columnList, peptideSequenceOrder, parent.distinguishModifiedPeptides(), minThreshold);
			parent.addMinMaxHeatMapValues(dataset);

			final HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, rowList, columnList,
					colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);
			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				final List<String> rowList = new ArrayList<String>();
				final List<String> columnList = new ArrayList<String>();
				idSets = getIdentificationSets(experiment.getName(), null, false);
				final double[][] dataset = DatasetFactory.createPeptideOccurrenceHeapMapDataSet(experiment, idSets,
						rowList, columnList, peptideSequenceOrder, parent.distinguishModifiedPeptides(), minThreshold);
				final HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, rowList,
						columnList, colorScale);
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
		parent.setInformation1(parent.getCurrentChartType().getName() + " / " + IdentificationItemEnum.PROTEIN);

		final double colorScale = optionsFactory.getColorScale();
		final int minOccurrenceThreshold = optionsFactory.getHeatMapThreshold();
		final List<String> proteinACCOrder = parent.getProteinAccsFromACCFilter();

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
			final List<String> rowList = new ArrayList<String>();
			final List<String> columnList = new ArrayList<String>();

			final double[][] dataset = DatasetFactory.createProteinOccurrenceHeapMapDataSet(experimentList, idSets,
					rowList, columnList, proteinACCOrder, minOccurrenceThreshold, countNonConclusiveProteins);
			parent.addMinMaxHeatMapValues(dataset);

			final HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, rowList, columnList,
					colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);
			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			final List<String> rowList = new ArrayList<String>();
			final List<String> columnList = new ArrayList<String>();

			final double[][] dataset = DatasetFactory.createProteinOccurrenceHeapMapDataSet(experimentList, idSets,
					rowList, columnList, proteinACCOrder, minOccurrenceThreshold, countNonConclusiveProteins);
			parent.addMinMaxHeatMapValues(dataset);

			final HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, rowList, columnList,
					colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);
			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final List<String> rowList = new ArrayList<String>();
			final List<String> columnList = new ArrayList<String>();

			final double[][] dataset = DatasetFactory.createProteinOccurrenceHeapMapDataSet(experimentList, idSets,
					rowList, columnList, proteinACCOrder, minOccurrenceThreshold, countNonConclusiveProteins);
			parent.addMinMaxHeatMapValues(dataset);

			final HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, rowList, columnList,
					colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);
			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				final List<String> rowList = new ArrayList<String>();
				final List<String> columnList = new ArrayList<String>();
				idSets = getIdentificationSets(experiment.getName(), null, false);
				final double[][] dataset = DatasetFactory.createProteinOccurrenceHeapMapDataSet(experiment, idSets,
						rowList, columnList, proteinACCOrder, minOccurrenceThreshold, countNonConclusiveProteins);

				final HeatMapChart chart = new HeatMapChart(parent.getChartTitle(chartType), dataset, rowList,
						columnList, colorScale);
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

		final double colorScale = optionsFactory.getColorScale();
		final int minThreshold = optionsFactory.getHeatMapThreshold();
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
		final List<String> proteinACCOrder = parent.getProteinAccsFromACCFilter();
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
			final List<String> rowList = new ArrayList<String>();
			final List<String> columnList = new ArrayList<String>();

			final double[][] dataset = DatasetFactory.createPeptidesPerProteinHeapMapDataSet(experimentList, idSets,
					rowList, columnList, proteinACCOrder, parent.distinguishModifiedPeptides(), minThreshold,
					countNonConclusiveProteins, isPSMs);
			parent.addMinMaxHeatMapValues(dataset);

			final HeatMapChart chart = new HeatMapChart(title, dataset, rowList, columnList, colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);
			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			final List<String> rowList = new ArrayList<String>();
			final List<String> columnList = new ArrayList<String>();

			final double[][] dataset = DatasetFactory.createPeptidesPerProteinHeapMapDataSet(experimentList, idSets,
					rowList, columnList, proteinACCOrder, parent.distinguishModifiedPeptides(), minThreshold,
					countNonConclusiveProteins, isPSMs);
			parent.addMinMaxHeatMapValues(dataset);

			final HeatMapChart chart = new HeatMapChart(title, dataset, rowList, columnList, colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);
			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final List<String> rowList = new ArrayList<String>();
			final List<String> columnList = new ArrayList<String>();

			final double[][] dataset = DatasetFactory.createPeptidesPerProteinHeapMapDataSet(experimentList, idSets,
					rowList, columnList, proteinACCOrder, parent.distinguishModifiedPeptides(), minThreshold,
					countNonConclusiveProteins, isPSMs);
			parent.addMinMaxHeatMapValues(dataset);

			final HeatMapChart chart = new HeatMapChart(title, dataset, rowList, columnList, colorScale);
			chart.setHighValueColor(highColor);
			chart.setLowValueColor(lowColor);
			return chart.getjPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				final List<String> rowList = new ArrayList<String>();
				final List<String> columnList = new ArrayList<String>();
				idSets = getIdentificationSets(experiment.getName(), null, false);
				final double[][] dataset = DatasetFactory.createPeptidesPerProteinHeapMapDataSet(experiment, idSets,
						rowList, columnList, proteinACCOrder, parent.distinguishModifiedPeptides(), minThreshold,
						countNonConclusiveProteins, isPSMs);

				final HeatMapChart chart = new HeatMapChart(title, dataset, rowList, columnList, colorScale);
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
		vennChartMap.clear();
		parent.setInformation1(parent.getCurrentChartType().getName() + " / " + plotItem);
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
			for (final Experiment experiment : experiments) {
				for (final Object identificationSet : experiment.getNextLevelIdentificationSetList()) {
					final Replicate replicate = (Replicate) identificationSet;
					final String repName = replicate.getFullName();
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
			final VennChart chart = new VennChart(parent.getChartTitle(chartType), idSet1, label1, idSet2, label2,
					idSet3, label3, plotItem, parent.distinguishModifiedPeptides(), proteinSelection, color1, color2,
					color3);

			vennChartMap.put(null, chart);
			final String intersectionsText = chart.getIntersectionsText(null);
			optionsFactory.setIntersectionText(intersectionsText);
			return chart.getChartPanel();

		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final List<Experiment> experiments = experimentList.getExperiments();

			for (final Experiment experiment : experiments) {
				final String expName = experiment.getFullName();
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
			final VennChart chart = new VennChart(parent.getChartTitle(chartType), idSet1, label1, idSet2, label2,
					idSet3, label3, plotItem, parent.distinguishModifiedPeptides(), proteinSelection, color1, color2,
					color3);
			vennChartMap.put(null, chart);
			optionsFactory.setIntersectionText(chart.getIntersectionsText(null));
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			if (idSet1 == null || idSet2 == null) {
				throw new IllegalMiapeArgumentException(
						"Please, select a different comparison level to be able to select at least 2 datasets");
			}
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			String intersectionText = "";
			for (final Experiment experiment : experimentList.getExperiments()) {
				final int numReplicates = 1;
				idSet1 = null;
				label1 = null;
				idSet2 = null;
				label2 = null;
				idSet3 = null;
				label3 = null;
				for (final Replicate replicate : experiment.getNextLevelIdentificationSetList()) {

					final String repName = replicate.getFullName();
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
				if (idSet1 == null || idSet2 == null) {
					final JLabel label = new JLabel("Please, select at least 2 datasets to show the Venn diagram for '"
							+ experiment.getName() + "'");
					final JPanel panel = new JPanel();
					panel.add(label);
					chartList.add(panel);
					continue;
				}
				final VennChart chart = new VennChart(
						parent.getChartTitle(chartType) + " (" + experiment.getName() + ")", idSet1, label1, idSet2,
						label2, idSet3, label3, plotItem, parent.distinguishModifiedPeptides(), proteinSelection,
						color1, color2, color3);
				vennChartMap.put(experiment.getName(), chart);

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
		parent.setInformation1(parent.getCurrentChartType().getName() + " / " + plotItem);
		String xAxisLabel;
		String yAxisLabel = "";

		final String scoreName = null;
		final PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();

		final boolean stackedChart = optionsFactory.showAsStackedChart();
		final boolean asPercentage = optionsFactory.getAsPercentage();
		final boolean pieChart = optionsFactory.showAsPieChart();
		final boolean average = optionsFactory.showAverageOverReplicates();
		final boolean occurrenceFilterEnabled = parent.isOccurrenceFilterEnabled();
		final boolean isTotalSerieShown = optionsFactory.isTotalSerieShown();
		final boolean differentIdentificationsShown = !optionsFactory.isDifferentIdentificationsShown();
		final boolean totalVersusDifferentSelected = optionsFactory.isTotalVersusDifferentSelected();
		String chartTitle = parent.getChartTitle(chartType);
		if (totalVersusDifferentSelected) {
			if (plotItem.equals(IdentificationItemEnum.PEPTIDE)) {
				yAxisLabel = "# peptides / # PSMs";
				chartTitle = "Ratio number of peptides / number of PSMs";
			} else {
				yAxisLabel = "# diff proteins / # total proteins";
				chartTitle = "Ratio number of different proteins / number of proteins";
			}
		} else if (differentIdentificationsShown) {
			if (plotItem.equals(IdentificationItemEnum.PEPTIDE)) {
				yAxisLabel = "# different peptides";
				chartTitle = "Number of peptides";
			} else {
				chartTitle = "Number of proteins";
				yAxisLabel = "# different proteins";
			}
		} else {
			if (plotItem.equals(IdentificationItemEnum.PEPTIDE)) {
				yAxisLabel = "# PSMs";
				chartTitle = "Number of PSMs";
			} else {
				yAxisLabel = "# total proteins";
				chartTitle = "Number of proteins";
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
				final PieChart chart = new PieChart(chartTitle, parent.getChartSubtitle(chartType, option), dataset);
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
				final StackedBarChart chart = new StackedBarChart(chartTitle,
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
				final BarChart chart = new BarChart(chartTitle, parent.getChartSubtitle(chartType, option), xAxisLabel,
						yAxisLabel, dataset, plotOrientation);
				if (totalVersusDifferentSelected) {
					chart.setNonIntegerItemLabels(StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING,
							"#.##");
				}
				// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			// System.out.println("sd");
			if (average) {
				if (plotItem.equals(IdentificationItemEnum.PEPTIDE)) {
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
				final PieChart chart = new PieChart(chartTitle, parent.getChartSubtitle(chartType, option), dataset);
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
				final StackedBarChart chart = new StackedBarChart(chartTitle,
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
				final BarChart chart = new BarChart(chartTitle, parent.getChartSubtitle(chartType, option), xAxisLabel,
						yAxisLabel, dataset, plotOrientation);
				if (totalVersusDifferentSelected) {
					chart.setNonIntegerItemLabels(StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING,
							"#.##");
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
				if (plotItem.equals(IdentificationItemEnum.PEPTIDE)) {
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
				final PieChart chart = new PieChart(chartTitle, parent.getChartSubtitle(chartType, option), dataset);
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
				final StackedBarChart chart = new StackedBarChart(chartTitle,
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
				final BarChart chart = new BarChart(chartTitle, parent.getChartSubtitle(chartType, option), xAxisLabel,
						yAxisLabel, dataset, plotOrientation);
				if (totalVersusDifferentSelected) {
					chart.setNonIntegerItemLabels(StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING,
							"#.##");
				}
				// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
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
					final PieChart chart = new PieChart(chartTitle, parent.getChartSubtitle(chartType, option),
							dataset);

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
					final StackedBarChart chart = new StackedBarChart(chartTitle,
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
					final BarChart chart = new BarChart(chartTitle, parent.getChartSubtitle(chartType, option),
							xAxisLabel, yAxisLabel, dataset, plotOrientation);
					if (totalVersusDifferentSelected) {
						chart.setNonIntegerItemLabels(StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING,
								"#.##");
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

		final String scoreName = null;
		final PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();

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
				final PieChart chart = new PieChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), dataset);
				return chart.getChartPanel();
			} else if (stackedChart) {
				CategoryDataset dataset = null;
				dataset = DatasetFactory.createNumberSingleHitProteinsCategoryDataSet(idSets,
						differentIdentificationsShown, countNonConclusiveProteins);
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				chart.setIntegerItemLabels();
				return chart.getChartPanel();
			} else {
				CategoryDataset dataset = null;

				dataset = DatasetFactory.createNumberSingleHitProteinsCategoryDataSet(idSets,
						differentIdentificationsShown, countNonConclusiveProteins);
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
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
				final PieChart chart = new PieChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), dataset);
				return chart.getChartPanel();
			} else if (stackedChart) {
				CategoryDataset dataset = null;

				dataset = DatasetFactory.createNumberSingleHitProteinsCategoryDataSet(idSets,
						differentIdentificationsShown, countNonConclusiveProteins);
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				chart.setIntegerItemLabels();
				return chart.getChartPanel();
			} else {
				CategoryDataset dataset = null;
				// if (isOccurrenceByReplicatesEnabled)

				dataset = DatasetFactory.createNumberSingleHitProteinsCategoryDataSet(idSets,
						differentIdentificationsShown, countNonConclusiveProteins);
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
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
				final PieChart chart = new PieChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), dataset);
				return chart.getChartPanel();
			} else if (stackedChart) {
				CategoryDataset dataset = null;

				dataset = DatasetFactory.createNumberSingleHitProteinsCategoryDataSet(idSets,
						differentIdentificationsShown, countNonConclusiveProteins);
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				chart.setIntegerItemLabels();
				return chart.getChartPanel();
			} else {
				CategoryDataset dataset = null;
				// if (isOccurrenceByReplicatesEnabled)

				dataset = DatasetFactory.createNumberSingleHitProteinsCategoryDataSet(idSets,
						differentIdentificationsShown, countNonConclusiveProteins);
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);

				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, false);
				if (!pieChart && !stackedChart && idSets.size() > 1 && !occurrenceFilterEnabled && isTotalSerieShown)
					idSets.add(experiment);
				xAxisLabel = experiment.getName();
				if (pieChart) {
					final PieDataset dataset = DatasetFactory.createNumberSingleHitProteinsPieDataSet(idSets,
							differentIdentificationsShown, countNonConclusiveProteins);
					final PieChart chart = new PieChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), dataset);

					chartList.add(chart.getChartPanel());
				} else if (stackedChart) {
					CategoryDataset dataset = null;
					dataset = DatasetFactory.createNumberSingleHitProteinsCategoryDataSet(idSets,
							differentIdentificationsShown, countNonConclusiveProteins);
					final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation, asPercentage);

					chart.setIntegerItemLabels();

					chartList.add(chart.getChartPanel());
				} else {
					CategoryDataset dataset = null;

					dataset = DatasetFactory.createNumberSingleHitProteinsCategoryDataSet(idSets,
							differentIdentificationsShown, countNonConclusiveProteins);

					final BarChart chart = new BarChart(parent.getChartTitle(chartType),
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

		final String scoreName = null;
		final PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();

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
		String chartTitle = parent.getChartTitle(chartType);
		if (showPSMsorPeptides) {
			chartTitle = chartTitle.replace("peptides", "PSMs");
		}
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			if (!stackedChart && idSets.size() > 1 && !occurrenceFilterEnabled && isTotalSerieShown)
				idSets.add(experimentList);
			// System.out.println("ha");
			xAxisLabel = "level 2";

			if (stackedChart) {
				CategoryDataset dataset = null;
				dataset = DatasetFactory.createPeptideNumberInProteinsCategoryDataSet(idSets, maximum,
						showPSMsorPeptides, countNonConclusiveProteins);
				final StackedBarChart chart = new StackedBarChart(chartTitle,
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				chart.setIntegerItemLabels();
				return chart.getChartPanel();
			} else {
				CategoryDataset dataset = null;

				dataset = DatasetFactory.createPeptideNumberInProteinsCategoryDataSet(idSets, maximum,
						showPSMsorPeptides, countNonConclusiveProteins);
				final BarChart chart = new BarChart(chartTitle, parent.getChartSubtitle(chartType, option), xAxisLabel,
						yAxisLabel, dataset, plotOrientation);
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
				final StackedBarChart chart = new StackedBarChart(chartTitle,
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				chart.setIntegerItemLabels();
				return chart.getChartPanel();
			} else {
				CategoryDataset dataset = null;
				// if (isOccurrenceByReplicatesEnabled)

				dataset = DatasetFactory.createPeptideNumberInProteinsCategoryDataSet(idSets, maximum,
						showPSMsorPeptides, countNonConclusiveProteins);
				final BarChart chart = new BarChart(chartTitle, parent.getChartSubtitle(chartType, option), xAxisLabel,
						yAxisLabel, dataset, plotOrientation);

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
				final StackedBarChart chart = new StackedBarChart(chartTitle,
						parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
						asPercentage);
				chart.setIntegerItemLabels();
				return chart.getChartPanel();
			} else {
				CategoryDataset dataset = null;
				// if (isOccurrenceByReplicatesEnabled)

				dataset = DatasetFactory.createPeptideNumberInProteinsCategoryDataSet(idSets, maximum,
						showPSMsorPeptides, countNonConclusiveProteins);
				final BarChart chart = new BarChart(chartTitle, parent.getChartSubtitle(chartType, option), xAxisLabel,
						yAxisLabel, dataset, plotOrientation);

				return chart.getChartPanel();
			}
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, false);
				if (!stackedChart && idSets.size() > 1 && !occurrenceFilterEnabled && isTotalSerieShown)
					idSets.add(experiment);
				xAxisLabel = experiment.getName();
				if (stackedChart) {
					CategoryDataset dataset = null;
					dataset = DatasetFactory.createPeptideNumberInProteinsCategoryDataSet(idSets, maximum,
							showPSMsorPeptides, countNonConclusiveProteins);
					final StackedBarChart chart = new StackedBarChart(chartTitle,
							parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset,
							plotOrientation, asPercentage);

					chart.setIntegerItemLabels();

					chartList.add(chart.getChartPanel());
				} else {
					CategoryDataset dataset = null;

					dataset = DatasetFactory.createPeptideNumberInProteinsCategoryDataSet(idSets, maximum,
							showPSMsorPeptides, countNonConclusiveProteins);

					final BarChart chart = new BarChart(chartTitle, parent.getChartSubtitle(chartType, option),
							xAxisLabel, yAxisLabel, dataset, plotOrientation);

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
		final String yAxisLabel = "Average Protein Coverage";

		final PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		final boolean retrieveProteinSeqs = true;
		final int selectedOption = JOptionPane.showConfirmDialog(parent,
				"<html>In order to calculate protein coverage, the program will retrieve the protein sequence from the Internet,<br>which can take several minutes, depending on the number of proteins.<br>Are you sure you want to continue?</html>",
				"Warning", JOptionPane.YES_NO_OPTION);

		if (selectedOption == JOptionPane.NO_OPTION) {
			throw new IllegalMiapeArgumentException(
					"In order to calculate protein coverage, the program will retrieve the protein sequence from the Internet");
		}
		parent.setProteinSequencesRetrieved(true);

		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			xAxisLabel = "level 2";

			final CategoryDataset dataset = DatasetFactory.createAverageProteinCoverageStatisticalCategoryDataSet(
					idSets, retrieveProteinSeqs, countNonConclusiveProteins);
			final BarChart chart = new BarChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			xAxisLabel = "experiment";

			final CategoryDataset dataset = DatasetFactory.createAverageProteinCoverageStatisticalCategoryDataSet(
					idSets, retrieveProteinSeqs, countNonConclusiveProteins);
			final BarChart chart = new BarChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			xAxisLabel = "experiment list";

			final CategoryDataset dataset = DatasetFactory.createAverageProteinCoverageStatisticalCategoryDataSet(
					idSets, retrieveProteinSeqs, countNonConclusiveProteins);
			final BarChart chart = new BarChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				xAxisLabel = experiment.getName();
				final CategoryDataset dataset = DatasetFactory.createAverageProteinCoverageStatisticalCategoryDataSet(
						idSets, retrieveProteinSeqs, countNonConclusiveProteins);
				final BarChart chart = new BarChart(parent.getChartTitle(chartType),
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
		final int bins = optionsFactory.getHistogramBins();
		final boolean addZeroZeroValue = true;
		final HistogramType histogramType = optionsFactory.getHistogramType();

		final String xAxisLabel = "Protein coverage (%)";
		final String frequencyType = "proteins";

		final boolean retrieveProteinSeqs = true;
		final int selectedOption = JOptionPane.showConfirmDialog(parent,
				"<html>In order to calculate protein coverage, the program will retrieve the protein sequence from the Internet,<br>which can take several minutes, depending on the number of proteins.<br>Are you sure you want to continue?</html>",
				"Warning", JOptionPane.YES_NO_OPTION);

		if (selectedOption == JOptionPane.NO_OPTION)
			throw new IllegalMiapeArgumentException(
					"In order to calculate protein coverage, the program will retrieve the protein sequence from the Internet");
		parent.setProteinSequencesRetrieved(true);

		List<IdentificationSet> idSets = getIdentificationSets(null, null, false);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			final HistogramDataset dataset = DatasetFactory.createProteinCoverageHistogramDataSet(idSets, bins,
					histogramType, retrieveProteinSeqs, countNonConclusiveProteins);
			final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, frequencyType);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			final HistogramDataset dataset = DatasetFactory.createProteinCoverageHistogramDataSet(idSets, bins,
					histogramType, retrieveProteinSeqs, countNonConclusiveProteins);
			final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, frequencyType);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			final HistogramDataset dataset = DatasetFactory.createProteinCoverageHistogramDataSet(idSets, bins,
					histogramType, retrieveProteinSeqs, countNonConclusiveProteins);
			final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, frequencyType);
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getFullName(), null, true);
				final HistogramDataset dataset = DatasetFactory.createProteinCoverageHistogramDataSet(idSets, bins,
						histogramType, retrieveProteinSeqs, countNonConclusiveProteins);
				final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType), experiment.getName(),
						dataset, xAxisLabel, frequencyType);
				// chart.setXRangeValues(0, 100);
				chartList.add(chart.getChartPanel());
			}
			return chartList;
			// this.jPanelChart.addGraphicPanel(chartList);
		}
		return null;
	}

	private Object showRepeatabilityHistogramStackedChart(IdentificationItemEnum plotItem) {
		parent.setInformation1(parent.getCurrentChartType().getName() + " / " + plotItem);
		String xAxisLabel;
		String yAxisLabel = "";
		final boolean asPercentage = optionsFactory.getAsPercentage();
		final PlotOrientation plotOrientation = optionsFactory.getPlotOrientation();
		if (plotItem.equals(IdentificationItemEnum.PEPTIDE))
			yAxisLabel = "# peptides";
		else
			yAxisLabel = "# proteins";
		final int maximum = optionsFactory.getMaximumOccurrence();
		final boolean overReplicates = optionsFactory.getOverReplicates();
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
			final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
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
			final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
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
			final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), xAxisLabel, yAxisLabel, dataset, plotOrientation,
					asPercentage);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getFullName(), null, true);
				xAxisLabel = "experiment";

				CategoryDataset dataset = null;
				if (!overReplicates)
					dataset = DatasetFactory.createRepeatabilityCategoryDataSet(idSets, plotItem,
							parent.distinguishModifiedPeptides(), maximum, countNonConclusiveProteins);
				else
					dataset = DatasetFactory.createRepeatabilityOverReplicatesCategoryDataSet(idSets, plotItem,
							parent.distinguishModifiedPeptides(), maximum, countNonConclusiveProteins);
				final StackedBarChart chart = new StackedBarChart(parent.getChartTitle(chartType),
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
		parent.setInformation1(parent.getCurrentChartType().getName() + " / " + plotItem);
		String scoreName = null;
		final boolean distinguish = parent.distinguishModifiedPeptides();
		if (plotItem.equals(IdentificationItemEnum.PEPTIDE)) {
			scoreName = optionsFactory.getPeptideScoreName();

		} else {
			scoreName = optionsFactory.getProteinScoreName();
		}
		final Map<String, JCheckBox> scoreComparisonJCheckBoxes = optionsFactory.getIdSetsJCheckBoxes();

		String xAxisLabel = scoreName;
		String yAxisLabel = scoreName;
		final boolean applyLog = optionsFactory.isApplyLog();
		if (applyLog) {
			xAxisLabel = "log10(" + xAxisLabel + ")";
			yAxisLabel = "log10(" + yAxisLabel + ")";
		}
		final boolean showRegressionLine = optionsFactory.showRegressionLine();
		final boolean showDiagonalLine = optionsFactory.showDiagonalLine();

		final boolean separateDecoyHits = optionsFactory.isSeparatedDecoyHits();
		List<IdentificationSet> idSets = getIdentificationSets(null, scoreComparisonJCheckBoxes, false);
		if (idSets.size() == 2) {
			xAxisLabel = xAxisLabel + " (" + idSets.get(0).getFullName() + ")";
			yAxisLabel = yAxisLabel + " (" + idSets.get(1).getFullName() + ")";
		}
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			final Pair<XYDataset, MyXYItemLabelGenerator> dataset = DatasetFactory.createScoreXYDataSet(idSets,
					scoreName, plotItem, distinguish, applyLog, separateDecoyHits, countNonConclusiveProteins);
			final XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
			if (showRegressionLine)
				chart.addRegressionLine(true);
			if (showDiagonalLine)
				chart.addDiagonalLine();
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			final Pair<XYDataset, MyXYItemLabelGenerator> dataset = DatasetFactory.createScoreXYDataSet(idSets,
					scoreName, plotItem, distinguish, applyLog, separateDecoyHits, countNonConclusiveProteins);
			final XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
			if (showRegressionLine)
				chart.addRegressionLine(true);
			if (showDiagonalLine)
				chart.addDiagonalLine();
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				try {
					idSets = getIdentificationSets(experiment.getFullName(), scoreComparisonJCheckBoxes, false);

					if (!idSets.isEmpty()) {
						final Pair<XYDataset, MyXYItemLabelGenerator> dataset = DatasetFactory.createScoreXYDataSet(
								idSets, scoreName, plotItem, distinguish, applyLog, separateDecoyHits,
								countNonConclusiveProteins);
						final XYPointChart chart = new XYPointChart(
								parent.getChartTitle(chartType) + ": " + experiment.getName(),
								parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
						if (showRegressionLine)
							chart.addRegressionLine(true);
						if (showDiagonalLine)
							chart.addDiagonalLine();
						chartList.add(chart.getChartPanel());
					}
				} catch (final IllegalMiapeArgumentException e) {
					final JPanel jpanel = new JPanel();
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
		parent.setInformation1(parent.getCurrentChartType().getName() + " / " + plotItem);
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

		final int bins = optionsFactory.getHistogramBins();
		final boolean addTotal = optionsFactory.isTotalSerieShown();
		final boolean addZeroZeroValue = false;
		final HistogramType histogramType = optionsFactory.getHistogramType();
		final boolean applyLog = optionsFactory.isApplyLog();
		final boolean separateDecoyHits = optionsFactory.isSeparatedDecoyHits();
		String xAxisLabel = scoreName;
		if (applyLog) {
			xAxisLabel = "log10(" + xAxisLabel + ")";
		}
		String frequencyType = "";
		switch (plotItem) {
		case PEPTIDE:
			frequencyType = "peptides";
			break;
		case PROTEIN:
			frequencyType = "protein";
			break;
		case PSM:
			frequencyType = "PSMs";
			break;
		default:
			break;
		}

		List<IdentificationSet> idSets = getIdentificationSets(null, null, addTotal);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			final HistogramDataset dataset = DatasetFactory.createScoreHistogramDataSet(idSets, scoreName, plotItem,
					bins, addZeroZeroValue, histogramType, applyLog, separateDecoyHits, countNonConclusiveProteins);
			final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, frequencyType);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			final HistogramDataset dataset = DatasetFactory.createScoreHistogramDataSet(idSets, scoreName, plotItem,
					bins, addZeroZeroValue, histogramType, applyLog, separateDecoyHits, countNonConclusiveProteins);
			final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, frequencyType);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			final HistogramDataset dataset = DatasetFactory.createScoreHistogramDataSet(idSets, scoreName, plotItem,
					bins, addZeroZeroValue, histogramType, applyLog, separateDecoyHits, countNonConclusiveProteins);
			final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, frequencyType);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {

				idSets = getIdentificationSets(experiment.getName(), null, addTotal);
				final HistogramDataset dataset = DatasetFactory.createScoreHistogramDataSet(idSets, scoreName, plotItem,
						bins, addZeroZeroValue, histogramType, applyLog, separateDecoyHits, countNonConclusiveProteins);
				final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType), experiment.getName(),
						dataset, xAxisLabel, frequencyType);
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
		final boolean showProteinLevel = optionsFactory.showProteinFDRLevel();
		final boolean showPeptideLevel = optionsFactory.showPeptideFDRLevel();
		final boolean showPSMLevel = optionsFactory.showPSMFDRLevel();

		if (showProteinLevel && !showPeptideLevel && !showPSMLevel) {
			yAxisLabel = "# proteins";
		} else if (!showProteinLevel && showPeptideLevel && !showPSMLevel) {
			yAxisLabel = "# peptides";
		} else if (!showProteinLevel && !showPeptideLevel && showPSMLevel) {
			yAxisLabel = "# PSMs";
		} else {
			yAxisLabel = "# (Proteins/Peptides/PSMs)";
		}
		final String xAxisLabel = "FDR (%)";
		final boolean isTotalSerieShown = optionsFactory.isTotalSerieShown();
		List<IdentificationSet> idSets = getIdentificationSets(null, null, isTotalSerieShown);
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			final XYDataset dataset = DatasetFactory.createFDRDataSet(idSets, showProteinLevel, showPeptideLevel,
					showPSMLevel, countNonConclusiveProteins);
			final XYLineChart chart = new XYLineChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {

			final XYDataset dataset = DatasetFactory.createFDRDataSet(idSets, showProteinLevel, showPeptideLevel,
					showPSMLevel, countNonConclusiveProteins);
			final XYLineChart chart = new XYLineChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);

			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {

			final XYDataset dataset = DatasetFactory.createFDRDataSet(idSets, showProteinLevel, showPeptideLevel,
					showPSMLevel, countNonConclusiveProteins);
			final XYLineChart chart = new XYLineChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel);

			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, isTotalSerieShown);
				if (isTotalSerieShown)
					idSets.add(experiment);
				final XYDataset dataset = DatasetFactory.createFDRDataSet(idSets, showProteinLevel, showPeptideLevel,
						showPSMLevel, countNonConclusiveProteins);
				final XYLineChart chart = new XYLineChart(parent.getChartTitle(chartType),
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

		final boolean showPSMs = optionsFactory.showPSMs();
		final boolean showPeptides = optionsFactory.showPeptides();
		final boolean showProteins = optionsFactory.showProteins();
		final boolean showScoreVsFDR = optionsFactory.showScoreVsFDR();
		if (!showPSMs && !showPeptides && !showProteins && !showScoreVsFDR)
			throw new IllegalMiapeArgumentException(
					"Select PSM, Peptides, Proteins or Score vs FDR to generate the chart");
		if (!showPSMs && !showPeptides && !showProteins && showScoreVsFDR)
			yAxisLabel = "# proteins";

		List<IdentificationSet> idSets = getIdentificationSets(null, optionsFactory.getIdSetsJCheckBoxes(), false);
		final java.util.Set<String> scoreNames = new THashSet<String>();
		for (final IdentificationSet identificationSet : idSets) {
			final FDRFilter fdrFilter = identificationSet.getFDRFilter();
			if (fdrFilter != null)
				if (!scoreNames.contains(fdrFilter.getSortingParameters().getScoreName()))
					scoreNames.add(fdrFilter.getSortingParameters().getScoreName());
		}
		String scoreNamesString = "";
		for (final String string : scoreNames) {
			if (!"".equals(scoreNamesString))
				scoreNamesString = scoreNamesString + ", ";
			scoreNamesString = scoreNamesString + string;
		}
		final String xAxisLabel = "Peptide score (" + scoreNamesString + ")";

		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			XYLineChart chart = null;
			try {
				final XYDataset dataset = DatasetFactory.createFDRvsScoreDataSet(idSets, showPSMs, showPeptides,
						showProteins, countNonConclusiveProteins);

				chart = new XYLineChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
						dataset, xAxisLabel, yAxisLabel);
			} catch (final IllegalMiapeArgumentException e) {

			}
			if (showScoreVsFDR) {
				XYDataset dataset2 = null;
				try {
					dataset2 = DatasetFactory.createScoreVsNumProteinsDataSet(idSets, countNonConclusiveProteins);
				} catch (final IllegalMiapeArgumentException e) {

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
				final XYDataset dataset = DatasetFactory.createFDRvsScoreDataSet(idSets, showPSMs, showPeptides,
						showProteins, countNonConclusiveProteins);

				chart = new XYLineChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
						dataset, xAxisLabel, yAxisLabel);
			} catch (final IllegalMiapeArgumentException e) {

			}
			if (showScoreVsFDR) {
				XYDataset dataset2 = null;
				try {
					dataset2 = DatasetFactory.createScoreVsNumProteinsDataSet(idSets, countNonConclusiveProteins);
				} catch (final IllegalMiapeArgumentException e) {

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

				final XYDataset dataset = DatasetFactory.createFDRvsScoreDataSet(idSets, showPSMs, showPeptides,
						showProteins, countNonConclusiveProteins);

				chart = new XYLineChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
						dataset, xAxisLabel, yAxisLabel);

			} catch (final IllegalMiapeArgumentException e) {

			}
			if (showScoreVsFDR) {
				XYDataset dataset2 = null;
				try {
					dataset2 = DatasetFactory.createScoreVsNumProteinsDataSet(idSets, countNonConclusiveProteins);
				} catch (final IllegalMiapeArgumentException e) {

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
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), optionsFactory.getIdSetsJCheckBoxes(), false);
				XYLineChart chart = null;

				// if (fdrFilter != null)
				// xAxisLabel = xAxisLabel + ": "
				// + fdrFilter.getSortingParameters().getScoreName();
				try {
					final XYDataset dataset = DatasetFactory.createFDRvsScoreDataSet(idSets, showPSMs, showPeptides,
							showProteins, countNonConclusiveProteins);

					chart = new XYLineChart(parent.getChartTitle(chartType), parent.getChartSubtitle(chartType, option),
							dataset, xAxisLabel, yAxisLabel);
				} catch (final IllegalMiapeArgumentException e) {

				}
				if (showScoreVsFDR) {
					final XYDataset dataset2 = DatasetFactory.createScoreVsNumProteinsDataSet(idSets,
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
		final int bins = optionsFactory.getHistogramBins();
		final HistogramType histogramType = optionsFactory.getHistogramType();
		final String xAxisLabel = "protein SPC ratio (log10)";
		final String frequencyType = "ratios";
		final Map<String, JCheckBox> idSetsCheckBoxes = optionsFactory.getIdSetsJCheckBoxes();
		final ProteinGroupComparisonType proteinGroupComparisonType = optionsFactory.getProteinGroupComparisonType();
		final boolean distinguish = parent.distinguishModifiedPeptides();
		List<IdentificationSet> idSets = getIdentificationSets(null, idSetsCheckBoxes, false);
		if (idSets.size() != 2)
			throw new IllegalMiapeArgumentException("Select two datasets");
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			final HistogramDataset dataset = DatasetFactory.createPeptideCountingHistogramDataSet(idSets.get(0),
					idSets.get(1), proteinGroupComparisonType, histogramType, distinguish, countNonConclusiveProteins,
					bins);
			final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, true, frequencyType);
			chart.centerRangeAxisOnZero();
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			final HistogramDataset dataset = DatasetFactory.createPeptideCountingHistogramDataSet(idSets.get(0),
					idSets.get(1), proteinGroupComparisonType, histogramType, distinguish, countNonConclusiveProteins,
					bins);

			final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, true, frequencyType);
			chart.centerRangeAxisOnZero();
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final HistogramDataset dataset = DatasetFactory.createPeptideCountingHistogramDataSet(idSets.get(0),
					idSets.get(1), proteinGroupComparisonType, histogramType, distinguish, countNonConclusiveProteins,
					bins);
			final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, true, frequencyType);
			chart.centerRangeAxisOnZero();
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				final HistogramDataset dataset = DatasetFactory.createPeptideCountingHistogramDataSet(idSets.get(0),
						idSets.get(1), proteinGroupComparisonType, histogramType, distinguish,
						countNonConclusiveProteins, bins);
				final HistogramChart chart = new HistogramChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, true, frequencyType);
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
		final int bins = optionsFactory.getHistogramBins();
		final String selectedScoreName = optionsFactory.getPeptideScoreName();
		final String xAxisLabel = "protein SPC ratio (log10)";
		final String yAxisLabel = selectedScoreName;
		final Map<String, JCheckBox> idSetsCheckBoxes = optionsFactory.getIdSetsJCheckBoxes();
		final ProteinGroupComparisonType proteinGroupComparisonType = optionsFactory.getProteinGroupComparisonType();
		final boolean distinguish = parent.distinguishModifiedPeptides();
		List<IdentificationSet> idSets = getIdentificationSets(null, idSetsCheckBoxes, false);
		if (idSets.size() != 2)
			throw new IllegalMiapeArgumentException("Select two datasets");
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {

			final Pair<XYDataset, MyXYItemLabelGenerator> dataset = DatasetFactory
					.createPeptideCountingVsScoreXYDataSet(idSets.get(0), idSets.get(1), proteinGroupComparisonType,
							distinguish, selectedScoreName);
			final XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel, false);
			chart.centerRangeAxisOnZero();
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			final Pair<XYDataset, MyXYItemLabelGenerator> dataset = DatasetFactory
					.createPeptideCountingVsScoreXYDataSet(idSets.get(0), idSets.get(1), proteinGroupComparisonType,
							distinguish, selectedScoreName);

			final XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel, false);
			chart.centerRangeAxisOnZero();
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final Pair<XYDataset, MyXYItemLabelGenerator> dataset = DatasetFactory
					.createPeptideCountingVsScoreXYDataSet(idSets.get(0), idSets.get(1), proteinGroupComparisonType,
							distinguish, selectedScoreName);
			final XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
					parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel, false);
			chart.centerRangeAxisOnZero();
			// chart.setXRangeValues(0, 100);
			// this.jPanelChart.setGraphicPanel(chart.getChartPanel());
			return chart.getChartPanel();
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			for (final Experiment experiment : experimentList.getExperiments()) {
				idSets = getIdentificationSets(experiment.getName(), null, true);
				final Pair<XYDataset, MyXYItemLabelGenerator> dataset = DatasetFactory
						.createPeptideCountingVsScoreXYDataSet(idSets.get(0), idSets.get(1), proteinGroupComparisonType,
								distinguish, selectedScoreName);
				final XYPointChart chart = new XYPointChart(parent.getChartTitle(chartType),
						parent.getChartSubtitle(chartType, option), dataset, xAxisLabel, yAxisLabel, false);
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
		return vennChartMap.get(experimentName).getVennData();
	}

	/**
	 * @param experimentName
	 * @return
	 * 
	 */
	public VennChart getVennChart(String experimentName) {
		return vennChartMap.get(experimentName);
	}

	/**
	 * @return
	 */
	public String getChartType() {
		return chartType + " (" + option + ")";
	}
}
