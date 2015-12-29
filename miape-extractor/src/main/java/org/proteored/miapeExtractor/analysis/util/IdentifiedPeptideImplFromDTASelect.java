package org.proteored.miapeExtractor.analysis.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.proteored.miapeExtractor.analysis.gui.tasks.IdentificationSetFromFileParserTask;
import org.proteored.miapeExtractor.gui.tasks.OntologyLoaderTask;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.cv.ControlVocabularyTerm;
import org.proteored.miapeapi.cv.msi.Score;
import org.proteored.miapeapi.factories.msi.PeptideScoreBuilder;
import org.proteored.miapeapi.interfaces.msi.IdentifiedPeptide;
import org.proteored.miapeapi.interfaces.msi.IdentifiedProtein;
import org.proteored.miapeapi.interfaces.msi.InputData;
import org.proteored.miapeapi.interfaces.msi.PeptideModification;
import org.proteored.miapeapi.interfaces.msi.PeptideScore;

import edu.scripps.yates.dtaselectparser.util.DTASelectModification;
import edu.scripps.yates.dtaselectparser.util.DTASelectPSM;

public class IdentifiedPeptideImplFromDTASelect implements IdentifiedPeptide {
	private final DTASelectPSM dtaSelectPSM;
	private final ControlVocabularyManager cvManager = OntologyLoaderTask
			.getCvManager();
	private final List<IdentifiedProtein> proteins = new ArrayList<IdentifiedProtein>();
	private final int id;
	private static Integer seed;
	public final static Map<String, IdentifiedPeptide> map = new HashMap<String, IdentifiedPeptide>();

	public IdentifiedPeptideImplFromDTASelect(DTASelectPSM dtaSelectPSM) {
		this.dtaSelectPSM = dtaSelectPSM;
		id = getRandomInt();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IdentifiedPeptideImplFromDTASelect) {
			if (((IdentifiedPeptideImplFromDTASelect) obj).getSpectrumRef()
					.equals(getSpectrumRef())) {
				return true;
			}
		}
		return super.equals(obj);
	}

	private int getRandomInt() {

		if (seed == null) {
			Random generator = new Random();
			seed = generator.nextInt(Integer.MAX_VALUE);
		}
		seed = seed + 1;
		return seed;
	}

	@Override
	public int getId() {

		return id;
	}

	@Override
	public String getSequence() {
		if (dtaSelectPSM.getSequence() != null
				&& dtaSelectPSM.getSequence().getSequence() != null) {
			String seq = IdentificationSetFromFileParserTask
					.parseSequence(dtaSelectPSM.getSequence().getSequence());
			if (seq != null)
				return seq;
		}
		return IdentificationSetFromFileParserTask.parseSequence(dtaSelectPSM
				.getFullSequence());
	}

	@Override
	public Set<PeptideScore> getScores() {
		Set<PeptideScore> ret = new HashSet<PeptideScore>();
		final Double conf = dtaSelectPSM.getConf();
		if (conf != null) {
			PeptideScore score = new PeptideScoreBuilder("Conf%",
					conf.toString()).build();
			ret.add(score);
		}
		final Double deltacn = dtaSelectPSM.getDeltacn();
		if (deltacn != null) {
			ControlVocabularyTerm scoreTerm = null;
			if ("sequest".equalsIgnoreCase(dtaSelectPSM.getSearchEngine())) {
				Score.getInstance(cvManager);
				scoreTerm = Score.getSequestDeltaCNTerm(cvManager);
			} else if ("ProLuCID".equalsIgnoreCase(dtaSelectPSM
					.getSearchEngine())) {
				Score.getInstance(cvManager);
				scoreTerm = Score.getProLuCIDDeltaCNTerm(cvManager);
			}
			String scoreName = "DeltaCN";
			if (scoreTerm != null) {
				scoreName = scoreTerm.getPreferredName();
			}
			PeptideScore score = new PeptideScoreBuilder(scoreName,
					deltacn.toString()).build();
			ret.add(score);
		}
		final Double xcorr = dtaSelectPSM.getXcorr();
		if (xcorr != null) {
			ControlVocabularyTerm scoreTerm = null;
			if ("sequest".equalsIgnoreCase(dtaSelectPSM.getSearchEngine())) {
				Score.getInstance(cvManager);
				scoreTerm = Score.getSequestXCorrTerm(cvManager);
			} else if ("ProLuCID".equalsIgnoreCase(dtaSelectPSM
					.getSearchEngine())) {
				Score.getInstance(cvManager);
				scoreTerm = Score.getProLuCIDXCorrCNTerm(cvManager);
			}
			String scoreName = "XCorr";
			if (scoreTerm != null) {
				scoreName = scoreTerm.getPreferredName();
			}
			PeptideScore score = new PeptideScoreBuilder(scoreName,
					xcorr.toString()).build();
			ret.add(score);
		}
		if (dtaSelectPSM.getProb() != null) {
			PeptideScore score = new PeptideScoreBuilder("Prob", dtaSelectPSM
					.getProb().toString()).build();
			ret.add(score);
		}
		if (dtaSelectPSM.getProb_score() != null) {
			PeptideScore score = new PeptideScoreBuilder("Prob Score",
					dtaSelectPSM.getProb_score().toString()).build();
			ret.add(score);
		}
		if (dtaSelectPSM.getIonProportion() != null) {
			PeptideScore score = new PeptideScoreBuilder("Ion proportion",
					dtaSelectPSM.getIonProportion().toString()).build();
			ret.add(score);
		}
		if (dtaSelectPSM.getTotalIntensity() != null) {
			PeptideScore score = new PeptideScoreBuilder("Total Intensity",
					dtaSelectPSM.getTotalIntensity().toString()).build();
			ret.add(score);
		}
		return ret;
	}

	@Override
	public Set<PeptideModification> getModifications() {
		final List<DTASelectModification> modifications = dtaSelectPSM
				.getModifications();
		Set<PeptideModification> ret = new HashSet<PeptideModification>();
		if (modifications != null) {
			for (DTASelectModification dtaSelectPTM : modifications) {
				ret.add(new PeptideModificationImplFromDTASelect(dtaSelectPTM));
			}
		}
		return ret;
	}

	@Override
	public String getCharge() {
		return dtaSelectPSM.getCharge();
	}

	@Override
	public String getMassDesviation() {
		final Double ppmError = dtaSelectPSM.getPpmError();
		if (ppmError != null) {
			return ppmError.toString();
		}
		return null;
	}

	@Override
	public String getSpectrumRef() {
		return dtaSelectPSM.getPsmIdentifier();
	}

	@Override
	public InputData getInputData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRank() {

		return 1;
	}

	@Override
	public List<IdentifiedProtein> getIdentifiedProteins() {
		return proteins;
	}

	@Override
	public String getRetentionTimeInSeconds() {
		return null;
	}

}
