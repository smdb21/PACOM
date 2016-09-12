/*
 * Miape2ExperimentListDialog.java Created on __DATE__, __TIME__
 */

package org.proteored.pacom.analysis.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jfree.ui.ExtensionFileFilter;
import org.jfree.ui.RefineryUtilities;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.IdentificationItemEnum;
import org.proteored.miapeapi.experiment.model.PeptideOccurrence;
import org.proteored.miapeapi.experiment.model.ProteinGroupOccurrence;
import org.proteored.miapeapi.experiment.model.filters.FDRFilter;
import org.proteored.miapeapi.experiment.model.filters.Filter;
import org.proteored.miapeapi.experiment.model.filters.PeptideSequenceFilter;
import org.proteored.miapeapi.experiment.model.filters.ProteinACCFilter;
import org.proteored.miapeapi.experiment.model.filters.ScoreFilter;
import org.proteored.miapeapi.experiment.model.sort.SorterUtil;
import org.proteored.pacom.analysis.charts.WordCramChart;
import org.proteored.pacom.analysis.conf.ExperimentListAdapter;
import org.proteored.pacom.analysis.conf.jaxb.CPExperimentList;
import org.proteored.pacom.analysis.exporters.gui.ExporterDialog;
import org.proteored.pacom.analysis.exporters.gui.ExporterToPRIDEDialog;
import org.proteored.pacom.analysis.exporters.gui.IdentificationTableFrame;
import org.proteored.pacom.analysis.exporters.gui.PEXBulkSubmissionSummaryFileCreatorDialog;
import org.proteored.pacom.analysis.genes.GeneDistributionReader;
import org.proteored.pacom.analysis.gui.pike.Miape2PIKEFrame;
import org.proteored.pacom.analysis.gui.tasks.ChartCreatorTask;
import org.proteored.pacom.analysis.gui.tasks.CuratedExperimentSaver;
import org.proteored.pacom.analysis.gui.tasks.DataLoaderTask;
import org.proteored.pacom.analysis.gui.tasks.MemoryCheckerTask;
import org.proteored.pacom.analysis.util.ImageUtils;
import org.proteored.pacom.gui.ImageManager;
import org.proteored.pacom.gui.MainFrame;
import org.proteored.pacom.gui.tasks.OntologyLoaderTask;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

/**
 *
 * @author __USER__
 */
public class ChartManagerFrame extends javax.swing.JFrame implements PropertyChangeListener, TreeDialog {
	private FiltersDialog filterDialog = null;
	public static final String PROTEIN_SCORE_DISTRIBUTION = "Protein score Distribution";
	public static final String PEPTIDE_SCORE_DISTRIBUTION = "Peptide score Distribution";
	public static final String PROTEIN_SCORE_COMPARISON = "Protein score Comparison";
	public static final String PEPTIDE_SCORE_COMPARISON = "Peptide score Comparison";
	public static final String PROTEIN_NUMBER_HISTOGRAM = "Protein number";
	public static final String PEPTIDE_NUMBER_HISTOGRAM = "Peptide number";
	public static final String PROTEIN_OVERLAPING = "Protein overlapping";
	public static final String PEPTIDE_OVERLAPING = "Peptide overlapping";
	public static final String PROTEIN_HEATMAP = "Protein heatMap";
	public static final String PEPTIDE_HEATMAP = "Peptide heatMap";
	public static final String MODIFICATION_SITES_NUMBER = "Number of modified sites";
	public static final String MODIFICATED_PEPTIDE_NUMBER = "Number of modified peptides";
	public static final String PEPTIDE_MODIFICATION_DISTRIBUTION = "Peptide modification distribution";
	public static final String PEPTIDE_MONITORING = "Peptide monitoring";
	public static final String PROTEIN_COVERAGE_DISTRIBUTION = "Protein coverage distribution";
	public static final String FDR = "False Discovery Rate";
	public static final String PROTEIN_REPEATABILITY = "Protein repeatability";
	public static final String PEPTIDE_REPEATABILITY = "Peptide repeatability";
	public static final String PROTEIN_SENSITIVITY_SPECIFICITY = "Protein sensitivity and specificity";
	public static final String MISSEDCLEAVAGE_DISTRIBUTION = "Missed cleavages distribution";
	public static final String PEPTIDE_MASS_DISTRIBUTION = "Peptide mass distribution";
	public static final String PEPTIDE_LENGTH_DISTRIBUTION = "Peptide length distribution";

	public static final String PROTEIN_COVERAGE = "Protein coverage";
	public static final String PEPTIDE_CHARGE_HISTOGRAM = "Peptide charge distribution";
	public static final String SINGLE_HIT_PROTEINS = "Single hit proteins";
	public static final String PEPTIDE_NUMBER_IN_PROTEINS = "Number of different peptides per protein";
	public static final String DELTA_MZ_OVER_MZ = "Peptide mass error";
	public static final String PSM_PEP_PROT = "PSMs/Peptides/Proteins";
	public static final String FDR_VS_SCORE = "FDRs vs Score & num. proteins vs Score";
	public static final String EXCLUSIVE_PROTEIN_NUMBER = "Number of exclusive proteins";
	public static final String EXCLUSIVE_PEPTIDE_NUMBER = "Number of exclusive peptides";
	public static final String PEPTIDE_PRESENCY_HEATMAP = "Peptide presence HeatMap";
	public static final String PROTEIN_NUMBER_OF_PEPTIDES_HEATMAP = "Number of peptides per protein HeatMap";
	public static final String PEPTIDE_NUM_PER_PROTEIN_MASS = "Number of peptides / protein molecular weight";
	public static final String HUMAN_CHROMOSOME_COVERAGE = "Human chromosome coverage";
	public static final String CHR16_MAPPING = "Human chromosome 16 mapping (SPanish-HPP)";
	public static final String CHR_MAPPING = "Proteins and genes per chromosome";
	public static final String CHR_PEPTIDES_MAPPING = "Peptides and PSMs per chromosome";
	public static final String PROTEIN_NAME_CLOUD = "Protein words cloud";
	public static final String PROTEIN_GROUP_TYPES = "Protein group type distribution";
	public static final String PEPTIDE_RT = "Peptide Retention Times distribution";
	public static final String PEPTIDE_RT_COMPARISON = "Peptide Retention Times Comparison";
	public static final String SINGLE_RT_COMPARISON = "Single Retention Times Comparison";
	public static final String SPECTROMETERS = "Spectrometers";
	public static final String INPUT_PARAMETERS = "Input parameters";
	public static final String PEPTIDE_COUNTING_HISTOGRAM = "Peptide counting ratio histogram";
	public static final String PEPTIDE_COUNTING_VS_SCORE = "Peptide counting ratio vs score";

	private final String[] chartTypes = new String[] { PROTEIN_SCORE_DISTRIBUTION, PEPTIDE_SCORE_DISTRIBUTION,
			PROTEIN_SCORE_COMPARISON, PEPTIDE_SCORE_COMPARISON, PROTEIN_NUMBER_HISTOGRAM, PEPTIDE_NUMBER_HISTOGRAM,
			PSM_PEP_PROT, SINGLE_HIT_PROTEINS, PEPTIDE_NUMBER_IN_PROTEINS, PEPTIDE_NUM_PER_PROTEIN_MASS,
			PROTEIN_REPEATABILITY, PEPTIDE_REPEATABILITY, PROTEIN_GROUP_TYPES, EXCLUSIVE_PROTEIN_NUMBER,
			EXCLUSIVE_PEPTIDE_NUMBER, PROTEIN_SENSITIVITY_SPECIFICITY, PEPTIDE_RT, PEPTIDE_RT_COMPARISON,
			SINGLE_RT_COMPARISON, SINGLE_HIT_PROTEINS, SPECTROMETERS, INPUT_PARAMETERS, PROTEIN_OVERLAPING,
			PEPTIDE_OVERLAPING, PROTEIN_COVERAGE, PROTEIN_COVERAGE_DISTRIBUTION, PROTEIN_HEATMAP,
			PROTEIN_NUMBER_OF_PEPTIDES_HEATMAP, PEPTIDE_HEATMAP, PEPTIDE_PRESENCY_HEATMAP, MODIFICATED_PEPTIDE_NUMBER,
			MODIFICATION_SITES_NUMBER, PEPTIDE_MODIFICATION_DISTRIBUTION, PEPTIDE_MONITORING,
			MISSEDCLEAVAGE_DISTRIBUTION, FDR, FDR_VS_SCORE, PEPTIDE_MASS_DISTRIBUTION, PEPTIDE_LENGTH_DISTRIBUTION,
			PEPTIDE_CHARGE_HISTOGRAM, CHR_MAPPING, CHR_PEPTIDES_MAPPING, HUMAN_CHROMOSOME_COVERAGE, CHR16_MAPPING,
			DELTA_MZ_OVER_MZ, PROTEIN_NAME_CLOUD, PEPTIDE_COUNTING_HISTOGRAM, PEPTIDE_COUNTING_VS_SCORE
			// ,LABELLED_PEPTIDE_MONITORING
	};
	private String currentChartType = PSM_PEP_PROT;
	private final String FDR_CANNOT_BE_CALCULATED_MESSAGE = "Global FDR cannot be calculated";

	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	public ExperimentList experimentList;
	private DataLoaderTask dataLoader;
	// hashmap to store the MIAPE documents that are retrieved <Identified,
	// FullPath to the file>
	private static HashMap<Integer, String> miapeMSIsRetrieved = new HashMap<Integer, String>();
	private static ChartManagerFrame instance;

	public static final String ONE_SERIES_PER_EXPERIMENT = "One series per level 1 (experiment)";
	public static final String ONE_SERIES_PER_REPLICATE = "One series per level 2 (fraction/band/replicate)";
	public static final String ONE_CHART_PER_EXPERIMENT = "One chart per level 1 (experiment)";
	public static final String ONE_SERIES_PER_EXPERIMENT_LIST = "One serie per level 0 (experiment list)";
	private static final String MENU_SEPARATION = "menu separation";

	private final JFrame parentFrame;
	private ChartCreatorTask chartCreator;
	private MemoryCheckerTask memoryChecker;
	private final AdditionalOptionsPanelFactory optionsFactory = new AdditionalOptionsPanelFactory(this);
	// private boolean isChr16ChartShowed;
	private IdentificationTableFrame identificationTable;
	private CuratedExperimentSaver curatedExperimentSaver;
	private boolean proteinSequencesRetrieved;
	private AppliedFiltersDialog filtersDialog;
	private PEXBulkSubmissionSummaryFileCreatorDialog pexSubmissionDialog;
	File cfgFile;
	private File currentFolder = new File(System.getProperty("user.dir"));
	private Long previousCfgFileSize;
	private Integer minPeptideLength;
	private Boolean groupProteinsAtExperimentListLevel;
	private Boolean isLocalProcessingInParallel;
	private boolean errorLoadingData;

	@Override
	public void dispose() {
		if (dataLoader != null && dataLoader.getState() == StateValue.STARTED) {
			boolean canceled = dataLoader.cancel(true);
			while (!canceled) {
				try {
					log.info("Waiting for cancelling data loader");
					Thread.sleep(100);
					canceled = dataLoader.cancel(true);
				} catch (InterruptedException e) {
				}
			}

		}
		if (chartCreator != null && chartCreator.getState() == StateValue.STARTED) {
			boolean canceled = chartCreator.cancel(true);
			while (!canceled) {
				try {
					log.info("Waiting for cancelling chartCreator");
					Thread.sleep(100);
					canceled = chartCreator.cancel(true);

				} catch (InterruptedException e) {
				}
			}

		}
		if (memoryChecker != null)
			memoryChecker.cancel(true);
		if (parentFrame != null) {
			parentFrame.setEnabled(true);
			parentFrame.setVisible(true);
		}
		GeneralOptionsDialogNoParallel.getInstance(this).dispose();
		super.dispose();
	}

	/**
	 * Creates new form Miape2ExperimentListDialog
	 *
	 * @param parentDialog
	 */
	@SuppressWarnings("restriction")
	private ChartManagerFrame(JFrame parentDialog, File cfgFile) {
		parentFrame = parentDialog;
		this.cfgFile = cfgFile;
		if (parentFrame != null)
			parentFrame.setVisible(false);

		try {
			UIManager.setLookAndFeel(new WindowsLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
		}
		initComponents();
		// set icon image
		setIconImage(ImageManager.getImageIcon(ImageManager.PROTEORED_MIAPE_API).getImage());
		// init CV NAMES of SCORE NAMES

		try {

			//
			// load data in this.experimentList
			jButtonShowTable.setEnabled(false);
			jButtonExport2Excel.setEnabled(false);
			jButtonExport2PEX.setEnabled(false);
			jButtonExport2PRIDE.setEnabled(false);

			// GeneralOptionsDialog generalOptionsDialog = GeneralOptionsDialog
			// .getInstance(this);
			// if (!generalOptionsDialog.isDoNotAskAgainSelected()) {
			// showGeneralOptions(true);
			// }
			//
			// loadData(generalOptionsDialog.getMinPeptideLength(),
			// generalOptionsDialog.groupProteinsAtExperimentListLevel(),
			// cfgFile, generalOptionsDialog.isLocalProcessingInParallel());

		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			appendStatus(e.getMessage());
			setProgressBarIndeterminate(false);

		}
		initializeChartTypeMenu();

		updateControlStates();

		loadButtonIcons();
		RefineryUtilities.centerFrameOnScreen(this);

		jTextAreaStatus.setFont(new JTextField().getFont());

	}

	public static ChartManagerFrame getInstance(Miape2ExperimentListDialog parentDialog, File cfgFile) {

		if (instance == null)
			instance = new ChartManagerFrame(parentDialog, cfgFile);
		instance.cfgFile = cfgFile;
		GeneralOptionsDialogNoParallel generalOptionsDialog = GeneralOptionsDialogNoParallel.getInstance(instance,
				true);
		boolean group = generalOptionsDialog.groupProteinsAtExperimentListLevel();
		int pepLength = generalOptionsDialog.getMinPeptideLength();
		boolean parallel = generalOptionsDialog.isLocalProcessingInParallel();

		if (!generalOptionsDialog.isDoNotAskAgainSelected()) {
			generalOptionsDialog.setVisible(true);
			// do not load data now...wait after GeneralOptions dispose
			return instance;
		}
		log.info("now=" + cfgFile.length() + " previous=" + instance.previousCfgFileSize);

		if (instance.dataShouldBeLoaded(cfgFile, group, pepLength, parallel)) {
			instance.loadData(pepLength, group, cfgFile, parallel);
		}

		return instance;
	}

	@Override
	public void setVisible(boolean b) {

		if (b) {
			// Memory checker
			memoryChecker = new MemoryCheckerTask();
			memoryChecker.addPropertyChangeListener(this);
			memoryChecker.execute();

			currentChartType = ChartManagerFrame.PSM_PEP_PROT;

			pack();
		}
		super.setVisible(b);
	}

	/**
	 * Checks if something has changed on the
	 * {@link GeneralOptionsDialogNoParallel}. If yes, the data it will return
	 * true. It also checks if the cgfFile is different and it will return true.
	 * Otherwise, it will return false.
	 *
	 *
	 * @return
	 */
	protected boolean dataShouldBeLoaded(File cfgFile, boolean groupingAtExperimentListLevel, int minPeptideLength,
			boolean processInparallel) {

		if (errorLoadingData) {
			return false;
		}
		if (cfgFile != null && !cfgFile.getAbsolutePath().equals(this.cfgFile.getAbsolutePath()))
			return true;
		if (previousCfgFileSize == null || (cfgFile != null && cfgFile.length() != previousCfgFileSize))
			return true;
		if (this.minPeptideLength == null || minPeptideLength != this.minPeptideLength)
			return true;
		if (groupProteinsAtExperimentListLevel == null || !String.valueOf(groupingAtExperimentListLevel)
				.equals(String.valueOf(groupProteinsAtExperimentListLevel)))
			return true;
		if (isLocalProcessingInParallel == null
				|| !String.valueOf(processInparallel).equals(String.valueOf(isLocalProcessingInParallel)))
			return true;
		if (experimentList == null)
			return true;

		return false;
	}

	/**
	 * Loads the data with the parameters taken from the
	 * {@link GeneralOptionsDialogNoParallel}
	 */
	private void loadData() {
		GeneralOptionsDialogNoParallel generalOptionsDialog = GeneralOptionsDialogNoParallel.getInstance(instance,
				true);
		boolean groupingAtExperimentListLevel = generalOptionsDialog.groupProteinsAtExperimentListLevel();
		int minPeptideLength = generalOptionsDialog.getMinPeptideLength();

		this.loadData(minPeptideLength, groupingAtExperimentListLevel, cfgFile, isLocalProcessingInParallel);

	}

	private void loadData(Integer minPeptideLength, boolean groupingAtExperimentListLevel, File cfgFile,
			boolean processInParallel) {

		this.cfgFile = cfgFile;
		this.minPeptideLength = minPeptideLength;
		groupProteinsAtExperimentListLevel = groupingAtExperimentListLevel;
		previousCfgFileSize = this.cfgFile.length();
		isLocalProcessingInParallel = processInParallel;
		CPExperimentList cpExpList = getCPExperimentList(cfgFile);
		setTitle(cpExpList.getName() + " - Data Comparison and Inspection Charts");

		// set to "loading data..." the information labels
		setInformation1("Loading data...");
		setInformation2("Loading data...");
		setInformation3("Loading data...");

		experimentList = null;
		System.gc();

		OntologyLoaderTask ontologyLoaderTask = new OntologyLoaderTask();
		ontologyLoaderTask.addPropertyChangeListener(this);
		ontologyLoaderTask.execute();

		// now is moved to the propertyListener, since the data will be loaded
		// after ontology loading
		// this.dataLoader = new DataLoaderTask(cpExpList, minPeptideLength,
		// groupingAtExperimentListLevel, null, processInParallel);
		// // filters);
		// this.dataLoader.addPropertyChangeListener(this);
		// this.dataLoader.execute();

	}

	protected void loadData(Integer minPeptideLength, boolean groupingAtExperimentListLevel,
			boolean processInParallel) {
		loadData(minPeptideLength, groupingAtExperimentListLevel, cfgFile, processInParallel);
	}

	private CPExperimentList getCPExperimentList(File cfgFile) {
		CPExperimentList cpExpList = new ExperimentListAdapter(cfgFile).getCpExperimentList();
		return cpExpList;
	}

	private void loadButtonIcons() {
		// STAR
		jButtonSaveAsFiltered.setIcon(ImageManager.getImageIcon(ImageManager.STAR));
		// STAR CLICKED
		jButtonSaveAsFiltered.setPressedIcon(ImageManager.getImageIcon(ImageManager.STAR_CLICKED));

		// FUNNEL
		jButtonSeeAppliedFilters.setIcon(ImageManager.getImageIcon(ImageManager.FUNNEL));
		// FUNNEL CLICKED
		jButtonSeeAppliedFilters.setPressedIcon(ImageManager.getImageIcon(ImageManager.FUNNEL_CLICKED));

		// TRASH
		jButtonDiscardFilteredData.setIcon(ImageManager.getImageIcon(ImageManager.TRASH));
		// TRASH CLICKED
		jButtonDiscardFilteredData.setPressedIcon(ImageManager.getImageIcon(ImageManager.TRASH_CLICKED));

		// TABLE
		jButtonShowTable.setIcon(ImageManager.getImageIcon(ImageManager.TABLE));
		// TABLE CLICKED
		jButtonShowTable.setPressedIcon(ImageManager.getImageIcon(ImageManager.TABLE_CLICKED));

		jButtonExport2Excel.setIcon(ImageManager.getImageIcon(ImageManager.EXCEL_TABLE));

		jButtonExport2PEX.setIcon(ImageManager.getImageIcon(ImageManager.PEX));

		jButtonExport2PRIDE.setIcon(ImageManager.getImageIcon(ImageManager.PRIDE_LOGO));
	}

	public AdditionalOptionsPanelFactory getOptionsFactory() {
		return optionsFactory;
	}

	private void initializeChartTypeMenu() {
		// Store all chartTypes in a mapping
		Map<String, JRadioButtonMenuItem> radioButtonMenuMap = new HashMap<String, JRadioButtonMenuItem>();
		for (final String chartType : chartTypes) {

			JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(chartType);
			menuItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					currentChartType = chartType;
					addCustomizationControls();
					startShowingChart();
				}
			});

