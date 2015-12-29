/*
 * ExporterToPRIDEDialog.java Created on __DATE__, __TIME__
 */

package org.proteored.miapeExtractor.analysis.exporters.gui;

import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jfree.ui.RefineryUtilities;
import org.proteored.miapeExtractor.analysis.exporters.ProteomeXchangeFilev2_1;
import org.proteored.miapeExtractor.analysis.exporters.tasks.PEXBulkSubmissionFileDownloaderTask;
import org.proteored.miapeExtractor.analysis.exporters.tasks.PEXBulkSubmissionFileWriterTask;
import org.proteored.miapeExtractor.analysis.exporters.tasks.PEXBulkSubmissionSummaryTreeLoaderTask;
import org.proteored.miapeExtractor.analysis.exporters.tasks.PRIDEExporterTask;
import org.proteored.miapeExtractor.analysis.gui.components.ExtendedJTree;
import org.proteored.miapeExtractor.gui.ImageManager;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.miapeapi.interfaces.msi.MiapeMSIDocument;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

/**
 * 
 * @author __USER__
 */
public class PEXBulkSubmissionSummaryFileCreatorDialog extends
		javax.swing.JDialog implements PropertyChangeListener {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private final ExperimentList experimentList;
	private int total = 0;
	private int numCreated = 0;
	private final static HashMap<Replicate, List<File>> rawFileMapByReplicate = new HashMap<Replicate, List<File>>();
	private File currentDirectory;
	private long tBegin;
	private boolean rawDataPresent = false;
	private ProteomeXchangeFilev2_1 pexFile;
	// TASKS:
	private PEXBulkSubmissionSummaryTreeLoaderTask treeloaderTask;
	private PEXBulkSubmissionFileDownloaderTask fileDownloaderTask;
	private PRIDEExporterTask prideExporterTask;
	private PEXBulkSubmissionFileWriterTask bulkSubmissionWriterTask;

	private final HashMap<Replicate, Set<String>> filesToSkip = new HashMap<Replicate, Set<String>>();

	public PEXBulkSubmissionSummaryFileCreatorDialog(Frame parent,
			ExperimentList experimentList) {
		super(parent, true);
		initComponents();
		try {
			UIManager.setLookAndFeel(new WindowsLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
		}
		this.experimentList = experimentList;
		if (this.experimentList != null) {

			int numExperiments = experimentList.getExperiments().size();
			jProgressBar.setValue(0);
			jProgressBar.setMaximum(100);
			total = numExperiments;
			// this.jProgressBar.setString(getProgressString());

			String plural = "";
			if (numExperiments > 1)
				plural = "s";
			String text = "<html>This project contains "
					+ numExperiments
					+ " experiment"
					+ plural
					+ ".<br>"
					+ numExperiments
					+ " PRIDE XML files will be created and a ProteomeXchange Bulk Submission File";

			text = text
					+ ".<br>Go to 'Add RAW data files' tab to add RAW files not contained in the MIAPE documents.<br>"
					+ "Then, select the output folder and the appropiate general options and click on 'ProteomeXchange' button.</html>";
			jLabelInformation.setText(text);
		}
		jTextAreaStatus.setFont(new JTextField().getFont());
		jTextAreaDescription.setFont(new JTextField().getFont());

		// set icon image
		setIconImage(ImageManager
				.getImageIcon(ImageManager.PROTEORED_MIAPE_API).getImage());

		// set title
		setTitle("Prepare data for a ProteomeXchange Bulk Submission");

		// center on screen
		RefineryUtilities.centerFrameOnScreen(this);

		// load icons
		jButtonStartPEX.setIcon(ImageManager.getImageIcon(ImageManager.PEX));
		jButtonCancel.setIcon(ImageManager.getImageIcon(ImageManager.STOP));
		jButtonAddRawDataFile.setIcon(ImageManager
				.getImageIcon(ImageManager.ADD));

		loadSummaryTree();

	}

	@Override
	public void setVisible(boolean b) {
		if (b)
			pack();
		super.setVisible(b);
	}

	@Override
	public void dispose() {
		cancelAllJobs();
		super.dispose();
	}

	private void cancelAllJobs() {
		if (treeloaderTask != null) {
			boolean cancel = treeloaderTask.cancel(true);
			while (!cancel && treeloaderTask.getState() == StateValue.STARTED) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				cancel = treeloaderTask.cancel(true);
			}
		}
		if (fileDownloaderTask != null
				&& fileDownloaderTask.getState() == StateValue.STARTED) {
			boolean cancel = fileDownloaderTask.cancel(true);
			while (!cancel) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				cancel = fileDownloaderTask.cancel(true);
			}
		}
		if (prideExporterTask != null
				&& prideExporterTask.getState() == StateValue.STARTED) {
			boolean cancel = prideExporterTask.cancel(true);
			while (!cancel) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				cancel = prideExporterTask.cancel(true);
			}
		}

		if (bulkSubmissionWriterTask != null
				&& bulkSubmissionWriterTask.getState() == StateValue.STARTED) {
			boolean cancel = bulkSubmissionWriterTask.cancel(true);
			while (!cancel) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				cancel = bulkSubmissionWriterTask.cancel(true);
			}
		}

		jProgressBar.setStringPainted(false);
		jProgressBar.setIndeterminate(false);
		jProgressBar.setValue(0);
	}

	private void loadSummaryTree() {
		// load summary tree
		if (treeloaderTask != null)
			treeloaderTask.cancel(true);
		treeloaderTask = new PEXBulkSubmissionSummaryTreeLoaderTask(
				experimentList, jTreeSummary,
				jCheckBoxIncludeMSIAttachedFiles.isSelected(),
				jCheckBoxIncludeMIAPEReports.isSelected());

		treeloaderTask.addPropertyChangeListener(this);
		treeloaderTask.execute();

		rawFileMapByReplicate.clear();
	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		jFileChooser = new javax.swing.JFileChooser();
		jTabbedPane1 = new javax.swing.JTabbedPane();
		jPanel1 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jTextFieldFolder = new javax.swing.JTextField();
		jButtonSelectFolder = new javax.swing.JButton();
		jCheckBoxCompress = new javax.swing.JCheckBox();
		jCheckBoxIncludeMSIAttachedFiles = new javax.swing.JCheckBox();
		jLabelShowMSIAttached = new javax.swing.JLabel();
		jCheckBoxIncludeMIAPEReports = new javax.swing.JCheckBox();
		jPanelTop = new javax.swing.JPanel();
		jLabelInformation = new javax.swing.JLabel();
		jPanel4 = new javax.swing.JPanel();
		jCheckBoxIncludeSpectra = new javax.swing.JCheckBox();
		jCheckBoxExcludeNotMatchedSpectra = new javax.swing.JCheckBox();
		jCheckBoxExcludeNonConclusiveProteins = new javax.swing.JCheckBox();
		jPanel5 = new javax.swing.JPanel();
		jLabel2 = new javax.swing.JLabel();
		jTextFieldTitle = new javax.swing.JTextField();
		jLabel3 = new javax.swing.JLabel();
		jScrollPane2 = new javax.swing.JScrollPane();
		jTextAreaDescription = new javax.swing.JTextArea();
		jLabel4 = new javax.swing.JLabel();
		jTextFieldKeywords = new javax.swing.JTextField();
		jLabel5 = new javax.swing.JLabel();
		jTextFieldPRIDELogin = new javax.swing.JTextField();
		jPanelSummary = new javax.swing.JPanel();
		jScrollPane3 = new javax.swing.JScrollPane();
		jTreeSummary = new ExtendedJTree();
		jPanel6 = new javax.swing.JPanel();
		jPanel7 = new javax.swing.JPanel();
		jButtonAddRawDataFile = new javax.swing.JButton();
		jLabelRawData = new javax.swing.JLabel();
		jPanelInformation = new javax.swing.JPanel();
		jLabel6 = new javax.swing.JLabel();
		jPanel9 = new javax.swing.JPanel();
		jButtonDeleteNode = new javax.swing.JButton();
		jPanel3 = new javax.swing.JPanel();
		jScrollPane1 = new javax.swing.JScrollPane();
		jTextAreaStatus = new javax.swing.JTextArea();
		jProgressBar = new javax.swing.JProgressBar();
		jPanel8 = new javax.swing.JPanel();
		jButtonStartPEX = new javax.swing.JButton();
		jButtonCancel = new javax.swing.JButton();

		jFileChooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
		jFileChooser
				.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);

		jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(
				javax.swing.BorderFactory.createEtchedBorder(),
				"General options"));

		jLabel1.setText("Output folder");
		jLabel1.setToolTipText("<html>\nAll files required for the ProteomeXchange<br>\nsubmission will be located here</html>");

		jTextFieldFolder
				.setToolTipText("<html> All files required for the ProteomeXchange<br> submission will be located here</html>");

		jButtonSelectFolder.setText("select");
		jButtonSelectFolder
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jButtonSelectFolderActionPerformed(evt);
					}
				});

		jCheckBoxCompress.setText("Compress all files (.gz)");
		jCheckBoxCompress
				.setToolTipText("<html>\nCompress all resulting files in the output folder.<br>\nThe files will be compressed using gzip.\n</html>");

		jCheckBoxIncludeMSIAttachedFiles.setSelected(true);
		jCheckBoxIncludeMSIAttachedFiles
				.setText("Include resulting files from MIAPE MSIs");
		jCheckBoxIncludeMSIAttachedFiles
				.addMouseListener(new java.awt.event.MouseAdapter() {
					@Override
					public void mouseEntered(java.awt.event.MouseEvent evt) {
						jCheckBoxIncludeMSIAttachedFilesMouseEntered(evt);
					}
				});
		jCheckBoxIncludeMSIAttachedFiles
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jCheckBoxIncludeMSIAttachedFilesActionPerformed(evt);
					}
				});

		jLabelShowMSIAttached.setFont(new java.awt.Font("Arial", 2, 12));
		jLabelShowMSIAttached.setText("(place mouse here to see the files)");
		jLabelShowMSIAttached
				.addMouseListener(new java.awt.event.MouseAdapter() {
					@Override
					public void mouseEntered(java.awt.event.MouseEvent evt) {
						jLabelShowMSIAttachedMouseEntered(evt);
					}
				});

		jCheckBoxIncludeMIAPEReports.setSelected(true);
		jCheckBoxIncludeMIAPEReports.setText("Include MIAPE reports");
		jCheckBoxIncludeMIAPEReports
				.setToolTipText("<html>\nInclude human readable MIAPE reports as HTML files in the ProteomeXchange submission as 'other' type files.<br>\nFor large number of MIAPE documents, it can take several minutes.\n</html>");
		jCheckBoxIncludeMIAPEReports
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jCheckBoxIncludeMIAPEReportsActionPerformed(evt);
					}
				});

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(
				jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout
				.setHorizontalGroup(jPanel2Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel2Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																javax.swing.GroupLayout.Alignment.TRAILING,
																jPanel2Layout
																		.createSequentialGroup()
																		.addComponent(
																				jCheckBoxIncludeMIAPEReports)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																				408,
																				Short.MAX_VALUE)
																		.addComponent(
																				jCheckBoxCompress))
														.addGroup(
																jPanel2Layout
																		.createSequentialGroup()
																		.addComponent(
																				jCheckBoxIncludeMSIAttachedFiles)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																		.addComponent(
																				jLabelShowMSIAttached))
														.addGroup(
																jPanel2Layout
																		.createSequentialGroup()
																		.addComponent(
																				jLabel1)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jTextFieldFolder,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				547,
																				Short.MAX_VALUE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jButtonSelectFolder)))
										.addContainerGap()));
		jPanel2Layout
				.setVerticalGroup(jPanel2Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel2Layout
										.createSequentialGroup()
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jCheckBoxCompress)
														.addComponent(
																jCheckBoxIncludeMIAPEReports))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jCheckBoxIncludeMSIAttachedFiles)
														.addComponent(
																jLabelShowMSIAttached))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel1)
														.addComponent(
																jButtonSelectFolder)
														.addComponent(
																jTextFieldFolder,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		jPanelTop.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		javax.swing.GroupLayout jPanelTopLayout = new javax.swing.GroupLayout(
				jPanelTop);
		jPanelTop.setLayout(jPanelTopLayout);
		jPanelTopLayout.setHorizontalGroup(jPanelTopLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				jPanelTopLayout
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(jLabelInformation,
								javax.swing.GroupLayout.DEFAULT_SIZE, 702,
								Short.MAX_VALUE).addContainerGap()));
		jPanelTopLayout.setVerticalGroup(jPanelTopLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				jPanelTopLayout
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(jLabelInformation,
								javax.swing.GroupLayout.DEFAULT_SIZE, 30,
								Short.MAX_VALUE).addContainerGap()));

		jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(
				javax.swing.BorderFactory.createEtchedBorder(),
				"PRIDE generation options"));

		jCheckBoxIncludeSpectra.setSelected(true);
		jCheckBoxIncludeSpectra.setText("include spectra");
		jCheckBoxIncludeSpectra
				.setToolTipText("<html>\nIf this option is not selected, spectra will not be added to the generated PRIDE XML file.\n</html>");
		jCheckBoxIncludeSpectra
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jCheckBoxIncludeSpectraActionPerformed(evt);
					}
				});

		jCheckBoxExcludeNotMatchedSpectra.setText("exclude no matched spectra");
		jCheckBoxExcludeNotMatchedSpectra
				.setToolTipText("<html>\nIf this option is selected, spectra that has not matched to any peptide<br>\n(or spectra that has matched to any peptide that has not passed the filter) <br>\nwill be removed from the generated PRIDE XML file.<br>\nThe resulting PRIDE XML file will be considerably smaller and will be<br> suitable for its inspection in tools like the PRIDE Viewer.<br>\n<b>Note: the resulting PRIDE XML will not valid for its submission to <br>\nthe ProteomeXchange consortium or EBI PRIDE repository. Non matched spectra <br> are required for publication purposes.</b><br>\n</html>");

		jCheckBoxExcludeNonConclusiveProteins.setSelected(true);
		jCheckBoxExcludeNonConclusiveProteins
				.setText("Exclude non-conclusive proteins");
		jCheckBoxExcludeNonConclusiveProteins
				.setToolTipText("<html>Select this option to not include the nonConclusive <br>proteins in the resulting PRIDE XML file.<br>\nIf non-conclusive proteins are included, they will be tagged with a cvParam<br>\nas non-conclusive, but then it will depend on the reader/viewer software to<br>\ninterpret correctly the number of proteins, since non-conclusive proteins<br> should not be counted in the total protein count.<html>");

		javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(
				jPanel4);
		jPanel4.setLayout(jPanel4Layout);
		jPanel4Layout
				.setHorizontalGroup(jPanel4Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel4Layout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(jCheckBoxIncludeSpectra)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jCheckBoxExcludeNotMatchedSpectra)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jCheckBoxExcludeNonConclusiveProteins)
										.addContainerGap(235, Short.MAX_VALUE)));
		jPanel4Layout
				.setVerticalGroup(jPanel4Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel4Layout
										.createSequentialGroup()
										.addGroup(
												jPanel4Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jCheckBoxIncludeSpectra)
														.addComponent(
																jCheckBoxExcludeNotMatchedSpectra)
														.addComponent(
																jCheckBoxExcludeNonConclusiveProteins))
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(
				javax.swing.BorderFactory.createEtchedBorder(),
				"Required metadata not found in MIAPEs"));

		jLabel2.setText("Title:");
		jLabel2.setToolTipText("Title of the project been submitted");

		jTextFieldTitle.setToolTipText("Title of the project been submitted");

		jLabel3.setText("Description:");
		jLabel3.setToolTipText("<html>\nA short description of the experiment being submitted. This will be<br>\nmade publicly available in ProteomeCentral and serves as an<br>\nabstract describing the submission, similar in concept to the abstract<br>\nof a scientific publication.</html>");

		jScrollPane2
				.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		jTextAreaDescription.setColumns(20);
		jTextAreaDescription.setRows(5);
		jTextAreaDescription
				.setToolTipText("<html>\nA short description of the experiment being submitted. This will be<br>\nmade publicly available in ProteomeCentral and serves as an<br>\nabstract describing the submission, similar in concept to the abstract<br>\nof a scientific publication.</html>");
		jTextAreaDescription.setWrapStyleWord(true);
		jScrollPane2.setViewportView(jTextAreaDescription);

		jLabel4.setText("Keywords:");
		jLabel4.setToolTipText("<html>\nA list of keywords that describe the content and type of the<br>\nexperiment being submitted. Multiple entries should be comma<br>\nseparated, it is recommended to provide a minimum of three<br>\nkeywords.</html>");

		jTextFieldKeywords
				.setToolTipText("<html>\nA list of keywords that describe the content and type of the<br>\nexperiment being submitted. Multiple entries should be comma<br>\nseparated, it is recommended to provide a minimum of three<br>\nkeywords.</html>");

		jLabel5.setText("PRIDE user name:");
		jLabel5.setToolTipText("<html>\nThe PRIDE user name. Submitters need to have a PRIDE account.\n</html>");

		jTextFieldPRIDELogin
				.setToolTipText("<html> The PRIDE user name. Submitters need to have a PRIDE account. </html>");

		javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(
				jPanel5);
		jPanel5.setLayout(jPanel5Layout);
		jPanel5Layout
				.setHorizontalGroup(jPanel5Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel5Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanel5Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																jPanel5Layout
																		.createSequentialGroup()
																		.addGroup(
																				jPanel5Layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								jLabel3)
																						.addComponent(
																								jLabel2))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(
																				jPanel5Layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								jScrollPane2,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								626,
																								Short.MAX_VALUE)
																						.addComponent(
																								jTextFieldTitle,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								626,
																								Short.MAX_VALUE)))
														.addGroup(
																jPanel5Layout
																		.createSequentialGroup()
																		.addComponent(
																				jLabel5)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jTextFieldPRIDELogin,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				597,
																				Short.MAX_VALUE))
														.addGroup(
																jPanel5Layout
																		.createSequentialGroup()
																		.addComponent(
																				jLabel4)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jTextFieldKeywords,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				635,
																				Short.MAX_VALUE)))
										.addContainerGap()));
		jPanel5Layout
				.setVerticalGroup(jPanel5Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel5Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanel5Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel2)
														.addComponent(
																jTextFieldTitle,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel5Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(jLabel3)
														.addComponent(
																jScrollPane2,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																46,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel5Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel4)
														.addComponent(
																jTextFieldKeywords,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel5Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel5)
														.addComponent(
																jTextFieldPRIDELogin,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(
				jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout
				.setHorizontalGroup(jPanel1Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel1Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanel1Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jPanel2,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																jPanelTop,
																javax.swing.GroupLayout.Alignment.TRAILING,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																jPanel4,
																javax.swing.GroupLayout.Alignment.TRAILING,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																jPanel5,
																javax.swing.GroupLayout.Alignment.TRAILING,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE))
										.addContainerGap()));
		jPanel1Layout
				.setVerticalGroup(jPanel1Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								jPanel1Layout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(
												jPanelTop,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addComponent(
												jPanel2,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jPanel4,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jPanel5,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(57, 57, 57)));

		jTabbedPane1.addTab("General options", jPanel1);

		jTreeSummary.setAutoscrolls(true);
		jTreeSummary.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jTreeSummaryMouseClicked(evt);
			}
		});
		jScrollPane3.setViewportView(jTreeSummary);

		jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(
				javax.swing.BorderFactory.createEtchedBorder(),
				"Add RAW data files"));

		jButtonAddRawDataFile
				.setIcon(new javax.swing.ImageIcon(
						"C:\\Users\\Salva\\workspace\\miape-extractor\\src\\main\\resources\\add.png")); // NOI18N
		jButtonAddRawDataFile.setText("Add new RAW data file");
		jButtonAddRawDataFile.setEnabled(false);
		jButtonAddRawDataFile
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jButtonAddRawDataFileActionPerformed(evt);
					}
				});

		jLabelRawData.setVerticalAlignment(javax.swing.SwingConstants.TOP);
		jLabelRawData.setAutoscrolls(true);

		javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(
				jPanel7);
		jPanel7.setLayout(jPanel7Layout);
		jPanel7Layout
				.setHorizontalGroup(jPanel7Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel7Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanel7Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jLabelRawData,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																264,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jButtonAddRawDataFile))
										.addContainerGap()));
		jPanel7Layout
				.setVerticalGroup(jPanel7Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel7Layout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(jButtonAddRawDataFile)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jLabelRawData,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												50,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		jPanelInformation.setBorder(javax.swing.BorderFactory
				.createEtchedBorder());

		jLabel6.setText("<html>Your MIAPE documents already have RAW files (probably mzML files).<br>\nInspect the summary tree to see them.<br>\nHowever, you can add Mass Spectrometry RAW binary files associated to each level 2 nodes in the tree.<br>\nSelect one level 2 node, and click on 'Add new RAW file' and select a RAW file to include it in the submission.</html>");
		jLabel6.setVerticalAlignment(javax.swing.SwingConstants.TOP);

		javax.swing.GroupLayout jPanelInformationLayout = new javax.swing.GroupLayout(
				jPanelInformation);
		jPanelInformation.setLayout(jPanelInformationLayout);
		jPanelInformationLayout
				.setHorizontalGroup(jPanelInformationLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelInformationLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(
												jLabel6,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												272, Short.MAX_VALUE)
										.addContainerGap()));
		jPanelInformationLayout
				.setVerticalGroup(jPanelInformationLayout.createParallelGroup(
						javax.swing.GroupLayout.Alignment.LEADING).addGroup(
						jPanelInformationLayout
								.createSequentialGroup()
								.addContainerGap()
								.addComponent(jLabel6,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										64, Short.MAX_VALUE).addContainerGap()));

		jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(
				javax.swing.BorderFactory.createEtchedBorder(), "Delete node"));

		jButtonDeleteNode
				.setIcon(new javax.swing.ImageIcon(
						"C:\\Users\\Salva\\workspace\\miape-extractor\\src\\main\\resources\\delete.png")); // NOI18N
		jButtonDeleteNode.setText("delete selected node");
		jButtonDeleteNode
				.setToolTipText("<html>Click here to remove the selected node in order to not<br> include it in the ProteomeXchange submission</html>");
		jButtonDeleteNode
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jButtonDeleteNodeActionPerformed(evt);
					}
				});

		javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(
				jPanel9);
		jPanel9.setLayout(jPanel9Layout);
		jPanel9Layout.setHorizontalGroup(jPanel9Layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				jPanel9Layout.createSequentialGroup().addContainerGap()
						.addComponent(jButtonDeleteNode)
						.addContainerGap(99, Short.MAX_VALUE)));
		jPanel9Layout.setVerticalGroup(jPanel9Layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				jPanel9Layout
						.createSequentialGroup()
						.addComponent(jButtonDeleteNode)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE)));

		javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(
				jPanel6);
		jPanel6.setLayout(jPanel6Layout);
		jPanel6Layout
				.setHorizontalGroup(jPanel6Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								jPanel6Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanel6Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.TRAILING)
														.addComponent(
																jPanel9,
																javax.swing.GroupLayout.Alignment.LEADING,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																jPanel7,
																javax.swing.GroupLayout.Alignment.LEADING,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																jPanelInformation,
																javax.swing.GroupLayout.Alignment.LEADING,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE))
										.addContainerGap()));
		jPanel6Layout
				.setVerticalGroup(jPanel6Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel6Layout
										.createSequentialGroup()
										.addComponent(
												jPanelInformation,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addComponent(
												jPanel7,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jPanel9,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(212, 212, 212)));

		javax.swing.GroupLayout jPanelSummaryLayout = new javax.swing.GroupLayout(
				jPanelSummary);
		jPanelSummary.setLayout(jPanelSummaryLayout);
		jPanelSummaryLayout
				.setHorizontalGroup(jPanelSummaryLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelSummaryLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(
												jScrollPane3,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												399,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jPanel6,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addContainerGap()));
		jPanelSummaryLayout
				.setVerticalGroup(jPanelSummaryLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								jPanelSummaryLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanelSummaryLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.TRAILING)
														.addComponent(
																jScrollPane3,
																javax.swing.GroupLayout.Alignment.LEADING,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																453,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jPanel6,
																javax.swing.GroupLayout.Alignment.LEADING,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																453,
																Short.MAX_VALUE))
										.addContainerGap()));

		jTabbedPane1
				.addTab("Submission Summary - Add RAW files", jPanelSummary);

		jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(
				javax.swing.BorderFactory.createEtchedBorder(), "Status"));

		jScrollPane1
				.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane1.setAutoscrolls(true);

		jTextAreaStatus.setColumns(20);
		jTextAreaStatus.setLineWrap(true);
		jTextAreaStatus.setRows(5);
		jTextAreaStatus.setWrapStyleWord(true);
		jScrollPane1.setViewportView(jTextAreaStatus);

		jProgressBar.setString("");
		jProgressBar.setStringPainted(true);

		javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(
				jPanel3);
		jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout
				.setHorizontalGroup(jPanel3Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel3Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanel3Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jScrollPane1,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																723,
																Short.MAX_VALUE)
														.addComponent(
																jProgressBar,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																723,
																Short.MAX_VALUE))
										.addContainerGap()));
		jPanel3Layout
				.setVerticalGroup(jPanel3Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								jPanel3Layout
										.createSequentialGroup()
										.addComponent(
												jScrollPane1,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												42, Short.MAX_VALUE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jProgressBar,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)));

		jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(
				javax.swing.BorderFactory.createEtchedBorder(), "Action"));

		jButtonStartPEX
				.setIcon(new javax.swing.ImageIcon(
						"C:\\Users\\Salva\\workspace\\miape-extractor\\src\\main\\resources\\pex.png")); // NOI18N
		jButtonStartPEX
				.setToolTipText("Click here to prepare data for a ProteomeXchange submission");
		jButtonStartPEX.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonStartPEXActionPerformed(evt);
			}
		});

		jButtonCancel
				.setIcon(new javax.swing.ImageIcon(
						"C:\\Users\\Salva\\workspace\\miape-extractor\\src\\main\\resources\\stop.png")); // NOI18N
		jButtonCancel.setText("Cancel");
		jButtonCancel.setToolTipText("Cancel process");
		jButtonCancel.setEnabled(false);
		jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonCancelActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(
				jPanel8);
		jPanel8.setLayout(jPanel8Layout);
		jPanel8Layout
				.setHorizontalGroup(jPanel8Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel8Layout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(jButtonStartPEX)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED,
												453, Short.MAX_VALUE)
										.addComponent(jButtonCancel)
										.addContainerGap()));
		jPanel8Layout
				.setVerticalGroup(jPanel8Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel8Layout
										.createSequentialGroup()
										.addGroup(
												jPanel8Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jButtonStartPEX)
														.addComponent(
																jButtonCancel))
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(
														jTabbedPane1,
														javax.swing.GroupLayout.Alignment.TRAILING,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														759, Short.MAX_VALUE)
												.addComponent(
														jPanel3,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														Short.MAX_VALUE)
												.addComponent(
														jPanel8,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														Short.MAX_VALUE))
								.addContainerGap()));
		layout.setVerticalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addComponent(jTabbedPane1,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										509,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jPanel8,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(jPanel3,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGap(39, 39, 39)));

		java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit()
				.getScreenSize();
		setBounds((screenSize.width - 799) / 2, (screenSize.height - 752) / 2,
				799, 752);
	}// </editor-fold>
		// GEN-END:initComponents

	private void jButtonDeleteNodeActionPerformed(java.awt.event.ActionEvent evt) {
		deleteSelectedNode();
	}

	private void deleteSelectedNode() {

		final boolean rootNodeSelected = jTreeSummary.isOnlyOneNodeSelected(1);
		final boolean childrenOfReplicateNodeSelected = jTreeSummary
				.isOnlyOneNodeSelected(4);
		if (!rootNodeSelected && childrenOfReplicateNodeSelected) {
			// remove the file from the mappings
			String nodeString = (String) jTreeSummary.getSelectedNode()
					.getUserObject();
			if (nodeString
					.startsWith(PEXBulkSubmissionSummaryTreeLoaderTask.RAW
							+ ": ")) {
				nodeString = nodeString
						.split(PEXBulkSubmissionSummaryTreeLoaderTask.RAW
								+ ": ")[1];
				final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) jTreeSummary
						.getSelectedNode().getParent();
				final Replicate replicate = (Replicate) parent.getUserObject();
				addToFilesToSkip(replicate, nodeString);
				jTreeSummary.removeSelectedNode();
			} else {
				appendStatus("That node cannot be deleted");
			}

		} else {
			appendStatus("That node cannot be deleted");
		}

	}

	private void addToFilesToSkip(Replicate replicate, String fileName) {
		if (filesToSkip.containsKey(replicate)) {
			filesToSkip.get(replicate).add(fileName);
		} else {
			HashSet<String> set = new HashSet<String>();
			set.add(fileName);
			filesToSkip.put(replicate, set);
		}
	}

	private void jCheckBoxIncludeMIAPEReportsActionPerformed(
			java.awt.event.ActionEvent evt) {
		loadSummaryTree();
	}

	private void jButtonStartPEXActionPerformed(java.awt.event.ActionEvent evt) {
		startPEXBulkFileExportation();
	}

	private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {
		cancelAllJobs();
		jButtonCancel.setEnabled(false);
		jButtonStartPEX.setEnabled(true);
		appendStatus("Process cancelled");
	}

	private void jTreeSummaryMouseClicked(java.awt.event.MouseEvent evt) {
		final boolean replicateNodeSelected = jTreeSummary
				.isOnlyOneNodeSelected(3);
		// log.info(onlyOneNodeSelected);
		if (replicateNodeSelected) {
			jButtonAddRawDataFile.setEnabled(true);
			jLabelRawData
					.setText("<html>Click on 'Add raw data file' to attach <br>"
							+ "a RAW data file to the submission</html>");
		} else {
			jButtonAddRawDataFile.setEnabled(false);
			jLabelRawData.setText("");
		}
	}

	private void jCheckBoxIncludeMSIAttachedFilesActionPerformed(
			java.awt.event.ActionEvent evt) {
		loadSummaryTree();
	}

	private void jButtonAddRawDataFileActionPerformed(
			java.awt.event.ActionEvent evt) {
		final boolean replicateNodeSelected = jTreeSummary
				.isOnlyOneNodeSelected(3);
		if (!replicateNodeSelected)
			return;
		try {
			if (currentDirectory == null)
				currentDirectory = getOutputFolder();
		} catch (IllegalMiapeArgumentException e) {

		}
		JFileChooser fileChooser = new JFileChooser(currentDirectory);
		fileChooser.showDialog(this, "Select RAW file");
		File selectedFile = fileChooser.getSelectedFile();
		if (selectedFile != null) {
			currentDirectory = selectedFile.getParentFile();
			if (selectedFile.exists()) {
				String name = FilenameUtils.getName(selectedFile
						.getAbsolutePath());
				// Add node to the tree in the correct position
				DefaultMutableTreeNode selectedNode = jTreeSummary
						.getSelectedNode();
				DefaultMutableTreeNode newNode = jTreeSummary.addNewNode(
						PEXBulkSubmissionSummaryTreeLoaderTask.RAW + ": "
								+ name, selectedNode);

				jTreeSummary.scrollToNode(newNode);
				// Add the raw file to the RAW file Map
				Replicate rep = (Replicate) selectedNode.getUserObject();
				if (PEXBulkSubmissionSummaryFileCreatorDialog.rawFileMapByReplicate
						.containsKey(rep)) {
					PEXBulkSubmissionSummaryFileCreatorDialog.rawFileMapByReplicate
							.get(rep).add(selectedFile);
				} else {
					List<File> fileList = new ArrayList<File>();
					fileList.add(selectedFile);
					PEXBulkSubmissionSummaryFileCreatorDialog.rawFileMapByReplicate
							.put(rep, fileList);
				}
				rawDataPresent = true;
			} else {
				appendStatus("File not found: "
						+ selectedFile.getAbsolutePath());
			}
		}

	}

	private void jCheckBoxIncludeMSIAttachedFilesMouseEntered(
			java.awt.event.MouseEvent evt) {
		String tip = getTipOfMSIAttachedFiles();
		jCheckBoxIncludeMSIAttachedFiles.setToolTipText(tip);
	}

	private void jLabelShowMSIAttachedMouseEntered(java.awt.event.MouseEvent evt) {
		String tip = getTipOfMSIAttachedFiles();
		jLabelShowMSIAttached.setToolTipText(tip);
	}

	private boolean includeMSIAttachedFiles() {
		return jCheckBoxIncludeMSIAttachedFiles.isSelected();
	}

	private boolean includeMIAPEReports() {
		return jCheckBoxIncludeMIAPEReports.isSelected();
	}

	private String getTipOfMSIAttachedFiles() {
		String tip = "<html>";
		List<String> locations = new ArrayList<String>();
		if (experimentList != null) {
			List<MiapeMSIDocument> miapeMSIs = experimentList.getMiapeMSIs();
			for (MiapeMSIDocument miapeMSIDocument : miapeMSIs) {
				if (miapeMSIDocument.getAttachedFileLocation() != null
						&& !"".equals(miapeMSIDocument
								.getAttachedFileLocation())) {
					try {
						URL url = new URL(
								miapeMSIDocument.getAttachedFileLocation());
						locations.add(miapeMSIDocument
								.getAttachedFileLocation());

					} catch (MalformedURLException e) {
						// do nothing
					}
				}
			}

			if (!locations.isEmpty()) {
				boolean plural = locations.size() > 1;
				tip += locations.size() + " file";
				if (plural)
					tip += "s";
				tip += " will be included in the ProteomeXchange submission if checkbox is selected:<br><ul>";
				for (String location : locations) {
					tip += "<li>" + location + "</li>";

				}
				tip += "</ul></html>";
				return tip;

			}
		}
		return null;
	}

	private void jCheckBoxIncludeSpectraActionPerformed(
			java.awt.event.ActionEvent evt) {
		jCheckBoxExcludeNotMatchedSpectra.setEnabled(jCheckBoxIncludeSpectra
				.isSelected());
	}

	private void jButtonSelectFolderActionPerformed(
			java.awt.event.ActionEvent evt) {
		final int showDialog = jFileChooser.showDialog(this, "Select folder");
		if (showDialog == JFileChooser.APPROVE_OPTION) {
			final File selectedFile = jFileChooser.getSelectedFile();
			if (selectedFile != null)
				jTextFieldFolder.setText(selectedFile.getAbsolutePath());
		}
	}

	private void startPEXBulkFileExportation() {

		try {
			// check if there is any RAW file in the submission
			if (!rawDataPresent) {
				JOptionPane
						.showMessageDialog(
								this,
								"<html>RAW files are required for a ProteomeXchange submission.<br>"
										+ "Go to Submission 'Summary - Add RAW files' tab.</html>",
								"RAW files required", JOptionPane.ERROR_MESSAGE);

				return;
			}
			tBegin = System.currentTimeMillis();
			File outputFolder = getOutputFolder();
			log.info("Output folder=" + outputFolder.getAbsolutePath());

			pexFile = new ProteomeXchangeFilev2_1(outputFolder, experimentList,
					filesToSkip);
			pexFile.setProjectTitle(getProjectTitle());
			pexFile.setProjectShortDescription(getDescription());
			pexFile.setKeyword(getKeywords());
			pexFile.setPrideLogin(getPrideLogin());

			// If the user has selected extra RAW files, add them to the pex
			// file
			if (!PEXBulkSubmissionSummaryFileCreatorDialog.rawFileMapByReplicate
					.isEmpty()) {
				for (Replicate replicate : PEXBulkSubmissionSummaryFileCreatorDialog.rawFileMapByReplicate
						.keySet()) {
					List<File> files = PEXBulkSubmissionSummaryFileCreatorDialog.rawFileMapByReplicate
							.get(replicate);
					if (files != null && !files.isEmpty())
						pexFile.addReplicateRawFiles(replicate, files);
				}
			}
			fileDownloaderTask = new PEXBulkSubmissionFileDownloaderTask(
					pexFile, includeMSIAttachedFiles(), includeMIAPEReports(),
					compressDownloadedData());
			fileDownloaderTask.addPropertyChangeListener(this);
			fileDownloaderTask.execute();

			jButtonCancel.setEnabled(true);
		} catch (IllegalMiapeArgumentException e) {
			jTextAreaStatus.setText(e.getMessage());
		}

	}

	private boolean compressDownloadedData() {
		return jCheckBoxCompress.isSelected();
	}

	private String getPrideLogin() {
		String prideLoginText = jTextFieldPRIDELogin.getText();
		if (!"".equals(prideLoginText)) {
			return prideLoginText;
		}
		throw new IllegalMiapeArgumentException(
				"A user name for the PRIDE repository is required. If you don't have any user name, register here: http://www.ebi.ac.uk/pride/startRegistration.do");
	}

	private String getDescription() {
		String descriptionText = jTextAreaDescription.getText();
		if (!"".equals(descriptionText)) {
			return descriptionText;
		}
		throw new IllegalMiapeArgumentException(
				"A short description for the project is required");
	}

	private String getProjectTitle() {
		String titleText = jTextFieldTitle.getText();
		if (!"".equals(titleText)) {
			return titleText;
		}
		throw new IllegalMiapeArgumentException(
				"A title for the project is required");
	}

	private Set<String> getKeywords() {
		String keywordsText = jTextFieldKeywords.getText();
		if (!"".equals(keywordsText)) {
			Set<String> ret = new HashSet<String>();
			if (keywordsText.contains(",")) {
				String[] split = keywordsText.split(",");
				for (String string : split) {
					ret.add(string);
				}
			} else {
				throw new IllegalMiapeArgumentException(
						"At least three keywords are required (separated by commas)");
			}
			if (ret.size() >= 3)
				return ret;
			else
				throw new IllegalMiapeArgumentException(
						"At least three keywords are required (separated by commas)");
		}
		throw new IllegalMiapeArgumentException(
				"No Keywords have been instroduced");
	}

	private File getOutputFolder() {
		File output = jFileChooser.getSelectedFile();
		if (output != null && output.isDirectory())
			return output;
		throw new IllegalMiapeArgumentException("Output folder not valid");
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				ExporterToPRIDEDialog dialog = new ExporterToPRIDEDialog(
						new javax.swing.JFrame(), null);
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
	private javax.swing.JButton jButtonAddRawDataFile;
	private javax.swing.JButton jButtonCancel;
	private javax.swing.JButton jButtonDeleteNode;
	private javax.swing.JButton jButtonSelectFolder;
	private javax.swing.JButton jButtonStartPEX;
	private javax.swing.JCheckBox jCheckBoxCompress;
	private javax.swing.JCheckBox jCheckBoxExcludeNonConclusiveProteins;
	private javax.swing.JCheckBox jCheckBoxExcludeNotMatchedSpectra;
	private javax.swing.JCheckBox jCheckBoxIncludeMIAPEReports;
	private javax.swing.JCheckBox jCheckBoxIncludeMSIAttachedFiles;
	private javax.swing.JCheckBox jCheckBoxIncludeSpectra;
	private javax.swing.JFileChooser jFileChooser;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JLabel jLabelInformation;
	private javax.swing.JLabel jLabelRawData;
	private javax.swing.JLabel jLabelShowMSIAttached;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JPanel jPanel9;
	private javax.swing.JPanel jPanelInformation;
	private javax.swing.JPanel jPanelSummary;
	private javax.swing.JPanel jPanelTop;
	private javax.swing.JProgressBar jProgressBar;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JScrollPane jScrollPane3;
	private javax.swing.JTabbedPane jTabbedPane1;
	private javax.swing.JTextArea jTextAreaDescription;
	private javax.swing.JTextArea jTextAreaStatus;
	private javax.swing.JTextField jTextFieldFolder;
	private javax.swing.JTextField jTextFieldKeywords;
	private javax.swing.JTextField jTextFieldPRIDELogin;
	private javax.swing.JTextField jTextFieldTitle;
	private ExtendedJTree jTreeSummary;

	// End of variables declaration//GEN-END:variables

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(
				PRIDEExporterTask.SINGLE_PRIDE_EXPORTING_STARTED)) {
			jProgressBar.setIndeterminate(true);
			jButtonStartPEX.setEnabled(false);
			String experimentName = (String) evt.getNewValue();
			if (experimentName != null) {
				appendStatus("Creating PRIDE XML file for experiment: "
						+ experimentName + "...");
				jProgressBar.setString(getProgressString());
			}
			jProgressBar.setString("Step 2/3: Creating PRIDE XML files");
			jProgressBar.setStringPainted(true);
		} else if (evt.getPropertyName().equals(
				PRIDEExporterTask.SINGLE_PRIDE_EXPORTED)) {
			File generatedFile = (File) evt.getNewValue();
			if (generatedFile != null && generatedFile.exists()) {
				appendStatus("PRIDE XML file created: "
						+ FilenameUtils
								.getName(generatedFile.getAbsolutePath())
						+ " (" + getFileSizeString(generatedFile) + ")");
				numCreated++;
				jProgressBar.setString(getProgressString());

			}
		} else if (evt.getPropertyName().equals(
				PRIDEExporterTask.SINGLE_PRIDE_ALREADY_PRESENT)) {
			File generatedFile = (File) evt.getNewValue();
			if (generatedFile != null && generatedFile.exists()) {
				appendStatus("PRIDE XML file already present in output folder: "
						+ FilenameUtils
								.getName(generatedFile.getAbsolutePath())
						+ " (" + getFileSizeString(generatedFile) + ")");
				appendStatus("PRIDE XML creation will be skipped since there is already a file in the output folder with the name '"
						+ FilenameUtils
								.getName(generatedFile.getAbsolutePath()) + "'");
				numCreated++;
				jProgressBar.setString(getProgressString());

			}
		} else if (evt.getPropertyName().equals(
				PRIDEExporterTask.SINGLE_PRIDE_COMPRESSING_STARTED)) {
			File fileToCompress = (File) evt.getNewValue();
			if (fileToCompress != null && fileToCompress.exists()) {
				appendStatus("Compressing PRIDE XML file: "
						+ FilenameUtils.getName(fileToCompress
								.getAbsolutePath()) + " ("
						+ getFileSizeString(fileToCompress) + ")");
			}

		} else if (evt.getPropertyName().equals(
				PRIDEExporterTask.SINGLE_PRIDE_COMPRESSING_FINISHED)) {
			File compressedFile = (File) evt.getNewValue();
			if (compressedFile != null && compressedFile.exists()) {
				appendStatus("PRIDE XML file compressed: "
						+ FilenameUtils.getName(compressedFile
								.getAbsolutePath()) + " ("
						+ getFileSizeString(compressedFile) + ")");
			}
		} else if (evt.getPropertyName().equals(
				PRIDEExporterTask.PRIDE_EXPORTER_DONE)) {
			List<File> generatedFiles = (List<File>) evt.getNewValue();
			jProgressBar.setIndeterminate(false);
			jButtonStartPEX.setEnabled(true);

			if (generatedFiles != null && !generatedFiles.isEmpty()) {
				String plural = "";
				if (generatedFiles.size() > 1)
					plural = "s";
				String message = generatedFiles.size()
						+ " PRIDE XML file"
						+ plural
						+ " generated at '"
						+ FilenameUtils.getFullPath(generatedFiles.iterator()
								.next().getAbsolutePath()) + "':\n";
				int num = 1;
				for (File file : generatedFiles) {
					message = message + num++ + ": '" + file.getName() + " ("
							+ getFileSizeString(file) + ")'\n";

				}

				appendStatus(message);
				// Start PEXBulkSubmissionWriter
				bulkSubmissionWriterTask = new PEXBulkSubmissionFileWriterTask(
						pexFile);
				bulkSubmissionWriterTask.addPropertyChangeListener(this);
				bulkSubmissionWriterTask.execute();

			}
		} else if (evt.getPropertyName().equals(
				PRIDEExporterTask.PRIDE_EXPORTER_ERROR)) {
			jProgressBar.setIndeterminate(false);
			jButtonStartPEX.setEnabled(true);
			String errorMessage = (String) evt.getNewValue();
			setStatus(errorMessage);
			jProgressBar.setStringPainted(false);
			jButtonCancel.setEnabled(false);

		} else if (evt.getPropertyName().equals(
				PRIDEExporterTask.SINGLE_PRIDE_EXPORTED_ERROR)) {
			// do not stop the process
			String errorMessage = (String) evt.getNewValue();
			appendStatus(errorMessage);

		} else if (evt
				.getPropertyName()
				.equals(PEXBulkSubmissionFileDownloaderTask.PEX_FILE_DOWNLOADING_STARTED)) {
			jButtonStartPEX.setEnabled(false);
			String url = (String) evt.getNewValue();
			appendStatus("Downloading from: " + url);
			jProgressBar.setString("Step 1/3: Downloading required files");
			jProgressBar.setStringPainted(true);

		} else if (evt.getPropertyName().equals(
				PEXBulkSubmissionFileDownloaderTask.PEX_FILE_DOWNLOADED)) {
			File downloadedFile = (File) evt.getNewValue();
			appendStatus("File downloaded: "
					+ FilenameUtils.getName(downloadedFile.getAbsolutePath())
					+ " (" + getFileSizeString(downloadedFile) + ")");
		} else if (evt
				.getPropertyName()
				.equals(PEXBulkSubmissionFileDownloaderTask.PEX_FILE_FOUND_IN_OUTPUT_FOLDER)) {
			File downloadedFile = (File) evt.getNewValue();
			appendStatus("File already found in output folder: "
					+ FilenameUtils.getName(downloadedFile.getAbsolutePath()));
		} else if (evt
				.getPropertyName()
				.equals(PEXBulkSubmissionFileDownloaderTask.PEX_FILE_DOWNLOADING_FINISH)) {
			jButtonStartPEX.setEnabled(true);
			int num = (Integer) evt.getNewValue();
			appendStatus(num + " files were downloaded to the output folder.");

			// Start PRIDE XML file exportation
			prideExporterTask = new PRIDEExporterTask(experimentList,
					getOutputFolder(), jCheckBoxIncludeSpectra.isSelected(),
					jCheckBoxExcludeNotMatchedSpectra.isSelected(),
					jCheckBoxCompress.isSelected(),
					jCheckBoxExcludeNonConclusiveProteins.isSelected());
			prideExporterTask.addPropertyChangeListener(this);
			prideExporterTask.execute();

		} else if (evt.getPropertyName().equals(
				PEXBulkSubmissionFileDownloaderTask.PEX_FILE_DOWNLOADING_ERROR)) {
			jButtonStartPEX.setEnabled(true);
			String message = (String) evt.getNewValue();
			appendStatus(message);

		} else if (evt.getPropertyName().equals(
				PEXBulkSubmissionFileWriterTask.PEX_EXPORT_STARTED)) {
			appendStatus("Writting ProteomeXchange bulk submission file");
			jProgressBar
					.setString("Step 3/3: Writting ProteomeXchange Bulk Submission Summary file");
			jProgressBar.setStringPainted(true);
		} else if (evt.getPropertyName().equals(
				PEXBulkSubmissionFileWriterTask.PEX_EXPORT_FINISH)) {
			File pexFile = (File) evt.getNewValue();
			if (pexFile != null) {
				appendStatus("ProteomeXchange bulk submission file created succesfully at output folder: "
						+ FilenameUtils.getName(pexFile.getAbsolutePath()));
				jProgressBar
						.setString("ProteomeXchange Bulk Submission Summary file created");
				jButtonCancel.setEnabled(false);
				long tFinal = System.currentTimeMillis();
				appendStatus("It took "
						+ getFormatedTime((tFinal - tBegin) / 1000.0));
				showFinishDialog();
			}
		} else if (evt.getPropertyName().equals(
				PEXBulkSubmissionFileWriterTask.PEX_EXPORT_ERROR)) {
			appendStatus("Error when writting ProteomeXchange bulk submission file: "
					+ evt.getNewValue());
			jProgressBar.setStringPainted(true);
			jButtonCancel.setEnabled(false);

		} else if (evt
				.getPropertyName()
				.equals(PEXBulkSubmissionFileDownloaderTask.PEX_FILE_COMPRESSION_STARTED)) {
			File filetoCompress = (File) evt.getNewValue();
			appendStatus("Compressing file: "
					+ filetoCompress.getAbsolutePath());
		} else if (evt
				.getPropertyName()
				.equals(PEXBulkSubmissionFileDownloaderTask.PEX_FILE_COMPRESSION_FINISHED)) {
			File fileCompressed = (File) evt.getNewValue();
			appendStatus("File compressed at: "
					+ fileCompressed.getAbsolutePath());
		} else if (PEXBulkSubmissionSummaryTreeLoaderTask.BULK_SUBMISSION_TREE_LOADER_STARTS
				.equals(evt.getPropertyName())) {
			appendStatus("Loading summary tree. Checking URL files attached to MIAPEs...");
			rawDataPresent = false;
			jButtonCancel.setEnabled(true);
		} else if (PEXBulkSubmissionSummaryTreeLoaderTask.BULK_SUBMISSION_TREE_LOADER_EXPERIMENT_LOADED
				.equals(evt.getPropertyName())) {
			// do nothing
		} else if (PEXBulkSubmissionSummaryTreeLoaderTask.BULK_SUBMISSION_TREE_LOADER_FINISH
				.equals(evt.getPropertyName())) {
			if (evt.getNewValue() == null) {
				appendStatus("Summary tree loaded.");
			} else {
				appendStatus("Error loading summary tree: " + evt.getNewValue());
			}
			// Fill summary tree information text, depending on there is already
			// raw files or not
			String informationMessage = "";
			if (rawDataPresent) {
				informationMessage = "<html>Your MIAPE documents already have RAW files (probably mzML files).<br>"
						+ "Inspect the summary tree to see them.<br>"
						+ "However, it is recommended to include Mass Spectrometry RAW <b>binary</b> files in the submission.<br>"
						+ "Select one level 2 node, and click on 'Add new RAW file' and select the appropiate RAW file for that node, in order to include it in the submission.</html>";

			} else {
				informationMessage = "<html>Your MIAPE documents doesn't contain any RAW file.<br>"

						+ "For any ProteomeXchange submission, it is required to include add Mass Spectrometry RAW files.<br>"
						+ "Select one level 2 node, and click on 'Add new RAW file' and select the appropiate RAW file for that node, in order to include it in the submission.</html>";
			}
			jLabel6.setText(informationMessage);
		} else if ("progress".equals(evt.getPropertyName())) {
			int value = (Integer) evt.getNewValue();
			jProgressBar.setValue(value);
			jProgressBar.setString(value + "%");
			jProgressBar.repaint();
		} else if (PEXBulkSubmissionSummaryTreeLoaderTask.BULK_SUBMISSION_TREE_LOADER_RAW_DATA_PRESENT
				.equals(evt.getPropertyName())) {
			rawDataPresent = true;
		}

	}

	private String getFormatedTime(double timeInSeconds) {
		if (timeInSeconds < 60)
			return timeInSeconds + " sg.";
		if (timeInSeconds > 60) {
			DecimalFormat df = new DecimalFormat("#.#");
			return df.format(timeInSeconds / 60) + " min.";
		}
		return String.valueOf(timeInSeconds);
	}

	private void showFinishDialog() {
		PEXSubmissionPreparedDialog dialog = new PEXSubmissionPreparedDialog(
				this, true, pexFile.getOutputFolder());
		dialog.setVisible(true);
	}

	private String getFileSizeString(File file) {
		if (file != null) {
			long bytes = file.length() / 1024;
			long mBytes = bytes / 1024;
			if (mBytes == 0)
				return bytes + " bytes";
			long gBytes = mBytes / 1024;
			if (gBytes == 0)
				return mBytes + " Mb";
			return gBytes + " Gb";
		}
		return "";
	}

	private String getProgressString() {
		final List<Experiment> experiments = experimentList.getExperiments();
		if (numCreated < experiments.size()) {
			Experiment currentExperiment = experiments.get(numCreated);
			int num = numCreated + 1;
			return "Creating (" + num + "/" + total + ") 'PRIDE for '"
					+ currentExperiment.getName() + "' ...";
		}
		return "";
	}

	private void appendStatus(String notificacion) {
		// log.info("Appending status to: " + notificacion);
		jTextAreaStatus.append(notificacion + "\n");
		jTextAreaStatus
				.setCaretPosition(jTextAreaStatus.getText().length() - 1);

	}

	private void setStatus(String notificacion) {
		log.info("Setting status to: " + notificacion);
		jTextAreaStatus.setText(notificacion + "\n");
		jTextAreaStatus
				.setCaretPosition(jTextAreaStatus.getText().length() - 1);

	}

}