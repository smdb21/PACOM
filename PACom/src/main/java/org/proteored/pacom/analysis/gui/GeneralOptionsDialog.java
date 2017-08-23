/*
 * GeneralOptions.java Created on __DATE__, __TIME__
 */

package org.proteored.pacom.analysis.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;

import org.jfree.ui.RefineryUtilities;
import org.proteored.miapeapi.experiment.model.datamanager.DataManager;

import com.lowagie.text.Font;

/**
 *
 * @author __USER__
 */
public class GeneralOptionsDialog extends javax.swing.JDialog {
	private static GeneralOptionsDialog instance;
	private Integer previousMinPeptideLength;
	private Boolean previousGroupAtExperimentListLevel;
	private Boolean previousDoNotGroupNonConclusiveProteins;
	private Boolean previousSeparateNonConclusiveProteins;
	private final ChartManagerFrame parent;
	private boolean dataShouldnotBeLoaded;

	public static GeneralOptionsDialog getInstance(ChartManagerFrame parent, boolean showDoNotAskAgain) {
		getInstance(parent);

		instance.jCheckBoxDoNotAskAgain.setVisible(showDoNotAskAgain);
		return instance;
	}

	public static GeneralOptionsDialog getInstance(ChartManagerFrame parent) {
		if (instance == null) {
			instance = new GeneralOptionsDialog(parent);
		} else {
		}

		String numText = instance.jTextFieldPeptideLength.getText();
		Integer num = null;
		try {
			num = Integer.valueOf(numText);
		} catch (NumberFormatException e) {
			num = DataManager.DEFAULT_MIN_PEPTIDE_LENGTH;
		}
		instance.previousMinPeptideLength = num;
		instance.previousGroupAtExperimentListLevel = instance.groupProteinsAtExperimentListLevel();
		instance.previousDoNotGroupNonConclusiveProteins = instance.isDoNotGroupNonConclusiveProteins();
		instance.previousSeparateNonConclusiveProteins = instance.isSeparateNonConclusiveProteins();
		return instance;
	}

	@Override
	public void setVisible(boolean b) {
		showWarningLabel(false);
		if (b) {
			pack();
		}
		super.setVisible(b);
	}

	public void restoreDefaultValues() {
		jTextFieldPeptideLength.setText(String.valueOf(DataManager.DEFAULT_MIN_PEPTIDE_LENGTH));
	}

	public boolean isDoNotAskAgainSelected() {
		return jCheckBoxDoNotAskAgain.isSelected();
	}

	public boolean isDoNotGroupNonConclusiveProteins() {
		return jCheckboxDiscardNonconclusiveor.isSelected();
	}

	public boolean isSeparateNonConclusiveProteins() {
		return jCheckBoxSeparateNonconclusivesubset.isSelected();

	}

	@Override
	public void dispose() {

		super.dispose();
		Integer minPeptideLength = getMinPeptideLength();
		boolean groupAtExperimentListLevel = groupProteinsAtExperimentListLevel();
		boolean localProcessingInParallel = isLocalProcessingInParallel();
		boolean doNotGroupNonConclusiveProteins = isDoNotGroupNonConclusiveProteins();
		boolean separateNonConclusiveProteins = isSeparateNonConclusiveProteins();
		final boolean dataShouldBeLoaded = parent.dataShouldBeLoaded(parent.cfgFile, groupAtExperimentListLevel,
				doNotGroupNonConclusiveProteins, separateNonConclusiveProteins, minPeptideLength,
				localProcessingInParallel);
		if (dataShouldBeLoaded && !dataShouldnotBeLoaded)
			parent.loadData(minPeptideLength, groupAtExperimentListLevel, doNotGroupNonConclusiveProteins,
					separateNonConclusiveProteins, localProcessingInParallel);
		dataShouldnotBeLoaded = false;
	}

	public void disposeNotLoadingData() {
		this.dataShouldnotBeLoaded = true;
		dispose();
	}

	public Integer getMinPeptideLength() {
		try {

			return Integer.valueOf(jTextFieldPeptideLength.getText());
		} catch (Exception e) {
			// by default
			return null;
		}
	}

	public boolean groupProteinsAtExperimentListLevel() {
		return jCheckBoxGroupProteinsInExperimentList.isSelected();
	}

	public boolean isLocalProcessingInParallel() {
		return false;
	}

	public GeneralOptionsDialog() {
		this(null);
	}

