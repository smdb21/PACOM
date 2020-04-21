/*
 * Miape2ExperimentListDialog.java Created on __DATE__, __TIME__
 */

package org.proteored.pacom.analysis.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
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
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker.StateValue;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ui.UIUtils;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.experiment.VennData;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.IdentificationItemEnum;
import org.proteored.miapeapi.experiment.model.IdentificationSet;
import org.proteored.miapeapi.experiment.model.PeptideOccurrence;
import org.proteored.miapeapi.experiment.model.ProteinGroupOccurrence;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.miapeapi.experiment.model.filters.FDRFilter;
import org.proteored.miapeapi.experiment.model.filters.Filter;
import org.proteored.miapeapi.experiment.model.filters.Filters;
import org.proteored.miapeapi.experiment.model.filters.PeptideSequenceFilter;
import org.proteored.miapeapi.experiment.model.filters.ProteinACCFilter;
import org.proteored.miapeapi.experiment.model.filters.ProteinACCFilterByProteinComparatorKey;
import org.proteored.miapeapi.experiment.model.filters.ScoreFilter;
import org.proteored.miapeapi.experiment.model.sort.ProteinComparatorKey;
import org.proteored.miapeapi.experiment.model.sort.ProteinGroupComparisonType;
import org.proteored.miapeapi.experiment.model.sort.SorterUtil;
import org.proteored.pacom.analysis.charts.ImageFileFormat;
import org.proteored.pacom.analysis.charts.ImageLabel;
import org.proteored.pacom.analysis.charts.VennChart;
import org.proteored.pacom.analysis.charts.WordCramChart;
import org.proteored.pacom.analysis.conf.ExperimentListAdapter;
import org.proteored.pacom.analysis.conf.jaxb.CPExperimentList;
import org.proteored.pacom.analysis.exporters.gui.ExporterDialog;
import org.proteored.pacom.analysis.exporters.gui.ExporterToPRIDEDialog;
import org.proteored.pacom.analysis.exporters.gui.IdentificationTableFrame;
import org.proteored.pacom.analysis.exporters.gui.PEXBulkSubmissionSummaryFileCreatorDialog;
import org.proteored.pacom.analysis.genes.GeneDistributionReader;
import org.proteored.pacom.analysis.gui.tasks.ChartCreatorTask;
import org.proteored.pacom.analysis.gui.tasks.CuratedExperimentSaver;
import org.proteored.pacom.analysis.gui.tasks.DataLoaderTask;
import org.proteored.pacom.analysis.gui.tasks.MemoryCheckerTask;
import org.proteored.pacom.analysis.util.DataLevel;
import org.proteored.pacom.analysis.util.DoSomethingToChangeColorInChart;
import org.proteored.pacom.analysis.util.ImageUtils;
import org.proteored.pacom.gui.AbstractJFrameWithAttachedHelpAndAttachedChartTypeDialog;
import org.proteored.pacom.gui.ImageManager;
import org.proteored.pacom.gui.MainFrame;
import org.proteored.pacom.gui.OpenHelpButton;
import org.proteored.pacom.gui.tasks.OntologyLoaderTask;
import org.proteored.pacom.utils.AppVersion;
import org.proteored.pacom.utils.ExtensionFileFilter;
import org.proteored.pacom.utils.PACOMSoftware;
import org.proteored.pacom.utils.ToolTipUtil;

import edu.scripps.yates.utilities.checksum.MD5Checksum;
import edu.scripps.yates.utilities.dates.DatesUtil;
import edu.scripps.yates.utilities.swing.ComponentEnableStateKeeper;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

/**
 *
 * @author __USER__
 */
