package org.proteored.pacom.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingWorker.StateValue;

import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.exceptions.MiapeDatabaseException;
import org.proteored.miapeapi.exceptions.MiapeSecurityException;
import org.proteored.miapeapi.factories.MiapeDocumentFactory;
import org.proteored.miapeapi.factories.ms.MiapeMSDocumentFactory;
import org.proteored.miapeapi.interfaces.MiapeDate;
import org.proteored.miapeapi.interfaces.ms.MiapeMSDocument;
import org.proteored.miapeapi.xml.ms.MIAPEMSXmlFile;
import org.proteored.miapeapi.xml.ms.MiapeMSDocumentImpl;
import org.proteored.miapeapi.xml.ms.MiapeMSXmlFactory;
import org.proteored.miapeapi.xml.ms.merge.MiapeMSMerger;
import org.proteored.pacom.analysis.util.FileManager;
import org.proteored.pacom.gui.importjobs.InputFileType;
import org.proteored.pacom.gui.miapemsforms.MetadataLoader;
import org.proteored.pacom.gui.tasks.MIAPEMSChecker;
import org.proteored.pacom.utils.ComponentEnableStateKeeper;

public class MiapeMSFormsDialog extends JDialog implements PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2703930602334693462L;
	private final MiapeExtractionFrame parentFrame;
	private javax.swing.JLabel jLabelMiapeMSMetadata;
	private javax.swing.JComboBox<String> jComboBoxMetadata;
	private javax.swing.JButton jButtonEditMetadata;
	private MIAPEMSChecker miapeMSChecker;
	private static MiapeMSFormsDialog instance;
	private final ComponentEnableStateKeeper enableStateKeeper = new ComponentEnableStateKeeper();
	private JProgressBar jProgressBar = new JProgressBar();
	private final ControlVocabularyManager cvManager;

	private MiapeMSFormsDialog(MiapeExtractionFrame parentFrame, ControlVocabularyManager cvManager) {
		super(parentFrame, true);
		setTitle("Mass Spectrometry metadata (for PRIDE export)");
		this.parentFrame = parentFrame;
		this.cvManager = cvManager;
		initComponents();
	}

	public static MiapeMSFormsDialog getInstance(MiapeExtractionFrame parentFrame, ControlVocabularyManager cvManager) {
		if (instance == null) {
			instance = new MiapeMSFormsDialog(parentFrame, cvManager);
		}
		return instance;
	}

	private void initComponents() {
		BorderLayout borderLayout = new BorderLayout();
		getContentPane().setLayout(borderLayout);
		javax.swing.JScrollPane jScrollPanel = new javax.swing.JScrollPane();
		jScrollPanel.setPreferredSize(new Dimension(400, 350));
		getContentPane().add(jScrollPanel, BorderLayout.CENTER);
		jScrollPanel.setBorder(null);
		jScrollPanel.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		jComboBoxMetadata = new javax.swing.JComboBox<String>();

		jComboBoxMetadata
				.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Loading metadata templates..." }));
		jComboBoxMetadata.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				jComboBoxMetadataItemStateChanged(evt);
			}
		});
		jButtonEditMetadata = new javax.swing.JButton();
		jButtonEditMetadata.setIcon(ImageManager.getImageIcon(ImageManager.FINISH));
		jButtonEditMetadata.setPressedIcon(ImageManager.getImageIcon(ImageManager.FINISH_CLICKED));

		jButtonEditMetadata.setIcon(new javax.swing.ImageIcon(
				"C:\\Users\\Salva\\workspace\\miape-extractor\\src\\main\\resources\\finish.png")); // NOI18N
		jButtonEditMetadata.setText("Edit");
		jButtonEditMetadata.setToolTipText("Edit Mass Spectrometry metadata");
		jButtonEditMetadata.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonEditMetadataActionPerformed(evt);
			}
		});

		jLabelMiapeMSMetadata = new javax.swing.JLabel();
		jScrollPanel.setViewportView(jLabelMiapeMSMetadata);
		jLabelMiapeMSMetadata.setVerticalAlignment(javax.swing.SwingConstants.TOP);
		jLabelMiapeMSMetadata.setAutoscrolls(true);

		javax.swing.JPanel jPanel5North = new javax.swing.JPanel();
		getContentPane().add(jPanel5North, BorderLayout.NORTH);

		jPanel5North.setToolTipText(
				"<html>Here you can define some required metadata to<br>\r\n complement information from MGF, mzML or PRIDE XML files.</html>");
		javax.swing.GroupLayout gl_jPanel5North = new javax.swing.GroupLayout(jPanel5North);
		gl_jPanel5North.setHorizontalGroup(gl_jPanel5North.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_jPanel5North.createSequentialGroup().addContainerGap()
						.addComponent(jComboBoxMetadata, GroupLayout.PREFERRED_SIZE, 263, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(jButtonEditMetadata, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(76, Short.MAX_VALUE)));
		gl_jPanel5North.setVerticalGroup(gl_jPanel5North.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_jPanel5North.createSequentialGroup().addContainerGap()
						.addGroup(gl_jPanel5North.createParallelGroup(Alignment.BASELINE)
								.addComponent(jComboBoxMetadata, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jButtonEditMetadata))
						.addContainerGap(316, Short.MAX_VALUE)));
		jPanel5North.setLayout(gl_jPanel5North);

		pack();

	}

	@Override
	public void setVisible(boolean b) {
		if (b) {
			pack();
			java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
			java.awt.Dimension dialogSize = getSize();
			setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);
		}
		super.setVisible(b);
	}

	@Override
	public void dispose() {

		if (miapeMSChecker != null) {
			miapeMSChecker.cancel(true);
		}
	}

	private void jButtonEditMetadataActionPerformed(java.awt.event.ActionEvent evt) {
		if (miapeMSChecker == null || !miapeMSChecker.getState().equals(StateValue.STARTED)) {
			miapeMSChecker = new MIAPEMSChecker(null, null, this.parentFrame, this, false);
			miapeMSChecker.addPropertyChangeListener(this.parentFrame);
			miapeMSChecker.execute();

		} else {
			this.parentFrame.appendStatus("MIAPE MS metadata is currently being checked. Try again later");
		}
	}

	private void jComboBoxMetadataItemStateChanged(java.awt.event.ItemEvent evt) {
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			String metadataFileName = (String) jComboBoxMetadata.getSelectedItem();
			MetadataLoader metadataLoader = new MetadataLoader(metadataFileName);
			metadataLoader.addPropertyChangeListener(this);
			metadataLoader.execute();

			// if (metadataFileName != null && !"".equals(metadataFileName)) {
			// jCheckBoxMS.setSelected(true);
			// } else {
			// jCheckBoxMS.setSelected(false);
			// }
		}
	}

	public void initMetadataCombo(String selectedConfigurationName, ControlVocabularyManager cvManager) {
		if (parentFrame.isGeneratingMS()) {
			jButtonEditMetadata.setEnabled(true);
			jComboBoxMetadata.setEnabled(true);
			jLabelMiapeMSMetadata.setEnabled(true);
		}

		final List<String> metadataList = FileManager.getMetadataTemplateList(cvManager);
		// sort by name
		Collections.sort(metadataList);
		if (metadataList != null) {
			metadataList.add(0, "");
			jComboBoxMetadata.setModel(new DefaultComboBoxModel(metadataList.toArray()));
			if (selectedConfigurationName != null)
				jComboBoxMetadata.setSelectedItem(selectedConfigurationName);
			else
				jLabelMiapeMSMetadata.setText("");
		}
		if (!metadataList.isEmpty() && !"".equals(metadataList.get(0))) {
			this.parentFrame.appendStatus("Metadata templates loaded.");
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (MetadataLoader.METADATA_READED.equals(evt.getPropertyName())) {
			String string = (String) evt.getNewValue();
			jLabelMiapeMSMetadata.setText(string);
		} else if (MIAPEMSChecker.MIAPE_MS_CHECKING_STARTED.equals(evt.getPropertyName())) {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			enableStateKeeper.keepEnableStates(this);
			enableStateKeeper.disable(this);
		} else if (MIAPEMSChecker.MIAPE_MS_FORMS_OPENING.equals(evt.getPropertyName())) {
			this.parentFrame.appendStatus("Opening MS metadata editor...");
		} else if (MIAPEMSChecker.MIAPE_MS_CHECKING_IN_PROGRESS.equals(evt.getPropertyName())) {
			this.parentFrame.appendStatus("Extracting MS metadata from file...");
			jProgressBar.setIndeterminate(true);
		} else if (MIAPEMSChecker.MIAPE_MS_CHECKING_ERROR.equals(evt.getPropertyName())) {
			enableStateKeeper.setToPreviousState(this);
			String error = evt.getNewValue().toString();
			this.parentFrame.appendStatus(error);
			jProgressBar.setIndeterminate(false);
		} else if (MIAPEMSChecker.MIAPE_MS_CHECKING_DONE.equals(evt.getPropertyName())) {
			enableStateKeeper.setToPreviousState(this);
			this.parentFrame.appendStatus("MS metadata edition completed. Click again on Import data.");
			jProgressBar.setIndeterminate(false);
			setCursor(null);
		} else if (MIAPEMSChecker.MIAPE_MS_METADATA_EXTRACTION_DONE.equals(evt.getPropertyName())) {
			this.parentFrame.appendStatus("Metadata loaded.");
			initMetadataCombo(null, getControlVocabularyManager());
			jProgressBar.setIndeterminate(false);
		}
	}

	public void startMIAPEMSChecker(File attachedMSFile, InputFileType inputFileType) {
		// open MIAPEMSForms
		miapeMSChecker = new MIAPEMSChecker(attachedMSFile, inputFileType, this.parentFrame, this, true);
		miapeMSChecker.addPropertyChangeListener(this);
		miapeMSChecker.execute();
	}

	public MiapeMSDocument getMiapeMSFromMetadata() {

		final String metadataName = (String) jComboBoxMetadata.getSelectedItem();
		if ("".equals(metadataName))
			return null;
		final File metadataFile = FileManager.getMetadataFile(metadataName);
		if (metadataFile == null)
			return null;
		MIAPEMSXmlFile xmlFile = new MIAPEMSXmlFile(metadataFile);

		try {
			MiapeMSDocument metadataMiapeMS = MiapeMSXmlFactory.getFactory().toDocument(xmlFile,
					getControlVocabularyManager(), null, null, null);
			String miapeName = "MS dataset from '" + metadataMiapeMS.getName() + "' metadata template";

			((MiapeMSDocumentImpl) metadataMiapeMS).setName(miapeName);

			String miapeMSName = metadataMiapeMS.getName();
			// merge with a MIAPE with just a project

			MiapeDate today = new MiapeDate(new Date());
			MiapeMSDocument miapeMSJustWithProject = (MiapeMSDocument) MiapeMSDocumentFactory
					.createMiapeMSDocumentBuilder(
							MiapeDocumentFactory.createProjectBuilder(this.parentFrame.getProjectName())
									.date(new MiapeDate(new Date())).build(),
							miapeMSName, null)
					.date(today).modificationDate(new Date()).build();
			MiapeMSDocument ret = MiapeMSMerger.getInstance(getControlVocabularyManager()).merge(metadataMiapeMS,
					miapeMSJustWithProject);

			return ret;
		} catch (MiapeDatabaseException e) {
			e.printStackTrace();
		} catch (MiapeSecurityException e) {
			e.printStackTrace();
		}

		return null;
	}

	private ControlVocabularyManager getControlVocabularyManager() {
		return this.cvManager;
	}

	public void enableMetadataJComboBox(boolean b) {
		this.jComboBoxMetadata.setEnabled(b);
	}

	public void enableJButtonEditMetadata(boolean b) {
		this.jButtonEditMetadata.setEnabled(b);
	}

	public String getSelectedMetadata() {
		return (String) this.jComboBoxMetadata.getSelectedItem();
	}

}
