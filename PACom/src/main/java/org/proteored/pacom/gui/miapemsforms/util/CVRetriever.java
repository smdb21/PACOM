package org.proteored.pacom.gui.miapemsforms.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.proteored.miapeapi.cv.AdditionalInformationName;
import org.proteored.miapeapi.cv.CellTypes;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.cv.ControlVocabularyTerm;
import org.proteored.miapeapi.cv.HumanDisseases;
import org.proteored.miapeapi.cv.MainTaxonomies;
import org.proteored.miapeapi.cv.SampleInformation;
import org.proteored.miapeapi.cv.SampleProcessingStep;
import org.proteored.miapeapi.cv.TissuesTypes;
import org.proteored.miapeapi.cv.ms.AcquisitionSoftware;
import org.proteored.miapeapi.cv.ms.ContactPositionMS;
import org.proteored.miapeapi.cv.ms.DataProcessingAction;
import org.proteored.miapeapi.cv.ms.DataProcessingSoftware;
import org.proteored.miapeapi.cv.ms.DissociationMethod;
import org.proteored.miapeapi.cv.ms.EsiName;
import org.proteored.miapeapi.cv.ms.GasType;
import org.proteored.miapeapi.cv.ms.InstrumentModel;
import org.proteored.miapeapi.cv.ms.InstrumentVendor;
import org.proteored.miapeapi.cv.ms.IonSourceName;
import org.proteored.miapeapi.cv.ms.LaserType;
import org.proteored.miapeapi.cv.ms.MaldiDissociationMethod;
import org.proteored.miapeapi.cv.ms.MassAnalyzerType;
import org.proteored.miapeapi.cv.ms.PressureUnit;
import org.proteored.miapeapi.cv.ms.ReflectronState;
import org.proteored.miapeapi.cv.ms.SamplePlateType;
import org.proteored.miapeapi.cv.ms.SupplyType;
import org.proteored.miapeapi.experiment.model.sort.SorterUtil;

import gnu.trove.set.hash.THashSet;

public class CVRetriever {
	private final ControlVocabularyManager cvManager;
	private final List<String> addInfos = new ArrayList<String>();

	public CVRetriever(ControlVocabularyManager cvManager) {
		this.cvManager = cvManager;
	}

	private List<String> getNames(List<ControlVocabularyTerm> terms) {
		List<String> ret = new ArrayList<String>();
		Set<String> names = new THashSet<String>(); // not allow repeated values
		if (terms != null) {
			for (ControlVocabularyTerm controlVocabularyTerm : terms) {
				final String preferredName = controlVocabularyTerm.getPreferredName();
				if (preferredName.length() < 70 && !names.contains(preferredName)) {
					names.add(preferredName);
					ret.add(preferredName);
				}
			}
		}
		SorterUtil.sortStringNoCaseSensitive(ret);
		return ret;
	}

	public List<String> getAnalyzerNames() {
		final List<ControlVocabularyTerm> analyzerNames = MassAnalyzerType.getInstance(cvManager).getPossibleValues();
		return getNames(analyzerNames);
	}

	public List<String> getManufacturerNames() {
		final List<ControlVocabularyTerm> possibleValues = InstrumentVendor.getInstance(cvManager).getPossibleValues();
		return getNames(possibleValues);

	}

	public List<String> getInstrumentNames() {

		final List<ControlVocabularyTerm> instrumentNames = InstrumentModel.getInstance(cvManager).getPossibleValues();
		return getNames(instrumentNames);

	}

	public List<String> getRelectronStates() {
		final List<ControlVocabularyTerm> reflectronStates = ReflectronState.getInstance(cvManager).getPossibleValues();
		return getNames(reflectronStates);
	}

	public List<String> getEsiNames() {
		final List<ControlVocabularyTerm> esiNames = EsiName.getInstance(cvManager).getPossibleValues();
		return getNames(esiNames);
	}

	public List<String> getSupplyTypeNames() {
		final List<ControlVocabularyTerm> esiNames = SupplyType.getInstance(cvManager).getPossibleValues();
		return getNames(esiNames);
	}

