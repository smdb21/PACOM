/*
 * GeneralOptions.java Created on __DATE__, __TIME__
 */

package org.proteored.pacom.analysis.gui;

import java.awt.event.KeyEvent;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jfree.ui.RefineryUtilities;
import org.proteored.miapeapi.experiment.model.datamanager.DataManager;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

/**
 *
 * @author __USER__
 */
public class GeneralOptionsDialog extends javax.swing.JDialog {
	private static final int MAXIMUM_NUM_CORES = 6;
	private static GeneralOptionsDialog instance;
	private Integer previousMinPeptideLength;
	private Boolean previousGroupAtExperimentListLevel;
	private Boolean previousLocalProcessingInParallel;
	private final ChartManagerFrame parent;

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
		instance.previousLocalProcessingInParallel = instance.isLocalProcessingInParallel();
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

	@Override
	public void dispose() {

		super.dispose();
		Integer minPeptideLength = getMinPeptideLength();
		boolean groupAtExperimentListLevel = groupProteinsAtExperimentListLevel();
		boolean localProcessingInParallel = isLocalProcessingInParallel();
		final boolean dataShouldBeLoaded = parent.dataShouldBeLoaded(parent.cfgFile, groupAtExperimentListLevel,
				minPeptideLength, localProcessingInParallel);
		if (dataShouldBeLoaded)
			parent.loadData(minPeptideLength, groupAtExperimentListLevel, localProcessingInParallel);

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
		return jCheckBoxProcessInParallel.isSelected();
	}

	/** Creates new form GeneralOptions */
	private GeneralOptionsDialog(ChartManagerFrame parent) {
		super(parent, true);
		try {
			UIManager.setLookAndFeel(new WindowsLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
		}
		initComponents();
		setTitle("General Options");
		this.parent = parent;
		jLabelWarning.setVisible(false);
		RefineryUtilities.centerFrameOnScreen(this);

		String text = getCoresToUseText();
		jLabelUsingCores.setText(text);
	}

	private String getCoresToUseText() {
		final int availableProcessors = Runtime.getRuntime().availableProcessors();
		if (jCheckBoxProcessInParallel.isSelected()) {
			final int availableNumSystemCores = edu.scripps.yates.cores.SystemCoreManager
					.getAvailableNumSystemCores(MAXIMUM_NUM_CORES);
			return "Using " + availableNumSystemCores + " out of " + availableProcessors + " cores";
		} else {
			return "Using just 1 out of " + availableProcessors + " core";
		}

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
		jPanel4 = new javax.swing.JPanel();
		jCheckBoxProcessInParallel = new javax.swing.JCheckBox();
		jLabelUsingCores = new javax.swing.JLabel();
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
		jTextFieldPeptideLength.setText("7");
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

		jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"Aggregation level"));

		jCheckBoxGroupProteinsInExperimentList.setSelected(true);
		jCheckBoxGroupProteinsInExperimentList.setText("Group proteins at level 0");
		jCheckBoxGroupProteinsInExperimentList.setToolTipText(
				"<html>By default, protein grouping algorithm is applied at level 1<br>\nwhich is usually at experiment level. In that case, protein groups <br>\nat level 0 (experiment list) are not recalculated.<br>\nIf this option is selected, all protein groups will be rebuilded from <br>\npeptides comming from any lower level. <br>\nNote that the resulting protein groups will not be able to be repeated.</html>");
		jCheckBoxGroupProteinsInExperimentList.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxGroupProteinsInExperimentListActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
		jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel3Layout.createSequentialGroup().addContainerGap()
						.addComponent(jCheckBoxGroupProteinsInExperimentList).addContainerGap(214, Short.MAX_VALUE)));
		jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel3Layout.createSequentialGroup().addContainerGap()
						.addComponent(jCheckBoxGroupProteinsInExperimentList)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"Parallel processing"));

		jCheckBoxProcessInParallel.setSelected(true);
		jCheckBoxProcessInParallel.setText("Process in parallel");
		jCheckBoxProcessInParallel.setToolTipText(
				"<html>By default, protein grouping algorithm is applied at level 1<br>\nwhich is usually at experiment level. In that case, protein groups <br>\nat level 0 (experiment list) are not recalculated.<br>\nIf this option is selected, all protein groups will be rebuilded from <br>\npeptides comming from any lower level. <br>\nNote that the resulting protein groups will not be able to be repeated.</html>");
		jCheckBoxProcessInParallel.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxProcessInParallelActionPerformed(evt);
			}
		});

		jLabelUsingCores.setText("Using cores");

		javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
		jPanel4.setLayout(jPanel4Layout);
		jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel4Layout.createSequentialGroup().addContainerGap()
						.addComponent(jCheckBoxProcessInParallel).addGap(18, 18, 18).addComponent(jLabelUsingCores,
								javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
						.addContainerGap()));
		jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel4Layout.createSequentialGroup()
						.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jCheckBoxProcessInParallel).addComponent(jLabelUsingCores))
						.addContainerGap(18, Short.MAX_VALUE)));

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
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
								.addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(jLabelWarning, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup()
						.addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jLabelWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 46,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jPanel5,
								javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.PREFERRED_SIZE)));

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
		// GEN-END:initComponents

	private void jCheckBoxProcessInParallelActionPerformed(java.awt.event.ActionEvent evt) {

		jLabelUsingCores.setText(getCoresToUseText());

		if (!String.valueOf(jCheckBoxProcessInParallel.isSelected())
				.equals(String.valueOf(previousLocalProcessingInParallel)))
			showWarningLabel(true);
		else
			showWarningLabel(false);
	}

	private void jCheckBoxGroupProteinsInExperimentListActionPerformed(java.awt.event.ActionEvent evt) {
		if (!String.valueOf(jCheckBoxGroupProteinsInExperimentList.isSelected())
				.equals(String.valueOf(previousGroupAtExperimentListLevel)))
			showWarningLabel(true);
		else
			showWarningLabel(false);
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
	private javax.swing.JCheckBox jCheckBoxProcessInParallel;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabelUsingCores;
	private javax.swing.JLabel jLabelWarning;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JTextField jTextFieldPeptideLength;
	// End of variables declaration//GEN-END:variables

}