public class ChartManagerFrame extends AbstractJFrameWithAttachedHelpAndAttachedChartTypeDialog
		implements PropertyChangeListener, TreeDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5566179230963643716L;

	private FiltersDialog filterDialog = null;

	private ChartType currentChartType = ChartType.PSM_PEP_PROT;
	private final String FDR_CANNOT_BE_CALCULATED_MESSAGE = "Global FDR cannot be calculated";

	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	public ExperimentList experimentList;
	private DataLoaderTask dataLoader;
	// Map to store the MIAPE documents that are retrieved <Identified,
	// FullPath to the file>
	private static TIntObjectHashMap<String> miapeMSIsRetrieved = new TIntObjectHashMap<String>();
	private static ChartManagerFrame instance;

	public static final String ONE_SERIES_PER_EXPERIMENT = "One data series per level 1";
	public static final String ONE_SERIES_PER_REPLICATE = "One data series per level 2";
	public static final String ONE_CHART_PER_EXPERIMENT = "One separate chart per level 1";
	public static final String ONE_SERIES_PER_EXPERIMENT_LIST = "One single data series (level 0)";
	public static final String MENU_SEPARATION = "menu separation";

	private final JFrame parentFrame;
	private ChartCreatorTask chartCreator;
	private MemoryCheckerTask memoryChecker;
	private final AdditionalOptionsPanelFactory optionsFactory = new AdditionalOptionsPanelFactory(this);
	// private boolean isChr16ChartShowed;
	private IdentificationTableFrame identificationTable;
	private CuratedExperimentSaver curatedExperimentSaver;
	private AppliedFiltersDialog filtersDialog;
	private PEXBulkSubmissionSummaryFileCreatorDialog pexSubmissionDialog;
	File cfgFile;
	private Long previousCfgFileSize;
	private Integer minPeptideLength;
	private Boolean groupProteinsAtExperimentListLevel;
	private Boolean doNotGroupNonConclusiveProteins;
	private Boolean separateNonConclusiveProteins;
	private Boolean isLocalProcessingInParallel;
	private boolean errorLoadingData;
	private String previousMd5Checksum;
	private final Map<Object, Object> previousTogleValues = new THashMap<Object, Object>();
	private long t1;
	private final ComponentEnableStateKeeper enableStateKeeper = new ComponentEnableStateKeeper();
	private final ReentrantLock chartCreatorlock = new ReentrantLock(true);
	private boolean creatingChartLock = false;;

	@Override
	public void dispose() {
		cancelDataLoader();
		cancelChartCreator();
		if (memoryChecker != null)
			memoryChecker.cancel(true);
		if (parentFrame != null) {
			parentFrame.setEnabled(true);
			parentFrame.setVisible(true);
		}
		errorLoadingData = true;
		super.dispose();
	}

	/**
	 * Creates new form Miape2ExperimentListDialog
	 *
	 * @param parentDialog
	 */
	@SuppressWarnings("restriction")
	private ChartManagerFrame(JFrame parentDialog, File cfgFile) {
		super(400);
		parentFrame = parentDialog;
		this.cfgFile = cfgFile;
		if (parentFrame != null)
			parentFrame.setVisible(false);

		initComponents();
		// set icon image
		setIconImage(ImageManager.getImageIcon(ImageManager.PACOM_LOGO).getImage());
		// init CV NAMES of SCORE NAMES

		try {

			//
			// load data in this.experimentList
			jButtonShowTable.setEnabled(false);
			jButtonExport2Excel.setEnabled(false);
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

		} catch (final Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			appendStatus(e.getMessage());
			setProgressBarIndeterminate(false);

		}
		initializeChartTypeMenu();

		updateControlStates();

		loadButtonIcons();

		jTextAreaStatus.setFont(new JTextField().getFont());

		final AppVersion version = MainFrame.getVersion();
		if (version != null) {
			final String suffix = " (v" + version.toString() + ")";
			setTitle(getTitle() + suffix);
		}
		enableStateKeeper.addReverseComponent(jButtonCancel);
		enableStateKeeper.addInvariableComponent(jTextAreaStatus);
		enableStateKeeper.addInvariableComponent(jButtonHelp);

		UIUtils.centerFrameOnScreen(this);
	}

	public static ChartManagerFrame getInstance(Miape2ExperimentListDialog parentDialog, File cfgFile) {
		return getInstance(parentDialog, cfgFile, null);
	}

	public static ChartManagerFrame getInstance(Miape2ExperimentListDialog parentDialog, File cfgFile,
			Boolean resetErrorLoadingData) {

		if (instance == null)
			instance = new ChartManagerFrame(parentDialog, cfgFile);
		instance.cfgFile = cfgFile;
		if (resetErrorLoadingData != null && resetErrorLoadingData) {
			instance.errorLoadingData = false;
		}
		final GeneralOptionsDialog generalOptionsDialog = GeneralOptionsDialog.getInstance(instance, true);
		final boolean group = generalOptionsDialog.groupProteinsAtExperimentListLevel();
		final boolean donotGroupNonConclusiveProteins = generalOptionsDialog.isDoNotGroupNonConclusiveProteins();
		final boolean separateNonConclusiveProteins = generalOptionsDialog.isSeparateNonConclusiveProteins();
		final int pepLength = generalOptionsDialog.getMinPeptideLength();
		final boolean parallel = generalOptionsDialog.isLocalProcessingInParallel();

		if (!generalOptionsDialog.isDoNotAskAgainSelected()) {
			generalOptionsDialog.setVisible(true);
			// do not load data now...wait after GeneralOptions dispose
			return instance;
		}
		log.info("now=" + cfgFile.length() + " previous=" + instance.previousCfgFileSize);

		if (instance.dataShouldBeLoaded(cfgFile, group, donotGroupNonConclusiveProteins, separateNonConclusiveProteins,
				pepLength, parallel)) {
			instance.loadData(pepLength, group, donotGroupNonConclusiveProteins, separateNonConclusiveProteins, cfgFile,
					parallel);
		}

		return instance;
	}

	@Override
	public void setVisible(boolean b) {
		log.info("ChartManager visible=" + b);

		if (b) {
			// Memory checker
			memoryChecker = new MemoryCheckerTask();
			memoryChecker.addPropertyChangeListener(this);
			memoryChecker.execute();

			currentChartType = ChartType.PSM_PEP_PROT;

			pack();
		}
		super.setVisible(b);
	}

	/**
	 * Checks if something has changed on the {@link GeneralOptionsDialog}. If
	 * yes, the data it will return true. It also checks if the cgfFile is
	 * different and it will return true. Otherwise, it will return false.
	 *
	 *
	 * @return
	 */
	protected boolean dataShouldBeLoaded(File cfgFile, boolean groupingAtExperimentListLevel,
			boolean doNotGroupNonConclusiveProteins, boolean separateNonConclusiveProteins, int minPeptideLength,
			boolean processInparallel) {

		if (errorLoadingData) {
			return false;
		}
		if (cfgFile != null && !cfgFile.getAbsolutePath().equals(this.cfgFile.getAbsolutePath()))
			return true;
		try {
			final String md5Checksum = MD5Checksum.getMD5ChecksumFromFileName(cfgFile.getAbsolutePath());
			if (previousMd5Checksum == null || (cfgFile != null && !md5Checksum.equals(previousMd5Checksum))) {
				previousMd5Checksum = md5Checksum;
				return true;
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}

		if (previousCfgFileSize == null || (cfgFile != null && cfgFile.length() != previousCfgFileSize)) {
			return true;
		}
		if (this.minPeptideLength == null || minPeptideLength != this.minPeptideLength) {
			return true;
		}
		if (groupProteinsAtExperimentListLevel == null
				|| Boolean.compare(groupingAtExperimentListLevel, groupProteinsAtExperimentListLevel) != 0) {
			return true;
		}
		if (this.doNotGroupNonConclusiveProteins == null
				|| Boolean.compare(doNotGroupNonConclusiveProteins, this.doNotGroupNonConclusiveProteins) != 0) {
			return true;
		}
		if (this.separateNonConclusiveProteins == null
				|| Boolean.compare(separateNonConclusiveProteins, this.separateNonConclusiveProteins) != 0) {
			return true;
		}
		if (isLocalProcessingInParallel == null
				|| !String.valueOf(processInparallel).equals(String.valueOf(isLocalProcessingInParallel))) {
			return true;
		}
		if (experimentList == null) {
			return true;
		}
		return false;
	}

	/**
	 * Loads the data with the parameters taken from the
	 * {@link GeneralOptionsDialog}
	 */
	private void loadData() {
		final GeneralOptionsDialog generalOptionsDialog = GeneralOptionsDialog.getInstance(instance, true);
		final boolean groupingAtExperimentListLevel = generalOptionsDialog.groupProteinsAtExperimentListLevel();
		final boolean doNotGroupNonConclusiveProteins = generalOptionsDialog.isDoNotGroupNonConclusiveProteins();
		final boolean separateNonConclusiveProteins = generalOptionsDialog.isSeparateNonConclusiveProteins();
		final int minPeptideLength = generalOptionsDialog.getMinPeptideLength();

		this.loadData(minPeptideLength, groupingAtExperimentListLevel, doNotGroupNonConclusiveProteins,
				separateNonConclusiveProteins, cfgFile, isLocalProcessingInParallel);

	}

	private void loadData(Integer minPeptideLength, boolean groupingAtExperimentListLevel,
			boolean doNotGroupNonConclusiveProteins, boolean separateNonConclusiveProteins, File cfgFile,
			boolean processInParallel) {

		this.cfgFile = cfgFile;
		this.minPeptideLength = minPeptideLength;
		groupProteinsAtExperimentListLevel = groupingAtExperimentListLevel;
		this.doNotGroupNonConclusiveProteins = doNotGroupNonConclusiveProteins;
		this.separateNonConclusiveProteins = separateNonConclusiveProteins;
		previousCfgFileSize = this.cfgFile.length();
		isLocalProcessingInParallel = processInParallel;
		final CPExperimentList cpExpList = getCPExperimentList(cfgFile);
		setTitle(cpExpList.getName() + " - Chart Viewer");
		final AppVersion version = MainFrame.getVersion();
		if (version != null) {
			final String suffix = " (v" + version.toString() + ")";
			setTitle(getTitle() + suffix);
		}
		// set to "loading data..." the information labels
		setInformation1("Loading data...");
		setInformation2("Loading data...");
		setInformation3("Loading data...");

		experimentList = null;

		final OntologyLoaderTask ontologyLoaderTask = new OntologyLoaderTask();
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
			boolean donotGroupNonConclusiveProteins, boolean separateNonConclusiveProteins, boolean processInParallel) {
		loadData(minPeptideLength, groupingAtExperimentListLevel, donotGroupNonConclusiveProteins,
				separateNonConclusiveProteins, cfgFile, processInParallel);
	}

	private CPExperimentList getCPExperimentList(File cfgFile) {
		final CPExperimentList cpExpList = new ExperimentListAdapter(cfgFile, isAnnotateProteinsInUniprot(),
				GeneralOptionsDialog.getInstance(this).isDoNotGroupNonConclusiveProteins(),
				GeneralOptionsDialog.getInstance(this).isSeparateNonConclusiveProteins()).getCpExperimentList();
		return cpExpList;
	}

	private boolean isAnnotateProteinsInUniprot() {
		// TODO GET THIS FROM INTERFACE
		// SALVA
		return true;
	}

	private void loadButtonIcons() {
		// STAR
		jButtonSaveAsFiltered.setIcon(ImageManager.getImageIcon(ImageManager.STAR));
		jButtonSaveAsFiltered.setPressedIcon(ImageManager.getImageIcon(ImageManager.STAR_CLICKED));

		// FUNNEL
		jButtonSeeAppliedFilters.setIcon(ImageManager.getImageIcon(ImageManager.FUNNEL));
		jButtonSeeAppliedFilters.setPressedIcon(ImageManager.getImageIcon(ImageManager.FUNNEL_CLICKED));

		// TABLE
		jButtonShowTable.setIcon(ImageManager.getImageIcon(ImageManager.TABLE));
		jButtonShowTable.setPressedIcon(ImageManager.getImageIcon(ImageManager.TABLE_CLICKED));
		// EXCEL
		jButtonExport2Excel.setIcon(ImageManager.getImageIcon(ImageManager.EXCEL_TABLE));
		jButtonExport2Excel.setPressedIcon(ImageManager.getImageIcon(ImageManager.EXCEL_TABLE_CLICKED));
		// PRIDE
		jButtonExport2PRIDE.setIcon(ImageManager.getImageIcon(ImageManager.PRIDE_LOGO));
		jButtonExport2PRIDE.setPressedIcon(ImageManager.getImageIcon(ImageManager.PRIDE_LOGO_CLICKED));
		// cancel
		jButtonCancel.setIcon(ImageManager.getImageIcon(ImageManager.STOP));
		jButtonCancel.setPressedIcon(ImageManager.getImageIcon(ImageManager.STOP_CLICKED));
	}

	public AdditionalOptionsPanelFactory getOptionsFactory() {
		return optionsFactory;
	}

	private void initializeChartTypeMenu() {

		// Identification numbers
		final JMenu menuIdNumbers = new JMenu("Number of identifications");
		final List<String> idNumbersMenus = new ArrayList<String>();
		idNumbersMenus.add(ChartType.PSM_PEP_PROT.getName());
		idNumbersMenus.add(MENU_SEPARATION); // MENU SEPARATION
		idNumbersMenus.add(ChartType.PEPTIDE_NUMBER_HISTOGRAM.getName());
		idNumbersMenus.add(ChartType.PEPTIDE_REPEATABILITY.getName());
		idNumbersMenus.add(ChartType.EXCLUSIVE_PEPTIDE_NUMBER.getName());
		idNumbersMenus.add(ChartType.PEPTIDE_NUMBER_IN_PROTEINS.getName());
		idNumbersMenus.add(MENU_SEPARATION); // MENU SEPARATION
		idNumbersMenus.add(ChartType.PROTEIN_NUMBER_HISTOGRAM.getName());
		idNumbersMenus.add(ChartType.PROTEIN_REPEATABILITY.getName());
		idNumbersMenus.add(ChartType.EXCLUSIVE_PROTEIN_NUMBER.getName());
		idNumbersMenus.add(ChartType.SINGLE_HIT_PROTEINS.getName());
		addSubmenus(menuIdNumbers, idNumbersMenus, jMenuChartType);
		// add action to show right panel
		menuIdNumbers.addMouseListener(getMouseListenerForShowingAttachedPanelForChartTypes(idNumbersMenus));

		// Overlappings
		final JMenu menuOverlappings = new JMenu("Reproducibility and Overlap");
		final List<String> overlappingMenus = new ArrayList<String>();
		overlappingMenus.add(ChartType.PEPTIDE_OVERLAPING.getName());
		overlappingMenus.add(ChartType.EXCLUSIVE_PEPTIDE_NUMBER.getName());
		overlappingMenus.add(ChartType.PEPTIDE_REPEATABILITY.getName());
		overlappingMenus.add(ChartType.PEPTIDE_PRESENCY_HEATMAP.getName());
		overlappingMenus.add(ChartType.PEPTIDE_OCCURRENCE_HEATMAP.getName());
		overlappingMenus.add(MENU_SEPARATION); // MENU SEPARATION
		overlappingMenus.add(ChartType.PROTEIN_OVERLAPING.getName());
		overlappingMenus.add(ChartType.EXCLUSIVE_PROTEIN_NUMBER.getName());
		overlappingMenus.add(ChartType.PROTEIN_REPEATABILITY.getName());
		overlappingMenus.add(ChartType.PROTEIN_OCURRENCE_HEATMAP.getName());
		addSubmenus(menuOverlappings, overlappingMenus, jMenuChartType);
		// add action to show right panel
		menuOverlappings.addMouseListener(getMouseListenerForShowingAttachedPanelForChartTypes(overlappingMenus));

		// Sensibility
		final JMenu menuSensitivity = new JMenu("FDR, Sensitivity, Specificity, Accuracy");
		final List<String> sensitivityMenus = new ArrayList<String>();
		sensitivityMenus.add(ChartType.FDR.getName());
		sensitivityMenus.add(ChartType.FDR_VS_SCORE.getName());
		sensitivityMenus.add(MENU_SEPARATION);
		sensitivityMenus.add(ChartType.PROTEIN_SENSITIVITY_SPECIFICITY.getName());
		addSubmenus(menuSensitivity, sensitivityMenus, jMenuChartType);
		// add action to show right panel
		menuSensitivity.addMouseListener(getMouseListenerForShowingAttachedPanelForChartTypes(sensitivityMenus));

		// Scores
		final JMenu menuScores = new JMenu("Scores");
		final List<String> scoreMenus = new ArrayList<String>();
		scoreMenus.add(ChartType.PEPTIDE_SCORE_COMPARISON.getName());
		scoreMenus.add(ChartType.PEPTIDE_SCORE_DISTRIBUTION.getName());
		scoreMenus.add(MENU_SEPARATION); // MENU SEPARATION
		scoreMenus.add(ChartType.PROTEIN_SCORE_COMPARISON.getName());
		scoreMenus.add(ChartType.PROTEIN_SCORE_DISTRIBUTION.getName());
		scoreMenus.add(MENU_SEPARATION); // MENU SEPARATION
		scoreMenus.add(ChartType.FDR_VS_SCORE.getName());
		addSubmenus(menuScores, scoreMenus, jMenuChartType);
		// add action to show right panel
		menuScores.addMouseListener(getMouseListenerForShowingAttachedPanelForChartTypes(scoreMenus));

		// protein features
		final JMenu menuProteinFeatures = new JMenu("Protein features");
		final List<String> proteinFeaturesMenus = new ArrayList<String>();
		proteinFeaturesMenus.add(ChartType.PROTEIN_NAME_CLOUD.getName());
		proteinFeaturesMenus.add(MENU_SEPARATION); // MENU SEPARATION
		proteinFeaturesMenus.add(ChartType.PROTEIN_GROUP_TYPES.getName());
		proteinFeaturesMenus.add(MENU_SEPARATION); // MENU SEPARATION
		proteinFeaturesMenus.add(ChartType.EXCLUSIVE_PROTEIN_NUMBER.getName());
		proteinFeaturesMenus.add(ChartType.PROTEIN_REPEATABILITY.getName());
		proteinFeaturesMenus.add(ChartType.PEPTIDE_NUM_PER_PROTEIN_MASS.getName());
		proteinFeaturesMenus.add(MENU_SEPARATION);
		proteinFeaturesMenus.add(ChartType.PROTEIN_OCURRENCE_HEATMAP.getName());
		proteinFeaturesMenus.add(ChartType.PEPTIDES_PER_PROTEIN_HEATMAP.getName());
		proteinFeaturesMenus.add(ChartType.PSMS_PER_PROTEIN_HEATMAP.getName());
		proteinFeaturesMenus.add(MENU_SEPARATION);
		proteinFeaturesMenus.add(ChartType.PROTEIN_COVERAGE.getName());
		proteinFeaturesMenus.add(ChartType.PROTEIN_COVERAGE_DISTRIBUTION.getName());
		addSubmenus(menuProteinFeatures, proteinFeaturesMenus, jMenuChartType);
		// add action to show right panel
		menuProteinFeatures
				.addMouseListener(getMouseListenerForShowingAttachedPanelForChartTypes(proteinFeaturesMenus));

		// peptide features
		final JMenu menuPeptideFeatures = new JMenu("Peptide features");
		final List<String> peptideFeaturesMenus = new ArrayList<String>();
		peptideFeaturesMenus.add(ChartType.MISSEDCLEAVAGE_DISTRIBUTION.getName());
		peptideFeaturesMenus.add(MENU_SEPARATION); // MENU SEPARATION
		peptideFeaturesMenus.add(ChartType.PEPTIDE_MASS_DISTRIBUTION.getName());
		peptideFeaturesMenus.add(MENU_SEPARATION); // MENU SEPARATION
		peptideFeaturesMenus.add(ChartType.PEPTIDE_LENGTH_DISTRIBUTION.getName());
		peptideFeaturesMenus.add(MENU_SEPARATION); // MENU SEPARATION
		peptideFeaturesMenus.add(ChartType.PEPTIDE_CHARGE_HISTOGRAM.getName());
		peptideFeaturesMenus.add(MENU_SEPARATION); // MENU SEPARATION
		peptideFeaturesMenus.add(ChartType.DELTA_MZ_OVER_MZ.getName());
		peptideFeaturesMenus.add(MENU_SEPARATION); // MENU SEPARATION
		peptideFeaturesMenus.add(ChartType.PEPTIDE_RT.getName());
		peptideFeaturesMenus.add(ChartType.PEPTIDE_RT_COMPARISON.getName());
		peptideFeaturesMenus.add(ChartType.SINGLE_RT_COMPARISON.getName());
		peptideFeaturesMenus.add(MENU_SEPARATION); // MENU SEPARATION
		peptideFeaturesMenus.add(ChartType.EXCLUSIVE_PEPTIDE_NUMBER.getName());
		peptideFeaturesMenus.add(ChartType.PEPTIDE_REPEATABILITY.getName());
		peptideFeaturesMenus.add(MENU_SEPARATION); // MENU SEPARATION
		peptideFeaturesMenus.add(ChartType.PEPTIDE_OCCURRENCE_HEATMAP.getName());
		peptideFeaturesMenus.add(ChartType.PSMS_PER_PEPTIDE_HEATMAP.getName());
		peptideFeaturesMenus.add(ChartType.PEPTIDE_PRESENCY_HEATMAP.getName());
		addSubmenus(menuPeptideFeatures, peptideFeaturesMenus, jMenuChartType);
		peptideFeaturesMenus.add(MENU_SEPARATION); // MENU SEPARATION
		// add action to show right panel
		menuPeptideFeatures
				.addMouseListener(getMouseListenerForShowingAttachedPanelForChartTypes(peptideFeaturesMenus));

		// Peptide modifications as submenu of peptide features
		final JMenu subMenuPeptideModifications = new JMenu("Peptide modifications");
		final List<String> peptideModificationsMenus = new ArrayList<String>();
		peptideModificationsMenus.add(ChartType.PEPTIDE_MODIFICATION_DISTRIBUTION.getName());
		peptideModificationsMenus.add(ChartType.PEPTIDE_MONITORING.getName());
		peptideModificationsMenus.add(ChartType.MODIFICATED_PEPTIDE_NUMBER.getName());
		peptideModificationsMenus.add(ChartType.MODIFICATION_SITES_NUMBER.getName());
		addSubmenus(subMenuPeptideModifications, peptideModificationsMenus, menuPeptideFeatures);
		// add action to show right panel
		subMenuPeptideModifications
				.addMouseListener(getMouseListenerForShowingAttachedPanelForChartTypes(peptideModificationsMenus));

		final JMenu menuPeptideModifications = new JMenu("Peptide modifications");
		addSubmenus(menuPeptideModifications, peptideModificationsMenus, jMenuChartType);
		// add action to show right panel
		menuPeptideModifications
				.addMouseListener(getMouseListenerForShowingAttachedPanelForChartTypes(peptideModificationsMenus));

		// heatmaps
		final JMenu menuHeatMaps = new JMenu("Heatmaps");
		final List<String> heatMapsMenus = new ArrayList<String>();
		heatMapsMenus.add(ChartType.PROTEIN_OCURRENCE_HEATMAP.getName());
		heatMapsMenus.add(ChartType.PEPTIDE_OCCURRENCE_HEATMAP.getName());
		heatMapsMenus.add(ChartType.PEPTIDES_PER_PROTEIN_HEATMAP.getName());
		heatMapsMenus.add(ChartType.PSMS_PER_PROTEIN_HEATMAP.getName());
		heatMapsMenus.add(ChartType.PSMS_PER_PEPTIDE_HEATMAP.getName());
		heatMapsMenus.add(ChartType.PEPTIDE_PRESENCY_HEATMAP.getName());
		addSubmenus(menuHeatMaps, heatMapsMenus, jMenuChartType);
		// add action to show right panel
		menuHeatMaps.addMouseListener(getMouseListenerForShowingAttachedPanelForChartTypes(heatMapsMenus));

		// Gene mapping
		final JMenu menuHumanGenes = new JMenu("Human genes and chromosomes");
		final List<String> humanGenesMenus = new ArrayList<String>();
		humanGenesMenus.add(ChartType.HUMAN_CHROMOSOME_COVERAGE.getName());
		humanGenesMenus.add(ChartType.CHR_MAPPING.getName());
		humanGenesMenus.add(ChartType.CHR_PEPTIDES_MAPPING.getName());
		// humanGenesMenus.add(MENU_SEPARATION); // MENU SEPARATION
		// humanGenesMenus.add(CHR16_MAPPING);
		addSubmenus(menuHumanGenes, humanGenesMenus, jMenuChartType);
		// add action to show right panel
		menuHumanGenes.addMouseListener(getMouseListenerForShowingAttachedPanelForChartTypes(humanGenesMenus));

		// Peptide counting
		final JMenu menuPeptideCounting = new JMenu("Peptide counting");
		final List<String> peptideCountingMenus = new ArrayList<String>();
		peptideCountingMenus.add(ChartType.PEPTIDE_COUNTING_HISTOGRAM.getName());
		peptideCountingMenus.add(ChartType.PEPTIDE_COUNTING_VS_SCORE.getName());
		addSubmenus(menuPeptideCounting, peptideCountingMenus, jMenuChartType);
		// add action to show right panel
		menuPeptideCounting
				.addMouseListener(getMouseListenerForShowingAttachedPanelForChartTypes(peptideCountingMenus));

		// Metadata
		final JMenu menuMetadata = new JMenu("Metadata");
		final List<String> metadataMenus = new ArrayList<String>();
		metadataMenus.add(ChartType.SPECTROMETERS.getName());
		metadataMenus.add(ChartType.INPUT_PARAMETERS.getName());
		addSubmenus(menuMetadata, metadataMenus, jMenuChartType);
		// add action to show right panel
		menuMetadata.addMouseListener(getMouseListenerForShowingAttachedPanelForChartTypes(metadataMenus));

	}

	private MouseListener getMouseListenerForShowingAttachedPanelForChartTypes(List<String> idNumbersMenus) {
		final MouseListener ret = new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				showAttachedHelpDialog(idNumbersMenus);

			}

			@Override
			public void mouseExited(MouseEvent e) {
			}
		};
		return ret;
	}

	private void addSubmenus(JMenu menu, List<String> chartTypesForSubmenus, JMenu parentMenu) {

		for (final String chartType : chartTypesForSubmenus) {
			if (MENU_SEPARATION.equals(chartType)) {
				menu.addSeparator();
				// } else if (radioButtonMenuMap.containsKey(chartType)){
				// menu.add(radioButtonMenuMap.get(chartType));
				// }
			} else {
				final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(chartType);
				final ChartType chartTypeObj = ChartType.getFromName(chartType);
				final String menuTooltip = "<html><b>" + chartTypeObj.getName() + "</b><br>"
						+ ToolTipUtil.splitWordsInHTMLLines(chartTypeObj.getDescription(),
								menuItem.getFontMetrics(menuItem.getFont()), 300)
						+ "</html>";
				menuItem.setToolTipText(menuTooltip);
				chartTypeMenuButtonGroup.add(menuItem);
				menuItem.addItemListener(new ItemListener() {

					@Override
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							currentChartType = ChartType.getFromName(chartType);
							addCustomizationControls();
							startShowingChart(menuItem);
						}
						registerNewValue(menuItem);
					}
				});
				final List<String> list = new ArrayList<String>();
				list.add(chartType);
				menuItem.addMouseListener(getMouseListenerForShowingAttachedPanelForChartTypes(list));

				menu.add(menuItem);
			}
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
		final long t1 = System.currentTimeMillis();
		final int genesFromProteinGroup = GeneDistributionReader.getInstance()
				.getGenesFromProteinGroup(experimentList.getIdentifiedProteinGroups()).size();
		final int firstGenesFromProteinGroup = GeneDistributionReader.getInstance()
				.getFirstGenesFromProteinGroup(experimentList.getIdentifiedProteinGroups()).size();
		log.debug((System.currentTimeMillis() - t1) + " msg");

		String ret = "<html>PSMs: " + numPeptides + " (" + numDifferentPeptides + " unique peptides)<br>";
		ret += "Protein groups: " + numProteins;
		if (numDifferentProteins != numProteins) {
			ret += " (" + numDifferentProteins + " uniques)";
		}
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

		final String format = "#.##";
		DecimalFormat df = new java.text.DecimalFormat(format);

		final StringBuilder sb = new StringBuilder();
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
			} catch (final IllegalMiapeArgumentException ex) {
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
			} catch (final IllegalMiapeArgumentException ex) {
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
			} catch (final IllegalMiapeArgumentException ex) {
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
		final ChartType chartType = currentChartType;
		final String[] options = getOptionsByChartType(chartType);
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

	private String[] getOptionsByChartType(ChartType chartType) {
		// TODO
		String[] ret = null;

		if (
		// CHR16_MAPPING.equals(chartType) ||
		ChartType.CHR_MAPPING.equals(chartType) || ChartType.CHR_PEPTIDES_MAPPING.equals(chartType)
				|| ChartType.PROTEIN_NAME_CLOUD.equals(chartType)) {
			ret = new String[2];
			ret[0] = ONE_SERIES_PER_EXPERIMENT_LIST;
			ret[1] = ONE_CHART_PER_EXPERIMENT;
		} else if (ChartType.INPUT_PARAMETERS.equals(chartType) || ChartType.SPECTROMETERS.equals(chartType)) {
			ret = new String[2];
			ret[0] = ONE_SERIES_PER_REPLICATE;
			ret[1] = ONE_CHART_PER_EXPERIMENT;
		} else if (experimentList.getExperiments().size() > 1) {
			ret = new String[4];
			ret[0] = ONE_SERIES_PER_EXPERIMENT_LIST;
			ret[1] = ONE_SERIES_PER_EXPERIMENT;
			ret[2] = ONE_SERIES_PER_REPLICATE;
			ret[3] = ONE_CHART_PER_EXPERIMENT;

			if (chartType.equals(ChartType.FDR_VS_SCORE)) {

				ret = new String[3];
				ret[0] = ONE_SERIES_PER_EXPERIMENT_LIST;
				ret[1] = ONE_SERIES_PER_EXPERIMENT;
				ret[2] = ONE_CHART_PER_EXPERIMENT;

			} else if (chartType.equals(ChartType.PEPTIDE_REPEATABILITY)
					|| chartType.equals(ChartType.PROTEIN_REPEATABILITY)) {
				if (getOptionsFactory().getOverReplicates()) {
					ret = new String[2];
					ret[0] = ONE_SERIES_PER_EXPERIMENT;
					ret[1] = ONE_CHART_PER_EXPERIMENT;
				} else if (chartType.equals(ChartType.PROTEIN_REPEATABILITY)) {
					ret = new String[1];
					ret[0] = ONE_SERIES_PER_EXPERIMENT_LIST;
				}
			} else if (chartType.equals(ChartType.PROTEIN_OVERLAPING) || chartType.equals(ChartType.PEPTIDE_OVERLAPING)
					|| chartType.equals(ChartType.PROTEIN_SCORE_COMPARISON)
					|| chartType.equals(ChartType.PEPTIDE_SCORE_COMPARISON)
					|| chartType.equals(ChartType.EXCLUSIVE_PROTEIN_NUMBER)
					|| chartType.equals(ChartType.EXCLUSIVE_PEPTIDE_NUMBER)
					|| chartType.equals(ChartType.PEPTIDE_RT_COMPARISON)
					|| chartType.equals(ChartType.PEPTIDE_COUNTING_HISTOGRAM)
					|| chartType.equals(ChartType.PEPTIDE_COUNTING_VS_SCORE)) {
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
		final java.awt.GridBagConstraints gridBagConstraints;

		chartTypeMenuButtonGroup = new javax.swing.ButtonGroup();
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
		jPanelInformation = new javax.swing.JPanel();
		jLabelInformation1 = new javax.swing.JLabel();
		jLabelInformation2 = new javax.swing.JLabel();
		jLabelInformation3 = new javax.swing.JLabel();
		jButtonSeeAppliedFilters = new javax.swing.JButton();
		jButtonSeeAppliedFilters.setEnabled(false);
		jButtonShowTable = new javax.swing.JButton();
		jButtonSaveAsFiltered = new javax.swing.JButton();
		jButtonExport2PRIDE = new javax.swing.JButton();
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
		jMenuOptions = new javax.swing.JMenu();
		jMenuItemGeneralOptions = new javax.swing.JMenuItem();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Charts Viewer");
		getContentPane().setLayout(new BorderLayout(0, 0));

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

		jButtonHelp = new OpenHelpButton(this);
		final javax.swing.GroupLayout jPanelStatusLayout = new javax.swing.GroupLayout(jPanelStatus);
		jPanelStatusLayout
				.setHorizontalGroup(jPanelStatusLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(jPanelStatusLayout.createSequentialGroup().addContainerGap()
								.addGroup(jPanelStatusLayout.createParallelGroup(Alignment.LEADING)
										.addGroup(jPanelStatusLayout.createSequentialGroup()
												.addComponent(jProgressBarMemoryUsage, GroupLayout.DEFAULT_SIZE, 983,
														Short.MAX_VALUE)
												.addContainerGap())
										.addGroup(jPanelStatusLayout.createSequentialGroup()
												.addComponent(jScrollPane3, GroupLayout.DEFAULT_SIZE, 983,
														Short.MAX_VALUE)
												.addContainerGap())
										.addGroup(Alignment.TRAILING,
												jPanelStatusLayout.createSequentialGroup().addComponent(jButtonHelp)
														.addContainerGap())
										.addGroup(Alignment.TRAILING,
												jPanelStatusLayout
														.createSequentialGroup().addComponent(jProgressBar,
																GroupLayout.DEFAULT_SIZE, 983, Short.MAX_VALUE)
														.addContainerGap()))));
		jPanelStatusLayout.setVerticalGroup(jPanelStatusLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(jPanelStatusLayout.createSequentialGroup()
						.addComponent(jScrollPane3, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(jProgressBar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(jProgressBarMemoryUsage, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(jButtonHelp).addContainerGap()));
		jPanelStatus.setLayout(jPanelStatusLayout);
		getContentPane().add(jPanelStatus, BorderLayout.PAGE_END);

		jPanelChartType.setBorder(javax.swing.BorderFactory
				.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Comparison level"));

		jComboBoxChartOptions.addActionListener(new java.awt.event.ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				jComboBoxChartOptionsItemStateChanged();
			}
		});

		final javax.swing.GroupLayout jPanelChartTypeLayout = new javax.swing.GroupLayout(jPanelChartType);
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
		jPanelAdditionalCustomizations.getVerticalScrollBar().setUnitIncrement(16);
		final javax.swing.GroupLayout jPanelAddOptionsLayout = new javax.swing.GroupLayout(jPanelAddOptions);
		jPanelAddOptions.setLayout(jPanelAddOptionsLayout);
		jPanelAddOptionsLayout.setHorizontalGroup(jPanelAddOptionsLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 324, Short.MAX_VALUE));
		jPanelAddOptionsLayout.setVerticalGroup(jPanelAddOptionsLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 394, Short.MAX_VALUE));

		jPanelAdditionalCustomizations.setViewportView(jPanelAddOptions);

		jPanelPeptideCounting.setBorder(
				new TitledBorder(null, "Peptide counting", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		jCheckBoxUniquePeptides.setText("distinguish modified peptides");
		jCheckBoxUniquePeptides.setToolTipText(
				"<html>If this option is not selected:<br>\nA peptide identified as unmodified and for<br>\ninstance containing an oxidized Methionine is<br>\ncounted as one.<br>\n</html>");
		jCheckBoxUniquePeptides.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jCheckBoxUniquePeptidesItemStateChanged();
			}
		});

		final javax.swing.GroupLayout jPanelPeptideCountingLayout = new javax.swing.GroupLayout(jPanelPeptideCounting);
		jPanelPeptideCountingLayout.setHorizontalGroup(jPanelPeptideCountingLayout
				.createParallelGroup(Alignment.LEADING).addGroup(jPanelPeptideCountingLayout.createSequentialGroup()
						.addContainerGap().addComponent(jCheckBoxUniquePeptides).addContainerGap(55, Short.MAX_VALUE)));
		jPanelPeptideCountingLayout.setVerticalGroup(jPanelPeptideCountingLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanelPeptideCountingLayout.createSequentialGroup().addComponent(jCheckBoxUniquePeptides)
						.addContainerGap(30, Short.MAX_VALUE)));
		jPanelPeptideCounting.setLayout(jPanelPeptideCountingLayout);

		jPanelInformation.setBorder(javax.swing.BorderFactory
				.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Global Information"));

		jButtonSeeAppliedFilters.setIcon(new javax.swing.ImageIcon(
				"C:\\Users\\Salva\\workspace\\miape-extractor\\src\\main\\resources\\funnel.png")); // NOI18N
		jButtonSeeAppliedFilters.setToolTipText("Show currently applied filters.");
		jButtonSeeAppliedFilters.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonSeeAppliedFiltersActionPerformed(evt);
			}
		});

		jButtonShowTable.setIcon(new javax.swing.ImageIcon(
				"C:\\Users\\Salva\\workspace\\miape-extractor\\src\\main\\resources\\table.png")); // NOI18N
		jButtonShowTable.setToolTipText(
				"<html>\r\nClick here to show the whole dataset in a table.<br>\r\nYou will be able to sort and filter data quickly.\r\n</html>");
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
				"<html>\r\nSave the datasets of the project as <b>curated datasets</b>.<br>\r\nThis will save the datasets <b>AFTER</b> aplying some filters.<br>\r\nThey will be saved individually in a separate location and they will<br>\r\nbe available to be added to new comparison projects in the dropdown<br>\r\ncontrol of the Comparison Projects Manager.\r\n</html>");
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
				"<html>\r\nClick here to <b>export</b> current data to <b>PRIDE XML</b>.<br>\r\nA single PRIDE XML file will be created for each one level 1 node,<br>\r\nintegrating all information in that node in a single file.<br>\r\nIf the datasets were created with some peak list files associated,<br>\r\nthe generated PRIDE XML will include the spectra on it.\r\n</html>");
		jButtonExport2PRIDE.setEnabled(false);
		jButtonExport2PRIDE.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonExport2PRIDEActionPerformed(evt);
			}
		});

		jButtonExport2Excel.setIcon(new javax.swing.ImageIcon(
				"C:\\Users\\Salva\\workspace\\miape-extractor\\src\\main\\resources\\excel_table.png")); // NOI18N
		jButtonExport2Excel.setToolTipText(
				"<html>\r\nClick here <b>export</b> current data to a<br>\r\n<b>Tab Separated Values (TSV) formatted file</b> that <br>\r\ncan be opened by Excel.</html>");
		jButtonExport2Excel.setEnabled(false);
		jButtonExport2Excel.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonExport2ExcelActionPerformed(evt);
			}
		});

		jButtonCancel = new JButton();
		jButtonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelTask();
			}
		});
		jButtonCancel.setToolTipText(
				"<html>\r\nClick here to <b>Cancel current task</b>,<br>\r\nthat is, data loading or chart creation.\r\n</html>");
		jButtonCancel.setEnabled(false);

		final javax.swing.GroupLayout jPanelInformationLayout = new javax.swing.GroupLayout(jPanelInformation);
		jPanelInformationLayout
				.setHorizontalGroup(jPanelInformationLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(jPanelInformationLayout.createSequentialGroup().addContainerGap()
								.addGroup(jPanelInformationLayout.createParallelGroup(Alignment.LEADING)
										.addComponent(jLabelInformation3, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE,
												292, Short.MAX_VALUE)
										.addComponent(jLabelInformation2, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE,
												292, Short.MAX_VALUE)
										.addComponent(jLabelInformation1, GroupLayout.DEFAULT_SIZE, 292,
												Short.MAX_VALUE)
										.addGroup(jPanelInformationLayout.createSequentialGroup()
												.addGroup(jPanelInformationLayout.createParallelGroup(Alignment.LEADING)
														.addGroup(jPanelInformationLayout.createSequentialGroup()
																.addComponent(jButtonSeeAppliedFilters,
																		GroupLayout.PREFERRED_SIZE, 47,
																		GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(ComponentPlacement.RELATED)
																.addComponent(jButtonSaveAsFiltered,
																		GroupLayout.PREFERRED_SIZE, 47,
																		GroupLayout.PREFERRED_SIZE))
														.addComponent(jButtonExport2PRIDE, GroupLayout.DEFAULT_SIZE,
																101, Short.MAX_VALUE))
												.addPreferredGap(ComponentPlacement.RELATED)
												.addGroup(jPanelInformationLayout.createParallelGroup(Alignment.LEADING)
														.addGroup(jPanelInformationLayout.createSequentialGroup()
																.addComponent(jButtonShowTable,
																		GroupLayout.PREFERRED_SIZE, 47,
																		GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(ComponentPlacement.RELATED)
																.addComponent(jButtonExport2Excel,
																		GroupLayout.PREFERRED_SIZE, 46,
																		GroupLayout.PREFERRED_SIZE))
														.addComponent(jButtonCancel, GroupLayout.PREFERRED_SIZE, 101,
																GroupLayout.PREFERRED_SIZE))
												.addGap(54)))
								.addContainerGap()));
		jPanelInformationLayout.setVerticalGroup(jPanelInformationLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanelInformationLayout.createSequentialGroup()
						.addComponent(jLabelInformation1, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(jLabelInformation2, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(jLabelInformation3, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(jPanelInformationLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(jButtonShowTable, GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
								.addComponent(jButtonSeeAppliedFilters, GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jButtonSaveAsFiltered, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(jButtonExport2Excel, 0, 0, Short.MAX_VALUE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(jPanelInformationLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(jButtonExport2PRIDE).addComponent(jButtonCancel,
										GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE))
						.addContainerGap()));
		jPanelInformation.setLayout(jPanelInformationLayout);

		final javax.swing.GroupLayout jPanelLeftLayout = new javax.swing.GroupLayout(jPanelLeft);
		jPanelLeftLayout.setHorizontalGroup(jPanelLeftLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(jPanelLeftLayout.createSequentialGroup().addGroup(jPanelLeftLayout
						.createParallelGroup(Alignment.TRAILING)
						.addComponent(jPanelAdditionalCustomizations, GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
						.addComponent(jPanelPeptideCounting, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 280,
								Short.MAX_VALUE)
						.addComponent(jPanelInformation, GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE).addComponent(
								jPanelChartType, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE))
						.addContainerGap()));
		jPanelLeftLayout.setVerticalGroup(jPanelLeftLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanelLeftLayout.createSequentialGroup().addContainerGap()
						.addComponent(jPanelInformation, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(jPanelChartType, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(jPanelPeptideCounting, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(jPanelAdditionalCustomizations, GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)));
		jPanelLeft.setLayout(jPanelLeftLayout);
		getContentPane().add(jPanelLeft, BorderLayout.WEST);

		jPanelRigth.setBorder(
				javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Chart"));

		jScrollPaneChart.setBorder(null);
		jScrollPaneChart.getVerticalScrollBar().setUnitIncrement(16);
		jScrollPaneChart.setViewportView(jPanelChart);
		final GridBagLayout gbl_jPanelChart = new GridBagLayout();
		gbl_jPanelChart.columnWidths = new int[] { 0 };
		gbl_jPanelChart.rowHeights = new int[] { 0 };
		gbl_jPanelChart.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_jPanelChart.rowWeights = new double[] { Double.MIN_VALUE };
		jPanelChart.setLayout(gbl_jPanelChart);
		getContentPane().add(jPanelRigth, BorderLayout.CENTER);
		jPanelRigth.setLayout(new BorderLayout(0, 0));
		jPanelRigth.add(jScrollPaneChart);
		// jPanelRigth.setBackground(Color.white);

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

		final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		final int screenSizeWidth = screenSize.width;
		final int screenSizeHeight = screenSize.height;
		final int windowWidth = screenSizeWidth / 2;
		final int windowHeight = screenSizeHeight * 2 / 3;
		setPreferredSize(new Dimension(960, 860));
		final int x = screenSizeWidth / 4;
		final int y = screenSizeHeight / 6;
		setBounds(x, y, 1037, 900);

	}// </editor-fold>

	protected void cancelTask() {
		if (dataLoader != null && dataLoader.getState() == StateValue.STARTED) {
			log.info("Cancelling data loader");
			appendStatus("Cancelling data loader...");
			cancelDataLoader();
		}
		if (chartCreator != null && chartCreator.getState() == StateValue.STARTED) {
			log.info("Cancelling chart creator");
			appendStatus("Cancelling chart creator");
			cancelChartCreator();
		}
	}

	private void cancelDataLoader() {
		if (dataLoader != null && dataLoader.getState() == StateValue.STARTED) {
			boolean canceled = dataLoader.cancel(true);
			while (!canceled) {
				try {
					log.info("Waiting for cancelling data loader");
					Thread.sleep(100);
					canceled = dataLoader.cancel(true);
				} catch (final InterruptedException e) {
				}
			}

		}
	}

	private void cancelChartCreator() {
		if (chartCreator != null && chartCreator.getState() == StateValue.STARTED) {
			boolean canceled = chartCreator.cancel(true);
			while (!canceled) {
				try {
					log.info("Waiting for cancelling chartCreator");
					Thread.sleep(100);
					canceled = chartCreator.cancel(true);

				} catch (final InterruptedException e) {
				}
			}

		}
	}

	private void jMenuItemGeneralOptionsActionPerformed(java.awt.event.ActionEvent evt) {
		GeneralOptionsDialog.getInstance(this, false).setVisible(true);
	}

	private void jButtonExport2ExcelActionPerformed(java.awt.event.ActionEvent evt) {
		exportToTSV();
	}

	private void jButtonExport2PRIDEActionPerformed(java.awt.event.ActionEvent evt) {
		exportToPRIDE();
	}

	private void exportToPEX() {
		// do not do it if data loading or char creator are running
		if ((dataLoader != null && dataLoader.getState() == StateValue.STARTED)
				|| (chartCreator != null && chartCreator.getState() == StateValue.STARTED)) {
			appendStatus("Please, wait until task is done.");
		} else {
			pexSubmissionDialog = new PEXBulkSubmissionSummaryFileCreatorDialog(this, experimentList);
			pexSubmissionDialog.setVisible(true);
		}
	}

	private void jCheckBoxMenuItemPeptideForMRMFilterActionPerformed(java.awt.event.ActionEvent evt) {
		filterDialog.setPeptideForMRMFilterEnabled(jCheckBoxMenuItemPeptideForMRMFilter.isSelected());
		if (jCheckBoxMenuItemPeptideForMRMFilter.isSelected()) {
			if (filterDialog.getPeptideForMRMFilter() == null) {
				filterDialog.setCurrentIndex(FiltersDialog.PEPTIDEFORMRMFILTER_INDEX);
				filterDialog.setVisible(true);
			}
		}
		startShowingChart(jCheckBoxMenuItemPeptideForMRMFilter);
	}

	private void jCheckBoxMenuItemPeptideSequenceFilterActionPerformed(java.awt.event.ActionEvent evt) {
		filterDialog.setPeptideSequencesFilterEnabled(jCheckBoxMenuItemPeptideSequenceFilter.isSelected());
		if (jCheckBoxMenuItemPeptideSequenceFilter.isSelected()) {
			if (filterDialog.getPeptideSequencesFilter() == null) {
				filterDialog.setCurrentIndex(FiltersDialog.PEPTIDESEQUENCEFILTER_INDEX);
				filterDialog.setVisible(true);
			}
		}
		startShowingChart(jCheckBoxMenuItemPeptideSequenceFilter);
	}

	private void jButtonShowTableMouseClicked(java.awt.event.MouseEvent evt) {

	}

	private void jButtonSaveAsFilteredActionPerformed(java.awt.event.ActionEvent evt) {
		saveAsCuratedExperiment();
	}

	private void saveAsCuratedExperiment() {
		curatedExperimentSaver = new CuratedExperimentSaver(this, cfgFile, experimentList,
				OntologyLoaderTask.getCvManager(),
				GeneralOptionsDialog.getInstance(this).isDoNotGroupNonConclusiveProteins(),
				GeneralOptionsDialog.getInstance(this).isSeparateNonConclusiveProteins());
		curatedExperimentSaver.addPropertyChangeListener(this);
		curatedExperimentSaver.execute();

	}

	private void jButtonShowTableActionPerformed(java.awt.event.ActionEvent evt) {
		showIdentificationTable();
	}

	private void showIdentificationTable() {
		if ((dataLoader != null && dataLoader.getState() == StateValue.STARTED)
				|| (chartCreator != null && chartCreator.getState() == StateValue.STARTED)) {
			appendStatus("Please, wait until task is done.");
		} else {
			log.info("Showing identification table");
			// if (this.identificationTable == null)
			identificationTable = IdentificationTableFrame.getInstance(this, toSet(experimentList), filtered);

			identificationTable.setVisible(true);
		}

	}

	private void jButtonSeeAppliedFiltersActionPerformed(java.awt.event.ActionEvent evt) {
		showAppliedFiltersDialog();
	}

	private void showAppliedFiltersDialog() {
		filtersDialog = new AppliedFiltersDialog(this, true, filterDialog.getFilters());
		filtersDialog.setVisible(true);
	}

	private void jCheckBoxMenuItemPeptideLenthFilterActionPerformed(java.awt.event.ActionEvent evt) {
		filterDialog.setPeptideLengthFilterEnabled(jCheckBoxMenuItemPeptideLenthFilter.isSelected());
		if (jCheckBoxMenuItemPeptideLenthFilter.isSelected()) {
			if (filterDialog.getPeptideLengthFilter() == null) {
				filterDialog.setCurrentIndex(FiltersDialog.PEPTIDELENGTHFILTER_INDEX);
				filterDialog.setVisible(true);
			}
		}
		startShowingChart(jCheckBoxMenuItemPeptideLenthFilter);
	}

	private void jCheckBoxMenuItemPeptideNumberFilterActionPerformed(java.awt.event.ActionEvent evt) {
		filterDialog.setPeptideNumberFilterEnabled(jCheckBoxMenuItemPeptideNumberFilter.isSelected());
		if (jCheckBoxMenuItemPeptideNumberFilter.isSelected()) {
			if (filterDialog.getPeptideNumberFilter() == null) {
				filterDialog.setCurrentIndex(FiltersDialog.PEPTIDENUMBERFILTER_INDEX);
				filterDialog.setVisible(true);
			}
		}
		startShowingChart(jCheckBoxMenuItemPeptideNumberFilter);
	}

	private void jCheckBoxMenuItemProteinACCFilterActionPerformed(java.awt.event.ActionEvent evt) {
		filterDialog.setProteinACCFilterEnabled(jCheckBoxMenuItemProteinACCFilter.isSelected());
		if (jCheckBoxMenuItemProteinACCFilter.isSelected()) {
			if (filterDialog.getProteinACCFilter() == null) {
				filterDialog.setCurrentIndex(FiltersDialog.PROTEINACCFILTER_INDEX);
				filterDialog.setVisible(true);
			}
		}
		startShowingChart(jCheckBoxMenuItemProteinACCFilter);
	}

	private void jCheckBoxMenuItemModificationFilterActionPerformed(java.awt.event.ActionEvent evt) {
		filterDialog.setModificationFilterEnabled(jCheckBoxMenuItemModificationFilter.isSelected());
		if (jCheckBoxMenuItemModificationFilter.isSelected()) {
			if (filterDialog.getModificationFilter() == null) {
				filterDialog.setCurrentIndex(FiltersDialog.MODIFICATIONFILTER_INDEX);
				filterDialog.setVisible(true);
			}
		}
		startShowingChart(jCheckBoxMenuItemModificationFilter);
	}

	private void exportToTSV() {
		if ((dataLoader != null && dataLoader.getState() == StateValue.STARTED)
				|| (chartCreator != null && chartCreator.getState() == StateValue.STARTED)) {
			appendStatus("Please, wait until task is done.");
		} else {
			final ExporterDialog exporterDialog = new ExporterDialog(this, this, toSet(experimentList), null);
			exporterDialog.setVisible(true);
		}
	}

	private void exportToPRIDE() {
		if ((dataLoader != null && dataLoader.getState() == StateValue.STARTED)
				|| (chartCreator != null && chartCreator.getState() == StateValue.STARTED)) {
			appendStatus("Please, wait until task is done.");
		} else {
			final ExporterToPRIDEDialog exporterDialog = new ExporterToPRIDEDialog(this, experimentList);
			exporterDialog.setVisible(true);
		}
	}

	private void jComboBoxChartOptionsItemStateChanged() {
		// if (evt.getStateChange() == ItemEvent.SELECTED) {
		if (dataLoader == null || !dataLoader.isDone())
			return;
		// add additional customizations
		addCustomizationControls();
		// repaint
		startShowingChart(jComboBoxChartOptions);
		// } else {
		// log.info(evt.getStateChange());
		// }
	}

	private void jCheckBoxMenuItemOcurrenceFilterActionPerformed(java.awt.event.ActionEvent evt) {
		filterDialog.setOccurrenceFilterEnabled(jCheckBoxMenuItemOcurrenceFilter.isSelected());
		if (jCheckBoxMenuItemOcurrenceFilter.isSelected()) {
			if (filterDialog.getOccurrenceFilter() == null) {
				filterDialog.setCurrentIndex(FiltersDialog.OCCURRENCEFILTER_INDEX);
				filterDialog.setVisible(true);
			}
		}
		startShowingChart(jCheckBoxMenuItemOcurrenceFilter);
	}

	private void jCheckBoxMenuItemScoreFiltersActionPerformed(java.awt.event.ActionEvent evt) {
		filterDialog.setScoreFilterEnabled(jCheckBoxMenuItemScoreFilters.isSelected());
		if (jCheckBoxMenuItemScoreFilters.isSelected()) {
			final List<ScoreFilter> filters = filterDialog.getScoreFilters(IdentificationItemEnum.PROTEIN);
			filters.addAll(filterDialog.getScoreFilters(IdentificationItemEnum.PEPTIDE));
			if (filters.isEmpty()) {
				filterDialog.setCurrentIndex(FiltersDialog.SCOREFILTER_INDEX);
				filterDialog.setVisible(true);
			} else {
				boolean thereIsScoreFilterDefined = false;
				for (final Filter filter : filters) {
					if (filter instanceof ScoreFilter)
						thereIsScoreFilterDefined = true;
				}
				if (!thereIsScoreFilterDefined) {
					filterDialog.setCurrentIndex(FiltersDialog.SCOREFILTER_INDEX);
					filterDialog.setVisible(true);
				}
			}
		}
		startShowingChart(jCheckBoxMenuItemScoreFilters);
	}

	private void jCheckBoxMenuItemFDRFilterActionPerformed(java.awt.event.ActionEvent evt) {
		log.info("FDRFILTER ACTION EVENT!!: from " + evt.getSource().getClass().getCanonicalName() + " "
				+ evt.getActionCommand() + " id:" + evt.getID() + " modifiers:" + evt.getModifiers());

		filterDialog.setFDRFilterEnabled(jCheckBoxMenuItemFDRFilter.isSelected());
		if (jCheckBoxMenuItemFDRFilter.isSelected()) {
			final List<Filter> filters = filterDialog.getFilters();
			if (filters.isEmpty()) {
				log.info("There is not filters defined");
				filterDialog.setCurrentIndex(FiltersDialog.FDRFILTER_INDEX);
				filterDialog.setVisible(true);
				// Do not show the chart until the definition of the filters
				return;
			} else {
				// log.info("There are " + filters.size() + " filters defined");
				boolean thereIsFDRFilterDefined = false;
				for (final Filter filter : filters) {
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
		startShowingChart(jCheckBoxMenuItemFDRFilter);

	}

	private void jMenuItemDefineFiltersActionPerformed(java.awt.event.ActionEvent evt) {
		showFilterDefinitionDialog();
	}

	private void showFilterDefinitionDialog() {
		log.info("Showing the filter definition dialog");

		filterDialog.setVisible(true);
	}

	private void jCheckBoxUniquePeptidesItemStateChanged() {

		optionsFactory.updatePeptideSequenceList();
		startShowingChart(jCheckBoxUniquePeptides);

	}

	public void addCustomizationControls() {
		// TODO add more customizations if you add more chart types

		final ChartType chartType = currentChartType;
		final String options = (String) jComboBoxChartOptions.getSelectedItem();

		// remove all content
		jPanelAddOptions.removeAll();

		if (ChartType.PROTEIN_SCORE_DISTRIBUTION.equals(chartType)
				|| ChartType.PEPTIDE_SCORE_DISTRIBUTION.equals(chartType)) {
			addLineHistogramControls(true, false);
		} else if (ChartType.PROTEIN_COVERAGE_DISTRIBUTION.equals(chartType)) {
			addLineHistogramControls(false, false);
		} else if (ChartType.PEPTIDE_MASS_DISTRIBUTION.equals(chartType)) {
			addLineHistogramControls(false, true);
		} else if (ChartType.PROTEIN_SCORE_COMPARISON.equals(chartType)
				|| ChartType.PEPTIDE_SCORE_COMPARISON.equals(chartType)) {
			addScoreComparisonControls(options);
		} else if (ChartType.PEPTIDE_NUMBER_HISTOGRAM.equals(chartType)
				|| ChartType.PROTEIN_NUMBER_HISTOGRAM.equals(chartType)) {
			addHistogramBarControls(ChartType.PEPTIDE_NUMBER_HISTOGRAM.equals(chartType));
		} else if (ChartType.PROTEIN_COVERAGE.equals(chartType)) {
			addProteinCoverageHistogramBarControls();
		} else if (ChartType.PEPTIDE_CHARGE_HISTOGRAM.equals(chartType)) {
			addChargeHistogramBarControls();
		} else if (ChartType.PEPTIDE_OVERLAPING.equals(chartType) || ChartType.PROTEIN_OVERLAPING.equals(chartType)) {
			addOverlapingControls(options, 0, false, true);
		} else if (ChartType.EXCLUSIVE_PROTEIN_NUMBER.equals(chartType)
				|| ChartType.EXCLUSIVE_PEPTIDE_NUMBER.equals(chartType)) {
			addOverlapingControls(options, null, false, false);
		} else if (ChartType.PEPTIDES_PER_PROTEIN_HEATMAP.equals(chartType)
				|| ChartType.PSMS_PER_PEPTIDE_HEATMAP.equals(chartType)
				|| ChartType.PSMS_PER_PROTEIN_HEATMAP.equals(chartType)) {
			addHeatMapControls(false);
		} else if (ChartType.PEPTIDE_OCCURRENCE_HEATMAP.equals(chartType)
				|| ChartType.PROTEIN_OCURRENCE_HEATMAP.equals(chartType)) {
			addHeatMapControls(true);
		} else if (ChartType.MODIFICATION_SITES_NUMBER.equals(chartType)
				|| ChartType.MODIFICATED_PEPTIDE_NUMBER.equals(chartType)) {
			addPeptideModificationControls(true);
		} else if (ChartType.PEPTIDE_MODIFICATION_DISTRIBUTION.equals(chartType)) {
			addPeptideModificationControls(false);
		} else if (ChartType.PEPTIDE_MONITORING.equals(chartType)) {
			addPeptideMonitoringControls();
		} else if (ChartType.PROTEIN_REPEATABILITY.equals(chartType)
				|| ChartType.PEPTIDE_REPEATABILITY.equals(chartType)) {
			addRepeatibilityControls();
		} else if (ChartType.PROTEIN_SENSITIVITY_SPECIFICITY.equals(chartType)) {
			addSensitivitySpecificityControls();
		} else if (ChartType.MISSEDCLEAVAGE_DISTRIBUTION.equals(chartType)) {
			addMissedCleavageDistributionControls(options);
		} else if (ChartType.PEPTIDE_LENGTH_DISTRIBUTION.equals(chartType)) {
			addPeptideLengthDistributionControls(options);
			// } else if (CHR16_MAPPING.equals(chartType)) {
			// // this.isChr16ChartShowed = true;
			// addChr16MappingControls();
		} else if (ChartType.SINGLE_HIT_PROTEINS.equals(chartType)) {
			addSingleHitProteinControls();
		} else if (ChartType.PEPTIDE_NUMBER_IN_PROTEINS.equals(chartType)) {
			addPeptideNumberInProteinsControls();
		} else if (ChartType.DELTA_MZ_OVER_MZ.equals(chartType)) {
			addDeltaOverZControls(options);
		} else if (ChartType.PSM_PEP_PROT.equals(chartType)) {
			addPSM_PEP_PROT_Controls(options);
		} else if (ChartType.FDR_VS_SCORE.equals(chartType)) {
			addFDR_VS_Score_Controls(options);
		} else if (ChartType.CHR_MAPPING.equals(chartType)) {
			addAllHumanChromosomeMappingControls();
		} else if (ChartType.CHR_PEPTIDES_MAPPING.equals(chartType)) {
			addAllHumanChromosomePeptideMappingControls();
		} else if (ChartType.FDR.equals(chartType)) {
			addFDR_Plots_Controls();
		} else if (ChartType.PEPTIDE_PRESENCY_HEATMAP.equals(chartType)) {
			addPeptidePresencyControls();
		} else if (ChartType.PEPTIDE_NUM_PER_PROTEIN_MASS.equals(chartType)) {
			addLineHistogramControls(false, false);
		} else if (ChartType.HUMAN_CHROMOSOME_COVERAGE.equals(chartType)) {
			addHumanChromosomeCoverageControls();
		} else if (ChartType.PROTEIN_NAME_CLOUD.equals(chartType)) {
			addWordCramControls();
		} else if (ChartType.PROTEIN_GROUP_TYPES.equals(chartType)) {
			addProteingGroupTypesDistributionControls(options);
		} else if (ChartType.PEPTIDE_RT_COMPARISON.equals(chartType)) {
			addPeptideRTComparisonControls(options);
		} else if (ChartType.PEPTIDE_RT.equals(chartType)) {
			addPeptideRTControls(options);
		} else if (ChartType.SINGLE_RT_COMPARISON.equals(chartType)) {
			addSingleRTControls(options);
		} else if (ChartType.PEPTIDE_COUNTING_HISTOGRAM.equals(chartType)
				|| ChartType.PEPTIDE_COUNTING_VS_SCORE.equals(chartType)) {
			addPeptideCoutingControls(options);
		}
		jPanelAddOptions.repaint();
	}

	private void addPeptideCoutingControls(String options) {
		jPanelAddOptions.setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();

		// Just in case of proteins

		log.info("Creating list of replicates...");

		c.gridy = 0;

		if (ChartType.PEPTIDE_COUNTING_VS_SCORE.equals(currentChartType)) {
			final DefaultComboBoxModel peptideScoreNames = getPeptideScoreNames();
			jPanelAddOptions.add(optionsFactory.getPeptideScorePanel(peptideScoreNames), c);
		}
		if (ChartType.PEPTIDE_COUNTING_HISTOGRAM.equals(currentChartType)) {
			c.gridy++;
			jPanelAddOptions.add(optionsFactory.getBinsPanel(), c);
		}
		c.gridy++;
		jPanelAddOptions.add(optionsFactory.getHistogramTypePanel(), c);
		c.gridy++;
		jPanelAddOptions.add(new JLabel("Protein groups are equal if (select one option):"), c);
		// take into account just one protein per group
		c.gridy++;
		jPanelAddOptions.add(optionsFactory.getJcheckBoxOneProteinPerGroup(), c);

		c.gridy++;
		jPanelAddOptions.add(new JLabel("<html><br></html>"), c);
		c.gridy++;
		jPanelAddOptions.add(new JLabel("Select from the following checkBoxes:"), c);
		c.gridy++;

		if (options.equals(ChartManagerFrame.ONE_CHART_PER_EXPERIMENT)) {
			jPanelAddOptions.add(optionsFactory.getExperimentsCheckboxes(false, 2, false, null), c);
		} else if (options.equals(ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT)) {
			jPanelAddOptions.add(optionsFactory.getExperimentsCheckboxes(false, 2, false, null), c);
		} else if (options.equals(ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST)) {
			jPanelAddOptions.add(optionsFactory.getExperimentsCheckboxes(false, 2, false, null), c);
		} else if (options.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			jPanelAddOptions.add(optionsFactory.getReplicatesCheckboxes(false, false, 2, false, null), c);
		}

		jPanelAddOptions.repaint();
	}

	private void addSingleRTControls(String options) {
		jPanelAddOptions.removeAll();
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		final JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();
		c.gridx = 0;
		c.gridy = 0;
		panel.add(jPanelAdditional1, c);

		// ////////////// ROW2
		final JPanel jPanelAdditional2 = optionsFactory.getPeptideSequencesPanel(true);

		c.gridx = 0;
		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		panel.add(jPanelAdditional2, c);

		final JLabel jlabel = new JLabel("(Multiple selections are allowed)");
		jlabel.setToolTipText(
				"<html>Multiple selections are allowed:<br>Press CTRL key and select more than one modification.</html>");
		c.gridy++;
		panel.add(jlabel, c);

		// /////////////// ROW5
		final JCheckBox checkbox6 = optionsFactory.getShowInMinutesCheckBox();
		c.gridx = 0;
		c.gridy++;
		panel.add(checkbox6, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);
	}

	private void addPeptideRTControls(String options) {
		jPanelAddOptions.removeAll();
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		final JPanel jPanelAdditional1 = optionsFactory.getHistogramTypePanel();
		c.gridx = 0;
		c.gridy = 0;
		panel.add(jPanelAdditional1, c);

		// //////////////// ROW2
		final JPanel jPanelAdditional2 = optionsFactory.getBinsPanel();

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
		if (!ONE_SERIES_PER_EXPERIMENT_LIST.equals(options)) {
			final JLabel label = new JLabel("Select from the following checkBoxes:");
			c.gridy++;
			panel.add(label, c);
			JPanel checkboxesPanel = null;
			if (ONE_CHART_PER_EXPERIMENT.equals(options)) {
				checkboxesPanel = optionsFactory.getReplicatesCheckboxes(true, false, 2, false, null);
			} else if (ONE_SERIES_PER_EXPERIMENT.equals(options)) {
				checkboxesPanel = optionsFactory.getExperimentsCheckboxes(false, 2, false, null);
			} else if (ONE_SERIES_PER_REPLICATE.equals(options)) {
				checkboxesPanel = optionsFactory.getReplicatesCheckboxes(false, false, 2, false, null);
			}

			c.gridy++;
			panel.add(checkboxesPanel, c);
		}
	}

	private void addPeptideRTComparisonControls(String options) {
		if (options == null)
			options = ChartManagerFrame.ONE_CHART_PER_EXPERIMENT;
		jPanelAddOptions.removeAll();
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;

		// //////////////// ROW3
		final JPanel jPanelAdditional5 = optionsFactory.getShowRegressionLinePanel();
		panel.add(jPanelAdditional5, c);
		// //////////////// ROW4
		final JPanel jPanelAdditional6 = optionsFactory.getShowDiagonalLinePanel();
		c.gridy++;
		panel.add(jPanelAdditional6, c);

		final JCheckBox showInMinutesCheckBox = optionsFactory.getShowInMinutesCheckBox();
		c.gridy++;
		panel.add(showInMinutesCheckBox, c);

		if (!ONE_SERIES_PER_EXPERIMENT_LIST.equals(options)) {
			final JLabel label = new JLabel("Select from the following checkBoxes:");
			c.gridy++;
			panel.add(label, c);
			JPanel checkboxesPanel = null;
			if (ONE_CHART_PER_EXPERIMENT.equals(options)) {
				checkboxesPanel = optionsFactory.getReplicatesCheckboxes(true, false, 2, false, null);
			} else if (ONE_SERIES_PER_EXPERIMENT.equals(options)) {
				checkboxesPanel = optionsFactory.getExperimentsCheckboxes(false, 2, false, null);
			} else if (ONE_SERIES_PER_REPLICATE.equals(options)) {
				checkboxesPanel = optionsFactory.getReplicatesCheckboxes(false, false, 2, false, null);
			}

			c.gridy++;
			panel.add(checkboxesPanel, c);
		}

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	private void addWordCramControls() {
		jPanelAddOptions.removeAll();
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
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
		final String tooltip = "Words in this text area will not be shown in the protein cloud";
		label2.setToolTipText(tooltip);
		c.gridy++;
		final JTextArea skipWordsTextArea = optionsFactory.getSkipWordsTextArea();
		skipWordsTextArea.setWrapStyleWord(true);
		final JScrollPane scrollPane = new JScrollPane(skipWordsTextArea);

		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

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
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
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
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);

		// /////////////// ROW1
		final JCheckBox jCheckBox2 = optionsFactory.getHeatMapBinaryCheckBox(false);
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		panel.add(jCheckBox2, c);
		// /////////////// ROW2
		final JPanel jPanelAdditional3 = optionsFactory.getUserPeptideListPanel(false);
		c.gridy++;
		panel.add(jPanelAdditional3, c);
		// /////////////// ROW3
		final JLabel jlabel = new JLabel("<html><b>Important</b>:<br>If peptide containing modifications are <br>"
				+ "inserted here, <font color='RED'>enable the 'distinguish mod.<br>"
				+ "and unmod.' checkbox above</font>.<br><b>Examples</b>:<br>Without modifications:<br><ul><li>CSVFYGAPSK</li><li>DNQRPSGVPDR</li></ul>Containing modifications (modifications are<br>"
				+ "specified inserting the monoisotopic mass delta<br>between braquets):<br><ul><li>C(+57.02)SVFYGAPSK</li><li>DGWSAQPTC(+57.02)IK</li></ul>"
				+ "</html>");
		c.gridy++;
		panel.add(jlabel, c);
		// /////////////// ROW4
		final JPanel colorScalePanel = optionsFactory.getColorScalePanel(false);
		c.gridy++;
		panel.add(colorScalePanel, c);

		final JButton jbuttonSave = optionsFactory.getSaveButton();
		c.gridy++;
		panel.add(jbuttonSave, c);

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
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
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
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(10, 0, 0, 0);
		final JCheckBox showPSMCheckBox = optionsFactory.getShowPSMCheckBox();
		c.gridx = 0;
		c.gridy = 0;
		panel.add(showPSMCheckBox, c);
		final JCheckBox showPeptidesCheckBox = optionsFactory.getShowPeptidesCheckBox();
		c.gridy++;
		panel.add(showPeptidesCheckBox, c);
		final JCheckBox showProteinsCheckBox = optionsFactory.getShowProteinsCheckBox();
		c.gridy++;
		panel.add(showProteinsCheckBox, c);
		final JCheckBox showScoreVsFDRCheckBox = optionsFactory.getShowScoreVsFDRCheckBox();
		c.gridy++;
		panel.add(showScoreVsFDRCheckBox, c);

		if (!ONE_SERIES_PER_EXPERIMENT_LIST.equals(options)) {
			final JLabel label = new JLabel("Select from the following checkBoxes:");
			c.gridy++;
			panel.add(label, c);
			JPanel checkboxesPanel = null;
			if (ONE_CHART_PER_EXPERIMENT.equals(options)) {
				checkboxesPanel = optionsFactory.getReplicatesCheckboxes(true, false, 3, false, null);
			} else if (ONE_SERIES_PER_EXPERIMENT.equals(options)) {
				checkboxesPanel = optionsFactory.getExperimentsCheckboxes(false, 3, false, null);
			} else if (ONE_SERIES_PER_REPLICATE.equals(options)) {
				checkboxesPanel = optionsFactory.getReplicatesCheckboxes(false, false, 3, false, null);
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
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);

		c.gridx = 0;
		c.gridy = 0;

		final JCheckBox showAsPieChartCheckBox = optionsFactory.getShowAsPieChartCheckBox();

		// /////////////// ROW1
		if (!showAsPieChartCheckBox.isSelected()) {
			final JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();
			c.gridy++;

			panel.add(jPanelAdditional1, c);
		}

		c.gridy++;
		final JPanel peptideOrPSMSelector = optionsFactory.getPeptideOrPSMSelector();
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
		final JLabel label1 = new JLabel("<html><br><br>Filter by Human taxonomy or chromosome:</html>");
		c.gridy++;
		panel.add(label1, c);
		final JButton button = new JButton("Filter Human proteins");
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

		final JButton button2 = new JButton("Filter by chromosome");
		button2.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				final String chr = (String) chromosomesCombo.getSelectedItem();
				ChartManagerFrame.this.exportHumanProteins(chr);
			}
		});
		button2.setToolTipText(
				"<html>Filter proteins encoded by genes from the chromosome sected in the combo-box<br>discarding any other protein encoded by any gene in any other chromosome.</html>");
		c.gridy++;
		panel.add(button2, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);
		jPanelAddOptions.repaint();
	}

	private void addAllHumanChromosomeMappingControls() {
		jPanelAddOptions.removeAll();
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);

		c.gridx = 0;
		c.gridy = 0;

		final JLabel label2 = new JLabel("<html><b>Note</b>: Number of genes can be different from<br>"
				+ "number of proteins, since isoforms are considered<br>" + "as different proteins<br><br></html>");
		panel.add(label2, c);

		final JCheckBox showAsPieChartCheckBox = optionsFactory.getShowAsPieChartCheckBox();

		final JCheckBox takeGeneFromFirstProteinCheckBox = optionsFactory.getTakeGeneFromFirstProteinCheckbox();

		// /////////////// ROW1
		if (!showAsPieChartCheckBox.isSelected()) {
			final JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();
			c.gridy++;

			panel.add(jPanelAdditional1, c);
		}

		c.gridy++;
		final JPanel proteinOrGeneSelector = optionsFactory.getProteinOrGeneSelector();
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
		final JLabel label1 = new JLabel("<html><br><br>Filter by Human taxonomy or chromosome:</html>");
		c.gridy++;
		panel.add(label1, c);
		final JButton button = new JButton("Filter Human proteins");
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

		final JButton button2 = new JButton("Filter by chromosome");
		button2.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				final String chr = (String) chromosomesCombo.getSelectedItem();
				ChartManagerFrame.this.exportHumanProteins(chr);
			}
		});
		button2.setToolTipText(
				"<html>Filter proteins encoded by genes from the chromosome sected in the combo-box<br>discarding any other protein encoded by any gene in any other chromosome.</html>");

		c.gridy++;
		panel.add(button2, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);
		jPanelAddOptions.repaint();
	}

	protected void exportHumanProteins(String chr) {
		final Set<String> filterProteinACC = GeneDistributionReader.getInstance().getProteinGeneMapping(chr).keySet();

		filterDialog.enableProteinACCFilter(filterProteinACC);
		filterDialog.applyFilters(experimentList);

	}

	private void addPSM_PEP_PROT_Controls(String options) {
		jPanelAddOptions.removeAll();
		jPanelAddOptions.setLayout(new BorderLayout());
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(10, 0, 0, 0);
		c.gridx = 0;
		c.gridy = 0;

		// do not show PSMs if FDR filter is activated because number of PSMs
		// will be equal to the number of peptides

		final JCheckBox showPSMCheckBox = optionsFactory.getShowPSMCheckBox();
		panel.add(showPSMCheckBox, c);

		c.gridy++;
		final JCheckBox showPeptidesCheckBox = optionsFactory.getShowPeptidesCheckBox();
		panel.add(showPeptidesCheckBox, c);

		c.gridy++;
		final JCheckBox showPeptidesCheckBoxPlusCharge = optionsFactory.getShowPeptidesPlusChargeCheckBox();
		panel.add(showPeptidesCheckBoxPlusCharge, c);

		c.gridy++;
		final JCheckBox showProteinsCheckBox = optionsFactory.getShowProteinsCheckBox();
		panel.add(showProteinsCheckBox, c);

		final JCheckBox showTotalSerieCheckBox = optionsFactory.getShowTotalSerieCheckBox(false);
		if (!ONE_SERIES_PER_EXPERIMENT_LIST.equals(options)
				&& !(ONE_SERIES_PER_EXPERIMENT.equals(options) && experimentList.getExperiments().size() < 2)) {
			c.gridy++;
			final JCheckBox showTotalSerie = showTotalSerieCheckBox;
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
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(10, 0, 0, 0);
		final JPanel showRegressionLinePanel = optionsFactory.getShowRegressionLinePanel();
		c.gridx = 0;
		c.gridy = 0;
		panel.add(showRegressionLinePanel, c);

		if (!ONE_SERIES_PER_EXPERIMENT_LIST.equals(options)) {
			final JLabel label = new JLabel("Select from the following checkBoxes:");
			c.gridy++;
			panel.add(label, c);
			JPanel checkboxesPanel = null;
			if (ONE_CHART_PER_EXPERIMENT.equals(options)) {
				checkboxesPanel = optionsFactory.getReplicatesCheckboxes(true, false, 3, false, null);
			} else if (ONE_SERIES_PER_EXPERIMENT.equals(options)) {
				checkboxesPanel = optionsFactory.getExperimentsCheckboxes(false, 3, false, null);
			} else if (ONE_SERIES_PER_REPLICATE.equals(options)) {
				checkboxesPanel = optionsFactory.getReplicatesCheckboxes(false, false, 3, false, null);
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
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		final JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();
		c.gridx = 0;
		c.gridy = 0;

		panel.add(jPanelAdditional1, c);
		// /////////////// ROW2
		final JCheckBox checkbox2 = optionsFactory.getShowAsStackedChartCheckBox();
		c.gridx = 0;
		c.gridy = 1;

		panel.add(checkbox2, c);
		// /////////////// ROW3
		final JCheckBox checkbox3 = optionsFactory.getShowAsPercentageCheckBox();
		c.gridx = 0;
		c.gridy = 2;

		panel.add(checkbox3, c);
		// /////////////// ROW6
		final JCheckBox jCheckBox6 = optionsFactory.getShowTotalSerieCheckBox(true);
		if (jCheckBox6 != null && !isOccurrenceFilterEnabled()) {
			c.gridx = 0;
			c.gridy = 5;
			panel.add(jCheckBox6, c);
		}
		// /////////////// ROW7
		final JCheckBox jCheckBox7 = optionsFactory.getShowDifferentIdentificationsCheckBox("PSMs or peptides");
		jCheckBox7.setToolTipText(
				"<html>If this option is activated, the chart will show the number of proteins having x <b>number of PSMs</b>.<br>"
						+ "If this option is not activated, the chart will show the number of proteins having x <b>number of peptides</b>.</html>");
		c.gridx = 0;
		c.gridy = 6;
		panel.add(jCheckBox7, c);
		// /////////////// ROW7
		final JPanel jpanelMax = optionsFactory.getMaximumNumOccurrence("maximum", 30, 5);
		c.gridx = 0;
		c.gridy = 7;
		panel.add(jpanelMax, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	private void addSingleHitProteinControls() {
		jPanelAddOptions.removeAll();
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		final JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();
		c.gridx = 0;
		c.gridy = 0;

		panel.add(jPanelAdditional1, c);
		// /////////////// ROW2
		final JCheckBox checkbox2 = optionsFactory.getShowAsStackedChartCheckBox();
		c.gridx = 0;
		c.gridy = 1;

		panel.add(checkbox2, c);

		// /////////////// ROW4
		final JCheckBox jCheckBox = optionsFactory.getShowAsPieChartCheckBox();
		c.gridx = 0;
		c.gridy = 3;
		panel.add(jCheckBox, c);

		// /////////////// ROW6
		final JCheckBox jCheckBox6 = optionsFactory.getShowTotalSerieCheckBox(true);
		if (jCheckBox6 != null && !isOccurrenceFilterEnabled()) {
			c.gridx = 0;
			c.gridy = 5;
			panel.add(jCheckBox6, c);
		}
		// /////////////// ROW7
		final JCheckBox jCheckBox7 = optionsFactory
				.getShowDifferentIdentificationsCheckBox("single hit peptide or PSM");
		jCheckBox7.setToolTipText(
				"<html>If this option is activated, the chart will show the number of proteins having just one PSM.<br>"
						+ "If the option is desactivated, the chart will show the number of proteins having just one peptide.</htm>");
		c.gridx = 0;
		c.gridy = 6;
		panel.add(jCheckBox7, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	private void addProteinCoverageHistogramBarControls() {
		jPanelAddOptions.removeAll();

		jPanelAddOptions.setLayout(new GridBagLayout());

		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		c.gridx = 0;
		c.gridy = 0;
		jPanelAddOptions.add(optionsFactory.getPlotOrientationPanel(), c);
		c.gridx = 0;
		c.gridy++;
		jPanelAddOptions.add(optionsFactory.getShowTotalSerieCheckBox(false), c);
	}

	private void addMissedCleavageDistributionControls(String options) {
		jPanelAddOptions.removeAll();
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		final JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();

		c.gridx = 0;
		c.gridy = 0;
		panel.add(jPanelAdditional1, c);

		// /////////////// ROW3
		final JPanel jPanelAdditional3 = optionsFactory.getMaximumNumOccurrence("Maximum occurrence:", 10, 4);
		c.gridx = 0;
		c.gridy++;
		panel.add(jPanelAdditional3, c);

		// /////////////// ROW4
		final JCheckBox checkbox4 = optionsFactory.getShowAsPercentageCheckBox();
		c.gridx = 0;
		c.gridy++;
		panel.add(checkbox4, c);
		// /////////////// ROW5
		final JCheckBox checkbox5 = optionsFactory.getShowAsStackedChartCheckBox();
		c.gridx = 0;
		c.gridy++;
		panel.add(checkbox5, c);

		if (!ONE_SERIES_PER_EXPERIMENT_LIST.equals(options)) {
			final JCheckBox checkbox6 = optionsFactory.getShowTotalSerieCheckBox(true);
			if (checkbox6 != null) {
				c.gridy++;
				panel.add(checkbox6, c);
			}
		}

		final JPanel jPanelCleavage = optionsFactory.getCleavagePanel();
		c.gridx = 0;
		c.gridy++;
		panel.add(jPanelCleavage, c);
		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	private void addProteingGroupTypesDistributionControls(String options) {
		jPanelAddOptions.removeAll();
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		final JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();

		c.gridx = 0;
		c.gridy = 0;
		panel.add(jPanelAdditional1, c);

		// /////////////// ROW4
		final JCheckBox checkbox4 = optionsFactory.getShowAsPercentageCheckBox();
		c.gridx = 0;
		c.gridy++;
		panel.add(checkbox4, c);
		// /////////////// ROW5
		final JCheckBox checkbox5 = optionsFactory.getShowAsStackedChartCheckBox();
		c.gridx = 0;
		c.gridy++;
		panel.add(checkbox5, c);

		if (!ONE_SERIES_PER_EXPERIMENT_LIST.equals(options)) {
			final JCheckBox checkbox6 = optionsFactory.getShowTotalSerieCheckBox(true);
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
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		final JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();

		c.gridx = 0;
		c.gridy = 0;
		panel.add(jPanelAdditional1, c);
		// /////////////// ROW4
		final JCheckBox checkbox4 = optionsFactory.getShowAsPercentageCheckBox();
		c.gridx = 0;
		c.gridy++;
		panel.add(checkbox4, c);
		// /////////////// ROW5
		final JCheckBox checkbox5 = optionsFactory.getShowAsStackedChartCheckBox();
		c.gridx = 0;
		c.gridy++;
		panel.add(checkbox5, c);

		if (!ONE_SERIES_PER_EXPERIMENT_LIST.equals(options)) {
			final JCheckBox checkbox6 = optionsFactory.getShowTotalSerieCheckBox(true);
			if (checkbox6 != null) {
				c.gridy++;
				panel.add(checkbox6, c);
			}
		}
		// /////////////// ROW7
		final JPanel jpanelMin = optionsFactory.getMinimumNumOccurrence("minimum", 40, 7);
		c.gridy++;
		panel.add(jpanelMin, c);

		final JPanel jpanelMax = optionsFactory.getMaximumNumOccurrence("maximum", 40, 20);
		c.gridy++;
		panel.add(jpanelMax, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	private void addSensitivitySpecificityControls() {
		jPanelAddOptions.removeAll();
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		final JPanel jPanelAdditional1 = optionsFactory.getProteinsInSamplePanel();
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
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		final JCheckBox checkbox = optionsFactory.getShowAsPercentageCheckBox();
		c.gridx = 0;
		c.gridy = 0;
		panel.add(checkbox, c);

		// /////////////// ROW2
		final JPanel jPanelAdditional3 = optionsFactory.getMaximumNumOccurrence("Maximum occurrence:", 40, 4);
		c.gridx = 0;
		c.gridy = 1;
		panel.add(jPanelAdditional3, c);

		// /////////////// ROW3
		final JPanel jPanelAdditional4 = optionsFactory.getOverReplicatesPanel();

		c.gridx = 0;
		c.gridy = 2;
		panel.add(jPanelAdditional4, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	private void addPeptideMonitoringControls() {
		jPanelAddOptions.removeAll();
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		final JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();
		c.gridx = 0;
		c.gridy = 0;
		panel.add(jPanelAdditional1, c);
		// ////////////// ROW2
		final JPanel jPanelAdditional2 = optionsFactory.getPeptideSequencesPanel(distinguishModifiedPeptides());

		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.BOTH;
		panel.add(jPanelAdditional2, c);

		// /////////////// ROW3
		final JPanel jPanelAdditional3 = optionsFactory.getUserPeptideListPanel(true);

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
				final List<PeptideOccurrence> occurrenceList = new ArrayList<PeptideOccurrence>();
				for (final PeptideOccurrence identificationOccurrence : peptideOccurrences) {
					occurrenceList.add(identificationOccurrence);
				}
				log.info("There is " + occurrenceList.size() + " peptide sequences");
				final String[] ret = new String[occurrenceList.size()];
				int i = 0;
				SorterUtil.sortPeptideOcurrencesBySequence(occurrenceList);
				for (final PeptideOccurrence identificationOccurrence : occurrenceList) {

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
				final List<PeptideOccurrence> occurrenceList = new ArrayList<PeptideOccurrence>();
				for (final PeptideOccurrence identificationOccurrence : peptideOccurrences) {
					occurrenceList.add(identificationOccurrence);
				}
				log.info("There is " + occurrenceList.size() + " peptide+charge sequences");
				final String[] ret = new String[occurrenceList.size()];
				int i = 0;
				SorterUtil.sortPeptideOcurrencesBySequence(occurrenceList);
				for (final PeptideOccurrence identificationOccurrence : occurrenceList) {

					ret[i] = identificationOccurrence.getKey();

					i++;
				}

				// return DatasetFactory.toSortedArray(ret);
				return ret;
			}
		}
		return null;
	}

	private void addPeptideModificationControls(boolean multipleSelectionOfPTMList) {
		jPanelAddOptions.removeAll();
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		final JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();
		c.gridx = 0;
		c.gridy = 0;
		panel.add(jPanelAdditional1, c);

		// ////////////// ROW2
		final JPanel jPanelAdditional2 = optionsFactory.getModificationListPanel(multipleSelectionOfPTMList);

		c.gridx = 0;
		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		panel.add(jPanelAdditional2, c);
		if (multipleSelectionOfPTMList) {
			final JLabel jlabel = new JLabel("(Multiple selections are allowed)");
			jlabel.setToolTipText(
					"<html>Multiple selections are allowed:<br>Press CTRL key and select more than one modification.</html>");
			c.gridy++;
			panel.add(jlabel, c);
		}
		// /////////////// ROW4
		final JCheckBox checkBox4 = optionsFactory.getShowAsStackedChartCheckBox();
		c.gridx = 0;
		c.gridy++;
		panel.add(checkBox4, c);
		// /////////////// ROW5
		final JCheckBox checkbox5 = optionsFactory.getShowAsPercentageCheckBox();
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
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		if (currentChartType.equals(ChartType.PROTEIN_SCORE_COMPARISON)) {
			// //////////////// ROW1
			final DefaultComboBoxModel proteinScoreNames = getProteinScoreNames();
			if (proteinScoreNames != null) {
				final JPanel jPanelAdditional3 = optionsFactory.getProteinScorePanel(proteinScoreNames);

				panel.add(jPanelAdditional3, c);
			}
		}

		if (currentChartType.equals(ChartType.PEPTIDE_SCORE_COMPARISON)) {
			// //////////////// ROW2
			final DefaultComboBoxModel peptideScoreNames = getPeptideScoreNames();
			if (peptideScoreNames != null) {
				final JPanel jPanelAdditional4 = optionsFactory.getPeptideScorePanel(peptideScoreNames);
				c.gridy++;
				panel.add(jPanelAdditional4, c);
			}
		}
		// //////////////// ROW
		final JCheckBox jCheckApplyLog = optionsFactory.getApplyLogCheckBox();
		c.gridy++;
		panel.add(jCheckApplyLog, c);

		// //////////////// ROW3
		final JPanel jPanelAdditional5 = optionsFactory.getShowRegressionLinePanel();
		c.gridy++;
		panel.add(jPanelAdditional5, c);
		// //////////////// ROW4
		final JPanel jPanelAdditional6 = optionsFactory.getShowDiagonalLinePanel();
		c.gridy++;
		panel.add(jPanelAdditional6, c);
		// /
		final JCheckBox separateDecoyHits = optionsFactory.getSeparatedDecoyHitsCheckBox();
		separateDecoyHits.setEnabled(experimentList.getFDRFilter() != null);
		c.gridy++;
		panel.add(separateDecoyHits, c);
		if (!ONE_SERIES_PER_EXPERIMENT_LIST.equals(options)) {
			final JLabel label = new JLabel("Select from the following checkBoxes:");
			c.gridy++;
			panel.add(label, c);
			JPanel checkboxesPanel = null;
			if (ONE_CHART_PER_EXPERIMENT.equals(options)) {
				checkboxesPanel = optionsFactory.getReplicatesCheckboxes(true, false, 3, false, null);
			} else if (ONE_SERIES_PER_EXPERIMENT.equals(options)) {
				checkboxesPanel = optionsFactory.getExperimentsCheckboxes(false, 3, false, null);
			} else if (ONE_SERIES_PER_REPLICATE.equals(options)) {
				checkboxesPanel = optionsFactory.getReplicatesCheckboxes(false, false, 3, false, null);
			}

			c.gridy++;
			panel.add(checkboxesPanel, c);
		}
		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	private void addHeatMapControls(boolean occurrenceThreshold) {
		jPanelAddOptions.removeAll();
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		final JPanel jPanelAdditional1 = optionsFactory.getColorScalePanel(true);
		c.gridx = 0;
		c.gridy = 0;
		panel.add(jPanelAdditional1, c);

		// //////////////// ROW2
		final JPanel jPanelAdditional2 = optionsFactory.getHeatMapThresholdPanel(occurrenceThreshold);
		c.gridy++;
		panel.add(jPanelAdditional2, c);

		final JButton jbuttonSave = optionsFactory.getSaveButton();
		c.gridy++;
		panel.add(jbuttonSave, c);
		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);

	}

	public void addMinMaxHeatMapValues(double[][] dataset) {

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		double minRow = Double.MAX_VALUE;
		double maxRow = Double.MIN_VALUE;
		for (int i = 0; i < dataset.length; i++) {
			double rowSum = 0;
			for (int j = 0; j < dataset[i].length; j++) {
				final double d = dataset[i][j];
				rowSum += d;
				if (d < min)
					min = d;
				if (d > max)
					max = d;
			}
			if (rowSum > maxRow) {
				maxRow = rowSum;
			}
			if (rowSum < minRow) {
				minRow = rowSum;
			}
		}

		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(10, 0, 0, 0);

		final JLabel label = new JLabel("<html>Heatmap counts:<br>min cell value= " + Double.valueOf(min).intValue()
				+ "<br>max cell value= " + Double.valueOf(max).intValue() + "<br>min sum of row cell values= "
				+ Double.valueOf(minRow).intValue() + "<br>max sum of row cell value= "
				+ Double.valueOf(maxRow).intValue() + "<br>" + "number of rows in the heatmap= " + dataset.length
				+ "</html>");
		label.setToolTipText(
				"<html>These are the minimum value that corresponds with the 'low color',<br>and the maximum value that corresponds with the 'high color'.<br>"
						+ "The number of items is the number of proteins or peptides that are showed in the chart.</html>");
		panel.add(label, c);
		jPanelAddOptions.add(panel);

	}

	private void addOverlapingControls(String options, Integer numberOfSelectedCheckBoxes, boolean selectAllCheckBoxes,
			boolean addColorChooser) {
		if (options == null)
			options = ChartManagerFrame.ONE_CHART_PER_EXPERIMENT;
		if (ONE_CHART_PER_EXPERIMENT.equals(options)) {
			addOverlappingControlsChartPerExperiment(numberOfSelectedCheckBoxes, selectAllCheckBoxes, addColorChooser);
		} else if (ONE_SERIES_PER_EXPERIMENT.equals(options)) {
			addOverlappingControlsSeriePerExperiment(numberOfSelectedCheckBoxes, selectAllCheckBoxes, addColorChooser);
		} else if (ONE_SERIES_PER_REPLICATE.equals(options)) {
			addOverlappingControlsSeriePerReplicate(numberOfSelectedCheckBoxes, selectAllCheckBoxes, addColorChooser);
		}
	}

	private void addOverlappingControlsChartPerExperiment(Integer numberOfSelectedCheckBoxes,
			boolean selectAllCheckBoxes, boolean addColorChooser) {
		// jPanelAddOptions.setLayout(new BoxLayout(jPanelAddOptions,
		// BoxLayout.PAGE_AXIS));
		jPanelAddOptions.setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(5, 0, 5, 0);
		c.gridx = 0;
		c.gridy = 0;
		// Just in case of proteins

		log.info("Creating list of replicates...");
		if (numberOfSelectedCheckBoxes == null)
			numberOfSelectedCheckBoxes = Integer.MAX_VALUE;

		if (numberOfSelectedCheckBoxes.equals(Integer.MAX_VALUE)) {
			final JLabel label = new JLabel(
					"<html>Number of identifications that are detected <b>just</b> <br>in each set.</html>");
			jPanelAddOptions.add(label, c);
			c.gridy++;
		}
		if (ChartType.PROTEIN_OVERLAPING.equals(currentChartType)
				|| ChartType.EXCLUSIVE_PROTEIN_NUMBER.equals(currentChartType)
				|| ChartType.PEPTIDE_COUNTING_HISTOGRAM.equals(currentChartType)) {
			jPanelAddOptions.add(new JLabel("Protein groups are equal if (select one option):"), c);
			c.gridy++;
			// take into account just one protein per group
			jPanelAddOptions.add(optionsFactory.getJcheckBoxOneProteinPerGroup(), c);
			c.gridy++;
		}
		if (ChartType.EXCLUSIVE_PROTEIN_NUMBER.equals(currentChartType)
				|| ChartType.EXCLUSIVE_PEPTIDE_NUMBER.equals(currentChartType)) {
			jPanelAddOptions.add(optionsFactory.getAccumulativeTrendCheckBox(), c);
			c.gridy++;
		}
		if (ChartType.PEPTIDE_COUNTING_HISTOGRAM.equals(currentChartType)) {
			jPanelAddOptions.add(optionsFactory.getBinsPanel(), c);
			c.gridy++;
			jPanelAddOptions.add(optionsFactory.getHistogramTypePanel(), c);
			c.gridy++;
		}
		jPanelAddOptions.add(new JLabel("Select from the following checkBoxes:"), c);
		c.gridy++;
		jPanelAddOptions.add(optionsFactory.getReplicatesCheckboxes(true, selectAllCheckBoxes, 0, addColorChooser,
				getMethodToUpdateColorInChart()), c);
		c.gridy++;

		if (!numberOfSelectedCheckBoxes.equals(Integer.MAX_VALUE)
				&& !ChartType.PEPTIDE_COUNTING_HISTOGRAM.equals(currentChartType)) {
			// Add save image button
			final JButton jbuttonSave = new JButton("Save image(s)");
			jbuttonSave.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					saveOverlappingImages();
				}
			});
			optionsFactory.getControlList().add(jbuttonSave);
			jPanelAddOptions.add(jbuttonSave, c);
			c.gridy++;
			final JLabel jLabelIntersectionsText = optionsFactory.getJLabelIntersectionsText();
			jLabelIntersectionsText.setText(null);
			jPanelAddOptions.add(jLabelIntersectionsText, c);
			optionsFactory.getControlList().add(jLabelIntersectionsText);
			c.gridy++;

			// add the buttons per chart (in chart per experiment there is one
			// graph per experiment
			final List<String> experimentNames = new ArrayList<String>();
			final TObjectIntHashMap<String> numReplicatesPerExperiment = new TObjectIntHashMap<String>();

			for (final Experiment experiment : experimentList.getExperiments()) {
				experimentNames.add(experiment.getFullName());
				numReplicatesPerExperiment.put(experiment.getFullName(), experiment.getNumReplicates());
			}

			String overlapString = null;
			// loop over the experiment names
			for (final String experimentName : experimentNames) {
				final int numDatasets = numReplicatesPerExperiment.get(experimentName);
				if (numDatasets > 1) {
					String labelString = "Export buttons:";
					if (experimentName != null) {
						labelString = "Export buttons for '" + experimentName + "':";
					}
					final JLabel labelExperiment = new JLabel(labelString);
					jPanelAddOptions.add(labelExperiment, c);
					optionsFactory.getControlList().add(labelExperiment);
					c.gridy++;
					overlapString = "A,B";
					// export just in 1 button
					final JButton jbuttonExportJustIn1 = new JButton("Export just in A");
					jbuttonExportJustIn1.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(java.awt.event.ActionEvent evt) {
							exportJustIn1(experimentName);
						}
					});
					jPanelAddOptions.add(jbuttonExportJustIn1, c);
					optionsFactory.getControlList().add(jbuttonExportJustIn1);
					c.gridy++;
					// export just in 2 button
					final JButton jbuttonExportJustIn2 = new JButton("Export just in B");
					jbuttonExportJustIn2.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(java.awt.event.ActionEvent evt) {
							exportJustIn2(experimentName);
						}
					});
					jPanelAddOptions.add(jbuttonExportJustIn2, c);
					optionsFactory.getControlList().add(jbuttonExportJustIn2);

					c.gridy++;
				}
				if (numDatasets > 2) {
					// if (numberOfSelectedCheckBoxes > 2) {
					overlapString += ",C";
					// export just in 3 button
					final JButton jbuttonExportJustIn3 = new JButton("Export just in C");
					jbuttonExportJustIn3.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(java.awt.event.ActionEvent evt) {
							exportJustIn3(experimentName);
						}
					});
					jPanelAddOptions.add(jbuttonExportJustIn3, c);
					optionsFactory.getControlList().add(jbuttonExportJustIn3);

					c.gridy++;
				}
				// export overlapped button
				final JButton jbuttonExportOverlap = new JButton("Export Overlap (" + overlapString + ")");
				jbuttonExportOverlap.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						exportOverlapped(experimentName);
					}
				});
				jPanelAddOptions.add(jbuttonExportOverlap, c);
				optionsFactory.getControlList().add(jbuttonExportOverlap);

				c.gridy++;

			}
		}

		jPanelAddOptions.repaint();
	}

	private DoSomethingToChangeColorInChart getMethodToUpdateColorInChart() {
		final DoSomethingToChangeColorInChart ret = new DoSomethingToChangeColorInChart() {

			@Override
			public Void doSomething(String experimentName, String idSetName, Color color) {
				final VennChart vennChart = chartCreator.getVennChart(experimentName);
				if (vennChart != null) {
					// set color
					vennChart.setColorToSeries(idSetName, color);
					// change venn diagram with the new colors
					try {
						vennChart.updateChart();

					} catch (final MalformedURLException e) {
						e.printStackTrace();
						throw new IllegalMiapeArgumentException(e.getMessage());
					}
				}
				return null;
			}
		};
		return ret;
	}

	/**
	 * 
	 */
	protected void exportJustIn1(String experimentName) {
		if (chartCreator != null) {
			final VennData vennData = chartCreator.getVennData(experimentName);
			if (vennData != null) {
				log.info(vennData.getUniqueTo1().size() + " just in 1");
				final IdentificationSet idSet = getSelectedIdentificationSetsForVennChart(experimentName)[0];
				String datasetName = null;
				if (idSet != null) {
					datasetName = idSet.getName();
				}
				exportSubset(vennData.getUniqueTo1(), idSet, datasetName);
			}
		}
	}

	private IdentificationSet[] getSelectedIdentificationSetsForVennChart(String experimentName) {
		final IdentificationSet[] ret = new IdentificationSet[3];
		IdentificationSet idSet1 = null;
		IdentificationSet idSet2 = null;
		IdentificationSet idSet3 = null;
		final String option = (String) jComboBoxChartOptions.getSelectedItem();
		final Map<String, JCheckBox> checkBoxControls = optionsFactory.getIdSetsJCheckBoxes();
		if (option.equals(ChartManagerFrame.ONE_SERIES_PER_REPLICATE)) {
			final List<Experiment> experiments = experimentList.getExperiments();
			for (final Experiment experiment : experiments) {
				for (final Object identificationSet : experiment.getNextLevelIdentificationSetList()) {
					final Replicate replicate = (Replicate) identificationSet;
					final String repName = replicate.getFullName();
					if (checkBoxControls.containsKey(repName) && checkBoxControls.get(repName).isSelected()) {
						if (idSet1 == null) {
							idSet1 = replicate;
						} else if (idSet2 == null) {
							idSet2 = replicate;
						} else if (idSet3 == null) {
							idSet3 = replicate;
						}
					}
				}
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final List<Experiment> experiments = experimentList.getExperiments();

			for (final Experiment experiment : experiments) {
				final String expName = experiment.getFullName();
				if (checkBoxControls.containsKey(expName) && checkBoxControls.get(expName).isSelected()) {
					if (idSet1 == null) {
						idSet1 = experiment;
					} else if (idSet2 == null) {
						idSet2 = experiment;
					} else if (idSet3 == null) {
						idSet3 = experiment;
					}
				}
			}
		} else if (ChartManagerFrame.ONE_SERIES_PER_EXPERIMENT_LIST.equals(option)) {
			idSet1 = experimentList;
		} else if (ChartManagerFrame.ONE_CHART_PER_EXPERIMENT.equals(option)) {
			final List<JPanel> chartList = new ArrayList<JPanel>();
			final String intersectionText = "";
			for (final Experiment experiment : experimentList.getExperiments()) {
				if (experiment.getFullName().equals(experimentName)) {
					final int numReplicates = 1;
					idSet1 = null;
					idSet2 = null;
					idSet3 = null;
					for (final Replicate replicate : experiment.getNextLevelIdentificationSetList()) {
						final String repName = replicate.getFullName();
						if (checkBoxControls.containsKey(repName) && checkBoxControls.get(repName).isSelected()) {
							if (idSet1 == null) {
								idSet1 = replicate;
							} else if (idSet2 == null) {
								idSet2 = replicate;
							} else if (idSet3 == null) {
								idSet3 = replicate;
							}
						}
					}
				}
			}
		}
		ret[0] = idSet1;
		ret[1] = idSet2;
		ret[2] = idSet3;
		return ret;
	}

	protected void exportJustIn2(String experimentName) {
		if (chartCreator != null) {
			final VennData vennData = chartCreator.getVennData(experimentName);
			if (vennData != null) {
				log.info(vennData.getUniqueTo2().size() + " just in 2");
				final IdentificationSet idSet = getSelectedIdentificationSetsForVennChart(experimentName)[1];
				String datasetName = null;
				if (idSet != null) {
					datasetName = idSet.getName();
				}
				exportSubset(vennData.getUniqueTo2(), idSet, datasetName);
			}
		}
	}

	protected void exportJustIn3(String experimentName) {
		if (chartCreator != null) {
			final VennData vennData = chartCreator.getVennData(experimentName);
			if (vennData != null) {
				log.info(vennData.getUniqueTo3().size() + " just in 3");
				final IdentificationSet idSet = getSelectedIdentificationSetsForVennChart(experimentName)[2];
				String datasetName = null;
				if (idSet != null) {
					datasetName = idSet.getName();
				}
				exportSubset(vennData.getUniqueTo3(), idSet, datasetName);
			}
		}
	}

	protected void exportOverlapped(String experimentName) {
		if (chartCreator != null) {
			final VennData vennData = chartCreator.getVennData(experimentName);
			if (vennData != null) {
				final Collection<Object> intersection123 = vennData.getIntersection123();
				if (!intersection123.isEmpty()) {
					log.info(intersection123.size() + " overlapped");
					String datasetName1 = null;
					String datasetName2 = null;
					String datasetName3 = null;
					final Set<IdentificationSet> idSets = new THashSet<IdentificationSet>();
					final IdentificationSet idSet1 = getSelectedIdentificationSetsForVennChart(experimentName)[0];
					if (idSet1 != null) {
						datasetName1 = idSet1.getName();
						idSets.add(idSet1);
					}
					final IdentificationSet idSet2 = getSelectedIdentificationSetsForVennChart(experimentName)[1];
					if (idSet2 != null) {
						datasetName2 = idSet2.getName();
						idSets.add(idSet2);
					}
					final IdentificationSet idSet3 = getSelectedIdentificationSetsForVennChart(experimentName)[2];
					if (idSet3 != null) {
						datasetName3 = idSet3.getName();
						idSets.add(idSet3);
					}
					exportSubset(intersection123, idSets, datasetName1, datasetName2, datasetName3);
				} else {
					final Set<IdentificationSet> idSets = new THashSet<IdentificationSet>();

					final Collection<Object> intersection12 = vennData.getIntersection12();
					if (!intersection12.isEmpty()) {
						String datasetName1 = null;
						String datasetName2 = null;
						final IdentificationSet idSet1 = getSelectedIdentificationSetsForVennChart(experimentName)[0];
						if (idSet1 != null) {
							datasetName1 = idSet1.getName();
							idSets.add(idSet1);
						}
						final IdentificationSet idSet2 = getSelectedIdentificationSetsForVennChart(experimentName)[1];
						if (idSet2 != null) {
							datasetName2 = idSet2.getName();
							idSets.add(idSet2);
						}

						log.info(intersection12.size() + " overlapped");
						exportSubset(intersection12, idSets, datasetName1, datasetName2);
					}
				}
			}
		}

	}

	private Set<IdentificationSet> toSet(IdentificationSet... idSets) {
		final Set<IdentificationSet> ret = new THashSet<IdentificationSet>();
		for (final IdentificationSet idSet : idSets) {
			if (idSet != null) {
				ret.add(idSet);
			}
		}
		return ret;
	}

	private void exportSubset(Collection<Object> collection, IdentificationSet idSet, String... datasetNames) {
		final Set<IdentificationSet> set = new THashSet<IdentificationSet>();
		set.add(idSet);
		exportSubset(collection, set, datasetNames);
	}

	/**
	 * @param uniqueTo2
	 */
	private void exportSubset(Collection<Object> collection, Collection<IdentificationSet> idSets,
			String... datasetNames) {
		if (collection.isEmpty()) {
			appendStatus("Collection is empty!");
			return;
		}

		final ExporterDialog exporterDialog = new ExporterDialog(this, this, idSets, getDataLevel(), true);

		Filters filter = null;
		if (currentChartType.equals(ChartType.PROTEIN_OVERLAPING)) {

			final Set<Object> keys = new THashSet<Object>();
			final ProteinGroupComparisonType proteinGroupComparisonType = additionalOptionsPanelFactory
					.getProteinGroupComparisonType();
			for (final Object object : collection) {
				if (object instanceof ProteinGroupOccurrence) {
					final ProteinGroupOccurrence pgo = (ProteinGroupOccurrence) object;
					keys.add(pgo.getKey(proteinGroupComparisonType));
				} else {
					log.info(object.getClass().getName());
				}
			}
			if (proteinGroupComparisonType == ProteinGroupComparisonType.SHARE_ONE_PROTEIN) {
				final Set<ProteinComparatorKey> set = new THashSet<ProteinComparatorKey>();
				for (final Object obj : keys) {
					set.add((ProteinComparatorKey) obj);
				}
				filter = new ProteinACCFilterByProteinComparatorKey(set,
						GeneralOptionsDialog.getInstance(this).isDoNotGroupNonConclusiveProteins(),
						GeneralOptionsDialog.getInstance(this).isSeparateNonConclusiveProteins());
			} else {
				final Set<String> set = new THashSet<String>();
				for (final Object obj : keys) {
					set.add(obj.toString());
				}
				filter = new ProteinACCFilter(set,
						GeneralOptionsDialog.getInstance(this).isDoNotGroupNonConclusiveProteins(),
						GeneralOptionsDialog.getInstance(this).isSeparateNonConclusiveProteins());
			}
		} else if (currentChartType.equals(ChartType.PEPTIDE_OVERLAPING)) {
			final Set<String> sequences = new THashSet<String>();
			for (final Object object : collection) {
				if (object instanceof PeptideOccurrence) {
					final PeptideOccurrence po = (PeptideOccurrence) object;
					sequences.add(po.getKey());
				} else if (object instanceof String) {
					sequences.add((String) object);
				} else {
					log.info(object.getClass().getName() + "");
				}
			}
			filter = new PeptideSequenceFilter(sequences, true,
					GeneralOptionsDialog.getInstance(this).isDoNotGroupNonConclusiveProteins(),
					GeneralOptionsDialog.getInstance(this).isSeparateNonConclusiveProteins(),
					PACOMSoftware.getInstance());
		}
		exporterDialog.setFilter(filter);
		exporterDialog.setVisible(true);

	}

	private DataLevel getDataLevel() {
		if (ONE_SERIES_PER_EXPERIMENT.equals(getOptionChart())) {
			return DataLevel.LEVEL1;
		} else if (ONE_SERIES_PER_EXPERIMENT_LIST.equals(getOptionChart())) {
			return DataLevel.LEVEL0;
		} else if (ONE_CHART_PER_EXPERIMENT.equals(getOptionChart())
				|| ONE_SERIES_PER_REPLICATE.equals(getOptionChart())) {
			return DataLevel.LEVEL2;
		}
		throw new IllegalArgumentException("Not possible");
	}

	/**
	 * @param experimentList2
	 * @param datasetName
	 * @return
	 */
	private IdentificationSet getDatasetFromName(IdentificationSet idSet, String datasetName) {
		if (idSet.getName().equals(datasetName)) {
			return idSet;
		}
		try {
			for (final Object obj : idSet.getNextLevelIdentificationSetList()) {
				final IdentificationSet idSet2 = (IdentificationSet) obj;
				final IdentificationSet ret = getDatasetFromName(idSet2, datasetName);
				if (ret != null) {
					return ret;
				}
			}
		} catch (final UnsupportedOperationException e) {
			return null;
		}
		return null;
	}

	public ProteinGroupComparisonType getComparisonType() {
		return additionalOptionsPanelFactory.getProteinGroupComparisonType();
	}

	private void addOverlappingControlsSeriePerReplicate(Integer numberOfSelectedCheckBoxes,
			boolean selectAllCheckBoxes, boolean addColorChooser) {
		// jPanelAddOptions.setLayout(new BoxLayout(jPanelAddOptions,
		// BoxLayout.PAGE_AXIS));
		jPanelAddOptions.setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(5, 0, 5, 0);
		c.gridx = 0;
		c.gridy = 0;
		log.info("Creating list of replicates...");

		if (numberOfSelectedCheckBoxes == null)
			numberOfSelectedCheckBoxes = Integer.MAX_VALUE;
		if (numberOfSelectedCheckBoxes.equals(Integer.MAX_VALUE)) {
			final JLabel label = new JLabel(
					"<html>Number of identifications that are detected<br><b>just</b> in each set.</html>");
			jPanelAddOptions.add(label, c);
			c.gridy++;
		}
		// Just in case of proteins
		if (ChartType.PROTEIN_OVERLAPING.equals(currentChartType)
				|| ChartType.EXCLUSIVE_PROTEIN_NUMBER.equals(currentChartType)
				|| ChartType.PEPTIDE_COUNTING_HISTOGRAM.equals(currentChartType)) {
			jPanelAddOptions.add(new JLabel("Protein groups are equal if (select one option):"), c);
			c.gridy++;
			// take into account just one protein per group
			jPanelAddOptions.add(optionsFactory.getJcheckBoxOneProteinPerGroup(), c);
			c.gridy++;
		}
		if (ChartType.EXCLUSIVE_PROTEIN_NUMBER.equals(currentChartType)
				|| ChartType.EXCLUSIVE_PEPTIDE_NUMBER.equals(currentChartType)) {
			jPanelAddOptions.add(optionsFactory.getAccumulativeTrendCheckBox(), c);
			c.gridy++;
		}

		if (ChartType.PEPTIDE_COUNTING_HISTOGRAM.equals(currentChartType)) {
			jPanelAddOptions.add(optionsFactory.getBinsPanel(), c);
			c.gridy++;
			jPanelAddOptions.add(optionsFactory.getHistogramTypePanel(), c);
			c.gridy++;
		}

		// jPanelAddOptions.add(new JLabel("<html><br></html>"), c);
		// c.gridy++;
		jPanelAddOptions.add(new JLabel("Select from the following checkBoxes:"), c);
		c.gridy++;

		jPanelAddOptions.add(optionsFactory.getReplicatesCheckboxes(false, selectAllCheckBoxes, 0, addColorChooser,
				getMethodToUpdateColorInChart()), c);
		c.gridy++;
		if (!numberOfSelectedCheckBoxes.equals(Integer.MAX_VALUE)
				&& !ChartType.PEPTIDE_COUNTING_HISTOGRAM.equals(currentChartType)) {
			// Add save image button
			final JButton jbuttonSave = new JButton("Save image");
			jbuttonSave.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					saveOverlappingImage();
				}
			});
			jPanelAddOptions.add(jbuttonSave, c);
			c.gridy++;

			int numDatasets = 0;
			for (final Experiment experiment : experimentList.getExperiments()) {
				numDatasets += experiment.getNumReplicates();
			}
			if (numDatasets > 1) {
				String overlapString = "A,B";

				final JLabel jLabelIntersectionsText = optionsFactory.getJLabelIntersectionsText();
				jLabelIntersectionsText.setText(null);
				jPanelAddOptions.add(jLabelIntersectionsText, c);
				c.gridy++;

				final JLabel labelExperiment = new JLabel("Export buttons:");
				jPanelAddOptions.add(labelExperiment, c);
				c.gridy++;
				// export just in 1 button
				final JButton jbuttonExportJustIn1 = new JButton("Export just in A");
				jbuttonExportJustIn1.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						exportJustIn1(null);
					}
				});
				jPanelAddOptions.add(jbuttonExportJustIn1, c);
				c.gridy++;
				// export just in 2 button
				final JButton jbuttonExportJustIn2 = new JButton("Export just in B");
				jbuttonExportJustIn2.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						exportJustIn2(null);
					}
				});
				jPanelAddOptions.add(jbuttonExportJustIn2, c);
				c.gridy++;

				if (numDatasets > 2) {
					// if (numberOfSelectedCheckBoxes > 2) {
					overlapString += ",C";
					// export just in 3 button
					final JButton jbuttonExportJustIn3 = new JButton("Export just in C");
					jbuttonExportJustIn3.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(java.awt.event.ActionEvent evt) {
							exportJustIn3(null);
						}
					});
					jPanelAddOptions.add(jbuttonExportJustIn3, c);
					c.gridy++;
				}
				// export overlapped button
				final JButton jbuttonExportOverlap = new JButton("Export Overlap (" + overlapString + ")");
				jbuttonExportOverlap.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						exportOverlapped(null);
					}
				});
				jPanelAddOptions.add(jbuttonExportOverlap, c);
				c.gridy++;

			}
		}

		jPanelAddOptions.repaint();
	}

	private void addOverlappingControlsSeriePerExperiment(Integer numberOfSelectedCheckBoxes,
			boolean selectAllCheckBoxes, boolean addColorChooser) {
		// jPanelAddOptions.setLayout(new BoxLayout(jPanelAddOptions,
		// BoxLayout.PAGE_AXIS));
		jPanelAddOptions.setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(5, 0, 5, 0);
		c.gridx = 0;
		c.gridy = 0;
		log.info("Creating list of replicates...");
		if (numberOfSelectedCheckBoxes == null)
			numberOfSelectedCheckBoxes = Integer.MAX_VALUE;
		if (numberOfSelectedCheckBoxes.equals(Integer.MAX_VALUE)) {
			final JLabel label = new JLabel(
					"<html>Number of identifications that are detected<br><b>just</b> in each set.</html>");
			jPanelAddOptions.add(label, c);
			c.gridy++;
		}

		// Just in case of proteins
		if (ChartType.PROTEIN_OVERLAPING.equals(currentChartType)
				|| ChartType.EXCLUSIVE_PROTEIN_NUMBER.equals(currentChartType)
				|| ChartType.PEPTIDE_COUNTING_HISTOGRAM.equals(currentChartType)) {
			jPanelAddOptions.add(new JLabel("Protein groups are equal if (select one option):"), c);
			c.gridy++;
			// take into account just one protein per group
			jPanelAddOptions.add(optionsFactory.getJcheckBoxOneProteinPerGroup(), c);
			c.gridy++;
		}
		if (ChartType.EXCLUSIVE_PROTEIN_NUMBER.equals(currentChartType)
				|| ChartType.EXCLUSIVE_PEPTIDE_NUMBER.equals(currentChartType)) {
			jPanelAddOptions.add(optionsFactory.getAccumulativeTrendCheckBox(), c);
			c.gridy++;
		}

		if (ChartType.PEPTIDE_COUNTING_HISTOGRAM.equals(currentChartType)) {
			jPanelAddOptions.add(optionsFactory.getBinsPanel(), c);
			c.gridy++;
			jPanelAddOptions.add(optionsFactory.getHistogramTypePanel(), c);
			c.gridy++;
		}

		jPanelAddOptions.add(new JLabel("Select from the following checkBoxes:"), c);
		c.gridy++;
		jPanelAddOptions.add(optionsFactory.getExperimentsCheckboxes(selectAllCheckBoxes, 0, addColorChooser,
				getMethodToUpdateColorInChart()), c);
		c.gridy++;
		if (!numberOfSelectedCheckBoxes.equals(Integer.MAX_VALUE)
				&& !ChartType.PEPTIDE_COUNTING_HISTOGRAM.equals(currentChartType)) {
			// Add save image button
			final JButton jbuttonSave = new JButton("Save image");
			jbuttonSave.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					saveOverlappingImage();
				}
			});
			optionsFactory.getControlList().add(jbuttonSave);
			final int numDatasets = experimentList.getNumExperiments();
			String overlapString = "A,B";
			if (numDatasets > 1) {
				jPanelAddOptions.add(jbuttonSave, c);
				c.gridy++;
				final JLabel jLabelIntersectionsText = optionsFactory.getJLabelIntersectionsText();
				optionsFactory.setIntersectionText(null);
				jPanelAddOptions.add(jLabelIntersectionsText, c);
				c.gridy++;
				final JLabel labelExperiment = new JLabel("Export buttons:");
				jPanelAddOptions.add(labelExperiment, c);
				c.gridy++;
				// export just in 1 button
				final JButton jbuttonExportJustIn1 = new JButton("Export just in A");
				jbuttonExportJustIn1.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						exportJustIn1(null);
					}
				});
				jPanelAddOptions.add(jbuttonExportJustIn1, c);
				c.gridy++;
				// export just in 2 button
				final JButton jbuttonExportJustIn2 = new JButton("Export just in B");
				jbuttonExportJustIn2.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						exportJustIn2(null);
					}
				});
				jPanelAddOptions.add(jbuttonExportJustIn2, c);
				c.gridy++;

				if (numDatasets > 2) {
					// if (numberOfSelectedCheckBoxes > 2) {
					overlapString += ",C";
					// export just in 3 button
					final JButton jbuttonExportJustIn3 = new JButton("Export just in C");
					jbuttonExportJustIn3.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(java.awt.event.ActionEvent evt) {
							exportJustIn3(null);
						}
					});
					jPanelAddOptions.add(jbuttonExportJustIn3, c);
					c.gridy++;
				}
				// export overlapped button
				final JButton jbuttonExportOverlap = new JButton("Export Overlap (" + overlapString + ")");
				jbuttonExportOverlap.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						exportOverlapped(null);
					}
				});
				jPanelAddOptions.add(jbuttonExportOverlap, c);
				c.gridy++;

			}
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
				final JScrollPane jpanel = (JScrollPane) component;
				final Component component2 = jpanel.getComponent(0);
				if (component2 instanceof JViewport) {
					final JViewport viewPort = (JViewport) component2;
					final Component component3 = viewPort.getComponent(0);
					if (component3 instanceof JLabel) {
						final JLabel label = (JLabel) component3;
						final ImageIcon icon = (ImageIcon) label.getIcon();
						final Image image = icon.getImage();
						final JFileChooser fileChooser = new JFileChooser(MainFrame.currentFolder);
						fileChooser.setDialogTitle("Save heatmap");
						for (final ImageFileFormat imageFileFormat : ImageFileFormat.values()) {
							if (imageFileFormat == ImageFileFormat.GIF) {
								continue;// if doesnt allow to zoom
							}
							fileChooser.addChoosableFileFilter(new ExtensionFileFilter(imageFileFormat.getExtension(),
									imageFileFormat.getDescription()));
						}
						fileChooser.setAcceptAllFileFilterUsed(false);

						final int retVal = fileChooser.showSaveDialog(this);

						if (retVal == JFileChooser.APPROVE_OPTION) {
							file = fileChooser.getSelectedFile();
							if ("".equals(FilenameUtils.getExtension(file.getAbsolutePath()))) {
								file = new File(file.getAbsolutePath() + "."
										+ ImageFileFormat
												.getFromDescription(fileChooser.getFileFilter().getDescription())
												.getExtension());
							}
							MainFrame.currentFolder = file.getParentFile();
							final String path = ImageUtils.saveImage(image, file);
							appendStatus("Image file saved to:" + path);
						}
					}
					return;
				}
			} else if (component instanceof JPanel) {
				final JPanel jpanel = (JPanel) component;
				for (final Component component2 : jpanel.getComponents()) {
					// if (component2 instanceof JPanel) {
					// JPanel jpanel2 = (JPanel) component2;
					final Component component3 = jpanel.getComponent(0);
					if (component3 instanceof JLabel) {
						final JLabel label = (JLabel) component3;
						final ImageIcon icon = (ImageIcon) label.getIcon();
						final Image image = icon.getImage();
						final JFileChooser fileChooser = new JFileChooser(MainFrame.currentFolder);
						fileChooser.setDialogTitle("Save heatmap");
						for (final ImageFileFormat imageFileFormat : ImageFileFormat.values()) {
							if (imageFileFormat == ImageFileFormat.GIF) {
								continue;// if doesnt allow to zoom
							}
							fileChooser.addChoosableFileFilter(new ExtensionFileFilter(imageFileFormat.getExtension(),
									imageFileFormat.getDescription()));
						}
						fileChooser.setAcceptAllFileFilterUsed(false);
						final int retVal = fileChooser.showSaveDialog(this);

						if (retVal == JFileChooser.APPROVE_OPTION) {
							file = fileChooser.getSelectedFile();
							if ("".equals(FilenameUtils.getExtension(file.getAbsolutePath()))) {
								file = new File(file.getAbsolutePath() + "."
										+ ImageFileFormat
												.getFromDescription(fileChooser.getFileFilter().getDescription())
												.getExtension());
							}
							MainFrame.currentFolder = file.getParentFile();
							final String path = ImageUtils.saveImage(image, file);
							appendStatus("Image file saved to:" + path);
						}
					}

				}
				return;
			}
		} catch (final IOException e) {
			error = e.getMessage();
			e.printStackTrace();
		} catch (final Exception e) {
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
		final JFileChooser fileChooser = new JFileChooser(MainFrame.currentFolder);
		try {
			final Component component = jPanelChart.getComponent(0);
			if (component instanceof JPanel) {
				final JPanel jpanel = (JPanel) component;
				for (final Component component2 : jpanel.getComponents()) {
					if (component2 instanceof JPanel) {
						final JPanel jpanel2 = (JPanel) component2;
						final Component component3 = jpanel2.getComponent(0);
						if (component3 instanceof JLabel) {
							final JLabel label = (JLabel) component3;
							String title = "";
							if (label instanceof ImageLabel) {
								title = ((ImageLabel) label).getTitle();
							}
							final ImageIcon icon = (ImageIcon) label.getIcon();
							if (icon == null) {// image not selected
								continue;
							}
							final Image image = icon.getImage();

							for (final ImageFileFormat imageFileFormat : ImageFileFormat.values()) {
								fileChooser.addChoosableFileFilter(new ExtensionFileFilter(
										imageFileFormat.getExtension(), imageFileFormat.getDescription()));
							}
							fileChooser.setDialogTitle("Saving overlap image '" + title + "'");
							fileChooser.setAcceptAllFileFilterUsed(false);
							final int retVal = fileChooser.showSaveDialog(this);

							if (retVal == JFileChooser.APPROVE_OPTION) {
								file = fileChooser.getSelectedFile();
								MainFrame.currentFolder = file.getParentFile();

								final String path = ImageUtils.saveImage(image, file);
								appendStatus("Image file saved to:" + path);
							}
						}

					}
				}
				return;
			}
		} catch (final IOException e) {
			error = e.getMessage();
			e.printStackTrace();
		} catch (final Exception e) {
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
				final JPanel jpanel = (JPanel) component;
				final Component component2 = jpanel.getComponent(0);
				if (component2 instanceof JLabel) {
					final JLabel label = (JLabel) component2;
					String title = "";
					if (label instanceof ImageLabel) {
						title = ((ImageLabel) label).getTitle();
					}
					final ImageIcon icon = (ImageIcon) label.getIcon();
					final Image image = icon.getImage();
					final JFileChooser fileChooser = new JFileChooser(MainFrame.currentFolder);
					for (final ImageFileFormat imageFileFormat : ImageFileFormat.values()) {
						fileChooser.addChoosableFileFilter(new ExtensionFileFilter(imageFileFormat.getExtension(),
								imageFileFormat.getDescription()));
					}
					fileChooser.setDialogTitle("Saving overlap image '" + title + "'");
					fileChooser.setAcceptAllFileFilterUsed(false);
					final int retVal = fileChooser.showSaveDialog(this);

					if (retVal == JFileChooser.APPROVE_OPTION) {
						file = fileChooser.getSelectedFile();
						if ("".equals(FilenameUtils.getExtension(file.getAbsolutePath()))) {
							file = new File(file.getAbsolutePath() + "." + ImageFileFormat
									.getFromDescription(fileChooser.getFileFilter().getDescription()).getExtension());
						}
						MainFrame.currentFolder = file.getParentFile();
						final String path = ImageUtils.saveImage(image, file);
						appendStatus("Image file saved to:" + path);
					}
					return;
				}
			}
		} catch (final IOException e) {
			error = e.getMessage();
			e.printStackTrace();
		} catch (final Exception e) {
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
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		final JPanel jPanelAdditional1 = optionsFactory.getHistogramTypePanel();
		c.gridx = 0;
		c.gridy = 0;
		panel.add(jPanelAdditional1, c);

		// //////////////// ROW2
		final JPanel jPanelAdditional2 = optionsFactory.getBinsPanel();

		c.gridy++;
		panel.add(jPanelAdditional2, c);

		if (showScoreNames) {
			if (currentChartType.getName().contains("rotein")) {
				// //////////////// ROW3
				final DefaultComboBoxModel<String> proteinScoreNames = getProteinScoreNames();
				if (proteinScoreNames != null) {
					final JPanel jPanelAdditional3 = optionsFactory.getProteinScorePanel(proteinScoreNames);
					c.gridy++;
					panel.add(jPanelAdditional3, c);
				}
			}
			if (currentChartType.getName().contains("eptide")) {
				// //////////////// ROW4
				final DefaultComboBoxModel<String> peptideScoreNames = getPeptideScoreNames();
				if (peptideScoreNames != null) {
					final JPanel jPanelAdditional4 = optionsFactory.getPeptideScorePanel(peptideScoreNames);
					c.gridy++;
					panel.add(jPanelAdditional4, c);

				}
			}
			// //////////////// ROW
			final JCheckBox jCheckApplyLog = optionsFactory.getApplyLogCheckBox();
			c.gridy++;
			panel.add(jCheckApplyLog, c);
		}
		if (addMOverZComboBox) {
			// //////////////// ROW5
			final JPanel jPanelAdditional3 = optionsFactory.getMOverZPanel();
			c.gridy++;
			panel.add(jPanelAdditional3, c);
		}

		final String option = (String) jComboBoxChartOptions.getSelectedItem();
		if (!option.equals(ONE_SERIES_PER_EXPERIMENT_LIST)) {
			final JCheckBox showTotalSerieCheckBox = optionsFactory.getShowTotalSerieCheckBox(false);
			if (showTotalSerieCheckBox != null) {
				c.gridy++;
				panel.add(showTotalSerieCheckBox, c);
			}
		}

		if (currentChartType.equals(ChartType.PEPTIDE_SCORE_DISTRIBUTION)
				|| currentChartType.equals(ChartType.PROTEIN_SCORE_DISTRIBUTION)) {
			final JCheckBox separateDecoyHits = optionsFactory.getSeparatedDecoyHitsCheckBox();
			separateDecoyHits.setEnabled(experimentList.getFDRFilter() != null);
			c.gridy++;
			panel.add(separateDecoyHits, c);
		}

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);
	}

	private void addHistogramBarControls(boolean showPeptideHistogram) {
		jPanelAddOptions.removeAll();
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		final JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();
		c.gridx = 0;
		c.gridy = 0;

		panel.add(jPanelAdditional1, c);
		// /////////////// ROW2
		final JCheckBox checkbox2 = optionsFactory.getShowAsStackedChartCheckBox();
		c.gridx = 0;
		c.gridy = 1;

		panel.add(checkbox2, c);
		// /////////////// ROW3
		final JCheckBox checkbox3 = optionsFactory.getShowAsPercentageCheckBox();
		c.gridx = 0;
		c.gridy = 2;

		panel.add(checkbox3, c);
		// /////////////// ROW4
		final JCheckBox jCheckBox = optionsFactory.getShowAsPieChartCheckBox();
		c.gridx = 0;
		c.gridy = 3;
		panel.add(jCheckBox, c);
		// /////////////// ROW5
		final String option = (String) jComboBoxChartOptions.getSelectedItem();
		if (ONE_SERIES_PER_EXPERIMENT.equals(option)) {
			final JCheckBox jCheckBox2 = optionsFactory.getShowAverageOverReplicatesCheckBox();
			c.gridx = 0;
			c.gridy = 4;
			panel.add(jCheckBox2, c);
		}
		// /////////////// ROW6
		final JCheckBox jCheckBox6 = optionsFactory.getShowTotalSerieCheckBox(true);
		if (jCheckBox6 != null && !isOccurrenceFilterEnabled()) {
			c.gridx = 0;
			c.gridy = 5;
			panel.add(jCheckBox6, c);
		}
		// /////////////// ROW7
		if (showPeptideHistogram) {
			final JCheckBox jCheckBox7 = optionsFactory.getShowDifferentIdentificationsCheckBox("PSMs or peptides");
			jCheckBox7.setToolTipText(
					"<html>If this option is activated, the histogram will show the <b>number of PSMs</b>.<br>"
							+ "If this option is desactivated, the histogram will show the <b>number of peptides</b>.</html>");
			c.gridx = 0;
			c.gridy = 6;
			panel.add(jCheckBox7, c);
		}
		// /////////////// ROW7
		final JCheckBox jCheckBox8 = optionsFactory.getShowTotalVersusDifferentCheckBox();
		c.gridx = 0;
		c.gridy++;
		panel.add(jCheckBox8, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);
	}

	private void addChargeHistogramBarControls() {
		jPanelAddOptions.removeAll();
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);
		// /////////////// ROW1
		final JPanel jPanelAdditional1 = optionsFactory.getPlotOrientationPanel();
		c.gridx = 0;
		c.gridy = 0;

		panel.add(jPanelAdditional1, c);
		// /////////////// ROW2
		final JCheckBox checkbox2 = optionsFactory.getShowAsStackedChartCheckBox();
		c.gridx = 0;
		c.gridy = 1;

		panel.add(checkbox2, c);
		// /////////////// ROW3
		final JCheckBox checkbox3 = optionsFactory.getShowAsPercentageCheckBox();
		c.gridx = 0;
		c.gridy = 2;

		panel.add(checkbox3, c);

		jPanelAddOptions.setLayout(new BorderLayout());
		jPanelAddOptions.add(panel, BorderLayout.NORTH);
	}

	void updateControlStates() {
		final ChartType chartType = currentChartType;
		// String options = (String)
		// this.jComboBoxChartOptions.getSelectedItem();
		// TODO

		// ENABLE PROTEIN AND DISABLE PEPTIDE CONTROLS
		if (ChartType.PROTEIN_SCORE_COMPARISON.equals(chartType)
				|| ChartType.PROTEIN_SCORE_DISTRIBUTION.equals(chartType)) {
			optionsFactory.enablePeptideScoreNameControls(false);
			optionsFactory.enableProteinScoreNameControls(true);

			// ENABLE PEPTIDE AND DISABLE PROTEIN CONTROLS
		} else if (ChartType.PEPTIDE_SCORE_COMPARISON.equals(chartType)
				|| ChartType.PEPTIDE_SCORE_DISTRIBUTION.equals(chartType) || ChartType.FDR.equals(chartType)
				|| ChartType.PEPTIDE_REPEATABILITY.equals(chartType)) {
			optionsFactory.enablePeptideScoreNameControls(true);
			optionsFactory.enableProteinScoreNameControls(false);
		}
		if (ChartType.PEPTIDE_NUMBER_HISTOGRAM.equals(chartType) || ChartType.PEPTIDE_OVERLAPING.equals(chartType)
				|| ChartType.PEPTIDE_OCCURRENCE_HEATMAP.equals(chartType)
				|| ChartType.PSMS_PER_PEPTIDE_HEATMAP.equals(chartType)
				|| ChartType.PEPTIDE_SCORE_COMPARISON.equals(chartType)
				|| ChartType.PEPTIDE_MONITORING.equals(chartType) || ChartType.PEPTIDE_REPEATABILITY.equals(chartType)
				|| ChartType.PEPTIDE_PRESENCY_HEATMAP.equals(chartType) || ChartType.PSM_PEP_PROT.equals(chartType)
				|| ChartType.EXCLUSIVE_PEPTIDE_NUMBER.equals(chartType)
				|| ChartType.PEPTIDES_PER_PROTEIN_HEATMAP.equals(chartType))
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

	public String getChartSubtitle(ChartType chartType, String option) {
		// if (chartType.equals(PROTEIN_SCORE_DISTRIBUTION)) {
		// if (option.equals(ONE_SERIES_PER_REPLICATE))
		// return ONE_SERIES_PER_REPLICATE;
		// }
		if (option != null)
			return option;
		return "";

	}

	public String getChartTitle(ChartType chartType) {
		// if (chartType.equals(PROTEIN_SCORE_DISTRIBUTION)) {
		// return "Protein score distribution";
		// }else if (PEPTIDE_SCORE_DISTRIBUTION.equals(chartType)){
		// return "Peptide score distribution";
		// }
		return chartType.getName();

	}

	public void showChart() {

		final ChartType chartType = currentChartType;
		final String option = (String) jComboBoxChartOptions.getSelectedItem();

		// if (chartCreator != null &&
		// chartCreator.getState().equals(StateValue.STARTED)) {
		// log.info("Trying to cancel chart creator from thread " +
		// Thread.currentThread().getId());
		// while (!chartCreator.cancel(true)) {
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
		// log.info("Chart creator cancelled from thread " +
		// Thread.currentThread().getId());
		//
		// }

		if (!creatingChartLock && (chartCreator == null || chartCreator.getState().equals(StateValue.DONE))
				&& filterDialog.isFilterTaskFinished()) {
			log.info("Trying to acquire the lock by thread " + Thread.currentThread().getId());
			if (!chartCreatorlock.tryLock()) {
				log.info(
						"Chart creation in progress. Skiping this call... by thread " + Thread.currentThread().getId());
				return;
			}
			log.debug("lock acquired by thread " + Thread.currentThread().getId());
			creatingChartLock = true;
			enableStateKeeper.keepEnableStates(this);
			enableStateKeeper.disable(this);
			appendStatus("Creating chart '" + chartType.getName() + "' (" + option + ")...");
			setProgressBarIndeterminate(true);

			chartCreator = new ChartCreatorTask(this, chartType, option, experimentList);
			chartCreator.addPropertyChangeListener(this);
			chartCreator.execute();
			t1 = System.currentTimeMillis();

		} else {
			log.info("The chart cannot be generated until the previous chart is finished");
			// this.appendStatus("Wait to finish the chart");
		}
	}

	public void registerNewValue(Object component) {
		final Object currentValue = getcurrentvalueFromComponent(component);
		previousTogleValues.put(component, currentValue);
	}

	public void startShowingChart(Object causantComponent) {
		if (causantComponent != null) {
			try {
				final Object currentValue = getcurrentvalueFromComponent(causantComponent);
				if (currentValue != null) {
					if (previousTogleValues.containsKey(causantComponent)) {
						final Object previousValuesOfSelection = previousTogleValues.get(causantComponent);

						if (currentValue.equals(previousValuesOfSelection)) {
							log.info("Component didnt really changed: " + currentValue + ", causant: "
									+ causantComponent);
							return;
						}
					}
					previousTogleValues.put(causantComponent, currentValue);
				}
			} catch (final IllegalArgumentException e) {
				// this is because the causant is a menu and it is not selected,
				// so we dont want it to trigger the chart generator
				log.info("component is a menu but it is not selected. Skipping execution of chart creator");
				previousTogleValues.put(causantComponent, false);
				return;
			}
		}

		if (dataLoader == null || !dataLoader.isDone()) {
			// appendStatus("Datasets are being loaded. Please wait...");
			return;
		}

		setEmptyChart();

		updateOptionComboBox();

		applyFilters();

	}

	private Object getcurrentvalueFromComponent(Object causantComponent) {
		if (causantComponent instanceof JCheckBox) {
			final JCheckBox checkBox = (JCheckBox) causantComponent;
			return checkBox.isSelected();
		} else if (causantComponent instanceof JRadioButtonMenuItem) {
			final JRadioButtonMenuItem ratioItem = (JRadioButtonMenuItem) causantComponent;
			return ratioItem.isSelected();
		} else if (causantComponent instanceof JComboBox) {
			final JComboBox combo = (JComboBox) causantComponent;
			return combo.getSelectedItem();
		} else if (causantComponent instanceof JRadioButton) {
			final JRadioButton combo = (JRadioButton) causantComponent;
			return combo.isSelected();
		} else {
			log.info("Class " + causantComponent.getClass());
		}
		return null;
	}

	// GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.ButtonGroup chartTypeMenuButtonGroup;
	private javax.swing.JButton jButtonExport2Excel;
	private javax.swing.JButton jButtonExport2PRIDE;
	private javax.swing.JButton jButtonSaveAsFiltered;
	private javax.swing.JButton jButtonSeeAppliedFilters;
	private javax.swing.JButton jButtonShowTable;
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
	private javax.swing.JMenu jMenuOptions;
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
	private AdditionalOptionsPanelFactory additionalOptionsPanelFactory;
	private JButton jButtonCancel;
	private JButton jButtonHelp;

	private boolean proteinSequencesRetrieved;

	private boolean filtered;

	// End of variables declaration//GEN-END:variables

	@Override
	public synchronized void propertyChange(PropertyChangeEvent evt) {

		if ("progress".equals(evt.getPropertyName())) {
			final int progress = (Integer) evt.getNewValue();
			if (evt.getSource().equals(memoryChecker))
				jProgressBarMemoryUsage.setValue(progress);
			else
				jProgressBar.setValue(progress);

		} else if (ChartCreatorTask.CHART_GENERATION_STARTED.equals(evt.getPropertyName())) {

		} else if (ChartCreatorTask.CHART_CANCELED.equals(evt.getPropertyName())) {
			enableStateKeeper.setToPreviousState(this);
			log.info("Unlocking lock because task cancelled from thread: " + Thread.currentThread().getId());
			chartCreatorlock.unlock();
			creatingChartLock = false;
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
			final String notificacion = evt.getNewValue().toString();
			appendStatus(notificacion);
		} else if (DataLoaderTask.DATA_LOADED_START.equals(evt.getPropertyName())) {
			enableStateKeeper.keepEnableStates(this);
			enableStateKeeper.disable(this);
			setProteinSequencesRetrieved(false);

			appendStatus("Reading project data...");
			appendStatus("Depending on the size of the data, it can take a few seconds or a couple of minutes...");
			setEmptyChart();

			setProgressBarIndeterminate(true);
		} else if (DataLoaderTask.DATA_LOADED_DONE.equals(evt.getPropertyName())) {
			enableStateKeeper.setToPreviousState(this);
			errorLoadingData = false;

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
			// apply sorting parameters (this has to be after the customization
			// controls)
			updateControlStates();
			enableMenus(true);
			jButtonShowTable.setEnabled(true);
			jButtonExport2Excel.setEnabled(true);
			jButtonExport2PRIDE.setEnabled(true);
			startShowingChart(null);

		} else if (DataLoaderTask.DATA_LOADED_ERROR.equals(evt.getPropertyName())) {
			enableStateKeeper.setToPreviousState(this);
			jButtonCancel.setEnabled(false);
			errorLoadingData = true;
			final String erromessage = (String) evt.getNewValue();
			setProgressBarIndeterminate(false);
			final GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.WEST;
			c.gridx = 0;
			c.gridy = 0;
			final JLabel label = new JLabel(erromessage);
			label.setBackground(Color.white);
			final JPanel panel = new JPanel();
			panel.setBackground(Color.white);
			panel.add(label, c);
			jPanelChart.add(panel);
			JOptionPane.showMessageDialog(this, erromessage);
			appendStatus(erromessage);
			dispose();
		} else if (ChartCreatorTask.CHART_GENERATED.equals(evt.getPropertyName())
				|| ChartCreatorTask.CHART_ERROR_GENERATED.equals(evt.getPropertyName())) {

			try { // to release the lock
				jPanelChart.removeAll();
				final BorderLayout borderLayout = new BorderLayout();
				jPanelChart.setLayout(borderLayout);

				final Object object = evt.getNewValue();

				if (object instanceof JComponent) {
					// this.jPanelChart.setGraphicPanel((JComponent) object);
					final JComponent jComponent = (JComponent) object;
					jPanelChart.add(jComponent, BorderLayout.CENTER);
					if (jComponent instanceof ChartPanel) {
						((ChartPanel) jComponent).updateUI();
					} else {
						if (jComponent.getComponent(0) instanceof JComponent) {
							((JComponent) jComponent.getComponent(0)).updateUI();
						}
					}
				} else if (object instanceof List) {
					final List lista = (List) object;
					if (lista.get(0) instanceof JPanel) {
						final List<JPanel> chartList = (List<JPanel>) object;
						final JPanel panel = new JPanel();
						panel.setLayout(new GridLayout(chartList.size(), 1, 0, 20));
						jPanelChart.add(panel, BorderLayout.CENTER);
						for (final JPanel jPanel : chartList) {
							panel.add(jPanel);
							if (jPanel instanceof ChartPanel) {
								((ChartPanel) jPanel).updateUI();
							} else {
								if (jPanel.getComponent(0) instanceof JComponent) {
									((JComponent) jPanel.getComponent(0)).updateUI();
								}

							}
						}
					} else if (lista.get(0) instanceof Panel) {
						final List<Panel> chartList = (List<Panel>) object;
						final JPanel panel = new JPanel();
						panel.setLayout(new GridLayout(chartList.size(), 1, 0, 20));
						for (final Panel jPanel : chartList) {
							panel.add(jPanel);
							if (jPanel instanceof WordCramChart) {
								final WordCramChart wordCramChart = (WordCramChart) jPanel;
								wordCramChart.addPropertyChangeListener(this);
								wordCramChart.draw(Double.valueOf(jPanelChart.getSize().getWidth()).intValue(), 550);
							}
						}

						jPanelChart.add(panel, BorderLayout.CENTER);
						jScrollPaneChart.getViewport().addChangeListener(new ChangeListener() {

							@Override
							public void stateChanged(ChangeEvent e) {
								jScrollPaneChart.revalidate();
								ChartManagerFrame.this.repaint();
							}
						});

					}
					// this.jPanelChart.addGraphicPanel((List<JPanel>) object);
				} else if (object instanceof Component) {
					jPanelChart.add((Component) object, BorderLayout.CENTER);
					if (object instanceof WordCramChart) {
						final WordCramChart wordCramChart = (WordCramChart) object;
						wordCramChart.addPropertyChangeListener(this);
						wordCramChart.draw(Double.valueOf(jPanelChart.getSize().getWidth()).intValue(),
								Double.valueOf(jPanelChart.getSize().getHeight()).intValue());
					}
				}
				jPanelChart.repaint();
				if (ChartCreatorTask.CHART_GENERATED.equals(evt.getPropertyName())) {
					// disable scroll in case of current chart is a word clod
					if (currentChartType.equals(ChartType.PROTEIN_NAME_CLOUD)) {
						jScrollPaneChart.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
						jScrollPaneChart.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
					} else {
						jScrollPaneChart.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
						jScrollPaneChart
								.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

					}

					setNumIdentificationsLabel();
					setFDRLabel();
					final double t2 = System.currentTimeMillis() * 1.0;
					appendStatus("Chart created in " + DatesUtil.getDescriptiveTimeFromMillisecs(t2 - t1));
					if (currentChartType.equals(ChartType.PROTEIN_NAME_CLOUD)) {
						appendStatus("Wait some seconds for the cloud loading...");
					}

				} else if (ChartCreatorTask.CHART_ERROR_GENERATED.equals(evt.getPropertyName())) {
					appendStatus("Error generating the chart.");
				}
				setProgressBarIndeterminate(false);

			} finally {
				log.debug("Unlocking lock from thread " + Thread.currentThread().getId());
				enableStateKeeper.setToPreviousState(this);

				updateControlStates();
				jPanelAddOptions.updateUI();
				if (!filterDialog.getFilters().isEmpty()) {
					jButtonSeeAppliedFilters.setEnabled(true);
					jButtonSaveAsFiltered.setEnabled(true);

				} else {
					jButtonSeeAppliedFilters.setEnabled(false);
					jButtonSaveAsFiltered.setEnabled(false);
				}
				jButtonCancel.setEnabled(false);
				chartCreatorlock.unlock();
				creatingChartLock = false;
			}
		} else if (ChartCreatorTask.DATASET_PROGRESS.equals(evt.getPropertyName())) {
			final String message = (String) evt.getNewValue();
			jProgressBar.setStringPainted(true);
			jProgressBar.setString(message);

		} else if (MemoryCheckerTask.MEMORY_CHECKER_DATA_REPORT.equals(evt.getPropertyName())) {

			final String memoryUsage = (String) evt.getNewValue();
			jProgressBarMemoryUsage.setString("Memory usage: " + memoryUsage);
		} else if (CuratedExperimentSaver.CURATED_EXP_SAVER_START.equals(evt.getPropertyName())) {
			enableStateKeeper.keepEnableStates(this);
			enableStateKeeper.disable(this);
			appendStatus("Saving dataset(s) as curated...");
			appendStatus("This task will be performed in background");
			jButtonSaveAsFiltered.setEnabled(false);
		} else if (CuratedExperimentSaver.CURATED_EXP_SAVER_END.equals(evt.getPropertyName())) {
			enableStateKeeper.setToPreviousState(this);

			setProgressBarIndeterminate(false);
			jProgressBar.setString("");
			jProgressBar.setValue(0);
			appendStatus("Datasets saved as curated.");
			jButtonSaveAsFiltered.setEnabled(true);

		} else if (CuratedExperimentSaver.CURATED_EXP_SAVER_ERROR.equals(evt.getPropertyName())) {
			enableStateKeeper.setToPreviousState(this);
			jButtonCancel.setEnabled(false);
			setProgressBarIndeterminate(false);
			jProgressBar.setString("");
			appendStatus("Error saving curated datasets: " + evt.getNewValue());
			jButtonSaveAsFiltered.setEnabled(true);

		} else if (CuratedExperimentSaver.CURATED_EXP_SAVER_PROGRESS.equals(evt.getPropertyName())) {
			appendStatus((String) evt.getNewValue());

		} else if (OntologyLoaderTask.ONTOLOGY_LOADING_FINISHED.equals(evt.getPropertyName())) {
			log.info("Ontologies loaded.");
			final long time = (Long) evt.getNewValue();

			appendStatus("Ontologies loaded in " + DatesUtil.getDescriptiveTimeFromMillisecs(time));
			jProgressBar.setIndeterminate(false);
			final CPExperimentList cpExpList = getCPExperimentList(cfgFile);
			dataLoader = new DataLoaderTask(cpExpList, minPeptideLength, groupProteinsAtExperimentListLevel, null,
					isLocalProcessingInParallel, isAnnotateProteinsInUniprot(),
					GeneralOptionsDialog.getInstance(this).isDoNotGroupNonConclusiveProteins(),
					GeneralOptionsDialog.getInstance(this).isSeparateNonConclusiveProteins());
			// filters);
			dataLoader.addPropertyChangeListener(this);
			dataLoader.execute();
		} else if (OntologyLoaderTask.ONTOLOGY_LOADING_STARTED.equals(evt.getPropertyName())) {
			appendStatus("Loading ontologies. Please wait...");
			jProgressBar.setIndeterminate(true);
		} else if (OntologyLoaderTask.ONTOLOGY_LOADING_ERROR.equals(evt.getPropertyName())) {
			appendStatus("Error loading ontologies. Please contact to miape-support@proteored.org.");
			appendStatus((String) evt.getNewValue());
		} else if (WordCramChart.WORDCRAMCREATED.equals(evt.getPropertyName())) {
			appendStatus("Protein words cloud created");
		}

	}

	private void destroyWordCrams(Container container) {
		if (container.getComponentCount() > 0) {
			for (final Component component2 : container.getComponents()) {
				if (component2 instanceof WordCramChart) {
					final WordCramChart chart = (WordCramChart) component2;
					chart.destroy();
				} else {
					if (component2 instanceof Container) {
						destroyWordCrams((Container) component2);
					}
				}
			}
		}
	}

	private void enableMenus(boolean b) {
		jMenuFilters.setEnabled(b);
		jMenuChartType.setEnabled(b);
		jMenuOptions.setEnabled(b);
	}

	private DefaultComboBoxModel<String> getProteinScoreNames() {
		final List<String> scoreNames = experimentList.getProteinScoreNames();
		if (scoreNames != null && !scoreNames.isEmpty()) {
			Collections.sort(scoreNames);
			return new DefaultComboBoxModel<String>(scoreNames.toArray(new String[0]));
		}
		return null;
	}

	private DefaultComboBoxModel<String> getPeptideScoreNames() {
		final List<String> scoreNames = experimentList.getPeptideScoreNames();
		if (scoreNames != null && !scoreNames.isEmpty()) {
			Collections.sort(scoreNames);
			return new DefaultComboBoxModel<String>(scoreNames.toArray(new String[0]));
		}
		return null;
	}

	public void appendStatus(String notificacion) {
		if (notificacion == null) {
			return;
		}
		final String formatedDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG)
				.format(new Date(System.currentTimeMillis()));
		jTextAreaStatus.append(formatedDate + ": " + notificacion + "\n");
		jTextAreaStatus.setCaretPosition(jTextAreaStatus.getText().length() - 1);

	}

	void setStatus(String notificacion) {
		if (notificacion == null) {
			return;
		}
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

	public void setInformation1(ChartType chartType) {
		jLabelInformation1.setText(chartType.getName());
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

	public ChartType getCurrentChartType() {
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
					+ "<ul><li>Lower levels (level 1 or level 2) have different FDR thresholds.</li>"
					+ "<li>Lower levels have used different search engines and therefore, the score used to calculate the FDR filter is different.</li></ul>");
		else
			setToolTipInformation2("<html>Global False Discovery Rates: " + "<ul><li>at Protein level </li>"
					+ "<li>at Peptide level</li>" + "<li>at Peptide Spectrum Match level</li></ul>"
					+ "These values are calculated at level 0.</html>");

	}

	public void setNumIdentificationsLabel() {
		setInformation3(getNumberIdentificationsString());
		setToolTipInformation3(
				"<html>These numbers means:<ul><li>Number of peptides and unique peptides, NOT distinguishing differently modificated peptides.</li>"
						+ "<li>Number of proteins/protein groups and unique proteins/protein groups (not considering Non-conclusive proteins).</li>"
						+ "<li>Number of different human genes (if mapped) and, in brackets number of human genes just taking one per proteing group.</li></ul></html>");
	}

	public void setEmptyChart() {
		destroyWordCrams(jPanelChart);
		jPanelChart.removeAll();
		jPanelChart.repaint();
	}

	public void exportChr16Proteins() {
		final Set<String> filterProteinACC = GeneDistributionReader.getInstance().getProteinGeneMapping("16").keySet();

		filterDialog.enableProteinACCFilter(filterProteinACC);
		filterDialog.applyFilters(experimentList);

	}

	// public boolean isChr16ChartShowed() {
	// return this.isChr16ChartShowed;
	// }

	public void clearIdentificationTable() {
		identificationTable = null;

	}

	public FiltersDialog getFiltersDialog() {
		return filterDialog;
	}

	public JTextArea getTextAreaStatus() {
		return jTextAreaStatus;
	}

	public void saveWordCramImage() {
		if (currentChartType.equals(ChartType.PROTEIN_NAME_CLOUD)) {
			log.info("Saving Protein name cloud");
			String error = "";
			try {
				final List<WordCramChart> charts = new ArrayList<WordCramChart>();
				final Component component = jPanelChart.getComponent(0);
				if (component instanceof WordCramChart) {
					final WordCramChart wordCram = (WordCramChart) component;
					charts.add(wordCram);

				} else if (component instanceof JPanel) {
					final JPanel jpanel = (JPanel) component;
					if (jpanel.getComponent(0) instanceof WordCramChart) {

						for (final Component component2 : jpanel.getComponents()) {
							if (component2 instanceof WordCramChart) {
								charts.add((WordCramChart) component2);
							}
						}

					}
				}
				saveWordCramCharts(charts);
			} catch (final Exception e) {
				error = e.getMessage();
				e.printStackTrace();
			}

			appendStatus(error);
		}
	}

	private void saveWordCramCharts(List<WordCramChart> charts) {
		if (charts != null && !charts.isEmpty()) {

			final JFileChooser fileChooser = new JFileChooser(MainFrame.currentFolder);
			fileChooser.addChoosableFileFilter(new ExtensionFileFilter("tif", "TIFF Image File"));

			fileChooser.setDialogTitle("Saving protein word cloud image");
			fileChooser.setAcceptAllFileFilterUsed(false);
			final int retVal = fileChooser.showSaveDialog(this);

			if (retVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				if ("".equals(FilenameUtils.getExtension(file.getAbsolutePath()))) {
					file = new File(file.getAbsolutePath() + ".tif");
				}
				MainFrame.currentFolder = file.getParentFile();
				for (int i = 0; i < charts.size(); i++) {
					if (i > 0) {
						file = getNextFile(file, i);
					}
					final WordCramChart wordCram = charts.get(i);
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
		// now we count them, because it will depend on the user when selecting
		// the option to separate the subset (nonconclusive) proteins in
		// different groups or not
		return true;
	}

	public void setErrorLoadingData(boolean b) {
		errorLoadingData = b;
	}

	public List<String> getProteinAccsFromACCFilter() {
		final List<Filter> filters = filterDialog.getFilters();
		if (filters != null) {
			for (final Filter filter : filters) {
				if (filter instanceof ProteinACCFilterByProteinComparatorKey) {
					final ProteinACCFilterByProteinComparatorKey proteinAccFilter = (ProteinACCFilterByProteinComparatorKey) filter;
					return proteinAccFilter.getSortedAccessions();
				}
			}
		}
		return new ArrayList<String>();
	}

	public List<String> getPeptideSequencesFromPeptideSequenceFilter() {
		final List<Filter> filters = filterDialog.getFilters();
		if (filters != null) {
			for (final Filter filter : filters) {
				if (filter instanceof PeptideSequenceFilter) {
					final PeptideSequenceFilter peptideSequenceFilter = (PeptideSequenceFilter) filter;
					return peptideSequenceFilter.getSortedSequences();
				}
			}
		}
		return new ArrayList<String>();
	}

	/**
	 * @param additionalOptionsPanelFactory
	 */
	public void setAdditionalOptionsPanelFactory(AdditionalOptionsPanelFactory additionalOptionsPanelFactory) {
		this.additionalOptionsPanelFactory = additionalOptionsPanelFactory;
	}

	public AdditionalOptionsPanelFactory getAdditionalOptionsPanelFactory() {
		return additionalOptionsPanelFactory;
	}

	@Override
	public List<String> getHelpMessages() {
		final String[] ret = { "Chart Viewer help", //
				"This window is where you will be able to explore and compare your datasets. The main panel will contain the chart and it will be automatically updated with any customization option selected.", //
				"<b>Select a chart</b>", //
				"You will be able to change the chart type by selecting a different <b>'Chart Type'</b> in the menu at any time. The chart types are arranged into 10 categories.", //
				"<b>Customize your chart</b>", //
				"Most of the charts provide a set of controls inside of the <b>'Additional options'</b> panel at the left.", //
				"Using those options (clicking on checkboxes, radio buttons, selecting from drop-down menus, etc), will automatically reload the chart.", //
				"You can also edit the appearance of the chart <b>by right-clicking on the chart</b> panel surface and selecting the desired option. Among them, you can change the font family, size and color, the axis scales or the tick labels and marks.", //
				"<b>Export chart images</b>", //
				"You can export the charts in different formats (<b>PNG</b>, <b>SVG</b> or <b>PDF</b>), copy the chart image to the clipboard, ", //
				"<b>Peptide counting</b>", //
				"At the middle left you can find the <b>'Peptide counting'</b> panel with a checkbox to <b>distinguish modified peptides</b>. "
						+ "If this option is <i>selected</i>, peptides with the same sequence but having different modification states will be counted "
						+ "as different peptides. If this option is <i>not selected</i>, peptides with the same sequence will be counted once regardless"
						+ " the modification state. You can try to select and deselect the checkbox and observe how the number of peptides changes in the charts that shows the number of peptides.", //
				"<b>Comparison level</b>", //
				"Just above the <i>Peptide counting</i> option, you will find the <b>Comparison level</b> drop-down menu.", //
				"Depending on which option you select, you will be able to explore different levels of aggregation of the data, depending on the organization of the <i>three-level Comparison Project Tree</i>:", //
				"- <b>one single data series (level 0)</b>: a chart with just one data series which aggregates all the individual datasets,", //
				"- <b>one data series per level 1</b>: a chart with one data series per each of the level 1 nodes which aggregates all the individual datasets pending from that node, ", //
				"- <b>one data series per level 2</b>: a chart with one data series per each of the level 2 nodes which aggregates all the individual datasets pending from that node,", //
				"- <b>one separate chart per level 1</b>: this will generate a different chart per each one of the level 1 nodes. Each of these charts will contain a data series per level 2 nodes pending on that level 1 node.", //
				"<b>Global information</b>", //
				"At the top left of the window you can see some text lines with information about the current dataset and chart:", //
				"- the <b>chart type</b> that is currently selected,", //
				"- the dataset <b>FDR values</b> at protein, peptide and PSM level, if a <i>FDR filter</i> has applied. Note that although the FDR filter is applied independently in each level 1 node, here you will find the global FDR calculated after applying the threshold defined in the filter and aggregating all the data.", //
				"- the <b>number of PSMs and number of peptides</b> (which depends on the <i>distinguish modified peptides</i> option,", //
				"- the <b>number of protein groups</b>, that will correspond to the number of protein groups at the level 0 of the Comparison Project Tree,", //
				"- the <b>number of Human genes</b>, in case of having recognizable Human proteins (from UniProt), and in brackets, the number of Human genes just counting one per protein group.", //
				"<b>Save as curated, show table, and export (Excel and PRIDE XML) buttons</b>", //
				"Just below the information text lines, you will find the following buttons:", //
				"- <b>Show current applied filters:</b> This button will open a panel showing the filters that have been applied to the dataset. If more than one filter has been applied, it will show the order in which they were applied.", //
				"- <b>Save the datasets as curated datasets:</b> This button will open a new panel for saving the datasets as curated, which means that they will be available for adding them to a new Comparison Project Tree containing the current data after applying the filters that are currently active. Note that this option is only available if a filter has been applied.", //
				"- <b>Show the whole datasets in a table:</b> This button will open a table in which you can explore all the data in a single table. The table will show either the proteins or the peptides and all their associated information will be shown in different sortable columns.", //
				"- <b>Export datasets to an Excel file:</b> This button will open a new panel containing different options for exporting the data into a single <b>tab-separated text file</b>.", //
				"- <b>Export datasets to PRIDE XML:</b> This option will open a new panel containing the different options for exporting the data into PRIDE XML files. This will create a different PRIDE XML file per level 1 node, aggregating all the data pending from it into a single file. If the dataset was imported with an associated Mass Spectrometry metadata and peak list file, these metadata and these spectra will be incorporated into the PRIDE XML. Currently, only <i>MGF</i> and <i>mzML</i> files are compatible with this option.", //
				"- <b>Cancel current task:</b> This button will cancel any running task, such as loading the datasets, applying the filters, etc...", //
				"<b>Filters menu</b>", //
				"This menu provides the list of filters that can be applied to the datasets. See the <i>filters help</i> in the filters panel.", //
				"By selecting one of the filters, the filters panel will appear to configure the parametersof the selected filter. If the filter parameters were already set up, the filter will be automatically applied without opening the filters panel. When a filter is active, its option in this menu remains selected.", //
				"In order to deactivate a filter, either open the filters panel or just click on it on this menu.",
				"<b>General options menu</b>", //
				"By selecting this option, the <i>General options panel</i> will appear, even if you checked the option for not showing this panel again. Any change in the options of this panel will reload the entire project again." };

		return Arrays.asList(ret);
	}

	public boolean isProteinSequencesRetrieved() {
		return proteinSequencesRetrieved;
	}

	public void setProteinSequencesRetrieved(boolean b) {
		proteinSequencesRetrieved = b;
	}

	public void setFiltered(boolean b) {
		filtered = b;
	}
}