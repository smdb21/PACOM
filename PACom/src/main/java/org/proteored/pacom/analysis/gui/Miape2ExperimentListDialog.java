/*
 * Miape2ExperimentListDialog.java Created on __DATE__, __TIME__
 */

package org.proteored.pacom.analysis.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jfree.ui.RefineryUtilities;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.exceptions.MiapeDataInconsistencyException;
import org.proteored.miapeapi.interfaces.MiapeHeader;
import org.proteored.pacom.analysis.conf.ComparisonProjectFileUtil;
import org.proteored.pacom.analysis.conf.jaxb.CPExperiment;
import org.proteored.pacom.analysis.conf.jaxb.CPExperimentList;
import org.proteored.pacom.analysis.conf.jaxb.CPMS;
import org.proteored.pacom.analysis.conf.jaxb.CPMSI;
import org.proteored.pacom.analysis.conf.jaxb.CPMSIList;
import org.proteored.pacom.analysis.conf.jaxb.CPMSList;
import org.proteored.pacom.analysis.conf.jaxb.CPReplicate;
import org.proteored.pacom.analysis.gui.components.ExtendedJTree;
import org.proteored.pacom.analysis.gui.components.MyTreeRenderer;
import org.proteored.pacom.analysis.gui.tasks.InitializeProjectComboBoxTask;
import org.proteored.pacom.analysis.gui.tasks.LocalDataTreeLoaderTask;
import org.proteored.pacom.analysis.gui.tasks.MiapeTreeIntegrityCheckerTask;
import org.proteored.pacom.analysis.util.FileManager;
import org.proteored.pacom.gui.AbstractJFrameWithAttachedHelpDialog;
import org.proteored.pacom.gui.ImageManager;
import org.proteored.pacom.gui.MainFrame;
import org.proteored.pacom.gui.OpenHelpButton;
import org.proteored.pacom.utils.AppVersion;
import org.proteored.pacom.utils.ComponentEnableStateKeeper;

/**
 *
 * @author __USER__
 */
public class Miape2ExperimentListDialog extends AbstractJFrameWithAttachedHelpDialog implements PropertyChangeListener {
	private static final int PROJECT_LEVEL = 1;
	private static final int EXPERIMENT_LEVEL = 2;
	private static final int REPLICATE_LEVEL = 3;
	private static final int MIAPE_LEVEL = 3;
	private static final int MIAPE_REPLICATE_LEVEL = 4;
	private static final int MIAPE_PROJECT_LEVEL = 2;
	private static final String DEFAULT_PROJECT_NAME = "Comparison project name";
	private static final String DEFAULT_EXPERIMENT_NAME = "experiment 1";
	final static String MIAPE_ID_REGEXP = "Dataset_(?:MS|MSI)_(\\d+).*";
	final static String LOCAL_MIAPE_ID_REGEXP = "Dataset_(?:MS|MSI)_(\\d+)_.+$";
	final static String LOCAL_MIAPE_UNIQUE_NAME_REGEXP = "Dataset_(?:MS|MSI)_\\d+_(.*)";

	final static String MIAPE_PROJECT_NAME_REGEXP = "\\d+:\\s(.*)";
	private static final String MIAPE_LOCAL_PROJECT_NAME_REGEXP = "(.*)";

	private boolean saved = false;
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");
	private static final String NO_CURATED_EXPERIMENTS_AVAILABLE = "No curated experiments available";
	private static Miape2ExperimentListDialog instance;
	// Associates MIAPE MSI IDs (key) with associated MIAPE MS IDs (values)

	// collection of threads that retrieve MIAPE MSI from server <miape id to
	// retrieve, miape
	// retriever>

	private File currentCgfFile;

	// Map to store the MIAPE documents that are retrieved <Identified,
	// FullPath to the file>
	// private static TIntObjectHashMap< String> miapeMSIsRetrieved = new
	// TIntObjectHashMap< String>();

	public static final String MESSAGE_SPLITTER = "****";
	public static final String SCAPED_MESSAGE_SPLITTER = "\\*\\*\\*\\*";
	public static final String DEFAULT_USER_NAME = "default_user";
	private final MainFrame parent;

	private boolean correctlyInitialized = false;
	private boolean finishPressed;
	private InitializeProjectComboBoxTask projectComboLoaderTask;
	private LocalDataTreeLoaderTask localDataTreeLoaderTask;
	private MiapeTreeIntegrityCheckerTask integrityChecker;
	private final ComponentEnableStateKeeper enableStateKeeper = new ComponentEnableStateKeeper();

