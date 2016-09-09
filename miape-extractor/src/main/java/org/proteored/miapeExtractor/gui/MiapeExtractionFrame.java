/*
 * Standard2MIAPEDialog.java Created on __DATE__, __TIME__
 */

package org.proteored.miapeExtractor.gui;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker.StateValue;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.proteored.miapeExtractor.analysis.gui.tasks.MiapeRetrieverManager;
import org.proteored.miapeExtractor.analysis.util.FileManager;
import org.proteored.miapeExtractor.gui.miapemsforms.MetadataLoader;
import org.proteored.miapeExtractor.gui.tasks.LoadProjectsTask;
import org.proteored.miapeExtractor.gui.tasks.MIAPEMSChecker;
import org.proteored.miapeExtractor.gui.tasks.MiapeExtractionTask;
import org.proteored.miapeExtractor.gui.tasks.OntologyLoaderTask;
import org.proteored.miapeExtractor.gui.tasks.OntologyLoaderWaiter;
import org.proteored.miapeExtractor.utils.HttpUtilities;
import org.proteored.miapeExtractor.utils.MiapeExtractionParametersUtil;
import org.proteored.miapeExtractor.utils.MiapeExtractionResult;
import org.proteored.miapeExtractor.utils.MiapeExtractionRunParameters;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.exceptions.MiapeDatabaseException;
import org.proteored.miapeapi.exceptions.MiapeSecurityException;
import org.proteored.miapeapi.factories.MiapeDocumentFactory;
import org.proteored.miapeapi.factories.ms.MiapeMSDocumentFactory;
import org.proteored.miapeapi.interfaces.MiapeDate;
import org.proteored.miapeapi.interfaces.ms.MiapeMSDocument;
import org.proteored.miapeapi.xml.ms.MIAPEMSXmlFile;
import org.proteored.miapeapi.xml.ms.MiapeMSDocumentImpl;
import org.proteored.miapeapi.xml.ms.MiapeMSXmlFactory;
import org.proteored.miapeapi.xml.ms.merge.MiapeMSMerger;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

/**
 *
 * @author __USER__
 */
