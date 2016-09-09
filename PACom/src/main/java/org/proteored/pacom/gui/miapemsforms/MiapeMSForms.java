/*
 * MiapeMSForms.java
 * Created on __DATE__, __TIME__
 */

package org.proteored.pacom.gui.miapemsforms;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jfree.ui.RefineryUtilities;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.exceptions.MiapeDatabaseException;
import org.proteored.miapeapi.exceptions.MiapeSecurityException;
import org.proteored.miapeapi.exceptions.WrongXMLFormatException;
import org.proteored.miapeapi.interfaces.ms.MiapeMSDocument;
import org.proteored.miapeapi.xml.ms.MIAPEMSXmlFile;
import org.proteored.miapeapi.xml.ms.MiapeMSXmlFactory;
import org.proteored.pacom.analysis.util.FileManager;
import org.proteored.pacom.gui.MiapeExtractionFrame;
import org.proteored.pacom.gui.tasks.MIAPEMSChecker;
import org.proteored.pacom.gui.tasks.OntologyLoaderTask;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

/**
 * 
 * @author __USER__
 */
public class MiapeMSForms extends javax.swing.JDialog {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger("log4j.logger.org.proteored");

	private MiapeMSDocument miapeMS;
	private final ControlVocabularyManager cvManager;
	private final MIAPEMSChecker miapemsChecker;
	private final MIAPEMSFormManager miapeMSFormManager;
	private int currentSlide = 0;
	public static final int ADD_INFO_SLIDE = 0;
	public static final int CONTACT_SLIDE = 1;
	public static final int SPECTROMETER_SLIDE = 2;
	public static final int ANALYZERS_SLIDE = 4;
	public static final int ION_SOURCES_SLIDE = 3;
	public static final int ACTIVATION_SLIDE = 5;
	public static final int ACQUISITION_SLIDE = 6;
	public static final int DATA_ANALYSIS_SLIDE = 7;

	/**
	 * Creates new form MiapeMSForms
	 * 
	 * @param frame
	 * 
	 * @param miapemsChecker
	 */
	public MiapeMSForms(MiapeExtractionFrame frame, MIAPEMSChecker miapemsChecker,
			MiapeMSDocument miapeMS) {
		super(frame, true);
		try {
			UIManager.setLookAndFeel(new WindowsLookAndFeel());
		} catch (UnsupportedLookAndFeelException ex) {
		}
		initComponents();
		this.setTitle("MIAPE MS Metadata editor");
		this.cvManager = OntologyLoaderTask.getCvManager();
		this.miapeMSFormManager = new MIAPEMSFormManager(this, cvManager);
		this.miapeMS = miapeMS;
		this.miapeMSFormManager.loadMIAPEMS(miapeMS, cvManager);
		setConfigurationName();
		this.miapemsChecker = miapemsChecker;
		initConfigurationsCombox();
		showMIAPEdata(ADD_INFO_SLIDE);
		RefineryUtilities.centerFrameOnScreen(this);
		disableCurrentButton();
	}

	public MiapeMSForms(MiapeExtractionFrame frame, MIAPEMSChecker miapemsChecker,
			MiapeMSDocument miapeMS, ControlVocabularyManager cvManager) {
		super(frame, true);
		try {
			UIManager.setLookAndFeel(new WindowsLookAndFeel());
		} catch (UnsupportedLookAndFeelException ex) {
		}
		initComponents();
		this.setTitle("MIAPE MS Metadata editor");
		this.cvManager = cvManager;
		this.miapeMSFormManager = new MIAPEMSFormManager(this, cvManager);
		this.miapeMS = miapeMS;
		this.miapeMSFormManager.loadMIAPEMS(miapeMS, cvManager);
		setConfigurationName();
		this.miapemsChecker = miapemsChecker;
		initConfigurationsCombox();
		showMIAPEdata(ADD_INFO_SLIDE);
		RefineryUtilities.centerFrameOnScreen(this);
		disableCurrentButton();

	}

	private void setConfigurationName() {
		final String selectedItem = (String) this.jComboBoxConfigurations.getSelectedItem();
		if (selectedItem != null && !"".equals(selectedItem))
			this.jTextFieldConfigurationName.setText(selectedItem);
		else {
			if (this.miapeMS != null && this.miapeMS.getName() != null) {
				String name = this.miapeMS.getName();
				this.jTextFieldConfigurationName.setText(name);
			}
		}

	}

