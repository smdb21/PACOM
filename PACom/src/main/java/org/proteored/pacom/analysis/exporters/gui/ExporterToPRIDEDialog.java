/*
 * ExporterToPRIDEDialog.java Created on __DATE__, __TIME__
 */

package org.proteored.pacom.analysis.exporters.gui;

import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.SwingWorker.StateValue;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.pacom.analysis.exporters.tasks.PRIDEExporterTask;
import org.proteored.pacom.gui.ImageManager;
import org.proteored.pacom.gui.MainFrame;

/**
 *
 * @author __USER__
 */
public class ExporterToPRIDEDialog extends javax.swing.JDialog implements PropertyChangeListener {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private final ExperimentList experimentList;
	private int total = 0;
	private int numCreated = 0;

	private PRIDEExporterTask exporterTask;

	public ExporterToPRIDEDialog(Frame parent, ExperimentList experimentList) {
		super(parent, true);
		initComponents();

		this.experimentList = experimentList;
		if (this.experimentList != null) {
			int numExperiments = experimentList.getExperiments().size();
			jProgressBar.setValue(0);
			jProgressBar.setMaximum(numExperiments);
			total = numExperiments;
			// this.jProgressBar.setString(getProgressString());

			String plural = "";
			if (numExperiments > 1)
				plural = "s";
			String text = "<html>This project contains " + numExperiments + " dataset" + plural + " (at level 1).<br>"
					+ numExperiments + " PRIDE XML files will be created";
			if (numExperiments > 1)
				text = text + "(one per each experiment)";
			text = text + ".<br>Select the output folder and click on 'export' button.</html>";
			jLabelInformation.setText(text);
		}
		jTextAreaStatus.setFont(new JTextField().getFont());
		// set icon image
		setIconImage(ImageManager.getImageIcon(ImageManager.PACOM_LOGO).getImage());
		// set button icons
		jButtonExport.setIcon(ImageManager.getImageIcon(ImageManager.PRIDE_LOGO));
		jButtonExport.setPressedIcon(ImageManager.getImageIcon(ImageManager.PRIDE_LOGO_CLICKED));
		jButtonCancel.setIcon(ImageManager.getImageIcon(ImageManager.STOP));
		jButtonCancel.setPressedIcon(ImageManager.getImageIcon(ImageManager.STOP_CLICKED));
		// set title
		setTitle("Export to PRIDE XML");
		pack();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Window#dispose()
	 */
	@Override
	public void dispose() {
		jButtonCancelActionPerformed(null);
		super.dispose();
	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		jFileChooser = new JFileChooser(MainFrame.currentFolder);
		jPanel1 = new javax.swing.JPanel();
		jButtonExport = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		jCheckBoxIncludeSpectra = new javax.swing.JCheckBox();
		jLabel1 = new javax.swing.JLabel();
		jTextFieldFolder = new javax.swing.JTextField();
		jButtonSelectFolder = new javax.swing.JButton();
		jCheckBoxExcludeNotMatchedSpectra = new javax.swing.JCheckBox();
		jCheckBoxCompress = new javax.swing.JCheckBox();
		jCheckBoxExcludeNonConclusiveProteins = new javax.swing.JCheckBox();
		jPanel4 = new javax.swing.JPanel();
		jLabelInformation = new javax.swing.JLabel();
		jPanel3 = new javax.swing.JPanel();
		jScrollPane1 = new javax.swing.JScrollPane();
		jTextAreaStatus = new javax.swing.JTextArea();
		jProgressBar = new javax.swing.JProgressBar();
		jButtonCancel = new javax.swing.JButton();

		jFileChooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
		jFileChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);

		jButtonExport.setText("Export to PRIDE XML");
		jButtonExport.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonExportActionPerformed(evt);
			}
		});

		jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"PRIDE generation options"));

		jCheckBoxIncludeSpectra.setSelected(true);
		jCheckBoxIncludeSpectra.setText("include spectra");
		jCheckBoxIncludeSpectra.setToolTipText(
				"<html>\nIf this option is not selected, spectra will not be added to the generated PRIDE XML file.\n</html>");
		jCheckBoxIncludeSpectra.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxIncludeSpectraActionPerformed(evt);
			}
		});

		jLabel1.setText("Output folder");

		jButtonSelectFolder.setText("select");
		jButtonSelectFolder.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonSelectFolderActionPerformed(evt);
			}
		});

		jCheckBoxExcludeNotMatchedSpectra.setText("exclude no matched spectra");
		jCheckBoxExcludeNotMatchedSpectra.setToolTipText(
				"<html>\nIf this option is selected, spectra that has not matched to any peptide<br>\n(or spectra that has matched to any peptide that has not passed the filter) <br>\nwill be removed from the generated PRIDE XML file.<br>\nThe resulting PRIDE XML file will be considerably smaller and will be<br> suitable for its inspection in tools like the PRIDE Viewer.<br>\n<b>Note: the resulting PRIDE XML will not valid for its submission to <br>\nthe ProteomeXchange consortium or EBI PRIDE repository. Non matched spectra <br> are required for publication purposes.</b><br>\n</html>");

		jCheckBoxCompress.setText("compress resulting file (gzip)");
		jCheckBoxCompress.setToolTipText(
				"<html>\nIf this option is selected, resulting PRIDE XML<br>\nfiles will be compressed at the output folder.\n</html>");

		jCheckBoxExcludeNonConclusiveProteins.setSelected(true);
		jCheckBoxExcludeNonConclusiveProteins.setText("exclude non-conclusive proteins");
		jCheckBoxExcludeNonConclusiveProteins.setToolTipText(
				"<html>Select this option to not include the nonConclusive <br>proteins in the resulting PRIDE XML file.<br>\nIf non-conclusive proteins are included, they will be tagged with a cvParam<br>\nas non-conclusive, but then it will depend on the reader/viewer software to<br>\ninterpret correctly the number of proteins, since non-conclusive proteins<br> should not be counted in the total protein count.<html>");

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(jPanel2Layout.createSequentialGroup().addComponent(jLabel1)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jTextFieldFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 355,
												javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jButtonSelectFolder))
						.addGroup(jPanel2Layout.createSequentialGroup()
								.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(jCheckBoxIncludeSpectra)
										.addComponent(jCheckBoxExcludeNotMatchedSpectra))
								.addGap(141, 141, 141)
								.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(jCheckBoxExcludeNonConclusiveProteins)
										.addComponent(jCheckBoxCompress))))
						.addContainerGap(27, Short.MAX_VALUE)));
		jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup()
						.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jCheckBoxIncludeSpectra).addComponent(jCheckBoxCompress))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jCheckBoxExcludeNotMatchedSpectra)
								.addComponent(jCheckBoxExcludeNonConclusiveProteins))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
						.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel1)
								.addComponent(jTextFieldFolder, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jButtonSelectFolder))
						.addContainerGap()));

		jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
		jPanel4.setLayout(jPanel4Layout);
		jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel4Layout.createSequentialGroup().addContainerGap()
						.addComponent(jLabelInformation, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
						.addContainerGap()));
		jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel4Layout.createSequentialGroup().addContainerGap().addComponent(jLabelInformation,
						javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap()));

		jPanel3.setBorder(
				javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Status"));

		jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane1.setAutoscrolls(true);

		jTextAreaStatus.setColumns(20);
		jTextAreaStatus.setLineWrap(true);
		jTextAreaStatus.setRows(5);
		jTextAreaStatus.setWrapStyleWord(true);
		jScrollPane1.setViewportView(jTextAreaStatus);

		jProgressBar.setString("");
		jProgressBar.setStringPainted(true);

		javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
		jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
						jPanel3Layout.createSequentialGroup().addContainerGap()
								.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
										.addComponent(jProgressBar, javax.swing.GroupLayout.Alignment.LEADING,
												javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
								.addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE))
						.addContainerGap()));
		jPanel3Layout
				.setVerticalGroup(
						jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(jPanel3Layout.createSequentialGroup()
										.addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 76,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap()));

		jButtonCancel.setText("Cancel");
		jButtonCancel.setEnabled(false);
		jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonCancelActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
						jPanel1Layout.createSequentialGroup().addContainerGap()
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
										.addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(javax.swing.GroupLayout.Alignment.LEADING,
										jPanel1Layout.createSequentialGroup().addComponent(jButtonExport)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
														352, Short.MAX_VALUE)
												.addComponent(jButtonCancel))
										.addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE))
								.addContainerGap()));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup().addContainerGap()
						.addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jButtonExport).addComponent(jButtonCancel))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
				jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 602) / 2, (screenSize.height - 403) / 2, 602, 403);
	}// </editor-fold>
		// GEN-END:initComponents

	private void jCheckBoxIncludeSpectraActionPerformed(java.awt.event.ActionEvent evt) {
		jCheckBoxExcludeNotMatchedSpectra.setEnabled(jCheckBoxIncludeSpectra.isSelected());
	}

	private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {
		if (exporterTask != null && exporterTask.getState() == StateValue.STARTED) {
			boolean canceled = exporterTask.cancel(true);
			while (!canceled) {
				canceled = exporterTask.cancel(true);
				if (!canceled)
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}

			}
		}
		jButtonCancel.setEnabled(false);
		jProgressBar.setValue(0);
		jProgressBar.setString("");
	}

	private void jButtonSelectFolderActionPerformed(java.awt.event.ActionEvent evt) {
		final int showDialog = jFileChooser.showDialog(this, "Select folder");
		if (showDialog == JFileChooser.APPROVE_OPTION) {
			final File selectedFile = jFileChooser.getSelectedFile();
			if (selectedFile != null)
				jTextFieldFolder.setText(selectedFile.getAbsolutePath());
		}
	}

	private void jButtonExportActionPerformed(java.awt.event.ActionEvent evt) {
		exportToPRIDE();
	}

	private void exportToPRIDE() {

		try {
			File outputFolder = getOutputFolder();
			boolean addPeakList = jCheckBoxIncludeSpectra.isSelected();
			boolean excludeNonMatchedSpectra = jCheckBoxExcludeNotMatchedSpectra.isSelected();
			boolean compressResultingFiles = jCheckBoxCompress.isSelected();
			boolean excludeNonConclusiveProteins = jCheckBoxExcludeNonConclusiveProteins.isSelected();
			log.info("Output folder=" + outputFolder.getAbsolutePath());
			log.info("ADD peak list=" + addPeakList);

			jButtonCancel.setEnabled(true);
			if (exporterTask == null || !exporterTask.getState().equals(StateValue.STARTED)) {
				exporterTask = new PRIDEExporterTask(experimentList, outputFolder, addPeakList,
						excludeNonMatchedSpectra, compressResultingFiles, excludeNonConclusiveProteins);
				exporterTask.addPropertyChangeListener(this);
				exporterTask.execute();
			} else {
				appendStatus("Exporting data to PRIDE XML.");
				appendStatus("Click on cancel before to start another exporting process.");
			}

		} catch (IllegalMiapeArgumentException e) {
			jTextAreaStatus.setText(e.getMessage());
		}

	}

	private File getOutputFolder() {
		File output = jFileChooser.getSelectedFile();
		if (output != null && output.isDirectory()) {
			MainFrame.currentFolder = output;
			return output;
		}
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
				ExporterToPRIDEDialog dialog = new ExporterToPRIDEDialog(new javax.swing.JFrame(), null);
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
	private javax.swing.JButton jButtonCancel;
	private javax.swing.JButton jButtonExport;
	private javax.swing.JButton jButtonSelectFolder;
	private javax.swing.JCheckBox jCheckBoxCompress;
	private javax.swing.JCheckBox jCheckBoxExcludeNonConclusiveProteins;
	private javax.swing.JCheckBox jCheckBoxExcludeNotMatchedSpectra;
	private javax.swing.JCheckBox jCheckBoxIncludeSpectra;
	private javax.swing.JFileChooser jFileChooser;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabelInformation;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JProgressBar jProgressBar;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JTextArea jTextAreaStatus;
	private javax.swing.JTextField jTextFieldFolder;

	// End of variables declaration//GEN-END:variables

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(PRIDEExporterTask.SINGLE_PRIDE_EXPORTING_STARTED)) {
			jProgressBar.setIndeterminate(true);
			jButtonExport.setEnabled(false);
			String experimentName = (String) evt.getNewValue();
			if (experimentName != null) {
				appendStatus("Creating PRIDE XML file for experiment: " + experimentName + "...");
				jProgressBar.setString(getProgressString());
			}
		} else if (evt.getPropertyName().equals(PRIDEExporterTask.SINGLE_PRIDE_EXPORTED)) {
			File generatedFile = (File) evt.getNewValue();
			if (generatedFile != null && generatedFile.exists()) {
				appendStatus("PRIDE XML file created at: " + generatedFile.getAbsolutePath() + " ("
						+ getFileSizeString(generatedFile) + ")");
				numCreated++;
				jProgressBar.setString(getProgressString());

			}

		} else if (evt.getPropertyName().equals(PRIDEExporterTask.SINGLE_PRIDE_COMPRESSING_STARTED)) {
			File compressingFile = (File) evt.getNewValue();
			if (compressingFile != null && compressingFile.exists()) {
				appendStatus("Compressing PRIDE XML file: " + FilenameUtils.getName(compressingFile.getAbsolutePath())
						+ " (" + getFileSizeString(compressingFile) + ")");
				numCreated++;
				jProgressBar.setString(getProgressString());

			}
		} else if (evt.getPropertyName().equals(PRIDEExporterTask.SINGLE_PRIDE_COMPRESSING_FINISHED)) {
			File compressedFile = (File) evt.getNewValue();
			if (compressedFile != null && compressedFile.exists()) {
				appendStatus("PRIDE XML file compressed: " + FilenameUtils.getName(compressedFile.getAbsolutePath())
						+ " (" + getFileSizeString(compressedFile) + ")");
				numCreated++;
				jProgressBar.setString(getProgressString());

			}
		} else if (evt.getPropertyName().equals(PRIDEExporterTask.SINGLE_PRIDE_ALREADY_PRESENT)) {
			File generatedFile = (File) evt.getNewValue();
			if (generatedFile != null && generatedFile.exists()) {
				appendStatus(
						"PRIDE XML file already exists at: " + FilenameUtils.getName(generatedFile.getAbsolutePath())
								+ " (" + getFileSizeString(generatedFile) + ")");
				jProgressBar.setString(getProgressString());

			}
		} else if (evt.getPropertyName().equals(PRIDEExporterTask.PRIDE_EXPORTER_DONE)) {
			List<File> generatedFiles = (List<File>) evt.getNewValue();
			jProgressBar.setIndeterminate(false);
			jButtonExport.setEnabled(true);
			jButtonCancel.setEnabled(false);

			if (generatedFiles != null && !generatedFiles.isEmpty()) {
				String plural = "";
				if (generatedFiles.size() > 1)
					plural = "s";
				String message = generatedFiles.size() + " PRIDE XML file" + plural + " located at '"
						+ FilenameUtils.getFullPath(generatedFiles.iterator().next().getAbsolutePath()) + "':\n";
				int num = 1;
				for (File file : generatedFiles) {
					message = message + num++ + ": '" + file.getName() + " (" + getFileSizeString(file) + ")'\n";

				}

				appendStatus(message);
				jProgressBar.setString(numCreated + " files created");

			}
		} else if (evt.getPropertyName().equals(PRIDEExporterTask.PRIDE_EXPORTER_ERROR)) {
			jProgressBar.setIndeterminate(false);
			jButtonExport.setEnabled(true);
			jButtonCancel.setEnabled(false);
			String errorMessage = (String) evt.getNewValue();
			setStatus(errorMessage);

		} else if (evt.getPropertyName().equals(PRIDEExporterTask.SINGLE_PRIDE_EXPORTED_ERROR)) {

			String errorMessage = (String) evt.getNewValue();
			appendStatus(errorMessage);

		}

	}

	private String getFileSizeString(File file) {
		if (file != null) {
			long bytes = file.length() / 1024;
			long mBytes = bytes / 1024;
			if (mBytes == 0)
				return bytes + "bytes";
			long gBytes = mBytes / 1024;
			if (gBytes == 0)
				return mBytes + "Mb";
			return gBytes + "Gb";
		}
		return "";
	}

	private String getProgressString() {
		final List<Experiment> experiments = experimentList.getExperiments();
		if (numCreated < experiments.size()) {
			Experiment currentExperiment = experiments.get(numCreated);
			int num = numCreated + 1;
			return "Creating (" + num + "/" + total + ") 'PRIDE for '" + currentExperiment.getName() + "' ...";
		}
		return "";
	}

	private void appendStatus(String notificacion) {
		log.info("Appending status to: " + notificacion);
		jTextAreaStatus.append(notificacion + "\n");
		jTextAreaStatus.setCaretPosition(jTextAreaStatus.getText().length() - 1);

	}

	private void setStatus(String notificacion) {
		log.info("Setting status to: " + notificacion);
		jTextAreaStatus.setText(notificacion + "\n");
		jTextAreaStatus.setCaretPosition(jTextAreaStatus.getText().length() - 1);

	}

}