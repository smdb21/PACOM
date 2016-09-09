/*
 * MiapeExtractionBatchFrame.java Created on __DATE__, __TIME__
 */

package org.proteored.miapeExtractor.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jfree.ui.RefineryUtilities;
import org.proteored.miapeExtractor.gui.tasks.MiapeExtractionTask;
import org.proteored.miapeExtractor.gui.tasks.OntologyLoaderWaiter;
import org.proteored.miapeExtractor.utils.HttpUtilities;
import org.proteored.miapeExtractor.utils.MiapeExtractionBatchManager;
import org.proteored.miapeExtractor.utils.MiapeExtractionResult;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.util.MiapeReportsLinkGenerator;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeAPIWebserviceDelegate;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeDatabaseException_Exception;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeSecurityException_Exception;
import org.proteored.miapeapi.webservice.clients.miapeextractor.MiapeExtractorDelegate;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

/**
 *
 * @author __USER__
 */
public class MiapeExtractionBatchFrame extends javax.swing.JFrame implements PropertyChangeListener {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("log4j.logger.org.proteored");
	private final MiapeExtractorDelegate miapeExtractorWebservice = null;
	private final MiapeAPIWebserviceDelegate miapeAPIWebservice = null;
	private ControlVocabularyManager cvManager;
	private final HashMap<Integer, List<JComponent>> miapeExtractionTaskJComponents = new HashMap<Integer, List<JComponent>>();
	private MiapeExtractionBatchManager miapeExtractorBatchManager;
	private final int jProgressBarIndex = 2;
	private final int jButtonStartIndex = 3;
	private final int jButtonStopIndex = 4;
	private final int jButtonMSReportIndex = 5;
	private final int jButtonMSIReportIndex = 6;

	private final MainFrame parentFrame;
	private boolean ontologiesLoaded = false;
	private final HashMap<Integer, MiapeExtractionResult> obtainedResults = new HashMap<Integer, MiapeExtractionResult>();
	private static MiapeExtractionBatchFrame instance;
	private boolean startAllJobsRequested = false;