	private void initConfigurationsCombox() {

		final List<String> metadataList = FileManager.getMetadataList(cvManager);
		Collections.sort(metadataList);
		metadataList.add(0, "");
		this.jComboBoxConfigurations.setModel(new DefaultComboBoxModel(metadataList.toArray()));
	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		jPanelTop = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jComboBoxConfigurations = new javax.swing.JComboBox();
		jButtonLoad = new javax.swing.JButton();
		jButtonFinish = new javax.swing.JButton();
		jButtonSave = new javax.swing.JButton();
		jTextFieldConfigurationName = new javax.swing.JTextField();
		jPanel1 = new javax.swing.JPanel();
		jButtonSampleInformation = new javax.swing.JButton();
		jButtonContact = new javax.swing.JButton();
		jButtonSpectrometer = new javax.swing.JButton();
		jButtonIonSources = new javax.swing.JButton();
		jButtonAnalyzer = new javax.swing.JButton();
		jButtonActivationDissociation = new javax.swing.JButton();
		jButtonDataAcquisition = new javax.swing.JButton();
		jButtonDataAnalysis = new javax.swing.JButton();
		jButtonPrevious = new javax.swing.JButton();
		jButtonNext = new javax.swing.JButton();
		jLabel2 = new javax.swing.JLabel();
		jPanelDown = new javax.swing.JPanel();
		jScrollPaneDown = new javax.swing.JScrollPane();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("MIAPE MS Metadata");

		jPanelTop.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		jLabel1.setText("Load configurations:");

		jComboBoxConfigurations.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "" }));

		jButtonLoad.setText("Load");
		jButtonLoad.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonLoadActionPerformed(evt);
			}
		});

		jButtonFinish.setText("Save and close");
		jButtonFinish.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonFinishActionPerformed(evt);
			}
		});

		jButtonSave.setText("Save");
		jButtonSave.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonSaveActionPerformed(evt);
			}
		});

		jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(
				javax.swing.BorderFactory.createEtchedBorder(), "MIAPE MS Section"));

		jButtonSampleInformation.setText("Sample");
		jButtonSampleInformation
				.setToolTipText("Go to Sample Information and Additional Information");
		jButtonSampleInformation.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonSampleInformationActionPerformed(evt);
			}
		});

		jButtonContact.setText("Contact");
		jButtonContact.setToolTipText("Go to Contact");
		jButtonContact.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonContactActionPerformed(evt);
			}
		});

		jButtonSpectrometer.setText("Spectrometer");
		jButtonSpectrometer.setToolTipText("Go to Spectrometer");
		jButtonSpectrometer.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonSpectrometerActionPerformed(evt);
			}
		});

		jButtonIonSources.setText("Ion sources");
		jButtonIonSources.setToolTipText("Go to Ion Sources");
		jButtonIonSources.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonIonSourcesActionPerformed(evt);
			}
		});

		jButtonAnalyzer.setText("Analysers");
		jButtonAnalyzer.setToolTipText("Go to Analysers");
		jButtonAnalyzer.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonAnalyzerActionPerformed(evt);
			}
		});

		jButtonActivationDissociation.setText("Activation / Dissociation");
		jButtonActivationDissociation.setToolTipText("Go to Activation / Dissociation");
		jButtonActivationDissociation.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonActivationDissociationActionPerformed(evt);
			}
		});

		jButtonDataAcquisition.setText("Data Acquisition");
		jButtonDataAcquisition.setToolTipText("Go to Data Acquisition");
		jButtonDataAcquisition.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonDataAcquisitionActionPerformed(evt);
			}
		});

		jButtonDataAnalysis.setText("Data Analysis");
		jButtonDataAnalysis.setToolTipText("Go to Data Analysis");
		jButtonDataAnalysis.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonDataAnalysisActionPerformed(evt);
			}
		});

		jButtonPrevious.setText("<<");
		jButtonPrevious.setToolTipText("Go to previous MIAPE section");
		jButtonPrevious.setEnabled(false);
		jButtonPrevious.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonPreviousActionPerformed(evt);
			}
		});

		jButtonNext.setText(">>");
		jButtonNext.setToolTipText("Go to next MIAPE section");
		jButtonNext.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonNextActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout
				.setHorizontalGroup(jPanel1Layout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel1Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanel1Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																jPanel1Layout
																		.createSequentialGroup()
																		.addComponent(
																				jButtonActivationDissociation)
																		.addGap(18, 18, 18)
																		.addComponent(
																				jButtonDataAcquisition)
																		.addGap(18, 18, 18)
																		.addComponent(
																				jButtonDataAnalysis))
														.addGroup(
																jPanel1Layout
																		.createSequentialGroup()
																		.addComponent(
																				jButtonSampleInformation,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				81,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addGap(18, 18, 18)
																		.addComponent(
																				jButtonContact,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				75,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addGap(18, 18, 18)
																		.addComponent(
																				jButtonSpectrometer,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				103,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addGap(18, 18, 18)
																		.addComponent(
																				jButtonIonSources)
																		.addGap(18, 18, 18)
																		.addComponent(
																				jButtonAnalyzer,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				83,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addGap(18, 18, 18)
																		.addComponent(
																				jButtonPrevious)
																		.addGap(18, 18, 18)
																		.addComponent(jButtonNext)))
										.addContainerGap()));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				jPanel1Layout
						.createSequentialGroup()
						.addGroup(
								jPanel1Layout
										.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(jButtonSampleInformation)
										.addComponent(jButtonContact)
										.addComponent(jButtonSpectrometer)
										.addComponent(jButtonIonSources)
										.addComponent(jButtonAnalyzer)
										.addComponent(jButtonPrevious).addComponent(jButtonNext))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(
								jPanel1Layout
										.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(jButtonActivationDissociation)
										.addComponent(jButtonDataAcquisition)
										.addComponent(jButtonDataAnalysis)).addContainerGap()));

		jLabel2.setText("Configuration name:");

		javax.swing.GroupLayout jPanelTopLayout = new javax.swing.GroupLayout(jPanelTop);
		jPanelTop.setLayout(jPanelTopLayout);
		jPanelTopLayout
				.setHorizontalGroup(jPanelTopLayout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanelTopLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanelTopLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																jPanelTopLayout
																		.createSequentialGroup()
																		.addGroup(
																				jPanelTopLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addGroup(
																								jPanelTopLayout
																										.createSequentialGroup()
																										.addComponent(
																												jLabel1)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												jComboBoxConfigurations,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												235,
																												javax.swing.GroupLayout.PREFERRED_SIZE)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												jButtonLoad))
																						.addComponent(
																								jPanel1,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addContainerGap(
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				Short.MAX_VALUE))
														.addGroup(
																jPanelTopLayout
																		.createSequentialGroup()
																		.addComponent(jLabel2)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jTextFieldConfigurationName,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				300,
																				Short.MAX_VALUE)
																		.addGap(18, 18, 18)
																		.addComponent(jButtonSave)
																		.addGap(18, 18, 18)
																		.addComponent(jButtonFinish)
																		.addGap(72, 72, 72)))));
		jPanelTopLayout.setVerticalGroup(jPanelTopLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				jPanelTopLayout
						.createSequentialGroup()
						.addContainerGap()
						.addGroup(
								jPanelTopLayout
										.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(jLabel1)
										.addComponent(jComboBoxConfigurations,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jButtonLoad))
						.addGap(10, 10, 10)
						.addGroup(
								jPanelTopLayout
										.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(jLabel2)
										.addComponent(jTextFieldConfigurationName,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jButtonSave).addComponent(jButtonFinish))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap()));

		jPanelDown.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		jScrollPaneDown.setBorder(null);
		jScrollPaneDown
				.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		javax.swing.GroupLayout jPanelDownLayout = new javax.swing.GroupLayout(jPanelDown);
		jPanelDown.setLayout(jPanelDownLayout);
		jPanelDownLayout.setHorizontalGroup(jPanelDownLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				jPanelDownLayout
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(jScrollPaneDown, javax.swing.GroupLayout.DEFAULT_SIZE, 677,
								Short.MAX_VALUE).addContainerGap()));
		jPanelDownLayout.setVerticalGroup(jPanelDownLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				jPanelDownLayout
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(jScrollPaneDown, javax.swing.GroupLayout.DEFAULT_SIZE, 489,
								Short.MAX_VALUE).addContainerGap()));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(
								layout.createParallelGroup(
										javax.swing.GroupLayout.Alignment.TRAILING, false)
										.addComponent(jPanelDown,
												javax.swing.GroupLayout.Alignment.LEADING,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addComponent(jPanelTop,
												javax.swing.GroupLayout.Alignment.LEADING,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(jPanelTop, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13,
								Short.MAX_VALUE)
						.addComponent(jPanelDown, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap()));

		pack();
	}// </editor-fold>
		// GEN-END:initComponents

	private void jButtonContactActionPerformed(java.awt.event.ActionEvent evt) {
		setButtonsEnabled(true);
		this.jButtonContact.setEnabled(false);
		this.currentSlide = CONTACT_SLIDE;
		showMIAPEdata(CONTACT_SLIDE);

	}

	private void setButtonsEnabled(boolean b) {
		this.jButtonActivationDissociation.setEnabled(b);
		this.jButtonAnalyzer.setEnabled(b);
		this.jButtonDataAcquisition.setEnabled(b);
		this.jButtonDataAnalysis.setEnabled(b);
		this.jButtonIonSources.setEnabled(b);
		this.jButtonSampleInformation.setEnabled(b);
		this.jButtonSpectrometer.setEnabled(b);
		this.jButtonContact.setEnabled(b);
	}

	private void jButtonDataAcquisitionActionPerformed(java.awt.event.ActionEvent evt) {
		setButtonsEnabled(true);
		this.jButtonDataAcquisition.setEnabled(false);
		this.currentSlide = ACQUISITION_SLIDE;
		showMIAPEdata(ACQUISITION_SLIDE);

	}

	private void jButtonActivationDissociationActionPerformed(java.awt.event.ActionEvent evt) {
		setButtonsEnabled(true);
		this.jButtonActivationDissociation.setEnabled(false);
		this.currentSlide = ACTIVATION_SLIDE;
		showMIAPEdata(ACTIVATION_SLIDE);
	}

	private void jButtonAnalyzerActionPerformed(java.awt.event.ActionEvent evt) {
		setButtonsEnabled(true);
		this.jButtonAnalyzer.setEnabled(false);
		this.currentSlide = ANALYZERS_SLIDE;
		showMIAPEdata(ANALYZERS_SLIDE);
	}

	private void jButtonIonSourcesActionPerformed(java.awt.event.ActionEvent evt) {
		setButtonsEnabled(true);
		this.jButtonIonSources.setEnabled(false);
		this.currentSlide = ION_SOURCES_SLIDE;
		showMIAPEdata(ION_SOURCES_SLIDE);
	}

	private void jButtonSpectrometerActionPerformed(java.awt.event.ActionEvent evt) {
		setButtonsEnabled(true);
		this.jButtonSpectrometer.setEnabled(false);
		this.currentSlide = SPECTROMETER_SLIDE;
		showMIAPEdata(SPECTROMETER_SLIDE);
	}

	private void jButtonSampleInformationActionPerformed(java.awt.event.ActionEvent evt) {
		setButtonsEnabled(true);
		this.jButtonSampleInformation.setEnabled(false);
		this.currentSlide = ADD_INFO_SLIDE;
		showMIAPEdata(ADD_INFO_SLIDE);
	}

	private void jButtonDataAnalysisActionPerformed(java.awt.event.ActionEvent evt) {
		setButtonsEnabled(true);
		this.jButtonDataAnalysis.setEnabled(false);
		this.currentSlide = DATA_ANALYSIS_SLIDE;
		showMIAPEdata(DATA_ANALYSIS_SLIDE);
	}

	private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {
		final String configurationName = this.jTextFieldConfigurationName.getText();
		if (!"".equals(configurationName)) {
			try {
				final MiapeMSDocument miapemsFromForms = this.miapeMSFormManager
						.getMIAPEMSFromForms(null);

				final String metadataFilePath = FileManager.getMetadataFolder() + configurationName
						+ ".xml";
				miapemsFromForms.toXml().saveAs(metadataFilePath);
				JOptionPane.showMessageDialog(this, "MIAPE MS file saved at: " + metadataFilePath);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane
						.showMessageDialog(this, "Error saving MIAPE MS file: " + e.getMessage());
			}
		} else {
			JOptionPane.showMessageDialog(this, "Enter a name for the MIAPE MS data");
		}
	}

	private void jButtonFinishActionPerformed(java.awt.event.ActionEvent evt) {
		String configName = this.jTextFieldConfigurationName.getText();
		if ("".equals(configName)) {
			int option = JOptionPane
					.showConfirmDialog(
							this,
							"<html>Current configuration is not saved.<br>Do you want to save it?<br>If YES, put a name on the text field and click 'Save' or 'Finish' again.</html>",
							"Save before continue?", JOptionPane.YES_NO_OPTION);
			if (option == JOptionPane.YES_OPTION)
				return;
		} else {
			// int option = JOptionPane.showConfirmDialog(this,
			// "Do you want to save current configuration as '" + configName +
			// "'?",
			// "Save before continue?", JOptionPane.YES_NO_OPTION);
			// if (option == JOptionPane.YES_OPTION)
			this.jButtonSaveActionPerformed(null);
		}

		if (this.miapemsChecker != null)
			this.miapemsChecker.finished(this.jTextFieldConfigurationName.getText());

		this.dispose();
	}

	private void jButtonPreviousActionPerformed(java.awt.event.ActionEvent evt) {
		this.currentSlide--;
		if (currentSlide == 0) {
			this.jButtonPrevious.setEnabled(false);
		}
		this.jButtonNext.setEnabled(true);
		disableCurrentButton();
		showMIAPEdata(currentSlide);
		RefineryUtilities.centerFrameOnScreen(this);
	}

	private void jButtonNextActionPerformed(java.awt.event.ActionEvent evt) {
		this.currentSlide++;
		if (currentSlide == DATA_ANALYSIS_SLIDE) {
			this.jButtonNext.setEnabled(false);
		}
		this.jButtonPrevious.setEnabled(true);
		disableCurrentButton();
		showMIAPEdata(currentSlide);
		RefineryUtilities.centerFrameOnScreen(this);
	}

	private void disableCurrentButton() {
		this.setButtonsEnabled(true);
		if (currentSlide == ADD_INFO_SLIDE)
			this.jButtonSampleInformation.setEnabled(false);
		else if (currentSlide == CONTACT_SLIDE)
			this.jButtonContact.setEnabled(false);
		else if (currentSlide == SPECTROMETER_SLIDE)
			this.jButtonSpectrometer.setEnabled(false);
		else if (currentSlide == ANALYZERS_SLIDE)
			this.jButtonAnalyzer.setEnabled(false);
		else if (currentSlide == ION_SOURCES_SLIDE)
			this.jButtonIonSources.setEnabled(false);
		else if (currentSlide == ACTIVATION_SLIDE)
			this.jButtonActivationDissociation.setEnabled(false);
		else if (currentSlide == ACQUISITION_SLIDE)
			this.jButtonDataAcquisition.setEnabled(false);
		else if (currentSlide == DATA_ANALYSIS_SLIDE)
			this.jButtonDataAnalysis.setEnabled(false);
	}

	private void jButtonLoadActionPerformed(java.awt.event.ActionEvent evt) {
		setConfigurationName();
		// this.jButtonPrevious.setEnabled(false);
		// this.jButtonNext.setEnabled(true);
		// this.currentSlide = 0;
		loadConfiguration();

	}

	private void loadConfiguration() {
		final String miapeFileName = (String) this.jComboBoxConfigurations.getSelectedItem();
		final File metadataFile = FileManager.getMetadataFile(miapeFileName);
		boolean loadMetadata = true;
		if (metadataFile != null && this.miapeMS != null) {
			final int selectedOption = JOptionPane
					.showConfirmDialog(
							this,
							"<html>If you continue, the information of MIAPE MS metadata will be overriten.<br>Do you want to continue?</html>",
							"Warining", JOptionPane.YES_NO_OPTION);
			if (selectedOption == JOptionPane.NO_OPTION) {
				loadMetadata = false;
			}
		}
		if (loadMetadata) {
			String errorMessage = "";
			try {
				MiapeMSDocument newMiapeMS = MiapeMSXmlFactory.getFactory().toDocument(
						new MIAPEMSXmlFile(metadataFile), cvManager, null, null, null);
				this.miapeMS = newMiapeMS;
				this.miapeMSFormManager.loadMIAPEMS(newMiapeMS, cvManager);
				showMIAPEdata(this.currentSlide);
			} catch (MiapeDatabaseException e) {
				errorMessage = e.getMessage();
				e.printStackTrace();
			} catch (MiapeSecurityException e) {
				errorMessage = e.getMessage();
				e.printStackTrace();
			} catch (WrongXMLFormatException e) {
				errorMessage = e.getMessage();
				e.printStackTrace();
			}
			if (!"".equals(errorMessage))
				JOptionPane.showMessageDialog(this, "<html>Error loading MIAPE MS data:<br>"
						+ errorMessage + "</html>");
		}

	}

	public void showMIAPEdata(int slideNumber) {

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		// panel.setBorder(new TitledBorder("asdf"));
		if (slideNumber == 0) {
			this.jButtonPrevious.setEnabled(false);
		} else {
			this.jButtonPrevious.setEnabled(true);
		}
		if (slideNumber == DATA_ANALYSIS_SLIDE) {
			this.jButtonNext.setEnabled(false);
		} else {
			this.jButtonNext.setEnabled(true);
		}
		if (slideNumber == CONTACT_SLIDE) {
			JPanel panel2 = new JPanel(new GridBagLayout());
			GridBagConstraints c = MIAPEMSFormManager.getGridBagContraints();
			// panel2.add(this.miapeMSFormManager.getMiapeNamePanel(), c);
			// c.gridy++;
			panel2.add(this.miapeMSFormManager.getContactPanel(), c);

			panel.add(panel2);
			this.jScrollPaneDown.setViewportView(panel);
		} else if (slideNumber == ADD_INFO_SLIDE) {

			JPanel panel2 = new JPanel(new GridBagLayout());
			GridBagConstraints c = MIAPEMSFormManager.getGridBagContraints();
			// panel2.add(this.miapeMSFormManager.getMiapeNamePanel(), c);
			// c.gridy++;
			panel2.add(this.miapeMSFormManager.getAddInfoPanel(), c);

			panel.add(panel2);
			this.jScrollPaneDown.setViewportView(panel);

		} else if (slideNumber == SPECTROMETER_SLIDE) {

			JPanel panel2 = new JPanel(new GridBagLayout());
			GridBagConstraints c = MIAPEMSFormManager.getGridBagContraints();
			// panel2.add(this.miapeMSFormManager.getMiapeNamePanel(), c);
			// c.gridy++;
			panel2.add(this.miapeMSFormManager.getSpectrometerPanel(), c);

			panel.add(panel2);
			this.jScrollPaneDown.setViewportView(panel);

		} else if (slideNumber == ANALYZERS_SLIDE || slideNumber == ION_SOURCES_SLIDE
				|| slideNumber == ACTIVATION_SLIDE) {
			JPanel panel2 = new JPanel(new GridBagLayout());
			GridBagConstraints c = MIAPEMSFormManager.getGridBagContraints();
			// panel2.add(this.miapeMSFormManager.getMiapeNamePanel(), c);
			// c.gridy++;
			panel2.add(this.miapeMSFormManager.getInstrumentConfigurationPanel(slideNumber), c);
			panel.add(panel2);

			this.jScrollPaneDown.setViewportView(panel);

		} else if (slideNumber == ACQUISITION_SLIDE || slideNumber == DATA_ANALYSIS_SLIDE) {
			JPanel panel2 = new JPanel(new GridBagLayout());
			GridBagConstraints c = MIAPEMSFormManager.getGridBagContraints();
			// panel2.add(this.miapeMSFormManager.getMiapeNamePanel(), c);
			// c.gridy++;

			panel2.add(this.miapeMSFormManager.getSpectrumGenerationPanel(slideNumber), c);
			panel.add(panel2);

			this.jScrollPaneDown.setViewportView(panel);

		}

	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new MiapeMSForms(null, null, null, OntologyLoaderTask.getTestCvManager())
						.setVisible(true);
			}
		});
	}

	// GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.JButton jButtonActivationDissociation;
	private javax.swing.JButton jButtonAnalyzer;
	private javax.swing.JButton jButtonContact;
	private javax.swing.JButton jButtonDataAcquisition;
	private javax.swing.JButton jButtonDataAnalysis;
	private javax.swing.JButton jButtonFinish;
	private javax.swing.JButton jButtonIonSources;
	private javax.swing.JButton jButtonLoad;
	private javax.swing.JButton jButtonNext;
	private javax.swing.JButton jButtonPrevious;
	private javax.swing.JButton jButtonSampleInformation;
	private javax.swing.JButton jButtonSave;
	private javax.swing.JButton jButtonSpectrometer;
	private javax.swing.JComboBox jComboBoxConfigurations;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanelDown;
	private javax.swing.JPanel jPanelTop;
	private javax.swing.JScrollPane jScrollPaneDown;
	private javax.swing.JTextField jTextFieldConfigurationName;

	// End of variables declaration//GEN-END:variables

	public MiapeMSDocument getMiapeMSMetadata(String projectName) {
		if (this.miapeMSFormManager != null)
			return this.miapeMSFormManager.getMIAPEMSFromForms(projectName);
		return null;
	}

}