	/** Creates new form GeneralOptions */
	public GeneralOptionsDialog(ChartManagerFrame parent) {
		super(parent, true);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
				| IllegalAccessException e) {
			e.printStackTrace();
		}
		this.setFont(new java.awt.Font("TimesRoman", Font.BOLD, 20));
		initComponents();
		setTitle("General Options");
		this.parent = parent;
		jLabelWarning.setVisible(false);
		RefineryUtilities.centerFrameOnScreen(this);

	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		jPanel1 = new javax.swing.JPanel();
		jLabelWarning = new javax.swing.JLabel();
		jPanel2 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jTextFieldPeptideLength = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		jPanel3 = new javax.swing.JPanel();
		jCheckBoxGroupProteinsInExperimentList = new javax.swing.JCheckBox();

		jPanel5 = new javax.swing.JPanel();
		jButtonClose = new javax.swing.JButton();
		jCheckBoxDoNotAskAgain = new javax.swing.JCheckBox();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("General Options");
		setResizable(false);

		jLabelWarning.setForeground(new java.awt.Color(204, 0, 51));
		jLabelWarning.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		jLabelWarning.setText("<html><b>Note</b>: Any change here will cause project data reloading.</html>");

		jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"Minimum peptide length"));

		jLabel1.setText("Do not process peptides sequences containing less than ");
		jLabel1.setToolTipText(
				"<html>Peptides with short sequences can be somehow problematic, <br> since short decoy sequences could have similar identification <br> scores than a non decoy short sequence.</html>");

		jTextFieldPeptideLength.setHorizontalAlignment(javax.swing.JTextField.CENTER);
		jTextFieldPeptideLength.setText(String.valueOf(DataManager.DEFAULT_MIN_PEPTIDE_LENGTH));
		jTextFieldPeptideLength.setToolTipText(
				"<html>Peptides with short sequences can be somehow problematic, <br>\nsince short decoy sequences could have similar identification <br>\nscores than a non decoy short sequence.</html>");
		jTextFieldPeptideLength.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyReleased(java.awt.event.KeyEvent evt) {
				jTextFieldPeptideLengthKeyReleased(evt);
			}
		});

		jLabel2.setText("AA");
		jLabel2.setToolTipText(
				"<html>Peptides with short sequences can be somehow problematic, <br> since short decoy sequences could have similar identification <br> scores than a non decoy short sequence.</html>");

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup().addContainerGap().addComponent(jLabel1)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jTextFieldPeptideLength, javax.swing.GroupLayout.PREFERRED_SIZE, 28,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel2)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		jPanel2Layout
				.setVerticalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanel2Layout.createSequentialGroup().addContainerGap().addGroup(jPanel2Layout
								.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel1)
								.addComponent(jTextFieldPeptideLength, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabel2))
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jPanel3.setBorder(
				new TitledBorder(null, "Protein grouping options", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		jCheckBoxGroupProteinsInExperimentList.setSelected(true);
		jCheckBoxGroupProteinsInExperimentList.setText("Group proteins at level 0");
		jCheckBoxGroupProteinsInExperimentList.setToolTipText(
				"<html>\r\nProtein grouping algorithm is applied at level 1.<br>\r\nIf this option is disabled, protein groups at level 0 are not recalculated.<br>\r\nIf this option is enabled, all protein groups in level 0 will be rebuilded from <br>\r\npeptides comming from level 1<br>\r\nNote that the resulting protein groups will not be able to be repeated.</html>");
		jCheckBoxGroupProteinsInExperimentList.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxGroupProteinsInExperimentListActionPerformed(evt);
			}
		});

		jCheckboxDiscardNonconclusiveor = new JCheckBox("Discard Non-Conclusive (subset) proteins");
		jCheckboxDiscardNonconclusiveor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedDiscardNonConclusive();
			}
		});
		jCheckboxDiscardNonconclusiveor.setSelected(true);
		jCheckboxDiscardNonconclusiveor.setToolTipText(
				"<html>\r\nNon-Conclusive proteins are proteins which have all their peptides <br>\r\nshared with other proteins, so they are subset proteins.<br>\r\nIf this option is enabled, Non-Conclusive proteins are discarded.\r\n</html>");

		jCheckBoxSeparateNonconclusivesubset = new JCheckBox("Separate Non-conclusive (subset) proteins");
		jCheckBoxSeparateNonconclusivesubset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				separateNonConclusiveSelected();
			}
		});
		jCheckBoxSeparateNonconclusivesubset.setEnabled(false);
		jCheckBoxSeparateNonconclusivesubset.setToolTipText(
				"<html>\r\nNon-Conclusive proteins are proteins which have all their peptides <br>\r\nshared with other proteins, so they are subset proteins.<br>\r\nIf this option is enabled, Non-Conclusive proteins will be separated <br>\r\nfrom the proteins which share peptides with them.<br>\r\nIf this options is disabled, Non-Conclusibe proteins will be integrated <br>\r\nin all other protein groups which share peptides with them.<br>\r\nTake into account this for protein group comparisons in overlapping of proteins.\r\n</html>\r\n</html>");

		javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
		jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanel3Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
								.addComponent(jCheckBoxGroupProteinsInExperimentList)
								.addComponent(jCheckBoxSeparateNonconclusivesubset)
								.addComponent(jCheckboxDiscardNonconclusiveor))
						.addContainerGap(494, Short.MAX_VALUE)));
		jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanel3Layout.createSequentialGroup().addContainerGap()
						.addComponent(jCheckBoxGroupProteinsInExperimentList)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(jCheckboxDiscardNonconclusiveor)
						.addGap(1).addComponent(jCheckBoxSeparateNonconclusivesubset).addContainerGap()));
		jPanel3.setLayout(jPanel3Layout);

		jButtonClose.setText("OK");
		jButtonClose.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonCloseActionPerformed(evt);
			}
		});

		jCheckBoxDoNotAskAgain.setText("do not ask again");
		jCheckBoxDoNotAskAgain.setToolTipText("Do not show again this dialog");

		javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
		jPanel5.setLayout(jPanel5Layout);
		jPanel5Layout.setHorizontalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel5Layout.createSequentialGroup().addContainerGap().addComponent(jButtonClose)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 205, Short.MAX_VALUE)
						.addComponent(jCheckBoxDoNotAskAgain).addContainerGap()));
		jPanel5Layout.setVerticalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel5Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jButtonClose).addComponent(jCheckBoxDoNotAskAgain))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING).addGroup(jPanel1Layout
				.createSequentialGroup().addContainerGap()
				.addGroup(jPanel1Layout.createParallelGroup(Alignment.TRAILING, false)
						.addComponent(jPanel5, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE)
						.addComponent(jLabelWarning, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
								GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(Alignment.LEADING, jPanel1Layout.createParallelGroup(Alignment.TRAILING, false)
								.addComponent(jPanel3, Alignment.LEADING, 0, 0, Short.MAX_VALUE).addComponent(jPanel2,
										Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 403, Short.MAX_VALUE)))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING).addGroup(jPanel1Layout
				.createSequentialGroup()
				.addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(jLabelWarning, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED).addComponent(jPanel5, GroupLayout.PREFERRED_SIZE,
						GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)));
		jPanel1.setLayout(jPanel1Layout);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		pack();
	}// </editor-fold>

	protected void separateNonConclusiveSelected() {
		if (Boolean.compare(isSeparateNonConclusiveProteins(), previousSeparateNonConclusiveProteins) != 0) {
			showWarningLabel(true);
		} else {
			showWarningLabel(false);
		}

	}

	// GEN-END:initComponents

	protected void selectedDiscardNonConclusive() {
		jCheckBoxSeparateNonconclusivesubset.setEnabled(!jCheckboxDiscardNonconclusiveor.isSelected());
		if (Boolean.compare(isDoNotGroupNonConclusiveProteins(), previousDoNotGroupNonConclusiveProteins) != 0) {
			showWarningLabel(true);
		} else {
			showWarningLabel(false);
		}
	}

	private void jCheckBoxGroupProteinsInExperimentListActionPerformed(java.awt.event.ActionEvent evt) {
		if (Boolean.compare(groupProteinsAtExperimentListLevel(), previousGroupAtExperimentListLevel) != 0) {
			showWarningLabel(true);
		} else {
			showWarningLabel(false);
		}
	}

	private void jTextFieldPeptideLengthKeyReleased(java.awt.event.KeyEvent evt) {
		Integer min = Integer.MAX_VALUE;
		try {
			min = Integer.valueOf(jTextFieldPeptideLength.getText());
		} catch (NumberFormatException e) {

		}
		if (!min.equals(previousMinPeptideLength))
			showWarningLabel(true);
		else {
			showWarningLabel(false);
		}
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			dispose();
		}
	}

	private void showWarningLabel(boolean show) {
		jLabelWarning.setVisible(show);
		pack();
	}

	private void jButtonCloseActionPerformed(java.awt.event.ActionEvent evt) {
		dispose();
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				GeneralOptionsDialog dialog = new GeneralOptionsDialog(null);
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
	private javax.swing.JButton jButtonClose;
	private javax.swing.JCheckBox jCheckBoxDoNotAskAgain;
	private javax.swing.JCheckBox jCheckBoxGroupProteinsInExperimentList;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabelWarning;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JTextField jTextFieldPeptideLength;
	private JCheckBox jCheckBoxSeparateNonconclusivesubset;
	private JCheckBox jCheckboxDiscardNonconclusiveor;
}