	/**
	 * Creates new form MiapeExtractionBatchFrame
	 *
	 * @param mainFrame
	 */
	private MiapeExtractionBatchFrame(MainFrame mainFrame) {
		try {
			UIManager.setLookAndFeel(new WindowsLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
		}
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
		jFileChooser1 = MainFrame.fileChooser;
		jPanel1 = new javax.swing.JPanel();
		jTextFieldBatchFile = new javax.swing.JTextField();
		jButtonSelectBatchFile = new javax.swing.JButton();
		jPanel3 = new javax.swing.JPanel();
		jButtonStart = new javax.swing.JButton();
		jButtonCancel = new javax.swing.JButton();
		jScrollPane1 = new javax.swing.JScrollPane();
		jTextAreaStatus = new javax.swing.JTextArea();
		jButtonRestartFailedTasks = new javax.swing.JButton();
		jScrollPaneJobQueue = new javax.swing.JScrollPane();
		jPanelJobQueue = new javax.swing.JPanel();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Batch data import");

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

		jButtonStart.setText("Start batch import");
		jButtonStart.setEnabled(false);
		jButtonStart.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonStartActionPerformed(evt);
			}
		});

		jButtonCancel.setText("Cancel all");
		jButtonCancel.setEnabled(false);
		jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonCancelActionPerformed(evt);
			}
		});

		jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		jTextAreaStatus.setColumns(20);
		jTextAreaStatus.setLineWrap(true);
		jTextAreaStatus.setRows(5);
		jTextAreaStatus.setWrapStyleWord(true);
		jScrollPane1.setViewportView(jTextAreaStatus);

		jButtonRestartFailedTasks.setText("Restart failed tasks");
		jButtonRestartFailedTasks.setEnabled(false);
		jButtonRestartFailedTasks.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonRestartFailedTasksActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
		jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel3Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(jButtonRestartFailedTasks, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jButtonCancel, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jButtonStart, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 913, Short.MAX_VALUE)));
		jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel3Layout.createSequentialGroup().addContainerGap().addComponent(jButtonStart)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jButtonCancel)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jButtonRestartFailedTasks).addContainerGap(18, Short.MAX_VALUE))
				.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE));

		jScrollPaneJobQueue.setBorder(javax.swing.BorderFactory
				.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Job queue"));
		jScrollPaneJobQueue.setAutoscrolls(true);

		javax.swing.GroupLayout jPanelJobQueueLayout = new javax.swing.GroupLayout(jPanelJobQueue);
		jPanelJobQueue.setLayout(jPanelJobQueueLayout);
		jPanelJobQueueLayout.setHorizontalGroup(jPanelJobQueueLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 1079, Short.MAX_VALUE));
		jPanelJobQueueLayout.setVerticalGroup(jPanelJobQueueLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 338, Short.MAX_VALUE));

		jScrollPaneJobQueue.setViewportView(jPanelJobQueue);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
								.addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(jScrollPaneJobQueue, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, 1091, Short.MAX_VALUE)
								.addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE))
						.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jScrollPaneJobQueue, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap()));

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
				for (List<JComponent> components : miapeExtractionTaskJComponents.values()) {
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
		File currentDirectory = new File(System.getProperty("user.dir"));
		if (jFileChooser1 != null)
			currentDirectory = jFileChooser1.getCurrentDirectory();
		jFileChooser1 = new JFileChooser(currentDirectory);
		jFileChooser1.setDialogTitle("Select a import batch file");
		jFileChooser1.setFileFilter(new TFileExtension("txt files", new String[] { "txt" }));
		jFileChooser1.showOpenDialog(this);
		File file;
		if (jFileChooser1.getSelectedFile() != null) {
			jTextAreaStatus.setText("");
			appendStatus("Loading batch file...");
			file = jFileChooser1.getSelectedFile();
			MiapeExtractionTask.resetIdentifiers();

			jTextFieldBatchFile.setText(file.getAbsolutePath());
			log.info("Selected File: " + file.getAbsolutePath());

			startMiapeExtractionBatchManager(file);

		}
	}

	private void startMiapeExtractionBatchManager(File file) {
		try {
			miapeExtractorBatchManager = new MiapeExtractionBatchManager(file, this, miapeExtractorWebservice,
					miapeAPIWebservice, cvManager);
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
		if (miapeExtractionTask.getParameters().isLocalProcessing()) {
			labelText = "Job '" + miapeExtractionTask.getRunIdentifier() + "'";
		} else {
			labelText = "Job '" + miapeExtractionTask.getRunIdentifier() + "' (upload to server)";
		}
		// job id label
		JLabel label = new JLabel(labelText);
		componentList.add(label);

		// description label
		JLabel label2 = new JLabel("<html>" + miapeExtractionTask.getDescription() + "</html>");
		componentList.add(label2);

		// Progress bar
		JProgressBar bar = new JProgressBar();
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
						if (!miapeExtractionTask.isLocalMIAPEExtraction()) {
							String miapeType = "MS";
							int userId = getUserID();
							URL directLink = MiapeReportsLinkGenerator.getMiapePublicLink(userId, miapeID, miapeType);
							if (directLink != null) {
								Object[] dialog_options = { "Yes, open browser", "No, close this dialog" };
								int selected_option = JOptionPane.showOptionDialog(MiapeExtractionBatchFrame.this,
										"Click on yes to open a browser to go directly to the MIAPE " + miapeType
												+ " document report." + "\n",
										"Show MIAPE " + miapeType + " report", JOptionPane.YES_NO_CANCEL_OPTION,
										JOptionPane.QUESTION_MESSAGE, null, dialog_options, dialog_options[1]);
								if (selected_option == 0) { // Yes
									HttpUtilities.openURL(directLink.toString());
								}
							}
						} else {
							URL directLink = obtainedResults.get(runIdentifier).getDirectLinkToMIAPEMS();
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
			}
		});
		msReportButton.setEnabled(false);
		msReportButton.setToolTipText("Show MIAPE MS report on the online MIAPE Generator tool");
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

						if (!miapeExtractionTask.isLocalMIAPEExtraction()) {
							String miapeType = "MSI";
							int userId = getUserID();
							URL directLink = MiapeReportsLinkGenerator.getMiapePublicLink(userId, miapeID, miapeType);
							if (directLink != null) {
								Object[] dialog_options = { "Yes, open browser", "No, close this dialog" };
								int selected_option = JOptionPane.showOptionDialog(MiapeExtractionBatchFrame.this,
										"Click on yes to open a browser to go directly to the dataset " + miapeType
												+ " document report." + "\n",
										"Show MIAPE " + miapeType + " report", JOptionPane.YES_NO_CANCEL_OPTION,
										JOptionPane.QUESTION_MESSAGE, null, dialog_options, dialog_options[1]);
								if (selected_option == 0) { // Yes
									HttpUtilities.openURL(directLink.toString());
								}
							}
						} else {
							URL directLink = obtainedResults.get(runIdentifier).getDirectLinkToMIAPEMSI();
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
			}
		});
		msiReportButton.setEnabled(false);
		if (miapeExtractionTask.isLocalMIAPEExtraction())
			msiReportButton.setToolTipText("Show imported dataset file in your local folder");
		else
			msiReportButton.setToolTipText("Show MIAPE MSI report in the online MIAPE Generator tool");
		componentList.add(msiReportButton);

		miapeExtractionTaskJComponents.put(miapeExtractionTask.getRunIdentifier(), componentList);

		return componentList;
	}

	private void appendStatus(String text) {
		jTextAreaStatus.append(text + "\n");
		jTextAreaStatus.setCaretPosition(jTextAreaStatus.getText().length() - 1);
	}

	private int getUserID() {
		try {
			if (MainFrame.userName != null && MainFrame.password != null)
				return miapeAPIWebservice.getUserId(MainFrame.userName, MainFrame.password);
		} catch (MiapeDatabaseException_Exception e) {
			e.printStackTrace();
		} catch (MiapeSecurityException_Exception e) {
			e.printStackTrace();
		}
		return -1;
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
			long sg = result.getMilliseconds() / 1000;
			appendStatus("\nJob '" + jobID + "' finished at " + getFormatedDate() + ". It took " + sg + " sg.");
			String message = "Created datasets are: ";
			String extendedMessage = "";
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
				message += "Identification dataset ID: " + result.getMiapeMSI_Identifier();
				extendedMessage += "Identification dataset ID: " + result.getMiapeMSI_Identifier();
				if (result.getDirectLinkToMIAPEMSI() != null)
					extendedMessage += " Direct link: " + result.getDirectLinkToMIAPEMSI();
				// enable MSI report button if available
				if (result.getDirectLinkToMIAPEMSI() != null)
					enableMiapeMSIreport(jobID, true);
			}
			appendStatus(message);
			JProgressBar progressBar = getProgressBar(jobID);
			if (progressBar != null) {
				progressBar.setString("FINISHED (" + sg + " sg.)");
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
			String ms_id = (String) evt.getNewValue();
			log.info("MS dataset created with ID:" + ms_id);
		} else if (MiapeExtractionTask.MIAPE_MSI_CREATED_DONE.equals(evt.getPropertyName())) {
			String msi_id = (String) evt.getNewValue();
			log.info("Identification dataset created with ID:" + msi_id);
		} else if (MiapeExtractionTask.MIAPE_CREATION_UPLOADING_FILE.equals(evt.getPropertyName())) {
			int jobID = (Integer) evt.getNewValue();
			if (miapeExtractorBatchManager.isProcessing(jobID)) {
				JProgressBar bar = getProgressBar(jobID);
				if (bar != null)
					bar.setString("Uploading file...");
			}
		} else if (MiapeExtractionTask.MIAPE_CREATION_UPLOADING_FILE_DONE.equals(evt.getPropertyName())) {
			int jobID = (Integer) evt.getNewValue();
			if (miapeExtractorBatchManager.isProcessing(jobID)) {
				JProgressBar bar = getProgressBar(jobID);
				if (bar != null)
					bar.setString("Uploading file done.");
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
		} else if (MiapeExtractionTask.MIAPE_CREATION_WAITING_FOR_SERVER.equals(evt.getPropertyName())) {
			int jobID = (Integer) evt.getNewValue();
			if (miapeExtractorBatchManager.isProcessing(jobID)) {
				JProgressBar bar = getProgressBar(jobID);
				if (bar != null)
					bar.setString("Waiting for server...");
			}
		} else if (MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER.equals(evt.getPropertyName())) {
			int jobID = (Integer) evt.getNewValue();
			if (miapeExtractorBatchManager.isProcessing(jobID)) {
				JProgressBar bar = getProgressBar(jobID);
				if (bar != null)
					bar.setString("Sending dataset info to server...");
			}
		} else if (OntologyLoaderWaiter.ONTOLOGY_LOADED.equals(evt.getPropertyName())) {
			cvManager = (ControlVocabularyManager) evt.getNewValue();
			appendStatus("Ontologies loaded");
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
}