	public List<String> getMaldiDissociations() {
		final List<ControlVocabularyTerm> ret = MaldiDissociationMethod.getInstance(cvManager).getPossibleValues();
		return getNames(ret);
	}

	public List<String> getLaserTypes() {
		final List<ControlVocabularyTerm> ret = LaserType.getInstance(cvManager).getPossibleValues();
		return getNames(ret);
	}

	public List<String> getOtherIonSourceNames() {
		final List<ControlVocabularyTerm> ret = IonSourceName.getInstance(cvManager).getPossibleValues();
		return getNames(ret);
	}

	public List<String> getGasTypes() {
		final List<ControlVocabularyTerm> ret = GasType.getInstance(cvManager).getPossibleValues();
		return getNames(ret);
	}

	public List<String> getPressureUnits() {
		final List<ControlVocabularyTerm> ret = PressureUnit.getInstance(cvManager).getPossibleValues();
		return getNames(ret);
	}

	public List<String> getActivationTypes() {
		final List<ControlVocabularyTerm> ret = DissociationMethod.getInstance(cvManager).getPossibleValues();
		return getNames(ret);
	}

	public List<String> getSoftwareAcquisitionNames() {
		final List<ControlVocabularyTerm> ret = AcquisitionSoftware.getInstance(cvManager).getPossibleValues();
		return getNames(ret);
	}

	public List<String> getDataAnalysisSoftwareNames() {
		final List<ControlVocabularyTerm> ret = DataProcessingSoftware.getInstance(cvManager).getPossibleValues();
		return getNames(ret);
	}

	public List<String> getDataAnalysisDescription() {
		final List<ControlVocabularyTerm> ret = DataProcessingAction.getInstance(cvManager).getPossibleValues();
		return getNames(ret);
	}

	public List<String> getAddInformations() {
		if (!addInfos.isEmpty())
			return addInfos;
		final List<String> addInfos = getNames(AdditionalInformationName.getInstance(cvManager).getPossibleValues());
		final List<String> tissueNames = getTissueNames();
		final List<String> cellTypes = getCellTypesNames();
		final List<String> diseaseNames = getDisseasesNames();
		final List<String> samplePrepNames = getSampleProcessingSteps();
		final List<String> taxonomyNames = getTaxonomies();
		List<String> ret = new ArrayList<String>();

		// return all data that is not of these other ontologies
		for (String string : addInfos) {
			if (!tissueNames.contains(string) && !cellTypes.contains(string) && !diseaseNames.contains(string)
					&& !samplePrepNames.contains(string) && !taxonomyNames.contains(string))
				ret.add(string);
		}
		this.addInfos.addAll(ret);
		return ret;
	}

	public List<String> getSampleProcessingSteps() {
		final List<ControlVocabularyTerm> ret = SampleProcessingStep.getInstance(cvManager).getPossibleValues();
		return getNames(ret);
	}

	public List<String> getSampleProperties() {
		final List<ControlVocabularyTerm> ret = SampleInformation.getInstance(cvManager).getPossibleValues();
		return getNames(ret);
	}

	public List<String> getTissueNames() {
		final List<ControlVocabularyTerm> ret = TissuesTypes.getInstance(cvManager).getPossibleValues();
		return getNames(ret);
	}

	public List<String> getCellTypesNames() {
		final List<ControlVocabularyTerm> ret = CellTypes.getInstance(cvManager).getPossibleValues();
		return getNames(ret);
	}

	public List<String> getDisseasesNames() {
		final List<ControlVocabularyTerm> ret = HumanDisseases.getInstance(cvManager).getPossibleValues();
		return getNames(ret);
	}

	public List<String> getPositions() {
		final List<ControlVocabularyTerm> ret = ContactPositionMS.getInstance(cvManager).getPossibleValues();
		return getNames(ret);
	}

	public List<String> getSamplePlateType() {
		final List<ControlVocabularyTerm> ret = SamplePlateType.getInstance(cvManager).getPossibleValues();
		return getNames(ret);
	}

	public List<String> getTaxonomies() {
		final List<ControlVocabularyTerm> ret = MainTaxonomies.getInstance(cvManager).getPossibleValues();
		return getNames(ret);
	}
}
