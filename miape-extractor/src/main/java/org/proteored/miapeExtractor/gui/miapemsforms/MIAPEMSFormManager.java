package org.proteored.miapeExtractor.gui.miapemsforms;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.proteored.miapeExtractor.gui.miapemsforms.util.CVRetriever;
import org.proteored.miapeapi.cv.CellTypes;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.cv.ControlVocabularySet;
import org.proteored.miapeapi.cv.HumanDisseases;
import org.proteored.miapeapi.cv.MainTaxonomies;
import org.proteored.miapeapi.cv.SampleInformation;
import org.proteored.miapeapi.cv.SampleProcessingStep;
import org.proteored.miapeapi.cv.TissuesTypes;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.factories.EquipmentBuilder;
import org.proteored.miapeapi.factories.MSContactBuilder;
import org.proteored.miapeapi.factories.MiapeDocumentFactory;
import org.proteored.miapeapi.factories.ms.AcquisitionBuilder;
import org.proteored.miapeapi.factories.ms.ActivationDissociationBuilder;
import org.proteored.miapeapi.factories.ms.AdditionalInformationBuilder;
import org.proteored.miapeapi.factories.ms.AnalyserBuilder;
import org.proteored.miapeapi.factories.ms.DataAnalysisBuilder;
import org.proteored.miapeapi.factories.ms.EsiBuilder;
import org.proteored.miapeapi.factories.ms.InstrumentConfigurationBuilder;
import org.proteored.miapeapi.factories.ms.MaldiBuilder;
import org.proteored.miapeapi.factories.ms.MiapeMSDocumentBuilder;
import org.proteored.miapeapi.factories.ms.MiapeMSDocumentFactory;
import org.proteored.miapeapi.factories.ms.Other_IonSourceBuilder;
import org.proteored.miapeapi.factories.ms.SpectrometerBuilder;
import org.proteored.miapeapi.interfaces.Equipment;
import org.proteored.miapeapi.interfaces.ms.Acquisition;
import org.proteored.miapeapi.interfaces.ms.ActivationDissociation;
import org.proteored.miapeapi.interfaces.ms.Analyser;
import org.proteored.miapeapi.interfaces.ms.DataAnalysis;
import org.proteored.miapeapi.interfaces.ms.Esi;
import org.proteored.miapeapi.interfaces.ms.InstrumentConfiguration;
import org.proteored.miapeapi.interfaces.ms.MSAdditionalInformation;
import org.proteored.miapeapi.interfaces.ms.MSContact;
import org.proteored.miapeapi.interfaces.ms.Maldi;
import org.proteored.miapeapi.interfaces.ms.MiapeMSDocument;
import org.proteored.miapeapi.interfaces.ms.Other_IonSource;
import org.proteored.miapeapi.interfaces.ms.Spectrometer;
import org.proteored.miapeapi.spring.SpringHandler;

public class MIAPEMSFormManager {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger("log4j.logger.org.proteored");

	private static final String REFLECTRON_TOOLTIP = "<html>Just in case of MALDI sources:<br>Time-of-Flight drift tube: reflectron status.</html>";
	protected static final int ANALYZERS_INDEX = 0;
	protected static final int ESI_INDEX = 1;
	protected static final int MALDI_INDEX = 2;
	protected static final int OTHER_ION_SOURCE_INDEX = 3;
	protected static final int ACTIVATION_INDEX = 4;
	private static final String ESIPARAMETERS_TOOLTIP = "<html>Other parameters discriminant for the experiment.<br>Where appropiate, and if considered as <br>discriminating elements of the source <br>parameters, describe these values.</html>";
	private static final String INTERFACE_DESCRIPTION_TOOLTIP = "<html>List any modification made to the standard specification.<br>If the interface is enterily custom-build, describe it or provide a reference if available.</html>";
	private static final String SPRAYER_DESCRIPTION_TOOLTIP = "<html>List any modification made to the standard specification.<br>If the sprayer is enterily custom-build, describe it or provide a reference if available.</html>";
	private static final String PLATE_TYPE_TOOLTIP = "<html>The material of which the target plate is made (usually stainless steel, or coated glass);<br> if the plate has a special construction then that should be briefly described and catalogue and lot numbers given where available.</html>";
	private static final String MATRIX_TOOLTIP = "<html>The material in which the sample is embedded on the target<br> (e.g. alpha-cyano-4-hydroxycinnamic acid).</html>";
	private static final String MALDI_DISS_TOOLTIP = "<html>Confirm whether post-source decay, <br>laser-induced decomposition, or in-source dissociation was performed.</html>";
	private static final String MALDI_SUMMARY_TOOLTIP = "<html>If so provide a brief description of the process (for example, summarise the stepwise reduction of reflector voltage).</html>";
	private static final String EXTRACTION_TOOLTIP = "<html>State whether a delay between laser shot and ion acceleration is employed.</html>";
	private static final String LASER_WAVELENGTH_TOOLTIP = "<html>The wavelength of the generated pulse (in nanometers).</html>";
	private static final String LASER_PARAMETERS_TOOLTIP = "<html>Other details of the laser used to shoot at the matrix-embedded sample<br>"
			+ "if considered as important for the interpretation of data; <br>"
			+ "this might include the pulse energy, focus diameter, attenuation details,<br>"
			+ "pulse duration at full-width half maximum, frequency of shots in Hertz <br>"
			+ "and average number of shots fired to generate each combined mass spectrum.</html>";
	public List<JComponent> spectrometer = new ArrayList<JComponent>();
	public List acquisitions = new ArrayList();
	public List dataAnalysises = new ArrayList();
	private static final String MALDI = "MALDI";
	private static final String OTHER = "OTHER";
	private static final String ESI = "ESI";

	private static final String OTHER_ION_SOURCE_TOOLTIP = "<html>Describe the ion source and provide<br>"
			+ "relevant and discriminating parameters for its use.</html>";

	private static final String ACTIVATION_NAME_TOOLTIP = "<html>The hardware element where the activation and/or dissociation<br>"
			+ "occurs. For instance a quadrupole collision cell, a 3D ion trap,<br>"
			+ "the ion source (for ISD, PSD, LID, isCID).</html>";

	private static final String GAS_TYPE_TOOLTIP = "<html>Gas type (when used):<br>"
			+ "The composition of the gas used to fragment ions,<br>"
			+ "for instance in the collision cell.</html>";

	private static final String GAS_PRESSURE_TOOLTIP = "<html>Gas type (when used):<br>"
			+ "The pressure of the gas used to fragment ions,<br>"
			+ "for instance in the collision cell.</html>";

	private static final String ACTIVATION_TYPE_TOOLTIP = "<html>The type of activation and/or dissociation used in the fragmentation process.<br>"
			+ "Examples might include Collision Induced Dissociation (CID) with static or spread collision energy,<br>"
			+ "Electron Transfer Dissociation (ETD) with provided activator molecules<html>";

	private static final String ACQUISITION_SOFTWARE_NAME_TOOLTIP = "<html>The instrument management and data analysis package name<br>"
			+ "where there are several piecces of software involved, give name,<br>"
			+ "version and role for each of them. Mention also upgrades not reflected"
			+ "in the version number</html>";

	private static final String ACQUISITION_PARAMETERS_TOOLTIP = "<html>The information on how the MS data have been generated.<br>"
			+ "It describes the instrument's parameter settings / acquisition method<br>"
			+ "file or information describing the acquisition conditions and settings of the MS run.<br>"
			+ "Ideally this should be a URI+filename, for example an export of the acquisition method.<br>"
			+ "An explicit text description of the acquisition process is also desirable.<br>"
			+ "This includes the acquisition sequence (for instance as simplified as top-five"
			+ "<br>method with a cycle made of one full MS1 scan in the Orbitrap, followed<br>"
			+ "by a precursor selection of 5 most intense ions applying an exclusion window<br>"
			+ "of 30 seconds and followed by the acquisition of 5 product ion scans generated<br>"
			+ "in the LTQ analyser, and detected in the LTQ). This allows to differentiate between<br>"
			+ "the use of a selected precursor window vs unselected fragmentation (MS`E/bbCID/AIF).<br>"
			+ "This also allows to explicitely describe pre-defined the acquisition method of<br>"
			+ "a SRM experiment where all transitions and detection windows are specified.</html>";

	private static final String ACQUISITION_PARAMETERS_FILE_TOOLTIP = "<html>The URL to the acquisition parameters file</html>";

	private static final String TARGET_LIST_TOOLTIP = "<html>For targeted approaches:<br>Target list (or 'inclusion list') configured prior to the run</html>";

	private static final String TARGET_FILE_TOOLTIP = "<html>Location of transition list file</html>";

	private static final String DATAANALYSIS_SOFTWARE_NAME_TOOLTIP = "<html>The MS data analysis package name.</html>";

	private static final String DATAANALYSIS_PARAMETERS_TOOLTIP = "<html>Parameters used in the generation of peak lists or processed spectra:<br>"
			+ "The information on how the spectra have been processed.<br>"
			+ "This include the list of parameters triggering the generation<br>"
			+ "of peak lists, chromatograms, images from raw data or already<br>"
			+ "processed data and the order in which they have been used.</html>";

	private static final String DATAANALYSIS_PARAMETERS_FILE_TOOLTIP = "<html>Parameter file URL</html>";

	private static final int TEXT_FIELD_COLUMNS = 50;
	private static final int TEXTAREA_ROWS = 10;
	private static final int TEXTAREA_COLUMNS = 50;

	private final String[] ionSourceTypes = { "", ESI, MALDI, OTHER };
	public JComboBox ionSourceTypeCombo;
	private JTextField miapeName = new JTextField(20);

	private List instrumentConfiguration = new ArrayList();
	private List<JComponent> addInfos = new ArrayList<JComponent>();
	private List<JComponent> samplePreps = new ArrayList<JComponent>();
	private List<JComponent> contact = new ArrayList<JComponent>();

	private final String CUSTOMIZATIONS_TOOLTIP = "<html>Any significant (i.e. affecting behaviour) deviations from the manufacture's specification for the mass spectrometer.</html>";
	private final ControlVocabularyManager cvManager;
	private final CVRetriever cvRetriever;
	private final MiapeMSForms miapeMSForms;
	private final Font font = new TextField().getFont();
	private final Border border = new EtchedBorder();
	private boolean showMaldi = false;
	private boolean showEsi = false;
	private boolean showOther;

	public MIAPEMSFormManager(MiapeMSForms miapeMSForms,
			ControlVocabularyManager cvManager) {
		this.cvManager = cvManager;
		cvRetriever = new CVRetriever(cvManager);
		this.miapeMSForms = miapeMSForms;
		ionSourceTypeCombo = createIonSourceTypeCombo("");

	}

