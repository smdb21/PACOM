/*
 * MainFrame.java Created on __DATE__, __TIME__
 */

package org.proteored.pacom.gui;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker.StateValue;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.proteored.pacom.analysis.gui.Miape2ExperimentListDialog;
import org.proteored.pacom.analysis.util.FileManager;
import org.proteored.pacom.gui.tasks.CheckUpdateTask;
import org.proteored.pacom.gui.tasks.OntologyLoaderTask;
import org.proteored.pacom.gui.tasks.OntologyLoaderWaiter;
import org.proteored.pacom.utils.AppVersion;
import org.proteored.pacom.utils.ExampleProject;
import org.proteored.pacom.utils.PropertiesReader;

/**
 *
 * @author __USER__
 */
public class MainFrame extends AbstractJFrameWithAttachedHelpDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4998871134548349223L;
	// by default

	private static final String URL_MIAPE_EXTRACTOR_TUTORIAL = "https://github.com/smdb21/PACOM/wiki";
	private static final String URL_MIAPE_EXTRACTOR_BATCH_TUTORIAL = "https://github.com/smdb21/PACOM/wiki/How-to-import-datasets#importing-multiple-datasets-with-batch-import";

	// by default:
	private static AppVersion version;
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("log4j.logger.org.proteored");
	private OntologyLoaderTask ontologyLoader;
	private CheckUpdateTask updateTask;
	private Miape2ExperimentListDialog miape2experimentListDialog;
	private MiapeExtractionBatchFrame miapeExtractionBatchFrame;
	private final static String dataInspectionTooltip = "<html><br><ul><li>Inspect your data creating your own inspection projects.</li><li>Compare complex experiments in an intuitive way.</li><li>Get a lot of charts representing qualitative data from your experiments.</li><li>Filter data applying several filters (FDR, score thresholds, etc...)</li><li>Export your data into PRIDE XML format</li></ul><br></html>";
	private final static String dataImportToolTip = "<html><br>Extract and import datasets from input data files such as:<br><ul><li>mzIdentML</li><li>mzML</li><li>PRIDE XML</li><li>X!Tandem output XML</li> <li>DTASelect output</li><li>Separated values tables</li></ul><br></html>";
	private final static String batchDataImportToolTip = "<html><br>Batch data import from datasets files such as: <ul> <li>mzIdentML</li> <li>mzML</li> <li>PRIDE XML</li> <li>X!Tandem output XML</li> <li>DTASelect output</li> <li>Separated values tables</li> </ul>Using a batch data import file you can import multiple datasets.<br><br></html>";

	public static final boolean parallelProcessingOnExtraction = true;

	private static final String APP_PROPERTIES = "app.properties";

	private final boolean checkUpdates = true;

	public static File currentFolder = new File(".");

	/**
	 * Creates new form MainFrame
	 *
	 * @throws Exception
	 */
	public MainFrame() {
		super(40);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
				| IllegalAccessException e) {
			e.printStackTrace();
		}

		// set icon image
		setIconImage(ImageManager.getImageIcon(ImageManager.PACOM_LOGO).getImage());

		try {
			// do background tasks for resources loading
			// this.ontologyLoader = new OntologyLoaderTask();
			// this.ontologyLoader.addPropertyChangeListener(this);
			// this.ontologyLoader.execute();

			// Launch update checker
			if (checkUpdates) {
				// this has to be modified
				// TODO
				updateTask = new CheckUpdateTask();
				updateTask.execute();
			}
			initComponents();

			try {
				version = getVersion();
				if (version != null) {
					String suffix = " (v" + version.toString() + ")";
					if (!getTitle().endsWith(suffix))
						setTitle(getTitle() + suffix);
				}
			} catch (Exception e1) {
			}

			// load ontologies
			loadOntologies();

			// load exampleprojects
			loadExampleProjectsInCombo();

			// show help
			showAttachedHelpDialog();

		} catch (Exception e) {
			String message = "";
			if (e.getMessage().startsWith("XML reader error") || e.getMessage().contains("Failed to access")) {
				message = "Webservice is unreachable.";
			} else {
				message = e.getMessage();
			}
			writeErrorMessage(message);
		}

	}

	private void loadExampleProjectsInCombo() {
		final String[] abbreviations = ExampleProject.getAbbreviations();
		for (String abbreviation : abbreviations) {
			jComboBoxExampleProjects.addItem(abbreviation);
		}
	}

	private void loadOntologies() {
		// OntologyLoaderTask.getCvManager();
		new OntologyLoaderWaiter().execute();
	}

	private void writeErrorMessage(String message) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>Error initializating the tool: <b>" + message
				+ "</b><br/>Try to restart and if the problem persist, contact to 'salvador@scripps.edu'</html>");
		// this.jLabelInit.setText(sb.toString());

		// cancel tasks
		if (ontologyLoader != null)
			ontologyLoader.cancel(true);
		if (updateTask != null)
			updateTask.cancel(true);

		// show error message
		JOptionPane.showMessageDialog(this, sb.toString(), "Error loading", JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void dispose() {
		if (ontologyLoader != null) {
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {

				}
				boolean canceled = ontologyLoader.cancel(true);
				if (canceled || ontologyLoader.getState() != StateValue.STARTED)
					break;
			}
		}
		if (updateTask != null)
			updateTask.cancel(true);

		super.dispose();
	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		jPanel1 = new javax.swing.JPanel();
		jMenuBar1 = new javax.swing.JMenuBar();
		jMenu1 = new javax.swing.JMenu();
		jMenuItemExit = new javax.swing.JMenuItem();
		jMenu2 = new javax.swing.JMenu();
		jMenuItemStandard2MIAPE = new javax.swing.JMenuItem();
		jMenuItemBatchMiapeExtraction = new javax.swing.JMenuItem();
		jMenu3 = new javax.swing.JMenu();
		jMenuItemStartProjectComparison = new javax.swing.JMenuItem();
		jMenuHelp = new javax.swing.JMenu();
		jMenuItemMIAPEExtractionTutorial = new javax.swing.JMenuItem();
		jMenuItemMIAPEExtractionBatchTutorial = new javax.swing.JMenuItem();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("PACOM - Proteomics Assay COMparator");
		setResizable(false);

		jMenu1.setText("Exit");

		jMenuItemExit.setAccelerator(
				javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
		jMenuItemExit.setText("Exit");
		jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				exit(evt);
			}
		});
		jMenu1.add(jMenuItemExit);

		jMenuBar1.add(jMenu1);

		jMenu2.setText("Import data");
		jMenu2.setToolTipText(dataImportToolTip);

		jMenuItemStandard2MIAPE.setAccelerator(
				javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.ALT_MASK));
		jMenuItemStandard2MIAPE.setText("Go to Import data");
		jMenuItemStandard2MIAPE.setToolTipText(dataImportToolTip);
		jMenuItemStandard2MIAPE.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItemStandard2MIAPEActionPerformed(evt);
			}
		});
		jMenu2.add(jMenuItemStandard2MIAPE);

		jMenuItemBatchMiapeExtraction.setAccelerator(
				javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.ALT_MASK));
		jMenuItemBatchMiapeExtraction.setText("Go to Batch Import data");
		jMenuItemBatchMiapeExtraction.setToolTipText(batchDataImportToolTip);
		jMenuItemBatchMiapeExtraction.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItemBatchMiapeExtractionActionPerformed(evt);
			}
		});
		jMenu2.add(jMenuItemBatchMiapeExtraction);

		jMenuBar1.add(jMenu2);

		jMenu3.setText("Data inspection");

		jMenuItemStartProjectComparison.setAccelerator(
				javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.ALT_MASK));
		jMenuItemStartProjectComparison.setText("Go to Data Inspection");
		jMenuItemStartProjectComparison.setToolTipText(dataInspectionTooltip);
		jMenuItemStartProjectComparison.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItemStartProjectComparisonActionPerformed(evt);
			}
		});
		jMenu3.add(jMenuItemStartProjectComparison);

		jMenuBar1.add(jMenu3);

		jMenuHelp.setText("Help");

		jMenuItemMIAPEExtractionTutorial.setAccelerator(
				javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.ALT_MASK));
		jMenuItemMIAPEExtractionTutorial.setText("PACOM Tutorial");
		jMenuItemMIAPEExtractionTutorial.setToolTipText("Download the PACOM Tutorial (PDF)");
		jMenuItemMIAPEExtractionTutorial.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItemMIAPEExtractionTutorialActionPerformed(evt);
			}
		});

		mntmShowHelpDialog = new JMenuItem("Show help dialog");
		mntmShowHelpDialog.setToolTipText("Click here to open the help dialog of this page");
		mntmShowHelpDialog.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				showAttachedHelpDialog();

			}
		});
		mntmShowHelpDialog.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.ALT_MASK));
		jMenuHelp.add(mntmShowHelpDialog);
		jMenuHelp.add(jMenuItemMIAPEExtractionTutorial);

		jMenuItemMIAPEExtractionBatchTutorial.setAccelerator(
				javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.ALT_MASK));
		jMenuItemMIAPEExtractionBatchTutorial.setText("Batch Import data Tutorial");
		jMenuItemMIAPEExtractionBatchTutorial.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItemMIAPEExtractionBatchTutorialActionPerformed(evt);
			}
		});
		jMenuHelp.add(jMenuItemMIAPEExtractionBatchTutorial);

		jMenuBar1.add(jMenuHelp);

		setJMenuBar(jMenuBar1);

		jButtonShowHelp = new JButton("");
		jButtonShowHelp.setToolTipText("Click here to open the help dialog of this page");
		jButtonShowHelp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (getHelpDialog().isVisible()) {
					getHelpDialog().setMinimized(true);
					getHelpDialog().setVisible(false);
				} else {
					showAttachedHelpDialog();
				}
			}
		});
		jButtonShowHelp.setIcon(ImageManager.getImageIcon(ImageManager.HELP_ICON));
		jButtonShowHelp.setPressedIcon(ImageManager.getImageIcon(ImageManager.HELP_ICON_CLICKED));
		jButtonShowHelp.setRolloverIcon(ImageManager.getImageIcon(ImageManager.HELP_ICON));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, 800, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGroup(Alignment.TRAILING, layout.createSequentialGroup().addContainerGap(735, Short.MAX_VALUE)
						.addComponent(jButtonShowHelp).addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, 300, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(jButtonShowHelp)));
		GridBagLayout gbl_jPanel1 = new GridBagLayout();
		gbl_jPanel1.columnWidths = new int[] { 0, 0, 0, 0, 161 };
		gbl_jPanel1.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0 };
		jPanel1.setLayout(gbl_jPanel1);

		JLabel lblExamples = new JLabel("Examples project");
		lblExamples.setVerticalAlignment(SwingConstants.TOP);
		lblExamples.setHorizontalAlignment(SwingConstants.LEFT);
		lblExamples.setFont(new Font("Dialog", Font.PLAIN, 18));
		GridBagConstraints gbc_lblExamples = new GridBagConstraints();
		gbc_lblExamples.insets = new Insets(10, 10, 10, 10);
		gbc_lblExamples.gridx = 4;
		gbc_lblExamples.gridy = 1;
		jPanel1.add(lblExamples, gbc_lblExamples);
		JLabel jLabelInit = new javax.swing.JLabel("Import data");
		jLabelInit.setHorizontalAlignment(SwingConstants.LEFT);
		jLabelInit.setFont(new Font("Dialog", Font.PLAIN, 18));
		jLabelInit.setVerticalAlignment(javax.swing.SwingConstants.TOP);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(10, 10, 10, 10);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		jPanel1.add(jLabelInit, c);
		JLabel jLabelInit2 = new javax.swing.JLabel("Batch import data");
		jLabelInit2.setHorizontalAlignment(SwingConstants.LEFT);
		jLabelInit2.setFont(new Font("Dialog", Font.PLAIN, 18));
		jLabelInit2.setVerticalAlignment(javax.swing.SwingConstants.TOP);
		GridBagConstraints c2 = new GridBagConstraints();
		c2.fill = GridBagConstraints.FIRST_LINE_START;
		c2.insets = new Insets(10, 10, 10, 10);
		c2.gridx = 1;
		c2.gridy = 1;
		c2.gridwidth = 1;
		jPanel1.add(jLabelInit2, c2);
		JLabel jLabelInit3 = new javax.swing.JLabel("Data Inspection");
		jLabelInit3.setHorizontalAlignment(SwingConstants.LEFT);
		jLabelInit3.setFont(new Font("Dialog", Font.PLAIN, 18));
		jLabelInit3.setVerticalAlignment(javax.swing.SwingConstants.TOP);
		GridBagConstraints c3 = new GridBagConstraints();
		c3.fill = GridBagConstraints.FIRST_LINE_START;
		c3.insets = new Insets(10, 10, 10, 10);
		c3.gridx = 2;
		c3.gridy = 1;
		c3.gridwidth = 1;
		jPanel1.add(jLabelInit3, c3);
		GridBagConstraints c4 = new GridBagConstraints();
		c4.gridheight = 2;
		c4.fill = GridBagConstraints.HORIZONTAL;
		c4.insets = new Insets(10, 10, 10, 10);
		c4.gridx = 0;
		c4.gridy = 3;
		c4.gridwidth = 1;
		JButton loadButton = new JButton();
		loadButton.setIcon(ImageManager.getImageIcon(ImageManager.LOAD_LOGO_128));
		loadButton.setPressedIcon(ImageManager.getImageIcon(ImageManager.LOAD_LOGO_128_CLICKED));
		loadButton.setRolloverIcon(ImageManager.getImageIcon(ImageManager.LOAD_LOGO_128_HOVER));

		loadButton.setFocusable(false);
		loadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startDataImport();

			}
		});
		loadButton.setToolTipText(dataImportToolTip);
		loadButton.setBorder(BorderFactory.createEmptyBorder());
		loadButton.setContentAreaFilled(false);
		loadButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		jPanel1.add(loadButton, c4);
		//
		GridBagConstraints c5 = new GridBagConstraints();
		c5.gridheight = 2;
		c5.fill = GridBagConstraints.HORIZONTAL;
		c5.insets = new Insets(10, 10, 10, 10);
		c5.gridx = 1;
		c5.gridy = 3;
		c5.gridwidth = 1;
		JButton batchLoadButton = new JButton();
		batchLoadButton.setIcon(ImageManager.getImageIcon(ImageManager.BATCH_LOAD_LOGO_128));
		batchLoadButton.setPressedIcon(ImageManager.getImageIcon(ImageManager.BATCH_LOAD_LOGO_128_CLICKED));
		batchLoadButton.setRolloverIcon(ImageManager.getImageIcon(ImageManager.BATCH_LOAD_LOGO_128_HOVER));

		batchLoadButton.setFocusable(false);
		batchLoadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showBatchMiapeExtractionFrame();

			}
		});
		batchLoadButton.setToolTipText(batchDataImportToolTip);
		batchLoadButton.setBorder(BorderFactory.createEmptyBorder());
		batchLoadButton.setContentAreaFilled(false);
		batchLoadButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		jPanel1.add(batchLoadButton, c5);
		//
		GridBagConstraints c6 = new GridBagConstraints();
		c6.gridheight = 2;
		c6.fill = GridBagConstraints.HORIZONTAL;
		c6.insets = new Insets(10, 10, 10, 10);
		c6.gridx = 2;
		c6.gridy = 3;
		c6.gridwidth = 1;
		JButton inspectButton = new JButton();
		inspectButton.setIcon(ImageManager.getImageIcon(ImageManager.PACOM_LOGO_128));
		inspectButton.setPressedIcon(ImageManager.getImageIcon(ImageManager.PACOM_LOGO_128_CLICKED));
		inspectButton.setRolloverIcon(ImageManager.getImageIcon(ImageManager.PACOM_LOGO_128_HOVER));
		inspectButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		inspectButton.setFocusable(false);
		inspectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startProjectComparison();
			}
		});
		inspectButton.setToolTipText(dataInspectionTooltip);
		inspectButton.setBorder(BorderFactory.createEmptyBorder());
		inspectButton.setContentAreaFilled(false);
		jPanel1.add(inspectButton, c6);

		jButtonExample1 = new JButton();
		jButtonExample1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jButtonExampleClicked();
			}
		});

		jComboBoxExampleProjects = new JComboBox<String>();
		jComboBoxExampleProjects.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exampleProjectSelected();
			}
		});
		jComboBoxExampleProjects.setToolTipText("Select one of the available example datasets");
		GridBagConstraints gbc_jComboBoxExampleProjects = new GridBagConstraints();
		gbc_jComboBoxExampleProjects.insets = new Insets(0, 0, 5, 0);
		gbc_jComboBoxExampleProjects.gridx = 4;
		gbc_jComboBoxExampleProjects.gridy = 3;
		jPanel1.add(jComboBoxExampleProjects, gbc_jComboBoxExampleProjects);
		jButtonExample1.setToolTipText("<html>Click here to directly load one of the available examples</html>");
		jButtonExample1.setCursor(new Cursor(Cursor.HAND_CURSOR));
		jButtonExample1.setVerticalAlignment(SwingConstants.TOP);
		jButtonExample1.setIcon(ImageManager.getImageIcon(ImageManager.PACOM_LOGO_64));
		jButtonExample1.setPressedIcon(ImageManager.getImageIcon(ImageManager.PACOM_LOGO_64_CLICKED));
		jButtonExample1.setRolloverIcon(ImageManager.getImageIcon(ImageManager.PACOM_LOGO_64_HOVER));
		jButtonExample1.setFocusable(false);
		jButtonExample1.setContentAreaFilled(false);
		jButtonExample1.setBorder(BorderFactory.createEmptyBorder());
		GridBagConstraints gbc_jButtonExample1 = new GridBagConstraints();
		gbc_jButtonExample1.gridy = 4;
		gbc_jButtonExample1.gridx = 4;
		jPanel1.add(jButtonExample1, gbc_jButtonExample1);
		getContentPane().setLayout(layout);

		pack();
		java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		java.awt.Dimension dialogSize = getSize();
		setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);
	}// </editor-fold>

	protected void exampleProjectSelected() {
		String exampleProject = jComboBoxExampleProjects.getSelectedItem().toString();
		if ("".equals(exampleProject)) {
			jButtonExample1.setIcon(ImageManager.getImageIcon(ImageManager.PACOM_LOGO_64_CLICKED));
			jButtonExample1.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		} else {
			jButtonExample1.setIcon(ImageManager.getImageIcon(ImageManager.PACOM_LOGO_64));
			jButtonExample1.setCursor(new Cursor(Cursor.HAND_CURSOR));
		}

	}

	protected void jButtonExampleClicked() {
		String exampleProject = jComboBoxExampleProjects.getSelectedItem().toString();
		if (!"".equals(exampleProject)) {
			loadExample(ExampleProject.getByAbbreviation(exampleProject).getName());
		}
	}

	private void loadExample(String projectName) {
		String projectXMLFilePath = FileManager.getProjectXMLFilePath(projectName);
		File projectFile = new File(projectXMLFilePath);
		if (!projectFile.exists()) {
			// appendStatus("Project '" + projectName + "' has not found in the
			// projects folder");
			return;
		}
		try {
			log.info("Reading project configuration file: " + projectName);
			// setStatus("Loading project: " + projectName + "...");
			Miape2ExperimentListDialog.getInstance(this).initializeProjectTree(projectFile);
			Miape2ExperimentListDialog.getInstance(this).setCurrentCgfFile(projectFile);
			Miape2ExperimentListDialog.getInstance(this).showChartManager();
			log.info("Project configuration file readed");
			this.setVisible(false);
		} catch (Exception e) {
			e.printStackTrace();
			log.warn(e.getMessage());
			return;
		}
	}

	public void startDataImport() {
		setVisible(false);
		MiapeExtractionFrame standard2MIAPEDialog = MiapeExtractionFrame.getInstance(this, true);
		standard2MIAPEDialog.setVisible(true);

	}

	// GEN-END:initComponents

	private void jMenuItemMIAPEExtractionBatchTutorialActionPerformed(java.awt.event.ActionEvent evt) {
		showMiapeExtractionBatchTutorial();
	}

	private void showMiapeExtractionBatchTutorial() {
		final int showConfirmDialog = JOptionPane.showConfirmDialog(this,
				"Do you want to open a browser window to go to the PACOM Wiki file?", "Go to WIKI",
				JOptionPane.YES_NO_OPTION);
		if (showConfirmDialog == JOptionPane.YES_OPTION) {
			try {
				openBrowser(URL_MIAPE_EXTRACTOR_BATCH_TUTORIAL);
			} catch (IOException e) {

			} catch (URISyntaxException e) {

			}
		}

	}

	private void jMenuItemBatchMiapeExtractionActionPerformed(java.awt.event.ActionEvent evt) {
		showBatchMiapeExtractionFrame();
	}

	private void showBatchMiapeExtractionFrame() {
		setVisible(false);
		miapeExtractionBatchFrame = MiapeExtractionBatchFrame.getInstace(this);
		miapeExtractionBatchFrame.setVisible(true);

	}

	private void jMenuItemMIAPEExtractionTutorialActionPerformed(java.awt.event.ActionEvent evt) {
		showMIAPEExtractionTutorial();
	}

	private void showMIAPEExtractionTutorial() {
		final int showConfirmDialog = JOptionPane.showConfirmDialog(this,
				"Do you want to open a browser window to go to the PACOM Wiki page?", "Go to WIKI",
				JOptionPane.YES_NO_OPTION);
		if (showConfirmDialog == JOptionPane.YES_OPTION) {
			try {
				openBrowser(URL_MIAPE_EXTRACTOR_TUTORIAL);
			} catch (IOException e) {

			} catch (URISyntaxException e) {

			}
		}
	}

	private void jMenuItemStartProjectComparisonActionPerformed(java.awt.event.ActionEvent evt) {
		startProjectComparison();
	}

	public void startProjectComparison() {
		setVisible(false);
		miape2experimentListDialog = Miape2ExperimentListDialog.getInstance(this);
		if (miape2experimentListDialog.isCorrectlyInitialized())
			miape2experimentListDialog.setVisible(true);

	}

	private void exit(java.awt.event.ActionEvent evt) {
		dispose();
	}

	private void jMenuItemStandard2MIAPEActionPerformed(java.awt.event.ActionEvent evt) {
		startDataImport();

	}

	public void openBrowser(String url) throws IOException, URISyntaxException {
		java.awt.Desktop browser = java.awt.Desktop.getDesktop();
		java.net.URI location = new java.net.URI(url);
		browser.browse(location);
	}

	// public static void autoScroll(JScrollPane jScrollPane,
	// final JTextArea jTextArea) {
	// jScrollPane.getVerticalScrollBar().addAdjustmentListener(
	// new AdjustmentListener() {
	// @Override
	// public void adjustmentValueChanged(AdjustmentEvent e) {
	// jTextArea.select(jTextArea.getHeight() + 1000, 0);
	// }
	// });
	// }

	public static String getHash(String password, byte[] salt)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.reset();

		return digest.digest(password.getBytes()).toString();
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {

		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
					new MainFrame().setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	// GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.JMenu jMenu1;
	private javax.swing.JMenu jMenu2;
	private javax.swing.JMenu jMenu3;
	private javax.swing.JMenuBar jMenuBar1;
	private javax.swing.JMenu jMenuHelp;
	private javax.swing.JMenuItem jMenuItemBatchMiapeExtraction;
	private javax.swing.JMenuItem jMenuItemExit;
	private javax.swing.JMenuItem jMenuItemMIAPEExtractionBatchTutorial;
	private javax.swing.JMenuItem jMenuItemMIAPEExtractionTutorial;
	private javax.swing.JMenuItem jMenuItemStandard2MIAPE;
	private javax.swing.JMenuItem jMenuItemStartProjectComparison;
	private javax.swing.JPanel jPanel1;
	private JButton jButtonShowHelp;
	private JMenuItem mntmShowHelpDialog;
	private JComboBox<String> jComboBoxExampleProjects;
	private JButton jButtonExample1;

	// End of variables declaration//GEN-END:variables

	public static AppVersion getVersion() {
		if (version == null) {
			try {
				String tmp = PropertiesReader.getProperties(APP_PROPERTIES).getProperty("assembly.dir");
				if (tmp.contains("v")) {
					version = new AppVersion(tmp.split("v")[1]);
				} else {
					version = new AppVersion(tmp);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return version;

	}

	@Override
	public List<String> getHelpMessages() {
		String[] array = { "This is the main window in PACOM", //
				"From here you can: ", //
				"", //
				"- click on <b>Import data</b> to go import datasets into PACOM", //
				"", //
				"- click on <b>Batch import data</b> to import several datasets in batch", //
				"", //
				"- click on <b>Data Inspection</b> to open the comparison project builder", //
				"", //
				"- select and click one of the available <b>example projects</b> and directly start to visualize the data." };
		return Arrays.asList(array);
	}

}