/*
 * Miape2ExperimentListDialog.java Created on __DATE__, __TIME__
 */

package org.proteored.pacom.analysis.gui;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jfree.ui.RefineryUtilities;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.interfaces.MiapeHeader;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeAPIWebserviceDelegate;
import org.proteored.pacom.analysis.conf.ExperimentAdapter;
import org.proteored.pacom.analysis.conf.ExperimentListAdapter;
import org.proteored.pacom.analysis.conf.jaxb.CPExperiment;
import org.proteored.pacom.analysis.conf.jaxb.CPExperimentList;
import org.proteored.pacom.analysis.conf.jaxb.CPMS;
import org.proteored.pacom.analysis.conf.jaxb.CPMSI;
import org.proteored.pacom.analysis.conf.jaxb.CPMSIList;
import org.proteored.pacom.analysis.conf.jaxb.CPMSList;
import org.proteored.pacom.analysis.conf.jaxb.CPReplicate;
import org.proteored.pacom.analysis.gui.components.ExtendedJTree;
import org.proteored.pacom.analysis.gui.components.MyTreeRenderer;
import org.proteored.pacom.analysis.gui.tasks.ExternalDataTreeLoaderTask;
import org.proteored.pacom.analysis.gui.tasks.InitializeProjectComboBoxTask;
import org.proteored.pacom.analysis.gui.tasks.LocalDataTreeLoaderTask;
import org.proteored.pacom.analysis.gui.tasks.MiapeRetrieverManager;
import org.proteored.pacom.analysis.gui.tasks.MiapeRetrieverTask;
import org.proteored.pacom.analysis.gui.tasks.MiapeTreeIntegrityCheckerTask;
import org.proteored.pacom.analysis.util.FileManager;
import org.proteored.pacom.gui.ImageManager;
import org.proteored.pacom.gui.MainFrame;
import org.proteored.pacom.gui.tasks.TreeLoaderTask;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

/**
 *
 * @author __USER__
 */
public class Miape2ExperimentListDialog extends javax.swing.JFrame implements PropertyChangeListener {
	private static final int PROJECT_LEVEL = 1;
	private static final int EXPERIMENT_LEVEL = 2;
	private static final int REPLICATE_LEVEL = 3;
	private static final int MIAPE_LEVEL = 3;
	private static final int MIAPE_REPLICATE_LEVEL = 4;
	private static final int MIAPE_PROJECT_LEVEL = 2;
	private static final String DEFAULT_PROJECT_NAME = "Comparison project";
	private static final String DEFAULT_EXPERIMENT_NAME = "experiment 1";
	final static String MIAPE_ID_REGEXP = "Dataset_(?:MS|MSI)_(\\d+).*";
	final static String LOCAL_MIAPE_ID_REGEXP = "Dataset_(?:MS|MSI)_(\\d+)_.+$";
	final static String LOCAL_MIAPE_UNIQUE_NAME_REGEXP = "Dataset_(?:MS|MSI)_\\d+_(.*)";

	final static String MIAPE_PROJECT_NAME_REGEXP = "\\d+:\\s(.*)";
	private static final String MIAPE_LOCAL_PROJECT_NAME_REGEXP = "'(.*)'";

	private boolean saved = false;
	public TreeLoaderTask treeLoaderTask;
	public MiapeAPIWebserviceDelegate miapeAPIWebservice;
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");
	private static final String NO_CURATED_EXPERIMENTS_AVAILABLE = "No curated experiments available";
	private static Miape2ExperimentListDialog instance;
	// Associates MIAPE MSI IDs (key) with associated MIAPE MS IDs (values)

	// collection of threads that retrieve MIAPE MSI from server <miape id to
	// retrieve, miape
	// retriever>

	private File currentCgfFile;

	// hashmap to store the MIAPE documents that are retrieved <Identified,
	// FullPath to the file>
	// private static HashMap<Integer, String> miapeMSIsRetrieved = new
	// HashMap<Integer, String>();

	public static final String MESSAGE_SPLITTER = "****";
	public static final String SCAPED_MESSAGE_SPLITTER = "\\*\\*\\*\\*";
	public static final String DEFAULT_USER_NAME = "default_user";
	private final MainFrame parent;

	private boolean correctlyInitialized = false;
	private boolean finishPressed;
	private InitializeProjectComboBoxTask projectComboLoaderTask;
	private ExternalDataTreeLoaderTask externalDataTreeLoaderTask;
	private LocalDataTreeLoaderTask localDataTreeLoaderTask;

