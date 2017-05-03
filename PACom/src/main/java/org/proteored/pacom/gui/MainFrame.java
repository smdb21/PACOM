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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.proteored.miapeapi.interfaces.Software;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeAPIWebserviceDelegate;
import org.proteored.miapeapi.webservice.clients.miapeextractor.MiapeExtractorDelegate;
import org.proteored.pacom.analysis.gui.Miape2ExperimentListDialog;
import org.proteored.pacom.analysis.gui.tasks.MiapeRetrieverManager;
import org.proteored.pacom.gui.tasks.CheckUpdateTask;
import org.proteored.pacom.gui.tasks.LoginTask;
import org.proteored.pacom.gui.tasks.OntologyLoaderTask;
import org.proteored.pacom.gui.tasks.OntologyLoaderWaiter;
import org.proteored.pacom.utils.MiapeExtractorSoftware;
import org.proteored.pacom.utils.PropertiesReader;

/**
 *
 * @author __USER__
 */
public class MainFrame extends javax.swing.JFrame implements PropertyChangeListener {
	private LoginDialog loginFrame;

	public static String userName;
	public static String password;
	public int userID;
	// by default
	public static String miapetool_access_script = "http://www.proteored.org/acceso.asp?pmArea=8";

	private static final String URL_MIAPE_EXTRACTOR_TUTORIAL = "http://legacy.proteored.org/miape/MIAPE%20Extractor%20Tutorial.pdf";
	private static final String URL_MIAPE_EXTRACTOR_BATCH_TUTORIAL = "http://legacy.proteored.org/miape/Batch%20MIAPE%20Extraction_Tutorial.pdf";

	private static MiapeExtractorDelegate miapeExtractorWebservice;
	private static MiapeAPIWebserviceDelegate miapeAPIWebservice;

	// by default:
	public static String ftpPath = "ftp://proteo.cnb.csic.es/pub/tmp/";
	private static String version;
	public static boolean emailNotifications;
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("log4j.logger.org.proteored");
	private OntologyLoaderTask ontologyLoader;
	private CheckUpdateTask updateTask;
	private Miape2ExperimentListDialog miape2experimentListDialog;
	private static Boolean unattendedRetrieverEnabled;
	private MiapeExtractionBatchFrame miapeExtractionBatchFrame;

	private final boolean checkUpdates = false;
	public static boolean localWorkflow = false;

	public static File currentFolder = new File(".");

	/**
	 * Creates new form MainFrame
	 *
	 * @throws Exception
	 */
	public MainFrame() {
		/*
		 * MiapeMSDocument miapeMS; miapeMS =
		 * createDefault_4800MALDITOFTOF_MiapeMS(); System.out.println(
		 * "INFO despues del readMiapeMSMetadata: " +
		 * miapeMS.toXml().toString());
		 */
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		try {
			version = PropertiesReader.getProperties().getProperty(PropertiesReader.PACOM_VERSION);
			if (version != null) {
				String suffix = " (v" + version + ")";
				if (!getTitle().endsWith(suffix))
					setTitle(getTitle() + suffix);
			}
		} catch (Exception e1) {
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
				updateTask.addPropertyChangeListener(this);
				updateTask.execute();
			}
			initializeProperties();
			initComponents();

			// load ontologies
			loadOntologies();

			// show login dialog at startup
			if (!localWorkflow) {
				jMenuItemLoginActionPerformed(null);
			}
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

	private void loadOntologies() {
		// OntologyLoaderTask.getCvManager();
		new OntologyLoaderWaiter().execute();
	}

	private static void initializeProperties() throws Exception {
		if (localWorkflow) {
			return;
		}
		// Get properties from resource file
		Properties prop = PropertiesReader.getProperties();

		ftpPath = prop.getProperty(PropertiesReader.FTP_PATH);
		log.info("Reading properties file: ftpPath: " + ftpPath);

		emailNotifications = Boolean.parseBoolean(prop.getProperty(PropertiesReader.EMAIL_NOTIFICATIONS));
		log.info("Reading properties file: " + PropertiesReader.EMAIL_NOTIFICATIONS + ": " + emailNotifications);

		// instrumentResourceFileURL = prop
		// .getProperty("instruments.information.file");
		// log.info("Reading properties file: instruments.information.file: "
		// + instrumentResourceFileURL);

		miapetool_access_script = prop.getProperty(PropertiesReader.MIAPE_TOOL_ACCESS_SCRIPT);
		log.info("Reading properties file: " + PropertiesReader.MIAPE_TOOL_ACCESS_SCRIPT + ": "
				+ miapetool_access_script);

		unattendedRetrieverEnabled = Boolean
				.valueOf(prop.getProperty(PropertiesReader.MIAPE_EXTRACTOR_UNATENDEDRETRIEVER));
		log.info("Unattended retriver enabled=" + unattendedRetrieverEnabled);

		localWorkflow = Boolean.valueOf(prop.getProperty(PropertiesReader.LOCAL_WORKFLOW));
		log.info("Local workflow = " + localWorkflow);

	}

	private void writeErrorMessage(String message) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>Error initializating the tool: <b>" + message
				+ "</b><br/>Try to restart and if the problem persist, contact to 'miape_support@proteored.org'</html>");
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

