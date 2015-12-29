package org.proteored.miapeExtractor.analysis.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import org.proteored.miapeExtractor.analysis.charts.HeatChart;
import org.proteored.miapeExtractor.analysis.charts.WordCramChart;
import org.proteored.miapeExtractor.analysis.genes.GeneDistributionReader;
import org.proteored.miapeExtractor.analysis.gui.tasks.DatasetFactory;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.ExtendedIdentifiedPeptide;
import org.proteored.miapeapi.experiment.model.PeptideOccurrence;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.miapeapi.experiment.model.sort.Order;
import org.proteored.miapeapi.experiment.model.sort.SorterUtil;
import org.proteored.miapeapi.xml.util.ProteinGroupComparisonType;

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
	private final HashSet<String> proteinsInSample = new HashSet<String>();
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
	private final HashMap<String, JCheckBox> scoreComparisonJCheckBoxes = new HashMap<String, JCheckBox>();

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

	public AdditionalOptionsPanelFactory(ChartManagerFrame frame) {
		this.frame = frame;
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
		PlotOrientation[] plotOrientations = { PlotOrientation.VERTICAL,
				PlotOrientation.HORIZONTAL };
		if (this.jComboBoxPlotOrientation == null) {
			this.jComboBoxPlotOrientation = new JComboBox(plotOrientations);
			controlList.add(jComboBoxPlotOrientation);
		}
		if (getPreviousPlotOrientation() != null)
			jComboBoxPlotOrientation
					.setSelectedItem(getPreviousPlotOrientation());
		this.jComboBoxPlotOrientation
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED)
							frame.startShowingChart();

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

	public JPanel getMaximumNumOccurrence(String labelString, int maxMaximum,
			int selectedNumber) {
		JPanel jPanelAdditional3 = new JPanel();
		if (labelString == null || "".equals(labelString))
			labelString = "Maximum modif. occurrence:";

		JLabel jlabel3 = new JLabel(labelString);

		if (this.jComboBoxMaximumOccurrence == null
				|| this.jComboBoxMaximumOccurrence.getItemCount() != maxMaximum) {
			Integer[] occurrences = new Integer[maxMaximum];
			for (int i = 0; i < occurrences.length; i++) {
				occurrences[i] = i + 1;
			}
			this.jComboBoxMaximumOccurrence = new JComboBox(occurrences);
			controlList.add(jComboBoxMaximumOccurrence);
		}
		if (getPreviousMaximumOccurrence() != null)
			jComboBoxMaximumOccurrence
					.setSelectedItem(getPreviousMaximumOccurrence());
		else
			jComboBoxMaximumOccurrence.setSelectedItem(selectedNumber);

		this.jComboBoxMaximumOccurrence
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						frame.startShowingChart();
					}
				});

		jPanelAdditional3.setLayout(new BorderLayout());
		jPanelAdditional3.add(jlabel3, BorderLayout.BEFORE_FIRST_LINE);
		jPanelAdditional3.add(jComboBoxMaximumOccurrence, BorderLayout.CENTER);
		return jPanelAdditional3;
	}

	public JPanel getMinimumNumOccurrence(String labelString, int maxMinimum,
			int selectedNumber) {
		JPanel jPanelAdditional3 = new JPanel();
		if (labelString == null || "".equals(labelString))
			labelString = "Minimum occurrence:";

		JLabel jlabel3 = new JLabel(labelString);

		if (this.jComboBoxMinimumOccurrence == null) {
			Integer[] occurrences = new Integer[maxMinimum];
			for (int i = 0; i < occurrences.length; i++) {
				occurrences[i] = i + 1;
			}
			this.jComboBoxMinimumOccurrence = new JComboBox(occurrences);
			controlList.add(jComboBoxMinimumOccurrence);
		}
		if (getPreviousMinimumOccurrence() != null)
			jComboBoxMinimumOccurrence
					.setSelectedItem(getPreviousMinimumOccurrence());
		else
			jComboBoxMinimumOccurrence.setSelectedItem(selectedNumber);

		this.jComboBoxMinimumOccurrence
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						frame.startShowingChart();
					}
				});

		jPanelAdditional3.setLayout(new BorderLayout());
		jPanelAdditional3.add(jlabel3, BorderLayout.BEFORE_FIRST_LINE);
		jPanelAdditional3.add(jComboBoxMinimumOccurrence, BorderLayout.CENTER);
		return jPanelAdditional3;
	}

	public Integer getPreviousMaximumOccurrence() {
		Integer previousMaximumOccurrence = null;
		if (this.jComboBoxMaximumOccurrence != null
				&& this.jComboBoxMaximumOccurrence.getSelectedIndex() > 0)
			previousMaximumOccurrence = (Integer) this.jComboBoxMaximumOccurrence
					.getSelectedItem();
		return previousMaximumOccurrence;
	}

	public Integer getPreviousMinimumOccurrence() {
		Integer previousMinimumOccurrence = null;
		if (this.jComboBoxMinimumOccurrence != null
				&& this.jComboBoxMinimumOccurrence.getSelectedIndex() > 0)
			previousMinimumOccurrence = (Integer) this.jComboBoxMinimumOccurrence
					.getSelectedItem();
		return previousMinimumOccurrence;
	}

	private PlotOrientation getPreviousPlotOrientation() {
		PlotOrientation previousPlotOrientation = null;
		if (jComboBoxPlotOrientation != null) {
			previousPlotOrientation = (PlotOrientation) jComboBoxPlotOrientation
					.getSelectedItem();
		}
		return previousPlotOrientation;
	}

	public JCheckBox getShowAsPercentageCheckBox() {

		if (this.jCheckBoxAsPercentage == null) {
			this.jCheckBoxAsPercentage = new JCheckBox("Normalize");
			// controlList.add(jCheckBoxAsPercentage); not add because sometimes
			// has to be disabled
		}
		this.jCheckBoxAsPercentage.setEnabled(true);

		this.jCheckBoxAsPercentage
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						frame.startShowingChart();

					}
				});

		return jCheckBoxAsPercentage;
	}

	public JCheckBox getShowAsStackedChartCheckBox() {
		if (this.jCheckBoxShowAsStackedChart == null) {
			this.jCheckBoxShowAsStackedChart = new JCheckBox(
					"Show as stacked chart");
			// this.controlList.add(jCheckBoxShowAsStackedChart); not add
			// becouse sometimes has to be disabled
		}
		if (this.jCheckBoxAsPercentage != null)
			this.jCheckBoxAsPercentage.setEnabled(false);
		this.jCheckBoxShowAsStackedChart.setEnabled(true);
		this.jCheckBoxShowAsStackedChart
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {

						AdditionalOptionsPanelFactory.this
								.enableShowAsPieChart(!AdditionalOptionsPanelFactory.this.jCheckBoxShowAsStackedChart
										.isSelected());
						AdditionalOptionsPanelFactory.this
								.enableAsPercentage(AdditionalOptionsPanelFactory.this.jCheckBoxShowAsStackedChart
										.isSelected());
						AdditionalOptionsPanelFactory.this
								.enableShowTotalSerie(!AdditionalOptionsPanelFactory.this.jCheckBoxShowAsStackedChart
										.isSelected());
						AdditionalOptionsPanelFactory.this
								.enableShowTotalVersusDifferent(!AdditionalOptionsPanelFactory.this.jCheckBoxShowAsStackedChart
										.isSelected());
						frame.startShowingChart();
					}
				});

		return jCheckBoxShowAsStackedChart;
	}

	protected void enableShowAsPieChart(boolean b) {
		if (this.jCheckBoxShowAsPieChart != null) {
			this.jCheckBoxShowAsPieChart.setEnabled(b);
			if (!b)
				this.jCheckBoxShowAsPieChart.setSelected(b);
		}

	}

	protected void enableShowAsStackedChart(boolean b) {
		if (this.jCheckBoxShowAsStackedChart != null) {
			this.jCheckBoxShowAsStackedChart.setEnabled(b);
			if (!b)
				this.jCheckBoxShowAsStackedChart.setSelected(b);
		}
	}

	protected void enableAsPercentage(boolean b) {
		if (this.jCheckBoxAsPercentage != null)
			this.jCheckBoxAsPercentage.setEnabled(b);
	}

	protected void enableShowTotalSerie(boolean b) {
		if (this.jCheckBoxShowTotalSerie != null)
			this.jCheckBoxShowTotalSerie.setEnabled(b);
	}

	public JPanel getProteinsInSamplePanel() {
		JPanel jPanelAdditional1 = new JPanel();

		if (this.jButtonShowInputTextFrame == null) {
			this.jButtonShowInputTextFrame = new JButton(
					"Define/show proteins in sample");
			controlList.add(jButtonShowInputTextFrame);
		}

		this.jButtonShowInputTextFrame
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						InputTextDialog dialog = new InputTextDialog(
								frame,
								"Proteins in sample",
								"<html>Paste the accession codes <br/>of the proteins in the sample</html>",
								AdditionalOptionsPanelFactory.this.proteinsInSample);

						AdditionalOptionsPanelFactory.this.proteinsInSample
								.clear();
						AdditionalOptionsPanelFactory.this.proteinsInSample
								.addAll(dialog.getPastedInfo());
						updateProteinsInSampleLabel();
						frame.startShowingChart();
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
		if (this.proteinsInSample == null || this.proteinsInSample.isEmpty())
			text = "<html>No proteins in sample defined.<br/><br/>Click on the button to enter the protein list<br/><br/></html>";
		else
			text = "<html>" + this.proteinsInSample.size()
					+ " protein defined <br/><br/></html>";
		if (this.jlabelProteinsInSample == null)
			this.jlabelProteinsInSample = new JLabel(text);
		else
			this.jlabelProteinsInSample.setText(text);
	}

	public JCheckBox getCheckBoxSensitivity() {
		if (this.jCheckBoxSensitivity == null) {
			this.jCheckBoxSensitivity = new JCheckBox("Sensitivity");
			controlList.add(jCheckBoxSensitivity);
			this.jCheckBoxSensitivity
					.setToolTipText("<html>Sensitivity relates to the test's ability to identify positive results<br>Sensitivity or True Positive Rate = TP/(TP+FN)</html>");
			this.jCheckBoxSensitivity.setSelected(true);
		}
		this.jCheckBoxSensitivity
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						frame.startShowingChart();
					}
				});
		return jCheckBoxSensitivity;
	}

	public JCheckBox getCheckBoxAccuracy() {
		if (this.jCheckBoxAccuracy == null) {
			this.jCheckBoxAccuracy = new JCheckBox("Accuracy");
			controlList.add(jCheckBoxAccuracy);
			this.jCheckBoxAccuracy
					.setToolTipText("<html>Accuracy is the proportion of true results (True Positives and True Negatives) in the identified proteins.<br>Accuracy = (TP+TN)/(TP+TN+FP+FN)</html>");
			this.jCheckBoxAccuracy.setSelected(true);
		}
		this.jCheckBoxAccuracy
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						frame.startShowingChart();
					}
				});
		return this.jCheckBoxAccuracy;
	}

	public JCheckBox getCheckBoxSpecificity() {
		if (this.jCheckBoxSpecificity == null) {
			this.jCheckBoxSpecificity = new JCheckBox("Specificity");
			controlList.add(jCheckBoxSpecificity);
			this.jCheckBoxSpecificity
					.setToolTipText("<html>Specificity or True Negative Rate, relates to the ability to identify negative results.<br>Specificity = TN/(FP+TN) = 1-FPR</html>");
			this.jCheckBoxSpecificity.setSelected(true);
		}
		this.jCheckBoxSpecificity
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						frame.startShowingChart();
					}
				});
		return this.jCheckBoxSpecificity;
	}

	public JCheckBox getCheckBoxPrecision() {
		if (this.jCheckBoxPrecision == null) {
			this.jCheckBoxPrecision = new JCheckBox("Precision");
			controlList.add(jCheckBoxPrecision);
			this.jCheckBoxPrecision
					.setToolTipText("<html>Precision is the fraction of True Positives over proteins reported as positives.<br>Precision = TP/(TP+FP)</html>");
		}
		this.jCheckBoxPrecision
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						frame.startShowingChart();
					}
				});
		return this.jCheckBoxPrecision;
	}

	public JCheckBox getCheckBoxNPV() {
		if (this.jCheckBoxNPV == null) {
			this.jCheckBoxNPV = new JCheckBox("NPV");
			controlList.add(jCheckBoxNPV);
			this.jCheckBoxNPV
					.setToolTipText("<html>NPV is the fraction of True Negatives over proteins reported as negatives.<br>Negative Predictive Value (NPV) = TN/(TN+FN)</html>");
		}
		this.jCheckBoxNPV.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				frame.startShowingChart();
			}
		});
		return this.jCheckBoxNPV;
	}

	public JCheckBox getCheckBoxFDR() {
		if (this.jCheckBoxFDR == null) {
			this.jCheckBoxFDR = new JCheckBox("FDR");
			controlList.add(jCheckBoxFDR);
			this.jCheckBoxFDR
					.setToolTipText("<html>Error rate.<br>False Discovery Rate (FDR) = FP/(FP+TP)</html>");
		}
		this.jCheckBoxFDR.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				frame.startShowingChart();
			}
		});
		return this.jCheckBoxFDR;

	}

	public JPanel getOverReplicatesPanel() {
		JPanel jPanelAdditional4 = new JPanel();

		if (this.jCheckBoxOverReplicates == null) {
			this.jCheckBoxOverReplicates = new JCheckBox(
					"Repeatibility over next level");
			controlList.add(jCheckBoxOverReplicates);
		}
		this.jCheckBoxOverReplicates
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						frame.startShowingChart();
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
	// frame.startShowingChart();
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
			previousHistogramType = (HistogramType) jComboBoxHistogramType
					.getSelectedItem();
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
		if (this.jlabelPeptideListHeader == null) {
			this.jlabelPeptideListHeader = new JLabel(num
					+ " peptide sequences:");
			controlList.add(jlabelPeptideListHeader);
		} else
			this.jlabelPeptideListHeader.setText(num + " peptide sequences:");
		if (this.jListPeptides == null) {
			this.jListPeptides = new MyJPanelList();
			controlList.add(jListPeptides);
			this.jListPeptides.jListPeptides.setListData(peptides);
		} else {
			this.jListPeptides.jListPeptides.setListData(peptides);
		}
		this.jListPeptides.jListPeptides
				.addListSelectionListener(new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent evt) {
						if (evt.getValueIsAdjusting())
							return;
						frame.startShowingChart();
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

		if (this.jTextAreaUserPeptideList == null) {
			this.jTextAreaUserPeptideList = new JTextArea();
			this.jTextAreaUserPeptideList.setFont(new JTextField().getFont());
			controlList.add(jTextAreaUserPeptideList);
		}
		jTextAreaUserPeptideList.setColumns(22);
		jTextAreaUserPeptideList.setLineWrap(false);
		jTextAreaUserPeptideList.setRows(10);
		jTextAreaUserPeptideList.setWrapStyleWord(false);
		if (this.userPeptideList != null)
			this.jTextAreaUserPeptideList.setText(this.userPeptideList);

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
			jbuttonAddToPlot
					.setToolTipText("Click here to automatically select the inserted peptides in the peptide list (if present)");
			controlList.add(jbuttonAddToPlot);
			jbuttonAddToPlot
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							AdditionalOptionsPanelFactory.this
									.addUserPeptideListToPeptideSelection();
						}
					});

			if (this.jTextContaining == null) {
				this.jTextContaining = new JTextField(10);
				controlList.add(jTextContaining);
			}

			c.gridx = 0;
			jPanelAdditional3.add(new JLabel("Containing:"), c);
			c.gridy++;
			c.gridy++;
			jPanelAdditional3.add(this.jTextContaining, c);
			c.gridy++;
			c.gridx = 0;
			jPanelAdditional3.add(jbuttonAddToPlot, c);
		}
		return jPanelAdditional3;
	}

	public List<String> getUserPeptideList() {
		if (this.jTextAreaUserPeptideList != null) {
			final String text = this.jTextAreaUserPeptideList.getText();
			List<String> ret = new ArrayList<String>();
			if (text.contains("\n")) {
				final String[] split = text.split("\n");
				log.info("Returning " + split.length
						+ " peptide sequences from user");
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
		String containing = this.jTextContaining.getText();
		boolean distinguishModPep = frame.distinguishModifiedPeptides();
		final String userPeptideList = this.jTextAreaUserPeptideList.getText();
		List<String> resultingSequences = new ArrayList<String>();
		if (userPeptideList != null && !"".equals(userPeptideList)) {
			this.userPeptideList = userPeptideList;
			String[] splitted = this.userPeptideList.split("\n");
			log.info("the user has input " + splitted.length + " sequences");

			for (String userSequence : splitted) {
				final Collection<PeptideOccurrence> peptideSequences = frame.experimentList
						.getPeptideOccurrenceList(distinguishModPep).values();
				for (PeptideOccurrence identificationOccurrence : peptideSequences) {
					final List<ExtendedIdentifiedPeptide> identificationItemList = identificationOccurrence
							.getPeptides();
					for (ExtendedIdentifiedPeptide extendedIdentifiedPeptide : identificationItemList) {
						final String sequence = extendedIdentifiedPeptide
								.getSequence();
						if (sequence.equalsIgnoreCase(userSequence)) {
							if (distinguishModPep) {
								final List<String> modifiedSequences = ExtendedIdentifiedPeptide
										.getModifiedSequences(sequence);
								if (modifiedSequences != null
										&& !modifiedSequences.isEmpty()) {
									for (String modifiedSequence : modifiedSequences) {
										if (!resultingSequences
												.contains(modifiedSequence)) {
											if ((!"".equals(containing) && modifiedSequence
													.contains(containing))
													|| "".equals(containing)) {
												resultingSequences
														.add(modifiedSequence);
												System.out
														.println(resultingSequences
																.size()
																+ " -> "
																+ modifiedSequence);
												if (!modifiedSequence
														.contains("79.97"))
													System.out.println("HOLA");
											}
										}
									}
								}
							} else {
								if (!resultingSequences.contains(sequence))
									resultingSequences.add(sequence);
							}
						} else if (extendedIdentifiedPeptide
								.getModificationString().equals(userSequence)) {
							if (!resultingSequences.contains(userSequence)) {
								if ((!"".equals(containing) && userSequence
										.contains(containing))
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
						.getPeptideOccurrenceList(distinguishModPep).values();
				for (PeptideOccurrence identificationOccurrence : peptideSequences) {
					final List<ExtendedIdentifiedPeptide> identificationItemList = identificationOccurrence
							.getPeptides();
					for (ExtendedIdentifiedPeptide extendedIdentifiedPeptide : identificationItemList) {
						if (distinguishModPep) {
							final String modifiedSequence = extendedIdentifiedPeptide
									.getModificationString();
							if (modifiedSequence.contains(containing)) {
								if (!resultingSequences
										.contains(modifiedSequence))
									resultingSequences.add(modifiedSequence);
							}
						} else {
							final String sequence = extendedIdentifiedPeptide
									.getSequence();
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
			this.jListPeptides.jListPeptides.setListData(resultingSequences
					.toArray());
			this.jListPeptides.repaint();
			this.jListPeptides.jListPeptides.setSelectionInterval(0,
					resultingSequences.size() - 1);
			this.jlabelPeptideListHeader.setText(resultingSequences.size()
					+ " peptides sequences");
			frame.startShowingChart();
		}
	}

	public JPanel getModificationListPanel() {
		int selectionModel = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;

		JPanel jPanelAdditional2 = new JPanel();
		Object[] modifications = this.getModifications();
		String num = "";
		if (modifications != null && modifications.length > 0)
			num = String.valueOf(modifications.length);
		if (this.jLabelModificationListHeader == null) {
			this.jLabelModificationListHeader = new JLabel(num + " PMTs:");
			controlList.add(jLabelModificationListHeader);
		} else
			this.jLabelModificationListHeader.setText(num + " PMTs:");
		if (this.jListModifications == null) {
			this.jListModifications = new JList(modifications);
			controlList.add(jListModifications);
		} else
			this.jListModifications.setListData(modifications);
		this.jListModifications.setSelectionMode(selectionModel);
		this.jListModifications
				.addListSelectionListener(new ListSelectionListener() {
					private Object[] previousJListSelection;

					@Override
					public void valueChanged(ListSelectionEvent evt) {
						if (evt.getValueIsAdjusting())
							return;
						Object[] selectedValues = ((JList) evt.getSource())
								.getSelectedValues();
						if (!isTheSameSelection(selectedValues)) {
							previousJListSelection = selectedValues;
							frame.startShowingChart();
						}
					}

					private boolean isTheSameSelection(Object[] selectedValues) {
						AdditionalOptionsPanelFactory.this.previousModificationsSelected = previousJListSelection;
						if (previousJListSelection == null
								&& selectedValues != null)
							return false;
						if (previousJListSelection != null
								&& selectedValues == null)
							return false;
						if (previousJListSelection == null
								&& selectedValues == null)
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
		if (this.previousModificationsSelected != null
				&& previousModificationsSelected.length > 0) {
			List<Integer> selectedIndexes = new ArrayList<Integer>();
			for (Object modification : previousModificationsSelected) {
				for (int i = 0; i < this.jListModifications.getModel()
						.getSize(); i++) {
					final Object element = this.jListModifications.getModel()
							.getElementAt(i);
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
			this.jListModifications.setSelectedIndices(temp);
		}
		jPanelAdditional2.setLayout(new BorderLayout());
		jPanelAdditional2.add(jLabelModificationListHeader,
				BorderLayout.BEFORE_FIRST_LINE);
		jPanelAdditional2.add(new JScrollPane(jListModifications),
				BorderLayout.LINE_START);
		return jPanelAdditional2;
	}

	public String[] getModifications() {
		log.info("Getting modification names");
		if (frame.experimentList != null) {
			final List<String> differentPeptideModificationNames = frame.experimentList
					.getDifferentPeptideModificationNames();
			if (differentPeptideModificationNames != null
					&& !differentPeptideModificationNames.isEmpty()) {
				log.info("There is " + differentPeptideModificationNames.size()
						+ " different modifications");
				return differentPeptideModificationNames.toArray(new String[0]);
			}
		}
		return null;
	}

	public JPanel getProteinScorePanel(DefaultComboBoxModel proteinScoreNames) {
		JPanel jPanelAdditional3 = new JPanel();
		JLabel jlabel3 = new JLabel("Protein score:");
		if (this.jComboBoxProteinScoreNames == null) {
			this.jComboBoxProteinScoreNames = new JComboBox(proteinScoreNames);
			controlList.add(jComboBoxProteinScoreNames);
		}
		this.jComboBoxProteinScoreNames = new JComboBox(proteinScoreNames);
		this.jComboBoxProteinScoreNames.setSelectedIndex(0); // select the
																// first
																// score
		this.jComboBoxProteinScoreNames
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(java.awt.event.ItemEvent evt) {
						if (evt.getStateChange() == ItemEvent.SELECTED)
							frame.startShowingChart();
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

		if (this.jComboBoxPeptideScoreNames == null) {
			this.jComboBoxPeptideScoreNames = new JComboBox(peptideScoreNames);
			controlList.add(jComboBoxPeptideScoreNames);
			// Select the first score
			this.jComboBoxPeptideScoreNames.setSelectedIndex(0);
		}
		this.jComboBoxPeptideScoreNames = new JComboBox(peptideScoreNames);

		this.jComboBoxPeptideScoreNames
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(java.awt.event.ItemEvent evt) {
						if (evt.getStateChange() == ItemEvent.SELECTED)
							frame.startShowingChart();
					}
				});
		jPanelAdditional4.setLayout(new BorderLayout());
		jPanelAdditional4.add(jlabel4, BorderLayout.LINE_START);
		jPanelAdditional4.add(jComboBoxPeptideScoreNames, BorderLayout.CENTER);
		return jPanelAdditional4;
	}

	public JPanel getShowRegressionLinePanel() {
		JPanel jPanelAdditional5 = new JPanel();
		if (this.jCheckBoxAddRegressionLine == null) {
			this.jCheckBoxAddRegressionLine = new JCheckBox(
					"Show regression line");
			controlList.add(jCheckBoxAddRegressionLine);
			this.jCheckBoxAddRegressionLine.setSelected(true);
		}

		this.jCheckBoxAddRegressionLine
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(java.awt.event.ItemEvent evt) {
						frame.startShowingChart();
					}
				});
		jPanelAdditional5.setLayout(new BorderLayout());
		jPanelAdditional5.add(jCheckBoxAddRegressionLine, BorderLayout.CENTER);
		return jPanelAdditional5;
	}

	public JPanel getShowDiagonalLinePanel() {
		JPanel jPanelAdditional6 = new JPanel();
		if (this.jCheckBoxAddDiagonalLine == null) {
			this.jCheckBoxAddDiagonalLine = new JCheckBox("Show diagonal line");
			controlList.add(jCheckBoxAddDiagonalLine);
			this.jCheckBoxAddDiagonalLine.setSelected(true);
		}

		this.jCheckBoxAddDiagonalLine
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(java.awt.event.ItemEvent evt) {
						frame.startShowingChart();
					}
				});
		jPanelAdditional6.setLayout(new BorderLayout());
		jPanelAdditional6.add(jCheckBoxAddDiagonalLine, BorderLayout.CENTER);
		return jPanelAdditional6;
	}

	public boolean showDiagonalLine() {
		if (this.jCheckBoxAddDiagonalLine != null)
			return this.jCheckBoxAddDiagonalLine.isSelected();
		return false;
	}

	public boolean showRegressionLine() {
		if (this.jCheckBoxAddRegressionLine != null)
			return this.jCheckBoxAddRegressionLine.isSelected();
		return false;
	}

	public JPanel getColorScalePanel(boolean alwaysEnabled) {
		JPanel jPanelAdditional1 = new JPanel();

		String[] scaleTypes = { LINEAR_SCALE, LOGARITHMIC_SCALE,
				EXPONENTIAL_SCALE };
		if (this.jComboBoxColorScale == null) {
			this.jComboBoxColorScale = new JComboBox(scaleTypes);
			this.controlList.add(this.jComboBoxColorScale);

			this.jComboBoxColorScale
					.addItemListener(new java.awt.event.ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							frame.startShowingChart();
						}
					});
			controlList.add(jComboBoxColorScale);
		}
		if (alwaysEnabled)
			this.jComboBoxColorScale.setEnabled(true);
		if (getPreviousColorScale() != null)
			jComboBoxColorScale.setSelectedItem(getPreviousColorScale());

		if (this.jComboBoxHighColorScale == null) {
			this.jComboBoxHighColorScale = new JComboBox(getColors());
			this.controlList.add(this.jComboBoxHighColorScale);
			this.jComboBoxHighColorScale
					.addItemListener(new java.awt.event.ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							frame.startShowingChart();
						}
					});
		}
		if (this.jComboBoxLowColorScale == null) {
			this.jComboBoxLowColorScale = new JComboBox(getColors());
			this.controlList.add(this.jComboBoxLowColorScale);
			this.jComboBoxLowColorScale
					.addItemListener(new java.awt.event.ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							frame.startShowingChart();
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
		if (this.jComboBoxHighColorScale != null) {
			final Color color = getColor((String) this.jComboBoxHighColorScale
					.getSelectedItem());
			if (color != null)
				return color;
		}
		return Color.red;
	}

	public Color getLowColorScale() {
		if (this.jComboBoxLowColorScale != null) {
			final Color color = getColor((String) this.jComboBoxLowColorScale
					.getSelectedItem());
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
		return null;
	}

	private Object[] getColors() {
		List<String> colors = new ArrayList<String>();
		colors.add("select a color...");
		colors.add("black");
		colors.add("blue");
		colors.add("cyan");
		colors.add("darkGray");
		colors.add("gray");
		colors.add("green");
		colors.add("lightGray");
		colors.add("magenta");
		colors.add("orange");
		colors.add("pink");
		colors.add("red");
		colors.add("white");
		colors.add("yellow");
		return colors.toArray();
	}

	public JPanel getHeatMapThresholdPanel() {
		JPanel jPanelAdditional2 = new JPanel();
		JLabel jlabel2 = new JLabel(
				"Do not paint rows with less than (occurrence):");
		String text = "<html>The number of identification sets that the item occurs <br>"
				+ "will be compared with this number.</html>";
		jlabel2.setToolTipText(text);
		if (this.jTextHeatMapThreshold == null) {
			this.jTextHeatMapThreshold = new JTextField("2", 4);
			controlList.add(jTextHeatMapThreshold);
		}
		this.jTextHeatMapThreshold.setToolTipText(text);

		this.jTextHeatMapThreshold
				.addFocusListener(new java.awt.event.FocusAdapter() {
					private String previousNumber = "";

					@Override
					public void focusLost(java.awt.event.FocusEvent evt) {
						final JTextField jtextfiled = (JTextField) evt
								.getSource();
						if (!previousNumber.equals(jtextfiled.getText())) {
							if (checkPositiveNumber(jtextfiled.getText()))
								frame.startShowingChart();
						} else {
							// log.info("The BIN number has not changed");
						}
					}

					@Override
					public void focusGained(java.awt.event.FocusEvent evt) {
						final JTextField jtextfiled = (JTextField) evt
								.getSource();
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
		HistogramType[] histogramTypes = { HistogramType.FREQUENCY,
				HistogramType.RELATIVE_FREQUENCY, HistogramType.SCALE_AREA_TO_1 };
		if (this.jComboBoxHistogramType == null) {
			this.jComboBoxHistogramType = new JComboBox(histogramTypes);
			controlList.add(jComboBoxHistogramType);
		}
		if (getPreviousHistogramType() != null)
			jComboBoxHistogramType.setSelectedItem(getPreviousHistogramType());
		this.jComboBoxHistogramType
				.addItemListener(new java.awt.event.ItemListener() {
					private int previousSelectedIndex = -1;

					@Override
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED)
							if (jComboBoxHistogramType.getSelectedIndex() != this.previousSelectedIndex) {
								log.info("previous selected index set to "
										+ jComboBoxHistogramType
												.getSelectedIndex()
										+ " and previously was setted to "
										+ this.previousSelectedIndex);
								this.previousSelectedIndex = jComboBoxHistogramType
										.getSelectedIndex();
								log.info("selected index now is "
										+ this.previousSelectedIndex);
								frame.startShowingChart();

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
		if (this.jTextHistogramBin == null) {
			this.jTextHistogramBin = new JTextField("30", 4);
			controlList.add(jTextHistogramBin);
		}
		this.jTextHistogramBin
				.addFocusListener(new java.awt.event.FocusAdapter() {
					private String previousNumber = "";

					@Override
					public void focusLost(java.awt.event.FocusEvent evt) {
						final JTextField jtextfiled = (JTextField) evt
								.getSource();
						if (!previousNumber.equals(jtextfiled.getText())) {
							if (checkPositiveNumber(jtextfiled.getText()))
								frame.startShowingChart();
						} else {
							// log.info("The BIN number has not changed");
						}
					}

					@Override
					public void focusGained(java.awt.event.FocusEvent evt) {
						final JTextField jtextfiled = (JTextField) evt
								.getSource();
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
		if (this.jComboBoxMOverZ == null) {
			this.jComboBoxMOverZ = new JComboBox(new String[] { M, MOVERZ });
			controlList.add(jComboBoxMOverZ);
		}
		this.jComboBoxMOverZ.setSelectedIndex(0); // select
													// the
													// first
													// score
		this.jComboBoxMOverZ.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED)
					frame.startShowingChart();
			}
		});
		jPanelAdditional3.setLayout(new BorderLayout());
		jPanelAdditional3.add(jlabel3, BorderLayout.LINE_START);
		jPanelAdditional3.add(jComboBoxMOverZ, BorderLayout.CENTER);
		return jPanelAdditional3;
	}

	public void enableProteinScoreNameControls(boolean b) {
		if (this.jComboBoxProteinScoreNames != null)
			this.jComboBoxProteinScoreNames.setEnabled(b);
	}

	public void enablePeptideScoreNameControls(boolean b) {
		if (this.jComboBoxPeptideScoreNames != null)
			this.jComboBoxPeptideScoreNames.setEnabled(b);
	}

	public PlotOrientation getPlotOrientation() {
		if (this.jComboBoxPlotOrientation != null) {
			if (this.jComboBoxPlotOrientation.getSelectedItem().equals(
					PlotOrientation.VERTICAL))
				return PlotOrientation.VERTICAL;
			else if (this.jComboBoxPlotOrientation.getSelectedItem().equals(
					PlotOrientation.HORIZONTAL))
				return PlotOrientation.HORIZONTAL;
		}
		// by default:
		return PlotOrientation.VERTICAL;
	}

	public HashSet<String> getProteinsInSample() {
		return this.proteinsInSample;
	}

	public int getHistogramBins() {
		if (this.jTextHistogramBin != null) {
			try {
				return Integer.valueOf(this.jTextHistogramBin.getText());
			} catch (NumberFormatException e) {

			}
		}
		// by default
		return 30;
	}

	public HistogramType getHistogramType() {
		if (this.jComboBoxHistogramType != null) {
			HistogramType histogramTypeString = (HistogramType) this.jComboBoxHistogramType
					.getSelectedItem();
			if (HistogramType.FREQUENCY.equals(histogramTypeString))
				return HistogramType.FREQUENCY;
			else if (HistogramType.RELATIVE_FREQUENCY
					.equals(histogramTypeString))
				return HistogramType.RELATIVE_FREQUENCY;
			else if (HistogramType.SCALE_AREA_TO_1.equals(histogramTypeString))
				return HistogramType.SCALE_AREA_TO_1;
		}
		// by default:
		return HistogramType.FREQUENCY;
	}

	public double getColorScale() {
		if (this.jComboBoxColorScale != null) {
			final String selectedItem = (String) this.jComboBoxColorScale
					.getSelectedItem();
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

	public int getMinOccurrenceThreshold() {
		if (this.jTextHeatMapThreshold != null) {
			try {
				return Integer.valueOf(this.jTextHeatMapThreshold.getText());
			} catch (NumberFormatException e) {

			}
		}
		// by default
		return 3;
	}

	public String getPeptideScoreName() {
		String scoreName = null;
		if (this.jComboBoxPeptideScoreNames != null) {
			try {
				scoreName = (String) this.jComboBoxPeptideScoreNames
						.getSelectedItem();
			} catch (Exception e) {

			}
		} else {
			log.warn("Peptide score name combo box is not present");
		}
		return scoreName;
	}

	public String getProteinScoreName() {

		String scoreName = null;
		if (this.jComboBoxProteinScoreNames != null) {
			try {
				scoreName = (String) this.jComboBoxProteinScoreNames
						.getSelectedItem();
			} catch (Exception e) {

			}
		} else {
			log.warn("Protein score name combo box is not present");
		}
		return scoreName;
	}

	public void disableAdditionalOptionControls(boolean b) {
		for (JComponent component : this.controlList) {
			component.setEnabled(b);
		}
		for (JCheckBox checkbox : this.scoreComparisonJCheckBoxes.values()) {
			checkbox.setEnabled(b);
		}

	}

	public JPanel getExperimentsCheckboxes(boolean selectAll, int numSelected) {
		log.info("Creating list of replicates...");
		JPanel jpanel = new JPanel();
		jpanel.setLayout(new BoxLayout(jpanel, BoxLayout.PAGE_AXIS));
		this.scoreComparisonJCheckBoxes.clear();
		int numExperiment = 1;
		for (Experiment experiment : frame.experimentList.getExperiments()) {
			final String experimentName = experiment.getName();
			boolean selected = false;
			if (numExperiment <= numSelected || selectAll)
				selected = true;
			JCheckBox checkBox = new JCheckBox(experiment.getName(), selected);
			checkBox.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					frame.startShowingChart();
				}
			});

			this.scoreComparisonJCheckBoxes.put(experimentName, checkBox);
			jpanel.add(checkBox);
			numExperiment++;

		}
		return jpanel;
	}

	public HashMap<String, JCheckBox> getIdSetsJCheckBoxes() {
		return this.scoreComparisonJCheckBoxes;

	}

	public JPanel getReplicatesCheckboxes(boolean separateExperiments,
			boolean selectAll, int numSelected) {
		log.info("Creating list of replicates...");
		JPanel jpanel = new JPanel();
		jpanel.setLayout(new BoxLayout(jpanel, BoxLayout.PAGE_AXIS));
		this.scoreComparisonJCheckBoxes.clear();
		int numReplicates = 1;
		for (Experiment experiment : frame.experimentList.getExperiments()) {
			JLabel labelExperiment = new JLabel(experiment.getName() + ":");
			jpanel.add(labelExperiment);
			if (separateExperiments)
				numReplicates = 1;
			for (Object idSet : experiment.getNextLevelIdentificationSetList()) {
				Replicate replicate = (Replicate) idSet;
				boolean selected = false;
				if (numReplicates <= numSelected || selectAll)
					selected = true;

				JCheckBox checkBox = new JCheckBox(replicate.getName(),
						selected);
				checkBox.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						frame.startShowingChart();
					}
				});
				this.scoreComparisonJCheckBoxes.put(replicate.getFullName(),
						checkBox);
				jpanel.add(checkBox);
				numReplicates++;
			}

		}
		return jpanel;
	}

	public String[] getSelectedModifications() {
		try {
			// added because sometimes the jList was not ready to get the
			// selected values in "getSelectedModifications"
			Thread.sleep(100);
		} catch (InterruptedException e) {

		}
		if (this.jListModifications != null) {

			final Object[] selectedValues = this.jListModifications
					.getSelectedValues();
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

	public Object[] getSelectedPeptides() {
		try {
			// added because sometimes the jList was not ready to get the
			// selected values in "getSelectedPeptides"
			Thread.sleep(100);
		} catch (InterruptedException e) {

		}
		if (this.jListPeptides != null) {

			final Object[] selectedValues = this.jListPeptides.jListPeptides
					.getSelectedValues();
			if (selectedValues.length > 0) {
				return selectedValues;
			}
		}
		return null;
	}

	public boolean getAsPercentage() {
		if (this.jCheckBoxAsPercentage != null)
			return this.jCheckBoxAsPercentage.isSelected();
		return true;
	}

	public int getMaximumOccurrence() {
		if (this.jComboBoxMaximumOccurrence != null) {
			return (Integer) this.jComboBoxMaximumOccurrence.getSelectedItem();
		}
		return Integer.MAX_VALUE;
	}

	public int getMinimumOccurrence() {
		if (this.jComboBoxMinimumOccurrence != null) {
			return (Integer) this.jComboBoxMinimumOccurrence.getSelectedItem();
		}
		return 0;
	}

	public boolean getMOverZ() {
		if (this.jComboBoxMOverZ != null) {
			String selection = (String) this.jComboBoxMOverZ.getSelectedItem();
			if (selection.equals(MOVERZ)) {
				return true;
			} else if (selection.equals(M)) {
				return false;
			}
		}
		return false;
	}

	public boolean getOverReplicates() {
		if (this.jCheckBoxOverReplicates != null)
			return this.jCheckBoxOverReplicates.isSelected();
		return false;
	}

	public boolean isAccuracy() {
		if (this.jCheckBoxAccuracy != null)
			return this.jCheckBoxAccuracy.isSelected();
		return false;
	}

	public boolean isSpecificity() {
		if (this.jCheckBoxSpecificity != null)
			return this.jCheckBoxSpecificity.isSelected();
		return false;
	}

	public boolean isPrecision() {
		if (this.jCheckBoxPrecision != null)
			return this.jCheckBoxPrecision.isSelected();
		return false;
	}

	public boolean isNPV() {
		if (this.jCheckBoxNPV != null)
			return this.jCheckBoxNPV.isSelected();
		return false;
	}

	public boolean isFDR() {
		if (this.jCheckBoxFDR != null)
			return this.jCheckBoxFDR.isSelected();
		return false;
	}

	public boolean showAsStackedChart() {
		if (this.jCheckBoxShowAsStackedChart != null) {
			return this.jCheckBoxShowAsStackedChart.isSelected();
		}
		return false;
	}

	public boolean isSensitivity() {
		if (this.jCheckBoxSensitivity != null)
			return this.jCheckBoxSensitivity.isSelected();
		return false;
	}

	public void updatePeptideSequenceList() {
		boolean distiguishModificatedPeptides = frame
				.distinguishModifiedPeptides();
		if (this.jListPeptides != null) {
			int size = this.jListPeptides.jListPeptides.getModel().getSize();
			log.info("Updating peptide list of " + size + " elements");
			final Object[] selectedValues = this.jListPeptides.jListPeptides
					.getSelectedValues();
			this.jListPeptides.jListPeptides.setListData(frame
					.getPeptidesFromExperiments(distiguishModificatedPeptides));
			size = this.jListPeptides.jListPeptides.getModel().getSize();
			log.info("Now has " + size + " elements");
			this.jlabelPeptideListHeader.setText(size + " peptide sequences:");
			if (selectedValues != null && selectedValues.length > 0) {
				List<Integer> selectedIndexes = new ArrayList<Integer>();
				for (Object object : selectedValues) {
					String sequence = (String) object;
					for (int i = 0; i < size; i++) {
						String listSequence = (String) this.jListPeptides.jListPeptides
								.getModel().getElementAt(i);
						if (sequence.equals(listSequence))
							selectedIndexes.add(i);

					}

				}
				if (!selectedIndexes.isEmpty()) {
					final int[] array = DatasetFactory.toArray(selectedIndexes);
					this.jListPeptides.jListPeptides.setSelectedIndices(array);
				}
			}
		}
	}

	private void updateModificationList() {
		if (this.jListModifications != null) {
			int size = this.jListModifications.getModel().getSize();
			log.info("Updating modification list of " + size + " elements");
			final Object[] selectedValues = this.jListModifications
					.getSelectedValues();
			this.jListModifications.setListData(getModifications());
			size = this.jListModifications.getModel().getSize();
			log.info("Now has " + size + " elements");
			this.jLabelModificationListHeader.setText(size + " PTMs:");
			if (selectedValues != null && selectedValues.length > 0) {
				List<Integer> selectedIndexes = new ArrayList<Integer>();
				for (Object object : selectedValues) {
					String modif = (String) object;
					for (int i = 0; i < size; i++) {
						String modification = (String) this.jListModifications
								.getModel().getElementAt(i);
						if (modif.equals(modification))
							selectedIndexes.add(i);
					}

				}
				if (!selectedIndexes.isEmpty()) {
					final int[] array = DatasetFactory.toArray(selectedIndexes);
					this.jListModifications.setSelectedIndices(array);
				}
			}
		}
	}

	public JButton getSaveButton() {
		// Add save image buttion
		if (this.jbuttonSaveImage == null) {
			this.jbuttonSaveImage = new JButton("Save image");
			this.controlList.add(jbuttonSaveImage);
			jbuttonSaveImage
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							frame.saveHeatMapImage();
						}
					});
		}
		return jbuttonSaveImage;
	}

	public JCheckBox getShowAsPieChartCheckBox() {

		if (this.jCheckBoxShowAsPieChart == null) {
			this.jCheckBoxShowAsPieChart = new JCheckBox("Show as pie chart");
			// controlList.add(jCheckBoxShowAsPieChart);not add becaouse
			// sometimes has to be disabled
		}

		this.jCheckBoxShowAsPieChart
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						AdditionalOptionsPanelFactory.this
								.enableAsPercentage(!AdditionalOptionsPanelFactory.this.jCheckBoxShowAsPieChart
										.isSelected());
						AdditionalOptionsPanelFactory.this
								.enableShowAsStackedChart(!AdditionalOptionsPanelFactory.this.jCheckBoxShowAsPieChart
										.isSelected());
						AdditionalOptionsPanelFactory.this
								.enableShowTotalSerie(!AdditionalOptionsPanelFactory.this.jCheckBoxShowAsPieChart
										.isSelected());
						AdditionalOptionsPanelFactory.this
								.enableShowTotalVersusDifferent(!AdditionalOptionsPanelFactory.this.jCheckBoxShowAsPieChart
										.isSelected());
						frame.startShowingChart();
					}
				});

		return jCheckBoxShowAsPieChart;
	}

	public JCheckBox getShowAverageOverReplicatesCheckBox() {

		if (this.jCheckBoxShowAverage == null) {
			this.jCheckBoxShowAverage = new JCheckBox(
					"Show as average next level");
			// controlList.add(jCheckBoxShowAsPieChart);not add becaouse
			// sometimes has to be disabled
		}

		this.jCheckBoxShowAverage
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						frame.startShowingChart();
					}
				});

		return jCheckBoxShowAverage;
	}

	public boolean showAsPieChart() {
		if (this.jCheckBoxShowAsPieChart != null) {
			return this.jCheckBoxShowAsPieChart.isSelected();
		}
		return false;
	}

	public JPanel getLabelledModificationsPanel() {
		JPanel jPanel = new JPanel();
		JLabel jlabel = new JLabel(
				"<html>Select two modifications from the following<br> comboboxes in order to show the number<br>of peptides that have been detected<br>containing the two variants:</html>");
		if (this.jComboBoxModificationA == null) {
			this.jComboBoxModificationA = new JComboBox();
			controlList.add(jComboBoxModificationA);
		}
		final String[] modifications = getModifications();
		this.jComboBoxModificationA.removeAllItems();
		this.jComboBoxModificationA.addItem(NO_MODIFICATION);
		for (String modif : modifications) {
			this.jComboBoxModificationA.addItem(modif);
		}
		this.jComboBoxModificationA.setSelectedIndex(0); // select
		this.jComboBoxModificationA
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(java.awt.event.ItemEvent evt) {
						if (evt.getStateChange() == ItemEvent.SELECTED)
							frame.startShowingChart();
					}
				});

		if (this.jComboBoxModificationB == null) {
			this.jComboBoxModificationB = new JComboBox();
			controlList.add(jComboBoxModificationB);
		}
		this.jComboBoxModificationB.removeAllItems();
		this.jComboBoxModificationB.addItem(NO_MODIFICATION);
		for (String modif : modifications) {
			this.jComboBoxModificationB.addItem(modif);
		}
		this.jComboBoxModificationB.setSelectedIndex(0); // select
		this.jComboBoxModificationB
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(java.awt.event.ItemEvent evt) {
						if (evt.getStateChange() == ItemEvent.SELECTED)
							frame.startShowingChart();
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
		if (this.jComboBoxModificationA != null)
			return (String) this.jComboBoxModificationA.getSelectedItem();
		return null;
	}

	public String getModificationB() {
		if (this.jComboBoxModificationB != null)
			return (String) this.jComboBoxModificationB.getSelectedItem();
		return null;
	}

	public boolean showAverageOverReplicates() {
		if (this.jCheckBoxShowAverage != null) {
			return this.jCheckBoxShowAverage.isSelected();
		}
		return false;
	}

	public JCheckBox getShowTotalSerieCheckBox(
			boolean takeIntoAccountPieAndStackedViews) {
		if (takeIntoAccountPieAndStackedViews) {
			if (this.jCheckBoxShowAsPieChart != null
					&& this.jCheckBoxShowAsPieChart.isSelected())
				return null;
			if (this.jCheckBoxShowAsStackedChart != null
					&& this.jCheckBoxShowAsStackedChart.isSelected())
				return null;
		}

		if (this.jCheckBoxShowTotalSerie == null) {
			this.jCheckBoxShowTotalSerie = new JCheckBox("Show total series");
			// controlList.add(jCheckBoxShowAsPieChart);not add becaouse
			// sometimes has to be disabled
		}

		this.jCheckBoxShowTotalSerie
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						frame.startShowingChart();
					}
				});

		return jCheckBoxShowTotalSerie;
	}

	public boolean isTotalSerieShown() {
		if (this.jCheckBoxShowTotalSerie != null) {
			return this.jCheckBoxShowTotalSerie.isSelected();
		}
		return false;
	}

	public JCheckBox getShowDifferentIdentificationsCheckBox() {

		if (this.jCheckBoxShowDifferentIdentifications == null) {
			this.jCheckBoxShowDifferentIdentifications = new JCheckBox(
					"Show different identifications number");
			// controlList.add(jCheckBoxShowAsPieChart);not add becaouse
			// sometimes has to be disabled
		}
		this.jCheckBoxShowDifferentIdentifications.setSelected(true);
		this.jCheckBoxShowDifferentIdentifications
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						AdditionalOptionsPanelFactory.this
								.enableShowTotalVersusDifferent(!AdditionalOptionsPanelFactory.this.jCheckBoxShowDifferentIdentifications
										.isSelected());
						frame.startShowingChart();
					}
				});

		return jCheckBoxShowDifferentIdentifications;
	}

	public boolean isDifferentIdentificationsShown() {
		if (this.jCheckBoxShowDifferentIdentifications != null) {
			return this.jCheckBoxShowDifferentIdentifications.isSelected();
		}
		return false;
	}

	public JCheckBox getShowTotalVersusDifferentCheckBox() {

		if (this.jCheckBoxTotalVersusDifferent == null) {
			this.jCheckBoxTotalVersusDifferent = new JCheckBox(
					"Show different/total identification ratios");
			// controlList.add(jCheckBoxShowAsPieChart);not add becaouse
			// sometimes has to be disabled
		}
		this.jCheckBoxTotalVersusDifferent
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						AdditionalOptionsPanelFactory.this
								.enableShowDifferentIdentifications(!AdditionalOptionsPanelFactory.this.jCheckBoxTotalVersusDifferent
										.isSelected());
						AdditionalOptionsPanelFactory.this
								.enableShowAsStackedChart(!AdditionalOptionsPanelFactory.this.jCheckBoxTotalVersusDifferent
										.isSelected());
						AdditionalOptionsPanelFactory.this
								.enableShowAsPieChart(!AdditionalOptionsPanelFactory.this.jCheckBoxTotalVersusDifferent
										.isSelected());
						frame.startShowingChart();
					}
				});

		return jCheckBoxTotalVersusDifferent;
	}

	protected void enableShowTotalVersusDifferent(boolean b) {
		if (this.jCheckBoxTotalVersusDifferent != null) {
			this.jCheckBoxTotalVersusDifferent.setEnabled(b);
			if (!b)
				this.jCheckBoxTotalVersusDifferent.setSelected(b);
		}

	}

	protected void enableShowDifferentIdentifications(boolean b) {
		if (this.jCheckBoxShowDifferentIdentifications != null) {
			this.jCheckBoxShowDifferentIdentifications.setEnabled(b);
			if (!b)
				this.jCheckBoxShowDifferentIdentifications.setSelected(b);
		}

	}

	public boolean isTotalVersusDifferentSelected() {
		if (this.jCheckBoxTotalVersusDifferent != null) {
			return this.jCheckBoxTotalVersusDifferent.isSelected();
		}
		return false;
	}

	public JLabel getJLabelIntersectionsText() {
		if (this.jLabelIntersectionsText == null)
			this.jLabelIntersectionsText = new JLabel();
		return this.jLabelIntersectionsText;
	}

	public void setIntersectionText(String text) {
		getJLabelIntersectionsText().setText("<html>" + text + "</html>");
		getJLabelIntersectionsText().setToolTipText(
				getJLabelIntersectionsText().getText());
	}

	public JPanel getChr16MappingControls() {
		JPanel jPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;

		c.gridwidth = 2;
		c.gridy = 0;
		jPanel.add(
				new JLabel(
						"<html><b>Note</b>: This chart is specially designed for the<br>"
								+ "spanish participants in the HPP consortium that<br>"
								+ "analyse the Human Chromosome 16."
								+ "<br>"
								+ "<b>Note 2</b>: Number of genes can be different from<br>"
								+ "number of proteins, since isoforms are considered<br>"
								+ "as different proteins<br><br>" + "</html>"),
				c);

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
		if (this.known_unknown == null) {
			this.known_unknown = new JComboBox(new DefaultComboBoxModel(
					new String[] { BOTH, KNOWN, UNKNOWN }));
			this.known_unknown.setToolTipText(text2);
			this.known_unknown
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							frame.startShowingChart();
						}
					});
		}
		c.gridx = 1;
		jPanel.add(this.known_unknown, c);

		c.gridx = 0;
		if (this.notAssigned == null) {
			this.notAssigned = new JCheckBox("show not assigned");
			this.notAssigned
					.setToolTipText("Show genes or protein products not assigned to any research group");
			this.notAssigned.setSelected(true);
			this.notAssigned.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					frame.startShowingChart();
				}
			});
		}
		c.gridy++;

		jPanel.add(this.notAssigned, c);

		c.gridy++;

		jPanel.add(this.getTakeGeneFromFirstProteinCheckbox(), c);

		c.gridx = 0;
		c.gridwidth = 2;
		c.gridy++;
		jPanel.add(new JLabel("Groups:"), c);

		if (this.assignedGroups.isEmpty()) {
			List<String> assignedGroupsStrings = GeneDistributionReader
					.getInstance().getAssignedGroupsNames();

			for (String group : assignedGroupsStrings) {
				JCheckBox checkBox = new JCheckBox(group);
				checkBox.setSelected(true);
				checkBox.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						frame.startShowingChart();
					}
				});
				this.assignedGroups.add(checkBox);
			}
		}
		c.gridwidth = 1;
		boolean left = true;
		for (JCheckBox jcheckBox : this.assignedGroups) {

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
		JLabel label = new JLabel(
				"<html><br><br>Click here for just filter Chr16 proteins</html>");
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
		if (this.assignedGroups != null) {
			for (JCheckBox jcheckBox : this.assignedGroups) {
				if (jcheckBox.isSelected())
					ret.add(jcheckBox.getText());
			}
		}
		log.info(ret.size() + " groups selected");
		return ret;
	}

	public boolean isNotAssignedShowed() {
		if (this.notAssigned != null) {
			return this.notAssigned.isSelected();
		}
		return false;
	}

	public String getProteinOrGene() {
		if (this.proteinOrGeneSelector != null) {
			return (String) this.proteinOrGeneSelector.getSelectedItem();
		}
		return null;
	}

	public String getPeptideOrPSM() {
		if (this.peptideOrPSMSelector != null) {
			return (String) this.peptideOrPSMSelector.getSelectedItem();
		}
		return null;
	}

	public String getChr16KnownOrUnknown() {
		if (this.known_unknown != null)
			return (String) this.known_unknown.getSelectedItem();
		return null;
	}

	public JCheckBox getShowPSMCheckBox() {

		if (this.jCheckBoxShowPSM == null) {
			this.jCheckBoxShowPSM = new JCheckBox("Show PSMs");
			this.jCheckBoxShowPSM.setSelected(true);

			controlList.add(jCheckBoxShowPSM);
		}

		this.jCheckBoxShowPSM
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						frame.startShowingChart();
					}
				});

		return jCheckBoxShowPSM;
	}

	public JCheckBox getShowPeptidesCheckBox() {

		if (this.jCheckBoxShowPeptides == null) {
			this.jCheckBoxShowPeptides = new JCheckBox("Show Peptides");
			this.jCheckBoxShowPeptides.setSelected(true);

			controlList.add(jCheckBoxShowPeptides);
		}

		this.jCheckBoxShowPeptides
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						frame.startShowingChart();
					}
				});

		return jCheckBoxShowPeptides;
	}

	public JCheckBox getShowProteinsCheckBox() {

		if (this.jCheckBoxShowProteins == null) {
			this.jCheckBoxShowProteins = new JCheckBox("Show Proteins");
			this.jCheckBoxShowProteins.setSelected(true);
			controlList.add(jCheckBoxShowProteins);
		}

		this.jCheckBoxShowProteins
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						frame.startShowingChart();
					}
				});

		return jCheckBoxShowProteins;
	}

	public JCheckBox getShowScoreVsFDRCheckBox() {

		if (this.jCheckBoxShowScoreVsFDR == null) {
			this.jCheckBoxShowScoreVsFDR = new JCheckBox(
					"Show Score vs Num. proteins");
			this.jCheckBoxShowScoreVsFDR.setSelected(true);
			controlList.add(jCheckBoxShowScoreVsFDR);
		}

		this.jCheckBoxShowScoreVsFDR
				.addItemListener(new java.awt.event.ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						frame.startShowingChart();
					}
				});

		return jCheckBoxShowScoreVsFDR;
	}

	public boolean showScoreVsFDR() {
		if (this.jCheckBoxShowScoreVsFDR != null) {
			return this.jCheckBoxShowScoreVsFDR.isSelected();
		}
		return false;
	}

	public boolean showPSMs() {
		if (this.jCheckBoxShowPSM != null) {
			return this.jCheckBoxShowPSM.isSelected();
		}
		return false;
	}

	public boolean showPeptides() {
		if (this.jCheckBoxShowPeptides != null) {
			return this.jCheckBoxShowPeptides.isSelected();
		}
		return false;
	}

	public boolean showProteins() {
		if (this.jCheckBoxShowProteins != null) {
			return this.jCheckBoxShowProteins.isSelected();
		}
		return false;
	}

	public JCheckBox getShowTotalChromosomeProteins() {
		if (this.jCheckBoxShowTotalChromosomeProteins == null) {
			this.jCheckBoxShowTotalChromosomeProteins = new JCheckBox(
					"Show total prots. from chrms.");
			this.jCheckBoxShowTotalChromosomeProteins
					.addItemListener(new java.awt.event.ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							frame.startShowingChart();
						}
					});
		}
		return jCheckBoxShowTotalChromosomeProteins;
	}

	public boolean isShownTotalChromosomeProteins() {
		if (this.jCheckBoxShowTotalChromosomeProteins != null) {
			return this.jCheckBoxShowTotalChromosomeProteins.isSelected();
		}
		return false;
	}

	public JCheckBox getHeatMapBinaryCheckBox(boolean b) {
		if (this.jcheckBoxHeatMapBinary == null) {
			this.jcheckBoxHeatMapBinary = new JCheckBox("presence/absence");
			this.jcheckBoxHeatMapBinary
					.addItemListener(new java.awt.event.ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent evt) {
							frame.startShowingChart();
							// Disable jcombobox of color scale if binary is
							// selected
							if (AdditionalOptionsPanelFactory.this.jComboBoxColorScale != null) {
								AdditionalOptionsPanelFactory.this.jComboBoxColorScale
										.setEnabled(((JCheckBox) evt
												.getSource()).isSelected());
							}
						}
					});
			this.jcheckBoxHeatMapBinary
					.setToolTipText("If checked, the heatmap will show only two different values: present (red) or not present (green)");
		}
		this.controlList.add(this.jcheckBoxHeatMapBinary);
		return this.jcheckBoxHeatMapBinary;
	}

	public boolean isHeatMapBinary() {
		if (this.jcheckBoxHeatMapBinary != null)
			return this.jcheckBoxHeatMapBinary.isSelected();
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
		if (this.proteinOrGeneSelector == null) {
			this.proteinOrGeneSelector = new JComboBox(
					new DefaultComboBoxModel(new String[] { BOTH, PROTEIN,
							GENES }));
			this.proteinOrGeneSelector.setToolTipText(string);
			this.proteinOrGeneSelector
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							frame.startShowingChart();
						}
					});
		}

		c.gridx++;
		jPanel.add(this.proteinOrGeneSelector, c);
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
		if (this.peptideOrPSMSelector == null) {
			this.peptideOrPSMSelector = new JComboBox(new DefaultComboBoxModel(
					new String[] { BOTH, PEPTIDE, PSM }));
			this.peptideOrPSMSelector.setToolTipText(string);
			this.peptideOrPSMSelector
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							frame.startShowingChart();
						}
					});
		}

		c.gridx++;
		jPanel.add(this.peptideOrPSMSelector, c);
		return jPanel;
	}

	public JPanel getJcheckBoxOneProteinPerGroup() {
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.PAGE_AXIS));

		if (this.jradioButtonFirstProteinPerGroup == null
				|| this.jradioButtonAllProteinsPerGroup == null
				|| this.jradioButtonBestProteinPerGroup == null
				|| this.jradioButtonShareOneProtein == null) {
			this.jradioButtonAllProteinsPerGroup = new JRadioButton(
					"share all proteins", true);
			this.jradioButtonAllProteinsPerGroup
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							frame.startShowingChart();
						}
					});

			this.jradioButtonFirstProteinPerGroup = new JRadioButton(
					"share the first protein");
			this.jradioButtonFirstProteinPerGroup
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							frame.startShowingChart();
						}
					});

			this.jradioButtonBestProteinPerGroup = new JRadioButton(
					"share the best protein per group");
			this.jradioButtonBestProteinPerGroup
					.addItemListener(new java.awt.event.ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							frame.startShowingChart();
						}
					});
			this.jradioButtonShareOneProtein = new JRadioButton(
					"share any protein");
			this.jradioButtonShareOneProtein
					.addItemListener(new java.awt.event.ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							frame.startShowingChart();
						}
					});

			ButtonGroup group = new ButtonGroup();
			group.add(jradioButtonAllProteinsPerGroup);
			group.add(jradioButtonBestProteinPerGroup);
			group.add(jradioButtonFirstProteinPerGroup);
		}
		this.jradioButtonAllProteinsPerGroup
				.setToolTipText("<html>Take into account <b>all the proteins </b> per each protein group<br>"
						+ "(just groups containing the same set of proteins will be considered as equivalents).</html>");
		this.jradioButtonFirstProteinPerGroup
				.setToolTipText("<html>Take into account just the <b>first protein</b> per each protein group.</html>");
		this.jradioButtonBestProteinPerGroup
				.setToolTipText("<html>Take into account the <b>best protein</b> per each protein group<br>"
						+ "(the protein containing the best score if available, or<br>"
						+ "the protein containing the best peptide).</html>");

		jPanel.add(this.jradioButtonAllProteinsPerGroup);
		jPanel.add(this.jradioButtonFirstProteinPerGroup);
		jPanel.add(this.jradioButtonBestProteinPerGroup);
		return jPanel;
	}

	public ProteinGroupComparisonType getProteinGroupComparisonType() {
		if (this.jradioButtonFirstProteinPerGroup != null
				&& this.jradioButtonFirstProteinPerGroup.isSelected())
			return ProteinGroupComparisonType.FIRST_PROTEIN;
		if (this.jradioButtonAllProteinsPerGroup != null
				&& this.jradioButtonAllProteinsPerGroup.isSelected())
			return ProteinGroupComparisonType.ALL_PROTEINS;
		if (this.jradioButtonBestProteinPerGroup != null
				&& this.jradioButtonBestProteinPerGroup.isSelected())
			return ProteinGroupComparisonType.BEST_PROTEIN;
		if (this.jradioButtonShareOneProtein != null
				&& this.jradioButtonShareOneProtein.isSelected())
			return ProteinGroupComparisonType.SHARE_ONE_PROTEIN;
		return null;
	}

	public boolean isFirstProteinPerGroupSelected() {
		if (this.jradioButtonFirstProteinPerGroup != null)
			return this.jradioButtonFirstProteinPerGroup.isSelected();
		return false;
	}

	public boolean isAllProteinsPerGroupSelected() {
		if (this.jradioButtonAllProteinsPerGroup != null)
			return this.jradioButtonAllProteinsPerGroup.isSelected();
		return false;
	}

	public boolean isBestProteinPerGroupSelected() {
		if (this.jradioButtonBestProteinPerGroup != null)
			return this.jradioButtonBestProteinPerGroup.isSelected();
		return false;
	}

	public boolean isShareOneProteinSelected() {
		if (this.jradioButtonShareOneProtein != null)
			return this.jradioButtonShareOneProtein.isSelected();
		return false;
	}

	public boolean isShowAsSpiderPlot() {
		if (this.jcheckBoxShowSpiderPlot != null)
			return jcheckBoxShowSpiderPlot.isSelected();
		return false;
	}

	public JPanel getShowAsSpiderPlotCheckBox() {
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.PAGE_AXIS));

		if (this.jcheckBoxShowSpiderPlot == null) {
			this.jcheckBoxShowSpiderPlot = new JCheckBox("show spider plot");
			this.jcheckBoxShowSpiderPlot
					.addItemListener(new java.awt.event.ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							frame.startShowingChart();
						}
					});

		}

		jPanel.add(this.jcheckBoxShowSpiderPlot);

		return jPanel;
	}

	public JCheckBox getAccumulativeTrendCheckBox() {
		if (this.jcheckBoxAccumulativeTrend == null) {
			this.jcheckBoxAccumulativeTrend = new JCheckBox(
					"show accumulative trend");
			this.jcheckBoxAccumulativeTrend.setSelected(true);
			this.controlList.add(jcheckBoxAccumulativeTrend);
			this.jcheckBoxAccumulativeTrend
					.addItemListener(new java.awt.event.ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							frame.startShowingChart();
						}
					});

		}
		return this.jcheckBoxAccumulativeTrend;
	}

	public boolean isAccumulativeTrendSelected() {
		if (this.jcheckBoxAccumulativeTrend == null)
			return false;
		return this.jcheckBoxAccumulativeTrend.isSelected();
	}

	public JCheckBox getTakeGeneFromFirstProteinCheckbox() {
		if (this.jCheckBoxTakeGeneFromFirstProtein == null) {
			this.jCheckBoxTakeGeneFromFirstProtein = new JCheckBox(
					"take just gene from first in group");
			this.jCheckBoxTakeGeneFromFirstProtein.setSelected(true);
			this.controlList.add(jCheckBoxTakeGeneFromFirstProtein);
			this.jCheckBoxTakeGeneFromFirstProtein
					.addItemListener(new java.awt.event.ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							frame.startShowingChart();
						}
					});

		}
		return this.jCheckBoxTakeGeneFromFirstProtein;
	}

	public boolean isTakeGeneFromFirstProteinSelected() {
		if (this.jCheckBoxTakeGeneFromFirstProtein != null)
			return this.jCheckBoxTakeGeneFromFirstProtein.isSelected();
		return false;
	}

	public JPanel getFDRCheckBoxesPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		if (this.jCheckBoxShowProteinFDR == null) {
			this.jCheckBoxShowProteinFDR = new JCheckBox(
					"show FDR at protein level");
			this.controlList.add(jCheckBoxShowProteinFDR);
			this.jCheckBoxShowProteinFDR.setSelected(true);

			this.jCheckBoxShowProteinFDR
					.addItemListener(new java.awt.event.ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							frame.startShowingChart();
						}
					});
		}
		c.gridx = 0;
		c.gridy = 0;
		panel.add(jCheckBoxShowProteinFDR, c);
		if (this.jCheckBoxShowPeptideFDR == null) {
			this.jCheckBoxShowPeptideFDR = new JCheckBox(
					"show FDR at peptide level");
			this.controlList.add(jCheckBoxShowPeptideFDR);
			this.jCheckBoxShowPeptideFDR
					.addItemListener(new java.awt.event.ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							frame.startShowingChart();
						}
					});
		}
		c.gridy++;
		panel.add(jCheckBoxShowPeptideFDR, c);
		if (this.jCheckBoxShowPSMFDR == null) {
			this.jCheckBoxShowPSMFDR = new JCheckBox("show FDR at PSM level");
			this.controlList.add(jCheckBoxShowPSMFDR);
			this.jCheckBoxShowPSMFDR
					.addItemListener(new java.awt.event.ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							frame.startShowingChart();
						}
					});
		}
		c.gridy++;
		panel.add(jCheckBoxShowPSMFDR, c);

		return panel;
	}

	public boolean showProteinFDRLevel() {
		if (this.jCheckBoxShowProteinFDR == null)
			return false;
		return this.jCheckBoxShowProteinFDR.isSelected();
	}

	public boolean showPeptideFDRLevel() {
		if (this.jCheckBoxShowPeptideFDR == null)
			return false;
		return this.jCheckBoxShowPeptideFDR.isSelected();
	}

	public boolean showPSMFDRLevel() {
		if (this.jCheckBoxShowPSMFDR == null)
			return false;
		return this.jCheckBoxShowPSMFDR.isSelected();
	}

	public JComboBox getFontComboBox() {
		if (this.jComboBoxFont == null) {
			this.jComboBoxFont = new JComboBox();

			this.controlList.add(jComboBoxFont);
			this.jComboBoxFont
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							frame.startShowingChart();
						}
					});
		}
		this.jComboBoxFont.setModel(new DefaultComboBoxModel(WordCramChart
				.getFonts()));
		return this.jComboBoxFont;
	}

	public String getFont() {
		if (this.jComboBoxFont != null) {
			return (String) this.jComboBoxFont.getSelectedItem();
		}
		return null;
	}

	public JTextField getMaxNumberWordsText() {
		if (this.jTextFieldMaxNumberWords == null) {
			this.jTextFieldMaxNumberWords = new JTextField(10);
			this.controlList.add(jTextFieldMaxNumberWords);
			this.jTextFieldMaxNumberWords
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							frame.startShowingChart();
						}
					});
			this.jTextFieldMaxNumberWords.setText("100");
		}
		return this.jTextFieldMaxNumberWords;
	}

	public int getMaxNumberWords() {
		if (this.jTextFieldMaxNumberWords != null) {
			try {
				return Integer.valueOf(this.jTextFieldMaxNumberWords.getText());
			} catch (NumberFormatException e) {

			}
		}
		return 1000;
	}

	public JTextField getMinWordLengthText() {
		if (this.jTextFieldMinWordLength == null) {
			this.jTextFieldMinWordLength = new JTextField(10);
			this.controlList.add(jTextFieldMinWordLength);
			this.jTextFieldMinWordLength
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							frame.startShowingChart();
						}
					});
			this.jTextFieldMinWordLength.setText("4");
		}
		return this.jTextFieldMinWordLength;
	}

	public int getMinWordLength() {
		if (this.jTextFieldMinWordLength != null) {
			try {
				return Integer.valueOf(this.jTextFieldMinWordLength.getText());
			} catch (NumberFormatException e) {

			}
		}
		return 4;
	}

	public JTextArea getSkipWordsTextArea() {
		if (this.jTextAreaSkipWords == null) {
			this.jTextAreaSkipWords = new JTextArea(10, 20);
			final List<String> defaultSkippedWords = WordCramChart
					.getDefaultSkippedWords();
			SorterUtil.sortStringByLength(defaultSkippedWords, false);
			StringBuilder sb = new StringBuilder();
			int minWordLength = this.getMinWordLength();
			for (String string : defaultSkippedWords) {
				if (string.length() >= minWordLength)
					sb.append(string + "\n");
			}
			this.jTextAreaSkipWords.setText(sb.toString());
			this.controlList.add(jTextAreaSkipWords);
		}
		this.jTextAreaSkipWords.setRows(5);
		return this.jTextAreaSkipWords;
	}

	public List<String> getSkipWords() {
		List<String> list = new ArrayList<String>();
		if (this.jTextAreaSkipWords != null) {
			final String text = this.jTextAreaSkipWords.getText();
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
		if (this.jButtonDrawWordCram == null) {
			this.jButtonDrawWordCram = new JButton("Redraw");
			this.controlList.add(jButtonDrawWordCram);
			this.jButtonDrawWordCram
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							frame.startShowingChart();
						}
					});
		}
		return this.jButtonDrawWordCram;
	}

	public JButton getSaveDrawWordCramButton() {
		if (this.jButtonSaveDrawWordCram == null) {
			this.jButtonSaveDrawWordCram = new JButton("Save image to file");
			this.controlList.add(jButtonSaveDrawWordCram);
			this.jButtonSaveDrawWordCram
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {

							frame.saveWordCramImage();
						}
					});
		}
		return this.jButtonSaveDrawWordCram;
	}

	public JLabel getJLabelSelectedWord() {
		if (this.jLabelSelectedWord == null) {
			this.jLabelSelectedWord = new JLabel("(selected word)");
			this.controlList.add(jLabelSelectedWord);
		}
		final Font previousFont = this.jLabelSelectedWord.getFont();
		this.jLabelSelectedWord.setFont(new Font(previousFont.getName(),
				Font.BOLD, previousFont.getSize()));
		return this.jLabelSelectedWord;
	}

	public JLabel getJLabelSelectedProteins() {
		if (this.jLabelSelectedProteins == null) {
			this.jLabelSelectedProteins = new JLabel(
					"(proteins containing selected word)");
			this.controlList.add(jLabelSelectedProteins);

		}
		final Font previousFont = this.jLabelSelectedProteins.getFont();
		this.jLabelSelectedProteins.setFont(new Font(previousFont.getName(),
				Font.BOLD, previousFont.getSize()));
		return this.jLabelSelectedProteins;
	}

	public JCheckBox getApplyLogCheckBox() {
		if (this.jCheckBoxApplyLog == null) {
			this.jCheckBoxApplyLog = new JCheckBox("Apply logs");
			this.jCheckBoxApplyLog
					.setToolTipText("Apply logarithm to base=10 to values");
			this.jCheckBoxApplyLog
					.addItemListener(new java.awt.event.ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							frame.startShowingChart();
						}
					});
			this.controlList.add(jCheckBoxApplyLog);
		}
		return this.jCheckBoxApplyLog;
	}

	public boolean isApplyLog() {
		if (this.jCheckBoxApplyLog != null) {
			return this.jCheckBoxApplyLog.isSelected();
		}
		return false;
	}

	public JCheckBox getSeparatedDecoyHitsCheckBox() {
		if (this.jCheckBoxSeparatedDecoyHits == null) {
			this.jCheckBoxSeparatedDecoyHits = new JCheckBox(
					"Separate decoy hits");
			this.jCheckBoxSeparatedDecoyHits
					.setToolTipText("Show a separate series for decoy hits");
			this.jCheckBoxSeparatedDecoyHits
					.addItemListener(new java.awt.event.ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							frame.startShowingChart();
						}
					});
			this.controlList.add(jCheckBoxSeparatedDecoyHits);
		}
		return this.jCheckBoxSeparatedDecoyHits;
	}

	public boolean isSeparatedDecoyHits() {
		if (this.jCheckBoxSeparatedDecoyHits != null) {
			return this.jCheckBoxSeparatedDecoyHits.isSelected();
		}
		return false;
	}

	public JCheckBox getShowInMinutesCheckBox() {
		if (this.jCheckBoxShowInMinutes == null) {
			this.jCheckBoxShowInMinutes = new JCheckBox("Show time in minutes");
			this.jCheckBoxShowInMinutes
					.setToolTipText("Show times in minutes if selected or in seconds if not selected");
			this.jCheckBoxShowInMinutes
					.addItemListener(new java.awt.event.ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							frame.startShowingChart();
						}
					});
			this.jCheckBoxShowInMinutes.setSelected(true);
			this.controlList.add(jCheckBoxShowInMinutes);
		}
		return this.jCheckBoxShowInMinutes;
	}

	public boolean showInMinutes() {
		if (this.jCheckBoxShowInMinutes != null) {
			return this.jCheckBoxShowInMinutes.isSelected();
		}
		return false;
	}

	public JCheckBox getIsPSMorPeptideCheckBox() {
		if (this.jCheckBoxIsPSMorPeptide == null) {
			this.jCheckBoxIsPSMorPeptide = new JCheckBox("Number of PSMs");
			this.jCheckBoxIsPSMorPeptide
					.setToolTipText("<html>If selected, the colors represent the number of PSMs per protein.<br>"
							+ "If not selected, the colors represent the number of peptides per protein.</html>");
			this.jCheckBoxIsPSMorPeptide
					.addItemListener(new java.awt.event.ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							frame.startShowingChart();
						}
					});
			this.jCheckBoxIsPSMorPeptide.setSelected(true);
			this.controlList.add(jCheckBoxIsPSMorPeptide);
		}
		return this.jCheckBoxIsPSMorPeptide;
	}

	/**
	 * Returns true if it represents PSMs or false if represents peptides
	 * 
	 * @return
	 */
	public boolean isPSMs() {
		return this.jCheckBoxIsPSMorPeptide.isSelected();
	}
}
