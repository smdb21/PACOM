/*
 * Miape2ExperimentListDialog.java Created on __DATE__, __TIME__
 */

package org.proteored.pacom.analysis.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jfree.chart.ui.UIUtils;
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
import org.proteored.pacom.analysis.gui.components.AbstractExtendedJTree;
import org.proteored.pacom.analysis.gui.components.ComparisonProjectExtendedJTree;
import org.proteored.pacom.analysis.gui.components.ExtendedJTree;
import org.proteored.pacom.analysis.gui.components.MyDefaultTreeCellEditor;
import org.proteored.pacom.analysis.gui.components.MyProjectTreeNode;
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

import edu.scripps.yates.utilities.swing.ComponentEnableStateKeeper;

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
	final static String LOCAL_MIAPE_ID_REGEXP = "Dataset_(?:MS|MSI)_(\\d+)";
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
				} catch (final InterruptedException e) {
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
				} catch (final InterruptedException e) {
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

		jTextAreaStatus.setFont(new JTextField().getFont());

		// set renderer to Project JTree
		jTreeProject.setCellRenderer(new MyTreeRenderer());
		jTreeLocalMIAPEMSIs.setCellRenderer(new MyTreeRenderer());
		jTreeProject.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent evt) {
				log.debug("lead selection: " + evt.getNewLeadSelectionPath());
				log.debug("path: " + evt.getPath());
			}
		});

		// set custom tree editor

		final MyDefaultTreeCellEditor<MyProjectTreeNode> editor = new MyDefaultTreeCellEditor<MyProjectTreeNode>(
				jTreeProject, (DefaultTreeCellRenderer) jTreeProject.getCellRenderer());
		// editor.addCellEditorListener(new CellEditorListener() {
		//
		// @Override
		// public void editingStopped(ChangeEvent e) {
		// log.info("editingStopped " + e.getSource());
		// updateTextFieldsWithTree(editor.getLastUpdatedName());
		// }
		//
		// @Override
		// public void editingCanceled(ChangeEvent e) {
		// log.info("editingCanceled " + e.getSource());
		//
		// }
		// });
		jTreeProject.setCellEditor(editor);
		try {
			getContentPane().setLayout(new BorderLayout(0, 0));
			final JPanel northPanel = new JPanel();
			northPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
			northPanel.add(jPanel7);
			final GroupLayout gl_jPanel7 = new GroupLayout(jPanel7);
			gl_jPanel7.setHorizontalGroup(gl_jPanel7.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_jPanel7.createSequentialGroup().addGap(5)
							.addComponent(jComboBox1, GroupLayout.PREFERRED_SIZE, 248, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED).addComponent(jButtonLoadSavedProject).addGap(5)
							.addComponent(jButtonRemoveSavedProject)));
			gl_jPanel7.setVerticalGroup(gl_jPanel7.createParallelGroup(Alignment.LEADING).addGroup(gl_jPanel7
					.createSequentialGroup()
					.addGroup(gl_jPanel7.createParallelGroup(Alignment.LEADING)
							.addGroup(
									gl_jPanel7.createSequentialGroup().addGap(5).addComponent(jButtonLoadSavedProject))
							.addGroup(gl_jPanel7.createSequentialGroup().addGap(5)
									.addComponent(jButtonRemoveSavedProject))
							.addGroup(gl_jPanel7.createSequentialGroup().addGap(7).addComponent(jComboBox1,
									GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
			jPanel7.setLayout(gl_jPanel7);
			northPanel.add(jPanel6);
			jPanel6.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			jPanel6.add(jLabel5);
			jPanel6.add(jLabelMIAPEMSINumber);
			jPanel6.add(jButtonStartLoading);
			getContentPane().add(northPanel, BorderLayout.NORTH);
			getContentPane().add(jPanel4, BorderLayout.SOUTH);
			getContentPane().add(jPanel1, BorderLayout.WEST);
			jPanel1.setLayout(new GridLayout(0, 1, 0, 0));
			jPanel1.add(jScrollPane5);
			getContentPane().add(jPanel2, BorderLayout.CENTER);
			jPanel2.setLayout(new BorderLayout(0, 0));
			jPanel2.add(panel, BorderLayout.SOUTH);
			jPanel2.add(jScrollPane2);
			// getContentPane().add(jPanel7);
			final JPanel eastPanel = new JPanel();
			eastPanel.setPreferredSize(new Dimension(440, 390));
			eastPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			eastPanel.setAlignmentY(Component.TOP_ALIGNMENT);
			eastPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
			eastPanel.add(jPanel3);
			jPanel3.setLayout(new BorderLayout(0, 0));
			jPanel3.add(panel_1, BorderLayout.NORTH);
			jPanel3.add(panel_2, BorderLayout.SOUTH);

			final JPanel panelOptions = new JPanel();
			panelOptions.setAlignmentX(Component.LEFT_ALIGNMENT);
			panelOptions.setLayout(new BorderLayout(0, 0));
			panelOptions.add(jPanel8, BorderLayout.NORTH);
			final FlowLayout fl_jPanel8 = new FlowLayout(FlowLayout.LEFT, 5, 5);
			jPanel8.setLayout(fl_jPanel8);

			panel_5 = new JPanel();
			jPanel8.add(panel_5);
			jLabel4 = new javax.swing.JLabel();
			panel_5.add(jLabel4);

			jLabel4.setText("Node name:");
			jLabel4.setToolTipText(
					"<html>The name of the level 2 node.<br>\r\nWhen you add a new level 2 node by double clicking on an imported dataset while selecting a level 1 node, it will be named with this name.<br>\r\nIf an 'Incrementing suffix' number is stated, it will be added to the name.<br>\r\nThe name of the level 2 nodes will be editable by selecting them and <br>\r\nchanging the name in the textbox above..</html>");
			jTextFieldLabelNameTemplate = new javax.swing.JTextField();
			panel_5.add(jTextFieldLabelNameTemplate);
			jTextFieldLabelNameTemplate.setColumns(10);

			jTextFieldLabelNameTemplate.setText("rep:");
			jTextFieldLabelNameTemplate.setToolTipText(
					"<html>The name of the level 2 node.<br>\r\nWhen you add a new level 2 node by double clicking on an imported dataset while selecting a level 1 node, it will be named with this name.<br>\r\nIf an 'Incrementing suffix' number is stated, it will be added to the name.<br>\r\nThe name of the level 2 nodes will be editable by selecting them and <br>\r\nchanging the name in the textbox above..</html>");
			jLabel6 = new javax.swing.JLabel();
			panel_5.add(jLabel6);

			jLabel6.setText("Incrementing suffix:");
			jLabel6.setToolTipText(
					"<html>The sufix of the name of the level 2 node.<br>\r\nWhen you add a new level 2 node by double clicking on an imported dataset while selecting a level 1 node, it will be named with the name in 'Node name' plus this suffix that will be automatically incremented any time<br>\r\nyou add a new level 2 node.</html>");
			jTextFieldLabelNumberTemplate = new javax.swing.JTextField();
			panel_5.add(jTextFieldLabelNumberTemplate);
			jTextFieldLabelNumberTemplate.setColumns(3);

			jTextFieldLabelNumberTemplate.setText("1");
			jTextFieldLabelNumberTemplate.setToolTipText(
					"<html>The sufix of the name of the level 2 node.<br>\r\nWhen you add a new level 2 node by double clicking on an imported dataset while selecting a level 1 node, it will be named with the name in 'Node name' plus this suffix that will be automatically incremented any time<br>\r\nyou add a new level 2 node.</html>");
			panelOptions.add(jPanel9, BorderLayout.CENTER);
			jPanel9.setLayout(new BorderLayout(0, 0));

			panel_4 = new JPanel();
			final FlowLayout flowLayout_1 = (FlowLayout) panel_4.getLayout();
			flowLayout_1.setAlignment(FlowLayout.LEFT);
			jPanel9.add(panel_4, BorderLayout.NORTH);
			jComboBoxCuratedExperiments = new javax.swing.JComboBox<String>();
			panel_4.add(jComboBoxCuratedExperiments);
			jComboBoxCuratedExperiments.setEditable(false);
			jComboBoxCuratedExperiments.setMaximumRowCount(50);

			jComboBoxCuratedExperiments
					.setModel(new DefaultComboBoxModel<String>(new String[] { "No curated datasets available" }));
			jComboBoxCuratedExperiments.setToolTipText(
					"<html>\n<b>Curated experiments<b/> are created in the Charts<br> viewer, usually after applying some filters.<br> This curated projects are lighter than normal projects<br> since filtered-out data is discarted and is not loaded. </html>");
			jComboBoxCuratedExperiments.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jComboBoxCuratedExperimentsActionPerformed(evt);
				}
			});

			panel_3 = new JPanel();
			final FlowLayout flowLayout = (FlowLayout) panel_3.getLayout();
			flowLayout.setAlignment(FlowLayout.LEFT);
			jPanel9.add(panel_3, BorderLayout.SOUTH);
			jButtonAddCuratedExperiment = new javax.swing.JButton();
			panel_3.add(jButtonAddCuratedExperiment);

			jButtonAddCuratedExperiment.setText("Add");
			jButtonAddCuratedExperiment.setToolTipText(
					"<html>\r<b>Add curated dataset</b><br>\r\nClick here to add a <b>curated dataset</b> to the comparison project.<br>\r\r\nCurated datasets are created in the Charts Viewer. They are datasets that<br>\r\r\nare saved after applying some filters.<br>\r\r\nThis curated datasets are lighter than normal projects since filtered-out<br>\r\r\ndata is discarted and is not loaded.\r\r\n</html>");
			jButtonAddCuratedExperiment.setEnabled(false);
			jButtonRemoveCuratedExperiment = new javax.swing.JButton();
			panel_3.add(jButtonRemoveCuratedExperiment);

			jButtonRemoveCuratedExperiment.setText("Remove");
			jButtonRemoveCuratedExperiment.setToolTipText(
					"<html>\r\n<b>Remove curated dataset</b><br>\r\nClick to remove the selected curated dataset.<br>\r\nCurated datasets are created in the Charts Viewer. They are datasets that<br>\r\nare saved after applying some filters.<br>\r\nThis curated datasets are lighter than normal projects since filtered-out<br>\r\ndata is discarted and is not loaded.\r\n</html>");
			jButtonRemoveCuratedExperiment.setEnabled(false);
			jButtonRemoveCuratedExperiment.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButtonRemoveCuratedExperimentActionPerformed(evt);
				}
			});
			jButtonAddCuratedExperiment.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButtonAddCuratedExperimentActionPerformed(evt);
				}
			});
			panelOptions.add(jPanel5, BorderLayout.SOUTH);
			jPanel5.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
			jPanel5.add(jButtonGoToImport);
			jPanel5.add(jButtonSave);
			jPanel5.add(jButtonFinish);
			jPanel5.add(jButtonHelp);
			eastPanel.add(panelOptions);
			getContentPane().add(eastPanel, BorderLayout.EAST);
			// getContentPane().add(jPanel5);
			// getContentPane().add(jPanel8);
			// getContentPane().add(jPanel9);
			// getContentPane().add(jPanel3);
			// getContentPane().add(jPanel6);
			// appendStatus("Offline mode");
			// appendStatus("MIAPE MSI documents cannot be loaded since the
			// user has not logged-in.");
			// appendStatus("Load a previously saved comparison project or
			// login again.");

			loadIcons();
			// clean tree
			cleanTrees();

			// Initialize project tree
			initializeProjectTree(DEFAULT_PROJECT_NAME, DEFAULT_EXPERIMENT_NAME);
			correctlyInitialized = true;
		} catch (final Exception e) {
			e.printStackTrace();
			log.warn(e.getMessage());
			appendStatus(e.getMessage());
		}

		final AppVersion version = MainFrame.getVersion();
		if (version != null) {
			final String suffix = " (v" + version.toString() + ")";
			setTitle(getTitle() + suffix);
		}

		pack();
		UIUtils.centerFrameOnScreen(this);
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
		final DefaultMutableTreeNode nodoRaiz = new DefaultMutableTreeNode("No projects found");
		final DefaultTreeModel modeloArbol = new DefaultTreeModel(nodoRaiz);
		jTreeProject.setModel(modeloArbol);
		jTreeLocalMIAPEMSIs.setModel(modeloArbol);
	}

	private void loadIcons() {
		// set icon image
		setIconImage(ImageManager.getImageIcon(ImageManager.PACOM_LOGO).getImage());
		jButtonStartLoading.setIcon(ImageManager.getImageIcon(ImageManager.RELOAD_SMALL));
		jButtonStartLoading.setPressedIcon(ImageManager.getImageIcon(ImageManager.RELOAD_CLICKED_SMALL));
		jButtonSave.setIcon(ImageManager.getImageIcon(ImageManager.SAVE));
		jButtonSave.setPressedIcon(ImageManager.getImageIcon(ImageManager.SAVE_CLICKED));
		jButtonFinish.setIcon(ImageManager.getImageIcon(ImageManager.FINISH));
		jButtonFinish.setPressedIcon(ImageManager.getImageIcon(ImageManager.FINISH_CLICKED));
		jButtonLoadSavedProject.setIcon(ImageManager.getImageIcon(ImageManager.LOAD_SMALL));
		jButtonLoadSavedProject.setPressedIcon(ImageManager.getImageIcon(ImageManager.LOAD_SMALL_CLICKED));

		jButtonRemoveSavedProject.setIcon(ImageManager.getImageIcon(ImageManager.DELETE_SMALL));
		jButtonRemoveSavedProject.setPressedIcon(ImageManager.getImageIcon(ImageManager.DELETE_CLICKED_SMALL));

		jButtonAddCuratedExperiment.setIcon(ImageManager.getImageIcon(ImageManager.ADD_SMALL));
		jButtonAddCuratedExperiment.setPressedIcon(ImageManager.getImageIcon(ImageManager.ADD_CLICKED_SMALL));
		jButtonAddExperiment.setIcon(ImageManager.getImageIcon(ImageManager.ADD_SMALL));
		jButtonAddExperiment.setPressedIcon(ImageManager.getImageIcon(ImageManager.ADD_CLICKED_SMALL));
		jButtonAddReplicate.setIcon(ImageManager.getImageIcon(ImageManager.ADD_SMALL));
		jButtonAddReplicate.setPressedIcon(ImageManager.getImageIcon(ImageManager.ADD_CLICKED_SMALL));
		jButtonClearProject2.setIcon(ImageManager.getImageIcon(ImageManager.CLEAR_SMALL));
		jButtonClearProject2.setPressedIcon(ImageManager.getImageIcon(ImageManager.CLEAR_CLICKED_SMALL));
		jButtonDeleteNode.setIcon(ImageManager.getImageIcon(ImageManager.DELETE_SMALL));
		jButtonDeleteNode.setPressedIcon(ImageManager.getImageIcon(ImageManager.DELETE_CLICKED_SMALL));
		jButtonRemoveCuratedExperiment.setIcon(ImageManager.getImageIcon(ImageManager.DELETE_SMALL));
		jButtonRemoveCuratedExperiment.setPressedIcon(ImageManager.getImageIcon(ImageManager.DELETE_CLICKED_SMALL));

		jButtonGoToImport.setIcon(ImageManager.getImageIcon(ImageManager.LOAD_LOGO_32));
		jButtonGoToImport.setPressedIcon(ImageManager.getImageIcon(ImageManager.LOAD_LOGO_32_CLICKED));

	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		jPanel1 = new javax.swing.JPanel();
		jPanel1.setPreferredSize(new Dimension(280, 300));
		jPanel10 = new javax.swing.JPanel();
		jScrollPane1 = new javax.swing.JScrollPane();
		jScrollPane5 = new javax.swing.JScrollPane();
		jScrollPane5.setWheelScrollingEnabled(true);
		jScrollPane5.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jTreeLocalMIAPEMSIs = new ExtendedJTree();
		jPanel11 = new javax.swing.JPanel();
		jScrollPane4 = new javax.swing.JScrollPane();
		jLabel8 = new javax.swing.JLabel();
		jButtonAddManualList = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		jPanel2.setPreferredSize(new Dimension(250, 300));
		jScrollPane2 = new javax.swing.JScrollPane();
		jScrollPane2.setWheelScrollingEnabled(true);
		jScrollPane2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		jScrollPane2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jTreeProject = new ComparisonProjectExtendedJTree(true, true);
		jPanel4 = new javax.swing.JPanel();
		jScrollPane3 = new javax.swing.JScrollPane();
		jScrollPane3.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jScrollPane3.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane3.setWheelScrollingEnabled(true);
		jTextAreaStatus = new javax.swing.JTextArea();
		jProgressBar = new javax.swing.JProgressBar();
		jPanel6 = new javax.swing.JPanel();
		jLabel5 = new javax.swing.JLabel();
		jLabelMIAPEMSINumber = new javax.swing.JLabel();
		jButtonStartLoading = new javax.swing.JButton();
		jPanel7 = new javax.swing.JPanel();
		jComboBox1 = new javax.swing.JComboBox<String>();
		jComboBox1.setSize(new Dimension(200, 20));
		jComboBox1.setMinimumSize(new Dimension(200, 20));
		jComboBox1.setEditable(false);
		jComboBox1.setMaximumRowCount(100);
		jComboBox1.setToolTipText("Select here a saved comparison project");
		jButtonLoadSavedProject = new javax.swing.JButton();
		jButtonLoadSavedProject.setToolTipText("Click here to load the selected saved project");
		jButtonRemoveSavedProject = new javax.swing.JButton();
		jButtonRemoveSavedProject.setToolTipText(
				"<html>\r\nClick here to delete the selected saved project.<br>\r\n<b>This action cannot be undone</b>\r\n</html>\r\n");
		jPanel3 = new javax.swing.JPanel();
		jPanel3.setAlignmentY(Component.TOP_ALIGNMENT);
		jPanel8 = new javax.swing.JPanel();
		jPanel8.setAlignmentX(Component.LEFT_ALIGNMENT);
		jPanel9 = new javax.swing.JPanel();
		jPanel9.setAlignmentY(Component.TOP_ALIGNMENT);
		jPanel9.setAlignmentX(Component.LEFT_ALIGNMENT);
		jPanel5 = new javax.swing.JPanel();
		jButtonFinish = new javax.swing.JButton();
		jButtonSave = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Comparison Project Manager");

		jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"Imported datasets"));

		jPanel10.setToolTipText("Input datasets in remote repository");

		final javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
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

		jTreeLocalMIAPEMSIs.setAutoscrolls(true);
		jTreeLocalMIAPEMSIs.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jTreeLocalMIAPEMSIsMouseClicked(evt);
			}
		});
		jScrollPane5.setViewportView(jTreeLocalMIAPEMSIs);

		jPanel11.setToolTipText("External protein lists");

		jLabel8.setText("Add new protein lists:");

		jButtonAddManualList.setText("Add");
		jButtonAddManualList.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				// jButtonAddManualListActionPerformed(evt);
			}
		});

		final javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
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

		jPanel2.setBorder(
				new TitledBorder(null, "Comparison Project Tree", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		jTreeProject.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jButtonDeleteNode.setEnabled(jTreeProject.getSelectionCount() > 0);
			}
		});

		jScrollPane2.setViewportView(jTreeProject);

		panel = new JPanel();
		final FlowLayout flowLayout_1 = (FlowLayout) panel.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);

		jButtonClearProject2 = new JButton("Clear project tree");
		panel.add(jButtonClearProject2);
		jButtonClearProject2.setToolTipText("Click here to clear the Comparison Project.\r\n");
		jButtonDeleteNode = new javax.swing.JButton();
		panel.add(jButtonDeleteNode);
		jButtonDeleteNode.setToolTipText("Click here to delete the selected node in the Comparison Project tree");

		jButtonDeleteNode.setText("Delete selected node");
		jButtonDeleteNode.setEnabled(false);
		jButtonDeleteNode.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonDeleteNodeActionPerformed(evt);
			}
		});
		jButtonClearProject2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				jButtonClearProjectTreeActionPerformed(e);

			}
		});

		jPanel4.setBorder(
				javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Status"));

		jTextAreaStatus.setColumns(20);
		jTextAreaStatus.setLineWrap(true);
		jTextAreaStatus.setRows(5);
		jTextAreaStatus.setWrapStyleWord(true);
		jTextAreaStatus.setEditable(false);
		jScrollPane3.setViewportView(jTextAreaStatus);

		final javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
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
				"Datasets in PACOM"));

		jLabel5.setText("Datasets imported in PACOM:");

		jLabelMIAPEMSINumber.setText("0");

		jButtonStartLoading.setText("Reload");
		jButtonStartLoading.setToolTipText("<html>Reloads datasets from the local system.<br>"
				+ "You can import other datasets from other PACOM instances<br>"
				+ "by copying them at the corresponding folder:<br><i>" + FileManager.getMiapeLocalDataPath()
				+ "</i></html>");
		jButtonStartLoading.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonStartLoadingActionPerformed(evt);
			}
		});

		jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"Load previously saved Comparison Projects"));

		jComboBox1.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jComboBox1ActionPerformed(evt);
			}
		});

		jButtonLoadSavedProject.setText("Load project");
		jButtonLoadSavedProject.setEnabled(false);
		jButtonLoadSavedProject.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jButtonLoadSavedProjectMouseClicked(evt);
			}
		});

		jButtonRemoveSavedProject.setText("Delete project");
		jButtonRemoveSavedProject.setEnabled(false);
		jButtonRemoveSavedProject.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jButtonRemoveSavedProjectMouseClicked(evt);
			}
		});

		jPanel3.setBorder(
				new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Add nodes to Comparison Project Tree",
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));

		panel_1 = new JPanel();
		final FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);

		panel_2 = new JPanel();
		jLabel3 = new javax.swing.JLabel();
		panel_2.add(jLabel3);

		jLabel3.setText("Add Level 2 node:");
		jLabel3.setToolTipText("<html>Add a level 2 node</html>");
		jTextReplicateName = new javax.swing.JTextField();
		panel_2.add(jTextReplicateName);
		jTextReplicateName.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		jTextReplicateName.setColumns(30);

		jTextReplicateName.setToolTipText(
				"<html>\r\nAdd a new level 2 node.<br>\r\nTo add a new level 2 node, select a level 1 node, type a new name<br>\r\nand click on the 'Add' button in the right.</html>");
		jButtonAddReplicate = new javax.swing.JButton();
		panel_2.add(jButtonAddReplicate);
		jButtonAddReplicate.setText("Add");
		jButtonAddReplicate.setToolTipText("Click here to add a new level 2 node to the Comparison Project Tree.");
		jButtonAddReplicate.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonAddReplicateActionPerformed(evt);
			}
		});
		jTextReplicateName.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyPressed(java.awt.event.KeyEvent evt) {
				jTextReplicateNameKeyPressed(evt);
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
		jLabel2 = new javax.swing.JLabel();
		panel_1.add(jLabel2);

		jLabel2.setText("Add Level 1 node:");
		jLabel2.setToolTipText("<html>\nAdd a level 1 node</html>");
		jTextExperimentName = new javax.swing.JTextField();
		panel_1.add(jTextExperimentName);
		jTextExperimentName.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		jTextExperimentName.setColumns(30);

		jTextExperimentName.setToolTipText(
				"<html>\r\nAdd a new Level 1 node.<br>\r\nHere you can add a new level 1 node.<br>\r\nTo add a new level 1 node, type a new name, and click on the 'Add' button in the right.<br>\r\n</html>");
		jButtonAddExperiment = new javax.swing.JButton();
		panel_1.add(jButtonAddExperiment);

		jButtonAddExperiment.setText("Add");
		jButtonAddExperiment.setToolTipText("Click here to add a new level 1 node to the Comparison Project Tree.");
		jButtonAddExperiment.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonAddExperimentActionPerformed(evt);
			}
		});
		jTextExperimentName.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyPressed(java.awt.event.KeyEvent evt) {
				jTextExperimentNameKeyPressed(evt);
			}

		});

		jPanel8.setBorder(new TitledBorder(null, "Level 2 node name template", TitledBorder.LEADING, TitledBorder.TOP,
				null, null));
		jPanel8.setToolTipText(
				"<html>\nThe name generator template defines which names will be<br>\nautomatically assigned to the Level 2 nodes<br> (replicates/fractions/bands)</html>");

		jPanel9.setBorder(new TitledBorder(null, "Add curated datasets to Comparison Project Tree",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		jPanel9.setToolTipText(
				"<html>\r\n<b>Curated datasets</b> are created in the Chart Viewer,<br> usually after appling some filters.<br> This curated projects are lighter than normal projects<br> since filtered-out data is discarted and is not loaded.</html>");

		jPanel5.setBorder(null);

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

		pack();
	}// </editor-fold>
		// GEN-END:initComponents

	protected void goToImport() {
		setVisible(false);
		parent.startDataImport();
	}

	private void jTreeLocalMIAPEMSIsMouseClicked(java.awt.event.MouseEvent evt) {
		try {
			localMiapeTreeClicked(evt);
		} catch (final IllegalMiapeArgumentException e) {
			appendStatus("Error: " + e.getMessage());
		}
	}

	private void localMiapeTreeClicked(MouseEvent evt) {
		// double click
		if (evt.getClickCount() == 2) {
			if (jTreeLocalMIAPEMSIs.isOnlyOneNodeSelected(MIAPE_LEVEL)) {
				// get MIAPE ID
				final String miapeID = jTreeLocalMIAPEMSIs.getStringFromSelection(LOCAL_MIAPE_ID_REGEXP);
				// get MIAPE Name
				String miapeName = jTreeLocalMIAPEMSIs.getStringFromSelection(LOCAL_MIAPE_UNIQUE_NAME_REGEXP);
				if ("".equals(miapeName)) {
					miapeName = null;// "Dataset_" + miapeID;
				}
				// get local project name
				final String projectName = jTreeLocalMIAPEMSIs
						.getStringFromParentOfSelection(MIAPE_LOCAL_PROJECT_NAME_REGEXP);
				// start retrieving
				final Integer msi_id = Integer.valueOf(miapeID);

				final Integer ms_id = getAssociatedLocalMIAPEMSId(msi_id, projectName, miapeName);
				// if a replicate is selected
				if (jTreeProject.isOnlyOneNodeSelected(REPLICATE_LEVEL)
						|| jTreeProject.isOnlyOneNodeSelected(MIAPE_REPLICATE_LEVEL)) {

					MyProjectTreeNode replicateNode = jTreeProject.getSelectedNode();
					if (jTreeProject.isOnlyOneNodeSelected(MIAPE_REPLICATE_LEVEL)) {
						replicateNode = (MyProjectTreeNode) replicateNode.getParent();
					}
					// check if the experiment node is curated. If so, throw
					// exception
					final MyProjectTreeNode experimentNode = (MyProjectTreeNode) replicateNode.getParent();
					final CPExperiment experiment = (CPExperiment) experimentNode.getUserObject();
					if (experiment.isCurated()) {
						throw new IllegalMiapeArgumentException(
								"A non curated dataset cannot be added to a curated experiment");
					}
					final CPReplicate cpReplicate = (CPReplicate) replicateNode.getUserObject();
					// String miapeNodeName = getMIAPENodeNameFromTemplate();

					final String miapeNodeName = FilenameUtils
							.getBaseName(FileManager.getMiapeMSILocalFileName(Integer.valueOf(miapeID), miapeName));

					final CPMSI cpMsi = new CPMSI();
					cpMsi.setId(msi_id);
					cpMsi.setName(miapeNodeName);
					cpMsi.setLocal(true);
					cpMsi.setLocalProjectName(projectName);
					if (ms_id != null) {
						cpMsi.setMiapeMsIdRef(ms_id);
						final CPMS cpMS = new CPMS();
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
					final MyProjectTreeNode newNode = jTreeProject.addNewNode(cpMsi, replicateNode);
					// scroll to experiment node to let add more replicates
					jTreeProject.selectNode(experimentNode);
					jTreeProject.expandNode(experimentNode);
					saved = false;
					// }
				} else if (jTreeProject.isOnlyOneNodeSelected(EXPERIMENT_LEVEL)) {
					final MyProjectTreeNode experimentNode = jTreeProject.getSelectedNode();
					final CPExperiment cpExp = (CPExperiment) experimentNode.getUserObject();
					if (cpExp.isCurated()) {
						throw new IllegalMiapeArgumentException(
								"A non curated dataset cannot be added to a curated experiment");
					}
					// add replicate node
					final String defaultReplicateName = getMIAPENodeNameFromTemplate();
					final CPReplicate cpRep = new CPReplicate();
					cpRep.setName(defaultReplicateName);

					cpExp.getCPReplicate().add(cpRep);
					final MyProjectTreeNode replicateNode = jTreeProject.addNewNode(cpRep, experimentNode);
					// add miape node
					final CPMSI cpMsi = new CPMSI();
					cpMsi.setId(msi_id);
					cpMsi.setLocal(true);
					cpMsi.setLocalProjectName(projectName);
					if (ms_id != null) {
						cpMsi.setMiapeMsIdRef(ms_id);
						final CPMS cpMS = new CPMS();
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
					final CPMSIList cpMsiList = new CPMSIList();
					cpMsiList.getCPMSI().add(cpMsi);
					cpRep.setCPMSIList(cpMsiList);
					final MyProjectTreeNode newNode = jTreeProject.addNewNode(cpMsi, replicateNode);
					// scroll to experiment node to let add more replicates
					jTreeProject.selectNode(experimentNode);
					jTreeProject.expandNode(experimentNode);
					saved = false;
				} else {
					// add experiment node, level 1 node
					final MyProjectTreeNode experimentNode = addExperiment("experiment");

					if (experimentNode != null) {
						final CPExperiment cpExp = (CPExperiment) experimentNode.getUserObject();
						if (cpExp.isCurated()) {
							throw new IllegalMiapeArgumentException(
									"A non curated dataset cannot be added to a curated experiment");
						}
						// add replicate node
						final String defaultReplicateName = getMIAPENodeNameFromTemplate();
						final CPReplicate cpRep = new CPReplicate();
						cpRep.setName(defaultReplicateName);

						cpExp.getCPReplicate().add(cpRep);
						final MyProjectTreeNode replicateNode = jTreeProject.addNewNode(cpRep, experimentNode);
						// add miape node
						final CPMSI cpMsi = new CPMSI();
						cpMsi.setId(msi_id);
						cpMsi.setLocal(true);
						cpMsi.setLocalProjectName(projectName);
						if (ms_id != null) {
							cpMsi.setMiapeMsIdRef(ms_id);
							final CPMS cpMS = new CPMS();
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
						cpMsi.setName(FilenameUtils.getBaseName(
								FileManager.getMiapeMSILocalFileName(Integer.valueOf(miapeID), miapeName)));
						final CPMSIList cpMsiList = new CPMSIList();
						cpMsiList.getCPMSI().add(cpMsi);
						cpRep.setCPMSIList(cpMsiList);
						final MyProjectTreeNode newNode = jTreeProject.addNewNode(cpMsi, replicateNode);
						// scroll to experiment node to let add more replicates
						jTreeProject.selectNode(experimentNode);
						jTreeProject.expandNode(experimentNode);
						saved = false;
					}
				}
				// increment number template
				incrementSuffixIfNumber();
				// expand tree
				// jTreeProject.expandAll();

			} else if (jTreeLocalMIAPEMSIs.isOnlyOneNodeSelected(MIAPE_PROJECT_LEVEL)) {
				// if the user clicks on a MIAPE PROJECT
				log.debug("Local dataset project selected");
				final int numMIAPEsInMIAPEProject = jTreeLocalMIAPEMSIs.getSelectedNode().getChildCount();
				if (numMIAPEsInMIAPEProject > 0) {

					// IF the node 0 is selected in the comparison project
					final String localProjectName = jTreeLocalMIAPEMSIs
							.getStringFromSelection(MIAPE_LOCAL_PROJECT_NAME_REGEXP);
					final int selectedOption = JOptionPane.showConfirmDialog(this,
							"<html>Do you want to add all datasets in the dataset folder to a new level 1 node called '"
									+ localProjectName + "' in the Comparison Project Tree?</html>",
							"Add datasets to Comparison Project Tree", JOptionPane.YES_NO_CANCEL_OPTION);
					if (selectedOption == JOptionPane.YES_OPTION) {
						final MyProjectTreeNode projectNode = jTreeProject.getRootNode();
						final CPExperimentList cpExpList = (CPExperimentList) projectNode.getUserObject();
						final CPExperiment cpExp = new CPExperiment();
						cpExp.setName(localProjectName);
						cpExp.setCurated(false);
						// Add a new experiment node
						final MyProjectTreeNode experimentNode = jTreeProject.addNewNode(cpExp, projectNode);

						jTextExperimentName.setText(localProjectName);

						final Enumeration<DefaultMutableTreeNode> children = jTreeLocalMIAPEMSIs.getSelectedNode()
								.children();
						while (children.hasMoreElements()) {
							final DefaultMutableTreeNode miapeMSIChild = children.nextElement();
							final String msi_id = AbstractExtendedJTree.getString(LOCAL_MIAPE_ID_REGEXP,
									(String) miapeMSIChild.getUserObject());
							final String miapeName = AbstractExtendedJTree.getString(LOCAL_MIAPE_UNIQUE_NAME_REGEXP,
									(String) miapeMSIChild.getUserObject());
							final Integer ms_id = getAssociatedLocalMIAPEMSId(Integer.valueOf(msi_id), localProjectName,
									miapeName);
							// start retrieving
							// startMIAPEMSIRetrieving(Integer.valueOf(miapeID));
							// add replicate node
							final String replicateName = getMIAPENodeNameFromTemplate();
							final CPReplicate cpRep = new CPReplicate();
							cpRep.setName(replicateName);

							cpExp.getCPReplicate().add(cpRep);
							final MyProjectTreeNode replicateNode = jTreeProject.addNewNode(cpRep, experimentNode);
							// add miape node
							final String miapeNodeName = FilenameUtils.getBaseName(
									FileManager.getMiapeMSILocalFileName(Integer.valueOf(msi_id), miapeName));
							final CPMSI cpMsi = new CPMSI();
							cpMsi.setId(Integer.valueOf(msi_id));
							cpMsi.setName(miapeNodeName);
							cpMsi.setLocal(true);
							cpMsi.setLocalProjectName(localProjectName);
							if (ms_id != null) {
								cpMsi.setMiapeMsIdRef(ms_id);
								final CPMS cpMS = new CPMS();
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
							final CPMSIList cpMsiList = new CPMSIList();
							cpMsiList.getCPMSI().add(cpMsi);
							cpRep.setCPMSIList(cpMsiList);
							final MyProjectTreeNode newNode = jTreeProject.addNewNode(cpMsi, replicateNode);
							jTreeProject.selectNode(newNode);
							saved = false;
							// increment number template
							incrementSuffixIfNumber();
						}
						cpExpList.getCPExperiment().add(cpExp);
						jTreeProject.scrollToNode(experimentNode);
						jTreeProject.expandNode(experimentNode);
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

			final int associatedMiapeMSRef = new MiapeHeader(msiFile, false).getMiapeRef();

			if (associatedMiapeMSRef != -1) {
				return associatedMiapeMSRef;
			}

		}
		return null;
	}

	private void jButtonRemoveSavedProjectMouseClicked(java.awt.event.MouseEvent evt) {
		final String projectName = (String) jComboBox1.getSelectedItem();
		if (projectName != null && !"".equals(projectName)) {
			final int option = JOptionPane.showConfirmDialog(this,
					"<html>The comparison project will be deleted.<br>Are you sure?</html>",
					"Delete comparison project", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (option == JOptionPane.YES_OPTION) {
				final boolean deleted = FileManager.removeProjectXMLFile(projectName);
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
		final int selectedOption = JOptionPane.showConfirmDialog(this,
				"<html>Curated experiment '" + curatedExperimentName
						+ "' will be removed from the disk.<br>Do you really want to continue?</html>",
				"Add datasets to comparison project", JOptionPane.YES_NO_CANCEL_OPTION);
		if (selectedOption == JOptionPane.YES_OPTION) {
			log.info("Removing curated experiment files ... " + curatedExperimentName);
			jButtonRemoveCuratedExperiment.setEnabled(false);
			final boolean removed = FileManager.removeCuratedExperimentFiles(curatedExperimentName);
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
			final File curatedExpFile = new File(curatedExperimentXMLFilePath);
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
			final boolean saved = save(true);
			if (!saved)
				return;
		} catch (final IllegalMiapeArgumentException e) {
			appendStatus(e.getMessage());

		}

	}

	public void jButtonFinishActionPerformed(java.awt.event.ActionEvent evt) {
		finishPressed = true;

		// SAVE project file
		try {
			final boolean saved = save(true);
			if (!saved) {
				return;
			}

		} catch (final IllegalMiapeArgumentException e) {
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
				final CPExperiment cpExp = (CPExperiment) ((MyProjectTreeNode) jTreeProject.getSelectedNode()
						.getParent()).getUserObject();
				cpExp.getCPReplicate().remove(cpRep);

			} else if (jTreeProject.isOnlyOneNodeSelected(MIAPE_REPLICATE_LEVEL)) {
				final CPMSI cpMSI = (CPMSI) jTreeProject.getSelectedNode().getUserObject();
				final CPReplicate cpRep = (CPReplicate) ((MyProjectTreeNode) jTreeProject.getSelectedNode().getParent())
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

	public JComboBox<String> getProjectComboBox() {
		return jComboBox1;
	}

	public void setCorrectlyInitialized(boolean b) {
		correctlyInitialized = b;
	}

	private void jButtonLoadSavedProjectMouseClicked(java.awt.event.MouseEvent evt) {
		final String projectName = (String) jComboBox1.getSelectedItem();
		if (projectName != null && !"".equals(projectName)) {

			final String projectXMLFilePath = FileManager.getProjectXMLFilePath(projectName);
			final File projectFile = new File(projectXMLFilePath);
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
			} catch (final Exception e) {
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
		final String prefix = jTextFieldLabelNameTemplate.getText();
		final String sufix = jTextFieldLabelNumberTemplate.getText();

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
		} catch (final NumberFormatException e) {
			// do nothing
		}
	}

	private boolean save(boolean checkTree) throws IllegalMiapeArgumentException {
		// create object tree from project tree
		final CPExperimentList cpExpList = getCPExperimentListFromTree();
		if (saved) {
			log.info("The tree is already saved. Showing chart manager");

			try {
				// Save again the file because when getCPExpListFromTree is
				// called, some miapeMS node are added
				FileManager.saveProjectFile(cpExpList);
			} catch (final JAXBException e) {
				throw new IllegalMiapeArgumentException(e.getMessage());
			}
			final String expListName = cpExpList.getName();
			currentCgfFile = FileManager.getProjectFile(expListName);
			showChartManager();

			return false;
		}

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
					"<html>The Comparison Project '" + cpExpList.getName()
							+ "' already exists.<br>Are you sure you want to overwrite it?</html>",
					"Warning, Comparison Project already exists", JOptionPane.YES_NO_OPTION);
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
		} catch (final JAXBException e) {
			appendStatus("Error saving project file: " + e.getMessage());
			log.warn(e.getMessage());
			e.printStackTrace();
			throw new IllegalMiapeArgumentException(e);
		}

		return true;
	}

	private CPExperimentList getCPExperimentListFromTree() {

		final TreeModel model = jTreeProject.getModel();
		final MyProjectTreeNode root = (MyProjectTreeNode) model.getRoot();

		final Object userObject = root.getUserObject();
		if (userObject instanceof CPExperimentList) {

			final CPExperimentList cpExperimentList = (CPExperimentList) userObject;
			final List<CPExperiment> cpExps = cpExperimentList.getCPExperiment();
			if (cpExps != null)
				for (final CPExperiment cpExp : cpExps) {
					final List<CPReplicate> cpReps = cpExp.getCPReplicate();
					if (cpReps != null)
						for (final CPReplicate cpRep : cpReps) {
							CPMSList cpMsList = cpRep.getCPMSList();
							// if there is not MIAPE MS list, is because there
							// is not local and the referenced miape ms
							// documents have to be retrieved
							if (cpMsList == null || cpMsList.getCPMS() == null || cpMsList.getCPMS().isEmpty()) {
								final CPMSIList cpmsiList = cpRep.getCPMSIList();
								cpMsList = new CPMSList();
								if (cpmsiList != null) {
									for (final CPMSI cpMSI : cpmsiList.getCPMSI()) {
										if (((cpMSI.isLocal() != null && !cpMSI.isLocal())
												|| cpMSI.isLocal() == null)) {
											final CPMS cpMS = new CPMS();
											final Integer miapeMsIdRef = cpMSI.getMiapeMsIdRef();
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

	private void jTextExperimentNameKeyPressed(java.awt.event.KeyEvent evt) {
		// if RETURN is pressed, throw ADD button event
		if (KeyEvent.VK_ENTER == evt.getKeyCode()) {
			jButtonAddExperimentActionPerformed(null);

		}
	}

	private void jButtonAddReplicateActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			addReplicate();
		} catch (final IllegalMiapeArgumentException e) {
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
					final MyProjectTreeNode experimentTreeNode = jTreeProject.getSelectedNode();
					final CPExperiment cpExp = (CPExperiment) experimentTreeNode.getUserObject();
					if (cpExp.isCurated())
						throw new IllegalMiapeArgumentException(
								"A non curated dataset cannot be added to a curated experiment ");
					final CPReplicate cpRep = new CPReplicate();
					cpRep.setName(replicateName);
					cpExp.getCPReplicate().add(cpRep);
					// add the replicate node on the experiment node
					final MyProjectTreeNode replicateTreeNode = jTreeProject.addNewNode(cpRep, experimentTreeNode);
					if (jTreeLocalMIAPEMSIs.isOnlyOneNodeSelected(MIAPE_LEVEL)) {
						final int miapeID = Integer
								.valueOf(jTreeLocalMIAPEMSIs.getStringFromSelection(MIAPE_ID_REGEXP));

						final String miapeName = (String) jTreeLocalMIAPEMSIs.getSelectedNode().getUserObject();
						final CPMSI cpMSI = new CPMSI();
						cpMSI.setName(miapeName);
						cpMSI.setId(miapeID);
						if (cpRep.getCPMSIList() == null) {
							cpRep.setCPMSIList(new CPMSIList());
						}
						cpRep.getCPMSIList().getCPMSI().add(cpMSI);

						// add the miape node on the replicate node
						final MyProjectTreeNode newNode = jTreeProject.addNewNode(cpMSI

								, replicateTreeNode);
						jTreeProject.selectNode(newNode);

						saved = false;
					} else {
						saved = false;
						jTreeProject.selectNode(replicateTreeNode);
					}

				} else if (jTreeProject.isOnlyOneNodeSelected(REPLICATE_LEVEL)) {
					final int miapeID = Integer.valueOf(jTreeLocalMIAPEMSIs.getStringFromSelection(MIAPE_ID_REGEXP));

					// get selected experiment node
					final MyProjectTreeNode replicateTreeNode = jTreeProject.getSelectedNode();
					final CPReplicate cpRep = (CPReplicate) replicateTreeNode.getUserObject();

					// add the miape node on the replicate node
					final String miapeNodeName = (String) jTreeLocalMIAPEMSIs.getSelectedNode().getUserObject();
					final CPMSI cpMSI = new CPMSI();
					cpMSI.setName(miapeNodeName);
					cpMSI.setId(miapeID);
					cpRep.getCPMSIList().getCPMSI().add(cpMSI);

					final MyProjectTreeNode newNode = jTreeProject.addNewNode(cpMSI
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
			addExperiment(jTextExperimentName.getText());
		} catch (final IllegalMiapeArgumentException e) {
			appendStatus("Error: " + e.getMessage());
		}

	}

	private MyProjectTreeNode addExperiment(String experimentName) {

		if (!"".equals(experimentName)) {
			final CPExperiment cpExp = new CPExperiment();
			cpExp.setName(experimentName);
			cpExp.setCurated(false);
			final DefaultTreeModel model = (DefaultTreeModel) jTreeProject.getModel();
			if (model != null) {
				final MyProjectTreeNode parentNode = (MyProjectTreeNode) model.getRoot();
				final CPExperimentList cpExpList = (CPExperimentList) parentNode.getUserObject();
				cpExpList.getCPExperiment().add(cpExp);
				final MyProjectTreeNode newNode = jTreeProject.addNewNode(cpExp, parentNode);
				jTreeProject.scrollToNode(newNode);
				jTreeProject.expandNode(newNode);
				saved = false;
				return newNode;
			}
		} else {
			throw new IllegalMiapeArgumentException("Type a name for the level 1 node before to click on Add button");
		}
		return null;
	}

	private void initializeProjectTree(String projectName, String experimentName) {
		final CPExperimentList cpExpList = new CPExperimentList();
		cpExpList.setName(projectName);
		final MyProjectTreeNode nodoRaizMSI = new MyProjectTreeNode(cpExpList);
		final DefaultTreeModel modeloArbolMSI = new DefaultTreeModel(nodoRaizMSI);
		jTreeProject.setModel(modeloArbolMSI);

		final CPExperiment cpExp = new CPExperiment();
		cpExp.setName(experimentName);
		cpExpList.getCPExperiment().add(cpExp);
		final MyProjectTreeNode experimentNode = jTreeProject.addNewNode(cpExp, nodoRaizMSI);
		jTreeProject.selectNode(experimentNode);
		saved = false;
	}

	public void initializeProjectTree(File projectFile) {
		CPExperimentList cpExperimentList = null;
		try {
			cpExperimentList = ComparisonProjectFileUtil.getExperimentListFromComparisonProjectFile(projectFile);

			// String projectName = cpExperimentList.getName();
			final MyProjectTreeNode nodoRaizMSI = new MyProjectTreeNode(cpExperimentList);
			final DefaultTreeModel modeloArbolMSI = new DefaultTreeModel(nodoRaizMSI);
			jTreeProject.setModel(modeloArbolMSI);
			final Iterator<CPExperiment> cpExpIterator = cpExperimentList.getCPExperiment().iterator();
			while (cpExpIterator.hasNext()) {
				final CPExperiment cpExperiment = cpExpIterator.next();
				final boolean curated = cpExperiment.isCurated();
				final MyProjectTreeNode experimentNode = jTreeProject.addNewNode(cpExperiment, nodoRaizMSI);
				boolean hasOneReplicate = false;
				final Iterator<CPReplicate> cpRepIterator = cpExperiment.getCPReplicate().iterator();
				while (cpRepIterator.hasNext()) {
					final CPReplicate cpReplicate = cpRepIterator.next();
					final MyProjectTreeNode replicateNode = jTreeProject.addNewNode(cpReplicate, experimentNode);

					if (cpReplicate.getCPMSIList() != null) {
						for (final CPMSI cpMsi : cpReplicate.getCPMSIList().getCPMSI()) {

							// LOCAL MIAPE
							boolean addNode = true;
							if (curated) {
								final File file = new File(
										FileManager.getMiapeMSICuratedXMLFilePathFromMiapeInformation(cpMsi));
								if (!file.exists()) {
									final String notificacion = "Warning:  '" + cpMsi.getName()
											+ ".xml' file doesn't exist at folder '"
											+ FileManager.getCuratedExperimentFolderPath(cpMsi.getLocalProjectName())
											+ "'";
									log.warn(notificacion);
									appendStatus(notificacion);
									addNode = false;
								}

							} else {
								final File file = new File(
										FileManager.getMiapeMSIXMLFileLocalPathFromMiapeInformation(cpMsi));
								if (!file.exists()) {
									addNode = false;
									final String notificacion = "Warning:  '" + cpMsi.getName()
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
		} catch (final JAXBException e) {
			log.warn(e.getMessage());
			throw new MiapeDataInconsistencyException(
					"Error loading " + projectFile.getAbsolutePath() + " config file: " + e.getMessage());
		}
	}

	private void addExperimentToProjectTree(File projectFile) {
		try {
			final MyProjectTreeNode experimentListNode = jTreeProject.getRootNode();
			final CPExperiment CPExperiment = ComparisonProjectFileUtil
					.getExperimentFromComparisonProjectFile(projectFile);
			CPExperiment.setCurated(true);
			final CPExperimentList cpExperimentList = (CPExperimentList) experimentListNode.getUserObject();
			cpExperimentList.getCPExperiment().add(CPExperiment);

			final MyProjectTreeNode experimentNode = jTreeProject.addNewNode(CPExperiment, experimentListNode);
			for (final CPReplicate cpReplicate : CPExperiment.getCPReplicate()) {

				final MyProjectTreeNode replicateNode = jTreeProject.addNewNode(cpReplicate, experimentNode);
				if (cpReplicate.getCPMSIList() != null) {
					for (final CPMSI cpMsi : cpReplicate.getCPMSIList().getCPMSI()) {
						if (cpMsi.getLocalProjectName() == null) {
							cpMsi.setLocalProjectName(CPExperiment.getName());
						}
						if (cpMsi.getName().contains("MIAPE_MSI")) {
							cpMsi.setName(FileManager.getMiapeMSILocalFileName(cpMsi.getId(), null));
						}
						// final MyProjectTreeNode miapeNode =
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
		} catch (final JAXBException e) {
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

				final Miape2ExperimentListDialog dialog = new Miape2ExperimentListDialog(null);
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
	private javax.swing.JButton jButtonDeleteNode;
	private javax.swing.JButton jButtonFinish;
	private javax.swing.JButton jButtonLoadSavedProject;
	private javax.swing.JButton jButtonRemoveCuratedExperiment;
	private javax.swing.JButton jButtonRemoveSavedProject;
	private javax.swing.JButton jButtonSave;
	private javax.swing.JButton jButtonStartLoading;
	private javax.swing.JComboBox<String> jComboBox1;
	private javax.swing.JComboBox<String> jComboBoxCuratedExperiments;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JLabel jLabel8;
	private javax.swing.JLabel jLabelMIAPEMSINumber;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel10;
	private javax.swing.JPanel jPanel11;
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
	private javax.swing.JTextArea jTextAreaStatus;
	private javax.swing.JTextField jTextExperimentName;
	private javax.swing.JTextField jTextFieldLabelNameTemplate;
	private javax.swing.JTextField jTextFieldLabelNumberTemplate;
	private javax.swing.JTextField jTextReplicateName;
	private ExtendedJTree jTreeLocalMIAPEMSIs;

	private ComparisonProjectExtendedJTree jTreeProject;
	private JButton jButtonGoToImport;
	private JButton jButtonHelp;
	private JButton jButtonClearProject2;
	private JPanel panel;
	private JPanel panel_1;
	private JPanel panel_2;
	private JPanel panel_3;
	private JPanel panel_4;
	private JPanel panel_5;

	// End of variables declaration//GEN-END:variables

	// @Override

	@Override
	public synchronized void propertyChange(PropertyChangeEvent evt) {
		if ("progress".equals(evt.getPropertyName())) {
			final int progress = (Integer) evt.getNewValue();
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
			final String notificacion = evt.getNewValue().toString();
			appendStatus(notificacion);
		} else if (MiapeTreeIntegrityCheckerTask.INTEGRITY_OK.equals(evt.getPropertyName())) {
			enableStateKeeper.setToPreviousState(this);
			jProgressBar.setIndeterminate(false);

			try {
				save(false);
				if (finishPressed) {
					showChartManager();
				}
			} catch (final IllegalMiapeArgumentException e) {
				setStatus(e.getMessage());
				return;
			}
		} else if (MiapeTreeIntegrityCheckerTask.INTEGRITY_START.equals(evt.getPropertyName())) {
			enableStateKeeper.keepEnableStates(this);
			enableStateKeeper.disable(this);
			jProgressBar.setIndeterminate(true);

		} else if (MiapeTreeIntegrityCheckerTask.INTEGRITY_ERROR.equals(evt.getPropertyName())) {
			jProgressBar.setIndeterminate(false);
			enableStateKeeper.setToPreviousState(this);
			final String message = (String) evt.getNewValue();
			JOptionPane.showMessageDialog(this, message, "Error in comparison project", JOptionPane.OK_OPTION);
			appendStatus("Save canceled");
		} else if (LocalDataTreeLoaderTask.LOCAL_TREE_LOADER_FINISHED.equals(evt.getPropertyName())) {
			enableStateKeeper.setToPreviousState(this);
			final Integer numLoaded = (Integer) evt.getNewValue();
			jLabelMIAPEMSINumber.setText(numLoaded.toString());
			appendStatus(numLoaded + " datasets loaded.");
		} else if (LocalDataTreeLoaderTask.LOCAL_TREE_LOADER_ERROR.equals(evt.getPropertyName())) {
			enableStateKeeper.setToPreviousState(this);
			appendStatus("Error loading local datasets: " + evt.getNewValue());
		} else if (LocalDataTreeLoaderTask.LOCAL_TREE_LOADER_STARTS.equals(evt.getPropertyName())) {
			enableStateKeeper.keepEnableStates(this);
			enableStateKeeper.disable(this);
		}
		pack();
	}

	public void showChartManager() {
		final boolean showChartManager = true;

		if (showChartManager) {
			setVisible(false);
			final boolean resetErrorLoadingData = true;
			final ChartManagerFrame chartManager = ChartManagerFrame.getInstance(this, currentCgfFile,
					resetErrorLoadingData);
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

	public JComboBox<String> getCuratedComboBox() {
		return jComboBoxCuratedExperiments;
	}

	@Override
	public void setState(int state) {
		super.setState(state);
	}

	@Override
	public List<String> getHelpMessages() {
		final String[] ret = {

				"Comparison Project Manager", //
				"From here you can:", //
				"- load previously saved comparison projects", //
				"- create new comparison projects,", //
				"At the left panel (<i>Imported datasets</i>) you will see the datasets that have been imported in the system. And in the center you will be able to edit the Comparison Project Tree.", //
				"", //
				"<b>How to build or edit a comparison project:</b>", //
				"In order to create or edit a comparison project you will have to add new nodes and then add the datasets on these nodes.", //
				"1. Click on <i>'Clear comparison project'</i> button if you want to start a new project. Otherwise you can work over the default outline "
						+ "of the new project or over any other project you have loaded. As long as you save it with a different name (name of the root node), you will not override the information of the original project.", //
				"2. Edit the name of the project by double clicking the root node.", //
				"3. Use <i>Add Level 1 node</i> and <i>Add Level 2 node</i> controls at the right to add nodes to the tree. The nodes will be added to the selected node on the Comparison Project Tree.", //
				"4. Add the individual datasets to the tree by double clicking on them (from the left panel). "
						+ "They will be added to the node in the Comparison Project Tree that is selected. New parent nodes will be created if necessary.", //
				"5. You can edit the names of the created nodes by double clicking on them.", //
				"6. Repeat steps 3 to 5 to include and organize all the datasets you want to explore and compare.", //
				"", //
				"<b>Load previously created comparison projects:</b>", //
				"1. Select a saved project from the dropdown menu at the top left of this window.", //
				"2. Click on <i>'Load project'</i> button", //
				"3. The project will be loaded in the Comparison Project Tree, where can be edited if you want.", //
				"4. Click on <i>'Save project'</i> button to save the project if it has been modified.", //
				"5. Click on <i>'Next'</i> button in order to go to the Chart Viewer.", //

				"", //
				"<b>The level 2 node name template:</b>", //
				"It is used to automatically assign consecutive numbers to level 2 node names with a predefined prefix.", //
				"This is specially helpfull when you add multiple datasets and you want to name them with the same name but sequentially numbered.", //
				"By typing a name like <i>'replicate'</i> and having the <i>'incrementing suffix'</i> as <i>'1'</i> will create level 2 nodes named as: <i>'replicate1'</i>, <i>'replicate2'</i>, <i>'replicate3'</i>, etc...", //
				"", //
				"<b>Importance of the arragement of the datasets in the Comparison project tree</b>", //
				"The information contained in each dataset will be aggregated with other datasets if they are depending on the same node in the comparison project,", //
				"Later, the '<i>Chart Viewer</i>' will provide you the option to <i>switch between different levels of aggregation</i> of the data:", //
				"- <i>one single data series (level 0)</i>: a chart with just one data series which aggregates all the individual datasets,", //
				"- <i>one data series per level 1</i>: a chart with one data series per each of the level 1 nodes which aggregates all the individual datasets pending from that node, ", //
				"- <i>one data series per level 2</i>: a chart with one data series per each of the level 2 nodes which aggregates all the individual datasets pending from that node,", //
				"- <i>one separate chart per level 1</i>: this will generate a different chart per each one of the level 1 nodes. Each of these charts will contain a data series per level 2 nodes pending on that level 1 node.", //
				// //
				"<b>Adding curated datasets into your comparison project:</b>", //
				"Alternatively to add any dataset from the imported datasets, you can also add <b>curated datasets</b> as individual datasets."
						+ " To create curated datasets, you have to do it on the next step, that is, the '<i>Chart Viewer</i>'"
						+ " where after applying some filters you can save the datasets as <i>'curated'</i>. ", //
				"To do that, select the curated dataset from the drop-down menu, and click on the "
						+ "<i>'Add'</i> button. This will add a new level 1 node to the Comparison Project Tree with a distinguisable star symbol.", //
				"<b>Saving your comparison project:</b>", //
				"Click on <i>'Save project'</i> button to save the project or click on <i>'Next'</i> "
						+ "button to save and go to the <i>Chart Viewer</i>.", //

				"<b>How to delete a node:</b>", //
				"To delete any node, just select it and click on <i>'Delete selected node'</i> button or <b>press delete key</b>.", //

		};
		return Arrays.asList(ret);
	}

}