		int miapesBeingLoaded = MiapeRetrieverManager.getInstance(userName, password).getSize();
		if (miapesBeingLoaded > 0) {
			int option = JOptionPane.showConfirmDialog(this,
					"<html>Some MIAPE MSIs (" + miapesBeingLoaded
							+ ") are being downloaded in background for its use in the comparisons.<br>"
							+ "If you close the tool these downloadings will be interrupted.<br>"
							+ "Do you want to close the tool and interrupt the downloading?</html>",
					"MIAPE MSI document being downloaded", JOptionPane.YES_NO_CANCEL_OPTION);
			if (option != JOptionPane.YES_OPTION)
				return;
		}
		super.dispose();
	}

	public static MiapeExtractorDelegate getMiapeExtractorWebservice() {
		if (miapeExtractorWebservice == null)
			try {
				initializeProperties();
				return miapeExtractorWebservice;

			} catch (Exception e) {
				e.printStackTrace();
			}
		return miapeExtractorWebservice;
	}

	public static MiapeAPIWebserviceDelegate getMiapeAPIWebservice() {
		if (miapeAPIWebservice == null)
			try {
				initializeProperties();
				return miapeAPIWebservice;
			} catch (Exception e) {
				e.printStackTrace();
			}
		return miapeAPIWebservice;
	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		jPanel1 = new javax.swing.JPanel();
		jMenuBar1 = new javax.swing.JMenuBar();
		jMenu1 = new javax.swing.JMenu();
		jMenuItemLogin = new javax.swing.JMenuItem();
		jMenuItemExit = new javax.swing.JMenuItem();
		jMenu2 = new javax.swing.JMenu();
		jMenuItemStandard2MIAPE = new javax.swing.JMenuItem();
		jMenuItemBatchMiapeExtraction = new javax.swing.JMenuItem();
		jMenu3 = new javax.swing.JMenu();
		jMenuItemStartProjectComparison = new javax.swing.JMenuItem();
		jMenuBrowseMIAPEs = new javax.swing.JMenu();
		jMenuItemBrowseMIAPEs = new javax.swing.JMenuItem();
		jMenu4 = new javax.swing.JMenu();
		jMenuItemShowQueries = new javax.swing.JMenuItem();
		jMenuHelp = new javax.swing.JMenu();
		jMenuItemMIAPEExtractionTutorial = new javax.swing.JMenuItem();
		jMenuItemMIAPEExtractionBatchTutorial = new javax.swing.JMenuItem();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("PACOM - Proteomics Assay COMparator");
		setResizable(false);

		if (!localWorkflow) {
			jMenu1.setText("Login");
			jMenuItemLogin.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L,
					java.awt.event.InputEvent.ALT_MASK));
			jMenuItemLogin.setText("Login");
			jMenuItemLogin.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jMenuItemLoginActionPerformed(evt);
				}
			});
			jMenu1.add(jMenuItemLogin);
		} else {
			jMenu1.setText("Exit");
		}
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
		jMenu2.setToolTipText("Import MIAPE information");

		jMenuItemStandard2MIAPE.setAccelerator(
				javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.ALT_MASK));
		jMenuItemStandard2MIAPE.setText("Import data");
		jMenuItemStandard2MIAPE.setToolTipText(
				"<html>\nExtract and import datasets from input data files such as:<br>\n<ul>\n<li>mzIdentML</li>\n<li>mzML</li>\n<li>PRIDE XML</li>\n<li>X!Tandem output XML</li>\n</ul>\n</html>");
		jMenuItemStandard2MIAPE.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItemStandard2MIAPEActionPerformed(evt);
			}
		});
		jMenu2.add(jMenuItemStandard2MIAPE);

		jMenuItemBatchMiapeExtraction.setAccelerator(
				javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.ALT_MASK));
		jMenuItemBatchMiapeExtraction.setText("Batch import data");
		jMenuItemBatchMiapeExtraction.setToolTipText(
				"<html> Batch import data from proteomics data files such as:<br> <ul> <li>mzIdentML</li> <li>mzML</li> <li>PRIDE XML</li> <li>X!Tandem output XML</li> </ul><br>\nUsing a batch miape extraction file, you can create multiple MIAPE documents in batch mode.</html>");
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
		jMenuItemStartProjectComparison.setText("Data inspection project");
		jMenuItemStartProjectComparison.setToolTipText(
				"<html>\n<ul>\n<li>Inspect your data creating your own inspection projects.</li>\n<li>Compare complex experiments in an intuitive way.</li>\n<li>Get a lot of charts representing qualitative data from your experiments.</li>\n<li>Filter data applying several filters (FDR, score thresholds, etc...)</li>\n<li>Prepare your data for a <a href=\"http://www.proteomexchange.org\">ProteomeXchange</a> submission</li>\n</ul>\n</html>");
		jMenuItemStartProjectComparison.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItemStartProjectComparisonActionPerformed(evt);
			}
		});
		jMenu3.add(jMenuItemStartProjectComparison);

		jMenuBar1.add(jMenu3);

		if (!localWorkflow) {
			jMenuBrowseMIAPEs.setText("MIAPE repository browser");
			jMenuBrowseMIAPEs.setEnabled(false);

			jMenuItemBrowseMIAPEs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R,
					java.awt.event.InputEvent.ALT_MASK));
			jMenuItemBrowseMIAPEs.setText("Browse over MIAPE repository");
			jMenuItemBrowseMIAPEs.setToolTipText(
					"<html>Browser over accesible MIAPE projects<br>\nfrom the ProteoRed MIAPE repository:<br>\n<ul>\n<li>Show MIAPE reports.</li>\n<li>Delete projects and documents.</li>\n</ul>\n</html>");
			jMenuItemBrowseMIAPEs.setEnabled(false);
			jMenuItemBrowseMIAPEs.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jMenuItemBrowseMIAPEsActionPerformed(evt);
				}
			});
			jMenuBrowseMIAPEs.add(jMenuItemBrowseMIAPEs);

			jMenuBar1.add(jMenuBrowseMIAPEs);

			jMenu4.setText("Queries");
			jMenu4.setToolTipText(
					"<html>\n<b>This is a <font color='red'>beta</font> version</b>.<br>\nMake queries in the ProteoRed MIAPE repository.<br>\nExamples:<br>\n<ul>\n<li>get all peptides identified with a certain sequence</li>\n<li>get all peptides identified from a certain protein accession</li>\n<li>retrieve spectra assigned to a certain peptide</li>\n</ul>\n</html>");
			jMenu4.setEnabled(false);

			jMenuItemShowQueries.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q,
					java.awt.event.InputEvent.ALT_MASK));
			jMenuItemShowQueries.setText("Go to queries dialog");
			jMenuItemShowQueries.setToolTipText(
					"<html> <b>This is a <font color='red'>beta</font> version</b>.<br> Make queries in the ProteoRed MIAPE repository.<br> Examples:<br> <ul> <li>get all peptides identified with a certain sequence</li> <li>get all peptides identified from a certain protein accession</li> <li>retrieve spectra assigned to a certain peptide</li> </ul> </html>");
			jMenuItemShowQueries.setEnabled(false);
			jMenuItemShowQueries.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jMenuItemShowQueriesActionPerformed(evt);
				}
			});
			jMenu4.add(jMenuItemShowQueries);

			jMenuBar1.add(jMenu4);
		}
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
		jMenuHelp.add(jMenuItemMIAPEExtractionTutorial);

		jMenuItemMIAPEExtractionBatchTutorial.setAccelerator(
				javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.ALT_MASK));
		jMenuItemMIAPEExtractionBatchTutorial.setText("Batch data import Tutorial");
		jMenuItemMIAPEExtractionBatchTutorial.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItemMIAPEExtractionBatchTutorialActionPerformed(evt);
			}
		});
		jMenuHelp.add(jMenuItemMIAPEExtractionBatchTutorial);

		jMenuBar1.add(jMenuHelp);

		setJMenuBar(jMenuBar1);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, 800, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, 300, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		jPanel1.setLayout(new GridBagLayout());
		JLabel jLabelInit = new javax.swing.JLabel("Import data");
		jLabelInit.setHorizontalAlignment(SwingConstants.LEFT);
		jLabelInit.setFont(new Font("Dialog", Font.PLAIN, 18));
		jLabelInit.setVerticalAlignment(javax.swing.SwingConstants.TOP);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(10, 10, 10, 10);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		jPanel1.add(jLabelInit, c);
		JLabel jLabelInit2 = new javax.swing.JLabel("Data Inspection");
		jLabelInit2.setHorizontalAlignment(SwingConstants.LEFT);
		jLabelInit2.setFont(new Font("Dialog", Font.PLAIN, 18));
		jLabelInit2.setVerticalAlignment(javax.swing.SwingConstants.TOP);
		c.fill = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(10, 10, 10, 10);
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		jPanel1.add(jLabelInit2, c);
		GridBagConstraints c2 = new GridBagConstraints();
		c2.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 10, 10, 10);
		c2.gridx = 0;
		c2.gridy = 1;
		c2.gridwidth = 1;
		JButton loadButton = new JButton();
		loadButton.setIcon(ImageManager.getImageIcon(ImageManager.LOAD_LOGO_128));
		loadButton.setPressedIcon(ImageManager.getImageIcon(ImageManager.LOAD_LOGO_128_CLICKED));
		loadButton.setRolloverIcon(ImageManager.getImageIcon(ImageManager.LOAD_LOGO_128_HOVER));

		loadButton.setFocusable(false);
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startDataImport();

			}
		});
		loadButton.setToolTipText("Import data");
		loadButton.setBorder(BorderFactory.createEmptyBorder());
		loadButton.setContentAreaFilled(false);
		loadButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		jPanel1.add(loadButton, c2);
		GridBagConstraints c3 = new GridBagConstraints();
		c3.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 10, 10, 10);
		c3.gridx = 1;
		c3.gridy = 1;
		c3.gridwidth = 1;
		JButton inspectButton = new JButton();
		inspectButton.setIcon(ImageManager.getImageIcon(ImageManager.PACOM_LOGO_128));
		inspectButton.setPressedIcon(ImageManager.getImageIcon(ImageManager.PACOM_LOGO_128_CLICKED));
		inspectButton.setRolloverIcon(ImageManager.getImageIcon(ImageManager.PACOM_LOGO_128_HOVER));
		inspectButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		inspectButton.setFocusable(false);
		inspectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startProjectComparison();

			}
		});
		inspectButton.setToolTipText("Data inspection");
		inspectButton.setBorder(BorderFactory.createEmptyBorder());
		inspectButton.setContentAreaFilled(false);
		jPanel1.add(inspectButton, c3);
		getContentPane().setLayout(layout);

		pack();
		java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		java.awt.Dimension dialogSize = getSize();
		setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);
	}// </editor-fold>

	private void startDataImport() {
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
				"Do you want to open a browser window to go to the Tutorial pdf file?", "Go to tutorial",
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
		miapeExtractionBatchFrame = MiapeExtractionBatchFrame.getInstace(this);
		miapeExtractionBatchFrame.setVisible(true);
		setVisible(false);
	}

	private void jMenuItemShowQueriesActionPerformed(java.awt.event.ActionEvent evt) {
		// TODO add your handling code here:
	}

	private void enableLoginDependentMenus(boolean b) {
		jMenuItemBrowseMIAPEs.setEnabled(b);
		jMenuBrowseMIAPEs.setEnabled(b);

	}

	private void jMenuItemMIAPEExtractionTutorialActionPerformed(java.awt.event.ActionEvent evt) {
		showMIAPEExtractionTutorial();
	}

	private void showMIAPEExtractionTutorial() {
		final int showConfirmDialog = JOptionPane.showConfirmDialog(this,
				"Do you want to open a browser window to go to the Tutorial pdf file?", "Go to tutorial",
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

	private void startProjectComparison() {
		setVisible(false);
		miape2experimentListDialog = Miape2ExperimentListDialog.getInstance(this);
		if (miape2experimentListDialog.isCorrectlyInitialized())
			miape2experimentListDialog.setVisible(true);

	}

	private void exit(java.awt.event.ActionEvent evt) {
		dispose();
	}

	private void jMenuItemBrowseMIAPEsActionPerformed(java.awt.event.ActionEvent evt) {

		showBrowseData();
	}

	private void showBrowseData() {
		ProteoRedMIAPEBrowserFrame miape2StandardDialog = ProteoRedMIAPEBrowserFrame.getInstance(this);
		miape2StandardDialog.setVisible(true);
		setVisible(false);

	}

	private void jMenuItemStandard2MIAPEActionPerformed(java.awt.event.ActionEvent evt) {
		startDataImport();

	}

	private void jMenuItemLoginActionPerformed(java.awt.event.ActionEvent evt) {
		showLogin();
	}

	private void showLogin() {
		if (!isVisible())
			setVisible(true);
		enableLoginDependentMenus(false);
		loginFrame = LoginDialog.getInstance(this, true, unattendedRetrieverEnabled);
		loginFrame.setVisible(true);

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
					new MainFrame().setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	// GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.JLabel jLabelInit;
	private javax.swing.JMenu jMenu1;
	private javax.swing.JMenu jMenu2;
	private javax.swing.JMenu jMenu3;
	private javax.swing.JMenu jMenu4;
	private javax.swing.JMenuBar jMenuBar1;
	private javax.swing.JMenu jMenuBrowseMIAPEs;
	private javax.swing.JMenu jMenuHelp;
	private javax.swing.JMenuItem jMenuItemBatchMiapeExtraction;
	private javax.swing.JMenuItem jMenuItemBrowseMIAPEs;
	private javax.swing.JMenuItem jMenuItemExit;
	private javax.swing.JMenuItem jMenuItemLogin;
	private javax.swing.JMenuItem jMenuItemMIAPEExtractionBatchTutorial;
	private javax.swing.JMenuItem jMenuItemMIAPEExtractionTutorial;
	private javax.swing.JMenuItem jMenuItemShowQueries;
	private javax.swing.JMenuItem jMenuItemStandard2MIAPE;
	private javax.swing.JMenuItem jMenuItemStartProjectComparison;
	private javax.swing.JPanel jPanel1;
	private JPanel panel;

	// End of variables declaration//GEN-END:variables

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getPropertyName().equals(LoginTask.LOGIN_OK)) {
			enableLoginDependentMenus(true);
			jLabelInit.setText("Logged as " + MainFrame.userName);
		}

	}

	public static Software getMiapeExtractorSoftware() {
		Software software = new MiapeExtractorSoftware();
		return software;
	}

	public static String getVersion() {
		if (version == null) {
			try {

				version = PropertiesReader.getProperties().getProperty(PropertiesReader.PACOM_VERSION);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return version;

	}

	public static void setMiapeAPIWebservice(MiapeAPIWebserviceDelegate miapeAPIWebservice2) {
		MainFrame.miapeAPIWebservice = miapeAPIWebservice2;

	}

	public static void setMiapeExtractorWebservice(MiapeExtractorDelegate miapeExtractorWebservice2) {
		MainFrame.miapeExtractorWebservice = miapeExtractorWebservice2;

	}
}