package org.proteored.pacom.gui.miapemsforms;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.cv.ControlVocabularyTerm;
import org.proteored.miapeapi.cv.MainTaxonomies;
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
import org.proteored.miapeapi.xml.ms.MIAPEMSXmlFile;
import org.proteored.miapeapi.xml.ms.MiapeMSXmlFactory;
import org.proteored.pacom.analysis.util.FileManager;
import org.proteored.pacom.gui.tasks.OntologyLoaderTask;

public class MetadataLoader extends SwingWorker<String, Void> {
	public static final String METADATA_READED = "metadata_readed";
	private final String metadataFileName;
	private static ControlVocabularyManager cvManager;

	public MetadataLoader(String metadataFileName) {
		this.metadataFileName = metadataFileName;
		MetadataLoader.cvManager = OntologyLoaderTask.getCvManager();
	}

	@Override
	protected String doInBackground() throws Exception {

		MiapeMSDocument metadataMiapeMS = getMiapeMSFromSelection(metadataFileName);
		if (metadataMiapeMS != null)
			return getTextFromMIAPE(metadataMiapeMS);

		return null;
	}

	private static MiapeMSDocument getMiapeMSFromSelection(
			String metadataFileName) {
		try {
			if (metadataFileName != null && !"".equals(metadataFileName)) {
				final File metadataFile = FileManager
						.getMetadataFile(metadataFileName);
				if (metadataFile != null) {
					MiapeMSDocument metadataMiapeMS = MiapeMSXmlFactory
							.getFactory().toDocument(
									new MIAPEMSXmlFile(metadataFile),
									OntologyLoaderTask.getCvManager(), null,
									null, null);
					return metadataMiapeMS;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String getTextFromMIAPE(MiapeMSDocument metadataMiapeMS) {
		StringBuilder sb = new StringBuilder();
		if (metadataMiapeMS != null) {
			sb.append("<html>");
			MSContact contact = metadataMiapeMS.getContact();
			if (contact != null) {
				sb.append("<b>Contact</b>: '" + contact.getName());
				String lastName = contact.getLastName();
				if (lastName != null && !"".equals(lastName))
					sb.append(" " + lastName);
				sb.append("'<br>");
				String institution = contact.getInstitution();
				if (institution != null && !"".equals(institution))
					sb.append("<b>Institution: '</b>" + institution + "'<br>");
				String department = contact.getDepartment();
				if (department != null && !"".equals(department))
					sb.append("<b>Department: '</b>" + department + "'<br>");
			} else {
				sb.append("<b>Contact</b>: '<font color='red'>not defined</font>'<br>");
			}
			String specie = getSpecieFromAdditionalInformations(metadataMiapeMS
					.getAdditionalInformations());
			if (specie != null & !"".equals(specie)) {
				sb.append("<b>Specie</b>: '" + specie + "'<br>");
			} else {
				sb.append("<b>Specie</b>: '<font color='red'>not defined</font>' <font color='red'><b>(REQUIRED FOR PX SUBMISSION)</b></font><br>");
			}

			final Set<Spectrometer> spectrometers = metadataMiapeMS
					.getSpectrometers();
			if (spectrometers != null && !spectrometers.isEmpty()) {
				final Spectrometer spectrometer = spectrometers.iterator()
						.next();
				sb.append("<b>Spectrometer</b>: '" + spectrometer.getName()
						+ "'");
				final String manufacturer = spectrometer.getManufacturer();
				if (manufacturer != null && !"".equals(manufacturer))
					sb.append(" / '" + manufacturer + "'");
				sb.append("<br>");
			} else {
				sb.append("<b>Spectrometer</b>: '<font color='red'>not defined</font>'<br>");
			}
			final List<InstrumentConfiguration> instrumentConfigurations = metadataMiapeMS
					.getInstrumentConfigurations();
			if (instrumentConfigurations != null
					&& !instrumentConfigurations.isEmpty()) {
				final InstrumentConfiguration instrumentConfiguration = instrumentConfigurations
						.get(0);
				final List<Analyser> analyzers = instrumentConfiguration
						.getAnalyzers();
				if (analyzers != null && !analyzers.isEmpty()) {
					sb.append("<b>" + analyzers.size() + " analyzers</b>: ");
					for (int i = 0; i < analyzers.size(); i++) {
						sb.append("'" + analyzers.get(i).getName() + "'");
						if (i != analyzers.size() - 1)
							sb.append(" / ");
					}
					sb.append("<br>");
				}
				final List<Esi> esis = instrumentConfiguration.getEsis();
				if (esis != null && !esis.isEmpty()) {
					Esi esi = esis.get(0);
					sb.append("<b>Ion source ESI</b>: '" + esi.getName()
							+ "'<br>");
					if (esi.getInterfaces() != null
							&& !esi.getInterfaces().isEmpty()) {
						sb.append("<b>ESI interface</b>: '"
								+ esi.getInterfaces().iterator().next()
										.getName() + "'<br>");
					} else {
						sb.append("<b>ESI interface</b>:  '<font color='red'>not defined</font>'<br>");
					}
					if (esi.getSprayers() != null
							&& !esi.getSprayers().isEmpty()) {
						sb.append("<b>ESI sprayer</b>: '"
								+ esi.getSprayers().iterator().next().getName()
								+ "'<br>");
					} else {
						sb.append("<b>ESI sprayer</b>:  '<font color='red'>not defined</font>'<br>");
					}
				}
				final List<Maldi> maldis = instrumentConfiguration.getMaldis();
				if (maldis != null && !maldis.isEmpty()) {
					Maldi maldi = maldis.get(0);
					sb.append("<b>Ion source MALDI</b>: '" + maldi.getName()
							+ "'<br>");
				}

				final List<Other_IonSource> other = instrumentConfiguration
						.getOther_IonSources();
				if (other != null && !other.isEmpty()) {
					sb.append("<b>Ion source</b>: '" + other.get(0).getName()
							+ "'<br>");
				}
				if ((esis == null && maldis == null && other == null)
						|| (esis.isEmpty() && maldis.isEmpty() && other
								.isEmpty())) {
					sb.append("<b>Ion source</b>: '<font color='red'>not defined</font>'<br>");
				}
				final Set<ActivationDissociation> activationDissociations = instrumentConfiguration
						.getActivationDissociations();
				if (activationDissociations != null
						&& !activationDissociations.isEmpty())
					sb.append("<b>Activation</b>: '"
							+ activationDissociations.iterator().next()
									.getActivationType() + "'<br>");
				else {
					sb.append("<b>Activation</b>: '<font color='red'>not defined</font>'<br>");
				}
			} else {
				sb.append("<b>Instrument configuration</b>: '<font color='red'>not defined</font>'<br>");
			}
			final Set<Acquisition> acquisitions = metadataMiapeMS
					.getAcquisitions();
			if (acquisitions != null && !acquisitions.isEmpty()) {
				for (Acquisition acquisition : acquisitions) {
					sb.append("<b>Acquisition software</b>: '"
							+ acquisition.getName() + "'<br>");
				}

			} else {
				sb.append("<b>Acquisition software</b>: '<font color='red'>not defined</font>'<br>");
			}
			final Set<DataAnalysis> dataAnalysis = metadataMiapeMS
					.getDataAnalysis();
			if (dataAnalysis != null && !dataAnalysis.isEmpty()) {
				for (DataAnalysis dataAnalysis2 : dataAnalysis) {

					sb.append("<b>Data analysis software</b>: '"
							+ dataAnalysis2.getName() + "'<br>");
				}

			} else {
				sb.append("<b>Data analysis software</b>: '<font color='red'>not defined</font>'<br>");
			}

			sb.append("</html>");
		}

		return sb.toString();
	}

	private static String getSpecieFromAdditionalInformations(
			List<MSAdditionalInformation> additionalInformations) {
		if (additionalInformations != null) {
			for (MSAdditionalInformation msAdditionalInformation : additionalInformations) {
				final ControlVocabularyTerm cvTerm = MainTaxonomies
						.getInstance(cvManager).getCVTermByPreferredName(
								msAdditionalInformation.getName());
				if (cvTerm != null)
					return cvTerm.getPreferredName();
				final String value = msAdditionalInformation.getValue();
				if (value != null) {
					final ControlVocabularyTerm cvTerm2 = MainTaxonomies
							.getInstance(cvManager).getCVTermByPreferredName(
									value);
					if (cvTerm2 != null)
						return cvTerm2.getPreferredName();
				}
			}
		}
		return null;
	}

	@Override
	protected void done() {
		try {
			final String string = get();
			firePropertyChange(METADATA_READED, null, string);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public static String getMetadataString(String selectedItem) {
		return getTextFromMIAPE(getMiapeMSFromSelection(selectedItem));
	}

}