	@Override
	public void dispose() {
		if (integrityChecker != null && integrityChecker.getState() == StateValue.STARTED) {
			boolean canceled = integrityChecker.cancel(true);
			while (!canceled) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				log.info("Waiting for integrityChecker closing");
				canceled = integrityChecker.cancel(true);
			}
			log.info("integrityChecker closed");
		}
		if (projectComboLoaderTask != null && projectComboLoaderTask.getState() == StateValue.STARTED) {
			boolean canceled = projectComboLoaderTask.cancel(true);
			while (!canceled) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				log.info("Waiting for projectComboLoaderTask closing");
				canceled = projectComboLoaderTask.cancel(true);
			}
			log.info("projectComboLoaderTask closed");
		}
		if (parent != null) {
			parent.setEnabled(true);
			parent.setVisible(true);
		}
		super.dispose();
	}

	/** Creates new form Miape2ExperimentListDialog */
	private Miape2ExperimentListDialog(MainFrame parent) {
		super(400);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
				| IllegalAccessException e) {
			e.printStackTrace();
		}

		initComponents();

		this.parent = parent;
		if (this.parent != null)
			this.parent.setVisible(false);

		loadIcons();

		jTextAreaStatus.setFont(new JTextField().getFont());
		RefineryUtilities.centerFrameOnScreen(this);

		// clean tree
		cleanTrees();

		// set renderer to Project JTree
		jTreeProject.setCellRenderer(new MyTreeRenderer());
		jTreeLocalMIAPEMSIs.setCellRenderer(new MyTreeRenderer());

		try {

			jButtonCancelLoading.setEnabled(false);
			appendStatus("Offline mode");
			// appendStatus("MIAPE MSI documents cannot be loaded since the
			// user has not logged-in.");
			// appendStatus("Load a previously saved comparison project or
			// login again.");

			// Initialize project tree
			initializeProjectTree(DEFAULT_PROJECT_NAME, DEFAULT_EXPERIMENT_NAME);
			correctlyInitialized = true;
		} catch (Exception e) {
			e.printStackTrace();
			log.warn(e.getMessage());
			appendStatus(e.getMessage());
		}

		AppVersion version = MainFrame.getVersion();
		if (version != null) {
			String suffix = " (v" + version.toString() + ")";
			this.setTitle(getTitle() + suffix);
		}
	}

	public static Miape2ExperimentListDialog getInstance(MainFrame parent) {
		if (instance == null) {
			instance = new Miape2ExperimentListDialog(parent);
		}
		return instance;
	}

	@Override
	public void setVisible(boolean b) {
		if (b) {
			loadProjectCombo();
			fillLocalMIAPETree();
			jButtonStartLoading.setEnabled(true);
			pack();
		}
		super.setVisible(b);
	}

	private void fillLocalMIAPETree() {
		localDataTreeLoaderTask = new LocalDataTreeLoaderTask(jTreeLocalMIAPEMSIs);
		localDataTreeLoaderTask.addPropertyChangeListener(this);
		localDataTreeLoaderTask.execute();

	}

	public void cleanTrees() {
		DefaultMutableTreeNode nodoRaiz = new DefaultMutableTreeNode("No projects found");
		DefaultTreeModel modeloArbol = new DefaultTreeModel(nodoRaiz);
		jTreeProject.setModel(modeloArbol);
		jTreeLocalMIAPEMSIs.setModel(modeloArbol);
	}

	private void loadIcons() {
		// set icon image
		setIconImage(ImageManager.getImageIcon(ImageManager.PACOM_LOGO).getImage());
		jButtonStartLoading.setIcon(ImageManager.getImageIcon(ImageManager.RELOAD));
		jButtonStartLoading.setPressedIcon(ImageManager.getImageIcon(ImageManager.RELOAD_CLICKED));
		jButtonCancelLoading.setIcon(ImageManager.getImageIcon(ImageManager.STOP));
		jButtonCancelLoading.setPressedIcon(ImageManager.getImageIcon(ImageManager.STOP_CLICKED));
		jButtonSave.setIcon(ImageManager.getImageIcon(ImageManager.SAVE));
		jButtonSave.setPressedIcon(ImageManager.getImageIcon(ImageManager.SAVE_CLICKED));
		jButtonFinish.setIcon(ImageManager.getImageIcon(ImageManager.FINISH));
		jButtonFinish.setPressedIcon(ImageManager.getImageIcon(ImageManager.FINISH_CLICKED));
		jButtonLoadSavedProject.setIcon(ImageManager.getImageIcon(ImageManager.LOAD));
		jButtonLoadSavedProject.setPressedIcon(ImageManager.getImageIcon(ImageManager.LOAD_CLICKED));

		jButtonRemoveSavedProject.setIcon(ImageManager.getImageIcon(ImageManager.DELETE));
		jButtonRemoveSavedProject.setPressedIcon(ImageManager.getImageIcon(ImageManager.DELETE_CLICKED));

		jButtonAddCuratedExperiment.setIcon(ImageManager.getImageIcon(ImageManager.ADD));
		jButtonAddCuratedExperiment.setPressedIcon(ImageManager.getImageIcon(ImageManager.ADD_CLICKED));
		jButtonAddExperiment.setIcon(ImageManager.getImageIcon(ImageManager.ADD));
		jButtonAddExperiment.setPressedIcon(ImageManager.getImageIcon(ImageManager.ADD_CLICKED));
		jButtonAddReplicate.setIcon(ImageManager.getImageIcon(ImageManager.ADD));
		jButtonAddReplicate.setPressedIcon(ImageManager.getImageIcon(ImageManager.ADD_CLICKED));
		jButtonClearProject2.setIcon(ImageManager.getImageIcon(ImageManager.CLEAR));
		jButtonClearProject2.setPressedIcon(ImageManager.getImageIcon(ImageManager.CLEAR_CLICKED));
		jButtonDeleteNode.setIcon(ImageManager.getImageIcon(ImageManager.DELETE));
		jButtonDeleteNode.setPressedIcon(ImageManager.getImageIcon(ImageManager.DELETE_CLICKED));
		jButtonRemoveCuratedExperiment.setIcon(ImageManager.getImageIcon(ImageManager.DELETE));
		jButtonRemoveCuratedExperiment.setPressedIcon(ImageManager.getImageIcon(ImageManager.DELETE_CLICKED));

		jButtonGoToImport.setIcon(ImageManager.getImageIcon(ImageManager.LOAD_LOGO_32));
		jButtonGoToImport.setPressedIcon(ImageManager.getImageIcon(ImageManager.LOAD_LOGO_32_CLICKED));

	}

	private void enableCancelTreeLoadinButton(boolean b) {
		jButtonCancelLoading.setEnabled(b);
	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		jPanel1 = new javax.swing.JPanel();
		jTabbedPane = new javax.swing.JTabbedPane();
		jPanel10 = new javax.swing.JPanel();
		jScrollPane1 = new javax.swing.JScrollPane();
		jPanel12 = new javax.swing.JPanel();
		jScrollPane5 = new javax.swing.JScrollPane();
		jTreeLocalMIAPEMSIs = new ExtendedJTree();
		jPanel11 = new javax.swing.JPanel();
		jScrollPane4 = new javax.swing.JScrollPane();
		jLabel8 = new javax.swing.JLabel();
		jButtonAddManualList = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		jScrollPane2 = new javax.swing.JScrollPane();
		jTreeProject = new ExtendedJTree(true);
		jPanel4 = new javax.swing.JPanel();
		jScrollPane3 = new javax.swing.JScrollPane();
		jTextAreaStatus = new javax.swing.JTextArea();
		jProgressBar = new javax.swing.JProgressBar();
		jPanel6 = new javax.swing.JPanel();
		jLabel5 = new javax.swing.JLabel();
		jLabelMIAPEMSINumber = new javax.swing.JLabel();
		jButtonCancelLoading = new javax.swing.JButton();
		jButtonCancelLoading.setToolTipText("Click here to stop the loading of the datasets and projects\r\n");
		jButtonStartLoading = new javax.swing.JButton();
		jPanel7 = new javax.swing.JPanel();
		jLabel7 = new javax.swing.JLabel();
		jComboBox1 = new javax.swing.JComboBox<String>();
		jComboBox1.setToolTipText("Select here a saved comparison project");
		jButtonLoadSavedProject = new javax.swing.JButton();
		jButtonLoadSavedProject.setToolTipText("Click here to load the selected saved project");
		jButtonRemoveSavedProject = new javax.swing.JButton();
		jButtonRemoveSavedProject.setToolTipText(
				"<html>\r\nClick here to delete the selected saved project.<br>\r\n<b>This action cannot be undone</b>\r\n</html>\r\n");
		jPanel3 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jTextProjectName = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		jTextExperimentName = new javax.swing.JTextField();
		jButtonAddExperiment = new javax.swing.JButton();
		jLabel3 = new javax.swing.JLabel();
		jTextReplicateName = new javax.swing.JTextField();
		jButtonAddReplicate = new javax.swing.JButton();
		jButtonDeleteNode = new javax.swing.JButton();
		jButtonDeleteNode.setToolTipText("Click here to delete the selected node in the Comparison Project tree");
		jPanel8 = new javax.swing.JPanel();
		jTextFieldLabelNameTemplate = new javax.swing.JTextField();
		jLabel4 = new javax.swing.JLabel();
		jLabel6 = new javax.swing.JLabel();
		jTextFieldLabelNumberTemplate = new javax.swing.JTextField();
		jPanel9 = new javax.swing.JPanel();
		jComboBoxCuratedExperiments = new javax.swing.JComboBox<String>();
		jButtonAddCuratedExperiment = new javax.swing.JButton();
		jButtonRemoveCuratedExperiment = new javax.swing.JButton();
		jPanel5 = new javax.swing.JPanel();
		jButtonFinish = new javax.swing.JButton();
		jButtonSave = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Comparison Projects Manager");

		jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"Individual datasets"));

		jTabbedPane.setToolTipText("Input datasets stored in local system");

		jPanel10.setToolTipText("Input datasets in remote repository");

		javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
		jPanel10.setLayout(jPanel10Layout);
		jPanel10Layout.setHorizontalGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel10Layout.createSequentialGroup().addContainerGap()
						.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
						.addContainerGap()));
		jPanel10Layout.setVerticalGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
						jPanel10Layout.createSequentialGroup().addContainerGap()
								.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE)
								.addContainerGap()));

		jPanel12.setToolTipText("Input datasets stored in local system");

		jTreeLocalMIAPEMSIs.setAutoscrolls(true);
		jTreeLocalMIAPEMSIs.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jTreeLocalMIAPEMSIsMouseClicked(evt);
			}
		});
		jScrollPane5.setViewportView(jTreeLocalMIAPEMSIs);

		javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
		jPanel12.setLayout(jPanel12Layout);
		jPanel12Layout.setHorizontalGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel12Layout.createSequentialGroup().addContainerGap()
						.addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
						.addContainerGap()));
		jPanel12Layout.setVerticalGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
						jPanel12Layout.createSequentialGroup().addContainerGap()
								.addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE)
								.addContainerGap()));

		jTabbedPane.addTab("Imported datasets", jPanel12);

		jPanel11.setToolTipText("External protein lists");

		jLabel8.setText("Add new protein lists:");

		jButtonAddManualList.setText("Add");
		jButtonAddManualList.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				// jButtonAddManualListActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
		jPanel11.setLayout(jPanel11Layout);
		jPanel11Layout.setHorizontalGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel11Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
								.addGroup(jPanel11Layout.createSequentialGroup().addComponent(jLabel8)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jButtonAddManualList)))
						.addContainerGap()));
		jPanel11Layout.setVerticalGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel11Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel8).addComponent(jButtonAddManualList))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
						.addContainerGap()));

		// jTabbedPane.addTab("External protein lists", jPanel11);

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup()
						.addComponent(jTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
						.addContainerGap()));

		jPanel2.setBorder(
				new TitledBorder(null, "Comparison Project Tree", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		jTreeProject.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jTreeProjectMouseClicked(evt);
			}
		});

		jScrollPane2.setViewportView(jTreeProject);

		jButtonClearProject2 = new JButton("Clear comparison project");
		jButtonClearProject2.setToolTipText("Click here to clear the Comparison Project.\r\n");
		jButtonClearProject2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				jButtonClearProjectTreeActionPerformed(e);

			}
		});

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
		jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
								.addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
								.addComponent(jButtonClearProject2, Alignment.TRAILING))
						.addContainerGap()));
		jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup()
						.addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(jButtonClearProject2)));
		jPanel2.setLayout(jPanel2Layout);

		jPanel4.setBorder(
				javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Status"));

		jTextAreaStatus.setColumns(20);
		jTextAreaStatus.setLineWrap(true);
		jTextAreaStatus.setRows(5);
		jTextAreaStatus.setWrapStyleWord(true);
		jTextAreaStatus.setEditable(false);
		jScrollPane3.setViewportView(jTextAreaStatus);

		javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
		jPanel4.setLayout(jPanel4Layout);
		jPanel4Layout
				.setHorizontalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanel4Layout.createSequentialGroup().addContainerGap()
								.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 1023,
												Short.MAX_VALUE)
										.addComponent(jProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 1023,
												Short.MAX_VALUE))
								.addContainerGap()));
		jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel4Layout.createSequentialGroup()
						.addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 74,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 23,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"Loaded input datasets"));

		jLabel5.setText("Input datasets:");

		jLabelMIAPEMSINumber.setText("0");

		jButtonCancelLoading.setText("Stop loading input data");
		jButtonCancelLoading.setEnabled(false);
		jButtonCancelLoading.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
			}
		});

		jButtonStartLoading.setText("Reload datasets and projects");
		jButtonStartLoading.setToolTipText("Reloads datasets and projects");
		jButtonStartLoading.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonStartLoadingActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
		jPanel6.setLayout(jPanel6Layout);
		jPanel6Layout.setHorizontalGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel6Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(jPanel6Layout.createSequentialGroup().addComponent(jLabel5)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jLabelMIAPEMSINumber, javax.swing.GroupLayout.PREFERRED_SIZE, 69,
												javax.swing.GroupLayout.PREFERRED_SIZE))
								.addGroup(jPanel6Layout.createSequentialGroup().addComponent(jButtonCancelLoading)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jButtonStartLoading)))
						.addContainerGap(108, Short.MAX_VALUE)));
		jPanel6Layout.setVerticalGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel6Layout.createSequentialGroup()
						.addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel5).addComponent(jLabelMIAPEMSINumber))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jButtonCancelLoading).addComponent(jButtonStartLoading))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"Load previously saved projects"));

		jLabel7.setText("Saved projects:");

		jComboBox1.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jComboBox1ActionPerformed(evt);
			}
		});

		jButtonLoadSavedProject.setText("Load");
		jButtonLoadSavedProject.setEnabled(false);
		jButtonLoadSavedProject.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jButtonLoadSavedProjectMouseClicked(evt);
			}
		});

		jButtonRemoveSavedProject.setText("delete");
		jButtonRemoveSavedProject.setEnabled(false);
		jButtonRemoveSavedProject.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jButtonRemoveSavedProjectMouseClicked(evt);
			}
		});

		javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
		jPanel7.setLayout(jPanel7Layout);
		jPanel7Layout.setHorizontalGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel7Layout.createSequentialGroup().addContainerGap().addComponent(jLabel7)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 351,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jButtonLoadSavedProject)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jButtonRemoveSavedProject).addContainerGap(26, Short.MAX_VALUE)));
		jPanel7Layout
				.setVerticalGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanel7Layout.createSequentialGroup().addContainerGap().addGroup(jPanel7Layout
								.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel7)
								.addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jButtonLoadSavedProject).addComponent(jButtonRemoveSavedProject))
								.addContainerGap(23, Short.MAX_VALUE)));

		jPanel3.setBorder(
				javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Edit"));

		jLabel1.setText("Comparison project name:");

		jTextProjectName.setToolTipText(
				"<html>\r\nName of the comparison project root name.<br>\r\nTo edit the name of the comparison project, select the root of the project<br>\r\nand change the name.\r\n</html>");
		jTextProjectName.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyReleased(java.awt.event.KeyEvent evt) {
				jTextProjectNameKeyReleased(evt);

			}
		});
		jTextProjectName.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {

			}

			@Override
			public void focusGained(FocusEvent e) {
				boolean selected = jTreeProject.selectRoot();
				if (selected) {
					enableExperimentControls(jTextExperimentName.getText(), true);
				}
			}
		});

		jLabel2.setText("Level 1 node name:");
		jLabel2.setToolTipText(
				"<html>\nThe level 1 can represent an experiment or simply a <br>\ncompilation of peptide and protein identification sets.\n</html>");

		jTextExperimentName.setToolTipText(
				"<html>\r\nLevel 1 node name. edition<br>\r\nHere you can edit a selected level 1 node or add a new level 1 node.<br>\r\nTo add a new level 1 node, select the root of the comparison project tree,<br>\r\ntype a new name, and click on the 'Add' button in the right.<br>\r\nTo edit the name of an already existing level 1 node, selected it and<br>\r\nchange the name.\r\n</html>");
		jTextExperimentName.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyPressed(java.awt.event.KeyEvent evt) {
				jTextExperimentNameKeyPressed(evt);
			}

			@Override
			public void keyReleased(java.awt.event.KeyEvent evt) {
				jTextExperimentNameKeyReleased(evt);
			}
		});
		jTextExperimentName.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {

			}

			@Override
			public void focusGained(FocusEvent e) {
				final String experimentName = jTextExperimentName.getText();
				if (!"".equals(experimentName)) {
					boolean selected = jTreeProject.selectNode(experimentName, 1);
					if (selected) {
						// enable replicate
						enableReplicateControls(jTextReplicateName.getText(), true);
					}
				}
			}
		});
		jButtonAddExperiment.setText("Add");
		jButtonAddExperiment.setToolTipText("Click here to add a new level 1 node to the Comparison Project Tree.");
		jButtonAddExperiment.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonAddExperimentActionPerformed(evt);
			}
		});

		jLabel3.setText("Level 2 node name:");
		jLabel3.setToolTipText(
				"<html>\nThe level 2 can represent a peptide and protein identification set comming from:<br>\n<ul>\n<li>a technical/biological replicate,</li>\n<li>a fraction in a LC separation,</li>\n<li>a gel spot,</li>\n<li>or a gel band.</li>\n</html>");

		jTextReplicateName.setToolTipText(
				"<html>\r\nLevel 2 node name. edition<br>\r\nTo add a new level 2 node, select a level 1 node, type a new name<br>\r\nand click on the 'Add' button in the right.<br>\r\nTo edit the name of an already existing level 2 node, selected it and<br>\r\nchange the name.\r\n</html>");
		jTextReplicateName.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyPressed(java.awt.event.KeyEvent evt) {
				jTextReplicateNameKeyPressed(evt);
			}

			@Override
			public void keyReleased(java.awt.event.KeyEvent evt) {
				jTextReplicateNameKeyReleased(evt);
			}
		});
		jTextReplicateName.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {

			}

			@Override
			public void focusGained(FocusEvent e) {
				final String replicateName = jTextReplicateName.getText();
				if (!"".equals(replicateName)) {
					jTreeProject.selectNode(replicateName, 2);
				}
			}
		});
		jButtonAddReplicate.setText("Add");
		jButtonAddReplicate.setToolTipText("Click here to add a new level 2 node to the Comparison Project Tree.");
		jButtonAddReplicate.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonAddReplicateActionPerformed(evt);
			}
		});

		jButtonDeleteNode.setText("Delete selected node");
		jButtonDeleteNode.setEnabled(false);
		jButtonDeleteNode.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonDeleteNodeActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
		jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING).addGroup(jPanel3Layout
				.createSequentialGroup().addContainerGap()
				.addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING).addGroup(jPanel3Layout
						.createSequentialGroup().addComponent(jLabel1).addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(jTextProjectName, GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
						.addGap(118))
						.addGroup(jPanel3Layout.createSequentialGroup()
								.addGroup(jPanel3Layout.createParallelGroup(Alignment.TRAILING)
										.addGroup(Alignment.LEADING,
												jPanel3Layout.createSequentialGroup().addComponent(jLabel3)
														.addPreferredGap(ComponentPlacement.UNRELATED)
														.addComponent(jTextReplicateName, GroupLayout.DEFAULT_SIZE, 303,
																Short.MAX_VALUE))
										.addGroup(Alignment.LEADING,
												jPanel3Layout.createSequentialGroup().addComponent(jLabel2)
														.addPreferredGap(ComponentPlacement.UNRELATED)
														.addComponent(jTextExperimentName, GroupLayout.DEFAULT_SIZE,
																303, Short.MAX_VALUE)))
								.addGap(18)
								.addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
										.addComponent(jButtonAddReplicate).addComponent(jButtonAddExperiment))
								.addGap(13))
						.addGroup(jPanel3Layout.createSequentialGroup().addComponent(jButtonDeleteNode)
								.addContainerGap(356, Short.MAX_VALUE)))));
		jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING).addGroup(jPanel3Layout
				.createSequentialGroup().addContainerGap(14, Short.MAX_VALUE)
				.addGroup(jPanel3Layout.createParallelGroup(Alignment.BASELINE).addComponent(jLabel1).addComponent(
						jTextProjectName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE))
				.addGap(18)
				.addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
						.addGroup(Alignment.TRAILING,
								jPanel3Layout.createSequentialGroup().addGap(21)
										.addGroup(jPanel3Layout.createParallelGroup(Alignment.BASELINE)
												.addComponent(jTextExperimentName, GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(jLabel2)))
						.addComponent(jButtonAddExperiment, Alignment.TRAILING))
				.addGap(18)
				.addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
						.addGroup(Alignment.TRAILING, jPanel3Layout.createParallelGroup(Alignment.BASELINE)
								.addComponent(jTextReplicateName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabel3))
						.addComponent(jButtonAddReplicate, Alignment.TRAILING))
				.addGap(31).addComponent(jButtonDeleteNode).addContainerGap(20, Short.MAX_VALUE)));
		jPanel3.setLayout(jPanel3Layout);

		jPanel8.setBorder(new TitledBorder(null, "Level 2 node name template", TitledBorder.LEADING, TitledBorder.TOP,
				null, null));
		jPanel8.setToolTipText(
				"<html>\nThe name generator template defines which names will be<br>\nautomatically assigned to the Level 2 nodes<br> (replicates/fractions/bands)</html>");

		jTextFieldLabelNameTemplate.setText("rep:");
		jTextFieldLabelNameTemplate.setToolTipText(
				"<html>The name of the level 2 node.<br>\r\nWhen you add a new level 2 node, it will be named with this name.<br>\r\nIf an 'Incrementing suffix' number is stated, it will be added to the name.<br>\r\nThe name of the level 2 nodes will be editable by selecting them and <br>\r\nchanging the name in the textbox above..</html>");

		jLabel4.setText("Node name:");
		jLabel4.setToolTipText(
				"<html>The name of the level 2 node.<br>\r\nWhen you add a new level 2 node, it will be named with this name.<br>\r\nIf an 'Incrementing suffix' number is stated, it will be added to the name.<br>\r\nThe name of the level 2 nodes will be editable by selecting them and <br>\r\nchanging the name in the textbox above..</html>");

		jLabel6.setText("Incrementing suffix:");
		jLabel6.setToolTipText(
				"<html>The sufix of the name of the level 2 node.<br>\r\nIt will be automatically incremented any time<br>\r\nyou add a new level 2 node.</html>");

		jTextFieldLabelNumberTemplate.setText("1");
		jTextFieldLabelNumberTemplate.setToolTipText(
				"<html>The sufix of the name of the level 2 node.<br>\r\nIt will be automatically incremented any time<br>\r\nyou add a new level 2 node.</html>");

		javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
		jPanel8Layout.setHorizontalGroup(jPanel8Layout.createParallelGroup(Alignment.LEADING).addGroup(jPanel8Layout
				.createSequentialGroup().addContainerGap().addComponent(jLabel4)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(jTextFieldLabelNameTemplate, GroupLayout.PREFERRED_SIZE, 107, GroupLayout.PREFERRED_SIZE)
				.addGap(18).addComponent(jLabel6).addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(jTextFieldLabelNumberTemplate, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(167, Short.MAX_VALUE)));
		jPanel8Layout.setVerticalGroup(jPanel8Layout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanel8Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel8Layout.createParallelGroup(Alignment.BASELINE).addComponent(jLabel4)
								.addComponent(jTextFieldLabelNameTemplate, GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabel6).addComponent(jTextFieldLabelNumberTemplate,
										GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		jPanel8.setLayout(jPanel8Layout);

		jPanel9.setBorder(new TitledBorder(null, "Add curated datasets to project", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		jPanel9.setToolTipText(
				"<html>\r\n<b>Curated datasets</b> are created in the Chart Viewer,<br> usually after appling some filters.<br> This curated projects are lighter than normal projects<br> since filtered-out data is discarted and is not loaded.</html>");

		jComboBoxCuratedExperiments
				.setModel(new DefaultComboBoxModel(new String[] { "No curated datasets available" }));
		jComboBoxCuratedExperiments.setToolTipText(
				"<html>\n<b>Curated experiments<b/> are created in the Charts<br> viewer, usually after applying some filters.<br> This curated projects are lighter than normal projects<br> since filtered-out data is discarted and is not loaded. </html>");
		jComboBoxCuratedExperiments.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jComboBoxCuratedExperimentsActionPerformed(evt);
			}
		});

		jButtonAddCuratedExperiment.setText("Add curated dataset");
		jButtonAddCuratedExperiment.setToolTipText(
				"<html>\r\nClick here to add a <b>curated dataset</b> to the comparison project.<br>\r\nCurated datasets are created in the Charts Viewer. They are datasets that<br>\r\nare saved after applying some filters.<br>\r\nThis curated datasets are lighter than normal projects since filtered-out<br>\r\ndata is discarted and is not loaded.\r\n</html>");
		jButtonAddCuratedExperiment.setEnabled(false);
		jButtonAddCuratedExperiment.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonAddCuratedExperimentActionPerformed(evt);
			}
		});

		jButtonRemoveCuratedExperiment.setText("Remove curated dataset");
		jButtonRemoveCuratedExperiment.setToolTipText(
				"<html>\r\nClick to remove the selected curated dataset.<br>\r\nCurated datasets are created in the Charts Viewer. They are datasets that<br>\r\nare saved after applying some filters.<br>\r\nThis curated datasets are lighter than normal projects since filtered-out<br>\r\ndata is discarted and is not loaded.\r\n</html>");
		jButtonRemoveCuratedExperiment.setEnabled(false);
		jButtonRemoveCuratedExperiment.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonRemoveCuratedExperimentActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
		jPanel9Layout.setHorizontalGroup(jPanel9Layout.createParallelGroup(Alignment.LEADING).addGroup(jPanel9Layout
				.createSequentialGroup().addContainerGap()
				.addGroup(jPanel9Layout.createParallelGroup(Alignment.LEADING)
						.addGroup(jPanel9Layout.createSequentialGroup().addComponent(jButtonAddCuratedExperiment)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(jButtonRemoveCuratedExperiment))
						.addComponent(jComboBoxCuratedExperiments, 0, 515, Short.MAX_VALUE))
				.addContainerGap()));
		jPanel9Layout.setVerticalGroup(jPanel9Layout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanel9Layout.createSequentialGroup().addContainerGap()
						.addComponent(jComboBoxCuratedExperiments, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(18)
						.addGroup(jPanel9Layout.createParallelGroup(Alignment.LEADING)
								.addComponent(jButtonRemoveCuratedExperiment).addComponent(jButtonAddCuratedExperiment))
						.addContainerGap()));
		jPanel9.setLayout(jPanel9Layout);

		jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		jButtonFinish.setText("Next");
		jButtonFinish.setToolTipText("Click to go to the Charts Viewer");
		jButtonFinish.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonFinishActionPerformed(evt);
			}
		});

		jButtonSave.setText("Save project");
		jButtonSave.setToolTipText("Click to save current comparison project");
		jButtonSave.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonSaveActionPerformed(evt);
			}
		});

		jButtonGoToImport = new JButton("Go to Import");
		jButtonGoToImport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				goToImport();
			}
		});
		jButtonGoToImport.setToolTipText("<html>Click to go to the <b>Import Dataset</b> window</html>");

		jButtonHelp = new OpenHelpButton(this);
		javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
		jPanel5Layout.setHorizontalGroup(jPanel5Layout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanel5Layout.createSequentialGroup().addContainerGap().addComponent(jButtonGoToImport)
						.addGap(18).addComponent(jButtonSave).addGap(18).addComponent(jButtonFinish)
						.addPreferredGap(ComponentPlacement.RELATED, 79, Short.MAX_VALUE).addComponent(jButtonHelp)
						.addGap(20)));
		jPanel5Layout.setVerticalGroup(jPanel5Layout.createParallelGroup(Alignment.TRAILING)
				.addGroup(jPanel5Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel5Layout.createParallelGroup(Alignment.BASELINE).addComponent(jButtonGoToImport)
								.addComponent(jButtonSave).addComponent(jButtonFinish))
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGroup(jPanel5Layout.createSequentialGroup().addContainerGap(29, Short.MAX_VALUE)
						.addComponent(jButtonHelp).addContainerGap()));
		jPanel5.setLayout(jPanel5Layout);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		layout.setHorizontalGroup(
				layout.createParallelGroup(Alignment.TRAILING).addGroup(layout.createSequentialGroup().addContainerGap()
						.addGroup(layout.createParallelGroup(Alignment.TRAILING).addComponent(jPanel4,
								GroupLayout.DEFAULT_SIZE, 1234, Short.MAX_VALUE)
								.addGroup(layout
										.createSequentialGroup()
										.addGroup(layout.createParallelGroup(Alignment.LEADING)
												.addGroup(layout.createSequentialGroup()
														.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(jPanel2, GroupLayout.DEFAULT_SIZE,
																368, Short.MAX_VALUE))
												.addComponent(jPanel7, GroupLayout.DEFAULT_SIZE,
														GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(layout.createParallelGroup(Alignment.LEADING)
												.addComponent(jPanel5, GroupLayout.DEFAULT_SIZE,
														GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(jPanel8, GroupLayout.DEFAULT_SIZE,
														GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(jPanel9, GroupLayout.DEFAULT_SIZE,
														GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(jPanel3, GroupLayout.DEFAULT_SIZE,
														GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(jPanel6, GroupLayout.DEFAULT_SIZE, 547,
														Short.MAX_VALUE))))
						.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(jPanel7, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(jPanel6, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(Alignment.LEADING)
						.addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
								.addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addGroup(layout.createSequentialGroup()
										.addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(jPanel9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(jPanel8, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(jPanel5,
												GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE))))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(jPanel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		getContentPane().setLayout(layout);

		pack();
	}// </editor-fold>
		// GEN-END:initComponents

	protected void goToImport() {
		this.setVisible(false);
		this.parent.startDataImport();
	}

	private void jTreeLocalMIAPEMSIsMouseClicked(java.awt.event.MouseEvent evt) {
		try {
			localMiapeTreeClicked(evt);
		} catch (IllegalMiapeArgumentException e) {
			appendStatus("Error: " + e.getMessage());
		}
	}

	private void localMiapeTreeClicked(MouseEvent evt) {
		// double click
		if (evt.getClickCount() == 2) {
			if (jTreeLocalMIAPEMSIs.isOnlyOneNodeSelected(MIAPE_LEVEL)) {
				// get MIAPE ID
				String miapeID = jTreeLocalMIAPEMSIs.getStringFromSelection(LOCAL_MIAPE_ID_REGEXP);
				// get MIAPE Name
				String miapeName = jTreeLocalMIAPEMSIs.getStringFromSelection(LOCAL_MIAPE_UNIQUE_NAME_REGEXP);
				if ("".equals(miapeName)) {
					miapeName = "Dataset_" + miapeID;
				}
				// get local project name
				String projectName = jTreeLocalMIAPEMSIs
						.getStringFromParentOfSelection(MIAPE_LOCAL_PROJECT_NAME_REGEXP);
				// start retrieving
				final Integer msi_id = Integer.valueOf(miapeID);

				Integer ms_id = getAssociatedLocalMIAPEMSId(msi_id, projectName, miapeName);
				// if a replicate is selected
				if (jTreeProject.isOnlyOneNodeSelected(REPLICATE_LEVEL)
						|| jTreeProject.isOnlyOneNodeSelected(MIAPE_REPLICATE_LEVEL)) {

					DefaultMutableTreeNode replicateNode = jTreeProject.getSelectedNode();
					if (jTreeProject.isOnlyOneNodeSelected(MIAPE_REPLICATE_LEVEL)) {
						replicateNode = (DefaultMutableTreeNode) replicateNode.getParent();
					}
					// check if the experiment node is curated. If so, throw
					// exception
					CPExperiment experiment = (CPExperiment) ((DefaultMutableTreeNode) replicateNode.getParent())
							.getUserObject();
					if (experiment.isCurated()) {
						throw new IllegalMiapeArgumentException(
								"A non curated dataset cannot be added to a curated experiment");
					}
					CPReplicate cpReplicate = (CPReplicate) replicateNode.getUserObject();
					// String miapeNodeName = getMIAPENodeNameFromTemplate();

					String miapeNodeName = FilenameUtils
							.getBaseName(FileManager.getMiapeMSILocalFileName(Integer.valueOf(miapeID), miapeName));

					CPMSI cpMsi = new CPMSI();
					cpMsi.setId(msi_id);
					cpMsi.setName(miapeNodeName);
					cpMsi.setLocal(true);
					cpMsi.setLocalProjectName(projectName);
					if (ms_id != null) {
						cpMsi.setMiapeMsIdRef(ms_id);
						CPMS cpMS = new CPMS();
						cpMS.setId(ms_id);
						cpMS.setName(FilenameUtils.getBaseName(FileManager.getMiapeMSLocalFileName(ms_id)));
						cpMS.setLocalProjectName(projectName);
						CPMSList cpMsList = null;
						if (cpReplicate.getCPMSList() == null) {
							cpMsList = new CPMSList();
							cpReplicate.setCPMSList(cpMsList);
						} else {
							cpMsList = cpReplicate.getCPMSList();
						}
						cpMsList.getCPMS().add(cpMS);
					}
					CPMSIList cpMsiList = null;
					if (cpReplicate.getCPMSIList() == null)
						cpMsiList = new CPMSIList();
					else
						cpMsiList = cpReplicate.getCPMSIList();
					cpMsiList.getCPMSI().add(cpMsi);
					cpReplicate.setCPMSIList(cpMsiList);
					final DefaultMutableTreeNode newNode = jTreeProject.addNewNode(cpMsi, replicateNode);
					// scroll to experiment node to let add more replicates
					jTreeProject.selectNode(newNode);
					saved = false;
					// }
				} else if (jTreeProject.isOnlyOneNodeSelected(EXPERIMENT_LEVEL)) {
					DefaultMutableTreeNode experimentNode = jTreeProject.getSelectedNode();
					CPExperiment cpExp = (CPExperiment) experimentNode.getUserObject();
					if (cpExp.isCurated()) {
						throw new IllegalMiapeArgumentException(
								"A non curated dataset cannot be added to a curated experiment");
					}
					// add replicate node
					String defaultReplicateName = getMIAPENodeNameFromTemplate();
					CPReplicate cpRep = new CPReplicate();
					cpRep.setName(defaultReplicateName);

					cpExp.getCPReplicate().add(cpRep);
					final DefaultMutableTreeNode replicateNode = jTreeProject.addNewNode(cpRep, experimentNode);
					// add miape node
					CPMSI cpMsi = new CPMSI();
					cpMsi.setId(msi_id);
					cpMsi.setLocal(true);
					cpMsi.setLocalProjectName(projectName);
					if (ms_id != null) {
						cpMsi.setMiapeMsIdRef(ms_id);
						CPMS cpMS = new CPMS();
						cpMS.setId(ms_id);
						cpMS.setName(FilenameUtils.getBaseName(FileManager.getMiapeMSLocalFileName(ms_id)));
						cpMS.setLocalProjectName(projectName);
						cpMS.setLocal(true);
						CPMSList cpMsList = null;
						if (cpRep.getCPMSList() == null) {
							cpMsList = new CPMSList();
							cpRep.setCPMSList(cpMsList);
						} else {
							cpMsList = cpRep.getCPMSList();
						}
						cpMsList.getCPMS().add(cpMS);
					}
					cpMsi.setName(FilenameUtils
							.getBaseName(FileManager.getMiapeMSILocalFileName(Integer.valueOf(miapeID), miapeName)));
					CPMSIList cpMsiList = new CPMSIList();
					cpMsiList.getCPMSI().add(cpMsi);
					cpRep.setCPMSIList(cpMsiList);
					final DefaultMutableTreeNode newNode = jTreeProject.addNewNode(cpMsi, replicateNode);
					// scroll to experiment node to let add more replicates
					jTreeProject.selectNode(newNode);
					saved = false;
				} else {
					throw new IllegalMiapeArgumentException("Select an level 1 node in the Comparison Project Tree.");
				}
				// increment number template
				incrementSuffixIfNumber();
				// expand tree
				// jTreeProject.expandAll();

			} else if (jTreeLocalMIAPEMSIs.isOnlyOneNodeSelected(MIAPE_PROJECT_LEVEL)) {
				// if the user clicks on a MIAPE PROJECT
				log.info("Local dataset project selected");
				final int numMIAPEsInMIAPEProject = jTreeLocalMIAPEMSIs.getSelectedNode().getChildCount();
				if (numMIAPEsInMIAPEProject > 0) {

					// IF the node 0 is selected in the comparison project
					String localProjectName = jTreeLocalMIAPEMSIs
							.getStringFromSelection(MIAPE_LOCAL_PROJECT_NAME_REGEXP);
					final int selectedOption = JOptionPane.showConfirmDialog(this,
							"<html>Do you want to add all datasets in the project to a new level 1 node called '"
									+ localProjectName + "' in the comparison project?</html>",
							"Add datasets to comparison project", JOptionPane.YES_NO_CANCEL_OPTION);
					if (selectedOption == JOptionPane.YES_OPTION) {
						DefaultMutableTreeNode projectNode = jTreeProject.getRootNode();
						CPExperimentList cpExpList = (CPExperimentList) projectNode.getUserObject();
						CPExperiment cpExp = new CPExperiment();
						cpExp.setName(localProjectName);
						cpExp.setCurated(false);
						// Add a new experiment node
						final DefaultMutableTreeNode experimentNode = jTreeProject.addNewNode(cpExp, projectNode);

						jTextExperimentName.setText(localProjectName);

						final Enumeration children = jTreeLocalMIAPEMSIs.getSelectedNode().children();
						while (children.hasMoreElements()) {
							DefaultMutableTreeNode miapeMSIChild = (DefaultMutableTreeNode) children.nextElement();
							String msi_id = ExtendedJTree.getString(LOCAL_MIAPE_ID_REGEXP,
									(String) miapeMSIChild.getUserObject());
							String miapeName = ExtendedJTree.getString(LOCAL_MIAPE_UNIQUE_NAME_REGEXP,
									(String) miapeMSIChild.getUserObject());
							Integer ms_id = getAssociatedLocalMIAPEMSId(Integer.valueOf(msi_id), localProjectName,
									miapeName);
							// start retrieving
							// startMIAPEMSIRetrieving(Integer.valueOf(miapeID));
							// add replicate node
							String replicateName = getMIAPENodeNameFromTemplate();
							CPReplicate cpRep = new CPReplicate();
							cpRep.setName(replicateName);

							cpExp.getCPReplicate().add(cpRep);
							DefaultMutableTreeNode replicateNode = jTreeProject.addNewNode(cpRep, experimentNode);
							// add miape node
							String miapeNodeName = FilenameUtils.getBaseName(
									FileManager.getMiapeMSILocalFileName(Integer.valueOf(msi_id), miapeName));
							CPMSI cpMsi = new CPMSI();
							cpMsi.setId(Integer.valueOf(msi_id));
							cpMsi.setName(miapeNodeName);
							cpMsi.setLocal(true);
							cpMsi.setLocalProjectName(localProjectName);
							if (ms_id != null) {
								cpMsi.setMiapeMsIdRef(ms_id);
								CPMS cpMS = new CPMS();
								cpMS.setId(ms_id);
								cpMS.setName(FilenameUtils.getBaseName(FileManager.getMiapeMSLocalFileName(ms_id)));
								cpMS.setLocalProjectName(localProjectName);
								cpMS.setLocal(true);
								CPMSList cpMsList = null;
								if (cpRep.getCPMSList() == null) {
									cpMsList = new CPMSList();
									cpRep.setCPMSList(cpMsList);
								} else {
									cpMsList = cpRep.getCPMSList();
								}
								cpMsList.getCPMS().add(cpMS);

							}
							CPMSIList cpMsiList = new CPMSIList();
							cpMsiList.getCPMSI().add(cpMsi);
							cpRep.setCPMSIList(cpMsiList);
							final DefaultMutableTreeNode newNode = jTreeProject.addNewNode(cpMsi, replicateNode);
							jTreeProject.selectNode(newNode);
							saved = false;
							// increment number template
							incrementSuffixIfNumber();
						}
						cpExpList.getCPExperiment().add(cpExp);
						// jTreeProject.scrollToNode(projectNode);
						// jTreeProject.expandNode(projectNode);
						// expand tree
						// jTreeProject.expandAll();
					}

				} else {
					throw new IllegalMiapeArgumentException("The project has not any dataset!");
				}
			}
		}

	}

	private Integer getAssociatedLocalMIAPEMSId(Integer id, String projectName, String miapeName) {
		final File msiFile = new File(
				FileManager.getMiapeLocalDataPath(projectName) + FileManager.getMiapeMSILocalFileName(id, miapeName));
		if (msiFile.exists()) {
			MiapeHeader miapeMSIHeader;
			// try {
			// final MIAPEMSIXmlFile miapemsiXmlFile = new
			// MIAPEMSIXmlFile(msiFile);
			// miapemsiXmlFile.setCvUtil(OntologyLoaderTask.getCvManager());
			// MiapeMSIDocument miapeDocument =
			// miapemsiXmlFile.toDocument();
			// miapeMSIHeader = new MiapeHeader(miapeDocument);

			// final MIAPEMSIXmlFile miapemsiXmlFile = new
			// MIAPEMSIXmlFile(msiFile);
			// miapemsiXmlFile.setCvUtil(OntologyLoaderTask.getCvManager());
			// MiapeMSIDocument miapeDocument =
			// miapemsiXmlFile.toDocument();
			miapeMSIHeader = new MiapeHeader(msiFile, false);
			log.debug("referenced MS dataset: " + miapeMSIHeader.getMiapeRef());
			if (miapeMSIHeader.getMiapeRef() != -1)
				return miapeMSIHeader.getMiapeRef();
			// }
			// catch (MiapeDatabaseException e) {
			// e.printStackTrace();
			// } catch (MiapeSecurityException e) {
			// e.printStackTrace();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }

		}
		return null;
	}

	private void jButtonRemoveSavedProjectMouseClicked(java.awt.event.MouseEvent evt) {
		String projectName = (String) jComboBox1.getSelectedItem();
		if (projectName != null && !"".equals(projectName)) {
			int option = JOptionPane.showConfirmDialog(this,
					"<html>The comparison project will be deleted.<br>Are you sure?</html>",
					"Delete comparison project", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (option == JOptionPane.YES_OPTION) {
				boolean deleted = FileManager.removeProjectXMLFile(projectName);
				if (deleted) {
					appendStatus("Comparison project '" + projectName + "' deleted");
					loadProjectCombo();
				}
			}
		}
	}

	private void jComboBoxCuratedExperimentsActionPerformed(java.awt.event.ActionEvent evt) {

		final Object object = jComboBoxCuratedExperiments.getSelectedItem();
		if (NO_CURATED_EXPERIMENTS_AVAILABLE.equals(object) || "".equals(object)) {
			jButtonAddCuratedExperiment.setEnabled(false);
			jButtonRemoveCuratedExperiment.setEnabled(false);
		} else {
			jButtonAddCuratedExperiment.setEnabled(true);
			jButtonRemoveCuratedExperiment.setEnabled(true);
		}
	}

	private void jButtonRemoveCuratedExperimentActionPerformed(java.awt.event.ActionEvent evt) {
		final Object selectedItem = jComboBoxCuratedExperiments.getSelectedItem();
		if (selectedItem != null && !"".equals(selectedItem)
				&& !NO_CURATED_EXPERIMENTS_AVAILABLE.equals(selectedItem)) {
			removeCuratedExperiment(selectedItem.toString());
		}
	}

	private void removeCuratedExperiment(String curatedExperimentName) {
		int selectedOption = JOptionPane.showConfirmDialog(this,
				"<html>Curated experiment '" + curatedExperimentName
						+ "' will be removed from the disk.<br>Do you really want to continue?</html>",
				"Add datasets to comparison project", JOptionPane.YES_NO_CANCEL_OPTION);
		if (selectedOption == JOptionPane.YES_OPTION) {
			log.info("Removing curated experiment files ... " + curatedExperimentName);
			jButtonRemoveCuratedExperiment.setEnabled(false);
			boolean removed = FileManager.removeCuratedExperimentFiles(curatedExperimentName);
			if (removed)
				appendStatus("Curated experiment '" + curatedExperimentName + "' removed succesfully");
			else
				appendStatus("Some error occurred while removing curated experiment '" + curatedExperimentName + "'");

			loadProjectCombo();
		}
	}

	private void loadProjectCombo() {
		projectComboLoaderTask = new InitializeProjectComboBoxTask(this);
		projectComboLoaderTask.addPropertyChangeListener(this);
		projectComboLoaderTask.execute();
	}

	private void jButtonAddCuratedExperimentActionPerformed(java.awt.event.ActionEvent evt) {
		// See if the project list node is selected

		final String selectedItem = (String) jComboBoxCuratedExperiments.getSelectedItem();
		if (selectedItem != null && !NO_CURATED_EXPERIMENTS_AVAILABLE.equals(selectedItem)
				&& !"".equals(selectedItem)) {
			final String curatedExperimentXMLFilePath = FileManager.getCuratedExperimentXMLFilePath(selectedItem);
			File curatedExpFile = new File(curatedExperimentXMLFilePath);
			if (curatedExpFile.exists()) {
				addExperimentToProjectTree(curatedExpFile);
			} else {
				appendStatus("Error loading curated experiment: " + selectedItem + "at: "
						+ FileManager.getCuratedExperimentsDataPath());
			}
		} else {
			appendStatus("No curated experiments available or selected.");
		}

	}

	private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {
		finishPressed = false;
		saved = false;
		// SAVE project file
		try {
			boolean saved = save(true);
			if (!saved)
				return;
		} catch (IllegalMiapeArgumentException e) {
			appendStatus(e.getMessage());

		}

	}

	public void jButtonFinishActionPerformed(java.awt.event.ActionEvent evt) {
		finishPressed = true;

		// SAVE project file
		try {
			boolean saved = save(true);
			if (!saved) {
				return;
			}

		} catch (IllegalMiapeArgumentException e) {
			appendStatus(e.getMessage());
			return;
		}

		// }
	}

	private void jButtonStartLoadingActionPerformed(java.awt.event.ActionEvent evt) {

		log.info("Starting datasets tree loading");

		fillLocalMIAPETree();
		loadProjectCombo();
	}

	private void jTextProjectNameKeyReleased(java.awt.event.KeyEvent evt) {

		String projectName = jTextProjectName.getText();

		projectName = projectName.trim();
		if (!"".equals(projectName)) {
			// update the root node of the project tree
			final DefaultTreeModel model = (DefaultTreeModel) jTreeProject.getModel();
			if (model == null) {
				initializeProjectTree(DEFAULT_PROJECT_NAME, DEFAULT_EXPERIMENT_NAME);
			} else {
				DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
				CPExperimentList cpExpList = (CPExperimentList) root.getUserObject();
				cpExpList.setName(projectName);
				saved = false;
			}
			jTreeProject.reload();

		}
	}

	private void jButtonDeleteNodeActionPerformed(java.awt.event.ActionEvent evt) {
		if (jTreeProject.getSelectionCount() > 0) {
			if (jTreeProject.isOnlyOneNodeSelected(PROJECT_LEVEL)) {
				// do nothing
			} else if (jTreeProject.isOnlyOneNodeSelected(EXPERIMENT_LEVEL)) {
				final CPExperiment cpExp = (CPExperiment) jTreeProject.getSelectedNode().getUserObject();
				final CPExperimentList cpExpList = (CPExperimentList) jTreeProject.getRootNode().getUserObject();
				cpExpList.getCPExperiment().remove(cpExp);

			} else if (jTreeProject.isOnlyOneNodeSelected(REPLICATE_LEVEL)) {
				final CPReplicate cpRep = (CPReplicate) jTreeProject.getSelectedNode().getUserObject();
				CPExperiment cpExp = (CPExperiment) ((DefaultMutableTreeNode) jTreeProject.getSelectedNode()
						.getParent()).getUserObject();
				cpExp.getCPReplicate().remove(cpRep);

			} else if (jTreeProject.isOnlyOneNodeSelected(MIAPE_REPLICATE_LEVEL)) {
				final CPMSI cpMSI = (CPMSI) jTreeProject.getSelectedNode().getUserObject();
				CPReplicate cpRep = (CPReplicate) ((DefaultMutableTreeNode) jTreeProject.getSelectedNode().getParent())
						.getUserObject();
				cpRep.getCPMSIList().getCPMSI().remove(cpMSI);

			}
			jTreeProject.removeSelectedNode();
			// this.jTreeProject.expandAll();
			saved = false;
		}
	}

	private void jButtonClearProjectTreeActionPerformed(java.awt.event.ActionEvent evt) {
		initializeProjectTree(DEFAULT_PROJECT_NAME, DEFAULT_EXPERIMENT_NAME);
	}

	public JComboBox getProjectComboBox() {
		return jComboBox1;
	}

	public void setCorrectlyInitialized(boolean b) {
		correctlyInitialized = b;
	}

	private void jButtonLoadSavedProjectMouseClicked(java.awt.event.MouseEvent evt) {
		String projectName = (String) jComboBox1.getSelectedItem();
		if (projectName != null && !"".equals(projectName)) {

			String projectXMLFilePath = FileManager.getProjectXMLFilePath(projectName);
			File projectFile = new File(projectXMLFilePath);
			if (!projectFile.exists()) {

				appendStatus("Project '" + projectName + "' has not found in the projects folder");
				return;

			}
			try {
				log.info("Reading project configuration file: " + projectName);
				setStatus("Loading project: " + projectName + "...");
				initializeProjectTree(projectFile);
				log.info("Project configuration file readed");
				appendStatus("Project loaded");
			} catch (Exception e) {
				e.printStackTrace();
				log.warn(e.getMessage());
				appendStatus("Some error occurred when loading project '" + projectName + "': " + e.getMessage());
				return;
			}
		}

	}

	private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {
		final Object selectedItem = jComboBox1.getSelectedItem();
		if ("".equals(selectedItem)) {
			jButtonLoadSavedProject.setEnabled(false);
			jButtonRemoveSavedProject.setEnabled(false);
		} else {
			jButtonLoadSavedProject.setEnabled(true);
			jButtonRemoveSavedProject.setEnabled(true);
		}
	}

	private String getMIAPENodeNameFromTemplate() {
		String prefix = jTextFieldLabelNameTemplate.getText();
		String sufix = jTextFieldLabelNumberTemplate.getText();

		String miapeNodeName = "";
		if (!"".equals(prefix) || !"".equals(sufix)) {
			miapeNodeName = prefix + sufix;
		} else {
			miapeNodeName = (String) jTreeLocalMIAPEMSIs.getSelectedNode().getUserObject();
		}
		return miapeNodeName;
	}

	private void incrementSuffixIfNumber() {
		try {
			Integer num = Integer.valueOf(jTextFieldLabelNumberTemplate.getText());
			num++;
			jTextFieldLabelNumberTemplate.setText(String.valueOf(num));
		} catch (NumberFormatException e) {
			// do nothing
		}
	}

	private void jTreeProjectMouseClicked(java.awt.event.MouseEvent evt) {

		if (jTreeProject.isOnlyOneNodeSelected(EXPERIMENT_LEVEL)) {
			String expName = jTreeProject.getStringFromSelection("(.*)");
			enableExperimentControls(expName, false);
			enableReplicateControls("", true);
		} else if (jTreeProject.isOnlyOneNodeSelected(REPLICATE_LEVEL)) {
			String repName = jTreeProject.getStringFromSelection("(.*)");
			enableReplicateControls(repName, false);
			disableExperimentControls();
		} else if (jTreeProject.isOnlyOneNodeSelected(PROJECT_LEVEL)) {
			enableExperimentControls("", true);
			disableReplicateControls();
		} else if (jTreeProject.isOnlyOneNodeSelected(MIAPE_REPLICATE_LEVEL)) {
			disableExperimentControls();
			disableReplicateControls();
		}
		jButtonDeleteNode.setEnabled(true);
	}

	private void enableExperimentControls(String expName, boolean alsoButton) {
		jTextExperimentName.setEnabled(true);
		jTextExperimentName.setText(expName);
		jButtonAddExperiment.setEnabled(alsoButton);
	}

	private void enableReplicateControls(String repName, boolean alsoButton) {
		jTextReplicateName.setEnabled(true);
		jTextReplicateName.setText(repName);
		jButtonAddReplicate.setEnabled(alsoButton);
	}

	private void disableReplicateControls() {
		// jTextReplicateName.setText("");
		// jTextReplicateName.setEnabled(false);
		jButtonAddReplicate.setEnabled(false);
	}

	private void disableExperimentControls() {
		// jTextExperimentName.setText("");
		// jTextExperimentName.setEnabled(false);
		jButtonAddExperiment.setEnabled(false);
	}

	private boolean save(boolean checkTree) throws IllegalMiapeArgumentException {
		// create object tree from project tree

		if (saved) {
			log.info("The tree is already saved. Showing chart manager");

			CPExperimentList cpExpList = getCPExperimentListFromTree();
			try {
				// Save again the file because when getCPExpListFromTree is
				// called, some miapeMS node are added
				FileManager.saveProjectFile(cpExpList);
			} catch (JAXBException e) {
				throw new IllegalMiapeArgumentException(e.getMessage());
			}
			String expListName = cpExpList.getName();
			currentCgfFile = FileManager.getProjectFile(expListName);
			showChartManager();

			return false;
		}
		CPExperimentList cpExpList = getCPExperimentListFromTree();
		if (checkTree) {
			integrityChecker = new MiapeTreeIntegrityCheckerTask(this, cpExpList, false);
			integrityChecker.addPropertyChangeListener(this);
			integrityChecker.execute();

			return false;
		}
		if (cpExpList == null) {
			return false;
		}

		final String projectXMLFilePath = FileManager.getProjectXMLFilePath(cpExpList.getName());
		if (FileManager.existsProjectXMLFile(cpExpList.getName())) {
			// Show dialog
			final int option = JOptionPane.showConfirmDialog(this,
					"<html>The protein/peptide identification comparison project '" + cpExpList.getName()
							+ "' already exists.<br>Are you sure you want to overwrite it?</html>",
					"Warning, project file already exists", JOptionPane.YES_NO_OPTION);
			if (option == JOptionPane.NO_OPTION)
				throw new IllegalMiapeArgumentException("Save canceled");
		}
		appendStatus("Saving project file to: " + projectXMLFilePath);
		log.info("Saving project file to: " + projectXMLFilePath);
		// create the config file from the trees
		currentCgfFile = null;

		try {
			currentCgfFile = FileManager.saveProjectFile(cpExpList);

			loadProjectCombo();
		} catch (JAXBException e) {
			appendStatus("Error saving project file: " + e.getMessage());
			log.warn(e.getMessage());
			e.printStackTrace();
			throw new IllegalMiapeArgumentException(e);
		}

		return true;
	}

	private CPExperimentList getCPExperimentListFromTree() {

		final TreeModel model = jTreeProject.getModel();
		final DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

		final Object userObject = root.getUserObject();
		if (userObject instanceof CPExperimentList) {

			final CPExperimentList cpExperimentList = (CPExperimentList) userObject;
			final List<CPExperiment> cpExps = cpExperimentList.getCPExperiment();
			if (cpExps != null)
				for (CPExperiment cpExp : cpExps) {
					final List<CPReplicate> cpReps = cpExp.getCPReplicate();
					if (cpReps != null)
						for (CPReplicate cpRep : cpReps) {
							CPMSList cpMsList = cpRep.getCPMSList();
							// if there is not MIAPE MS list, is because there
							// is not local and the referenced miape ms
							// documents have to be retrieved
							if (cpMsList == null || cpMsList.getCPMS() == null || cpMsList.getCPMS().isEmpty()) {
								final CPMSIList cpmsiList = cpRep.getCPMSIList();
								cpMsList = new CPMSList();
								if (cpmsiList != null) {
									for (CPMSI cpMSI : cpmsiList.getCPMSI()) {
										if (((cpMSI.isLocal() != null && !cpMSI.isLocal())
												|| cpMSI.isLocal() == null)) {
											CPMS cpMS = new CPMS();
											Integer miapeMsIdRef = cpMSI.getMiapeMsIdRef();
											if (miapeMsIdRef != null) {
												cpMS.setId(miapeMsIdRef);
												cpMS.setName(FilenameUtils.getBaseName(
														FileManager.getMiapeMSLocalFileName(miapeMsIdRef)));
											}
											cpMsList.getCPMS().add(cpMS);
										}
									}
									if (!cpMsList.getCPMS().isEmpty())
										cpRep.setCPMSList(cpMsList);
								}

							}

						}
				}
			return cpExperimentList;
		}
		return null;

	}

	private void jTextReplicateNameKeyPressed(java.awt.event.KeyEvent evt) {
		// if RETURN is pressed, throw ADD button event
		if (KeyEvent.VK_ENTER == evt.getKeyCode()) {
			jButtonAddReplicateActionPerformed(null);
		}
	}

	private void jTextReplicateNameKeyReleased(java.awt.event.KeyEvent evt) {

		String repName = jTextReplicateName.getText();
		repName = repName.trim();

		if (jTreeProject.isOnlyOneNodeSelected(REPLICATE_LEVEL)) {
			final DefaultMutableTreeNode selectedNode = jTreeProject.getSelectedNode();
			jTreeProject.renameSelectedNode(repName);
			jTreeProject.selectNode(selectedNode);
			saved = false;
		}
	}

	private void jTextExperimentNameKeyPressed(java.awt.event.KeyEvent evt) {
		// if RETURN is pressed, throw ADD button event
		if (KeyEvent.VK_ENTER == evt.getKeyCode()) {
			jButtonAddExperimentActionPerformed(null);

		}
	}

	private void jTextExperimentNameKeyReleased(java.awt.event.KeyEvent evt) {

		final char keyChar = evt.getKeyChar();
		final int keyCode = evt.getKeyCode();
		String newExpName = jTextExperimentName.getText();
		newExpName = newExpName.trim();
		if (jTreeProject.isOnlyOneNodeSelected(EXPERIMENT_LEVEL)) {
			final DefaultMutableTreeNode selectedNode = jTreeProject.getSelectedNode();
			CPExperiment cpExp = (CPExperiment) selectedNode.getUserObject();
			String previousExpName = cpExp.getName();
			try {
				// if it is curated, rename the curated folder and xml project
				// before to rename the node
				if (cpExp.isCurated()) {
					if (!previousExpName.equals(newExpName)) {
						// RENAME THE FOLDER
						File curatedExpFolder = new File(FileManager.getCuratedExperimentFolderPath(previousExpName));
						File newCuratedExpFolder = new File(FileManager.getCuratedExperimentFolderPath(newExpName));
						curatedExpFolder.renameTo(newCuratedExpFolder);

						// RENAME THE CURATED EXPERIMENT FILE
						File curatedExpFile = new File(
								FileManager.getCuratedExperimentsDataPath() + FileManager.PATH_SEPARATOR + newExpName
										+ FileManager.PATH_SEPARATOR + previousExpName + ".xml");
						File newCuratedExpFile = new File(FileManager.getCuratedExperimentXMLFilePath(newExpName));
						curatedExpFile.renameTo(newCuratedExpFile);
						CPExperiment cpExp2 = FileManager.getCPExperiment(newCuratedExpFile);
						if (cpExp2 != null) {
							cpExp2.setName(newExpName);
							FileManager.saveCuratedExperimentFile(cpExp2);
						}

					}
				}

				jTreeProject.renameSelectedNode(newExpName);
				jTreeProject.selectNode(selectedNode);
				saved = false;
			} catch (JAXBException e) {
				// go to previous name
				jTextExperimentName.setText(previousExpName);
			}

		}
		log.debug("Released:'" + newExpName + "'");
		log.debug("Char = " + keyChar + " CODE = " + keyCode);
	}

	private void jButtonAddReplicateActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			addReplicate();
		} catch (IllegalMiapeArgumentException e) {
			appendStatus("Error: " + e.getMessage());
		}

	}

	private void addReplicate() {
		final String replicateName = jTextReplicateName.getText();
		if (!"".equals(replicateName)) {
			final DefaultTreeModel model = (DefaultTreeModel) jTreeProject.getModel();
			if (model != null) {
				if (jTreeProject.isOnlyOneNodeSelected(EXPERIMENT_LEVEL)) {
					// get selected experiment node
					DefaultMutableTreeNode experimentTreeNode = jTreeProject.getSelectedNode();
					CPExperiment cpExp = (CPExperiment) experimentTreeNode.getUserObject();
					if (cpExp.isCurated())
						throw new IllegalMiapeArgumentException(
								"A non curated dataset cannot be added to a curated experiment ");
					final CPReplicate cpRep = new CPReplicate();
					cpRep.setName(replicateName);
					cpExp.getCPReplicate().add(cpRep);
					// add the replicate node on the experiment node
					final DefaultMutableTreeNode replicateTreeNode = jTreeProject.addNewNode(cpRep, experimentTreeNode);
					if (jTreeLocalMIAPEMSIs.isOnlyOneNodeSelected(MIAPE_LEVEL)) {
						int miapeID = Integer.valueOf(jTreeLocalMIAPEMSIs.getStringFromSelection(MIAPE_ID_REGEXP));

						String miapeName = (String) jTreeLocalMIAPEMSIs.getSelectedNode().getUserObject();
						CPMSI cpMSI = new CPMSI();
						cpMSI.setName(miapeName);
						cpMSI.setId(miapeID);
						if (cpRep.getCPMSIList() == null) {
							cpRep.setCPMSIList(new CPMSIList());
						}
						cpRep.getCPMSIList().getCPMSI().add(cpMSI);

						// add the miape node on the replicate node
						final DefaultMutableTreeNode newNode = jTreeProject.addNewNode(cpMSI

								, replicateTreeNode);
						jTreeProject.selectNode(newNode);

						saved = false;
					} else {
						saved = false;
						jTreeProject.selectNode(replicateTreeNode);
					}

				} else if (jTreeProject.isOnlyOneNodeSelected(REPLICATE_LEVEL)) {
					int miapeID = Integer.valueOf(jTreeLocalMIAPEMSIs.getStringFromSelection(MIAPE_ID_REGEXP));

					// get selected experiment node
					DefaultMutableTreeNode replicateTreeNode = jTreeProject.getSelectedNode();
					CPReplicate cpRep = (CPReplicate) replicateTreeNode.getUserObject();

					// add the miape node on the replicate node
					String miapeNodeName = (String) jTreeLocalMIAPEMSIs.getSelectedNode().getUserObject();
					CPMSI cpMSI = new CPMSI();
					cpMSI.setName(miapeNodeName);
					cpMSI.setId(miapeID);
					cpRep.getCPMSIList().getCPMSI().add(cpMSI);

					final DefaultMutableTreeNode newNode = jTreeProject.addNewNode(cpMSI
					// getMIAPENodeName(String.valueOf(miapeID))
							, replicateTreeNode);
					jTreeProject.scrollToNode(replicateTreeNode);
					jTreeProject.expandNode(replicateTreeNode);
					saved = false;
					// expand tree
					// jTreeProject.expandAll();
				} else {
					throw new IllegalMiapeArgumentException("Select a level 1 node before to add a level 2 node");
				}
			}
		} else {
			throw new IllegalMiapeArgumentException("Type a name for the level 2 node before to click on Add button");
		}

	}

	private void jButtonAddExperimentActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			addExperiment();
		} catch (IllegalMiapeArgumentException e) {
			appendStatus("Error: " + e.getMessage());
		}

	}

	private void addExperiment() {
		final String experimentName = jTextExperimentName.getText();
		if (!"".equals(experimentName)) {
			CPExperiment cpExp = new CPExperiment();
			cpExp.setName(experimentName);
			cpExp.setCurated(false);
			final DefaultTreeModel model = (DefaultTreeModel) jTreeProject.getModel();
			if (model != null) {
				DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) model.getRoot();
				final CPExperimentList cpExpList = (CPExperimentList) parentNode.getUserObject();
				cpExpList.getCPExperiment().add(cpExp);
				final DefaultMutableTreeNode newNode = jTreeProject.addNewNode(cpExp, parentNode);
				jTreeProject.scrollToNode(newNode);
				jTreeProject.expandNode(newNode);
				saved = false;
				enableReplicateControls("", true);
			}
		} else {
			throw new IllegalMiapeArgumentException("Type a name for the level 1 node before to click on Add button");
		}

	}

	private void initializeProjectTree(String projectName, String experimentName) {
		CPExperimentList cpExpList = new CPExperimentList();
		cpExpList.setName(projectName);
		DefaultMutableTreeNode nodoRaizMSI = new DefaultMutableTreeNode(cpExpList);
		DefaultTreeModel modeloArbolMSI = new DefaultTreeModel(nodoRaizMSI);
		jTreeProject.setModel(modeloArbolMSI);
		jTextProjectName.setText(projectName);

		CPExperiment cpExp = new CPExperiment();
		cpExp.setName(experimentName);
		cpExpList.getCPExperiment().add(cpExp);
		final DefaultMutableTreeNode experimentNode = jTreeProject.addNewNode(cpExp, nodoRaizMSI);
		jTreeProject.selectNode(experimentNode);
		jTextExperimentName.setText(experimentName);
		saved = false;
	}

	public void initializeProjectTree(File projectFile) {
		CPExperimentList cpExperimentList = null;
		try {
			cpExperimentList = ComparisonProjectFileUtil.getExperimentListFromComparisonProjectFile(projectFile);

			String projectName = cpExperimentList.getName();
			DefaultMutableTreeNode nodoRaizMSI = new DefaultMutableTreeNode(cpExperimentList);
			DefaultTreeModel modeloArbolMSI = new DefaultTreeModel(nodoRaizMSI);
			jTreeProject.setModel(modeloArbolMSI);
			jTextProjectName.setText(projectName);
			final Iterator<CPExperiment> cpExpIterator = cpExperimentList.getCPExperiment().iterator();
			while (cpExpIterator.hasNext()) {
				CPExperiment cpExperiment = cpExpIterator.next();
				boolean curated = cpExperiment.isCurated();
				final DefaultMutableTreeNode experimentNode = jTreeProject.addNewNode(cpExperiment, nodoRaizMSI);
				boolean hasOneReplicate = false;
				final Iterator<CPReplicate> cpRepIterator = cpExperiment.getCPReplicate().iterator();
				while (cpRepIterator.hasNext()) {
					CPReplicate cpReplicate = cpRepIterator.next();
					final DefaultMutableTreeNode replicateNode = jTreeProject.addNewNode(cpReplicate, experimentNode);

					if (cpReplicate.getCPMSIList() != null) {
						for (CPMSI cpMsi : cpReplicate.getCPMSIList().getCPMSI()) {

							// LOCAL MIAPE
							boolean addNode = true;
							if (curated) {
								File file = new File(
										FileManager.getMiapeMSICuratedXMLFilePathFromMiapeInformation(cpMsi));
								if (!file.exists()) {
									String notificacion = "Warning:  '" + cpMsi.getName()
											+ ".xml' file doesn't exist at folder '"
											+ FileManager.getCuratedExperimentFolderPath(cpMsi.getLocalProjectName())
											+ "'";
									log.warn(notificacion);
									appendStatus(notificacion);
									addNode = false;
								}

							} else {
								File file = new File(
										FileManager.getMiapeMSIXMLFileLocalPathFromMiapeInformation(cpMsi));
								if (!file.exists()) {
									addNode = false;
									String notificacion = "Warning:  '" + cpMsi.getName()
											+ ".xml' file doesn't exist at folder '"
											+ FileManager.getMiapeLocalDataPath(cpMsi.getLocalProjectName()) + "'";
									log.warn(notificacion);
									appendStatus(notificacion);
								}
							}
							if (addNode) {
								jTreeProject.addNewNode(cpMsi, replicateNode);
								hasOneReplicate = true;
							} else {
								cpRepIterator.remove();
								jTreeProject.removeNode(replicateNode);
							}

						}
					}

				}
				if (!hasOneReplicate) {
					cpExpIterator.remove();
					jTreeProject.removeNode(experimentNode);

				}
			}

			jTreeProject.selectNode(nodoRaizMSI);
			jTreeProject.expandNode(nodoRaizMSI);
			saved = true;
		} catch (JAXBException e) {
			log.warn(e.getMessage());
			throw new MiapeDataInconsistencyException(
					"Error loading " + projectFile.getAbsolutePath() + " config file: " + e.getMessage());
		}
	}

	private boolean getAnnotateProteinsInUniprot() {
		// TODO
		// SALVA: get this from interface
		return true;
	}

	private void addExperimentToProjectTree(File projectFile) {
		try {
			final DefaultMutableTreeNode experimentListNode = jTreeProject.getRootNode();
			CPExperiment CPExperiment = ComparisonProjectFileUtil.getExperimentFromComparisonProjectFile(projectFile);
			CPExperiment.setCurated(true);
			CPExperimentList cpExperimentList = (CPExperimentList) experimentListNode.getUserObject();
			cpExperimentList.getCPExperiment().add(CPExperiment);

			String experimentName = CPExperiment.getName();

			jTextExperimentName.setText(experimentName);

			final DefaultMutableTreeNode experimentNode = jTreeProject.addNewNode(CPExperiment, experimentListNode);
			for (CPReplicate cpReplicate : CPExperiment.getCPReplicate()) {

				final DefaultMutableTreeNode replicateNode = jTreeProject.addNewNode(cpReplicate, experimentNode);
				if (cpReplicate.getCPMSIList() != null) {
					for (CPMSI cpMsi : cpReplicate.getCPMSIList().getCPMSI()) {
						// final DefaultMutableTreeNode miapeNode =
						jTreeProject.addNewNode(cpMsi, replicateNode);
						// cpMsi.setName(FilenameUtils.getName(
						// FileManager.getMiapeMSICuratedXMLFilePath(cpMsi.getId(),
						// experimentName, cpMsi.getName())));

					}
				}
			}
			jTreeProject.selectNode(experimentNode);
			jTreeProject.expandNode(experimentNode);
			saved = true;
		} catch (JAXBException e) {
			log.warn(e.getMessage());
			throw new MiapeDataInconsistencyException(
					"Error loading " + projectFile.getAbsolutePath() + " config file: " + e.getMessage());
		}
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

				Miape2ExperimentListDialog dialog = new Miape2ExperimentListDialog(null);
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
	private javax.swing.JButton jButtonAddCuratedExperiment;
	private javax.swing.JButton jButtonAddExperiment;
	private javax.swing.JButton jButtonAddManualList;
	private javax.swing.JButton jButtonAddReplicate;
	private javax.swing.JButton jButtonCancelLoading;
	private javax.swing.JButton jButtonDeleteNode;
	private javax.swing.JButton jButtonFinish;
	private javax.swing.JButton jButtonLoadSavedProject;
	private javax.swing.JButton jButtonRemoveCuratedExperiment;
	private javax.swing.JButton jButtonRemoveSavedProject;
	private javax.swing.JButton jButtonSave;
	private javax.swing.JButton jButtonStartLoading;
	private javax.swing.JComboBox<String> jComboBox1;
	private javax.swing.JComboBox<String> jComboBoxCuratedExperiments;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JLabel jLabel7;
	private javax.swing.JLabel jLabel8;
	private javax.swing.JLabel jLabelMIAPEMSINumber;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel10;
	private javax.swing.JPanel jPanel11;
	private javax.swing.JPanel jPanel12;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JPanel jPanel9;
	public javax.swing.JProgressBar jProgressBar;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JScrollPane jScrollPane3;
	private javax.swing.JScrollPane jScrollPane4;
	private javax.swing.JScrollPane jScrollPane5;
	private javax.swing.JTabbedPane jTabbedPane;
	private javax.swing.JTextArea jTextAreaStatus;
	private javax.swing.JTextField jTextExperimentName;
	private javax.swing.JTextField jTextFieldLabelNameTemplate;
	private javax.swing.JTextField jTextFieldLabelNumberTemplate;
	private javax.swing.JTextField jTextProjectName;
	private javax.swing.JTextField jTextReplicateName;
	private ExtendedJTree jTreeLocalMIAPEMSIs;

	private ExtendedJTree jTreeProject;
	private JButton jButtonGoToImport;
	private JButton jButtonHelp;
	private JButton jButtonClearProject2;

	// End of variables declaration//GEN-END:variables

	// @Override

	@Override
	public synchronized void propertyChange(PropertyChangeEvent evt) {
		if ("progress".equals(evt.getPropertyName())) {
			int progress = (Integer) evt.getNewValue();
			jProgressBar.setValue(progress);
			jProgressBar.setString(progress + "%");
			jProgressBar.setStringPainted(true);
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
		} else if (MiapeTreeIntegrityCheckerTask.INTEGRITY_OK.equals(evt.getPropertyName())) {
			this.enableStateKeeper.setToPreviousState(this);
			jProgressBar.setIndeterminate(false);

			try {
				save(false);
				if (finishPressed) {
					showChartManager();
				}
			} catch (IllegalMiapeArgumentException e) {
				setStatus(e.getMessage());
				return;
			}
		} else if (MiapeTreeIntegrityCheckerTask.INTEGRITY_START.equals(evt.getPropertyName())) {
			this.enableStateKeeper.keepEnableStates(this);
			this.enableStateKeeper.disable(this);
			jProgressBar.setIndeterminate(true);

		} else if (MiapeTreeIntegrityCheckerTask.INTEGRITY_ERROR.equals(evt.getPropertyName())) {
			jProgressBar.setIndeterminate(false);
			this.enableStateKeeper.setToPreviousState(this);
			String message = (String) evt.getNewValue();
			JOptionPane.showMessageDialog(this, message, "Error in comparison project", JOptionPane.OK_OPTION);
			appendStatus("Save canceled");
		} else if (LocalDataTreeLoaderTask.LOCAL_TREE_LOADER_FINISHED.equals(evt.getPropertyName())) {
			this.enableStateKeeper.setToPreviousState(this);
			Integer numLoaded = (Integer) evt.getNewValue();
			appendStatus(numLoaded + " datasets loaded.");
		} else if (LocalDataTreeLoaderTask.LOCAL_TREE_LOADER_ERROR.equals(evt.getPropertyName())) {
			this.enableStateKeeper.setToPreviousState(this);
			appendStatus("Error loading local datasets: " + evt.getNewValue());
		} else if (LocalDataTreeLoaderTask.LOCAL_TREE_LOADER_STARTS.equals(evt.getPropertyName())) {
			this.enableStateKeeper.keepEnableStates(this);
			this.enableStateKeeper.disable(this);
		}
		pack();
	}

	public void showChartManager() {
		boolean showChartManager = true;

		if (showChartManager) {
			setVisible(false);
			boolean resetErrorLoadingData = true;
			ChartManagerFrame chartManager = ChartManagerFrame.getInstance(this, currentCgfFile, resetErrorLoadingData);
			chartManager.setVisible(true);
		}
	}

	public void setCurrentCgfFile(File currentCgfFile) {
		this.currentCgfFile = currentCgfFile;
	}

	public void appendStatus(String notificacion) {
		jTextAreaStatus.append(notificacion + "\n");
		jTextAreaStatus.setCaretPosition(jTextAreaStatus.getText().length() - 1);

	}

	private void setStatus(String notificacion) {
		jTextAreaStatus.setText(notificacion + "\n");
		jTextAreaStatus.setCaretPosition(jTextAreaStatus.getText().length() - 1);

	}

	public boolean isCorrectlyInitialized() {
		return correctlyInitialized;
	}

	public JComboBox getCuratedComboBox() {
		return jComboBoxCuratedExperiments;
	}

	@Override
	public void setState(int state) {
		super.setState(state);
	}

	@Override
	public List<String> getHelpMessages() {
		String[] ret = {

				"Comparison Projects Manager", //
				"From here you can:", //
				"- load previously created comparison projects", //
				"- create new comparison projects,", //
				"", //
				"At the left panel you will see the datasets that are imported in the system. And at the right you will be able to edit the nodes of the Comparison Project Tree located at the center of the window.", //
				"<b>Load previously created comparison projects:</b>", //
				"1. Select a saved project from the dropdown menu at the top left of this window.", //
				"2. Click on <i>'Load'</i> button", //
				"3. The project will be loaded in the Comparison Project Tree, where can be edited if you want.", //
				"4. Click on <i>'Save project'</i> to save the project if it has been modified.", //
				"5. Click on <i>'Next'</i> in order to go to the Chart Viewer.", //
				"<b>Create new comparison projects</b>", //
				"In order to create a new comparison project, you have to build a Comparison Project Tree by adding the already imported datasets (at the left panel) and grouping them in nodes in a 3-level hierarquical tree. Individual datasets will be assigned to level 2 nodes. Level 1 nodes will collapse the information from individual datasets. Level 0 node or root of the Comparison Project Tree will collapse the information from all the datasets in the project.", //
				"The Chart Viewer will provide you the option to <b>switch between different levels of aggregation</b> of the data:", //
				"- <i>one single data series (level 0)</i>: a chart with just one data series which aggregates all the individual datasets,", //
				"- <i>one data series per level 1</i>: a chart with one data series per each of the level 1 nodes which aggregates all the individual datasets pending from that node, ", //
				"- <i>one data series per level 2</i>: a chart with one data series per each of the level 2 nodes which aggregates all the individual datasets pending from that node,", //
				"- <i>one separate chart per level 1</i>: this will generate a different chart per each one of the level 1 nodes. Each of these charts will contain a data series per level 2 nodes pending on that level 1 node.", //
				"<b>How to build or edit a comparison project:</b>", //
				"In order to create or edit a comparison project you will have to add new nodes and then add the datasets on these nodes.", //
				"1. Click on <i>'Clear comparison project'</i> button if you want to start a new project. Otherwise you can work over the default outline "
						+ "of the new project or over any other project. As long as you save it with a different name, you will not override the information of the original project.", //
				"2. Select the root node of the Comparison Project Tree.", //
				"3. Edit its name on the corresponding text box at the <i>'Edit'</i> panel at the right.", //
				"4. Select the root node of the Comparison Project Tree again.", //
				"5. Type a name for a new level 1 node in the corresponding text box and click on the corresponding <i>'Add'</i> button.", //
				"6. Select the level 1 node that you just created.", //
				"7. Type a name for a new level 2 node in the corresponding text box and click on the corresponding <i>'Add'</i> button.", //
				"8. Select the new level 2 node that you just created.", //
				"9. Explore your imported datasets with the panel at the left, and double click on the individual dataset that you want to assign to the "
						+ "selected level 2 node. You can add more than one dataset in a single level 2 node. ", //
				"10. Repeat steps 4-5 and 6-9 to include and organize all the datasets you want to explore and compare.", //
				"<b>Adding datasets using the Level 2 node name template:</b>", //
				"11. Alternatively to step 8, you can select a level 1 node and double click on one individual dataset. This will automatically "
						+ "create a new level 2 node with this dataset associated to it. The name of the level 2 node will be automatically generated by "
						+ "the <i>'Level 2 node name template'</i> (see description below).", //
				"12. Alternatively to step 9, you can also select the level 0 node and double click on one of the folders containing imported datasets."
						+ "A new level 1 node will be created containing all the individual datasets of the folder and the names of the level 2 nodes will be automatically generated by the "
						+ "<i>'Level 2 node name template'</i> (see description below).", // .",
																							// //
				"<b>Adding curated datasets into your comparison project:</b>", //
				"Alternatively to add any dataset from the imported datasets, you can also add <b>curated datasets</b> as individual datasets."
						+ " To create curated datasets, you have to do it on the next step, that is, the '<i>Chart Viewer</i>'"
						+ " where after applying some filters you can save the datasets as <i>'curated'</i>. "
						+ "To do that, select the curated dataset from the drop-down menu just below the <i>edit panel</i>, and click on the "
						+ "<i>'Add curated dataset'</i> button. this will add a new level 1 node to the Comparison Project Tree with a distinguisable star symbol.", //
				"<b>Saving your comparison project:</b>", //
				"Click on <i>'Save project'</i> button to save the project or click on <i>'Next'</i> "
						+ "button to save and go to the <i>Chart Viewer</i>.", //

				"<b>How to delete a node:</b>", //
				"To delete any node, just select it and click on <i>'Delete selected node'</i> button.", //

				"<b>The level 2 node name template:</b>", //
				"Located at the bottom rigth of this window, it is used to automatically assign consecutive numbers to level 2 node names with a predefined name.", //
				"This is specially helpfull when you add imported datasets following steps 11 or 12 where the level 2 nodes are automatically created and their names are created using the name template.", //
				"By typing a name like <i>'replicate'</i> and having the <i>'incrementing suffix'</i> as <i>'1'</i> will create level 2 nodes named as: <i>'replicate1'</i>, <i>'replicate2'</i>, <i>'replicate3'</i>, etc..."//
		};
		return Arrays.asList(ret);
	}

}