	protected void ionSourceChanged(ItemEvent evt) {
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			final String ionSourceSelected = (String) evt.getItem();
			if (ionSourceSelected.equals(MALDI)) {
				showMaldi = true;
				showEsi = false;
				showOther = false;
			} else if (ionSourceSelected.equals(ESI)) {
				showMaldi = false;
				showEsi = true;
				showOther = false;
			} else if (ionSourceSelected.equals(OTHER)) {
				showEsi = false;
				showMaldi = false;
				showOther = true;
			} else if (ionSourceSelected.equals("")) {
				showEsi = false;
				showMaldi = false;
				showOther = false;
			}
			miapeMSForms.showMIAPEdata(MiapeMSForms.ION_SOURCES_SLIDE);
		}

	}

	public MiapeMSDocument getMIAPEMSFromForms(String projectName) {
		String name = miapeName.getText();
		if ("".equals(name))
			name = "name";

		if (projectName == null || !"".equals(projectName))
			projectName = "MIAPE project";

		MiapeMSDocumentBuilder builder = MiapeMSDocumentFactory
				.createMiapeMSDocumentBuilder(MiapeDocumentFactory
						.createProjectBuilder(projectName).build(), name, null,
						null, SpringHandler.getInstance().getXmlManager(),
						cvManager);

		// CONTACT
		final String contactName = ((JTextField) contact.get(0)).getText();
		if (contactName != null && !"".equals(contactName)) {
			final String contactLastName = ((JTextField) contact.get(1))
					.getText();
			final String contactInstitution = ((JTextField) contact.get(2))
					.getText();
			final String contactDepartment = ((JTextField) contact.get(3))
					.getText();
			final String contactPosition = (String) ((JComboBox) contact.get(4))
					.getSelectedItem();
			final String contactEmail = ((JTextField) contact.get(5)).getText();
			final String contactAddress = ((JTextArea) contact.get(6))
					.getText();
			final String contactTlfn = ((JTextField) contact.get(7)).getText();

			MSContactBuilder contactBuilder = MiapeDocumentFactory
					.createMSContactBuilder(contactName, contactLastName,
							contactEmail);
			if (contactInstitution != null && !"".equals(contactInstitution))
				contactBuilder.institution(contactInstitution);
			if (contactDepartment != null && !"".equals(contactDepartment))
				contactBuilder.department(contactDepartment);
			if (contactPosition != null && !"".equals(contactPosition))
				contactBuilder.position(contactPosition);
			if (contactAddress != null && !"".equals(contactAddress))
				contactBuilder.address(contactAddress);
			if (contactTlfn != null && !"".equals(contactTlfn))
				contactBuilder.telephone(contactTlfn);
			builder.contact(contactBuilder.build());
		}

		// SPECTROMETER
		Set<Spectrometer> spectrometers = new HashSet<Spectrometer>();
		final String spectrometerName = (String) ((JComboBox) spectrometer
				.get(0)).getSelectedItem();
		if (!"".equals(spectrometerName)) {
			final SpectrometerBuilder spectrometerBuilder = MiapeMSDocumentFactory
					.createSpectrometerBuilder(spectrometerName);
			final String manufacturer = (String) ((JComboBox) spectrometer
					.get(1)).getSelectedItem();
			if (!"".equals(manufacturer))
				spectrometerBuilder.manufacturer(manufacturer);
			final String customizations = ((JTextArea) spectrometer.get(2))
					.getText();
			if (!"".equals(customizations))
				spectrometerBuilder.customizations(customizations);
			final String version = ((JTextField) spectrometer.get(3)).getText();
			if (!"".equals(version))
				spectrometerBuilder.version(version);
			spectrometers.add(spectrometerBuilder.build());
			builder.spectrometers(spectrometers);
		}
		// Instrument Configuration
		List<InstrumentConfiguration> ics = new ArrayList<InstrumentConfiguration>();
		String icName = "IC";
		if (!"".equals(icName)) {

			final InstrumentConfigurationBuilder icBuilder = MiapeMSDocumentFactory
					.createInstrumentConfigurationBuilder(icName);

			List analyserList = (List) instrumentConfiguration
					.get(ANALYZERS_INDEX);
			if (analyserList != null && !analyserList.isEmpty()) {
				List<Analyser> analysers = new ArrayList<Analyser>();
				for (Object object : analyserList) {
					List<JComponent> analyserComponentList = (List<JComponent>) object;
					final String analyserName = (String) ((JComboBox) analyserComponentList
							.get(0)).getSelectedItem();

					if (!"".equals(analyserName)) {
						final String analyserDescription = ((JTextArea) analyserComponentList
								.get(1)).getText();
						final String reflectron = (String) ((JComboBox) analyserComponentList
								.get(2)).getSelectedItem();
						AnalyserBuilder analyserBuilder = MiapeMSDocumentFactory
								.createAnalyserBuilder(analyserName);
						if (!"".equals(analyserDescription))
							analyserBuilder.description(analyserDescription);
						if (!"".equals(reflectron))
							analyserBuilder.reflectron(reflectron);
						analysers.add(analyserBuilder.build());
					}
				}
				if (!analysers.isEmpty())
					icBuilder.analysers(analysers);
			}

			if (showEsi) {
				List<JComponent> esiComponentList = (List<JComponent>) instrumentConfiguration
						.get(ESI_INDEX);
				if (esiComponentList != null && !esiComponentList.isEmpty()) {
					final String esiName = (String) ((JComboBox) esiComponentList
							.get(0)).getSelectedItem();
					final String supplyType = (String) ((JComboBox) esiComponentList
							.get(1)).getSelectedItem();
					final String parameters = ((JTextArea) esiComponentList
							.get(2)).getText();

					final List<JComponent> interfaceComponents = (List<JComponent>) esiComponentList
							.get(3);
					String interfaceName = ((JTextField) interfaceComponents
							.get(0)).getText();
					Equipment interfaceEquipment = null;
					if (interfaceName != null && !"".equals(interfaceName)) {
						String interfaceManufacturer = ((JTextField) interfaceComponents
								.get(1)).getText();
						String interfaceModel = ((JTextField) interfaceComponents
								.get(2)).getText();
						String interfaceDescription = ((JTextArea) interfaceComponents
								.get(3)).getText();
						EquipmentBuilder interfaceBuilder = MiapeDocumentFactory
								.createEquipmentBuilder(interfaceName);
						if (!"".equals(interfaceManufacturer))
							interfaceBuilder
									.manufacturer(interfaceManufacturer);
						if (!"".equals(interfaceModel))
							interfaceBuilder.model(interfaceModel);
						if (!"".equals(interfaceDescription))
							interfaceBuilder.description(interfaceDescription);
						interfaceEquipment = interfaceBuilder.build();
					}

					final List<JComponent> sprayerComponents = (List<JComponent>) esiComponentList
							.get(4);
					String sprayerName = ((JTextField) sprayerComponents.get(0))
							.getText();
					Equipment sprayerEquipment = null;
					if (sprayerName != null && !"".equals(sprayerName)) {
						String sprayerManufacturer = ((JTextField) sprayerComponents
								.get(1)).getText();
						String sprayerModel = ((JTextField) sprayerComponents
								.get(2)).getText();
						String sprayerDescription = ((JTextArea) sprayerComponents
								.get(3)).getText();
						EquipmentBuilder sprayerBuilder = MiapeDocumentFactory
								.createEquipmentBuilder(sprayerName);
						if (!"".equals(sprayerManufacturer))
							sprayerBuilder.manufacturer(sprayerManufacturer);
						if (!"".equals(sprayerModel))
							sprayerBuilder.model(sprayerModel);
						if (!"".equals(sprayerDescription))
							sprayerBuilder.description(sprayerDescription);
						sprayerEquipment = sprayerBuilder.build();
					}

					if (!"".equals(esiName)) {
						EsiBuilder esiBuilder = MiapeMSDocumentFactory
								.createEsiBuilder(esiName);
						if (!"".equals(supplyType))
							esiBuilder.supplyType(supplyType);
						if (!"".equals(parameters))
							esiBuilder.parameters(parameters);
						if (interfaceEquipment != null)
							esiBuilder.interfaceEquipment(interfaceEquipment);
						if (sprayerEquipment != null)
							esiBuilder.sprayer(sprayerEquipment);
						icBuilder.esi(esiBuilder.build());
					}

				}
			}
			if (showMaldi) {
				List<JComponent> maldiComponentList = (List<JComponent>) instrumentConfiguration
						.get(MALDI_INDEX);
				if (maldiComponentList != null && !maldiComponentList.isEmpty()) {

					final String maldiName = ((JTextField) maldiComponentList
							.get(0)).getText();
					final String plateType = (String) ((JComboBox) maldiComponentList
							.get(1)).getSelectedItem();
					final String matrix = ((JTextField) maldiComponentList
							.get(2)).getText();
					final String dissociation = (String) ((JComboBox) maldiComponentList
							.get(3)).getSelectedItem();
					final String dissociationSummary = ((JTextArea) maldiComponentList
							.get(4)).getText();
					final Boolean extraction = ((JCheckBox) maldiComponentList
							.get(5)).isSelected();
					final String laser = (String) ((JComboBox) maldiComponentList
							.get(6)).getSelectedItem();
					final String wavelength = ((JTextField) maldiComponentList
							.get(7)).getText();
					final String laserParams = ((JTextArea) maldiComponentList
							.get(8)).getText();

					if (!"".equals(maldiName)) {
						MaldiBuilder maldiBuilder = MiapeMSDocumentFactory
								.createMaldiBuilder(maldiName);
						if (!"".equals(plateType))
							maldiBuilder.plateType(plateType);
						if (!"".equals(matrix))
							maldiBuilder.matrix(matrix);
						if (!"".equals(dissociation))
							maldiBuilder.dissociation(dissociation);
						if (!"".equals(dissociationSummary))
							maldiBuilder
									.dissociationSummary(dissociationSummary);
						if (!"".equals(extraction))
							maldiBuilder.extraction(extraction.toString());
						if (!"".equals(laser))
							maldiBuilder.laser(laser);
						if (!"".equals(wavelength))
							maldiBuilder.laserWaveLength(wavelength);
						if (!"".equals(laserParams))
							maldiBuilder.laserParameters(laserParams);
						icBuilder.maldi(maldiBuilder.build());
					}
				}
			}
			if (showOther) {
				List<JComponent> otherSourceComponentList = (List<JComponent>) instrumentConfiguration
						.get(OTHER_ION_SOURCE_INDEX);
				if (otherSourceComponentList != null
						&& !otherSourceComponentList.isEmpty()) {

					final String otherSourceName = (String) ((JComboBox) otherSourceComponentList
							.get(0)).getSelectedItem();
					final String otherSourceParameters = ((JTextArea) otherSourceComponentList
							.get(1)).getText();
					if (!"".equals(otherSourceName)) {
						final Other_IonSourceBuilder other_IonSourceBuilder = MiapeMSDocumentFactory
								.createOther_IonSourceBuilder(otherSourceName);
						if (!"".equals(otherSourceParameters))
							other_IonSourceBuilder
									.parameters(otherSourceParameters);
						icBuilder
								.otherIonSource(other_IonSourceBuilder.build());
					}
				}
			}
			List<JComponent> activationComponentList = (List<JComponent>) instrumentConfiguration
					.get(ACTIVATION_INDEX);
			if (activationComponentList != null
					&& !activationComponentList.isEmpty()) {

				final String activationName = ((JTextField) activationComponentList
						.get(0)).getText();
				final String gasType = (String) ((JComboBox) activationComponentList
						.get(1)).getSelectedItem();
				final String gasPressure = ((JTextField) activationComponentList
						.get(2)).getText();
				final String gasPressureUnits = (String) ((JComboBox) activationComponentList
						.get(3)).getSelectedItem();
				final String activationType = (String) ((JComboBox) activationComponentList
						.get(4)).getSelectedItem();
				if (!"".equals(activationName)) {

					final ActivationDissociationBuilder activationBuilder = MiapeMSDocumentFactory
							.createActivationDissociationBuilder(activationName);
					if (!"".equals(gasType))
						activationBuilder.gas(gasType);
					if (!"".equals(gasPressure))
						activationBuilder.pressure(gasPressure);
					if (!"".equals(gasPressureUnits))
						activationBuilder.pressureUnit(gasPressureUnits);
					if (!"".equals(activationType))
						activationBuilder.activationType(activationType);
					icBuilder.activationDissociation(activationBuilder.build());
				} else if (!"".equals(gasType) || !"".equals(gasPressure)
						|| !"".equals(gasPressureUnits)
						|| !"".equals(activationType)) {
					throw new IllegalMiapeArgumentException(
							"A name of the instrument component is necessary to define the 'Activation/Dissociation' information");
				}

			}
			ics.add(icBuilder.build());

			if (!ics.isEmpty())
				builder.instrumentConfigurations(ics);
		}

		if (!acquisitions.isEmpty()) {
			Set<Acquisition> acquisitionSet = new HashSet<Acquisition>();
			for (Object object : acquisitions) {
				List<JComponent> acquisitionComponentList = (List<JComponent>) object;
				if (acquisitionComponentList != null
						&& !acquisitionComponentList.isEmpty()) {
					final String acquisitionName = (String) ((JComboBox) acquisitionComponentList
							.get(0)).getSelectedItem();
					if (acquisitionName != null && !"".equals(acquisitionName)) {
						final String acquisitionParameters = ((JTextArea) acquisitionComponentList
								.get(1)).getText();
						final String paramFile = ((JTextField) acquisitionComponentList
								.get(2)).getText();
						final String acquisitionManufacturer = ((JTextField) acquisitionComponentList
								.get(3)).getText();
						final String acquisitionVersion = ((JTextField) acquisitionComponentList
								.get(4)).getText();
						final String acquisitionDescr = ((JTextArea) acquisitionComponentList
								.get(5)).getText();
						final String acquisitionTargetList = ((JTextArea) acquisitionComponentList
								.get(6)).getText();
						final String acquisitionTargetFile = ((JTextField) acquisitionComponentList
								.get(7)).getText();

						final AcquisitionBuilder acquisitionBuilder = MiapeMSDocumentFactory
								.createAcquisitionBuilder(acquisitionName);
						if (!"".equals(acquisitionParameters))
							acquisitionBuilder
									.parameters(acquisitionParameters);
						if (!"".equals(paramFile))
							acquisitionBuilder.parameterFile(paramFile);
						if (!"".equals(acquisitionManufacturer))
							acquisitionBuilder
									.manufacturer(acquisitionManufacturer);
						if (!"".equals(acquisitionVersion))
							acquisitionBuilder.version(acquisitionVersion);
						if (!"".equals(acquisitionDescr))
							acquisitionBuilder.description(acquisitionDescr);
						if (!"".equals(acquisitionTargetList))
							acquisitionBuilder
									.targetList(acquisitionTargetList);
						if (!"".equals(acquisitionTargetFile))
							acquisitionBuilder
									.transitionListFile(acquisitionTargetFile);
						acquisitionSet.add(acquisitionBuilder.build());
					}
				}
			}
			if (!acquisitionSet.isEmpty())
				builder.acquisitions(acquisitionSet);
		}
		if (!dataAnalysises.isEmpty()) {
			Set<DataAnalysis> dataAnalysisSet = new HashSet<DataAnalysis>();
			for (Object object : dataAnalysises) {
				List<JComponent> dataAnalysisComponentList = (List<JComponent>) object;
				if (dataAnalysisComponentList != null
						&& !dataAnalysisComponentList.isEmpty()) {
					final String dataAnalysisName = (String) ((JComboBox) dataAnalysisComponentList
							.get(0)).getSelectedItem();
					if (dataAnalysisName != null
							&& !"".equals(dataAnalysisName)) {
						final String dataAnalysisVersion = ((JTextField) dataAnalysisComponentList
								.get(4)).getText();
						final String dataAnalysisManufacturer = ((JTextField) dataAnalysisComponentList
								.get(3)).getText();
						final String dataAnalysisDescription = (String) ((JComboBox) dataAnalysisComponentList
								.get(5)).getSelectedItem();
						final String dataAnalysisParameters = ((JTextArea) dataAnalysisComponentList
								.get(1)).getText();
						final String dataAnalysisParamFile = ((JTextField) dataAnalysisComponentList
								.get(2)).getText();

						final DataAnalysisBuilder dataAnalysisBuilder = MiapeMSDocumentFactory
								.createDataAnalysisBuilder(dataAnalysisName);
						if (!"".equals(dataAnalysisVersion))
							dataAnalysisBuilder.version(dataAnalysisVersion);
						if (!"".equals(dataAnalysisManufacturer))
							dataAnalysisBuilder
									.manufacturer(dataAnalysisManufacturer);
						if (!"".equals(dataAnalysisDescription))
							dataAnalysisBuilder
									.description(dataAnalysisDescription);
						if (!"".equals(dataAnalysisParameters))
							dataAnalysisBuilder
									.parameters(dataAnalysisParameters);
						if (!"".equals(dataAnalysisParamFile))
							dataAnalysisBuilder
									.parametersLocation(dataAnalysisParamFile);

						dataAnalysisSet.add(dataAnalysisBuilder.build());
					}
				}
			}
			if (!dataAnalysisSet.isEmpty())
				builder.dataAnalyses(dataAnalysisSet);
		}
		List<MSAdditionalInformation> addInfos = new ArrayList<MSAdditionalInformation>();
		if (!this.addInfos.isEmpty()) {
			// sample name
			final String sampleName = ((JTextArea) this.addInfos.get(0))
					.getText();
			if (sampleName != null && !"".equals(sampleName)) {
				addInfos.add(MiapeMSDocumentFactory
						.createAdditionalInformationBuilder(
								SampleInformation.getInstance(cvManager)
										.getSampleBatchTerm()
										.getPreferredName()).value(sampleName)
						.build());

			}
			// sample name
			final String sampleVolumen = ((JTextField) this.addInfos.get(1))
					.getText();
			if (sampleVolumen != null && !"".equals(sampleVolumen)) {
				addInfos.add(MiapeMSDocumentFactory
						.createAdditionalInformationBuilder(
								SampleInformation.getInstance(cvManager)
										.getSampleVolumenTerm()
										.getPreferredName())
						.value(sampleVolumen).build());

			}
			// tissue
			final String sampletissue = (String) ((JComboBox) this.addInfos
					.get(2)).getSelectedItem();
			if (sampletissue != null && !"".equals(sampletissue)) {
				addInfos.add(MiapeMSDocumentFactory
						.createAdditionalInformationBuilder(sampletissue)
						.build());

			}

			// cell type
			final String cellType = (String) ((JComboBox) this.addInfos.get(3))
					.getSelectedItem();
			if (cellType != null && !"".equals(cellType)) {
				addInfos.add(MiapeMSDocumentFactory
						.createAdditionalInformationBuilder(cellType).build());

			}
			// disease
			final String sampleDisease = (String) ((JComboBox) this.addInfos
					.get(4)).getSelectedItem();
			if (sampleDisease != null && !"".equals(sampleDisease)) {
				addInfos.add(MiapeMSDocumentFactory
						.createAdditionalInformationBuilder(sampleDisease)
						.build());

			}
			// taxonomy
			final String taxonomy = (String) ((JComboBox) this.addInfos.get(5))
					.getSelectedItem();
			if (taxonomy != null && !"".equals(taxonomy)) {
				addInfos.add(MiapeMSDocumentFactory
						.createAdditionalInformationBuilder(taxonomy).build());

			}

			if (this.addInfos.size() > 6) {
				for (int i = 6; i < this.addInfos.size(); i++) {
					final String addInfoName = (String) ((JComboBox) this.addInfos
							.get(i)).getSelectedItem();
					if (!"".equals(addInfoName)) {
						AdditionalInformationBuilder addInfoBuilder = MiapeMSDocumentFactory
								.createAdditionalInformationBuilder(addInfoName);
						if (i + 1 < this.addInfos.size()) {
							final String addInfoValue = ((JTextArea) this.addInfos
									.get(i + 1)).getText();
							addInfoBuilder.value(addInfoValue);
						}
						addInfos.add(addInfoBuilder.build());
					}
					i++;
				}
			}

		}
		if (!samplePreps.isEmpty()) {
			for (int i = 0; i < samplePreps.size(); i++) {
				final String addInfoName = (String) ((JComboBox) samplePreps
						.get(i)).getSelectedItem();
				if (!"".equals(addInfoName)) {
					AdditionalInformationBuilder addInfoBuilder = MiapeMSDocumentFactory
							.createAdditionalInformationBuilder(addInfoName);
					if (i + 1 < samplePreps.size()) {
						final String addInfoValue = ((JTextArea) samplePreps
								.get(i + 1)).getText();
						addInfoBuilder.value(addInfoValue);
					}
					addInfos.add(addInfoBuilder.build());
				}
				i++;
			}
		}
		if (!addInfos.isEmpty())
			builder.additionalInformations(addInfos);

		return builder.build();
	}

	private void resetValues() {
		spectrometer.clear();
		addInfos.clear();
		instrumentConfiguration.clear();
		miapeName.setText(null);
		dataAnalysises.clear();
		acquisitions.clear();
		contact.clear();
	}

	public void loadMIAPEMS(MiapeMSDocument miapeMS,
			ControlVocabularyManager cvManager) {

		resetValues();

		if (miapeMS == null) {
			miapeName = getTextField(null);
			// CONTACT
			contact = getContactEmpyList();

			// ADD INFOS
			addInfos = getAddInformationEmptyLists();
			// Sample prep
			samplePreps = getSamplePreparationEmptyLists();

			// SPECTROMETER
			// Spectrometer name
			spectrometer
					.add(getComboBox(cvRetriever.getInstrumentNames(), null));
			// Spectrometer manufacturer
			spectrometer.add(getComboBox(cvRetriever.getManufacturerNames(),
					null));
			// Spectrometer customizations
			JTextArea customizations = getTextArea(null);
			customizations.setToolTipText(CUSTOMIZATIONS_TOOLTIP);
			spectrometer.add(customizations);
			// Spectrometer version
			spectrometer.add(getTextField(null));

			// INSTRUMENT CONFIGURATION NAME
			instrumentConfiguration = getInstrumentConfigurationEmptyList();

			// ACQUISITIONS
			acquisitions.add(getAcquisitionEmptyList());
			// DATA ANALYSIS
			dataAnalysises.add(getDataAnalysisEmptyList());
			return;
		}

		// if MIAPE MS IS NOT NULL:

		// NAME
		String miapeNameText = miapeMS.getName();
		if (miapeNameText != null) {
			miapeName = getTextField(miapeNameText);
		}
		// CONTACT
		MSContact miapeContact = miapeMS.getContact();
		if (miapeContact != null) {
			contact = getContactList(miapeContact);
		} else {
			contact = getContactEmpyList();
		}
		// ADD INFO AND SAMPLE PROCESSING
		final List<MSAdditionalInformation> additionalInformations = miapeMS
				.getAdditionalInformations();
		if (additionalInformations != null && !additionalInformations.isEmpty()) {
			addInfos = getAddInformationList(additionalInformations);
			samplePreps = getSamplePreparationList(additionalInformations);
		} else {
			addInfos = getAddInformationEmptyLists();
			samplePreps = getSamplePreparationEmptyLists();
		}

		// SPECTROMETER
		final Set<Spectrometer> spectrometers = miapeMS.getSpectrometers();
		if (spectrometers != null && !spectrometers.isEmpty()) {
			Spectrometer spect = spectrometers.iterator().next();
			// Spectrometer name
			List<String> instrumentNames = cvRetriever.getInstrumentNames();
			JComboBox name = getComboBox(instrumentNames, spect.getName());
			spectrometer.add(name);
			// Spectrometer manufacturer
			List<String> manufacturersNames = cvRetriever
					.getManufacturerNames();
			JComboBox manufacturer = getComboBox(manufacturersNames,
					spect.getManufacturer());
			spectrometer.add(manufacturer);
			// Spectrometer customizations
			JTextArea customizations = getTextArea(spect.getCustomizations());
			customizations.setToolTipText(CUSTOMIZATIONS_TOOLTIP);
			spectrometer.add(customizations);
			// Spectrometer version
			JTextField version = getTextField(spect.getVersion());
			spectrometer.add(version);
		} else {

			// Spectrometer name
			spectrometer
					.add(getComboBox(cvRetriever.getInstrumentNames(), null));
			// Spectrometer manufacturer
			spectrometer.add(getComboBox(cvRetriever.getManufacturerNames(),
					null));
			// Spectrometer customizations
			JTextArea customizations = getTextArea(null);
			customizations.setToolTipText(CUSTOMIZATIONS_TOOLTIP);
			spectrometer.add(customizations);
			// Spectrometer version
			spectrometer.add(getTextField(null));
		}

		final List<InstrumentConfiguration> instrumentConfigurations = miapeMS
				.getInstrumentConfigurations();
		if (instrumentConfigurations != null
				&& !instrumentConfigurations.isEmpty()) {
			InstrumentConfiguration instrumentConfiguration = instrumentConfigurations
					.get(0);
			this.instrumentConfiguration = getInstrumentConfigurationList(instrumentConfiguration);

		} else {
			instrumentConfiguration = getInstrumentConfigurationEmptyList();
		}

		final Set<Acquisition> acquisitionSet = miapeMS.getAcquisitions();
		if (acquisitionSet != null && !acquisitionSet.isEmpty()) {
			acquisitions.clear();
			for (Acquisition acquisition : acquisitionSet) {
				acquisitions.add(getAcquisitionList(acquisition));
			}
		} else {
			acquisitions.add(getAcquisitionEmptyList());
		}

		final Set<DataAnalysis> dataAnalysisSet = miapeMS.getDataAnalysis();
		if (dataAnalysisSet != null && !dataAnalysisSet.isEmpty()) {
			dataAnalysises.clear();
			for (DataAnalysis dataAnalysis : dataAnalysisSet) {
				dataAnalysises.add(getDataAnalysisList(dataAnalysis));
			}
		} else {
			dataAnalysises.add(getDataAnalysisEmptyList());
		}
	}

	private List<JComponent> getSamplePreparationEmptyLists() {
		List<JComponent> ret = new ArrayList<JComponent>();

		// Others name
		ret.add(getComboBox(cvRetriever.getSampleProcessingSteps(), null));
		// others value
		ret.add(getTextArea(null));

		return ret;
	}

	private List<JComponent> getContactList(MSContact miapeContact) {
		List<JComponent> ret = new ArrayList<JComponent>();
		ret.add(getTextField(miapeContact.getName()));
		ret.add(getTextField(miapeContact.getLastName()));
		ret.add(getTextField(miapeContact.getInstitution()));
		ret.add(getTextField(miapeContact.getDepartment()));
		ret.add(getComboBox(cvRetriever.getPositions(),
				miapeContact.getPosition()));
		ret.add(getTextField(miapeContact.getEmail()));
		ret.add(getTextArea(miapeContact.getAddress()));
		ret.add(getTextField(miapeContact.getTelephone()));
		return ret;
	}

	private List<JComponent> getContactEmpyList() {
		List<JComponent> ret = new ArrayList<JComponent>();
		ret.add(getTextField(null));
		ret.add(getTextField(null));
		ret.add(getTextField(null));
		ret.add(getTextField(null));
		ret.add(getComboBox(cvRetriever.getPositions(), null));
		ret.add(getTextField(null));
		ret.add(getTextArea(null));
		ret.add(getTextField(null));
		return ret;
	}

	private List<JComponent> getAddInformationList(
			List<MSAdditionalInformation> additionalInformations) {
		List<JComponent> ret = new ArrayList<JComponent>();
		final String sampleBatchCVName = cvManager.getControlVocabularyName(
				SampleInformation.SAMPLE_BATCH_ACC,
				SampleInformation.getInstance(cvManager));
		final String sampleVolumneName = cvManager.getControlVocabularyName(
				SampleInformation.SAMPLE_VOLUME_ACC,
				SampleInformation.getInstance(cvManager));

		// sample name
		ret.add(getTextArea(getAddInfoValue(additionalInformations,
				sampleBatchCVName)));
		// sample volume
		ret.add(getTextField(getAddInfoValue(additionalInformations,
				sampleVolumneName)));

		// tissue
		ret.add(getComboBox(
				cvRetriever.getTissueNames(),
				getAddInfoName(additionalInformations,
						TissuesTypes.getInstance(cvManager))));

		// cell type
		ret.add(getComboBox(
				cvRetriever.getCellTypesNames(),
				getAddInfoName(additionalInformations,
						CellTypes.getInstance(cvManager))));
		// dissease
		ret.add(getComboBox(
				cvRetriever.getDisseasesNames(),
				getAddInfoName(additionalInformations,
						HumanDisseases.getInstance(cvManager))));
		// taxonomies
		ret.add(getComboBox(
				cvRetriever.getTaxonomies(),
				getAddInfoName(additionalInformations,
						MainTaxonomies.getInstance(cvManager))));

		// Other add infos:
		boolean sampleNameFound = false;
		boolean sampleVolumenFound = false;
		boolean tissueFound = false;
		boolean cellTypeFound = false;
		boolean diseaseFound = false;
		boolean someOtherAdded = false;
		boolean taxonomyFound = false;
		for (MSAdditionalInformation msAddInfo : additionalInformations) {
			if (msAddInfo.getName().equals(sampleBatchCVName)
					&& !sampleNameFound) {
				sampleNameFound = true;
				continue;
			}
			if (msAddInfo.getName().equals(sampleVolumneName)
					&& !sampleVolumenFound) {
				sampleVolumenFound = true;
				continue;
			}
			if (cvManager.isCV(msAddInfo.getName(),
					TissuesTypes.getInstance(cvManager))
					&& !tissueFound) {
				tissueFound = true;
				continue;
			}
			if (cvManager.isCV(msAddInfo.getName(),
					CellTypes.getInstance(cvManager))
					&& !cellTypeFound) {
				cellTypeFound = true;
				continue;
			}
			if (cvManager.isCV(msAddInfo.getName(),
					HumanDisseases.getInstance(cvManager))
					&& !diseaseFound) {
				diseaseFound = true;
				continue;
			}
			if (cvManager.isCV(msAddInfo.getName(),
					MainTaxonomies.getInstance(cvManager))
					&& !taxonomyFound) {
				taxonomyFound = true;
				continue;
			}

			if (!cvManager.isCV(msAddInfo.getName(),
					SampleProcessingStep.getInstance(cvManager))) {
				// if it is not any of the previous cvs
				ret.add(getComboBox(cvRetriever.getSampleProcessingSteps(),
						msAddInfo.getName()));
				ret.add(getTextArea(msAddInfo.getValue()));
				someOtherAdded = true;
			}

		}
		if (!someOtherAdded) {
			ret.add(getComboBox(cvRetriever.getAddInformations(), null));
			ret.add(getTextArea(null));

		}

		return ret;
	}

	private List<JComponent> getSamplePreparationList(
			List<MSAdditionalInformation> additionalInformations) {
		List<JComponent> ret = new ArrayList<JComponent>();

		for (MSAdditionalInformation msAddInfo : additionalInformations) {
			if (cvManager.isCV(msAddInfo.getName(),
					SampleProcessingStep.getInstance(cvManager))) {
				// if it is not any of the previous cvs
				ret.add(getComboBox(cvRetriever.getSampleProcessingSteps(),
						msAddInfo.getName()));
				ret.add(getTextArea(msAddInfo.getValue()));
			}
		}

		return ret;
	}

	private String getAddInfoName(
			List<MSAdditionalInformation> additionalInformations,
			ControlVocabularySet... cvSets) {
		for (MSAdditionalInformation addInfo : additionalInformations) {
			for (ControlVocabularySet controlVocabularySet : cvSets) {
				final boolean isCv = cvManager.isCV(addInfo.getName(),
						controlVocabularySet);
				if (isCv) {
					return addInfo.getName();
				}
			}
		}
		return null;
	}

	private MSAdditionalInformation getAddInfo(
			List<MSAdditionalInformation> additionalInformations,
			ControlVocabularySet... cvSets) {
		for (MSAdditionalInformation addInfo : additionalInformations) {
			for (ControlVocabularySet controlVocabularySet : cvSets) {
				final boolean isCv = cvManager.isCV(addInfo.getName(),
						controlVocabularySet);
				if (isCv) {
					return addInfo;
				}
			}
		}
		return null;
	}

	private String getAddInfoValue(
			List<MSAdditionalInformation> additionalInformations, String name) {
		for (MSAdditionalInformation msAdditionalInformation : additionalInformations) {
			if (msAdditionalInformation.getName().equals(name))
				return msAdditionalInformation.getValue();
		}
		return null;
	}

	private List<JComponent> getAddInformationEmptyLists() {
		List<JComponent> ret = new ArrayList<JComponent>();

		// sample name
		ret.add(getTextArea(null));
		// sample volume
		ret.add(getTextField(null));
		// tissue
		ret.add(getComboBox(cvRetriever.getTissueNames(), null));
		// cell types
		ret.add(getComboBox(cvRetriever.getCellTypesNames(), null));
		// disease
		ret.add(getComboBox(cvRetriever.getDisseasesNames(), null));
		// taxonomy
		ret.add(getComboBox(cvRetriever.getTaxonomies(), null));
		// Others name
		ret.add(getComboBox(cvRetriever.getAddInformations(), null));
		// others value
		ret.add(getTextArea(null));

		return ret;
	}

	private List<JComponent> getDataAnalysisEmptyList() {
		List<JComponent> ret = new ArrayList<JComponent>();
		final JComboBox comboBox = getComboBox(
				cvRetriever.getDataAnalysisSoftwareNames(), null);
		comboBox.setToolTipText(DATAANALYSIS_SOFTWARE_NAME_TOOLTIP);
		ret.add(comboBox);
		final JTextArea textArea = getTextArea(null);
		textArea.setToolTipText(DATAANALYSIS_PARAMETERS_TOOLTIP);
		ret.add(textArea);
		final JTextField textField = getTextField(null);
		textField.setToolTipText(DATAANALYSIS_PARAMETERS_FILE_TOOLTIP);
		ret.add(textField);
		ret.add(getTextField(null));
		ret.add(getTextField(null));
		final JComponent comboBox2 = getComboBox(
				cvRetriever.getDataAnalysisDescription(), null);
		ret.add(comboBox2);
		return ret;
	}

	private List<JComponent> getDataAnalysisList(DataAnalysis dataAnalysis) {
		List<JComponent> ret = new ArrayList<JComponent>();
		final JComboBox comboBox = getComboBox(
				cvRetriever.getDataAnalysisSoftwareNames(),
				dataAnalysis.getName());
		comboBox.setToolTipText(DATAANALYSIS_SOFTWARE_NAME_TOOLTIP);
		ret.add(comboBox);
		final JTextArea textArea = getTextArea(dataAnalysis.getParameters());
		textArea.setToolTipText(DATAANALYSIS_PARAMETERS_TOOLTIP);
		ret.add(textArea);
		final JTextField textField = getTextField(dataAnalysis
				.getParametersLocation());
		textField.setToolTipText(DATAANALYSIS_PARAMETERS_FILE_TOOLTIP);
		ret.add(textField);
		ret.add(getTextField(dataAnalysis.getManufacturer()));
		ret.add(getTextField(dataAnalysis.getVersion()));
		final JComponent comboBox2 = getComboBox(
				cvRetriever.getDataAnalysisDescription(),
				dataAnalysis.getDescription());
		ret.add(comboBox2);

		return ret;
	}

	private List<JComponent> getAcquisitionEmptyList() {
		List<JComponent> ret = new ArrayList<JComponent>();
		final JComboBox comboBox = getComboBox(
				cvRetriever.getSoftwareAcquisitionNames(), null);
		comboBox.setToolTipText(ACQUISITION_SOFTWARE_NAME_TOOLTIP);
		ret.add(comboBox);
		final JTextArea textArea = getTextArea(null);
		textArea.setToolTipText(ACQUISITION_PARAMETERS_TOOLTIP);
		ret.add(textArea);
		final JTextField textField = getTextField(null);
		textField.setToolTipText(ACQUISITION_PARAMETERS_FILE_TOOLTIP);
		ret.add(textField);
		ret.add(getTextField(null));
		ret.add(getTextField(null));
		ret.add(getTextArea(null));
		final JTextArea textArea2 = getTextArea(null);
		textArea2.setToolTipText(TARGET_LIST_TOOLTIP);
		ret.add(textArea2);
		final JTextField textField2 = getTextField(null);
		textField2.setToolTipText(TARGET_FILE_TOOLTIP);
		ret.add(textField2);
		return ret;
	}

	private List<JComponent> getAcquisitionList(Acquisition acquisition) {
		List<JComponent> ret = new ArrayList<JComponent>();
		final JComboBox comboBox = getComboBox(
				cvRetriever.getSoftwareAcquisitionNames(),
				acquisition.getName());
		comboBox.setToolTipText(ACQUISITION_SOFTWARE_NAME_TOOLTIP);
		ret.add(comboBox);
		final JTextArea textArea = getTextArea(acquisition.getParameters());
		textArea.setToolTipText(ACQUISITION_PARAMETERS_TOOLTIP);
		ret.add(textArea);
		final JTextField textField = getTextField(acquisition
				.getParameterFile());
		textField.setToolTipText(ACQUISITION_PARAMETERS_FILE_TOOLTIP);
		ret.add(textField);
		ret.add(getTextField(acquisition.getManufacturer()));
		ret.add(getTextField(acquisition.getVersion()));
		ret.add(getTextArea(acquisition.getDescription()));
		final JTextArea textArea2 = getTextArea(acquisition.getTargetList());
		textArea2.setToolTipText(TARGET_LIST_TOOLTIP);
		ret.add(textArea2);
		final JTextField textField2 = getTextField(acquisition
				.getTransitionListFile());
		textField2.setToolTipText(TARGET_FILE_TOOLTIP);
		ret.add(textField2);
		return ret;
	}

	private List getInstrumentConfigurationList(
			InstrumentConfiguration instrumentConfiguration) {
		List ret = new ArrayList();

		// ANALYZERS
		final List<Analyser> analyzers = instrumentConfiguration.getAnalyzers();
		List analysersList = new ArrayList();
		if (analyzers != null && !analyzers.isEmpty()) {
			for (Analyser analyser : analyzers) {
				List<JComponent> analyserComponentsList = getAnalyserComponentList(analyser);
				analysersList.add(analyserComponentsList);
			}
		} else {
			analysersList = getAnalyzerEmptyList();
		}
		ret.add(ANALYZERS_INDEX, analysersList);
		// END ANALYZER

		// ESI:
		final List<Esi> esis = instrumentConfiguration.getEsis();
		List esiComponentsList = new ArrayList();
		if (esis != null && !esis.isEmpty()) {
			ionSourceTypeCombo = createIonSourceTypeCombo(ESI);
			Esi esi = esis.get(0);
			esiComponentsList = getEsiComponentList(esi);
		} else {
			esiComponentsList = getEsiEmptyList();
		}
		ret.add(ESI_INDEX, esiComponentsList);
		// END ESI

		// MALDI
		final List<Maldi> maldis = instrumentConfiguration.getMaldis();
		List maldiComponentsList = new ArrayList();
		if (maldis != null && !maldis.isEmpty()) {
			ionSourceTypeCombo = createIonSourceTypeCombo(MALDI);
			Maldi maldi = maldis.get(0);
			maldiComponentsList = getMaldiComponentList(maldi);
		} else {
			maldiComponentsList = getMaldiEmptyList();
		}
		ret.add(MALDI_INDEX, maldiComponentsList);
		// END MALDI

		// OTHER ION SOURCE
		final List<Other_IonSource> others = instrumentConfiguration
				.getOther_IonSources();
		List<JComponent> othersComponentsList = new ArrayList<JComponent>();
		if (others != null && !others.isEmpty()) {
			ionSourceTypeCombo = createIonSourceTypeCombo(OTHER);
			Other_IonSource ionSource = others.get(0);
			othersComponentsList = getOtherIonSourceComponentList(ionSource);
		} else {
			othersComponentsList = getOtherIonSourceEmptyList();
		}
		ret.add(OTHER_ION_SOURCE_INDEX, othersComponentsList);
		// END OTHER ION SOURCE

		// ACTIVATION
		final Set<ActivationDissociation> activations = instrumentConfiguration
				.getActivationDissociations();
		List<JComponent> activationComponentsList = new ArrayList();
		if (activations != null && !activations.isEmpty()) {
			ActivationDissociation activation = activations.iterator().next();
			activationComponentsList = getActivationComponentList(activation);
		} else {
			activationComponentsList = getActivationEmptyList();
		}
		ret.add(ACTIVATION_INDEX, activationComponentsList);
		// END ACTIVATION
		return ret;
	}

	private List getInstrumentConfigurationEmptyList() {
		List ret = new ArrayList();

		// ANALYZERS
		List analysersList = getAnalyzerEmptyList();
		ret.add(ANALYZERS_INDEX, analysersList);
		// END ANALYZER

		// ESI:
		List esiComponentsList = getEsiEmptyList();
		ret.add(ESI_INDEX, esiComponentsList);
		// END ESI

		// MALDI
		List maldiComponentsList = getMaldiEmptyList();
		ret.add(MALDI_INDEX, maldiComponentsList);
		// END MALDI

		// OTHER ION SOURCE
		List otherIonSourceComponentsList = getOtherIonSourceEmptyList();
		ret.add(OTHER_ION_SOURCE_INDEX, otherIonSourceComponentsList);
		// END OTHER ION SOURCE

		// ACTIVATION
		List activationComponentsList = getActivationEmptyList();
		ret.add(ACTIVATION_INDEX, activationComponentsList);
		// END ACTIVATION
		return ret;
	}

	private List<JComponent> getActivationEmptyList() {
		List<JComponent> ret = new ArrayList<JComponent>();
		final JTextField textField = getTextField(null);
		textField.setToolTipText(ACTIVATION_NAME_TOOLTIP);
		ret.add(textField);
		final JComboBox comboBox = getComboBox(cvRetriever.getGasTypes(), null);
		comboBox.setToolTipText(GAS_TYPE_TOOLTIP);
		ret.add(comboBox);
		final JTextField textField2 = getTextField(null);
		textField2.setToolTipText(GAS_PRESSURE_TOOLTIP);
		ret.add(textField2);
		final JComboBox comboBox2 = getComboBox(cvRetriever.getPressureUnits(),
				null);
		comboBox2.setToolTipText(GAS_PRESSURE_TOOLTIP);
		ret.add(comboBox2);
		final JComboBox comboBox3 = getComboBox(
				cvRetriever.getActivationTypes(), null);
		comboBox3.setToolTipText(ACTIVATION_TYPE_TOOLTIP);
		ret.add(comboBox3);
		return ret;
	}

	private List<JComponent> getActivationComponentList(
			ActivationDissociation activation) {
		List<JComponent> ret = new ArrayList<JComponent>();
		final JTextField textField = getTextField(activation.getName());
		textField.setToolTipText(ACTIVATION_NAME_TOOLTIP);
		ret.add(textField);
		final JComboBox comboBox = getComboBox(cvRetriever.getGasTypes(),
				activation.getGasType());
		comboBox.setToolTipText(GAS_TYPE_TOOLTIP);
		ret.add(comboBox);
		final JTextField textField2 = getTextField(activation.getGasPressure());
		textField2.setToolTipText(GAS_PRESSURE_TOOLTIP);
		ret.add(textField2);
		final JComboBox comboBox2 = getComboBox(cvRetriever.getPressureUnits(),
				activation.getPressureUnit());
		comboBox2.setToolTipText(GAS_PRESSURE_TOOLTIP);
		ret.add(comboBox2);
		final JComboBox comboBox3 = getComboBox(
				cvRetriever.getActivationTypes(),
				activation.getActivationType());
		comboBox3.setToolTipText(ACTIVATION_TYPE_TOOLTIP);
		ret.add(comboBox3);
		return ret;
	}

	private List getOtherIonSourceEmptyList() {
		List<JComponent> ret = new ArrayList<JComponent>();

		ret.add(getComboBox(cvRetriever.getOtherIonSourceNames(), null));
		final JTextArea textArea = getTextArea(null);
		textArea.setToolTipText(OTHER_ION_SOURCE_TOOLTIP);
		ret.add(textArea);

		return ret;
	}

	private List<JComponent> getOtherIonSourceComponentList(
			Other_IonSource ionSource) {
		List<JComponent> ret = new ArrayList<JComponent>();

		final String name = ionSource.getName();
		if (name != null && "".equals(name)) {
			ret.add(getComboBox(cvRetriever.getOtherIonSourceNames(), name));
			final JTextArea textArea = getTextArea(ionSource.getParameters());
			textArea.setToolTipText(OTHER_ION_SOURCE_TOOLTIP);
			ret.add(textArea);
		}

		return ret;
	}

	private JComboBox createIonSourceTypeCombo(String itemToSelect) {
		JComboBox ret = new JComboBox(new DefaultComboBoxModel(ionSourceTypes));
		ret.setSelectedItem(itemToSelect);
		if (itemToSelect.equals(ESI)) {
			showEsi = true;
			showMaldi = false;
			showOther = false;
		} else if (itemToSelect.equals(MALDI)) {
			showEsi = false;
			showMaldi = true;
			showOther = false;
		} else if (itemToSelect.equals(OTHER)) {
			showEsi = false;
			showMaldi = false;
			showOther = true;
		}
		ret.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				ionSourceChanged(evt);
			}
		});
		return ret;
	}

	private List<JComponent> getMaldiEmptyList() {
		List<JComponent> maldiComponentsList = new ArrayList<JComponent>();
		maldiComponentsList.add(getTextField(null));
		final JComboBox comboBoxPlate = getComboBox(
				cvRetriever.getSamplePlateType(), null);
		comboBoxPlate.setToolTipText(PLATE_TYPE_TOOLTIP);
		maldiComponentsList.add(comboBoxPlate);
		final JTextField textFieldMatrix = getTextField(null);
		textFieldMatrix.setToolTipText(MATRIX_TOOLTIP);
		maldiComponentsList.add(textFieldMatrix);
		final JComboBox comboBoxMaldiDissociations = getComboBox(
				cvRetriever.getMaldiDissociations(), null);
		comboBoxMaldiDissociations.setToolTipText(MALDI_DISS_TOOLTIP);
		maldiComponentsList.add(comboBoxMaldiDissociations);
		final JTextArea textFieldMaldiDissSumary = getTextArea(null);
		textFieldMaldiDissSumary.setToolTipText(MALDI_SUMMARY_TOOLTIP);
		maldiComponentsList.add(textFieldMaldiDissSumary);
		final JCheckBox checkboxExtraction = getCheckBox("delayed extraction",
				false);
		maldiComponentsList.add(checkboxExtraction);
		checkboxExtraction.setToolTipText(EXTRACTION_TOOLTIP);
		maldiComponentsList.add(getComboBox(cvRetriever.getLaserTypes(), null));
		final JTextField textFieldLaserWavelength = getTextField(null);
		textFieldLaserWavelength.setToolTipText(LASER_WAVELENGTH_TOOLTIP);
		maldiComponentsList.add(textFieldLaserWavelength);
		final JTextArea textFieldLaserParameters = getTextArea(null);
		textFieldLaserParameters.setToolTipText(LASER_PARAMETERS_TOOLTIP);
		maldiComponentsList.add(textFieldLaserParameters);
		return maldiComponentsList;
	}

	private List<JComponent> getMaldiComponentList(Maldi maldi) {
		List<JComponent> maldiComponentsList = new ArrayList<JComponent>();
		maldiComponentsList.add(getTextField(maldi.getName()));
		final JComboBox comboBoxPlate = getComboBox(
				cvRetriever.getSamplePlateType(), maldi.getPlateType());
		comboBoxPlate.setToolTipText(PLATE_TYPE_TOOLTIP);
		maldiComponentsList.add(comboBoxPlate);
		final JTextField textFieldMatrix = getTextField(maldi.getMatrix());
		textFieldMatrix.setToolTipText(MATRIX_TOOLTIP);
		maldiComponentsList.add(textFieldMatrix);
		final JComboBox comboBoxMaldiDissociations = getComboBox(
				cvRetriever.getMaldiDissociations(), maldi.getDissociation());
		comboBoxMaldiDissociations.setToolTipText(MALDI_DISS_TOOLTIP);
		maldiComponentsList.add(comboBoxMaldiDissociations);
		final JTextArea textFieldMaldiDissSumary = getTextArea(maldi
				.getDissociationSummary());
		textFieldMaldiDissSumary.setToolTipText(MALDI_SUMMARY_TOOLTIP);
		maldiComponentsList.add(textFieldMaldiDissSumary);

		final String extraction = maldi.getExtraction();
		JCheckBox checkBoxExtraction = null;

		if (extraction != null
				&& (extraction.equalsIgnoreCase("false")
						|| extraction.contains("without") || extraction
							.contains("no")))
			checkBoxExtraction = getCheckBox("delayed extraction", false);
		else
			checkBoxExtraction = getCheckBox("delayed extraction", true);
		checkBoxExtraction.setToolTipText(EXTRACTION_TOOLTIP);

		maldiComponentsList.add(checkBoxExtraction);

		maldiComponentsList.add(getComboBox(cvRetriever.getLaserTypes(),
				maldi.getLaser()));
		final JTextField textFieldLaserWavelength = getTextField(maldi
				.getLaserWaveLength());
		textFieldLaserWavelength.setToolTipText(LASER_WAVELENGTH_TOOLTIP);
		maldiComponentsList.add(textFieldLaserWavelength);
		final JTextArea textFieldLaserParameters = getTextArea(maldi
				.getLaserParameters());
		textFieldLaserParameters.setToolTipText(LASER_PARAMETERS_TOOLTIP);
		maldiComponentsList.add(textFieldLaserParameters);
		return maldiComponentsList;
	}

	private List getEsiComponentList(Esi esi) {
		List esiComponentsList = new ArrayList();
		esiComponentsList.add(getComboBox(cvRetriever.getEsiNames(),
				esi.getName()));
		esiComponentsList.add(getComboBox(cvRetriever.getSupplyTypeNames(),
				esi.getSupplyType()));
		final JTextArea textField = getTextArea(esi.getParameters());
		textField.setToolTipText(ESIPARAMETERS_TOOLTIP);
		esiComponentsList.add(textField);
		// INTERFACES
		List<JComponent> interfaceComponents = new ArrayList<JComponent>();
		final Set<Equipment> interfaces = esi.getInterfaces();
		if (interfaces != null && !interfaces.isEmpty()) {
			Equipment equipment = interfaces.iterator().next();
			interfaceComponents = getInterfaceComponentLisT(equipment);
		} else {
			interfaceComponents = getInterfaceEmptyList();
		}
		esiComponentsList.add(interfaceComponents);
		// SPRAYERS
		List<JComponent> sprayerComponents = new ArrayList<JComponent>();
		final Set<Equipment> sprayers = esi.getSprayers();
		if (sprayers != null && !sprayers.isEmpty()) {
			Equipment equipment = sprayers.iterator().next();
			sprayerComponents = getSprayerComponentList(equipment);
		} else {
			sprayerComponents = getSprayerEmptyList();
		}
		esiComponentsList.add(sprayerComponents);
		return esiComponentsList;
	}

	private List<JComponent> getSprayerEmptyList() {
		List<JComponent> sprayerComponents = new ArrayList<JComponent>();
		sprayerComponents.add(getTextField(null));
		sprayerComponents.add(getTextField(null));
		sprayerComponents.add(getTextField(null));
		final JTextArea textFieldDescription = getTextArea(null);
		textFieldDescription.setToolTipText(SPRAYER_DESCRIPTION_TOOLTIP);
		sprayerComponents.add(textFieldDescription);
		return sprayerComponents;
	}

	private List<JComponent> getSprayerComponentList(Equipment equipment) {
		List<JComponent> sprayerComponents = new ArrayList<JComponent>();
		sprayerComponents.add(getTextField(equipment.getName()));
		sprayerComponents.add(getTextField(equipment.getManufacturer()));
		sprayerComponents.add(getTextField(equipment.getModel()));
		final JTextArea textFieldDescription = getTextArea(equipment
				.getDescription());
		textFieldDescription.setToolTipText(SPRAYER_DESCRIPTION_TOOLTIP);
		sprayerComponents.add(textFieldDescription);
		return sprayerComponents;
	}

	private List<JComponent> getInterfaceComponentLisT(Equipment equipment) {
		List<JComponent> interfaceComponents = new ArrayList<JComponent>();
		interfaceComponents.add(getTextField(equipment.getName()));
		interfaceComponents.add(getTextField(equipment.getManufacturer()));
		interfaceComponents.add(getTextField(equipment.getModel()));
		final JTextArea textFieldDescription = getTextArea(equipment
				.getDescription());
		textFieldDescription.setToolTipText(INTERFACE_DESCRIPTION_TOOLTIP);
		interfaceComponents.add(textFieldDescription);
		return interfaceComponents;
	}

	private List<JComponent> getInterfaceEmptyList() {
		List<JComponent> interfaceComponents = new ArrayList<JComponent>();
		interfaceComponents.add(getTextField(null));
		interfaceComponents.add(getTextField(null));
		interfaceComponents.add(getTextField(null));
		final JTextArea textFieldDescription = getTextArea(null);
		textFieldDescription.setToolTipText(INTERFACE_DESCRIPTION_TOOLTIP);
		interfaceComponents.add(textFieldDescription);
		return interfaceComponents;
	}

	private List getEsiEmptyList() {
		List esiComponentsList = new ArrayList();
		esiComponentsList.add(getComboBox(cvRetriever.getEsiNames(), null)); // 0
		esiComponentsList.add(getComboBox(cvRetriever.getSupplyTypeNames(),
				null)); // 1
		final JTextArea textField = getTextArea(null);
		textField.setToolTipText(ESIPARAMETERS_TOOLTIP); // 2
		esiComponentsList.add(textField);
		// INTERFACES

		List<JComponent> interfaceComponents = new ArrayList<JComponent>();
		interfaceComponents.add(getTextField(null));
		interfaceComponents.add(getTextField(null));
		interfaceComponents.add(getTextField(null));
		final JTextArea textFieldDescription = getTextArea(null);
		textFieldDescription.setToolTipText(INTERFACE_DESCRIPTION_TOOLTIP);
		interfaceComponents.add(textFieldDescription);

		esiComponentsList.add(interfaceComponents); // 3
		// SPRAYERS

		List<JComponent> sprayerComponents = new ArrayList<JComponent>();
		sprayerComponents.add(getTextField(null));
		sprayerComponents.add(getTextField(null));
		sprayerComponents.add(getTextField(null));
		final JTextArea textFieldsprayerDescription = getTextArea(null);
		textFieldDescription.setToolTipText(SPRAYER_DESCRIPTION_TOOLTIP);
		sprayerComponents.add(textFieldsprayerDescription);

		esiComponentsList.add(sprayerComponents); // 4

		return esiComponentsList;
	}

	private List<JComponent> getAnalyserComponentList(Analyser analyser) {
		List<JComponent> analyserComponentsList = new ArrayList<JComponent>();

		analyserComponentsList.add(getComboBox(cvRetriever.getAnalyzerNames(),
				analyser.getName()));
		analyserComponentsList.add(getTextArea(analyser.getDescription()));
		final JComboBox comboBox = getComboBox(
				cvRetriever.getRelectronStates(), analyser.getReflectron());
		comboBox.setToolTipText(REFLECTRON_TOOLTIP);
		analyserComponentsList.add(comboBox);
		return analyserComponentsList;
	}

	private List getAnalyzerEmptyList() {
		List analysersList = new ArrayList();

		List<JComponent> analyserComponentsList = new ArrayList<JComponent>();
		analyserComponentsList.add(getComboBox(cvRetriever.getAnalyzerNames(),
				null));
		analyserComponentsList.add(getTextArea(null));
		final JComboBox comboBox = getComboBox(
				cvRetriever.getRelectronStates(), null);
		comboBox.setToolTipText(REFLECTRON_TOOLTIP);
		analyserComponentsList.add(comboBox);
		analysersList.add(analyserComponentsList);

		return analysersList;
	}

	public JPanel getContactPanel() {
		JPanel contactPanel = new JPanel(new GridBagLayout());
		contactPanel.setBorder(new TitledBorder("Contact"));
		GridBagConstraints contactC = getGridBagContraints();

		if (!contact.isEmpty()) {
			// name
			JLabel labelName = new JLabel("Name:");
			contactC.gridx = 0;
			contactPanel.add(labelName, contactC);
			contactC.gridx = 1;
			contactPanel.add(contact.get(0), contactC);
			// last name
			contactC.gridy++;
			JLabel labelLastName = new JLabel("Last name:");
			contactC.gridx = 0;
			contactPanel.add(labelLastName, contactC);
			contactC.gridx = 1;
			contactPanel.add(contact.get(1), contactC);
			// institution
			contactC.gridy++;
			JLabel labelInstitution = new JLabel("Institution:");
			contactC.gridx = 0;
			contactPanel.add(labelInstitution, contactC);
			contactC.gridx = 1;
			contactPanel.add(contact.get(2), contactC);
			// department
			contactC.gridy++;
			JLabel labelDepartment = new JLabel("Department:");
			contactC.gridx = 0;
			contactPanel.add(labelDepartment, contactC);
			contactC.gridx = 1;
			contactPanel.add(contact.get(3), contactC);
			// Position
			contactC.gridy++;
			JLabel labelPosition = new JLabel("Position:");
			contactC.gridx = 0;
			contactPanel.add(labelPosition, contactC);
			contactC.gridx = 1;
			contactPanel.add(contact.get(4), contactC);
			// Email
			contactC.gridy++;
			JLabel labelemail = new JLabel("Email:");
			contactC.gridx = 0;
			contactPanel.add(labelemail, contactC);
			contactC.gridx = 1;
			contactPanel.add(contact.get(5), contactC);
			// Address
			contactC.gridy++;
			JLabel labelAddress = new JLabel("Address:");
			contactC.gridx = 0;
			contactPanel.add(labelAddress, contactC);
			contactC.gridx = 1;
			contactPanel.add(getScrollableComponent(contact.get(6)), contactC);
			// Telephone
			contactC.gridy++;
			JLabel labelTelephone = new JLabel("Tlf:");
			contactC.gridx = 0;
			contactPanel.add(labelTelephone, contactC);
			contactC.gridx = 1;
			contactPanel.add(contact.get(7), contactC);
		}

		return contactPanel;
	}

	public JPanel getSpectrumGenerationPanel(int slide) {

		JPanel spectrumGenerationPanel = new JPanel(new GridBagLayout());
		spectrumGenerationPanel.setBorder(new TitledBorder(
				"4. Spectrum and peak list generation and annotation"));
		GridBagConstraints spectrumGenerationC = getGridBagContraints();

		if (slide == MiapeMSForms.ACQUISITION_SLIDE) {
			JPanel acquisitionsPanel = new JPanel(new GridBagLayout());
			GridBagConstraints acquisitionsC = getGridBagContraints();
			acquisitionsPanel.setBorder(new TitledBorder(
					"4.1 Data acquisition software and parameters"));
			int i = 0;
			for (Object object : acquisitions) {
				List<JComponent> acquisitionComponentList = (List<JComponent>) object;

				JPanel acquisitionPanel = new JPanel(new GridBagLayout());
				acquisitionPanel.setBorder(new TitledBorder("Data acquisition "
						+ (i + 1)));
				GridBagConstraints acquisitionC = getGridBagContraints();
				acquisitionsC.gridy = i;

				// name
				JLabel labelName = new JLabel("Software name:");
				labelName.setToolTipText(ACQUISITION_SOFTWARE_NAME_TOOLTIP);
				acquisitionC.gridx = 0;
				acquisitionPanel.add(labelName, acquisitionC);
				acquisitionC.gridx = 1;
				acquisitionPanel.add(acquisitionComponentList.get(0),
						acquisitionC);

				// version
				acquisitionC.gridy++;
				JLabel labelVersion = new JLabel("Software version:");
				acquisitionC.gridx = 0;
				acquisitionPanel.add(labelVersion, acquisitionC);
				acquisitionC.gridx = 1;
				acquisitionPanel.add(acquisitionComponentList.get(4),
						acquisitionC);

				// software manufacturer
				acquisitionC.gridy++;
				JLabel labelManufacturer = new JLabel("Software manufacturer:");
				acquisitionC.gridx = 0;
				acquisitionPanel.add(labelManufacturer, acquisitionC);
				acquisitionC.gridx = 1;
				acquisitionPanel.add(acquisitionComponentList.get(3),
						acquisitionC);

				// description
				acquisitionC.gridy++;
				JLabel labelDescription = new JLabel("Role of the software:");
				acquisitionC.gridx = 0;
				acquisitionPanel.add(labelDescription, acquisitionC);
				acquisitionC.gridx = 1;
				acquisitionPanel
						.add(getScrollableComponent(acquisitionComponentList
								.get(5)), acquisitionC);

				// parameters
				acquisitionC.gridy++;
				JLabel labelParameters = new JLabel("Acquisition parameters:");
				labelParameters
						.setToolTipText(ACQUISITION_SOFTWARE_NAME_TOOLTIP);
				acquisitionC.gridx = 0;
				acquisitionPanel.add(labelParameters, acquisitionC);
				acquisitionC.gridx = 1;
				acquisitionPanel
						.add(getScrollableComponent(acquisitionComponentList
								.get(1)), acquisitionC);
				// TODO

				// parameter file
				acquisitionC.gridy++;
				JLabel labelParameterFile = new JLabel("URL to parameter file:");
				labelParameterFile
						.setToolTipText(ACQUISITION_PARAMETERS_FILE_TOOLTIP);
				acquisitionC.gridx = 0;
				acquisitionPanel.add(labelParameterFile, acquisitionC);
				acquisitionC.gridx = 1;
				acquisitionPanel.add(acquisitionComponentList.get(2),
						acquisitionC);

				// label SRM
				acquisitionC.gridy++;
				JLabel label = new JLabel(
						"<html><br>For targeted approaches:</html>");
				acquisitionC.gridx = 0;
				acquisitionPanel.add(label, acquisitionC);

				// target list
				acquisitionC.gridy++;
				JLabel labelTargetList = new JLabel("Target list:");
				labelTargetList.setToolTipText(TARGET_LIST_TOOLTIP);
				acquisitionC.gridx = 0;
				acquisitionPanel.add(labelTargetList, acquisitionC);
				acquisitionC.gridx = 1;
				acquisitionPanel
						.add(getScrollableComponent(acquisitionComponentList
								.get(6)), acquisitionC);

				// target list file
				acquisitionC.gridy++;
				JLabel labelTargetListFile = new JLabel(
						"<html>URL to the transition<br>list file:</html>");
				labelTargetListFile.setToolTipText(TARGET_FILE_TOOLTIP);
				acquisitionC.gridx = 0;
				acquisitionPanel.add(labelTargetListFile, acquisitionC);
				acquisitionC.gridx = 1;
				acquisitionPanel.add(acquisitionComponentList.get(7),
						acquisitionC);

				// Add button for remove this acquisition
				final int index = i;
				JButton removeAcquisitionButton = new JButton("Remove");
				removeAcquisitionButton
						.setToolTipText("Remove this acquisition software");
				removeAcquisitionButton
						.addActionListener(new java.awt.event.ActionListener() {
							@Override
							public void actionPerformed(
									java.awt.event.ActionEvent evt) {
								acquisitions.remove(index);
								miapeMSForms
										.showMIAPEdata(MiapeMSForms.ACQUISITION_SLIDE);

							}
						});
				acquisitionC.gridy = 0;
				acquisitionC.gridx = 2;
				acquisitionC.gridheight = 8;
				acquisitionC.anchor = GridBagConstraints.CENTER;
				acquisitionPanel.add(removeAcquisitionButton, acquisitionC);

				acquisitionsPanel.add(acquisitionPanel, acquisitionsC);

				i++;
			}
			// Add button for add more acquisitions
			JButton addAcquisitionButton = new JButton(
					"Add new data acquisition software");
			addAcquisitionButton
					.setToolTipText("Add new data acquisition software");
			addAcquisitionButton
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							final List<JComponent> acquisitionEmptyList = MIAPEMSFormManager.this
									.getAcquisitionEmptyList();
							acquisitions.add(acquisitionEmptyList);
							miapeMSForms
									.showMIAPEdata(MiapeMSForms.ACQUISITION_SLIDE);

						}
					});
			acquisitionsC.gridy++;
			acquisitionsPanel.add(addAcquisitionButton, acquisitionsC);

			spectrumGenerationPanel.add(acquisitionsPanel, spectrumGenerationC);

		} else if (slide == MiapeMSForms.DATA_ANALYSIS_SLIDE) {

			JPanel dataAnalysisesPanel = new JPanel(new GridBagLayout());
			GridBagConstraints dataAnalysisesC = getGridBagContraints();
			dataAnalysisesPanel
					.setBorder(new TitledBorder("4.2 Data Analysis"));
			int i = 0;
			for (Object object : dataAnalysises) {
				List<JComponent> dataAnalysisComponentList = (List<JComponent>) object;

				JPanel dataAnalysisPanel = new JPanel(new GridBagLayout());
				dataAnalysisPanel.setBorder(new TitledBorder("Data analysis "
						+ (i + 1)));
				GridBagConstraints dataAnalysisC = getGridBagContraints();
				dataAnalysisesC.gridy = i;

				// name
				JLabel labelName = new JLabel("Software name:");
				labelName.setToolTipText(DATAANALYSIS_SOFTWARE_NAME_TOOLTIP);
				dataAnalysisC.gridx = 0;
				dataAnalysisPanel.add(labelName, dataAnalysisC);
				dataAnalysisC.gridx = 1;
				dataAnalysisPanel.add(dataAnalysisComponentList.get(0),
						dataAnalysisC);

				// version
				dataAnalysisC.gridy++;
				JLabel labelVersion = new JLabel("Software version:");
				dataAnalysisC.gridx = 0;
				dataAnalysisPanel.add(labelVersion, dataAnalysisC);
				dataAnalysisC.gridx = 1;
				dataAnalysisPanel.add(dataAnalysisComponentList.get(4),
						dataAnalysisC);

				// software manufacturer
				dataAnalysisC.gridy++;
				JLabel labelManufacturer = new JLabel("Software manufacturer:");
				dataAnalysisC.gridx = 0;
				dataAnalysisPanel.add(labelManufacturer, dataAnalysisC);
				dataAnalysisC.gridx = 1;
				dataAnalysisPanel.add(dataAnalysisComponentList.get(3),
						dataAnalysisC);

				// description
				dataAnalysisC.gridy++;
				JLabel labelDescription = new JLabel("Role of the software:");
				dataAnalysisC.gridx = 0;
				dataAnalysisPanel.add(labelDescription, dataAnalysisC);
				dataAnalysisC.gridx = 1;
				dataAnalysisPanel.add(dataAnalysisComponentList.get(5),
						dataAnalysisC);

				// parameters
				dataAnalysisC.gridy++;
				JLabel labelParameters = new JLabel("Parameters:");
				labelParameters.setToolTipText(DATAANALYSIS_PARAMETERS_TOOLTIP);
				dataAnalysisC.gridx = 0;
				dataAnalysisPanel.add(labelParameters, dataAnalysisC);
				dataAnalysisC.gridx = 1;
				dataAnalysisPanel
						.add(getScrollableComponent(dataAnalysisComponentList
								.get(1)), dataAnalysisC);

				// parameter file
				dataAnalysisC.gridy++;
				JLabel labelParameterFile = new JLabel("URL to parameter file:");
				labelParameterFile
						.setToolTipText(DATAANALYSIS_PARAMETERS_FILE_TOOLTIP);
				dataAnalysisC.gridx = 0;
				dataAnalysisPanel.add(labelParameterFile, dataAnalysisC);
				dataAnalysisC.gridx = 1;
				dataAnalysisPanel.add(dataAnalysisComponentList.get(2),
						dataAnalysisC);

				// Add button for remove this acquisition
				final int index = i;
				JButton removeAcquisitionButton = new JButton("Remove");
				removeAcquisitionButton
						.setToolTipText("Remove this data analysis software");
				removeAcquisitionButton
						.addActionListener(new java.awt.event.ActionListener() {
							@Override
							public void actionPerformed(
									java.awt.event.ActionEvent evt) {
								dataAnalysises.remove(index);
								miapeMSForms
										.showMIAPEdata(MiapeMSForms.DATA_ANALYSIS_SLIDE);

							}
						});
				dataAnalysisC.gridy = 0;
				dataAnalysisC.gridx = 2;
				dataAnalysisC.gridheight = 8;
				dataAnalysisC.anchor = GridBagConstraints.CENTER;
				dataAnalysisPanel.add(removeAcquisitionButton, dataAnalysisC);

				dataAnalysisesPanel.add(dataAnalysisPanel, dataAnalysisesC);

				i++;
			}
			// Add button for add more acquisitions
			JButton addDataAnalysisButton = new JButton(
					"Add new data analysis software");
			addDataAnalysisButton
					.setToolTipText("Add new data analysis software");
			addDataAnalysisButton
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							final List<JComponent> dataAnalysisEmptyList = MIAPEMSFormManager.this
									.getDataAnalysisEmptyList();
							dataAnalysises.add(dataAnalysisEmptyList);
							miapeMSForms
									.showMIAPEdata(MiapeMSForms.DATA_ANALYSIS_SLIDE);

						}
					});
			dataAnalysisesC.gridy++;
			dataAnalysisesPanel.add(addDataAnalysisButton, dataAnalysisesC);
			spectrumGenerationPanel.add(dataAnalysisesPanel,
					spectrumGenerationC);
		}
		return spectrumGenerationPanel;
	}

	private Component getScrollableComponent(JComponent jComponent) {
		JScrollPane js = new JScrollPane(jComponent);
		js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		js.setBorder(border);
		js.setPreferredSize(new Dimension(jComponent.getPreferredSize().width,
				70));
		return js;
	}

	public JPanel getInstrumentConfigurationPanel(int slide) {

		JPanel instrumentConfigurationPanel = new JPanel(new GridBagLayout());
		instrumentConfigurationPanel.setBorder(new TitledBorder(
				"Instrument configuration"));
		GridBagConstraints instrumentCgfC = getGridBagContraints();

		if (slide == MiapeMSForms.ANALYZERS_SLIDE) {
			// ANALYZERS
			final List analyzerList = (List) instrumentConfiguration
					.get(ANALYZERS_INDEX);

			JPanel analyzersPanel = new JPanel(new GridBagLayout());
			GridBagConstraints analycersC = getGridBagContraints();
			analyzersPanel.setBorder(new TitledBorder(
					"3. Post source component - 3.1 Analysers"));
			int i = 0;
			for (Object object : analyzerList) {
				List<JComponent> analyserComponentList = (List<JComponent>) object;

				JPanel analyzerPanel = new JPanel(new GridBagLayout());
				analyzerPanel
						.setBorder(new TitledBorder("Analyser " + (i + 1)));
				GridBagConstraints analyzerC = getGridBagContraints();
				analycersC.gridy = i;

				// Analyzer name
				JLabel labelName = new JLabel("Name:");
				analyzerC.gridx = 0;
				analyzerPanel.add(labelName, analyzerC);
				analyzerC.gridx = 1;
				analyzerPanel.add(analyserComponentList.get(0), analyzerC);

				// Analyzer description
				analyzerC.gridy++;
				JLabel labelDescription = new JLabel("Description:");
				analyzerC.gridx = 0;
				analyzerPanel.add(labelDescription, analyzerC);
				analyzerC.gridx = 1;
				analyzerPanel.add(
						getScrollableComponent(analyserComponentList.get(1)),
						analyzerC);

				// Analyzer reflectron state
				analyzerC.gridy++;
				JLabel labelReflectron = new JLabel("Reflectron state:");
				labelReflectron.setToolTipText(REFLECTRON_TOOLTIP);
				analyzerC.gridx = 0;
				analyzerPanel.add(labelReflectron, analyzerC);
				analyzerC.gridx = 1;
				analyzerPanel.add(analyserComponentList.get(2), analyzerC);

				// Add button for remove this analyzer
				final int index = i;
				JButton removeAnalyzerButton = new JButton("Remove");
				removeAnalyzerButton.setToolTipText("Remove this analyzer");
				removeAnalyzerButton
						.addActionListener(new java.awt.event.ActionListener() {
							@Override
							public void actionPerformed(
									java.awt.event.ActionEvent evt) {
								final List analyersListTMP = (List) instrumentConfiguration
										.get(ANALYZERS_INDEX);
								analyersListTMP.remove(index);
								miapeMSForms
										.showMIAPEdata(MiapeMSForms.ANALYZERS_SLIDE);

							}
						});
				analyzerC.gridy = 0;
				analyzerC.gridx = 2;
				analyzerC.gridheight = 3;
				analyzerC.anchor = GridBagConstraints.CENTER;
				analyzerPanel.add(removeAnalyzerButton, analyzerC);

				analyzersPanel.add(analyzerPanel, analycersC);

				i++;
			}

			// Add button for add more analyzers
			JButton addAnalyzerButton = new JButton("Add new analyser");
			addAnalyzerButton.setToolTipText("Add new analyser");
			addAnalyzerButton
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							List<JComponent> analyserComponentListTMP = (List<JComponent>) MIAPEMSFormManager.this
									.getAnalyzerEmptyList().get(0);

							final List analyzerListTMP = (List) instrumentConfiguration
									.get(ANALYZERS_INDEX);

							analyzerListTMP.add(analyserComponentListTMP);

							miapeMSForms
									.showMIAPEdata(MiapeMSForms.ANALYZERS_SLIDE);

						}
					});
			analycersC.gridy++;
			analyzersPanel.add(addAnalyzerButton, analycersC);

			instrumentCgfC.gridy++;
			instrumentConfigurationPanel.add(analyzersPanel, instrumentCgfC);

			// END ANALYSER
		}

		if (slide == MiapeMSForms.ION_SOURCES_SLIDE) {
			instrumentCgfC.gridy++;
			JPanel panTMP = new JPanel();
			panTMP.add(new JLabel("Ion source type:"));
			panTMP.add(ionSourceTypeCombo);
			instrumentConfigurationPanel.add(panTMP, instrumentCgfC);

			if (showEsi) {
				// ESI
				final List esiList = (List) instrumentConfiguration
						.get(ESI_INDEX);

				JPanel esiPanel = new JPanel(new GridBagLayout());
				esiPanel.setBorder(new TitledBorder(
						"2. Ion sources - 2.1 Electrospray Ionisation (ESI)"));
				GridBagConstraints esiC = getGridBagContraints();

				// ESI name
				esiC.gridy = 0;
				JLabel labelName = new JLabel("Name:");
				esiC.gridx = 0;
				esiPanel.add(labelName, esiC);
				esiC.gridx = 1;
				JComboBox name = (JComboBox) esiList.get(0);
				esiPanel.add(name, esiC);

				// ESI supply type
				esiC.gridy = 1;
				JLabel labelSupplyType = new JLabel("Supply type:");
				esiC.gridx = 0;
				esiPanel.add(labelSupplyType, esiC);
				esiC.gridx = 1;
				JComboBox supplyType = (JComboBox) esiList.get(1);
				esiPanel.add(supplyType, esiC);

				// ESI parameters
				esiC.gridy = 2;
				JLabel labelParameters = new JLabel("Other parameters:");
				labelParameters.setToolTipText(ESIPARAMETERS_TOOLTIP);
				esiC.gridx = 0;
				esiPanel.add(labelParameters, esiC);
				esiC.gridx = 1;
				JTextArea parameters = (JTextArea) esiList.get(2);
				esiPanel.add(getScrollableComponent(parameters), esiC);

				// Interfaces
				List<JComponent> interfaceComponents = (List<JComponent>) esiList
						.get(3);

				JPanel interfacePanel = new JPanel(new GridBagLayout());
				interfacePanel.setBorder(new TitledBorder("ESI interface"));
				GridBagConstraints interfaceC = getGridBagContraints();

				// interface name
				JLabel labelInterfaceName = new JLabel("Name:");
				interfacePanel.add(labelInterfaceName, interfaceC);
				interfaceC.gridx = 1;
				interfacePanel.add(interfaceComponents.get(0), interfaceC);

				// interface manufacturer
				JLabel labelInterfaceManufacturer = new JLabel("Manufacturer:");
				interfaceC.gridx = 0;
				interfaceC.gridy++;
				interfacePanel.add(labelInterfaceManufacturer, interfaceC);
				interfaceC.gridx = 1;
				interfacePanel.add(interfaceComponents.get(1), interfaceC);

				// interface model
				JLabel labelInterfaceModel = new JLabel("Model:");
				interfaceC.gridx = 0;
				interfaceC.gridy++;
				interfacePanel.add(labelInterfaceModel, interfaceC);
				interfaceC.gridx = 1;
				interfacePanel.add(interfaceComponents.get(2), interfaceC);

				// interface description
				JLabel labelInterfaceDecription = new JLabel("Description:");
				interfaceC.gridx = 0;
				interfaceC.gridy++;
				interfacePanel.add(labelInterfaceDecription, interfaceC);
				interfaceC.gridx = 1;
				interfacePanel.add(
						getScrollableComponent(interfaceComponents.get(3)),
						interfaceC);

				esiC.gridy++;
				esiPanel.add(interfacePanel, esiC);

				// Sprayers
				List<JComponent> sprayerComponents = (List<JComponent>) esiList
						.get(4);

				JPanel sprayerPanel = new JPanel(new GridBagLayout());
				sprayerPanel.setBorder(new TitledBorder("ESI sprayer"));
				GridBagConstraints sprayerC = getGridBagContraints();

				// interface name
				JLabel labelSprayerName = new JLabel("Name:");
				sprayerPanel.add(labelSprayerName, sprayerC);
				sprayerC.gridx = 1;
				sprayerPanel.add(sprayerComponents.get(0), sprayerC);

				// Sprayer manufacturer
				JLabel labelSprayerManufacturer = new JLabel("Manufacturer:");
				sprayerC.gridx = 0;
				sprayerC.gridy++;
				sprayerPanel.add(labelSprayerManufacturer, sprayerC);
				sprayerC.gridx = 1;
				sprayerPanel.add(sprayerComponents.get(1), sprayerC);

				// Sprayer model
				JLabel labelSprayerModel = new JLabel("Model:");
				sprayerC.gridx = 0;
				sprayerC.gridy++;
				sprayerPanel.add(labelSprayerModel, sprayerC);
				sprayerC.gridx = 1;
				sprayerPanel.add(sprayerComponents.get(2), sprayerC);

				// Sprayer description
				JLabel labelSprayerDecription = new JLabel("Description:");
				sprayerC.gridx = 0;
				sprayerC.gridy++;
				sprayerPanel.add(labelSprayerDecription, sprayerC);
				sprayerC.gridx = 1;
				sprayerPanel.add(
						getScrollableComponent(sprayerComponents.get(3)),
						sprayerC);

				esiC.gridy++;
				esiPanel.add(sprayerPanel, esiC);

				instrumentCgfC.gridy++;
				instrumentConfigurationPanel.add(esiPanel, instrumentCgfC);

				// END ESI
			}
			if (showMaldi) {
				// MALDI
				final List<JComponent> maldiList = (List<JComponent>) instrumentConfiguration
						.get(MALDI_INDEX);

				JPanel maldisPanel = new JPanel(new GridBagLayout());
				maldisPanel.setBorder(new TitledBorder(
						"2. Ion sources - 2.2 MALDI"));
				GridBagConstraints maldiC = getGridBagContraints();

				// maldi name
				JLabel labelNameMALDI = new JLabel("Name:");
				maldiC.gridx = 0;
				maldisPanel.add(labelNameMALDI, maldiC);
				maldiC.gridx = 1;
				maldisPanel.add(maldiList.get(0), maldiC);
				// maldi plate
				maldiC.gridy++;
				JLabel labelPlateMALDI = new JLabel(
						"Plate composition (or type):");
				maldiC.gridx = 0;
				maldisPanel.add(labelPlateMALDI, maldiC);
				maldiC.gridx = 1;
				maldisPanel.add(maldiList.get(1), maldiC);
				// maldi matrix
				maldiC.gridy++;
				JLabel labelMatrixMALDI = new JLabel(
						"Matrix composition (if applicable):");
				maldiC.gridx = 0;
				maldisPanel.add(labelMatrixMALDI, maldiC);
				maldiC.gridx = 1;
				maldisPanel.add(maldiList.get(2), maldiC);
				// maldi PSD
				maldiC.gridy++;
				JLabel labelPSDMALDI = new JLabel("PSD (or LID/ISD):");
				labelPSDMALDI.setToolTipText(MALDI_DISS_TOOLTIP);
				maldiC.gridx = 0;
				maldisPanel.add(labelPSDMALDI, maldiC);
				maldiC.gridx = 1;
				maldisPanel.add(maldiList.get(3), maldiC);
				// maldi PSD description
				maldiC.gridy++;
				JLabel labelPSDSummaryMALDI = new JLabel(
						"PSD (or LID/ISD) description:");
				maldiC.gridx = 0;
				maldisPanel.add(labelPSDSummaryMALDI, maldiC);
				maldiC.gridx = 1;
				maldisPanel.add(getScrollableComponent(maldiList.get(4)),
						maldiC);
				// maldi extraction
				maldiC.gridy++;
				JLabel labelExtractionMALDI = new JLabel(
						"Operation with delayed extraction:");
				labelExtractionMALDI.setToolTipText(EXTRACTION_TOOLTIP);
				maldiC.gridx = 0;
				maldisPanel.add(labelExtractionMALDI, maldiC);
				maldiC.gridx = 1;
				maldisPanel.add(maldiList.get(5), maldiC);
				// maldi laesr type
				maldiC.gridy++;
				JLabel labelLaserTypeMALDI = new JLabel("Laser type:");
				maldiC.gridx = 0;
				maldisPanel.add(labelLaserTypeMALDI, maldiC);
				maldiC.gridx = 1;
				maldisPanel.add(maldiList.get(6), maldiC);

				// maldi laesr wavelength
				maldiC.gridy++;
				JLabel labelLaserWaveMALDI = new JLabel("Wavelength (nm):");
				labelLaserWaveMALDI.setToolTipText(LASER_WAVELENGTH_TOOLTIP);
				maldiC.gridx = 0;
				maldisPanel.add(labelLaserWaveMALDI, maldiC);
				maldiC.gridx = 1;
				maldisPanel.add(maldiList.get(7), maldiC);

				// maldi parameters
				maldiC.gridy++;
				JLabel labelLaserParametersMALDI = new JLabel(
						"Other laser related parameters:");
				labelLaserParametersMALDI
						.setToolTipText(LASER_PARAMETERS_TOOLTIP);
				maldiC.gridx = 0;
				maldisPanel.add(labelLaserParametersMALDI, maldiC);
				maldiC.gridx = 1;
				maldisPanel.add(getScrollableComponent(maldiList.get(8)),
						maldiC);

				instrumentCgfC.gridy++;
				instrumentConfigurationPanel.add(maldisPanel, instrumentCgfC);
				// END MALDI
			}
			if (showOther) {
				// MALDI
				final List<JComponent> otherIonSourceList = (List<JComponent>) instrumentConfiguration
						.get(OTHER_ION_SOURCE_INDEX);

				JPanel otherPanel = new JPanel(new GridBagLayout());
				otherPanel.setBorder(new TitledBorder(
						"2. Ion sources - 2.3 Other ion source"));
				GridBagConstraints otherC = getGridBagContraints();

				// name
				JLabel labelNameOtherSource = new JLabel("Name:");
				otherC.gridx = 0;
				otherPanel.add(labelNameOtherSource, otherC);
				otherC.gridx = 1;
				otherPanel.add(otherIonSourceList.get(0), otherC);

				// description
				otherC.gridy++;
				JLabel labelParametersOtherSource = new JLabel(
						"Relevant params:");
				labelParametersOtherSource
						.setToolTipText(OTHER_ION_SOURCE_TOOLTIP);
				otherC.gridx = 0;
				otherPanel.add(labelParametersOtherSource, otherC);
				otherC.gridx = 1;
				otherPanel.add(otherIonSourceList.get(1), otherC);

				instrumentCgfC.gridy++;
				instrumentConfigurationPanel.add(otherPanel, instrumentCgfC);
			}
		}
		if (slide == MiapeMSForms.ACTIVATION_SLIDE) {
			// ACTIVATION
			final List<JComponent> activationComponentList = (List<JComponent>) instrumentConfiguration
					.get(ACTIVATION_INDEX);

			JPanel activationPanel = new JPanel(new GridBagLayout());
			activationPanel
					.setBorder(new TitledBorder(
							"3. Post source component - 3.2 Activation / dissociation"));
			GridBagConstraints activationC = getGridBagContraints();

			// name
			JLabel labelInstrumentComponentActivation = new JLabel(
					"Instrument component:");
			labelInstrumentComponentActivation
					.setToolTipText(ACTIVATION_NAME_TOOLTIP);
			activationC.gridx = 0;
			activationPanel
					.add(labelInstrumentComponentActivation, activationC);
			activationC.gridx = 1;
			activationPanel.add(activationComponentList.get(0), activationC);

			// activation type
			activationC.gridy++;
			JLabel labelActivationType = new JLabel(
					"Activation / dissociation type:");
			labelActivationType.setToolTipText(ACTIVATION_TYPE_TOOLTIP);
			activationC.gridx = 0;
			activationPanel.add(labelActivationType, activationC);
			activationC.gridx = 1;
			activationPanel.add(activationComponentList.get(4), activationC);

			// gas type
			activationC.gridy++;
			JLabel labelGasType = new JLabel("Gas type:");
			labelGasType.setToolTipText(GAS_TYPE_TOOLTIP);
			activationC.gridx = 0;
			activationPanel.add(labelGasType, activationC);
			activationC.gridx = 1;
			activationPanel.add(activationComponentList.get(1), activationC);

			// gas pressure
			activationC.gridy++;
			JLabel labelGasPressure = new JLabel("Gas pressure:");
			labelGasType.setToolTipText(GAS_PRESSURE_TOOLTIP);
			activationC.gridx = 0;
			activationPanel.add(labelGasPressure, activationC);
			activationC.gridx = 1;
			activationPanel.add(activationComponentList.get(2), activationC);

			// gas pressure units
			activationC.gridy++;
			JLabel labelGasPressureUnits = new JLabel("Pressure units:");
			labelGasType.setToolTipText(GAS_PRESSURE_TOOLTIP);
			activationC.gridx = 0;
			activationPanel.add(labelGasPressureUnits, activationC);
			activationC.gridx = 1;
			activationPanel.add(activationComponentList.get(3), activationC);

			instrumentCgfC.gridy++;
			instrumentConfigurationPanel.add(activationPanel, instrumentCgfC);
			// END ACTIVATION
		}
		return instrumentConfigurationPanel;
	}

	public JPanel getSpectrometerPanel() {

		JPanel ret = new JPanel(new GridBagLayout());
		GridBagConstraints cRet = getGridBagContraints();

		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(new TitledBorder("Spectrometer"));
		GridBagConstraints c = getGridBagContraints();

		// Spectrometer name
		JLabel labelName = new JLabel("Name:");
		panel.add(labelName, c);
		c.gridx = 1;
		panel.add(spectrometer.get(0), c);

		// Spectrometer manufacturer
		c.gridx = 0;
		c.gridy++;
		JLabel labelManufacturer = new JLabel("Manufacturer:");
		panel.add(labelManufacturer, c);
		c.gridx = 1;
		panel.add(spectrometer.get(1), c);

		// Spectrometer customization
		c.gridx = 0;
		c.gridy++;
		JLabel labelCustomization = new JLabel("customizations:");
		labelCustomization.setToolTipText(CUSTOMIZATIONS_TOOLTIP);
		panel.add(labelCustomization, c);
		c.gridx = 1;
		panel.add(getScrollableComponent(spectrometer.get(2)), c);

		// Spectrometer version
		c.gridx = 0;
		c.gridy++;
		JLabel labelVersion = new JLabel("version:");
		panel.add(labelVersion, c);
		c.gridx = 1;
		panel.add(spectrometer.get(3), c);

		cRet.gridx = 0;
		ret.add(panel, cRet);

		cRet.gridy++;

		return ret;
	}

	public JPanel getMiapeNamePanel() {

		JPanel ret = new JPanel(new GridBagLayout());

		GridBagConstraints c = getGridBagContraints();
		JLabel labelName = new JLabel("Miape Name:");
		ret.add(labelName, c);
		c.gridx = 1;
		ret.add(miapeName, c);
		return ret;

	}

	private JComboBox getComboBox(List<String> list, String name) {
		if (name == null)
			name = "";
		if (!list.contains(name))
			list.add(0, name);
		ComboBoxModel aModel = new DefaultComboBoxModel(list.toArray());
		aModel.setSelectedItem(name);
		JComboBox ret = new JComboBox(aModel);
		return ret;
	}

	public static GridBagConstraints getGridBagContraints() {
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.NORTHWEST;

		return c;
	}

	private JTextField getTextField(String text) {
		int size = TEXT_FIELD_COLUMNS;
		if (text != null && !"".equals(text)) {
			if (text.length() > size)
				size = text.length();

		}

		if (text == null)
			text = "";
		log.info("Getting text field: " + text);
		final JTextField ret = new JTextField(text);
		ret.setColumns(TEXT_FIELD_COLUMNS);
		// ret.setPreferredSize(dimension);
		// final Dimension size2 = ret.getPreferredSize();
		return ret;
	}

	private JTextArea getTextArea(String text) {
		log.info("Getting text area: " + text);

		JTextArea ret = new JTextArea(text);
		ret.setFont(font);
		ret.setBorder(border);
		ret.setRows(TEXTAREA_ROWS);
		ret.setColumns(TEXTAREA_COLUMNS);
		ret.setAutoscrolls(true);
		ret.setBorder(null);
		ret.setLineWrap(true);
		ret.setWrapStyleWord(true);
		return ret;
	}

	private JCheckBox getCheckBox(String text, boolean selected) {
		log.info("Getting check box: " + text);
		JCheckBox ret = new JCheckBox(text);
		ret.setSelected(selected);
		return ret;
	}

	public JPanel getAddInfoPanel() {
		JPanel addInfosPanel = new JPanel(new GridBagLayout());
		addInfosPanel.setBorder(new TitledBorder("Additional information"));
		GridBagConstraints addInfosC = getGridBagContraints();

		// SAMPLE IFNORMATION
		JPanel samplePanel = new JPanel(new GridBagLayout());
		GridBagConstraints sampleC = getGridBagContraints();
		samplePanel.setBorder(new TitledBorder("Sample information"));

		// Sample Name
		JLabel labelSampleName = new JLabel(
				"<html>Name(s) /<br>Identifier(s):</html>");
		sampleC.gridx = 0;
		samplePanel.add(labelSampleName, sampleC);
		sampleC.gridx = 1;
		samplePanel.add(getScrollableComponent(addInfos.get(0)), sampleC);

		// Sample Volume
		sampleC.gridy++;
		JLabel labelSampleVolume = new JLabel("Amount(s)/Volume(s):");
		sampleC.gridx = 0;
		samplePanel.add(labelSampleVolume, sampleC);
		sampleC.gridx = 1;
		samplePanel.add(addInfos.get(1), sampleC);

		// Sample tissue
		sampleC.gridy++;
		JLabel labelSampleTissue = new JLabel("Tissue:");
		sampleC.gridx = 0;
		samplePanel.add(labelSampleTissue, sampleC);
		sampleC.gridx = 1;
		samplePanel.add(addInfos.get(2), sampleC);

		// cell type
		sampleC.gridy++;
		JLabel labelCellType = new JLabel("Cell type:");
		sampleC.gridx = 0;
		samplePanel.add(labelCellType, sampleC);
		sampleC.gridx = 1;
		samplePanel.add(addInfos.get(3), sampleC);

		// Sample disease
		sampleC.gridy++;
		JLabel labelSampleDisease = new JLabel("Dissease:");
		sampleC.gridx = 0;
		samplePanel.add(labelSampleDisease, sampleC);
		sampleC.gridx = 1;
		samplePanel.add(addInfos.get(4), sampleC);
		addInfosPanel.add(samplePanel, addInfosC);

		// taxonomy
		sampleC.gridy++;
		JLabel labelTaxo = new JLabel("Sample taxonomy:");
		sampleC.gridx = 0;
		samplePanel.add(labelTaxo, sampleC);
		sampleC.gridx = 1;
		samplePanel.add(addInfos.get(5), sampleC);
		addInfosPanel.add(samplePanel, addInfosC);

		// SAMPLE PREPS
		// END SAMPLE INFORMATION
		if (!samplePreps.isEmpty()) {
			for (int i = 0; i < samplePreps.size(); i++) {

				JPanel addInfoPanel = new JPanel(new GridBagLayout());
				addInfoPanel.setBorder(new TitledBorder(
						"Sample preparation steps"));
				GridBagConstraints addInfoC = getGridBagContraints();

				// name
				JLabel labelName = new JLabel("Name:");
				addInfoC.gridx = 0;
				addInfoPanel.add(labelName, addInfoC);
				addInfoC.gridx = 1;
				addInfoC.gridwidth = 2;
				addInfoPanel.add(samplePreps.get(i), addInfoC);
				addInfoC.gridwidth = 1;
				// value
				addInfoC.gridy++;
				JLabel labelValue = new JLabel("Value (optional):");
				addInfoC.gridx = 0;
				addInfoPanel.add(labelValue, addInfoC);
				addInfoC.gridx++;
				addInfoPanel.add(
						getScrollableComponent(samplePreps.get(i + 1)),
						addInfoC);

				// Add button for remove this add info
				final int index = i;
				JButton removeSamplePrepButton = new JButton("Remove");
				removeSamplePrepButton
						.setToolTipText("Remove this sample preparation step");
				removeSamplePrepButton
						.addActionListener(new java.awt.event.ActionListener() {
							@Override
							public void actionPerformed(
									java.awt.event.ActionEvent evt) {
								samplePreps.remove(index);
								samplePreps.remove(index);
								miapeMSForms
										.showMIAPEdata(MiapeMSForms.ADD_INFO_SLIDE);

							}
						});

				addInfoC.gridx++;

				// addInfoC.anchor = GridBagConstraints.CENTER;
				addInfoPanel.add(removeSamplePrepButton, addInfoC);
				addInfoC.gridx = 0;
				addInfosC.gridy++;
				addInfosPanel.add(addInfoPanel, addInfosC);
				i++;
			}
		}
		// Add button for add more add info
		JButton addSamplePrepButton = new JButton("Add new sample prep. step");
		addSamplePrepButton.setToolTipText("Add new sample preparation step");
		addSamplePrepButton
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						// name
						samplePreps.add(getComboBox(
								cvRetriever.getSampleProcessingSteps(), null));
						// others value
						samplePreps.add(getTextArea(null));
						miapeMSForms.showMIAPEdata(MiapeMSForms.ADD_INFO_SLIDE);

					}
				});
		addInfosC.gridy++;
		addInfosPanel.add(addSamplePrepButton, addInfosC);

		// Additional informations
		if (addInfos.size() > 6) {
			for (int i = 6; i < addInfos.size(); i++) {

				JPanel addInfoPanel = new JPanel(new GridBagLayout());
				addInfoPanel.setBorder(new TitledBorder(
						"Additional information"));
				GridBagConstraints addInfoC = getGridBagContraints();

				// name
				JLabel labelName = new JLabel("Name:");
				addInfoC.gridx = 0;
				addInfoPanel.add(labelName, addInfoC);
				addInfoC.gridx = 1;
				addInfoC.gridwidth = 2;
				addInfoPanel.add(addInfos.get(i), addInfoC);
				addInfoC.gridwidth = 1;
				// value
				addInfoC.gridy++;
				JLabel labelValue = new JLabel("Value (optional):");
				addInfoC.gridx = 0;
				addInfoPanel.add(labelValue, addInfoC);
				addInfoC.gridx++;
				addInfoPanel.add(getScrollableComponent(addInfos.get(i + 1)),
						addInfoC);

				// Add button for remove this add info
				final int index = i;
				JButton removeAddInfoButton = new JButton("Remove");
				removeAddInfoButton
						.setToolTipText("Remove this additional information");
				removeAddInfoButton
						.addActionListener(new java.awt.event.ActionListener() {
							@Override
							public void actionPerformed(
									java.awt.event.ActionEvent evt) {
								addInfos.remove(index);
								addInfos.remove(index);
								miapeMSForms
										.showMIAPEdata(MiapeMSForms.ADD_INFO_SLIDE);

							}
						});

				addInfoC.gridx++;

				// addInfoC.anchor = GridBagConstraints.CENTER;
				addInfoPanel.add(removeAddInfoButton, addInfoC);
				addInfoC.gridx = 0;
				addInfosC.gridy++;
				addInfosPanel.add(addInfoPanel, addInfosC);
				i++;
			}
		}
		// Add button for add more add info
		JButton addAddInfoButton = new JButton("Add new add. info.");
		addAddInfoButton.setToolTipText("Add new additional information");
		addAddInfoButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				// name
				addInfos.add(getComboBox(cvRetriever.getAddInformations(), null));
				// others value
				addInfos.add(getTextArea(null));
				miapeMSForms.showMIAPEdata(MiapeMSForms.ADD_INFO_SLIDE);

			}
		});
		addInfosC.gridy++;
		addInfosPanel.add(addAddInfoButton, addInfosC);

		return addInfosPanel;

	}

	private int getComponentIndex(List addInfoList, String controlVocabularyName) {
		int i = 0;
		for (Object object : addInfoList) {
			List<JComponent> componentList = (List<JComponent>) object;
			JComponent jComponentName = componentList.get(0);
			JComponent jComponentValue = componentList.get(1);
			if (jComponentName instanceof JComboBox) {
				if (((JComboBox) jComponentName).getSelectedItem().equals(
						controlVocabularyName)) {
					return i;
				}
			}
			i++;
		}
		return -1;
	}
}
