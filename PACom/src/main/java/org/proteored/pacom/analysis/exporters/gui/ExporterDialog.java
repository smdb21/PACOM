/*
 * ExporterDialog.java Created on __DATE__, __TIME__
 */

package org.proteored.pacom.analysis.exporters.gui;

import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.proteored.miapeapi.experiment.model.IdentificationSet;
import org.proteored.miapeapi.experiment.model.filters.Filters;
import org.proteored.miapeapi.experiment.model.sort.ProteinGroupComparisonType;
import org.proteored.pacom.analysis.exporters.ExporterManager;
import org.proteored.pacom.analysis.exporters.tasks.JTableLoader;
import org.proteored.pacom.analysis.exporters.tasks.TSVExporter;
import org.proteored.pacom.analysis.exporters.util.ExporterUtil;
import org.proteored.pacom.analysis.gui.ChartManagerFrame;
import org.proteored.pacom.analysis.gui.TsvFileFilter;
import org.proteored.pacom.analysis.util.DataLevel;
import org.proteored.pacom.gui.ImageManager;
import org.proteored.pacom.gui.MainFrame;

import gnu.trove.set.hash.THashSet;

/**
 *
 * @author __USER__
 */
public class ExporterDialog extends javax.swing.JDialog implements PropertyChangeListener, ExporterManager {
	private static final String DATA_LEVEL_TOOLTIP = "<html>When exporting proteins, this option will determine which level of integration you want to export.<br>"
			+ "Remember that in each level, the proteins are reorganized (PAnalyzer protein grouping algorithm is applied),<br>"
			+ "and so the protein groups will likely not be the same in different levels of data integration</html>";
	private final Set<IdentificationSet> idSets = new THashSet<IdentificationSet>();
	private TSVExporter exporter;
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");
	private boolean previousCollapsePeptides;
	private boolean previousCollapseProteins;
	private boolean previousIncludeDecoy;
	private boolean previousIncludeGene;
	private boolean previousSearchForProteinSequence;
	private final boolean isFDRApplied;
	private Filters filter;
	private Boolean distinguisModifiedPeptides;
	private final ChartManagerFrame chartManagerFrame;
	private JLabel lblLevelToExport;
	private DataLevel dataLevel;

	public ExporterDialog(Frame parentFrame, ChartManagerFrame parent, Collection<IdentificationSet> idSets,
			DataLevel dataLevel) {
		this(parentFrame, parent, idSets, dataLevel, false);
	}

	public ExporterDialog(Frame parentFrame, ChartManagerFrame parent, Collection<IdentificationSet> idSets,
			DataLevel dataLevel, boolean exportingSubSetsFromOverlappings) {
		super(parentFrame, true);
		this.chartManagerFrame = parent;
		this.distinguisModifiedPeptides = parent.distinguishModifiedPeptides();
		this.idSets.addAll(idSets);
		this.dataLevel = dataLevel;
		initComponents();

		if (!this.idSets.isEmpty() && parent.getFiltersDialog() != null
				&& parent.getFiltersDialog().isFDRFilterDefined()) {
			jCheckBoxIncludeDecoy.setEnabled(true);
			isFDRApplied = true;
		} else {
			isFDRApplied = false;
		}
		if (exportingSubSetsFromOverlappings
				&& parent.getCurrentChartType().equals(ChartManagerFrame.PROTEIN_OVERLAPING)) {
			// disable export peptides
			this.jCheckBoxCollapsePeptides.setEnabled(false);
			this.jRadioButtonExportPeptides.setEnabled(false);
			this.jRadioButtonExportProteins.setSelected(true);
			this.jCheckBoxCollapseProteins.setEnabled(true);
		}
		if (exportingSubSetsFromOverlappings
				&& parent.getCurrentChartType().equals(ChartManagerFrame.PEPTIDE_OVERLAPING)) {
			// disable export proteins
			this.jCheckBoxCollapseProteins.setEnabled(false);
			this.jRadioButtonExportProteins.setEnabled(false);
			this.jRadioButtonExportPeptides.setSelected(true);
			this.jCheckBoxCollapsePeptides.setEnabled(true);

		}
		// Just enable if the protein sequences have not been retrieved before
		jCheckBoxSearchForProteinSequence.setEnabled(!parent.isProteinSequencesRetrieved());

		// if (parent != null && parent.isChr16ChartShowed())
		// this.jCheckBoxIncludeGeneInfo.setEnabled(true);

		// set icon image
		setIconImage(ImageManager.getImageIcon(ImageManager.PACOM_LOGO).getImage());

		jButtonCancel.setIcon(ImageManager.getImageIcon(ImageManager.STOP));
		jButtonExport.setIcon(ImageManager.getImageIcon(ImageManager.EXCEL_TABLE));
		pack();

		if (dataLevel != null) {
			dataLevelComboBox.setSelectedItem(dataLevel);
			dataLevelComboBox.setEnabled(false);
		}
	}

