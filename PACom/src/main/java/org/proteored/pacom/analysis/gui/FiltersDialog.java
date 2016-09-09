/*
 * FiltersDialog.java Created on __DATE__, __TIME__
 */

package org.proteored.pacom.analysis.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker.StateValue;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.cv.Accession;
import org.proteored.miapeapi.cv.ControlVocabularyTerm;
import org.proteored.miapeapi.cv.msi.Score;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.IdentificationItemEnum;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.miapeapi.experiment.model.filters.ComparatorOperator;
import org.proteored.miapeapi.experiment.model.filters.FDRFilter;
import org.proteored.miapeapi.experiment.model.filters.Filter;
import org.proteored.miapeapi.experiment.model.filters.LogicOperator;
import org.proteored.miapeapi.experiment.model.filters.ModificationFilter;
import org.proteored.miapeapi.experiment.model.filters.ModificationFilterItem;
import org.proteored.miapeapi.experiment.model.filters.OccurrenceFilter;
import org.proteored.miapeapi.experiment.model.filters.PeptideLengthFilter;
import org.proteored.miapeapi.experiment.model.filters.PeptideNumberFilter;
import org.proteored.miapeapi.experiment.model.filters.PeptideSequenceFilter;
import org.proteored.miapeapi.experiment.model.filters.PeptidesForMRMFilter;
import org.proteored.miapeapi.experiment.model.filters.ProteinACCFilter;
import org.proteored.miapeapi.experiment.model.filters.ScoreFilter;
import org.proteored.miapeapi.experiment.model.sort.SortingManager;
import org.proteored.miapeapi.experiment.model.sort.SortingParameters;
import org.proteored.pacom.analysis.gui.components.MyJTable;
import org.proteored.pacom.analysis.gui.tasks.FilterTask;
import org.proteored.pacom.gui.ImageManager;
import org.proteored.pacom.gui.MainFrame;
import org.proteored.pacom.gui.tasks.OntologyLoaderTask;

/**
 * 
 * @author __USER__
 */
