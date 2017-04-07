/*
 * ManualIdentificationSetCreatorDialog.java Created on __DATE__, __TIME__
 */

package org.proteored.pacom.analysis.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.io.FilenameUtils;
import org.jfree.ui.RefineryUtilities;
import org.proteored.miapeapi.factories.msi.MiapeMSIDocumentBuilder;
import org.proteored.miapeapi.interfaces.msi.IdentifiedProteinSet;
import org.proteored.miapeapi.interfaces.msi.MiapeMSIDocument;
import org.proteored.pacom.analysis.gui.tasks.IdentificationSetFromDTASelectFileTask;
import org.proteored.pacom.analysis.gui.tasks.IdentificationSetFromFileParserTask;
import org.proteored.pacom.analysis.util.FileManager;
import org.proteored.pacom.gui.ImageManager;
import org.proteored.pacom.gui.MainFrame;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

/**
 *
 * @author __USER__
 */
public class ManualIdentificationSetCreatorDialog extends javax.swing.JDialog implements PropertyChangeListener {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("log4j.logger.org.proteored");
	private static ManualIdentificationSetCreatorDialog instance;
	private IdentificationSetFromFileParserTask identificationSetFromFileParser;
	private IdentificationSetFromDTASelectFileTask identificationSetFromDTASelect;
	private MiapeMSIDocumentBuilder miapeMSIBuilder;
	private final Miape2ExperimentListDialog parent;
	private JFileChooser fileChooser;
	private boolean saved;
	private File selectedFile;

	/** Creates new form ManualIdentificationSetCreatorDialog */
	private ManualIdentificationSetCreatorDialog(Miape2ExperimentListDialog parent) {
		super(parent, true);
		this.parent = parent;
		initComponents();

		try {
			UIManager.setLookAndFeel(new WindowsLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
		}
		loadIcons();
		jTextAreaStatus.setFont(new JTextField().getFont());
		RefineryUtilities.centerFrameOnScreen(this);
		pack();
	}

	public static ManualIdentificationSetCreatorDialog getInstance(Miape2ExperimentListDialog parent) {
		if (instance == null) {
			instance = new ManualIdentificationSetCreatorDialog(parent);
		}
		RefineryUtilities.centerFrameOnScreen(instance);
		return instance;
	}

	private void loadIcons() {
		// set icon image
		setIconImage(ImageManager.getImageIcon(ImageManager.PROTEORED_MIAPE_API).getImage());
		jButtonClose.setIcon(ImageManager.getImageIcon(ImageManager.FINISH));
		jButtonClose.setPressedIcon(ImageManager.getImageIcon(ImageManager.FINISH_CLICKED));

		jButtonImport.setIcon(ImageManager.getImageIcon(ImageManager.SAVE));
		jButtonImport.setPressedIcon(ImageManager.getImageIcon(ImageManager.SAVE_CLICKED));

	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		buttonGroup1 = new javax.swing.ButtonGroup();
		jLabelInfo = new javax.swing.JLabel();
		jLabelInfo2 = new javax.swing.JLabel();
		jLabelInfo3 = new javax.swing.JLabel();
		jLabelInfo4 = new javax.swing.JLabel();
		jLabelInfo5 = new javax.swing.JLabel();
		jPanel1 = new javax.swing.JPanel();
		jTextFieldFilePath = new javax.swing.JTextField();
		jButtonSelectFile = new javax.swing.JButton();
		jPanel3 = new javax.swing.JPanel();
		jLabelParsingResult = new javax.swing.JLabel();
		jButtonClose = new javax.swing.JButton();
		jButtonImport = new javax.swing.JButton();
		jPanel4 = new javax.swing.JPanel();
		jScrollPane1 = new javax.swing.JScrollPane();
		jTextAreaStatus = new javax.swing.JTextArea();
		jPanel5 = new javax.swing.JPanel();
		jTextFieldName = new javax.swing.JTextField();
		jLabelInfo6 = new javax.swing.JLabel();

		setTitle("Identification Set Creator");
		setResizable(false);

		jLabelInfo.setText("Here, you can create your own protein/peptide identification set.");

		jLabelInfo2.setText("You can use DTASelect output file or a tabular separated values (TSV) file with:");

		jLabelInfo3.setText("The first column containing protein accessions.");

		jLabelInfo4.setText("The second column containing peptide sequences.");

		jLabelInfo5.setText("An optional third column containing PSM score numbers.");

		jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"Input file"));

