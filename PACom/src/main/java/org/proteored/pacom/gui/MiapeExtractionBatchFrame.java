/*
 * MiapeExtractionBatchFrame.java Created on __DATE__, __TIME__
 */

package org.proteored.pacom.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingWorker.StateValue;
import javax.swing.border.TitledBorder;

import org.jfree.ui.RefineryUtilities;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.pacom.gui.tasks.MiapeExtractionTask;
import org.proteored.pacom.gui.tasks.OntologyLoaderWaiter;
import org.proteored.pacom.utils.AppVersion;
import org.proteored.pacom.utils.HttpUtilities;
import org.proteored.pacom.utils.MiapeExtractionBatchManager;
import org.proteored.pacom.utils.MiapeExtractionResult;

import edu.scripps.yates.utilities.dates.DatesUtil;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 *
 * @author __USER__
 */
public class MiapeExtractionBatchFrame extends AbstractJFrameWithAttachedHelpDialog implements PropertyChangeListener {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("log4j.logger.org.proteored");
	private ControlVocabularyManager cvManager;
	private final TIntObjectHashMap<List<JComponent>> miapeExtractionTaskJComponents = new TIntObjectHashMap<List<JComponent>>();
	private MiapeExtractionBatchManager miapeExtractorBatchManager;
	private final int jProgressBarIndex = 2;
	private final int jButtonStartIndex = 3;
	private final int jButtonStopIndex = 4;
	private final int jButtonMSReportIndex = 5;
	private final int jButtonMSIReportIndex = 6;

	private final MainFrame parentFrame;
	private boolean ontologiesLoaded = false;
	private final TIntObjectHashMap<MiapeExtractionResult> obtainedResults = new TIntObjectHashMap<MiapeExtractionResult>();
	private static MiapeExtractionBatchFrame instance;
	private boolean startAllJobsRequested = false;

	/**
	 * Creates new form MiapeExtractionBatchFrame
	 *
	 * @param mainFrame
	 */
	private MiapeExtractionBatchFrame(MainFrame mainFrame) {
		super(400);
		setResizable(false);
		this.setIconImage(ImageManager.getImageIcon(ImageManager.BATCH_LOAD_LOGO_128).getImage());
		initComponents();
		parentFrame = mainFrame;
		// WebservicesLoaderTask loader = WebservicesLoaderTask.getInstace();
		// miapeExtractorWebservice = loader.getMiapeExtractorWebservice(true);
		// miapeAPIWebservice = loader.getMiapeAPIWebservice(true);

		appendStatus("Loading ontologies...");
		OntologyLoaderWaiter waiter = new OntologyLoaderWaiter();
		waiter.addPropertyChangeListener(this);
		waiter.execute();

		RefineryUtilities.centerFrameOnScreen(this);
		jTextAreaStatus.setFont(new JLabel().getFont());

		AppVersion version = MainFrame.getVersion();
		if (version != null) {
			String suffix = " (v" + version.toString() + ")";
			this.setTitle(getTitle() + suffix);
		}
	}

	public static MiapeExtractionBatchFrame getInstace(MainFrame mainFrame) {
		if (instance == null)
			instance = new MiapeExtractionBatchFrame(mainFrame);
		return instance;
	}

