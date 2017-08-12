package org.proteored.pacom.analysis.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramType;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.ExtendedIdentifiedPeptide;
import org.proteored.miapeapi.experiment.model.PeptideOccurrence;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.miapeapi.experiment.model.sort.Order;
import org.proteored.miapeapi.experiment.model.sort.ProteinGroupComparisonType;
import org.proteored.miapeapi.experiment.model.sort.SorterUtil;
import org.proteored.pacom.analysis.charts.HeatChart;
import org.proteored.pacom.analysis.charts.WordCramChart;
import org.proteored.pacom.analysis.genes.GeneDistributionReader;
import org.proteored.pacom.analysis.gui.components.JLabelColor;
import org.proteored.pacom.analysis.gui.tasks.DatasetFactory;
import org.proteored.pacom.analysis.util.DoSomethingToChangeColorInChart;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

public class AdditionalOptionsPanelFactory {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private final ChartManagerFrame frame;

	// private static AdditionalOptionsPanelFactory instance;

	private static final String LOGARITHMIC_SCALE = "Logarithmic";
	private static final String LINEAR_SCALE = "Linear";
	private static final String EXPONENTIAL_SCALE = "Exponential";
	private static final String MOVERZ = "m/z";
	private static final String M = "Da";
	public static final String DEFAULT_PROTEIN_SCORE_NAME = "Mascot:Score";
	public static final Order PROTEIN_SCORE_ORDER = Order.DESCENDANT;
	public static final String DEFAULT_PEPTIDE_SCORE_NAME = "Mascot:expectation value";
	public static final Order PEPTIDE_SCORE_ORDER = Order.ASCENDANT;

	public static final String NO_MODIFICATION = "Not modified";

	private JComboBox jComboBoxHistogramType;
	private JTextField jTextHistogramBin;
	public JComboBox jComboBoxPlotOrientation;
	private JComboBox jComboBoxColorScale;
	private JTextField jTextHeatMapThreshold;
	// private JComboBox jComboBoxBestProteinHit;
	private JComboBox jComboBoxPeptideScoreNames;
	private JComboBox jComboBoxProteinScoreNames;
	private JList jListModifications;
	// private JList jListPeptides;
	private MyJPanelList jListPeptides;
	private JLabel jlabelPeptideListHeader;
	private JLabel jLabelModificationListHeader;
	private JTextArea jTextAreaUserPeptideList;
	public String userPeptideList;
	private JTextField jTextContaining;
	private JCheckBox jCheckBoxAsPercentage;
	private final Set<String> proteinsInSample = new THashSet<String>();
	private JButton jButtonShowInputTextFrame;
	private JLabel jlabelProteinsInSample;
	private JComboBox jComboBoxMaximumOccurrence;
	private JCheckBox jCheckBoxAddRegressionLine;
	private JCheckBox jCheckBoxAddDiagonalLine;
	private JComboBox jComboBoxMOverZ;
	private JCheckBox jCheckBoxOverReplicates;
	private JCheckBox jCheckBoxSensitivity;
	private JCheckBox jCheckBoxFDR;
	private JCheckBox jCheckBoxNPV;
	private JCheckBox jCheckBoxPrecision;
	private JCheckBox jCheckBoxSpecificity;
	private JCheckBox jCheckBoxAccuracy;
	private JCheckBox jCheckBoxShowAsStackedChart;

	private final List<JComponent> controlList = new ArrayList<JComponent>();

	private JCheckBox jCheckBoxShowAsPieChart;

	protected Object[] previousModificationsSelected;

	private JComboBox jComboBoxModificationA;

	private JComboBox jComboBoxModificationB;

	private JCheckBox jCheckBoxShowAverage;

	private JCheckBox jCheckBoxShowTotalSerie;

	protected JCheckBox jCheckBoxShowDifferentIdentifications;

	private JCheckBox jCheckBoxTotalVersusDifferent;

	private JLabel jLabelIntersectionsText;

	private final List<JCheckBox> assignedGroups = new ArrayList<JCheckBox>();
	private JCheckBox notAssigned;
	private JComboBox proteinOrGeneSelector;
	private JComboBox known_unknown;
	public static final String BOTH = "both";
	public static final String PROTEIN = "proteins";
	public static final String GENES = "genes";
	public static final String PEPTIDE = "peptide";
	public static final String PSM = "psm";
	public static final String KNOWN = "known";
	public static final String UNKNOWN = "unknown";
	private final Map<String, JCheckBox> idSetsJCheckBoxes = new THashMap<String, JCheckBox>();
	private final Map<String, Color> idSetsColors = new THashMap<String, Color>();

	private JCheckBox jCheckBoxShowPSM;

	private JCheckBox jCheckBoxShowPeptides;

	private JCheckBox jCheckBoxShowProteins;

	private JCheckBox jCheckBoxShowScoreVsFDR;

	private JCheckBox jCheckBoxShowTotalChromosomeProteins;

	private JCheckBox jcheckBoxHeatMapBinary;

	private JComboBox jComboBoxHighColorScale;

	private JComboBox jComboBoxLowColorScale;

	private JRadioButton jradioButtonFirstProteinPerGroup;

	private JRadioButton jradioButtonBestProteinPerGroup;

	private JRadioButton jradioButtonAllProteinsPerGroup;

	private JCheckBox jcheckBoxShowSpiderPlot;

	private JRadioButton jradioButtonShareOneProtein;

	private JCheckBox jcheckBoxAccumulativeTrend;

	private JCheckBox jCheckBoxTakeGeneFromFirstProtein;

	private JCheckBox jCheckBoxShowProteinFDR;

	private JCheckBox jCheckBoxShowPSMFDR;

	private JCheckBox jCheckBoxShowPeptideFDR;

	private JComboBox jComboBoxFont;

	private JTextField jTextFieldMaxNumberWords;

	private JTextArea jTextAreaSkipWords;

	private JButton jButtonDrawWordCram;

	private JLabel jLabelSelectedWord;

	private JTextField jTextFieldMinWordLength;

	private JButton jButtonSaveDrawWordCram;

	private JLabel jLabelSelectedProteins;

	protected File currentDirectory;

	private JCheckBox jCheckBoxApplyLog;

	private JCheckBox jCheckBoxSeparatedDecoyHits;

	private JCheckBox jCheckBoxShowInMinutes;

	private JComboBox peptideOrPSMSelector;

	private JButton jbuttonSaveImage;

	private JComboBox jComboBoxMinimumOccurrence;

	private JCheckBox jCheckBoxIsPSMorPeptide;

	private JCheckBox jCheckBoxShowPeptidesPlusCharge;

	public AdditionalOptionsPanelFactory(ChartManagerFrame frame) {
		this.frame = frame;
		if (this.frame != null) {
			this.frame.setAdditionalOptionsPanelFactory(this);
		}
	}

	// public static AdditionalOptionsPanelFactory getInstance(ChartManagerFrame
	// frame) {
	//
	// instance = new AdditionalOptionsPanelFactory(frame);
	// return instance;
	// }

