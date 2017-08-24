/*
 * IdentificationTableFrame.java Created on __DATE__, __TIME__
 */

package org.proteored.pacom.analysis.exporters.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;
import org.jfree.ui.RefineryUtilities;
import org.proteored.miapeapi.experiment.model.IdentificationSet;
import org.proteored.miapeapi.experiment.model.sort.ProteinGroupComparisonType;
import org.proteored.pacom.analysis.exporters.ExporterManager;
import org.proteored.pacom.analysis.exporters.tasks.JTableLoader;
import org.proteored.pacom.analysis.exporters.util.ExportedColumns;
import org.proteored.pacom.analysis.exporters.util.ExporterUtil;
import org.proteored.pacom.analysis.gui.ChartManagerFrame;
import org.proteored.pacom.analysis.util.DataLevel;
import org.proteored.pacom.gui.ImageManager;
import org.proteored.pacom.utils.ComponentEnableStateKeeper;

import gnu.trove.set.hash.THashSet;

/**
 * 
 * @author __USER__
 */
public class IdentificationTableFrame extends javax.swing.JFrame implements ExporterManager, PropertyChangeListener {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");
	private static final int NUM_MIN_TYPED_CHARS = 2;

	private final Set<IdentificationSet> idSets = new THashSet<IdentificationSet>();

	private JTableLoader tableExporter;
	private ScrollableJTable scrollablePanel;
	private final ChartManagerFrame parentFrame;
	private JButton jButtonExport2Excel;
	private static IdentificationTableFrame instance;
	private final ComponentEnableStateKeeper enableStateKeeper = new ComponentEnableStateKeeper();

	public static IdentificationTableFrame getInstance(ChartManagerFrame parent, Collection<IdentificationSet> idSets) {
		if (instance == null || !idSets.containsAll(instance.idSets))
			instance = new IdentificationTableFrame(parent, idSets);

		// Just enable if the FDR filter is enabled
		if (idSets != null && !idSets.isEmpty() && parent.getFiltersDialog() != null
				&& parent.getFiltersDialog().isFDRFilterDefined()) {
			instance.jCheckBoxIncludeDecoy.setEnabled(true);
		}
		return instance;
	}

	@Override
	public void setVisible(boolean b) {
		if (b)
			pack();
		super.setVisible(b);
	}

	private String getIdentificationSetNameString() {
		StringBuilder sb = new StringBuilder();
		for (IdentificationSet identificationSet : ExporterUtil.getSelectedIdentificationSets(this.idSets,
				getDataLevel())) {
			if (!"".equals(sb.toString())) {
				sb.append(",");
			}
			sb.append(identificationSet.getName());
		}
		return sb.toString();
	}