	@Override
	public void dispose() {
		cancelAllJobs();
		if (parentFrame != null)
			parentFrame.setVisible(true);
		super.dispose();
	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		// jFileChooser1 = new javax.swing.JFileChooser();
		jFileChooser1 = new JFileChooser(MainFrame.currentFolder);
		jPanel1 = new javax.swing.JPanel();
		jTextFieldBatchFile = new javax.swing.JTextField();
		jButtonSelectBatchFile = new javax.swing.JButton();
		jPanel3 = new javax.swing.JPanel();
		jScrollPane1 = new javax.swing.JScrollPane();
		jScrollPaneJobQueue = new javax.swing.JScrollPane();
		jPanelJobQueue = new javax.swing.JPanel();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Batch Data Import");

		jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"Batch file"));

		jTextFieldBatchFile.setToolTipText("Input batch import file");

		jButtonSelectBatchFile.setText("Select");
		jButtonSelectBatchFile.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonSelectBatchFileActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(jTextFieldBatchFile, javax.swing.GroupLayout.DEFAULT_SIZE, 985, Short.MAX_VALUE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jButtonSelectBatchFile).addContainerGap()));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup()
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jButtonSelectBatchFile).addComponent(jTextFieldBatchFile,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
		jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING).addComponent(jScrollPane1,
				Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 1208, Short.MAX_VALUE));
		jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING).addComponent(jScrollPane1,
				GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE));
		jTextAreaStatus = new javax.swing.JTextArea();
		jTextAreaStatus.setEditable(false);
		jScrollPane1.setViewportView(jTextAreaStatus);

		jTextAreaStatus.setColumns(20);
		jTextAreaStatus.setLineWrap(true);
		jTextAreaStatus.setRows(5);
		jTextAreaStatus.setWrapStyleWord(true);
		jPanel3.setLayout(jPanel3Layout);

		jScrollPaneJobQueue.setBorder(javax.swing.BorderFactory
				.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Job queue"));

		javax.swing.GroupLayout jPanelJobQueueLayout = new javax.swing.GroupLayout(jPanelJobQueue);
		jPanelJobQueue.setLayout(jPanelJobQueueLayout);
		jPanelJobQueueLayout.setHorizontalGroup(jPanelJobQueueLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 1079, Short.MAX_VALUE));
		jPanelJobQueueLayout.setVerticalGroup(jPanelJobQueueLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 338, Short.MAX_VALUE));

		jScrollPaneJobQueue.setViewportView(jPanelJobQueue);

		JPanel jPanelImage = new JPanel();
		jPanelImage.setBorder(new TitledBorder(null, "Controls", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(jPanel3, 0, 0, Short.MAX_VALUE)
						.addComponent(jPanel1, 0, 0, Short.MAX_VALUE)
						.addGroup(layout.createSequentialGroup()
								.addComponent(jPanelImage, GroupLayout.PREFERRED_SIZE, 145, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addComponent(jScrollPaneJobQueue, GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)))
				.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(Alignment.LEADING)
						.addComponent(jScrollPaneJobQueue, GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
						.addComponent(jPanelImage, GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE))
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		GridBagLayout gbl_jPanelImage = new GridBagLayout();
		gbl_jPanelImage.columnWidths = new int[] { 0, 0 };
		gbl_jPanelImage.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_jPanelImage.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_jPanelImage.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		jPanelImage.setLayout(gbl_jPanelImage);
		jButtonStart = new javax.swing.JButton();
		GridBagConstraints gbc_jButtonStart = new GridBagConstraints();
		gbc_jButtonStart.fill = GridBagConstraints.HORIZONTAL;
		gbc_jButtonStart.insets = new Insets(0, 0, 5, 0);
		gbc_jButtonStart.gridx = 0;
		gbc_jButtonStart.gridy = 0;
		jPanelImage.add(jButtonStart, gbc_jButtonStart);

		jButtonStart.setText("Start batch import");
		jButtonStart.setEnabled(false);
		jButtonRestartFailedTasks = new javax.swing.JButton();
		GridBagConstraints gbc_jButtonRestartFailedTasks = new GridBagConstraints();
		gbc_jButtonRestartFailedTasks.fill = GridBagConstraints.HORIZONTAL;
		gbc_jButtonRestartFailedTasks.insets = new Insets(0, 0, 5, 0);
		gbc_jButtonRestartFailedTasks.gridx = 0;
		gbc_jButtonRestartFailedTasks.gridy = 1;
		jPanelImage.add(jButtonRestartFailedTasks, gbc_jButtonRestartFailedTasks);

		jButtonRestartFailedTasks.setText("Restart failed tasks");
		jButtonRestartFailedTasks.setEnabled(false);
		jButtonCancel = new javax.swing.JButton();
		GridBagConstraints gbc_jButtonCancel = new GridBagConstraints();
		gbc_jButtonCancel.fill = GridBagConstraints.HORIZONTAL;
		gbc_jButtonCancel.insets = new Insets(0, 0, 5, 0);
		gbc_jButtonCancel.gridx = 0;
		gbc_jButtonCancel.gridy = 2;
		jPanelImage.add(jButtonCancel, gbc_jButtonCancel);

		jButtonCancel.setText("Cancel all");
		jButtonCancel.setEnabled(false);

		JButton jButtonHelp2 = new OpenHelpButton(this);
		GridBagConstraints gbc_jbuttonHelp2 = new GridBagConstraints();
		gbc_jbuttonHelp2.anchor = GridBagConstraints.WEST;
		gbc_jbuttonHelp2.insets = new Insets(0, 0, 5, 0);
		gbc_jbuttonHelp2.gridx = 0;
		gbc_jbuttonHelp2.gridy = 3;
		jPanelImage.add(jButtonHelp2, gbc_jbuttonHelp2);
		jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonCancelActionPerformed(evt);
			}
		});
		jButtonRestartFailedTasks.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonRestartFailedTasksActionPerformed(evt);
			}
		});
		jButtonStart.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonStartActionPerformed(evt);
			}
		});
		getContentPane().setLayout(layout);

		pack();
	}// </editor-fold>

	// GEN-END:initComponents

	private void jButtonRestartFailedTasksActionPerformed(java.awt.event.ActionEvent evt) {
		if (!miapeExtractorBatchManager.getRunningJobs().isEmpty())
			appendStatus("Failed jobs will be restarted after current job was finished.");
		restartFailedJobs();
	}

	private void restartFailedJobs() {
		boolean anyJobRestarted = miapeExtractorBatchManager.startMiapeExtractions(true);
		if (!anyJobRestarted) {
			appendStatus("No jobs have been restarted. Failed jobs can only be restarted twice.");
		}
	}

	private void jButtonStartActionPerformed(java.awt.event.ActionEvent evt) {
		startAllJobs();

	}

	private void startAllJobs() {
		startAllJobsRequested = true;
		if (!ontologiesLoaded) {
			appendStatus("Ontologies are being loading. Please wait...");
			jButtonStart.setEnabled(false);
			return;
		}
		if (miapeExtractorBatchManager == null) {
			appendStatus("Load a batch file first");
			return;
		}
		appendStatus("Starting batch import file process...");

		miapeExtractorBatchManager.startMiapeExtractions(false);

		jTextAreaStatus.setText("");

	}

	private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {
		cancelAllJobs();
		jButtonCancel.setEnabled(false);
		jButtonStart.setEnabled(true);

	}

	private void cancelAllJobs() {

		if (miapeExtractorBatchManager != null) {
			final List<MiapeExtractionTask> miapeExtractionQueue = miapeExtractorBatchManager.getMiapeExtractionQueue();
			boolean pendingJobs = false;
			for (MiapeExtractionTask miapeExtractionTask : miapeExtractionQueue) {
				if (miapeExtractionTask.getState() != StateValue.DONE) {
					pendingJobs = true;
					break;
				}
			}
			if (pendingJobs) {
				// show warning
				final int option = JOptionPane.showConfirmDialog(this,
						"Are you sure you want to discard all pending jobs?", "Cancelling jobs",
						JOptionPane.YES_NO_CANCEL_OPTION);
				if (option == JOptionPane.YES_OPTION) {
					appendStatus("Cancelling all jobs");
					miapeExtractorBatchManager.cancelMiapeExtractions();
				}
				for (List<JComponent> components : miapeExtractionTaskJComponents.valueCollection()) {
					components.get(jButtonStartIndex).setEnabled(true);
					components.get(jButtonStopIndex).setEnabled(false);
				}
			}
		}
	}

	private void jButtonSelectBatchFileActionPerformed(java.awt.event.ActionEvent evt) {
		jPanelJobQueue.removeAll();
		jPanelJobQueue.repaint();
		selectInputBatchFile();
		jButtonStart.setEnabled(true);
	}

	private void selectInputBatchFile() {

		jFileChooser1 = new JFileChooser(MainFrame.currentFolder);
		jFileChooser1.setDialogTitle("Select a import batch file");
		jFileChooser1.setFileFilter(new TFileExtension("txt files", new String[] { "txt" }));
		jFileChooser1.showOpenDialog(this);
		File file;
		File selectedFile = jFileChooser1.getSelectedFile();
		if (selectedFile != null) {
			MainFrame.currentFolder = selectedFile.getParentFile();
			jTextAreaStatus.setText("");
			appendStatus("Loading batch file...");
			file = selectedFile;
			MiapeExtractionTask.resetIdentifiers();

			jTextFieldBatchFile.setText(file.getAbsolutePath());
			log.info("Selected File: " + file.getAbsolutePath());

			startMiapeExtractionBatchManager(file);

		}
	}

	private void startMiapeExtractionBatchManager(File file) {
		try {
			miapeExtractorBatchManager = new MiapeExtractionBatchManager(file, this, cvManager);
			List<MiapeExtractionTask> miapeExtractionQueue = miapeExtractorBatchManager.getMiapeExtractionQueue();
			loadJobQueuePanel(miapeExtractionQueue);
			updateButtonsState();
		} catch (IllegalMiapeArgumentException e) {
			appendStatus("Error: " + e.getMessage());
		}
	}

	private void loadJobQueuePanel(List<MiapeExtractionTask> miapeExtractionQueue) {
		jPanelJobQueue.removeAll();
		jPanelJobQueue.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 10, 2, 10);
		c.gridy = 0;
		c.gridx = 0;
		c.anchor = GridBagConstraints.NORTHWEST;
		for (MiapeExtractionTask miapeExtractionTask : miapeExtractionQueue) {
			List<JComponent> jComponents = getComponents(miapeExtractionTask);
			c.gridx = 0;
			for (JComponent jComponent : jComponents) {
				jPanelJobQueue.add(jComponent, c);
				c.gridx++;
			}
			c.gridy++;
		}
		jPanelJobQueue.repaint();
		jScrollPaneJobQueue.setAlignmentX(Component.LEFT_ALIGNMENT);

		appendStatus(miapeExtractorBatchManager.getMiapeExtractionQueue().size() + " jobs loaded from batch file.");
		pack();

		jButtonStart.setEnabled(true);
	}

	private List<JComponent> getComponents(final MiapeExtractionTask miapeExtractionTask) {
		List<JComponent> componentList = new ArrayList<JComponent>();
		log.info("Getting components from task: " + miapeExtractionTask.getRunIdentifier());

		String labelText = "";

		labelText = "Job '" + miapeExtractionTask.getRunIdentifier() + "'";

		// job id label
		JLabel label = new JLabel(labelText);
		componentList.add(label);

		// description label
		JLabel label2 = new JLabel("<html>" + miapeExtractionTask.getDescription() + "</html>");
		componentList.add(label2);

		// Progress bar
		JProgressBar bar = new JProgressBar();
		// bar.setSize(50, bar.getHeight());
		bar.setToolTipText("progress from task " + miapeExtractionTask.getRunIdentifier());
		bar.setStringPainted(true);
		bar.setString("");
		componentList.add(bar);

		// BUttons
		final JButton startButton = new JButton("start");
		final JButton stopButton = new JButton("cancel");
		final JButton msReportButton = new JButton("MS dataset");
		msReportButton.setIcon(ImageManager.getImageIcon(ImageManager.DOC));
		final JButton msiReportButton = new JButton("Id dataset");
		msiReportButton.setIcon(ImageManager.getImageIcon(ImageManager.DOC));

		// Start button
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!ontologiesLoaded) {
					MiapeExtractionBatchFrame.this
							.appendStatus("Please, wait for the ontologies loading, and then click again on start...");
					return;
				}
				int runIdentifier = miapeExtractionTask.getRunIdentifier();

				boolean started = miapeExtractorBatchManager.startMiapeExtraction(runIdentifier);
				if (!started)
					MiapeExtractionBatchFrame.this.appendStatus("This job cannot be started.");
			}
		});
		componentList.add(startButton);
		// Stop button
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int runIdentifier = miapeExtractionTask.getRunIdentifier();
				miapeExtractorBatchManager.cancelMiapeExtraction(runIdentifier);
			}
		});
		stopButton.setEnabled(false);
		componentList.add(stopButton);

		// report button
		msReportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int runIdentifier = miapeExtractionTask.getRunIdentifier();
				if (obtainedResults.containsKey(runIdentifier)) {
					MiapeExtractionResult miapeExtractionResult = obtainedResults.get(runIdentifier);
					Integer miapeID = miapeExtractionResult.getMiapeMS_Identifier();
					if (miapeID != null) {

						File directLink = obtainedResults.get(runIdentifier).getDirectLinkToMIAPEMS();
						if (directLink != null) {

							Object[] dialog_options = { "Yes, open data file", "No, close this dialog" };
							int selected_option = JOptionPane.showOptionDialog(MiapeExtractionBatchFrame.this,
									"Click on yes to open the data file." + "\n", "Show data file",
									JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
									dialog_options, dialog_options[1]);
							if (selected_option == 0) { // Yes
								HttpUtilities.openURL(directLink.toString());
							}
						}

					}
				}
			}
		});
		msReportButton.setEnabled(false);
		msReportButton.setToolTipText("Show imported dataset file in your local folderr");
		componentList.add(msReportButton);

		// report button
		msiReportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int runIdentifier = miapeExtractionTask.getRunIdentifier();
				if (obtainedResults.containsKey(runIdentifier)) {
					MiapeExtractionResult miapeExtractionResult = obtainedResults.get(runIdentifier);
					Integer miapeID = miapeExtractionResult.getMiapeMSI_Identifier();
					if (miapeID != null) {

						File directLink = obtainedResults.get(runIdentifier).getDirectLinkToMIAPEMSI();
						if (directLink != null) {
							Object[] dialog_options = { "Yes, open data file", "No, close this dialog" };
							int selected_option = JOptionPane.showOptionDialog(MiapeExtractionBatchFrame.this,
									"Click on yes to open the data file." + "\n", "Show data file",
									JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
									dialog_options, dialog_options[1]);
							if (selected_option == 0) { // Yes
								Desktop desktop = Desktop.getDesktop();

								try {
									desktop.open(directLink);
								} catch (IOException e1) {
									e1.printStackTrace();
								}

							}
						}

					}
				}
			}
		});
		msiReportButton.setEnabled(false);
		msiReportButton.setToolTipText("Show imported dataset file in your local folder");
		componentList.add(msiReportButton);

		miapeExtractionTaskJComponents.put(miapeExtractionTask.getRunIdentifier(), componentList);

		return componentList;
	}

	private void appendStatus(String text) {
		jTextAreaStatus.append(text + "\n");
		jTextAreaStatus.setCaretPosition(jTextAreaStatus.getText().length() - 1);
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new MiapeExtractionBatchFrame(null).setVisible(true);
			}
		});
	}

	// GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.JButton jButtonCancel;
	private javax.swing.JButton jButtonRestartFailedTasks;
	private javax.swing.JButton jButtonSelectBatchFile;
	private javax.swing.JButton jButtonStart;
	private javax.swing.JFileChooser jFileChooser1;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanelJobQueue;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPaneJobQueue;
	private javax.swing.JTextArea jTextAreaStatus;
	private javax.swing.JTextField jTextFieldBatchFile;

	// End of variables declaration//GEN-END:variables

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (MiapeExtractionTask.MIAPE_CREATION_STARTS.equals(evt.getPropertyName())) {
			// disable start button
			jButtonStart.setEnabled(false);
			// enable cancel button
			jButtonCancel.setEnabled(true);

			int jobID = (Integer) evt.getNewValue();
			enableMiapeMSIreport(jobID, false);
			enableMiapeMSreport(jobID, false);

			if (miapeExtractorBatchManager.isProcessing(jobID)) {
				JProgressBar progressBar = getProgressBar(jobID);
				if (progressBar != null) {
					progressBar.setString("Extracting input data...");
					progressBar.setIndeterminate(true);
				}
				JButton startButton = getStartButton(jobID);
				startButton.setEnabled(false);
				JButton stopButton = getStopButton(jobID);
				stopButton.setEnabled(true);

				appendStatus("\nStartig Job '" + jobID + "' at: " + getFormatedDate());
			}

		} else if (MiapeExtractionTask.MIAPE_CREATION_ERROR.equals(evt.getPropertyName())) {
			MiapeExtractionResult result = (MiapeExtractionResult) evt.getNewValue();
			int jobID = result.getMiapeExtractionTaskIdentifier();

			JProgressBar progressBar = getProgressBar(jobID);
			if (progressBar != null) {
				progressBar.setString("FAILED");
				progressBar.setForeground(Color.RED);
				progressBar.setIndeterminate(false);
				progressBar.setValue(100);
				// write a tooltip to the progress bar of the job containing the
				// error message
				progressBar.setToolTipText("Error: " + result.getErrorMessage());
			}
			JButton startButton = getStartButton(jobID);
			if (startButton != null)
				startButton.setEnabled(true);
			JButton stopButton = getStopButton(jobID);
			if (stopButton != null)
				stopButton.setEnabled(false);
			appendStatus("Error in job '" + jobID + "' : " + result.getErrorMessage() + " at: " + getFormatedDate());

			updateButtonsState();
		} else if (MiapeExtractionTask.MIAPE_CREATION_TOTAL_DONE.equals(evt.getPropertyName())) {
			MiapeExtractionResult result = (MiapeExtractionResult) evt.getNewValue();

			int jobID = result.getMiapeExtractionTaskIdentifier();
			obtainedResults.put(jobID, result);
			String timeElapsed = DatesUtil.getDescriptiveTimeFromMillisecs(result.getMilliseconds());
			appendStatus("\nJob '" + jobID + "' finished at " + getFormatedDate() + ". It took " + timeElapsed + ".");
			String message = "Created datasets are: ";
			String extendedMessage = "Datasets created in " + timeElapsed + "<br> ";
			if (result.getMiapeMS_Identifier() != null) {
				message = "MS Dataset ID: " + result.getMiapeMS_Identifier();
				extendedMessage += message;
				if (result.getDirectLinkToMIAPEMS() != null)
					extendedMessage += " Direct link: " + result.getDirectLinkToMIAPEMS();
				// enable MS report button if available
				if (result.getDirectLinkToMIAPEMS() != null)
					enableMiapeMSreport(jobID, true);
			}
			if (result.getMiapeMSI_Identifier() != null) {
				if (!"Created datasets are: ".equals(message)) {
					message += ", ";
					extendedMessage += "<br>";
				}
				message += "Dataset ID: " + result.getMiapeMSI_Identifier();
				extendedMessage += "Dataset ID: " + result.getMiapeMSI_Identifier();
				if (result.getDirectLinkToMIAPEMSI() != null) {
					extendedMessage += " Direct link: " + result.getDirectLinkToMIAPEMSI();
					message += " Created file: " + result.getDirectLinkToMIAPEMSI();
				}
				// enable MSI report button if available
				if (result.getDirectLinkToMIAPEMSI() != null)
					enableMiapeMSIreport(jobID, true);
			}
			appendStatus(message);
			JProgressBar progressBar = getProgressBar(jobID);
			if (progressBar != null) {
				progressBar.setString("FINISHED (" + timeElapsed + ".)");
				progressBar.setValue(100);
				progressBar.setForeground(Color.GREEN);
				progressBar.setIndeterminate(false);
				progressBar.setToolTipText("<html>" + extendedMessage + "</html>");
			}
			JButton startButton = getStartButton(jobID);
			startButton.setEnabled(false);
			JButton stopButton = getStopButton(jobID);
			stopButton.setEnabled(false);

			updateButtonsState();

		} else if (MiapeExtractionTask.MIAPE_MS_CREATED_DONE.equals(evt.getPropertyName())) {
			File ms_id = (File) evt.getNewValue();
			log.info("MS dataset created at:" + ms_id.getAbsolutePath());
		} else if (MiapeExtractionTask.MIAPE_MSI_CREATED_DONE.equals(evt.getPropertyName())) {
			File msi_id = (File) evt.getNewValue();
			log.info("Identification dataset created at:" + msi_id.getAbsolutePath());
		} else if (MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE.equals(evt.getPropertyName())) {
			String description = (String) evt.getNewValue();
			if (evt.getOldValue() != null) {
				int jobID = (Integer) evt.getOldValue();
				if (miapeExtractorBatchManager.isProcessing(jobID)) {
					JProgressBar bar = getProgressBar(jobID);
					if (bar != null)
						bar.setString("Copying file...");
				}
			}
		} else if (MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE.equals(evt.getPropertyName())) {
			String description = (String) evt.getNewValue();
			if (evt.getOldValue() != null) {
				int jobID = (Integer) evt.getOldValue();
				if (miapeExtractorBatchManager.isProcessing(jobID)) {
					JProgressBar bar = getProgressBar(jobID);
					if (bar != null)
						bar.setString("Extracting information...");
				}
			}
		} else if (MiapeExtractionTask.MIAPE_CREATION_COMPRESSING_FILE.equals(evt.getPropertyName())) {
			int jobID = (Integer) evt.getNewValue();
			if (miapeExtractorBatchManager.isProcessing(jobID)) {
				JProgressBar bar = getProgressBar(jobID);
				if (bar != null)
					bar.setString("Compressing file...");
			}
		} else if (MiapeExtractionTask.MIAPE_CREATION_COMPRESSING_FILE_DONE.equals(evt.getPropertyName())) {
			int jobID = (Integer) evt.getNewValue();
			if (miapeExtractorBatchManager.isProcessing(jobID)) {
				JProgressBar bar = getProgressBar(jobID);
				if (bar != null)
					bar.setString("Compressing file done.");
			}
		} else if (OntologyLoaderWaiter.ONTOLOGY_LOADED.equals(evt.getPropertyName())) {
			cvManager = (ControlVocabularyManager) evt.getNewValue();
			appendStatus("Ontologies loaded.");
			ontologiesLoaded = true;
			if (startAllJobsRequested)
				startAllJobs();
		} else if (MiapeExtractionTask.MIAPE_CREATION_CANCELED.equals(evt.getPropertyName())) {
			int jobID = (Integer) evt.getNewValue();
			JProgressBar bar = getProgressBar(jobID);
			if (bar != null) {
				bar.setValue(100);
				bar.setString("CANCELED");
				bar.setForeground(new Color(255, 153, 51));
				bar.setIndeterminate(false);
			}
			JButton startButton = getStartButton(jobID);
			startButton.setEnabled(true);
			JButton stopButton = getStopButton(jobID);
			stopButton.setEnabled(false);
			appendStatus("Job '" + jobID + "' canceled at: " + getFormatedDate());
			updateButtonsState();

		} else if (MiapeExtractionTask.MIAPE_CREATION_WAITING_FOR_OTHER_JOB_COMPLETION.equals(evt.getPropertyName())) {
			MiapeExtractionResult result = (MiapeExtractionResult) evt.getNewValue();
			int jobID = result.getMiapeExtractionTaskIdentifier();

			JProgressBar bar = getProgressBar(jobID);
			if (bar != null) {
				bar.setValue(100);
				bar.setString("Waiting for other job...");
				bar.setIndeterminate(false);
			}
		}

	}

	private void updateButtonsState() {
		// check if there is any failed job, and if yes, enable start failed
		// button
		jButtonRestartFailedTasks.setEnabled(!miapeExtractorBatchManager.getFailedJobs().isEmpty());

		// check if there is any running job, if not, disable cancel running
		// jobs
		if (miapeExtractorBatchManager.getRunningJobs().isEmpty())
			jButtonCancel.setEnabled(false);

	}

	private void enableMiapeMSreport(int jobID, boolean b) {
		if (miapeExtractionTaskJComponents.containsKey(jobID)) {
			List<JComponent> list = miapeExtractionTaskJComponents.get(jobID);
			JComponent button = list.get(jButtonMSReportIndex);
			button.setEnabled(b);
		}

	}

	private void enableMiapeMSIreport(int jobID, boolean b) {
		if (miapeExtractionTaskJComponents.containsKey(jobID)) {
			List<JComponent> list = miapeExtractionTaskJComponents.get(jobID);
			JComponent button = list.get(jButtonMSIReportIndex);
			button.setEnabled(b);
		}

	}

	private String getFormatedDate() {
		String formatedDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG)
				.format(new Date(System.currentTimeMillis()));
		return formatedDate;
	}

	private JButton getStopButton(int jobID) {
		if (miapeExtractionTaskJComponents.containsKey(jobID)) {
			return (JButton) miapeExtractionTaskJComponents.get(jobID).get(4);
		}
		return null;
	}

	private JButton getStartButton(int jobID) {
		if (miapeExtractionTaskJComponents.containsKey(jobID)) {
			return (JButton) miapeExtractionTaskJComponents.get(jobID).get(3);
		}
		return null;
	}

	private JProgressBar getProgressBar(int jobID) {
		if (miapeExtractionTaskJComponents.containsKey(jobID)) {
			List<JComponent> list = miapeExtractionTaskJComponents.get(jobID);
			JProgressBar progressBar = (JProgressBar) list.get(jProgressBarIndex);
			return progressBar;
		}
		return null;
	}

	@Override
	public List<String> getHelpMessages() {
		List<String> ret = new ArrayList<String>();

		ret.add("Batch import data help");
		ret.add("In order to import multiple input files as different datasets in PACOM you need to use an <b>Batch Import text file</b>.");
		ret.add("<b>Batch import text file format:</b>");
		ret.add("This file is composed by multiple blocks, each one corresponding to one <b>dataset import job</b>. Each block starts with '<b>"
				+ MiapeExtractionBatchManager.START_MIAPE_EXTRACTION
				+ "</b>' followed by a <b>job number</b>, and ends with '<b>"
				+ MiapeExtractionBatchManager.END_MIAPE_EXTRACTION + "</b>'.");
		ret.add("In each block you need to specify the following items (each one in one line):");
		ret.add("- (Mandatory) One '<b>" + MiapeExtractionBatchManager.MIAPE_PROJECT
				+ "</b>' element followed by a name of the project. It is just a way of organizing the data into the system, so it will appear under that folder when creating the inspection project.");
		ret.add("- (Mandatory) One input type element (<b>" + MiapeExtractionBatchManager.getInputTypesString()
				+ "</b>) followed by the full path to the input file.");
		ret.add("- (Optional) One '<b>" + MiapeExtractionBatchManager.METADATA
				+ "</b>' element followed by the name of a MS metadata template previously generated (in single Data Import option).");
		ret.add("- (Optional) One '<b>" + MiapeExtractionBatchManager.MS_JOB_REF
				+ "</b>' element followed by the job number of a previous job in which a MS dataset has been generated, that is, using '<b>"
				+ MiapeExtractionBatchManager.MGF + "</b>' or '<b>" + MiapeExtractionBatchManager.MZML
				+ "</b>' input data files alone. This will link the MS dataset to the identification dataset specified in the same job.");
		ret.add("");
		ret.add("Once you have your Batch Import text file, you can load it here and it will be validated. If everything is ok, you will see a dataset import job in the <b>job queue</b>.");
		ret.add("After that, you can start all jobs sequencially by clicking on <i>'Start batch import'</i> button, or you can start individual jobs in the job queue.");
		ret.add("<b>Example batch import text files</b>");
		ret.add("PACOM package contains some <b>example batch import text files</b> located at the installation folder (<i>'"
				+ System.getProperty("user.dir") + "'</i>) that you can use as templates, such as:");
		ret.add("- <i>Batch_Import_from_DTASelect_files.txt</i>");
		ret.add("- <i>Batch_Import_from_mzIdentML_MGF_files.txt</i>");
		ret.add("- <i>Batch_Import_from_Xtandem_files.txt</i>");

		return ret;
	}
}