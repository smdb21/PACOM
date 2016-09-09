package org.proteored.miapeExtractor.chart;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RefineryUtilities;
import org.junit.Before;
import org.junit.Test;
import org.proteored.miapeExtractor.analysis.charts.BarChart;
import org.proteored.miapeExtractor.analysis.charts.HeatChart;
import org.proteored.miapeExtractor.analysis.charts.HeatMapChart;
import org.proteored.miapeExtractor.analysis.charts.HistogramChart;
import org.proteored.miapeExtractor.analysis.charts.VennChart;
import org.proteored.miapeExtractor.analysis.charts.XYPointChart;
import org.proteored.miapeExtractor.analysis.gui.tasks.DatasetFactory;
import org.proteored.miapeExtractor.analysis.util.Util;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.exceptions.MiapeDatabaseException;
import org.proteored.miapeapi.exceptions.MiapeSecurityException;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.IdentificationItemEnum;
import org.proteored.miapeapi.experiment.model.IdentificationSet;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.miapeapi.experiment.model.filters.FDRFilter;
import org.proteored.miapeapi.experiment.model.filters.Filter;
import org.proteored.miapeapi.experiment.model.sort.Order;
import org.proteored.miapeapi.experiment.model.sort.SortingParameters;
import org.proteored.miapeapi.interfaces.msi.MiapeMSIDocument;
import org.proteored.miapeapi.interfaces.xml.MiapeXmlFile;
import org.proteored.miapeapi.spring.SpringHandler;
import org.proteored.miapeapi.xml.msi.MIAPEMSIXmlFile;
import org.proteored.miapeapi.xml.msi.MiapeMSIXmlFactory;
import org.proteored.miapeapi.xml.util.ProteinGroupComparisonType;

import junit.framework.Assert;

public class LocalTests {

	String ftpPath;
	String version;
	private final String userName = "smartinez@cnb.csic.es";
	private final String password = "test";
	private static String path = "C:\\inetpub\\wwwroot\\ISB\\data\\PME6\\MIAPE_MSI_XML\\";
	private ControlVocabularyManager cvManager;
	private SortingParameters sprot;
	private SortingParameters spep;
	private final List<Filter> filters = new ArrayList<Filter>();
	private static final int minPeptideLength = 7;
	private static ExperimentList experiments;
	private static final Logger log = Logger.getLogger("log4j.logger.org.proteored");
	private static final int MAX_FILES = 100;

	@Before
	public void initialize() {
		cvManager = SpringHandler.getInstance().getCVManager();
		sprot = new SortingParameters("Mascot:score", Order.DESCENDANT);
		spep = new SortingParameters("Mascot:score", Order.DESCENDANT);
		FDRFilter proteinFDRFilter = new FDRFilter(1.0f, "rev_", true, sprot, IdentificationItemEnum.PROTEIN, null,
				null, null);
		FDRFilter peptideFDRFilter = new FDRFilter(1.0f, "rev_", true, spep, IdentificationItemEnum.PEPTIDE, null, null,
				null);
		filters.add(proteinFDRFilter);
		filters.add(peptideFDRFilter);
	}