	/** Creates new form IdentificationTableFrame */
	private IdentificationTableFrame(ChartManagerFrame parent, Collection<IdentificationSet> idSets) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
				| IllegalAccessException e) {
			e.printStackTrace();
		}
		initComponents();
		// MainFrame.autoScroll(jScrollPane3, jTextAreaStatus);
		this.idSets.addAll(idSets);
		if (!this.idSets.isEmpty()) {
			setTitle("Identification table: '" + getIdentificationSetNameString() + "'");
		} else {
			setTitle("Identification table");
		}
		loadTable();
		// Just enable if the protein sequences have not been retrieved before
		jCheckBoxSearchForProteinSequence.setEnabled(!parent.isProteinSequencesRetrieved());

		parentFrame = parent;

		// set icon image
		setIconImage(ImageManager.getImageIcon(ImageManager.PACOM_LOGO).getImage());

		// set font to textarea
		jTextAreaStatus.setFont(new JLabel().getFont());

		// Initialize combo box of column names
		updateColumnNamesComboBox();

		// icon button excel
		jButtonExport2Excel.setIcon(ImageManager.getImageIcon(ImageManager.EXCEL_TABLE));

		enableStateKeeper.addReverseComponent(this.jButtonCancel);
	}

	private void updateColumnNamesComboBox() {
		List<String> columnsStringList = ExportedColumns.getColumnsStringForTable(showPeptides(), isGeneInfoIncluded(),
				jCheckBoxIncludeDecoy.isEnabled(),
				ExporterUtil.getSelectedIdentificationSets(this.idSets, getDataLevel()));
		jComboBoxColumnNames.setModel(new DefaultComboBoxModel(columnsStringList.toArray()));
		jComboBoxColumnNames.setSelectedIndex(3);

	}

	@Override
	public void dispose() {
		if (tableExporter != null && tableExporter.getState() == StateValue.STARTED) {
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
		jCheckBoxSearchForProteinSequence = new javax.swing.JCheckBox();
		jCheckBoxIncludeGeneInfo = new javax.swing.JCheckBox();
		jCheckBoxCollapsePeptides = new javax.swing.JCheckBox();
		jCheckBoxCollapseProteins = new javax.swing.JCheckBox();
		jRadioButtonShowProteins = new javax.swing.JRadioButton();
		jRadioButtonShowPeptides = new javax.swing.JRadioButton();
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

		jPanelOptions.setBorder(javax.swing.BorderFactory
				.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Options"));

		jCheckBoxIncludeDecoy.setText("include DECOY hits");
		jCheckBoxIncludeDecoy.setToolTipText(
				"<html>If this options is activated, decoy peptides/proteins <br>will also be included in the exported file.<br>\nThis options only is available if a FDR filter is applied.</html>");
		jCheckBoxIncludeDecoy.setEnabled(false);
		jCheckBoxIncludeDecoy.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxIncludeDecoyActionPerformed(evt);
			}
		});

		jCheckBoxSearchForProteinSequence.setText("calculate protein coverages");
		jCheckBoxSearchForProteinSequence.setToolTipText(
				"<html>\nThis options will retrieve protein sequences from the Internet in order to\n<br>\ncalculate the protein coverage. Depending on the number of proteins you\n<br>have, it can take several minutes.\n</html>");
		jCheckBoxSearchForProteinSequence.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxSearchForProteinSequenceActionPerformed(evt);
			}
		});

		jCheckBoxIncludeGeneInfo.setText("include gene information");
		jCheckBoxIncludeGeneInfo.setToolTipText(
				"<html>\nThis option will retrieve genes associated with the proteins from the UniprotKB.\nDepending on the number of proteins you\n<br>have, it can take several minutes.</html>");
		jCheckBoxIncludeGeneInfo.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxIncludeGeneInfoActionPerformed(evt);
			}
		});

		jCheckBoxCollapsePeptides.setText("hidde redundant peptides");
		jCheckBoxCollapsePeptides.setToolTipText(
				"<html>\nIf this option is activated, if the same peptide has been detected\n<br>\nmore than once, it will appear only once in the exported table and\n<br>\nthe score will be the best score of all occurrences.</html>");
		jCheckBoxCollapsePeptides.setEnabled(false);
		jCheckBoxCollapsePeptides.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxCollapsePeptidesActionPerformed(evt);
			}
		});

		jCheckBoxCollapseProteins.setSelected(true);
		jCheckBoxCollapseProteins.setText("hidde redundant proteins");
		jCheckBoxCollapseProteins.setToolTipText(
				"<html>\nIf this option is activated, if the same protein has been detected\n<br>\nmore than once, it will appear only once in the exported table and\n<br>\nthe score will be the best score of all occurrences.</html>");
		jCheckBoxCollapseProteins.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxCollapseProteinsActionPerformed(evt);
			}
		});

		buttonGroupProteinOrPeptide.add(jRadioButtonShowProteins);
		jRadioButtonShowProteins.setSelected(true);
		jRadioButtonShowProteins.setText("show proteins");
		jRadioButtonShowProteins.setToolTipText("Show just proteins");
		jRadioButtonShowProteins.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jRadioButtonShowProteinsActionPerformed(evt);
			}
		});

		buttonGroupProteinOrPeptide.add(jRadioButtonShowPeptides);
		jRadioButtonShowPeptides.setText("show peptides");
		jRadioButtonShowPeptides.setToolTipText("Show just peptides");
		jRadioButtonShowPeptides.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jRadioButtonShowPeptidesActionPerformed(evt);
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

		lblDataLevel = new JLabel("Data level:");

		dataLevelComboBox = new JComboBox();
		for (DataLevel dataLevel : DataLevel.values()) {
			dataLevelComboBox.addItem(dataLevel);
		}
		dataLevelComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				dataLevelSelected();
			}
		});

		jButtonExport2Excel = new JButton();
		jButtonExport2Excel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportTSV();
			}
		});
		jButtonExport2Excel.setToolTipText(
				"<html>Export current data to a Tab Separated Values file<br>that can be opened by Excel.</html>");
		jButtonExport2Excel.setEnabled(false);
		javax.swing.GroupLayout jPanelOptionsLayout = new javax.swing.GroupLayout(jPanelOptions);
		jPanelOptionsLayout.setHorizontalGroup(jPanelOptionsLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanelOptionsLayout.createSequentialGroup().addContainerGap()
						.addGroup(jPanelOptionsLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(jCheckBoxIncludeDecoy).addComponent(jCheckBoxIncludeGeneInfo)
								.addComponent(jCheckBoxSearchForProteinSequence).addComponent(jRadioButtonShowPeptides)
								.addComponent(jRadioButtonShowProteins).addComponent(jCheckBoxCollapseProteins)
								.addGroup(jPanelOptionsLayout.createSequentialGroup().addComponent(lblDataLevel)
										.addPreferredGap(ComponentPlacement.UNRELATED).addComponent(dataLevelComboBox,
												GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE))
								.addComponent(jCheckBoxCollapsePeptides)
								.addGroup(jPanelOptionsLayout.createSequentialGroup().addComponent(jButtonCancel)
										.addGap(18).addComponent(jButtonExport2Excel, GroupLayout.PREFERRED_SIZE, 46,
												GroupLayout.PREFERRED_SIZE)))
						.addContainerGap(92, Short.MAX_VALUE)));
		jPanelOptionsLayout.setVerticalGroup(jPanelOptionsLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(jPanelOptionsLayout.createSequentialGroup()
						.addComponent(jCheckBoxIncludeDecoy, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(jCheckBoxIncludeGeneInfo, GroupLayout.PREFERRED_SIZE, 30,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(jCheckBoxSearchForProteinSequence, GroupLayout.PREFERRED_SIZE, 30,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(jRadioButtonShowProteins)
						.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(jRadioButtonShowPeptides).addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(jCheckBoxCollapseProteins, GroupLayout.PREFERRED_SIZE, 30,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(jCheckBoxCollapsePeptides, GroupLayout.PREFERRED_SIZE, 30,
								GroupLayout.PREFERRED_SIZE)
						.addGap(8)
						.addGroup(jPanelOptionsLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblDataLevel)
								.addComponent(dataLevelComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addGap(15)
						.addGroup(jPanelOptionsLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(jButtonCancel).addComponent(jButtonExport2Excel,
										GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE))
						.addGap(40)));
		jPanelOptions.setLayout(jPanelOptionsLayout);

		jPanelFilterByColumn.setBorder(javax.swing.BorderFactory
				.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Filter table"));

		jComboBoxColumnNames.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "asdf" }));
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

		javax.swing.GroupLayout jPanelFilterByColumnLayout = new javax.swing.GroupLayout(jPanelFilterByColumn);
		jPanelFilterByColumn.setLayout(jPanelFilterByColumnLayout);
		jPanelFilterByColumnLayout.setHorizontalGroup(jPanelFilterByColumnLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanelFilterByColumnLayout.createSequentialGroup().addContainerGap()
						.addGroup(jPanelFilterByColumnLayout
								.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel1)
								.addComponent(jLabel2))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanelFilterByColumnLayout
								.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jTextFieldFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 185,
										Short.MAX_VALUE)
								.addComponent(jComboBoxColumnNames, 0, 185, Short.MAX_VALUE))
						.addContainerGap()));
		jPanelFilterByColumnLayout.setVerticalGroup(jPanelFilterByColumnLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanelFilterByColumnLayout.createSequentialGroup()
						.addGroup(jPanelFilterByColumnLayout
								.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel1)
								.addComponent(jComboBoxColumnNames, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanelFilterByColumnLayout
								.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel2)
								.addComponent(jTextFieldFilter, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jPanelStatus.setBorder(new TitledBorder(null, "Status", TitledBorder.LEADING, TitledBorder.TOP, null, null));

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

		javax.swing.GroupLayout jPanelStatusLayout = new javax.swing.GroupLayout(jPanelStatus);
		jPanelStatusLayout.setHorizontalGroup(jPanelStatusLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(jPanelStatusLayout.createSequentialGroup().addContainerGap()
						.addGroup(jPanelStatusLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(jScrollPane3, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 223,
										Short.MAX_VALUE)
								.addComponent(jProgressBar1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 223,
										Short.MAX_VALUE))
						.addContainerGap()));
		jPanelStatusLayout.setVerticalGroup(jPanelStatusLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanelStatusLayout.createSequentialGroup().addContainerGap()
						.addComponent(jScrollPane3, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE)
						.addGap(5)
						.addComponent(jProgressBar1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		jPanelStatus.setLayout(jPanelStatusLayout);

		javax.swing.GroupLayout jPanelLeftLayout = new javax.swing.GroupLayout(jPanelLeft);
		jPanelLeftLayout.setHorizontalGroup(jPanelLeftLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(jPanelLeftLayout.createSequentialGroup().addContainerGap()
						.addGroup(jPanelLeftLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(jPanelOptions, GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
								.addComponent(jPanelStatus, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 259,
										Short.MAX_VALUE)
								.addComponent(jPanelFilterByColumn, GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE))
						.addContainerGap()));
		jPanelLeftLayout.setVerticalGroup(jPanelLeftLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanelLeftLayout.createSequentialGroup().addContainerGap()
						.addComponent(jPanelOptions, GroupLayout.PREFERRED_SIZE, 314, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(jPanelFilterByColumn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(jPanelStatus, GroupLayout.PREFERRED_SIZE, 183, Short.MAX_VALUE).addGap(0)));
		jPanelLeft.setLayout(jPanelLeftLayout);

		getContentPane().add(jPanelLeft, java.awt.BorderLayout.WEST);

		java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 863) / 2, (screenSize.height - 618) / 2, 525, 447);
	}// </editor-fold>

	protected void exportTSV() {
		ExporterDialog exporterDialog = new ExporterDialog(this, parentFrame,
				ExporterUtil.getSelectedIdentificationSets(idSets, getDataLevel()), getDataLevel());
		// disable any modification
		exporterDialog.setControlsDisabled();
		exporterDialog.setControlStatusEnabled(false);
		// set exporting parameters
		exporterDialog.setExporterParameters(this);
		exporterDialog.enableExportButton(true);
		exporterDialog.setVisible(true);
	}

	protected void dataLevelSelected() {
		updateSizesLabels();
		loadTable();

	}

	// GEN-END:initComponents

	private void jComboBoxColumnNamesItemStateChanged(java.awt.event.ItemEvent evt) {
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			applyFilters();
		}
	}

	private void jTextFieldFilterKeyReleased(java.awt.event.KeyEvent evt) {
		applyFilters();
	}

	private void applyFilters() {
		// Just if loading table is not running
		if (tableExporter == null || tableExporter.getState() != StateValue.STARTED) {
			final String text = jTextFieldFilter.getText();
			String currentSelection = null;
			// if (text.length() > NUM_MIN_TYPED_CHARS)
			if (jComboBoxColumnNames.getSelectedIndex() != -1) {
				currentSelection = (String) jComboBoxColumnNames.getSelectedItem();
			}

			IdentificationTableFrame.this.scrollablePanel.setFilter(currentSelection, text);
			int rowCount = IdentificationTableFrame.this.scrollablePanel.getTable().getRowCount();
			if (rowCount > 0)
				IdentificationTableFrame.this.appendStatus("Showing " + rowCount + " rows");

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

	private void jRadioButtonShowPeptidesActionPerformed(java.awt.event.ActionEvent evt) {
		if (jRadioButtonShowPeptides.isSelected()) {
			jCheckBoxCollapsePeptides.setSelected(true);
			jCheckBoxCollapsePeptides.setEnabled(true);
			jCheckBoxCollapseProteins.setEnabled(false);
			jCheckBoxSearchForProteinSequence.setEnabled(false);

			updateSizesLabels();
			loadTable();
		}
	}

	private void jRadioButtonShowProteinsActionPerformed(java.awt.event.ActionEvent evt) {
		if (jRadioButtonShowProteins.isSelected()) {
			jCheckBoxCollapseProteins.setSelected(true);
			jCheckBoxCollapseProteins.setEnabled(true);
			jCheckBoxCollapsePeptides.setEnabled(false);
			jCheckBoxSearchForProteinSequence.setEnabled(true);
			updateSizesLabels();
			loadTable();
		}

	}

	private void jCheckBoxCollapseProteinsActionPerformed(java.awt.event.ActionEvent evt) {
		updateSizesLabels();
		loadTable();
	}

	private void jCheckBoxCollapsePeptidesActionPerformed(java.awt.event.ActionEvent evt) {
		updateSizesLabels();
		loadTable();
	}

	private void jCheckBoxIncludeGeneInfoActionPerformed(java.awt.event.ActionEvent evt) {
		loadTable();
	}

	private void jCheckBoxSearchForProteinSequenceActionPerformed(java.awt.event.ActionEvent evt) {
		loadTable();

	}

	private void jCheckBoxIncludeDecoyActionPerformed(java.awt.event.ActionEvent evt) {
		loadTable();
	}

	private void updateSizesLabels() {
		updateColumnNamesComboBox();

	}

	public void appendStatus(String notificacion) {
		jTextAreaStatus.append(notificacion + "\n");
		jTextAreaStatus.setCaretPosition(jTextAreaStatus.getText().length() - 1);

	}

	public void setStatus(String notificacion) {
		jTextAreaStatus.setText(notificacion + "\n");
		jTextAreaStatus.setCaretPosition(jTextAreaStatus.getText().length() - 1);

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(JTableLoader.DATA_EXPORTING_STARTING)) {
			enableStateKeeper.keepEnableStates(this);
			enableStateKeeper.disable(this);

			setStatus("Loading " + getNum() + " items in table...");

			RefineryUtilities.centerFrameOnScreen(this);
			applyFilters();
		} else if (evt.getPropertyName().equals(JTableLoader.DATA_EXPORTING_DONE)) {
			enableStateKeeper.setToPreviousState(this);
			String items = "proteins";
			if (showPeptides())
				items = "peptides";

			scrollablePanel.initializeSorter();
			scrollablePanel.getTable().repaint();

			jButtonCancel.setEnabled(false);
			int numLoaded = (Integer) evt.getNewValue();
			int num = getNum();
			if (num != numLoaded) {
				int numNonconclusive = numLoaded - num;
				String notificacion = numLoaded + " " + items + " loaded.";
				if (numNonconclusive > 0) {
					notificacion = notificacion + " " + numNonconclusive + " NONCONCLUSIVE " + items
							+ " were included.";
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
		} else if (JTableLoader.DATA_EXPORTING_ERROR.equals(evt.getPropertyName())) {

			enableStateKeeper.setToPreviousState(this);
			appendStatus("Error: " + evt.getNewValue().toString());

		} else if (evt.getPropertyName().equals(JTableLoader.DATA_EXPORTING_CANCELED)) {
			enableStateKeeper.setToPreviousState(this);
			int num = (Integer) evt.getNewValue();
			jProgressBar1.setValue(0);
			scrollablePanel.initializeSorter();
			scrollablePanel.getTable().repaint();
			appendStatus(num + " rows where loaded.");
			appendStatus("Table loading cancelled.");

			applyFilters();
		} else if (evt.getPropertyName().equals(JTableLoader.PROTEIN_SEQUENCE_RETRIEVAL)) {
			appendStatus(evt.getNewValue().toString());
			jProgressBar1.setIndeterminate(true);
			jProgressBar1.setValue(0);
			jProgressBar1.setStringPainted(false);
		} else if (evt.getPropertyName().equals(JTableLoader.PROTEIN_SEQUENCE_RETRIEVAL_DONE)) {
			appendStatus("Information retrieved.");
			jProgressBar1.setIndeterminate(false);
			jProgressBar1.setStringPainted(true);
		}

	}

	private void loadTable() {
		if (scrollablePanel == null) {
			scrollablePanel = new ScrollableJTable();
			scrollablePanel.setPreferredSize(new Dimension(800, 700));
			getContentPane().add(scrollablePanel);
		}
		// update combo box of column names
		updateColumnNamesComboBox();

		if (false) {
			// Remove the previous JScrollableJTable if present
			final Component[] components = getContentPane().getComponents();
			int indexToRemove = -1;
			for (int i = 0; i < components.length; i++) {
				Component component = components[i];
				if (component instanceof ScrollableJTable)
					indexToRemove = i;

			}
			if (indexToRemove > -1) {
				getContentPane().remove(indexToRemove);
			}

		}
		((MyIdentificationTable) scrollablePanel.getTable()).clearData();

		pack();

		// Load data in table
		if (tableExporter != null && tableExporter.getState().equals(StateValue.STARTED)) {
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
						ExporterUtil.getSelectedIdentificationSets(idSets, getDataLevel()), scrollablePanel.getTable());
				tableExporter.addPropertyChangeListener(IdentificationTableFrame.this);
				tableExporter.execute();
			}
		});

	}

	private int getNum() {
		int num = 0;
		for (IdentificationSet idSet : ExporterUtil.getSelectedIdentificationSets(this.idSets, getDataLevel())) {
			if (jRadioButtonShowProteins.isSelected()) {
				if (jCheckBoxCollapseProteins.isSelected()) {
					num += idSet.getNumDifferentProteinGroups(true);
					if (!jCheckBoxIncludeDecoy.isEnabled() || !jCheckBoxIncludeDecoy.isSelected()) {
						num = num - idSet.getNumDifferentProteinGroupsDecoys();
					}
					// num += idSet.getNumDifferentNonConclusiveProteinGroups();
				} else {
					num += idSet.getTotalNumProteinGroups(true);
					if (!jCheckBoxIncludeDecoy.isEnabled() || !jCheckBoxIncludeDecoy.isSelected()) {
						num = num - idSet.getNumProteinGroupDecoys();
					}
					// num += idSet.getTotalNumNonConclusiveProteinGroups();
				}
			} else {
				if (jCheckBoxCollapsePeptides.isSelected()) {
					num += idSet.getNumDifferentPeptides(true);
					if (!jCheckBoxIncludeDecoy.isEnabled() || !jCheckBoxIncludeDecoy.isSelected()) {
						num = num - idSet.getNumDifferentPeptideDecoys(true);
					}
				} else {
					num += idSet.getTotalNumPeptides();
					if (!jCheckBoxIncludeDecoy.isEnabled() || !jCheckBoxIncludeDecoy.isSelected()) {
						num = num - idSet.getNumPeptideDecoys();
					}
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
	private javax.swing.JCheckBox jCheckBoxIncludeGeneInfo;
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
	private JLabel lblDataLevel;
	private JComboBox dataLevelComboBox;

	// End of variables declaration//GEN-END:variables

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
	public boolean isFDRApplied() {
		if (idSets != null && !idSets.isEmpty() && parentFrame.getFiltersDialog() != null
				&& parentFrame.getFiltersDialog().isFDRFilterDefined())
			return true;
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.proteored.pacom.analysis.exporters.ExporterManager#
	 * isDistinguishModifiedPeptides()
	 */
	@Override
	public boolean isDistinguishModifiedPeptides() {
		return parentFrame.distinguishModifiedPeptides();
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
		return this.parentFrame.getComparisonType();
	}

	@Override
	public DataLevel getDataLevel() {
		Object dataLevelSelected = dataLevelComboBox.getSelectedItem();
		if (dataLevelSelected != null) {
			return (DataLevel) dataLevelSelected;
		}
		return DataLevel.LEVEL0;
	}
}