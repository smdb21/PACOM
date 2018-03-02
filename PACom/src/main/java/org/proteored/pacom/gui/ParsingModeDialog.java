/*
 * ParsingModeDialog.java Created on __DATE__, __TIME__
 */

package org.proteored.pacom.gui;

/**
 * 
 * @author __USER__
 */
public class ParsingModeDialog extends javax.swing.JDialog {

	private final MiapeExtractionFrame parentDialog;

	/** Creates new form ParsingModeDialog */
	public ParsingModeDialog(MiapeExtractionFrame miapeExtractionFrameNEW, boolean modal) {
		super(miapeExtractionFrameNEW, modal);
		initComponents();
		this.parentDialog = miapeExtractionFrameNEW;
	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		buttonGroup1 = new javax.swing.ButtonGroup();
		jPanel8 = new javax.swing.JPanel();
		jRadioButtonFastParsing = new javax.swing.JRadioButton();
		jRadioButtonSlowParsing = new javax.swing.JRadioButton();
		jScrollPane2 = new javax.swing.JScrollPane();
		jTextArea1 = new javax.swing.JTextArea();
		jButtonChoose = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Choose mzML parsing mode");

		jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("mzML parsing mode"));

		buttonGroup1.add(jRadioButtonFastParsing);
		jRadioButtonFastParsing.setSelected(true);
		jRadioButtonFastParsing.setText("shallow parsing (faster, recommended for large files)");

		buttonGroup1.add(jRadioButtonSlowParsing);
		jRadioButtonSlowParsing.setText("deep parsing (slower, recommended for small files)");
		jRadioButtonSlowParsing.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jRadioButtonSlowParsingActionPerformed(evt);
			}
		});

		jScrollPane2.setBorder(null);
		jScrollPane2.setAutoscrolls(true);

		jTextArea1.setColumns(20);
		jTextArea1.setEditable(false);
		jTextArea1.setFont(new java.awt.Font("Segoe UI", 0, 12));
		jTextArea1.setLineWrap(true);
		jTextArea1.setRows(5);
		jTextArea1.setText(
				"Choose one of these parsing modes:\n\n- fast parsing: 'run' element in mzML file is skipped (some minimal information like activation type will be missed). However, most of the information is captured. Large files (more than 5Gb) can be processed in 4 minutes.\n\n- deep parsing: using the jmzML API: for large files it takes several minutes, so it is recommended for smaller files (100 Mb.)\n\nNote: Although spectra and chromatograms are required by MIAPE MS guidelines, they are not extracted from the file, since MIAPE repository is not a proteomics raw repository. However, a link to the original mzML file will be provided in the general features section of the MIAPE MS.");
		jTextArea1.setWrapStyleWord(true);
		jTextArea1.setBorder(null);
		jTextArea1.setFocusable(false);
		jTextArea1.setOpaque(false);
		jScrollPane2.setViewportView(jTextArea1);

		jButtonChoose.setText("Select");
		jButtonChoose.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonChooseActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
		jPanel8.setLayout(jPanel8Layout);
		jPanel8Layout.setHorizontalGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel8Layout.createSequentialGroup().addContainerGap().addGroup(jPanel8Layout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
								jPanel8Layout.createSequentialGroup().addGroup(jPanel8Layout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
										.addComponent(jRadioButtonSlowParsing, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(jRadioButtonFastParsing, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										.addGap(62, 62, 62).addComponent(jButtonChoose))
						.addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE))
						.addContainerGap()));
		jPanel8Layout.setVerticalGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel8Layout.createSequentialGroup()
						.addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(jPanel8Layout.createSequentialGroup().addComponent(jRadioButtonFastParsing)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jRadioButtonSlowParsing))
						.addGroup(jPanel8Layout.createSequentialGroup().addContainerGap().addComponent(jButtonChoose)))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
						.addGap(24, 24, 24)));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE)
						.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jPanel8,
						javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap()));

		pack();
		java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		java.awt.Dimension dialogSize = getSize();
		setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);
	}// </editor-fold>
		// GEN-END:initComponents

	private void jRadioButtonSlowParsingActionPerformed(java.awt.event.ActionEvent evt) {
		// TODO add your handling code here:
	}

	private void jButtonChooseActionPerformed(java.awt.event.ActionEvent evt) {
		// check the radio buttons and fill the isFastParsing variable from the
		// parent dialog.
		if (this.jRadioButtonFastParsing.isSelected()) {
			this.parentDialog.isFastParsing = true;
			this.parentDialog.isShallowParsing = false;
		} else if (this.jRadioButtonSlowParsing.isSelected()) {
			this.parentDialog.isFastParsing = false;
			this.parentDialog.isShallowParsing = true;
		}
		// close
		this.closeDialog();
	}

	private void closeDialog() {
		super.dispose();
	}

	@Override
	public void dispose() {
		this.parentDialog.isFastParsing = false;
		this.parentDialog.isShallowParsing = false;
		super.dispose();
	}

	// GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.ButtonGroup buttonGroup1;
	private javax.swing.JButton jButtonChoose;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JRadioButton jRadioButtonFastParsing;
	private javax.swing.JRadioButton jRadioButtonSlowParsing;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JTextArea jTextArea1;
	// End of variables declaration//GEN-END:variables

}