public class FiltersDialog extends javax.swing.JDialog implements
		PropertyChangeListener {
	private static final Logger log = Logger
			.getLogger("log4j.logger.org.proteored");
	private static final String REPLICATES = "replicates";
	private static final String TIMES = "times";
	private static FiltersDialog instance;
	private ChartManagerFrame parent;
	private ExperimentList experimentList;
	// SCORE FILTER TABLE
	private final int SCORE_NAME_COLUMN = 0;
	private final int PROTEIN_PEPTIDE_COLUMN = 1;
	private final int OPERATOR_COLUMN = 2;
	private final int THRESHOLD__COLUMN = 3;

	// FDR FILTER TABLE
	private final int EXPERIMENT_NAME_COLUMN = 0;
	private final int REPLICATE_NAME_COLUMN = 1;
	private final int PEPTIDE_SCORE_COLUMN = 2;
	private final int FDR_THRESHOLD_COLUMN = 3;
	private final List<Filter> previousFilters = new ArrayList<Filter>();
	private File fastaFile;
	private ProteinACCFilter proteinACCFilter;
	private FilterTask filterTask;
	private static final String MESSAGE_SEPARATOR = ":";

	public static final int FDRFILTER_INDEX = 0;
	public static final int SCOREFILTER_INDEX = 1;
	public static final int OCCURRENCEFILTER_INDEX = 2;
	public static final int MODIFICATIONFILTER_INDEX = 3;
	public static final int PROTEINACCFILTER_INDEX = 4;
	public static final int PEPTIDENUMBERFILTER_INDEX = 5;
	public static final int PEPTIDELENGTHFILTER_INDEX = 6;
	public static final int PEPTIDEFORMRMFILTER_INDEX = 7;

	public static final int PEPTIDESEQUENCEFILTER_INDEX = 8;

	// modification filters
	private final List<List<JComponent>> modificationFilterControls = new ArrayList<List<JComponent>>();
	private List<String> modifications;
	private PeptideSequenceFilter peptideSequenceFilter;
	private JButton addButton;
	private JButton removeButton;

	/**
	 * Creates new form FiltersDialog
	 * 
	 * @param experimentList
	 */
	private FiltersDialog(ChartManagerFrame parent,
			ExperimentList experimentList) {
		super(parent, true);
		this.parent = parent;
		initComponents();
		initProteinACCListListener();
		initPeptideSequenceListListener();

		this.experimentList = experimentList;
		initialize();

		jTextFieldRegexp
				.setToolTipText("<html>Set here a Regular Expression. Examples: <ul><li>.*REVERSED.*</li><li>.*_rev</li></lu></html>");
		// set icon image
		setIconImage(ImageManager
				.getImageIcon(ImageManager.PROTEORED_MIAPE_API).getImage());

	}

	private void initialize() {
		initializeFDRScoreNamesTable();
		initializeScoreNameComboBox();
		initializeScoreNameTable();
		initializeModificationsComboBox();
		setModificationFilterEnabled(false);
		setFDRFilterEnabled(false);
		setProteinACCFilterEnabled(false);
		setPeptideForMRMFilterEnabled(false);
		setPeptideLengthFilterEnabled(false);
		setPeptideNumberFilterEnabled(false);
		setPeptideSequencesFilterEnabled(false);
		setOccurrenceFilterEnabled(false);
		setScoreFilterEnabled(false);
	}

	public static FiltersDialog getInstance(ChartManagerFrame parent,
			ExperimentList experimentList) {
		if (instance == null) {
			instance = new FiltersDialog(parent, experimentList);
		} else {
			instance.experimentList = experimentList;
			instance.parent = parent;
			instance.initialize();
		}
		return instance;
	}

	public static FiltersDialog getNewInstance(ChartManagerFrame parent,
			ExperimentList experimentList) {

		instance = new FiltersDialog(parent, experimentList);

		instance.experimentList = experimentList;
		instance.parent = parent;
		instance.initialize();

		return instance;
	}

	private void initProteinACCListListener() {
		jTextAreaProteinACCList.getDocument().addDocumentListener(
				new DocumentListener() {
					@Override
					public void changedUpdate(DocumentEvent e) {
						String text;
						try {
							text = e.getDocument().getText(0, e.getLength());
							if (text.contains("\n")) {
								final String[] split = text.split("\n");
								int size = 0;
								for (String string : split) {
									if (!"".equals(string.trim()))
										size++;
								}
								jLabelNumProteinsProteinACCFilter.setText(size
										+ " protein accessions");
							} else {
								jLabelNumProteinsProteinACCFilter
										.setText("0 protein accessions");
							}

						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}

					}

					@Override
					public void insertUpdate(DocumentEvent e) {
						String text;
						try {
							text = e.getDocument().getText(
									0,
									e.getDocument().getEndPosition()
											.getOffset());
							if (text.contains("\n")) {
								final String[] split = text.split("\n");
								jLabelNumProteinsProteinACCFilter
										.setText(split.length
												+ " protein accessions");
							} else {
								jLabelNumProteinsProteinACCFilter
										.setText("0 protein accessions");
							}

						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}
					}

					@Override
					public void removeUpdate(DocumentEvent e) {
						String text;
						try {
							text = e.getDocument().getText(
									0,
									e.getDocument().getEndPosition()
											.getOffset());
							if (text.contains("\n")) {
								final String[] split = text.split("\n");
								jLabelNumProteinsProteinACCFilter
										.setText(split.length
												+ " protein accessions");
							} else {
								jLabelNumProteinsProteinACCFilter
										.setText("0 protein accessions");
							}

						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}
					}
				});

	}

	private void initPeptideSequenceListListener() {
		jTextAreaPeptideSequencesList.getDocument().addDocumentListener(
				new DocumentListener() {
					@Override
					public void changedUpdate(DocumentEvent e) {
						String text;
						try {
							text = e.getDocument().getText(0, e.getLength());
							if (text.contains("\n")) {
								final String[] split = text.split("\n");
								jLabelNumPeptidesPeptideSequenceFilter
										.setText(split.length
												+ " peptide sequences");
							} else {
								jLabelNumPeptidesPeptideSequenceFilter
										.setText("0 peptide sequences");
							}

						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}

					}

					@Override
					public void insertUpdate(DocumentEvent e) {
						String text;
						try {
							text = e.getDocument().getText(0, e.getLength());
							if (text.contains("\n")) {
								final String[] split = text.split("\n");
								jLabelNumPeptidesPeptideSequenceFilter
										.setText(split.length
												+ " peptide sequences");
							} else {
								jLabelNumPeptidesPeptideSequenceFilter
										.setText("0 peptide sequences");
							}

						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}
					}

					@Override
					public void removeUpdate(DocumentEvent e) {
						String text;
						try {
							text = e.getDocument().getText(0, e.getLength());
							if (text.contains("\n")) {
								final String[] split = text.split("\n");
								jLabelNumPeptidesPeptideSequenceFilter
										.setText(split.length
												+ " peptide sequences");
							} else {
								jLabelNumPeptidesPeptideSequenceFilter
										.setText("0 peptide sequences");
							}

						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}
					}
				});

	}

	// @Override
	// public void setVisible(boolean b) {
	// // TODO que se seleccionen modificaciones ya seleccionadas
	// initializeModificationsComboBox();
	// super.setVisible(b);
	// }

	private void initializeModificationsComboBox() {

		// PTM filter:

		modificationFilterControls.clear();
		modificationFilterControls.add(getModificationControls());

		// show modification control in panel
		showModificationControls();

	}

	private void showModificationControls() {
		log.info("Showing Modification filter controls with "
				+ modificationFilterControls.size() + " rows");
		jPanelOccurrenceParameters1.removeAll();
		jPanelOccurrenceParameters1.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 10, 10);
		c.gridy = 0;
		// first row contains "+" and "-" buttons
		removeButton = new JButton("-");
		removeButton.setToolTipText("Remove latest row");
		removeButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if (modificationFilterControls.size() > 1) {
					modificationFilterControls
							.remove(modificationFilterControls.size() - 1);
					if (!modificationFilterControls.isEmpty())
						modificationFilterControls.get(
								modificationFilterControls.size() - 1).remove(
								modificationFilterControls.get(
										modificationFilterControls.size() - 1)
										.size() - 1);
					showModificationControls();
				}
			}
		});
		addButton = new JButton("+");
		addButton.setToolTipText("Add new row");
		addButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				FiltersDialog.this.addNewPTMFilter();
				showModificationControls();
			}
		});
		c.gridx = 0;
		jPanelOccurrenceParameters1.add(addButton, c);
		c.gridx = 1;
		jPanelOccurrenceParameters1.add(removeButton, c);

		c.gridy = 1;
		c.gridx = 0;
		c.gridwidth = 7;
		ModificationFilter modificationFilter = getModificationFilter();
		if (modificationFilter != null)
			jPanelOccurrenceParameters1.add(
					new JLabel(modificationFilter.toString()), c);
		c.gridwidth = 1;
		c.gridy++;
		for (int i = 0; i < modificationFilterControls.size(); i++) {
			List<JComponent> rowControls = modificationFilterControls.get(i);

			// include peptides
			c.gridx = 0;
			JLabel label = new JLabel("Include peptides ");
			jPanelOccurrenceParameters1.add(label, c);

			// containing
			c.gridx = 1;
			jPanelOccurrenceParameters1.add(rowControls.get(0), c);

			// this PTM
			c.gridx = 2;
			JLabel label2 = new JLabel(" this PTM: ");
			jPanelOccurrenceParameters1.add(label2, c);

			// PTM combo
			c.gridx = 3;
			jPanelOccurrenceParameters1.add(rowControls.get(1), c);

			// number
			c.gridx = 4;
			jPanelOccurrenceParameters1.add(rowControls.get(2), c);

			// times
			c.gridx = 5;
			jPanelOccurrenceParameters1.add(new JLabel("times (*)"), c);

			// logic operator
			c.gridx = 6;
			if (rowControls.size() > 3)
				jPanelOccurrenceParameters1.add(rowControls.get(3), c);

			c.gridy++;
		}
		c.gridx = 0;
		c.gridwidth = 6;
		jPanelOccurrenceParameters1
				.add(new JLabel(
						"(*) left empty if you want to filter by a PTM that occurs 1 or more times"),
						c);
		pack();
	}

	private List<String> getModificationsList() {
		if (modifications == null || modifications.isEmpty()) {
			modifications = experimentList
					.getDifferentPeptideModificationNames();
			log.info(modifications.size() + " modifications found");
			// modification combo box
			modifications.add(0, ModificationFilter.NOT_MODIFIED);
			modifications.add(0, "");

		}
		return modifications;
	}

	private List<JComponent> getModificationControls() {
		List<JComponent> filterComponents = new ArrayList<JComponent>();

		// containing - not containing
		final DefaultComboBoxModel aModel = new DefaultComboBoxModel();
		aModel.addElement("containing");
		aModel.addElement("not containing");
		JComboBox comboBoxContaining = new JComboBox();
		comboBoxContaining.setModel(aModel);
		comboBoxContaining.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				showModificationControls();
			}
		});
		filterComponents.add(comboBoxContaining);

		// modification combo box
		final DefaultComboBoxModel aModel2 = new DefaultComboBoxModel(
				getModificationsList().toArray());
		JComboBox comboBoxModifications = new JComboBox();
		comboBoxModifications.setModel(aModel2);
		comboBoxModifications.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				showModificationControls();
			}
		});
		filterComponents.add(comboBoxModifications);
		// number
		final JTextField numberText = new JTextField("", 3);
		numberText.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				showModificationControls();

			}

			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		filterComponents.add(numberText);

		return filterComponents;
	}

	protected void addNewPTMFilter() {
		log.info("Adding new filter from " + modificationFilterControls.size()
				+ " filters");
		// add logical operator to the latest row
		// containing - not containing

		if (modificationFilterControls.size() > 0) {
			final DefaultComboBoxModel aModel3 = new DefaultComboBoxModel();
			aModel3.addElement("");
			aModel3.addElement("AND");
			aModel3.addElement("OR");
			JComboBox comboBoxLogicalOperator = new JComboBox();
			comboBoxLogicalOperator.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					showModificationControls();
				}
			});
			comboBoxLogicalOperator.setModel(aModel3);

			modificationFilterControls.get(
					modificationFilterControls.size() - 1).add(
					comboBoxLogicalOperator);
		}
		List<JComponent> modificationControls = getModificationControls();

		modificationFilterControls.add(modificationControls);
		log.info("There is " + modificationFilterControls.size()
				+ " rows after adding a new row");
	}

	public ModificationFilter getModificationFilter() {
		if (jCheckBoxModificationFilterActivation.isSelected()) {
			ModificationFilter modifFilter = new ModificationFilter(
					MainFrame.getMiapeExtractorSoftware());
			List<JComponent> previousFilterComponent = null;
			for (int i = 0; i < modificationFilterControls.size(); i++) {

				List<JComponent> componentList = modificationFilterControls
						.get(i);

				// containing - not containing
				boolean containing = false;
				JComboBox comboContaining = (JComboBox) componentList.get(0);
				String selection = (String) comboContaining.getSelectedItem();
				if (selection.equals(ModificationFilterItem.CONTAINING))
					containing = true;
				else
					containing = false;

				// modification name
				JComboBox comboModifName = (JComboBox) componentList.get(1);
				String modificationName = (String) comboModifName
						.getSelectedItem();
				if ("".equals(modificationName)) {
					// just delete if it is the second or more
					// if (this.modificationFilterControls.size() > 1)
					// this.modificationFilterControls.remove(i);
					// do not construct a filter with this
					continue;
				}
				// number
				Integer num = null;
				JTextField numberTextField = (JTextField) componentList.get(2);
				String number = numberTextField.getText();

				try {
					num = Integer.valueOf(number);

				} catch (NumberFormatException e) {
					if (!"".equals(number))
						// just delete if it is the second or more
						// if (this.modificationFilterControls.size() > 1)
						// this.modificationFilterControls.remove(i);
						// do not construct a filter with this
						continue;
				}

				// operator
				LogicOperator logicOperator = null;
				if (previousFilterComponent != null) {
					JComboBox operatorCombo = (JComboBox) previousFilterComponent
							.get(3);
					String operatorString = (String) operatorCombo
							.getSelectedItem();
					if (operatorString.equals(LogicOperator.AND.toString())) {
						logicOperator = LogicOperator.AND;
					} else if (operatorString.equals(LogicOperator.OR
							.toString())) {
						logicOperator = LogicOperator.OR;
					} else {
						// just delete if it is the second or more
						// if (this.modificationFilterControls.size() > 1)
						// this.modificationFilterControls.remove(i);
						// do not construct a filter with this
						continue;
					}
				}
				ModificationFilterItem modifFilterItem = new ModificationFilterItem(
						logicOperator, modificationName, containing, num);
				modifFilter.addModificationItem(modifFilterItem);

				previousFilterComponent = componentList;

			}
			if (!modifFilter.getModificationFilterItems().isEmpty())
				return modifFilter;
		}
		return null;
	}

	@Override
	public void dispose() {
		try {
			validateFilters();
			super.dispose();

			if (parent instanceof ChartManagerFrame) {
				if (filtersHaveChanged()) {
					// TODO que se desactive el menu check cuando el filtro está
					// mal (hacer un getFilterXX y si es null, desactivar
					parent.setFDRFilterMenuSelected(jCheckBoxFDRFilterActivation
							.isSelected());
					parent.setOccurrenceFilterMenuSelected(jCheckBoxOccurrenceFilterActivation
							.isSelected());
					parent.setScoreFilterMenuSelected(jCheckBoxScoreFilterActivation
							.isSelected());
					parent.setModificationFilterMenuSelected(jCheckBoxModificationFilterActivation
							.isSelected());
					parent.setProteinACCFilterMenuSelected(jCheckBoxProteinACCFilterActivation
							.isSelected());
					parent.setPeptideNumberFilterMenuSelected(jCheckBoxPeptideNumberFilterActivation
							.isSelected());
					parent.setPeptideLengthFilterMenuSelected(jCheckBoxPeptideLengthFilterActivation
							.isSelected());

					parent.setPeptideSequencesFilterMenuSelected(jCheckBoxPeptideSequencesFilterActivation
							.isSelected());

					parent.setPeptideForMRMFilterMenuSelected(jCheckBoxPeptideForMRMFilterActivation
							.isSelected());

					parent.updateControlStates();
					// DISABLE THIS LINE BECAUSE THE PREVIOUS ONES ARE THROWING
					// AN EVENT THAT PAINTS THE CHART!!!
					parent.startShowingChart();

					parent.clearIdentificationTable();
				}
			}

		} catch (IllegalMiapeArgumentException ex) {

			String message = ex.getMessage();
			if (message.contains(MESSAGE_SEPARATOR)) {
				final String[] split = message.split(MESSAGE_SEPARATOR);
				try {
					int index = Integer.valueOf(split[0]);
					jTabbedPaneFilters.setSelectedIndex(index);
					message = split[1];
				} catch (NumberFormatException e) {
					message = split[0];
				}
			}
			JOptionPane.showMessageDialog(this, message,
					"Error defining filters", JOptionPane.ERROR_MESSAGE);
		}

	}

	private void validateFilters() {

		validateFDRFilters();
		validateScoreFilters();
		validateOccurrenceFilter();
		validateModificationFilter();
		validateProteinACCFilter();
		validatePeptideNumberFilter();
		validatePeptideLengthFilter();
		validatePeptideSequencesFilter();
		validatePeptideForMRMFilter();
	}

	private void validatePeptideForMRMFilter() {
		if (jCheckBoxPeptideForMRMFilterActivation.isSelected()) {
			int min = -1;
			try {

				final String textMin = jTextFieldPeptideLengthMinMRM.getText();
				min = Integer.valueOf(textMin);
				if (min < 1)
					throw new IllegalMiapeArgumentException(
							FiltersDialog.PEPTIDEFORMRMFILTER_INDEX
									+ FiltersDialog.MESSAGE_SEPARATOR
									+ "Fill some positive number in the minimum length of the peptide");
			} catch (NumberFormatException e) {
				if ("".equals(jTextFieldPeptideLengthMinMRM.getText()))
					throw new IllegalMiapeArgumentException(
							FiltersDialog.PEPTIDEFORMRMFILTER_INDEX
									+ FiltersDialog.MESSAGE_SEPARATOR
									+ "Fill some positive number in the minimum length of the peptide");

				throw new IllegalMiapeArgumentException(
						FiltersDialog.PEPTIDEFORMRMFILTER_INDEX
								+ FiltersDialog.MESSAGE_SEPARATOR
								+ "Error in min value. It has to be a positive number");
			}
			try {
				final String textMax = jTextFieldPeptideLengthMaxMRM.getText();
				if (!"".equals(textMax)) {
					int max = Integer.valueOf(textMax);
					if (max < 1)
						throw new IllegalMiapeArgumentException(
								FiltersDialog.PEPTIDEFORMRMFILTER_INDEX
										+ FiltersDialog.MESSAGE_SEPARATOR
										+ "Fill some positive number in the maximum length of the peptide");
					if (max < min)
						throw new IllegalMiapeArgumentException(
								FiltersDialog.PEPTIDEFORMRMFILTER_INDEX
										+ FiltersDialog.MESSAGE_SEPARATOR
										+ "Maximum length of the peptide cannot be lower than the minimum length.");
				}

			} catch (NumberFormatException e) {
				throw new IllegalMiapeArgumentException(
						FiltersDialog.PEPTIDEFORMRMFILTER_INDEX
								+ FiltersDialog.MESSAGE_SEPARATOR
								+ "Error in max value. It has to be a positive number");
			}
		}
	}

	private void validatePeptideSequencesFilter() {
		if (jCheckBoxPeptideSequencesFilterActivation.isSelected()) {
			if ("".equals(jTextAreaPeptideSequencesList.getText()))
				throw new IllegalMiapeArgumentException(
						FiltersDialog.PEPTIDESEQUENCEFILTER_INDEX
								+ FiltersDialog.MESSAGE_SEPARATOR
								+ "Paste at least one peptide sequence");
		}

	}

	private void validatePeptideLengthFilter() {
		if (jCheckBoxPeptideLengthFilterActivation.isSelected()) {
			int min = -1;
			try {
				min = Integer.valueOf(jTextFieldPeptideLengthMin.getText());
				if (min < 0)
					throw new NumberFormatException(
							"The minimum number of peptide length has to be a positive number");
				if (!"".equals(jTextFieldPeptideLengthMax.getText())) {
					Integer max = Integer.valueOf(jTextFieldPeptideLengthMax
							.getText());
					if (max < 0)
						throw new NumberFormatException(
								"The maximum number of peptide length has to be a positive number");
					if (min > max)
						throw new IllegalMiapeArgumentException(
								FiltersDialog.PEPTIDELENGTHFILTER_INDEX
										+ FiltersDialog.MESSAGE_SEPARATOR
										+ "The maximum length of the peptide cannot be lower than the minimum.");
				}
			} catch (NumberFormatException e) {
				if ("".equals(jTextFieldPeptideLengthMin.getText()))
					throw new IllegalMiapeArgumentException(
							FiltersDialog.PEPTIDELENGTHFILTER_INDEX
									+ FiltersDialog.MESSAGE_SEPARATOR
									+ "Filll some positive number in the minimum length of the peptide");
				throw new IllegalMiapeArgumentException(
						FiltersDialog.PEPTIDELENGTHFILTER_INDEX
								+ FiltersDialog.MESSAGE_SEPARATOR
								+ e.getMessage());
			}
		}

	}

	private void validateModificationFilter() {
		if (jCheckBoxModificationFilterActivation.isSelected()) {
			if (!modificationFilterControls.isEmpty()) {
				for (List<JComponent> controls : modificationFilterControls) {
					// 1: name od the modification
					JComboBox comboName = (JComboBox) controls.get(1);
					String name = (String) comboName.getSelectedItem();
					if ("".equals(name))
						throw new IllegalMiapeArgumentException(
								FiltersDialog.MODIFICATIONFILTER_INDEX
										+ FiltersDialog.MESSAGE_SEPARATOR
										+ "You must select a modification name from the combo box");
					// number of times
					JTextField times = (JTextField) controls.get(2);
					String numberText = times.getText();

					try {
						Integer num = Integer.valueOf(numberText);
						if (num == 0)
							throw new NumberFormatException();
					} catch (NumberFormatException e) {
						if (!"".equals(numberText))
							throw new IllegalMiapeArgumentException(
									FiltersDialog.MODIFICATIONFILTER_INDEX
											+ FiltersDialog.MESSAGE_SEPARATOR
											+ "You must enter a valid positive number in the text field or left it empty.");
					}
					// logic operator
					if (controls.size() > 3) {
						JComboBox comboOperator = (JComboBox) controls.get(3);
						String selection = (String) comboOperator
								.getSelectedItem();
						if ("".equals(selection))
							throw new IllegalMiapeArgumentException(
									FiltersDialog.MODIFICATIONFILTER_INDEX
											+ FiltersDialog.MESSAGE_SEPARATOR
											+ "You must select a logic operator (AND or OR) from the combo box");
					}
				}
			}
		}

	}

	private void validateOccurrenceFilter() {
		if (jCheckBoxOccurrenceFilterActivation.isSelected()) {

			if (jTextFieldOccurrenceThreshold.getText().equals(""))
				throw new IllegalMiapeArgumentException(OCCURRENCEFILTER_INDEX
						+ MESSAGE_SEPARATOR
						+ "Define some value for the occurrence filter");
			else {
				try {
					Integer valueOf = Integer
							.valueOf(jTextFieldOccurrenceThreshold.getText());
					if (valueOf < 0)
						throw new IllegalMiapeArgumentException(
								OCCURRENCEFILTER_INDEX
										+ MESSAGE_SEPARATOR
										+ "Occurrence threshold must be a positive integer");
				} catch (NumberFormatException e) {
					throw new IllegalMiapeArgumentException(
							OCCURRENCEFILTER_INDEX
									+ MESSAGE_SEPARATOR
									+ "Occurrence threshold must be a positive integer");
				}
			}
			final String selectedItem = (String) jComboBoxReplicatesOrTimes
					.getSelectedItem();
			if ("".equals(selectedItem))
				throw new IllegalMiapeArgumentException(OCCURRENCEFILTER_INDEX
						+ MESSAGE_SEPARATOR + "Select 'replicates' or 'times'");
		}

	}

	private void validateScoreFilters() {
		if (jCheckBoxScoreFilterActivation.isSelected()) {
			if (jTableScoreFilter.getRowCount() == 0)
				throw new IllegalMiapeArgumentException(SCOREFILTER_INDEX
						+ MESSAGE_SEPARATOR
						+ "A score threshold must be defined");
			Vector dataVector = ((DefaultTableModel) jTableScoreFilter
					.getModel()).getDataVector();
			for (Object object : dataVector) {
				Vector rowVector = (Vector) object;

				for (Object object2 : rowVector) {
					if (object2 == null || object2.toString().equals(""))
						throw new IllegalMiapeArgumentException(
								SCOREFILTER_INDEX
										+ MESSAGE_SEPARATOR
										+ "Some value need to be selected in the table of score thresholds");
				}
				try {
					Float valueOf = Float.valueOf((String) rowVector
							.get(THRESHOLD__COLUMN));
				} catch (NumberFormatException e) {
					throw new IllegalMiapeArgumentException(SCOREFILTER_INDEX
							+ MESSAGE_SEPARATOR + "Threhold value "
							+ rowVector.get(THRESHOLD__COLUMN)
							+ " is not a valid float number");
				}
			}
		}
	}

	private void validateFDRFilters() {
		if (jCheckBoxFDRFilterActivation.isSelected()) {
			if (!jRadioButtonRegexp.isSelected()
					&& !jRadioButtonPrefix.isSelected())
				throw new IllegalMiapeArgumentException(FDRFILTER_INDEX
						+ MESSAGE_SEPARATOR
						+ "Regular expression or prefix must be defined");
			if (jRadioButtonPrefix.isSelected()
					&& jTextFieldPrefix.getText().equals(""))
				throw new IllegalMiapeArgumentException(
						"Prefix must be defined in FDR parameters");

			if (jRadioButtonRegexp.isSelected()) {
				if (jTextFieldRegexp.getText().equals(""))
					throw new IllegalMiapeArgumentException(
							FDRFILTER_INDEX
									+ MESSAGE_SEPARATOR
									+ "Regular expression must be defined in FDR parameters");
				else {
					try {
						Pattern.compile(jTextFieldRegexp.getText());
					} catch (PatternSyntaxException e) {
						throw new IllegalMiapeArgumentException(FDRFILTER_INDEX
								+ MESSAGE_SEPARATOR
								+ "Invalid regular expression: "
								+ e.getMessage());
					}
				}
			}
			Vector dataVector = ((DefaultTableModel) jTableFDRFilter.getModel())
					.getDataVector();
			for (Object object : dataVector) {
				Vector rowVector = (Vector) object;

				try {
					String string = String.valueOf(rowVector
							.get(FDR_THRESHOLD_COLUMN));
					float fdr = Float.valueOf(string);

					if (fdr < 0.0 || fdr > 100.0)
						throw new IllegalMiapeArgumentException(
								FDRFILTER_INDEX
										+ MESSAGE_SEPARATOR
										+ "Peptide FDR value should be a number between 0 and 100");
				} catch (NumberFormatException e) {
					throw new IllegalMiapeArgumentException(
							FDRFILTER_INDEX
									+ MESSAGE_SEPARATOR
									+ "Peptide FDR value should be a number between 0 and 100");
				}
			}

		}
	}

	private void validateProteinACCFilter() {
		if (jCheckBoxProteinACCFilterActivation.isSelected()) {
			if (jRadioButtonSelectFastaFile.isSelected()) {
				if (fastaFile == null || !fastaFile.exists())
					throw new IllegalMiapeArgumentException(
							PROTEINACCFILTER_INDEX + MESSAGE_SEPARATOR
									+ "Select a fasta file");
			} else if (jRadioButtonPasteProteinACCList.isSelected()) {
				String proteinList = jTextAreaProteinACCList.getText();
				if ("".equals(proteinList)) {
					throw new IllegalMiapeArgumentException(
							PROTEINACCFILTER_INDEX + MESSAGE_SEPARATOR
									+ "Paste a protein accession list");
				}
			}

		}
		// even if it is not selected, if the textarea doesn't contain any
		// protein, update the counter to the number
		String proteinList = jTextAreaProteinACCList.getText();
		if ("".equals(proteinList)) {
			jLabelNumProteinsProteinACCFilter.setText("0 protein accessions");
		} else {
			if (proteinList.contains("\n")) {
				final String[] split = proteinList.split("\n");
				jLabelNumProteinsProteinACCFilter.setText(split.length
						+ " protein accessions");
			}
		}
	}

	private void validatePeptideNumberFilter() {
		if (jCheckBoxPeptideNumberFilterActivation.isSelected()) {
			try {
				Integer min = Integer
						.valueOf(jTextFieldPeptideNumber.getText());
				if (min < 0)
					throw new NumberFormatException(
							PEPTIDENUMBERFILTER_INDEX
									+ MESSAGE_SEPARATOR
									+ "The minimum number of peptides has to be a positive number");
			} catch (NumberFormatException e) {
				if (e.getMessage().startsWith("For input string"))
					throw new IllegalMiapeArgumentException(
							PEPTIDENUMBERFILTER_INDEX
									+ MESSAGE_SEPARATOR
									+ "Minimum number of peptides is empty. Fill a number in the text field or deactivate the Peptide Number Filter");
				throw new IllegalMiapeArgumentException(
						PEPTIDENUMBERFILTER_INDEX + MESSAGE_SEPARATOR
								+ e.getMessage());
			}
		}
	}

	private void initializeScoreNameTable() {
		HashMap<Integer, List<DefaultCellEditor>> hash = new HashMap<Integer, List<DefaultCellEditor>>();

		DefaultTableModel model = new DefaultTableModel();
		model.addColumn("Score Name");
		model.addColumn("Protein/Peptide");
		model.addColumn("Operator");
		model.addColumn("Threshold value");
		jTableScoreFilter.setModel(model);

		List<DefaultCellEditor> editors = new ArrayList<DefaultCellEditor>();
		ComparatorOperator[] items = { ComparatorOperator.LESS,
				ComparatorOperator.LESS_OR_EQUAL, ComparatorOperator.MORE,
				ComparatorOperator.MORE_OR_EQUAL };
		editors.add(new DefaultCellEditor(new JComboBox(items)));
		hash.put(OPERATOR_COLUMN, editors);

		// List<DefaultCellEditor> editors2 = new
		// ArrayList<DefaultCellEditor>();
		// IdentificationItem[] items2 = { IdentificationItem.PROTEIN,
		// IdentificationItem.PEPTIDE };
		// editors2.add(new DefaultCellEditor(new JComboBox(items2)));
		// hash.put(PROTEIN_PEPTIDE_COLUMN, editors2);

		List<DefaultCellEditor> editors3 = new ArrayList<DefaultCellEditor>();
		DefaultCellEditor e = new DefaultCellEditor(new JTextField(10));
		e.setClickCountToStart(1);
		editors3.add(e);
		hash.put(THRESHOLD__COLUMN, editors3);

		jTableScoreFilter.setEditors(hash);

	}

	private void initializeScoreNameComboBox() {
		List<String> proteinScoreNames = experimentList.getProteinScoreNames();
		jComboBoxProteinScoreNames.setModel(new DefaultComboBoxModel());
		jComboBoxProteinScoreNames.addItem("-");
		for (String proteinScore : proteinScoreNames) {
			if (!"".equals(proteinScore))
				jComboBoxProteinScoreNames.addItem(proteinScore);
		}

		List<String> peptideScoreNames = experimentList.getPeptideScoreNames();
		jComboBoxPeptideScoreNames.setModel(new DefaultComboBoxModel());
		jComboBoxPeptideScoreNames.addItem("-");
		for (String peptideScore : peptideScoreNames) {
			if (!"".equals(peptideScore))
				jComboBoxPeptideScoreNames.addItem(peptideScore);
		}
	}

	private void initializeFDRScoreNamesTable() {
		HashMap<Integer, List<DefaultCellEditor>> hash = new HashMap<Integer, List<DefaultCellEditor>>();

		DefaultTableModel model = new DefaultTableModel();
		jTableFDRFilter.setModel(model);
		model.addColumn("Level 1 (experiment)");
		model.addColumn("Level 2 (fraction/band/replicate)");
		model.addColumn("Peptide Score");
		model.addColumn("Threshold (%)");
		List<DefaultCellEditor> fdrThresholdEditorlist = new ArrayList<DefaultCellEditor>();
		for (Experiment experiment : experimentList
				.getNextLevelIdentificationSetList()) {
			for (Replicate replicate : experiment
					.getNextLevelIdentificationSetList()) {
				List<String> scoreNames = replicate.getPeptideScoreNames();

				List<String> peptideScoreNames = new ArrayList<String>();
				peptideScoreNames.addAll(scoreNames);
				if (peptideScoreNames == null || peptideScoreNames.isEmpty())
					continue;
				Collections.sort(peptideScoreNames);
				if (!"".equals(peptideScoreNames.get(0)))
					peptideScoreNames.add(0, "");

				Vector rowData = new Vector();
				rowData.add(experiment.getName());
				rowData.add(replicate.getName());

				ControlVocabularyTerm mascotScoreTerm = Score.getInstance(
						OntologyLoaderTask.getCvManager())
						.getCVTermByAccession(new Accession("MS:1001171"));

				if (peptideScoreNames.size() > 1) {
					final JComboBox comboBox = new JComboBox(
							peptideScoreNames.toArray());
					if (peptideScoreNames.size() == 3
							&& peptideScoreNames.get(2).equalsIgnoreCase(
									mascotScoreTerm.getPreferredName())) {
						comboBox.setSelectedIndex(2);
						rowData.add(peptideScoreNames.get(2));
					} else if (peptideScoreNames.size() == 2
							&& peptideScoreNames.get(1).equalsIgnoreCase(
									mascotScoreTerm.getPreferredName())) {
						comboBox.setSelectedIndex(1);
						rowData.add(peptideScoreNames.get(1));
					} else {
						comboBox.setSelectedIndex(1);
						rowData.add(peptideScoreNames.get(1));
					}
					if (hash.containsKey(PEPTIDE_SCORE_COLUMN)) {
						hash.get(PEPTIDE_SCORE_COLUMN).add(
								new DefaultCellEditor(comboBox));
					} else {
						List<DefaultCellEditor> list = new ArrayList<DefaultCellEditor>();
						list.add(new DefaultCellEditor(comboBox));
						hash.put(PEPTIDE_SCORE_COLUMN, list);
					}
				}
				rowData.add(null);

				fdrThresholdEditorlist.add(new DefaultCellEditor(
						new JTextField()));

				model.addRow(rowData);
			}
		}
		hash.put(FDR_THRESHOLD_COLUMN, fdrThresholdEditorlist);
		jTableFDRFilter.setEditors(hash);
	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		buttonGroup1 = new javax.swing.ButtonGroup();
		buttonGroup2 = new javax.swing.ButtonGroup();
		jButtonFinish = new javax.swing.JButton();
		jTabbedPaneFilters = new javax.swing.JTabbedPane();
		jPanelFDRFilter = new javax.swing.JPanel();
		jCheckBoxFDRFilterActivation = new javax.swing.JCheckBox();
		jPanelFDRParameters = new javax.swing.JPanel();
		jCheckBoxConcatenated = new javax.swing.JCheckBox();
		jRadioButtonPrefix = new javax.swing.JRadioButton();
		jRadioButtonRegexp = new javax.swing.JRadioButton();
		jTextFieldRegexp = new javax.swing.JTextField();
		jTextFieldPrefix = new javax.swing.JTextField();
		jTextFieldPeptideFDRThreshold = new javax.swing.JTextField();
		jPanelScoreFDRParameters = new javax.swing.JPanel();
		jScrollPane1 = new javax.swing.JScrollPane();
		jTableFDRFilter = new MyJTable();
		jLabelPeptideFDRThreshold = new javax.swing.JLabel();
		jButtonAddThresholdToReplicates = new javax.swing.JButton();
		jComboBoxPeptideOrProteinFDR = new javax.swing.JComboBox();
		jPanelScoreFilter = new javax.swing.JPanel();
		jCheckBoxScoreFilterActivation = new javax.swing.JCheckBox();
		jPanelScoreParameters = new javax.swing.JPanel();
		jComboBoxProteinScoreNames = new javax.swing.JComboBox();
		jLabel1 = new javax.swing.JLabel();
		jButtonAddScoreToList = new javax.swing.JButton();
		jScrollPane2 = new javax.swing.JScrollPane();
		jTableScoreFilter = new MyJTable();
		jComboBoxPeptideScoreNames = new javax.swing.JComboBox();
		jLabel2 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		jButtonRemoveScoreToList = new javax.swing.JButton();
		jPanelOccurrenceFilter = new javax.swing.JPanel();
		jCheckBoxOccurrenceFilterActivation = new javax.swing.JCheckBox();
		jPanelOccurrenceParameters = new javax.swing.JPanel();
		jLabel4 = new javax.swing.JLabel();
		jComboBoxProteinOrPeptide = new javax.swing.JComboBox();
		jLabel5 = new javax.swing.JLabel();
		jTextFieldOccurrenceThreshold = new javax.swing.JTextField();
		jCheckBoxDistinguishModPeptides = new javax.swing.JCheckBox();
		jComboBoxReplicatesOrTimes = new javax.swing.JComboBox();
		jPanelModificationFilter = new javax.swing.JPanel();
		jCheckBoxModificationFilterActivation = new javax.swing.JCheckBox();
		jPanelOccurrenceParameters1 = new javax.swing.JPanel();
		jPanelProteinAccessionFilter = new javax.swing.JPanel();
		jCheckBoxProteinACCFilterActivation = new javax.swing.JCheckBox();
		jPanelProteinACCList = new javax.swing.JPanel();
		jLabelProteinACCList = new javax.swing.JLabel();
		jRadioButtonSelectFastaFile = new javax.swing.JRadioButton();
		jRadioButtonPasteProteinACCList = new javax.swing.JRadioButton();
		jLabelFastaFilePath = new javax.swing.JLabel();
		jScrollPaneProteinACCList = new javax.swing.JScrollPane();
		jTextAreaProteinACCList = new javax.swing.JTextArea();
		jLabelNumProteinsProteinACCFilter = new javax.swing.JLabel();
		jPanelPeptideNumber = new javax.swing.JPanel();
		jCheckBoxPeptideNumberFilterActivation = new javax.swing.JCheckBox();
		jPanelProteinACCList1 = new javax.swing.JPanel();
		jLabelPeptideNumber = new javax.swing.JLabel();
		jLabelNumPeptidesTextField = new javax.swing.JLabel();
		jTextFieldPeptideNumber = new javax.swing.JTextField();
		jPanelPeptideLength = new javax.swing.JPanel();
		jCheckBoxPeptideLengthFilterActivation = new javax.swing.JCheckBox();
		jPanelProteinACCList2 = new javax.swing.JPanel();
		jLabelPeptideLength = new javax.swing.JLabel();
		jLabelPeptideLengthMin = new javax.swing.JLabel();
		jTextFieldPeptideLengthMin = new javax.swing.JTextField();
		jLabelPeptideLengthMin1 = new javax.swing.JLabel();
		jTextFieldPeptideLengthMax = new javax.swing.JTextField();
		jLabelPeptideLengthMin2 = new javax.swing.JLabel();
		jLabelPeptideLengthMin3 = new javax.swing.JLabel();
		jPanelPeptideForMRM = new javax.swing.JPanel();
		jCheckBoxPeptideForMRMFilterActivation = new javax.swing.JCheckBox();
		jPanelProteinACCList3 = new javax.swing.JPanel();
		jLabelPeptideMRM1 = new javax.swing.JLabel();
		jLabelPeptideMRMFrom = new javax.swing.JLabel();
		jTextFieldPeptideLengthMinMRM = new javax.swing.JTextField();
		jLabelPeptideMRMTo = new javax.swing.JLabel();
		jTextFieldPeptideLengthMaxMRM = new javax.swing.JTextField();
		jLabelPeptidMRMAminoacids = new javax.swing.JLabel();
		jLabelPeptideMRMExplanation = new javax.swing.JLabel();
		jCheckBoxIgnoreMissedCleavages = new javax.swing.JCheckBox();
		jCheckBoxIgnoreM = new javax.swing.JCheckBox();
		jCheckBoxIgnoreW = new javax.swing.JCheckBox();
		jCheckBoxIgnoreQ = new javax.swing.JCheckBox();
		jCheckBoxRequireUniquePeptides = new javax.swing.JCheckBox();
		jPanelPeptideSequencesFilter = new javax.swing.JPanel();
		jCheckBoxPeptideSequencesFilterActivation = new javax.swing.JCheckBox();
		jPanelProteinACCList4 = new javax.swing.JPanel();
		jScrollPanePeptideSequencesList = new javax.swing.JScrollPane();
		jTextAreaPeptideSequencesList = new javax.swing.JTextArea();
		jLabelNumPeptidesPeptideSequenceFilter = new javax.swing.JLabel();
		jCheckBoxDistinguishModificatedPeptidesSequenceFilter = new javax.swing.JCheckBox();
		jLabelPeptideSequenceFilterExplanation = new javax.swing.JLabel();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Filters definition");

		jButtonFinish.setText("Finish");
		jButtonFinish.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonFinishActionPerformed(evt);
			}
		});

		jCheckBoxFDRFilterActivation.setText("Activated");
		jCheckBoxFDRFilterActivation
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jCheckBoxFDRFilterActivationActionPerformed(evt);
					}
				});

		jPanelFDRParameters.setBorder(javax.swing.BorderFactory
				.createTitledBorder(
						javax.swing.BorderFactory.createEtchedBorder(),
						"FDR parameters"));

		jCheckBoxConcatenated.setSelected(true);
		jCheckBoxConcatenated.setText("FW + DC concatenated database");
		jCheckBoxConcatenated
				.setToolTipText("<html>Select this option is search was dome<br>\nagainst a database with the decoy entries in the same database</html>");
		jCheckBoxConcatenated.setEnabled(false);

		buttonGroup1.add(jRadioButtonPrefix);
		jRadioButtonPrefix.setText("decoy prefix");
		jRadioButtonPrefix.setEnabled(false);
		jRadioButtonPrefix.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jRadioButtonPrefixMouseClicked(evt);
			}
		});

		buttonGroup1.add(jRadioButtonRegexp);
		jRadioButtonRegexp.setText("decoy regular expression");
		jRadioButtonRegexp.setEnabled(false);
		jRadioButtonRegexp.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jRadioButtonRegexpMouseClicked(evt);
			}
		});

		jTextFieldRegexp.setEnabled(false);

		jTextFieldPrefix.setEnabled(false);

		jTextFieldPeptideFDRThreshold
				.setToolTipText("Enter a number between 0 and 100");
		jTextFieldPeptideFDRThreshold.setEnabled(false);

		jPanelScoreFDRParameters.setBorder(javax.swing.BorderFactory
				.createTitledBorder(
						javax.swing.BorderFactory.createEtchedBorder(),
						"Peptide scores that will determine the FDR"));

		jTableFDRFilter.setModel(new javax.swing.table.DefaultTableModel(
				new Object[][] { { null, null, null, null },
						{ null, null, null, null }, { null, null, null, null },
						{ null, null, null, null } }, new String[] { "Title 1",
						"Title 2", "Title 3", "Title 4" }));
		jTableFDRFilter.setCellSelectionEnabled(true);
		jTableFDRFilter.setEnabled(false);
		jTableFDRFilter.setFocusable(false);
		jScrollPane1.setViewportView(jTableFDRFilter);

		javax.swing.GroupLayout jPanelScoreFDRParametersLayout = new javax.swing.GroupLayout(
				jPanelScoreFDRParameters);
		jPanelScoreFDRParameters.setLayout(jPanelScoreFDRParametersLayout);
		jPanelScoreFDRParametersLayout
				.setHorizontalGroup(jPanelScoreFDRParametersLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelScoreFDRParametersLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(
												jScrollPane1,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));
		jPanelScoreFDRParametersLayout
				.setVerticalGroup(jPanelScoreFDRParametersLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelScoreFDRParametersLayout
										.createSequentialGroup()
										.addComponent(
												jScrollPane1,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												162, Short.MAX_VALUE)
										.addContainerGap()));

		jLabelPeptideFDRThreshold.setText("FDR Threshold (%):");
		jLabelPeptideFDRThreshold
				.setToolTipText("FDR threshold at peptide level");
		jLabelPeptideFDRThreshold.setEnabled(false);

		jButtonAddThresholdToReplicates.setText(">>");
		jButtonAddThresholdToReplicates
				.setToolTipText("<html>Add this threshold value to all replicates.</html>");
		jButtonAddThresholdToReplicates.setEnabled(false);
		jButtonAddThresholdToReplicates
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jButtonAddThresholdToReplicatesActionPerformed(evt);
					}
				});

		jComboBoxPeptideOrProteinFDR
				.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
						"FDR at peptide level", "FDR at protein level",
						"FDR at PSM level" }));
		jComboBoxPeptideOrProteinFDR
				.setToolTipText("<html><ul><li>\nIf FDR is applied at peptide level, redundancy of PSMs will be removed to\n<br>\nobtain the best match for each 'sequence+mods'. This list is sorted by<br>\nthe score, and then an FDR will be applied. Proteins having no peptides<br> after the filter, will be also discarded.</li>\n<li>\nIf FDR is applied at protein level, the proteins will be sorted taking<br>\ntheir best peptide. Then the FDR will be applied over the proteins.<br> Peptides that have no associated proteins after the filter will be also discarded.</li></ul>\n</html>");
		jComboBoxPeptideOrProteinFDR.setEnabled(false);

		javax.swing.GroupLayout jPanelFDRParametersLayout = new javax.swing.GroupLayout(
				jPanelFDRParameters);
		jPanelFDRParameters.setLayout(jPanelFDRParametersLayout);
		jPanelFDRParametersLayout
				.setHorizontalGroup(jPanelFDRParametersLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelFDRParametersLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanelFDRParametersLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jCheckBoxConcatenated)
														.addGroup(
																jPanelFDRParametersLayout
																		.createSequentialGroup()
																		.addComponent(
																				jRadioButtonPrefix)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																				82,
																				Short.MAX_VALUE)
																		.addComponent(
																				jTextFieldPrefix,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				73,
																				javax.swing.GroupLayout.PREFERRED_SIZE))
														.addGroup(
																jPanelFDRParametersLayout
																		.createSequentialGroup()
																		.addComponent(
																				jRadioButtonRegexp)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jTextFieldRegexp,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				86,
																				Short.MAX_VALUE))
														.addGroup(
																jPanelFDRParametersLayout
																		.createSequentialGroup()
																		.addComponent(
																				jLabelPeptideFDRThreshold)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jTextFieldPeptideFDRThreshold,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				39,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jButtonAddThresholdToReplicates))
														.addComponent(
																jComboBoxPeptideOrProteinFDR,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jPanelScoreFDRParameters,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(40, 40, 40)));
		jPanelFDRParametersLayout
				.setVerticalGroup(jPanelFDRParametersLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelFDRParametersLayout
										.createSequentialGroup()
										.addGroup(
												jPanelFDRParametersLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																jPanelFDRParametersLayout
																		.createSequentialGroup()
																		.addComponent(
																				jCheckBoxConcatenated)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(
																				jPanelFDRParametersLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.BASELINE)
																						.addComponent(
																								jRadioButtonPrefix)
																						.addComponent(
																								jTextFieldPrefix,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(
																				jPanelFDRParametersLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.BASELINE)
																						.addComponent(
																								jRadioButtonRegexp)
																						.addComponent(
																								jTextFieldRegexp,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addGap(21,
																				21,
																				21)
																		.addComponent(
																				jComboBoxPeptideOrProteinFDR,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(
																				jPanelFDRParametersLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.BASELINE)
																						.addComponent(
																								jLabelPeptideFDRThreshold)
																						.addComponent(
																								jTextFieldPeptideFDRThreshold,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								jButtonAddThresholdToReplicates)))
														.addComponent(
																jPanelScoreFDRParameters,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE))
										.addContainerGap()));

		javax.swing.GroupLayout jPanelFDRFilterLayout = new javax.swing.GroupLayout(
				jPanelFDRFilter);
		jPanelFDRFilter.setLayout(jPanelFDRFilterLayout);
		jPanelFDRFilterLayout
				.setHorizontalGroup(jPanelFDRFilterLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelFDRFilterLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanelFDRFilterLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jPanelFDRParameters,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jCheckBoxFDRFilterActivation,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																88,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));
		jPanelFDRFilterLayout
				.setVerticalGroup(jPanelFDRFilterLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelFDRFilterLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(
												jCheckBoxFDRFilterActivation)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jPanelFDRParameters,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addContainerGap()));

		jTabbedPaneFilters.addTab("FDR filter", jPanelFDRFilter);

		jCheckBoxScoreFilterActivation.setText("Activated");
		jCheckBoxScoreFilterActivation
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jCheckBoxScoreFilterActivationActionPerformed(evt);
					}
				});

		jPanelScoreParameters.setBorder(javax.swing.BorderFactory
				.createTitledBorder(
						javax.swing.BorderFactory.createEtchedBorder(),
						"Score Threshold parameters"));

		jComboBoxProteinScoreNames.setEnabled(false);
		jComboBoxProteinScoreNames
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(java.awt.event.ItemEvent evt) {
						jComboBoxProteinScoreNamesItemStateChanged(evt);
					}
				});

		jLabel1.setText("Select an Score from the ComboBox below and click on \">>\" button to define a threshold with that score");
		jLabel1.setEnabled(false);

		jButtonAddScoreToList.setText(">>");
		jButtonAddScoreToList.setEnabled(false);
		jButtonAddScoreToList
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jButtonAddScoreToListActionPerformed(evt);
					}
				});

		jTableScoreFilter.setModel(new javax.swing.table.DefaultTableModel(
				new Object[][] { { null, null, null, null },
						{ null, null, null, null }, { null, null, null, null },
						{ null, null, null, null } }, new String[] { "Title 1",
						"Title 2", "Title 3", "Title 4" }));
		jTableScoreFilter.setEnabled(false);
		jTableScoreFilter.setFocusable(false);
		jScrollPane2.setViewportView(jTableScoreFilter);

		jComboBoxPeptideScoreNames.setEnabled(false);
		jComboBoxPeptideScoreNames
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(java.awt.event.ItemEvent evt) {
						jComboBoxPeptideScoreNamesItemStateChanged(evt);
					}
				});

		jLabel2.setText("Protein Scores:");
		jLabel2.setEnabled(false);

		jLabel3.setText("Peptide Scores:");
		jLabel3.setEnabled(false);

		jButtonRemoveScoreToList.setText("<<");
		jButtonRemoveScoreToList.setEnabled(false);
		jButtonRemoveScoreToList
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jButtonRemoveScoreToListActionPerformed(evt);
					}
				});

		javax.swing.GroupLayout jPanelScoreParametersLayout = new javax.swing.GroupLayout(
				jPanelScoreParameters);
		jPanelScoreParameters.setLayout(jPanelScoreParametersLayout);
		jPanelScoreParametersLayout
				.setHorizontalGroup(jPanelScoreParametersLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelScoreParametersLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanelScoreParametersLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(jLabel1)
														.addGroup(
																jPanelScoreParametersLayout
																		.createSequentialGroup()
																		.addGroup(
																				jPanelScoreParametersLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								jLabel2)
																						.addComponent(
																								jLabel3))
																		.addGap(5,
																				5,
																				5)
																		.addGroup(
																				jPanelScoreParametersLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.TRAILING,
																								false)
																						.addGroup(
																								jPanelScoreParametersLayout
																										.createSequentialGroup()
																										.addComponent(
																												jButtonRemoveScoreToList)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												Short.MAX_VALUE)
																										.addComponent(
																												jButtonAddScoreToList))
																						.addGroup(
																								jPanelScoreParametersLayout
																										.createParallelGroup(
																												javax.swing.GroupLayout.Alignment.LEADING)
																										.addComponent(
																												jComboBoxProteinScoreNames,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												119,
																												javax.swing.GroupLayout.PREFERRED_SIZE)
																										.addComponent(
																												jComboBoxPeptideScoreNames,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												119,
																												javax.swing.GroupLayout.PREFERRED_SIZE)))
																		.addGap(36,
																				36,
																				36)
																		.addComponent(
																				jScrollPane2,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.PREFERRED_SIZE)))
										.addContainerGap(40, Short.MAX_VALUE)));
		jPanelScoreParametersLayout
				.setVerticalGroup(jPanelScoreParametersLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelScoreParametersLayout
										.createSequentialGroup()
										.addComponent(jLabel1)
										.addGroup(
												jPanelScoreParametersLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																jPanelScoreParametersLayout
																		.createSequentialGroup()
																		.addGap(7,
																				7,
																				7)
																		.addGroup(
																				jPanelScoreParametersLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.BASELINE)
																						.addComponent(
																								jLabel2)
																						.addComponent(
																								jComboBoxProteinScoreNames,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(
																				jPanelScoreParametersLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.BASELINE)
																						.addComponent(
																								jLabel3)
																						.addComponent(
																								jComboBoxPeptideScoreNames,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(
																				jPanelScoreParametersLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.BASELINE)
																						.addComponent(
																								jButtonAddScoreToList)
																						.addComponent(
																								jButtonRemoveScoreToList)))
														.addGroup(
																jPanelScoreParametersLayout
																		.createSequentialGroup()
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jScrollPane2,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				176,
																				Short.MAX_VALUE)))
										.addContainerGap()));

		javax.swing.GroupLayout jPanelScoreFilterLayout = new javax.swing.GroupLayout(
				jPanelScoreFilter);
		jPanelScoreFilter.setLayout(jPanelScoreFilterLayout);
		jPanelScoreFilterLayout
				.setHorizontalGroup(jPanelScoreFilterLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelScoreFilterLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanelScoreFilterLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jPanelScoreParameters,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																jCheckBoxScoreFilterActivation))
										.addContainerGap()));
		jPanelScoreFilterLayout
				.setVerticalGroup(jPanelScoreFilterLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelScoreFilterLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(
												jCheckBoxScoreFilterActivation)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jPanelScoreParameters,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addContainerGap()));

		jTabbedPaneFilters.addTab("Score filter", jPanelScoreFilter);

		jCheckBoxOccurrenceFilterActivation.setText("Activated");
		jCheckBoxOccurrenceFilterActivation
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jCheckBoxOccurrenceFilterActivationActionPerformed(evt);
					}
				});

		jPanelOccurrenceParameters.setBorder(javax.swing.BorderFactory
				.createTitledBorder(
						javax.swing.BorderFactory.createEtchedBorder(),
						"Occurrence filter parameters"));

		jLabel4.setText("Just include");
		jLabel4.setEnabled(false);

		jComboBoxProteinOrPeptide
				.setModel(new javax.swing.DefaultComboBoxModel(
						IdentificationItemEnum.values()));
		jComboBoxProteinOrPeptide.setEnabled(false);
		jComboBoxProteinOrPeptide
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(java.awt.event.ItemEvent evt) {
						jComboBoxProteinOrPeptideItemStateChanged(evt);
					}
				});

		jLabel5.setText("that are present in at least");
		jLabel5.setEnabled(false);

		jTextFieldOccurrenceThreshold.setEnabled(false);

		jCheckBoxDistinguishModPeptides
				.setText("distinguish modificated peptides");
		jCheckBoxDistinguishModPeptides.setEnabled(false);

		jComboBoxReplicatesOrTimes
				.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
						"", "replicates", "times" }));
		jComboBoxReplicatesOrTimes.setEnabled(false);
		jComboBoxReplicatesOrTimes
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(java.awt.event.ItemEvent evt) {
						jComboBoxReplicatesOrTimesItemStateChanged(evt);
					}
				});

		javax.swing.GroupLayout jPanelOccurrenceParametersLayout = new javax.swing.GroupLayout(
				jPanelOccurrenceParameters);
		jPanelOccurrenceParameters.setLayout(jPanelOccurrenceParametersLayout);
		jPanelOccurrenceParametersLayout
				.setHorizontalGroup(jPanelOccurrenceParametersLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelOccurrenceParametersLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(jLabel4)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jComboBoxProteinOrPeptide,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												66,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jLabel5)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jTextFieldOccurrenceThreshold,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												26,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jComboBoxReplicatesOrTimes,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addComponent(
												jCheckBoxDistinguishModPeptides)
										.addContainerGap()));
		jPanelOccurrenceParametersLayout
				.setVerticalGroup(jPanelOccurrenceParametersLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelOccurrenceParametersLayout
										.createSequentialGroup()
										.addGroup(
												jPanelOccurrenceParametersLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel4)
														.addComponent(jLabel5)
														.addComponent(
																jTextFieldOccurrenceThreshold,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jComboBoxReplicatesOrTimes,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jComboBoxProteinOrPeptide,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jCheckBoxDistinguishModPeptides))
										.addContainerGap(13, Short.MAX_VALUE)));

		javax.swing.GroupLayout jPanelOccurrenceFilterLayout = new javax.swing.GroupLayout(
				jPanelOccurrenceFilter);
		jPanelOccurrenceFilter.setLayout(jPanelOccurrenceFilterLayout);
		jPanelOccurrenceFilterLayout
				.setHorizontalGroup(jPanelOccurrenceFilterLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelOccurrenceFilterLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanelOccurrenceFilterLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																jPanelOccurrenceFilterLayout
																		.createSequentialGroup()
																		.addComponent(
																				jCheckBoxOccurrenceFilterActivation)
																		.addContainerGap(
																				693,
																				Short.MAX_VALUE))
														.addGroup(
																jPanelOccurrenceFilterLayout
																		.createSequentialGroup()
																		.addComponent(
																				jPanelOccurrenceParameters,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				Short.MAX_VALUE)
																		.addGap(159,
																				159,
																				159)))));
		jPanelOccurrenceFilterLayout
				.setVerticalGroup(jPanelOccurrenceFilterLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelOccurrenceFilterLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(
												jCheckBoxOccurrenceFilterActivation)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jPanelOccurrenceParameters,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addContainerGap(187, Short.MAX_VALUE)));

		jTabbedPaneFilters.addTab("Occurrence filter", jPanelOccurrenceFilter);

		jCheckBoxModificationFilterActivation.setText("Activated");
		jCheckBoxModificationFilterActivation
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jCheckBoxModificationFilterActivationActionPerformed(evt);
					}
				});

		jPanelOccurrenceParameters1.setBorder(javax.swing.BorderFactory
				.createTitledBorder(
						javax.swing.BorderFactory.createEtchedBorder(),
						"Modification filter parameters"));

		javax.swing.GroupLayout jPanelOccurrenceParameters1Layout = new javax.swing.GroupLayout(
				jPanelOccurrenceParameters1);
		jPanelOccurrenceParameters1
				.setLayout(jPanelOccurrenceParameters1Layout);
		jPanelOccurrenceParameters1Layout
				.setHorizontalGroup(jPanelOccurrenceParameters1Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGap(0, 423, Short.MAX_VALUE));
		jPanelOccurrenceParameters1Layout
				.setVerticalGroup(jPanelOccurrenceParameters1Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGap(0, 29, Short.MAX_VALUE));

		javax.swing.GroupLayout jPanelModificationFilterLayout = new javax.swing.GroupLayout(
				jPanelModificationFilter);
		jPanelModificationFilter.setLayout(jPanelModificationFilterLayout);
		jPanelModificationFilterLayout
				.setHorizontalGroup(jPanelModificationFilterLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelModificationFilterLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanelModificationFilterLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jCheckBoxModificationFilterActivation)
														.addComponent(
																jPanelOccurrenceParameters1,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap(333, Short.MAX_VALUE)));
		jPanelModificationFilterLayout
				.setVerticalGroup(jPanelModificationFilterLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelModificationFilterLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(
												jCheckBoxModificationFilterActivation)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jPanelOccurrenceParameters1,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addContainerGap(196, Short.MAX_VALUE)));

		jTabbedPaneFilters.addTab("PTM filter", jPanelModificationFilter);

		jCheckBoxProteinACCFilterActivation.setText("Activated");
		jCheckBoxProteinACCFilterActivation
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jCheckBoxProteinACCFilterActivationActionPerformed(evt);
					}
				});

		jPanelProteinACCList.setBorder(javax.swing.BorderFactory
				.createTitledBorder(
						javax.swing.BorderFactory.createEtchedBorder(),
						"Define the protein ACC list"));

		jLabelProteinACCList
				.setText("Select a fasta file with the proteins of interest or paste the protein accession list below:");
		jLabelProteinACCList.setEnabled(false);

		buttonGroup2.add(jRadioButtonSelectFastaFile);
		jRadioButtonSelectFastaFile.setText("Select fasta file:");
		jRadioButtonSelectFastaFile.setEnabled(false);
		jRadioButtonSelectFastaFile
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jRadioButtonSelectFastaFileActionPerformed(evt);
					}
				});

		buttonGroup2.add(jRadioButtonPasteProteinACCList);
		jRadioButtonPasteProteinACCList.setText("Paste protein accessions");
		jRadioButtonPasteProteinACCList.setEnabled(false);
		jRadioButtonPasteProteinACCList
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jRadioButtonPasteProteinACCListActionPerformed(evt);
					}
				});

		jLabelFastaFilePath.setText("(file not selected)");
		jLabelFastaFilePath.setEnabled(false);

		jScrollPaneProteinACCList.setEnabled(false);

		jTextAreaProteinACCList.setColumns(20);
		jTextAreaProteinACCList.setLineWrap(true);
		jTextAreaProteinACCList.setRows(5);
		jTextAreaProteinACCList.setToolTipText("Paste protein accession here");
		jTextAreaProteinACCList.setWrapStyleWord(true);
		jTextAreaProteinACCList.setEnabled(false);
		jScrollPaneProteinACCList.setViewportView(jTextAreaProteinACCList);

		jLabelNumProteinsProteinACCFilter.setText("0 protein accessions");
		jLabelNumProteinsProteinACCFilter.setEnabled(false);

		javax.swing.GroupLayout jPanelProteinACCListLayout = new javax.swing.GroupLayout(
				jPanelProteinACCList);
		jPanelProteinACCList.setLayout(jPanelProteinACCListLayout);
		jPanelProteinACCListLayout
				.setHorizontalGroup(jPanelProteinACCListLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelProteinACCListLayout
										.createSequentialGroup()
										.addGroup(
												jPanelProteinACCListLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																jPanelProteinACCListLayout
																		.createSequentialGroup()
																		.addContainerGap()
																		.addGroup(
																				jPanelProteinACCListLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								jRadioButtonPasteProteinACCList)
																						.addComponent(
																								jLabelProteinACCList)
																						.addGroup(
																								jPanelProteinACCListLayout
																										.createSequentialGroup()
																										.addComponent(
																												jRadioButtonSelectFastaFile)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																												27,
																												Short.MAX_VALUE)
																										.addComponent(
																												jLabelFastaFilePath,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												586,
																												javax.swing.GroupLayout.PREFERRED_SIZE))))
														.addGroup(
																jPanelProteinACCListLayout
																		.createSequentialGroup()
																		.addGap(33,
																				33,
																				33)
																		.addComponent(
																				jScrollPaneProteinACCList,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				279,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																		.addComponent(
																				jLabelNumProteinsProteinACCFilter,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				225,
																				javax.swing.GroupLayout.PREFERRED_SIZE)))
										.addContainerGap()));
		jPanelProteinACCListLayout
				.setVerticalGroup(jPanelProteinACCListLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelProteinACCListLayout
										.createSequentialGroup()
										.addComponent(jLabelProteinACCList)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanelProteinACCListLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jRadioButtonSelectFastaFile)
														.addComponent(
																jLabelFastaFilePath))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jRadioButtonPasteProteinACCList)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanelProteinACCListLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jScrollPaneProteinACCList,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																127,
																Short.MAX_VALUE)
														.addComponent(
																jLabelNumProteinsProteinACCFilter))
										.addContainerGap()));

		javax.swing.GroupLayout jPanelProteinAccessionFilterLayout = new javax.swing.GroupLayout(
				jPanelProteinAccessionFilter);
		jPanelProteinAccessionFilter
				.setLayout(jPanelProteinAccessionFilterLayout);
		jPanelProteinAccessionFilterLayout
				.setHorizontalGroup(jPanelProteinAccessionFilterLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelProteinAccessionFilterLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanelProteinAccessionFilterLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jPanelProteinACCList,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																jCheckBoxProteinACCFilterActivation,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																88,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap()));
		jPanelProteinAccessionFilterLayout
				.setVerticalGroup(jPanelProteinAccessionFilterLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelProteinAccessionFilterLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(
												jCheckBoxProteinACCFilterActivation)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jPanelProteinACCList,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addContainerGap()));

		jTabbedPaneFilters.addTab("Protein ACC filter",
				jPanelProteinAccessionFilter);

		jCheckBoxPeptideNumberFilterActivation.setText("Activated");
		jCheckBoxPeptideNumberFilterActivation
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jCheckBoxPeptideNumberFilterActivationActionPerformed(evt);
					}
				});

		jPanelProteinACCList1.setBorder(javax.swing.BorderFactory
				.createTitledBorder(
						javax.swing.BorderFactory.createEtchedBorder(),
						"Peptide Number Filter"));

		jLabelPeptideNumber
				.setText("Filter out proteins that don't have a minimum number of peptides");
		jLabelPeptideNumber.setEnabled(false);

		jLabelNumPeptidesTextField.setText("Minimum number of peptides:");
		jLabelNumPeptidesTextField.setEnabled(false);

		jTextFieldPeptideNumber.setEnabled(false);

		javax.swing.GroupLayout jPanelProteinACCList1Layout = new javax.swing.GroupLayout(
				jPanelProteinACCList1);
		jPanelProteinACCList1.setLayout(jPanelProteinACCList1Layout);
		jPanelProteinACCList1Layout
				.setHorizontalGroup(jPanelProteinACCList1Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelProteinACCList1Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanelProteinACCList1Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																jPanelProteinACCList1Layout
																		.createSequentialGroup()
																		.addComponent(
																				jLabelNumPeptidesTextField)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jTextFieldPeptideNumber,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				31,
																				javax.swing.GroupLayout.PREFERRED_SIZE))
														.addComponent(
																jLabelPeptideNumber))
										.addContainerGap(384, Short.MAX_VALUE)));
		jPanelProteinACCList1Layout
				.setVerticalGroup(jPanelProteinACCList1Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelProteinACCList1Layout
										.createSequentialGroup()
										.addComponent(jLabelPeptideNumber)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanelProteinACCList1Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabelNumPeptidesTextField)
														.addComponent(
																jTextFieldPeptideNumber,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap(167, Short.MAX_VALUE)));

		javax.swing.GroupLayout jPanelPeptideNumberLayout = new javax.swing.GroupLayout(
				jPanelPeptideNumber);
		jPanelPeptideNumber.setLayout(jPanelPeptideNumberLayout);
		jPanelPeptideNumberLayout
				.setHorizontalGroup(jPanelPeptideNumberLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelPeptideNumberLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanelPeptideNumberLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jPanelProteinACCList1,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																jCheckBoxPeptideNumberFilterActivation,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																88,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap()));
		jPanelPeptideNumberLayout
				.setVerticalGroup(jPanelPeptideNumberLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelPeptideNumberLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(
												jCheckBoxPeptideNumberFilterActivation)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jPanelProteinACCList1,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addContainerGap()));

		jTabbedPaneFilters.addTab("Peptide number filter", jPanelPeptideNumber);

		jCheckBoxPeptideLengthFilterActivation.setText("Activated");
		jCheckBoxPeptideLengthFilterActivation
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jCheckBoxPeptideLengthFilterActivationActionPerformed(evt);
					}
				});

		jPanelProteinACCList2.setBorder(javax.swing.BorderFactory
				.createTitledBorder(
						javax.swing.BorderFactory.createEtchedBorder(),
						"Peptide Length Filter"));

		jLabelPeptideLength
				.setText("Just to include peptide sequences between these limits:");
		jLabelPeptideLength.setEnabled(false);

		jLabelPeptideLengthMin.setText("From");
		jLabelPeptideLengthMin.setEnabled(false);

		jTextFieldPeptideLengthMin.setEnabled(false);

		jLabelPeptideLengthMin1.setText("to");
		jLabelPeptideLengthMin1.setEnabled(false);

		jTextFieldPeptideLengthMax.setEnabled(false);

		jLabelPeptideLengthMin2.setText("aminoacids");
		jLabelPeptideLengthMin2.setEnabled(false);

		jLabelPeptideLengthMin3
				.setText("(i.e. from 6 to 'empty' means to filter out peptides with less than 6 AA) ");
		jLabelPeptideLengthMin3.setEnabled(false);

		javax.swing.GroupLayout jPanelProteinACCList2Layout = new javax.swing.GroupLayout(
				jPanelProteinACCList2);
		jPanelProteinACCList2.setLayout(jPanelProteinACCList2Layout);
		jPanelProteinACCList2Layout
				.setHorizontalGroup(jPanelProteinACCList2Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelProteinACCList2Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanelProteinACCList2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																jPanelProteinACCList2Layout
																		.createSequentialGroup()
																		.addComponent(
																				jLabelPeptideLengthMin)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jTextFieldPeptideLengthMin,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				31,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jLabelPeptideLengthMin1)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jTextFieldPeptideLengthMax,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				31,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jLabelPeptideLengthMin2)
																		.addGap(28,
																				28,
																				28)
																		.addComponent(
																				jLabelPeptideLengthMin3))
														.addComponent(
																jLabelPeptideLength))
										.addContainerGap(150, Short.MAX_VALUE)));
		jPanelProteinACCList2Layout
				.setVerticalGroup(jPanelProteinACCList2Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelProteinACCList2Layout
										.createSequentialGroup()
										.addComponent(jLabelPeptideLength)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanelProteinACCList2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabelPeptideLengthMin)
														.addComponent(
																jTextFieldPeptideLengthMin,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabelPeptideLengthMin1)
														.addComponent(
																jTextFieldPeptideLengthMax,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabelPeptideLengthMin2)
														.addComponent(
																jLabelPeptideLengthMin3))
										.addContainerGap(167, Short.MAX_VALUE)));

		javax.swing.GroupLayout jPanelPeptideLengthLayout = new javax.swing.GroupLayout(
				jPanelPeptideLength);
		jPanelPeptideLength.setLayout(jPanelPeptideLengthLayout);
		jPanelPeptideLengthLayout
				.setHorizontalGroup(jPanelPeptideLengthLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelPeptideLengthLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanelPeptideLengthLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jPanelProteinACCList2,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																jCheckBoxPeptideLengthFilterActivation,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																88,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap()));
		jPanelPeptideLengthLayout
				.setVerticalGroup(jPanelPeptideLengthLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelPeptideLengthLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(
												jCheckBoxPeptideLengthFilterActivation)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jPanelProteinACCList2,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addContainerGap()));

		jTabbedPaneFilters.addTab("Peptide length filter", jPanelPeptideLength);

		jCheckBoxPeptideForMRMFilterActivation.setText("Activated");
		jCheckBoxPeptideForMRMFilterActivation
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jCheckBoxPeptideForMRMFilterActivationActionPerformed(evt);
					}
				});

		jPanelProteinACCList3.setBorder(javax.swing.BorderFactory
				.createTitledBorder(
						javax.swing.BorderFactory.createEtchedBorder(),
						"Peptides for MRM analysis Filter"));

		jLabelPeptideMRM1
				.setText("Filter out peptides that do not complain with the following rules:");
		jLabelPeptideMRM1.setEnabled(false);

		jLabelPeptideMRMFrom.setText("From");
		jLabelPeptideMRMFrom.setEnabled(false);

		jTextFieldPeptideLengthMinMRM.setEnabled(false);

		jLabelPeptideMRMTo.setText("to");
		jLabelPeptideMRMTo.setEnabled(false);

		jTextFieldPeptideLengthMaxMRM.setEnabled(false);

		jLabelPeptidMRMAminoacids.setText("aminoacids");
		jLabelPeptidMRMAminoacids.setEnabled(false);

		jLabelPeptideMRMExplanation
				.setText("(i.e. from 6 to 'empty' means to filter out peptides with less than 6 AA) ");
		jLabelPeptideMRMExplanation.setEnabled(false);

		jCheckBoxIgnoreMissedCleavages.setSelected(true);
		jCheckBoxIgnoreMissedCleavages.setText("ignore missed-cleavages");
		jCheckBoxIgnoreMissedCleavages
				.setToolTipText("Ignore peptides containing missed-cleavages");
		jCheckBoxIgnoreMissedCleavages.setEnabled(false);

		jCheckBoxIgnoreM.setSelected(true);
		jCheckBoxIgnoreM.setText("ignore \"M\"");
		jCheckBoxIgnoreM.setToolTipText("Ignore peptides containing \"M\"");
		jCheckBoxIgnoreM.setEnabled(false);

		jCheckBoxIgnoreW.setSelected(true);
		jCheckBoxIgnoreW.setText("ignore \"W\"");
		jCheckBoxIgnoreW.setToolTipText("Ignore peptides containing \"W\"");
		jCheckBoxIgnoreW.setEnabled(false);

		jCheckBoxIgnoreQ.setSelected(true);
		jCheckBoxIgnoreQ.setText("ignore \"Q\" at first position");
		jCheckBoxIgnoreQ
				.setToolTipText("Ignore peptides containing a \"Q\" at the first aminoacid.");
		jCheckBoxIgnoreQ.setEnabled(false);

		jCheckBoxRequireUniquePeptides.setSelected(true);
		jCheckBoxRequireUniquePeptides.setText("require unique peptides");
		jCheckBoxRequireUniquePeptides
				.setToolTipText("A unique peptide is a peptide that has not been asigned to more than one protein.");
		jCheckBoxRequireUniquePeptides.setEnabled(false);

		javax.swing.GroupLayout jPanelProteinACCList3Layout = new javax.swing.GroupLayout(
				jPanelProteinACCList3);
		jPanelProteinACCList3.setLayout(jPanelProteinACCList3Layout);
		jPanelProteinACCList3Layout
				.setHorizontalGroup(jPanelProteinACCList3Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelProteinACCList3Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanelProteinACCList3Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jCheckBoxRequireUniquePeptides)
														.addComponent(
																jCheckBoxIgnoreQ)
														.addComponent(
																jCheckBoxIgnoreW)
														.addComponent(
																jCheckBoxIgnoreM)
														.addGroup(
																jPanelProteinACCList3Layout
																		.createSequentialGroup()
																		.addComponent(
																				jLabelPeptideMRMFrom)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jTextFieldPeptideLengthMinMRM,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				31,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jLabelPeptideMRMTo)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jTextFieldPeptideLengthMaxMRM,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				31,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jLabelPeptidMRMAminoacids)
																		.addGap(28,
																				28,
																				28)
																		.addComponent(
																				jLabelPeptideMRMExplanation))
														.addComponent(
																jLabelPeptideMRM1)
														.addComponent(
																jCheckBoxIgnoreMissedCleavages))
										.addContainerGap(150, Short.MAX_VALUE)));
		jPanelProteinACCList3Layout
				.setVerticalGroup(jPanelProteinACCList3Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelProteinACCList3Layout
										.createSequentialGroup()
										.addComponent(jLabelPeptideMRM1)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanelProteinACCList3Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabelPeptideMRMFrom)
														.addComponent(
																jTextFieldPeptideLengthMinMRM,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabelPeptideMRMTo)
														.addComponent(
																jTextFieldPeptideLengthMaxMRM,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabelPeptidMRMAminoacids)
														.addComponent(
																jLabelPeptideMRMExplanation))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jCheckBoxIgnoreMissedCleavages)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jCheckBoxIgnoreM)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jCheckBoxIgnoreW)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jCheckBoxIgnoreQ)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jCheckBoxRequireUniquePeptides)
										.addContainerGap(39, Short.MAX_VALUE)));

		javax.swing.GroupLayout jPanelPeptideForMRMLayout = new javax.swing.GroupLayout(
				jPanelPeptideForMRM);
		jPanelPeptideForMRM.setLayout(jPanelPeptideForMRMLayout);
		jPanelPeptideForMRMLayout
				.setHorizontalGroup(jPanelPeptideForMRMLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelPeptideForMRMLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanelPeptideForMRMLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jPanelProteinACCList3,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																jCheckBoxPeptideForMRMFilterActivation,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																88,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap()));
		jPanelPeptideForMRMLayout
				.setVerticalGroup(jPanelPeptideForMRMLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelPeptideForMRMLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(
												jCheckBoxPeptideForMRMFilterActivation)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jPanelProteinACCList3,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addContainerGap()));

		jTabbedPaneFilters.addTab("Peptides for MRM filter",
				jPanelPeptideForMRM);

		jCheckBoxPeptideSequencesFilterActivation.setText("Activated");
		jCheckBoxPeptideSequencesFilterActivation
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jCheckBoxPeptideSequencesFilterActivationActionPerformed(evt);
					}
				});

		jPanelProteinACCList4.setBorder(javax.swing.BorderFactory
				.createTitledBorder(
						javax.swing.BorderFactory.createEtchedBorder(),
						"Paste the peptide sequence list"));

		jScrollPanePeptideSequencesList.setEnabled(false);

		jTextAreaPeptideSequencesList.setColumns(20);
		jTextAreaPeptideSequencesList.setLineWrap(true);
		jTextAreaPeptideSequencesList.setRows(5);
		jTextAreaPeptideSequencesList
				.setToolTipText("Paste protein accession here");
		jTextAreaPeptideSequencesList.setWrapStyleWord(true);
		jTextAreaPeptideSequencesList.setEnabled(false);
		jScrollPanePeptideSequencesList
				.setViewportView(jTextAreaPeptideSequencesList);

		jLabelNumPeptidesPeptideSequenceFilter.setText("0 peptide sequences");
		jLabelNumPeptidesPeptideSequenceFilter.setEnabled(false);

		jCheckBoxDistinguishModificatedPeptidesSequenceFilter
				.setText("Distinguish modificated peptides");
		jCheckBoxDistinguishModificatedPeptidesSequenceFilter.setEnabled(false);

		jLabelPeptideSequenceFilterExplanation
				.setText("(if selected, you can paste modificated sequences like: SNRT(+79.97)YPDR)");
		jLabelPeptideSequenceFilterExplanation.setEnabled(false);

		javax.swing.GroupLayout jPanelProteinACCList4Layout = new javax.swing.GroupLayout(
				jPanelProteinACCList4);
		jPanelProteinACCList4.setLayout(jPanelProteinACCList4Layout);
		jPanelProteinACCList4Layout
				.setHorizontalGroup(jPanelProteinACCList4Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelProteinACCList4Layout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(
												jScrollPanePeptideSequencesList,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												279,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGroup(
												jPanelProteinACCList4Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																jPanelProteinACCList4Layout
																		.createSequentialGroup()
																		.addGap(33,
																				33,
																				33)
																		.addComponent(
																				jLabelNumPeptidesPeptideSequenceFilter,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				225,
																				javax.swing.GroupLayout.PREFERRED_SIZE))
														.addGroup(
																jPanelProteinACCList4Layout
																		.createSequentialGroup()
																		.addGap(18,
																				18,
																				18)
																		.addGroup(
																				jPanelProteinACCList4Layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								jLabelPeptideSequenceFilterExplanation)
																						.addComponent(
																								jCheckBoxDistinguishModificatedPeptidesSequenceFilter))))
										.addContainerGap()));
		jPanelProteinACCList4Layout
				.setVerticalGroup(jPanelProteinACCList4Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelProteinACCList4Layout
										.createSequentialGroup()
										.addGroup(
												jPanelProteinACCList4Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																jPanelProteinACCList4Layout
																		.createSequentialGroup()
																		.addComponent(
																				jCheckBoxDistinguishModificatedPeptidesSequenceFilter)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jLabelPeptideSequenceFilterExplanation)
																		.addGap(28,
																				28,
																				28)
																		.addComponent(
																				jLabelNumPeptidesPeptideSequenceFilter))
														.addComponent(
																jScrollPanePeptideSequencesList,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																199,
																Short.MAX_VALUE))
										.addContainerGap()));

		javax.swing.GroupLayout jPanelPeptideSequencesFilterLayout = new javax.swing.GroupLayout(
				jPanelPeptideSequencesFilter);
		jPanelPeptideSequencesFilter
				.setLayout(jPanelPeptideSequencesFilterLayout);
		jPanelPeptideSequencesFilterLayout
				.setHorizontalGroup(jPanelPeptideSequencesFilterLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelPeptideSequencesFilterLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanelPeptideSequencesFilterLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jPanelProteinACCList4,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jCheckBoxPeptideSequencesFilterActivation,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																88,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap(42, Short.MAX_VALUE)));
		jPanelPeptideSequencesFilterLayout
				.setVerticalGroup(jPanelPeptideSequencesFilterLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelPeptideSequencesFilterLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(
												jCheckBoxPeptideSequencesFilterActivation)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jPanelProteinACCList4,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addContainerGap()));

		jTabbedPaneFilters.addTab("Peptide Sequences Filter",
				jPanelPeptideSequencesFilter);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addGroup(
														layout.createSequentialGroup()
																.addGap(352,
																		352,
																		352)
																.addComponent(
																		jButtonFinish))
												.addComponent(
														jTabbedPaneFilters,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														785,
														javax.swing.GroupLayout.PREFERRED_SIZE))
								.addContainerGap(
										javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)));
		layout.setVerticalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						javax.swing.GroupLayout.Alignment.TRAILING,
						layout.createSequentialGroup()
								.addComponent(jTabbedPaneFilters,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										334, Short.MAX_VALUE)
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jButtonFinish).addContainerGap()));

		java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit()
				.getScreenSize();
		setBounds((screenSize.width - 813) / 2, (screenSize.height - 417) / 2,
				813, 417);
	}// </editor-fold>
		// GEN-END:initComponents

	private void jCheckBoxPeptideSequencesFilterActivationActionPerformed(
			java.awt.event.ActionEvent evt) {
		enablePeptideSequencesFilter(jCheckBoxPeptideSequencesFilterActivation
				.isSelected());
	}

	private void jCheckBoxPeptideForMRMFilterActivationActionPerformed(
			java.awt.event.ActionEvent evt) {
		enablePeptideForMRMFilter(jCheckBoxPeptideForMRMFilterActivation
				.isSelected());
	}

	private void jCheckBoxPeptideLengthFilterActivationActionPerformed(
			java.awt.event.ActionEvent evt) {
		enablePeptideLengthFilter(jCheckBoxPeptideLengthFilterActivation
				.isSelected());
	}

	private void jRadioButtonSelectFastaFileActionPerformed(
			java.awt.event.ActionEvent evt) {
		if (jRadioButtonSelectFastaFile.isSelected()) {
			proteinACCFilter = null;
			jLabelFastaFilePath.setEnabled(jRadioButtonSelectFastaFile
					.isSelected());
			JFileChooser fileChooser = new JFileChooser();
			final int showOpenDialog = fileChooser.showOpenDialog(this);
			if (showOpenDialog == JFileChooser.APPROVE_OPTION) {
				fastaFile = fileChooser.getSelectedFile();
				jLabelFastaFilePath.setText(fastaFile.getAbsolutePath());
			}

		}
	}

	private void jCheckBoxPeptideNumberFilterActivationActionPerformed(
			java.awt.event.ActionEvent evt) {
		enablePeptideNumberFilter(jCheckBoxPeptideNumberFilterActivation
				.isSelected());
	}

	private void jButtonAddThresholdToReplicatesActionPerformed(
			java.awt.event.ActionEvent evt) {
		Float threshold = null;
		final String thresholdText = jTextFieldPeptideFDRThreshold.getText();
		try {
			threshold = Float.valueOf(thresholdText);
			if (threshold < 0 || threshold > 100)
				throw new NumberFormatException();
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "<html>Threshold value '"
					+ thresholdText + "' is not valid.<br>"
					+ "A number between 0.0 and 100 is required.</html>",
					"Error in threhold value", JOptionPane.ERROR_MESSAGE);
			return;
		}
		for (int row = 0; row < jTableFDRFilter.getModel().getRowCount(); row++) {

			((DefaultTableModel) jTableFDRFilter.getModel()).setValueAt(
					threshold, row, FDR_THRESHOLD_COLUMN);
		}

	}

	private void jRadioButtonPasteProteinACCListActionPerformed(
			java.awt.event.ActionEvent evt) {
		proteinACCFilter = null;
		jTextAreaProteinACCList.setEnabled(jRadioButtonPasteProteinACCList
				.isSelected());
		jLabelFastaFilePath.setEnabled(!jRadioButtonPasteProteinACCList
				.isSelected());
	}

	private void jCheckBoxProteinACCFilterActivationActionPerformed(
			java.awt.event.ActionEvent evt) {
		proteinACCFilter = null;
		enableProteinACCFilter(jCheckBoxProteinACCFilterActivation.isSelected());
	}

	private void enablePeptideNumberFilter(boolean selected) {
		jTextFieldPeptideNumber.setEnabled(selected);
		jLabelNumPeptidesTextField.setEnabled(selected);
		jLabelPeptideNumber.setEnabled(selected);
		parent.jCheckBoxMenuItemPeptideNumberFilter.setSelected(selected);
	}

	private void enableProteinACCFilter(boolean selected) {
		jLabelProteinACCList.setEnabled(selected);
		jRadioButtonPasteProteinACCList.setEnabled(selected);
		jRadioButtonSelectFastaFile.setEnabled(selected);
		jLabelFastaFilePath.setEnabled(selected);
		parent.jCheckBoxMenuItemProteinACCFilter.setSelected(selected);
		jLabelNumProteinsProteinACCFilter.setEnabled(selected);
		jTextAreaProteinACCList.setEnabled(selected);
	}

	private void jCheckBoxModificationFilterActivationActionPerformed(
			java.awt.event.ActionEvent evt) {
		enableModificationFilter(jCheckBoxModificationFilterActivation
				.isSelected());
	}

	private void enableModificationFilter(boolean selected) {
		for (Object rowControls : modificationFilterControls) {
			List<JComponent> components = (List<JComponent>) rowControls;
			for (JComponent jComponent : components) {
				jComponent.setEnabled(selected);
			}
		}

		parent.jCheckBoxMenuItemModificationFilter.setSelected(selected);
		removeButton.setEnabled(selected);
		addButton.setEnabled(selected);
	}

	private void jButtonRemoveScoreToListActionPerformed(
			java.awt.event.ActionEvent evt) {
		final int selectedRow = jTableScoreFilter.getSelectedRow();
		if (selectedRow >= 0)
			((DefaultTableModel) jTableScoreFilter.getModel())
					.removeRow(selectedRow);
	}

	private void jComboBoxReplicatesOrTimesItemStateChanged(
			java.awt.event.ItemEvent evt) {
		final String string1 = "that are present in at least";
		final String string2 = "that are present at least";
		if (jComboBoxReplicatesOrTimes.getSelectedItem().toString()
				.equals("replicates"))
			jLabel5.setText(string1);
		else if (jComboBoxReplicatesOrTimes.getSelectedItem().toString()
				.equals("times"))
			jLabel5.setText(string2);
	}

	protected void jButtonFinishActionPerformed(ActionEvent evt) {
		dispose();

	}

	protected void jCheckBoxOccurrenceFilterActivationActionPerformed(
			ActionEvent evt) {
		enableOccurrenceFilters(jCheckBoxOccurrenceFilterActivation
				.isSelected());
	}

	protected void jComboBoxProteinScoreNamesItemStateChanged(ItemEvent evt) {
		if (jComboBoxPeptideScoreNames.getItemCount() > 0)
			jComboBoxPeptideScoreNames.setSelectedIndex(0);

	}

	protected void jButtonAddScoreToListActionPerformed(ActionEvent evt) {
		DefaultTableModel model = (DefaultTableModel) jTableScoreFilter
				.getModel();
		if (jComboBoxPeptideScoreNames.getSelectedIndex() > 0) {
			String peptideScore = (String) jComboBoxPeptideScoreNames
					.getSelectedItem();
			if (!alreadyIsInTable(peptideScore, IdentificationItemEnum.PEPTIDE)) {
				Vector rowData = new Vector();
				rowData.add(peptideScore);
				rowData.add(IdentificationItemEnum.PEPTIDE);
				rowData.add("select the operator");
				rowData.add(null);
				model.addRow(rowData);
			}
		} else if (jComboBoxProteinScoreNames.getSelectedIndex() > 0) {
			String proteinScore = (String) jComboBoxProteinScoreNames
					.getSelectedItem();
			if (!alreadyIsInTable(proteinScore, IdentificationItemEnum.PROTEIN)) {
				Vector rowData = new Vector();
				rowData.add(proteinScore);
				rowData.add(IdentificationItemEnum.PROTEIN);
				rowData.add("select the operator");
				rowData.add(null);
				model.addRow(rowData);
			}
		}
	}

	private boolean alreadyIsInTable(String scoreName,
			IdentificationItemEnum item) {
		Vector dataVector = ((DefaultTableModel) jTableScoreFilter.getModel())
				.getDataVector();
		for (Object object : dataVector) {
			Vector rowVector = (Vector) object;
			if (rowVector.get(SCORE_NAME_COLUMN).equals(scoreName)
					&& rowVector.get(PROTEIN_PEPTIDE_COLUMN).equals(item))
				return true;
		}
		return false;
	}

	protected void jComboBoxPeptideScoreNamesItemStateChanged(ItemEvent evt) {
		if (jComboBoxProteinScoreNames.getItemCount() > 0)
			jComboBoxProteinScoreNames.setSelectedIndex(0);

	}

	protected void jRadioButtonRegexpMouseClicked(MouseEvent evt) {
		if (jRadioButtonRegexp.isSelected()) {

			jTextFieldRegexp.setEnabled(true);
			jTextFieldPrefix.setEnabled(false);
		} else {
			jTextFieldRegexp.setEnabled(false);
			jTextFieldPrefix.setEnabled(true);
		}

	}

	protected void jRadioButtonPrefixMouseClicked(MouseEvent evt) {
		if (jRadioButtonPrefix.isSelected()) {
			jTextFieldPrefix.setEnabled(true);
			jTextFieldRegexp.setEnabled(false);
		} else {
			jTextFieldPrefix.setEnabled(false);
			jTextFieldRegexp.setEnabled(true);
		}

	}

	protected void jCheckBoxFDRFilterActivationActionPerformed(ActionEvent evt) {
		enableFDRFilters(jCheckBoxFDRFilterActivation.isSelected());

	}

	private void jComboBoxProteinOrPeptideItemStateChanged(
			java.awt.event.ItemEvent evt) {
		if (jComboBoxProteinOrPeptide.getSelectedItem().equals(
				IdentificationItemEnum.PROTEIN))
			jCheckBoxDistinguishModPeptides.setEnabled(false);
		else if (jComboBoxProteinOrPeptide.getSelectedItem().equals(
				IdentificationItemEnum.PEPTIDE))
			jCheckBoxDistinguishModPeptides.setEnabled(true);
	}

	private void jCheckBoxScoreFilterActivationActionPerformed(
			java.awt.event.ActionEvent evt) {
		enableScoreFilters(jCheckBoxScoreFilterActivation.isSelected());

	}

	private void enablePeptideLengthFilter(boolean selected) {
		log.info("peptide length filter selected = " + selected);
		jTextFieldPeptideLengthMin.setEnabled(selected);
		jTextFieldPeptideLengthMax.setEnabled(selected);
		jLabelPeptideLength.setEnabled(selected);
		jLabelPeptideLengthMin.setEnabled(selected);
		jLabelPeptideLengthMin1.setEnabled(selected);
		jLabelPeptideLengthMin2.setEnabled(selected);
		jLabelPeptideLengthMin3.setEnabled(selected);
		parent.jCheckBoxMenuItemPeptideLenthFilter.setSelected(selected);
	}

	private void enablePeptideSequencesFilter(boolean selected) {
		jTextAreaPeptideSequencesList.setEnabled(selected);
		jCheckBoxDistinguishModificatedPeptidesSequenceFilter
				.setEnabled(selected);
		jLabelPeptideSequenceFilterExplanation.setEnabled(selected);
		jLabelNumPeptidesPeptideSequenceFilter.setEnabled(selected);
		jLabelPeptideSequenceFilterExplanation.setEnabled(selected);
		parent.jCheckBoxMenuItemPeptideSequenceFilter.setSelected(selected);
		if (!selected)
			peptideSequenceFilter = null;
	}

	private void enablePeptideForMRMFilter(boolean selected) {
		jCheckBoxIgnoreM.setEnabled(selected);
		jCheckBoxIgnoreMissedCleavages.setEnabled(selected);
		jCheckBoxIgnoreQ.setEnabled(selected);
		jCheckBoxIgnoreW.setEnabled(selected);
		jCheckBoxRequireUniquePeptides.setEnabled(selected);
		jTextFieldPeptideLengthMaxMRM.setEnabled(selected);
		jTextFieldPeptideLengthMinMRM.setEnabled(selected);
		jLabelPeptideMRM1.setEnabled(selected);
		jLabelPeptideMRMExplanation.setEnabled(selected);
		jLabelPeptideMRMFrom.setEnabled(selected);
		jLabelPeptideMRMTo.setEnabled(selected);
		jLabelPeptidMRMAminoacids.setEnabled(selected);
		parent.jCheckBoxMenuItemPeptideForMRMFilter.setSelected(selected);
	}

	private void enableFDRFilters(boolean selected) {
		log.info("fdr filter selected=" + selected);
		jTableFDRFilter.setEnabled(selected);
		jCheckBoxConcatenated.setEnabled(selected);
		jRadioButtonPrefix.setEnabled(selected);
		jRadioButtonRegexp.setEnabled(selected);
		if (!selected || (selected && jRadioButtonPrefix.isSelected()))
			jTextFieldPrefix.setEnabled(selected);
		if (!selected || (selected && jRadioButtonRegexp.isSelected()))
			jTextFieldRegexp.setEnabled(selected);
		jLabelPeptideFDRThreshold.setEnabled(selected);
		jTextFieldPeptideFDRThreshold.setEnabled(selected);
		jButtonAddThresholdToReplicates.setEnabled(selected);
		jComboBoxPeptideOrProteinFDR.setEnabled(selected);
		parent.jCheckBoxMenuItemFDRFilter.setSelected(selected);

	}

	private void enableScoreFilters(boolean selected) {
		log.info("score filter selected=" + selected);
		jLabel1.setEnabled(selected);
		jLabel2.setEnabled(selected);
		jLabel3.setEnabled(selected);
		jComboBoxProteinScoreNames.setEnabled(selected);
		jComboBoxPeptideScoreNames.setEnabled(selected);
		jTableScoreFilter.setEnabled(selected);
		jButtonAddScoreToList.setEnabled(selected);
		jButtonRemoveScoreToList.setEnabled(selected);
		parent.jCheckBoxMenuItemScoreFilters.setSelected(selected);

	}

	private void enableOccurrenceFilters(boolean selected) {
		log.info("occurrence filter selected=" + selected);
		jLabel4.setEnabled(selected);
		jLabel5.setEnabled(selected);
		jComboBoxProteinOrPeptide.setEnabled(selected);
		jTextFieldOccurrenceThreshold.setEnabled(selected);
		// disabled because at the begining it is disabled:
		// this.jCheckBoxDistinguishModPeptides.setEnabled(selected);
		jComboBoxReplicatesOrTimes.setEnabled(selected);
		parent.jCheckBoxMenuItemOcurrenceFilter.setSelected(selected);

	}

	public List<Filter> getFilters() {
		List<Filter> ret = new ArrayList<Filter>();
		// log.info("Getting filters");
		ret.addAll(getScoreFilters(IdentificationItemEnum.PROTEIN));
		ret.addAll(getScoreFilters(IdentificationItemEnum.PEPTIDE));
		ret.addAll(getFDRFilters());
		final OccurrenceFilter occurrenceFilter = getOccurrenceFilter();
		if (occurrenceFilter != null)
			ret.add(occurrenceFilter);
		final ModificationFilter modificationFilter = getModificationFilter();
		if (modificationFilter != null)
			ret.add(modificationFilter);
		final ProteinACCFilter proteinAccessionFilter = getProteinACCFilter();
		if (proteinAccessionFilter != null)
			ret.add(proteinAccessionFilter);

		final PeptideNumberFilter peptideNumberFilter = getPeptideNumberFilter();
		if (peptideNumberFilter != null)
			ret.add(peptideNumberFilter);

		final PeptideLengthFilter peptideLengthFilter = getPeptideLengthFilter();
		if (peptideLengthFilter != null)
			ret.add(peptideLengthFilter);

		final PeptideSequenceFilter peptideSequencesFilter = getPeptideSequencesFilter();
		if (peptideSequencesFilter != null)
			ret.add(peptideSequencesFilter);

		final PeptidesForMRMFilter peptideForMRMFilter = getPeptideForMRMFilter();
		if (peptideForMRMFilter != null)
			ret.add(peptideForMRMFilter);
		// log.info("returning " + ret.size() + " filters");
		return ret;
	}

	public PeptidesForMRMFilter getPeptideForMRMFilter() {
		if (jCheckBoxPeptideForMRMFilterActivation.isSelected()) {
			try {
				validatePeptideForMRMFilter();
			} catch (IllegalMiapeArgumentException e) {
				return null;
			}
			boolean ignoreM = jCheckBoxIgnoreM.isSelected();
			boolean ignoreW = jCheckBoxIgnoreW.isSelected();
			boolean ignoreQAtBeginning = jCheckBoxIgnoreQ.isSelected();
			boolean ignoreMissedCleavages = jCheckBoxIgnoreMissedCleavages
					.isSelected();
			Integer minLength = 0;
			Integer maxLength = Integer.MAX_VALUE;
			try {
				minLength = Integer.valueOf(jTextFieldPeptideLengthMinMRM
						.getText());
			} catch (NumberFormatException e) {

			}
			try {
				maxLength = Integer.valueOf(jTextFieldPeptideLengthMaxMRM
						.getText());
			} catch (NumberFormatException e) {

			}
			boolean requireUnique = jCheckBoxRequireUniquePeptides.isSelected();
			PeptidesForMRMFilter ret = new PeptidesForMRMFilter(ignoreM,
					ignoreW, ignoreQAtBeginning, ignoreMissedCleavages,
					minLength, maxLength, requireUnique,
					MainFrame.getMiapeExtractorSoftware());
			return ret;
		}
		return null;
	}

	public PeptideSequenceFilter getPeptideSequencesFilter() {
		if (jCheckBoxPeptideSequencesFilterActivation.isSelected()) {
			try {
				validatePeptideSequencesFilter();
			} catch (IllegalMiapeArgumentException e) {
				return null;
			}
			if (peptideSequenceFilter != null)
				return peptideSequenceFilter;

			String peptideListText = jTextAreaPeptideSequencesList.getText();
			String[] peptideList = peptideListText.split("\n");
			List<String> peptideSequenceList = new ArrayList<String>();
			for (String pepSequence : peptideList) {
				peptideSequenceList.add(pepSequence);
			}
			final boolean distinguisModificatedPeptides = false;
			final PeptideSequenceFilter peptideSequenceFilter = new PeptideSequenceFilter(
					peptideSequenceList, distinguisModificatedPeptides,
					MainFrame.getMiapeExtractorSoftware());
			this.peptideSequenceFilter = peptideSequenceFilter;
			return peptideSequenceFilter;
		}
		return null;
	}

	public PeptideNumberFilter getPeptideNumberFilter() {
		if (jCheckBoxPeptideNumberFilterActivation.isSelected()) {
			try {
				Integer min = Integer
						.valueOf(jTextFieldPeptideNumber.getText());
				PeptideNumberFilter filter = new PeptideNumberFilter(min,
						MainFrame.getMiapeExtractorSoftware());
				return filter;
			} catch (Exception e) {

			}
		}
		return null;
	}

	public PeptideLengthFilter getPeptideLengthFilter() {
		if (jCheckBoxPeptideLengthFilterActivation.isSelected()) {
			try {
				validatePeptideLengthFilter();
			} catch (IllegalMiapeArgumentException e) {
				return null;
			}
			try {
				Integer min = Integer.valueOf(jTextFieldPeptideLengthMin
						.getText());
				int max = Integer.MAX_VALUE;
				if (!"".equals(jTextFieldPeptideLengthMax.getText()))
					max = Integer.valueOf(jTextFieldPeptideLengthMax.getText());
				PeptideLengthFilter filter = new PeptideLengthFilter(min, max,
						MainFrame.getMiapeExtractorSoftware());
				return filter;
			} catch (NumberFormatException e) {

			}
		}
		return null;
	}

	public ProteinACCFilter getProteinACCFilter() {
		if (jCheckBoxProteinACCFilterActivation.isSelected()) {
			if (proteinACCFilter != null)
				return proteinACCFilter;
			if (jRadioButtonPasteProteinACCList.isSelected()) {
				String proteinListText = jTextAreaProteinACCList.getText();
				if ("".equals(proteinListText))
					return null;
				String[] proteinList = proteinListText.split("\n");
				List<String> proteinACCList = new ArrayList<String>();
				for (String proteinACC : proteinList) {
					if (!"".equals(proteinACC.trim()))
						proteinACCList.add(proteinACC);
				}
				final ProteinACCFilter proteinACCFilter = new ProteinACCFilter(
						proteinACCList, MainFrame.getMiapeExtractorSoftware());
				this.proteinACCFilter = proteinACCFilter;
				return proteinACCFilter;
			} else if (jRadioButtonSelectFastaFile.isSelected()) {
				if (fastaFile != null && fastaFile.exists()) {
					try {
						final ProteinACCFilter proteinACCFilter = new ProteinACCFilter(
								fastaFile,
								MainFrame.getMiapeExtractorSoftware());
						this.proteinACCFilter = proteinACCFilter;
						return proteinACCFilter;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	public List<FDRFilter> getFDRFilters() {
		// log.info("Getting FDR filters");
		List<FDRFilter> ret = new ArrayList<FDRFilter>();
		// SortingParameters sortingParameters = null;
		if (jCheckBoxFDRFilterActivation.isSelected()) {
			// log.info("Menu is activated.");
			for (Experiment experiment : experimentList
					.getNextLevelIdentificationSetList()) {
				for (Replicate replicate : experiment
						.getNextLevelIdentificationSetList()) {
					FDRFilter replicateFDRFilter = getReplicateFDRFilter(
							experiment.getName(), replicate.getName());
					if (replicateFDRFilter != null) {
						ret.add(replicateFDRFilter);
						// if (sortingParameters == null)
						// sortingParameters =
						// getSortingParameters(experiment.getName());
					}
				}
			}
		}
		// log.info("Returning " + ret.size() + " FDR filters");
		return ret;
	}

	public void applyFilters(ExperimentList experimentList) {
		if (filtersHaveChanged()) {
			previousFilters.clear();
			final List<Filter> filters = getFilters();
			previousFilters.addAll(filters);
			enableInputMethods(false);
			setProgressIndeterminate(true);
			parent.setEmptyChart();

			boolean filterReplicates = false;
			if (ChartManagerFrame.ONE_SERIES_PER_REPLICATE.equals(parent
					.getOptionChart()))
				filterReplicates = true;
			log.info("Calling to APPLY FILTERS in FiltersDialog!!!");
			filterTask = new FilterTask(filters, experimentList,
					filterReplicates);
			filterTask.addPropertyChangeListener(this);
			filterTask.execute();
			parent.setStatus("Filtering...");
		} else {
			// TODO
			// this.filterTask = new FilterTask(null, null);
			// filterTask.addPropertyChangeListener(this);
			// filterTask.execute();
			parent.showChart();
		}

	}

	public boolean isFilterTaskFinished() {
		if (filterTask != null) {
			if (filterTask.getState().equals(StateValue.DONE))
				return true;
			return false;
		}
		return true;
	}

	private void setProgressIndeterminate(boolean b) {
		if (parent instanceof ChartManagerFrame) {
			parent.setProgressBarIndeterminate(b);
		}

	}

	private boolean filtersHaveChanged() {
		final List<Filter> filters = getFilters();
		if (filters.isEmpty() && !previousFilters.isEmpty())
			return true;
		if (filters.size() != previousFilters.size())
			return true;
		for (Filter filter : filters) {
			boolean filterFound = false;
			for (Filter previousFilter : previousFilters) {
				if (previousFilter.equals(filter)) {
					filterFound = true;
					break;
				}
			}
			if (!filterFound)
				return true;
		}

		return false;
	}

	public OccurrenceFilter getOccurrenceFilter() {
		if (jCheckBoxOccurrenceFilterActivation.isSelected()) {
			IdentificationItemEnum item = (IdentificationItemEnum) jComboBoxProteinOrPeptide
					.getSelectedItem();

			String replicatesOrTimes = (String) jComboBoxReplicatesOrTimes
					.getSelectedItem();
			boolean replicatesBool = true;
			if (replicatesOrTimes.equals(REPLICATES))
				replicatesBool = true;
			else if (replicatesOrTimes.equals(TIMES))
				replicatesBool = false;
			try {
				Integer minOccurrence = Integer
						.valueOf(jTextFieldOccurrenceThreshold.getText());
				boolean distinguisModificatedPeptides = getDistinguisModificatedPeptides();
				OccurrenceFilter filter = new OccurrenceFilter(minOccurrence,
						item, distinguisModificatedPeptides, replicatesBool,
						MainFrame.getMiapeExtractorSoftware());

				return filter;
			} catch (NumberFormatException e) {
				log.info(jTextFieldOccurrenceThreshold.getText()
						+ " cannot be converted to Integer: " + e.getMessage());
			}
		}
		return null;
	}

	@Override
	public void setVisible(boolean b) {
		showModificationControls();
		super.setVisible(b);
	}

	private boolean getDistinguisModificatedPeptides() {
		return jCheckBoxDistinguishModPeptides.isSelected();
	}

	public List<ScoreFilter> getScoreFilters(IdentificationItemEnum item) {
		List<ScoreFilter> ret = new ArrayList<ScoreFilter>();
		if (jCheckBoxScoreFilterActivation.isSelected()) {
			Vector dataVector = ((DefaultTableModel) jTableScoreFilter
					.getModel()).getDataVector();
			for (Object columnVector : dataVector) {
				Vector rowVector = (Vector) columnVector;
				try {
					Float threshold = Float.valueOf((String) rowVector
							.get(THRESHOLD__COLUMN));
					String scoreName = String.valueOf(rowVector
							.get(SCORE_NAME_COLUMN));
					ComparatorOperator includeOperator = (ComparatorOperator) rowVector
							.get(OPERATOR_COLUMN);
					if (rowVector.get(PROTEIN_PEPTIDE_COLUMN).toString()
							.equalsIgnoreCase(item.toString())) {
						ScoreFilter scoreFilter = new ScoreFilter(threshold,
								scoreName, includeOperator, item,
								MainFrame.getMiapeExtractorSoftware());
						ret.add(scoreFilter);
					}
				} catch (NumberFormatException e) {
					log.info(rowVector.get(THRESHOLD__COLUMN)
							+ " cannot be converted to Float: "
							+ e.getMessage());
				}
			}
		}
		return ret;
	}

	private FDRFilter getReplicateFDRFilter(String experimentName,
			String replicateName) {
		try {
			if (jCheckBoxFDRFilterActivation.isSelected()) {
				SortingParameters sortingParameters = getSortingParameters(
						experimentName, replicateName);
				if (sortingParameters == null) {
					throw new IllegalMiapeArgumentException(
							"There is not a definition of sorting parameters for the replicate ("
									+ replicateName + ")");
				}
				boolean concatenatedDecoyDB = jCheckBoxConcatenated
						.isSelected();

				Float threshold = getFDRThresholdValue(replicateName);
				if (threshold == null)
					throw new IllegalMiapeArgumentException();
				if (threshold < 0 || threshold > 100)
					throw new IllegalMiapeArgumentException(
							"Threshold value '"
									+ threshold
									+ "' is not valid. It has to be a number between 0.0 and 100");
				IdentificationItemEnum item = getFDRPeptideOrProtein();
				if (jRadioButtonPrefix.isSelected()) {
					String prefix = jTextFieldPrefix.getText();

					return new FDRFilter(threshold, prefix,
							concatenatedDecoyDB, sortingParameters, item,
							experimentName, replicateName,
							MainFrame.getMiapeExtractorSoftware());
				} else {
					Pattern pattern = Pattern.compile(jTextFieldRegexp
							.getText());
					return new FDRFilter(threshold, pattern,
							concatenatedDecoyDB, sortingParameters, item,
							experimentName, replicateName,
							MainFrame.getMiapeExtractorSoftware());
				}

			}
		} catch (NumberFormatException ex) {
			log.info("Threshold value error");
		} catch (PatternSyntaxException ex) {
			log.info("Patter error: '" + jTextFieldRegexp.getText() + "'");
		} catch (IllegalMiapeArgumentException ex) {

		}
		return null;
	}

	private IdentificationItemEnum getFDRPeptideOrProtein() {
		if (jComboBoxPeptideOrProteinFDR.getSelectedItem().equals(
				"FDR at peptide level")) {
			return IdentificationItemEnum.PEPTIDE;
		} else if (jComboBoxPeptideOrProteinFDR.getSelectedItem().equals(
				"FDR at protein level")) {
			return IdentificationItemEnum.PROTEIN;
		} else if (jComboBoxPeptideOrProteinFDR.getSelectedItem().equals(
				"FDR at PSM level")) {
			return IdentificationItemEnum.PSM;
		}
		return IdentificationItemEnum.PEPTIDE;
	}

	private Float getFDRThresholdValue(String replicateName) {
		Vector dataVector = ((DefaultTableModel) jTableFDRFilter.getModel())
				.getDataVector();
		for (Object columnVector : dataVector) {
			Vector rowVector = (Vector) columnVector;
			if (rowVector.get(REPLICATE_NAME_COLUMN).equals(replicateName)) {
				try {
					final Object object = rowVector.get(FDR_THRESHOLD_COLUMN);
					if (object != null)
						return (Float) object;
				} catch (Exception e) {

				}
			}
		}
		return null;
	}

	private SortingParameters getSortingParameters(String experimentName,
			String replicateName) {
		Vector dataVector = ((DefaultTableModel) jTableFDRFilter.getModel())
				.getDataVector();
		for (Object columnVector : dataVector) {
			Vector rowVector = (Vector) columnVector;
			if (rowVector.get(REPLICATE_NAME_COLUMN).equals(replicateName)
					&& rowVector.get(EXPERIMENT_NAME_COLUMN).equals(
							experimentName)) {
				String peptideScoreName = (String) rowVector
						.get(PEPTIDE_SCORE_COLUMN);
				if (peptideScoreName != null && !"".equals(peptideScoreName)) {
					return SortingManager.getInstance(
							OntologyLoaderTask.getCvManager())
							.getSortingParameters(peptideScoreName);
				}
			}
		}
		return null;
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {

		FiltersDialog dialog = new FiltersDialog(null, null);

		dialog.setVisible(true);
	}

	// GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.ButtonGroup buttonGroup1;
	private javax.swing.ButtonGroup buttonGroup2;
	private javax.swing.JButton jButtonAddScoreToList;
	private javax.swing.JButton jButtonAddThresholdToReplicates;
	private javax.swing.JButton jButtonFinish;
	private javax.swing.JButton jButtonRemoveScoreToList;
	private javax.swing.JCheckBox jCheckBoxConcatenated;
	private javax.swing.JCheckBox jCheckBoxDistinguishModPeptides;
	private javax.swing.JCheckBox jCheckBoxDistinguishModificatedPeptidesSequenceFilter;
	private javax.swing.JCheckBox jCheckBoxFDRFilterActivation;
	private javax.swing.JCheckBox jCheckBoxIgnoreM;
	private javax.swing.JCheckBox jCheckBoxIgnoreMissedCleavages;
	private javax.swing.JCheckBox jCheckBoxIgnoreQ;
	private javax.swing.JCheckBox jCheckBoxIgnoreW;
	private javax.swing.JCheckBox jCheckBoxModificationFilterActivation;
	private javax.swing.JCheckBox jCheckBoxOccurrenceFilterActivation;
	private javax.swing.JCheckBox jCheckBoxPeptideForMRMFilterActivation;
	private javax.swing.JCheckBox jCheckBoxPeptideLengthFilterActivation;
	private javax.swing.JCheckBox jCheckBoxPeptideNumberFilterActivation;
	private javax.swing.JCheckBox jCheckBoxPeptideSequencesFilterActivation;
	private javax.swing.JCheckBox jCheckBoxProteinACCFilterActivation;
	private javax.swing.JCheckBox jCheckBoxRequireUniquePeptides;
	private javax.swing.JCheckBox jCheckBoxScoreFilterActivation;
	private javax.swing.JComboBox jComboBoxPeptideOrProteinFDR;
	private javax.swing.JComboBox jComboBoxPeptideScoreNames;
	private javax.swing.JComboBox jComboBoxProteinOrPeptide;
	private javax.swing.JComboBox jComboBoxProteinScoreNames;
	private javax.swing.JComboBox jComboBoxReplicatesOrTimes;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel jLabelFastaFilePath;
	private javax.swing.JLabel jLabelNumPeptidesPeptideSequenceFilter;
	private javax.swing.JLabel jLabelNumPeptidesTextField;
	private javax.swing.JLabel jLabelNumProteinsProteinACCFilter;
	private javax.swing.JLabel jLabelPeptidMRMAminoacids;
	private javax.swing.JLabel jLabelPeptideFDRThreshold;
	private javax.swing.JLabel jLabelPeptideLength;
	private javax.swing.JLabel jLabelPeptideLengthMin;
	private javax.swing.JLabel jLabelPeptideLengthMin1;
	private javax.swing.JLabel jLabelPeptideLengthMin2;
	private javax.swing.JLabel jLabelPeptideLengthMin3;
	private javax.swing.JLabel jLabelPeptideMRM1;
	private javax.swing.JLabel jLabelPeptideMRMExplanation;
	private javax.swing.JLabel jLabelPeptideMRMFrom;
	private javax.swing.JLabel jLabelPeptideMRMTo;
	private javax.swing.JLabel jLabelPeptideNumber;
	private javax.swing.JLabel jLabelPeptideSequenceFilterExplanation;
	private javax.swing.JLabel jLabelProteinACCList;
	private javax.swing.JPanel jPanelFDRFilter;
	private javax.swing.JPanel jPanelFDRParameters;
	private javax.swing.JPanel jPanelModificationFilter;
	private javax.swing.JPanel jPanelOccurrenceFilter;
	private javax.swing.JPanel jPanelOccurrenceParameters;
	private javax.swing.JPanel jPanelOccurrenceParameters1;
	private javax.swing.JPanel jPanelPeptideForMRM;
	private javax.swing.JPanel jPanelPeptideLength;
	private javax.swing.JPanel jPanelPeptideNumber;
	private javax.swing.JPanel jPanelPeptideSequencesFilter;
	private javax.swing.JPanel jPanelProteinACCList;
	private javax.swing.JPanel jPanelProteinACCList1;
	private javax.swing.JPanel jPanelProteinACCList2;
	private javax.swing.JPanel jPanelProteinACCList3;
	private javax.swing.JPanel jPanelProteinACCList4;
	private javax.swing.JPanel jPanelProteinAccessionFilter;
	private javax.swing.JPanel jPanelScoreFDRParameters;
	private javax.swing.JPanel jPanelScoreFilter;
	private javax.swing.JPanel jPanelScoreParameters;
	private javax.swing.JRadioButton jRadioButtonPasteProteinACCList;
	private javax.swing.JRadioButton jRadioButtonPrefix;
	private javax.swing.JRadioButton jRadioButtonRegexp;
	private javax.swing.JRadioButton jRadioButtonSelectFastaFile;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JScrollPane jScrollPanePeptideSequencesList;
	private javax.swing.JScrollPane jScrollPaneProteinACCList;
	private javax.swing.JTabbedPane jTabbedPaneFilters;
	private MyJTable jTableFDRFilter;
	private MyJTable jTableScoreFilter;
	private javax.swing.JTextArea jTextAreaPeptideSequencesList;
	private javax.swing.JTextArea jTextAreaProteinACCList;
	private javax.swing.JTextField jTextFieldOccurrenceThreshold;
	private javax.swing.JTextField jTextFieldPeptideFDRThreshold;
	private javax.swing.JTextField jTextFieldPeptideLengthMax;
	private javax.swing.JTextField jTextFieldPeptideLengthMaxMRM;
	private javax.swing.JTextField jTextFieldPeptideLengthMin;
	private javax.swing.JTextField jTextFieldPeptideLengthMinMRM;
	private javax.swing.JTextField jTextFieldPeptideNumber;
	private javax.swing.JTextField jTextFieldPrefix;
	private javax.swing.JTextField jTextFieldRegexp;

	// End of variables declaration//GEN-END:variables

	public void setFiltersEnabled(boolean selected) {
		setFDRFilterEnabled(selected);
		setOccurrenceFilterEnabled(selected);
		setScoreFilterEnabled(selected);

	}

	public void setScoreFilterEnabled(boolean selected) {
		jCheckBoxScoreFilterActivation.setSelected(selected);
		jCheckBoxScoreFilterActivationActionPerformed(null);
	}

	public void setOccurrenceFilterEnabled(boolean selected) {
		jCheckBoxOccurrenceFilterActivation.setSelected(selected);
		jCheckBoxOccurrenceFilterActivationActionPerformed(null);
	}

	public void setFDRFilterEnabled(boolean selected) {
		log.info("Setting FDR filter to: " + selected);
		jCheckBoxFDRFilterActivation.setSelected(selected);
		jCheckBoxFDRFilterActivationActionPerformed(null);
	}

	public void setModificationFilterEnabled(boolean selected) {
		jCheckBoxModificationFilterActivation.setSelected(selected);
		jCheckBoxModificationFilterActivationActionPerformed(null);
	}

	public void setProteinACCFilterEnabled(boolean selected) {
		jCheckBoxProteinACCFilterActivation.setSelected(selected);
		jCheckBoxProteinACCFilterActivationActionPerformed(null);
	}

	public void setPeptideNumberFilterEnabled(boolean selected) {
		jCheckBoxPeptideNumberFilterActivation.setSelected(selected);
		jCheckBoxPeptideNumberFilterActivationActionPerformed(null);
	}

	public void setPeptideLengthFilterEnabled(boolean selected) {
		jCheckBoxPeptideLengthFilterActivation.setSelected(selected);
		jCheckBoxPeptideLengthFilterActivationActionPerformed(null);
	}

	public void setPeptideSequencesFilterEnabled(boolean selected) {
		jCheckBoxPeptideSequencesFilterActivation.setSelected(selected);
		jCheckBoxPeptideSequencesFilterActivationActionPerformed(null);
	}

	public void setPeptideForMRMFilterEnabled(boolean selected) {
		jCheckBoxPeptideForMRMFilterActivation.setSelected(selected);
		jCheckBoxPeptideForMRMFilterActivationActionPerformed(null);
	}

	public void setCurrentIndex(int index) {
		if (jTabbedPaneFilters != null)
			jTabbedPaneFilters.setSelectedIndex(index);

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(FilterTask.FILTER_DONE)) {
			setProgressIndeterminate(false);
			enableInputMethods(true);

			if (parent instanceof ChartManagerFrame) {
				ChartManagerFrame chartManager = parent;

				chartManager.appendStatus("Filter done.");
				chartManager.setFDRLabel();
				chartManager.showChart();

			}
		}

	}

	public boolean isOccurrenceByReplicates() {
		final OccurrenceFilter occurrenceFilter = getOccurrenceFilter();
		if (occurrenceFilter != null && occurrenceFilter.isByReplicates())
			return true;
		return false;
	}

	public boolean isOccurrenceFilterEnabled() {
		final OccurrenceFilter occurrenceFilter = getOccurrenceFilter();
		if (occurrenceFilter == null)
			return false;
		return true;
	}

	public void enableProteinACCFilter(Collection<String> proteinACCList) {
		if (proteinACCList != null) {
			jCheckBoxProteinACCFilterActivation.setSelected(true);
			enableProteinACCFilter(true);
			StringBuffer proteinListText = new StringBuffer();
			List<String> list = new ArrayList<String>();
			for (String string : proteinACCList) {
				list.add(string);
			}
			Collections.sort(list);
			for (String proteinACC : list) {
				proteinListText.append(proteinACC + "\n");
			}

			jTextAreaProteinACCList.setText(proteinListText.toString());
			jRadioButtonPasteProteinACCList.setSelected(true);
			jRadioButtonSelectFastaFile.setSelected(false);
			jTextAreaProteinACCList.setEnabled(true);
		}
	}

	public boolean isFDRFilterDefined() {
		final List<FDRFilter> fdrFilters = getFDRFilters();
		if (fdrFilters != null && !fdrFilters.isEmpty())
			return true;
		return false;
	}

	/**
	 * Gets the score name used for construct the FDR filters. If there is more
	 * than one score name, return null
	 * 
	 * @return
	 */
	public String getUniqueFDRScoreName() {
		String scoreName = null;
		final List<FDRFilter> fdrFilters = getFDRFilters();
		if (fdrFilters != null)
			for (FDRFilter fdrFilter : fdrFilters) {
				if (scoreName == null)
					scoreName = fdrFilter.getSortingParameters().getScoreName();
				else if (!scoreName.equals(fdrFilter.getSortingParameters()
						.getScoreName()))
					return null;
			}
		return scoreName;
	}
}