public class MiapeExtractionFrame extends javax.swing.JFrame
		implements PropertyChangeListener, MiapeExtractionRunParameters {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("log4j.logger.org.proteored");
	private static MiapeExtractionFrame instance;
	private static final String MZIDENTML_FILE_LABEL = "mzIdentML file:";
	private static final String MZML_FILE_LABEL = "mzML file:";
	private static final String MGF_FILE_LABEL = "mgf file:";
	private static final String NOT_APPLICABLE = "not applicable";
	private static final String PRIDE_FILE_LABEL = "PRIDE xml file:";
	private static final String XTANDEM_FILE_LABEL = "X!Tandem xml file:";
	private static final String DTASELECT_FILE_LABEL = "DTASelect file:";

	@Override
	public void dispose() {
		if (miapeExtractionTask != null && miapeExtractionTask.getState() == StateValue.STARTED) {
			boolean canceled = miapeExtractionTask.cancel(true);
			while (!canceled) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {

				}
				canceled = miapeExtractionTask.cancel(true);
			}
		}
		if (miapeMSChecker != null)
			miapeMSChecker.cancel(true);
		if (mainFrame != null) {
			mainFrame.setEnabled(true);
			mainFrame.setVisible(true);
		}
		super.dispose();
	}

	private static final int ALLMODE = -1;
	private static final int PRIDEXMLMODE = 0;
	// private static final int MGFMZIDENTMLMODE = 1;
	private static final int MZIDENTMLMODE = 2;
	private static final int MZMLMODE = 3;
	private static final int MGFMODE = 4;
	private static final int DTASELECTMODE = 5;
	private static final int XTANDEMMODE = 6;
	// for mzML conversion
	public boolean isFastParsing = false;
	public boolean isShallowParsing = false;
	private MIAPEMSChecker miapeMSChecker;
	private boolean extractionStarted = false;
	private File currentPath = new File(".");
	private int currentUserID;
	private boolean listenToItemEvents;

	public static MiapeExtractionFrame getInstance(MainFrame mainFrame2, boolean b) {
		if (instance == null) {
			instance = new MiapeExtractionFrame(mainFrame2, b);
		}
		instance.mainFrame = mainFrame2;
		instance.initializeFrame();
		instance.changeRadioStatus();

		return instance;
	}

	/** Creates new form Standard2MIAPEDialog */
	private MiapeExtractionFrame(MainFrame parent, boolean modal) {

		// super(parent, modal);
		initComponents();
		try {
			UIManager.setLookAndFeel(new WindowsLookAndFeel());
		} catch (UnsupportedLookAndFeelException ex) {
		}

		if (parent != null) {
			ftpPath = MainFrame.ftpPath;
			mainFrame = parent;
			mainFrame.setEnabled(false);
			mainFrame.setVisible(false);
			currentUserID = mainFrame.userID;
			// autoscroll in the status field
			// this.mainFrame.autoScroll(jScrollPane1, jTextAreaStatus);
		} else {

		}
		changeRadioStatus();
		// Load projects in background
		loadProjects(false, storeMIAPEsInDB());

		FileManager.deleteMetadataFile(MIAPEMSChecker.CURRENT_MZML);
		FileManager.deleteMetadataFile(MIAPEMSChecker.CURRENT_PRIDEXML);

		// set icon image
		setIconImage(ImageManager.getImageIcon(ImageManager.PROTEORED_MIAPE_API).getImage());
		jButtonSubmit.setIcon(ImageManager.getImageIcon(ImageManager.ADD));
		jButtonSubmit.setPressedIcon(ImageManager.getImageIcon(ImageManager.ADD_CLICKED));
		jButtonClearStatus.setIcon(ImageManager.getImageIcon(ImageManager.CLEAR));
		jButtonClearStatus.setPressedIcon(ImageManager.getImageIcon(ImageManager.CLEAR_CLICKED));
		jButtonEditMetadata.setIcon(ImageManager.getImageIcon(ImageManager.FINISH));
		jButtonEditMetadata.setPressedIcon(ImageManager.getImageIcon(ImageManager.FINISH_CLICKED));

		// wait for the ontology loading. When done, it will notify to this
		// class and metadata combo will be able to be filled
		appendStatus("Loading ontologies...");
		OntologyLoaderWaiter waiter = new OntologyLoaderWaiter();
		waiter.addPropertyChangeListener(this);
		waiter.execute();
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.Window#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean b) {
		// check if the mainFrame user id has change. In that case, remove the
		// loaded projects
		if (currentUserID != mainFrame.userID) {
			currentUserID = mainFrame.userID;
			loadProjects(true, storeMIAPEsInDB());
		}
		if (mainFrame != null) {
			mainFrame.setVisible(!b);
		}
		super.setVisible(b);
	}

	/**
	 * Loads projects from repository in background
	 */
	private void loadProjects(boolean forceChange, boolean remoteStorage) {
		if (loadedProjects == null || loadedProjects.isEmpty() || forceChange) {
			if ((MainFrame.userName != null && MainFrame.password != null && remoteStorage) || !remoteStorage) {
				if (loadedProjects != null)
					loadedProjects.clear();
				LoadProjectsTask loadProjectsThread = new LoadProjectsTask(this, !remoteStorage, currentUserID,
						MainFrame.userName, MainFrame.password);
				loadProjectsThread.addPropertyChangeListener(this);
				loadProjectsThread.execute();
			}
		}

	}

	public void initMetadataCombo(String selectedConfigurationName, ControlVocabularyManager cvManager) {
		if (!isMzIdentMLSelected() && !isXTandemSelected() && !(isPRIDESelected() && !isMIAPEMSChecked())) {
			jButtonEditMetadata.setEnabled(true);
			jComboBoxMetadata.setEnabled(true);
			jLabelMiapeMSMetadata.setEnabled(true);
		}

		final List<String> metadataList = FileManager.getMetadataList(cvManager);
		// sort by name
		Collections.sort(metadataList);
		if (metadataList != null) {
			metadataList.add(0, "");
			jComboBoxMetadata.setModel(new DefaultComboBoxModel(metadataList.toArray()));
			if (selectedConfigurationName != null)
				jComboBoxMetadata.setSelectedItem(selectedConfigurationName);
			else
				jLabelMiapeMSMetadata.setText("");
		}
		appendStatus("Metadata templates loaded");
	}

	private void initializeFrame() {
		jTextFieldInputFile.setText("");
		jTextFieldInputFile2.setText("");
		jComboBoxMetadata.setSelectedIndex(0);
		jTextAreaStatus.setText("");
		jTextFieldProjectName.setText("");
		jProgressBar.setIndeterminate(false);
		this.setCursor(null); // turn off the wait cursor
		jButtonSubmit.setEnabled(true);
		if (miapeExtractionTask != null) {
			boolean canceled = miapeExtractionTask.cancel(true);
			log.info("Task canceled=" + canceled);
			miapeExtractionTask = null;
		}
		extractionStarted = false;

	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		buttonGroupInputFileFormat = new javax.swing.ButtonGroup();
		jFileChooser = MainFrame.fileChooser;
		buttonGroupProcessingType = new javax.swing.ButtonGroup();
		buttonGroupStoreOrNotStore = new javax.swing.ButtonGroup();
		jPanel1 = new javax.swing.JPanel();
		jTextFieldInputFile = new javax.swing.JTextField();
		jButtonInputFile = new javax.swing.JButton();
		jPanel4 = new javax.swing.JPanel();
		jRadioButtonMzIdentML = new javax.swing.JRadioButton();
		jRadioButtonMzIdentMLMGF = new javax.swing.JRadioButton();
		jRadioButtonXTandemMGF = new javax.swing.JRadioButton();
		jRadioButtonDTASelectMGF = new javax.swing.JRadioButton();
		jRadioButtonPRIDE = new javax.swing.JRadioButton();
		jRadioButtonMzML = new javax.swing.JRadioButton();
		jRadioButtonXTandem = new javax.swing.JRadioButton();
		jRadioButtonDTASelect = new javax.swing.JRadioButton();
		jRadioButtonMzMLMzIdentML = new javax.swing.JRadioButton();
		jRadioButtonMGF = new javax.swing.JRadioButton();
		jPanel6 = new javax.swing.JPanel();
		jCheckBoxMS = new javax.swing.JCheckBox();
		jCheckBoxMSI = new javax.swing.JCheckBox();
		jPanel8 = new javax.swing.JPanel();
		jRadioButtonLocalProcessing = new javax.swing.JRadioButton();
		jRadioButtonServerProcessing = new javax.swing.JRadioButton();
		jRadioButtonRemoteStorage = new javax.swing.JRadioButton();
		jRadioButtonNotRemoteStorage = new javax.swing.JRadioButton();
		jCheckBoxLocalProcessinInParallel = new javax.swing.JCheckBox();
		inputFileLabel1 = new javax.swing.JLabel();
		inputFileLabel2 = new javax.swing.JLabel();
		jTextFieldInputFile2 = new javax.swing.JTextField();
		jButtonInputFile2 = new javax.swing.JButton();
		jScrollPane2 = new javax.swing.JScrollPane();
		jPanel5 = new javax.swing.JPanel();
		jComboBoxMetadata = new javax.swing.JComboBox();
		jLabelMiapeMSMetadata = new javax.swing.JLabel();
		jButtonEditMetadata = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		jTextFieldProjectName = new javax.swing.JTextField();
		jButtonProject = new javax.swing.JButton();
		jPanel3 = new javax.swing.JPanel();
		jScrollPane1 = new javax.swing.JScrollPane();
		jTextAreaStatus = new javax.swing.JTextArea();
		jProgressBar = new javax.swing.JProgressBar();
		jButtonClearStatus = new javax.swing.JButton();
		jButtonSubmit = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Import data");
		setResizable(false);

		jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Input file(s)"));

		jButtonInputFile.setText("Select file");
		jButtonInputFile.setToolTipText("Select an input file to extract the information");
		jButtonInputFile.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonInputFileActionPerformed(evt);
			}
		});

		jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Input file(s) format"));

		buttonGroupInputFileFormat.add(jRadioButtonMzIdentML);
		jRadioButtonMzIdentML.setText("mzIdentML");
		jRadioButtonMzIdentML.setToolTipText(
				"<html>Extract MIAPE MSI information from a mzIdentML file.<br>\nIf a metadata template is used, a MIAPE MS will be also created.</html>");
		jRadioButtonMzIdentML.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jRadioButtonMzIdentMLActionPerformed(evt);
			}
		});

		buttonGroupInputFileFormat.add(jRadioButtonMzIdentMLMGF);
		jRadioButtonMzIdentMLMGF.setText("mgf + mzIdentML");
		jRadioButtonMzIdentMLMGF.setToolTipText(
				"<html>Extract MIAPE MS and MSI information from a mzIdentML file and a MGF file.<br>\nA PRIDE XML file could be created just in case of using a mgf file that has been used directly in the search.<br>\nA metadata template will be mandatory in order to complete MIAPE MS information.</html>");
		jRadioButtonMzIdentMLMGF.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jRadioButtonMzIdentMLMGFActionPerformed(evt);
			}
		});

		buttonGroupInputFileFormat.add(jRadioButtonXTandemMGF);
		jRadioButtonXTandemMGF.setText("mgf + XTandem XML");
		jRadioButtonXTandemMGF.setToolTipText(
				"<html>Extract MIAPE MS and MSI information from a XTandem XML file and a MGF file.<br>\nA PRIDE XML file could be created just in case of using a mgf file that has been used directly in the search.<br>\nA metadata template will be mandatory in order to complete MIAPE MS information.</html>");
		jRadioButtonXTandemMGF.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jRadioButtonXTandemMGFActionPerformed(evt);
			}
		});

		buttonGroupInputFileFormat.add(jRadioButtonDTASelectMGF);
		jRadioButtonDTASelectMGF.setText("mgf + DTASelect");
		jRadioButtonDTASelectMGF.setToolTipText(
				"<html>Extract MIAPE MS and MSI information from a DTASelect file and a MGF file.<br>\nA PRIDE XML file could be created just in case of using a mgf file that has been used directly in the search.<br>\nA metadata template will be mandatory in order to complete MIAPE MS information.</html>");
		jRadioButtonDTASelectMGF.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jRadioButtonDTASelectMGFActionPerformed(evt);
			}
		});

		buttonGroupInputFileFormat.add(jRadioButtonPRIDE);
		jRadioButtonPRIDE.setText("PRIDE XML");
		jRadioButtonPRIDE.setToolTipText(
				"<html>Extract MIAPE MS and/or MSI information from a PRIDE XML file.<br>\nIf a metadata template is used, its information will be added to the resulting MIAPE MS document.</html>");
		jRadioButtonPRIDE.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jRadioButtonPRIDEActionPerformed(evt);
			}
		});

		buttonGroupInputFileFormat.add(jRadioButtonMzML);
		jRadioButtonMzML.setSelected(true);
		jRadioButtonMzML.setText("mzML");
		jRadioButtonMzML.setToolTipText("Extract MIAPE MS information from a mzML file");
		jRadioButtonMzML.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jRadioButtonMzMLActionPerformed(evt);
			}
		});

		buttonGroupInputFileFormat.add(jRadioButtonXTandem);
		jRadioButtonXTandem.setText("XTandem XML");
		jRadioButtonXTandem.setToolTipText(
				"<html>Extract MIAPE MSI information from a XTandem XML result file.<br>\nIf a metadata template is used, a MIAPE MS will be also created.</html>");
		jRadioButtonXTandem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jRadioButtonXTandemActionPerformed(evt);
			}
		});

		buttonGroupInputFileFormat.add(jRadioButtonDTASelect);
		jRadioButtonDTASelect.setText("DTASelect");
		jRadioButtonDTASelect.setToolTipText(
				"<html>Extract MIAPE MSI information from a DTASelect result file.<br>\nIf a metadata template is used, a MIAPE MS will be also created.</html>");
		jRadioButtonDTASelect.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jRadioButtonDTASelectActionPerformed(evt);
			}
		});

		buttonGroupInputFileFormat.add(jRadioButtonMzMLMzIdentML);
		jRadioButtonMzMLMzIdentML.setText("mzML + mzIdentML");
		jRadioButtonMzMLMzIdentML.setToolTipText(
				"<html>Extract MIAPE MS and MSI information from a mzML file and a mzIdentML file<br>Just in case of having a mzIdentML exported from a MASCOT <br>search and the mzML file used in that search.</html>");
		jRadioButtonMzMLMzIdentML.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jRadioButtonMzMLMzIdentMLActionPerformed(evt);
			}
		});

		buttonGroupInputFileFormat.add(jRadioButtonMGF);
		jRadioButtonMGF.setText("mgf");
		jRadioButtonMGF.setToolTipText(
				"<html>Extract MIAPE MS information from a mgf file.<br>\nA metadata template will be mandatory in order to complete MIAPE MS information.</html>");
		jRadioButtonMGF.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jRadioButtonMGFActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
		jPanel4.setLayout(jPanel4Layout);
		jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel4Layout.createSequentialGroup()
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jRadioButtonMzML).addComponent(jRadioButtonMGF)
								.addComponent(jRadioButtonMzIdentMLMGF).addComponent(jRadioButtonPRIDE)
								.addComponent(jRadioButtonMzMLMzIdentML).addComponent(jRadioButtonMzIdentML)
								.addComponent(jRadioButtonXTandem).addComponent(jRadioButtonXTandemMGF)
								.addComponent(jRadioButtonDTASelect).addComponent(jRadioButtonDTASelectMGF))));
		jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel4Layout.createSequentialGroup().addComponent(jRadioButtonMzML)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jRadioButtonMGF)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jRadioButtonMzMLMzIdentML)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jRadioButtonMzIdentML)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jRadioButtonMzIdentMLMGF, javax.swing.GroupLayout.PREFERRED_SIZE, 25,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jRadioButtonPRIDE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jRadioButtonXTandem)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jRadioButtonXTandemMGF)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jRadioButtonDTASelect)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jRadioButtonDTASelectMGF).addContainerGap(53, Short.MAX_VALUE)));

		jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Output data type(s)"));
		jPanel6.setToolTipText("<html>Types of data that is going to be extracted and imported/html>");

		jCheckBoxMS.setSelected(true);
		jCheckBoxMS.setText("Mass Spectrometry data");
		jCheckBoxMS.setEnabled(false);
		jCheckBoxMS.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				jCheckBoxMSItemStateChanged(evt);
			}
		});

		jCheckBoxMSI.setText("Protein/Peptide identification data");
		jCheckBoxMSI.setEnabled(false);

		javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
		jPanel6.setLayout(jPanel6Layout);
		jPanel6Layout.setHorizontalGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jCheckBoxMS).addComponent(jCheckBoxMSI));
		jPanel6Layout.setVerticalGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel6Layout.createSequentialGroup().addComponent(jCheckBoxMS)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jCheckBoxMSI)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Type of processing"));
		jPanel8.setToolTipText("Type of processing: local or remote");

		buttonGroupProcessingType.add(jRadioButtonLocalProcessing);
		jRadioButtonLocalProcessing.setText("Local processing");
		jRadioButtonLocalProcessing
				.setToolTipText("<html><b>Local processing:</b><br>\nInput data is extracted locally.</html>");
		jRadioButtonLocalProcessing.addActionListener(new java.awt.event.ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				jRadioButtonLocalProcessingActionPerformed(evt);

			}
		});

		buttonGroupProcessingType.add(jRadioButtonServerProcessing);
		jRadioButtonServerProcessing.setSelected(true);
		jRadioButtonServerProcessing.setText("Remote processing");
		jRadioButtonServerProcessing.setToolTipText("<html><b>Not available.</b></html>");
		jRadioButtonServerProcessing.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				jRadioButtonServerProcessingActionPerformed(evt);
			}
		});

		buttonGroupStoreOrNotStore.add(jRadioButtonRemoteStorage);
		jRadioButtonRemoteStorage.setText("Store in repository");
		jRadioButtonRemoteStorage.setToolTipText("<html><b>Not available</b></html>");
		jRadioButtonRemoteStorage.setEnabled(false);
		jRadioButtonRemoteStorage.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				jRadioButtonRemoteStorageActionPerformed(evt);
			}
		});

		buttonGroupStoreOrNotStore.add(jRadioButtonNotRemoteStorage);
		jRadioButtonNotRemoteStorage.setSelected(true);
		jRadioButtonNotRemoteStorage.setText("Fully local workflow");
		jRadioButtonNotRemoteStorage.setToolTipText(
				"<html><b>Fully local workflow</b><br>\nExtracted information will be located in files under the user_data folder.</html>");
		jRadioButtonNotRemoteStorage.setEnabled(false);
		jRadioButtonNotRemoteStorage.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				jRadioButtonNotRemoteStorageActionPerformed(evt);
			}
		});

		jCheckBoxLocalProcessinInParallel.setSelected(true);
		jCheckBoxLocalProcessinInParallel.setText("multi-core processing");
		jCheckBoxLocalProcessinInParallel.setEnabled(false);

		javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
		jPanel8.setLayout(jPanel8Layout);
		jPanel8Layout.setHorizontalGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jRadioButtonLocalProcessing)
								.addGroup(jPanel8Layout.createSequentialGroup().addGap(21, 21, 21)
										.addGroup(jPanel8Layout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(jRadioButtonNotRemoteStorage)
												.addComponent(jRadioButtonRemoteStorage)
												.addComponent(jCheckBoxLocalProcessinInParallel))))
						.addGap(57, 57, 57))
				.addGroup(jPanel8Layout.createSequentialGroup().addContainerGap()
						.addComponent(jRadioButtonServerProcessing).addContainerGap(92, Short.MAX_VALUE)));
		jPanel8Layout.setVerticalGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel8Layout.createSequentialGroup().addGroup(jPanel8Layout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanel8Layout.createSequentialGroup().addComponent(jRadioButtonLocalProcessing)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jRadioButtonRemoteStorage))
						.addGroup(jPanel8Layout.createSequentialGroup().addGap(50, 50, 50)
								.addComponent(jRadioButtonNotRemoteStorage)))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxLocalProcessinInParallel)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
						.addComponent(jRadioButtonServerProcessing).addContainerGap()));

		inputFileLabel1.setText("mzML file:");

		inputFileLabel2.setText("not applicable");
		inputFileLabel2.setEnabled(false);

		jTextFieldInputFile2.setEnabled(false);

		jButtonInputFile2.setText("Select file");
		jButtonInputFile2.setToolTipText("Select a standard xml file to extract the input information");
		jButtonInputFile2.setEnabled(false);
		jButtonInputFile2.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonInputFile2ActionPerformed(evt);
			}
		});

		jScrollPane2.setBorder(null);
		jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Mass Spectrometry metadata"));
		jPanel5.setToolTipText(
				"<html>In case of using input files with MIAPE Mass Spectrometry information,<br>you can predefine some required<br> metadata to complement data from mzML or PRIDE XML.</html>");

		jComboBoxMetadata
				.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Loading metadata templates..." }));
		jComboBoxMetadata.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				jComboBoxMetadataItemStateChanged(evt);
			}
		});

		jLabelMiapeMSMetadata.setVerticalAlignment(javax.swing.SwingConstants.TOP);
		jLabelMiapeMSMetadata.setAutoscrolls(true);

		jButtonEditMetadata.setIcon(new javax.swing.ImageIcon(
				"C:\\Users\\Salva\\workspace\\miape-extractor\\src\\main\\resources\\finish.png")); // NOI18N
		jButtonEditMetadata.setText("Edit");
		jButtonEditMetadata.setToolTipText("Inspect Mass Spectrometry metadata");
		jButtonEditMetadata.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonEditMetadataActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
		jPanel5.setLayout(jPanel5Layout);
		jPanel5Layout.setHorizontalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel5Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
								.addComponent(jLabelMiapeMSMetadata, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(jComboBoxMetadata, javax.swing.GroupLayout.Alignment.LEADING, 0, 263,
										Short.MAX_VALUE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jButtonEditMetadata).addContainerGap(340, Short.MAX_VALUE)));
		jPanel5Layout.setVerticalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel5Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jButtonEditMetadata).addComponent(jComboBoxMetadata,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jLabelMiapeMSMetadata, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
						.addContainerGap()));

		jScrollPane2.setViewportView(jPanel5);

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addGroup(jPanel1Layout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanel1Layout.createSequentialGroup()
								.addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel1Layout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
										.addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE))
						.addGroup(jPanel1Layout.createSequentialGroup()
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(inputFileLabel2).addComponent(inputFileLabel1))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel1Layout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
										.addComponent(jTextFieldInputFile2).addComponent(jTextFieldInputFile,
												javax.swing.GroupLayout.DEFAULT_SIZE, 618, Short.MAX_VALUE))
								.addGap(18, 18, 18)
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(jButtonInputFile2).addComponent(jButtonInputFile))))
						.addContainerGap()));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup()
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(inputFileLabel1)
								.addComponent(jTextFieldInputFile, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jButtonInputFile))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(inputFileLabel2)
								.addComponent(jTextFieldInputFile2, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jButtonInputFile2))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
								.addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 250,
										Short.MAX_VALUE)
								.addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addGroup(javax.swing.GroupLayout.Alignment.LEADING,
										jPanel1Layout.createSequentialGroup()
												.addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
						.addContainerGap()));

		jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Project name"));
		jPanel2.setToolTipText(
				"<html>Write directly a new project name to create a new<br>\n project in which the data will be stored.</html>");

		jTextFieldProjectName.setToolTipText(
				"<html>Write directly a new project name to create a new<br>\n project in which the data will be stored.</html>");

		jButtonProject.setText("Select project");
		jButtonProject
				.setToolTipText("<html>Select one of your projects<br>or write a new name to create a new one</html>");
		jButtonProject.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonProjectActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(jTextFieldProjectName, javax.swing.GroupLayout.DEFAULT_SIZE, 667, Short.MAX_VALUE)
						.addGap(18, 18, 18).addComponent(jButtonProject).addContainerGap()));
		jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup()
						.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jButtonProject).addComponent(jTextFieldProjectName,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Status"));

		jScrollPane1.setAutoscrolls(true);

		jTextAreaStatus.setColumns(20);
		jTextAreaStatus.setEditable(false);
		jTextAreaStatus.setFont(new java.awt.Font("Dialog", 0, 10));
		jTextAreaStatus.setRows(5);
		jTextAreaStatus.setToolTipText("Task status");
		jScrollPane1.setViewportView(jTextAreaStatus);

		jButtonClearStatus.setText("Clear status");
		jButtonClearStatus.setToolTipText("Clear the status panel");
		jButtonClearStatus.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonClearStatusActionPerformed(evt);
			}
		});

		jButtonSubmit.setText("Import data");
		jButtonSubmit.setToolTipText("Start with the extraction of input data");
		jButtonSubmit.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonSubmitActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
		jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel3Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 788, Short.MAX_VALUE)
								.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 788, Short.MAX_VALUE)
								.addGroup(
										jPanel3Layout.createSequentialGroup().addComponent(jButtonClearStatus)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
														578, Short.MAX_VALUE)
												.addComponent(jButtonSubmit)))
						.addContainerGap()));
		jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel3Layout.createSequentialGroup()
						.addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jButtonClearStatus).addComponent(jButtonSubmit,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE))));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout
				.createSequentialGroup().addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
								.addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addContainerGap())
						.addGroup(layout.createSequentialGroup()
								.addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addContainerGap())
						.addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE))));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		pack();
		java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		java.awt.Dimension dialogSize = getSize();
		setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);
	}// </editor-fold>
		// GEN-END:initComponents

	private void jRadioButtonNotRemoteStorageActionPerformed(ActionEvent evt) {
		if (jRadioButtonNotRemoteStorage.isSelected())
			loadProjects(true, false);
	}

	private void jRadioButtonRemoteStorageActionPerformed(ActionEvent evt) {
		if (jRadioButtonRemoteStorage.isSelected())
			loadProjects(true, true);
	}

	private void jRadioButtonServerProcessingActionPerformed(ActionEvent evt) {

		if (jRadioButtonServerProcessing.isSelected()) {
			enableLocalRadioButtons(false);
			loadProjects(true, true);

		}
	}

	// private void
	// jButtonAddAdditionalDataActionPerformed(java.awt.event.ActionEvent evt) {
	// // select the additional information
	// // open select instrument window
	// if (this.jRadioButtonMzIdentMLMGF.isSelected())
	// this.selectInstrument();
	// }

	private void jRadioButtonLocalProcessingActionPerformed(java.awt.event.ActionEvent evt) {
		if (jRadioButtonLocalProcessing.isSelected()) {
			enableLocalRadioButtons(true);
			// showLocalProcessingWarn();
			loadProjects(true, jRadioButtonRemoteStorage.isSelected());
		}
	}

	private void jRadioButtonMGFActionPerformed(java.awt.event.ActionEvent evt) {
		// disable and select MIAPE MS
		jCheckBoxMS.setEnabled(false);
		jCheckBoxMS.setSelected(true);
		// disable and de-select MIAPE MSI
		jCheckBoxMSI.setEnabled(false);
		jCheckBoxMSI.setSelected(false);

		// enable secondary input file
		disableSecondaryInputTextFile();
		enablePrimaryInputTextFile(MGF_FILE_LABEL);

		String mgfPlusMetadataMessage = "<html>With this option, you will be able to create:<br><ul><li>a metadata input file from the mgf file plus some metadata entered in the metadata editor.</li></ul></html>";
		// show mzML + mzIdentML warning
		JOptionPane.showMessageDialog(this, mgfPlusMetadataMessage, "mgf + metadata", JOptionPane.INFORMATION_MESSAGE);

	}

	private void jCheckBoxMSItemStateChanged(java.awt.event.ItemEvent evt) {
		if (!jCheckBoxMS.isSelected()) {
			jComboBoxMetadata.setSelectedIndex(0);
		}
	}

	private void jButtonEditMetadataActionPerformed(java.awt.event.ActionEvent evt) {
		if (miapeMSChecker == null || !miapeMSChecker.getState().equals(StateValue.STARTED)) {
			if (miapeMSChecker != null)
				miapeMSChecker.cancel(true);

			boolean extractFromStandardFile;
			if ("".equals(jComboBoxMetadata.getSelectedItem()))
				extractFromStandardFile = true;
			else
				extractFromStandardFile = false;
			miapeMSChecker = new MIAPEMSChecker(this, extractFromStandardFile);
			miapeMSChecker.addPropertyChangeListener(this);
			miapeMSChecker.execute();
			appendStatus("Opening metadata editor...");

		} else
			appendStatus("MIAPE MS metadata is currently being checked. Try again later");
	}

	private void jComboBoxMetadataItemStateChanged(java.awt.event.ItemEvent evt) {
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			String metadataFileName = (String) jComboBoxMetadata.getSelectedItem();
			MetadataLoader metadataLoader = new MetadataLoader(metadataFileName);
			metadataLoader.addPropertyChangeListener(this);
			metadataLoader.execute();

			if (metadataFileName != null && !"".equals(metadataFileName)) {
				jCheckBoxMS.setSelected(true);
			} else {
				jCheckBoxMS.setSelected(false);
			}
		}
	}

	// private void
	// jButtonAddAdditionalDataActionPerformed(java.awt.event.ActionEvent evt) {
	// // select the additional information
	// // open select instrument window
	// if (this.jRadioButtonMzIdentMLMGF.isSelected())
	// this.selectInstrument();
	// }

	private void enableLocalRadioButtons(boolean b) {
		jRadioButtonRemoteStorage.setEnabled(b);
		jRadioButtonNotRemoteStorage.setEnabled(b);
		jCheckBoxLocalProcessinInParallel.setEnabled(b);

	}

	private void changeRadioStatus() {
		if (mainFrame != null) {
			listenToItemEvents = false;
			if (MainFrame.userName != null && !"".equals(MainFrame.userName) && MainFrame.password != null
					&& !"".equals(MainFrame.password)) {
				// enable user interactive options
				jRadioButtonLocalProcessing.setEnabled(true);
				jRadioButtonServerProcessing.setEnabled(true);
				jRadioButtonNotRemoteStorage.setEnabled(false);
				jRadioButtonRemoteStorage.setEnabled(false);
				// select store in repository and remote processing
				jRadioButtonRemoteStorage.setSelected(true);
				jRadioButtonServerProcessing.setSelected(true);

			} else {
				// disable user interactive options
				jRadioButtonLocalProcessing.setEnabled(false);
				jRadioButtonNotRemoteStorage.setEnabled(false);
				jRadioButtonRemoteStorage.setEnabled(false);
				jRadioButtonRemoteStorage.setEnabled(false);
				jRadioButtonServerProcessing.setEnabled(false);
				// enable multicore processing option
				jCheckBoxLocalProcessinInParallel.setEnabled(true);

				// select local processing, not remote storage
				jRadioButtonLocalProcessing.setSelected(true);
				jRadioButtonNotRemoteStorage.setSelected(true);
				jRadioButtonRemoteStorage.setSelected(false);
				jRadioButtonServerProcessing.setSelected(false);
			}
			listenToItemEvents = true;
		}

	}

	// private void showLocalProcessingWarn() {
	// if (!MainFrame.localWorkflow) {
	// String localProcessingMessage = "<html>If you select local processing,
	// you have two options:<br>"
	// + "<ul><li><b>Store in repository</b>: all the MIAPE information will be
	// extracted locally from the input files<br>"
	// + "and the extracted information will be sent to the ProteoRed MIAPE
	// repository.</li>"
	// + "<li><b>Fully local worflow</b>: all the MIAPE information will be
	// extracted locally from the input files<br>"
	// + "and the extracted information will stored in local files.<br>"
	// + "No interaction with ProteoRed MIAPE repository will be
	// performed.</li></ul></html>";
	// String title = "Local processing vs Remote processing";
	//
	// JOptionPane.showMessageDialog(this, localProcessingMessage, title,
	// JOptionPane.INFORMATION_MESSAGE);
	// }
	// }

	private void jRadioButtonMzMLMzIdentMLActionPerformed(java.awt.event.ActionEvent evt) {
		// disable and select MIAPE MS
		jCheckBoxMS.setEnabled(false);
		jCheckBoxMS.setSelected(true);
		// disable and select MIAPE MSI
		jCheckBoxMSI.setEnabled(false);
		jCheckBoxMSI.setSelected(true);

		enablePrimaryInputTextFile(MZML_FILE_LABEL);
		enableSecondaryInputTextFile(MZIDENTML_FILE_LABEL);

		// set mzIdentML file to the secondary input file label
		String mzMLPlusmzIdentMLMessage = "<html>With this option, you will be able to create:<br><ul><li>a MIAPE MS document from the mzML file, and</li> <li>a MIAPE MSI document from the mzIdentML file</li></ul>"
				+ "If the mzIdentML file comes from a MASCOT search, later you will be able to<br>create a complete PRIDE XML file from both MIAPE MS and MSI documents<br>(option 'export MIAPE to PRIDE XML')</html>";
		// show mzML + mzIdentML warnning
		JOptionPane.showMessageDialog(this, mzMLPlusmzIdentMLMessage, "mzML + mzIdentML",
				JOptionPane.INFORMATION_MESSAGE);
		// show local processing warning
		// if (jRadioButtonLocalProcessing.isSelected())
		// showLocalProcessingWarn();
	}

	private void jButtonInputFile2ActionPerformed(java.awt.event.ActionEvent evt) {

		int mode = ALLMODE;
		if (jRadioButtonMzIdentML.isSelected() || jRadioButtonMzIdentMLMGF.isSelected()
				|| jRadioButtonMzMLMzIdentML.isSelected()) {
			mode = MZIDENTMLMODE;
		} else if (jRadioButtonXTandem.isSelected() || jRadioButtonXTandemMGF.isSelected()) {
			mode = XTANDEMMODE;
		} else if (jRadioButtonDTASelect.isSelected() || jRadioButtonDTASelectMGF.isSelected()) {
			mode = DTASELECTMODE;
		}
		// select the file
		String selectedFile = selectFile(mode);
		if (selectedFile.compareTo("null") == 0)
			log.info("ERROR: I/O");
		else {
			jTextFieldInputFile2.setText(selectedFile);
		}

	}

	private void jRadioButtonXTandemActionPerformed(java.awt.event.ActionEvent evt) {

		// the same as for mzIdentML
		jRadioButtonMzIdentMLActionPerformed(evt);

		disablePrimaryInputTextFile();
		enableSecondaryInputTextFile(XTANDEM_FILE_LABEL);

		// reset combo box, deleting current mzml if exists
		FileManager.deleteMetadataFile(MIAPEMSChecker.CURRENT_MZML);

	}

	private void jRadioButtonDTASelectActionPerformed(java.awt.event.ActionEvent evt) {

		// the same as for mzIdentML
		jRadioButtonMzIdentMLActionPerformed(evt);

		disablePrimaryInputTextFile();
		enableSecondaryInputTextFile(DTASELECT_FILE_LABEL);

		// reset combo box, deleting current mzml if exists
		FileManager.deleteMetadataFile(MIAPEMSChecker.CURRENT_MZML);

	}

	private void jRadioButtonMzMLActionPerformed(java.awt.event.ActionEvent evt) {
		// disable and select MIAPE MS
		jCheckBoxMS.setEnabled(false);
		jCheckBoxMS.setSelected(true);
		// disable and not select MIAPE MSI
		jCheckBoxMSI.setEnabled(false);
		jCheckBoxMSI.setSelected(false);

		enablePrimaryInputTextFile(MZML_FILE_LABEL);
		disableSecondaryInputTextFile();
	}

	private void jButtonClearStatusActionPerformed(java.awt.event.ActionEvent evt) {
		jTextAreaStatus.setText("");
	}

	public MainFrame getMainFrame() {
		return mainFrame;
	}

	private void jRadioButtonMzIdentMLActionPerformed(java.awt.event.ActionEvent evt) {
		// disable and not select MIAPE MS
		jCheckBoxMS.setEnabled(false);
		jCheckBoxMS.setSelected(false);
		// disable and select MIAPE MSI
		jCheckBoxMSI.setEnabled(false);
		jCheckBoxMSI.setSelected(true);

		// disable secondary input file
		disablePrimaryInputTextFile();
		enableSecondaryInputTextFile(MZIDENTML_FILE_LABEL);

		// this.jLabelMiapeMSMetadata.setText("");
		// reset combo box, deleting current mzml if exists
		FileManager.deleteMetadataFile(MIAPEMSChecker.CURRENT_MZML);

	}

	private void jRadioButtonPRIDEActionPerformed(java.awt.event.ActionEvent evt) {
		// disable additional data labels
		// enable and check MIAPE MS checkbox
		jCheckBoxMS.setSelected(true);
		jCheckBoxMS.setEnabled(true);
		// enable and check MIAPE MSI checkbox
		jCheckBoxMSI.setEnabled(true);
		jCheckBoxMSI.setSelected(true);

		enablePrimaryInputTextFile(PRIDE_FILE_LABEL);
		disableSecondaryInputTextFile();

		// show check boxes tooltip
		showCheckBoxesTooltip();

		// reset combo box, deleting current mzml if exists
		FileManager.deleteMetadataFile(MIAPEMSChecker.CURRENT_MZML);

		String PRIDEMessage = "<html>With this option, you can choose:<ul><li>to create a MIAPE MS document,</li><li>to create a MIAPE MSI document, or</li><li>to create both MIAPE MS and MSI documents</li></ul></html>";
		// show mzML + mzIdentML warnning
		JOptionPane.showMessageDialog(this, PRIDEMessage, "PRIDE", JOptionPane.INFORMATION_MESSAGE);
	}

	private void jRadioButtonMzIdentMLMGFActionPerformed(java.awt.event.ActionEvent evt) {
		// disable and select MIAPE MS
		jCheckBoxMS.setEnabled(false);
		jCheckBoxMS.setSelected(true);
		// disable and select MIAPE MSI
		jCheckBoxMSI.setEnabled(false);
		jCheckBoxMSI.setSelected(true);

		enablePrimaryInputTextFile(MGF_FILE_LABEL);
		enableSecondaryInputTextFile(MZIDENTML_FILE_LABEL);

		String mgfPlusmzIdentMLMessage = "<html>With this option, you will be able to create <ul><li>a MIAPE MS document from the mgf file. "
				+ "(Some minimal information about the spectrometer<br>will be asked to you before to start the process).</li>"
				+ "<li>a MIAPE MSI document from the mzIdentML file.</li></ul>"
				+ "If the mzIdentML file comes from a MASCOT search that comes from the mgf file, "
				+ "later you will be able to create<br>a complete PRIDE XML file from both MIAPE MS and MSI documents (option MIAPE to Standard)</html>";
		// show mzML + mzIdentML warning
		JOptionPane.showMessageDialog(this, mgfPlusmzIdentMLMessage, "mgf + mzIdentML",
				JOptionPane.INFORMATION_MESSAGE);

		// show local processing warning
		// if (jRadioButtonLocalProcessing.isSelected())
		// showLocalProcessingWarn();

	}

	private void jRadioButtonXTandemMGFActionPerformed(java.awt.event.ActionEvent evt) {
		// disable and select MIAPE MS
		jCheckBoxMS.setEnabled(false);
		jCheckBoxMS.setSelected(true);
		// disable and select MIAPE MSI
		jCheckBoxMSI.setEnabled(false);
		jCheckBoxMSI.setSelected(true);

		enablePrimaryInputTextFile(MGF_FILE_LABEL);
		enableSecondaryInputTextFile(XTANDEM_FILE_LABEL);

		String mgfPlusXTandemMessage = "<html>With this option, you will be able to create <ul><li>a MIAPE MS document from the mgf file. "
				+ "(Some minimal information about the spectrometer<br>will be asked to you before to start the process).</li>"
				+ "<li>a MIAPE MSI document from the XTandem XML file.</li></ul>"
				+ "If the XTandem XML file comes from a search using the mgf file, "
				+ "later you will be able to create<br>a complete PRIDE XML file from both MIAPE MS and MSI documents (option MIAPE to Standard)</html>";
		// show mzML + mzIdentML warning
		JOptionPane.showMessageDialog(this, mgfPlusXTandemMessage, "mgf + XTandem XML",
				JOptionPane.INFORMATION_MESSAGE);

		// show local processing warning
		// if (jRadioButtonLocalProcessing.isSelected())
		// showLocalProcessingWarn();

	}

	private void jRadioButtonDTASelectMGFActionPerformed(java.awt.event.ActionEvent evt) {
		// disable and select MIAPE MS
		jCheckBoxMS.setEnabled(false);
		jCheckBoxMS.setSelected(true);
		// disable and select MIAPE MSI
		jCheckBoxMSI.setEnabled(false);
		jCheckBoxMSI.setSelected(true);

		enablePrimaryInputTextFile(MGF_FILE_LABEL);
		enableSecondaryInputTextFile(DTASELECT_FILE_LABEL);

		String mgfPlusDTASelectMessage = "<html>With this option, you will be able to create <ul><li>a MIAPE MS document from the mgf file. "
				+ "(Some minimal information about the spectrometer<br>will be asked to you before to start the process).</li>"
				+ "<li>a MIAPE MSI document from the DTASelect file.</li></ul>"
				+ "If the DTASelect file comes from a search using the mgf file, "
				+ "later you will be able to create<br>a complete PRIDE XML file from both MIAPE MS and MSI documents (option MIAPE to Standard)</html>";
		// show mzML + mzIdentML warning
		JOptionPane.showMessageDialog(this, mgfPlusDTASelectMessage, "mgf + DTASelect",
				JOptionPane.INFORMATION_MESSAGE);

		// show local processing warning
		// if (jRadioButtonLocalProcessing.isSelected())
		// showLocalProcessingWarn();

	}

	private void showCheckBoxesTooltip() {
		ToolTipManager.sharedInstance().mouseMoved(new MouseEvent(jPanel6, 0, 0, 0, 5, 45, 0, false));
	}

	private void enableSecondaryInputTextFile(String textSecondaryLabel) {
		jTextFieldInputFile2.setEnabled(true);
		inputFileLabel2.setText(textSecondaryLabel);
		inputFileLabel2.setEnabled(true);
		jButtonInputFile2.setEnabled(true);
	}

	private void enablePrimaryInputTextFile(String textPrimaryLabel) {
		jTextFieldInputFile.setEnabled(true);
		inputFileLabel1.setText(textPrimaryLabel);
		inputFileLabel1.setEnabled(true);
		jButtonInputFile.setEnabled(true);
	}

	private void disableSecondaryInputTextFile() {
		jTextFieldInputFile2.setEnabled(false);
		inputFileLabel2.setEnabled(false);
		jButtonInputFile2.setEnabled(false);
		inputFileLabel2.setText(NOT_APPLICABLE);
	}

	private void disablePrimaryInputTextFile() {
		jTextFieldInputFile.setEnabled(false);
		inputFileLabel1.setEnabled(false);
		jButtonInputFile.setEnabled(false);
		inputFileLabel1.setText(NOT_APPLICABLE);
	}

	// public void selectInstrument() {
	// InstrumentSummary[] data;
	// data = readInstrumentsData();
	// additionalDataForm = new TFrmInputTable(this, true,
	// TFrmInputTable.INTRUMENT_MODE, data);
	// additionalDataForm.setVisible(true);
	// }

	private String selectFile(int _mode) {
		String filename = "";
		jFileChooser = new JFileChooser(currentPath);
		switch (_mode) {
		case ALLMODE:
			jFileChooser.setDialogTitle("Select a file");
			break;
		case PRIDEXMLMODE:
			jFileChooser.setDialogTitle("Select a PRIDE XML file");

			jFileChooser.setFileFilter(new TFileExtension("PRIDE XML files", new String[] { "XML", "xml" }));

			break;
		// case MGFMZIDENTMLMODE:
		// this.jFileChooser.setDialogTitle("Select a mgf file");
		// this.jFileChooser.setFileFilter(new TFileExtension(
		// "Mascot Generic files", new String[] { "MGF", "mgf" }));
		// break;
		case MGFMODE:
			jFileChooser.setDialogTitle("Select a mgf file");
			jFileChooser.setFileFilter(new TFileExtension("Mascot Generic files", new String[] { "MGF", "mgf" }));
			break;
		case MZIDENTMLMODE:
			jFileChooser.setDialogTitle("Select a mzIdentML file");
			jFileChooser
					.setFileFilter(new TFileExtension("mzIdentML files", new String[] { "mzid", "xml", "mzidentml" }));
			break;
		case MZMLMODE:
			jFileChooser.setDialogTitle("Select a mzML file");
			jFileChooser.setFileFilter(new TFileExtension("mzML files", new String[] { "mzml", "xml" }));
			break;
		case DTASELECTMODE:
			jFileChooser.setDialogTitle("Select a DTASelect file");
			jFileChooser.setFileFilter(new TFileExtension("DTASelect-filter files", new String[] { "txt" }));
			break;
		case XTANDEMMODE:
			jFileChooser.setDialogTitle("Select a XTandem output");
			jFileChooser.setFileFilter(new TFileExtension("XTandem output files", new String[] { "xml" }));
			break;
		}
		// fileChooser.setDialogTitle("Select a PRIDE file");
		// fileChooser.setFileFilter(new TFileExtension("XML files", new
		// String[] { "XML", "xml" }));
		// fileChooser.setCurrentDirectory(openfileDirectrory);
		jFileChooser.showOpenDialog(this);
		File selectedFile = jFileChooser.getSelectedFile();
		if (selectedFile != null) {
			filename = selectedFile.toString();
			if (selectedFile.isDirectory())
				currentPath = selectedFile;
			else
				currentPath = selectedFile.getParentFile();

			log.info("Selected File: " + filename);
		} else
			filename = "null";
		return (filename);
	}

	private void jButtonSubmitActionPerformed(java.awt.event.ActionEvent evt) {
		if (evt.getSource() instanceof JButton) {
			if (!((JButton) evt.getSource()).isEnabled())
				return;
		}
		// clear status
		jTextAreaStatus.setText("");

		// in case of MIAPE MS generation conversion, check if MIAPE MS is
		// complete or not and then, show MIAPE MS Metadata forms
		if (jCheckBoxMS.isSelected()) {
			String selectedItem = (String) jComboBoxMetadata.getSelectedItem();
			if ("".equals(selectedItem)) {
				// do not let continue if mgf + mzIdentml or mgf + XTandem
				// options are selected. Other options, show a warning:
				if (isMzIdentMLPlusMGFSelected() || isXTandemPlusMGFSelected() || isDTASelectPlusMGFSelected()) {
					final int option = JOptionPane.showConfirmDialog(this,
							"<html>MGF input file doesn't contain any metadata. <br>"
									+ "You must select one preconfigured metadata information in the dropdown list or introduce the information yourself.<br><br>"
									+ "Do you want to go to complete metadata, click on YES.</html>",
							"No additional metadata has been selected", JOptionPane.YES_NO_CANCEL_OPTION);
					if (option == JOptionPane.YES_OPTION) {
						// open MIAPEMSForms
						miapeMSChecker = new MIAPEMSChecker(this, true);
						miapeMSChecker.addPropertyChangeListener(this);
						miapeMSChecker.execute();
					} else {
						return;
					}
				} else {
					final int option = JOptionPane.showConfirmDialog(this,
							"<html>Metadata requested by MIAPE MS guidelines is not usually present in the<br>"
									+ "input files (spectrometer details, data processing, etc...).<br><br>"
									+ "You can select one preconfigured metadata information in the dropdown list.<br><br>"
									+ "Do you want to continue without complete metadata? (YES)<br>"
									+ "If you want to go to complete metadata, click on NO.</html>",
							"No additional metadata has been selected", JOptionPane.YES_NO_CANCEL_OPTION);
					if (option == JOptionPane.NO_OPTION) {
						// open MIAPEMSForms
						miapeMSChecker = new MIAPEMSChecker(this, true);
						miapeMSChecker.addPropertyChangeListener(this);
						miapeMSChecker.execute();
					} else if (option == JOptionPane.CANCEL_OPTION) {
						return;
					} else {
						startExtraction();
						return;
					}
				}
			} else {
				String metadataString = MetadataLoader.getMetadataString(selectedItem);
				final int option = JOptionPane
						.showConfirmDialog(this,
								"<html>You have selected the following metadata to add to the MIAPE MS document.<br>Are you sure you want to continue?:<br><br>"
										+ metadataString + "</html>",
								"Metadata confirmation", JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION) {
					startExtraction();
					return;
				}
			}

		} else {
			startExtraction();
			return;
		}
		log.info("No miape extraction process started");
	}

	private synchronized void startExtraction() {

		// in case of mzML conversion, show the parsing mode dialog:
		if (jRadioButtonMzML.isSelected()) {
			ParsingModeDialog parsingModeDialog = new ParsingModeDialog(this, true);
			parsingModeDialog.setVisible(true);
			if (!isFastParsing && !isShallowParsing)
				return;
		}
		if (miapeExtractionTask != null && miapeExtractionTask.getState().equals(StateValue.STARTED)) {

			appendStatus("A request has been already sent to the server. Please wait...");
			return;
		}

		if (!extractionStarted) {
			extractionStarted = true;
			miapeExtractionTask = new MiapeExtractionTask(this, MainFrame.getMiapeExtractorWebservice(),
					MainFrame.getMiapeAPIWebservice(), MainFrame.userName, MainFrame.password,
					isLocalProcessingInParallel());
			miapeExtractionTask.addPropertyChangeListener(this);
			miapeExtractionTask.execute();
		} else {
			log.info("There is already a task for miape extraction");
		}

	}

	private boolean isLocalProcessingInParallel() {
		return jCheckBoxLocalProcessinInParallel.isSelected();
	}

	private void jButtonProjectActionPerformed(java.awt.event.ActionEvent evt) {
		appendStatus("Opening project table");
		loadProjects(true, false);
		Map<Integer, String> miapeProjects = getLoadedProjects();
		// this.appendStatus(miapeProjects.size() + " projects retrieved\n");
		if (miapeProjects != null && !miapeProjects.isEmpty()) {
			additionalDataForm = new TFrmInputTable(this, true, TFrmInputTable.PROJECT_MODE, miapeProjects);
			additionalDataForm.setVisible(true);
		} else {
			appendStatus("There is no projects to show");
		}
	}

	private void jButtonInputFileActionPerformed(java.awt.event.ActionEvent evt) {

		int mode = ALLMODE;
		if (jRadioButtonMzIdentML.isSelected()) {
			mode = MZIDENTMLMODE;
		} else if (jRadioButtonMzIdentMLMGF.isSelected() || jRadioButtonDTASelectMGF.isSelected()
				|| jRadioButtonXTandemMGF.isSelected()) {
			mode = MGFMODE;
		} else if (jRadioButtonPRIDE.isSelected()) {
			mode = PRIDEXMLMODE;
		} else if (jRadioButtonMzML.isSelected() || jRadioButtonMzMLMzIdentML.isSelected()) {
			mode = MZMLMODE;
		} else if (jRadioButtonMGF.isSelected()) {
			mode = MGFMODE;
		}
		// select the file
		String selectedFile = selectFile(mode);
		if ("".equals(selectedFile) || selectedFile.compareTo("null") == 0) {
			log.info("ERROR: MGF I/O");
			return;
		}
		jTextFieldInputFile.setText(selectedFile);

		if (mode == MZMLMODE || (mode == PRIDEXMLMODE && isMIAPEMSChecked())) {
			// Starting metadata extraction
			jLabelMiapeMSMetadata.setText("");
			MIAPEMSChecker checker = new MIAPEMSChecker(this, true);
			checker.addPropertyChangeListener(this);
			checker.setSave(true);
			checker.execute();

		}
	}

	// GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.ButtonGroup buttonGroupInputFileFormat;
	private javax.swing.ButtonGroup buttonGroupProcessingType;
	private javax.swing.ButtonGroup buttonGroupStoreOrNotStore;
	private javax.swing.JLabel inputFileLabel1;
	private javax.swing.JLabel inputFileLabel2;
	private javax.swing.JButton jButtonClearStatus;
	private javax.swing.JButton jButtonEditMetadata;
	public javax.swing.JButton jButtonInputFile;
	public javax.swing.JButton jButtonInputFile2;
	public javax.swing.JButton jButtonProject;
	public javax.swing.JButton jButtonSubmit;
	private javax.swing.JCheckBox jCheckBoxLocalProcessinInParallel;
	public javax.swing.JCheckBox jCheckBoxMS;
	public javax.swing.JCheckBox jCheckBoxMSI;
	public javax.swing.JComboBox jComboBoxMetadata;
	private javax.swing.JFileChooser jFileChooser;
	private javax.swing.JLabel jLabelMiapeMSMetadata;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JPanel jPanel8;
	public javax.swing.JProgressBar jProgressBar;
	private javax.swing.JRadioButton jRadioButtonLocalProcessing;
	public javax.swing.JRadioButton jRadioButtonMGF;
	public javax.swing.JRadioButton jRadioButtonMzIdentML;
	public javax.swing.JRadioButton jRadioButtonMzIdentMLMGF;
	public javax.swing.JRadioButton jRadioButtonXTandemMGF;
	private javax.swing.JRadioButton jRadioButtonDTASelectMGF;
	public javax.swing.JRadioButton jRadioButtonMzML;
	public javax.swing.JRadioButton jRadioButtonMzMLMzIdentML;
	private javax.swing.JRadioButton jRadioButtonNotRemoteStorage;
	public javax.swing.JRadioButton jRadioButtonPRIDE;
	private javax.swing.JRadioButton jRadioButtonRemoteStorage;
	private javax.swing.JRadioButton jRadioButtonServerProcessing;
	private javax.swing.JRadioButton jRadioButtonXTandem;
	private javax.swing.JRadioButton jRadioButtonDTASelect;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane2;
	public javax.swing.JTextArea jTextAreaStatus;
	private javax.swing.JTextField jTextFieldInputFile;
	private javax.swing.JTextField jTextFieldInputFile2;
	private javax.swing.JTextField jTextFieldProjectName;
	// End of variables declaration//GEN-END:variables

	private TFrmInputTable additionalDataForm;

	private MiapeExtractionTask miapeExtractionTask;
	private MainFrame mainFrame = null;
	public String ftpPath = null;
	public String id_msi;
	public String id_ms;
	private boolean isLoadingProjects; // indicate if the thread
										// LoadProjectsThread is already loading
										// or not
	private final Map<Integer, String> loadedProjects = new HashMap<Integer, String>();

	// public int selectedInstrumentNumber;

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress".equals(evt.getPropertyName())) {
			int progress = (Integer) evt.getNewValue();
			jProgressBar.setValue(progress);
		} else if (MiapeExtractionTask.NOTIFICATION.equals(evt.getPropertyName())) {
			String notificacion = evt.getNewValue().toString();
			appendStatus(notificacion);
		} else if (MiapeExtractionTask.MIAPE_CREATION_ERROR.equals(evt.getPropertyName())) {
			if (evt.getNewValue() != null) {
				MiapeExtractionResult errorMessage = (MiapeExtractionResult) evt.getNewValue();
				appendStatus(errorMessage.getErrorMessage());
			}
			jProgressBar.setIndeterminate(false);
			jButtonSubmit.setEnabled(true);
			this.setCursor(null); // turn off the wait cursor
			extractionStarted = false;
			appendStatus("Process finished.");
		} else if (MIAPEMSChecker.MIAPE_MS_CHECKING_IN_PROGRESS.equals(evt.getPropertyName())) {
			appendStatus("Extracting MIAPE MS metadata from file...");
			jProgressBar.setIndeterminate(true);
		} else if (MIAPEMSChecker.MIAPE_MS_CHECKING_ERROR.equals(evt.getPropertyName())) {
			String error = evt.getNewValue().toString();
			appendStatus(error);
			jProgressBar.setIndeterminate(false);
		} else if (MIAPEMSChecker.MIAPE_MS_CHECKING_DONE.equals(evt.getPropertyName())) {
			appendStatus(
					"MIAPE MS metadata edition completed. Click again on \"Create MIAPE(s)\" to extract MIAPE information.");
			jProgressBar.setIndeterminate(false);
		} else if (MetadataLoader.METADATA_READED.equals(evt.getPropertyName())) {
			String string = (String) evt.getNewValue();
			jLabelMiapeMSMetadata.setText(string);
		} else if (MiapeExtractionTask.MIAPE_MSI_CREATED_DONE.equals(evt.getPropertyName())) {
			extractionStarted = false;
			String miapeIDString = (String) evt.getNewValue();
			log.info("Miape MSI created done finished: " + miapeIDString);
			if (miapeIDString != null && !miapeExtractionTask.isLocalMIAPEExtraction()) {
				log.info("Starting retrieving of the MIAPE MSI created some moments ago");
				int miapeID = Integer.valueOf(miapeIDString);
				MiapeRetrieverManager.getInstance(MainFrame.userName, MainFrame.password)
						.addRetrievingWithPriority(miapeID, "MSI", null);
			}
			FileManager.deleteMetadataFile(MIAPEMSChecker.CURRENT_MZML);
			initMetadataCombo(null, getControlVocabularyManager());
			// load new projects
			loadProjects(false, storeMIAPEsInDB());
		} else if (MiapeExtractionTask.MIAPE_MS_CREATED_DONE.equals(evt.getPropertyName())) {
			extractionStarted = false;
			String miapeIDString = (String) evt.getNewValue();
			log.info("Miape MS created done finished: " + miapeIDString);
			if (miapeIDString != null) {
				log.info("Starting retrieving of the MIAPE MS created some moments ago");
				int miapeID = Integer.valueOf(miapeIDString);
				MiapeRetrieverManager.getInstance(MainFrame.userName, MainFrame.password)
						.addRetrievingWithPriority(miapeID, "MS", null);
			}
			// load new projects
			loadProjects(false, storeMIAPEsInDB());
		} else if (OntologyLoaderWaiter.ONTOLOGY_LOADED.equals(evt.getPropertyName())) {
			ControlVocabularyManager cvManager = (ControlVocabularyManager) evt.getNewValue();
			appendStatus("Ontologies loaded");
			initMetadataCombo(null, cvManager);

		} else if (MIAPEMSChecker.MIAPE_MS_METADATA_EXTRACTION_DONE.equals(evt.getPropertyName())) {
			appendStatus("Metadata loaded.");
			if (isMzMLPlusMzIdentMLSelected() || isMzMLSelected())
				initMetadataCombo(MIAPEMSChecker.CURRENT_MZML, getControlVocabularyManager());
			else if (isPRIDESelected() && isMIAPEMSChecked())
				initMetadataCombo(MIAPEMSChecker.CURRENT_PRIDEXML, getControlVocabularyManager());
			jProgressBar.setIndeterminate(false);
		} else if (MiapeExtractionTask.MIAPE_CREATION_TOTAL_DONE.equals(evt.getPropertyName())) {
			jProgressBar.setIndeterminate(false);
			MiapeExtractionResult extractionResult = (MiapeExtractionResult) evt.getNewValue();
			showOpenBrowserDialog(extractionResult.getDirectLinkToMIAPEMS(), extractionResult.getDirectLinkToMIAPEMSI(),
					extractionResult.getDirectLinkText());

			jProgressBar.setIndeterminate(false);
			jButtonSubmit.setEnabled(true);
			this.setCursor(null); // turn off the wait cursor
			extractionStarted = false;
			appendStatus("Process finished.");

		} else if (MiapeExtractionTask.MIAPE_CREATION_STARTS.equals(evt.getPropertyName())) {
			jButtonSubmit.setEnabled(false);
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			jProgressBar.setIndeterminate(true);
			appendStatus("Starting process...");

		}
	}

	@Override
	public MiapeMSDocument getMiapeMSMetadata() {

		final String miapeFileName = (String) jComboBoxMetadata.getSelectedItem();
		if ("".equals(miapeFileName))
			return null;
		final File metadataFile = FileManager.getMetadataFile(miapeFileName);
		if (metadataFile == null)
			return null;
		MIAPEMSXmlFile xmlFile = new MIAPEMSXmlFile(metadataFile);

		try {
			MiapeMSDocument metadataMiapeMS = MiapeMSXmlFactory.getFactory().toDocument(xmlFile,
					getControlVocabularyManager(), null, null, null);
			MiapeExtractionParametersUtil.setNameToMetadataMiapeMS((MiapeMSDocumentImpl) metadataMiapeMS, this);

			String miapeMSName = metadataMiapeMS.getName();
			// merge with a MIAPE with just a project

			MiapeDate today = new MiapeDate(new Date());
			MiapeMSDocument miapeMSJustWithProject = (MiapeMSDocument) MiapeMSDocumentFactory
					.createMiapeMSDocumentBuilder(
							MiapeDocumentFactory.createProjectBuilder(jTextFieldProjectName.getText())
									.date(new MiapeDate(new Date())).build(),
							miapeMSName, null)
					.date(today).modificationDate(new Date()).build();
			MiapeMSDocument ret = MiapeMSMerger.getInstance(getControlVocabularyManager()).merge(metadataMiapeMS,
					miapeMSJustWithProject);

			return ret;
		} catch (MiapeDatabaseException e) {
			e.printStackTrace();
		} catch (MiapeSecurityException e) {
			e.printStackTrace();
		}

		return null;
	}

	protected ControlVocabularyManager getControlVocabularyManager() {
		return OntologyLoaderTask.getCvManager();
	}

	// public void fillInstrumentUserSelection(String userSelection, int i) {
	// this.jLabelInstrument.setText(userSelection);
	// this.selectedInstrumentNumber = i + 1;
	// }

	public void fillProjectUserSelection(String userSelection) {
		jTextFieldProjectName.setText(userSelection);
	}

	@Override
	public String getProjectName() {
		return jTextFieldProjectName.getText();
	}

	public void setProjectName(String name) {
		jTextFieldProjectName.setText(name);
	}

	public String getPrimaryInputFileName() {
		return jTextFieldInputFile.getText();
	}

	public String getSecondaryInputFileName() {
		return jTextFieldInputFile2.getText();
	}

	@Override
	public String getMzMLFileName() {
		if (isMzMLSelected() || isMzMLPlusMzIdentMLSelected())
			return jTextFieldInputFile.getText();
		return null;
	}

	@Override
	public String getMzIdentMLFileName() {
		if (isMzIdentMLSelected() || isMzIdentMLPlusMGFSelected() || isMzMLPlusMzIdentMLSelected())
			return jTextFieldInputFile2.getText();
		return null;
	}

	@Override
	public String getPRIDEXMLFileName() {
		if (isPRIDESelected())
			return jTextFieldInputFile.getText();
		return null;
	}

	@Override
	public String getMgfFileName() {
		if (isMzIdentMLPlusMGFSelected() || isMGFSelected() || isXTandemPlusMGFSelected())
			return jTextFieldInputFile.getText();
		return null;
	}

	@Override
	public String getXTandemFileName() {
		if (isXTandemSelected())
			return jTextFieldInputFile2.getText();
		return null;
	}

	// public String getInstrument() {
	// return this.jLabelInstrument.getText();
	// }

	@Override
	public boolean isMzMLSelected() {
		return jRadioButtonMzML.isSelected();
	}

	@Override
	public boolean isMzIdentMLSelected() {
		return jRadioButtonMzIdentML.isSelected();
	}

	@Override
	public boolean isXTandemSelected() {
		return jRadioButtonXTandem.isSelected() || jRadioButtonXTandemMGF.isSelected();
	}

	@Override
	public boolean isMzIdentMLPlusMGFSelected() {
		return jRadioButtonMzIdentMLMGF.isSelected();
	}

	@Override
	public boolean isXTandemPlusMGFSelected() {
		return jRadioButtonXTandemMGF.isSelected();
	}

	@Override
	public boolean isPRIDESelected() {
		return jRadioButtonPRIDE.isSelected();
	}

	@Override
	public boolean isMzMLPlusMzIdentMLSelected() {
		return jRadioButtonMzMLMzIdentML.isSelected();
	}

	public String getStatus() {
		return jTextAreaStatus.getText();
	}

	private void appendStatus(String text) {
		jTextAreaStatus.append(text + "\n");
		jTextAreaStatus.setCaretPosition(jTextAreaStatus.getText().length() - 1);
	}

	@Override
	public boolean isMIAPEMSChecked() {
		return jCheckBoxMS.isSelected();
	}

	@Override
	public boolean isMIAPEMSIChecked() {
		return jCheckBoxMSI.isSelected();
	}

	@Override
	public boolean isLocalProcessing() {
		return jRadioButtonLocalProcessing.isSelected();
	}

	public void setLoadedProjects(Map<Integer, String> projects) {

		loadedProjects.clear();
		loadedProjects.putAll(projects);
	}

	private Map<Integer, String> getLoadedProjects() {
		while (isLoadingProjects()) {
			try {
				Thread.currentThread();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return loadedProjects;
	}

	public synchronized boolean isLoadingProjects() {
		return isLoadingProjects;
	}

	public synchronized void setLoadingProjects(boolean b) {
		isLoadingProjects = b;
	}

	public static void main(String[] args) {
		MiapeExtractionFrame instance = new MiapeExtractionFrame(null, false);
		instance.setVisible(true);

	}

	/**
	 * Show a dialog with the option of opening a browser with the direct link
	 * to the MIAPE documents
	 *
	 * @param msURL
	 * @param msiURL
	 * @param directLinks
	 */
	private void showOpenBrowserDialog(URL msURL, URL msiURL, String directLinks) {
		String plural = "";
		if (msURL != null && msiURL != null)
			plural = "(s)";

		Object[] dialog_options = { "Yes, open browser", "No, close this dialog" };
		int selected_option = JOptionPane.showOptionDialog(this,
				directLinks + "\n" + "\nClick on yes to open a browser to go directly to the document" + plural + "\n",
				"MIAPE document" + plural + " created", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, dialog_options, dialog_options[1]);
		if (selected_option == 0) { // Yes
			if (msURL != null)
				HttpUtilities.openURL(msURL.toString());
			if (msiURL != null)
				HttpUtilities.openURL(msiURL.toString());
		}
	}

	@Override
	public boolean isFastParsing() {
		return isFastParsing;
	}

	@Override
	public String getDescription() {
		return MiapeExtractionParametersUtil.getDescription(this);
	}

	@Override
	public List<File> getInputFiles() {
		List<File> ret = new ArrayList<File>();
		if (jTextFieldInputFile.isEnabled()) {
			File file = new File(jTextFieldInputFile.getText());
			if (file.exists())
				ret.add(file);
		}
		if (jTextFieldInputFile2.isEnabled()) {
			File file = new File(jTextFieldInputFile2.getText());
			if (file.exists())
				ret.add(file);
		}

		return ret;
	}

	@Override
	public Integer getAssociatedMiapeMS() {
		return null;
	}

	@Override
	public Integer getAssociatedMiapeMSGeneratorJob() {
		return null;
	}

	@Override
	public String getTemplateName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isMGFSelected() {
		return jRadioButtonMGF.isSelected();
	}

	@Override
	public boolean storeMIAPEsInDB() {

		final boolean b = jRadioButtonNotRemoteStorage.isSelected();
		return !b;
	}

	@Override
	public String getDtaSelectFileName() {
		if (isDTASelectSelected())
			return jTextFieldInputFile2.getText();
		return null;
	}

	@Override
	public boolean isDTASelectSelected() {
		return jRadioButtonDTASelect.isSelected();
	}

	@Override
	public boolean isDTASelectPlusMGFSelected() {
		return jRadioButtonDTASelectMGF.isSelected();
	}

}