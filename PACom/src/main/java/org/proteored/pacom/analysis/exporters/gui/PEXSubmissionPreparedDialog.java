/*
 * PEXSubmissionPreparedDialog.java
 *
 * Created on __DATE__, __TIME__
 */

package org.proteored.pacom.analysis.exporters.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jfree.ui.RefineryUtilities;
import org.proteored.pacom.gui.ImageManager;
import org.proteored.pacom.utils.HttpUtilities;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

/**
 * 
 * @author __USER__
 */
public class PEXSubmissionPreparedDialog extends javax.swing.JDialog {

	private static final String PEXWebsite = "http://www.proteomexchange.org/bulk-submission";
	private final File outputFolder;

	/** Creates new form PEXSubmissionPreparedDialog */
	public PEXSubmissionPreparedDialog(JDialog parent, boolean modal,
			File outputFolder) {
		super(parent);
		initComponents();

		this.outputFolder = outputFolder;
		try {
			UIManager.setLookAndFeel(new WindowsLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
		}
		// center on screen
		RefineryUtilities.centerFrameOnScreen(this);

		// load icons
		this.jButtonOpenPEXWebsite.setIcon(ImageManager
				.getImageIcon(ImageManager.PEX));
		this.jButtonOpenOutputFolder.setIcon(ImageManager
				.getImageIcon(ImageManager.FOLDER));
	}

	@Override
	public void setVisible(boolean b) {
		if (b)
			this.pack();
		super.setVisible(b);
	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		jPanelTop = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jPanelDown = new javax.swing.JPanel();
		jButtonOpenOutputFolder = new javax.swing.JButton();
		jButtonOpenPEXWebsite = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("ProteomeXchange Submission ready");

		jLabel1.setText("<html>Your data is now ready for a ProteomeXchange submission.<br><br>\nClick on <b>'Open output folder'</b> button so open it on your system.<br><br>\nClick on <b>'ProteomeXchange'</b> button to go to the ProteomeXchange<br>Bulk Submission page.<br>\nFollow instructions there using the data in the output folder.\n</html>");
		jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);

		javax.swing.GroupLayout jPanelTopLayout = new javax.swing.GroupLayout(
				jPanelTop);
		jPanelTop.setLayout(jPanelTopLayout);
		jPanelTopLayout.setHorizontalGroup(jPanelTopLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				jPanelTopLayout.createSequentialGroup().addContainerGap()
						.addComponent(jLabel1).addContainerGap()));
		jPanelTopLayout.setVerticalGroup(jPanelTopLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				jPanelTopLayout
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(jLabel1,
								javax.swing.GroupLayout.PREFERRED_SIZE, 119,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap()));

		jButtonOpenOutputFolder
				.setIcon(new javax.swing.ImageIcon(
						"C:\\Users\\Salva\\workspace\\miape-extractor\\src\\main\\resources\\folder.png")); // NOI18N
		jButtonOpenOutputFolder.setText("Open output folder");
		jButtonOpenOutputFolder
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jButtonOpenOutputFolderActionPerformed(evt);
					}
				});

		jButtonOpenPEXWebsite
				.setIcon(new javax.swing.ImageIcon(
						"C:\\Users\\Salva\\workspace\\miape-extractor\\src\\main\\resources\\pex.png")); // NOI18N
		jButtonOpenPEXWebsite
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jButtonOpenPEXWebsiteActionPerformed(evt);
					}
				});

		javax.swing.GroupLayout jPanelDownLayout = new javax.swing.GroupLayout(
				jPanelDown);
		jPanelDown.setLayout(jPanelDownLayout);
		jPanelDownLayout
				.setHorizontalGroup(jPanelDownLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelDownLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(jButtonOpenOutputFolder)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(jButtonOpenPEXWebsite)
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));
		jPanelDownLayout
				.setVerticalGroup(jPanelDownLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelDownLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanelDownLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jButtonOpenPEXWebsite)
														.addComponent(
																jButtonOpenOutputFolder))
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(
														jPanelDown,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														jPanelTop,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														Short.MAX_VALUE))
								.addContainerGap()));
		layout.setVerticalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap(
										javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(jPanelTop,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addComponent(jPanelDown,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE)));

		pack();
	}// </editor-fold>
		// GEN-END:initComponents

	private void jButtonOpenPEXWebsiteActionPerformed(
			java.awt.event.ActionEvent evt) {
		HttpUtilities.openURL(PEXWebsite);
	}

	private void jButtonOpenOutputFolderActionPerformed(
			java.awt.event.ActionEvent evt) {
		openOutputFolder();
	}

	private void openOutputFolder() {

		Desktop desktop = null;
		if (Desktop.isDesktopSupported()) {
			desktop = Desktop.getDesktop();
		}

		try {
			desktop.open(outputFolder);
		} catch (IOException e) {
		}
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				PEXSubmissionPreparedDialog dialog = new PEXSubmissionPreparedDialog(
						new javax.swing.JDialog(), true, new File("c:\\"));
				dialog.addWindowListener(new java.awt.event.WindowAdapter() {
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
	private javax.swing.JButton jButtonOpenOutputFolder;
	private javax.swing.JButton jButtonOpenPEXWebsite;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JPanel jPanelDown;
	private javax.swing.JPanel jPanelTop;
	// End of variables declaration//GEN-END:variables

}