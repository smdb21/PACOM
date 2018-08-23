/*
 * MainFrame.java Created on __DATE__, __TIME__
 */

package org.proteored.pacom.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker.StateValue;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.pacom.analysis.gui.Miape2ExperimentListDialog;
import org.proteored.pacom.analysis.util.FileManager;
import org.proteored.pacom.gui.tasks.CheckUpdateTask;
import org.proteored.pacom.gui.tasks.OntologyLoaderTask;
import org.proteored.pacom.gui.tasks.OntologyLoaderWaiter;
import org.proteored.pacom.utils.AppVersion;
import org.proteored.pacom.utils.ExampleProject;
import org.proteored.pacom.utils.PropertiesReader;

import com.compomics.util.general.MassCalc;

import edu.scripps.yates.utilities.dates.DatesUtil;
import umich.ms.fileio.filetypes.pepxml.jaxb.standard.MsmsRunSummary;

/**
 *
 * @author __USER__
 */
public class MainFrame extends AbstractJFrameWithAttachedHelpDialog implements PropertyChangeListener {

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
	private final static String dataInspectionTooltip = "<html><br><ul><li>Inspect your data creating your own inspection projects.</li><li>Compare complex experiments in an intuitive way.</li><li>Get a lot of charts representing qualitative data from your experiments.</li><li>Filter data applying several filters (FDR, score thresholds, etc...)</li><li>Export your data into PRIDE XML format</li></ul><br></html>";
	private final static String dataImportToolTip = "<html><br>Extract and import datasets from input data files such as:<br><ul><li>mzIdentML</li><li>mzML</li><li>pepXML</li><li>PRIDE XML</li><li>X!Tandem output XML</li> <li>DTASelect output</li><li>Separated values tables</li></ul><br></html>";

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
		super(300);
		setResizable(false);
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
					final String suffix = " (v" + version.toString() + ")";
					if (!getTitle().endsWith(suffix))
						setTitle(getTitle() + suffix);
				}
			} catch (final Exception e1) {
			}

			// load ontologies
			loadOntologies();

			// load other resources
			loadOtherResources();

			// load exampleprojects
			loadExampleProjectsInCombo();

			// show help
			showAttachedHelpDialog();

		} catch (final Exception e) {
			e.printStackTrace();
			String message = "";
			if (e.getMessage() != null
					&& (e.getMessage().startsWith("XML reader error") || e.getMessage().contains("Failed to access"))) {
				message = "Webservice is unreachable.";
			} else {
				message = e.getMessage();
			}
			writeErrorMessage(message);
		}

	}

	private void loadOtherResources() {
		final Runnable background = new Runnable() {

			@Override
			public void run() {
				final long t1 = System.currentTimeMillis();
				try {
					// this is done because when first creating this object, it
					// will load the masses tables from resource files. If this
					// is not done first, and it is done by various threads at
					// the same time, it happens that it could fail to load the
					// whole set of masses first and then, get an error trying
					// to calculate sequence masses
					log.info("Loading aminoacid masses...");
					final MassCalc massCalc = new MassCalc(MassCalc.MONOAA);
					log.info("Aminoacid masses loaded.");
					try {
						JAXBContext.newInstance(MsmsRunSummary.class);
					} catch (final JAXBException e) {
						e.printStackTrace();
					}
				} finally {
					final long t2 = System.currentTimeMillis();
					log.info("Other resources loaded in  " + DatesUtil.getDescriptiveTimeFromMillisecs(t2 - t1));
				}
			}
		};
		final Thread backgroundThread = new Thread(background);
		backgroundThread.start();
	}

	private void loadExampleProjectsInCombo() {
		final String[] abbreviations = ExampleProject.getAbbreviations();
		for (final String abbreviation : abbreviations) {
			jComboBoxExampleProjects.addItem(abbreviation);
		}
	}

	private void loadOntologies() {
		// OntologyLoaderTask.getCvManager();
		final OntologyLoaderWaiter ontologyLoaderWaiter = new OntologyLoaderWaiter();
		ontologyLoaderWaiter.addPropertyChangeListener(this);
		ontologyLoaderWaiter.execute();
	}

	private void writeErrorMessage(String message) {
		final StringBuilder sb = new StringBuilder();
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
				} catch (final InterruptedException e) {

				}
				final boolean canceled = ontologyLoader.cancel(true);
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
		jMenuBar1.setMargin(new Insets(5, 5, 5, 0));
		jMenu1 = new javax.swing.JMenu();
		jMenuItemExit = new javax.swing.JMenuItem();
		jMenu2 = new javax.swing.JMenu();
		jMenuItemStandard2MIAPE = new javax.swing.JMenuItem();
		// jMenuItemBatchMiapeExtraction = new javax.swing.JMenuItem();
		jMenu3 = new javax.swing.JMenu();
		jMenuItemStartProjectComparison = new javax.swing.JMenuItem();
		jMenuHelp = new javax.swing.JMenu();
		jMenuItemMIAPEExtractionTutorial = new javax.swing.JMenuItem();
		jMenuItemMIAPEExtractionBatchTutorial = new javax.swing.JMenuItem();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("PACOM - Proteomics Assay COMparator");

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

		// jMenuItemBatchMiapeExtraction.setAccelerator(
		// javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B,
		// java.awt.event.InputEvent.ALT_MASK));
		// jMenuItemBatchMiapeExtraction.setText("Go to Batch Import data");
		// jMenuItemBatchMiapeExtraction.setToolTipText(batchDataImportToolTip);
		// jMenuItemBatchMiapeExtraction.addActionListener(new
		// java.awt.event.ActionListener() {
		// @Override
		// public void actionPerformed(java.awt.event.ActionEvent evt) {
		// jMenuItemBatchMiapeExtractionActionPerformed(evt);
		// }
		// });
		// jMenu2.add(jMenuItemBatchMiapeExtraction);

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
		jMenuItemMIAPEExtractionTutorial.setText("PACOM tutorial (main wiki page)");
		jMenuItemMIAPEExtractionTutorial.setToolTipText("Go to the main PACOM wiki page");
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
		jMenuItemMIAPEExtractionBatchTutorial.setText("Import data Tutorial (wiki page)");
		jMenuItemMIAPEExtractionBatchTutorial
				.setToolTipText("Go to the PACOM wiki page where how to import datasets is explained.");

		jMenuItemMIAPEExtractionBatchTutorial.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItemMIAPEExtractionBatchTutorialActionPerformed(evt);
			}
		});
		jMenuHelp.add(jMenuItemMIAPEExtractionBatchTutorial);

		jMenuBar1.add(jMenuHelp);

		setJMenuBar(jMenuBar1);
		final GridBagLayout gbl_jPanel1 = new GridBagLayout();
		gbl_jPanel1.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0 };
		gbl_jPanel1.rowHeights = new int[] { 30, 0, 10, 0, 0 };
		gbl_jPanel1.columnWidths = new int[] { 0, 10, 30, 0, 30, 161, 0 };
		gbl_jPanel1.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		jPanel1.setLayout(gbl_jPanel1);

		final JLabel lblExamples = new JLabel("Inspect examples");
		lblExamples.setVerticalAlignment(SwingConstants.TOP);
		lblExamples.setHorizontalAlignment(SwingConstants.LEFT);
		lblExamples.setFont(new Font("Dialog", Font.PLAIN, 18));
		final GridBagConstraints gbc_lblExamples = new GridBagConstraints();
		gbc_lblExamples.insets = new Insets(10, 10, 10, 10);
		gbc_lblExamples.gridx = 5;
		gbc_lblExamples.gridy = 1;
		jPanel1.add(lblExamples, gbc_lblExamples);
		final JLabel jLabelInit = new javax.swing.JLabel("Import data");
		jLabelInit.setHorizontalAlignment(SwingConstants.LEFT);
		jLabelInit.setFont(new Font("Dialog", Font.PLAIN, 18));
		jLabelInit.setVerticalAlignment(javax.swing.SwingConstants.TOP);
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(10, 10, 10, 10);
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		jPanel1.add(jLabelInit, c);
		final JLabel jLabelInit3 = new javax.swing.JLabel("Inspect data");
		jLabelInit3.setHorizontalAlignment(SwingConstants.CENTER);
		jLabelInit3.setFont(new Font("Dialog", Font.PLAIN, 18));
		jLabelInit3.setVerticalAlignment(javax.swing.SwingConstants.TOP);
		final GridBagConstraints c3 = new GridBagConstraints();
		c3.insets = new Insets(10, 10, 10, 10);
		c3.gridx = 3;
		c3.gridy = 1;
		c3.gridwidth = 1;
		jPanel1.add(jLabelInit3, c3);
		final GridBagConstraints c4 = new GridBagConstraints();
		c4.gridheight = 2;
		c4.fill = GridBagConstraints.HORIZONTAL;
		c4.insets = new Insets(10, 10, 10, 10);
		c4.gridx = 1;
		c4.gridy = 3;
		c4.gridwidth = 1;
		final JButton loadButton = new JButton();
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
		final GridBagConstraints c6 = new GridBagConstraints();
		c6.gridheight = 2;
		c6.fill = GridBagConstraints.HORIZONTAL;
		c6.insets = new Insets(10, 10, 10, 10);
		c6.gridx = 3;
		c6.gridy = 3;
		c6.gridwidth = 1;
		final JButton inspectButton = new JButton();
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
		getContentPane().setLayout(new BorderLayout(0, 5));
		jComboBoxExampleProjects.setToolTipText("Select one of the available example datasets");
		final GridBagConstraints gbc_jComboBoxExampleProjects = new GridBagConstraints();
		gbc_jComboBoxExampleProjects.insets = new Insets(0, 0, 5, 5);
		gbc_jComboBoxExampleProjects.gridx = 5;
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
		final GridBagConstraints gbc_jButtonExample1 = new GridBagConstraints();
		gbc_jButtonExample1.insets = new Insets(0, 0, 5, 5);
		gbc_jButtonExample1.gridy = 4;
		gbc_jButtonExample1.gridx = 5;
		jPanel1.add(jButtonExample1, gbc_jButtonExample1);
		getContentPane().add(jPanel1, BorderLayout.CENTER);

		panel = new JPanel();
		final FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		jButtonShowHelp = new OpenHelpButton(this);
		panel.add(jButtonShowHelp);

		pack();
		final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		final java.awt.Dimension dialogSize = getSize();
		setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);
	}// </editor-fold>

	protected void exampleProjectSelected() {
		final String exampleProject = jComboBoxExampleProjects.getSelectedItem().toString();
		if ("".equals(exampleProject)) {
			jButtonExample1.setIcon(ImageManager.getImageIcon(ImageManager.PACOM_LOGO_64_CLICKED));
			jButtonExample1.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		} else {
			jButtonExample1.setIcon(ImageManager.getImageIcon(ImageManager.PACOM_LOGO_64));
			jButtonExample1.setCursor(new Cursor(Cursor.HAND_CURSOR));
		}

	}

	protected void jButtonExampleClicked() {
		final String exampleProject = jComboBoxExampleProjects.getSelectedItem().toString();
		if (!"".equals(exampleProject)) {
			loadExample(ExampleProject.getByAbbreviation(exampleProject).getName());
		}
	}

	private void loadExample(String projectName) {
		final String projectXMLFilePath = FileManager.getProjectXMLFilePath(projectName);
		final File projectFile = new File(projectXMLFilePath);
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
			setVisible(false);
		} catch (final Exception e) {
			e.printStackTrace();
			log.warn(e.getMessage());
			return;
		}
	}

	public void startDataImport() {
		setVisible(false);
		final MiapeExtractionFrame standard2MIAPEDialog = MiapeExtractionFrame.getInstance(this, true);
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
			} catch (final IOException e) {

			} catch (final URISyntaxException e) {

			}
		}

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
			} catch (final IOException e) {

			} catch (final URISyntaxException e) {

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
		final java.awt.Desktop browser = java.awt.Desktop.getDesktop();
		final java.net.URI location = new java.net.URI(url);
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
		final MessageDigest digest = MessageDigest.getInstance("SHA-256");
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
				} catch (final Exception e) {
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
	private JPanel panel;

	// End of variables declaration//GEN-END:variables

	public static AppVersion getVersion() {
		if (version == null) {
			try {
				final String tmp = PropertiesReader.getProperties(APP_PROPERTIES).getProperty("assembly.dir");
				if (tmp.contains("v")) {
					version = new AppVersion(tmp.split("v")[1]);
				} else {
					version = new AppVersion(tmp);
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return version;

	}

	@Override
	public List<String> getHelpMessages() {
		final String[] array = { "This is the main window in PACOM", //
				"From here you can: ", //

				"- click on <b>Import data</b> to import one or more datasets into PACOM <i>(Alt+E)</i>,", //

				"- click on <b>Inspect data</b> to open the comparison project builder <i>(Alt+I)</i>,", //

				"- select and click one of the available <b>example projects</b> and directly start to visualize data.", //

				"You can also go to the main PACOM wiki page by selecting the menu <i>Help->PACOM tutorial (main wiki page)</i>, or go to the PACOM wiki pages that"
						+ " explains how to import datasets by selecting the menu <i>Help->Import data Tutorial (wiki page)</i>"

		};
		return Arrays.asList(array);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (OntologyLoaderWaiter.ONTOLOGY_LOADING_ERROR.equals(evt.getPropertyName())) {
			writeErrorMessage(
					"Error loading ontologies. Please note that this could lead to a non expected behaviour of the tool.");

		} else if (OntologyLoaderWaiter.ONTOLOGY_LOADING_NETWORK_ERROR.equals(evt.getPropertyName())) {
			writeErrorMessage("Error trying to connect to the Internet.<br>"
					+ "PACom uses the internet connection to retrieve some resources, collect some protein annotations and check for new updates.<br>"
					+ "Please check your internet connection or institution firewall and run the software again.");

		} else if (OntologyLoaderWaiter.ONTOLOGY_LOADED.equals(evt.getPropertyName())) {
			final Object newValue = evt.getNewValue();
			if (newValue instanceof ControlVocabularyManager) {
				log.info("Ontologies loaded. Now loading metadata templates");
				EventQueue.invokeLater(new Runnable() {

					@Override
					public void run() {
						final long t0 = System.currentTimeMillis();
						log.info("Loading metadata templates");
						FileManager.getMetadataTemplateList((ControlVocabularyManager) newValue);
						log.info("Metadata templates loaded in "
								+ DatesUtil.getDescriptiveTimeFromMillisecs(System.currentTimeMillis() - t0));
					}
				});
				log.debug("Metadata templates will be loaded after all events are processed");
			}
		}

	}

}