	public void setFilter(Filters filter) {
		this.filter = filter;

	}

	private String getNumString(Integer numPeptides, Integer numProteins) {
		String ret = "";
		if (numProteins != null)
			ret = "Exporting " + numProteins + " proteins";
		if (numPeptides != null)
			if ("".equals(ret))
				ret = "Exporting " + numPeptides + " peptides";
			else
				ret = ret + " and " + numPeptides + " peptides";
		return ret;
	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		buttonGroup1 = new javax.swing.ButtonGroup();
		jFileChooser1 = new JFileChooser(MainFrame.currentFolder);
		jPanel1 = new javax.swing.JPanel();
		jTextFieldFilePath = new javax.swing.JTextField();
		jButtonSelect = new javax.swing.JButton();
		jPanelOptions = new javax.swing.JPanel();
		jCheckBoxIncludeDecoy = new javax.swing.JCheckBox();
		jCheckBoxSearchForProteinSequence = new javax.swing.JCheckBox();
		jCheckBoxIncludeGeneInfo = new javax.swing.JCheckBox();
		jCheckBoxCollapsePeptides = new javax.swing.JCheckBox();
		jCheckBoxCollapseProteins = new javax.swing.JCheckBox();
		jRadioButtonExportPeptides = new javax.swing.JRadioButton();
		jRadioButtonExportProteins = new javax.swing.JRadioButton();
		jPanel2 = new javax.swing.JPanel();
		jButtonExport = new javax.swing.JButton();
		jProgressBar1 = new javax.swing.JProgressBar();
		jButtonCancel = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Export Identification Data");
		setResizable(false);

		jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"Output file"));

		jButtonSelect.setText("Select");
		jButtonSelect.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonSelectActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(jTextFieldFilePath, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jButtonSelect)
						.addContainerGap()));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup()
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jButtonSelect).addComponent(jTextFieldFilePath,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jPanelOptions.setBorder(javax.swing.BorderFactory
				.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Options"));

		jCheckBoxIncludeDecoy.setText("include DECOY hits");
		jCheckBoxIncludeDecoy.setToolTipText(
				"<html>If this options is activated, decoy peptides/proteins <br>will also be included in the exported file.<br>\nThis options only is available if a FDR filter is applied.</html>");
		jCheckBoxIncludeDecoy.setEnabled(false);

		jCheckBoxSearchForProteinSequence.setText("calculate protein coverages");
		jCheckBoxSearchForProteinSequence.setToolTipText(
				"<html>\nThis options will retrieve protein sequences from the Internet in order to\n<br>\ncalculate the protein coverage. Depending on the number of proteins you\n<br>have, it can take several minutes.\n</html>");

		jCheckBoxIncludeGeneInfo.setText("include gene information");
		jCheckBoxIncludeGeneInfo.setToolTipText(
				"<html>\nThis option will retrieve genes associated with the proteins from the UniprotKB.\nDepending on the number of proteins you\n<br>have, it can take several minutes.</html>");

		jCheckBoxCollapsePeptides.setSelected(true);
		jCheckBoxCollapsePeptides.setText("hide redundant peptides");
		jCheckBoxCollapsePeptides.setToolTipText(
				"<html>\nIf this option is activated, if the same peptide has been detected\n<br>\nmore than once, it will appear only once in the exported table and\n<br>\nthe score will be the best score of all occurrences.</html>");
		jCheckBoxCollapsePeptides.setEnabled(false);

		jCheckBoxCollapseProteins.setSelected(true);
		jCheckBoxCollapseProteins.setText("hide redundant proteins");
		jCheckBoxCollapseProteins.setToolTipText(
				"<html>\nIf this option is activated, if the same protein has been detected\n<br>\nmore than once, it will appear only once in the exported table and\n<br>\nthe score will be the best score of all occurrences.</html>");

		buttonGroup1.add(jRadioButtonExportPeptides);
		jRadioButtonExportPeptides.setText("export peptides");
		jRadioButtonExportPeptides.setToolTipText(
				"<html>Peptide information will be together with its assigned protein\n<br>\naccession, but no more protein information will be exported.</html>");
		jRadioButtonExportPeptides.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jRadioButtonExportPeptidesActionPerformed(evt);
			}
		});

		buttonGroup1.add(jRadioButtonExportProteins);
		jRadioButtonExportProteins.setSelected(true);
		jRadioButtonExportProteins.setText("export proteins");
		jRadioButtonExportProteins.setToolTipText("Only protein information will be exported.");
		jRadioButtonExportProteins.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jRadioButtonExportProteinsActionPerformed(evt);
			}
		});

		lblLevelToExport = new JLabel("Level of integration:");
		lblLevelToExport.setToolTipText(DATA_LEVEL_TOOLTIP);
		dataLevelComboBox = new JComboBox<DataLevel>();
		dataLevelComboBox.setToolTipText(DATA_LEVEL_TOOLTIP);
		// data levels
		for (DataLevel dataLevel : DataLevel.values()) {
			dataLevelComboBox.addItem(dataLevel);
		}

		javax.swing.GroupLayout jPanelOptionsLayout = new javax.swing.GroupLayout(jPanelOptions);
		jPanelOptionsLayout.setHorizontalGroup(jPanelOptionsLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanelOptionsLayout.createSequentialGroup().addContainerGap().addGroup(jPanelOptionsLayout
						.createParallelGroup(Alignment.LEADING)
						.addGroup(jPanelOptionsLayout.createSequentialGroup().addGap(10).addComponent(dataLevelComboBox,
								GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(jPanelOptionsLayout.createSequentialGroup()
								.addGroup(jPanelOptionsLayout.createParallelGroup(Alignment.LEADING)
										.addComponent(jCheckBoxIncludeDecoy).addComponent(jCheckBoxIncludeGeneInfo)
										.addComponent(jCheckBoxSearchForProteinSequence).addComponent(lblLevelToExport))
								.addGap(53)
								.addGroup(jPanelOptionsLayout.createParallelGroup(Alignment.LEADING)
										.addComponent(jRadioButtonExportPeptides)
										.addComponent(jRadioButtonExportProteins)
										.addComponent(jCheckBoxCollapseProteins, GroupLayout.PREFERRED_SIZE, 198,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(jCheckBoxCollapsePeptides))))
						.addGap(28)));
		jPanelOptionsLayout.setVerticalGroup(jPanelOptionsLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanelOptionsLayout.createSequentialGroup()
						.addGroup(jPanelOptionsLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(jCheckBoxIncludeDecoy).addComponent(jRadioButtonExportProteins))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(jPanelOptionsLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(jRadioButtonExportPeptides).addComponent(jCheckBoxIncludeGeneInfo))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(jPanelOptionsLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(jCheckBoxCollapseProteins).addComponent(
										jCheckBoxSearchForProteinSequence))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(jPanelOptionsLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(jCheckBoxCollapsePeptides).addComponent(lblLevelToExport))
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(dataLevelComboBox,
								GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(27)));
		jPanelOptions.setLayout(jPanelOptionsLayout);

		jButtonExport.setIcon(new javax.swing.ImageIcon(ImageManager.EXCEL_TABLE)); // NOI18N
		jButtonExport.setPressedIcon(new ImageIcon(ImageManager.EXCEL_TABLE_CLICKED));
		jButtonExport.setText("Export");
		jButtonExport.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonExportActionPerformed(evt);
			}
		});

		jProgressBar1.setStringPainted(true);

		jButtonCancel.setIcon(new ImageIcon(ImageManager.STOP)); // NOI18N
		jButtonCancel.setPressedIcon(new ImageIcon(ImageManager.STOP_CLICKED));
		jButtonCancel.setText("Cancel");
		jButtonCancel.setEnabled(false);
		jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonCancelActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(jPanel2Layout.createSequentialGroup().addComponent(jButtonExport)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 228,
												Short.MAX_VALUE)
										.addComponent(jButtonCancel))
								.addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 434,
										Short.MAX_VALUE))
						.addContainerGap()));
		jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup().addGroup(jPanel2Layout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
						.addGroup(jPanel2Layout.createSequentialGroup().addContainerGap().addComponent(jButtonExport)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addGroup(jPanel2Layout.createSequentialGroup().addContainerGap().addComponent(jButtonCancel)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
						.addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap()));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.TRAILING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addGroup(layout.createParallelGroup(Alignment.LEADING)
								.addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
								.addComponent(jPanelOptions, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE))
						.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(jPanelOptions, GroupLayout.PREFERRED_SIZE, 163, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(23)
				.addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		getContentPane().setLayout(layout);

		java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 498) / 2, (screenSize.height - 419) / 2, 498, 419);
	}// </editor-fold>
		// GEN-END:initComponents

	@Override
	public DataLevel getDataLevel() {
		Object selectedItem = this.dataLevelComboBox.getSelectedItem();
		if (selectedItem != null) {
			return (DataLevel) selectedItem;
		}
		return DataLevel.LEVEL0;
	}

	private void jRadioButtonExportProteinsActionPerformed(java.awt.event.ActionEvent evt) {
		if (jRadioButtonExportProteins.isSelected()) {
			jCheckBoxCollapsePeptides.setEnabled(false);
			jCheckBoxCollapseProteins.setEnabled(true);
			if (this.dataLevel == null) {
				dataLevelComboBox.setEnabled(true);
			}
			if (isFDRApplied) {
				jCheckBoxIncludeDecoy.setEnabled(true);
			}
			jCheckBoxIncludeGeneInfo.setEnabled(true);
			jCheckBoxSearchForProteinSequence.setEnabled(true);
		}
	}

	private void jRadioButtonExportPeptidesActionPerformed(java.awt.event.ActionEvent evt) {
		if (jRadioButtonExportPeptides.isSelected()) {
			jCheckBoxCollapsePeptides.setEnabled(true);
			jCheckBoxCollapseProteins.setEnabled(false);
			dataLevelComboBox.setEnabled(false);
			jCheckBoxIncludeDecoy.setEnabled(false);
			jCheckBoxIncludeGeneInfo.setEnabled(false);
			jCheckBoxSearchForProteinSequence.setEnabled(false);
		}
	}

	private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {
		exporter.cancel(true);

	}

	private void jButtonExportActionPerformed(java.awt.event.ActionEvent evt) {
		log.info("Export button is pressed");
		final File file = getFile();
		setControlStatusEnabled(false);
		jButtonCancel.setEnabled(true);
		jProgressBar1.setIndeterminate(true);
		exporter = new TSVExporter(this, ExporterUtil.getSelectedIdentificationSets(idSets, getDataLevel()), file,
				this.filter);
		exporter.setDistinguisModificatedPeptides(isDistinguishModifiedPeptides());
		exporter.addPropertyChangeListener(this);
		exporter.execute();

	}

	@Override
	public boolean isDecoyHitsIncluded() {
		return jCheckBoxIncludeDecoy.isSelected();
	}

	@Override
	public boolean showPeptides() {
		return jRadioButtonExportPeptides.isSelected();

	}

	@Override
	public boolean isGeneInfoIncluded() {

		return jCheckBoxIncludeGeneInfo.isSelected();

	}

	@Override
	public boolean retrieveProteinSequences() {
		return jCheckBoxSearchForProteinSequence.isSelected();
	}

	private void jButtonSelectActionPerformed(java.awt.event.ActionEvent evt) {
		jTextFieldFilePath.setText(selectFile());
	}

	public String selectFile() {
		String filename = "";
		jFileChooser1 = new javax.swing.JFileChooser(MainFrame.currentFolder);
		jFileChooser1.setDialogTitle("Select an existing file or type the name for a new one");
		jFileChooser1.setFileFilter(new TsvFileFilter());
		jFileChooser1.showOpenDialog(this);
		File selectedFile = jFileChooser1.getSelectedFile();
		if (selectedFile != null) {
			MainFrame.currentFolder = selectedFile.getParentFile();
			filename = selectedFile.toString();
			String extension = FilenameUtils.getExtension(filename);
			if (extension == null || "".equals(extension)) {
				filename = filename + ".tsv";
			}
			log.info("Selected File: " + filename);
		} else {
			filename = "";
		}
		return filename;
	}

	private File getFile() {
		final String path = jTextFieldFilePath.getText();
		validatePath(path);
		return new File(path);
	}

	private void validatePath(String path) {
		if ("".equals(path)) {
			JOptionPane.showMessageDialog(this, "Select the output path", "Error", JOptionPane.WARNING_MESSAGE);
		}

	}

	// GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.ButtonGroup buttonGroup1;
	private javax.swing.JButton jButtonCancel;
	private javax.swing.JButton jButtonExport;
	private javax.swing.JButton jButtonSelect;
	private javax.swing.JCheckBox jCheckBoxCollapsePeptides;
	private javax.swing.JCheckBox jCheckBoxCollapseProteins;
	private javax.swing.JCheckBox jCheckBoxIncludeDecoy;
	private javax.swing.JCheckBox jCheckBoxIncludeGeneInfo;
	private javax.swing.JCheckBox jCheckBoxSearchForProteinSequence;
	private javax.swing.JFileChooser jFileChooser1;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	public javax.swing.JPanel jPanelOptions;
	private javax.swing.JProgressBar jProgressBar1;
	private javax.swing.JRadioButton jRadioButtonExportPeptides;
	private javax.swing.JRadioButton jRadioButtonExportProteins;
	private javax.swing.JTextField jTextFieldFilePath;
	private JComboBox<DataLevel> dataLevelComboBox;
	private boolean controlsDisabled = false;

	// End of variables declaration//GEN-END:variables

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (TSVExporter.DATA_EXPORTING_DONE.equals(evt.getPropertyName())) {
			final File file = (File) evt.getNewValue();
			JOptionPane.showMessageDialog(this, "Data exported succesfully at " + file.getAbsolutePath(),
					"Data exported", JOptionPane.INFORMATION_MESSAGE);
			setControlStatusToPrevious();
			jButtonCancel.setEnabled(false);
			jButtonExport.setEnabled(true);
			jProgressBar1.setValue(0);
			jProgressBar1.setString("");
			jProgressBar1.setIndeterminate(false);
		} else if ("progress".equals(evt.getPropertyName())) {
			int progress = (Integer) evt.getNewValue();
			jProgressBar1.setValue(progress);
			jProgressBar1.setString("Exporting..." + progress + "%");
			jButtonExport.setEnabled(false);

		} else if (TSVExporter.DATA_EXPORTING_ERROR.equals(evt.getPropertyName())) {
			JOptionPane.showMessageDialog(this, "Error exporting data: " + evt.getNewValue(), "Error exporting data",
					JOptionPane.INFORMATION_MESSAGE);
			setControlStatusToPrevious();
			jButtonCancel.setEnabled(false);
			jButtonExport.setEnabled(true);
			jProgressBar1.setIndeterminate(false);
		} else if (TSVExporter.DATA_EXPORTING_CANCELED.equals(evt.getPropertyName())) {
			jProgressBar1.setValue(0);
			setControlStatusToPrevious();
			jButtonCancel.setEnabled(false);
			jButtonExport.setEnabled(true);
			jProgressBar1.setIndeterminate(false);
			jProgressBar1.setString("Export cancelled");
			jProgressBar1.setStringPainted(true);

		} else if (evt.getPropertyName().equals(JTableLoader.PROTEIN_SEQUENCE_RETRIEVAL)) {
			jProgressBar1.setString("Retrieving protein sequences from uniprotKB...");
			jProgressBar1.setIndeterminate(true);
			jProgressBar1.setValue(0);
			jProgressBar1.setStringPainted(true);
		} else if (evt.getPropertyName().equals(JTableLoader.PROTEIN_SEQUENCE_RETRIEVAL_DONE)) {
			jProgressBar1.setString("Protein sequences retrieved");
			jProgressBar1.setIndeterminate(false);
			jProgressBar1.setStringPainted(true);
		}
	}

	private void setControlStatusToPrevious() {
		if (!controlsDisabled) {
			jCheckBoxCollapsePeptides.setEnabled(previousCollapsePeptides);
			jCheckBoxCollapseProteins.setEnabled(previousCollapseProteins);
			jCheckBoxIncludeDecoy.setEnabled(previousIncludeDecoy);
			jCheckBoxIncludeGeneInfo.setEnabled(previousIncludeGene);
			jCheckBoxSearchForProteinSequence.setEnabled(previousSearchForProteinSequence);
			jButtonExport.setEnabled(true);
			jRadioButtonExportPeptides.setEnabled(true);
			jRadioButtonExportProteins.setEnabled(true);
		}
	}

	public void setControlsDisabled() {
		this.controlsDisabled = true;
	}

	public void setControlStatusEnabled(boolean b) {
		previousCollapsePeptides = jCheckBoxCollapsePeptides.isEnabled();
		previousCollapseProteins = jCheckBoxCollapseProteins.isEnabled();
		previousIncludeDecoy = jCheckBoxIncludeDecoy.isEnabled();
		previousIncludeGene = jCheckBoxIncludeGeneInfo.isEnabled();
		previousSearchForProteinSequence = jCheckBoxSearchForProteinSequence.isEnabled();

		jCheckBoxCollapsePeptides.setEnabled(b);
		jCheckBoxCollapseProteins.setEnabled(b);
		jCheckBoxIncludeDecoy.setEnabled(b);
		jCheckBoxIncludeGeneInfo.setEnabled(b);
		jCheckBoxSearchForProteinSequence.setEnabled(b);
		jRadioButtonExportProteins.setEnabled(b);
		jRadioButtonExportPeptides.setEnabled(b);
		jButtonExport.setEnabled(b);
		lblLevelToExport.setEnabled(b);
		if (dataLevel == null) {
			dataLevelComboBox.setEnabled(b);
		}
	}

	@Override
	public boolean showBestPeptides() {
		return jCheckBoxCollapsePeptides.isSelected();
	}

	@Override
	public boolean showBestProteins() {
		return jCheckBoxCollapseProteins.isSelected();
	}

	@Override
	public boolean isFDRApplied() {
		return isFDRApplied;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.proteored.pacom.analysis.exporters.ExporterManager#
	 * isDistinguishModifiedPeptides()
	 */
	@Override
	public boolean isDistinguishModifiedPeptides() {
		if (this.distinguisModifiedPeptides != null) {
			return this.distinguisModifiedPeptides;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.proteored.pacom.analysis.exporters.ExporterManager#getComparisonType(
	 * )
	 */
	@Override
	public ProteinGroupComparisonType getComparisonType() {
		return this.chartManagerFrame.getComparisonType();
	}

	public void setExporterParameters(ExporterManager exporterManager) {
		jCheckBoxIncludeDecoy.setSelected(exporterManager.isDecoyHitsIncluded());
		jCheckBoxIncludeGeneInfo.setSelected(exporterManager.isGeneInfoIncluded());
		dataLevelComboBox.setSelectedItem(exporterManager.getDataLevel());
		jCheckBoxCollapsePeptides.setSelected(exporterManager.showBestPeptides());
		jCheckBoxCollapseProteins.setSelected(exporterManager.showBestProteins());
		jRadioButtonExportPeptides.setSelected(exporterManager.showPeptides());
		jRadioButtonExportProteins.setSelected(exporterManager.showBestProteins());
		jCheckBoxSearchForProteinSequence.setSelected(exporterManager.retrieveProteinSequences());
	}

	public void enableExportButton(boolean b) {
		jButtonExport.setEnabled(b);
	}

}