package org.proteored.pacom.analysis.exporters;

import org.proteored.miapeapi.experiment.model.sort.ProteinGroupComparisonType;

public interface ExporterManager {

	public boolean isReplicateAndExperimentOriginIncluded();

	public boolean isDecoyHitsIncluded();

	public boolean isFDRApplied();

	public boolean showPeptides();

	public boolean isGeneInfoIncluded();

	public boolean showBestPeptides();

	public boolean showBestProteins();

	public boolean retrieveProteinSequences();

	public boolean isNonConclusiveProteinsIncluded();

	public boolean isDistinguishModifiedPeptides();

	/**
	 * @return
	 */
	public ProteinGroupComparisonType getComparisonType();

}
