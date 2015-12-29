/*
 * IdentificationTableFrame.java Created on __DATE__, __TIME__
 */

package org.proteored.miapeExtractor.analysis.exporters.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;
import org.jfree.ui.RefineryUtilities;
import org.proteored.miapeExtractor.analysis.exporters.ExporterManager;
import org.proteored.miapeExtractor.analysis.exporters.tasks.JTableLoader;
import org.proteored.miapeExtractor.analysis.exporters.util.ExportedColumns;
import org.proteored.miapeExtractor.analysis.gui.ChartManagerFrame;
import org.proteored.miapeExtractor.gui.ImageManager;
import org.proteored.miapeapi.experiment.model.IdentificationSet;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

/**
 * 
 * @author __USER__
 */
public class IdentificationTableFrame extends javax.swing.JFrame implements
		ExporterManager, PropertyChangeListener {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");
	private static final int NUM_MIN_TYPED_CHARS = 2;

	private final IdentificationSet idSet;

	private JTableLoader tableExporter;
	private JPanel scrollPanel;

	private ScrollableJTable scrollablePanel;
	private final ChartManagerFrame parentFrame;
	private static IdentificationTableFrame instance;

	public static IdentificationTableFrame getInstance(
			ChartManagerFrame parent, IdentificationSet idSet) {
		if (instance == null || !idSet.equals(instance.idSet))
			instance = new IdentificationTableFrame(parent, idSet);

		// Just enable if the FDR filter is enabled
		if (idSet != null && parent.getFiltersDialog() != null
				&& parent.getFiltersDialog().isFDRFilterDefined())
			instance.jCheckBoxIncludeDecoy.setEnabled(true);
		return instance;
	}

	@Override
	public void setVisible(boolean b) {
		if (b)
			pack();
		super.setVisible(b);
	}

	/** Creates new form IdentificationTableFrame */
	private IdentificationTableFrame(ChartManagerFrame parent,
			IdentificationSet idSet) {
		try {
			UIManager.setLookAndFeel(new WindowsLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
		}
		initComponents();
		// MainFrame.autoScroll(jScrollPane3, jTextAreaStatus);
		this.idSet = idSet;
		if (this.idSet != null)
			setTitle("Identification table: '" + idSet.getName() + "'");
		else
			setTitle("Identification table");
		loadTable();
		// Just enable if the protein sequences have not been retrieved before
		jCheckBoxSearchForProteinSequence.setEnabled(!parent
				.isProteinSequencesRetrieved());

		parentFrame = parent;

		// set icon image
		setIconImage(ImageManager
				.getImageIcon(ImageManager.PROTEORED_MIAPE_API).getImage());

		// set font to textarea
		jTextAreaStatus.setFont(new JLabel().getFont());

		// Initialize combo box of column names
		updateColumnNamesComboBox();
	}

	private void updateColumnNamesComboBox() {
		List<String> columnsStringList = ExportedColumns
				.getColumnsStringForTable(
						isReplicateAndExperimentOriginIncluded(),
						showPeptides(), isGeneInfoIncluded(),
						jCheckBoxIncludeDecoy.isEnabled(), idSet);
		jComboBoxColumnNames.setModel(new DefaultComboBoxModel(
				columnsStringList.toArray()));
		jComboBoxColumnNames.setSelectedIndex(3);

	}

	@Override
	public void dispose() {
		if (tableExporter != null
				&& tableExporter.getState() == StateValue.STARTED) {
			boolean canceled = tableExporter.cancel(true);
			while (!canceled) {
				canceled = tableExporter.cancel(true);
				if (!canceled)
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
			}
		}
		if (parentFrame != null)
			parentFrame.setVisible(true);
		super.dispose();
	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		buttonGroupProteinOrPeptide = new javax.swing.ButtonGroup();
		jPanelLeft = new javax.swing.JPanel();
		jPanelOptions = new javax.swing.JPanel();
		jCheckBoxIncludeDecoy = new javax.swing.JCheckBox();
		jCheckBoxIncludeExperimentReplicateOrigin = new javax.swing.JCheckBox();
		jCheckBoxSearchForProteinSequence = new javax.swing.JCheckBox();
		jCheckBoxIncludeGeneInfo = new javax.swing.JCheckBox();
		jCheckBoxCollapsePeptides = new javax.swing.JCheckBox();
		jCheckBoxCollapseProteins = new javax.swing.JCheckBox();
		jRadioButtonShowProteins = new javax.swing.JRadioButton();
		jRadioButtonShowPeptides = new javax.swing.JRadioButton();
		jCheckBoxIncludeNonConclusiveProteins = new javax.swing.JCheckBox();
		jButtonCancel = new javax.swing.JButton();
		jPanelFilterByColumn = new javax.swing.JPanel();
		jComboBoxColumnNames = new javax.swing.JComboBox();
		jLabel1 = new javax.swing.JLabel();
		jTextFieldFilter = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		jPanelStatus = new javax.swing.JPanel();
		jScrollPane3 = new javax.swing.JScrollPane();
		jTextAreaStatus = new javax.swing.JTextArea();
		jProgressBar1 = new javax.swing.JProgressBar();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		jPanelLeft.setPreferredSize(new java.awt.Dimension(283, 400));
		jPanelLeft.setRequestFocusEnabled(false);

		jPanelOptions.setBorder(javax.swing.BorderFactory.createTitledBorder(
				javax.swing.BorderFactory.createEtchedBorder(), "Options"));

		jCheckBoxIncludeDecoy.setText("include DECOY hits");
		jCheckBoxIncludeDecoy
				.setToolTipText("<html>If this options is activated, decoy peptides/proteins <br>will also be included in the exported file.<br>\nThis options only is available if a FDR filter is applied.</html>");
		jCheckBoxIncludeDecoy.setEnabled(false);
		jCheckBoxIncludeDecoy
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jCheckBoxIncludeDecoyActionPerformed(evt);
					}
				});

		jCheckBoxIncludeExperimentReplicateOrigin
				.setText("include project tree level names");
		jCheckBoxIncludeExperimentReplicateOrigin
				.setToolTipText("<html>\nAdd columns indicating at which node of the comparison project tree, each identification item belongs\n</html>");
		jCheckBoxIncludeExperimentReplicateOrigin
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jCheckBoxIncludeExperimentReplicateOriginActionPerformed(evt);
					}
				});

		jCheckBoxSearchForProteinSequence
				.setText("calculate protein coverages");
		jCheckBoxSearchForProteinSequence
				.setToolTipText("<html>\nThis options will retrieve protein sequences from the Internet in order to\n<br>\ncalculate the protein coverage. Depending on the number of proteins you\n<br>have, it can take several minutes.\n</html>");
		jCheckBoxSearchForProteinSequence
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jCheckBoxSearchForProteinSequenceActionPerformed(evt);
					}
				});

		jCheckBoxIncludeGeneInfo.setText("include gene information");
		jCheckBoxIncludeGeneInfo
				.setToolTipText("<html>\nThis option will add some columns containing information<br>\nabout the genes associated with each protein.\n</html>");
		jCheckBoxIncludeGeneInfo
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jCheckBoxIncludeGeneInfoActionPerformed(evt);
					}
				});

		jCheckBoxCollapsePeptides.setText("show just the best peptides");
		jCheckBoxCollapsePeptides
				.setToolTipText("<html>\nIf this option is activated, if the same peptide has been detected\n<br>\nmore than once, it will appear only once in the exported table and\n<br>\nthe score will be the best score of all occurrences.</html>");
		jCheckBoxCollapsePeptides.setEnabled(false);
		jCheckBoxCollapsePeptides
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jCheckBoxCollapsePeptidesActionPerformed(evt);
					}
				});

		jCheckBoxCollapseProteins.setSelected(true);
		jCheckBoxCollapseProteins.setText("show just the best proteins");
		jCheckBoxCollapseProteins
				.setToolTipText("<html>\nIf this option is activated, if the same protein has been detected\n<br>\nmore than once, it will appear only once in the exported table and\n<br>\nthe score will be the best score of all occurrences.</html>");
		jCheckBoxCollapseProteins
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jCheckBoxCollapseProteinsActionPerformed(evt);
					}
				});

		buttonGroupProteinOrPeptide.add(jRadioButtonShowProteins);
		jRadioButtonShowProteins.setSelected(true);
		jRadioButtonShowProteins.setText("show proteins");
		jRadioButtonShowProteins.setToolTipText("Show just proteins");
		jRadioButtonShowProteins
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jRadioButtonShowProteinsActionPerformed(evt);
					}
				});

		buttonGroupProteinOrPeptide.add(jRadioButtonShowPeptides);
		jRadioButtonShowPeptides.setText("show peptides");
		jRadioButtonShowPeptides.setToolTipText("Show just peptides");
		jRadioButtonShowPeptides
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jRadioButtonShowPeptidesActionPerformed(evt);
					}
				});

		jCheckBoxIncludeNonConclusiveProteins
				.setText("include NON Conclusive proteins");
		jCheckBoxIncludeNonConclusiveProteins
				.setToolTipText("<html>Select this checkbox in order to exclude the <b>Non Conclusive</b> proteins<br>in the exported data.<br><b>Non conclusive protein</b> is a protein that shares all its matched peptides<br> with either conclusive or indistinguishable proteins.<br>\nA <b>conclusive protein</b> is a protein identified by at least one unique (distinct, discrete) peptide (peptides are considered different only if they can be distinguished by evidence in mass spectrum)<br>\nA <b>indistinguisable protein</b> is a member of a group of proteins sharing all peptides that are exclusive to the group (peptides are considered different only if they can be distinguished by evidence in mass spectrum).</html>\");\n");
		jCheckBoxIncludeNonConclusiveProteins
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jCheckBoxIncludeNonConclusiveProteinsActionPerformed(evt);
					}
				});

		jButtonCancel.setText("cancel");
		jButtonCancel.setToolTipText("Cancel data loading");
		jButtonCancel.setEnabled(false);
		jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonCancelActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanelOptionsLayout = new javax.swing.GroupLayout(
				jPanelOptions);
		jPanelOptions.setLayout(jPanelOptionsLayout);
		jPanelOptionsLayout
				.setHorizontalGroup(jPanelOptionsLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelOptionsLayout
										.createSequentialGroup()
										.addGroup(
												jPanelOptionsLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jCheckBoxIncludeDecoy)
														.addComponent(
																jCheckBoxIncludeExperimentReplicateOrigin)
														.addComponent(
																jCheckBoxIncludeGeneInfo)
														.addComponent(
																jCheckBoxIncludeNonConclusiveProteins)
														.addComponent(
																jCheckBoxSearchForProteinSequence)
														.addComponent(
																jRadioButtonShowPeptides)
														.addComponent(
																jCheckBoxCollapseProteins)
														.addComponent(
																jRadioButtonShowProteins)
														.addGroup(
																jPanelOptionsLayout
																		.createSequentialGroup()
																		.addComponent(
																				jCheckBoxCollapsePeptides)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																		.addComponent(
																				jButtonCancel)))
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));
		jPanelOptionsLayout
				.setVerticalGroup(jPanelOptionsLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelOptionsLayout
										.createSequentialGroup()
										.addComponent(
												jCheckBoxIncludeDecoy,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jCheckBoxIncludeExperimentReplicateOrigin,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jCheckBoxIncludeGeneInfo,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jCheckBoxIncludeNonConclusiveProteins,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jCheckBoxSearchForProteinSequence,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jRadioButtonShowProteins)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addComponent(jRadioButtonShowPeptides)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jCheckBoxCollapseProteins,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanelOptionsLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jCheckBoxCollapsePeptides,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																30,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jButtonCancel))
										.addGap(81, 81, 81)));

		jPanelFilterByColumn.setBorder(javax.swing.BorderFactory
				.createTitledBorder(
						javax.swing.BorderFactory.createEtchedBorder(),
						"Filter table"));

		jComboBoxColumnNames.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "asdf" }));
		jComboBoxColumnNames.setToolTipText("Column names");
		jComboBoxColumnNames.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				jComboBoxColumnNamesItemStateChanged(evt);
			}
		});

		jLabel1.setText("Column:");

		jTextFieldFilter.setToolTipText("Filter text");
		jTextFieldFilter.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyReleased(java.awt.event.KeyEvent evt) {
				jTextFieldFilterKeyReleased(evt);
			}
		});

		jLabel2.setText("Filter:");

		javax.swing.GroupLayout jPanelFilterByColumnLayout = new javax.swing.GroupLayout(
				jPanelFilterByColumn);
		jPanelFilterByColumn.setLayout(jPanelFilterByColumnLayout);
		jPanelFilterByColumnLayout
				.setHorizontalGroup(jPanelFilterByColumnLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelFilterByColumnLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanelFilterByColumnLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(jLabel1)
														.addComponent(jLabel2))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanelFilterByColumnLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jTextFieldFilter,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																185,
																Short.MAX_VALUE)
														.addComponent(
																jComboBoxColumnNames,
																0, 185,
																Short.MAX_VALUE))
										.addContainerGap()));
		jPanelFilterByColumnLayout
				.setVerticalGroup(jPanelFilterByColumnLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelFilterByColumnLayout
										.createSequentialGroup()
										.addGroup(
												jPanelFilterByColumnLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel1)
														.addComponent(
																jComboBoxColumnNames,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanelFilterByColumnLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel2)
														.addComponent(
																jTextFieldFilter,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		jPanelStatus.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		jScrollPane3.setToolTipText("Status");
		jScrollPane3.setAutoscrolls(true);

		jTextAreaStatus.setColumns(20);
		jTextAreaStatus.setEditable(false);
		jTextAreaStatus.setLineWrap(true);
		jTextAreaStatus.setRows(5);
		jTextAreaStatus.setToolTipText("Status");
		jTextAreaStatus.setWrapStyleWord(true);
		jScrollPane3.setViewportView(jTextAreaStatus);

		jProgressBar1.setPreferredSize(new java.awt.Dimension(316, 57));
		jProgressBar1.setStringPainted(true);

		javax.swing.GroupLayout jPanelStatusLayout = new javax.swing.GroupLayout(
				jPanelStatus);
		jPanelStatus.setLayout(jPanelStatusLayout);
		jPanelStatusLayout
				.setHorizontalGroup(jPanelStatusLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								jPanelStatusLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanelStatusLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.TRAILING)
														.addComponent(
																jProgressBar1,
																javax.swing.GroupLayout.Alignment.LEADING,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																244,
																Short.MAX_VALUE)
														.addComponent(
																jScrollPane3,
																javax.swing.GroupLayout.Alignment.LEADING,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																244,
																Short.MAX_VALUE))
										.addContainerGap()));
		jPanelStatusLayout
				.setVerticalGroup(jPanelStatusLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelStatusLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(
												jScrollPane3,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												23, Short.MAX_VALUE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jProgressBar1,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												22,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		javax.swing.GroupLayout jPanelLeftLayout = new javax.swing.GroupLayout(
				jPanelLeft);
		jPanelLeft.setLayout(jPanelLeftLayout);
		jPanelLeftLayout
				.setHorizontalGroup(jPanelLeftLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelLeftLayout
										.createSequentialGroup()
										.addGroup(
												jPanelLeftLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jPanelOptions,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																jPanelFilterByColumn,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																jPanelStatus,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE))
										.addContainerGap()));
		jPanelLeftLayout
				.setVerticalGroup(jPanelLeftLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelLeftLayout
										.createSequentialGroup()
										.addGap(13, 13, 13)
										.addComponent(
												jPanelOptions,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												288,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(1, 1, 1)
										.addComponent(
												jPanelFilterByColumn,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jPanelStatus,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addGap(104, 104, 104)));

		getContentPane().add(jPanelLeft, java.awt.BorderLayout.WEST);

		java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit()
				.getScreenSize();
		setBounds((screenSize.width - 863) / 2, (screenSize.height - 618) / 2,
				863, 618);
	}// </editor-fold>
		// GEN-END:initComponents

	private void jComboBoxColumnNamesItemStateChanged(
			java.awt.event.ItemEvent evt) {
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			applyFilters();
		}
	}

	private void jTextFieldFilterKeyReleased(java.awt.event.KeyEvent evt) {
		applyFilters();
	}

	private void applyFilters() {
		// Just if loading table is not running
		if (tableExporter == null
				|| tableExporter.getState() != StateValue.STARTED) {
			final String text = jTextFieldFilter.getText();
			String currentSelection = null;
			// if (text.length() > NUM_MIN_TYPED_CHARS)
			if (jComboBoxColumnNames.getSelectedIndex() != -1) {
				currentSelection = (String) jComboBoxColumnNames
						.getSelectedItem();
			}

			IdentificationTableFrame.this.scrollablePanel.setFilter(
					currentSelection, text);
			int rowCount = IdentificationTableFrame.this.scrollablePanel
					.getTable().getRowCount();
			if (rowCount > 0)
				IdentificationTableFrame.this.appendStatus("Showing "
						+ rowCount + " rows");

		} else {
			log.info("Not filtering because the table is being loading");
		}
	}

	private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {
		if (tableExporter != null) {
			while (true) {

				boolean cancelled = tableExporter.cancel(true);
				if (cancelled)
					break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}

	private void jCheckBoxIncludeNonConclusiveProteinsActionPerformed(
			ActionEvent evt) {
		updateSizesLabels();
		loadTable();
	}

	private void jRadioButtonShowPeptidesActionPerformed(
			java.awt.event.ActionEvent evt) {
		if (jRadioButtonShowPeptides.isSelected()) {
			jCheckBoxCollapsePeptides.setSelected(true);
			jCheckBoxCollapsePeptides.setEnabled(true);
			jCheckBoxCollapseProteins.setEnabled(false);
			jCheckBoxSearchForProteinSequence.setEnabled(false);

			updateSizesLabels();
			loadTable();
		}
	}

	private void jRadioButtonShowProteinsActionPerformed(
			java.awt.event.ActionEvent evt) {
		if (jRadioButtonShowProteins.isSelected()) {
			jCheckBoxCollapseProteins.setSelected(true);
			jCheckBoxCollapseProteins.setEnabled(true);
			jCheckBoxCollapsePeptides.setEnabled(false);
			jCheckBoxSearchForProteinSequence.setEnabled(true);
			updateSizesLabels();
			loadTable();
		}

	}

	private void jCheckBoxCollapseProteinsActionPerformed(
			java.awt.event.ActionEvent evt) {
		updateSizesLabels();
		loadTable();
	}

	private void jCheckBoxCollapsePeptidesActionPerformed(
			java.awt.event.ActionEvent evt) {
		updateSizesLabels();
		loadTable();
	}

	private void jCheckBoxIncludeGeneInfoActionPerformed(
			java.awt.event.ActionEvent evt) {
		loadTable();
	}

	private void jCheckBoxSearchForProteinSequenceActionPerformed(
			java.awt.event.ActionEvent evt) {
		loadTable();

	}

	private void jCheckBoxIncludeExperimentReplicateOriginActionPerformed(
			java.awt.event.ActionEvent evt) {
		loadTable();
	}

	private void jCheckBoxIncludeDecoyActionPerformed(
			java.awt.event.ActionEvent evt) {
		loadTable();
	}

	private void updateSizesLabels() {
		updateColumnNamesComboBox();

	}

	public void appendStatus(String notificacion) {
		jTextAreaStatus.append(notificacion + "\n");
		jTextAreaStatus
				.setCaretPosition(jTextAreaStatus.getText().length() - 1);

	}

	public void setStatus(String notificacion) {
		jTextAreaStatus.setText(notificacion + "\n");
		jTextAreaStatus
				.setCaretPosition(jTextAreaStatus.getText().length() - 1);

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(JTableLoader.DATA_EXPORTING_STARTING)) {
			setStatus("Loading " + getNum() + " items in table...");

			RefineryUtilities.centerFrameOnScreen(this);
			applyFilters();
		} else if (evt.getPropertyName().equals(
				JTableLoader.DATA_EXPORTING_DONE)) {
			String items = "proteins";
			if (showPeptides())
				items = "peptides";

			scrollablePanel.initializeSorter();
			scrollablePanel.getTable().repaint();

			jButtonCancel.setEnabled(false);
			int numLoaded = (Integer) evt.getNewValue();
			int num = getNum();
			if (isNonConclusiveProteinsIncluded() && num != numLoaded) {
				int numNonconclusive = numLoaded - num;
				String notificacion = numLoaded + " " + items + " loaded.";
				if (numNonconclusive > 0) {
					notificacion = notificacion + " " + numNonconclusive
							+ " NONCONCLUSIVE " + items + " were included.";
					appendStatus(notificacion);
				}
			} else {
				log.info("Table created");
				appendStatus(numLoaded + " " + items + " loaded.");
			}

			// update ProteinSequencesRetrieved variable in ChartManagerFrame
			if (jRadioButtonShowProteins.isSelected()) {
				if (jCheckBoxSearchForProteinSequence.isSelected()) {
					// if the protein sequences have been retrieved
					if (!parentFrame.isProteinSequencesRetrieved())
						parentFrame.setProteinSequencesRetrieved(true);
				}

			}
			// if a filter is typed, apply it
			applyFilters();

		} else if (evt.getPropertyName().equals("progress")) {
			jProgressBar1.setValue((Integer) evt.getNewValue());
		} else if (evt.getPropertyName().equals(
				JTableLoader.DATA_EXPORTING_SORTING)) {
			if (showPeptides()) {
				appendStatus("Sorting peptides by best score...");
			} else {
				appendStatus("Sorting proteins by best peptide score...");
			}
			jButtonCancel.setEnabled(true);
		} else if (evt.getPropertyName().equals(
				JTableLoader.DATA_EXPORTING_SORTING_DONE)) {
			appendStatus("Data sorted. Now loading table...");
		} else if (evt.getPropertyName().equals(
				JTableLoader.DATA_EXPORTING_CANCELED)) {
			int num = (Integer) evt.getNewValue();
			jButtonCancel.setEnabled(false);
			jProgressBar1.setValue(0);
			scrollablePanel.initializeSorter();
			scrollablePanel.getTable().repaint();
			appendStatus("Loading cancelled.");
			appendStatus(num + " rows where loaded.");
			applyFilters();
		}

	}

	private void loadTable() {
		scrollablePanel = new ScrollableJTable();
		// update combo box of column names
		updateColumnNamesComboBox();
		// Remove the previous JScrollableJTable if present
		final Component[] components = getContentPane().getComponents();
		int indexToRemove = -1;
		for (int i = 0; i < components.length; i++) {
			Component component = components[i];
			if (component instanceof ScrollableJTable)
				indexToRemove = i;

		}
		if (indexToRemove > -1)
			getContentPane().remove(indexToRemove);

		scrollablePanel.setPreferredSize(new Dimension(800, 700));
		getContentPane().add(scrollablePanel);

		pack();

		// Load data in table
		if (tableExporter != null
				&& tableExporter.getState().equals(StateValue.STARTED)) {
			while (true || !tableExporter.getState().equals(StateValue.STARTED)) {
				final boolean canceled = tableExporter.cancel(true);
				if (canceled)
					break;
			}

		}

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				tableExporter = new JTableLoader(IdentificationTableFrame.this,
						idSet, scrollablePanel.getTable());
				tableExporter
						.addPropertyChangeListener(IdentificationTableFrame.this);
				tableExporter.execute();
			}
		});

	}

	private int getNum() {
		int num = 0;
		if (jRadioButtonShowProteins.isSelected()) {
			if (jCheckBoxCollapseProteins.isSelected()) {
				num = idSet
						.getNumDifferentProteinGroups(isNonConclusiveProteinsIncluded());

				if (!jCheckBoxIncludeDecoy.isEnabled()
						|| !jCheckBoxIncludeDecoy.isSelected()) {
					num = num - idSet.getNumDifferentProteinGroupsDecoys();
				}
				if (isNonConclusiveProteinsIncluded())
					num += idSet.getNumDifferentNonConclusiveProteinGroups();

			} else {
				num = idSet
						.getTotalNumProteinGroups(isNonConclusiveProteinsIncluded());
				if (!jCheckBoxIncludeDecoy.isEnabled()
						|| !jCheckBoxIncludeDecoy.isSelected()) {
					num = num - idSet.getNumProteinGroupDecoys();
				}
				if (isNonConclusiveProteinsIncluded())
					num += idSet.getTotalNumNonConclusiveProteinGroups();

			}

		} else {
			if (jCheckBoxCollapsePeptides.isSelected()) {
				num = idSet.getNumDifferentPeptides(true);

				if (!jCheckBoxIncludeDecoy.isEnabled()
						|| !jCheckBoxIncludeDecoy.isSelected()) {
					num = num - idSet.getNumDifferentPeptideDecoys(true);
				}

			} else {

				num = idSet.getTotalNumPeptides();
				if (!jCheckBoxIncludeDecoy.isEnabled()
						|| !jCheckBoxIncludeDecoy.isSelected()) {
					num = num - idSet.getNumPeptideDecoys();
				}
			}
		}
		return num;
	}

	// GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.ButtonGroup buttonGroupProteinOrPeptide;
	private javax.swing.JButton jButtonCancel;
	private javax.swing.JCheckBox jCheckBoxCollapsePeptides;
	private javax.swing.JCheckBox jCheckBoxCollapseProteins;
	private javax.swing.JCheckBox jCheckBoxIncludeDecoy;
	private javax.swing.JCheckBox jCheckBoxIncludeExperimentReplicateOrigin;
	private javax.swing.JCheckBox jCheckBoxIncludeGeneInfo;
	private javax.swing.JCheckBox jCheckBoxIncludeNonConclusiveProteins;
	private javax.swing.JCheckBox jCheckBoxSearchForProteinSequence;
	private javax.swing.JComboBox jComboBoxColumnNames;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JPanel jPanelFilterByColumn;
	private javax.swing.JPanel jPanelLeft;
	public javax.swing.JPanel jPanelOptions;
	private javax.swing.JPanel jPanelStatus;
	private javax.swing.JProgressBar jProgressBar1;
	private javax.swing.JRadioButton jRadioButtonShowPeptides;
	private javax.swing.JRadioButton jRadioButtonShowProteins;
	private javax.swing.JScrollPane jScrollPane3;
	private javax.swing.JTextArea jTextAreaStatus;
	private javax.swing.JTextField jTextFieldFilter;

	// End of variables declaration//GEN-END:variables
	@Override
	public boolean isReplicateAndExperimentOriginIncluded() {
		return jCheckBoxIncludeExperimentReplicateOrigin.isSelected();
	}

	@Override
	public boolean isDecoyHitsIncluded() {
		return jCheckBoxIncludeDecoy.isSelected();
	}

	@Override
	public boolean showPeptides() {
		return jRadioButtonShowPeptides.isSelected();
	}

	@Override
	public boolean isGeneInfoIncluded() {
		return jCheckBoxIncludeGeneInfo.isSelected();
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
	public boolean retrieveProteinSequences() {
		return jCheckBoxSearchForProteinSequence.isSelected();
	}

	@Override
	public boolean isNonConclusiveProteinsIncluded() {
		return jCheckBoxIncludeNonConclusiveProteins.isSelected();
	}

	@Override
	public boolean isFDRApplied() {
		if (idSet != null && parentFrame.getFiltersDialog() != null
				&& parentFrame.getFiltersDialog().isFDRFilterDefined())
			return true;
		return false;
	}

}