		jButtonSelectFile.setText("Select file");
		jButtonSelectFile.setToolTipText("Click for select the file");
		jButtonSelectFile.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jButtonSelectFileMouseClicked(evt);
			}
		});

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addComponent(jButtonSelectFile)
						.addGap(18, 18, 18)
						.addComponent(jTextFieldFilePath, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
						.addContainerGap()));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup().addGap(8, 8, 8)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jButtonSelectFile).addComponent(jTextFieldFilePath,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addContainerGap()));

		jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"Parsing result"));

		jLabelParsingResult.setText("No file is selected");

		jButtonClose.setText("Close");
		jButtonClose.setToolTipText("Save Identification Set and close dialog");
		jButtonClose.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jButtonCloseMouseClicked(evt);
			}
		});

		jButtonImport.setText("Import data");
		jButtonImport.setToolTipText("Save Identification Set and close dialog");
		jButtonImport.setEnabled(false);
		jButtonImport.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jButtonImportMouseClicked(evt);
			}
		});

		javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
		jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(jLabelParsingResult, javax.swing.GroupLayout.PREFERRED_SIZE, 231,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 133, Short.MAX_VALUE)
						.addComponent(jButtonImport).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jButtonClose).addContainerGap()));
		jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel3Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jButtonClose).addComponent(jLabelParsingResult)
								.addComponent(jButtonImport))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jPanel4.setBorder(
				javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Status"));

		jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane1.setAutoscrolls(true);

		jTextAreaStatus.setColumns(20);
		jTextAreaStatus.setLineWrap(true);
		jTextAreaStatus.setRows(5);
		jTextAreaStatus.setWrapStyleWord(true);
		jScrollPane1.setViewportView(jTextAreaStatus);

		javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
		jPanel4.setLayout(jPanel4Layout);
		jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel4Layout.createSequentialGroup().addContainerGap()
						.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
						.addContainerGap()));
		jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel4Layout.createSequentialGroup()
						.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
						.addContainerGap()));

		jPanel5.setBorder(
				javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Name"));

		jTextFieldName.setText("My_identification_set");
		jTextFieldName.setToolTipText("Name of the identification set");

		javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
		jPanel5.setLayout(jPanel5Layout);
		jPanel5Layout.setHorizontalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel5Layout.createSequentialGroup().addContainerGap()
						.addComponent(jTextFieldName, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
						.addContainerGap()));
		jPanel5Layout.setVerticalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel5Layout.createSequentialGroup().addContainerGap()
						.addComponent(jTextFieldName, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(30, Short.MAX_VALUE)));

		jLabelInfo6.setText("Further columns will be ignored.");

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout
				.createSequentialGroup()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup().addContainerGap()
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(jLabelInfo).addComponent(jLabelInfo2)))
						.addGroup(layout.createSequentialGroup().addGap(34, 34, 34)
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(jLabelInfo4).addComponent(jLabelInfo3).addComponent(jLabelInfo5)
										.addComponent(jLabelInfo6)))
						.addGroup(layout.createSequentialGroup().addContainerGap()
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
										.addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE))))
				.addContainerGap(12, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jLabelInfo)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabelInfo2)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabelInfo3)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabelInfo4)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabelInfo5)
						.addGap(4, 4, 4).addComponent(jLabelInfo6)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
						javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
						javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
						javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap()));

		pack();
	}// </editor-fold>
		// GEN-END:initComponents

	private void jButtonCloseMouseClicked(java.awt.event.MouseEvent evt) {
		if (!saved) {
			int selectedOption = JOptionPane.showConfirmDialog(this,
					"Parsed data has not been imported. Are you sure you want to close before to import it?",
					"Parsed data not imported", JOptionPane.YES_NO_OPTION);
			if (JOptionPane.YES_OPTION == selectedOption)
				dispose();
		} else {
			dispose();
		}
	}

	private void jButtonImportMouseClicked(java.awt.event.MouseEvent evt) {
		importData();
	}

	private void importData() {
		appendStatus("Importing information from file...");
		// save on the fileManager
		if (miapeMSIBuilder != null) {
			try {
				String name = jTextFieldName.getText();
				MiapeMSIDocument miapeMSI = miapeMSIBuilder.name(name).build();
				boolean override = true;
				final File manualIdSetFile = FileManager.getManualIdSetFile(name);
				if (manualIdSetFile != null && manualIdSetFile.exists()) {
					int selectedOption = JOptionPane.showConfirmDialog(this,
							"<html>There is already an identification set with the name '" + name + "'<br>"
									+ "It is located at: '" + manualIdSetFile.getAbsolutePath() + "'<br>"
									+ "Are you sure you want to override the data?",
							"Identification set overriding", JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.WARNING_MESSAGE);
					if (selectedOption == JOptionPane.CANCEL_OPTION || selectedOption == JOptionPane.NO_OPTION)
						override = false;
				}
				if (override) {
					final String saved = FileManager.saveManualIdSetMiapeMSI(name, miapeMSI);
					this.saved = true;
					parent.addReloadLocalIdentificationSetsTree();
					appendStatus("Data imported successfully. File created at: '" + saved + "'");
				} else {
					appendStatus("Import canceled.");
				}
			} catch (IOException e) {
				e.printStackTrace();
				appendStatus("Error saving identification set on " + FileManager.getManualIdSetPath());
				appendStatus(e.getMessage());
				return;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				appendStatus("Error saving identification set on " + FileManager.getManualIdSetPath());
				appendStatus(e.getMessage());
				return;
			}
		}

	}

	private void jButtonSelectFileMouseClicked(java.awt.event.MouseEvent evt) {
		selectFile();
	}

	private void selectFile() {

		fileChooser = new JFileChooser(MainFrame.currentFolder);
		fileChooser.showDialog(this, "Select");
		selectedFile = fileChooser.getSelectedFile();
		if (selectedFile != null) {
			MainFrame.currentFolder = selectedFile.getParentFile();
			// set the name on the text field of the name
			final String name = FilenameUtils.getName(selectedFile.getAbsolutePath());
			if (name.contains(".")) {
				String[] split = name.split("\\.");
				String name2 = "";
				for (int i = 0; i < split.length - 1; i++) {
					name2 = name2 + split[i];
				}
				jTextFieldName.setText(name2);
			} else {
				jTextFieldName.setText(name);
			}
			jTextFieldFilePath.setText(selectedFile.getAbsolutePath());

			String idSetName = jTextFieldName.getText();
			if ("".equals(idSetName)) {
				appendStatus("Please, write a valid name and select again the file");
				return;
			}
			// try to parse as a DTASelect file first
			// if fails, then the regular parser will be executed
			try {
				identificationSetFromDTASelect = new IdentificationSetFromDTASelectFileTask(selectedFile, idSetName);
				identificationSetFromDTASelect.addPropertyChangeListener(this);
				identificationSetFromDTASelect.execute();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private String getSeparator() {

		return "\t";

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		if (IdentificationSetFromDTASelectFileTask.DTASELECT_PARSER_STARTS.equals(evt.getPropertyName())) {
			appendStatus("Parsing file as DTASelect-filter file...");
			jButtonImport.setEnabled(false);
			jButtonClose.setEnabled(false);
			jButtonSelectFile.setEnabled(false);
			jTextFieldFilePath.setEnabled(false);
			jTextFieldName.setEnabled(false);
		} else if (IdentificationSetFromDTASelectFileTask.DTASELECT_PARSER_ERROR.equals(evt.getPropertyName())) {
			appendStatus("Error parsing file as DTASelect file: " + evt.getNewValue());
			jButtonSelectFile.setEnabled(true);
			jTextFieldFilePath.setEnabled(true);
			jTextFieldName.setEnabled(true);
			jButtonClose.setEnabled(true);
			jButtonImport.setEnabled(false);
			appendStatus("Trying now as a regular TXT file...");
			String separator = getSeparator();
			String idSetName = jTextFieldName.getText();
			identificationSetFromFileParser = new IdentificationSetFromFileParserTask(selectedFile, idSetName,
					separator);
			identificationSetFromFileParser.addPropertyChangeListener(this);
			identificationSetFromFileParser.execute();

		} else if (IdentificationSetFromDTASelectFileTask.DTASELECT_PARSER_FINISHED.equals(evt.getPropertyName())) {
			log.info("MIAPE MSI created");
			saved = false;
			appendStatus("File parsed successfully.");
			appendStatus("Now click on Import Data button in order to actually import the data.");
			miapeMSIBuilder = (MiapeMSIDocumentBuilder) evt.getNewValue();
			int numProteins = getNumProteins(miapeMSIBuilder);
			int numPeptides = getNumPeptides(miapeMSIBuilder);
			jLabelParsingResult.setText(numProteins + " proteins and " + numPeptides + " peptides");
			jButtonClose.setEnabled(true);
			jButtonImport.setEnabled(true);
			jButtonSelectFile.setEnabled(true);
			jTextFieldFilePath.setEnabled(true);
			jTextFieldName.setEnabled(true);
		} else if (IdentificationSetFromFileParserTask.PARSER_STARTS.equals(evt.getPropertyName())) {
			appendStatus("Parsing file...");
			jButtonImport.setEnabled(false);
			jButtonClose.setEnabled(false);
			jButtonSelectFile.setEnabled(false);
			jTextFieldFilePath.setEnabled(false);
			jTextFieldName.setEnabled(false);
		} else if (IdentificationSetFromFileParserTask.PARSER_ERROR.equals(evt.getPropertyName())) {
			appendStatus("Error parsing file: " + evt.getNewValue());
			jButtonSelectFile.setEnabled(true);
			jTextFieldFilePath.setEnabled(true);
			jTextFieldName.setEnabled(true);
			jButtonClose.setEnabled(true);
			jButtonImport.setEnabled(false);
		} else if (IdentificationSetFromFileParserTask.PARSER_FINISHED.equals(evt.getPropertyName())) {
			log.info("MIAPE MSI created");
			saved = false;
			appendStatus("File parsed successfully.");
			miapeMSIBuilder = (MiapeMSIDocumentBuilder) evt.getNewValue();
			int numProteins = getNumProteins(miapeMSIBuilder);
			int numPeptides = getNumPeptides(miapeMSIBuilder);
			jLabelParsingResult.setText(numProteins + " proteins and " + numPeptides + " peptides");
			jButtonClose.setEnabled(true);
			jButtonImport.setEnabled(true);
			jButtonSelectFile.setEnabled(true);
			jTextFieldFilePath.setEnabled(true);
			jTextFieldName.setEnabled(true);
		}

	}

	private int getNumProteins(MiapeMSIDocumentBuilder miapeMSIBuilder) {
		int num = 0;
		if (miapeMSIBuilder != null) {
			MiapeMSIDocument miapeMSI = miapeMSIBuilder.build();
			if (miapeMSI.getIdentifiedProteinSets() != null)
				for (IdentifiedProteinSet proteinSet : miapeMSI.getIdentifiedProteinSets()) {
					if (proteinSet.getIdentifiedProteins() != null) {
						num += proteinSet.getIdentifiedProteins().size();
					}
				}
		}

		return num;
	}

	private int getNumPeptides(MiapeMSIDocumentBuilder miapeMSIBuilder) {
		int num = 0;
		if (miapeMSIBuilder != null) {
			MiapeMSIDocument miapeMSI = miapeMSIBuilder.build();
			if (miapeMSI.getIdentifiedPeptides() != null)

				num += miapeMSI.getIdentifiedPeptides().size();

		}
		return num;

	}

	public void appendStatus(String notificacion) {
		jTextAreaStatus.append(notificacion + "\n");
		jTextAreaStatus.setCaretPosition(jTextAreaStatus.getText().length() - 1);

	}

	private void setStatus(String notificacion) {
		jTextAreaStatus.setText(notificacion + "\n");
		jTextAreaStatus.setCaretPosition(jTextAreaStatus.getText().length() - 1);

	}

	// GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.ButtonGroup buttonGroup1;
	private javax.swing.JButton jButtonClose;
	private javax.swing.JButton jButtonImport;
	private javax.swing.JButton jButtonSelectFile;
	private javax.swing.JLabel jLabelInfo;
	private javax.swing.JLabel jLabelInfo2;
	private javax.swing.JLabel jLabelInfo3;
	private javax.swing.JLabel jLabelInfo4;
	private javax.swing.JLabel jLabelInfo5;
	private javax.swing.JLabel jLabelInfo6;
	private javax.swing.JLabel jLabelParsingResult;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JTextArea jTextAreaStatus;
	private javax.swing.JTextField jTextFieldFilePath;
	private javax.swing.JTextField jTextFieldName;
	// End of variables declaration//GEN-END:variables

}