			if (chartType.equals(currentChartType))
				menuItem.setSelected(true);
			buttonGroup2.add(menuItem);
			radioButtonMenuMap.put(chartType, menuItem);
		}

		// Identification numbers
		JMenu menuIdNumbers = new JMenu("Number of identifications");
		List<String> idNumbersMenus = new ArrayList<String>();
		idNumbersMenus.add(PSM_PEP_PROT);
		idNumbersMenus.add(MENU_SEPARATION); // MENU SEPARATION
		idNumbersMenus.add(PEPTIDE_NUMBER_HISTOGRAM);
		idNumbersMenus.add(PEPTIDE_REPEATABILITY);
		idNumbersMenus.add(EXCLUSIVE_PEPTIDE_NUMBER);
		idNumbersMenus.add(PEPTIDE_NUMBER_IN_PROTEINS);
		idNumbersMenus.add(MENU_SEPARATION); // MENU SEPARATION
		idNumbersMenus.add(PROTEIN_NUMBER_HISTOGRAM);
		idNumbersMenus.add(PROTEIN_REPEATABILITY);
		idNumbersMenus.add(EXCLUSIVE_PROTEIN_NUMBER);
		idNumbersMenus.add(SINGLE_HIT_PROTEINS);
		idNumbersMenus.add(PROTEIN_SENSITIVITY_SPECIFICITY);
		idNumbersMenus.add(PROTEIN_OVERLAPING);
		idNumbersMenus.add(PROTEIN_GROUP_TYPES);
		addSubmenus(menuIdNumbers, idNumbersMenus, radioButtonMenuMap, jMenuChartType);

		// Overlappings
		JMenu menuOverlappings = new JMenu("Overlappings");
		List<String> overlappingMenus = new ArrayList<String>();
		overlappingMenus.add(PEPTIDE_OVERLAPING);
		overlappingMenus.add(EXCLUSIVE_PEPTIDE_NUMBER);
		overlappingMenus.add(PEPTIDE_REPEATABILITY);
		overlappingMenus.add(PEPTIDE_PRESENCY_HEATMAP);
		overlappingMenus.add(PEPTIDE_HEATMAP);
		overlappingMenus.add(MENU_SEPARATION); // MENU SEPARATION
		overlappingMenus.add(PROTEIN_OVERLAPING);
		overlappingMenus.add(EXCLUSIVE_PROTEIN_NUMBER);
		overlappingMenus.add(PROTEIN_REPEATABILITY);
		overlappingMenus.add(PROTEIN_HEATMAP);
		addSubmenus(menuOverlappings, overlappingMenus, radioButtonMenuMap, jMenuChartType);

		// Scores
		JMenu menuScores = new JMenu("Scores");
		List<String> scoreMenus = new ArrayList<String>();
		scoreMenus.add(PEPTIDE_SCORE_COMPARISON);
		scoreMenus.add(PEPTIDE_SCORE_DISTRIBUTION);
		scoreMenus.add(MENU_SEPARATION); // MENU SEPARATION
		scoreMenus.add(PROTEIN_SCORE_COMPARISON);
		scoreMenus.add(PROTEIN_SCORE_DISTRIBUTION);
		scoreMenus.add(MENU_SEPARATION); // MENU SEPARATION
		scoreMenus.add(FDR_VS_SCORE);
		addSubmenus(menuScores, scoreMenus, radioButtonMenuMap, jMenuChartType);

		// protein features
		JMenu menuProteinFeatures = new JMenu("Protein features");
		List<String> proteinFeaturesMenus = new ArrayList<String>();
		proteinFeaturesMenus.add(PROTEIN_NAME_CLOUD);
		proteinFeaturesMenus.add(MENU_SEPARATION); // MENU SEPARATION
		proteinFeaturesMenus.add(PROTEIN_GROUP_TYPES);
		proteinFeaturesMenus.add(MENU_SEPARATION); // MENU SEPARATION
		proteinFeaturesMenus.add(EXCLUSIVE_PROTEIN_NUMBER);
		proteinFeaturesMenus.add(PROTEIN_REPEATABILITY);
		addSubmenus(menuProteinFeatures, proteinFeaturesMenus, radioButtonMenuMap, jMenuChartType);

		// protein coverage as submenu of protein features
		JMenu menuProteinCoverage = new JMenu("Protein coverage");
		List<String> proteinCoveragesMenus = new ArrayList<String>();
		proteinCoveragesMenus.add(PROTEIN_COVERAGE);

		proteinCoveragesMenus.add(PROTEIN_COVERAGE_DISTRIBUTION);
		addSubmenus(menuProteinCoverage, proteinCoveragesMenus, radioButtonMenuMap, menuProteinFeatures);

		// peptide features
		JMenu menuPeptideFeatures = new JMenu("Peptide features");
		List<String> peptideFeaturesMenus = new ArrayList<String>();
		peptideFeaturesMenus.add(MISSEDCLEAVAGE_DISTRIBUTION);
		peptideFeaturesMenus.add(MENU_SEPARATION); // MENU SEPARATION
		peptideFeaturesMenus.add(PEPTIDE_MASS_DISTRIBUTION);
		peptideFeaturesMenus.add(MENU_SEPARATION); // MENU SEPARATION
		peptideFeaturesMenus.add(PEPTIDE_LENGTH_DISTRIBUTION);
		peptideFeaturesMenus.add(MENU_SEPARATION); // MENU SEPARATION
		peptideFeaturesMenus.add(PEPTIDE_CHARGE_HISTOGRAM);
		peptideFeaturesMenus.add(MENU_SEPARATION); // MENU SEPARATION
		peptideFeaturesMenus.add(DELTA_MZ_OVER_MZ);
		peptideFeaturesMenus.add(MENU_SEPARATION); // MENU SEPARATION
		peptideFeaturesMenus.add(PEPTIDE_RT);
		peptideFeaturesMenus.add(PEPTIDE_RT_COMPARISON);
		peptideFeaturesMenus.add(SINGLE_RT_COMPARISON);
		peptideFeaturesMenus.add(MENU_SEPARATION); // MENU SEPARATION
		peptideFeaturesMenus.add(EXCLUSIVE_PEPTIDE_NUMBER);
		peptideFeaturesMenus.add(PEPTIDE_REPEATABILITY);
		peptideFeaturesMenus.add(MENU_SEPARATION); // MENU SEPARATION
		addSubmenus(menuPeptideFeatures, peptideFeaturesMenus, radioButtonMenuMap, jMenuChartType);

		// Peptide modifications as submenu of peptide features
		JMenu menuPeptideModifications = new JMenu("Peptide modifications");
		List<String> peptideModificationsMenus = new ArrayList<String>();
		peptideModificationsMenus.add(PEPTIDE_MODIFICATION_DISTRIBUTION);
		peptideModificationsMenus.add(PEPTIDE_MONITORING);
		peptideModificationsMenus.add(MODIFICATED_PEPTIDE_NUMBER);
		peptideModificationsMenus.add(MODIFICATION_SITES_NUMBER);
		addSubmenus(menuPeptideModifications, peptideModificationsMenus, radioButtonMenuMap, menuPeptideFeatures);

		// heatmaps
		JMenu menuHeatMaps = new JMenu("Heatmaps");
		List<String> heatMapsMenus = new ArrayList<String>();
		heatMapsMenus.add(PROTEIN_HEATMAP);
		heatMapsMenus.add(PROTEIN_NUMBER_OF_PEPTIDES_HEATMAP);
		heatMapsMenus.add(PEPTIDE_HEATMAP);
		heatMapsMenus.add(PEPTIDE_PRESENCY_HEATMAP);
		addSubmenus(menuHeatMaps, heatMapsMenus, radioButtonMenuMap, jMenuChartType);

		// False Discovery Rates
		JMenu menuFalseDiscoveryRate = new JMenu("False discovery Rates");
		List<String> fdrMenus = new ArrayList<String>();
		fdrMenus.add(FDR);
		fdrMenus.add(FDR_VS_SCORE);
		addSubmenus(menuFalseDiscoveryRate, fdrMenus, radioButtonMenuMap, jMenuChartType);

		// Gene mapping
		JMenu menuHumanGenes = new JMenu("Human genes and chromosomes");
		List<String> humanGenesMenus = new ArrayList<String>();
		humanGenesMenus.add(HUMAN_CHROMOSOME_COVERAGE);
		humanGenesMenus.add(CHR_MAPPING);
		humanGenesMenus.add(CHR_PEPTIDES_MAPPING);
		humanGenesMenus.add(MENU_SEPARATION); // MENU SEPARATION
		humanGenesMenus.add(CHR16_MAPPING);
		addSubmenus(menuHumanGenes, humanGenesMenus, radioButtonMenuMap, jMenuChartType);

		// Peptide counting
		JMenu menuPeptideCounting = new JMenu("Peptide counting");
		List<String> peptideCountingMenus = new ArrayList<String>();
		peptideCountingMenus.add(PEPTIDE_COUNTING_HISTOGRAM);
		peptideCountingMenus.add(PEPTIDE_COUNTING_VS_SCORE);
		addSubmenus(menuPeptideCounting, peptideCountingMenus, radioButtonMenuMap, jMenuChartType);

		// Metadata
		JMenu menuMetadata = new JMenu("Metadata");
		List<String> metadataMenus = new ArrayList<String>();
		metadataMenus.add(SPECTROMETERS);
		metadataMenus.add(INPUT_PARAMETERS);
		addSubmenus(menuMetadata, metadataMenus, radioButtonMenuMap, jMenuChartType);
	}

	private void addSubmenus(JMenu menu, List<String> chartTypesForSubmenus,
			Map<String, JRadioButtonMenuItem> radioButtonMenuMap, JMenu parentMenu) {

		for (String chartType : chartTypesForSubmenus) {
			if (MENU_SEPARATION.equals(chartType))
				menu.addSeparator();
			else if (radioButtonMenuMap.containsKey(chartType))
				menu.add(radioButtonMenuMap.get(chartType));
		}
		parentMenu.add(menu);
	}

	protected void applyFilters() {
		filterDialog.applyFilters(experimentList);

	}

	private String getNumberIdentificationsString() {
		final int numDifferentProteins = experimentList.getNumDifferentProteinGroups(countNonConclusiveProteins());
		final int numProteins = experimentList.getTotalNumProteinGroups(countNonConclusiveProteins());
		final int numDifferentPeptides = experimentList.getNumDifferentPeptides(distinguishModifiedPeptides());
		final int numPeptides = experimentList.getTotalNumPeptides();
		long t1 = System.currentTimeMillis();
		int genesFromProteinGroup = GeneDistributionReader.getInstance()
				.getGenesFromProteinGroup(experimentList.getIdentifiedProteinGroups()).size();
		int firstGenesFromProteinGroup = GeneDistributionReader.getInstance()
				.getFirstGenesFromProteinGroup(experimentList.getIdentifiedProteinGroups()).size();
		log.debug((System.currentTimeMillis() - t1) + " msg");

		String ret = "<html>Peptides: " + numPeptides + " (" + numDifferentPeptides + " uniques)<br>";
		ret += "Protein groups: " + numProteins;
		if (numDifferentProteins != numProteins)
			ret += " (" + numDifferentProteins + " uniques)";

		if (genesFromProteinGroup > 0) {
			log.debug(genesFromProteinGroup + " genes");
			ret += "<br>Human genes: " + genesFromProteinGroup;
		}
		if (genesFromProteinGroup != firstGenesFromProteinGroup) {
			log.debug(firstGenesFromProteinGroup + " genes, taking the first protein for each group");
			ret += " (" + firstGenesFromProteinGroup + ")";
		}
		ret += "</html>";

		return ret;
	}

	private String getFDRString() {

		String format = "#.##";
		DecimalFormat df = new java.text.DecimalFormat(format);

		StringBuilder sb = new StringBuilder();
		if (experimentList != null && experimentList.validFDRCalculation()) {
			final String uniqueFDRScoreName = filterDialog.getUniqueFDRScoreName();
			// protein level
			try {

				final float proteinFDR = experimentList.getProteinFDR(uniqueFDRScoreName);
				if (Float.valueOf(proteinFDR).equals(Float.NaN))
					sb.append("Global FDRs (Prot - Pep - PSM) = NA");
				else {
					if (proteinFDR < 0.001 && proteinFDR != 0.0)
						df = new DecimalFormat("#.###");

					sb.append("Global FDRs (Prot - Pep - PSM) = " + df.format(proteinFDR) + "%");
				}
			} catch (IllegalMiapeArgumentException ex) {
				// log.info("The FDR cannot be calculated: " + ex.getMessage());
			}

			// peptide level
			try {
				final float peptideFDR = experimentList.getPeptideFDR(uniqueFDRScoreName);
				if (Float.valueOf(peptideFDR).equals(Float.NaN))
					sb.append(" - NA");
				else {
					if (peptideFDR < 0.001 && peptideFDR != 0.0)
						df = new DecimalFormat("#.###");
					sb.append(" - " + df.format(peptideFDR) + "%");
				}
			} catch (IllegalMiapeArgumentException ex) {
				log.debug("The FDR cannot be calculated: " + ex.getMessage());
			}

			// PSM level
			try {
				final float psmFDR = experimentList.getPSMFDR(uniqueFDRScoreName);
				if (Float.valueOf(psmFDR).equals(Float.NaN))
					sb.append(" - NA");
				else {
					if (psmFDR < 0.001 && psmFDR != 0.0)
						df = new DecimalFormat("#.###");
					sb.append(" - " + df.format(psmFDR) + "%");
				}
			} catch (IllegalMiapeArgumentException ex) {
				log.debug("The FDR cannot be calculated: " + ex.getMessage());
			}

		}
		if (!"".equals(sb.toString()))
			return sb.toString();

		if (filterDialog.getFDRFilters() != null && !filterDialog.getFDRFilters().isEmpty()) {
			return FDR_CANNOT_BE_CALCULATED_MESSAGE;
		}
		return "FDR params not defined";
	}

	public String getOptionChart() {
		if (jComboBoxChartOptions != null)
			return (String) jComboBoxChartOptions.getSelectedItem();
		return null;
	}

	private void updateOptionComboBox() {
		String chartType = currentChartType;
		String[] options = getOptionsByChartType(chartType);
		final DefaultComboBoxModel model = (DefaultComboBoxModel) jComboBoxChartOptions.getModel();
		if (options == null) {
			model.removeAllElements();
			return;
		}
		// just update combo box if options have changed
		boolean optionsHaveChanged = false;

		String selectedItem = (String) model.getSelectedItem();
		if (options.length == 4 && selectedItem == null)
			selectedItem = options[1];
		if (model.getSize() != options.length)
			optionsHaveChanged = true;
		if (!optionsHaveChanged)
			for (int i = 0; i < model.getSize(); i++) {
				if (i <= options.length) {
					if (!options[i].equals(model.getElementAt(i)))
						optionsHaveChanged = true;
				}
			}

		if (optionsHaveChanged) {
			// set selected item the previous selected item (if present in the
			// new model)
			final DefaultComboBoxModel aModel = new DefaultComboBoxModel(options);
			for (int i = 0; i < aModel.getSize(); i++) {
				if (aModel.getElementAt(i).equals(selectedItem))
					aModel.setSelectedItem(aModel.getElementAt(i));
			}
			jComboBoxChartOptions.setModel(aModel);
		}
	}

	private String[] getOptionsByChartType(String chartType) {
		// TODO
		String[] ret = null;

		if (CHR16_MAPPING.equals(chartType) || CHR_MAPPING.equals(chartType) || CHR_PEPTIDES_MAPPING.equals(chartType)
				|| PROTEIN_NAME_CLOUD.equals(chartType)) {
			ret = new String[2];
			ret[0] = ONE_SERIES_PER_EXPERIMENT_LIST;
			ret[1] = ONE_CHART_PER_EXPERIMENT;
		} else if (INPUT_PARAMETERS.equals(chartType) || SPECTROMETERS.equals(chartType)) {
			ret = new String[2];
			ret[0] = ONE_SERIES_PER_REPLICATE;
			ret[1] = ONE_CHART_PER_EXPERIMENT;
		} else if (experimentList.getExperiments().size() > 1) {
			ret = new String[4];
			ret[0] = ONE_SERIES_PER_EXPERIMENT_LIST;
			ret[1] = ONE_SERIES_PER_EXPERIMENT;
			ret[2] = ONE_SERIES_PER_REPLICATE;
			ret[3] = ONE_CHART_PER_EXPERIMENT;

			if (chartType.equals(FDR_VS_SCORE)) {

				ret = new String[3];
				ret[0] = ONE_SERIES_PER_EXPERIMENT_LIST;
				ret[1] = ONE_SERIES_PER_EXPERIMENT;
				ret[2] = ONE_CHART_PER_EXPERIMENT;

			} else if (chartType.equals(PEPTIDE_REPEATABILITY) || chartType.equals(PROTEIN_REPEATABILITY)) {
				if (getOptionsFactory().getOverReplicates()) {
					ret = new String[2];
					ret[0] = ONE_SERIES_PER_EXPERIMENT;
					ret[1] = ONE_CHART_PER_EXPERIMENT;
				} else if (chartType.equals(PROTEIN_REPEATABILITY)) {
					ret = new String[1];
					ret[0] = ONE_SERIES_PER_EXPERIMENT_LIST;
				}
			} else if (chartType.equals(PROTEIN_OVERLAPING) || chartType.equals(PEPTIDE_OVERLAPING)
					|| chartType.equals(PROTEIN_SCORE_COMPARISON) || chartType.equals(PEPTIDE_SCORE_COMPARISON)
					|| chartType.equals(EXCLUSIVE_PROTEIN_NUMBER) || chartType.equals(EXCLUSIVE_PEPTIDE_NUMBER)
					|| chartType.equals(PEPTIDE_RT_COMPARISON) || chartType.equals(PEPTIDE_COUNTING_HISTOGRAM)
					|| chartType.equals(PEPTIDE_COUNTING_VS_SCORE)) {
				ret = new String[3];
				ret[0] = ONE_SERIES_PER_EXPERIMENT;
				ret[1] = ONE_SERIES_PER_REPLICATE;
				ret[2] = ONE_CHART_PER_EXPERIMENT;
			}
		} else {
			ret = new String[3];
			ret[0] = ONE_SERIES_PER_EXPERIMENT_LIST;
			ret[1] = ONE_SERIES_PER_EXPERIMENT;
			ret[2] = ONE_SERIES_PER_REPLICATE;

		}

		// } else if (PROTEIN_HEATMAP.equals(chartType) ||
		// PEPTIDE_HEATMAP.equals(chartType)) {
		// ret = new String[0];
		// }
		return ret;
	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		buttonGroup1 = new javax.swing.ButtonGroup();
		buttonGroupThresholds = new javax.swing.ButtonGroup();
		buttonGroupDecoyPrefix = new javax.swing.ButtonGroup();
		buttonGroup2 = new javax.swing.ButtonGroup();
		jPanelStatus = new javax.swing.JPanel();
		jScrollPane3 = new javax.swing.JScrollPane();
		jTextAreaStatus = new javax.swing.JTextArea();
		jProgressBar = new javax.swing.JProgressBar();
		jProgressBarMemoryUsage = new javax.swing.JProgressBar();
		jPanelLeft = new javax.swing.JPanel();
		jPanelChartType = new javax.swing.JPanel();
		jComboBoxChartOptions = new javax.swing.JComboBox();
		jPanelAdditionalCustomizations = new javax.swing.JScrollPane();
		jPanelAddOptions = new javax.swing.JPanel();
		jPanelPeptideCounting = new javax.swing.JPanel();
		jCheckBoxUniquePeptides = new javax.swing.JCheckBox();
		jCheckBoxCountNonConclusiveProteins = new javax.swing.JCheckBox();
		jPanelInformation = new javax.swing.JPanel();
		jLabelInformation1 = new javax.swing.JLabel();
		jLabelInformation2 = new javax.swing.JLabel();
		jLabelInformation3 = new javax.swing.JLabel();
		jButtonSeeAppliedFilters = new javax.swing.JButton();
		jButtonDiscardFilteredData = new javax.swing.JButton();
		jButtonShowTable = new javax.swing.JButton();
		jButtonSaveAsFiltered = new javax.swing.JButton();
		jButtonExport2PRIDE = new javax.swing.JButton();
		jButtonExport2PEX = new javax.swing.JButton();
		jButtonExport2Excel = new javax.swing.JButton();
		jPanelRigth = new javax.swing.JPanel();
		jScrollPaneChart = new javax.swing.JScrollPane();
		jPanelChart = new javax.swing.JPanel();
		jMenuBar1 = new javax.swing.JMenuBar();
		jMenuChartType = new javax.swing.JMenu();
		jMenuFilters = new javax.swing.JMenu();
		jMenuItemDefineFilters = new javax.swing.JMenuItem();
		jSeparator1 = new javax.swing.JSeparator();
		jSeparator2 = new javax.swing.JSeparator();
		jCheckBoxMenuItemFDRFilter = new javax.swing.JCheckBoxMenuItem();
		jCheckBoxMenuItemScoreFilters = new javax.swing.JCheckBoxMenuItem();
		jCheckBoxMenuItemOcurrenceFilter = new javax.swing.JCheckBoxMenuItem();
		jCheckBoxMenuItemModificationFilter = new javax.swing.JCheckBoxMenuItem();
		jCheckBoxMenuItemProteinACCFilter = new javax.swing.JCheckBoxMenuItem();
		jCheckBoxMenuItemPeptideNumberFilter = new javax.swing.JCheckBoxMenuItem();
		jCheckBoxMenuItemPeptideLenthFilter = new javax.swing.JCheckBoxMenuItem();
		jCheckBoxMenuItemPeptideSequenceFilter = new javax.swing.JCheckBoxMenuItem();
		jCheckBoxMenuItemPeptideForMRMFilter = new javax.swing.JCheckBoxMenuItem();
		jMenuPike = new javax.swing.JMenu();
		jMenuItemSend2PIKE = new javax.swing.JMenuItem();
		jMenuOptions = new javax.swing.JMenu();
		jMenuItemGeneralOptions = new javax.swing.JMenuItem();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("MIAPE Extractor Charts");
		getContentPane().setLayout(new java.awt.GridBagLayout());

		jPanelStatus.setBorder(
				javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Status"));

		jTextAreaStatus.setColumns(20);
		jTextAreaStatus.setEditable(false);
		jTextAreaStatus.setLineWrap(true);
		jTextAreaStatus.setRows(5);
		jTextAreaStatus.setWrapStyleWord(true);
		jScrollPane3.setViewportView(jTextAreaStatus);

		jProgressBarMemoryUsage.setToolTipText("Memory usage");
		jProgressBarMemoryUsage.setStringPainted(true);

		javax.swing.GroupLayout jPanelStatusLayout = new javax.swing.GroupLayout(jPanelStatus);
		jPanelStatus.setLayout(jPanelStatusLayout);
		jPanelStatusLayout.setHorizontalGroup(jPanelStatusLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanelStatusLayout.createSequentialGroup().addContainerGap()
						.addGroup(jPanelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 591, Short.MAX_VALUE)
								.addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 591, Short.MAX_VALUE)
								.addComponent(jProgressBarMemoryUsage, javax.swing.GroupLayout.DEFAULT_SIZE, 591,
										Short.MAX_VALUE))
						.addContainerGap()));
		jPanelStatusLayout
				.setVerticalGroup(jPanelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanelStatusLayout.createSequentialGroup()
								.addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 92,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jProgressBarMemoryUsage, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)));

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		getContentPane().add(jPanelStatus, gridBagConstraints);

		jPanelChartType.setBorder(javax.swing.BorderFactory
				.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Chart Option"));

		jComboBoxChartOptions.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				jComboBoxChartOptionsItemStateChanged(evt);
			}
		});

		javax.swing.GroupLayout jPanelChartTypeLayout = new javax.swing.GroupLayout(jPanelChartType);
		jPanelChartType.setLayout(jPanelChartTypeLayout);
		jPanelChartTypeLayout
				.setHorizontalGroup(jPanelChartTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanelChartTypeLayout.createSequentialGroup().addContainerGap()
								.addComponent(jComboBoxChartOptions, 0, 0, Short.MAX_VALUE).addContainerGap()));
		jPanelChartTypeLayout
				.setVerticalGroup(jPanelChartTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanelChartTypeLayout.createSequentialGroup()
								.addComponent(jComboBoxChartOptions, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jPanelAdditionalCustomizations.setBorder(javax.swing.BorderFactory
				.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Additional options"));
		jPanelAdditionalCustomizations
				.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		javax.swing.GroupLayout jPanelAddOptionsLayout = new javax.swing.GroupLayout(jPanelAddOptions);
		jPanelAddOptions.setLayout(jPanelAddOptionsLayout);
		jPanelAddOptionsLayout.setHorizontalGroup(jPanelAddOptionsLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 324, Short.MAX_VALUE));
		jPanelAddOptionsLayout.setVerticalGroup(jPanelAddOptionsLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 394, Short.MAX_VALUE));

		jPanelAdditionalCustomizations.setViewportView(jPanelAddOptions);

		jPanelPeptideCounting.setBorder(javax.swing.BorderFactory
				.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Peptide and protein counting"));

		jCheckBoxUniquePeptides.setText("distinguish mod. and unmod. peptides");
		jCheckBoxUniquePeptides.setToolTipText(
				"<html>If this option is not selected:<br>\nA peptide identified as unmodified and for<br>\ninstance containing an oxidized Methionine is<br>\ncounted as one.<br>\n</html>");
		jCheckBoxUniquePeptides.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				jCheckBoxUniquePeptidesItemStateChanged(evt);
			}
		});

		jCheckBoxCountNonConclusiveProteins.setText("count non-conclusive proteins");
		jCheckBoxCountNonConclusiveProteins.setToolTipText(
				"<html>If this option is selected:<br>\nProtein groups with a NON-CONSLUSIVE evidence<br>\nare taken into account in any chart.<br>\n</html>");
		jCheckBoxCountNonConclusiveProteins.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				jCheckBoxCountNonConclusiveProteinsItemStateChanged(evt);
			}
		});

		javax.swing.GroupLayout jPanelPeptideCountingLayout = new javax.swing.GroupLayout(jPanelPeptideCounting);
		jPanelPeptideCounting.setLayout(jPanelPeptideCountingLayout);
		jPanelPeptideCountingLayout.setHorizontalGroup(
				jPanelPeptideCountingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanelPeptideCountingLayout.createSequentialGroup().addContainerGap()
								.addGroup(jPanelPeptideCountingLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(jCheckBoxUniquePeptides)
										.addComponent(jCheckBoxCountNonConclusiveProteins))
								.addContainerGap(35, Short.MAX_VALUE)));
		jPanelPeptideCountingLayout.setVerticalGroup(jPanelPeptideCountingLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanelPeptideCountingLayout.createSequentialGroup().addComponent(jCheckBoxUniquePeptides)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxCountNonConclusiveProteins)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jPanelInformation.setBorder(javax.swing.BorderFactory
				.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Information"));

		jButtonSeeAppliedFilters.setIcon(new javax.swing.ImageIcon(
				"C:\\Users\\Salva\\workspace\\miape-extractor\\src\\main\\resources\\funnel.png")); // NOI18N
		jButtonSeeAppliedFilters.setToolTipText("<html>Show defined filters</html>");
		jButtonSeeAppliedFilters.setEnabled(false);
		jButtonSeeAppliedFilters.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonSeeAppliedFiltersActionPerformed(evt);
			}
		});

		jButtonDiscardFilteredData.setIcon(new javax.swing.ImageIcon(
				"C:\\Users\\Salva\\workspace\\miape-extractor\\src\\main\\resources\\trash.png")); // NOI18N
		jButtonDiscardFilteredData.setToolTipText(
				"<html>Discard proteins and peptides that <br>\n      have not been passed the filters.</html>");
		jButtonDiscardFilteredData.setEnabled(false);
		jButtonDiscardFilteredData.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonDiscardFilteredDataActionPerformed(evt);
			}
		});

		jButtonShowTable.setIcon(new javax.swing.ImageIcon(
				"C:\\Users\\Salva\\workspace\\miape-extractor\\src\\main\\resources\\table.png")); // NOI18N
		jButtonShowTable.setToolTipText("<html>Show dataset in a table.</html>");
		jButtonShowTable.setEnabled(false);
		jButtonShowTable.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jButtonShowTableMouseClicked(evt);
			}
		});
		jButtonShowTable.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonShowTableActionPerformed(evt);
			}
		});

		jButtonSaveAsFiltered.setIcon(new javax.swing.ImageIcon(
				"C:\\Users\\Salva\\workspace\\miape-extractor\\src\\main\\resources\\star.png")); // NOI18N
		jButtonSaveAsFiltered.setToolTipText(
				"<html>Save the experiments of the project as <b>curated experiments</b>.<br>\nThis will save the experiments containing just the peptides<br> and proteins that has passed the filters.<br>\nThey will be saved individually in a separate location.</html>");
		jButtonSaveAsFiltered.setEnabled(false);
		jButtonSaveAsFiltered.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonSaveAsFilteredActionPerformed(evt);
			}
		});

		jButtonExport2PRIDE.setIcon(new javax.swing.ImageIcon(
				"C:\\Users\\Salva\\workspace\\miape-extractor\\src\\main\\resources\\pride_logo_peq.jpg")); // NOI18N
		jButtonExport2PRIDE.setToolTipText(
				"<html>Export current data to PRIDE XML.<br>\nA PRIDE XML file will be created for each one<br>experiment/level 1 node.</html>");
		jButtonExport2PRIDE.setEnabled(false);
		jButtonExport2PRIDE.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonExport2PRIDEActionPerformed(evt);
			}
		});

		jButtonExport2PEX.setIcon(new javax.swing.ImageIcon(
				"C:\\Users\\Salva\\workspace\\miape-extractor\\src\\main\\resources\\pex.png")); // NOI18N
		// jButtonExport2PEX.setToolTipText(
		// "<html>Prepare current data for a ProteomeXchange
		// submission.<br>\nRequired files will be prepared at an output
		// folder,<br>\nand a Bulk Submission Summary file will be
		// created<br>\nto use it in the ProteomeXchange submission
		// tool.</html>");
		jButtonExport2PEX.setToolTipText(
				"<html>ProteomeXchange submission not supported anymore.<br>\nWe apologize for the inconveniences.</html>");

		jButtonExport2PEX.setEnabled(false);
		jButtonExport2PEX.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonExport2PEXActionPerformed(evt);
			}
		});

		jButtonExport2Excel.setIcon(new javax.swing.ImageIcon(
				"C:\\Users\\Salva\\workspace\\miape-extractor\\src\\main\\resources\\excel_table.png")); // NOI18N
		jButtonExport2Excel.setToolTipText(
				"<html>Export current data to a Tab Separated Values file<br>that can be opened by Excel.</html>");
		jButtonExport2Excel.setEnabled(false);
		jButtonExport2Excel.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonExport2ExcelActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanelInformationLayout = new javax.swing.GroupLayout(jPanelInformation);
		jPanelInformation.setLayout(jPanelInformationLayout);
		jPanelInformationLayout.setHorizontalGroup(jPanelInformationLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanelInformationLayout.createSequentialGroup().addContainerGap()
						.addGroup(jPanelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jLabelInformation3, javax.swing.GroupLayout.Alignment.TRAILING,
										javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
								.addComponent(jLabelInformation2, javax.swing.GroupLayout.Alignment.TRAILING,
										javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
								.addComponent(jLabelInformation1, javax.swing.GroupLayout.DEFAULT_SIZE, 262,
										Short.MAX_VALUE)
								.addGroup(jPanelInformationLayout.createSequentialGroup()
										.addGroup(jPanelInformationLayout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addGroup(jPanelInformationLayout.createSequentialGroup()
														.addComponent(jButtonSeeAppliedFilters,
																javax.swing.GroupLayout.PREFERRED_SIZE, 47,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(jButtonDiscardFilteredData,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																47, javax.swing.GroupLayout.PREFERRED_SIZE))
												.addComponent(jButtonExport2PRIDE, javax.swing.GroupLayout.DEFAULT_SIZE,
														101, Short.MAX_VALUE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(jPanelInformationLayout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
												.addGroup(jPanelInformationLayout.createSequentialGroup()
														.addComponent(jButtonSaveAsFiltered,
																javax.swing.GroupLayout.PREFERRED_SIZE, 47,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(jButtonShowTable,
																javax.swing.GroupLayout.PREFERRED_SIZE, 47,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(jButtonExport2Excel,
																javax.swing.GroupLayout.PREFERRED_SIZE, 46,
																javax.swing.GroupLayout.PREFERRED_SIZE))
												.addComponent(jButtonExport2PEX, 0, 0, Short.MAX_VALUE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
						.addContainerGap()));
		jPanelInformationLayout.setVerticalGroup(jPanelInformationLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanelInformationLayout.createSequentialGroup()
						.addComponent(jLabelInformation1, javax.swing.GroupLayout.PREFERRED_SIZE, 21,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jLabelInformation2, javax.swing.GroupLayout.PREFERRED_SIZE, 24,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jLabelInformation3, javax.swing.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jButtonExport2Excel, 0, 0, Short.MAX_VALUE)
								.addGroup(jPanelInformationLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
										.addComponent(jButtonSeeAppliedFilters, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(jButtonDiscardFilteredData, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addComponent(jButtonSaveAsFiltered, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jButtonShowTable, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanelInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jButtonExport2PEX, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jButtonExport2PRIDE))
						.addContainerGap()));

		javax.swing.GroupLayout jPanelLeftLayout = new javax.swing.GroupLayout(jPanelLeft);
		jPanelLeft.setLayout(jPanelLeftLayout);
		jPanelLeftLayout.setHorizontalGroup(jPanelLeftLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelLeftLayout.createSequentialGroup()
						.addGroup(jPanelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
								.addComponent(jPanelAdditionalCustomizations, javax.swing.GroupLayout.DEFAULT_SIZE, 286,
										Short.MAX_VALUE)
								.addComponent(jPanelInformation, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jPanelChartType, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(jPanelPeptideCounting, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE))
						.addContainerGap()));
		jPanelLeftLayout
				.setVerticalGroup(jPanelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanelLeftLayout.createSequentialGroup().addContainerGap()
								.addComponent(jPanelInformation, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jPanelChartType, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jPanelPeptideCounting, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jPanelAdditionalCustomizations, javax.swing.GroupLayout.DEFAULT_SIZE, 381,
										Short.MAX_VALUE)));

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridheight = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
		getContentPane().add(jPanelLeft, gridBagConstraints);

		jPanelRigth.setBorder(
				javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Chart"));

		jScrollPaneChart.setBorder(null);

		javax.swing.GroupLayout jPanelChartLayout = new javax.swing.GroupLayout(jPanelChart);
		jPanelChart.setLayout(jPanelChartLayout);
		jPanelChartLayout.setHorizontalGroup(jPanelChartLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 577, Short.MAX_VALUE));
		jPanelChartLayout.setVerticalGroup(jPanelChartLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 570, Short.MAX_VALUE));

		jScrollPaneChart.setViewportView(jPanelChart);

		javax.swing.GroupLayout jPanelRigthLayout = new javax.swing.GroupLayout(jPanelRigth);
		jPanelRigth.setLayout(jPanelRigthLayout);
		jPanelRigthLayout
				.setHorizontalGroup(jPanelRigthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanelRigthLayout.createSequentialGroup().addContainerGap()
								.addComponent(jScrollPaneChart, javax.swing.GroupLayout.PREFERRED_SIZE, 577,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		jPanelRigthLayout
				.setVerticalGroup(jPanelRigthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
								jPanelRigthLayout.createSequentialGroup()
										.addComponent(jScrollPaneChart, javax.swing.GroupLayout.PREFERRED_SIZE, 570,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 14);
		getContentPane().add(jPanelRigth, gridBagConstraints);

		jMenuChartType.setText("Chart Type");
		jMenuChartType.setEnabled(false);
		jMenuBar1.add(jMenuChartType);

		jMenuFilters.setText("Filters");
		jMenuFilters.setEnabled(false);

		jMenuItemDefineFilters.setAccelerator(
				javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
		jMenuItemDefineFilters.setText("Define filters");
		jMenuItemDefineFilters.setToolTipText("Click here to define the filters");
		jMenuItemDefineFilters.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItemDefineFiltersActionPerformed(evt);
			}
		});
		jMenuFilters.add(jMenuItemDefineFilters);
		jMenuFilters.add(jSeparator1);
		jMenuFilters.add(jSeparator2);

		jCheckBoxMenuItemFDRFilter.setText("FDR Filter(s)");
		jCheckBoxMenuItemFDRFilter.setToolTipText("Filter by FDR");
		jCheckBoxMenuItemFDRFilter.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxMenuItemFDRFilterActionPerformed(evt);
			}
		});
		jMenuFilters.add(jCheckBoxMenuItemFDRFilter);

		jCheckBoxMenuItemScoreFilters.setText("Score Threshold(s)");
		jCheckBoxMenuItemScoreFilters.setToolTipText("Filter by Scores");
		jCheckBoxMenuItemScoreFilters.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxMenuItemScoreFiltersActionPerformed(evt);
			}
		});
		jMenuFilters.add(jCheckBoxMenuItemScoreFilters);

		jCheckBoxMenuItemOcurrenceFilter.setText("Occurrence Filter");
		jCheckBoxMenuItemOcurrenceFilter.setToolTipText("Filter by accurrence over the replicates");
		jCheckBoxMenuItemOcurrenceFilter.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxMenuItemOcurrenceFilterActionPerformed(evt);
			}
		});
		jMenuFilters.add(jCheckBoxMenuItemOcurrenceFilter);

		jCheckBoxMenuItemModificationFilter.setText("Modification Filter");
		jCheckBoxMenuItemModificationFilter.setToolTipText("Filter peptides by a PTM");
		jCheckBoxMenuItemModificationFilter.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxMenuItemModificationFilterActionPerformed(evt);
			}
		});
		jMenuFilters.add(jCheckBoxMenuItemModificationFilter);

		jCheckBoxMenuItemProteinACCFilter.setText("Protein ACC Filter");
		jCheckBoxMenuItemProteinACCFilter.setToolTipText("Filter by a protein accession list");
		jCheckBoxMenuItemProteinACCFilter.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxMenuItemProteinACCFilterActionPerformed(evt);
			}
		});
		jMenuFilters.add(jCheckBoxMenuItemProteinACCFilter);

		jCheckBoxMenuItemPeptideNumberFilter.setText("Peptides per proteins Filter");
		jCheckBoxMenuItemPeptideNumberFilter.setToolTipText("Filter by the number of peptide that each protein has");
		jCheckBoxMenuItemPeptideNumberFilter.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxMenuItemPeptideNumberFilterActionPerformed(evt);
			}
		});
		jMenuFilters.add(jCheckBoxMenuItemPeptideNumberFilter);

		jCheckBoxMenuItemPeptideLenthFilter.setText("Peptide Length Filter");
		jCheckBoxMenuItemPeptideLenthFilter.setToolTipText("Filter by the number of aminoacids of each peptide");
		jCheckBoxMenuItemPeptideLenthFilter.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxMenuItemPeptideLenthFilterActionPerformed(evt);
			}
		});
		jMenuFilters.add(jCheckBoxMenuItemPeptideLenthFilter);

		jCheckBoxMenuItemPeptideSequenceFilter.setText("Peptide sequence Filter");
		jCheckBoxMenuItemPeptideSequenceFilter.setToolTipText("Filter by a list of peptide sequences ");
		jCheckBoxMenuItemPeptideSequenceFilter.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxMenuItemPeptideSequenceFilterActionPerformed(evt);
			}
		});
		jMenuFilters.add(jCheckBoxMenuItemPeptideSequenceFilter);

		jCheckBoxMenuItemPeptideForMRMFilter.setText("Peptide for MRM Filter");
		jCheckBoxMenuItemPeptideForMRMFilter.setToolTipText("Filter by the number of aminoacids that the peptides has");
		jCheckBoxMenuItemPeptideForMRMFilter.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxMenuItemPeptideForMRMFilterActionPerformed(evt);
			}
		});
		jMenuFilters.add(jCheckBoxMenuItemPeptideForMRMFilter);

		jMenuBar1.add(jMenuFilters);

		jMenuPike.setText("PIKE");
		jMenuPike.setToolTipText("Send protein list to the Protein Information and Knowledge Extractor (PIKE)");
		jMenuPike.setEnabled(false);

		jMenuItemSend2PIKE.setAccelerator(
				javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, java.awt.event.InputEvent.CTRL_MASK));
		jMenuItemSend2PIKE.setText("Send proteins to PIKE");
		jMenuItemSend2PIKE.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItemSend2PIKEActionPerformed(evt);
			}
		});
		jMenuPike.add(jMenuItemSend2PIKE);

		jMenuBar1.add(jMenuPike);

		jMenuOptions.setText("Options");

		jMenuItemGeneralOptions.setAccelerator(
				javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
		jMenuItemGeneralOptions.setText("General Options");
		jMenuItemGeneralOptions.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItemGeneralOptionsActionPerformed(evt);
			}
		});
		jMenuOptions.add(jMenuItemGeneralOptions);

		jMenuBar1.add(jMenuOptions);

		setJMenuBar(jMenuBar1);

		java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 935) / 2, (screenSize.height - 836) / 2, 935, 836);
	}// </editor-fold>
		// GEN-END:initComponents

	private void jCheckBoxCountNonConclusiveProteinsItemStateChanged(java.awt.event.ItemEvent evt) {
		// if (evt.getStateChange() == ItemEvent.SELECTED) {
		// JOptionPane
		// .showMessageDialog(
		// this,
		// "<html>NON CONCLUSIVE proteins will be taken into account for all the
		// charts.<br>"
		// +
		// "Following the recomendations from '<a
		// href=\"http://www.biomedcentral.com/pubmed/16009968\">Nesvizhskii A,
		// Aebersold R: Interpretation of shotgun proteomic data. Mol Cell
		// Proteomics 2005, 4(10):1419-1440</a>.</html>",
		// "NON-CONCLUSIVE proteins will be taken into account",
		// JOptionPane.INFORMATION_MESSAGE);
		// }
		startShowingChart();
	}

	private void jMenuItemGeneralOptionsActionPerformed(java.awt.event.ActionEvent evt) {
		GeneralOptionsDialogNoParallel.getInstance(this, false).setVisible(true);
	}

	private void jButtonDiscardFilteredDataActionPerformed(java.awt.event.ActionEvent evt) {
		discardFilteredData();
	}

	private void jButtonExport2ExcelActionPerformed(java.awt.event.ActionEvent evt) {
		exportToTSV();
	}

	private void jButtonExport2PEXActionPerformed(java.awt.event.ActionEvent evt) {
		exportToPEX();
	}

	private void jButtonExport2PRIDEActionPerformed(java.awt.event.ActionEvent evt) {
		exportToPRIDE();
	}

	private void exportToPEX() {
		// if (this.pexSubmissionDialog == null)
		pexSubmissionDialog = new PEXBulkSubmissionSummaryFileCreatorDialog(this, experimentList);
		pexSubmissionDialog.setVisible(true);
	}

	private void jCheckBoxMenuItemPeptideForMRMFilterActionPerformed(java.awt.event.ActionEvent evt) {
		filterDialog.setPeptideForMRMFilterEnabled(jCheckBoxMenuItemPeptideForMRMFilter.isSelected());
		if (jCheckBoxMenuItemPeptideForMRMFilter.isSelected()) {
			if (filterDialog.getPeptideForMRMFilter() == null) {
				filterDialog.setCurrentIndex(FiltersDialog.PEPTIDEFORMRMFILTER_INDEX);
				filterDialog.setVisible(true);
			}
		}
		startShowingChart();
	}

	private void jCheckBoxMenuItemPeptideSequenceFilterActionPerformed(java.awt.event.ActionEvent evt) {
		filterDialog.setPeptideSequencesFilterEnabled(jCheckBoxMenuItemPeptideSequenceFilter.isSelected());
		if (jCheckBoxMenuItemPeptideSequenceFilter.isSelected()) {
			if (filterDialog.getPeptideSequencesFilter() == null) {
				filterDialog.setCurrentIndex(FiltersDialog.PEPTIDESEQUENCEFILTER_INDEX);
				filterDialog.setVisible(true);
			}
		}
		startShowingChart();
	}

	private void jButtonShowTableMouseClicked(java.awt.event.MouseEvent evt) {

	}

	private void jButtonSaveAsFilteredActionPerformed(java.awt.event.ActionEvent evt) {
		saveAsCuratedExperiment();
	}

	private void saveAsCuratedExperiment() {
		curatedExperimentSaver = new CuratedExperimentSaver(this, experimentList, OntologyLoaderTask.getCvManager());
		curatedExperimentSaver.addPropertyChangeListener(this);
		curatedExperimentSaver.execute();

	}

	private void jButtonShowTableActionPerformed(java.awt.event.ActionEvent evt) {
		showIdentificationTable();
	}

	private void showIdentificationTable() {
		log.info("Showing identification table");
		// if (this.identificationTable == null)
		identificationTable = IdentificationTableFrame.getInstance(this, experimentList);
		identificationTable.setVisible(true);

	}

	private void jButtonSeeAppliedFiltersActionPerformed(java.awt.event.ActionEvent evt) {
		showAppliedFiltersDialog();
	}

	private void showAppliedFiltersDialog() {
		filtersDialog = new AppliedFiltersDialog(this, true, filterDialog.getFilters());
		filtersDialog.setVisible(true);
	}

	public void discardFilteredData() {

		// String message =
		// "<html>Proteins and peptides that have not passed the filters<br>"
		// + "are going to be permanently discarted.<br>"
		// + "If you permanently discard these data, you will not able:<br>"
		// + "<ul><li>to see discarted peptides and proteins</li>"
		// + "<li>to add/remove/modify some filter</li></ul>"
		// + "In the other hand, <b>you will save some extra memory</b><br>"
		// + "which can improve the performance for large datasets.<br>"
		// + "Are you sure do you want to discart the data?</html>";
		// int selectedOption = JOptionPane.showConfirmDialog(this, message,
		// "Warning discarting filtered-out data!",
		// JOptionPane.YES_NO_CANCEL_OPTION);
		// if (selectedOption == JOptionPane.YES_OPTION) {

		Runtime.getRuntime().gc();
		jMenuFilters.show(false);
		jButtonDiscardFilteredData.setEnabled(false);
		// }
	}

	private void jCheckBoxMenuItemPeptideLenthFilterActionPerformed(java.awt.event.ActionEvent evt) {
		filterDialog.setPeptideLengthFilterEnabled(jCheckBoxMenuItemPeptideLenthFilter.isSelected());
		if (jCheckBoxMenuItemPeptideLenthFilter.isSelected()) {
			if (filterDialog.getPeptideLengthFilter() == null) {
				filterDialog.setCurrentIndex(FiltersDialog.PEPTIDELENGTHFILTER_INDEX);
				filterDialog.setVisible(true);
			}
		}
		startShowingChart();
	}

	private void jCheckBoxMenuItemPeptideNumberFilterActionPerformed(java.awt.event.ActionEvent evt) {
		filterDialog.setPeptideNumberFilterEnabled(jCheckBoxMenuItemPeptideNumberFilter.isSelected());
		if (jCheckBoxMenuItemPeptideNumberFilter.isSelected()) {
			if (filterDialog.getPeptideNumberFilter() == null) {
				filterDialog.setCurrentIndex(FiltersDialog.PEPTIDENUMBERFILTER_INDEX);
				filterDialog.setVisible(true);
			}
		}
		startShowingChart();
	}

	private void jCheckBoxMenuItemProteinACCFilterActionPerformed(java.awt.event.ActionEvent evt) {
		filterDialog.setProteinACCFilterEnabled(jCheckBoxMenuItemProteinACCFilter.isSelected());
		if (jCheckBoxMenuItemProteinACCFilter.isSelected()) {
			if (filterDialog.getProteinACCFilter() == null) {
				filterDialog.setCurrentIndex(FiltersDialog.PROTEINACCFILTER_INDEX);
				filterDialog.setVisible(true);
			}
		}
		startShowingChart();
	}

	private void jMenuItemSend2PIKEActionPerformed(java.awt.event.ActionEvent evt) {
		final Collection<ProteinGroupOccurrence> proteinGroupOccurrences = experimentList
				.getProteinGroupOccurrenceList().values();
		List<String> accessions = new ArrayList<String>();
		for (ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrences) {
			accessions.addAll(proteinGroupOccurrence.getAccessions());
		}
		Miape2PIKEFrame pikeFrame = new Miape2PIKEFrame(this, accessions);
		pikeFrame.setVisible(true);
	}

	private void jCheckBoxMenuItemModificationFilterActionPerformed(java.awt.event.ActionEvent evt) {
		filterDialog.setModificationFilterEnabled(jCheckBoxMenuItemModificationFilter.isSelected());
		if (jCheckBoxMenuItemModificationFilter.isSelected()) {
			if (filterDialog.getModificationFilter() == null) {
				filterDialog.setCurrentIndex(FiltersDialog.MODIFICATIONFILTER_INDEX);
				filterDialog.setVisible(true);
			}
		}
		startShowingChart();
	}

	private void exportToTSV() {
		ExporterDialog exporterDialog = new ExporterDialog(this, experimentList);
		exporterDialog.setVisible(true);
	}

	private void exportToPRIDE() {

		ExporterToPRIDEDialog exporterDialog = new ExporterToPRIDEDialog(this, experimentList);
		exporterDialog.setVisible(true);
	}

	private void jComboBoxChartOptionsItemStateChanged(java.awt.event.ItemEvent evt) {
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			if (dataLoader == null || !dataLoader.isDone())
				return;
			// add additional customizations
			addCustomizationControls();
			// repaint
			startShowingChart();
		}
	}

	private void jCheckBoxMenuItemOcurrenceFilterActionPerformed(java.awt.event.ActionEvent evt) {
		filterDialog.setOccurrenceFilterEnabled(jCheckBoxMenuItemOcurrenceFilter.isSelected());
		if (jCheckBoxMenuItemOcurrenceFilter.isSelected()) {
			if (filterDialog.getOccurrenceFilter() == null) {
				filterDialog.setCurrentIndex(FiltersDialog.OCCURRENCEFILTER_INDEX);
				filterDialog.setVisible(true);
			}
		}
		startShowingChart();
	}

	private void jCheckBoxMenuItemScoreFiltersActionPerformed(java.awt.event.ActionEvent evt) {
		filterDialog.setScoreFilterEnabled(jCheckBoxMenuItemScoreFilters.isSelected());
		if (jCheckBoxMenuItemScoreFilters.isSelected()) {
			List<ScoreFilter> filters = filterDialog.getScoreFilters(IdentificationItemEnum.PROTEIN);
			filters.addAll(filterDialog.getScoreFilters(IdentificationItemEnum.PEPTIDE));
			if (filters.isEmpty()) {
				filterDialog.setCurrentIndex(FiltersDialog.SCOREFILTER_INDEX);
				filterDialog.setVisible(true);
			} else {
				boolean thereIsScoreFilterDefined = false;
				for (Filter filter : filters) {
					if (filter instanceof ScoreFilter)
						thereIsScoreFilterDefined = true;
				}
				if (!thereIsScoreFilterDefined) {
					filterDialog.setCurrentIndex(FiltersDialog.SCOREFILTER_INDEX);
					filterDialog.setVisible(true);
				}
			}
		}
		startShowingChart();
	}

	private void jCheckBoxMenuItemFDRFilterActionPerformed(java.awt.event.ActionEvent evt) {
		log.info("FDRFILTER ACTION EVENT!!: from " + evt.getSource().getClass().getCanonicalName() + " "
				+ evt.getActionCommand() + " id:" + evt.getID() + " modifiers:" + evt.getModifiers());

		filterDialog.setFDRFilterEnabled(jCheckBoxMenuItemFDRFilter.isSelected());
		if (jCheckBoxMenuItemFDRFilter.isSelected()) {
			List<Filter> filters = filterDialog.getFilters();
			if (filters.isEmpty()) {
				log.info("There is not filters defined");
				filterDialog.setCurrentIndex(FiltersDialog.FDRFILTER_INDEX);
				filterDialog.setVisible(true);
				// Do not show the chart until the definition of the filters
				return;
			} else {
				// log.info("There are " + filters.size() + " filters defined");
				boolean thereIsFDRFilterDefined = false;
				for (Filter filter : filters) {
					if (filter instanceof FDRFilter) {
						thereIsFDRFilterDefined = true;
						continue;
						// log.info("One filter is a FDR filter");
					}
				}
				if (!thereIsFDRFilterDefined) {
					filterDialog.setCurrentIndex(FiltersDialog.FDRFILTER_INDEX);
					filterDialog.setVisible(true);
					return;
				}
			}
		}
		startShowingChart();

	}

	private void jMenuItemDefineFiltersActionPerformed(java.awt.event.ActionEvent evt) {
		showFilterDefinitionDialog();
	}

	private void showFilterDefinitionDialog() {
		log.info("Showing the filter definition dialog");

		filterDialog.setVisible(true);
	}

	private void jCheckBoxUniquePeptidesItemStateChanged(java.awt.event.ItemEvent evt) {

		optionsFactory.updatePeptideSequenceList();
		startShowingChart();

	}

	public void addCustomizationControls() {
		// TODO add more customizations if you add more chart types

		String chartType = currentChartType;
		String options = (String) jComboBoxChartOptions.getSelectedItem();

		// remove all content
		jPanelAddOptions.removeAll();

		if (PROTEIN_SCORE_DISTRIBUTION.equals(chartType) || PEPTIDE_SCORE_DISTRIBUTION.equals(chartType)) {
			addLineHistogramControls(true, false);
		} else if (PROTEIN_COVERAGE_DISTRIBUTION.equals(chartType)) {
			addLineHistogramControls(false, false);
		} else if (PEPTIDE_MASS_DISTRIBUTION.equals(chartType)) {
			addLineHistogramControls(false, true);
		} else if (PROTEIN_SCORE_COMPARISON.equals(chartType) || PEPTIDE_SCORE_COMPARISON.equals(chartType)) {
			addScoreComparisonControls(options);
		} else if (PEPTIDE_NUMBER_HISTOGRAM.equals(chartType) || PROTEIN_NUMBER_HISTOGRAM.equals(chartType)) {
			addHistogramBarControls();
		} else if (PROTEIN_COVERAGE.equals(chartType)) {
			addProteinCoverageHistogramBarControls();
		} else if (PEPTIDE_CHARGE_HISTOGRAM.equals(chartType)) {
			addChargeHistogramBarControls();
		} else if (PEPTIDE_OVERLAPING.equals(chartType) || PROTEIN_OVERLAPING.equals(chartType)) {
			addOverlapingControls(options, 3, false);
		} else if (EXCLUSIVE_PROTEIN_NUMBER.equals(chartType) || EXCLUSIVE_PEPTIDE_NUMBER.equals(chartType)) {
			addOverlapingControls(options, null, true);
		} else if (PEPTIDE_HEATMAP.equals(chartType) || PROTEIN_HEATMAP.equals(chartType)
				|| PROTEIN_NUMBER_OF_PEPTIDES_HEATMAP.equals(chartType)) {
			addHeatMapControls();
		} else if (MODIFICATION_SITES_NUMBER.equals(chartType) || MODIFICATED_PEPTIDE_NUMBER.equals(chartType)) {
			addPeptideModificationControls();
		} else if (PEPTIDE_MODIFICATION_DISTRIBUTION.equals(chartType)) {
			addPeptideModificationControls();
		} else if (PEPTIDE_MONITORING.equals(chartType)) {
			addPeptideMonitoringControls();
		} else if (PROTEIN_REPEATABILITY.equals(chartType) || PEPTIDE_REPEATABILITY.equals(chartType)) {
			addRepeatibilityControls();
		} else if (PROTEIN_SENSITIVITY_SPECIFICITY.equals(chartType)) {
			addSensitivitySpecificityControls();
		} else if (MISSEDCLEAVAGE_DISTRIBUTION.equals(chartType)) {
			addMissedCleavageDistributionControls(options);
		} else if (PEPTIDE_LENGTH_DISTRIBUTION.equals(chartType)) {
			addPeptideLengthDistributionControls(options);
		} else if (CHR16_MAPPING.equals(chartType)) {
			// this.isChr16ChartShowed = true;
			addChr16MappingControls();
		} else if (SINGLE_HIT_PROTEINS.equals(chartType)) {
			addSingleHitProteinControls();
		} else if (PEPTIDE_NUMBER_IN_PROTEINS.equals(chartType)) {
			addPeptideNumberInProteinsControls();
		} else if (DELTA_MZ_OVER_MZ.equals(chartType)) {
			addDeltaOverZControls(options);
		} else if (PSM_PEP_PROT.equals(chartType)) {
			addPSM_PEP_PROT_Controls(options);
		} else if (FDR_VS_SCORE.equals(chartType)) {
			addFDR_VS_Score_Controls(options);
		} else if (CHR_MAPPING.equals(chartType)) {
			addAllHumanChromosomeMappingControls();
		} else if (CHR_PEPTIDES_MAPPING.equals(chartType)) {
			addAllHumanChromosomePeptideMappingControls();
		} else if (FDR.equals(chartType)) {
			addFDR_Plots_Controls();
		} else if (PEPTIDE_PRESENCY_HEATMAP.equals(chartType)) {
			addPeptidePresencyControls();
		} else if (PEPTIDE_NUM_PER_PROTEIN_MASS.equals(chartType)) {
			addLineHistogramControls(false, false);
		} else if (HUMAN_CHROMOSOME_COVERAGE.equals(chartType)) {
			addHumanChromosomeCoverageControls();
		} else if (PROTEIN_NAME_CLOUD.equals(chartType)) {
			addWordCramControls();
		} else if (PROTEIN_GROUP_TYPES.equals(chartType)) {
			addProteingGroupTypesDistributionControls(options);
		} else if (PEPTIDE_RT_COMPARISON.equals(chartType)) {
			addPeptideRTComparisonControls(options);
		} else if (PEPTIDE_RT.equals(chartType)) {
			addPeptideRTControls(options);
		} else if (SINGLE_RT_COMPARISON.equals(chartType)) {
			addSingleRTControls(options);
		} else if (PEPTIDE_COUNTING_HISTOGRAM.equals(chartType) || PEPTIDE_COUNTING_VS_SCORE.equals(chartType)) {
			addPeptideCoutingControls(options);
		}
		jPanelAddOptions.repaint();
	}

	private void addPeptideCoutingControls(String options) {
		jPanelAddOptions.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		// Just in case of proteins

		log.info("Creating list of replicates...");

		c.gridy = 0;

		if (PEPTIDE_COUNTING_VS_SCORE.equals(currentChartType)) {
			DefaultComboBoxModel peptideScoreNames = getPeptideScoreNames();
			jPanelAddOptions.add(optionsFactory.getPeptideScorePanel(peptideScoreNames), c);
		}
		if (PEPTIDE_COUNTING_HISTOGRAM.equals(currentChartType)) {
			c.gridy++;
			jPanelAddOptions.add(optionsFactory.getBinsPanel(), c);
		}
		c.gridy++;
		jPanelAddOptions.add(optionsFactory.getHistogramTypePanel(), c);
		c.gridy++;
		jPanelAddOptions.add(new JLabel("Two groups are equals if (select one option):"), c);
		// take into account just one protein per group
		c.gridy++;
		jPanelAddOptions.add(optionsFactory.getJcheckBoxOneProteinPerGroup(), c);

		c.gridy++;
		jPanelAddOptions.add(new JLabel("<html><br></html>"), c);
		c.gridy++;
		jPanelAddOptions.add(new JLabel("Select from the following checkBoxes:"), c);
		c.gridy++;

		if (options.equals(ChartManagerFrame.ONE_CHART_PER_EXPERIMENT)) {
			jPanelAddOptions.add(optionsFactory.getExperimentsCheckboxes(false, 2), c);
		} else if (options.equals(ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT)) {
			jPanelAddOptions.add(optionsFactory.getExperimentsCheckboxes(false, 2), c);
		} else if (options.equals(ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST)) {
			jPanelAddOptions.add(optionsFactory.getExperimentsCheckboxes(false, 2), c);
		} else if (options.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			jPanelAddOptions.add(optionsFactory.getReplicatesCheckboxes(false, false, 2), c);
		}

		jPanelAddOptions.repaint();
	}

	private void addSingleRTControls(String options) {
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();
		c.gridx = 0;
		c.gridy = 0;
		panel.add(jPanelAdditional1, c);

		// ////////////// ROW2
		JPanel jPanelAdditional2 = optionsFactory.getPeptideSequencesPanel(true);

		c.gridx = 0;
		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		panel.add(jPanelAdditional2, c);

		JLabel jlabel = new JLabel("(Multiple selections are allowed)");
		jlabel.setToolTipText(
				"<html>Multiple selections are allowed:<br>Press CTRL key and select more than one modification.</html>");
		c.gridy++;
		panel.add(jlabel, c);

		// /////////////// ROW5
		JCheckBox checkbox6 = optionsFactory.getShowInMinutesCheckBox();
		c.gridx = 0;
		c.gridy++;
		panel.add(checkbox6, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);
	}

	private void addPeptideRTControls(String options) {
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		JPanel jPanelAdditional1 = optionsFactory.getHistogramTypePanel();
		c.gridx = 0;
		c.gridy = 0;
		panel.add(jPanelAdditional1, c);

		// //////////////// ROW2
		JPanel jPanelAdditional2 = optionsFactory.getBinsPanel();

		c.gridy++;
		panel.add(jPanelAdditional2, c);

		final JCheckBox showInMinutesCheckBox = optionsFactory.getShowInMinutesCheckBox();
		c.gridy++;
		panel.add(showInMinutesCheckBox, c);

		final JCheckBox showTotalSeriesCheckBox = optionsFactory.getShowTotalSerieCheckBox(false);
		c.gridy++;
		panel.add(showTotalSeriesCheckBox, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	private void addPeptideRTComparisonControls(String options) {
		if (options == null)
			options = ChartManagerFrame.ONE_CHART_PER_EXPERIMENT;
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;

		// //////////////// ROW3
		JPanel jPanelAdditional5 = optionsFactory.getShowRegressionLinePanel();
		panel.add(jPanelAdditional5, c);
		// //////////////// ROW4
		JPanel jPanelAdditional6 = optionsFactory.getShowDiagonalLinePanel();
		c.gridy++;
		panel.add(jPanelAdditional6, c);

		final JCheckBox showInMinutesCheckBox = optionsFactory.getShowInMinutesCheckBox();
		c.gridy++;
		panel.add(showInMinutesCheckBox, c);

		if (!ONE_SERIES_PER_EXPERIMENT_LIST.equals(options)) {
			JLabel label = new JLabel("Select from the following checkBoxes:");
			c.gridy++;
			panel.add(label, c);
			JPanel checkboxesPanel = null;
			if (ONE_CHART_PER_EXPERIMENT.equals(options)) {
				checkboxesPanel = optionsFactory.getReplicatesCheckboxes(true, false, 2);
			} else if (ONE_SERIES_PER_EXPERIMENT.equals(options)) {
				checkboxesPanel = optionsFactory.getExperimentsCheckboxes(false, 2);
			} else if (ONE_SERIES_PER_REPLICATE.equals(options)) {
				checkboxesPanel = optionsFactory.getReplicatesCheckboxes(false, false, 2);
			}

			c.gridy++;
			panel.add(checkboxesPanel, c);
		}

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	private void addWordCramControls() {
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		c.gridx = 0;
		c.gridy = 0;

		final JLabel label24 = new JLabel(
				"<html>Words are extracted from protein descriptions.<br>The size of the words is proportional to their"
						+ "<br>ccurrences.</html>");
		panel.add(label24, c);
		c.gridy++;
		final JLabel label4 = new JLabel("Selected word:");
		panel.add(label4, c);
		c.gridy++;
		final JLabel label5 = optionsFactory.getJLabelSelectedWord();
		panel.add(label5, c);
		c.gridy++;
		final JLabel label = new JLabel("Select the font:");
		panel.add(label, c);
		c.gridy++;
		panel.add(optionsFactory.getFontComboBox(), c);
		c.gridy++;
		final JLabel label6 = new JLabel("Skip words with length less than:");
		panel.add(label6, c);
		c.gridy++;
		panel.add(optionsFactory.getMinWordLengthText(), c);
		final JLabel label3 = new JLabel("Maximum number of words to show:");
		c.gridy++;
		panel.add(label3, c);
		c.gridy++;
		panel.add(optionsFactory.getMaxNumberWordsText(), c);

		c.gridy++;
		final JLabel label2 = new JLabel("Words to skip:");
		panel.add(label2, c);
		String tooltip = "Words in this text area will not be shown in the protein cloud";
		label2.setToolTipText(tooltip);
		c.gridy++;
		final JTextArea skipWordsTextArea = optionsFactory.getSkipWordsTextArea();
		skipWordsTextArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(skipWordsTextArea);

		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		panel.add(scrollPane, c);
		skipWordsTextArea.setToolTipText(tooltip);

		c.gridy++;
		final JButton buttonReDraw = optionsFactory.getDrawWordCramButton();
		panel.add(buttonReDraw, c);

		c.gridy++;
		final JButton saveButton = optionsFactory.getSaveDrawWordCramButton();
		panel.add(saveButton, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	private void addHumanChromosomeCoverageControls() {
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		c.gridx = 0;
		c.gridy = 0;

		panel.add(optionsFactory.getPlotOrientationPanel(), c);
		c.gridy++;

		panel.add(optionsFactory.getTakeGeneFromFirstProteinCheckbox(), c);
		c.gridy++;
		panel.add(optionsFactory.getShowAsSpiderPlotCheckBox(), c);

		c.gridy++;
		panel.add(optionsFactory.getShowAsPercentageCheckBox(), c);
		c.gridy++;

		final JCheckBox showTotalSerieCheckBox = optionsFactory.getShowTotalSerieCheckBox(false);
		if (showTotalSerieCheckBox != null)
			panel.add(showTotalSerieCheckBox, c);
		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);
	}

	private void addPeptidePresencyControls() {
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);

		// /////////////// ROW1
		JCheckBox jCheckBox2 = optionsFactory.getHeatMapBinaryCheckBox(false);
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		panel.add(jCheckBox2, c);
		// /////////////// ROW2
		JPanel jPanelAdditional3 = optionsFactory.getUserPeptideListPanel(false);
		c.gridy++;
		panel.add(jPanelAdditional3, c);
		// /////////////// ROW3
		JLabel jlabel = new JLabel("<html><b>Important</b>:<br>If peptide containing modifications are <br>"
				+ "inserted here, <font color='RED'>enable the 'distinguish mod.<br>"
				+ "and unmod.' checkbox above</font>.<br><b>Examples</b>:<br>Without modifications:<br><ul><li>CSVFYGAPSK</li><li>DNQRPSGVPDR</li></ul>Containing modifications (modifications are<br>"
				+ "specified inserting the monoisotopic mass delta<br>between braquets):<br><ul><li>C(+57.02)SVFYGAPSK</li><li>DGWSAQPTC(+57.02)IK</li></ul>"
				+ "</html>");
		c.gridy++;
		panel.add(jlabel, c);
		// /////////////// ROW4
		JPanel colorScalePanel = optionsFactory.getColorScalePanel(false);
		c.gridy++;
		panel.add(colorScalePanel, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	private void addFDR_Plots_Controls() {
		final JCheckBox showTotalSerieCheckBox = optionsFactory.getShowTotalSerieCheckBox(false);
		if (ONE_SERIES_PER_EXPERIMENT.equals(getOptionChart()) && experimentList.getExperiments().size() < 2) {
			jPanelAddOptions.removeAll();
			if (showTotalSerieCheckBox != null)
				showTotalSerieCheckBox.setSelected(false);
			return;

		}

		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(10, 0, 0, 0);
		c.gridx = 0;
		c.gridy = 0;

		panel.add(optionsFactory.getFDRCheckBoxesPanel(), c);

		if (!ONE_SERIES_PER_EXPERIMENT_LIST.equals(getOptionChart())) {
			if (showTotalSerieCheckBox != null) {
				c.gridy++;
				panel.add(showTotalSerieCheckBox, c);
			}
		}
		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);
	}

	private void addFDR_VS_Score_Controls(String options) {
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(10, 0, 0, 0);
		JCheckBox showPSMCheckBox = optionsFactory.getShowPSMCheckBox();
		c.gridx = 0;
		c.gridy = 0;
		panel.add(showPSMCheckBox, c);
		JCheckBox showPeptidesCheckBox = optionsFactory.getShowPeptidesCheckBox();
		c.gridy++;
		panel.add(showPeptidesCheckBox, c);
		JCheckBox showProteinsCheckBox = optionsFactory.getShowProteinsCheckBox();
		c.gridy++;
		panel.add(showProteinsCheckBox, c);
		JCheckBox showScoreVsFDRCheckBox = optionsFactory.getShowScoreVsFDRCheckBox();
		c.gridy++;
		panel.add(showScoreVsFDRCheckBox, c);

		if (!ONE_SERIES_PER_EXPERIMENT_LIST.equals(options)) {
			JLabel label = new JLabel("Select from the following checkBoxes:");
			c.gridy++;
			panel.add(label, c);
			JPanel checkboxesPanel = null;
			if (ONE_CHART_PER_EXPERIMENT.equals(options)) {
				checkboxesPanel = optionsFactory.getReplicatesCheckboxes(true, false, 3);
			} else if (ONE_SERIES_PER_EXPERIMENT.equals(options)) {
				checkboxesPanel = optionsFactory.getExperimentsCheckboxes(false, 3);
			} else if (ONE_SERIES_PER_REPLICATE.equals(options)) {
				checkboxesPanel = optionsFactory.getReplicatesCheckboxes(false, false, 3);
			}

			if (checkboxesPanel != null) {
				c.gridx = 0;
				c.gridy++;
				panel.add(checkboxesPanel, c);
			}
		}
		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	private void addAllHumanChromosomePeptideMappingControls() {
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);

		c.gridx = 0;
		c.gridy = 0;

		JCheckBox showAsPieChartCheckBox = optionsFactory.getShowAsPieChartCheckBox();

		// /////////////// ROW1
		if (!showAsPieChartCheckBox.isSelected()) {
			JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();
			c.gridy++;

			panel.add(jPanelAdditional1, c);
		}

		c.gridy++;
		JPanel peptideOrPSMSelector = optionsFactory.getPeptideOrPSMSelector();
		panel.add(peptideOrPSMSelector, c);

		c.gridy++;
		panel.add(showAsPieChartCheckBox, c);
		showAsPieChartCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addCustomizationControls();
			}
		});

		// Filter button
		JLabel label1 = new JLabel("<html><br><br>Filter by Human taxonomy or chromosome:</html>");
		c.gridy++;
		panel.add(label1, c);
		JButton button = new JButton("Filter Human proteins");
		button.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ChartManagerFrame.this.exportHumanProteins(null);
			}
		});
		button.setToolTipText("Filter all proteins that belong to any of the Human chromosomes");
		c.gridy++;
		panel.add(button, c);

		// Filter combo
		final JComboBox chromosomesCombo = new JComboBox(GeneDistributionReader.chromosomeNames);
		chromosomesCombo.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {

			}
		});
		chromosomesCombo.setToolTipText("Chromosome names2");
		c.gridy++;
		panel.add(chromosomesCombo, c);

		JButton button2 = new JButton("Filter by chr.");
		button2.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				String chr = (String) chromosomesCombo.getSelectedItem();
				ChartManagerFrame.this.exportHumanProteins(chr);
			}
		});
		button2.setToolTipText("Filter proteins belonging to the chromosome sected in the combo-box");
		c.gridy++;
		panel.add(button2, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);
		jPanelAddOptions.repaint();
	}

	private void addAllHumanChromosomeMappingControls() {
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);

		c.gridx = 0;
		c.gridy = 0;

		JLabel label2 = new JLabel("<html><b>Note</b>: Number of genes can be different from<br>"
				+ "number of proteins, since isoforms are considered<br>" + "as different proteins<br><br></html>");
		panel.add(label2, c);

		JCheckBox showAsPieChartCheckBox = optionsFactory.getShowAsPieChartCheckBox();

		JCheckBox takeGeneFromFirstProteinCheckBox = optionsFactory.getTakeGeneFromFirstProteinCheckbox();

		// /////////////// ROW1
		if (!showAsPieChartCheckBox.isSelected()) {
			JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();
			c.gridy++;

			panel.add(jPanelAdditional1, c);
		}

		c.gridy++;
		JPanel proteinOrGeneSelector = optionsFactory.getProteinOrGeneSelector();
		panel.add(proteinOrGeneSelector, c);

		c.gridy++;
		panel.add(showAsPieChartCheckBox, c);
		showAsPieChartCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addCustomizationControls();
			}
		});
		c.gridy++;
		panel.add(takeGeneFromFirstProteinCheckBox, c);
		// Filter button
		JLabel label1 = new JLabel("<html><br><br>Filter by Human taxonomy or chromosome:</html>");
		c.gridy++;
		panel.add(label1, c);
		JButton button = new JButton("Filter Human proteins");
		button.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ChartManagerFrame.this.exportHumanProteins(null);
			}
		});
		button.setToolTipText("Filter all proteins that belong to any of the Human chromosomes");
		c.gridy++;
		panel.add(button, c);

		// Filter combo
		final JComboBox chromosomesCombo = new JComboBox(GeneDistributionReader.chromosomeNames);
		chromosomesCombo.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {

			}
		});
		chromosomesCombo.setToolTipText("Chromosome names2");
		c.gridy++;
		panel.add(chromosomesCombo, c);

		JButton button2 = new JButton("Filter by chr.");
		button2.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				String chr = (String) chromosomesCombo.getSelectedItem();
				ChartManagerFrame.this.exportHumanProteins(chr);
			}
		});
		button2.setToolTipText("Filter proteins belonging to the chromosome sected in the combo-box");
		c.gridy++;
		panel.add(button2, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);
		jPanelAddOptions.repaint();
	}

	private void addChr16MappingControls() {
		jPanelAddOptions.removeAll();
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTH;
		jPanelAddOptions.setLayout(new GridBagLayout());
		c.gridx = 0;
		c.gridy = 0;

		jPanelAddOptions.add(optionsFactory.getChr16MappingControls(), c);
	}

	protected void exportHumanProteins(String chr) {
		Set<String> filterProteinACC = GeneDistributionReader.getInstance().getProteinGeneMapping(chr).keySet();

		filterDialog.enableProteinACCFilter(filterProteinACC);
		filterDialog.applyFilters(experimentList);

	}

	private void addPSM_PEP_PROT_Controls(String options) {
		jPanelAddOptions.removeAll();
		jPanelAddOptions.setLayout(new BorderLayout());
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(10, 0, 0, 0);
		c.gridx = 0;
		c.gridy = 0;

		// do not show PSMs if FDR filter is activated because number of PSMs
		// will be equal to the number of peptides

		JCheckBox showPSMCheckBox = optionsFactory.getShowPSMCheckBox();
		panel.add(showPSMCheckBox, c);

		c.gridy++;
		JCheckBox showPeptidesCheckBox = optionsFactory.getShowPeptidesCheckBox();
		panel.add(showPeptidesCheckBox, c);

		c.gridy++;
		JCheckBox showPeptidesCheckBoxPlusCharge = optionsFactory.getShowPeptidesPlusChargeCheckBox();
		panel.add(showPeptidesCheckBoxPlusCharge, c);

		c.gridy++;
		JCheckBox showProteinsCheckBox = optionsFactory.getShowProteinsCheckBox();
		panel.add(showProteinsCheckBox, c);

		final JCheckBox showTotalSerieCheckBox = optionsFactory.getShowTotalSerieCheckBox(false);
		if (!ONE_SERIES_PER_EXPERIMENT_LIST.equals(options)
				&& !(ONE_SERIES_PER_EXPERIMENT.equals(options) && experimentList.getExperiments().size() < 2)) {
			c.gridy++;
			JCheckBox showTotalSerie = showTotalSerieCheckBox;
			if (showTotalSerie != null) {
				showTotalSerie.setEnabled(true);
				panel.add(showTotalSerie, c);
			}
		} else {
			if (showTotalSerieCheckBox != null)
				showTotalSerieCheckBox.setSelected(false);
		}

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);
		jPanelAddOptions.repaint();

	}

	private void addDeltaOverZControls(String options) {
		if (options == null)
			options = ChartManagerFrame.ONE_CHART_PER_EXPERIMENT;
		jPanelAddOptions.removeAll();
		jPanelAddOptions.setLayout(new BorderLayout());
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(10, 0, 0, 0);
		JPanel showRegressionLinePanel = optionsFactory.getShowRegressionLinePanel();
		c.gridx = 0;
		c.gridy = 0;
		panel.add(showRegressionLinePanel, c);

		if (!ONE_SERIES_PER_EXPERIMENT_LIST.equals(options)) {
			JLabel label = new JLabel("Select from the following checkBoxes:");
			c.gridy++;
			panel.add(label, c);
			JPanel checkboxesPanel = null;
			if (ONE_CHART_PER_EXPERIMENT.equals(options)) {
				checkboxesPanel = optionsFactory.getReplicatesCheckboxes(true, false, 3);
			} else if (ONE_SERIES_PER_EXPERIMENT.equals(options)) {
				checkboxesPanel = optionsFactory.getExperimentsCheckboxes(false, 3);
			} else if (ONE_SERIES_PER_REPLICATE.equals(options)) {
				checkboxesPanel = optionsFactory.getReplicatesCheckboxes(false, false, 3);
			}

			if (checkboxesPanel != null) {
				c.gridx = 0;
				c.gridy++;
				panel.add(checkboxesPanel, c);
			}
		}

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);
	}

	private void addPeptideNumberInProteinsControls() {
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();
		c.gridx = 0;
		c.gridy = 0;

		panel.add(jPanelAdditional1, c);
		// /////////////// ROW2
		JCheckBox checkbox2 = optionsFactory.getShowAsStackedChartCheckBox();
		c.gridx = 0;
		c.gridy = 1;

		panel.add(checkbox2, c);
		// /////////////// ROW3
		JCheckBox checkbox3 = optionsFactory.getShowAsPercentageCheckBox();
		c.gridx = 0;
		c.gridy = 2;

		panel.add(checkbox3, c);
		// /////////////// ROW6
		JCheckBox jCheckBox6 = optionsFactory.getShowTotalSerieCheckBox(true);
		if (jCheckBox6 != null && !isOccurrenceFilterEnabled()) {
			c.gridx = 0;
			c.gridy = 5;
			panel.add(jCheckBox6, c);
		}
		// /////////////// ROW7
		JCheckBox jCheckBox7 = optionsFactory.getShowDifferentIdentificationsCheckBox();
		c.gridx = 0;
		c.gridy = 6;
		panel.add(jCheckBox7, c);
		// /////////////// ROW7
		JPanel jpanelMax = optionsFactory.getMaximumNumOccurrence("maximum", 30, 5);
		c.gridx = 0;
		c.gridy = 7;
		panel.add(jpanelMax, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	private void addSingleHitProteinControls() {
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();
		c.gridx = 0;
		c.gridy = 0;

		panel.add(jPanelAdditional1, c);
		// /////////////// ROW2
		JCheckBox checkbox2 = optionsFactory.getShowAsStackedChartCheckBox();
		c.gridx = 0;
		c.gridy = 1;

		panel.add(checkbox2, c);

		// /////////////// ROW4
		JCheckBox jCheckBox = optionsFactory.getShowAsPieChartCheckBox();
		c.gridx = 0;
		c.gridy = 3;
		panel.add(jCheckBox, c);

		// /////////////// ROW6
		JCheckBox jCheckBox6 = optionsFactory.getShowTotalSerieCheckBox(true);
		if (jCheckBox6 != null && !isOccurrenceFilterEnabled()) {
			c.gridx = 0;
			c.gridy = 5;
			panel.add(jCheckBox6, c);
		}
		// /////////////// ROW7
		JCheckBox jCheckBox7 = optionsFactory.getShowDifferentIdentificationsCheckBox();
		c.gridx = 0;
		c.gridy = 6;
		panel.add(jCheckBox7, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	private void addProteinCoverageHistogramBarControls() {
		jPanelAddOptions.removeAll();

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(optionsFactory.getPlotOrientationPanel(), BorderLayout.NORTH);
	}

	private void addMissedCleavageDistributionControls(String options) {
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();

		c.gridx = 0;
		c.gridy = 0;
		panel.add(jPanelAdditional1, c);

		// /////////////// ROW3
		JPanel jPanelAdditional3 = optionsFactory.getMaximumNumOccurrence("Maximum occurrence:", 10, 4);
		c.gridx = 0;
		c.gridy++;
		panel.add(jPanelAdditional3, c);

		// /////////////// ROW4
		JCheckBox checkbox4 = optionsFactory.getShowAsPercentageCheckBox();
		c.gridx = 0;
		c.gridy++;
		panel.add(checkbox4, c);
		// /////////////// ROW5
		JCheckBox checkbox5 = optionsFactory.getShowAsStackedChartCheckBox();
		c.gridx = 0;
		c.gridy++;
		panel.add(checkbox5, c);

		if (!ONE_SERIES_PER_EXPERIMENT_LIST.equals(options)) {
			JCheckBox checkbox6 = optionsFactory.getShowTotalSerieCheckBox(true);
			if (checkbox6 != null) {
				c.gridy++;
				panel.add(checkbox6, c);
			}
		}

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	private void addProteingGroupTypesDistributionControls(String options) {
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();

		c.gridx = 0;
		c.gridy = 0;
		panel.add(jPanelAdditional1, c);

		// /////////////// ROW4
		JCheckBox checkbox4 = optionsFactory.getShowAsPercentageCheckBox();
		c.gridx = 0;
		c.gridy++;
		panel.add(checkbox4, c);
		// /////////////// ROW5
		JCheckBox checkbox5 = optionsFactory.getShowAsStackedChartCheckBox();
		c.gridx = 0;
		c.gridy++;
		panel.add(checkbox5, c);

		if (!ONE_SERIES_PER_EXPERIMENT_LIST.equals(options)) {
			JCheckBox checkbox6 = optionsFactory.getShowTotalSerieCheckBox(true);
			if (checkbox6 != null) {
				c.gridy++;
				panel.add(checkbox6, c);
			}
		}

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	private void addPeptideLengthDistributionControls(String options) {
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();

		c.gridx = 0;
		c.gridy = 0;
		panel.add(jPanelAdditional1, c);
		// /////////////// ROW4
		JCheckBox checkbox4 = optionsFactory.getShowAsPercentageCheckBox();
		c.gridx = 0;
		c.gridy++;
		panel.add(checkbox4, c);
		// /////////////// ROW5
		JCheckBox checkbox5 = optionsFactory.getShowAsStackedChartCheckBox();
		c.gridx = 0;
		c.gridy++;
		panel.add(checkbox5, c);

		if (!ONE_SERIES_PER_EXPERIMENT_LIST.equals(options)) {
			JCheckBox checkbox6 = optionsFactory.getShowTotalSerieCheckBox(true);
			if (checkbox6 != null) {
				c.gridy++;
				panel.add(checkbox6, c);
			}
		}
		// /////////////// ROW7
		JPanel jpanelMin = optionsFactory.getMinimumNumOccurrence("minimum", 40, 7);
		c.gridy++;
		panel.add(jpanelMin, c);

		JPanel jpanelMax = optionsFactory.getMaximumNumOccurrence("maximum", 40, 20);
		c.gridy++;
		panel.add(jpanelMax, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	private void addSensitivitySpecificityControls() {
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		JPanel jPanelAdditional1 = optionsFactory.getProteinsInSamplePanel();
		c.gridx = 0;
		c.gridy = 0;
		panel.add(jPanelAdditional1, c);
		// /////////////// ROW2
		c.gridx = 0;
		c.gridy = 1;
		panel.add(optionsFactory.getCheckBoxSensitivity(), c);

		// /////////////// ROW4
		c.gridx = 0;
		c.gridy = 2;
		panel.add(optionsFactory.getCheckBoxAccuracy(), c);
		// /////////////// ROW5

		c.gridx = 0;
		c.gridy = 3;
		panel.add(optionsFactory.getCheckBoxSpecificity(), c);
		// /////////////// ROW6

		c.gridx = 0;
		c.gridy = 4;
		panel.add(optionsFactory.getCheckBoxPrecision(), c);
		// /////////////// ROW6

		c.gridx = 0;
		c.gridy = 5;
		panel.add(optionsFactory.getCheckBoxNPV(), c);
		// /////////////// ROW7
		c.gridx = 0;
		c.gridy = 6;
		panel.add(optionsFactory.getCheckBoxFDR(), c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	private void addRepeatibilityControls() {
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		JCheckBox checkbox = optionsFactory.getShowAsPercentageCheckBox();
		c.gridx = 0;
		c.gridy = 0;
		panel.add(checkbox, c);

		// /////////////// ROW2
		JPanel jPanelAdditional3 = optionsFactory.getMaximumNumOccurrence("Maximum occurrence:", 40, 4);
		c.gridx = 0;
		c.gridy = 1;
		panel.add(jPanelAdditional3, c);

		// /////////////// ROW3
		JPanel jPanelAdditional4 = optionsFactory.getOverReplicatesPanel();

		c.gridx = 0;
		c.gridy = 2;
		panel.add(jPanelAdditional4, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	private void addPeptideMonitoringControls() {
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();
		c.gridx = 0;
		c.gridy = 0;
		panel.add(jPanelAdditional1, c);
		// ////////////// ROW2
		JPanel jPanelAdditional2 = optionsFactory.getPeptideSequencesPanel(distinguishModifiedPeptides());

		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.BOTH;
		panel.add(jPanelAdditional2, c);

		// /////////////// ROW3
		JPanel jPanelAdditional3 = optionsFactory.getUserPeptideListPanel(true);

		c.gridx = 0;
		c.gridy = 2;
		c.fill = GridBagConstraints.BOTH;
		panel.add(jPanelAdditional3, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	public String[] getPeptidesFromExperiments(boolean distinguishModPep) {
		log.info("Getting peptide sequences");
		if (experimentList != null) {

			final Collection<PeptideOccurrence> peptideOccurrences = experimentList
					.getPeptideChargeOccurrenceList(distinguishModPep).values();
			if (peptideOccurrences != null && !peptideOccurrences.isEmpty()) {
				List<PeptideOccurrence> occurrenceList = new ArrayList<PeptideOccurrence>();
				for (PeptideOccurrence identificationOccurrence : peptideOccurrences) {
					occurrenceList.add(identificationOccurrence);
				}
				log.info("There is " + occurrenceList.size() + " peptide sequences");
				String[] ret = new String[occurrenceList.size()];
				int i = 0;
				SorterUtil.sortPeptideOcurrencesBySequence(occurrenceList);
				for (PeptideOccurrence identificationOccurrence : occurrenceList) {

					ret[i] = identificationOccurrence.getKey();

					i++;
				}

				// return DatasetFactory.toSortedArray(ret);
				return ret;
			}
		}
		return null;
	}

	public String[] getPeptidesPlusChargeFromExperiments(boolean distinguishModPep) {
		log.info("Getting peptide sequences");
		if (experimentList != null) {

			final Collection<PeptideOccurrence> peptideOccurrences = experimentList
					.getPeptideChargeOccurrenceList(distinguishModPep).values();
			if (peptideOccurrences != null && !peptideOccurrences.isEmpty()) {
				List<PeptideOccurrence> occurrenceList = new ArrayList<PeptideOccurrence>();
				for (PeptideOccurrence identificationOccurrence : peptideOccurrences) {
					occurrenceList.add(identificationOccurrence);
				}
				log.info("There is " + occurrenceList.size() + " peptide+charge sequences");
				String[] ret = new String[occurrenceList.size()];
				int i = 0;
				SorterUtil.sortPeptideOcurrencesBySequence(occurrenceList);
				for (PeptideOccurrence identificationOccurrence : occurrenceList) {

					ret[i] = identificationOccurrence.getKey();

					i++;
				}

				// return DatasetFactory.toSortedArray(ret);
				return ret;
			}
		}
		return null;
	}

	private void addPeptideModificationControls() {
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();
		c.gridx = 0;
		c.gridy = 0;
		panel.add(jPanelAdditional1, c);

		// ////////////// ROW2
		JPanel jPanelAdditional2 = optionsFactory.getModificationListPanel();

		c.gridx = 0;
		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		panel.add(jPanelAdditional2, c);

		JLabel jlabel = new JLabel("(Multiple selections are allowed)");
		jlabel.setToolTipText(
				"<html>Multiple selections are allowed:<br>Press CTRL key and select more than one modification.</html>");
		c.gridy++;
		panel.add(jlabel, c);

		// /////////////// ROW4
		JCheckBox checkBox4 = optionsFactory.getShowAsStackedChartCheckBox();
		c.gridx = 0;
		c.gridy++;
		panel.add(checkBox4, c);
		// /////////////// ROW5
		JCheckBox checkbox5 = optionsFactory.getShowAsPercentageCheckBox();
		c.gridx = 0;
		c.gridy++;
		panel.add(checkbox5, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);
	}

	private void addScoreComparisonControls(String options) {
		if (options == null)
			options = ChartManagerFrame.ONE_CHART_PER_EXPERIMENT;
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		if (currentChartType.equals(PROTEIN_SCORE_COMPARISON)) {
			// //////////////// ROW1
			final DefaultComboBoxModel proteinScoreNames = getProteinScoreNames();
			if (proteinScoreNames != null) {
				JPanel jPanelAdditional3 = optionsFactory.getProteinScorePanel(proteinScoreNames);

				panel.add(jPanelAdditional3, c);
			}
		}

		if (currentChartType.equals(PEPTIDE_SCORE_COMPARISON)) {
			// //////////////// ROW2
			final DefaultComboBoxModel peptideScoreNames = getPeptideScoreNames();
			if (peptideScoreNames != null) {
				JPanel jPanelAdditional4 = optionsFactory.getPeptideScorePanel(peptideScoreNames);
				c.gridy++;
				panel.add(jPanelAdditional4, c);
			}
		}
		// //////////////// ROW
		JCheckBox jCheckApplyLog = optionsFactory.getApplyLogCheckBox();
		c.gridy++;
		panel.add(jCheckApplyLog, c);

		// //////////////// ROW3
		JPanel jPanelAdditional5 = optionsFactory.getShowRegressionLinePanel();
		c.gridy++;
		panel.add(jPanelAdditional5, c);
		// //////////////// ROW4
		JPanel jPanelAdditional6 = optionsFactory.getShowDiagonalLinePanel();
		c.gridy++;
		panel.add(jPanelAdditional6, c);
		// /
		JCheckBox separateDecoyHits = optionsFactory.getSeparatedDecoyHitsCheckBox();
		separateDecoyHits.setEnabled(experimentList.getFDRFilter() != null);
		c.gridy++;
		panel.add(separateDecoyHits, c);
		if (!ONE_SERIES_PER_EXPERIMENT_LIST.equals(options)) {
			JLabel label = new JLabel("Select from the following checkBoxes:");
			c.gridy++;
			panel.add(label, c);
			JPanel checkboxesPanel = null;
			if (ONE_CHART_PER_EXPERIMENT.equals(options)) {
				checkboxesPanel = optionsFactory.getReplicatesCheckboxes(true, false, 3);
			} else if (ONE_SERIES_PER_EXPERIMENT.equals(options)) {
				checkboxesPanel = optionsFactory.getExperimentsCheckboxes(false, 3);
			} else if (ONE_SERIES_PER_REPLICATE.equals(options)) {
				checkboxesPanel = optionsFactory.getReplicatesCheckboxes(false, false, 3);
			}

			c.gridy++;
			panel.add(checkboxesPanel, c);
		}
		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	private void addHeatMapControls() {
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		JPanel jPanelAdditional1 = optionsFactory.getColorScalePanel(true);
		c.gridx = 0;
		c.gridy = 0;
		panel.add(jPanelAdditional1, c);

		// //////////////// ROW2
		JPanel jPanelAdditional2 = optionsFactory.getHeatMapThresholdPanel();
		c.gridy++;
		panel.add(jPanelAdditional2, c);

		// //////////////// ROW3
		if (currentChartType.equals(PROTEIN_NUMBER_OF_PEPTIDES_HEATMAP)) {
			JCheckBox checkbox = optionsFactory.getIsPSMorPeptideCheckBox();
			c.gridy++;
			panel.add(checkbox, c);
		}

		JButton jbuttonSave = optionsFactory.getSaveButton();
		c.gridy++;
		panel.add(jbuttonSave, c);
		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	public void addMinMaxHeatMapValues(double[][] dataset) {

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (int i = 0; i < dataset.length; i++) {
			for (int j = 0; j < dataset[i].length; j++) {
				double d = dataset[i][j];
				if (d < min)
					min = d;
				if (d > max)
					max = d;
			}
		}
		if (min == 0.0)
			min = 0;

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(10, 0, 0, 0);
		String minString = String.valueOf(min);
		if (minString.contains(".")) {
			minString = minString.split("\\.")[0];
		}
		String maxString = String.valueOf(max);
		if (maxString.contains(".")) {
			maxString = maxString.split("\\.")[0];
		}

		JLabel label = new JLabel("<html>Parameters:<br>min= " + Integer.valueOf(minString) + "<br>max= "
				+ Integer.valueOf(maxString) + "<br>" + "Number of items= " + dataset.length + "</html>");
		label.setToolTipText(
				"<html>These are the minimum value that corresponds with the 'low color',<br>and the maximum value that corresponds with the 'high color'.<br>"
						+ "The number of items is the number of proteins or peptides that are showed in the chart, <br>"
						+ "that is that have an occurrence more than threshold over the identification sets.</html>");
		panel.add(label, c);
		jPanelAddOptions.add(panel);

	}

	private void addOverlapingControls(String options, Integer numberOfSelectedCheckBoxes,
			boolean selectAllCheckBoxes) {
		if (options == null)
			options = ChartManagerFrame.ONE_CHART_PER_EXPERIMENT;
		if (ONE_CHART_PER_EXPERIMENT.equals(options)) {
			addOverlappingControlsChartPerExperiment(numberOfSelectedCheckBoxes, selectAllCheckBoxes);
		} else if (ONE_SERIES_PER_EXPERIMENT.equals(options)) {
			addOverlappingControlsSeriePerExperiment(numberOfSelectedCheckBoxes, selectAllCheckBoxes);
		} else if (ONE_SERIES_PER_REPLICATE.equals(options)) {
			addOverlappingControlsSeriePerReplicate(numberOfSelectedCheckBoxes, selectAllCheckBoxes);
		}
	}

	private void addOverlappingControlsChartPerExperiment(Integer numberOfSelectedCheckBoxes,
			boolean selectAllCheckBoxes) {
		jPanelAddOptions.setLayout(new BoxLayout(jPanelAddOptions, BoxLayout.PAGE_AXIS));

		// Just in case of proteins

		log.info("Creating list of replicates...");
		if (numberOfSelectedCheckBoxes == null)
			numberOfSelectedCheckBoxes = Integer.MAX_VALUE;

		if (numberOfSelectedCheckBoxes.equals(Integer.MAX_VALUE)) {
			JLabel label = new JLabel(
					"<html>Number of identifications that are detected <b>just</b> <br>in each set.</html>");
			jPanelAddOptions.add(label);
		}
		if (PROTEIN_OVERLAPING.equals(currentChartType) || EXCLUSIVE_PROTEIN_NUMBER.equals(currentChartType)
				|| PEPTIDE_COUNTING_HISTOGRAM.equals(currentChartType)) {
			jPanelAddOptions.add(new JLabel("Two groups are equals if (select one option):"));
			// take into account just one protein per group
			jPanelAddOptions.add(optionsFactory.getJcheckBoxOneProteinPerGroup());
		}
		if (EXCLUSIVE_PROTEIN_NUMBER.equals(currentChartType) || EXCLUSIVE_PEPTIDE_NUMBER.equals(currentChartType)) {
			jPanelAddOptions.add(optionsFactory.getAccumulativeTrendCheckBox());
		}
		if (PEPTIDE_COUNTING_HISTOGRAM.equals(currentChartType)) {
			jPanelAddOptions.add(optionsFactory.getBinsPanel());
			jPanelAddOptions.add(optionsFactory.getHistogramTypePanel());
		}
		jPanelAddOptions.add(new JLabel("<html><br></html>"));
		jPanelAddOptions.add(new JLabel("Select from the following checkBoxes:"));
		jPanelAddOptions.add(optionsFactory.getReplicatesCheckboxes(true, selectAllCheckBoxes, 2));

		if (!numberOfSelectedCheckBoxes.equals(Integer.MAX_VALUE)
				&& !PEPTIDE_COUNTING_HISTOGRAM.equals(currentChartType)) {
			// Add save image button
			JButton jbuttonSave = new JButton("Save image(s)");
			jbuttonSave.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					saveOverlappingImages();
				}
			});
			jPanelAddOptions.add(jbuttonSave);

			jPanelAddOptions.add(optionsFactory.getJLabelIntersectionsText());
		}

		jPanelAddOptions.repaint();
	}

	private void addOverlappingControlsSeriePerReplicate(Integer numberOfSelectedCheckBoxes,
			boolean selectAllCheckBoxes) {
		jPanelAddOptions.setLayout(new BoxLayout(jPanelAddOptions, BoxLayout.PAGE_AXIS));

		log.info("Creating list of replicates...");

		if (numberOfSelectedCheckBoxes == null)
			numberOfSelectedCheckBoxes = Integer.MAX_VALUE;
		if (numberOfSelectedCheckBoxes.equals(Integer.MAX_VALUE)) {
			JLabel label = new JLabel(
					"<html>Number of identifications that are detected<br><b>just</b> in each set.</html>");
			jPanelAddOptions.add(label);
		}
		// Just in case of proteins
		if (PROTEIN_OVERLAPING.equals(currentChartType) || EXCLUSIVE_PROTEIN_NUMBER.equals(currentChartType)
				|| PEPTIDE_COUNTING_HISTOGRAM.equals(currentChartType)) {
			jPanelAddOptions.add(new JLabel("Two groups are equals if (select one option):"));
			// take into account just one protein per group
			jPanelAddOptions.add(optionsFactory.getJcheckBoxOneProteinPerGroup());
		}
		if (EXCLUSIVE_PROTEIN_NUMBER.equals(currentChartType) || EXCLUSIVE_PEPTIDE_NUMBER.equals(currentChartType)) {
			jPanelAddOptions.add(optionsFactory.getAccumulativeTrendCheckBox());
		}

		if (PEPTIDE_COUNTING_HISTOGRAM.equals(currentChartType)) {
			jPanelAddOptions.add(optionsFactory.getBinsPanel());
			jPanelAddOptions.add(optionsFactory.getHistogramTypePanel());
		}

		jPanelAddOptions.add(new JLabel("<html><br></html>"));
		jPanelAddOptions.add(new JLabel("Select from the following checkBoxes:"));

		jPanelAddOptions.add(optionsFactory.getReplicatesCheckboxes(false, selectAllCheckBoxes, 2));
		if (!numberOfSelectedCheckBoxes.equals(Integer.MAX_VALUE)
				&& !PEPTIDE_COUNTING_HISTOGRAM.equals(currentChartType)) {
			// Add save image button
			JButton jbuttonSave = new JButton("Save image");
			jbuttonSave.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					saveOverlappingImage();
				}
			});
			jPanelAddOptions.add(jbuttonSave);
			jPanelAddOptions.add(optionsFactory.getJLabelIntersectionsText());
		}

		jPanelAddOptions.repaint();
	}

	private void addOverlappingControlsSeriePerExperiment(Integer numberOfSelectedCheckBoxes,
			boolean selectAllCheckBoxes) {
		jPanelAddOptions.setLayout(new BoxLayout(jPanelAddOptions, BoxLayout.PAGE_AXIS));

		log.info("Creating list of replicates...");
		if (numberOfSelectedCheckBoxes == null)
			numberOfSelectedCheckBoxes = Integer.MAX_VALUE;
		if (numberOfSelectedCheckBoxes.equals(Integer.MAX_VALUE)) {
			JLabel label = new JLabel(
					"<html>Number of identifications that are detected<br><b>just</b> in each set.</html>");
			jPanelAddOptions.add(label);
		}

		// Just in case of proteins
		if (PROTEIN_OVERLAPING.equals(currentChartType) || EXCLUSIVE_PROTEIN_NUMBER.equals(currentChartType)
				|| PEPTIDE_COUNTING_HISTOGRAM.equals(currentChartType)) {
			jPanelAddOptions.add(new JLabel("Two groups are equals if (select one option):"));
			// take into account just one protein per group
			jPanelAddOptions.add(optionsFactory.getJcheckBoxOneProteinPerGroup());
		}
		if (EXCLUSIVE_PROTEIN_NUMBER.equals(currentChartType) || EXCLUSIVE_PEPTIDE_NUMBER.equals(currentChartType)) {
			jPanelAddOptions.add(optionsFactory.getAccumulativeTrendCheckBox());
		}

		if (PEPTIDE_COUNTING_HISTOGRAM.equals(currentChartType)) {
			jPanelAddOptions.add(optionsFactory.getBinsPanel());
			jPanelAddOptions.add(optionsFactory.getHistogramTypePanel());
		}

		jPanelAddOptions.add(new JLabel("<html><br></html>"));
		jPanelAddOptions.add(new JLabel("Select from the following checkBoxes:"));
		jPanelAddOptions.add(optionsFactory.getExperimentsCheckboxes(selectAllCheckBoxes, 3));
		if (!numberOfSelectedCheckBoxes.equals(Integer.MAX_VALUE)
				&& !PEPTIDE_COUNTING_HISTOGRAM.equals(currentChartType)) {
			// Add save image button
			JButton jbuttonSave = new JButton("Save image");
			jbuttonSave.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					saveOverlappingImage();
				}
			});
			jPanelAddOptions.add(jbuttonSave);
			jPanelAddOptions.add(optionsFactory.getJLabelIntersectionsText());
		}

		jPanelAddOptions.repaint();
	}

	public void saveHeatMapImage() {
		log.info("Saving heatmap image");
		String error = "";
		File file = null;
		try {
			final Component component = jPanelChart.getComponent(0);
			if (component instanceof JScrollPane) {
				JScrollPane jpanel = (JScrollPane) component;
				final Component component2 = jpanel.getComponent(0);
				if (component2 instanceof JViewport) {
					JViewport viewPort = (JViewport) component2;
					final Component component3 = viewPort.getComponent(0);
					if (component3 instanceof JLabel) {
						JLabel label = (JLabel) component3;
						final ImageIcon icon = (ImageIcon) label.getIcon();
						final Image image = icon.getImage();
						JFileChooser fileChooser = new JFileChooser();
						fileChooser.addChoosableFileFilter(new ExtensionFileFilter("JPG images", "jpg"));
						int retVal = fileChooser.showSaveDialog(this);

						if (retVal == JFileChooser.APPROVE_OPTION) {
							file = fileChooser.getSelectedFile();

							String path = ImageUtils.saveImage(image, file);
							appendStatus("Image file saved to:" + path);
						}
					}
					return;
				}
			} else if (component instanceof JPanel) {
				JPanel jpanel = (JPanel) component;
				for (Component component2 : jpanel.getComponents()) {
					// if (component2 instanceof JPanel) {
					// JPanel jpanel2 = (JPanel) component2;
					final Component component3 = jpanel.getComponent(0);
					if (component3 instanceof JLabel) {
						JLabel label = (JLabel) component3;
						final ImageIcon icon = (ImageIcon) label.getIcon();
						final Image image = icon.getImage();
						JFileChooser fileChooser = new JFileChooser();
						fileChooser.addChoosableFileFilter(new ExtensionFileFilter("JPG images", "jpg"));
						int retVal = fileChooser.showSaveDialog(this);

						if (retVal == JFileChooser.APPROVE_OPTION) {
							file = fileChooser.getSelectedFile();

							String path = ImageUtils.saveImage(image, file);
							appendStatus("Image file saved to:" + path);
						}
					}

				}
				return;
			}
		} catch (IOException e) {
			error = e.getMessage();
			e.printStackTrace();
		} catch (Exception e) {
			error = e.getMessage();
			e.printStackTrace();
		}
		if (file != null)
			appendStatus("Error saving image to file: " + file.getAbsolutePath());
		else
			appendStatus("Error saving image to file");
		appendStatus(error);
	}

	protected void saveOverlappingImages() {
		log.info("Saving overlapping image");
		String error = "";
		File file = null;
		JFileChooser fileChooser = new JFileChooser();
		try {
			final Component component = jPanelChart.getComponent(0);
			if (component instanceof JPanel) {
				JPanel jpanel = (JPanel) component;
				for (Component component2 : jpanel.getComponents()) {
					if (component2 instanceof JPanel) {
						JPanel jpanel2 = (JPanel) component2;
						final Component component3 = jpanel2.getComponent(0);
						if (component3 instanceof JLabel) {
							JLabel label = (JLabel) component3;
							final ImageIcon icon = (ImageIcon) label.getIcon();
							final Image image = icon.getImage();

							fileChooser.addChoosableFileFilter(new ExtensionFileFilter("JPG images", "jpg"));
							int retVal = fileChooser.showSaveDialog(this);

							if (retVal == JFileChooser.APPROVE_OPTION) {
								file = fileChooser.getSelectedFile();

								String path = ImageUtils.saveImage(image, file);
								appendStatus("Image file saved to:" + path);
							}
						}

					}
				}
				return;
			}
		} catch (IOException e) {
			error = e.getMessage();
			e.printStackTrace();
		} catch (Exception e) {
			error = e.getMessage();
			e.printStackTrace();
		}
		if (file != null)
			appendStatus("Error saving image to file: " + file.getAbsolutePath());
		else
			appendStatus("Error saving image to file");
		appendStatus(error);
	}

	protected void saveOverlappingImage() {
		log.info("Saving overlapping image");
		String error = "";
		File file = null;
		try {
			final Component component = jPanelChart.getComponent(0);
			if (component instanceof JPanel) {
				JPanel jpanel = (JPanel) component;
				final Component component2 = jpanel.getComponent(0);
				if (component2 instanceof JLabel) {
					JLabel label = (JLabel) component2;
					final ImageIcon icon = (ImageIcon) label.getIcon();
					final Image image = icon.getImage();
					JFileChooser fileChooser = new JFileChooser(currentFolder);
					fileChooser.addChoosableFileFilter(new ExtensionFileFilter("JPG images", "jpg"));
					int retVal = fileChooser.showSaveDialog(this);

					if (retVal == JFileChooser.APPROVE_OPTION) {
						file = fileChooser.getSelectedFile();
						currentFolder = file.getParentFile();
						String path = ImageUtils.saveImage(image, file);
						appendStatus("Image file saved to:" + path);
					}
					return;
				}
			}
		} catch (IOException e) {
			error = e.getMessage();
			e.printStackTrace();
		} catch (Exception e) {
			error = e.getMessage();
			e.printStackTrace();
		}
		if (file != null)
			appendStatus("Error saving image to file: " + file.getAbsolutePath());
		else
			appendStatus("Error saving image to file");
		appendStatus(error);
	}

	private void addLineHistogramControls(boolean showScoreNames, boolean addMOverZComboBox) {
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		JPanel jPanelAdditional1 = optionsFactory.getHistogramTypePanel();
		c.gridx = 0;
		c.gridy = 0;
		panel.add(jPanelAdditional1, c);

		// //////////////// ROW2
		JPanel jPanelAdditional2 = optionsFactory.getBinsPanel();

		c.gridy++;
		panel.add(jPanelAdditional2, c);

		if (showScoreNames) {
			if (currentChartType.contains("rotein")) {
				// //////////////// ROW3
				final DefaultComboBoxModel proteinScoreNames = getProteinScoreNames();
				if (proteinScoreNames != null) {
					JPanel jPanelAdditional3 = optionsFactory.getProteinScorePanel(proteinScoreNames);
					c.gridy++;
					panel.add(jPanelAdditional3, c);
				}
			}
			if (currentChartType.contains("eptide")) {
				// //////////////// ROW4
				final DefaultComboBoxModel peptideScoreNames = getPeptideScoreNames();
				if (peptideScoreNames != null) {
					JPanel jPanelAdditional4 = optionsFactory.getPeptideScorePanel(peptideScoreNames);
					c.gridy++;
					panel.add(jPanelAdditional4, c);

				}
			}
			// //////////////// ROW
			JCheckBox jCheckApplyLog = optionsFactory.getApplyLogCheckBox();
			c.gridy++;
			panel.add(jCheckApplyLog, c);
		}
		if (addMOverZComboBox) {
			// //////////////// ROW5
			JPanel jPanelAdditional3 = optionsFactory.getMOverZPanel();
			c.gridy++;
			panel.add(jPanelAdditional3, c);
		}

		String option = (String) jComboBoxChartOptions.getSelectedItem();
		if (!option.equals(ONE_SERIES_PER_EXPERIMENT_LIST)) {
			JCheckBox showTotalSerieCheckBox = optionsFactory.getShowTotalSerieCheckBox(false);
			if (showTotalSerieCheckBox != null) {
				c.gridy++;
				panel.add(showTotalSerieCheckBox, c);
			}
		}

		if (currentChartType.equals(PEPTIDE_SCORE_DISTRIBUTION)
				|| currentChartType.equals(PROTEIN_SCORE_DISTRIBUTION)) {
			JCheckBox separateDecoyHits = optionsFactory.getSeparatedDecoyHitsCheckBox();
			separateDecoyHits.setEnabled(experimentList.getFDRFilter() != null);
			c.gridy++;
			panel.add(separateDecoyHits, c);
		}

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);
	}

	private void addHistogramBarControls() {
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();
		c.gridx = 0;
		c.gridy = 0;

		panel.add(jPanelAdditional1, c);
		// /////////////// ROW2
		JCheckBox checkbox2 = optionsFactory.getShowAsStackedChartCheckBox();
		c.gridx = 0;
		c.gridy = 1;

		panel.add(checkbox2, c);
		// /////////////// ROW3
		JCheckBox checkbox3 = optionsFactory.getShowAsPercentageCheckBox();
		c.gridx = 0;
		c.gridy = 2;

		panel.add(checkbox3, c);
		// /////////////// ROW4
		JCheckBox jCheckBox = optionsFactory.getShowAsPieChartCheckBox();
		c.gridx = 0;
		c.gridy = 3;
		panel.add(jCheckBox, c);
		// /////////////// ROW5
		String option = (String) jComboBoxChartOptions.getSelectedItem();
		if (ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			JCheckBox jCheckBox2 = optionsFactory.getShowAverageOverReplicatesCheckBox();
			c.gridx = 0;
			c.gridy = 4;
			panel.add(jCheckBox2, c);
		}
		// /////////////// ROW6
		JCheckBox jCheckBox6 = optionsFactory.getShowTotalSerieCheckBox(true);
		if (jCheckBox6 != null && !isOccurrenceFilterEnabled()) {
			c.gridx = 0;
			c.gridy = 5;
			panel.add(jCheckBox6, c);
		}
		// /////////////// ROW7
		JCheckBox jCheckBox7 = optionsFactory.getShowDifferentIdentificationsCheckBox();
		c.gridx = 0;
		c.gridy = 6;
		panel.add(jCheckBox7, c);
		// /////////////// ROW7
		JCheckBox jCheckBox8 = optionsFactory.getShowTotalVersusDifferentCheckBox();
		c.gridx = 0;
		c.gridy = 7;
		panel.add(jCheckBox8, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);
	}

	private void addChargeHistogramBarControls() {
		jPanelAddOptions.removeAll();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();
		c.gridx = 0;
		c.gridy = 0;

		panel.add(jPanelAdditional1, c);
		// /////////////// ROW2
		JCheckBox checkbox2 = optionsFactory.getShowAsStackedChartCheckBox();
		c.gridx = 0;
		c.gridy = 1;

		panel.add(checkbox2, c);
		// /////////////// ROW3
		JCheckBox checkbox3 = optionsFactory.getShowAsPercentageCheckBox();
		c.gridx = 0;
		c.gridy = 2;

		panel.add(checkbox3, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);
	}

	void updateControlStates() {
		String chartType = currentChartType;
		// String options = (String)
		// this.jComboBoxChartOptions.getSelectedItem();
		// TODO

		// ENABLE PROTEIN AND DISABLE PEPTIDE CONTROLS
		if (PROTEIN_SCORE_COMPARISON.equals(chartType) || PROTEIN_SCORE_DISTRIBUTION.equals(chartType)) {
			optionsFactory.enablePeptideScoreNameControls(false);
			optionsFactory.enableProteinScoreNameControls(true);

			// ENABLE PEPTIDE AND DISABLE PROTEIN CONTROLS
		} else if (PEPTIDE_SCORE_COMPARISON.equals(chartType) || PEPTIDE_SCORE_DISTRIBUTION.equals(chartType)
				|| FDR.equals(chartType) || PEPTIDE_REPEATABILITY.equals(chartType)) {
			optionsFactory.enablePeptideScoreNameControls(true);
			optionsFactory.enableProteinScoreNameControls(false);
		}
		if (PEPTIDE_NUMBER_HISTOGRAM.equals(chartType) || PEPTIDE_OVERLAPING.equals(chartType)
				|| PEPTIDE_HEATMAP.equals(chartType) || PEPTIDE_SCORE_COMPARISON.equals(chartType)
				|| PEPTIDE_MONITORING.equals(chartType) || PEPTIDE_REPEATABILITY.equals(chartType)
				|| PEPTIDE_PRESENCY_HEATMAP.equals(chartType) || PSM_PEP_PROT.equals(chartType)
				|| EXCLUSIVE_PEPTIDE_NUMBER.equals(chartType) || PROTEIN_NUMBER_OF_PEPTIDES_HEATMAP.equals(chartType))
			jCheckBoxUniquePeptides.setEnabled(true);
		else
			jCheckBoxUniquePeptides.setEnabled(false);

	}

	public static synchronized void addToMiapeRetrievedHashMap(int miapeID, String path) {
		if (!miapeMSIsRetrieved.containsKey(miapeID))
			miapeMSIsRetrieved.put(miapeID, path);
	}

	public Boolean distinguishModifiedPeptides() {
		return jCheckBoxUniquePeptides.isSelected();
	}

	public String getChartSubtitle(String chartType, String option) {
		// if (chartType.equals(PROTEIN_SCORE_DISTRIBUTION)) {
		// if (option.equals(ONE_SERIES_PER_REPLICATE))
		// return ONE_SERIES_PER_REPLICATE;
		// }
		if (option != null)
			return option;
		return "";

	}

	public String getChartTitle(String chartType) {
		// if (chartType.equals(PROTEIN_SCORE_DISTRIBUTION)) {
		// return "Protein score distribution";
		// }else if (PEPTIDE_SCORE_DISTRIBUTION.equals(chartType)){
		// return "Peptide score distribution";
		// }
		return chartType;

	}

	public void showChart() {

		// String chartType = (String)
		// this.jComboBoxChartType.getSelectedItem();
		String chartType = currentChartType;

		String option = (String) jComboBoxChartOptions.getSelectedItem();
		// log.info("showing chart (" + chartType + " / " + option + ")");
		if (chartCreator != null && chartCreator.getState().equals(StateValue.STARTED)) {

			while (!chartCreator.cancel(true)) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if ((chartCreator == null || chartCreator.getState().equals(StateValue.DONE))
				&& filterDialog.isFilterTaskFinished()) {
			String formatedDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG)
					.format(new Date(System.currentTimeMillis()));
			appendStatus("Creating chart at '" + formatedDate + "'...");
			setProgressBarIndeterminate(true);
			disableControls(false);
			chartCreator = new ChartCreatorTask(this, chartType, option, experimentList);
			chartCreator.addPropertyChangeListener(this);
			chartCreator.execute();
		} else {
			// log.info("The chart cannot be generated until the previous chart
			// is finished");
			// this.appendStatus("Wait to finish the chart");
		}
	}

	public void startShowingChart() {
		if (!dataLoader.isDone()) {
			appendStatus("MIAPE data is already being loading. Please wait...");
			return;
		}
		setEmptyChart();
		updateOptionComboBox();
		applyFilters();

	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Create the miape_api webservice proxy
				// Get properties from resource file
				// File cfgfile = new File(
				// "C:\\Users\\Salva\\Workspace\\miape-extractor\\user_data\\projects\\PME6
				// CSIC-UAB vs PCM.xml");
				File cfgfile = new File(
						"C:\\Users\\Salva\\workspace\\miape-extractor\\user_data\\projects\\Comparison project.xml");
				MainFrame mainFrame = new MainFrame();
				mainFrame.userName = "smartinez@cnb.csic.es";
				mainFrame.password = "test";

				ChartManagerFrame dialog = new ChartManagerFrame(null, cfgfile);
				dialog.addWindowListener(new java.awt.event.WindowAdapter() {
					@Override
					public void windowClosing(java.awt.event.WindowEvent e) {
						System.exit(0);
					}
				});
				dialog.setVisible(true);

			}
		});
	}

	// GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.ButtonGroup buttonGroup1;
	private javax.swing.ButtonGroup buttonGroup2;
	private javax.swing.ButtonGroup buttonGroupDecoyPrefix;
	private javax.swing.ButtonGroup buttonGroupThresholds;
	private javax.swing.JButton jButtonDiscardFilteredData;
	private javax.swing.JButton jButtonExport2Excel;
	private javax.swing.JButton jButtonExport2PEX;
	private javax.swing.JButton jButtonExport2PRIDE;
	private javax.swing.JButton jButtonSaveAsFiltered;
	private javax.swing.JButton jButtonSeeAppliedFilters;
	private javax.swing.JButton jButtonShowTable;
	private javax.swing.JCheckBox jCheckBoxCountNonConclusiveProteins;
	public javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemFDRFilter;
	public javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemModificationFilter;
	public javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemOcurrenceFilter;
	public javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemPeptideForMRMFilter;
	public javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemPeptideLenthFilter;
	public javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemPeptideNumberFilter;
	public javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemPeptideSequenceFilter;
	public javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemProteinACCFilter;
	public javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemScoreFilters;
	private javax.swing.JCheckBox jCheckBoxUniquePeptides;
	private javax.swing.JComboBox jComboBoxChartOptions;
	private javax.swing.JLabel jLabelInformation1;
	private javax.swing.JLabel jLabelInformation2;
	private javax.swing.JLabel jLabelInformation3;
	private javax.swing.JMenuBar jMenuBar1;
	private javax.swing.JMenu jMenuChartType;
	private javax.swing.JMenu jMenuFilters;
	private javax.swing.JMenuItem jMenuItemDefineFilters;
	private javax.swing.JMenuItem jMenuItemGeneralOptions;
	private javax.swing.JMenuItem jMenuItemSend2PIKE;
	private javax.swing.JMenu jMenuOptions;
	private javax.swing.JMenu jMenuPike;
	private javax.swing.JPanel jPanelAddOptions;
	private javax.swing.JScrollPane jPanelAdditionalCustomizations;
	private javax.swing.JPanel jPanelChart;
	private javax.swing.JPanel jPanelChartType;
	private javax.swing.JPanel jPanelInformation;
	private javax.swing.JPanel jPanelLeft;
	private javax.swing.JPanel jPanelPeptideCounting;
	private javax.swing.JPanel jPanelRigth;
	private javax.swing.JPanel jPanelStatus;
	public javax.swing.JProgressBar jProgressBar;
	private javax.swing.JProgressBar jProgressBarMemoryUsage;
	private javax.swing.JScrollPane jScrollPane3;
	private javax.swing.JScrollPane jScrollPaneChart;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JSeparator jSeparator2;
	private javax.swing.JTextArea jTextAreaStatus;

	// End of variables declaration//GEN-END:variables

	private void disableControls(boolean b) {
		// if (b)
		// log.info("Enabling controls");
		// else
		// log.info("Disabling controls");
		jComboBoxChartOptions.setEnabled(b);
		jCheckBoxUniquePeptides.setEnabled(b);
		jCheckBoxCountNonConclusiveProteins.setEnabled(b);
		optionsFactory.disableAdditionalOptionControls(b);
		// log.info("Finish disabling/enabling");
	}

	@Override
	public String getUserName() {
		return MainFrame.userName;
	}

	@Override
	public String getPassword() {
		return MainFrame.password;

	}

	@Override
	public synchronized void propertyChange(PropertyChangeEvent evt) {
		if ("progress".equals(evt.getPropertyName())) {
			int progress = (Integer) evt.getNewValue();
			if (evt.getSource().equals(memoryChecker))
				jProgressBarMemoryUsage.setValue(progress);
			else
				jProgressBar.setValue(progress);

		}
		// else if ("state".equals(evt.getPropertyName())) {
		// if (task != null && !task.isCancelled()) {
		// // not if it is from the miapeHeaderLoader task
		// final StateValue state = (StateValue) evt.getNewValue();
		// if (state.equals(StateValue.DONE)) {
		// this.disableControls(true);
		// this.appendStatus("Process finish");
		// } else if (state.equals(StateValue.STARTED)) {
		// this.appendStatus("Starting process");
		// }
		// }
		// }
		else if ("notificacion".equals(evt.getPropertyName())) {
			String notificacion = evt.getNewValue().toString();
			appendStatus(notificacion);
		} else if (DataLoaderTask.DATA_LOADED_START.equals(evt.getPropertyName())) {
			appendStatus("Reading project data...");
			appendStatus("Depending on the size of the data, it can take a few minutes...");
			setEmptyChart();
			disableControls(false);
			setProgressBarIndeterminate(true);
		} else if (DataLoaderTask.DATA_LOADED_DONE.equals(evt.getPropertyName())) {
			errorLoadingData = false;
			disableControls(true);
			if (experimentList != null)
				experimentList = null;
			experimentList = (ExperimentList) evt.getNewValue();

			// force a new instance of the filters dialog, to reset all filters
			filterDialog = FiltersDialog.getNewInstance(this, experimentList);

			// if (!this.filterDialog.getFilters().isEmpty())
			setFDRLabel();

			appendStatus("Project data imported succesfully");
			log.info("Project data imported succesfully");
			updateOptionComboBox();
			addCustomizationControls();
			// apply sorting parameters (this has to be after the customization
			// controls)
			updateControlStates();
			enableMenus(true);
			startShowingChart();
			jButtonShowTable.setEnabled(true);
			jButtonExport2Excel.setEnabled(true);
			// jButtonExport2PEX.setEnabled(true);
			jButtonExport2PRIDE.setEnabled(true);

		} else if (DataLoaderTask.DATA_LOADED_ERROR.equals(evt.getPropertyName())) {
			errorLoadingData = true;
			String erromessage = (String) evt.getNewValue();
			setProgressBarIndeterminate(false);

			JOptionPane.showMessageDialog(this, erromessage);

			dispose();
		} else if (ChartCreatorTask.CHART_GENERATED.equals(evt.getPropertyName())
				|| ChartCreatorTask.CHART_ERROR_GENERATED.equals(evt.getPropertyName())) {
			jPanelChart.removeAll();
			jPanelChart.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.WEST;
			c.gridx = 0;
			c.gridy = 0;
			Object object = evt.getNewValue();

			if (object instanceof JComponent) {
				// this.jPanelChart.setGraphicPanel((JComponent) object);
				jPanelChart.add((JComponent) object, c);
			} else if (object instanceof List) {
				List lista = (List) object;
				if (lista.get(0) instanceof JPanel) {
					List<JPanel> chartList = (List<JPanel>) object;
					JPanel panel = new JPanel();

					c.insets = new Insets(10, 0, 0, 0);
					panel.setLayout(new GridBagLayout());
					for (JPanel jPanel : chartList) {
						panel.add(jPanel, c);
						c.gridy++;
					}
					c.gridy = 0;
					jPanelChart.add(panel, c);
				} else if (lista.get(0) instanceof Panel) {
					List<Panel> chartList = (List<Panel>) object;
					JPanel panel = new JPanel();

					c.insets = new Insets(10, 0, 0, 0);
					panel.setLayout(new GridBagLayout());
					for (Panel jPanel : chartList) {
						panel.add(jPanel, c);
						c.gridy++;
					}
					c.gridy = 0;
					jPanelChart.add(panel, c);
					jScrollPaneChart.getViewport().addChangeListener(new ChangeListener() {

						@Override
						public void stateChanged(ChangeEvent e) {
							jScrollPaneChart.revalidate();
							ChartManagerFrame.this.repaint();
						}
					});

				}
				// this.jPanelChart.addGraphicPanel((List<JPanel>) object);
			} else if (object instanceof Panel) {
				jPanelChart.add((Panel) object, c);
			}
			jPanelChart.repaint();
			if (ChartCreatorTask.CHART_GENERATED.equals(evt.getPropertyName())) {
				String formatedDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG)
						.format(new Date(System.currentTimeMillis()));
				setNumIdentificationsLabel();
				setFDRLabel();
				appendStatus("Chart created at '" + formatedDate + "'");
				if (currentChartType.equals(PROTEIN_NAME_CLOUD)) {
					appendStatus("Wait some seconds for the cloud loading...");
				}

			} else if (ChartCreatorTask.CHART_ERROR_GENERATED.equals(evt.getPropertyName())) {
				appendStatus("Error generating the chart.");
			}
			setProgressBarIndeterminate(false);
			disableControls(true);
			updateControlStates();
			if (!filterDialog.getFilters().isEmpty()) {
				jButtonDiscardFilteredData.setEnabled(true);
				jButtonSeeAppliedFilters.setEnabled(true);
				jButtonSaveAsFiltered.setEnabled(true);

			} else {
				jButtonDiscardFilteredData.setEnabled(false);
				jButtonSeeAppliedFilters.setEnabled(false);
				jButtonSaveAsFiltered.setEnabled(false);
			}
			pack();
		} else if (ChartCreatorTask.DATASET_PROGRESS.equals(evt.getPropertyName())) {
			String message = (String) evt.getNewValue();
			jProgressBar.setStringPainted(true);
			jProgressBar.setString(message);

		} else if (MemoryCheckerTask.MEMORY_CHECKER_DATA_REPORT.equals(evt.getPropertyName())) {

			String memoryUsage = (String) evt.getNewValue();
			jProgressBarMemoryUsage.setString("Memory usage: " + memoryUsage);
		} else if (CuratedExperimentSaver.CURATED_EXP_SAVER_START.equals(evt.getPropertyName())) {
			appendStatus("Saving experiment(s) as curated...");
			appendStatus("This task will be performed in background");
			jButtonSaveAsFiltered.setEnabled(false);
		} else if (CuratedExperimentSaver.CURATED_EXP_SAVER_END.equals(evt.getPropertyName())) {
			setProgressBarIndeterminate(false);
			jProgressBar.setString("");
			jProgressBar.setValue(0);
			appendStatus("Experiments saved as curated.");
			jButtonSaveAsFiltered.setEnabled(true);

		} else if (CuratedExperimentSaver.CURATED_EXP_SAVER_ERROR.equals(evt.getPropertyName())) {
			setProgressBarIndeterminate(false);
			jProgressBar.setString("");
			appendStatus("Error saving curated experiments: " + evt.getNewValue());
			jButtonSaveAsFiltered.setEnabled(true);

		} else if (CuratedExperimentSaver.CURATED_EXP_SAVER_PROGRESS.equals(evt.getPropertyName())) {
			appendStatus((String) evt.getNewValue());

		} else if (OntologyLoaderTask.ONTOLOGY_LOADING_FINISHED.equals(evt.getPropertyName())) {
			log.info("Ontologies loaded");
			long time = (Long) evt.getNewValue();
			time = time / 1000;
			appendStatus("Ontologies loaded in " + time + " sg.");
			jProgressBar.setIndeterminate(false);
			CPExperimentList cpExpList = getCPExperimentList(cfgFile);
			dataLoader = new DataLoaderTask(cpExpList, minPeptideLength, groupProteinsAtExperimentListLevel, null,
					isLocalProcessingInParallel);
			// filters);
			dataLoader.addPropertyChangeListener(this);
			dataLoader.execute();
		} else if (OntologyLoaderTask.ONTOLOGY_LOADING_STARTED.equals(evt.getPropertyName())) {
			appendStatus("Loading ontologies. Please wait...");
			jProgressBar.setIndeterminate(true);
		} else if (OntologyLoaderTask.ONTOLOGY_LOADING_ERROR.equals(evt.getPropertyName())) {
			appendStatus("Error loading ontologies. Please contact to miape-support@proteored.org.");
			appendStatus((String) evt.getNewValue());
		}

	}

	private void enableMenus(boolean b) {
		jMenuFilters.setEnabled(b);
		jMenuChartType.setEnabled(b);
		jMenuPike.setEnabled(b);
		jMenuOptions.setEnabled(b);
	}

	private DefaultComboBoxModel getProteinScoreNames() {
		List<String> scoreNames = experimentList.getProteinScoreNames();
		if (scoreNames != null && !scoreNames.isEmpty()) {
			return new DefaultComboBoxModel(scoreNames.toArray());
		}
		return null;
	}

	private DefaultComboBoxModel getPeptideScoreNames() {
		List<String> scoreNames = experimentList.getPeptideScoreNames();
		if (scoreNames != null && !scoreNames.isEmpty()) {
			Collections.sort(scoreNames);
			String[] scoreNameArray = new String[scoreNames.size()];
			int i = 0;
			for (String scoreName : scoreNames) {
				scoreNameArray[i] = scoreName;
				i++;
			}
			return new DefaultComboBoxModel(scoreNameArray);
		}
		return null;
	}

	public void appendStatus(String notificacion) {
		jTextAreaStatus.append(notificacion + "\n");
		jTextAreaStatus.setCaretPosition(jTextAreaStatus.getText().length() - 1);

	}

	void setStatus(String notificacion) {
		jTextAreaStatus.setText(notificacion + "\n");
		jTextAreaStatus.setCaretPosition(jTextAreaStatus.getText().length() - 1);

	}

	@Override
	public JProgressBar getProgressBar() {
		return jProgressBar;
	}

	public void setExperimentList(ExperimentList experimentList) {
		this.experimentList = experimentList;
	}

	public ExperimentList getExperimentList() {
		return experimentList;
	}

	public boolean isFDRThresholdEnabled() {
		return !filterDialog.getFDRFilters().isEmpty();
	}

	public void setFDRFilterMenuSelected(boolean selected) {
		jCheckBoxMenuItemFDRFilter.setSelected(selected);

	}

	public void setScoreFilterMenuSelected(boolean selected) {
		jCheckBoxMenuItemScoreFilters.setSelected(selected);

	}

	public void setOccurrenceFilterMenuSelected(boolean selected) {
		jCheckBoxMenuItemOcurrenceFilter.setSelected(selected);

	}

	public void setModificationFilterMenuSelected(boolean selected) {
		jCheckBoxMenuItemModificationFilter.setSelected(selected);

	}

	public void setProteinACCFilterMenuSelected(boolean selected) {
		jCheckBoxMenuItemProteinACCFilter.setSelected(selected);
	}

	public void setPeptideNumberFilterMenuSelected(boolean selected) {
		jCheckBoxMenuItemPeptideNumberFilter.setSelected(selected);
	}

	public void setPeptideLengthFilterMenuSelected(boolean selected) {
		jCheckBoxMenuItemPeptideLenthFilter.setSelected(selected);

	}

	public void setPeptideSequencesFilterMenuSelected(boolean selected) {
		jCheckBoxMenuItemPeptideSequenceFilter.setSelected(selected);

	}

	public void setPeptideForMRMFilterMenuSelected(boolean selected) {
		jCheckBoxMenuItemPeptideForMRMFilter.setSelected(selected);

	}

	public void setInformation1(String string) {
		jLabelInformation1.setText(string);
	}

	public void setInformation2(String string) {
		jLabelInformation2.setText(string);
	}

	public void setToolTipInformation2(String string) {
		jLabelInformation2.setToolTipText(string);
	}

	public void setInformation3(String string) {
		jLabelInformation3.setText(string);

	}

	public String getCurrentChartType() {
		return currentChartType;
	}

	public void setProgressBarIndeterminate(boolean b) {
		jProgressBar.setIndeterminate(b);

	}

	public void setToolTipInformation3(String string) {
		jLabelInformation3.setToolTipText(string);

	}

	public boolean isOccurrenceByReplicates() {
		return filterDialog.isOccurrenceByReplicates();
	}

	public boolean isOccurrenceFilterEnabled() {
		return filterDialog.isOccurrenceFilterEnabled();
	}

	public void setFDRLabel() {
		final String fdrString = getFDRString();
		setInformation2(fdrString);
		if (fdrString.equals(FDR_CANNOT_BE_CALCULATED_MESSAGE))
			setToolTipInformation2("<html>Global FDR cannot be calculated due the following reasons:<br>"
					+ "<ul><li>Lower levels (level 1, that is, experiment level, or level 2, that is, fractions/bands/replicates level) have different FDR thresholds.</li>"
					+ "<li>Lower levels have used different search engines and therefore, the score used to calculate the FDR filter is different.</li></ul>");
		else
			setToolTipInformation2("<html>Global False Discovery Rates: " + "<ul><li>at Protein level </li>"
					+ "<li>at Peptide level</li>" + "<li>at Peptide Spectrum Match level</li></ul>"
					+ "These values are calculated at level 0 (experiment list).</html>");

	}

	public void setNumIdentificationsLabel() {
		setInformation3(getNumberIdentificationsString());
		setToolTipInformation3(
				"<html>These numbers means:<ul><li>Number of peptides and unique peptides, NOT distinguishing differently modificated peptides.</li>"
						+ "<li>Number of proteins/protein groups and unique proteins/protein groups (not considering NON_CONCLUSIVE groups).</li>"
						+ "<li>Number of different human genes (if mapped) and, in brackets number of human genes just taking one per proteing group.</li></ul></html>");
	}

	public void setEmptyChart() {
		jPanelChart.removeAll();
		jPanelChart.repaint();
	}

	public void exportChr16Proteins() {
		Set<String> filterProteinACC = GeneDistributionReader.getInstance().getProteinGeneMapping("16").keySet();

		filterDialog.enableProteinACCFilter(filterProteinACC);
		filterDialog.applyFilters(experimentList);

	}

	// public boolean isChr16ChartShowed() {
	// return this.isChr16ChartShowed;
	// }

	public void clearIdentificationTable() {
		identificationTable = null;

	}

	public void setProteinSequencesRetrieved(boolean retrieveProteinSeqs) {
		proteinSequencesRetrieved = retrieveProteinSeqs;
	}

	public boolean isProteinSequencesRetrieved() {
		return proteinSequencesRetrieved;
	}

	public FiltersDialog getFiltersDialog() {
		return filterDialog;
	}

	public JTextArea getTextAreaStatus() {
		return jTextAreaStatus;
	}

	public void saveWordCramImage() {
		if (currentChartType.equals(PROTEIN_NAME_CLOUD)) {
			log.info("Saving Protein name cloud");
			String error = "";
			try {
				List<WordCramChart> charts = new ArrayList<WordCramChart>();
				final Component component = jPanelChart.getComponent(0);
				if (component instanceof WordCramChart) {
					WordCramChart wordCram = (WordCramChart) component;
					charts.add(wordCram);

				} else if (component instanceof JPanel) {
					JPanel jpanel = (JPanel) component;
					if (jpanel.getComponent(0) instanceof WordCramChart) {

						for (Component component2 : jpanel.getComponents()) {
							if (component2 instanceof WordCramChart) {
								charts.add((WordCramChart) component2);
							}
						}

					}
				}
				saveWordCramCharts(charts);
			} catch (Exception e) {
				error = e.getMessage();
				e.printStackTrace();
			}

			appendStatus(error);
		}
	}

	private void saveWordCramCharts(List<WordCramChart> charts) {
		if (charts != null && !charts.isEmpty()) {

			JFileChooser fileChooser = new JFileChooser(currentFolder);
			fileChooser.addChoosableFileFilter(new ExtensionFileFilter("TIF images", "tif"));
			int retVal = fileChooser.showSaveDialog(this);

			if (retVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				currentFolder = file.getParentFile();
				for (int i = 0; i < charts.size(); i++) {
					if ("".equals(FilenameUtils.getExtension(file.getAbsolutePath())))
						file = new File(file.getAbsolutePath() + ".tif");
					if (i > 0) {
						file = getNextFile(file, i);
					}

					WordCramChart wordCram = charts.get(i);
					wordCram.saveFrame(file.getAbsolutePath());
					appendStatus("Image saved to:" + file.getAbsolutePath());
				}

			}
			return;
		}
	}

	private File getNextFile(File file, int num) {
		if (file != null) {
			String name = FilenameUtils.getFullPath(file.getAbsolutePath())
					+ FilenameUtils.getBaseName(file.getAbsolutePath());
			if (name.contains("_")) {
				name = name.split("_")[0];
			}
			return new File(name + "_" + num + "." + FilenameUtils.getExtension(file.getAbsolutePath()));
		}
		return null;
	}

	public boolean countNonConclusiveProteins() {
		return jCheckBoxCountNonConclusiveProteins.isSelected();
	}

	public void setErrorLoadingData(boolean b) {
		errorLoadingData = b;
	}

	public List<String> getProteinAccsFromACCFilter() {
		List<Filter> filters = filterDialog.getFilters();
		if (filters != null) {
			for (Filter filter : filters) {
				if (filter instanceof ProteinACCFilter) {
					ProteinACCFilter proteinAccFilter = (ProteinACCFilter) filter;
					return proteinAccFilter.getSortedAccessions();
				}
			}
		}
		return new ArrayList<String>();
	}

	public List<String> getPeptideSequencesFromPeptideSequenceFilter() {
		List<Filter> filters = filterDialog.getFilters();
		if (filters != null) {
			for (Filter filter : filters) {
				if (filter instanceof PeptideSequenceFilter) {
					PeptideSequenceFilter peptideSequenceFilter = (PeptideSequenceFilter) filter;
					return peptideSequenceFilter.getSortedSequences();
				}
			}
		}
		return new ArrayList<String>();
	}
}