	public JPanel getPlotOrientationPanel() {
		GridBagConstraints c = new GridBagConstraints();
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel jlabel = new JLabel("Plot orientation:");
		PlotOrientation[] plotOrientations = { PlotOrientation.VERTICAL, PlotOrientation.HORIZONTAL };
		if (jComboBoxPlotOrientation == null) {
			jComboBoxPlotOrientation = new JComboBox(plotOrientations);
			getControlList().add(jComboBoxPlotOrientation);
		}
		if (getPreviousPlotOrientation() != null)
			jComboBoxPlotOrientation.setSelectedItem(getPreviousPlotOrientation());
		jComboBoxPlotOrientation.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				// if (evt.getStateChange() == ItemEvent.SELECTED) {
				frame.startShowingChart(evt.getSource());
				// } else {
				// log.info(evt.getStateChange());
				// }

			}
		});

		c.anchor = GridBagConstraints.NORTHWEST;

		c.gridx = 0;
		c.gridy = 0;

		panel.add(jlabel, c);
		c.gridy = 1;
		panel.add(jComboBoxPlotOrientation, c);
		return panel;
	}

	public JPanel getMaximumNumOccurrence(String labelString, int maxMaximum, int selectedNumber) {
		JPanel jPanelAdditional3 = new JPanel();
		if (labelString == null || "".equals(labelString))
			labelString = "Maximum modif. occurrence:";

		JLabel jlabel3 = new JLabel(labelString);

		if (jComboBoxMaximumOccurrence == null || jComboBoxMaximumOccurrence.getItemCount() != maxMaximum) {
			Integer[] occurrences = new Integer[maxMaximum];
			for (int i = 0; i < occurrences.length; i++) {
				occurrences[i] = i + 1;
			}
			jComboBoxMaximumOccurrence = new JComboBox(occurrences);
			getControlList().add(jComboBoxMaximumOccurrence);
		}
		if (getPreviousMaximumOccurrence() != null)
			jComboBoxMaximumOccurrence.setSelectedItem(getPreviousMaximumOccurrence());
		else
			jComboBoxMaximumOccurrence.setSelectedItem(selectedNumber);

		jComboBoxMaximumOccurrence.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent evt) {
				frame.startShowingChart(evt.getSource());
			}
		});

		jPanelAdditional3.setLayout(new BorderLayout());
		jPanelAdditional3.add(jlabel3, BorderLayout.BEFORE_FIRST_LINE);
		jPanelAdditional3.add(jComboBoxMaximumOccurrence, BorderLayout.CENTER);
		return jPanelAdditional3;
	}

	public JPanel getMinimumNumOccurrence(String labelString, int maxMinimum, int selectedNumber) {
		JPanel jPanelAdditional3 = new JPanel();
		if (labelString == null || "".equals(labelString))
			labelString = "Minimum occurrence:";

		JLabel jlabel3 = new JLabel(labelString);

		if (jComboBoxMinimumOccurrence == null) {
			Integer[] occurrences = new Integer[maxMinimum];
			for (int i = 0; i < occurrences.length; i++) {
				occurrences[i] = i + 1;
			}
			jComboBoxMinimumOccurrence = new JComboBox(occurrences);
			getControlList().add(jComboBoxMinimumOccurrence);
		}
		if (getPreviousMinimumOccurrence() != null)
			jComboBoxMinimumOccurrence.setSelectedItem(getPreviousMinimumOccurrence());
		else
			jComboBoxMinimumOccurrence.setSelectedItem(selectedNumber);

		jComboBoxMinimumOccurrence.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				frame.startShowingChart(evt.getSource());
			}
		});

		jPanelAdditional3.setLayout(new BorderLayout());
		jPanelAdditional3.add(jlabel3, BorderLayout.BEFORE_FIRST_LINE);
		jPanelAdditional3.add(jComboBoxMinimumOccurrence, BorderLayout.CENTER);
		return jPanelAdditional3;
	}

	public Integer getPreviousMaximumOccurrence() {
		Integer previousMaximumOccurrence = null;
		if (jComboBoxMaximumOccurrence != null && jComboBoxMaximumOccurrence.getSelectedIndex() > 0)
			previousMaximumOccurrence = (Integer) jComboBoxMaximumOccurrence.getSelectedItem();
		return previousMaximumOccurrence;
	}

	public Integer getPreviousMinimumOccurrence() {
		Integer previousMinimumOccurrence = null;
		if (jComboBoxMinimumOccurrence != null && jComboBoxMinimumOccurrence.getSelectedIndex() > 0)
			previousMinimumOccurrence = (Integer) jComboBoxMinimumOccurrence.getSelectedItem();
		return previousMinimumOccurrence;
	}

	private PlotOrientation getPreviousPlotOrientation() {
		PlotOrientation previousPlotOrientation = null;
		if (jComboBoxPlotOrientation != null) {
			previousPlotOrientation = (PlotOrientation) jComboBoxPlotOrientation.getSelectedItem();
		}
		return previousPlotOrientation;
	}

	public JCheckBox getShowAsPercentageCheckBox() {

		if (jCheckBoxAsPercentage == null) {
			jCheckBoxAsPercentage = new JCheckBox("Normalize");
			// controlList.add(jCheckBoxAsPercentage); not add because sometimes
			// has to be disabled
		}
		jCheckBoxAsPercentage.setEnabled(true);

		jCheckBoxAsPercentage.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				frame.startShowingChart(evt.getSource());

			}
		});

		return jCheckBoxAsPercentage;
	}

	public JCheckBox getShowAsStackedChartCheckBox() {
		if (jCheckBoxShowAsStackedChart == null) {
			jCheckBoxShowAsStackedChart = new JCheckBox("Show as stacked chart");
			// this.controlList.add(jCheckBoxShowAsStackedChart); not add
			// becouse sometimes has to be disabled
		}
		if (jCheckBoxAsPercentage != null)
			jCheckBoxAsPercentage.setEnabled(false);
		jCheckBoxShowAsStackedChart.setEnabled(true);
		jCheckBoxShowAsStackedChart.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {

				AdditionalOptionsPanelFactory.this.enableShowAsPieChart(!jCheckBoxShowAsStackedChart.isSelected());
				AdditionalOptionsPanelFactory.this.enableAsPercentage(jCheckBoxShowAsStackedChart.isSelected());
				AdditionalOptionsPanelFactory.this.enableShowTotalSerie(!jCheckBoxShowAsStackedChart.isSelected());
				AdditionalOptionsPanelFactory.this
						.enableShowTotalVersusDifferent(!jCheckBoxShowAsStackedChart.isSelected());
				frame.startShowingChart(evt.getSource());
			}
		});

		return jCheckBoxShowAsStackedChart;
	}

	protected void enableShowAsPieChart(boolean b) {
		if (jCheckBoxShowAsPieChart != null) {
			jCheckBoxShowAsPieChart.setEnabled(b);
			if (!b)
				jCheckBoxShowAsPieChart.setSelected(b);
		}

	}

	protected void enableShowAsStackedChart(boolean b) {
		if (jCheckBoxShowAsStackedChart != null) {
			jCheckBoxShowAsStackedChart.setEnabled(b);
			if (!b)
				jCheckBoxShowAsStackedChart.setSelected(b);
		}
	}

	protected void enableAsPercentage(boolean b) {
		if (jCheckBoxAsPercentage != null)
			jCheckBoxAsPercentage.setEnabled(b);
	}

	protected void enableShowTotalSerie(boolean b) {
		if (jCheckBoxShowTotalSerie != null)
			jCheckBoxShowTotalSerie.setEnabled(b);
	}

	public JPanel getProteinsInSamplePanel() {
		JPanel jPanelAdditional1 = new JPanel();

		if (jButtonShowInputTextFrame == null) {
			jButtonShowInputTextFrame = new JButton("Define/show proteins in sample");
			getControlList().add(jButtonShowInputTextFrame);
		}

		jButtonShowInputTextFrame.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				InputTextDialog dialog = new InputTextDialog(frame, "Proteins in sample",
						"<html>Paste the accession codes <br/>of the proteins in the sample</html>", proteinsInSample);

				proteinsInSample.clear();
				proteinsInSample.addAll(dialog.getPastedInfo());
				updateProteinsInSampleLabel();
				frame.startShowingChart(evt.getSource());
			}
		});
		updateProteinsInSampleLabel();
		jPanelAdditional1.setLayout(new BorderLayout());
		jPanelAdditional1.add(jlabelProteinsInSample, BorderLayout.NORTH);
		jPanelAdditional1.add(jButtonShowInputTextFrame, BorderLayout.SOUTH);
		return jPanelAdditional1;
	}

	private void updateProteinsInSampleLabel() {
		String text;
		if (proteinsInSample == null || proteinsInSample.isEmpty())
			text = "<html>No proteins in sample defined.<br/><br/>Click on the button to enter the protein list<br/><br/></html>";
		else
			text = "<html>" + proteinsInSample.size() + " protein defined <br/><br/></html>";
		if (jlabelProteinsInSample == null)
			jlabelProteinsInSample = new JLabel(text);
		else
			jlabelProteinsInSample.setText(text);
	}

	public JCheckBox getCheckBoxSensitivity() {
		if (jCheckBoxSensitivity == null) {
			jCheckBoxSensitivity = new JCheckBox("Sensitivity");
			getControlList().add(jCheckBoxSensitivity);
			jCheckBoxSensitivity.setToolTipText(
					"<html>Sensitivity relates to the test's ability to identify positive results<br>Sensitivity or True Positive Rate = TP/(TP+FN)</html>");
			jCheckBoxSensitivity.setSelected(true);
		}
		jCheckBoxSensitivity.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				frame.startShowingChart(evt.getSource());
			}
		});
		return jCheckBoxSensitivity;
	}

	public JCheckBox getCheckBoxAccuracy() {
		if (jCheckBoxAccuracy == null) {
			jCheckBoxAccuracy = new JCheckBox("Accuracy");
			getControlList().add(jCheckBoxAccuracy);
			jCheckBoxAccuracy.setToolTipText(
					"<html>Accuracy is the proportion of true results (True Positives and True Negatives) in the identified proteins.<br>Accuracy = (TP+TN)/(TP+TN+FP+FN)</html>");
			jCheckBoxAccuracy.setSelected(true);
		}
		jCheckBoxAccuracy.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				frame.startShowingChart(evt.getSource());
			}
		});
		return jCheckBoxAccuracy;
	}

	public JCheckBox getCheckBoxSpecificity() {
		if (jCheckBoxSpecificity == null) {
			jCheckBoxSpecificity = new JCheckBox("Specificity");
			getControlList().add(jCheckBoxSpecificity);
			jCheckBoxSpecificity.setToolTipText(
					"<html>Specificity or True Negative Rate, relates to the ability to identify negative results.<br>Specificity = TN/(FP+TN) = 1-FPR</html>");
			jCheckBoxSpecificity.setSelected(true);
		}
		jCheckBoxSpecificity.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				frame.startShowingChart(evt.getSource());
			}
		});
		return jCheckBoxSpecificity;
	}

	public JCheckBox getCheckBoxPrecision() {
		if (jCheckBoxPrecision == null) {
			jCheckBoxPrecision = new JCheckBox("Precision");
			getControlList().add(jCheckBoxPrecision);
			jCheckBoxPrecision.setToolTipText(
					"<html>Precision is the fraction of True Positives over proteins reported as positives.<br>Precision = TP/(TP+FP)</html>");
		}
		jCheckBoxPrecision.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				frame.startShowingChart(evt.getSource());
			}
		});
		return jCheckBoxPrecision;
	}

	public JCheckBox getCheckBoxNPV() {
		if (jCheckBoxNPV == null) {
			jCheckBoxNPV = new JCheckBox("NPV");
			getControlList().add(jCheckBoxNPV);
			jCheckBoxNPV.setToolTipText(
					"<html>NPV is the fraction of True Negatives over proteins reported as negatives.<br>Negative Predictive Value (NPV) = TN/(TN+FN)</html>");
		}
		jCheckBoxNPV.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				frame.startShowingChart(evt.getSource());
			}
		});
		return jCheckBoxNPV;
	}

	public JCheckBox getCheckBoxFDR() {
		if (jCheckBoxFDR == null) {
			jCheckBoxFDR = new JCheckBox("FDR");
			getControlList().add(jCheckBoxFDR);
			jCheckBoxFDR.setToolTipText("<html>Error rate.<br>False Discovery Rate (FDR) = FP/(FP+TP)</html>");
		}
		jCheckBoxFDR.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				frame.startShowingChart(evt.getSource());
			}
		});
		return jCheckBoxFDR;

	}

	public JPanel getOverReplicatesPanel() {
		JPanel jPanelAdditional4 = new JPanel();

		if (jCheckBoxOverReplicates == null) {
			jCheckBoxOverReplicates = new JCheckBox("Repeatibility over next level");
			getControlList().add(jCheckBoxOverReplicates);
		}
		jCheckBoxOverReplicates.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				frame.startShowingChart(evt.getSource());
			}
		});

		jPanelAdditional4.setLayout(new BorderLayout());

		jPanelAdditional4.add(jCheckBoxOverReplicates, BorderLayout.CENTER);
		return jPanelAdditional4;
	}

	// public JPanel getBestProteinHitPanel() {
	// JPanel jPanelAdditional1 = new JPanel();
	// JLabel jlabel = new
	// JLabel("Take into account the best hit of each protein:");
	// Options[] bestProteinHitOptions = { Options.NO, Options.YES, Options.BOTH
	// };
	// if (this.jComboBoxBestProteinHit == null) {
	// this.jComboBoxBestProteinHit = new JComboBox(bestProteinHitOptions);
	// controlList.add(jComboBoxBestProteinHit);
	// }
	// if (getPreviousBestProteinHit() != null)
	// jComboBoxBestProteinHit.setSelectedItem(getPreviousBestProteinHit());
	// this.jComboBoxBestProteinHit.addActionListener(new
	// java.awt.event.ActionListener() {
	// @Override
	// public void actionPerformed(java.awt.event.ActionEvent evt) {
	// frame.startShowingChart(evt.getSource());
	// }
	// });
	//
	// jPanelAdditional1.setLayout(new BorderLayout());
	// jPanelAdditional1.add(jlabel, BorderLayout.BEFORE_FIRST_LINE);
	// jPanelAdditional1.add(jComboBoxBestProteinHit, BorderLayout.CENTER);
	// return jPanelAdditional1;
	// }
	//
	// public Options getSelectedOption() {
	// if (this.jComboBoxBestProteinHit != null) {
	// return (Options) this.jComboBoxBestProteinHit.getSelectedItem();
	// }
	// return null;
	// }

	// private Options getPreviousBestProteinHit() {
	// Options previousBestProteinHit = null;
	// if (this.jComboBoxBestProteinHit != null)
	// previousBestProteinHit = (Options)
	// jComboBoxBestProteinHit.getSelectedItem();
	// return previousBestProteinHit;
	// }

	private HistogramType getPreviousHistogramType() {
		HistogramType previousHistogramType = null;
		if (jComboBoxHistogramType != null) {
			previousHistogramType = (HistogramType) jComboBoxHistogramType.getSelectedItem();
		}
		return previousHistogramType;
	}

	private String getPreviousColorScale() {

		String previousColorScale = null;
		if (jComboBoxColorScale != null) {
			previousColorScale = (String) jComboBoxColorScale.getSelectedItem();
		}
		return previousColorScale;
	}

	public JPanel getPeptideSequencesPanel(boolean distinguishModPep) {
		JPanel jPanelAdditional2 = new JPanel();
		String[] peptides = frame.getPeptidesFromExperiments(distinguishModPep);
		String num = "";
		if (peptides != null && peptides.length > 0)
			num = String.valueOf(peptides.length);
		if (jlabelPeptideListHeader == null) {
			jlabelPeptideListHeader = new JLabel(num + " peptide sequences:");
			getControlList().add(jlabelPeptideListHeader);
		} else
			jlabelPeptideListHeader.setText(num + " peptide sequences:");
		if (jListPeptides == null) {
			jListPeptides = new MyJPanelList();
			getControlList().add(jListPeptides);
			jListPeptides.jListPeptides.setListData(peptides);
		} else {
			jListPeptides.jListPeptides.setListData(peptides);
		}
		jListPeptides.jListPeptides.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent evt) {
				if (evt.getValueIsAdjusting())
					return;
				frame.startShowingChart(evt.getSource());
			}
		});
		GridBagConstraints c = new GridBagConstraints();
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 0, 0);

		jPanelAdditional2.setLayout(new GridBagLayout());
		c.gridx = 0;
		c.gridy = 0;
		jPanelAdditional2.add(jlabelPeptideListHeader, c);
		c.gridx = 0;
		c.gridy = 1;
		jPanelAdditional2.add(jListPeptides, c);
		// jPanelAdditional2.add(panelList, BorderLayout.LINE_START);

		return jPanelAdditional2;
	}

	public JPanel getUserPeptideListPanel(boolean addAddToPlotButton) {
		JPanel jPanelAdditional3 = new JPanel();
		JLabel jlabelUserPeptideList = new JLabel("Insert a peptide list:");
		GridBagConstraints c = new GridBagConstraints();
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 0, 0, 0);

		if (jTextAreaUserPeptideList == null) {
			jTextAreaUserPeptideList = new JTextArea();
			jTextAreaUserPeptideList.setFont(new JTextField().getFont());
			getControlList().add(jTextAreaUserPeptideList);
		}
		jTextAreaUserPeptideList.setColumns(22);
		jTextAreaUserPeptideList.setLineWrap(false);
		jTextAreaUserPeptideList.setRows(10);
		jTextAreaUserPeptideList.setWrapStyleWord(false);
		if (userPeptideList != null)
			jTextAreaUserPeptideList.setText(userPeptideList);

		jPanelAdditional3.setLayout(new GridBagLayout());
		c.gridx = 0;
		c.gridy = 0;

		jPanelAdditional3.add(jlabelUserPeptideList, c);
		c.gridx = 0;
		c.gridy++;
		JScrollPane scroll = new JScrollPane(jTextAreaUserPeptideList);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jPanelAdditional3.add(scroll, c);
		c.gridx = 0;
		c.gridy++;

		if (addAddToPlotButton) {
			JButton jbuttonAddToPlot = new JButton("Select on peptide list");
			jbuttonAddToPlot.setToolTipText(
					"Click here to automatically select the inserted peptides in the peptide list (if present)");
			getControlList().add(jbuttonAddToPlot);
			jbuttonAddToPlot.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					AdditionalOptionsPanelFactory.this.addUserPeptideListToPeptideSelection();
				}
			});

			if (jTextContaining == null) {
				jTextContaining = new JTextField(10);
				getControlList().add(jTextContaining);
			}

			c.gridx = 0;
			jPanelAdditional3.add(new JLabel("Containing:"), c);
			c.gridy++;
			c.gridy++;
			jPanelAdditional3.add(jTextContaining, c);
			c.gridy++;
			c.gridx = 0;
			jPanelAdditional3.add(jbuttonAddToPlot, c);
		}
		return jPanelAdditional3;
	}

	public List<String> getUserPeptideList() {
		if (jTextAreaUserPeptideList != null) {
			final String text = jTextAreaUserPeptideList.getText();
			List<String> ret = new ArrayList<String>();
			if (text.contains("\n")) {
				final String[] split = text.split("\n");
				log.info("Returning " + split.length + " peptide sequences from user");
				for (String string : split) {
					ret.add(string);
				}
				return ret;
			} else {
				if (!"".equals(text)) {
					ret.add(text);
					log.info("Returning 1 peptide sequences from user: " + text);
					return ret;
				}
			}
		}
		return null;
	}

	protected void addUserPeptideListToPeptideSelection() {
		String containing = jTextContaining.getText();
		boolean distinguishModPep = frame.distinguishModifiedPeptides();
		final String userPeptideList = jTextAreaUserPeptideList.getText();
		List<String> resultingSequences = new ArrayList<String>();
		if (userPeptideList != null && !"".equals(userPeptideList)) {
			this.userPeptideList = userPeptideList;
			String[] splitted = this.userPeptideList.split("\n");
			log.info("the user has input " + splitted.length + " sequences");

			for (String userSequence : splitted) {
				final Collection<PeptideOccurrence> peptideSequences = frame.experimentList
						.getPeptideChargeOccurrenceList(distinguishModPep).values();
				for (PeptideOccurrence identificationOccurrence : peptideSequences) {
					final List<ExtendedIdentifiedPeptide> identificationItemList = identificationOccurrence
							.getPeptides();
					for (ExtendedIdentifiedPeptide extendedIdentifiedPeptide : identificationItemList) {
						final String sequence = extendedIdentifiedPeptide.getSequence();
						if (sequence.equalsIgnoreCase(userSequence)) {
							if (distinguishModPep) {
								final List<String> modifiedSequences = ExtendedIdentifiedPeptide
										.getModifiedSequences(sequence);
								if (modifiedSequences != null && !modifiedSequences.isEmpty()) {
									for (String modifiedSequence : modifiedSequences) {
										if (!resultingSequences.contains(modifiedSequence)) {
											if ((!"".equals(containing) && modifiedSequence.contains(containing))
													|| "".equals(containing)) {
												resultingSequences.add(modifiedSequence);
												log.info(resultingSequences.size() + " -> " + modifiedSequence);

											}
										}
									}
								}
							} else {
								if (!resultingSequences.contains(sequence))
									resultingSequences.add(sequence);
							}
						} else if (extendedIdentifiedPeptide.getModificationString().equals(userSequence)) {
							if (!resultingSequences.contains(userSequence)) {
								if ((!"".equals(containing) && userSequence.contains(containing))
										|| "".equals(containing)) {
									resultingSequences.add(userSequence);
								}
							}
						}
					}
				}
			}

		} else {
			// if there is not a peptide list, look to the containing text box
			// and select the peptides that contain that string in their
			// modification strings
			if (!"".equals(containing)) {
				final Collection<PeptideOccurrence> peptideSequences = frame.experimentList
						.getPeptideChargeOccurrenceList(distinguishModPep).values();
				for (PeptideOccurrence identificationOccurrence : peptideSequences) {
					final List<ExtendedIdentifiedPeptide> identificationItemList = identificationOccurrence
							.getPeptides();
					for (ExtendedIdentifiedPeptide extendedIdentifiedPeptide : identificationItemList) {
						if (distinguishModPep) {
							final String modifiedSequence = extendedIdentifiedPeptide.getModificationString();
							if (modifiedSequence.contains(containing)) {
								if (!resultingSequences.contains(modifiedSequence))
									resultingSequences.add(modifiedSequence);
							}
						} else {
							final String sequence = extendedIdentifiedPeptide.getSequence();
							if (sequence.contains(containing)) {
								if (!resultingSequences.contains(sequence))

									resultingSequences.add(sequence);
							}
						}
					}
				}
			}
		}
		if (!resultingSequences.isEmpty()) {
			jListPeptides.jListPeptides.setListData(resultingSequences.toArray(new String[0]));
			jListPeptides.repaint();
			jListPeptides.jListPeptides.setSelectionInterval(0, resultingSequences.size() - 1);
			jlabelPeptideListHeader.setText(resultingSequences.size() + " peptides sequences");
			frame.startShowingChart(null);
		}
	}

	public JPanel getModificationListPanel() {
		int selectionModel = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;

		JPanel jPanelAdditional2 = new JPanel();
		Object[] modifications = getModifications();
		String num = "";
		if (modifications != null && modifications.length > 0)
			num = String.valueOf(modifications.length);
		if (jLabelModificationListHeader == null) {
			jLabelModificationListHeader = new JLabel(num + " PMTs:");
			getControlList().add(jLabelModificationListHeader);
		} else
			jLabelModificationListHeader.setText(num + " PMTs:");
		if (jListModifications == null) {
			jListModifications = new JList(modifications);
			getControlList().add(jListModifications);
		} else
			jListModifications.setListData(modifications);
		jListModifications.setSelectionMode(selectionModel);
		jListModifications.addListSelectionListener(new ListSelectionListener() {
			private Object[] previousJListSelection;

			@Override
			public void valueChanged(ListSelectionEvent evt) {
				if (evt.getValueIsAdjusting())
					return;
				Object[] selectedValues = ((JList) evt.getSource()).getSelectedValues();
				if (!isTheSameSelection(selectedValues)) {
					previousJListSelection = selectedValues;
					frame.startShowingChart(evt.getSource());
				}
			}

			private boolean isTheSameSelection(Object[] selectedValues) {
				previousModificationsSelected = previousJListSelection;
				if (previousJListSelection == null && selectedValues != null)
					return false;
				if (previousJListSelection != null && selectedValues == null)
					return false;
				if (previousJListSelection == null && selectedValues == null)
					return true;
				if (previousJListSelection.length != selectedValues.length)
					return false;

				for (Object object : selectedValues) {
					boolean found = false;

					for (Object object2 : previousJListSelection) {
						if (object.equals(object2))
							found = true;
					}
					if (!found)
						return false;

				}

				return true;
			}
		});
		if (previousModificationsSelected != null && previousModificationsSelected.length > 0) {
			List<Integer> selectedIndexes = new ArrayList<Integer>();
			for (Object modification : previousModificationsSelected) {
				for (int i = 0; i < jListModifications.getModel().getSize(); i++) {
					final Object element = jListModifications.getModel().getElementAt(i);
					if (element.equals(modification))
						selectedIndexes.add(i);
				}
			}
			int[] temp = new int[selectedIndexes.size()];
			int i = 0;
			for (Integer index : selectedIndexes) {
				temp[i] = index;
				i++;
			}
			jListModifications.setSelectedIndices(temp);
		}
		jPanelAdditional2.setLayout(new BorderLayout());
		jPanelAdditional2.add(jLabelModificationListHeader, BorderLayout.BEFORE_FIRST_LINE);
		jPanelAdditional2.add(new JScrollPane(jListModifications), BorderLayout.LINE_START);
		return jPanelAdditional2;
	}

	public String[] getModifications() {
		log.info("Getting modification names");
		if (frame.experimentList != null) {
			final List<String> differentPeptideModificationNames = frame.experimentList
					.getDifferentPeptideModificationNames();
			if (differentPeptideModificationNames != null && !differentPeptideModificationNames.isEmpty()) {
				log.info("There is " + differentPeptideModificationNames.size() + " different modifications");
				return differentPeptideModificationNames.toArray(new String[0]);
			}
		}
		return null;
	}

	public JPanel getProteinScorePanel(DefaultComboBoxModel proteinScoreNames) {
		JPanel jPanelAdditional3 = new JPanel();
		JLabel jlabel3 = new JLabel("Protein score:");
		if (jComboBoxProteinScoreNames == null) {
			jComboBoxProteinScoreNames = new JComboBox(proteinScoreNames);
			getControlList().add(jComboBoxProteinScoreNames);
		}
		jComboBoxProteinScoreNames = new JComboBox(proteinScoreNames);
		jComboBoxProteinScoreNames.setSelectedIndex(0); // select the
														// first
														// score
		jComboBoxProteinScoreNames.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED)
					frame.startShowingChart(evt.getSource());
			}
		});
		jPanelAdditional3.setLayout(new BorderLayout());
		jPanelAdditional3.add(jlabel3, BorderLayout.LINE_START);
		jPanelAdditional3.add(jComboBoxProteinScoreNames, BorderLayout.CENTER);
		return jPanelAdditional3;
	}

	public JPanel getPeptideScorePanel(DefaultComboBoxModel peptideScoreNames) {
		JPanel jPanelAdditional4 = new JPanel();
		JLabel jlabel4 = new JLabel("Peptide score:");

		if (jComboBoxPeptideScoreNames == null) {
			jComboBoxPeptideScoreNames = new JComboBox(peptideScoreNames);
			getControlList().add(jComboBoxPeptideScoreNames);
			// Select the first score
			jComboBoxPeptideScoreNames.setSelectedIndex(0);
		}
		jComboBoxPeptideScoreNames = new JComboBox(peptideScoreNames);

		jComboBoxPeptideScoreNames.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED)
					frame.startShowingChart(evt.getSource());
			}
		});
		jPanelAdditional4.setLayout(new BorderLayout());
		jPanelAdditional4.add(jlabel4, BorderLayout.LINE_START);
		jPanelAdditional4.add(jComboBoxPeptideScoreNames, BorderLayout.CENTER);
		return jPanelAdditional4;
	}

	public JPanel getShowRegressionLinePanel() {
		JPanel jPanelAdditional5 = new JPanel();
		if (jCheckBoxAddRegressionLine == null) {
			jCheckBoxAddRegressionLine = new JCheckBox("Show regression line");
			getControlList().add(jCheckBoxAddRegressionLine);
			jCheckBoxAddRegressionLine.setSelected(true);
		}

		jCheckBoxAddRegressionLine.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				frame.startShowingChart(evt.getSource());
			}
		});
		jPanelAdditional5.setLayout(new BorderLayout());
		jPanelAdditional5.add(jCheckBoxAddRegressionLine, BorderLayout.CENTER);
		return jPanelAdditional5;
	}

	public JPanel getShowDiagonalLinePanel() {
		JPanel jPanelAdditional6 = new JPanel();
		if (jCheckBoxAddDiagonalLine == null) {
			jCheckBoxAddDiagonalLine = new JCheckBox("Show diagonal line");
			getControlList().add(jCheckBoxAddDiagonalLine);
			jCheckBoxAddDiagonalLine.setSelected(true);
		}

		jCheckBoxAddDiagonalLine.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				frame.startShowingChart(evt.getSource());
			}
		});
		jPanelAdditional6.setLayout(new BorderLayout());
		jPanelAdditional6.add(jCheckBoxAddDiagonalLine, BorderLayout.CENTER);
		return jPanelAdditional6;
	}

	public boolean showDiagonalLine() {
		if (jCheckBoxAddDiagonalLine != null)
			return jCheckBoxAddDiagonalLine.isSelected();
		return false;
	}

	public boolean showRegressionLine() {
		if (jCheckBoxAddRegressionLine != null)
			return jCheckBoxAddRegressionLine.isSelected();
		return false;
	}

	public JPanel getColorScalePanel(boolean alwaysEnabled) {
		JPanel jPanelAdditional1 = new JPanel();

		String[] scaleTypes = { LINEAR_SCALE, LOGARITHMIC_SCALE, EXPONENTIAL_SCALE };
		if (jComboBoxColorScale == null) {
			jComboBoxColorScale = new JComboBox(scaleTypes);
			getControlList().add(jComboBoxColorScale);

			jComboBoxColorScale.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					if (evt.getStateChange() == ItemEvent.SELECTED) {
						frame.startShowingChart(evt.getSource());
					} else {
						log.info(evt.getStateChange());
					}
				}
			});
			getControlList().add(jComboBoxColorScale);
		}
		if (alwaysEnabled)
			jComboBoxColorScale.setEnabled(true);
		if (getPreviousColorScale() != null)
			jComboBoxColorScale.setSelectedItem(getPreviousColorScale());

		if (jComboBoxHighColorScale == null) {
			jComboBoxHighColorScale = new JComboBox(getColors().toArray());
			getControlList().add(jComboBoxHighColorScale);
			jComboBoxHighColorScale.addItemListener(new java.awt.event.ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent evt) {
					if (evt.getStateChange() == ItemEvent.SELECTED) {
						frame.startShowingChart(evt.getSource());
					} else {
						log.info(evt.getStateChange());
					}
				}
			});
		}
		if (jComboBoxLowColorScale == null) {
			jComboBoxLowColorScale = new JComboBox(getColors().toArray());
			getControlList().add(jComboBoxLowColorScale);
			jComboBoxLowColorScale.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					if (evt.getStateChange() == ItemEvent.SELECTED) {
						frame.startShowingChart(evt.getSource());
					} else {
						log.info(evt.getStateChange());
					}
				}
			});
		}
		jPanelAdditional1.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 0;
		jPanelAdditional1.add(new JLabel("Color scale:"), c);
		c.gridx = 1;
		jPanelAdditional1.add(jComboBoxColorScale, c);
		c.gridx = 0;
		c.gridy++;
		jPanelAdditional1.add(new JLabel("Low color:"), c);
		c.gridx = 1;
		jPanelAdditional1.add(jComboBoxLowColorScale, c);
		c.gridx = 0;
		c.gridy++;
		jPanelAdditional1.add(new JLabel("High color:"), c);
		c.gridx = 1;
		jPanelAdditional1.add(jComboBoxHighColorScale, c);
		return jPanelAdditional1;
	}

	public Color getHighColorScale() {
		if (jComboBoxHighColorScale != null) {
			final Color color = getColor((String) jComboBoxHighColorScale.getSelectedItem());
			if (color != null)
				return color;
		}
		return Color.red;
	}

	public Color getLowColorScale() {
		if (jComboBoxLowColorScale != null) {
			final Color color = getColor((String) jComboBoxLowColorScale.getSelectedItem());
			if (color != null)
				return color;
		}
		return Color.green;
	}

	private Color getColor(String selectedItem) {
		if ("black".equals(selectedItem))
			return Color.black;
		if ("blue".equals(selectedItem))
			return Color.blue;
		if ("cyan".equals(selectedItem))
			return Color.cyan;
		if ("darkGray".equals(selectedItem))
			return Color.darkGray;
		if ("gray".equals(selectedItem))
			return Color.gray;
		if ("green".equals(selectedItem))
			return Color.green;
		if ("lightGray".equals(selectedItem))
			return Color.lightGray;
		if ("magenta".equals(selectedItem))
			return Color.magenta;
		if ("orange".equals(selectedItem))
			return Color.orange;
		if ("pink".equals(selectedItem))
			return Color.pink;
		if ("red".equals(selectedItem))
			return Color.red;
		if ("white".equals(selectedItem))
			return Color.white;
		if ("yellow".equals(selectedItem))
			return Color.yellow;
		return Color.blue;
	}

	private List<String> getColors() {
		List<String> colors = new ArrayList<String>();
		colors.add("select a color...");
		colors.add("blue");
		colors.add("yellow");
		colors.add("green");
		colors.add("cyan");
		colors.add("darkGray");
		colors.add("gray");
		colors.add("lightGray");
		colors.add("magenta");
		colors.add("orange");
		colors.add("pink");
		colors.add("red");
		colors.add("white");

		colors.add("black");

		return colors;
	}

	public JPanel getHeatMapThresholdPanel(boolean occurrenceThreshold) {
		JPanel jPanelAdditional2 = new JPanel();

		String label = "Do not paint rows with less than";
		if (occurrenceThreshold) {
			label += " (occurrence):";
		}
		JLabel jlabel2 = new JLabel(label);
		String text = "";
		if (occurrenceThreshold) {
			text = "<html>The number of identification sets that the item occurs <br>"
					+ "will be compared with this number.</html>";
		} else {
			text = "<html>The sum of the number of PSMs in the row<br>" + "will be compared with this number.</html>";
		}
		jlabel2.setToolTipText(text);
		if (jTextHeatMapThreshold == null) {
			jTextHeatMapThreshold = new JTextField("2", 4);
			getControlList().add(jTextHeatMapThreshold);
		}
		jTextHeatMapThreshold.setToolTipText(text);

		jTextHeatMapThreshold.addFocusListener(new java.awt.event.FocusAdapter() {
			private String previousNumber = "";

			@Override
			public void focusLost(java.awt.event.FocusEvent evt) {
				final JTextField jtextfiled = (JTextField) evt.getSource();
				if (!previousNumber.equals(jtextfiled.getText())) {
					if (checkPositiveNumber(jtextfiled.getText()))
						frame.startShowingChart(evt.getSource());
				} else {
					// log.info("The BIN number has not changed");
				}
			}

			@Override
			public void focusGained(java.awt.event.FocusEvent evt) {
				final JTextField jtextfiled = (JTextField) evt.getSource();
				previousNumber = jtextfiled.getText();
				// log.info("Getting previous BIN number: " +
				// jtextfiled.getText());
			}
		});

		jPanelAdditional2.setLayout(new BorderLayout());
		jPanelAdditional2.add(jlabel2, BorderLayout.BEFORE_FIRST_LINE);
		jPanelAdditional2.add(jTextHeatMapThreshold, BorderLayout.CENTER);
		return jPanelAdditional2;
	}

	private boolean checkPositiveNumber(String text) {
		if (text != null && !"".equals(text)) {
			try {
				final Double fdr = Double.valueOf(text);
				if (fdr < 0.0)
					throw new NumberFormatException();
				return true;
			} catch (NumberFormatException e) {
				frame.appendStatus("That number is not well formed. It should be a positive number or 0");
				log.info("FDR is not a number");
			}
		}
		return false;
	}

	public JPanel getHistogramTypePanel() {
		JPanel jPanelAdditional1 = new JPanel();
		JLabel jlabel = new JLabel("Histogram type:");
		HistogramType[] histogramTypes = { HistogramType.FREQUENCY, HistogramType.RELATIVE_FREQUENCY,
				HistogramType.SCALE_AREA_TO_1 };
		if (jComboBoxHistogramType == null) {
			jComboBoxHistogramType = new JComboBox(histogramTypes);
			getControlList().add(jComboBoxHistogramType);
		}
		if (getPreviousHistogramType() != null)
			jComboBoxHistogramType.setSelectedItem(getPreviousHistogramType());
		jComboBoxHistogramType.addItemListener(new java.awt.event.ItemListener() {
			private int previousSelectedIndex = -1;

			@Override
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					if (jComboBoxHistogramType.getSelectedIndex() != previousSelectedIndex) {
						log.info("previous selected index set to " + jComboBoxHistogramType.getSelectedIndex()
								+ " and previously was setted to " + previousSelectedIndex);
						previousSelectedIndex = jComboBoxHistogramType.getSelectedIndex();
						log.info("selected index now is " + previousSelectedIndex);
						frame.startShowingChart(evt.getSource());

					}
				} else {
					log.info(evt.getStateChange());
				}
			}
		});
		jPanelAdditional1.setLayout(new BorderLayout());
		jPanelAdditional1.add(jlabel, BorderLayout.BEFORE_FIRST_LINE);
		jPanelAdditional1.add(jComboBoxHistogramType, BorderLayout.CENTER);

		return jPanelAdditional1;
	}

	public JPanel getBinsPanel() {
		JPanel jPanelAdditional2 = new JPanel();
		JLabel jlabel2 = new JLabel("Bins:");
		if (jTextHistogramBin == null) {
			jTextHistogramBin = new JTextField("30", 4);
			getControlList().add(jTextHistogramBin);
		}
		jTextHistogramBin.addFocusListener(new java.awt.event.FocusAdapter() {
			private String previousNumber = "";

			@Override
			public void focusLost(java.awt.event.FocusEvent evt) {
				final JTextField jtextfiled = (JTextField) evt.getSource();
				if (!previousNumber.equals(jtextfiled.getText())) {
					if (checkPositiveNumber(jtextfiled.getText()))
						frame.startShowingChart(evt.getSource());
				} else {
					// log.info("The BIN number has not changed");
				}
			}

			@Override
			public void focusGained(java.awt.event.FocusEvent evt) {
				final JTextField jtextfiled = (JTextField) evt.getSource();
				previousNumber = jtextfiled.getText();
				// log.info("Getting previous BIN number: " +
				// jtextfiled.getText());
			}
		});
		jPanelAdditional2.setLayout(new BorderLayout());
		jPanelAdditional2.add(jlabel2, BorderLayout.LINE_START);
		jPanelAdditional2.add(jTextHistogramBin, BorderLayout.CENTER);
		return jPanelAdditional2;
	}

	public JPanel getMOverZPanel() {
		JPanel jPanelAdditional3 = new JPanel();
		JLabel jlabel3 = new JLabel("Mass type:");
		if (jComboBoxMOverZ == null) {
			jComboBoxMOverZ = new JComboBox(new String[] { M, MOVERZ });
			getControlList().add(jComboBoxMOverZ);
		}
		jComboBoxMOverZ.setSelectedIndex(0); // select
												// the
												// first
												// score
		jComboBoxMOverZ.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					frame.startShowingChart(evt.getSource());
				}
			}
		});
		jPanelAdditional3.setLayout(new BorderLayout());
		jPanelAdditional3.add(jlabel3, BorderLayout.LINE_START);
		jPanelAdditional3.add(jComboBoxMOverZ, BorderLayout.CENTER);
		return jPanelAdditional3;
	}

	public void enableProteinScoreNameControls(boolean b) {
		if (jComboBoxProteinScoreNames != null)
			jComboBoxProteinScoreNames.setEnabled(b);
	}

	public void enablePeptideScoreNameControls(boolean b) {
		if (jComboBoxPeptideScoreNames != null)
			jComboBoxPeptideScoreNames.setEnabled(b);
	}

	public PlotOrientation getPlotOrientation() {
		if (jComboBoxPlotOrientation != null) {
			if (jComboBoxPlotOrientation.getSelectedItem().equals(PlotOrientation.VERTICAL))
				return PlotOrientation.VERTICAL;
			else if (jComboBoxPlotOrientation.getSelectedItem().equals(PlotOrientation.HORIZONTAL))
				return PlotOrientation.HORIZONTAL;
		}
		// by default:
		return PlotOrientation.VERTICAL;
	}

	public Set<String> getProteinsInSample() {
		return proteinsInSample;
	}

	public int getHistogramBins() {
		if (jTextHistogramBin != null) {
			try {
				return Integer.valueOf(jTextHistogramBin.getText());
			} catch (NumberFormatException e) {

			}
		}
		// by default
		return 30;
	}

	public HistogramType getHistogramType() {
		if (jComboBoxHistogramType != null) {
			HistogramType histogramTypeString = (HistogramType) jComboBoxHistogramType.getSelectedItem();
			if (HistogramType.FREQUENCY.equals(histogramTypeString))
				return HistogramType.FREQUENCY;
			else if (HistogramType.RELATIVE_FREQUENCY.equals(histogramTypeString))
				return HistogramType.RELATIVE_FREQUENCY;
			else if (HistogramType.SCALE_AREA_TO_1.equals(histogramTypeString))
				return HistogramType.SCALE_AREA_TO_1;
		}
		// by default:
		return HistogramType.FREQUENCY;
	}

	public double getColorScale() {
		if (jComboBoxColorScale != null) {
			final String selectedItem = (String) jComboBoxColorScale.getSelectedItem();
			if (selectedItem.equals(LOGARITHMIC_SCALE))
				return HeatChart.SCALE_LOGARITHMIC;
			if (selectedItem.equals(LINEAR_SCALE))
				return HeatChart.SCALE_LINEAR;
			if (selectedItem.equals(EXPONENTIAL_SCALE))
				return HeatChart.SCALE_EXPONENTIAL;
		}
		// by default
		return HeatChart.SCALE_LINEAR;
	}

	public int getHeatMapThreshold() {
		if (jTextHeatMapThreshold != null) {
			try {
				return Integer.valueOf(jTextHeatMapThreshold.getText());
			} catch (NumberFormatException e) {

			}
		}
		// by default
		return 3;
	}

	public String getPeptideScoreName() {
		String scoreName = null;
		if (jComboBoxPeptideScoreNames != null) {
			try {
				scoreName = (String) jComboBoxPeptideScoreNames.getSelectedItem();
			} catch (Exception e) {

			}
		} else {
			log.warn("Peptide score name combo box is not present");
		}
		return scoreName;
	}

	public String getProteinScoreName() {

		String scoreName = null;
		if (jComboBoxProteinScoreNames != null) {
			try {
				scoreName = (String) jComboBoxProteinScoreNames.getSelectedItem();
			} catch (Exception e) {

			}
		} else {
			log.warn("Protein score name combo box is not present");
		}
		return scoreName;
	}

	public void disableAdditionalOptionControls(boolean b) {
		for (JComponent component : getControlList()) {
			component.setEnabled(b);
		}
		for (JCheckBox checkbox : idSetsJCheckBoxes.values()) {
			checkbox.setEnabled(b);
		}

	}

	public JPanel getExperimentsCheckboxes(boolean selectAll, int numSelected, boolean addColorChooser,
			DoSomethingToChangeColorInChart methodToChangeColorInChart) {
		log.info("Creating list of replicates...");
		JPanel jpanel = new JPanel();
		// jpanel.setLayout(new BoxLayout(jpanel, BoxLayout.PAGE_AXIS));
		jpanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		idSetsJCheckBoxes.clear();
		idSetsColors.clear();
		List<String> colors = getColors();
		int colorIndex = 1; // starts with 1, cause the 0 in not a color
		int numExperiment = 1;
		for (Experiment experiment : frame.experimentList.getExperiments()) {
			final String experimentName = experiment.getName();
			boolean selected = false;
			if (numExperiment <= numSelected || selectAll)
				selected = true;
			JCheckBox checkBox = new JCheckBox(experiment.getName(), selected);
			checkBox.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});

			idSetsJCheckBoxes.put(experimentName, checkBox);
			jpanel.add(checkBox, c);

			if (addColorChooser) {
				c.gridx = 1;
				Color initialColor = getColor(colors.get(colorIndex++ % colors.size()));
				JLabelColor colorChooser = new JLabelColor("<html>&nbsp;&nbsp;&nbsp;</html>",
						new JColorChooser(initialColor), idSetsColors, null, experimentName,
						methodToChangeColorInChart);
				colorChooser.addMouseListener(new MouseListener() {

					@Override
					public void mouseReleased(MouseEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void mousePressed(MouseEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void mouseExited(MouseEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void mouseEntered(MouseEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void mouseClicked(MouseEvent e) {
						openColorChooser(colorChooser.getJColorChooser());

					}
				});
				jpanel.add(colorChooser, c);
				c.gridx = 0;
			}
			c.gridy++;
			numExperiment++;

		}
		return jpanel;
	}

	public Map<String, JCheckBox> getIdSetsJCheckBoxes() {
		return idSetsJCheckBoxes;
	}

	public Map<String, Color> getIdSetsColors() {
		return idSetsColors;
	}

	public JPanel getReplicatesCheckboxes(boolean separateExperiments, boolean selectAll, int numSelected,
			boolean addColorChooser, DoSomethingToChangeColorInChart methodToChangeColorInChart) {
		log.info("Creating list of replicates...");
		JPanel jpanel = new JPanel();
		// jpanel.setLayout(new BoxLayout(jpanel, BoxLayout.PAGE_AXIS));
		jpanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		// c.insets = new Insets(10, 0, 10, 5);
		idSetsJCheckBoxes.clear();
		idSetsColors.clear();
		int numReplicates = 1;
		List<String> colors = getColors();
		int colorIndex = 1;
		for (Experiment experiment : frame.experimentList.getExperiments()) {
			JLabel labelExperiment = new JLabel(experiment.getName() + ":");

			c.gridwidth = 2;
			if (c.gridy > 0) {
				c.gridy++;
			}
			c.gridwidth = 2;
			jpanel.add(labelExperiment, c);
			c.gridy++;
			if (separateExperiments) {
				numReplicates = 1;
				colorIndex = 1;
			}
			for (Object idSet : experiment.getNextLevelIdentificationSetList()) {
				Replicate replicate = (Replicate) idSet;
				boolean selected = false;
				if (numReplicates <= numSelected || selectAll)
					selected = true;

				JCheckBox checkBox = new JCheckBox(replicate.getName(), selected);
				checkBox.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent evt) {
						frame.startShowingChart(evt.getSource());
					}
				});
				idSetsJCheckBoxes.put(replicate.getFullName(), checkBox);
				c.gridy++;
				c.gridwidth = 1;

				jpanel.add(checkBox, c);
				if (addColorChooser) {
					c.gridx = 1;
					Color initialColor = getColor(colors.get(colorIndex++ % colors.size()));
					String vennDataName = separateExperiments ? experiment.getName() : null;
					JLabelColor colorChooser = new JLabelColor("<html>&nbsp;&nbsp;&nbsp;</html>",
							new JColorChooser(initialColor), idSetsColors, vennDataName, replicate.getFullName(),
							methodToChangeColorInChart);
					colorChooser.addMouseListener(new MouseListener() {

						@Override
						public void mouseReleased(MouseEvent e) {
							// TODO Auto-generated method stub

						}

						@Override
						public void mousePressed(MouseEvent e) {
							// TODO Auto-generated method stub

						}

						@Override
						public void mouseExited(MouseEvent e) {
							// TODO Auto-generated method stub

						}

						@Override
						public void mouseEntered(MouseEvent e) {
							// TODO Auto-generated method stub

						}

						@Override
						public void mouseClicked(MouseEvent e) {
							openColorChooser(colorChooser.getJColorChooser());

						}
					});
					jpanel.add(colorChooser, c);
					c.gridx = 0;
				}
				numReplicates++;
			}

		}
		return jpanel;
	}

	protected void openColorChooser(JColorChooser colorChooser) {
		JDialog d = new JDialog(frame);
		d.add(colorChooser);
		d.pack();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		int x = (screenSize.width - d.getWidth()) / 2;
		int y = (screenSize.height - d.getHeight()) / 2;
		d.setLocation(x, y);
		d.setVisible(true);
	}

	public String[] getSelectedModifications() {
		try {
			// added because sometimes the jList was not ready to get the
			// selected values in "getSelectedModifications"
			Thread.sleep(100);
		} catch (InterruptedException e) {

		}
		if (jListModifications != null) {

			final Object[] selectedValues = jListModifications.getSelectedValues();
			if (selectedValues != null && selectedValues.length > 0) {
				String[] ret = new String[selectedValues.length];
				int i = 0;
				for (Object object : selectedValues) {
					ret[i] = (String) object;
					i++;
				}
				return ret;

			}
		}
		return null;
	}

	public List<String> getSelectedPeptides() {
		try {
			// added because sometimes the jList was not ready to get the
			// selected values in "getSelectedPeptides"
			Thread.sleep(100);
		} catch (InterruptedException e) {

		}
		if (jListPeptides != null) {

			final List<String> selectedValues = jListPeptides.jListPeptides.getSelectedValuesList();
			if (!selectedValues.isEmpty()) {
				return selectedValues;
			}
		}
		return null;
	}

	public boolean getAsPercentage() {
		if (jCheckBoxAsPercentage != null)
			return jCheckBoxAsPercentage.isSelected();
		return true;
	}

	public int getMaximumOccurrence() {
		if (jComboBoxMaximumOccurrence != null) {
			return (Integer) jComboBoxMaximumOccurrence.getSelectedItem();
		}
		return Integer.MAX_VALUE;
	}

	public int getMinimumOccurrence() {
		if (jComboBoxMinimumOccurrence != null) {
			return (Integer) jComboBoxMinimumOccurrence.getSelectedItem();
		}
		return 0;
	}

	public boolean getMOverZ() {
		if (jComboBoxMOverZ != null) {
			String selection = (String) jComboBoxMOverZ.getSelectedItem();
			if (selection.equals(MOVERZ)) {
				return true;
			} else if (selection.equals(M)) {
				return false;
			}
		}
		return false;
	}

	public boolean getOverReplicates() {
		if (jCheckBoxOverReplicates != null)
			return jCheckBoxOverReplicates.isSelected();
		return false;
	}

	public boolean isAccuracy() {
		if (jCheckBoxAccuracy != null)
			return jCheckBoxAccuracy.isSelected();
		return false;
	}

	public boolean isSpecificity() {
		if (jCheckBoxSpecificity != null)
			return jCheckBoxSpecificity.isSelected();
		return false;
	}

	public boolean isPrecision() {
		if (jCheckBoxPrecision != null)
			return jCheckBoxPrecision.isSelected();
		return false;
	}

	public boolean isNPV() {
		if (jCheckBoxNPV != null)
			return jCheckBoxNPV.isSelected();
		return false;
	}

	public boolean isFDR() {
		if (jCheckBoxFDR != null)
			return jCheckBoxFDR.isSelected();
		return false;
	}

	public boolean showAsStackedChart() {
		if (jCheckBoxShowAsStackedChart != null) {
			return jCheckBoxShowAsStackedChart.isSelected();
		}
		return false;
	}

	public boolean isSensitivity() {
		if (jCheckBoxSensitivity != null)
			return jCheckBoxSensitivity.isSelected();
		return false;
	}

	public void updatePeptideSequenceList() {
		boolean distiguishModificatedPeptides = frame.distinguishModifiedPeptides();
		if (jListPeptides != null) {
			int size = jListPeptides.jListPeptides.getModel().getSize();
			log.info("Updating peptide list of " + size + " elements");
			final List selectedValues = jListPeptides.jListPeptides.getSelectedValuesList();
			jListPeptides.jListPeptides.setListData(frame.getPeptidesFromExperiments(distiguishModificatedPeptides));
			size = jListPeptides.jListPeptides.getModel().getSize();
			log.info("Now has " + size + " elements");
			jlabelPeptideListHeader.setText(size + " peptide sequences:");
			if (selectedValues != null && !selectedValues.isEmpty()) {
				List<Integer> selectedIndexes = new ArrayList<Integer>();
				for (Object object : selectedValues) {
					String sequence = (String) object;
					for (int i = 0; i < size; i++) {
						String listSequence = jListPeptides.jListPeptides.getModel().getElementAt(i);
						if (sequence.equals(listSequence))
							selectedIndexes.add(i);

					}

				}
				if (!selectedIndexes.isEmpty()) {
					final int[] array = DatasetFactory.toArray(selectedIndexes);
					jListPeptides.jListPeptides.setSelectedIndices(array);
				}
			}
		}
	}

	private void updateModificationList() {
		if (jListModifications != null) {
			int size = jListModifications.getModel().getSize();
			log.info("Updating modification list of " + size + " elements");
			final Object[] selectedValues = jListModifications.getSelectedValues();
			jListModifications.setListData(getModifications());
			size = jListModifications.getModel().getSize();
			log.info("Now has " + size + " elements");
			jLabelModificationListHeader.setText(size + " PTMs:");
			if (selectedValues != null && selectedValues.length > 0) {
				List<Integer> selectedIndexes = new ArrayList<Integer>();
				for (Object object : selectedValues) {
					String modif = (String) object;
					for (int i = 0; i < size; i++) {
						String modification = (String) jListModifications.getModel().getElementAt(i);
						if (modif.equals(modification))
							selectedIndexes.add(i);
					}

				}
				if (!selectedIndexes.isEmpty()) {
					final int[] array = DatasetFactory.toArray(selectedIndexes);
					jListModifications.setSelectedIndices(array);
				}
			}
		}
	}

	public JButton getSaveButton() {
		// Add save image buttion
		if (jbuttonSaveImage == null) {
			jbuttonSaveImage = new JButton("Save image");
			getControlList().add(jbuttonSaveImage);
			jbuttonSaveImage.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					frame.saveHeatMapImage();
				}
			});
		}
		return jbuttonSaveImage;
	}

	public JCheckBox getShowAsPieChartCheckBox() {

		if (jCheckBoxShowAsPieChart == null) {
			jCheckBoxShowAsPieChart = new JCheckBox("Show as pie chart");
			// controlList.add(jCheckBoxShowAsPieChart);not add becaouse
			// sometimes has to be disabled
		}

		jCheckBoxShowAsPieChart.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				AdditionalOptionsPanelFactory.this.enableAsPercentage(!jCheckBoxShowAsPieChart.isSelected());
				AdditionalOptionsPanelFactory.this.enableShowAsStackedChart(!jCheckBoxShowAsPieChart.isSelected());
				AdditionalOptionsPanelFactory.this.enableShowTotalSerie(!jCheckBoxShowAsPieChart.isSelected());
				AdditionalOptionsPanelFactory.this
						.enableShowTotalVersusDifferent(!jCheckBoxShowAsPieChart.isSelected());
				frame.startShowingChart(evt.getSource());
			}
		});

		return jCheckBoxShowAsPieChart;
	}

	public JCheckBox getShowAverageOverReplicatesCheckBox() {

		if (jCheckBoxShowAverage == null) {
			jCheckBoxShowAverage = new JCheckBox("Show as average next level");
			// controlList.add(jCheckBoxShowAsPieChart);not add becaouse
			// sometimes has to be disabled
		}

		jCheckBoxShowAverage.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				frame.startShowingChart(evt.getSource());
			}
		});

		return jCheckBoxShowAverage;
	}

	public boolean showAsPieChart() {
		if (jCheckBoxShowAsPieChart != null) {
			return jCheckBoxShowAsPieChart.isSelected();
		}
		return false;
	}

	public JPanel getLabelledModificationsPanel() {
		JPanel jPanel = new JPanel();
		JLabel jlabel = new JLabel(
				"<html>Select two modifications from the following<br> comboboxes in order to show the number<br>of peptides that have been detected<br>containing the two variants:</html>");
		if (jComboBoxModificationA == null) {
			jComboBoxModificationA = new JComboBox();
			getControlList().add(jComboBoxModificationA);
		}
		final String[] modifications = getModifications();
		jComboBoxModificationA.removeAllItems();
		jComboBoxModificationA.addItem(NO_MODIFICATION);
		for (String modif : modifications) {
			jComboBoxModificationA.addItem(modif);
		}
		jComboBoxModificationA.setSelectedIndex(0); // select
		jComboBoxModificationA.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					frame.startShowingChart(evt.getSource());
				}
			}
		});

		if (jComboBoxModificationB == null) {
			jComboBoxModificationB = new JComboBox();
			getControlList().add(jComboBoxModificationB);
		}
		jComboBoxModificationB.removeAllItems();
		jComboBoxModificationB.addItem(NO_MODIFICATION);
		for (String modif : modifications) {
			jComboBoxModificationB.addItem(modif);
		}
		jComboBoxModificationB.setSelectedIndex(0); // select
		jComboBoxModificationB.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					frame.startShowingChart(evt.getSource());
				}
			}
		});
		jPanel.setLayout(new GridBagLayout());
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.insets = new Insets(10, 10, 0, 0);
		jPanel.add(jlabel, gridBagConstraints);
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		jPanel.add(new JLabel("Modif A:"), gridBagConstraints);
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		jPanel.add(jComboBoxModificationA, gridBagConstraints);
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		jPanel.add(new JLabel("Modif B:"), gridBagConstraints);
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 3;
		jPanel.add(jComboBoxModificationB, gridBagConstraints);
		return jPanel;
	}

	public String getModificationA() {
		if (jComboBoxModificationA != null)
			return (String) jComboBoxModificationA.getSelectedItem();
		return null;
	}

	public String getModificationB() {
		if (jComboBoxModificationB != null)
			return (String) jComboBoxModificationB.getSelectedItem();
		return null;
	}

	public boolean showAverageOverReplicates() {
		if (jCheckBoxShowAverage != null) {
			return jCheckBoxShowAverage.isSelected();
		}
		return false;
	}

	public JCheckBox getShowTotalSerieCheckBox(boolean takeIntoAccountPieAndStackedViews) {
		if (takeIntoAccountPieAndStackedViews) {
			if (jCheckBoxShowAsPieChart != null && jCheckBoxShowAsPieChart.isSelected())
				return null;
			if (jCheckBoxShowAsStackedChart != null && jCheckBoxShowAsStackedChart.isSelected())
				return null;
		}

		if (jCheckBoxShowTotalSerie == null) {
			jCheckBoxShowTotalSerie = new JCheckBox("Show total series");
			jCheckBoxShowTotalSerie.setToolTipText("Show the next upper integration level series");
			// controlList.add(jCheckBoxShowAsPieChart);not add becaouse
			// sometimes has to be disabled

			jCheckBoxShowTotalSerie.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});
		}

		return jCheckBoxShowTotalSerie;
	}

	public boolean isTotalSerieShown() {
		if (jCheckBoxShowTotalSerie != null) {
			return jCheckBoxShowTotalSerie.isSelected();
		}
		return false;
	}

	public JCheckBox getShowDifferentIdentificationsCheckBox(String text) {

		if (jCheckBoxShowDifferentIdentifications == null) {
			jCheckBoxShowDifferentIdentifications = new JCheckBox(text);
			// controlList.add(jCheckBoxShowAsPieChart);not add becaouse
			// sometimes has to be disabled
		}
		jCheckBoxShowDifferentIdentifications.setText(text);
		jCheckBoxShowDifferentIdentifications.setSelected(true);
		jCheckBoxShowDifferentIdentifications.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				AdditionalOptionsPanelFactory.this
						.enableShowTotalVersusDifferent(!jCheckBoxShowDifferentIdentifications.isSelected());
				frame.startShowingChart(evt.getSource());
			}
		});

		return jCheckBoxShowDifferentIdentifications;
	}

	public boolean isDifferentIdentificationsShown() {
		if (jCheckBoxShowDifferentIdentifications != null) {
			return jCheckBoxShowDifferentIdentifications.isSelected();
		}
		return false;
	}

	public JCheckBox getShowTotalVersusDifferentCheckBox() {

		if (jCheckBoxTotalVersusDifferent == null) {
			jCheckBoxTotalVersusDifferent = new JCheckBox("Show different/total ratio");
			// controlList.add(jCheckBoxShowAsPieChart);not add becaouse
			// sometimes has to be disabled
		}
		jCheckBoxTotalVersusDifferent.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				AdditionalOptionsPanelFactory.this
						.enableShowDifferentIdentifications(!jCheckBoxTotalVersusDifferent.isSelected());
				AdditionalOptionsPanelFactory.this
						.enableShowAsStackedChart(!jCheckBoxTotalVersusDifferent.isSelected());
				AdditionalOptionsPanelFactory.this.enableShowAsPieChart(!jCheckBoxTotalVersusDifferent.isSelected());
				AdditionalOptionsPanelFactory.this
						.enableShowAsAverageOfNextLevels(!jCheckBoxTotalVersusDifferent.isSelected());
				frame.startShowingChart(evt.getSource());
			}
		});

		return jCheckBoxTotalVersusDifferent;
	}

	protected void enableShowAsAverageOfNextLevels(boolean b) {
		if (jCheckBoxShowAverage != null) {
			this.jCheckBoxShowAverage.setEnabled(b);
		}

	}

	protected void enableShowTotalVersusDifferent(boolean b) {
		if (jCheckBoxTotalVersusDifferent != null) {
			jCheckBoxTotalVersusDifferent.setEnabled(b);
			if (!b)
				jCheckBoxTotalVersusDifferent.setSelected(b);
		}

	}

	protected void enableShowDifferentIdentifications(boolean b) {
		if (jCheckBoxShowDifferentIdentifications != null) {
			jCheckBoxShowDifferentIdentifications.setEnabled(b);
			if (!b)
				jCheckBoxShowDifferentIdentifications.setSelected(b);
		}

	}

	public boolean isTotalVersusDifferentSelected() {
		if (jCheckBoxTotalVersusDifferent != null) {
			return jCheckBoxTotalVersusDifferent.isSelected();
		}
		return false;
	}

	public JLabel getJLabelIntersectionsText() {
		if (jLabelIntersectionsText == null) {
			jLabelIntersectionsText = new JLabel();
			getControlList().add(jLabelIntersectionsText);
		}
		return jLabelIntersectionsText;
	}

	public void setIntersectionText(String text) {
		if (text == null) {
			// getJLabelIntersectionsText().setText(null);
		} else {
			getJLabelIntersectionsText().setText("<html>" + text + "</html>");
			getJLabelIntersectionsText().setToolTipText(getJLabelIntersectionsText().getText());
		}

		Container parent = getJLabelIntersectionsText().getParent();
		if (parent != null) {
			parent.repaint();
			Container parent2 = parent.getParent();
			if (parent2 != null) {
				parent2.repaint();
			}
		}

	}

	public JPanel getChr16MappingControls() {
		JPanel jPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;

		c.gridwidth = 2;
		c.gridy = 0;
		jPanel.add(new JLabel("<html><b>Note</b>: This chart is specially designed for the<br>"
				+ "spanish participants in the HPP consortium that<br>" + "analyse the Human Chromosome 16." + "<br>"
				+ "<b>Note 2</b>: Number of genes can be different from<br>"
				+ "number of proteins, since isoforms are considered<br>" + "as different proteins<br><br>"
				+ "</html>"), c);

		c.gridwidth = 1;
		c.gridy++;
		c.gridx = 0;

		JPanel proteinOrGeneSelectorPanel = getProteinOrGeneSelector();

		jPanel.add(proteinOrGeneSelectorPanel, c);

		c.gridx = 0;
		c.gridwidth = 1;

		c.gridy++;
		JLabel jLabel2 = new JLabel("Show known/unknown:");
		String text2 = "Show just the genes or proteins classified by the SHPP as known, as unknown or both";
		jLabel2.setToolTipText(text2);
		jPanel.add(jLabel2, c);
		if (known_unknown == null) {
			known_unknown = new JComboBox(new DefaultComboBoxModel(new String[] { BOTH, KNOWN, UNKNOWN }));
			known_unknown.setToolTipText(text2);
			known_unknown.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});
		}
		c.gridx = 1;
		jPanel.add(known_unknown, c);

		c.gridx = 0;
		if (notAssigned == null) {
			notAssigned = new JCheckBox("show not assigned");
			notAssigned.setToolTipText("Show genes or protein products not assigned to any research group");
			notAssigned.setSelected(true);
			notAssigned.addItemListener(new java.awt.event.ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});
		}
		c.gridy++;

		jPanel.add(notAssigned, c);

		c.gridy++;

		jPanel.add(getTakeGeneFromFirstProteinCheckbox(), c);

		c.gridx = 0;
		c.gridwidth = 2;
		c.gridy++;
		jPanel.add(new JLabel("Groups:"), c);

		if (assignedGroups.isEmpty()) {
			List<String> assignedGroupsStrings = GeneDistributionReader.getInstance().getAssignedGroupsNames();

			for (String group : assignedGroupsStrings) {
				JCheckBox checkBox = new JCheckBox(group);
				checkBox.setSelected(true);
				checkBox.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent evt) {
						frame.startShowingChart(evt.getSource());
					}
				});
				assignedGroups.add(checkBox);
			}
		}
		c.gridwidth = 1;
		boolean left = true;
		for (JCheckBox jcheckBox : assignedGroups) {

			if (left) {
				c.gridy++;
				c.gridx = 0;
			} else {
				c.gridx = 1;
			}
			left = !left;
			jPanel.add(jcheckBox, c);
		}
		c.gridx = 1;
		c.gridwidth = 2;
		// Filter button
		JLabel label = new JLabel("<html><br><br>Click here for just filter Chr16 proteins</html>");
		JButton button = new JButton("Filter Chr16 proteins");
		button.addActionListener(new java.awt.event.ActionListener() {

			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				frame.exportChr16Proteins();
			}

		});
		// c.gridy++;
		c.gridx = 0;
		jPanel.add(label, c);
		c.gridy++;
		jPanel.add(button, c);

		return jPanel;
	}

	public List<String> getGroupToShow() {
		List<String> ret = new ArrayList<String>();
		if (assignedGroups != null) {
			for (JCheckBox jcheckBox : assignedGroups) {
				if (jcheckBox.isSelected())
					ret.add(jcheckBox.getText());
			}
		}
		log.info(ret.size() + " groups selected");
		return ret;
	}

	public boolean isNotAssignedShowed() {
		if (notAssigned != null) {
			return notAssigned.isSelected();
		}
		return false;
	}

	public String getProteinOrGene() {
		if (proteinOrGeneSelector != null) {
			return (String) proteinOrGeneSelector.getSelectedItem();
		}
		return null;
	}

	public String getPeptideOrPSM() {
		if (peptideOrPSMSelector != null) {
			return (String) peptideOrPSMSelector.getSelectedItem();
		}
		return null;
	}

	public String getChr16KnownOrUnknown() {
		if (known_unknown != null)
			return (String) known_unknown.getSelectedItem();
		return null;
	}

	public JCheckBox getShowPSMCheckBox() {

		if (jCheckBoxShowPSM == null) {
			jCheckBoxShowPSM = new JCheckBox("Show PSMs");
			jCheckBoxShowPSM.setSelected(true);

			getControlList().add(jCheckBoxShowPSM);
		}

		jCheckBoxShowPSM.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				frame.startShowingChart(evt.getSource());
			}
		});

		return jCheckBoxShowPSM;
	}

	public JCheckBox getShowPeptidesCheckBox() {

		if (jCheckBoxShowPeptides == null) {
			jCheckBoxShowPeptides = new JCheckBox("Show Peptides");
			jCheckBoxShowPeptides.setSelected(true);

			getControlList().add(jCheckBoxShowPeptides);
		}

		jCheckBoxShowPeptides.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				frame.startShowingChart(evt.getSource());
			}
		});

		return jCheckBoxShowPeptides;
	}

	public JCheckBox getShowPeptidesPlusChargeCheckBox() {

		if (jCheckBoxShowPeptidesPlusCharge == null) {
			jCheckBoxShowPeptidesPlusCharge = new JCheckBox("Show Peptides (diff by Z)");
			jCheckBoxShowPeptidesPlusCharge.setSelected(true);

			getControlList().add(jCheckBoxShowPeptidesPlusCharge);
		}

		jCheckBoxShowPeptidesPlusCharge.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				frame.startShowingChart(evt.getSource());
			}
		});

		return jCheckBoxShowPeptidesPlusCharge;
	}

	public JCheckBox getShowProteinsCheckBox() {

		if (jCheckBoxShowProteins == null) {
			jCheckBoxShowProteins = new JCheckBox("Show Proteins");
			jCheckBoxShowProteins.setSelected(true);
			getControlList().add(jCheckBoxShowProteins);
		}

		jCheckBoxShowProteins.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent evt) {

				frame.startShowingChart(jCheckBoxShowProteins);
			}
		});
		return jCheckBoxShowProteins;
	}

	public JCheckBox getShowScoreVsFDRCheckBox() {

		if (jCheckBoxShowScoreVsFDR == null) {
			jCheckBoxShowScoreVsFDR = new JCheckBox("Show Score vs Num. proteins");
			jCheckBoxShowScoreVsFDR.setSelected(true);
			getControlList().add(jCheckBoxShowScoreVsFDR);
		}

		jCheckBoxShowScoreVsFDR.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				frame.startShowingChart(jCheckBoxShowScoreVsFDR);
			}
		});

		return jCheckBoxShowScoreVsFDR;
	}

	public boolean showScoreVsFDR() {
		if (jCheckBoxShowScoreVsFDR != null) {
			return jCheckBoxShowScoreVsFDR.isSelected();
		}
		return false;
	}

	public boolean showPSMs() {
		if (jCheckBoxShowPSM != null) {
			return jCheckBoxShowPSM.isSelected();
		}
		return false;
	}

	public boolean showPeptides() {
		if (jCheckBoxShowPeptides != null) {
			return jCheckBoxShowPeptides.isSelected();
		}
		return false;
	}

	public boolean showPeptidesPlusCharge() {
		if (jCheckBoxShowPeptidesPlusCharge != null) {
			return jCheckBoxShowPeptidesPlusCharge.isSelected();
		}
		return false;
	}

	public boolean showProteins() {
		if (jCheckBoxShowProteins != null) {
			return jCheckBoxShowProteins.isSelected();
		}
		return false;
	}

	public JCheckBox getShowTotalChromosomeProteins() {
		if (jCheckBoxShowTotalChromosomeProteins == null) {
			jCheckBoxShowTotalChromosomeProteins = new JCheckBox("Show total prots. from chrms.");
			jCheckBoxShowTotalChromosomeProteins.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					frame.startShowingChart(jCheckBoxShowTotalChromosomeProteins);
				}
			});
		}
		return jCheckBoxShowTotalChromosomeProteins;
	}

	public boolean isShownTotalChromosomeProteins() {
		if (jCheckBoxShowTotalChromosomeProteins != null) {
			return jCheckBoxShowTotalChromosomeProteins.isSelected();
		}
		return false;
	}

	public JCheckBox getHeatMapBinaryCheckBox(boolean b) {
		if (jcheckBoxHeatMapBinary == null) {
			jcheckBoxHeatMapBinary = new JCheckBox("presence/absence");
			jcheckBoxHeatMapBinary.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					frame.startShowingChart(evt.getSource());
					// Disable jcombobox of color scale if binary is
					// selected
					if (jComboBoxColorScale != null) {
						jComboBoxColorScale.setEnabled(((JCheckBox) evt.getSource()).isSelected());
					}
				}
			});
			jcheckBoxHeatMapBinary.setToolTipText(
					"If checked, the heatmap will show only two different values: present (red) or not present (green)");
		}
		getControlList().add(jcheckBoxHeatMapBinary);
		return jcheckBoxHeatMapBinary;
	}

	public boolean isHeatMapBinary() {
		if (jcheckBoxHeatMapBinary != null)
			return jcheckBoxHeatMapBinary.isSelected();
		return false;
	}

	public JPanel getProteinOrGeneSelector() {
		String string = "Show just the number of genes, the number of proteins or both";
		JLabel jLabel = new JLabel("Proteins/Genes:");
		jLabel.setToolTipText(string);
		JPanel jPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		jPanel.add(jLabel, c);
		if (proteinOrGeneSelector == null) {
			proteinOrGeneSelector = new JComboBox(new DefaultComboBoxModel(new String[] { BOTH, PROTEIN, GENES }));
			proteinOrGeneSelector.setToolTipText(string);
			proteinOrGeneSelector.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});
		}

		c.gridx++;
		jPanel.add(proteinOrGeneSelector, c);
		return jPanel;
	}

	public JPanel getPeptideOrPSMSelector() {
		String string = "Show just the number of peptides, the number of PSMs or both";
		JLabel jLabel = new JLabel("Peptides/PSMs:");
		jLabel.setToolTipText(string);
		JPanel jPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		jPanel.add(jLabel, c);
		if (peptideOrPSMSelector == null) {
			peptideOrPSMSelector = new JComboBox(new DefaultComboBoxModel(new String[] { BOTH, PEPTIDE, PSM }));
			peptideOrPSMSelector.setToolTipText(string);
			peptideOrPSMSelector.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});
		}

		c.gridx++;
		jPanel.add(peptideOrPSMSelector, c);
		return jPanel;
	}

	public JPanel getJcheckBoxOneProteinPerGroup() {
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.PAGE_AXIS));

		if (jradioButtonFirstProteinPerGroup == null || jradioButtonAllProteinsPerGroup == null
				|| jradioButtonBestProteinPerGroup == null || jradioButtonShareOneProtein == null) {
			jradioButtonAllProteinsPerGroup = new JRadioButton("share all proteins", true);
			jradioButtonAllProteinsPerGroup.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});
			this.getControlList().add(jradioButtonAllProteinsPerGroup);
			jradioButtonFirstProteinPerGroup = new JRadioButton("share the protein with more evidence");
			jradioButtonFirstProteinPerGroup.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});
			this.getControlList().add(jradioButtonFirstProteinPerGroup);
			jradioButtonBestProteinPerGroup = new JRadioButton("share the best protein per group");
			jradioButtonBestProteinPerGroup.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});
			this.getControlList().add(jradioButtonBestProteinPerGroup);
			jradioButtonShareOneProtein = new JRadioButton("share any protein");
			jradioButtonShareOneProtein.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});
			getControlList().add(jradioButtonShareOneProtein);
			ButtonGroup group = new ButtonGroup();
			group.add(jradioButtonAllProteinsPerGroup);
			group.add(jradioButtonBestProteinPerGroup);
			group.add(jradioButtonFirstProteinPerGroup);
			group.add(jradioButtonShareOneProtein);
		}
		jradioButtonAllProteinsPerGroup.setToolTipText(
				"<html>Protein groups having <b>all the proteins in common</b> are considered as equivalent.</html>");
		jradioButtonFirstProteinPerGroup.setToolTipText(
				"<html>Protein groups having <b>the protein with more evidence in common</b> are considered as equivalent.<br>"
						+ "(the protein with more evidence in the group is the one with a unique peptide, or having more peptides or more PSMs).</html>");

		jradioButtonBestProteinPerGroup.setToolTipText(
				"<html>Protein groups having the same <b>best protein</b> are considered as equivalent.<br>"
						+ "(the best protein is the the protein containing the best score if available, or<br>"
						+ "the one containing the best peptide, taking into account the peptide score if recognized).</html>");
		jradioButtonShareOneProtein.setToolTipText(
				"<html>Protein groups having <b>at least one protein in common </b>are considered as equivalent.</html>");

		jPanel.add(jradioButtonAllProteinsPerGroup);
		jPanel.add(jradioButtonFirstProteinPerGroup);
		jPanel.add(jradioButtonBestProteinPerGroup);
		jPanel.add(jradioButtonShareOneProtein);
		return jPanel;
	}

	public ProteinGroupComparisonType getProteinGroupComparisonType() {
		if (jradioButtonFirstProteinPerGroup != null && jradioButtonFirstProteinPerGroup.isSelected())
			return ProteinGroupComparisonType.HIGHER_EVIDENCE_PROTEIN;
		if (jradioButtonAllProteinsPerGroup != null && jradioButtonAllProteinsPerGroup.isSelected())
			return ProteinGroupComparisonType.ALL_PROTEINS;
		if (jradioButtonBestProteinPerGroup != null && jradioButtonBestProteinPerGroup.isSelected())
			return ProteinGroupComparisonType.BEST_PROTEIN;
		if (jradioButtonShareOneProtein != null && jradioButtonShareOneProtein.isSelected())
			return ProteinGroupComparisonType.SHARE_ONE_PROTEIN;
		return ProteinGroupComparisonType.ALL_PROTEINS;
	}

	public boolean isFirstProteinPerGroupSelected() {
		if (jradioButtonFirstProteinPerGroup != null)
			return jradioButtonFirstProteinPerGroup.isSelected();
		return false;
	}

	public boolean isAllProteinsPerGroupSelected() {
		if (jradioButtonAllProteinsPerGroup != null)
			return jradioButtonAllProteinsPerGroup.isSelected();
		return false;
	}

	public boolean isBestProteinPerGroupSelected() {
		if (jradioButtonBestProteinPerGroup != null)
			return jradioButtonBestProteinPerGroup.isSelected();
		return false;
	}

	public boolean isShareOneProteinSelected() {
		if (jradioButtonShareOneProtein != null)
			return jradioButtonShareOneProtein.isSelected();
		return false;
	}

	public boolean isShowAsSpiderPlot() {
		if (jcheckBoxShowSpiderPlot != null)
			return jcheckBoxShowSpiderPlot.isSelected();
		return false;
	}

	public JPanel getShowAsSpiderPlotCheckBox() {
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.PAGE_AXIS));

		if (jcheckBoxShowSpiderPlot == null) {
			jcheckBoxShowSpiderPlot = new JCheckBox("show spider plot");
			jcheckBoxShowSpiderPlot.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});

		}

		jPanel.add(jcheckBoxShowSpiderPlot);

		return jPanel;
	}

	public JCheckBox getAccumulativeTrendCheckBox() {
		if (jcheckBoxAccumulativeTrend == null) {
			jcheckBoxAccumulativeTrend = new JCheckBox("show accumulative trend");
			jcheckBoxAccumulativeTrend.setSelected(true);
			getControlList().add(jcheckBoxAccumulativeTrend);
			jcheckBoxAccumulativeTrend.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});

		}
		return jcheckBoxAccumulativeTrend;
	}

	public boolean isAccumulativeTrendSelected() {
		if (jcheckBoxAccumulativeTrend == null)
			return false;
		return jcheckBoxAccumulativeTrend.isSelected();
	}

	public JCheckBox getTakeGeneFromFirstProteinCheckbox() {
		if (jCheckBoxTakeGeneFromFirstProtein == null) {
			jCheckBoxTakeGeneFromFirstProtein = new JCheckBox("one gene per protein group");
			jCheckBoxTakeGeneFromFirstProtein.setToolTipText("<html>Protein groups may be mapped to multiple genes.<br>"
					+ "If this option is selected, only one gene per protein group will be used in the chart.</html>");
			jCheckBoxTakeGeneFromFirstProtein.setSelected(true);
			getControlList().add(jCheckBoxTakeGeneFromFirstProtein);
			jCheckBoxTakeGeneFromFirstProtein.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});

		}
		return jCheckBoxTakeGeneFromFirstProtein;
	}

	public boolean isTakeGeneFromFirstProteinSelected() {
		if (jCheckBoxTakeGeneFromFirstProtein != null)
			return jCheckBoxTakeGeneFromFirstProtein.isSelected();
		return false;
	}

	public JPanel getFDRCheckBoxesPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		if (jCheckBoxShowProteinFDR == null) {
			jCheckBoxShowProteinFDR = new JCheckBox("show FDR at protein level");
			getControlList().add(jCheckBoxShowProteinFDR);
			jCheckBoxShowProteinFDR.setSelected(true);

			jCheckBoxShowProteinFDR.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});
		}
		c.gridx = 0;
		c.gridy = 0;
		panel.add(jCheckBoxShowProteinFDR, c);
		if (jCheckBoxShowPeptideFDR == null) {
			jCheckBoxShowPeptideFDR = new JCheckBox("show FDR at peptide level");
			getControlList().add(jCheckBoxShowPeptideFDR);
			jCheckBoxShowPeptideFDR.addItemListener(new java.awt.event.ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});
		}
		c.gridy++;
		panel.add(jCheckBoxShowPeptideFDR, c);
		if (jCheckBoxShowPSMFDR == null) {
			jCheckBoxShowPSMFDR = new JCheckBox("show FDR at PSM level");
			getControlList().add(jCheckBoxShowPSMFDR);
			jCheckBoxShowPSMFDR.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});
		}
		c.gridy++;
		panel.add(jCheckBoxShowPSMFDR, c);

		return panel;

	}

	public boolean showProteinFDRLevel() {
		if (jCheckBoxShowProteinFDR == null)
			return false;
		return jCheckBoxShowProteinFDR.isSelected();
	}

	public boolean showPeptideFDRLevel() {
		if (jCheckBoxShowPeptideFDR == null)
			return false;
		return jCheckBoxShowPeptideFDR.isSelected();
	}

	public boolean showPSMFDRLevel() {
		if (jCheckBoxShowPSMFDR == null)
			return false;
		return jCheckBoxShowPSMFDR.isSelected();
	}

	public JComboBox getFontComboBox() {
		if (jComboBoxFont == null) {
			jComboBoxFont = new JComboBox();

			getControlList().add(jComboBoxFont);
			jComboBoxFont.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});
		}
		jComboBoxFont.setModel(new DefaultComboBoxModel(WordCramChart.getFonts()));
		return jComboBoxFont;
	}

	public String getFont() {
		if (jComboBoxFont != null) {
			return (String) jComboBoxFont.getSelectedItem();
		}
		return null;
	}

	public JTextField getMaxNumberWordsText() {
		if (jTextFieldMaxNumberWords == null) {
			jTextFieldMaxNumberWords = new JTextField(10);
			getControlList().add(jTextFieldMaxNumberWords);
			jTextFieldMaxNumberWords.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});
			jTextFieldMaxNumberWords.setText("100");
		}
		return jTextFieldMaxNumberWords;
	}

	public int getMaxNumberWords() {
		if (jTextFieldMaxNumberWords != null) {
			try {
				return Integer.valueOf(jTextFieldMaxNumberWords.getText());
			} catch (NumberFormatException e) {

			}
		}
		return 1000;
	}

	public JTextField getMinWordLengthText() {
		if (jTextFieldMinWordLength == null) {
			jTextFieldMinWordLength = new JTextField(10);
			getControlList().add(jTextFieldMinWordLength);
			jTextFieldMinWordLength.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});
			jTextFieldMinWordLength.setText("4");
		}
		return jTextFieldMinWordLength;
	}

	public int getMinWordLength() {
		if (jTextFieldMinWordLength != null) {
			try {
				return Integer.valueOf(jTextFieldMinWordLength.getText());
			} catch (NumberFormatException e) {

			}
		}
		return 4;
	}

	public JTextArea getSkipWordsTextArea() {
		if (jTextAreaSkipWords == null) {
			jTextAreaSkipWords = new JTextArea(10, 20);
			final List<String> defaultSkippedWords = WordCramChart.getDefaultSkippedWords();
			SorterUtil.sortStringByLength(defaultSkippedWords, false);
			StringBuilder sb = new StringBuilder();
			int minWordLength = getMinWordLength();
			for (String string : defaultSkippedWords) {
				if (string.length() >= minWordLength)
					sb.append(string + "\n");
			}
			jTextAreaSkipWords.setText(sb.toString());
			getControlList().add(jTextAreaSkipWords);
		}
		jTextAreaSkipWords.setRows(5);
		return jTextAreaSkipWords;
	}

	public List<String> getSkipWords() {
		List<String> list = new ArrayList<String>();
		if (jTextAreaSkipWords != null) {
			final String text = jTextAreaSkipWords.getText();
			if (text.contains("\n")) {
				final String[] split = text.split("\n");
				for (String string : split) {
					list.add(string);
				}
			}
		}
		return list;
	}

	public JButton getDrawWordCramButton() {
		if (jButtonDrawWordCram == null) {
			jButtonDrawWordCram = new JButton("Redraw");
			getControlList().add(jButtonDrawWordCram);
			jButtonDrawWordCram.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});
		}
		return jButtonDrawWordCram;
	}

	public JButton getSaveDrawWordCramButton() {
		if (jButtonSaveDrawWordCram == null) {
			jButtonSaveDrawWordCram = new JButton("Save image to file");
			getControlList().add(jButtonSaveDrawWordCram);
			jButtonSaveDrawWordCram.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {

					frame.saveWordCramImage();
				}
			});
		}
		return jButtonSaveDrawWordCram;
	}

	public JLabel getJLabelSelectedWord() {
		if (jLabelSelectedWord == null) {
			jLabelSelectedWord = new JLabel("(selected word)");
			getControlList().add(jLabelSelectedWord);
		}
		final Font previousFont = jLabelSelectedWord.getFont();
		jLabelSelectedWord.setFont(new Font(previousFont.getName(), Font.BOLD, previousFont.getSize()));
		return jLabelSelectedWord;
	}

	public JLabel getJLabelSelectedProteins() {
		if (jLabelSelectedProteins == null) {
			jLabelSelectedProteins = new JLabel("(proteins containing selected word)");
			getControlList().add(jLabelSelectedProteins);

		}
		final Font previousFont = jLabelSelectedProteins.getFont();
		jLabelSelectedProteins.setFont(new Font(previousFont.getName(), Font.BOLD, previousFont.getSize()));
		return jLabelSelectedProteins;
	}

	public JCheckBox getApplyLogCheckBox() {
		if (jCheckBoxApplyLog == null) {
			jCheckBoxApplyLog = new JCheckBox("Apply logs");
			jCheckBoxApplyLog.setToolTipText("Apply logarithm to base=10 to values");
			jCheckBoxApplyLog.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});
			getControlList().add(jCheckBoxApplyLog);
		}
		return jCheckBoxApplyLog;
	}

	public boolean isApplyLog() {
		if (jCheckBoxApplyLog != null) {
			return jCheckBoxApplyLog.isSelected();
		}
		return false;
	}

	public JCheckBox getSeparatedDecoyHitsCheckBox() {
		if (jCheckBoxSeparatedDecoyHits == null) {
			jCheckBoxSeparatedDecoyHits = new JCheckBox("Separate decoy hits");
			jCheckBoxSeparatedDecoyHits.setToolTipText("Show a separate series for decoy hits");
			jCheckBoxSeparatedDecoyHits.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});
			getControlList().add(jCheckBoxSeparatedDecoyHits);
		}
		return jCheckBoxSeparatedDecoyHits;
	}

	public boolean isSeparatedDecoyHits() {
		if (jCheckBoxSeparatedDecoyHits != null) {
			return jCheckBoxSeparatedDecoyHits.isSelected();
		}
		return false;
	}

	public JCheckBox getShowInMinutesCheckBox() {
		if (jCheckBoxShowInMinutes == null) {
			jCheckBoxShowInMinutes = new JCheckBox("Show time in minutes");
			jCheckBoxShowInMinutes.setToolTipText("Show times in minutes if selected or in seconds if not selected");
			jCheckBoxShowInMinutes.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});
			jCheckBoxShowInMinutes.setSelected(true);
			getControlList().add(jCheckBoxShowInMinutes);
		}
		return jCheckBoxShowInMinutes;
	}

	public boolean showInMinutes() {
		if (jCheckBoxShowInMinutes != null) {
			return jCheckBoxShowInMinutes.isSelected();
		}
		return false;
	}

	public JCheckBox getIsPSMorPeptideCheckBox() {
		if (jCheckBoxIsPSMorPeptide == null) {
			jCheckBoxIsPSMorPeptide = new JCheckBox("Number of PSMs");
			jCheckBoxIsPSMorPeptide
					.setToolTipText("<html>If selected, the colors represent the number of PSMs per protein.<br>"
							+ "If not selected, the colors represent the number of peptides per protein.</html>");
			jCheckBoxIsPSMorPeptide.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					frame.startShowingChart(evt.getSource());
				}
			});
			jCheckBoxIsPSMorPeptide.setSelected(true);
			getControlList().add(jCheckBoxIsPSMorPeptide);
		}
		return jCheckBoxIsPSMorPeptide;
	}

	/**
	 * Returns true if it represents PSMs or false if represents peptides
	 *
	 * @return
	 */
	public boolean isPSMs() {
		return jCheckBoxIsPSMorPeptide.isSelected();
	}

	public List<JComponent> getControlList() {
		return controlList;
	}
}