	@Override
	public void dispose() {

		// cancel tree loader task
		if (treeLoaderTask != null && treeLoaderTask.getState() == StateValue.STARTED) {
			boolean canceled = treeLoaderTask.cancel(true);
			while (!canceled) {
				canceled = treeLoaderTask.cancel(true);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				log.info("Waiting for tree loader closing");

			}
		}
		if (externalDataTreeLoaderTask != null && externalDataTreeLoaderTask.getState() == StateValue.STARTED) {
			boolean canceled = externalDataTreeLoaderTask.cancel(true);
			while (!canceled) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				log.info("Waiting for local tree loader closing");
				canceled = externalDataTreeLoaderTask.cancel(true);
			}
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
		try {
			UIManager.setLookAndFeel(new WindowsLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
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
		jTreeMIAPEMSIs.setCellRenderer(new MyTreeRenderer());
		jTreeLocalMIAPEMSIs.setCellRenderer(new MyTreeRenderer());
		jTreeManualMIAPEMSIs.setCellRenderer(new MyTreeRenderer());

		// fill external data tree
		fillExternalIdSetsTree();

		// fill local data tree
		fillLocalMIAPETree();

		try {
			if (MainFrame.userName != null && !"".equals(MainFrame.userName) && MainFrame.password != null
					&& !"".equals(MainFrame.password)) {
				fillTreeFromProteoRedRepository();
				// ((org.apache.axis.client.Stub)
				// miapeAPIWebservice.getMiapeAPIWebservice())
				// .setTimeout(WEBSERVICE_TIMEOUT);

			} else {
				jButtonCancelLoading.setEnabled(false);
				appendStatus("Offline mode");
				// appendStatus("MIAPE MSI documents cannot be loaded since the
				// user has not logged-in.");
				// appendStatus("Load a previously saved comparison project or
				// login again.");
			}
			// Initialize project tree
			initializeProjectTree(DEFAULT_PROJECT_NAME, DEFAULT_EXPERIMENT_NAME);
			correctlyInitialized = true;
		} catch (Exception e) {
			e.printStackTrace();
			log.warn(e.getMessage());
			appendStatus(e.getMessage());
		}

		// register as listener of all the MIAPEs that are being downloaded
		MiapeRetrieverManager.getInstance(MainFrame.userName, MainFrame.password).setListener(this);

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
			initializeComboBoxes();
			jButtonStartLoading.setEnabled(true);
			pack();
		}
		super.setVisible(b);
	}

	private void fillTreeFromProteoRedRepository() {
		if (MainFrame.userName != null && !"".equals(MainFrame.userName) && MainFrame.password != null
				&& !"".equals(MainFrame.password)) {
			if (treeLoaderTask != null)
				treeLoaderTask.cancel(true);
			treeLoaderTask = new TreeLoaderTask(null, jTreeMIAPEMSIs, null, null, MainFrame.userName,
					MainFrame.password);
			treeLoaderTask.addPropertyChangeListener(this);
			treeLoaderTask.execute();

			jButtonCancelLoading.setEnabled(true);
		}
	}

	private void fillExternalIdSetsTree() {
		externalDataTreeLoaderTask = new ExternalDataTreeLoaderTask(jTreeManualMIAPEMSIs);
		externalDataTreeLoaderTask.addPropertyChangeListener(this);
		externalDataTreeLoaderTask.execute();

	}

	private void fillLocalMIAPETree() {
		localDataTreeLoaderTask = new LocalDataTreeLoaderTask(jTreeLocalMIAPEMSIs);
		localDataTreeLoaderTask.addPropertyChangeListener(this);
		localDataTreeLoaderTask.execute();

	}

	public void cleanTrees() {
		DefaultMutableTreeNode nodoRaiz = new DefaultMutableTreeNode("No projects found");
		DefaultTreeModel modeloArbol = new DefaultTreeModel(nodoRaiz);
		jTreeMIAPEMSIs.setModel(modeloArbol);
		jTreeProject.setModel(modeloArbol);
		jTreeManualMIAPEMSIs.setModel(modeloArbol);
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
		jButtonClearProjectTree.setIcon(ImageManager.getImageIcon(ImageManager.CLEAR));
		jButtonClearProjectTree.setPressedIcon(ImageManager.getImageIcon(ImageManager.CLEAR_CLICKED));
		jButtonDeleteNode.setIcon(ImageManager.getImageIcon(ImageManager.DELETE));
		jButtonDeleteNode.setPressedIcon(ImageManager.getImageIcon(ImageManager.DELETE_CLICKED));
		jButtonRemoveCuratedExperiment.setIcon(ImageManager.getImageIcon(ImageManager.DELETE));
		jButtonRemoveCuratedExperiment.setPressedIcon(ImageManager.getImageIcon(ImageManager.DELETE_CLICKED));

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
		jTreeMIAPEMSIs = new ExtendedJTree();
		jPanel12 = new javax.swing.JPanel();
		jScrollPane5 = new javax.swing.JScrollPane();
		jTreeLocalMIAPEMSIs = new ExtendedJTree();
		jPanel11 = new javax.swing.JPanel();
		jScrollPane4 = new javax.swing.JScrollPane();
		jTreeManualMIAPEMSIs = new ExtendedJTree();
		jLabel8 = new javax.swing.JLabel();
		jButtonAddManualList = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		jScrollPane2 = new javax.swing.JScrollPane();
		jTreeProject = new ExtendedJTree();
		jPanel4 = new javax.swing.JPanel();
		jScrollPane3 = new javax.swing.JScrollPane();
		jTextAreaStatus = new javax.swing.JTextArea();
		jProgressBar = new javax.swing.JProgressBar();
		jPanel6 = new javax.swing.JPanel();
		jLabel5 = new javax.swing.JLabel();
		jLabelMIAPEMSINumber = new javax.swing.JLabel();
		jButtonCancelLoading = new javax.swing.JButton();
		jButtonStartLoading = new javax.swing.JButton();
		jPanel7 = new javax.swing.JPanel();
		jLabel7 = new javax.swing.JLabel();
		jComboBox1 = new javax.swing.JComboBox();
		jButtonLoadSavedProject = new javax.swing.JButton();
		jButtonRemoveSavedProject = new javax.swing.JButton();
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
		jButtonClearProjectTree = new javax.swing.JButton();
		jPanel8 = new javax.swing.JPanel();
		jTextFieldLabelNameTemplate = new javax.swing.JTextField();
		jLabel4 = new javax.swing.JLabel();
		jLabel6 = new javax.swing.JLabel();
		jTextFieldLabelNumberTemplate = new javax.swing.JTextField();
		jPanel9 = new javax.swing.JPanel();
		jComboBoxCuratedExperiments = new javax.swing.JComboBox();
		jButtonAddCuratedExperiment = new javax.swing.JButton();
		jButtonRemoveCuratedExperiment = new javax.swing.JButton();
		jPanel5 = new javax.swing.JPanel();
		jButtonFinish = new javax.swing.JButton();
		jButtonSave = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Inspection projects manager");

		jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"Individual datasets"));

		jTabbedPane.setToolTipText("Input datasets stored in local system");

		jPanel10.setToolTipText("Input datasets in remote repository");