	@Test
	public void chartTests() {
		try {
			start(path);
			System.out.println("The end");
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void chartTests2() {
		try {
			start("C:\\Users\\Salva\\Workspaces\\workspace_helios_64\\miape-extractor\\src\\test\\resources");
			System.out.println("The end");
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	private void start(String path) throws IOException, JAXBException {
		experiments = createExperimentFromMiapeFolder(path);
		showCharts();
	}

	private ExperimentList createExperimentFromMiapeFolder(String folder) throws IOException, JAXBException {

		File miapeFolder = new File(folder);
		Assert.assertTrue(miapeFolder.isDirectory());
		final File[] listFiles = miapeFolder.listFiles();

		String currentExpName = null;

		List<Experiment> experimentList = new ArrayList<Experiment>();
		List<IdentificationSet> replicateList = new ArrayList<IdentificationSet>();
		HashMap<String, List<Replicate>> experimentHashMap = new HashMap<String, List<Replicate>>();

		int i = 0;
		for (File file : listFiles) {
			if (file.getName().substring(file.getName().length() - 3, file.getName().length())
					.equalsIgnoreCase("xml")) {
				if (i < MAX_FILES) {

					log.info("processing " + file.getName());
					String repName = getReplicateName(file.getName());
					if (repName == null || repName.equals(""))
						log.info("repname is null");
					String expName = getExpName(file.getName());
					MiapeMSIDocument miapeMSI = getMIAPEMSI(file);
					List<MiapeMSIDocument> listMIAPE = new ArrayList<MiapeMSIDocument>();
					listMIAPE.add(miapeMSI);
					Replicate replicate = new Replicate(repName, expName, null, listMIAPE, filters, null, cvManager,
							false);
					log.info("Exp " + expName + " Replicate " + repName + " proteins:"
							+ replicate.getTotalNumProteinGroups(false) + " peptides:"
							+ replicate.getNumDifferentPeptides(true));

					if (experimentHashMap.containsKey(expName))
						experimentHashMap.get(expName).add(replicate);
					else {
						List<Replicate> repL = new ArrayList<Replicate>();
						repL.add(replicate);
						experimentHashMap.put(expName, repL);
					}

					currentExpName = expName;
				}

				i++;
			}
		}
		for (String expName : experimentHashMap.keySet()) {
			final List<Replicate> list = experimentHashMap.get(expName);
			Experiment experiment = new Experiment(expName, list, filters, minPeptideLength, cvManager, false);
			experimentList.add(experiment);
		}
		ExperimentList expList = new ExperimentList("PME6", experimentList, false, filters, minPeptideLength, cvManager,
				false);
		return expList;

	}

	private MiapeMSIDocument getMIAPEMSI(File file) {
		MiapeXmlFile<MiapeMSIDocument> miapeMSIFile = new MIAPEMSIXmlFile(file);

		MiapeMSIDocument miapeMSI = null;
		try {
			miapeMSI = MiapeMSIXmlFactory.getFactory().toDocument(miapeMSIFile, cvManager, null, userName, password);
		} catch (MiapeDatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MiapeSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return miapeMSI;
	}

	private String getExpName(String name) {
		String REGEX1 = "(.*)_rep._MSI.xml";
		Pattern p = Pattern.compile(REGEX1);
		Matcher m = p.matcher(name);
		if (m.find()) {
			return m.group(1);
		} else {
			return "";
		}
	}

	private String getReplicateName(String name) {
		String REGEX1 = ".*_(rep.)_MSI.xml";
		Pattern p = Pattern.compile(REGEX1);
		Matcher m = p.matcher(name);
		if (m.find()) {
			return m.group(1);
		} else {
			return name;
		}
	}

	private void showCharts() {
		// this.showProteinHeatMap();
		// this.showVenCharts();
		// this.showScoreComparisonCharts();
		// this.showIdentificationNumberBarCharts();
		// this.showScoreDistributionCharts();
		// this.showIdentificationOccurrenceHistogramCharts();
		showCoverageHistogramCharts();
	}

	private void showCoverageHistogramCharts() {
		List<IdentificationSet> listOfIdSets = Util.getListOfIdSets(LocalTests.experiments.getExperiments());
		HistogramDataset dataset = DatasetFactory.createProteinCoverageHistogramDataSet(listOfIdSets, 20,
				HistogramType.FREQUENCY, false, false);
		final HistogramChart histogramChart = new HistogramChart("Protein coverage distribution",
				"Each line belongs to a different experiment", dataset, "protein coverage");
		JFrame jframe = showFrame(histogramChart.getChartPanel(), "MIAPE Extractor tool Charts - Line Histogram");
		showConfirmDialog(jframe);

	}

	private void showScoreDistributionCharts() {
		List<IdentificationSet> listOfIdSets = Util.getListOfIdSets(LocalTests.experiments.getExperiments());
		HistogramDataset dataset = DatasetFactory.createScoreHistogramDataSet(listOfIdSets, sprot.getScoreName(),
				IdentificationItemEnum.PROTEIN, 30, true, HistogramType.SCALE_AREA_TO_1, false, false, false);

		final HistogramChart histogramChart = new HistogramChart("Protein score distribution",
				"Each line belongs to a different experiment", dataset, sprot.getScoreName());
		JFrame jframe = showFrame(histogramChart.getChartPanel(), "MIAPE Extractor tool Charts - Line Histogram");
		showConfirmDialog(jframe);

		dataset = DatasetFactory.createScoreHistogramDataSet(listOfIdSets, spep.getScoreName(),
				IdentificationItemEnum.PEPTIDE, 20, true, HistogramType.SCALE_AREA_TO_1, false, false, false);

		final HistogramChart histogramChart2 = new HistogramChart("Peptide score distribution",
				"Each line belongs to a different experiment", dataset, spep.getScoreName());
		JFrame jframe2 = showFrame(histogramChart2.getChartPanel(), "MIAPE Extractor tool Charts - Line Histogram");
		showConfirmDialog(jframe2);

	}

	private void showIdentificationNumberBarCharts() {
		List<IdentificationSet> listOfIdSets = Util.getListOfIdSets(LocalTests.experiments.getExperiments());
		CategoryDataset dataset = DatasetFactory.createNumberIdentificationCategoryDataSet(listOfIdSets,
				IdentificationItemEnum.PROTEIN, true, null, false, false);
		BarChart proteinBarChart = new BarChart("Number of proteins identified",
				"Number of proteins identified from each replicate of each lab", "Experiment", "# proteins", dataset,
				PlotOrientation.VERTICAL);
		JFrame frame = showFrame(proteinBarChart.getChartPanel(), "MIAPE Extractor tool Charts - Bar Chart");
		showConfirmDialog(frame);

		dataset = DatasetFactory.createNumberIdentificationCategoryDataSet(listOfIdSets, IdentificationItemEnum.PEPTIDE,
				false, null, false, false);
		BarChart peptideBarChart = new BarChart("Number of peptides identified",
				"Number of peptides identified from each replicate of each lab", "Experiment", "# peptides", dataset,
				PlotOrientation.VERTICAL);
		JFrame frame2 = showFrame(peptideBarChart.getChartPanel(), "MIAPE Extractor tool Charts - Bar Chart");
		showConfirmDialog(frame2);
	}

	private void showScoreComparisonCharts() {
		for (IdentificationSet exp : experiments.getExperiments()) {
			Experiment experiment = (Experiment) exp;

			if (experiment.getReplicates().size() == 3) {
				IdentificationSet idset1 = experiment.getReplicates().get(0);
				String label1 = idset1.getName();

				IdentificationSet idset2 = experiment.getReplicates().get(1);
				String label2 = idset2.getName();
				IdentificationSet idset3 = experiment.getReplicates().get(2);
				String label3 = idset3.getName();

				String title = exp.getName();
				String subtitle = "Protein score comparison";
				List<IdentificationSet> idsets = new ArrayList<IdentificationSet>();
				idsets.add(idset1);
				idsets.add(idset2);
				idsets.add(idset3);
				XYDataset dataset = DatasetFactory.createScoreXYDataSet(idsets, sprot.getScoreName(),
						IdentificationItemEnum.PROTEIN, true, false, false, false);
				XYPointChart chartPanel = new XYPointChart(title, subtitle, dataset, sprot.getScoreName(),
						sprot.getScoreName());
				JFrame frame = showFrame(chartPanel.getChartPanel(), "MIAPE Extractor tool Charts - Scatter Chart");
				// showConfirmDialog(frame);

				// String title = exp.getName();
				// String subtitle = "Peptide score comparison";
				// ScatterChart chartPanelPep = new ScatterChart(title,
				// subtitle, idset1, idset2,
				// idset3, this.sprot.getScoreName(), PlotItem.PEPTIDE, true);
				// JFrame frame2 = showFrame(chartPanelPep,
				// "MIAPE Extractor tool Charts - Scatter Chart");
				// showConfirmDialog(frame2);
			}

		}

	}

	private void showVenCharts() {
		final List<Experiment> experimentList = experiments.getExperiments();
		for (IdentificationSet exp : experimentList) {

			System.out.println(exp.getName());
			Experiment experiment = (Experiment) exp;
			if (experiment.getReplicates().size() == 3) {
				IdentificationSet idset1 = experiment.getReplicates().get(0);
				String label1 = idset1.getName();
				IdentificationSet idset2 = experiment.getReplicates().get(1);
				String label2 = idset2.getName();
				IdentificationSet idset3 = experiment.getReplicates().get(2);
				String label3 = idset3.getName();

				// String title = "Protein overlapping over " +
				// experiment.getName() +
				// " replicates";
				// VenChartPanel chartPanel = new VenChartPanel(title, idset1,
				// label1, idset2,
				// label2,
				// idset3, label3, PlotItem.PROTEIN, null);
				// JFrame frame1 = showFrame(chartPanel,
				// "MIAPE Extractor tool Charts - Ven Chart");
				// showConfirmDialog(frame1);

				String title = "Peptide overlapping over " + experiment.getName() + " replicates";
				VennChart chartPanelPep = new VennChart(title, idset1, label1, idset2, label2, idset3, label3,
						IdentificationItemEnum.PEPTIDE, false, false, ProteinGroupComparisonType.ALL_PROTEINS);
				JFrame frame2 = showFrame(chartPanelPep.getChartPanel(), "MIAPE Extractor tool Charts - Ven Chart");
				// showConfirmDialog(frame2);
			}
		}

	}

	private void showProteinHeatMap() {
		List<String> rowList = new ArrayList<String>();
		List<String> columnList = new ArrayList<String>();
		Double min = null;
		Double max = null;
		// dataset
		List<IdentificationSet> listOfIdSets = Util.getListOfIdSets(LocalTests.experiments.getExperiments());
		double[][] dataset = DatasetFactory.createHeapMapDataSet(experiments, listOfIdSets, rowList, columnList,
				IdentificationItemEnum.PROTEIN, null, 8, min, max, false);

		final HeatMapChart heatMapChart = new HeatMapChart("Protein occurrence", dataset, rowList, columnList,
				HeatChart.SCALE_LINEAR);
		String imagePath = "O:\\Dropbox\\SEprot2012\\ProteinHeatMap.png";
		// Save picture to a file
		try {

			heatMapChart.saveImage(new File(imagePath));
		} catch (IOException e) {
			log.warn("The image cannot be saved at  :" + imagePath);
			e.printStackTrace();
		}
		JFrame frame = showFrame(heatMapChart.getjPanel(), "MIAPE Extractor tool Charts - HeatMap");

		showConfirmDialog(frame);

	}

	private void showConfirmDialog(JFrame frame) {
		JOptionPane.showConfirmDialog(frame, "A new chart has been created", "notification", JOptionPane.OK_OPTION);
	}

	private JFrame showFrame(JComponent chart, String appTitle) {
		JFrame frame = new JFrame(appTitle);
		frame.setTitle(appTitle);
		frame.setVisible(true);
		frame.setContentPane(chart);
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		return frame;

	}
}