		jTreeMIAPEMSIs.setAutoscrolls(true);
		jTreeMIAPEMSIs.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jTreeMIAPEMSIsMouseClicked(evt);
			}
		});
		jScrollPane1.setViewportView(jTreeMIAPEMSIs);

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
		if (!MainFrame.localWorkflow) {
			jTabbedPane.addTab("Remote data", jPanel10);
		}
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

		jTreeManualMIAPEMSIs.setAutoscrolls(true);
		jTreeManualMIAPEMSIs.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jTreeManualMIAPEMSIsMouseClicked(evt);
			}
		});
		jScrollPane4.setViewportView(jTreeManualMIAPEMSIs);

		jLabel8.setText("Add new protein lists:");

		jButtonAddManualList.setText("Add");
		jButtonAddManualList.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonAddManualListActionPerformed(evt);
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

		jTabbedPane.addTab("External protein lists", jPanel11);

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup()
						.addComponent(jTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
						.addContainerGap()));

		jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"Inspection Project"));

		jTreeProject.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jTreeProjectMouseClicked(evt);
			}
		});
		jScrollPane2.setViewportView(jTreeProject);

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup().addContainerGap()
						.addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
						.addContainerGap()));
		jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup()
						.addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
						.addContainerGap()));

		jPanel4.setBorder(
				javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Status"));

		jTextAreaStatus.setColumns(20);
		jTextAreaStatus.setLineWrap(true);
		jTextAreaStatus.setRows(5);
		jTextAreaStatus.setWrapStyleWord(true);
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
				jButtonCancelLoadingActionPerformed(evt);
			}
		});

		jButtonStartLoading.setText("Reload input data");
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

		jLabel1.setText("Project Name:");

		jTextProjectName.setToolTipText("Name of the inspection project");
		jTextProjectName.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyReleased(java.awt.event.KeyEvent evt) {
				jTextProjectNameKeyReleased(evt);
			}
		});

		jLabel2.setText("Level 1 Name (i.e. experiment / biological replicate):");
		jLabel2.setToolTipText(
				"<html>\nThe level 1 can represent an experiment or simply a <br>\ncompilation of peptide and protein identification sets.\n</html>");

		jTextExperimentName.setToolTipText(
				"<html> The level 1 can represent an experiment or simply a <br> compilation of peptide and protein identification sets. </html>");
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

		jButtonAddExperiment.setText("Add");
		jButtonAddExperiment.setToolTipText("Click here to add a new level 1 node to the inspection tree.");
		jButtonAddExperiment.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonAddExperimentActionPerformed(evt);
			}
		});

		jLabel3.setText("Level 2 Name (i.e. technical replicate/fraction/band):");
		jLabel3.setToolTipText(
				"<html>\nThe level 2 can represent a peptide and protein identification set comming from:<br>\n<ul>\n<li>a technical/biological replicate,</li>\n<li>a fraction in a LC separation,</li>\n<li>a gel spot,</li>\n<li>or a gel band.</li>\n</html>");

		jTextReplicateName.setToolTipText(
				"<html> The level 2 can represent a peptide and protein identification set comming from:<br> <ul> <li>a technical/biological replicate,</li> <li>a fraction in a LC separation,</li> <li>a gel spot,</li> <li>or a gel band.</li> </html>");
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

		jButtonAddReplicate.setText("Add");
		jButtonAddReplicate.setToolTipText("Click here to add a new level 2 node to the inspection tree.");
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

		jButtonClearProjectTree.setText("Clear project definition");
		jButtonClearProjectTree.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonClearProjectTreeActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
		jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel3Layout.createSequentialGroup().addContainerGap().addGroup(jPanel3Layout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
								jPanel3Layout.createSequentialGroup()
										.addComponent(jTextExperimentName, javax.swing.GroupLayout.DEFAULT_SIZE, 332,
												Short.MAX_VALUE)
										.addGap(6, 6, 6).addComponent(jButtonAddExperiment))
						.addComponent(jLabel3)
						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
								jPanel3Layout.createSequentialGroup()
										.addComponent(jTextReplicateName, javax.swing.GroupLayout.DEFAULT_SIZE, 331,
												Short.MAX_VALUE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jButtonAddReplicate))
						.addGroup(jPanel3Layout.createSequentialGroup().addComponent(jButtonDeleteNode)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 99,
										Short.MAX_VALUE)
								.addComponent(jButtonClearProjectTree))
						.addComponent(jLabel1).addComponent(jLabel2)
						.addComponent(jTextProjectName, javax.swing.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE))
						.addContainerGap()));
		jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel3Layout.createSequentialGroup().addComponent(jLabel1)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jTextProjectName, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel2)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jTextExperimentName, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jButtonAddExperiment))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel3)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jButtonAddReplicate).addComponent(jTextReplicateName,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jButtonDeleteNode).addComponent(jButtonClearProjectTree))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"Label generator template"));
		jPanel8.setToolTipText(
				"<html>\nThe label generator template defines which names will be<br>\nautomatically assigned to the Level 2 nodes<br> (replicates/fractions/bands)</html>");

		jTextFieldLabelNameTemplate.setText("replicate:");
		jTextFieldLabelNameTemplate.setToolTipText(
				"<html>The prefix of the name of the 'dataset'<br>\nwhen adding a new dataset to the comparison project.</html>");

		jLabel4.setText("Name:");
		jLabel4.setToolTipText(
				"<html>The prefix of the name of the 'dataset'<br> when adding a new dataset to the comparison project.</html>");

		jLabel6.setText("Number:");
		jLabel6.setToolTipText(
				"<html>The sufix of the name of the 'dataset'<br>\nwhen adding a new dataset to the comparison project.<br>\nIf this text is a number, it will be increment in sucessive addings.</html>");

		jTextFieldLabelNumberTemplate.setText("1");
		jTextFieldLabelNumberTemplate.setToolTipText(
				"<html>The sufix of the name of the 'dataset'<br> when adding a new dataset to the comparison project.<br> If this text is a number, it will be increment in sucessive addings.</html>");

		javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
		jPanel8.setLayout(jPanel8Layout);
		jPanel8Layout
				.setHorizontalGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanel8Layout.createSequentialGroup().addContainerGap().addComponent(jLabel4)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jTextFieldLabelNameTemplate, javax.swing.GroupLayout.PREFERRED_SIZE, 107,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGap(18, 18, 18).addComponent(jLabel6)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jTextFieldLabelNumberTemplate, javax.swing.GroupLayout.PREFERRED_SIZE,
										105, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addContainerGap(83, Short.MAX_VALUE)));
		jPanel8Layout
				.setVerticalGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanel8Layout.createSequentialGroup().addGroup(jPanel8Layout
								.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel4)
								.addComponent(jTextFieldLabelNameTemplate, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabel6).addComponent(jTextFieldLabelNumberTemplate,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE))
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"Load curated experiments"));
		jPanel9.setToolTipText(
				"<html>\n<b>Curated experiments<b/> are created in the Charts<br> viewer, usually after appling some filters.<br> This curated projects are lighter than normal projects<br> since filtered-out data is discarted and is not loaded.</html>");

		jComboBoxCuratedExperiments
				.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No curated experiments available" }));
		jComboBoxCuratedExperiments.setToolTipText(
				"<html>\n<b>Curated experiments<b/> are created in the MIAPE Extractor Charts<br> viewer, usually after applying some filters.<br> This curated projects are lighter than normal projects<br> since filtered-out data is discarted and is not loaded. </html>");
		jComboBoxCuratedExperiments.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jComboBoxCuratedExperimentsActionPerformed(evt);
			}
		});

		jButtonAddCuratedExperiment.setText("Add curated experiment");
		jButtonAddCuratedExperiment.setToolTipText(
				"<html>\nClick to add a <b>curated experiment</b> to the inspection project.<br>\nCurated experiments are created in the Data Comparison and Inspection Charts<br>\nviewer, usually after applying some filters.<br>\nThis curated projects are lighter than normal projects<br>\nsince filtered-out data is discarted and is not loaded.\n</html>");
		jButtonAddCuratedExperiment.setEnabled(false);
		jButtonAddCuratedExperiment.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonAddCuratedExperimentActionPerformed(evt);
			}
		});

		jButtonRemoveCuratedExperiment.setText("Remove curated experiment");
		jButtonRemoveCuratedExperiment.setToolTipText(
				"<html>\nClick to remove the selected curated experiment.<br>\nCurated experiments are created in the Data Comparison and Inspection Charts<br>\nviewer, usually after applying some filters.<br>\nThis curated projects are lighter than normal projects<br>\nsince filtered-out data is discarted and is not loaded.\n</html>");
		jButtonRemoveCuratedExperiment.setEnabled(false);
		jButtonRemoveCuratedExperiment.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonRemoveCuratedExperimentActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
		jPanel9.setLayout(jPanel9Layout);
		jPanel9Layout.setHorizontalGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel9Layout.createSequentialGroup().addContainerGap().addGroup(jPanel9Layout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addComponent(jComboBoxCuratedExperiments, 0, 393, Short.MAX_VALUE)
						.addGroup(jPanel9Layout.createSequentialGroup().addComponent(jButtonAddCuratedExperiment)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jButtonRemoveCuratedExperiment)))
						.addContainerGap()));
		jPanel9Layout.setVerticalGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel9Layout.createSequentialGroup()
						.addComponent(jComboBoxCuratedExperiments, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jButtonAddCuratedExperiment).addComponent(jButtonRemoveCuratedExperiment))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

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
		jButtonSave.setToolTipText("Click to save current inspection project");
		jButtonSave.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonSaveActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
		jPanel5.setLayout(jPanel5Layout);
		jPanel5Layout.setHorizontalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel5Layout.createSequentialGroup().addContainerGap().addComponent(jButtonSave)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 247, Short.MAX_VALUE)
						.addComponent(jButtonFinish).addContainerGap()));
		jPanel5Layout.setVerticalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel5Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jButtonSave).addComponent(jButtonFinish))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				javax.swing.GroupLayout.Alignment.TRAILING,
				layout.createSequentialGroup().addContainerGap().addGroup(layout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
						.addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(layout.createSequentialGroup()
												.addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE))
										.addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
						.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(layout.createSequentialGroup()
										.addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		pack();
	}// </editor-fold>
		// GEN-END:initComponents

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
				String miapeName = FilenameUtils
						.getBaseName(jTreeLocalMIAPEMSIs.getStringFromSelection(LOCAL_MIAPE_UNIQUE_NAME_REGEXP));
				// get local project name
				String projectName = jTreeLocalMIAPEMSIs
						.getStringFromParentOfSelection(MIAPE_LOCAL_PROJECT_NAME_REGEXP);
				// start retrieving
				final Integer msi_id = Integer.valueOf(miapeID);

				Integer ms_id = getAssociatedLocalMIAPEMSId(msi_id, projectName, miapeName);
				// if a replicate is selected
				if (jTreeProject.isOnlyOneNodeSelected(REPLICATE_LEVEL)) {

					DefaultMutableTreeNode replicateNode = jTreeProject.getSelectedNode();
					CPReplicate cpReplicate = (CPReplicate) replicateNode.getUserObject();
					// String miapeNodeName = getMIAPENodeNameFromTemplate();

					String miapeNodeName = FileManager.getMiapeMSILocalFileName(Integer.valueOf(miapeID), miapeName);

					CPMSI cpMsi = new CPMSI();
					cpMsi.setId(msi_id);
					cpMsi.setName(miapeNodeName);
					cpMsi.setLocal(true);
					cpMsi.setLocalProjectName(projectName);
					if (ms_id != null) {
						cpMsi.setMiapeMsIdRef(ms_id);
						CPMS cpMS = new CPMS();
						cpMS.setId(ms_id);
						cpMS.setName(FilenameUtils.getBaseName(FileManager.getMiapeMSLocalFileName(ms_id, null)));
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
					jTreeProject.addNewNode(cpMsi, replicateNode);
					// scroll to experiment node to let add more replicates
					jTreeProject.scrollToNode(replicateNode);
					saved = false;
					// }
				} else if (jTreeProject.isOnlyOneNodeSelected(EXPERIMENT_LEVEL)) {
					DefaultMutableTreeNode experimentNode = jTreeProject.getSelectedNode();
					CPExperiment cpExp = (CPExperiment) experimentNode.getUserObject();
					if (cpExp.isCurated())
						throw new IllegalMiapeArgumentException(
								"A non curated dataset cannot be added to a curated experiment");
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
						cpMS.setName(FilenameUtils.getBaseName(FileManager.getMiapeMSLocalFileName(ms_id, null)));
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
					jTreeProject.addNewNode(cpMsi, replicateNode);
					// scroll to experiment node to let add more replicates
					jTreeProject.scrollToNode(experimentNode);
					saved = false;
				} else {
					throw new IllegalMiapeArgumentException(
							"Select an experiment or fraction/band/replicate level in the inspection project.");
				}
				// increment number template
				incrementSuffixIfNumber();
				// expand tree
				// jTreeProject.expandAll();

			} else if (jTreeLocalMIAPEMSIs.isOnlyOneNodeSelected(MIAPE_PROJECT_LEVEL)) {
				// if the user clicks on a MIAPE PROJECT
				log.info("Miape local project selected");
				final int numMIAPEsInMIAPEProject = jTreeLocalMIAPEMSIs.getSelectedNode().getChildCount();
				if (numMIAPEsInMIAPEProject > 0) {

					// IF the node 0 is selected in the inspection project
					String localProjectName = jTreeLocalMIAPEMSIs
							.getStringFromSelection(MIAPE_LOCAL_PROJECT_NAME_REGEXP);
					final int selectedOption = JOptionPane.showConfirmDialog(this,
							"<html>Do you want to add all datasets in the project to a new level 1 node called '"
									+ localProjectName + "' in the inspection project?</html>",
							"Add datasets to inspection project", JOptionPane.YES_NO_CANCEL_OPTION);
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
							String miapeName = FilenameUtils.getBaseName(ExtendedJTree
									.getString(LOCAL_MIAPE_UNIQUE_NAME_REGEXP, (String) miapeMSIChild.getUserObject()));
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
								cpMS.setName(
										FilenameUtils.getBaseName(FileManager.getMiapeMSLocalFileName(ms_id, null)));
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
							jTreeProject.addNewNode(cpMsi, replicateNode);
							saved = false;
							// increment number template
							incrementSuffixIfNumber();
						}
						cpExpList.getCPExperiment().add(cpExp);
						jTreeProject.scrollToNode(projectNode);
						// expand tree
						// jTreeProject.expandAll();
					}

				} else {
					throw new IllegalMiapeArgumentException("The MIAPE Project has not MIAPE MSIs!");
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
			log.info("referenced MIAPE MS: " + miapeMSIHeader.getMiapeRef());
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
					"<html>The inspection project will be deleted.<br>Are you sure?</html>",
					"Delete inspection project", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (option == JOptionPane.YES_OPTION) {
				boolean deleted = FileManager.removeProjectXMLFile(projectName);
				if (deleted) {
					appendStatus("Inspection project '" + projectName + "' deleted");
					initializeComboBoxes();
				}
			}
		}
	}

	private void jTreeManualMIAPEMSIsMouseClicked(java.awt.event.MouseEvent evt) {
		try {
			manualMiapeTreeClicked(evt);
		} catch (IllegalMiapeArgumentException e) {
			appendStatus("Error: " + e.getMessage());
		}

	}

	private void manualMiapeTreeClicked(MouseEvent evt) {
		// double click
		if (evt.getClickCount() == 2) {
			if (jTreeManualMIAPEMSIs.isOnlyOneNodeSelected(EXPERIMENT_LEVEL)) {
				// get MIAPE ID
				String miapeName = jTreeManualMIAPEMSIs.getStringFromSelection();

				// if a relicate is selected
				if (jTreeProject.isOnlyOneNodeSelected(REPLICATE_LEVEL)) {

					DefaultMutableTreeNode replicateNode = jTreeProject.getSelectedNode();
					CPReplicate cpReplicate = (CPReplicate) replicateNode.getUserObject();
					// String miapeNodeName = getMIAPENodeNameFromTemplate();

					CPMSI cpMsi = new CPMSI();
					cpMsi.setId(-1);
					cpMsi.setManuallyCreated(true);
					cpMsi.setName(miapeName);
					CPMSIList cpMsiList = null;
					if (cpReplicate.getCPMSIList() == null)
						cpMsiList = new CPMSIList();
					else
						cpMsiList = cpReplicate.getCPMSIList();
					cpMsiList.getCPMSI().add(cpMsi);
					cpReplicate.setCPMSIList(cpMsiList);
					jTreeProject.addNewNode(cpMsi, replicateNode);
					// scroll to experiment node to let add more replicates
					jTreeProject.scrollToNode(replicateNode);
					saved = false;
					// }
				} else if (jTreeProject.isOnlyOneNodeSelected(EXPERIMENT_LEVEL)) {
					DefaultMutableTreeNode experimentNode = jTreeProject.getSelectedNode();
					CPExperiment cpExp = (CPExperiment) experimentNode.getUserObject();
					if (cpExp.isCurated())
						throw new IllegalMiapeArgumentException(
								"A non curated MIAPE MSI cannot be added to a curated experiment");
					// add replicate node
					String defaultReplicateName = getMIAPENodeNameFromTemplate();
					CPReplicate cpRep = new CPReplicate();
					cpRep.setName(defaultReplicateName);

					cpExp.getCPReplicate().add(cpRep);
					final DefaultMutableTreeNode replicateNode = jTreeProject.addNewNode(cpRep, experimentNode);
					// add miape node
					CPMSI cpMsi = new CPMSI();
					cpMsi.setId(-1);
					cpMsi.setManuallyCreated(true);
					cpMsi.setName(miapeName);
					CPMSIList cpMsiList = new CPMSIList();
					cpMsiList.getCPMSI().add(cpMsi);
					cpRep.setCPMSIList(cpMsiList);
					jTreeProject.addNewNode(cpMsi, replicateNode);
					// scroll to experiment node to let add more replicates
					jTreeProject.scrollToNode(experimentNode);
					saved = false;
				} else {
					throw new IllegalMiapeArgumentException(
							"Select an experiment or fraction/band/replicate level in the inspection project.");
				}
				// increment number template
				incrementSuffixIfNumber();
				// expand tree
				// jTreeProject.expandAll();

			}
		}

	}

	private void jButtonAddManualListActionPerformed(java.awt.event.ActionEvent evt) {

		addManualIdSet();

	}

	private void addManualIdSet() {

		// show manual id set creator dialog
		ManualIdentificationSetCreatorDialog dialog = ManualIdentificationSetCreatorDialog.getInstance(this);
		dialog.setVisible(true);
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
				"Add MIAPEs to inspection project", JOptionPane.YES_NO_CANCEL_OPTION);
		if (selectedOption == JOptionPane.YES_OPTION) {
			log.info("Removing curated experiment files ... " + curatedExperimentName);
			jButtonRemoveCuratedExperiment.setEnabled(false);
			boolean removed = FileManager.removeCuratedExperimentFiles(curatedExperimentName);
			if (removed)
				appendStatus("Curated experiment '" + curatedExperimentName + "' removed succesfully");
			else
				appendStatus("Some error occurred while removing curated experiment '" + curatedExperimentName + "'");

			projectComboLoaderTask = new InitializeProjectComboBoxTask(this);
			projectComboLoaderTask.addPropertyChangeListener(this);
			projectComboLoaderTask.execute();
		}
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

	private void jButtonFinishActionPerformed(java.awt.event.ActionEvent evt) {
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
		if (treeLoaderTask != null && treeLoaderTask.getState().equals(StateValue.DONE)) {
			treeLoaderTask.cancel(true);
			log.info("Cancelling miape msi tree loading");
		}
		log.info("Starting miape msi tree loading");
		fillTreeFromProteoRedRepository();
		fillExternalIdSetsTree();
		fillLocalMIAPETree();
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

	private void jButtonCancelLoadingActionPerformed(java.awt.event.ActionEvent evt) {
		if (treeLoaderTask != null)
			treeLoaderTask.cancel(true);
		setStatus("MIAPE loading canceled");
		// this.cancelRetrievingTasks();
		// this.disableControls(true);
		enableCancelTreeLoadinButton(false);

		jProgressBar.setValue(0);
		jProgressBar.setStringPainted(false);
		jProgressBar.setIndeterminate(false);
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
		cancelRetrievingTasks();
	}

	private void cancelRetrievingTasks() {
		// Cancel all pending tasks
		MiapeRetrieverManager.getInstance(MainFrame.userName, MainFrame.password).cancelAll();
		// enable controls

	}

	public void initializeComboBoxes() {
		projectComboLoaderTask = new InitializeProjectComboBoxTask(this);
		projectComboLoaderTask.execute();
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
				appendStatus("Project '" + projectName + "' has not found in the projects folder");
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

	private void jTreeMIAPEMSIsMouseClicked(java.awt.event.MouseEvent evt) {
		try {
			miapeTreeClicked(evt);
		} catch (IllegalMiapeArgumentException e) {
			appendStatus("Error: " + e.getMessage());
		}

	}

	private void miapeTreeClicked(MouseEvent evt) {
		// double click
		if (evt.getClickCount() == 2) {
			if (jTreeMIAPEMSIs.isOnlyOneNodeSelected(MIAPE_LEVEL)) {
				// get MIAPE ID
				String miapeID = jTreeMIAPEMSIs.getStringFromSelection(MIAPE_ID_REGEXP);

				// start retrieving
				final Integer id = Integer.valueOf(miapeID);
				startMIAPEMSIRetrieving(id);

				// if a relicate is selected
				if (jTreeProject.isOnlyOneNodeSelected(REPLICATE_LEVEL)) {

					DefaultMutableTreeNode replicateNode = jTreeProject.getSelectedNode();
					CPReplicate cpReplicate = (CPReplicate) replicateNode.getUserObject();
					// String miapeNodeName = getMIAPENodeNameFromTemplate();

					String miapeNodeName = FileManager.getMiapeMSILocalFileName(Integer.valueOf(miapeID), null);

					CPMSI cpMsi = new CPMSI();
					cpMsi.setId(id);
					cpMsi.setName(miapeNodeName);
					CPMSIList cpMsiList = null;
					if (cpReplicate.getCPMSIList() == null)
						cpMsiList = new CPMSIList();
					else
						cpMsiList = cpReplicate.getCPMSIList();
					cpMsiList.getCPMSI().add(cpMsi);
					cpReplicate.setCPMSIList(cpMsiList);
					jTreeProject.addNewNode(cpMsi, replicateNode);
					// scroll to experiment node to let add more replicates
					jTreeProject.scrollToNode(replicateNode);
					saved = false;
					// }
				} else if (jTreeProject.isOnlyOneNodeSelected(EXPERIMENT_LEVEL)) {
					DefaultMutableTreeNode experimentNode = jTreeProject.getSelectedNode();
					CPExperiment cpExp = (CPExperiment) experimentNode.getUserObject();
					if (cpExp.isCurated())
						throw new IllegalMiapeArgumentException(
								"A non curated MIAPE MSI cannot be added to a curated experiment");
					// add replicate node
					String defaultReplicateName = getMIAPENodeNameFromTemplate();
					CPReplicate cpRep = new CPReplicate();
					cpRep.setName(defaultReplicateName);

					cpExp.getCPReplicate().add(cpRep);
					final DefaultMutableTreeNode replicateNode = jTreeProject.addNewNode(cpRep, experimentNode);
					// add miape node
					CPMSI cpMsi = new CPMSI();
					cpMsi.setId(id);

					cpMsi.setName(FilenameUtils
							.getBaseName(FileManager.getMiapeMSILocalFileName(Integer.valueOf(miapeID), null)));
					CPMSIList cpMsiList = new CPMSIList();
					cpMsiList.getCPMSI().add(cpMsi);
					cpRep.setCPMSIList(cpMsiList);
					jTreeProject.addNewNode(cpMsi, replicateNode);
					// scroll to experiment node to let add more replicates
					jTreeProject.scrollToNode(experimentNode);
					saved = false;
				} else {
					throw new IllegalMiapeArgumentException(
							"Select an experiment or fraction/band/replicate level in the inspection project.");
				}
				// increment number template
				incrementSuffixIfNumber();
				// expand tree
				// jTreeProject.expandAll();

			} else if (jTreeMIAPEMSIs.isOnlyOneNodeSelected(MIAPE_PROJECT_LEVEL)) {
				// if the user clicks on a MIAPE PROJECT
				log.info("Miape project selected");
				final int numMIAPEsInMIAPEProject = jTreeMIAPEMSIs.getSelectedNode().getChildCount();
				if (numMIAPEsInMIAPEProject > 0) {

					// IF the node 0 is selected in the inspection project
					String experimentName = jTreeMIAPEMSIs.getStringFromSelection(MIAPE_PROJECT_NAME_REGEXP);
					final int selectedOption = JOptionPane.showConfirmDialog(this,
							"<html>Do you want to add all MIAPEs in the MIAPE project to a new level 1 node called '"
									+ experimentName + "' in the inspection project?</html>",
							"Add MIAPEs to inspection project", JOptionPane.YES_NO_CANCEL_OPTION);
					if (selectedOption == JOptionPane.YES_OPTION) {
						DefaultMutableTreeNode projectNode = jTreeProject.getRootNode();
						CPExperimentList cpExpList = (CPExperimentList) projectNode.getUserObject();
						CPExperiment cpExp = new CPExperiment();
						cpExp.setName(experimentName);
						cpExp.setCurated(false);
						// Add a new experiment node
						final DefaultMutableTreeNode experimentNode = jTreeProject.addNewNode(cpExp, projectNode);

						jTextExperimentName.setText(experimentName);

						final Enumeration children = jTreeMIAPEMSIs.getSelectedNode().children();
						while (children.hasMoreElements()) {
							DefaultMutableTreeNode miapeMSIChild = (DefaultMutableTreeNode) children.nextElement();
							String miapeID = ExtendedJTree.getString(MIAPE_ID_REGEXP,
									(String) miapeMSIChild.getUserObject());
							// start retrieving
							startMIAPEMSIRetrieving(Integer.valueOf(miapeID));
							// add replicate node
							String replicateName = getMIAPENodeNameFromTemplate();
							CPReplicate cpRep = new CPReplicate();
							cpRep.setName(replicateName);

							cpExp.getCPReplicate().add(cpRep);
							DefaultMutableTreeNode replicateNode = jTreeProject.addNewNode(cpRep, experimentNode);
							// add miape node
							String miapeNodeName = FileManager.getMiapeMSILocalFileName(Integer.valueOf(miapeID), null);
							CPMSI cpMsi = new CPMSI();
							cpMsi.setId(Integer.valueOf(miapeID));
							cpMsi.setName(miapeNodeName);
							CPMSIList cpMsiList = new CPMSIList();
							cpMsiList.getCPMSI().add(cpMsi);
							cpRep.setCPMSIList(cpMsiList);
							jTreeProject.addNewNode(cpMsi, replicateNode);
							saved = false;
							// increment number template
							incrementSuffixIfNumber();
						}
						cpExpList.getCPExperiment().add(cpExp);
						jTreeProject.scrollToNode(projectNode);
						// expand tree
						// jTreeProject.expandAll();
					}

				} else {
					throw new IllegalMiapeArgumentException("The MIAPE Project has not MIAPE MSIs!");
				}
			}
		}

	}

	private String getMIAPENodeNameFromTemplate() {
		String prefix = jTextFieldLabelNameTemplate.getText();
		String sufix = jTextFieldLabelNumberTemplate.getText();

		String miapeNodeName = "";
		if (!"".equals(prefix) || !"".equals(sufix)) {
			miapeNodeName = prefix + sufix;
		} else {
			miapeNodeName = (String) jTreeMIAPEMSIs.getSelectedNode().getUserObject();
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
		jTextReplicateName.setText("");
		jTextReplicateName.setEnabled(false);
		jButtonAddReplicate.setEnabled(false);
	}

	private void disableExperimentControls() {
		jTextExperimentName.setText("");
		jTextExperimentName.setEnabled(false);
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
			// int selectedOption = JOptionPane.showConfirmDialog(this,
			// "<html>Before to save the comparison project, some aspect should
			// be checked:<br>"
			// + "<ul><li>In order to apply an FDR threshold, the MIAPE MSIs
			// belonging to the same<br>"
			// + "'level 1' node must have been searched by the same search
			// engine.</li>"
			// + "<li>The same name for different level 1 nodes is not
			// allowed.</li>"
			// + "<li>The same name for different level 2 nodes belonging to the
			// same level 1 node is not allowed.</ul><br>"
			// + "Do you want to check the integrity of your project
			// now?</html>",
			// "Check integrity of the project?", JOptionPane.YES_NO_OPTION);
			// if (selectedOption == JOptionPane.YES_OPTION) {
			MiapeTreeIntegrityCheckerTask integrityChecker = new MiapeTreeIntegrityCheckerTask(this, cpExpList, false);
			integrityChecker.addPropertyChangeListener(this);
			integrityChecker.execute();
			// } else {
			// MiapeTreeIntegrityCheckerTask integrityChecker = new
			// MiapeTreeIntegrityCheckerTask(this, null, false);
			// integrityChecker.addPropertyChangeListener(this);
			// integrityChecker.execute();
			//
			// }
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
										if (((cpMSI.isLocal() != null && !cpMSI.isLocal()) || cpMSI.isLocal() == null)
												&& MiapeRetrieverManager
														.getInstance(MainFrame.userName, MainFrame.password)
														.getMiapeAssociations().containsKey(cpMSI.getId())) {
											CPMS cpMS = new CPMS();
											final Integer idMiapeMS = MiapeRetrieverManager
													.getInstance(MainFrame.userName, MainFrame.password)
													.getMiapeAssociations().get(cpMSI.getId());
											cpMS.setId(idMiapeMS);
											cpMS.setName(FilenameUtils
													.getBaseName(FileManager.getMiapeMSLocalFileName(idMiapeMS, null)));
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
		log.info("Released:'" + newExpName + "'");
		log.info("Char = " + keyChar + " CODE = " + keyCode);
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
					if (jTreeMIAPEMSIs.isOnlyOneNodeSelected(MIAPE_LEVEL)) {
						int miapeID = Integer.valueOf(jTreeMIAPEMSIs.getStringFromSelection(MIAPE_ID_REGEXP));

						startMIAPEMSIRetrieving(miapeID);

						// get selected experiment node
						DefaultMutableTreeNode experimentTreeNode = jTreeProject.getSelectedNode();
						CPExperiment cpExp = (CPExperiment) experimentTreeNode.getUserObject();
						if (cpExp.isCurated())
							throw new IllegalMiapeArgumentException(
									"A non curated MIAPE MSI cannot be added to a curated experiment");
						final CPReplicate cpRep = new CPReplicate();
						cpRep.setName(replicateName);
						cpExp.getCPReplicate().add(cpRep);
						// add the replicate node on the experiment node
						final DefaultMutableTreeNode replicateTreeNode = jTreeProject.addNewNode(cpRep,
								experimentTreeNode);
						String miapeName = (String) jTreeMIAPEMSIs.getSelectedNode().getUserObject();
						CPMSI cpMSI = new CPMSI();
						cpMSI.setName(miapeName);
						cpMSI.setId(miapeID);
						if (cpRep.getCPMSIList() == null) {
							cpRep.setCPMSIList(new CPMSIList());
						}
						cpRep.getCPMSIList().getCPMSI().add(cpMSI);

						// add the miape node on the replicate node
						jTreeProject.addNewNode(cpMSI
						// getMIAPENodeName(String.valueOf(miapeID))
								, replicateTreeNode);
						jTreeProject.scrollToNode(experimentTreeNode);
						saved = false;
						// expand tree
						// jTreeProject.expandAll();
					} else {
						throw new IllegalMiapeArgumentException(
								"Select a MIAPE MSI to associate to a new level 2 node");
					}
				} else if (jTreeProject.isOnlyOneNodeSelected(REPLICATE_LEVEL)) {
					int miapeID = Integer.valueOf(jTreeMIAPEMSIs.getStringFromSelection(MIAPE_ID_REGEXP));

					startMIAPEMSIRetrieving(miapeID);

					// get selected experiment node
					DefaultMutableTreeNode replicateTreeNode = jTreeProject.getSelectedNode();
					CPReplicate cpRep = (CPReplicate) replicateTreeNode.getUserObject();

					// add the miape node on the replicate node
					String miapeNodeName = (String) jTreeMIAPEMSIs.getSelectedNode().getUserObject();
					CPMSI cpMSI = new CPMSI();
					cpMSI.setName(miapeNodeName);
					cpMSI.setId(miapeID);
					cpRep.getCPMSIList().getCPMSI().add(cpMSI);

					jTreeProject.addNewNode(cpMSI
					// getMIAPENodeName(String.valueOf(miapeID))
							, replicateTreeNode);
					jTreeProject.scrollToNode(replicateTreeNode);
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

	private void startMIAPEMSIRetrieving(int miapeID) {
		if (MainFrame.userName == null || MainFrame.password == null)
			return;
		// chek if the miape is already retrieved
		File file = new File(FileManager.getMiapeMSIXMLFileLocalPathFromMiapeInformation(null, miapeID, null));
		if (file.exists()) {
			log.info("MIAPE MSI " + miapeID + " found in local system");
			if (MiapeRetrieverManager.getInstance(MainFrame.userName, MainFrame.password).getSize() == 0) {
				jProgressBar.setIndeterminate(false);
				jProgressBar.setStringPainted(false);
			}
			// retrieve anyway because it will be located at local system and
			// associated MIAPE MS will be detected
			MiapeRetrieverManager.getInstance(MainFrame.userName, MainFrame.password).addRetrieving(miapeID, "MSI",
					this);
			return;
		}

		jProgressBar.setIndeterminate(true);

		// add to the list of runners
		final String executingTasks = MiapeRetrieverManager.getInstance(MainFrame.userName, MainFrame.password)
				.addRetrievingWithPriority(miapeID, "MSI", this);

		appendStatus("Downloading MIAPE MSI " + executingTasks + " from ProteoRed repository.");
		appendStatus("This task will be performed in backgroud.");

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
		CPExperiment cpExp = new CPExperiment();
		cpExp.setName(experimentName);
		cpExp.setCurated(false);
		if (!"".equals(experimentName)) {
			final DefaultTreeModel model = (DefaultTreeModel) jTreeProject.getModel();
			if (model != null) {
				DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) model.getRoot();
				final CPExperimentList cpExpList = (CPExperimentList) parentNode.getUserObject();
				cpExpList.getCPExperiment().add(cpExp);
				final DefaultMutableTreeNode newNode = jTreeProject.addNewNode(cpExp, parentNode);
				jTreeProject.scrollToNode(newNode);
				saved = false;
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

	private void initializeProjectTree(File projectFile) {
		CPExperimentList cpExperimentList = new ExperimentListAdapter(projectFile, getAnnotateProteinsInUniprot())
				.getCpExperimentList();
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
						if (cpMsi.isManuallyCreated() == null || !cpMsi.isManuallyCreated()) {
							if (cpMsi.isLocal() == null || !cpMsi.isLocal()) {
								boolean addNode = true;
								final Integer miapeMSIID = Integer.valueOf(cpMsi.getId());
								cpMsi.setName(FilenameUtils.getBaseName(
										FileManager.getMiapeMSILocalFileName(miapeMSIID, cpMsi.getName())));
								File file = null;
								if (curated) {
									file = new File(FileManager.getMiapeMSICuratedXMLFilePathFromFullName(
											cpExperiment.getName(), cpMsi.getName()));
									if (!file.exists())
										addNode = false;

								} else {
									file = new File(FileManager.getMiapeMSIXMLFileLocalPathFromMiapeInformation(cpMsi));
								}
								if (addNode) {
									jTreeProject.addNewNode(cpMsi, replicateNode);
									hasOneReplicate = true;
								} else {
									cpRepIterator.remove();
									jTreeProject.removeNode(replicateNode);
								}
								if (miapeMSIID > 0)
									startMIAPEMSIRetrieving(miapeMSIID);
								// if the file was created/modified before to 3
								// October
								// 2012:
								if (MainFrame.userName != null && MainFrame.password != null
										&& (!file.exists() || file.lastModified() < Long.valueOf("1349251004048")))
									MiapeRetrieverManager.getInstance(MainFrame.userName, MainFrame.password)
											.addRetrieving(miapeMSIID, "MSI", this);
							} else {
								// LOCAL MIAPE
								boolean addNode = true;
								if (curated) {
									File file = new File(FileManager.getMiapeMSICuratedXMLFilePathFromMiapeInformation(
											cpMsi.getLocalProjectName(), cpMsi.getId(), cpMsi.getName()));
									if (!file.exists())
										addNode = false;

								} else {
									File file = new File(
											FileManager.getMiapeMSIXMLFileLocalPathFromMiapeInformation(cpMsi));
									if (!file.exists())
										addNode = false;
								}
								if (addNode) {
									jTreeProject.addNewNode(cpMsi, replicateNode);
									hasOneReplicate = true;
								} else {
									cpRepIterator.remove();
									jTreeProject.removeNode(replicateNode);
								}
							}
						} else {
							// MANUALLY CREATED
							boolean addNode = true;

							File file = FileManager.getManualIdSetFile(cpMsi.getName());
							if (file == null || !file.exists())
								addNode = false;
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

			}
			if (!hasOneReplicate) {
				cpExpIterator.remove();
				jTreeProject.removeNode(experimentNode);

			}
		}

		jTreeProject.selectNode(nodoRaizMSI);
		saved = true;
	}

	private boolean getAnnotateProteinsInUniprot() {
		// TODO
		// SALVA: get this from interface
		return true;
	}

	private void addExperimentToProjectTree(File projectFile) {
		final DefaultMutableTreeNode experimentListNode = jTreeProject.getRootNode();
		CPExperiment CPExperiment = new ExperimentAdapter(projectFile, getAnnotateProteinsInUniprot())
				.getCpExperiment();
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

					final Integer miapeMSIID = Integer.valueOf(cpMsi.getId());
					File file = new File(FileManager.getMiapeMSICuratedXMLFilePathFromMiapeInformation(
							cpMsi.getLocalProjectName(), cpMsi.getId(), cpMsi.getName()));
					// if the file was created/modified before to 3 October
					// 2012:
					if (MainFrame.userName != null && MainFrame.password != null
							&& (!file.exists() || file.lastModified() < Long.valueOf("1349251004048")))
						MiapeRetrieverManager.getInstance(MainFrame.userName, MainFrame.password)
								.addRetrieving(miapeMSIID, "MSI", this);
				}
			}
		}
		jTreeProject.selectNode(experimentNode);
		saved = true;
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
	private javax.swing.JButton jButtonClearProjectTree;
	private javax.swing.JButton jButtonDeleteNode;
	private javax.swing.JButton jButtonFinish;
	private javax.swing.JButton jButtonLoadSavedProject;
	private javax.swing.JButton jButtonRemoveCuratedExperiment;
	private javax.swing.JButton jButtonRemoveSavedProject;
	private javax.swing.JButton jButtonSave;
	private javax.swing.JButton jButtonStartLoading;
	private javax.swing.JComboBox jComboBox1;
	private javax.swing.JComboBox jComboBoxCuratedExperiments;
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
	private ExtendedJTree jTreeMIAPEMSIs;
	private ExtendedJTree jTreeManualMIAPEMSIs;
	private ExtendedJTree jTreeProject;

	// End of variables declaration//GEN-END:variables

	// @Override
	private void setLoadedMSIDocumentsNumber(String number) {
		jLabelMIAPEMSINumber.setText(number);

	}

	// @Override
	private void disableControls(boolean b) {
		jButtonAddExperiment.setEnabled(b);
		jButtonAddReplicate.setEnabled(b);
		jButtonFinish.setEnabled(b);
		jButtonClearProjectTree.setEnabled(b);
		jButtonSave.setEnabled(b);
		jButtonDeleteNode.setEnabled(b);

		jTextExperimentName.setEnabled(b);
		jTextProjectName.setEnabled(b);
		jTextReplicateName.setEnabled(b);
		jTreeMIAPEMSIs.setEnabled(b);
		jTreeProject.setEnabled(b);
		jTreeManualMIAPEMSIs.setEnabled(b);

		jTextFieldLabelNameTemplate.setEnabled(b);
		jTextFieldLabelNumberTemplate.setEnabled(b);

		jButtonLoadSavedProject.setEnabled(b);

	}

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
		} else if (MiapeRetrieverTask.MIAPE_LOADER_ERROR.equals(evt.getPropertyName())) {
			String message = (String) evt.getNewValue();
			if (message.contains(MESSAGE_SPLITTER)) {
				String[] splitter = message.split(SCAPED_MESSAGE_SPLITTER);
				Integer miapeID = Integer.valueOf(splitter[0]);
				String miapeType = splitter[1];
				String messageError = splitter[2];
				appendStatus("Error loading MIAPE " + miapeType + " " + miapeID + ": " + messageError);

			} else {
				appendStatus("Error loading MIAPE document: " + message);
				appendStatus("Try again or contact to miape_support@proteored.org");
			}
			int size = MiapeRetrieverManager.getInstance(MainFrame.userName, MainFrame.password).getSize();
			if (size < 1)
				jProgressBar.setIndeterminate(false);
		} else if (MiapeRetrieverTask.MIAPE_LOADER_DONE.equals(evt.getPropertyName())) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Integer size = MiapeRetrieverManager.getInstance(MainFrame.userName, MainFrame.password).getSize();
			if (size < 1)
				jProgressBar.setIndeterminate(false);
			String message = (String) evt.getNewValue();
			if (message.contains(MiapeRetrieverTask.MESSAGE_SPLITTER)) {
				Object[] splitter = message.split(MiapeRetrieverTask.SCAPED_MESSAGE_SPLITTER);
				int miapeID = Integer.valueOf((String) splitter[0]);
				String miapeType = (String) splitter[1];

				size = MiapeRetrieverManager.getInstance(MainFrame.userName, MainFrame.password).getSize();
				if (size < 1)
					jProgressBar.setIndeterminate(false);
				appendStatus("MIAPE " + miapeType + " " + miapeID + " retrieving finished at "
						+ DateFormat.getInstance().format(new Date(System.currentTimeMillis())) + ". " + size
						+ " pending...");

				// execute other miape retriever if available
				if (size > 0) {
					final String executingTasks = MiapeRetrieverManager
							.getInstance(MainFrame.userName, MainFrame.password).enumerate();

					appendStatus("Currently downloading in background: " + executingTasks + " from MIAPE repository.");
					// this.appendStatus("This task will be performed in
					// backgroud.");
				}
			}
		} else if (MiapeTreeIntegrityCheckerTask.INTEGRITY_OK.equals(evt.getPropertyName())) {
			jProgressBar.setIndeterminate(false);
			try {
				save(false);
				if (finishPressed)
					showChartManager();
			} catch (IllegalMiapeArgumentException e) {
				setStatus(e.getMessage());
				return;
			}

		} else if (MiapeTreeIntegrityCheckerTask.INTEGRITY_ERROR.equals(evt.getPropertyName())) {
			jProgressBar.setIndeterminate(false);
			String message = (String) evt.getNewValue();
			JOptionPane.showMessageDialog(this, message, "Error in comparison project", JOptionPane.OK_OPTION);
			appendStatus("Save canceled");
		} else if (TreeLoaderTask.TREE_LOADER_MSI_NUMBER.equals(evt.getPropertyName())) {
			Integer num = (Integer) evt.getNewValue();
			log.debug(num + " datasets loaded");
			setLoadedMSIDocumentsNumber(String.valueOf(num));
		} else if (TreeLoaderTask.TREE_LOADER_STARTS.equals(evt.getPropertyName())) {
			log.info("Starting tree loader");
			// this.disableControls(false);
			enableCancelTreeLoadinButton(true);
			appendStatus("Loading datasets. Please wait...");
			// appendStatus("You can stop MIAPE MSI loading by clicking on
			// 'Stop' button.");
		} else if (TreeLoaderTask.TREE_LOADER_DONE.equals(evt.getPropertyName())) {
			log.info("Tree loader ended");
			// this.disableControls(true);
			this.setCursor(null); // turn off the wait cursor
			jProgressBar.setIndeterminate(false);
			enableCancelTreeLoadinButton(false);
		} else if (TreeLoaderTask.TREE_LOADER_CANCELED.equals(evt.getPropertyName())) {
			jButtonStartLoading.setEnabled(true);
			jButtonCancelLoading.setEnabled(false);
		} else if (ExternalDataTreeLoaderTask.EXTERNAL_TREE_LOADER_FINISHED.equals(evt.getPropertyName())) {
			Integer numLoaded = (Integer) evt.getNewValue();
			appendStatus(numLoaded + " external protein list loaded.");
		} else if (ExternalDataTreeLoaderTask.EXTERNAL_TREE_LOADER_ERROR.equals(evt.getPropertyName())) {
			appendStatus("Error loading external protein lists: " + evt.getNewValue());
		} else if (LocalDataTreeLoaderTask.LOCAL_TREE_LOADER_FINISHED.equals(evt.getPropertyName())) {
			Integer numLoaded = (Integer) evt.getNewValue();
			appendStatus(numLoaded + " imported datasets loaded.");
		} else if (LocalDataTreeLoaderTask.LOCAL_TREE_LOADER_ERROR.equals(evt.getPropertyName())) {
			appendStatus("Error loading local datasets: " + evt.getNewValue());
		}
		pack();
	}

	private void showChartManager() {
		boolean showChartManager = true;

		if (showChartManager) {
			setVisible(false);
			ChartManagerFrame chartManager = ChartManagerFrame.getInstance(this, currentCgfFile);
			chartManager.setErrorLoadingData(false);
			chartManager.setVisible(true);
		}
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

	public void addReloadLocalIdentificationSetsTree() {
		fillExternalIdSetsTree();
	}
}